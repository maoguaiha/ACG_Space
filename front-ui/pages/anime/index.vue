<template>
  <div class="container mx-auto px-4 py-12">
    <!-- 返回按钮 -->
    <div class="mb-8">
      <button @click="router.back()" class="group w-10 h-10 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center transition-all hover:border-indigo-500/50 hover:bg-indigo-500/10" title="返回上一页">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m15 18-6-6 6-6"/></svg>
      </button>
    </div>

    <!-- 头部标题 -->
    <div class="mb-12">
      <h1 class="text-4xl font-black mb-3 flex items-center gap-4" :class="['theme-text-main']">
        <div class="w-12 h-12 rounded-2xl flex items-center justify-center shadow-lg text-white" :class="['theme-primary-bg']">
          <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M21 8V5a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v3"/><path d="M21 16v3a2 2 0 0 1-2 2H5a2 2 0 0 1-2 2v-3"/><path d="M4 12H2"/><path d="M10 12H8"/><path d="M16 12h-2"/><path d="M22 12h-2"/></svg>
        </div>
        番剧探索库
      </h1>
      <p class="text-lg" :class="['theme-text-muted']">直接探索 Bangumi (BGM.tv) 海量番剧资源，一键发现二次元世界。</p>
    </div>

    <!-- 搜索区域 -->
    <div class="mb-16 relative">
      <div class="relative group max-w-3xl z-30">
        <input
          v-model="bgmSearchKeyword"
          @keyup.enter="handleBgmSearch"
          @focus="showSuggestions = suggestions.length > 0"
          @blur="handleBlur"
                  ref="searchInput"
          type="text"
          placeholder="输入番剧名称进行全网搜索..."
          class="w-full border-2 focus:border-indigo-500 rounded-3xl pl-6 pr-32 py-5 text-xl outline-none transition-all shadow-2xl"
          :class="['theme-search-input']"
        />
        <button
          @click="handleBgmSearch"
          :disabled="isSearching"
          class="absolute right-3 top-1/2 -translate-y-1/2 bg-indigo-600 hover:bg-indigo-500 disabled:bg-slate-700 text-white px-8 py-3 rounded-2xl font-black transition-all active:scale-95"
        >
          {{ isSearching ? '搜索中...' : '搜索' }}
        </button>

        <!-- 模糊搜索下拉窗 -->
        <transition
          enter-active-class="transition duration-200 ease-out"
          enter-from-class="translate-y-1 opacity-0"
          enter-to-class="translate-y-0 opacity-100"
          leave-active-class="transition duration-150 ease-in"
          leave-from-class="translate-y-0 opacity-100"
          leave-to-class="translate-y-1 opacity-0"
        >
          <div v-if="showSuggestions" ref="suggestionBox" class="absolute top-full left-0 right-0 mt-2 bg-white dark:bg-slate-800 rounded-3xl shadow-2xl border border-slate-200 dark:border-slate-700 overflow-hidden z-[100] backdrop-blur-xl">
            <div class="max-h-[400px] overflow-y-auto py-2 custom-scrollbar">
              <div 
                v-for="item in suggestions" 
                :key="item.id"
                @click="selectSuggestion(item)"
                class="flex items-center gap-4 px-5 py-3 hover:bg-slate-100 dark:hover:bg-slate-700/50 cursor-pointer transition-colors group"
              >
                <img :src="item.images?.large || item.images?.common || item.image || item.images?.medium || 'https://placehold.jp/cccccc/ffffff/48x64.png'" alt="thumb" class="w-10 h-12 rounded overflow-hidden object-cover" />
                <div class="flex-1 min-w-0">
                  <div class="text-sm font-medium text-slate-900 dark:text-white theme-text-main truncate">{{ item.name_cn || item.name }}</div>
                  <div class="text-xs text-slate-500 dark:text-slate-400 theme-text-muted truncate">{{ item.summary || item.description || '' }}</div>
                </div>
                <button
                  @click.stop.prevent="handleToggleFollow(item.id)"
                  class="w-8 h-8 rounded-full flex items-center justify-center text-white bg-transparent hover:bg-slate-200 dark:hover:bg-slate-700"
                  :title="followedBgmIds.has(item.id) ? '取消收藏' : '收藏'"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" :fill="followedBgmIds.has(item.id) ? 'currentColor' : 'none'" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/></svg>
                </button>
              </div>
            </div>
          </div>
        </transition>
      </div>
    </div>

    <!-- 搜索结果 / 推荐内容 -->
    <div v-if="bgmResults.length > 0">
      <div class="flex items-center gap-4 mb-8">
          <h2 class="text-2xl font-bold" :class="['theme-text-main']">搜索结果</h2>
          <span class="text-sm" :class="['theme-text-muted']">共找到 {{ bgmResults.length }} 条记录</span>
          <div class="h-px flex-1" :class="['theme-stat-divider']"></div>
          <button
            @click="clearSearch"
            class="flex items-center gap-2 px-4 py-2 rounded-xl transition-all"
            :class="['theme-btn-secondary']"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="m15 18-6-6 6-6"/>
            </svg>
            返回番剧库
          </button>
        </div>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 xl:grid-cols-5 gap-8">
        <NuxtLink
          v-for="item in bgmResults"
          :key="item.id"
          :to="`/anime/bgm-${item.id}`"
          class="group rounded-3xl overflow-hidden transition-all duration-300 hover:-translate-y-2 flex flex-col relative shadow-sm hover:shadow-xl"
          :class="['theme-anime-card']"
        >
          <!-- 悬浮追番按钮 -->
          <button
            @click.stop.prevent="handleToggleFollow(item.id)"
            class="absolute top-4 right-4 z-20 w-10 h-10 rounded-full flex items-center justify-center transition-all shadow-lg active:scale-90"
            :class="followedBgmIds.has(item.id) ? 'bg-rose-600 text-white force-white' : 'bg-slate-900/60 text-white hover:bg-rose-600 backdrop-blur-md force-white'"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" :fill="followedBgmIds.has(item.id) ? 'currentColor' : 'none'" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5"><path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/></svg>
          </button>

          <div class="relative aspect-[3/4] overflow-hidden">
            <img :src="item.images?.large || item.images?.common || item.image || item.images?.medium || 'https://placehold.jp/334155/ffffff/300x400.png?text=No%20Image'" @error="(e: any) => e.target.src = 'https://placehold.jp/334155/ffffff/300x400.png?text=No%20Image'" referrerpolicy="no-referrer" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" loading="eager" />
            <div class="absolute inset-0 bg-gradient-to-t from-slate-900 via-transparent to-transparent opacity-80"></div>
            <div class="absolute bottom-4 left-4 right-4">
              <span class="text-[10px] font-black px-2 py-0.5 rounded uppercase tracking-wider force-white" :class="['theme-primary-bg']">ID: {{ item.id }}</span>
            </div>
          </div>
          <div class="p-5 flex-1 flex flex-col">
            <h3 class="text-base font-bold line-clamp-2 mb-2 group-hover:text-indigo-400 transition-colors" :class="['theme-anime-title']">{{ item.name_cn || item.name }}</h3>
            <p class="text-xs line-clamp-3 mb-4 leading-relaxed" :class="['theme-anime-summary']">{{ item.summary || '暂无简介' }}</p>
            <div class="mt-auto pt-4 border-t flex items-center justify-between" :class="['theme-anime-divider']">
              <span class="text-yellow-500 font-black text-sm">★ {{ getBangumiScore(item) }}</span>
              <span class="text-[10px] uppercase" :class="['theme-anime-status']">{{ followedBgmIds.has(item.id) ? '已收藏到库' : '未收藏' }}</span>
            </div>
          </div>
        </NuxtLink>
      </div>
    </div>

    <!-- 替换为 社区番剧库（简化） -->
    <div v-else>
      <div class="flex items-center gap-4 mb-6">
        <h2 class="text-2xl font-bold" :class="['theme-text-main']">社区番剧库</h2>
        <div class="h-px flex-1" :class="['theme-stat-divider']"></div>
        <NuxtLink to="/" class="text-sm" :class="['theme-text-link']">返回首页</NuxtLink>
      </div>

      <!-- 本地筛选与分类控制 -->
      <div class="flex flex-col gap-4 mb-6">
        <!-- 第一排：状态、年份、搜索、排序 -->
        <div class="flex flex-col md:flex-row items-start md:items-center gap-4">
          <div class="flex flex-wrap items-center gap-3">
            <span class="text-sm font-semibold" :class="['theme-text-muted']">状态</span>
            <button
              v-for="tab in statusTabs"
              :key="tab.value"
              @click="activeStatus = tab.value"
              class="px-4 py-2 rounded-2xl border transition-all text-sm font-medium"
              :class="activeStatus === tab.value ? 'theme-btn-filter-active' : 'theme-btn-filter'"
            >
              {{ tab.label }}
            </button>
          </div>

          <div class="flex flex-wrap items-center gap-3">
            <span class="text-sm font-semibold" :class="['theme-text-muted']">年份</span>
            <select v-model="selectedYear" class="px-3 py-2 rounded-2xl text-sm" :class="['theme-select']">
              <option :value="undefined">全部年份</option>
              <option v-for="year in yearOptions" :key="year" :value="year">{{ year }}年</option>
            </select>
          </div>

          <div class="flex-1"></div>

          <div class="flex items-center gap-3 w-full md:w-auto">
            <input v-model="searchKeyword" @keyup.enter="handleSearch" placeholder="筛选本地番剧..." class="px-4 py-2 rounded-2xl text-sm flex-1 md:w-48" :class="['theme-input']" />
            <select v-model="sortBy" class="px-3 py-2 rounded-2xl text-sm" :class="['theme-select']">
              <option value="default">默认排序</option>
              <option value="rating">按综合评分</option>
              <option value="year">按年份</option>
            </select>
            <select v-model.number="pageSize" class="px-3 py-2 rounded-2xl text-sm hidden sm:inline-block" :class="['theme-select']">
              <option :value="10">每页 10 条</option>
              <option :value="20">每页 20 条</option>
              <option :value="30">每页 30 条</option>
            </select>
            <button @click="resetFilter" class="px-3 py-2 rounded-2xl text-sm" :class="['theme-btn-secondary']">重置</button>
          </div>
        </div>

        <!-- 第二排：类型筛选 -->
        <div class="flex flex-wrap items-center gap-3">
          <span class="text-sm font-semibold" :class="['theme-text-muted']">类型</span>
          <button
            @click="clearAllGenres"
            class="px-4 py-2 rounded-2xl border transition-all text-sm font-medium"
            :class="selectedGenres.length === 0 ? 'theme-btn-filter-active' : 'theme-btn-filter'"
          >
            全部
          </button>
          <button
            v-for="genre in genreTags"
            :key="genre"
            @click="toggleGenre(genre)"
            class="px-4 py-2 rounded-2xl border transition-all text-sm font-medium"
            :class="selectedGenres.includes(genre) ? 'theme-btn-filter-active' : 'theme-btn-filter'"
          >
            {{ genre }}
          </button>
        </div>
      </div>

      <div v-if="animeStore.listLoading || loading" class="grid grid-cols-2 lg:grid-cols-5 gap-6">
        <div v-for="i in 10" :key="i" class="aspect-[3/4] bg-slate-800 dark:bg-slate-800 rounded-3xl animate-pulse" :class="['theme-card-loading']"></div>
      </div>

      <div v-else class="grid grid-cols-2 lg:grid-cols-5 gap-6">
        <NuxtLink
          v-for="anime in pagedFilteredAnimes"
          :key="anime.id"
          :to="`/anime/${anime.id}`"
          class="group relative aspect-[3/4] rounded-3xl overflow-hidden transition-all duration-300"
          :class="['theme-anime-card']"
        >
          <div class="relative w-full h-full">
            <img v-if="anime.coverUrl" :src="anime.coverUrl" referrerpolicy="no-referrer" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" loading="eager" />
            <div class="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-900/20 to-transparent"></div>
            <div class="absolute bottom-0 left-0 right-0 p-5">
              <h4 class="text-sm font-black text-white force-white line-clamp-1 mb-1">{{ anime.title }}</h4>
              <div class="flex items-center justify-between">
                <span class="text-[10px] text-slate-400 force-white">{{ anime.publishYear }}年</span>
                <span v-if="anime.rating" class="text-yellow-500 font-black text-xs">★ {{ anime.rating }}</span>
              </div>
            </div>
          </div>
        </NuxtLink>
      </div>

      <div v-if="totalPages > 1" class="mt-8 flex items-center justify-center gap-2">
        <button
          @click="goToPage(currentPage - 1)"
          :disabled="currentPage === 1"
          class="px-3 py-2 rounded-xl border text-sm disabled:opacity-40 disabled:cursor-not-allowed transition-all"
          :class="['theme-btn-filter']"
        >
          上一页
        </button>
        <button
          v-for="page in visiblePages"
          :key="`page-${page}`"
          @click="goToPage(page)"
          class="min-w-10 h-10 rounded-xl border text-sm font-bold transition-all"
          :class="currentPage === page ? 'theme-btn-filter-active' : 'theme-btn-filter'"
        >
          {{ page }}
        </button>
        <button
          @click="goToPage(currentPage + 1)"
          :disabled="currentPage === totalPages"
          class="px-3 py-2 rounded-xl border text-sm disabled:opacity-40 disabled:cursor-not-allowed transition-all"
          :class="['theme-btn-filter']"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted, onUnmounted } from 'vue'
