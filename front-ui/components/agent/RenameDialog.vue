<script setup lang="ts">
/**
 * 重命名对话框（三主题适配）。
 *
 * 用法：
 *   <RenameDialog
 *     v-model:open="renameDialog.open"
 *     title="重命名"
 *     :initial-value="renameDialog.title"
 *     placeholder="输入新标题"
 *     @confirm="handleConfirm"
 *   />
 *
 * 主题：
 *   - 深色：玻璃面板 + 紫色高亮「确定」按钮
 *   - 浅色：白底 + 蓝色高亮「确定」按钮
 *   - 粉色：淡粉底 + 粉色高亮「确定」按钮
 */
import { ref, watch, nextTick } from 'vue'

const props = defineProps<{
  open: boolean
  /** 对话框标题（默认「重命名」） */
  title?: string
  /** 输入框初始值 */
  initialValue?: string
  /** 输入框 placeholder */
  placeholder?: string
  /** 最大长度（默认 50） */
  maxLength?: number
  /** 危险操作模式（确认按钮红色）— 用于「删除分组」等 */
  danger?: boolean
  /** 危险模式的按钮文案 */
  confirmText?: string
}>()

const emit = defineEmits<{
  'update:open': [v: boolean]
  confirm: [value: string]
}>()

const value = ref('')
const inputRef = ref<HTMLInputElement>()

watch(
  () => props.open,
  (o) => {
    if (o) {
      value.value = props.initialValue || ''
      nextTick(() => inputRef.value?.focus())
    }
  },
  { immediate: true },
)

function close() {
  emit('update:open', false)
}

function onConfirm() {
  const v = value.value.trim()
  if (!v) return
  emit('confirm', v)
  emit('update:open', false)
}

/** Esc 关闭；Enter 确认（输入框聚焦时） */
function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') close()
  // Enter 由输入框原生处理（不会换行）
}
</script>

<template>
  <Teleport to="body">
    <Transition name="agent-dialog">
      <div v-if="open" class="agent-dialog-backdrop" @click.self="close" @keydown="onKeydown" tabindex="-1">
        <div class="agent-dialog-panel" role="dialog" aria-modal="true" :aria-label="title || '重命名'">
          <!-- 标题栏 -->
          <div class="flex items-center justify-between px-5 py-3 border-b" :class="['agent-dialog-header', 'theme-border']">
            <h2 class="text-base font-semibold" :class="['theme-text-main']">{{ title || '重命名' }}</h2>
            <button class="agent-dialog-close" :class="['theme-text-muted']" aria-label="关闭" @click="close">
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>

          <!-- 输入区 -->
          <div class="px-5 py-4">
            <input
              ref="inputRef"
              v-model="value"
              type="text"
              :maxlength="maxLength ?? 50"
              :placeholder="placeholder || '输入名称'"
              class="agent-dialog-input"
              :class="['theme-text-main', 'theme-card']"
              @keydown.enter.prevent="onConfirm"
            />
          </div>

          <!-- 底部按钮 -->
          <div class="flex items-center justify-end gap-2 px-5 py-3 border-t" :class="['agent-dialog-footer', 'theme-border']">
            <button class="agent-dialog-btn agent-dialog-btn-cancel" @click="close">取消</button>
            <button
              class="agent-dialog-btn"
              :class="danger ? 'agent-dialog-btn-danger' : 'agent-dialog-btn-primary'"
              :disabled="!value.trim()"
              @click="onConfirm"
            >
              {{ confirmText || '确定' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* 淡入淡出过渡 */
.agent-dialog-enter-active,
.agent-dialog-leave-active {
  transition: opacity 0.18s ease;
}
.agent-dialog-enter-from,
.agent-dialog-leave-to {
  opacity: 0;
}
</style>