<script setup lang="ts">
/**
 * AI 助手会话侧边栏——三主题适配。
 *
 * 功能：
 *   - 新建会话按钮
 *   - 会话列表（点击选中高亮）
 *   - 删除按钮（hover 显示 ×）
 *   - 底部返回首页入口
 */
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
}>()

const router = useRouter()

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
  <div class="flex flex-col h-full border-r" :class="['theme-border', 'theme-bg-secondary']">
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

    <!-- 会话列表 -->
    <div class="flex-1 overflow-y-auto px-2 pb-2 space-y-1">
      <div
        v-for="conv in conversations"
        :key="conv.id"
        class="group relative"
      >
        <button
          @click="emit('select', conv.id)"
          class="w-full text-left px-3 py-2 rounded-xl text-sm transition-colors"
          :class="
            activeId === conv.id
              ? ['theme-primary-bg', 'text-white']
              : ['theme-card', 'theme-card-hover', 'theme-text-main']
          "
        >
          <span class="truncate block">{{ conv.title || '新的对话' }}</span>
          <span class="text-xs mt-0.5 block truncate" :class="activeId === conv.id ? 'text-white/60' : 'opacity-50'">
            {{ formatDate(conv.updateTime) }}
          </span>
        </button>

        <!-- 删除按钮（hover 显示） -->
        <button
          @click.stop="emit('delete', conv.id)"
          class="absolute right-1 top-1/2 -translate-y-1/2 p-1 rounded-md opacity-0 group-hover:opacity-100 transition-opacity"
          :class="activeId === conv.id ? 'text-white/60 hover:text-white' : 'theme-text-muted hover:text-red-400'"
          title="删除会话"
        >
          <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        </button>
      </div>

      <!-- 无会话空态 -->
      <div v-if="conversations.length === 0" class="text-center py-8">
        <p class="text-xs" :class="['theme-text-muted']">暂无对话</p>
      </div>
    </div>

    <!-- 底部：返回首页 -->
    <div class="p-3 border-t" :class="['theme-border']">
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
