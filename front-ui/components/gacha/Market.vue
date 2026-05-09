<template>
  <div class="container mx-auto px-4">
    <div class="max-w-4xl mx-auto">
      <!-- Header -->
      <div class="text-center mb-6">
        <span class="inline-flex items-center gap-2 px-4 py-1.5 rounded-full text-xs font-bold tracking-wider bg-gradient-to-r from-emerald-400/20 to-teal-500/20 text-emerald-400 border border-emerald-400/30 mb-4">
          <span class="text-lg">🏪</span>
          FLEA MARKET
        </span>
        <h1 class="text-3xl font-black text-white mb-2">跳蚤市场</h1>
        <p class="text-slate-400">买卖数字资产，官方收取 1% 交易税</p>
      </div>

      <!-- User Balance -->
      <div class="flex items-center justify-center gap-4 mb-6">
        <div class="flex items-center gap-3 px-5 py-2.5 bg-gradient-to-r from-amber-500/10 to-orange-500/10 rounded-xl border border-amber-500/20">
          <div class="w-8 h-8 rounded-full bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center">
            <span class="text-sm">💰</span>
          </div>
          <div>
            <p class="text-slate-400 text-xs">我的积分</p>
            <p class="text-lg font-bold text-amber-400">{{ userPoints.toLocaleString() }}</p>
          </div>
        </div>
      </div>

      <!-- Search & Filter Bar -->
      <div class="flex flex-col md:flex-row gap-3 mb-6">
        <div class="flex-1 relative">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索商品名称..."
            class="w-full pl-12 pr-4 py-3 rounded-xl bg-slate-800/50 border border-slate-700 text-white placeholder-slate-500 focus:border-emerald-500 focus:outline-none transition-colors"
          />
        </div>
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
      <div v-else-if="errorListings" class="text-center text-red-400 py-8">{{ errorListings }}</div>

      <!-- Empty State -->
      <div v-else-if="filteredListings.length === 0" class="text-center text-slate-500 py-8">暂无商品</div>

      <!-- Listings Grid -->
      <div v-else class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
        <div
          v-for="listing in filteredListings"
          :key="listing.id"
          class="bg-slate-800/50 backdrop-blur-sm rounded-2xl overflow-hidden border border-slate-700/50 cursor-pointer transition-all hover:scale-[1.02] hover:shadow-xl"
          :class="getRarityBorderClass(listing.rarity)"
          @click="openListingDetail(listing)"
        >
          <div class="aspect-[3/4] relative">
            <img :src="listing.image" class="w-full h-full object-cover" />
            <div class="absolute inset-0 bg-gradient-to-t from-slate-900 via-transparent to-transparent"></div>
            <div class="absolute top-2 right-2 px-2 py-0.5 rounded-full text-xs font-bold" :class="getRarityBadgeClass(listing.rarity)">
              {{ listing.rarity }}
            </div>
          </div>
          <div class="p-3">
            <h3 class="text-sm font-bold text-white truncate mb-1">{{ listing.name }}</h3>
            <p class="text-xs text-slate-400 mb-2">{{ listing.type }} · {{ listing.sellerName }}</p>
            <div class="flex items-center justify-between">
              <span class="text-lg font-bold text-amber-400">{{ listing.price.toLocaleString() }}</span>
              <span class="text-xs text-slate-500">积分</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Listing Detail Modal -->
    <Teleport to="body">
      <transition
        enter-active-class="transition ease-out duration-300"
        enter-from-class="opacity-0 scale-95"
        enter-to-class="opacity-100 scale-100"
        leave-active-class="transition ease-in duration-200"
        leave-from-class="opacity-100 scale-100"
        leave-to-class="opacity-0 scale-95"
      >
        <div v-if="showListingDetail" class="fixed inset-0 z-[100] flex items-center justify-center p-4" @click="closeListingDetail">
          <div class="absolute inset-0 bg-slate-900/90 backdrop-blur-sm"></div>

          <div class="relative z-10 w-full max-w-md bg-slate-800/90 backdrop-blur-xl rounded-3xl border border-slate-700/50 overflow-hidden" @click.stop>
            <div class="relative h-48 overflow-hidden">
              <img :src="selectedListing?.image" class="w-full h-full object-cover" />
              <div class="absolute inset-0 bg-gradient-to-t from-slate-900 via-slate-900/50 to-transparent"></div>
              <button @click="closeListingDetail" class="absolute top-4 right-4 w-8 h-8 rounded-full bg-slate-900/50 backdrop-blur-sm flex items-center justify-center text-white/70 hover:text-white transition-colors">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
              <div class="absolute bottom-4 left-4 right-4">
                <span class="px-2 py-0.5 rounded-full text-xs font-bold" :class="getRarityBadgeClass(selectedListing?.rarity || '')">
                  {{ selectedListing?.rarity }}
                </span>
              </div>
            </div>

            <div class="p-6">
              <h3 class="text-xl font-black text-white mb-1">{{ selectedListing?.name }}</h3>
              <p class="text-sm text-slate-400 mb-4">{{ selectedListing?.type }} · 卖家: {{ selectedListing?.sellerName }}</p>

              <div class="space-y-3 mb-6">
                <div class="flex items-center justify-between py-2 border-b border-slate-700/50">
                  <span class="text-slate-500 text-sm">出售价格</span>
                  <span class="text-amber-400 font-bold text-lg">{{ selectedListing?.price.toLocaleString() }} 积分</span>
                </div>
                <div class="flex items-center justify-between py-2 border-b border-slate-700/50">
                  <span class="text-slate-500 text-sm">手续费 (1%)</span>
                  <span class="text-rose-400 font-medium">{{ serviceFee.toLocaleString() }} 积分</span>
                </div>
                <div class="flex items-center justify-between py-2 border-b border-slate-700/50">
                  <span class="text-slate-500 text-sm">卖家到手</span>
                  <span class="text-emerald-400 font-bold">{{ sellerEarnings.toLocaleString() }} 积分</span>
                </div>
              </div>

              <div class="bg-amber-500/10 border border-amber-500/20 rounded-xl p-4 mb-6">
                <p class="text-amber-400 text-sm">
                  <strong>提示：</strong>购买后将消耗对应积分，物品将转入您的背包。
                </p>
              </div>

              <button
                @click="handleBuy"
                :disabled="!canBuy"
                class="w-full py-3 rounded-xl font-bold bg-gradient-to-r from-emerald-500 to-teal-500 text-white shadow-lg hover:shadow-emerald-500/30 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {{ canBuy ? '立即购买' : '积分不足' }}
              </button>
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

