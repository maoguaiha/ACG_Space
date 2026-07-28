<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAnimeStore } from '~/stores/anime'
import { useUserStore } from '~/stores/user'
import { fetchFollowList, type BizAnime } from '~/composables/useApi'

const animeStore = useAnimeStore()
const userStore = useUserStore()
const router = useRouter()

const followList = ref<BizAnime[]>([])
const pending = ref(true)
const error = ref<string | null>(null)

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  try {
    followList.value = await fetchFollowList()
  } catch (e: any) {
    error.value = e?.message || '加载失败'
    console.error('加载追番列表失败:', e)
  } finally {
    pending.value = false
  }
})
</script>

<template>
  <div class="py-12">
    <div class="container mx-auto px-4">
      <!-- 返回按钮 -->
      <div class="mb-8">
        <button @click="router.back()" class="group w-10 h-10 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center transition-all hover:border-indigo-500/50 hover:bg-indigo-500/10" title="返回上一页">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m15 18-6-6 6-6"/></svg>
        </button>
      </div>
      <div class="flex items-center justify-between mb-10">
        <div>
          <h1 class="text-3xl font-bold theme-text-main mb-2">我的追番</h1>
          <p class="theme-text-muted text-sm">在这里查看你收藏的所有心仪番剧</p>
        </div>
      </div>

      <!-- 加载中 -->
      <div v-if="pending" class="flex flex-col items-center justify-center py-20 text-slate-500">
        <div class="w-10 h-10 border-4 border-indigo-500/30 border-t-indigo-500 rounded-full animate-spin mb-4"></div>
        <p>努力加载追番清单中...</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="!followList || followList.length === 0" class="flex flex-col items-center justify-center py-32 text-slate-500 bg-white/5 rounded-3xl border border-white/5">
        <div class="w-20 h-20 bg-slate-900 rounded-full flex items-center justify-center mb-6 border border-slate-800">
          <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/></svg>
        </div>
        <h3 class="text-lg font-medium mb-2 theme-text-main">你的追番列表还是空的</h3>
        <p class="mb-8 theme-text-muted">去发现一些有趣的番剧并加入追番吧！</p>
        <NuxtLink to="/" class="px-8 py-3 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl font-bold transition-all shadow-lg shadow-indigo-600/20 active:scale-95">
          去番剧库逛逛
        </NuxtLink>
      </div>

      <!-- 列表内容 -->
      <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-6">
        <NuxtLink
          v-for="(anime, index) in followList"
          :key="anime.id"
          :to="`/anime/${anime.id}`"
          class="group bg-slate-900/50 rounded-2xl overflow-hidden border border-white/5 hover:border-indigo-500/50 transition-all hover:-translate-y-2 hover:shadow-2xl hover:shadow-indigo-500/10 stagger-item"
          :style="{ animationDelay: `${index * 0.06}s` }"
        >
          <!-- 封面图 -->
          <div class="relative aspect-[2/3] overflow-hidden">
            <img
              :src="anime.coverUrl"
              :alt="anime.title"
              class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
            />
            <div class="absolute inset-0 bg-gradient-to-t from-slate-900 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity"></div>
            
            <!-- 状态标签 -->
            <div class="absolute top-2 left-2">
              <span class="text-[10px] font-bold text-white px-2 py-0.5 rounded-full shadow-lg" :class="animeStore.getStatusClass(anime.status)">
                {{ animeStore.getStatusLabel(anime.status) }}
              </span>
            </div>
          </div>

          <!-- 文字信息 -->
          <div class="p-3">
            <h3 class="text-white font-bold text-sm line-clamp-1 group-hover:text-indigo-400 transition-colors">
              {{ anime.title }}
            </h3>
            <div class="flex items-center justify-between mt-2">
              <span class="text-xs text-slate-500">{{ anime.publishYear }}年</span>
              <div class="flex items-center gap-0.5 text-yellow-500">
                <svg xmlns="http://www.w3.org/2000/svg" width="10" height="10" viewBox="0 0 24 24" fill="currentColor"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
                <span class="text-[10px] font-bold">{{ anime.rating }}</span>
              </div>
            </div>
          </div>
        </NuxtLink>
      </div>
    </div>
  </div>
</template>