import { useAnimeStore } from '~/stores/anime'
import { useUserStore } from '~/stores/user'
import { useAppStore } from '~/stores/app'
import { storeToRefs } from 'pinia'
import { searchBangumi, fetchBangumiCalendar, toggleFollowBangumiApi, fetchFollowStatusByBgmId } from '~/composables/useApi'

// ====== 状态声明 (从 Store 共享以保持持久化) ======
const animeStore = useAnimeStore()
const { bgmSearchKeyword, bgmResults, followedBgmIds, libraryPagination } = storeToRefs(animeStore)
const isSearching = ref(false)
const loading = ref(true)
const userStore = useUserStore()
const appStore = useAppStore()
const router = useRouter()
const route = useRoute()

// 模糊搜索建议相关
const suggestions = ref<any[]>([])
const showSuggestions = ref(false)
let debounceTimer: any = null

// refs to support outside-click closing of suggestion dropdown
const searchInput = ref<HTMLElement | null>(null)
const suggestionBox = ref<HTMLElement | null>(null)

function handleDocumentClick(e: MouseEvent) {
  if (!showSuggestions.value) return
  const target = e.target as Node
  if (searchInput.value && searchInput.value.contains(target)) return
  if (suggestionBox.value && suggestionBox.value.contains(target)) return
  showSuggestions.value = false
}

