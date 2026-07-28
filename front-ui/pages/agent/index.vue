<script setup lang="ts">
/**
 * AI 助手聊天页—Nuxt 3 用户端入口。
 *
 * 布局：桌面端左侧会话侧边栏（可折叠）+ 右侧对话区域；移动端全屏对话 + 抽屉式侧边栏。
 *   右侧聊天内容（消息列表 + 输入框）包裹在 max-w-3xl 容器内居中，避免大屏单行过长。
 *
 * 数据流：
 *   页面挂载 → 加载会话列表 → 若无会话则自动新建一个 →
 *   用户输入 → streamChat() → SSE 逐 token 追加到 streamingContent →
 *   流结束 → 将累积内容作为助手消息压入列表。
 *
 * 认证：auth middleware（未登录重定向 /login）。
 * 三主题：所有子组件通过 theme-* CSS 类 + CSS 变量自动适配。
 */
import { ref, computed, onMounted } from 'vue'
import {
  streamChat,
  fetchConversations,
  createConversation,
  deleteConversation,
  renameConversation,
  clearAllConversations,
  type ConversationItem,
} from '~/composables/useAgentApi'
import { useAppStore } from '~/stores/app'

import ChatWindow from '~/components/agent/ChatWindow.vue'
import ChatInput from '~/components/agent/ChatInput.vue'
import ConversationList from '~/components/agent/ConversationList.vue'

// ==================== 路由 / 认证 ====================
definePageMeta({ middleware: ['auth'] })
useHead({ title: 'AI 助手 - ACG Space' })

const appStore = useAppStore()

/** 当前驱动模型（与 python-agent 配置保持一致） */
const MODEL_LABEL = 'LongCat-2.0'

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
const sidebarCollapsed = ref(false)
const loading = ref(true)

/** 当前会话标题（顶栏居中显示） */
const activeTitle = computed(() => {
  const conv = conversations.value.find(c => c.id === activeConversationId.value)
  return conv?.title || '新的对话'
})

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

/** 重命名会话（侧边栏编辑标题） */
async function handleRenameConv(id: string, title: string) {
  try {
    await renameConversation(id, title)
    const conv = conversations.value.find(c => c.id === id)
    if (conv) conv.title = title
  } catch (e) {
    console.error('重命名会话失败', e)
    appStore.setMessage('重命名失败，请重试', 'error')
  }
}

/** 清空所有会话 */
async function handleClearAll() {
  try {
    await clearAllConversations()
    conversations.value = []
    await handleCreateConv()
    appStore.setMessage('已清除所有对话', 'success')
  } catch (e) {
    console.error('清空会话失败', e)
    appStore.setMessage('清除失败，请重试', 'error')
  }
}

/** AI 设置（占位，后续可扩展 System Prompt / 模型选择） */
function handleOpenSettings() {
  appStore.setMessage('AI 设置功能即将上线', 'info')
}

/** 清除上下文 / 开启新话题（重置 AI 记忆） */
function handleClearContext() {
  if (isStreaming.value) return
  messages.value = []
  // 开启一个全新会话即重置记忆（当前架构记忆与会话绑定）
  handleCreateConv()
}

// ==================== 对话 ====================

