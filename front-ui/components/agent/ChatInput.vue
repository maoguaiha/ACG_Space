<script setup lang="ts">
/**
 * 聊天输入组件——三主题适配。
 *
 * 交互：
 *   - Enter 发送（Shift+Enter 换行）
 *   - 输入框左侧外：🧹 清除上下文 / 开启新话题
 *   - 输入框内左侧：📎 附件（文本文件随消息内联，V1 仅支持 txt/md/json/csv 等）
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
  send: [content: string, file: File | null]
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
  // 发送时把当前选中的附件一并带上（无文件则传 null）；发送后清空，避免后续消息误带
  emit('send', text, selectedFile.value)
  inputText.value = ''
  clearSelectedFile()
}

/** Shift+Enter 换行 */
function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

/** 附件：打开文件选择（多模态分析预留；当前仅展示文件名，真实解析待 LLM 支持图片输入） */
const fileInputRef = ref<HTMLInputElement>()
const selectedFile = ref<File | null>(null)
function handleAttach() {
  fileInputRef.value?.click()
}
function onFilePicked(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files && input.files.length) {
    selectedFile.value = input.files[0]
  }
  input.value = '' // 允许重复选同一文件
}
function clearSelectedFile() {
  selectedFile.value = null
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
        <!-- 输入框胶囊：仅包含输入框 + 底部状态挂件（垃圾桶/左上别针已移除） -->
        <div class="agent-input-capsule flex-1 flex items-start rounded-2xl px-1 py-1">
          <!-- 输入框 + 底部状态挂件（别针已移入状态栏） -->
          <div class="relative flex-1">
            <!-- 已选附件气泡 -->
            <div v-if="selectedFile" class="flex items-center gap-1.5 mb-1.5 px-2 py-1 rounded-lg w-fit max-w-full" :class="['theme-card']">
              <svg class="w-3.5 h-3.5 flex-shrink-0" :class="['theme-text-muted']" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48" />
              </svg>
              <span class="text-xs truncate max-w-[160px]" :class="['theme-text-main']">{{ selectedFile.name }}</span>
              <button type="button" class="flex-shrink-0 p-0.5 rounded hover:bg-current/10" :class="['theme-text-muted']" title="移除" @click="clearSelectedFile">
                <svg class="w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
                </svg>
              </button>
            </div>
            <textarea
              ref="textareaRef"
              v-model="inputText"
              :placeholder="placeholder || '输入你的问题...'"
              :disabled="disabled"
              rows="1"
              @keydown="handleKeydown"
              class="agent-textarea w-full resize-none rounded-xl pl-3 pr-4 pt-3 text-sm leading-relaxed focus:outline-none transition-colors placeholder:opacity-40"
              :class="['theme-text-main']"
              style="min-height: 54px; max-height: 200px; padding-bottom: 36px"
            />

            <!-- 底部状态挂件：别针 + 网络状态 + 模型（不遮挡文字，textarea 已留 padding-bottom） -->
            <div class="agent-input-status">
              <button
                type="button"
                @click="handleAttach"
                class="agent-attach-btn"
                :class="['theme-text-muted']"
                title="上传文本文件（.txt/.md/.json/.csv 等，将随本条消息发送给 AI）"
              >
                <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48" />
                </svg>
              </button>
              <span class="flex items-center gap-1">
                <span class="agent-status-dot" />
                已连接
              </span>
              <span class="opacity-40">·</span>
              <span>{{ MODEL_LABEL }}</span>
            </div>
            <!-- 隐藏的文件选择器（别针触发）：V1 仅接受文本类扩展名 -->
            <input ref="fileInputRef" type="file" class="hidden" accept=".txt,.md,.markdown,.json,.csv,.log,.yaml,.yml,.xml,.text" @change="onFilePicked" />
          </div>
        </div>

        <!-- 流式生成中 → 停止按钮 -->
        <button
          v-if="isStreaming"
          type="button"
          @click="emit('stop')"
          class="flex-shrink-0 p-3 rounded-xl transition-all duration-200 hover:-translate-y-[2px] active:scale-[0.92]"
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
          class="flex-shrink-0 p-3 rounded-xl transition-all duration-200 hover:-translate-y-[2px] active:scale-[0.92] disabled:opacity-40 disabled:cursor-not-allowed"
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
/* 底部状态挂件：绝对定位于输入框左下角，pointer-events:none 不挡输入（按钮单独再开 auto） */
.agent-input-status {
  position: absolute;
  bottom: 7px;
  left: 12px; /* 对齐 textarea 的 pl-3 */
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  line-height: 1;
  color: var(--text-muted);
  pointer-events: none;
  user-select: none;
}

/* 状态栏里的附件按钮：单独开 pointer-events 以便可点击，hover 颜色加深 */
.agent-attach-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 2px;
  border-radius: 6px;
  pointer-events: auto;
  cursor: pointer;
  transition: color 0.2s, transform 0.2s, background-color 0.2s;
}
.agent-attach-btn:hover {
  color: var(--text-main, #ec4899);
  background-color: rgba(127, 127, 127, 0.12);
  transform: translateY(-1px);
}
.agent-attach-btn:active {
  transform: scale(0.92);
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
