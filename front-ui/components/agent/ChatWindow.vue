<script setup lang="ts">
/**
 * 消息列表 + 自动滚动底部的聊天窗口。
 *
 * 特性：
 *   - 空态：首次进入显示欢迎引导 + 快捷提示词
 *   - 已有消息：最大宽度 3xl 居中，列表从 ChatMessage 组件渲染
 *   - 思考中：流式尚未返回首个 token 时，显示三点跳动指示器 + 文案
 *   - 流式进行中：追加一条"正在生成"的助手气泡（闪烁光标）
 *   - 自动滚动：新消息到达或 token 追加时，smooth 滚动到锚点
 */
import { ref, watch, nextTick } from 'vue'

const props = defineProps<{
  messages: Array<{
    id: string
    role: 'user' | 'assistant'
    content: string
    isError?: boolean
  }>
  hasStreaming: boolean
  streamingContent: string
}>()

const emit = defineEmits<{
  'quick-send': [q: string]
}>()

const anchorRef = ref<HTMLElement>()

/** 是否处于"等待首个 token"的思考状态 */
const isThinking = () => props.hasStreaming && props.streamingContent.length === 0

/** 滚动到消息列表底部 */
function scrollToBottom() {
  nextTick(() => {
    anchorRef.value?.scrollIntoView({ behavior: 'smooth' })
  })
}

// 新消息 / token 追加 → 自动滚动
watch(() => props.messages.length, scrollToBottom)
watch(() => props.streamingContent, scrollToBottom)
</script>

<template>
  <div class="agent-chat-scroll flex-1 overflow-y-auto px-4 pt-14 pb-2">
    <!-- 空态：无消息且无流式  -->
    <div
      v-if="messages.length === 0 && !hasStreaming"
      class="agent-empty-state h-full flex flex-col items-center justify-center text-center px-4"
    >
      <div
        class="w-16 h-16 rounded-full flex items-center justify-center mb-4"
        :class="['theme-primary-bg']"
      >
        <svg class="w-8 h-8 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2z" />
        </svg>
      </div>
      <p class="text-xl font-semibold" :class="['theme-text-main']">你好！我是 ACG Space AI 助手</p>
      <p class="text-sm mt-2" :class="['theme-text-muted']">
        有什么我可以帮你的？可以问我平台玩法、抽赏保底、碎片合成、番剧推荐等问题。
      </p>
      <div class="mt-5 flex flex-wrap justify-center gap-2 max-w-lg">
        <button
          v-for="q in ['抽赏保底机制是什么？', '如何兑换实物奖品？', '推荐几部机战番', '如何合成碎片？']"
          :key="q"
          class="px-3 py-1.5 rounded-full text-xs transition-colors"
          :class="['theme-card', 'theme-card-hover', 'theme-text-muted']"
          @click="emit('quick-send', q)"
        >
          {{ q }}
        </button>
      </div>
    </div>

    <!-- 消息列表 -->
    <div class="max-w-3xl mx-auto">
      <ChatMessage
        v-for="msg in messages"
        :key="msg.id"
        :message="msg"
      />

      <!-- 思考中：等待首个 token -->
      <div v-if="hasStreaming && streamingContent.length === 0" class="flex gap-3 py-3 justify-start">
        <div
          class="flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold select-none"
          :class="['theme-primary-bg', 'text-white']"
        >
          AI
        </div>
        <div
          class="max-w-[80%] px-4 py-3 rounded-2xl flex items-center gap-2"
          :class="['theme-card', 'theme-text-muted']"
        >
          <span class="flex gap-1">
            <span class="w-2 h-2 rounded-full bg-current typing-dot" style="animation-delay: 0ms" />
            <span class="w-2 h-2 rounded-full bg-current typing-dot" style="animation-delay: 150ms" />
            <span class="w-2 h-2 rounded-full bg-current typing-dot" style="animation-delay: 300ms" />
          </span>
          <span class="text-sm">AI 助手正在思考...</span>
        </div>
      </div>

      <!-- 流式生成中（已有 token）：就地渲染内容 -->
      <div v-else-if="hasStreaming" class="flex gap-3 py-3 justify-start">
        <div
          class="flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold select-none"
          :class="['theme-primary-bg', 'text-white']"
        >
          AI
        </div>
        <div
          class="max-w-[80%] px-4 py-3 rounded-2xl whitespace-pre-wrap break-words text-sm leading-relaxed"
          :class="['theme-card', 'theme-text-main']"
        >
          {{ streamingContent }}<span class="animate-pulse select-none">|</span>
        </div>
      </div>
    </div>

    <!-- 锚点：自动滚动至此 -->
    <div ref="anchorRef" class="h-1" />
  </div>
</template>

<style scoped>
.typing-dot {
  display: inline-block;
  animation: typing-bounce 1.2s infinite ease-in-out;
}
@keyframes typing-bounce {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-4px);
    opacity: 1;
  }
}
</style>
