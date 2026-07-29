<script setup lang="ts">
/**
 * AI 助手会话侧边栏——千问式 V2.4。
 *
 * 功能：
 *   - 「对话分组」section（折叠/展开、分组级三点菜单）
 *   - 「最近对话」section（未分组的会话）
 *   - 每项右侧「⋯」按钮 → 弹出操作菜单：重命名 / 置顶 / 移动分组 / 删除
 *   - 「批量管理」开关：进入批量态显示复选框，多选后底部出现批量操作栏（删除 / 移动分组 / 全部取消）
 *   - 菜单外点击关闭、Esc 关闭
 */
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import type { ConversationItem, GroupItem } from '~/composables/useAgentApi'
import { useRouter } from 'vue-router'
import ConversationGroup from './ConversationGroup.vue'

const props = defineProps<{
  conversations: ConversationItem[]
  groups: GroupItem[]
  activeId: string | null
}>()

const emit = defineEmits<{
  select: [id: string]
  create: []
  delete: [id: string]
  rename: [id: string, title: string]
  pin: [id: string, pinned: boolean]
  moveToGroup: [id: string]
  batchDelete: [ids: string[]]
  clearAll: []
  openSettings: []
  // 分组管理
  renameGroup: [groupId: string, name: string]
  deleteGroup: [groupId: string]
  // 对话框控制（向上抛给父组件统一管理弹窗状态）
  openMoveGroup: [ids: string[]]
  openCreateGroup: []
}>()

const router = useRouter()

// ====================== 分组逻辑 ======================
/** 按 groupId 把会话分桶；null 桶为「最近对话未分组」 */
const groupedConvs = computed<Record<string, ConversationItem[]>>(() => {
  const map: Record<string, ConversationItem[]> = { __recent__: [] }
  for (const g of props.groups) map[g.id] = []
  for (const c of props.conversations) {
    const gid = c.groupId ? String(c.groupId) : '__recent__'
    if (!map[gid]) map[gid] = []
    map[gid].push(c)
  }
  return map
})
const recentConvs = computed<ConversationItem[]>(() => groupedConvs.value['__recent__'] || [])

// ====================== 目录折叠状态 ======================
/** 「对话分组」目录是否展开（默认展开） */
const groupsExpanded = ref(true)
function toggleGroupsDir() {
  groupsExpanded.value = !groupsExpanded.value
}

// ====================== 批量管理 ======================
const batchMode = ref(false)
const selectedIds = ref<Set<string>>(new Set())

function enterBatch() {
  batchMode.value = true
  selectedIds.value = new Set()
}
function exitBatch() {
  batchMode.value = false
  selectedIds.value = new Set()
}
function toggleSelect(id: string) {
  if (selectedIds.value.has(id)) {
    selectedIds.value.delete(id)
  } else {
    selectedIds.value.add(id)
  }
  // 触发响应式
  selectedIds.value = new Set(selectedIds.value)
}
function selectAll() {
  selectedIds.value = new Set(props.conversations.map(c => c.id))
}
function invertSelection() {
  selectedIds.value = new Set(
    props.conversations.filter(c => !selectedIds.value.has(c.id)).map(c => c.id),
  )
}
function onBatchDelete() {
  const ids = Array.from(selectedIds.value)
  if (ids.length === 0) return
  if (window.confirm(`确定删除选中的 ${ids.length} 个会话？此操作不可恢复。`)) {
    emit('batchDelete', ids)
    exitBatch()
  }
}
function onBatchMove() {
  const ids = Array.from(selectedIds.value)
  if (ids.length === 0) return
  // 把全部选中 id 抛给父组件，由统一的「移动分组」对话框批量处理
  emit('openMoveGroup', ids)
  exitBatch()
}

// ====================== 全局点击关闭菜单 ======================
function onDocClick() {
  // 留空：子组件（ConversationGroup）已自行管理各自的菜单开闭
}
onMounted(() => document.addEventListener('click', onDocClick))
onBeforeUnmount(() => document.removeEventListener('click', onDocClick))

// ====================== 底部固定区 ======================
const confirmingClear = ref(false)
function onClearAll() {
  if (confirmingClear.value) return
  if (window.confirm('确定要清除所有对话吗？此操作不可恢复。')) {
    emit('clearAll')
  }
  confirmingClear.value = false
}