// 社区番剧库 本地搜索/筛选（从首页迁移）
const searchKeyword = ref('')
const debouncedSearchKeyword = ref('')
const sortBy = ref<'default' | 'rating' | 'year'>('default')
const activeStatus = ref<'all' | 0 | 1 | 2>('all')
const selectedYear = ref<number | undefined>()
const selectedGenres = ref<string[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
let librarySearchDebounceTimer: ReturnType<typeof setTimeout> | null = null

const statusTabs = [
  { label: '全部', value: 'all' as const },
  { label: '连载中', value: 0 as const },
  { label: '已完结', value: 1 as const },
  { label: '待播', value: 2 as const },
]

const genreTags = ['热血', '异世界', '治愈', '搞笑', '恋爱', '战斗', '科幻', '奇幻', '悬疑', '日常']

const currentYear = new Date().getFullYear()
const yearOptions = computed(() => {
  const years = []
  for (let y = currentYear; y >= 2000; y--) {
    years.push(y)
  }
  return years
})

const pagedFilteredAnimes = computed(() => animeStore.animeList)
const totalPages = computed(() => Math.max(1, libraryPagination.value.pages || 1))

const visiblePages = computed(() => {
  const total = totalPages.value
  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => i + 1)
  }
  const start = Math.max(1, currentPage.value - 3)
  const end = Math.min(total, start + 6)
  const adjustedStart = Math.max(1, end - 6)
  return Array.from({ length: end - adjustedStart + 1 }, (_, i) => adjustedStart + i)
})

