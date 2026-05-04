<template>
  <div class="fixed inset-0 flex flex-col overflow-hidden" :class="['theme-chat-bg']">
    <div class="flex-shrink-0 border-b" :class="['theme-chat-header']">
      <div class="px-4 py-4">
        <div class="flex items-center gap-4 max-w-4xl mx-auto">
          <NuxtLink to="/messages" class="p-2 rounded-xl transition-colors" :class="['theme-btn-back']">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="m15 18-6-6 6-6"/>
            </svg>
          </NuxtLink>
          <NuxtLink :to="`/user/${route.params.userId}`" class="flex items-center gap-3 hover:opacity-80 transition-opacity">
            <div class="w-11 h-11 rounded-full overflow-hidden flex-shrink-0 ring-2" :class="['theme-avatar-ring']">
              <img v-if="otherUser?.avatar" :src="otherUser.avatar" class="w-full h-full object-cover" />
              <div v-else class="w-full h-full flex items-center justify-center text-white font-bold text-lg" :class="['theme-avatar-placeholder']">
                {{ (otherUser?.nickname || otherUser?.username || '?')[0] }}
              </div>
            </div>
            <div>
              <span class="font-bold" :class="['theme-text-main']">{{ otherUser?.nickname || otherUser?.username }}</span>
              <div class="flex items-center gap-1">
                <span class="w-2 h-2 bg-green-500 rounded-full"></span>
                <span class="text-xs" :class="['theme-text-muted']">在线</span>
              </div>
            </div>
          </NuxtLink>
        </div>
      </div>
    </div>

    <div class="flex-1 flex flex-col overflow-hidden">
      <div class="px-4 flex-1 flex flex-col min-h-0">
        <div class="flex-1 flex flex-col min-h-0 max-w-4xl mx-auto w-full">
          <!-- 聊天记录区域 - 可滚动 -->
          <div ref="messageListRef" class="flex-1 overflow-y-auto py-4 space-y-5 min-h-0">
            <div v-if="loading" class="flex justify-center py-8">
              <div class="w-8 h-8 border-4 border-indigo-500/30 border-t-indigo-500 rounded-full animate-spin"></div>
            </div>

            <div v-else-if="messages.length === 0" class="flex items-center justify-center h-full">
              <div class="text-center" :class="['theme-text-muted']">
                还没有消息记录，开始聊天吧！
              </div>
            </div>

            <div v-else class="py-2">
              <div
                v-for="(msg, index) in messages"
                :key="msg.id"
                class="flex gap-4 mb-5"
                :class="getCurrentUserId() === msg.fromUserId ? 'flex-row-reverse' : ''"
              >
                <div class="w-10 h-10 rounded-full overflow-hidden flex-shrink-0">
                  <img
                    v-if="getCurrentUserId() === msg.fromUserId && userStore.userInfo?.avatar"
                    :src="userStore.userInfo.avatar"
                    class="w-full h-full object-cover"
                  />
                  <div v-else-if="getCurrentUserId() === msg.fromUserId"
                    class="w-full h-full flex items-center justify-center text-white font-bold text-lg" :class="['theme-avatar-placeholder']">
                    {{ (userStore.userInfo?.nickname || userStore.userInfo?.username || '?')[0] }}
                  </div>
                  <img v-else-if="otherUser?.avatar" :src="otherUser.avatar" class="w-full h-full object-cover" />
                  <div v-else class="w-full h-full flex items-center justify-center text-white font-bold text-lg" :class="['theme-avatar-placeholder']">
                    {{ (otherUser?.nickname || otherUser?.username || '?')[0] }}
                  </div>
                </div>
                <div
                  class="max-w-[60%] px-5 py-3 rounded-2xl shadow-lg"
                  :class="getCurrentUserId() === msg.fromUserId ? 'theme-chat-message-self' : 'theme-chat-message-other'"
                >
                  <p class="text-sm leading-relaxed">{{ msg.content }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- 输入框区域 -->
          <div class="py-4 pb-6 px-4 flex-shrink-0">
            <div class="flex gap-3 max-w-4xl mx-auto">
              <input
                v-model="inputMessage"
                @keyup.enter="sendMessage"
                placeholder="输入消息..."
                class="flex-1 border rounded-2xl px-5 py-3.5 focus:outline-none focus:ring-2 transition-all"
                :class="['theme-chat-input']"
                maxlength="500"
              />
              <button
                @click="sendMessage"
                :disabled="!inputMessage.trim() || sending"
                class="px-7 py-3.5 disabled:opacity-50 disabled:cursor-not-allowed font-medium rounded-2xl transition-all shadow-lg"
                :class="['theme-btn-send']"
              >
                {{ sending ? '发送中...' : '发送' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { useUserStore } from '~/stores/user'
import { fetchConversation, sendMessage as apiSendMessage, markMessagesRead, fetchUserProfile, type MessageVO } from '~/composables/useApi'

const route = useRoute()
const userStore = useUserStore()

const otherUser = ref<any>(null)
const messages = ref<MessageVO[]>([])
const loading = ref(true)
const sending = ref(false)
const inputMessage = ref('')
const messageListRef = ref<HTMLElement>()

function getCurrentUserId(): string {
  return String(userStore.userInfo?.id || '')
}

if (!userStore.isLoggedIn) {
  navigateTo('/login')
}

function formatTime(dateStr: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

async function loadProfile() {
  try {
    otherUser.value = await fetchUserProfile(route.params.userId as string)
  } catch (e) {
    console.error('加载用户信息失败:', e)
  }
}

async function loadMessages() {
  loading.value = true
  try {
    messages.value = await fetchConversation(route.params.userId as string)
    messages.value.reverse()
    await nextTick()
    scrollToBottom()
  } catch (e) {
    console.error('加载消息失败:', e)
    messages.value = []
  } finally {
    loading.value = false
  }
}

async function markRead() {
  try {
    await markMessagesRead(route.params.userId as string)
  } catch (e) {
    console.error('标记已读失败:', e)
  }
}

function scrollToBottom() {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

async function sendMessage() {
  const content = inputMessage.value.trim()
  if (!content || sending.value) return

  sending.value = true
  try {
    await apiSendMessage(route.params.userId as string, content)
    inputMessage.value = ''
    await loadMessages()
  } catch (e) {
    console.error('发送消息失败:', e)
  } finally {
    sending.value = false
  }
}

onMounted(async () => {
  await loadProfile()
  await loadMessages()
  await markRead()
})

watch(() => route.params.userId, async () => {
  if (route.params.userId) {
    await loadProfile()
    await loadMessages()
    await markRead()
  }
})

useHead({
  title: otherUser.value?.nickname ? `${otherUser.value.nickname} - 私信 - ACG Space` : '私信 - ACG Space'
})
</script>
