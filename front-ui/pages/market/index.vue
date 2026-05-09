<template>
  <div class="min-h-screen pb-20">
    <!-- Header Section -->
    <section class="relative py-12 overflow-hidden">
      <!-- Background Glow -->
      <div class="absolute inset-0 bg-gradient-to-b from-emerald-900/20 via-slate-900 to-slate-900"></div>
      <div class="absolute top-20 left-1/4 w-[400px] h-[400px] bg-emerald-600/10 rounded-full blur-[120px]"></div>
      <div class="absolute top-20 right-1/4 w-[400px] h-[400px] bg-teal-600/10 rounded-full blur-[120px]"></div>

      <div class="container mx-auto px-4 relative z-10">
        <div class="max-w-4xl mx-auto">
          <div class="text-center mb-8">
            <span class="inline-flex items-center gap-2 px-4 py-1.5 rounded-full text-xs font-bold tracking-wider bg-gradient-to-r from-emerald-400/20 to-teal-500/20 text-emerald-400 border border-emerald-400/30 mb-4">
              <span class="text-lg">🏪</span>
              FLEA MARKET
            </span>
            <h1 class="text-4xl font-black text-white mb-2">跳蚤市场</h1>
            <p class="text-slate-400">买卖数字资产，官方收取 1% 交易税</p>
          </div>

          <!-- User Balance -->
          <div class="flex items-center justify-center gap-4 mb-8">
            <div class="flex items-center gap-3 px-6 py-3 bg-gradient-to-r from-amber-500/10 to-orange-500/10 rounded-2xl border border-amber-500/20">
              <div class="w-10 h-10 rounded-full bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center">
                <span class="text-lg">💰</span>
              </div>
              <div>
                <p class="text-slate-400 text-xs">我的积分</p>
                <p class="text-xl font-bold text-amber-400">{{ userPoints.toLocaleString() }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Filters Section -->
    <section class="container mx-auto px-4">
      <div class="max-w-4xl mx-auto">
        <!-- Search & Filter Bar -->
        <div class="flex flex-col md:flex-row gap-4 mb-6">
          <!-- Search -->
          <div class="flex-1 relative">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
            <input
              v-model="searchQuery"
              type="text"
              placeholder="搜索商品名称..."
              class="w-full pl-12 pr-4 py-3 rounded-xl bg-slate-800/50 border border-slate-700 text-white placeholder-slate-500 focus:border-emerald-500 focus:outline-none transition-colors"
            />
          </div>
          <!-- Rarity Filter -->
          <select
            v-model="filterRarity"
            class="px-4 py-3 rounded-xl bg-slate-800/50 border border-slate-700 text-white focus:border-emerald-500 focus:outline-none transition-colors"
          >
            <option value="">全部稀有度</option>
            <option value="SSR">SSR</option>
            <option value="SR">SR</option>
            <option value="R">R</option>
            <option value="N">N</option>
          </select>
          <!-- Type Filter -->
          <select
            v-model="filterType"
            class="px-4 py-3 rounded-xl bg-slate-800/50 border border-slate-700 text-white focus:border-emerald-500 focus:outline-none transition-colors"
          >
            <option value="">全部分类</option>
            <option value="角色">角色</option>
            <option value="武器">武器</option>
            <option value="服装">服装</option>
            <option value="材料">材料</option>
          </select>
          <!-- Sort -->
          <select
            v-model="sortBy"
            class="px-4 py-3 rounded-xl bg-slate-800/50 border border-slate-700 text-white focus:border-emerald-500 focus:outline-none transition-colors"
          >
            <option value="latest">最新上架</option>
            <option value="price_asc">价格从低到高</option>
            <option value="price_desc">价格从高到低</option>
          </select>
        </div>

        <!-- Results Count -->
        <div class="flex items-center justify-between mb-6">
          <p class="text-slate-400 text-sm">共找到 <span class="text-white font-bold">{{ filteredListings.length }}</span> 件商品</p>
          <NuxtLink to="/assets" class="text-sm text-emerald-400 hover:text-emerald-300 transition-colors">
            我要上架 →
          </NuxtLink>
        </div>

        <!-- Loading State -->
        <div v-if="loadingListings" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          <div v-for="i in 8" :key="i" class="bg-slate-800/50 backdrop-blur-sm rounded-2xl overflow-hidden border border-slate-700/50 animate-pulse">
            <div class="aspect-[3/4] bg-slate-700/50"></div>
            <div class="p-3 space-y-2">
              <div class="h-4 bg-slate-700/50 rounded w-3/4"></div>
              <div class="h-3 bg-slate-700/50 rounded w-1/2"></div>
            </div>
          </div>
        </div>

        <!-- Error State -->
        <div v-else-if="errorListings" class="text-center py-20">
          <div class="w-24 h-24 mx-auto mb-6 rounded-full bg-red-500/10 flex items-center justify-center">
            <span class="text-5xl">❌</span>
          </div>
          <h3 class="text-xl font-bold text-white mb-2">加载失败</h3>
          <p class="text-slate-400 mb-6">{{ errorListings }}</p>
          <button @click="fetchListings" class="inline-flex items-center gap-2 px-6 py-3 rounded-xl font-bold bg-gradient-to-r from-emerald-500 to-teal-500 text-white shadow-lg hover:shadow-emerald-500/30 transition-all">
            <span>重新加载</span>
          </button>
        </div>

        <!-- Listings Grid -->
        <div v-else class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          <template v-if="filteredListings.length > 0">
            <div
              v-for="(listing, index) in filteredListings"
              :key="listing.id"
              class="group relative bg-slate-800/50 backdrop-blur-sm rounded-2xl overflow-hidden border border-slate-700/50 hover:border-emerald-500/50 transition-all duration-300 hover:shadow-xl hover:shadow-emerald-500/10 cursor-pointer"
              :class="getRarityBorderClass(listing.rarity)"
              @click="openListingDetail(listing)"
              :style="{ animationDelay: `${index * 50}ms` }"
            >
              <!-- Image -->
              <div class="aspect-[3/4] relative overflow-hidden">
                <img :src="listing.image" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
                <div class="absolute inset-0 bg-gradient-to-t from-slate-900 via-transparent to-transparent"></div>
                <!-- Price Tag -->
                <div class="absolute bottom-3 left-3 px-3 py-1.5 rounded-xl bg-slate-900/90 backdrop-blur-sm">
                  <p class="text-sm font-bold text-amber-400">
                    <span class="text-xs">💰</span> {{ listing.price.toLocaleString() }}
                  </p>
                </div>
                <!-- Rarity Badge -->
                <div
                  class="absolute top-2 right-2 px-2 py-0.5 rounded-full text-[10px] font-bold"
                  :class="getRarityBadgeClass(listing.rarity)"
                >
                  {{ listing.rarity }}
                </div>
              </div>
              <!-- Info -->
              <div class="p-3">
                <h4 class="text-sm font-bold text-white truncate mb-1">{{ listing.name }}</h4>
                <div class="flex items-center justify-between">
                  <span class="text-xs text-slate-500">{{ listing.type }}</span>
                  <div class="flex items-center gap-1">
                    <div class="w-5 h-5 rounded-full bg-slate-700 overflow-hidden">
                      <img :src="listing.sellerAvatar" class="w-full h-full object-cover" />
                    </div>
                    <span class="text-[10px] text-slate-400">{{ listing.sellerName }}</span>
                  </div>
                </div>
              </div>
            </div>
          </template>
          <template v-else>
            <!-- Empty State -->
            <div class="col-span-full text-center py-20">
              <div class="w-24 h-24 mx-auto mb-6 rounded-full bg-slate-800/50 flex items-center justify-center">
                <span class="text-5xl">🔍</span>
              </div>
              <h3 class="text-xl font-bold text-white mb-2">暂无相关商品</h3>
              <p class="text-slate-400 mb-6">换个搜索条件试试吧</p>
            </div>
          </template>
        </div>

        <!-- Load More -->
        <div v-if="hasMore" class="text-center mt-10">
          <button
            @click="loadMore"
            :disabled="isLoading"
            class="px-8 py-3 rounded-xl font-bold bg-slate-800/50 text-white hover:bg-slate-700 transition-colors disabled:opacity-50"
          >
            <span v-if="isLoading" class="flex items-center gap-2">
              <svg class="animate-spin h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              加载中...
            </span>
            <span v-else>加载更多</span>
          </button>
        </div>
      </div>
    </section>

    <!-- Listing Detail Modal -->
    <Teleport to="body">
      <transition
        enter-active-class="transition ease-out duration-300"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="transition ease-in duration-200"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <div v-if="showListingDetail" class="fixed inset-0 z-[100] flex items-center justify-center p-4" @click="closeListingDetail">
          <div class="absolute inset-0 bg-slate-900/90 backdrop-blur-sm"></div>

          <div class="relative z-10 w-full max-w-md bg-slate-800/90 backdrop-blur-xl rounded-3xl border border-slate-700/50 overflow-hidden" @click.stop>
            <!-- Header Image -->
            <div class="relative h-64 overflow-hidden">
              <img :src="selectedListing?.image" class="w-full h-full object-cover" />
              <div class="absolute inset-0 bg-gradient-to-t from-slate-900 via-slate-900/50 to-transparent"></div>
              <button @click="closeListingDetail" class="absolute top-4 right-4 w-8 h-8 rounded-full bg-slate-900/50 backdrop-blur-sm flex items-center justify-center text-white/70 hover:text-white transition-colors">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
              <!-- Price Overlay -->
              <div class="absolute bottom-4 left-4 right-4">
                <div class="flex items-end justify-between">
                  <div class="px-4 py-2 rounded-xl bg-slate-900/90 backdrop-blur-sm">
                    <p class="text-xs text-slate-400 mb-1">售价</p>
                    <p class="text-2xl font-black text-amber-400">
                      <span class="text-sm">💰</span> {{ selectedListing?.price.toLocaleString() }}
                    </p>
                  </div>
                  <span class="px-2 py-0.5 rounded-full text-xs font-bold" :class="getRarityBadgeClass(selectedListing?.rarity || '')">
                    {{ selectedListing?.rarity }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Content -->
            <div class="p-6">
              <h3 class="text-xl font-black text-white mb-1">{{ selectedListing?.name }}</h3>
              <p class="text-sm text-slate-400 mb-4">{{ selectedListing?.type }} · {{ selectedListing?.source }}</p>

              <!-- Seller Info -->
              <div class="flex items-center gap-3 p-3 bg-slate-900/50 rounded-xl mb-4">
                <div class="w-10 h-10 rounded-full bg-slate-700 overflow-hidden">
                  <img :src="selectedListing?.sellerAvatar" class="w-full h-full object-cover" />
                </div>
                <div class="flex-1">
                  <p class="text-sm font-bold text-white">{{ selectedListing?.sellerName }}</p>
                  <p class="text-xs text-slate-500">卖家 · 发布于 {{ selectedListing?.listedAt }}</p>
                </div>
              </div>

              <!-- Fee Info -->
              <div class="bg-amber-500/10 border border-amber-500/20 rounded-xl p-4 mb-6">
                <div class="flex items-center justify-between mb-2">
                  <span class="text-slate-400 text-sm">商品价格</span>
                  <span class="text-white font-medium">💰 {{ selectedListing?.price.toLocaleString() }}</span>
                </div>
                <div class="flex items-center justify-between mb-2">
                  <span class="text-slate-400 text-sm">手续费 (1%)</span>
                  <span class="text-rose-400 text-medium">-💰 {{ serviceFee }}</span>
                </div>
                <div class="h-px bg-slate-700/50 my-2"></div>
                <div class="flex items-center justify-between">
                  <span class="text-slate-400 text-sm">卖家实得</span>
                  <span class="text-emerald-400 font-bold">💰 {{ sellerEarnings }}</span>
                </div>
              </div>

              <!-- Actions -->
              <div class="space-y-3">
                <button
                  @click="handleBuy"
                  :disabled="!canBuy"
                  class="w-full py-4 rounded-xl font-bold text-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                  :class="canBuy
                    ? 'bg-gradient-to-r from-emerald-500 to-teal-500 text-white shadow-lg hover:shadow-emerald-500/30'
                    : 'bg-slate-700 text-slate-400'"
                >
                  <span v-if="!canBuy">积分不足</span>
                  <span v-else>立即购买</span>
                </button>
                <button
                  @click="closeListingDetail"
                  class="w-full py-3 rounded-xl font-bold bg-slate-700 text-white hover:bg-slate-600 transition-colors"
                >
                  取消
                </button>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { marketApi, gachaApi, type MarketItem } from '~/composables/useV2Api'
import { useAuthGuard } from '~/composables/useAuthGuard'

const { requireAuth } = useAuthGuard()

interface Listing {
  id: string
  name: string
  type: string
  rarity: string
  image: string
  source: string
  price: number
  sellerId: string
  sellerName: string
  sellerAvatar: string
  listedAt: string
  status: number
}

const searchQuery = ref('')
const filterRarity = ref('')
const filterType = ref('')
const sortBy = ref('latest')
const isLoading = ref(false)
const hasMore = ref(true)
const loadingListings = ref(false)
const errorListings = ref('')

const userPoints = ref(12580)

const listings = ref<Listing[]>([])

function mapMarketItemToListing(item: MarketItem): Listing {
  return {
    id: String(item.id),
    name: item.itemName,
    type: item.itemType,
    rarity: item.itemRarity,
    image: item.itemImage,
    source: '上架交易',
    price: item.price,
    sellerId: String(item.sellerId),
    sellerName: item.sellerName || '匿名用户',
    sellerAvatar: item.sellerAvatar || 'https://picsum.photos/seed/default/100/100',
    listedAt: item.createTime ? formatTimeAgo(item.createTime) : '未知',
    status: item.status
  }
}

function formatTimeAgo(dateStr: string): string {
  const date = new Date(dateStr)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMins / 60)
  const diffDays = Math.floor(diffHours / 24)

  if (diffMins < 1) return '刚刚'
  if (diffMins < 60) return `${diffMins}分钟前`
  if (diffHours < 24) return `${diffHours}小时前`
  if (diffDays < 7) return `${diffDays}天前`
  return `${Math.floor(diffDays / 7)}周前`
}

async function fetchListings() {
  loadingListings.value = true
  errorListings.value = ''
  try {
    const params: any = {
      pageNum: 1,
      pageSize: 100
    }
    if (searchQuery.value) params.itemName = searchQuery.value
    if (filterRarity.value) params.rarity = filterRarity.value
    if (filterType.value) params.itemType = filterType.value
    if (sortBy.value === 'price_asc') params.sortBy = 'price_asc'
    else if (sortBy.value === 'price_desc') params.sortBy = 'price_desc'

    const result = await marketApi.fetchItems(params)
    listings.value = (result.records || []).map(mapMarketItemToListing)
  } catch (e: any) {
    errorListings.value = e.message || '获取商品列表失败'
    console.error('fetchListings error:', e)
  } finally {
    loadingListings.value = false
  }
}

onMounted(() => {
  fetchListings()
  fetchUserPoints()
})

// Listing Detail Modal
const showListingDetail = ref(false)
const selectedListing = ref<Listing | null>(null)

const serviceFee = computed(() => {
  if (!selectedListing.value) return 0
  return Math.max(1, Math.floor(selectedListing.value.price * 0.01))
})

const sellerEarnings = computed(() => {
  if (!selectedListing.value) return 0
  return selectedListing.value.price - serviceFee.value
})

const canBuy = computed(() => {
  if (!selectedListing.value) return false
  return userPoints.value >= selectedListing.value.price
})

// Filtered Listings
const filteredListings = computed(() => {
  let result = [...listings.value]

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(l => l.name.toLowerCase().includes(query))
  }

  if (filterRarity.value) {
    result = result.filter(l => l.rarity === filterRarity.value)
  }

  if (filterType.value) {
    result = result.filter(l => l.type === filterType.value)
  }

  if (sortBy.value === 'price_asc') {
    result.sort((a, b) => a.price - b.price)
  } else if (sortBy.value === 'price_desc') {
    result.sort((a, b) => b.price - a.price)
  }

  return result
})