const toggleGenre = (genre: string) => {
  const index = selectedGenres.value.indexOf(genre)
  if (index === -1) {
    selectedGenres.value.push(genre)
  } else {
    selectedGenres.value.splice(index, 1)
  }
}

const clearAllGenres = () => {
  selectedGenres.value = []
}

const resetFilter = () => {
  searchKeyword.value = ''
  activeStatus.value = 'all'
  selectedYear.value = undefined
  selectedGenres.value = []
  sortBy.value = 'default'
  currentPage.value = 1
}

const goToPage = (page: number) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
}

const handleSearch = () => {
  console.log('Searching for:', searchKeyword.value)
}

const getBangumiScore = (item: Record<string, unknown>): string => {
  const directScore = typeof item.score === 'number' ? item.score : null
  const ratingObj = typeof item.rating === 'object' && item.rating !== null
    ? item.rating as Record<string, unknown>
    : null
  const nestedScore = ratingObj && typeof ratingObj.score === 'number'
    ? ratingObj.score
    : null
  const score = directScore ?? nestedScore
  return typeof score === 'number' ? score.toFixed(1) : '0.0'
}

// 监听关键词变化，实现模糊搜索建议
watch(bgmSearchKeyword, (newVal) => {
  if (debounceTimer) clearTimeout(debounceTimer)
  
  const kw = newVal.trim()
  if (!kw) {
    suggestions.value = []
    showSuggestions.value = false
    return
  }

  debounceTimer = setTimeout(async () => {
    try {
      const data = await searchBangumi(kw)
      suggestions.value = data?.list?.slice(0, 6) || []
      // 检查建议项的关注状态
      suggestions.value.forEach(async item => {
        if (userStore.isLoggedIn) {
          const status = await fetchFollowStatusByBgmId(item.id)
          if (status) followedBgmIds.value.add(item.id)
        }
      })
      showSuggestions.value = suggestions.value.length > 0
    } catch (e) {
      console.error('Fetch suggestions failed', e)
    }
  }, 400)
})

