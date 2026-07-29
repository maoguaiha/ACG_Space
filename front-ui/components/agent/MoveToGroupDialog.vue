<script setup lang="ts">
/**
 * 移动到分组对话框（三主题）。
 *
 * - 列出已有分组（单选）
 * - 底部「+ 新分组」输入项（输入文字即创建并选中）
 * - 选中后点确定 → 调 moveConversationToGroup
 */
import { ref, computed, watch } from 'vue'
import type { GroupItem } from '~/composables/useAgentApi'

const props = defineProps<{
  open: boolean
  groups: GroupItem[]
}>()

const emit = defineEmits<{
  'update:open': [v: boolean]
  /**
   * 用户点击确定
   * @param target - 选中的目标分组（null=移回最近对话；'__new__:<name>' = 新建并移动）
   */
  confirm: [target: { groupId: number | null; newGroupName?: string }]
}>()

const selectedId = ref<string | null>(null) // null=最近对话；数字=已有分组 ID；'__new__'=新建
const newGroupName = ref('')

watch(
  () => props.open,
  (o) => {
    if (o) {
      selectedId.value = null
      newGroupName.value = ''
    }
  },
  { immediate: true },
)

const canConfirm = computed(() => {
  if (selectedId.value === null) return true
  if (selectedId.value === '__new__') return newGroupName.value.trim().length > 0
  return true
})

function close() {
  emit('update:open', false)
}

function onConfirm() {
  if (!canConfirm.value) return
  if (selectedId.value === null) {
    emit('confirm', { groupId: null })
  } else if (selectedId.value === '__new__') {
    emit('confirm', { groupId: null, newGroupName: newGroupName.value.trim() })
  } else {
    emit('confirm', { groupId: Number(selectedId.value) })
  }
  close()
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') close()
}
</script>

<template>
  <Teleport to="body">
    <Transition name="agent-dialog">
      <div v-if="open" class="agent-dialog-backdrop" @click.self="close" @keydown="onKeydown" tabindex="-1">
        <div class="agent-dialog-panel" role="dialog" aria-modal="true" aria-label="移动到分组">
          <div class="flex items-center justify-between px-5 py-3 border-b" :class="['agent-dialog-header', 'theme-border']">
            <h2 class="text-base font-semibold" :class="['theme-text-main']">移动到分组</h2>
            <button class="agent-dialog-close" :class="['theme-text-muted']" aria-label="关闭" @click="close">
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>

          <div class="px-3 py-3 max-h-80 overflow-y-auto hide-scrollbar-container">
            <!-- 最近对话（移回未分组） -->
            <label class="agent-group-option" :class="{ 'agent-group-option-active': selectedId === null }">
              <input v-model="selectedId" type="radio" :value="null" class="sr-only" />
              <span class="flex items-center gap-2 flex-1 min-w-0">
                <svg class="w-4 h-4 shrink-0" :class="['theme-text-muted']" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10" />
                  <polyline points="12 6 12 12 16 14" />
                </svg>
                <span class="truncate" :class="['theme-text-main']">最近对话（未分组）</span>
              </span>
            </label>

            <!-- 已有分组 -->
            <label
              v-for="g in groups"
              :key="g.id"
              class="agent-group-option"
              :class="{ 'agent-group-option-active': selectedId === g.id }"
            >
              <input v-model="selectedId" type="radio" :value="g.id" class="sr-only" />
              <span class="flex items-center gap-2 flex-1 min-w-0">
                <svg class="w-4 h-4 shrink-0" :class="['theme-text-muted']" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
                </svg>
                <span class="truncate" :class="['theme-text-main']">{{ g.name }}</span>
              </span>
            </label>

            <!-- 新分组（输入即创建并选中） -->
            <label class="agent-group-option agent-group-option-new" :class="{ 'agent-group-option-active': selectedId === '__new__' }">
              <input v-model="selectedId" type="radio" value="__new__" class="sr-only" />
              <span class="flex items-center gap-2 flex-1 min-w-0">
                <svg class="w-4 h-4 shrink-0" :class="['theme-text-muted']" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="12" y1="5" x2="12" y2="19" />
                  <line x1="5" y1="12" x2="19" y2="12" />
                </svg>
                <input
                  v-if="selectedId === '__new__'"
                  v-model="newGroupName"
                  type="text"
                  maxlength="50"
                  placeholder="新建分组名"
                  class="agent-new-group-input"
                  :class="['theme-text-main']"
                  @click.stop
                />
                <span v-else class="truncate" :class="['theme-text-main']">新建分组</span>
              </span>
            </label>
          </div>

          <div class="flex items-center justify-end gap-2 px-5 py-3 border-t" :class="['agent-dialog-footer', 'theme-border']">
            <button class="agent-dialog-btn agent-dialog-btn-cancel" @click="close">取消</button>
            <button
              class="agent-dialog-btn agent-dialog-btn-primary"
              :disabled="!canConfirm"
              @click="onConfirm"
            >
              确定移动
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.agent-dialog-enter-active,
.agent-dialog-leave-active {
  transition: opacity 0.18s ease;
}
.agent-dialog-enter-from,
.agent-dialog-leave-to {
  opacity: 0;
}
</style>