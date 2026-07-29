<script setup lang="ts">
/**
 * 会话分组区块（千问式侧边栏 V2.4）。
 *
 * - 折叠/展开（默认展开）
 * - 分组标题旁三点菜单（重命名 / 删除分组）
 * - 列表项沿用最近对话同款三点菜单
 */
import { ref } from 'vue'
import type { ConversationItem, GroupItem } from '~/composables/useAgentApi'

const props = defineProps<{
  group: GroupItem
  conversations: ConversationItem[]
  activeId: string | null
  /** 批量管理模式 */
  batchMode?: boolean
  /** 已选中的会话 id 集合 */
  selectedIds?: Set<string>
}>()

const emit = defineEmits<{
  select: [id: string]
  rename: [id: string, title: string]
  delete: [id: string]
  pin: [id: string, pinned: boolean]
  moveToGroup: [id: string]
  toggleSelect: [id: string]
  renameGroup: [groupId: string, name: string]
  deleteGroup: [groupId: string]
  /** 进入批量管理模式（全局） */
  enterBatch: []
  /** 在具体分组内新建对话（直接归属该分组） */
  newConversationInGroup: [groupId: string]
}>()

const collapsed = ref(false)
const headerMenuOpen = ref(false)

function toggleHeaderMenu() {
  headerMenuOpen.value = !headerMenuOpen.value
}

function closeHeaderMenu() {
  headerMenuOpen.value = false
}

function onHeaderNewConv() {
  closeHeaderMenu()
  emit('newConversationInGroup', props.group.id)
}

function onHeaderRename() {
  closeHeaderMenu()
  emit('renameGroup', props.group.id, props.group.name)
}

function onHeaderDelete() {
  closeHeaderMenu()
  emit('deleteGroup', props.group.id)
}

/** 行内三点菜单的开闭状态（按会话 id 索引） */
const itemMenuOpen = ref<string | null>(null)
function toggleItemMenu(id: string, e: Event) {
  e.stopPropagation()
  itemMenuOpen.value = itemMenuOpen.value === id ? null : id
}
function closeItemMenu() {
  itemMenuOpen.value = null
}

/** 点击外部关闭所有菜单 */
if (typeof window !== 'undefined') {
  // 用 onClickOutside 也行，但简化为 document click
}
function onDocClick() {
  headerMenuOpen.value = false
  itemMenuOpen.value = null
}

import { onMounted, onBeforeUnmount } from 'vue'
onMounted(() => document.addEventListener('click', onDocClick))
onBeforeUnmount(() => document.removeEventListener('click', onDocClick))

function isSelected(id: string): boolean {
  return props.selectedIds?.has(id) ?? false
}
</script>

