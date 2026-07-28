<script setup lang="ts">
/**
 * 聊天输入组件——三主题适配。
 *
 * 交互：
 *   - Enter 发送（Shift+Enter 换行）
 *   - 输入框左侧外：🧹 清除上下文 / 开启新话题
 *   - 输入框内左侧：📎 附件占位（多模态能力预留）
 *   - 发送按钮仅在内容非空且未流式时可用；流式中显示"停止生成"
 *   - textarea 自适应高度（基础 ~44px，最高 200px 后内部滚动）
 */
import { nextTick, ref, watch } from 'vue'

const props = defineProps<{
  disabled?: boolean
  isStreaming?: boolean
  placeholder?: string
}>()

const emit = defineEmits<{
  send: [content: string]
  stop: []
  clearContext: []
}>()

const textareaRef = ref<HTMLTextAreaElement>()
const inputText = ref('')

/** 发送消息 */
function handleSend() {
  const text = inputText.value.trim()
  if (!text || props.disabled || props.isStreaming) return
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

/** 附件占位（多模态能力预留） */
function handleAttach() {
  // TODO: 后续接入图片/文件上传与多模态分析
}

/** textarea 自适应高度（44px ~ 200px） */
watch(inputText, () => {
  nextTick(() => {
    const el = textareaRef.value
    if (!el) return
    el.style.height = 'auto'
    el.style.height = Math.min(el.scrollHeight, 200) + 'px'
  })
})
</script>

<template>
  <div class="border-t px-4 py-3" :class="['theme-border', 'theme-bg-secondary']">
    <div class="max-w-3xl mx-auto flex items-end gap-2">
      <!-- 清除上下文（输入框外部左侧） -->
      <button
        type="button"
        @click="emit('clearContext')"
        :disabled="isStreaming"
        class="flex-shrink-0 p-3 rounded-xl transition-colors disabled:opacity-40"
        :class="['theme-card', 'theme-card-hover', 'theme-text-muted']"
        title="开启新话题（清空对话记忆）"
      >
        <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="3 6 5 6 21 6" />
          <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
          <line x1="11" y1="11" x2="11" y2="17" />
          <line x1="15" y1="11" x2="15" y2="17" />
        </svg>
      </button>

      <!-- 输入框 + 附件占位 -->
      <div class="relative flex-1">
        <!-- 附件占位（输入框内部左侧，多模态能力预留） -->
        <button
          type="button"
          @click="handleAttach"
          class="absolute left-3 bottom-3 p-1 rounded-lg transition-colors"
          :class="['theme-text-muted', 'hover:theme-text-main']"
          title="上传图片 / 文件（即将支持）"
        >
          <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48" />
          </svg>
        </button>

        <textarea
          ref="textareaRef"
          v-model="inputText"
          :placeholder="placeholder || '输入你的问题...'"
          :disabled="disabled"
          rows="1"
          @keydown="handleKeydown"
          class="w-full resize-none rounded-xl pl-10 pr-4 py-3 text-sm leading-relaxed focus:outline-none transition-colors placeholder:opacity-40"
          :class="['theme-input', 'theme-text-main']"
          style="min-height: 44px; max-height: 200px"
        />
      </div>

      <!-- 流式生成中 → 停止按钮 -->
      <button
        v-if="isStreaming"
        type="button"
        @click="emit('stop')"
        class="flex-shrink-0 p-3 rounded-xl transition-colors"
        :class="['bg-red-500/20', 'text-red-400', 'hover:bg-red-500/30']"
        title="停止生成"
      >
        <svg class="w-5 h-5" viewBox="0 0 24 24" fill="currentColor">
          <rect x="4" y="4" width="16" height="16" rx="2" />
        </svg>
      </button>

      <!-- 非流式 → 发送按钮（空闲/禁用互斥，防止重复发送） -->
      <button
        v-else
        type="button"
        @click="handleSend"
        :disabled="!inputText.trim() || disabled || isStreaming"
        class="flex-shrink-0 p-3 rounded-xl transition-all disabled:opacity-40 disabled:cursor-not-allowed"
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