const props = defineProps<{
  userPoints: number
}>()

const emit = defineEmits<{
  (e: 'update-points'): void
}>()

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

const listings = ref<Listing[]>([])
const loadingListings = ref(false)
const errorListings = ref('')

const searchQuery = ref('')
const filterRarity = ref('')
const filterType = ref('')
const sortBy = ref('latest')

function mapMarketItemToListing(item: MarketItem): Listing {
  return {
    id: String(item.id),
    name: item.itemName,
    type: item.itemType,
    rarity: item.itemRarity,
    image: item.itemImage,
    source: '',
    price: item.price,
    sellerId: String(item.sellerId),
    sellerName: `用户${item.sellerId}`,
    sellerAvatar: '',
    listedAt: item.createTime ? new Date(item.createTime).toLocaleDateString('zh-CN') : '',
    status: item.status
  }
}

async function fetchListings() {
  loadingListings.value = true
  errorListings.value = ''
  try {
    const result = await marketApi.fetchItems({ pageNum: 1, pageSize: 100 })
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
})

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
  return props.userPoints >= selectedListing.value.price
})

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
  if (!selectedListing.value) return

  if (!confirm(`确认花费 ${selectedListing.value.price.toLocaleString()} 积分购买 "${selectedListing.value.name}" 吗？`)) {
    return
  }

  marketApi.buy(Number(selectedListing.value.id))
    .then(() => {
      alert('购买成功！')
      closeListingDetail()
      fetchListings()
      emit('update-points')
    })
    .catch((e: any) => {
      alert(e.message || '购买失败')
      console.error('handleBuy error:', e)
      closeListingDetail()
      fetchListings()
    })
}

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