function openListingDetail(listing: Listing) {
  selectedListing.value = listing
  showListingDetail.value = true
}

function closeListingDetail() {
  showListingDetail.value = false
}

function handleBuy() {
  const authorized = requireAuth()
  if (!authorized) return
  if (!canBuy.value) return
  if (!selectedListing.value) return

  if (!confirm(`确认花费 ${selectedListing.value.price.toLocaleString()} 积分购买 "${selectedListing.value.name}" 吗？`)) {
    return
  }

  marketApi.buy(Number(selectedListing.value.id))
    .then(() => {
      alert('购买成功！')
      closeListingDetail()
      fetchListings()
      fetchUserPoints()
    })
    .catch((e: any) => {
      alert(e.message || '购买失败')
      console.error('handleBuy error:', e)
    })
}

async function fetchUserPoints() {
  try {
    const points = await gachaApi.fetchUserPoints()
    userPoints.value = points
  } catch (e) {
    console.error('加载用户积分失败', e)
  }
}

function loadMore() {
  // TODO: Implement pagination
  isLoading.value = true
  setTimeout(() => {
    isLoading.value = false
    hasMore.value = false
  }, 1500)
}

// Helper Functions
function getRarityBorderClass(rarity: string): string {
  const classes: Record<string, string> = {
    'SSR': 'hover:shadow-amber-500/20',
    'SR': 'hover:shadow-purple-500/20',
    'R': 'hover:shadow-blue-500/20',
    'N': ''
  }
  return classes[rarity] || ''
}

function getRarityBadgeClass(rarity: string): string {
  const classes: Record<string, string> = {
    'SSR': 'bg-gradient-to-r from-amber-400 to-orange-500 text-slate-900',
    'SR': 'bg-gradient-to-r from-purple-500 to-pink-500 text-white',
    'R': 'bg-gradient-to-r from-blue-400 to-cyan-400 text-white',
    'N': 'bg-slate-600 text-slate-300'
  }
  return classes[rarity] || classes['N']
}
</script>