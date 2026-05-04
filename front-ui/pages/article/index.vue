<template>
  <div class="container mx-auto px-4 py-12">
    <div class="mb-12">
      <h1 class="text-4xl font-black text-slate-900 dark:text-white mb-3 flex items-center gap-4">
        <div class="w-12 h-12 rounded-2xl bg-indigo-600 flex items-center justify-center shadow-lg shadow-indigo-600/30 text-white">
          <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>
        </div>
        资讯博客
      </h1>
      <p class="text-slate-400 text-lg">阅读深度动漫解析与业界资讯，发现同好讨论</p>
    </div>

    <div class="flex flex-col lg:flex-row gap-10">
      <div class="flex-1">
        <div class="flex items-center gap-4 mb-8">
          <button
            v-for="cat in categoryTabs"
            :key="cat.value"
            @click="activeCategory = cat.value"
            class="px-4 py-2 rounded-2xl border transition-all text-sm font-medium"
            :class="activeCategory === cat.value ? 'bg-indigo-600 border-indigo-500 text-white' : 'bg-slate-800/40 border-slate-700/50 text-slate-300 hover:border-indigo-500/50'"
          >
            {{ cat.label }}
          </button>
          <div class="ml-auto flex items-center gap-3">
            <input
              v-model="searchKeyword"
              @keyup.enter="handleSearch"
              placeholder="搜索文章..."
              class="px-4 py-2 rounded-2xl bg-slate-800 border border-slate-700/50 text-sm w-48"
            />
            <select v-model="sortBy" class="px-3 py-2 rounded-2xl bg-slate-800 border border-slate-700/50 text-sm">
              <option value="default">默认排序</option>
              <option value="views">浏览量</option>
              <option value="likes">点赞数</option>
            </select>
          </div>
        </div>

        <div v-if="pending" class="space-y-6">
          <div v-for="i in 5" :key="i" class="bg-slate-800/40 rounded-3xl border border-slate-700/50 p-6 animate-pulse">
            <div class="h-6 bg-slate-700 rounded w-3/4 mb-4"></div>
            <div class="h-4 bg-slate-700 rounded w-full mb-2"></div>
            <div class="h-4 bg-slate-700 rounded w-2/3"></div>
          </div>
        </div>

        <div v-else-if="articles.length === 0" class="text-center py-20">
          <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="mx-auto mb-6 text-slate-600"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
          <p class="text-slate-500 text-lg">暂无文章</p>
        </div>

        <div v-else class="space-y-6">
          <NuxtLink
            v-for="article in articles"
            :key="article.id"
            :to="`/article/${article.id}`"
            class="block bg-slate-800/40 rounded-3xl border border-slate-700/50 overflow-hidden hover:border-indigo-500/50 transition-all duration-300 group"
          >
            <div class="flex flex-col md:flex-row">
              <div v-if="article.coverUrl" class="md:w-64 h-48 md:h-auto flex-shrink-0 overflow-hidden">
                <img :src="article.coverUrl" :alt="article.title" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
              </div>
              <div class="flex-1 p-6">
                <div class="flex items-center gap-2 mb-3">
                  <span v-if="article.category" class="px-2 py-0.5 bg-indigo-500/20 text-indigo-400 text-xs font-bold rounded">{{ article.category }}</span>
                  <span v-if="article.isVipOnly === 1" class="px-2 py-0.5 bg-rose-500/20 text-rose-400 text-xs font-bold rounded">VIP专享</span>
                  <span v-if="article.isFeatured === 1" class="px-2 py-0.5 bg-yellow-500/20 text-yellow-400 text-xs font-bold rounded">推荐</span>
                </div>
                <h3 class="text-xl font-bold text-white group-hover:text-indigo-400 transition-colors mb-2 line-clamp-2">{{ article.title }}</h3>
                <p class="text-slate-400 text-sm line-clamp-2 mb-4">{{ article.summary || '暂无摘要' }}</p>
                <div class="flex items-center justify-between text-xs text-slate-500">
                  <div class="flex items-center gap-4">
                    <span class="flex items-center gap-1">
                      <img v-if="article.authorAvatar" :src="article.authorAvatar" class="w-5 h-5 rounded-full" />
                      <span>{{ article.authorNickname || '匿名用户' }}</span>
                    </span>
                    <span>{{ formatDate(article.createTime) }}</span>
                  </div>
                  <div class="flex items-center gap-4">
                    <span class="flex items-center gap-1">
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                      {{ article.viewCount || 0 }}
                    </span>
                    <span class="flex items-center gap-1">
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="currentColor" stroke="none"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                      {{ article.likeCount || 0 }}
                    </span>
                    <span class="flex items-center gap-1">
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                      {{ article.commentCount || 0 }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </NuxtLink>
        </div>

        <div v-if="totalPages > 1" class="mt-8 flex items-center justify-center gap-2">
          <button
            @click="goToPage(currentPage - 1)"
            :disabled="currentPage === 1"
            class="px-4 py-2 rounded-xl bg-slate-800 border border-slate-700/50 text-sm disabled:opacity-40 disabled:cursor-not-allowed"
          >
            上一页
          </button>
          <button
            v-for="page in visiblePages"
            :key="page"
            @click="goToPage(page)"
            class="min-w-10 h-10 rounded-xl border text-sm font-bold transition-all"
            :class="currentPage === page ? 'bg-indigo-600 border-indigo-500 text-white' : 'bg-slate-800 border-slate-700/50 text-slate-300 hover:border-indigo-500/60'"
          >
            {{ page }}
          </button>
          <button
            @click="goToPage(currentPage + 1)"
            :disabled="currentPage === totalPages"
            class="px-4 py-2 rounded-xl bg-slate-800 border border-slate-700/50 text-sm disabled:opacity-40 disabled:cursor-not-allowed"
          >
            下一页
          </button>
        </div>
      </div>

      <div class="lg:w-80 flex-shrink-0">
        <div class="sticky top-8 bg-slate-800/40 rounded-3xl border border-slate-700/50 p-6">
          <h3 class="text-lg font-bold text-white mb-4">文章分类</h3>
          <div class="space-y-2">
            <button
              v-for="cat in categoryTabs"
              :key="cat.value"
              @click="activeCategory = cat.value"
              class="w-full text-left px-4 py-2 rounded-xl transition-all text-sm"
              :class="activeCategory === cat.value ? 'bg-indigo-600 text-white' : 'text-slate-400 hover:bg-slate-700/50 hover:text-white'"
            >
              {{ cat.label }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { fetchArticleList } from '~/composables/useApi'

interface ArticleListItem {
  id: string
  title: string
  summary: string
  coverUrl: string
  category: string
  tags: string
  authorId: string
  authorNickname: string
  authorAvatar: string
  viewCount: number
  likeCount: number
  commentCount: number
  isVipOnly: number
  isFeatured: number
  createTime: string
}

const pending = ref(false)
const articles = ref<ArticleListItem[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))
const searchKeyword = ref('')
const activeCategory = ref('')
const sortBy = ref('default')

const categoryTabs = [
  { label: '全部', value: '' },
  { label: '业界资讯', value: '业界资讯' },
  { label: '深度解析', value: '深度解析' },
  { label: '新番导视', value: '新番导视' },
  { label: '周边评测', value: '周边评测' },
]

const visiblePages = computed(() => {
  const total = totalPages.value
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1)
  const start = Math.max(1, currentPage.value - 3)
  const end = Math.min(total, start + 6)
  const adjustedStart = Math.max(1, end - 6)
  return Array.from({ length: end - adjustedStart + 1 }, (_, i) => adjustedStart + i)
})

async function loadArticles() {
  pending.value = true
  try {
    const result = await fetchArticleList({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined,
      category: activeCategory.value || undefined,
      sortBy: sortBy.value
    })
    articles.value = result.records
    total.value = result.total
  } catch (error) {
    console.error('加载文章列表失败:', error)
    articles.value = []
  } finally {
    pending.value = false
  }
}

function goToPage(page: number) {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
}

function handleSearch() {
  currentPage.value = 1
  loadArticles()
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'short', day: 'numeric' })
}

watch([currentPage, activeCategory, sortBy], () => {
  loadArticles()
}, { immediate: true })

useHead({
  title: '资讯博客 - ACG Space'
})
</script>