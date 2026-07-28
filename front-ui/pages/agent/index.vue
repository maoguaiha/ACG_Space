<script setup lang="ts">
/**
 * AI 助手聊天页—Nuxt 3 用户端入口。
 *
 * 布局：桌面端左侧会话侧边栏 + 右侧对话区域；移动端全屏对话 + 抽屉式侧边栏。
 *
 * 数据流：
 *   页面挂载 → 加载会话列表 → 若无会话则自动新建一个 →
 *   用户输入 → streamChat() → SSE 逐 token 追加到 streamingContent →
 *   流结束 → 将累积内容作为助手消息压入列表。
 *
 * 认证：auth middleware（未登录重定向 /login）。
 * 三主题：所有子组件通过 theme-* CSS 类 + CSS 变量自动适配。
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '~/stores/user'
import {
  streamChat,
  fetchConversations,
  createConversation,
  deleteConversation,
  type ConversationItem,
} from '~/composables/useAgentApi'

import ChatWindow from '~/components/agent/ChatWindow.vue'
import ChatInput from '~/components/agent/ChatInput.vue'
import ConversationList from '~/components/agent/ConversationList.vue'

// ==================== 路由 / 认证 ====================
definePageMeta({ middleware: ['auth'] })
useHead({ title: 'AI 助手 - ACG Space' })
const router = useRouter()
const userStore = useUserStore()

// ==================== 状态 ====================
interface LocalMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  isError?: boolean
}

const conversations = ref<ConversationItem[]>([])
const activeConversationId = ref<string | null>(null)
const messages = ref<LocalMessage[]>([])
const isStreaming = ref(false)
const streamingContent = ref('')
const abortController = ref<AbortController | null>(null)
const sidebarOpen = ref(false)
const loading = ref(true)

let msgSeq = 0
function nextMsgId(): string { return `${Date.now()}-${++msgSeq}` }

// ==================== 会话管理 ====================

async function loadConversations() {
  try {
    conversations.value = await fetchConversations()
    // 默认选中第一条，若无则新建
    if (conversations.value.length > 0) {
      activeConversationId.value = conversations.value[0].id
    } else {
      await handleCreateConv()
    }
  } catch (e) {
    console.error('加载会话列表失败', e)
    // 降级：直接新建一个
    await handleCreateConv()
  } finally {
    loading.value = false
  }
}

async function handleCreateConv() {
  try {
    const id = await createConversation()
    activeConversationId.value = id
    messages.value = []
    // 刷新列表
    conversations.value = await fetchConversations()
  } catch (e) {
    console.error('新建会话失败', e)
  }
}

async function handleSelectConv(id: string) {
  if (id === activeConversationId.value) return
  activeConversationId.value = id
  messages.value = [] // 切换会话时清空（历史由 Java 后端透传给 LLM）
}

async function handleDeleteConv(id: string) {
  try {
    await deleteConversation(id)
    conversations.value = conversations.value.filter(c => c.id !== id)
    if (activeConversationId.value === id) {
      // 当前会话被删，选第一条或新建
      if (conversations.value.length > 0) {
        activeConversationId.value = conversations.value[0].id
      } else {
        await handleCreateConv()
      }
      messages.value = []
    }
  } catch (e) {
    console.error('删除会话失败', e)
  }
}

// ==================== 对话 ====================

async function handleSend(content: string) {
  if (!content.trim() || isStreaming.value) return

  // 压入用户消息
  const userMsg: LocalMessage = { id: nextMsgId(), role: 'user', content }
  messages.value.push(userMsg)

  // 启动 SSE 流
  isStreaming.value = true
  streamingContent.value = ''
  abortController.value = new AbortController()

  try {
    await streamChat(
      content,
      activeConversationId.value,
      (token) => { streamingContent.value += token },
      (err) => {
        // Python 端报错（如配置缺失）—追加到流式内容末尾
        streamingContent.value += `\n\n⚠️ ${err}`
      },
      () => {
        // 流正常结束→将流式内容固化为一条助手消息
        const final = streamingContent.value
        if (final) {
          messages.value.push({ id: nextMsgId(), role: 'assistant', content: final })
        }
        streamingContent.value = ''
        isStreaming.value = false
      },
      abortController.value.signal,
    )
  } catch (e: any) {
    // AbortError = 用户手动停止，不显示错误
    if (e.name !== 'AbortError') {
      const errText = e.message || '服务异常'
      messages.value.push({ id: nextMsgId(), role: 'assistant', content: `❌ ${errText}`, isError: true })
    }
    isStreaming.value = false
    streamingContent.value = ''
  }
}

function handleStop() {
  abortController.value?.abort()
  // 将已接收的内容固化为助手消息
  const partial = streamingContent.value || '_（已停止）_'
  messages.value.push({ id: nextMsgId(), role: 'assistant', content: partial })
  streamingContent.value = ''
  isStreaming.value = false
}

/** 快捷问题发送（空态建议） */
function handleQuickSend(q: string) {
  handleSend(q)
}

