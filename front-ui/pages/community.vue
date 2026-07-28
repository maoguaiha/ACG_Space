<template>
  <div class="min-h-screen">
    <div class="container mx-auto px-4 py-8">
      <div class="max-w-5xl mx-auto">
        <!-- 页面标题 -->
        <div class="mb-8">
          <div class="flex items-center gap-4 mb-2">
            <NuxtLink to="/" class="p-2 rounded-xl transition-colors" :class="['theme-btn-back']">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="m15 18-6-6 6-6"/>
              </svg>
            </NuxtLink>
            <h1 class="text-3xl font-bold theme-text-main">社区</h1>
          </div>
          <p class="theme-text-muted">发现精彩文章和有趣的灵魂</p>
        </div>

        <!-- 搜索框 -->
        <div class="relative mb-8">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
            class="absolute left-4 top-1/2 -translate-y-1/2 theme-text-muted">
            <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input
            v-model="searchKeyword"
            :placeholder="activeTab === 'article' ? '搜索文章...' : '搜索用户...'"
            @input="onSearchInput"
            class="w-full rounded-2xl pl-12 pr-4 py-4 focus:outline-none transition-colors" :class="['theme-search-input']"
          />
        </div>

        <!-- Tab 栏 -->
        <div class="flex border-b mb-8" :class="['theme-tab-nav']">
          <button v-for="tab in tabs" :key="tab.key"
            @click="switchTab(tab.key)"
            class="px-6 py-3 text-sm font-medium transition-colors relative"
            :class="activeTab === tab.key ? 'theme-tab-active' : 'theme-tab-inactive'">
            {{ tab.label }}
            <div v-if="activeTab === tab.key" class="absolute bottom-0 left-0 right-0 h-0.5 rounded-full" :class="['theme-tab-active-bar']"></div>
          </button>
        </div>

        <!-- 分类筛选（仅文章栏） -->
        <div v-if="activeTab === 'article'" class="mb-5">
          <div class="flex items-center gap-3 flex-wrap">
            <span class="text-sm font-medium mr-2" :class="['theme-text-muted']">分类：</span>
            <button @click="switchCategory('')"
              class="px-4 py-2 text-sm rounded-xl transition-all duration-200"
              :class="selectedCategory === '' ? 'theme-btn-filter-active' : 'theme-btn-filter'">
              全部
            </button>
            <button v-for="cat in categories" :key="cat"
              @click="switchCategory(cat)"
              class="px-4 py-2 text-sm rounded-xl transition-all duration-200"
              :class="selectedCategory === cat ? 'theme-btn-filter-active' : 'theme-btn-filter'">
              {{ cat }}
            </button>
          </div>
        </div>

        <!-- 排序切换（仅文章栏） -->
        <div v-if="activeTab === 'article'" class="flex items-center gap-3 mb-6">
          <span class="text-sm font-medium mr-2" :class="['theme-text-muted']">排序：</span>
          <button v-for="s in sortOptions" :key="s.value"
            @click="switchSort(s.value)"
            class="px-4 py-2 text-sm rounded-xl transition-all duration-200"
            :class="sortBy === s.value ? 'theme-btn-filter-active' : 'theme-btn-filter'">
            {{ s.label }}
          </button>
        </div>

        <!-- 文章栏 -->
        <div v-if="activeTab === 'article'">
          <div v-if="loading" class="space-y-4">
            <div v-for="i in 3" :key="i" class="animate-pulse rounded-2xl p-6" :class="['theme-card']">
              <div class="h-4 rounded w-3/4 mb-3" :class="['theme-skeleton']"></div>
              <div class="h-3 rounded w-1/2" :class="['theme-skeleton']"></div>
            </div>
          </div>
          <div v-else-if="articles.length === 0" class="text-center py-16">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" :class="['mx-auto mb-4 theme-text-muted']">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>
            </svg>
            <p :class="['theme-text-muted']" v-if="searchKeyword">没有找到相关文章</p>
            <p :class="['theme-text-muted']" v-else>还没有文章</p>
          </div>
          <TransitionGroup v-else name="list" tag="div" class="space-y-4 relative" style="min-height:100px">
            <article v-for="article in articles" :key="article.id"
              class="rounded-2xl border overflow-hidden transition-all group" :class="['theme-card', 'theme-card-hover']">
              <NuxtLink :to="`/article/${article.id}`" class="flex flex-col md:flex-row">
                <div v-if="article.coverUrl" class="md:w-48 w-full h-48 md:h-48 overflow-hidden flex-shrink-0">
                  <img :src="article.coverUrl" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
                </div>
                <div v-else class="md:w-48 w-full h-48 md:h-48 flex-shrink-0 flex items-center justify-center" :class="['theme-card-placeholder']">
                  <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" :class="['theme-text-muted']">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>
                  </svg>
                </div>
                <div class="flex-1 p-4 md:p-6 flex flex-col justify-center min-h-48">
                  <div class="flex items-center gap-2 mb-2">
                    <span v-if="article.category" class="text-xs px-2 py-0.5 rounded-full" :class="['theme-badge-category']">{{ article.category }}</span>
                    <span v-if="article.isVipOnly === 1" class="text-xs px-2 py-0.5 bg-rose-500/10 text-rose-400 rounded-full">VIP</span>
                  </div>
                  <h3 class="text-lg font-bold mb-2 line-clamp-2" :class="['theme-text-main']">{{ article.title }}</h3>
                  <p class="text-sm line-clamp-2 mb-3" :class="['theme-text-muted']">{{ article.summary }}</p>
                  <div class="flex items-center gap-4 text-xs mt-auto" :class="['theme-text-muted']">
                    <NuxtLink :to="`/user/${article.authorId}`" class="flex items-center gap-2 transition-colors" :class="['theme-link-hover']" @click.stop>
                      <img v-if="article.authorAvatar" :src="article.authorAvatar" class="w-5 h-5 rounded-full" />
                      <div v-else class="w-5 h-5 rounded-full flex items-center justify-center text-[10px] text-white" :class="['theme-avatar-mini']">{{ (article.authorNickname || '匿')[0] }}</div>
                      <span>{{ article.authorNickname || '匿名' }}</span>
                    </NuxtLink>
                    <span>{{ formatDate(article.createTime) }}</span>
                    <span>{{ article.viewCount || 0 }} 阅读</span>
                    <span>{{ article.likeCount || 0 }} 赞</span>
                  </div>
                </div>
              </NuxtLink>
            </article>
          </TransitionGroup>
        </div>

        <!-- 用户栏 -->
        <div v-if="activeTab === 'user'">
          <div v-if="loading" class="space-y-3">
            <div v-for="i in 4" :key="i" class="animate-pulse bg-slate-800/40 rounded-2xl p-4 flex items-center gap-4">
              <div class="w-12 h-12 rounded-full bg-slate-700"></div>
              <div class="flex-1"><div class="h-3 bg-slate-700 rounded w-1/3 mb-2"></div><div class="h-3 bg-slate-700 rounded w-1/4"></div></div>
            </div>
          </div>
          <div v-else-if="users.length === 0" class="text-center py-16">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="mx-auto mb-4 text-slate-600">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
            <p class="text-slate-500" v-if="searchKeyword">没有找到相关用户</p>
            <p class="text-slate-500" v-else>暂无用户</p>
          </div>
          <div v-else class="space-y-3">
            <NuxtLink v-for="user in users" :key="user.id" :to="`/user/${user.id}`"
              class="flex items-center gap-4 bg-slate-800/40 rounded-2xl border border-slate-700/50 p-4 hover:border-indigo-500/30 transition-colors group">
              <img v-if="user.avatar" :src="user.avatar" class="w-12 h-12 rounded-full object-cover flex-shrink-0" />
              <div v-else class="w-12 h-12 rounded-full bg-indigo-600 flex items-center justify-center text-lg font-bold text-white flex-shrink-0">
                {{ (user.nickname || user.username || '?')[0] }}
              </div>
              <div class="flex-1 min-w-0">
                <p class="font-bold text-white group-hover:text-indigo-400 transition-colors truncate">{{ user.nickname || user.username }}</p>
                <p class="text-sm text-slate-500">@{{ user.username }} · {{ user.followerCount || 0 }} 粉丝</p>
              </div>
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-slate-600"><polyline points="9 18 15 12 9 6"/></svg>
            </NuxtLink>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="pages > 1" class="flex items-center justify-center gap-3 mt-10">
          <button @click="goPage(page - 1)" :disabled="page === 1"
            class="px-4 py-2 rounded-xl bg-slate-700/50 border border-slate-600 text-sm disabled:opacity-40 disabled:cursor-not-allowed hover:bg-slate-700 transition-colors">
            上一页
          </button>
          <span class="text-sm text-slate-400">{{ page }} / {{ pages }}</span>
          <button @click="goPage(page + 1)" :disabled="page === pages"
            class="px-4 py-2 rounded-xl bg-slate-700/50 border border-slate-600 text-sm disabled:opacity-40 disabled:cursor-not-allowed hover:bg-slate-700 transition-colors">
            下一页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { fetchArticleList, searchUsers, fetchArticleCategories, type ArticleListItem, type UserProfile } from '~/composables/useApi'

