<script setup lang="ts">
/**
 * 消息列表 + 自动滚动底部的聊天窗口。
 *
 * 特性：
 *   - 空态：首次进入显示欢迎引导
 *   - 已有消息：最大宽度 3xl 居中，列表从 ChatMessage 组件渲染
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
  <div class="flex-1 overflow-y-auto px-4 py-2">
    <!-- 空态：无消息且无流式  -->
    <div
      v-if="messages.length === 0 && !hasStreaming"
      class="h-full flex flex-col items-center justify-center"
    >
      <div
        class="w-16 h-16 rounded-full flex items-center justify-center mb-4"
        :class="['theme-primary-bg']"
      >
        <svg class="w-8 h-8 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2z" />
        </svg>
      </div>
      <p class="text-lg font-semibold" :class="['theme-text-main']">ACG Space AI 助手</p>
      <p class="text-sm mt-2 text-center" :class="['theme-text-muted']">
        可以问我平台玩法、抽赏保底、碎片合成、
        <br />番剧推荐等问题
      </p>
      <div class="mt-4 flex flex-wrap justify-center gap-2">
        <button
          v-for="q in ['抽赏保底机制是什么？', '如何兑换实物奖品？', '推荐几部机战番']"
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

      <!-- 流式生成中的助手气泡 -->
      <div v-if="hasStreaming" class="flex gap-3 py-3 justify-start">
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
