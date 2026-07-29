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

/** 当前驱动模型（与后端 / python-agent 配置保持一致） */
const MODEL_LABEL = 'LongCat-2.0'

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
    <div class="max-w-3xl mx-auto">
      <div class="flex items-end gap-2">
        <!-- 输入框胶囊：清空按钮 + 输入框聚合为统一外底板（任务 3） -->
        <div class="agent-input-capsule flex-1 flex items-start rounded-2xl px-1 py-1">
          <!-- 清除上下文（胶囊内左侧，不再是孤岛） -->
          <button
            type="button"
            @click="emit('clearContext')"
            :disabled="isStreaming"
            class="agent-clear-btn flex-shrink-0 p-3 rounded-xl transition-colors disabled:opacity-40"
            :class="['theme-text-muted']"
            title="开启新话题（清空对话记忆）"
          >
            <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="3 6 5 6 21 6" />
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
              <line x1="11" y1="11" x2="11" y2="17" />
              <line x1="15" y1="11" x2="15" y2="17" />
            </svg>
          </button>

          <!-- 输入框 + 附件占位 + 底部状态挂件 -->
          <div class="relative flex-1">
            <!-- 附件占位（输入框内部左上，多模态能力预留） -->
            <button
              type="button"
              @click="handleAttach"
              class="absolute left-3 top-[13px] p-1 rounded-lg transition-colors"
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
              class="agent-textarea w-full resize-none rounded-xl pl-10 pr-4 pt-3 text-sm leading-relaxed focus:outline-none transition-colors placeholder:opacity-40"
              :class="['theme-text-main']"
              style="min-height: 54px; max-height: 200px; padding-bottom: 36px"
            />

            <!-- 底部状态挂件：网络状态 + 模型（不遮挡文字，textarea 已留 padding-bottom） -->
            <div class="agent-input-status">
              <span class="flex items-center gap-1">
                <span class="agent-status-dot" />
                已连接
              </span>
              <span class="opacity-40">·</span>
              <span>{{ MODEL_LABEL }}</span>
            </div>
          </div>
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
  </div>
</template>

<style scoped>
/* 底部状态挂件：绝对定位于输入框左下角，pointer-events:none 不挡输入 */
.agent-input-status {
  position: absolute;
  bottom: 7px;
  left: 40px; /* 对齐 textarea 的 pl-10 */
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  line-height: 1;
  color: var(--text-muted);
  pointer-events: none;
  user-select: none;
}

/* 连接状态小绿点 */
.agent-status-dot {
  width: 6px;
  height: 6px;
  border-radius: 9999px;
  background-color: #22c55e;
  box-shadow: 0 0 0 2px rgba(34, 197, 94, 0.2);
}
</style>