async function handleSend(content: string) {
  if (!content.trim() || isStreaming.value) return

  // 乐观更新：立即把用户消息渲染到列表（不等待接口返回）
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
      <!-- ===== 侧边栏（桌面固定可折叠 / 移动抽屉） ===== -->
      <div
        class="fixed inset-y-[4rem] left-0 z-40 transform overflow-hidden transition-[width,transform] duration-200 ease-in-out
               md:relative md:inset-auto md:translate-x-0"
        :class="[
          'w-[270px]',
          sidebarOpen ? 'translate-x-0' : '-translate-x-full',
          sidebarCollapsed ? 'md:w-0 md:min-w-0' : 'md:w-[270px]'
        ]"
      >
        <ConversationList
          :conversations="conversations"
          :active-id="activeConversationId"
          @select="handleSelectConv"
          @create="handleCreateConv"
          @delete="handleDeleteConv"
          @rename="handleRenameConv"
          @clear-all="handleClearAll"
          @open-settings="handleOpenSettings"
        />
      </div>

      <!-- 移动端遮罩 -->
      <div
        v-if="sidebarOpen"
        class="fixed inset-0 z-30 bg-black/40 md:hidden"
        @click="sidebarOpen = false"
      />

      <!-- ===== 对话主区域 ===== -->
      <div class="flex-1 flex flex-col min-w-0 relative">
        <!-- 顶栏（~50px）：毛玻璃叠加层，聊天上滚时透视模糊（任务 2） -->
        <header
          class="agent-header absolute top-0 left-0 right-0 z-30 flex items-center gap-2 px-4 h-12"
        >
          <!-- 移动端侧边栏切换 -->
          <button
            class="md:hidden p-2 -ml-2 rounded-lg"
            :class="['theme-card']"
            @click="sidebarOpen = !sidebarOpen"
          >
            <svg class="w-5 h-5" :class="['theme-text-main']" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="3" y1="6" x2="21" y2="6" />
              <line x1="3" y1="12" x2="21" y2="12" />
              <line x1="3" y1="18" x2="21" y2="18" />
            </svg>
          </button>

          <!-- 桌面端：收起 / 展开侧边栏 -->
          <button
            class="hidden md:flex p-2 -ml-2 rounded-lg transition-colors"
            :class="['theme-card', 'theme-card-hover', 'theme-text-main']"
            :title="sidebarCollapsed ? '展开侧边栏' : '收起侧边栏'"
            @click="sidebarCollapsed = !sidebarCollapsed"
          >
            <svg v-if="!sidebarCollapsed" class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="11 17 6 12 11 7" />
              <polyline points="18 17 13 12 18 7" />
            </svg>
            <svg v-else class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="13 17 18 12 13 7" />
              <polyline points="6 17 11 12 6 7" />
            </svg>
          </button>

          <!-- 当前会话标题（居中） -->
          <h1 class="flex-1 text-center text-base font-bold truncate" :class="['theme-text-main']">
            {{ activeTitle }}
          </h1>

          <!-- 驱动模型标签 -->
          <span
            class="hidden sm:inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium"
            :class="['theme-card', 'theme-text-muted']"
          >
            ✨ 驱动核心: {{ MODEL_LABEL }}
          </span>

          <!-- 新对话 -->
          <button
            class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs transition-colors"
            :class="['theme-card', 'theme-card-hover', 'theme-text-muted']"
            @click="handleCreateConv"
          >
            <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19" />
              <line x1="5" y1="12" x2="19" y2="12" />
            </svg>
            新对话
          </button>
        </header>

        <!-- 加载骨架 -->
        <div v-if="loading" class="flex-1 flex items-center justify-center pt-12">
          <div class="animate-spin w-6 h-6 rounded-full border-2 border-transparent border-t-current" :class="['theme-text-muted']" />
        </div>

        <!-- 对话区域（max-w-3xl 居中，避免大屏单行过长） -->
        <template v-else>
          <div class="flex-1 min-h-0 w-full max-w-3xl mx-auto flex flex-col">
            <ChatWindow
              class="agent-chat-scroll"
              :messages="messages"
              :has-streaming="isStreaming"
              :streaming-content="streamingContent"
              @quick-send="handleQuickSend"
            />
            <ChatInput
              :disabled="false"
              :is-streaming="isStreaming"
              placeholder="输入你的问题，Enter 发送 / Shift+Enter 换行"
              @send="handleSend"
              @stop="handleStop"
              @clear-context="handleClearContext"
            />
          </div>
        </template>
      </div>
    </div>
  </div>
</template>
