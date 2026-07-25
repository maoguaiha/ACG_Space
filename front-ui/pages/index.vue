<template>
  <div>
    <!-- Hero Carousel Section -->
    <section class="relative h-[400px] md:h-[500px] w-full overflow-hidden">
      <!-- Carousel Track -->
      <div
        class="flex w-full h-full transition-transform duration-700 ease-in-out"
        :style="{ transform: `translateX(-${currentIndex * 100}%)` }"
      >
        <component
          :is="item.id ? 'a' : 'div'"
          v-for="(item, index) in heroItems"
          :key="index"
          :href="item.id ? `/anime/${item.id}` : undefined"
          class="w-full h-full flex-shrink-0 relative cursor-pointer"
          @click.prevent="item.id && navigateTo(`/anime/${item.id}`)"
        >
          <!-- Background Image with Gradient Overlay -->
          <div class="absolute inset-0 bg-slate-900">
            <img
              v-if="item.coverUrl"
              :src="item.coverUrl"
              :alt="item.title"
              referrerpolicy="no-referrer"
              class="absolute inset-0 w-full h-full object-cover opacity-80"
            />
            <div v-else
              class="absolute inset-0 bg-cover bg-center bg-no-repeat opacity-80"
              :style="{ backgroundImage: `url(${item.bgImage})` }"
            ></div>
            <div class="absolute inset-0" :style="{ background: `linear-gradient(to top, var(--hero-bg-from), var(--hero-bg-to) 100%)` }"></div>
          </div>

          <!-- Content -->
          <div class="container mx-auto px-4 h-full relative z-10 flex flex-col justify-end pb-20">
            <div class="max-w-2xl">
              <span class="inline-block px-3 py-1.5 mb-4 rounded-full text-xs font-bold tracking-wider drop-shadow-md" :class="['theme-carousel-tag']">
                {{ item.tag }}
              </span>
              <h1 class="text-3xl md:text-5xl font-extrabold tracking-tight mb-4 leading-tight drop-shadow-xl" :class="['theme-carousel-title']">
                {{ item.title }}
              </h1>
              <p class="text-base md:text-lg line-clamp-2 drop-shadow-lg" :class="['theme-carousel-desc']">
                {{ item.desc }}
              </p>
            </div>
          </div>
        </component>
      </div>

      <!-- Carousel Controls -->
      <div class="absolute bottom-6 left-0 w-full z-20">
        <div class="container mx-auto px-4 flex items-center justify-between">
          <div class="flex gap-2">
            <button
              v-for="(_, index) in heroItems"
              :key="'indicator-'+index"
              @click="goToSlide(index)"
              class="h-1.5 rounded-full transition-all duration-300"
              :class="currentIndex === index ? 'w-8 bg-indigo-500' : 'w-4 bg-white/30 hover:bg-white/50'"
            ></button>
          </div>
        </div>
      </div>
    </section>

    <!-- 今日放送热播 (来自 番剧库 页面) -->
    <section class="container mx-auto px-4 py-12">
      <div class="flex items-center gap-4 mb-8">
        <h2 class="text-2xl font-bold text-white">今日放送热播</h2>
        <div class="h-px flex-1 bg-slate-800"></div>
        <NuxtLink to="/anime" class="text-sm text-indigo-400 hover:underline">进入番剧库</NuxtLink>
      </div>

      <div v-if="!todaySchedule || todaySchedule.length === 0" class="grid grid-cols-2 lg:grid-cols-5 gap-6">
        <div v-for="i in 10" :key="i" class="aspect-[3/4] bg-slate-800 rounded-3xl animate-pulse"></div>
      </div>

      <div v-else class="grid grid-cols-2 lg:grid-cols-5 gap-6">
        <NuxtLink
          v-for="item in todaySchedule"
          :key="item.id"
          :to="`/anime/bgm-${item.id}`"
          class="group relative aspect-[3/4] rounded-3xl overflow-hidden bg-slate-800 border border-slate-700/50 hover:border-indigo-500 transition-all duration-300"
        >
          <button
            @click.stop.prevent="() => {}"
            class="absolute top-3 right-3 z-20 w-8 h-8 rounded-full flex items-center justify-center transition-all shadow-lg active:scale-90"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5"><path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/></svg>
          </button>

          <img :src="item.coverUrl" referrerpolicy="no-referrer" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
          <div class="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-900/20 to-transparent"></div>
          <div class="absolute bottom-0 left-0 right-0 p-5">
            <h4 class="text-sm font-black text-white force-white line-clamp-1 mb-1">{{ item.title }}</h4>
            <div class="flex items-center justify-between">
              <span class="text-[10px] text-slate-400 force-white">{{ item.publishYear }}年</span>
            </div>
          </div>
        </NuxtLink>
      </div>
    </section>

    <!-- 新番时间表 Section -->
    <section class="container mx-auto px-4 pb-20">
      <div class="flex items-center gap-3 mb-8">
        <h2 class="text-2xl font-bold text-white">新番时间表</h2>
        <div class="h-px flex-1 bg-slate-800"></div>
        <span class="text-[10px] font-bold text-slate-500 uppercase tracking-widest px-3 py-1 border border-slate-800 rounded-full">Bangumi Realtime</span>
      </div>

      <!-- Days Tabs -->
      <div class="flex gap-2 overflow-x-auto pb-4 hide-scrollbar mb-8">
        <button
          v-for="(day, index) in daysList"
          :key="index"
          @click="activeDay = index"
          class="flex-shrink-0 min-w-[100px] p-4 rounded-2xl border transition-all duration-300 flex flex-col items-center gap-1"
          :class="activeDay === index ? 'bg-indigo-600 border-indigo-500 text-white shadow-xl shadow-indigo-600/20' : 'bg-slate-800/40 border-slate-700/50 text-slate-400 hover:border-slate-600'"
        >
          <span class="text-[10px] font-bold opacity-60">{{ day.date }}</span>
          <span class="text-sm font-black">{{ day.name }}</span>
        </button>
      </div>

      <!-- Schedule Content List -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <NuxtLink
          v-for="item in currentSchedule"
          :key="item.id"
          :to="`/anime/bgm-${item.id}`"
          class="flex items-center gap-4 bg-slate-800/30 hover:bg-slate-800/60 border border-slate-700/30 p-3 rounded-2xl transition-all group"
        >
          <div class="w-14 h-14 rounded-xl overflow-hidden bg-slate-700 flex-shrink-0">
            <img v-if="item.coverUrl" :src="item.coverUrl" referrerpolicy="no-referrer" class="w-full h-full object-cover" />
          </div>
          <div class="min-w-0">
            <h4 class="text-sm font-bold text-slate-100 truncate group-hover:text-indigo-400 transition-colors">{{ item.title }}</h4>
            <p class="text-[11px] text-slate-500 mt-1">{{ item.publishYear }}年 · 正在播出</p>
          </div>
        </NuxtLink>
      </div>
    </section>

    <!-- 社区番剧库模块已移除（迁移至 /anime） -->

    
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useAnimeStore } from '~/stores/anime'
import { fetchBangumiCalendar, fetchFeaturedAnime } from '~/composables/useApi'

