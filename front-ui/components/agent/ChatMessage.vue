<script setup lang="ts">
/**
 * AI 助手单条消息气泡——三主题适配（CSS 变量 + theme-* 类）。
 *
 * 类型：
 *   - 用户消息：右上对齐，theme-primary-bg 渐变背景
 *   - 助手消息：左上对齐，theme-card 卡片背景 + AI 头像
 *   - 错误消息：红色半透明背景
 */
import { computed } from 'vue'

const props = defineProps<{
  message: {
    id: string
    role: 'user' | 'assistant'
    content: string
    isError?: boolean
  }
}>()

const bubbleClass = computed(() => {
  if (props.message.role === 'user') return ['theme-user-bubble']
  if (props.message.isError) return ['bg-red-500/10', 'border', 'border-red-500/30', 'text-red-400']
  return ['theme-card', 'theme-text-main']
})
</script>

<template>
  <div
    class="flex gap-3 py-3"
    :class="message.role === 'user' ? 'justify-end' : 'justify-start'"
  >
    <!-- 助手头像 -->
    <div
      v-if="message.role === 'assistant'"
      class="flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold select-none"
      :class="['theme-primary-bg', 'text-white']"
    >
      AI
    </div>

    <!-- 消息气泡 -->
    <div
      class="max-w-[80%] px-4 py-3 rounded-2xl whitespace-pre-wrap break-words text-sm leading-relaxed"
      :class="bubbleClass"
    >
      {{ message.content }}
    </div>

    <!-- 用户头像占位 -->
    <div
      v-if="message.role === 'user'"
      class="flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center select-none"
      :class="['theme-card', 'theme-text-muted']"
    >
      <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
        <circle cx="12" cy="7" r="4" />
      </svg>
    </div>
  </div>
</template>
