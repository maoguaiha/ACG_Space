<template>
  <div class="min-h-screen" :class="['theme-bg']">
    <div class="container mx-auto px-4 py-6">
      <div class="max-w-2xl mx-auto">
        <div class="flex items-center justify-between mb-6">
          <div class="flex items-center gap-4">
            <NuxtLink to="/" class="p-2 rounded-xl transition-colors" :class="['theme-btn-back']">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="m15 18-6-6 6-6"/>
              </svg>
            </NuxtLink>
            <h1 class="text-2xl font-bold" :class="['theme-text-main']">我的消息</h1>
          </div>
        </div>

        <div v-if="loading" class="space-y-3">
          <div v-for="i in 5" :key="i" class="animate-pulse rounded-2xl p-4" :class="['theme-card']">
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 rounded-full" :class="['theme-card-alt']"></div>
              <div class="flex-1">
                <div class="h-4 rounded w-24 mb-2" :class="['theme-card-alt']"></div>
                <div class="h-3 rounded w-48" :class="['theme-card-alt']"></div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="conversations.length === 0" class="text-center py-20">
          <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="mx-auto mb-4" :class="['theme-text-muted']">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          <p :class="['theme-text-muted']">暂无消息</p>
          <p class="text-sm mt-2" :class="['theme-text-muted']">快去和其他用户聊天吧！</p>
        </div>

        <div v-else class="space-y-2">
          <NuxtLink
            v-for="conv in conversations"
            :key="conv.userId"
            :to="`/message/${conv.userId}`"
            class="flex items-center gap-3 p-4 rounded-2xl border transition-colors"
            :class="['theme-card', 'theme-card-hover']"
          >
            <div class="relative">
              <img v-if="conv.avatar" :src="conv.avatar" class="w-12 h-12 rounded-full object-cover" />
              <div v-else class="w-12 h-12 rounded-full flex items-center justify-center text-white font-bold text-lg" :class="['theme-avatar-placeholder']">
                {{ (conv.nickname || conv.username || '?')[0] }}
              </div>
              <span v-if="conv.unreadCount > 0" class="absolute -top-1 -right-1 w-5 h-5 bg-rose-500 text-white text-xs font-bold rounded-full flex items-center justify-center">
                {{ conv.unreadCount > 9 ? '9+' : conv.unreadCount }}
              </span>
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center justify-between mb-1">
                <span class="font-bold" :class="['theme-text-main']">{{ conv.nickname || conv.username }}</span>
                <span class="text-xs" :class="['theme-text-muted']">{{ formatTime(conv.lastMessageTime) }}</span>
              </div>
              <p class="text-sm truncate" :class="['theme-text-muted']">{{ conv.lastMessage }}</p>
            </div>
          </NuxtLink>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '~/stores/user'
import { fetchConversationList, type ConversationVO } from '~/composables/useApi'

const userStore = useUserStore()
const loading = ref(true)
const conversations = ref<ConversationVO[]>([])

if (!userStore.isLoggedIn) {
  navigateTo('/login')
}

function formatTime(dateStr: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

async function loadConversations() {
  loading.value = true
  try {
    conversations.value = await fetchConversationList()
  } catch (e) {
    console.error('加载消息列表失败:', e)
    conversations.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadConversations()
})

useHead({
  title: '消息 - ACG Space'
})
</script>