watch([searchKeyword, sortBy, activeStatus], () => {
  currentPage.value = 1
})

watch(pageSize, () => {
  currentPage.value = 1
})

watch(totalPages, (pages) => {
  if (currentPage.value > pages) {
    currentPage.value = pages
  }
})

const loadLibraryPage = async () => {
  await animeStore.loadAnimeLibraryPage({
    pageNum: currentPage.value,
    pageSize: pageSize.value,
    title: debouncedSearchKeyword.value.trim() || undefined,
    status: activeStatus.value === 'all' ? undefined : activeStatus.value,
    publishYear: selectedYear.value,
    genres: selectedGenres.value.length > 0 ? selectedGenres.value : undefined,
    sortBy: sortBy.value
  })
}

watch(searchKeyword, (value) => {
  if (librarySearchDebounceTimer) {
    clearTimeout(librarySearchDebounceTimer)
  }
  librarySearchDebounceTimer = setTimeout(() => {
    debouncedSearchKeyword.value = value
  }, 300)
})

watch([currentPage, pageSize, sortBy, activeStatus, selectedYear], async () => {
  await loadLibraryPage()
})

watch(selectedGenres, async () => {
  await loadLibraryPage()
}, { deep: true })

watch(debouncedSearchKeyword, async () => {
  await loadLibraryPage()
})

const selectSuggestion = (item: any) => {
  showSuggestions.value = false
  navigateTo(`/anime/bgm-${item.id}`)
}

const handleBlur = () => {
  setTimeout(() => {
    showSuggestions.value = false
  }, 200)
}

// ====== 获取今日放送作为推荐 ======
const { data: calendar } = await useAsyncData('bangumiCalendar', () => fetchBangumiCalendar())

const todayItems = computed(() => {
  if (!calendar.value) return []
  const todayIndex = new Date().getDay() === 0 ? 7 : new Date().getDay()
  const dayData = calendar.value.find((d: any) => d.weekday.id === todayIndex)
  if (!dayData || !dayData.items) return []
  
  return dayData.items.slice(0, 10).map((item: any) => ({
    id: item.id,
    title: item.name_cn || item.name,
    coverUrl: item.images?.large || item.images?.common || '',
    publishYear: item.air_date ? item.air_date.substring(0, 4) : '?',
  }))
})