/** AI 设置（占位） */
function openSettings() {
  emit('openSettings')
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

    <!-- 滚动区 -->
    <div class="hide-scrollbar-container flex-1 overflow-y-auto px-2 pb-2 space-y-2">
      <!-- 对话分组 目录（可折叠） -->
      <div>
        <div class="flex items-center justify-between px-3 py-1.5">
          <button
            type="button"
            class="flex-1 min-w-0 flex items-center gap-1.5 text-left text-sm font-medium theme-text-muted hover:text-current transition-colors"
            @click="toggleGroupsDir"
          >
            <svg
              :class="['w-3 h-3 shrink-0 transition-transform', groupsExpanded ? '' : '-rotate-90']"
              viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"
              stroke-linecap="round" stroke-linejoin="round"
            >
              <polyline points="6 9 12 15 18 9" />
            </svg>
            <svg class="w-4 h-4 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 7a2 2 0 0 1 2-2h4l2 2h8a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
            </svg>
            <span class="truncate">对话分组<span v-if="groups.length > 0" class="opacity-60 ml-1">({{ groups.length }})</span></span>
          </button>
          <button
            class="text-xs px-2 py-0.5 rounded-md flex items-center gap-1 shrink-0 transition-colors"
            :class="['theme-text-muted hover:bg-black/5']"
            @click.stop="emit('openCreateGroup')"
            title="新建分组"
          >
            <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19" />
              <line x1="5" y1="12" x2="19" y2="12" />
            </svg>
            新建
          </button>
        </div>

        <div v-show="groupsExpanded">
          <div v-if="groups.length > 0" class="ml-3 pl-3 border-l theme-border space-y-1">
            <ConversationGroup
              v-for="g in groups"
              :key="g.id"
              :group="g"
              :conversations="groupedConvs[g.id] || []"
              :active-id="activeId"
              :batch-mode="batchMode"
              :selected-ids="selectedIds"
              @select="(id) => emit('select', id)"
              @rename="(id, t) => emit('rename', id, t)"
              @delete="(id) => emit('delete', id)"
              @pin="(id, p) => emit('pin', id, p)"
              @move-to-group="(id) => emit('moveToGroup', id)"
              @toggle-select="toggleSelect"
              @rename-group="(gid, name) => emit('renameGroup', gid, name)"
              @delete-group="(gid) => emit('deleteGroup', gid)"
              @enter-batch="enterBatch"
            />
          </div>
          <div v-else class="px-3 py-2 text-center">
            <p class="text-xs theme-text-muted">暂无分组，点击右上角「新建」</p>
          </div>
        </div>
      </div>

      <!-- 最近对话 section（标题由 ConversationGroup 渲染，避免重复） -->
      <div>
        <ConversationGroup
          v-if="recentConvs.length > 0"
          :group="{ id: '__recent__', name: '最近对话' }"
          :conversations="recentConvs"
          :active-id="activeId"
          :batch-mode="batchMode"
          :selected-ids="selectedIds"
          @select="(id) => emit('select', id)"
          @rename="(id, t) => emit('rename', id, t)"
          @delete="(id) => emit('delete', id)"
          @pin="(id, p) => emit('pin', id, p)"
          @move-to-group="(id) => emit('moveToGroup', id)"
          @toggle-select="toggleSelect"
          @enter-batch="enterBatch"
        />
        <div v-else class="text-center py-6">
          <p class="text-xs" :class="['theme-text-muted']">暂无对话</p>
        </div>
      </div>

      <!-- 批量态底部操作栏（sticky 到滚动容器底） -->
      <div v-if="batchMode" class="agent-batch-bar">
        <span class="text-xs flex-1" :class="['theme-text-muted']">
          已选 <span :class="['theme-text-main', 'font-medium']">{{ selectedIds.size }}</span> 个
        </span>
        <button class="text-xs px-2 py-1 rounded-md" :class="['theme-text-muted hover:bg-black/5']" @click="selectAll">全选</button>
        <button class="text-xs px-2 py-1 rounded-md" :class="['theme-text-muted hover:bg-black/5']" @click="invertSelection">反选</button>
        <button class="text-xs px-2 py-1 rounded-md" :class="['theme-text-muted hover:bg-black/5']" @click="onBatchMove">移动分组</button>
        <button class="text-xs px-2 py-1 rounded-md text-red-500 hover:bg-red-500/10" @click="onBatchDelete">删除</button>
        <button class="text-xs px-2 py-1 rounded-md ml-1" :class="['theme-text-muted hover:bg-black/5']" @click="exitBatch">退出批量</button>
      </div>
    </div>

    <!-- 底部固定区 -->
    <div class="p-3 border-t space-y-2" :class="['theme-border']">
      <button
        @click="openSettings"
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