<template>
  <div class="agent-conv-group">
    <!-- 分组标题 -->
    <div class="flex items-center px-3 py-1.5 group/header">
      <button
        class="flex-1 flex items-center gap-1.5 min-w-0 text-left text-sm font-medium theme-text-muted hover:text-current transition-colors"
        @click="collapsed = !collapsed"
      >
        <svg
          class="w-3 h-3 shrink-0 transition-transform"
          :class="[collapsed ? '-rotate-90' : '']"
          viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
        >
          <polyline points="6 9 12 15 18 9" />
        </svg>
        <span class="truncate">{{ group.name }}<span v-if="group.id !== '__recent__' && conversations.length > 0" class="opacity-60 ml-1">({{ conversations.length }})</span></span>
      </button>
      <!-- 分组级三点菜单（最近对话无管理需求，隐藏避免被下方会话遮挡） -->
      <div v-if="group.id !== '__recent__'" class="relative">
        <button
          class="p-1 rounded-md opacity-0 group-hover/header:opacity-100 transition-opacity"
          :class="['theme-text-muted hover:bg-black/5']"
          aria-label="分组操作"
          @click.stop="toggleHeaderMenu"
        >
          <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="currentColor">
            <circle cx="5" cy="12" r="1.6" />
            <circle cx="12" cy="12" r="1.6" />
            <circle cx="19" cy="12" r="1.6" />
          </svg>
        </button>
        <Transition name="agent-menu">
          <div v-if="headerMenuOpen" class="agent-context-menu" @click.stop>
            <button class="agent-context-menu-item" @click="onHeaderNewConv">
              <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="5" x2="12" y2="19" />
                <line x1="5" y1="12" x2="19" y2="12" />
              </svg>
              新建对话
            </button>
            <div class="agent-context-menu-divider" />
            <button class="agent-context-menu-item" @click="onHeaderRename">
              <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
              </svg>
              重命名分组
            </button>
            <div class="agent-context-menu-divider" />
            <button class="agent-context-menu-item agent-context-menu-item-danger" @click="onHeaderDelete">
              <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="3 6 5 6 21 6" />
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
              </svg>
              删除分组
            </button>
          </div>
        </Transition>
      </div>
    </div>

    <!-- 分组内的会话列表 -->
    <div v-if="!collapsed" class="space-y-1">
      <div
        v-for="conv in conversations"
        :key="conv.id"
        class="group/item relative"
      >
        <button
          @click="batchMode ? emit('toggleSelect', conv.id) : (itemMenuOpen === conv.id ? null : emit('select', conv.id))"
          class="w-full text-left px-3 py-2 rounded-xl text-sm transition-colors flex items-center gap-2"
          :class="
            activeId === conv.id
              ? ['theme-primary-bg', 'text-white']
              : ['theme-card', 'theme-card-hover', 'theme-text-main']
          "
        >
          <!-- 批量态复选框 -->
          <span
            v-if="batchMode"
            class="shrink-0 w-3.5 h-3.5 rounded border flex items-center justify-center"
            :class="
              isSelected(conv.id)
                ? (activeId === conv.id ? 'bg-white/90 border-white/90' : 'agent-batch-checkbox-on border-transparent')
                : (activeId === conv.id ? 'border-white/60' : 'agent-batch-checkbox-off theme-border')
            "
          >
            <svg v-if="isSelected(conv.id)" class="w-3 h-3" :class="activeId === conv.id ? 'text-indigo-600' : 'text-white'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
              <polyline points="20 6 9 17 4 12" />
            </svg>
          </span>
          <!-- 置顶小图钉 -->
          <svg v-if="conv.pinned === 1 && !batchMode" class="shrink-0 w-3 h-3" :class="activeId === conv.id ? 'text-white/80' : 'text-amber-500'" viewBox="0 0 24 24" fill="currentColor">
            <path d="M16 2l-4 4-7 7 4 4 7-7 4-4-4-4zM3 21l4-4" stroke="currentColor" stroke-width="2" fill="none" />
          </svg>
          <span class="truncate block flex-1 min-w-0">{{ conv.title || '新的对话' }}</span>
        </button>

        <!-- 三点菜单按钮（仅在非批量态显示） -->
        <div v-if="!batchMode" class="absolute right-1.5 top-1/2 -translate-y-1/2">
          <button
            class="p-1 rounded-md transition-opacity opacity-0 group-hover/item:opacity-100"
            :class="activeId === conv.id ? 'text-white/70 hover:text-white hover:bg-white/20' : 'theme-text-muted hover:bg-black/5'"
            aria-label="会话操作"
            @click.stop="toggleItemMenu(conv.id, $event)"
          >
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="currentColor">
              <circle cx="5" cy="12" r="1.6" />
              <circle cx="12" cy="12" r="1.6" />
              <circle cx="19" cy="12" r="1.6" />
            </svg>
          </button>
          <Transition name="agent-menu">
            <div v-if="itemMenuOpen === conv.id" class="agent-context-menu" @click.stop>
              <button class="agent-context-menu-item" @click="emit('enterBatch'); closeItemMenu()">
                <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="9 11 12 14 22 4" />
                  <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
                </svg>
                批量管理
              </button>
              <div class="agent-context-menu-divider" />
              <button class="agent-context-menu-item" @click="emit('rename', conv.id, conv.title || ''); closeItemMenu()">
                <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                </svg>
                重命名
              </button>
              <button class="agent-context-menu-item" @click="emit('pin', conv.id, conv.pinned !== 1); closeItemMenu()">
                <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 2l3 7h7l-5.5 4.5L18 21l-6-4-6 4 1.5-7.5L2 9h7z" />
                </svg>
                {{ conv.pinned === 1 ? '取消置顶' : '置顶此对话' }}
              </button>
              <div class="agent-context-menu-divider" />
              <button class="agent-context-menu-item" @click="emit('moveToGroup', conv.id); closeItemMenu()">
                <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
                </svg>
                移动分组
              </button>
              <button class="agent-context-menu-item agent-context-menu-item-danger" @click="emit('delete', conv.id); closeItemMenu()">
                <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6" />
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                </svg>
                删除
              </button>
            </div>
          </Transition>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.agent-menu-enter-active,
.agent-menu-leave-active {
  transition: opacity 0.12s ease, transform 0.12s ease;
}
.agent-menu-enter-from,
.agent-menu-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>