// cleanup listener on unmount — 必须在顶层注册，不能放在 async onMounted 的 await 之后
onUnmounted(() => {
  document.removeEventListener('click', handleDocumentClick)
  if (librarySearchDebounceTimer) {
    clearTimeout(librarySearchDebounceTimer)
    librarySearchDebounceTimer = null
  }
})

onMounted(async () => {
  loading.value = true
  // 确保社区番剧库数据已加载
  try {
    await loadLibraryPage()
  } catch (e) {
    console.error('Failed to load anime list on anime page', e)
  }

  // register document click listener for suggestion closing
  document.addEventListener('click', handleDocumentClick)

  loading.value = false

  // 处理登录返回后的逻辑
  if (route.query.followedBgmId) {
    const bgmId = parseInt(route.query.followedBgmId as string)
    if (userStore.isLoggedIn) {
      handleToggleFollow(bgmId)
      // 清理 URL
      router.replace('/anime')
    }
  }

  // 加载今日热播项的关注状态
  if (userStore.isLoggedIn) {
    todayItems.value.forEach(async (item: any) => {
      const status = await fetchFollowStatusByBgmId(item.id)
      if (status) followedBgmIds.value.add(item.id)
    })
  }
})

// ====== 搜索逻辑 ======
const handleBgmSearch = async () => {
  if (!bgmSearchKeyword.value.trim()) return

  isSearching.value = true
  try {
    // 先执行 Bangumi 搜索
    const data = await searchBangumi(bgmSearchKeyword.value)
    bgmResults.value = data?.list || []

    // 如果有结果，先进行批量导入并等待完成，再刷新本地库，保证页面显示为已收录内容
    if (bgmResults.value.length > 0) {
      try {
        appStore.showMessage('正在将搜索结果收录到番剧库，完成后会刷新展示（可能需要一些时间）')
        const ids = bgmResults.value.map((i: any) => i.id)
        await importFromBangumi(ids)
        await loadLibraryPage()
        appStore.showMessage('收录完成，已刷新本地番剧库')
      } catch (ie) {
        console.warn('自动收录部分或全部失败', ie)
        appStore.showMessage('收录部分失败，已展示搜索结果', 'warning')
      }
    }

    // 加载搜索结果的关注状态（并行处理）
    if (userStore.isLoggedIn) {
      bgmResults.value.forEach(async item => {
        const status = await fetchFollowStatusByBgmId(item.id)
        if (status) followedBgmIds.value.add(item.id)
      })
    }
  } catch (e) {
    console.error('BGM Search failed', e)
    appStore.showMessage('搜索失败，请稍后重试', 'error')
  } finally {
    isSearching.value = false
  }
}

import { importFromBangumi } from '~/composables/useApi'

const importAllResults = async () => {
  if (!bgmResults.value || bgmResults.value.length === 0) return
  try {
    const ids = bgmResults.value.map((i: any) => i.id)
    await importFromBangumi(ids)
    // 刷新社区番剧库
    await loadLibraryPage()
    appStore.showMessage('已将搜索结果全部收录到番剧库')
  } catch (e) {
    console.error('Import all failed', e)
    appStore.showMessage('收录失败，请检查网络或后端日志', 'error')
  }
}

// ====== 追番/同步 逻辑 (合并) ======
const handleToggleFollow = async (bgmId: number) => {
  if (!userStore.isLoggedIn) {
    // 未登录跳转，带上 bgmId 以便跳回后自动操作
    router.push(`/login?redirect=/anime&followedBgmId=${bgmId}`)
    return
  }
  
  try {
    const followed = await toggleFollowBangumiApi(bgmId)
    if (followed) {
      followedBgmIds.value.add(bgmId)
      appStore.showMessage('加入追番列表成功！')
    } else {
      followedBgmIds.value.delete(bgmId)
      appStore.showMessage('已取消追番', 'info')
    }
  } catch (e) {
    console.error('Follow failed', e)
    appStore.showMessage('操作失败，请检查网络', 'error')
  }
}

const clearSearch = () => {
  bgmSearchKeyword.value = ''
  bgmResults.value = []
  suggestions.value = []
  showSuggestions.value = false
}
</script>
