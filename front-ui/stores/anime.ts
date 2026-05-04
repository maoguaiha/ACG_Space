import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  fetchAnimeList,
  fetchAnimeDetail,
  fetchAnimeDetailByBgmId,
  fetchAnimeLibraryPage,
  type AnimeLibraryPageQuery,
  type BizAnime
} from '~/composables/useApi'

/**
 * 番剧数据 Store
 * 负责缓存番剧列表与详情，避免重复请求
 */
export const useAnimeStore = defineStore('anime', () => {
  // ====== 状态定义 ======

  /** 番剧列表 */
  const animeList = ref<BizAnime[]>([])

  /** 番剧详情缓存 Map（key 为番剧 id） */
  const animeDetailCache = ref<Map<string, BizAnime>>(new Map())

  /** 列表加载状态 */
  const listLoading = ref(false)
  const libraryPagination = ref({
    total: 0,
    current: 1,
    size: 20,
    pages: 1
  })

  /** 详情加载状态 */
  const detailLoading = ref(false)

  /** 错误信息 */
  const error = ref<string | null>(null)

  // ====== 计算属性 ======

  /** 连载中的番剧 */
  const ongoingAnimes = computed(() =>
    animeList.value.filter(a => a.status === 0)
  )

  /** 已完结的番剧 */
  const finishedAnimes = computed(() =>
    animeList.value.filter(a => a.status === 1)
  )

  // ====== 搜索状态 (持久化，用于从详情页返回时恢复) ======
  const bgmSearchKeyword = ref('')
  const bgmResults = ref<any[]>([])
  /** 记录在列表页中搜索到的番剧的关注状态 (BGM ID) */
  const followedBgmIds = ref<Set<number>>(new Set())

  // ====== Actions ======

  /**
   * 加载番剧列表
   * 若已有数据且非强制刷新，则跳过请求
   */
  async function loadAnimeList(force = false) {
    if (animeList.value.length > 0 && !force) return

    listLoading.value = true
    error.value = null
    try {
      animeList.value = await fetchAnimeList()
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '加载番剧列表失败'
      console.error('[AnimeStore] loadAnimeList error:', e)
    } finally {
      listLoading.value = false
    }
  }

  async function loadAnimeLibraryPage(query: AnimeLibraryPageQuery) {
    listLoading.value = true
    error.value = null
    try {
      const page = await fetchAnimeLibraryPage(query)
      animeList.value = page.records
      libraryPagination.value = {
        total: page.total,
        current: page.current,
        size: page.size,
        pages: page.pages
      }
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '加载社区番剧库失败'
      console.error('[AnimeStore] loadAnimeLibraryPage error:', e)
    } finally {
      listLoading.value = false
    }
  }

  /**
   * 加载番剧详情
   * 优先从缓存 Map 中读取，减少不必要的网络请求
   * 支持本地 ID 或 bgm- 前缀的 Bangumi ID
   */
  async function loadAnimeDetail(id: string): Promise<BizAnime | null> {
    // 命中缓存直接返回
    if (animeDetailCache.value.has(id)) {
      return animeDetailCache.value.get(id)!
    }

    detailLoading.value = true
    error.value = null
    try {
      let detail: BizAnime
      if (id.startsWith('bgm-')) {
        const bgmId = parseInt(id.replace('bgm-', ''))
        // Use the composable API function which resolves base URL correctly
        detail = await fetchAnimeDetailByBgmId(bgmId)
      } else {
        detail = await fetchAnimeDetail(id)
      }
      animeDetailCache.value.set(id, detail)
      return detail
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '加载番剧详情失败'
      console.error(`[AnimeStore] loadAnimeDetail(${id}) error:`, e)
      return null
    } finally {
      detailLoading.value = false
    }
  }

  /**
   * 根据番剧状态获取中文标签
   */
  function getStatusLabel(status: BizAnime['status']): string {
    const map: Record<number, string> = { 0: '连载中', 1: '已完结', 2: '未开播' }
    return map[status] ?? '未知'
  }

  /**
   * 根据番剧状态获取徽章颜色 class
   */
  function getStatusClass(status: BizAnime['status']): string {
    const map: Record<number, string> = {
      0: 'bg-indigo-600/90',
      1: 'bg-slate-600/90',
      2: 'bg-amber-600/90'
    }
    return map[status] ?? 'bg-slate-600/90'
  }

  return {
    // 状态
    animeList,
    animeDetailCache,
    listLoading,
    libraryPagination,
    detailLoading,
    error,
    // 搜索状态
    bgmSearchKeyword,
    bgmResults,
    followedBgmIds,
    // 计算属性
    ongoingAnimes,
    finishedAnimes,
    // Actions
    loadAnimeList,
    loadAnimeLibraryPage,
    loadAnimeDetail,
    getStatusLabel,
    getStatusClass
  }
})
