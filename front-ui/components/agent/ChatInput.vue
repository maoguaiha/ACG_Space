<script setup lang="ts">
/**
 * 聊天输入组件——三主题适配。
 *
 * 交互：
 *   - Enter 发送（Shift+Enter 换行）
 *   - 发送按钮仅当有内容且未禁用时可用
 *   - 流式生成中显示"停止"按钮替代"发送"
 *   - textarea 自动撑高（最大 120px → 滚动）
 */
import { ref, watch } from 'vue'

const props = defineProps<{
  disabled?: boolean
  isStreaming?: boolean
  placeholder?: string
}>()

const emit = defineEmits<{
  send: [content: string]
  stop: []
}>()

const textareaRef = ref<HTMLTextAreaElement>()
const inputText = ref('')

/** 发送消息 */
function handleSend() {
  const text = inputText.value.trim()
  if (!text || props.disabled) return
  emit('send', text)
  inputText.value = ''
}

/** Shift+Enter 换行 */
function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

/** textarea 自动撑高 */
watch(inputText, () => {
  nextTick(() => {
    const el = textareaRef.value
    if (!el) return
    el.style.height = 'auto'
    el.style.height = Math.min(el.scrollHeight, 120) + 'px'
  })
})

import { nextTick } from 'vue'
</script>

<template>
  <div class="border-t px-4 py-3" :class="['theme-border', 'theme-bg-secondary']">
    <div class="max-w-3xl mx-auto flex items-end gap-2">
      <textarea
        ref="textareaRef"
        v-model="inputText"
        :placeholder="placeholder || '输入你的问题...'"
        :disabled="disabled"
        rows="1"
        @keydown="handleKeydown"
        class="flex-1 resize-none rounded-xl px-4 py-3 text-sm leading-relaxed focus:outline-none transition-colors placeholder:opacity-40"
        :class="['theme-input', 'theme-text-main']"
        style="max-height: 120px"
      />

      <!-- 流式生成中 → 停止按钮 -->
      <button
        v-if="isStreaming"
        @click="emit('stop')"
        class="flex-shrink-0 p-3 rounded-xl transition-colors"
        :class="['bg-red-500/20', 'text-red-400', 'hover:bg-red-500/30']"
        title="停止生成"
      >
        <svg class="w-5 h-5" viewBox="0 0 24 24" fill="currentColor">
          <rect x="4" y="4" width="16" height="16" rx="2" />
        </svg>
      </button>

      <!-- 非流式 → 发送按钮 -->
      <button
        v-else
        @click="handleSend"
        :disabled="!inputText.trim() || disabled"
        class="flex-shrink-0 p-3 rounded-xl transition-all disabled:opacity-40"
        :class="['theme-primary-bg', 'text-white']"
        title="发送"
      >
        <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="22" y1="2" x2="11" y2="13" />
          <polygon points="22 2 15 22 11 13 2 9 22 2" />
        </svg>
      </button>
    </div>
  </div>
</template>
