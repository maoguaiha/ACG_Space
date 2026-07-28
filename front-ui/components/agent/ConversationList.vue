<script setup lang="ts">
/**
 * AI 助手会话侧边栏——三主题适配。
 *
 * 功能：
 *   - 新建会话按钮
 *   - 会话列表（点击选中高亮）
 *   - 悬浮操作：编辑标题（✏️）/ 删除（🗑️）
 *   - 底部固定区（不随列表滚动）：AI 设置 / 清除所有对话 / 返回首页
 */
import { ref, nextTick } from 'vue'
import type { ConversationItem } from '~/composables/useAgentApi'
import { useRouter } from 'vue-router'

const props = defineProps<{
  conversations: ConversationItem[]
  activeId: string | null
}>()

const emit = defineEmits<{
  select: [id: string]
  create: []
  delete: [id: string]
  rename: [id: string, title: string]
  clearAll: []
  openSettings: []
}>()

const router = useRouter()

/** 行内编辑状态 */
const editingId = ref<string | null>(null)
const editingTitle = ref('')
const editInputRef = ref<HTMLInputElement>()

function startEdit(conv: ConversationItem, e: Event) {
  e.stopPropagation()
  editingId.value = conv.id
  editingTitle.value = conv.title || ''
  nextTick(() => editInputRef.value?.focus())
}

function saveEdit(id: string) {
  const title = editingTitle.value.trim()
  if (title) {
    emit('rename', id, title)
  }
  editingId.value = null
}

function cancelEdit() {
  editingId.value = null
}

const confirmingClear = ref(false)
function onClearAll() {
  if (confirmingClear.value) return
  if (window.confirm('确定要清除所有对话吗？此操作不可恢复。')) {
    emit('clearAll')
  }
  confirmingClear.value = false
}

/** 格式化相对时间 */
function formatDate(iso: string): string {
  if (!iso) return ''
  const d = new Date(iso)
  const now = new Date()
  const diffMs = now.getTime() - d.getTime()
  const diffMin = Math.floor(diffMs / 60000)
  if (diffMin < 1) return '刚刚'
  if (diffMin < 60) return `${diffMin}分钟前`
  const diffHr = Math.floor(diffMin / 60)
  if (diffHr < 24) return `${diffHr}小时前`
  const diffDay = Math.floor(diffHr / 24)
  if (diffDay < 7) return `${diffDay}天前`
  return d.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}
</script>

<template>
  <div class="flex flex-col h-full agent-sidebar" :class="['theme-bg-secondary']">
    <!-- 新建会话 -->
    <div class="p-3">
      <button
        @click="emit('create')"
        class="w-full flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-medium transition-colors"
        :class="['theme-card', 'theme-card-hover', 'theme-text-main']"
      >
        <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19" />
          <line x1="5" y1="12" x2="19" y2="12" />
        </svg>
        新对话
      </button>
    </div>

    <!-- 会话列表（可滚动） -->
    <div class="hide-scrollbar-container flex-1 overflow-y-auto px-2 pb-2 space-y-1">
      <div
        v-for="conv in conversations"
        :key="conv.id"
        class="group relative"
      >
        <button
          @click="editingId === conv.id ? null : emit('select', conv.id)"
          class="w-full text-left px-3 py-2 rounded-xl text-sm transition-colors"
          :class="
            activeId === conv.id
              ? ['theme-primary-bg', 'text-white']
              : ['theme-card', 'theme-card-hover', 'theme-text-main']
          "
        >
          <!-- 行内编辑态 -->
          <input
            v-if="editingId === conv.id"
            ref="editInputRef"
            v-model="editingTitle"
            @click.stop
            @keydown.enter.prevent="saveEdit(conv.id)"
            @keydown.esc.prevent="cancelEdit"
            @blur="saveEdit(conv.id)"
            class="w-full bg-transparent outline-none border-b border-dashed border-white/60 text-white placeholder-white/60"
            :class="activeId === conv.id ? '' : 'theme-text-main'"
          />
          <template v-else>
            <span class="truncate block pr-14">{{ conv.title || '新的对话' }}</span>
            <span class="text-xs mt-0.5 block truncate" :class="activeId === conv.id ? 'text-white/60' : 'opacity-50'">
              {{ formatDate(conv.updateTime) }}
            </span>
          </template>
        </button>

        <!-- 悬浮操作：编辑 / 删除 -->
        <div
          v-if="editingId !== conv.id"
          class="absolute right-1.5 top-1/2 -translate-y-1/2 flex items-center gap-0.5 opacity-0 group-hover:opacity-100 transition-opacity"
        >
          <button
            @click="startEdit(conv, $event)"
            class="p-1 rounded-md transition-colors"
            :class="activeId === conv.id ? 'text-white/70 hover:text-white hover:bg-white/20' : 'theme-text-muted hover:text-indigo-400 hover:bg-black/5'"
            title="编辑标题"
          >
            <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
            </svg>
          </button>
          <button
            @click.stop="emit('delete', conv.id)"
            class="p-1 rounded-md transition-colors"
            :class="activeId === conv.id ? 'text-white/70 hover:text-white hover:bg-white/20' : 'theme-text-muted hover:text-red-400 hover:bg-black/5'"
            title="删除会话"
          >
            <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="3 6 5 6 21 6" />
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
            </svg>
          </button>
        </div>
      </div>

      <!-- 无会话空态 -->
      <div v-if="conversations.length === 0" class="text-center py-8">
        <p class="text-xs" :class="['theme-text-muted']">暂无对话</p>
      </div>
    </div>

    <!-- 底部固定区（不随列表滚动） -->
    <div class="p-3 border-t space-y-2" :class="['theme-border']">
      <button
        @click="emit('openSettings')"
        class="w-full flex items-center gap-2 px-3 py-2 rounded-xl text-sm transition-colors"
        :class="['theme-card', 'theme-card-hover', 'theme-text-main']"
        title="AI 设置（设定 System Prompt、选择模型）"
      >
        <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="3" />
          <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" />
        </svg>
        AI 设置
      </button>

      <button
        @click="onClearAll"
        class="w-full flex items-center gap-2 px-3 py-2 rounded-xl text-sm transition-colors text-red-400 hover:bg-red-500/10"
        title="清除所有对话"
      >
        <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="3 6 5 6 21 6" />
          <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
          <line x1="10" y1="11" x2="10" y2="17" />
          <line x1="14" y1="11" x2="14" y2="17" />
        </svg>
        清除所有对话
      </button>

      <button
        @click="router.push('/')"
        class="w-full flex items-center gap-2 px-3 py-2 rounded-xl text-sm transition-colors"
        :class="['theme-card', 'theme-card-hover', 'theme-text-main']"
      >
        <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
          <polyline points="9 22 9 12 15 12 15 22" />
        </svg>
        返回首页
      </button>
    </div>
  </div>
</template>