const route = useRoute()
const router = useRouter()

const activeTab = ref('article')
const searchKeyword = ref('')
const sortBy = ref('time')

const tabs = [
  { key: 'article', label: '文章' },
  { key: 'user', label: '用户' },
]

const sortOptions = [
  { value: 'time', label: '最新' },
  { value: 'views', label: '最热' },
  { value: 'likes', label: '最多点赞' },
]

// 文章
const articles = ref<ArticleListItem[]>([])
const users = ref<UserProfile[]>([])
const loading = ref(false)
const page = ref(1)
const pages = ref(1)
const categories = ref<string[]>([])
const selectedCategory = ref('')
let searchTimer: ReturnType<typeof setTimeout> | null = null

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

async function loadPage(p = 1) {
  loading.value = true
  page.value = p
  try {
    if (activeTab.value === 'article') {
      const res = await fetchArticleList({
        pageNum: p,
        pageSize: 10,
        keyword: searchKeyword.value || undefined,
        category: selectedCategory.value || undefined,
        sortBy: sortBy.value,
      })
      articles.value = res.records
      pages.value = res.pages || 1
    } else {
      const res = await searchUsers(searchKeyword.value, p)
      users.value = res.records
      pages.value = res.pages || 1
    }
  } finally {
    loading.value = false
  }
}

function switchCategory(category: string) {
  selectedCategory.value = category
  page.value = 1
  loadPage()
}

async function loadCategories() {
  try {
    categories.value = await fetchArticleCategories()
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

function switchTab(tab: string) {
  activeTab.value = tab
  page.value = 1
  loadPage()
}

function switchSort(s: string) {
  sortBy.value = s
  page.value = 1
  loadPage()
}

function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 1
    loadPage()
  }, 400)
}

function goPage(p: number) {
  if (p < 1 || p > pages.value) return
  loadPage(p)
}

onMounted(() => {
  loadCategories()
  loadPage()
})

useHead({
  title: '社区 - ACG Space',
})
</script>