// ====== 数据层 ======
const animeStore = useAnimeStore()

// 预取数据（SSR 阶段后端可能未就绪，必须用 try-catch 兜底，防止页面渲染崩溃）
await useAsyncData('homeData', async () => {
  try {
    await Promise.all([
      animeStore.loadAnimeList(),
    ])
  } catch {
    console.warn('[SSR] 番剧列表预取失败，使用空列表兜底')
  }
  return true
})

const { data: bangumiCalendar } = await useAsyncData('bangumiCalendar',
  () => fetchBangumiCalendar().catch(() => [])
)
const { data: featuredAnimeList } = await useAsyncData('featuredAnime',
  () => fetchFeaturedAnime().catch(() => [])
)

// ====== 搜索与筛选逻辑 ======
const searchKeyword = ref('')
const sortBy = ref<'default' | 'rating' | 'year'>('default')
const activeStatus = ref<'all' | 0 | 1 | 2>('all')

const statusTabs = [
  { label: '全部', value: 'all' as const },
  { label: '连载中', value: 0 as const },
  { label: '已完结', value: 1 as const },
  { label: '待播', value: 2 as const },
]

const filteredAnimes = computed(() => {
  let list = [...animeStore.animeList]
  
  if (activeStatus.value !== 'all') {
    list = list.filter(a => a.status === activeStatus.value)
  }
  
  const kw = searchKeyword.value.trim().toLowerCase()
  if (kw) {
    list = list.filter(a => a.title.toLowerCase().includes(kw) || a.titleOriginal?.toLowerCase().includes(kw))
  }
  
  if (sortBy.value === 'rating') {
    list.sort((a, b) => (b.rating ?? 0) - (a.rating ?? 0))
  } else if (sortBy.value === 'year') {
    list.sort((a, b) => (b.publishYear ?? 0) - (a.publishYear ?? 0))
  }
  
  return list
})