// ==================== 生命周期 ====================
onMounted(() => { loadConversations() })
</script>

<template>
  <div
    class="h-[calc(100dvh-4rem)] flex flex-col overflow-hidden"
    :class="['theme-bg']"
  >
    <div class="flex flex-1 overflow-hidden">
      <!-- ===== 侧边栏（桌面固定 / 移动抽屉） ===== -->
      <div
        class="fixed inset-y-[4rem] left-0 z-40 w-64 transform transition-transform duration-200
               md:relative md:inset-auto md:translate-x-0"
        :class="sidebarOpen ? 'translate-x-0' : '-translate-x-full'"
      >
        <ConversationList
          :conversations="conversations"
          :active-id="activeConversationId"
          @select="handleSelectConv"
          @create="handleCreateConv"
          @delete="handleDeleteConv"
        />
      </div>

      <!-- 移动端遮罩 -->
      <div
        v-if="sidebarOpen"
        class="fixed inset-0 z-30 bg-black/40 md:hidden"
        @click="sidebarOpen = false"
      />

      <!-- ===== 对话主区域 ===== -->
      <div class="flex-1 flex flex-col min-w-0">
        <!-- 顶栏（主题适配） -->
        <div
          class="flex items-center gap-3 px-4 py-2.5 border-b flex-shrink-0"
          :class="['theme-border', 'theme-bg-secondary']"
        >
          <!-- 移动端侧边栏切换 -->
          <button
            class="md:hidden p-2 rounded-lg"
            :class="['theme-card']"
            @click="sidebarOpen = !sidebarOpen"
          >
            <svg class="w-5 h-5" :class="['theme-text-main']" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="3" y1="6" x2="21" y2="6" />
              <line x1="3" y1="12" x2="21" y2="12" />
              <line x1="3" y1="18" x2="21" y2="18" />
            </svg>
          </button>
          <h1 class="text-lg font-bold" :class="['theme-text-main']">AI 助手</h1>

          <!-- 右侧操作（返回首页等） -->
          <div class="flex-1" />
          <button
            class="hidden md:flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs transition-colors"
            :class="['theme-card', 'theme-card-hover', 'theme-text-muted']"
            @click="handleCreateConv"
          >
            <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19" />
              <line x1="5" y1="12" x2="19" y2="12" />
            </svg>
            新对话
          </button>
        </div>

        <!-- 加载骨架 -->
        <div v-if="loading" class="flex-1 flex items-center justify-center">
          <div class="animate-spin w-6 h-6 rounded-full border-2 border-transparent border-t-current" :class="['theme-text-muted']" />
        </div>

        <!-- 对话区域 -->
        <template v-else>
          <ChatWindow
            :messages="messages"
            :has-streaming="isStreaming"
            :streaming-content="streamingContent"
            @quick-send="handleQuickSend"
          />
          <ChatInput
            :disabled="false"
            :is-streaming="isStreaming"
            placeholder="输入你的问题..."
            @send="handleSend"
            @stop="handleStop"
          />
        </template>
      </div>
    </div>
  </div>
</template>