const resetFilter = () => {
  searchKeyword.value = ''
  activeStatus.value = 'all'
  sortBy.value = 'default'
}

const handleSearch = () => {
  // 搜索逻辑已通过 computed 实现，此处可添加 UI 反馈
  console.log('Searching for:', searchKeyword.value)
}

// ====== 轮播图逻辑 ======
const currentIndex = ref(0)
let autoplayInterval: any = null

const heroItems = computed(() => {
  const staticBanners = [{
    id: null,
    title: '探索属于你的二次元新世界',
    desc: '每日更新最高质量的番剧推荐、硬核漫评与业界资讯。在这里，找到懂你的同好。',
    tag: 'ACG Space 推荐',
    tagColor: 'bg-indigo-600',
    bgImage: 'https://images.unsplash.com/photo-1578632767115-351597cf2477?q=80&w=2000&auto=format&fit=crop',
    coverUrl: '',
  }]
  
  const featured = featuredAnimeList.value
  if (!featured || featured.length === 0) return staticBanners
  
  return featured.map(anime => ({
    id: anime.id,
    title: anime.title,
    desc: anime.summary || '精彩剧情，等你探索。',
    tag: animeStore.getStatusLabel(anime.status),
    tagColor: animeStore.getStatusClass(anime.status),
    bgImage: anime.coverUrl,
    coverUrl: anime.coverUrl,
  }))
})

const goToSlide = (index: number) => {
  currentIndex.value = index
  resetAutoplay()
}

const resetAutoplay = () => {
  if (autoplayInterval) clearInterval(autoplayInterval)
  autoplayInterval = setInterval(() => {
    currentIndex.value = (currentIndex.value + 1) % heroItems.value.length
  }, 6000)
}

// ====== 时间表逻辑 ======
const dayNames = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

// 在 setup 阶段就计算好今天星期几，确保 SSR 和客户端初始值一致，避免 hydration mismatch
const now = new Date()
const todayIndex = now.getDay() === 0 ? 6 : now.getDay() - 1
const activeDay = ref(todayIndex)

const daysList = ref(dayNames.map((name, index) => {
  const diff = index - todayIndex
  const date = new Date(now)
  date.setDate(now.getDate() + diff)
  return { name, date: `${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}` }
}))

// 固定今天的番剧列表（不受 activeDay 点击影响）
const todaySchedule = computed(() => getScheduleForDay(todayIndex))

// 随用户点击星期标签变化的番剧列表
const currentSchedule = computed(() => getScheduleForDay(activeDay.value))

function getScheduleForDay(dayIndex: number) {
  if (!bangumiCalendar.value) return []
  const targetId = dayIndex + 1
  const dayData = bangumiCalendar.value.find((d: any) => d.weekday.id === targetId)
  if (!dayData || !dayData.items) return []
  return dayData.items.map((item: any) => ({
    id: item.id,
    title: item.name_cn || item.name,
    coverUrl: item.images?.large || item.images?.common || '',
    publishYear: item.air_date ? item.air_date.substring(0, 4) : '?',
  }))
}

onMounted(() => {
  resetAutoplay()
})

onUnmounted(() => {
  if (autoplayInterval) clearInterval(autoplayInterval)
})
</script>

<style scoped>
.hide-scrollbar::-webkit-scrollbar { display: none; }
.hide-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
</style>
