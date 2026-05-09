<template>
  <div class="container mx-auto px-4">
    <div class="max-w-4xl mx-auto">
      <!-- Stats Cards -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        <div class="bg-gradient-to-br from-amber-500/10 to-orange-500/10 backdrop-blur-sm rounded-xl p-3 border border-amber-500/20">
          <div class="flex items-center gap-2">
            <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center">
              <span class="text-sm">💰</span>
            </div>
            <div>
              <p class="text-amber-400/80 text-xs">剩余积分</p>
              <p class="text-lg font-bold text-amber-400">{{ userPoints.toLocaleString() }}</p>
            </div>
          </div>
        </div>
        <div class="bg-gradient-to-br from-cyan-500/10 to-blue-500/10 backdrop-blur-sm rounded-xl p-3 border border-cyan-500/20">
          <div class="flex items-center gap-2">
            <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-cyan-400 to-blue-500 flex items-center justify-center">
              <span class="text-sm">💎</span>
            </div>
            <div>
              <p class="text-cyan-400/80 text-xs">碎片</p>
              <p class="text-lg font-bold text-cyan-400">{{ userFragment }}</p>
            </div>
          </div>
        </div>
        <div class="bg-slate-800/50 backdrop-blur-sm rounded-xl p-3 border border-slate-700/50">
          <div class="flex items-center gap-2">
            <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-400 to-cyan-500 flex items-center justify-center">
              <span class="text-sm">📦</span>
            </div>
            <div>
              <p class="text-slate-500 text-xs">总资产</p>
              <p class="text-lg font-bold text-white">{{ totalAssets }}</p>
            </div>
          </div>
        </div>
        <div class="bg-slate-800/50 backdrop-blur-sm rounded-xl p-3 border border-slate-700/50">
          <div class="flex items-center gap-2">
            <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-purple-400 to-pink-500 flex items-center justify-center">
              <span class="text-sm">✨</span>
            </div>
            <div>
              <p class="text-slate-500 text-xs">SSR实物</p>
              <p class="text-lg font-bold text-white">{{ ssrPhysicalCount }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Fragment Exchange -->
      <div class="bg-gradient-to-r from-cyan-500/10 to-blue-500/10 rounded-2xl border border-cyan-500/20 p-4 mb-6">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-cyan-400 to-blue-500 flex items-center justify-center">
              <span class="text-2xl">💎</span>
            </div>
            <div>
              <p class="text-white font-bold">碎片兑换积分</p>
              <p class="text-cyan-400/80 text-sm">100 碎片 = 10 积分</p>
            </div>
          </div>
          <button
            @click="exchangeFragment"
            :disabled="userFragment < 100"
            class="px-6 py-2.5 rounded-xl font-bold bg-gradient-to-r from-cyan-500 to-blue-500 text-white shadow-lg hover:shadow-cyan-500/30 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
          >
            兑换
          </button>
        </div>
      </div>

      <!-- Synthesize Section -->
      <div class="bg-slate-800/50 rounded-2xl border border-slate-700/50 p-4 mb-6">
        <h3 class="text-lg font-bold text-white mb-4 flex items-center gap-2">
          <span>️</span> 合成工坊
        </h3>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div class="bg-slate-900/50 rounded-xl p-4 border border-slate-700/50">
            <div class="flex items-center gap-3 mb-3">
              <div class="w-10 h-10 rounded-lg bg-blue-500/20 flex items-center justify-center">
                <span class="text-lg"></span>
              </div>
              <div>
                <p class="text-white font-bold">R → SR</p>
                <p class="text-slate-400 text-xs">10个蓝色合成1个紫色</p>
              </div>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-slate-400 text-sm">拥有: {{ rCount }} 个</span>
              <button
                @click="openSynthesizeModal('R')"
                :disabled="rCount < 10"
                class="px-4 py-1.5 rounded-lg text-sm font-bold bg-blue-500/20 text-blue-400 hover:bg-blue-500/30 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
              >
                合成
              </button>
            </div>
          </div>
          <div class="bg-slate-900/50 rounded-xl p-4 border border-slate-700/50">
            <div class="flex items-center gap-3 mb-3">
              <div class="w-10 h-10 rounded-lg bg-purple-500/20 flex items-center justify-center">
                <span class="text-lg">🟣</span>
              </div>
              <div>
                <p class="text-white font-bold">SR → SSR</p>
                <p class="text-slate-400 text-xs">10个紫色合成1个金色</p>
              </div>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-slate-400 text-sm">拥有: {{ srCount }} 个</span>
              <button
                @click="openSynthesizeModal('SR')"
                :disabled="srCount < 10"
                class="px-4 py-1.5 rounded-lg text-sm font-bold bg-purple-500/20 text-purple-400 hover:bg-purple-500/30 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
              >
                合成
              </button>
            </div>
          </div>
          <div class="bg-slate-900/50 rounded-xl p-4 border border-slate-700/50">
            <div class="flex items-center gap-3 mb-3">
              <div class="w-10 h-10 rounded-lg bg-amber-500/20 flex items-center justify-center">
                <span class="text-lg">🟡</span>
              </div>
              <div>
                <p class="text-white font-bold">SSR → UR实物</p>
                <p class="text-slate-400 text-xs">10个金色合成1个红色实物</p>
              </div>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-slate-400 text-sm">拥有: {{ ssrCount }} 个</span>
              <button
                @click="openSynthesizeModal('SSR')"
                :disabled="ssrCount < 10"
                class="px-4 py-1.5 rounded-lg text-sm font-bold bg-amber-500/20 text-amber-400 hover:bg-amber-500/30 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
              >
                合成
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Filter Tabs -->
      <div class="flex gap-2 overflow-x-auto pb-2 mb-6">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          @click="activeTab = tab.id"
          class="px-4 py-2 rounded-xl text-sm font-medium whitespace-nowrap transition-all"
          :class="activeTab === tab.id
            ? 'bg-gradient-to-r from-indigo-500 to-purple-500 text-white shadow-lg'
            : 'bg-slate-800/50 text-slate-400 hover:bg-slate-700'"
        >
          {{ tab.name }}
        </button>
      </div>

      <!-- Assets Grid -->
      <div v-if="loadingAssets" class="text-center text-slate-400 py-8">加载中...</div>
      <div v-else-if="errorAssets" class="text-center text-red-400 py-8">{{ errorAssets }}</div>
      <div v-else-if="filteredAssets.length === 0" class="text-center text-slate-500 py-8">暂无资产</div>
      <div v-else class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-3">
        <div
          v-for="asset in filteredAssets"
          :key="asset.id"
          class="relative bg-slate-800/50 rounded-xl overflow-hidden group hover:scale-[1.05] transition-all duration-300 cursor-pointer"
          :class="getRarityBorderClass(asset.rarity)"
          @click="openAssetDetail(asset)"
        >
          <div class="aspect-square relative">
            <img :src="asset.image" class="w-full h-full object-cover" />
            <div class="absolute inset-0 bg-gradient-to-t from-slate-900 via-transparent to-transparent"></div>
          </div>
          <div class="absolute top-2 right-2 px-2 py-0.5 rounded-full text-xs font-bold" :class="getRarityBadgeClass(asset.rarity)">
            {{ asset.rarity }}
          </div>
          <div v-if="asset.isPhysical" class="absolute top-2 left-2 px-2 py-0.5 rounded-full text-xs font-bold bg-emerald-500/80 text-white">
            实物
          </div>
          <div v-if="asset.quantity > 1" class="absolute bottom-8 right-2 px-1.5 py-0.5 rounded-md text-xs font-bold bg-slate-900/80 text-white backdrop-blur-sm">
            ×{{ asset.quantity }}
          </div>
          <div class="absolute bottom-0 left-0 right-0 p-2 bg-gradient-to-t from-slate-900 to-transparent">
            <p class="text-xs font-bold text-white truncate">{{ asset.name }}</p>
            <p class="text-[10px] text-slate-400">{{ asset.type }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Asset Detail Modal -->
    <Teleport to="body">
      <div v-if="showAssetDetail" class="fixed inset-0 z-[100] flex items-center justify-center p-4" @click="closeAssetDetail">
        <div class="absolute inset-0 bg-slate-900/90 backdrop-blur-sm"></div>
        <div class="relative z-10 w-full max-w-md bg-slate-800/90 backdrop-blur-xl rounded-3xl border border-slate-700/50 overflow-hidden" @click.stop>
          <div class="relative h-48 overflow-hidden">
            <img :src="selectedAsset?.image" class="w-full h-full object-cover" />
            <div class="absolute inset-0 bg-gradient-to-t from-slate-900 via-slate-900/50 to-transparent"></div>
            <button @click="closeAssetDetail" class="absolute top-4 right-4 w-8 h-8 rounded-full bg-slate-900/50 backdrop-blur-sm flex items-center justify-center text-white/70 hover:text-white transition-colors">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
            <div class="absolute bottom-4 left-4 right-4">
              <span class="px-2 py-0.5 rounded-full text-xs font-bold" :class="getRarityBadgeClass(selectedAsset?.rarity || '')">
                {{ selectedAsset?.rarity }}
              </span>
              <span v-if="selectedAsset?.isPhysical" class="ml-2 px-2 py-0.5 rounded-full text-xs font-bold bg-emerald-500/80 text-white">
                实物
              </span>
            </div>
          </div>
          <div class="p-6">
            <h3 class="text-xl font-black text-white mb-1">{{ selectedAsset?.name }}</h3>
            <p class="text-sm text-slate-400 mb-4">{{ selectedAsset?.type }}</p>

            <div class="space-y-3 mb-6">
              <div class="flex items-center justify-between py-2 border-b border-slate-700/50">
                <span class="text-slate-500 text-sm">稀有度</span>
                <span class="font-bold" :class="getRarityTextClass(selectedAsset?.rarity || '')">{{ selectedAsset?.rarity }}</span>
              </div>
              <div class="flex items-center justify-between py-2 border-b border-slate-700/50">
                <span class="text-slate-500 text-sm">获取时间</span>
                <span class="text-white text-sm">{{ selectedAsset?.acquiredAt }}</span>
              </div>
            </div>

            <div class="space-y-3">
              <button
                v-if="selectedAsset?.isPhysical"
                @click="goToRedeem(selectedAsset)"
                class="w-full py-3 rounded-xl font-bold bg-gradient-to-r from-emerald-500 to-teal-500 text-white shadow-lg hover:shadow-emerald-500/30 transition-all"
              >
                兑换实物
              </button>
              <p v-else class="text-center text-slate-500 text-sm">此物品可用于合成</p>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Synthesize Modal -->
    <Teleport to="body">
      <div v-if="showSynthesize" class="fixed inset-0 z-[100] flex items-center justify-center p-4" @click="closeSynthesizeModal">
        <div class="absolute inset-0 bg-slate-900/90 backdrop-blur-sm"></div>
        <div class="relative z-10 w-full max-w-2xl bg-slate-800/90 backdrop-blur-xl rounded-3xl border border-slate-700/50 overflow-hidden max-h-[90vh] flex flex-col" @click.stop>
          <div class="p-6 border-b border-slate-700/50">
            <div class="text-center">
              <span class="text-4xl mb-2 block">️</span>
              <h3 class="text-xl font-black text-white mb-1">选择合成材料</h3>
              <p class="text-slate-400 text-sm">
                {{ synthesizeSource === 'R' ? '选择10个R品质物品 → 合成1个SR碎片' : synthesizeSource === 'SR' ? '选择10个SR品质物品 → 合成1个SSR碎片' : '选择10个SSR品质物品 → 合成1个UR碎片' }}
              </p>
            </div>
          </div>

          <div class="flex-1 overflow-y-auto p-6">
            <div class="flex items-center justify-between mb-4">
              <span class="text-slate-400 text-sm">已选择: <span class="text-white font-bold">{{ selectedTotalCount }}</span> (可合成 <span class="text-emerald-400 font-bold">{{ maxSynthesizeTimes }}</span> 次)</span>
              <button @click="selectedAssets = {}" class="text-xs text-slate-500 hover:text-white transition-colors">清空选择</button>
            </div>

            <div v-if="availableForSynthesis.length === 0" class="text-center py-8 text-slate-500">
              没有可用于合成的物品
            </div>
            <div v-else class="grid grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-3">
              <div
                v-for="asset in availableForSynthesis"
                :key="asset.id"
                class="relative rounded-xl overflow-hidden cursor-pointer transition-all"
                :class="[
                  getRarityBorderClass(asset.rarity),
                  (selectedAssets[asset.id] || 0) > 0 ? 'ring-2 ring-emerald-500 scale-[1.02]' : 'opacity-70 hover:opacity-100'
                ]"
                @click="openQuantitySelector(asset)"
              >
                <div class="aspect-square relative">
                  <img :src="asset.image" class="w-full h-full object-cover" />
                  <div class="absolute inset-0 bg-gradient-to-t from-slate-900 via-transparent to-transparent"></div>
                  <div v-if="(selectedAssets[asset.id] || 0) > 0" class="absolute inset-0 bg-emerald-500/20 flex items-center justify-center">
                    <span class="text-2xl">✓</span>
                  </div>
                </div>
                <div class="absolute top-1 right-1 px-1.5 py-0.5 rounded text-[10px] font-bold" :class="getRarityBadgeClass(asset.rarity)">
                  {{ asset.rarity }}
                </div>
                <div v-if="asset.quantity > 1" class="absolute bottom-8 right-2 px-1.5 py-0.5 rounded-md text-xs font-bold bg-slate-900/80 text-white backdrop-blur-sm">
                  ×{{ asset.quantity }}
                </div>
                <div v-if="(selectedAssets[asset.id] || 0) > 0" class="absolute bottom-8 left-2 px-1.5 py-0.5 rounded-md text-xs font-bold bg-emerald-500/80 text-white backdrop-blur-sm">
                  已选{{ selectedAssets[asset.id] }}
                </div>
                <div class="p-1.5">
                  <p class="text-[10px] font-bold text-white truncate">{{ asset.name }}</p>
                </div>
              </div>
            </div>
          </div>

          <div class="p-6 border-t border-slate-700/50">
            <div class="flex items-center justify-between mb-4">
              <span class="text-slate-400 text-sm">合成产物</span>
              <span class="text-white font-bold">
                {{ maxSynthesizeTimes }}个 {{ synthesizeSource === 'R' ? 'SR碎片' : synthesizeSource === 'SR' ? 'SSR碎片' : 'UR碎片' }}
              </span>
            </div>
            <div class="flex gap-3">
              <button @click="closeSynthesizeModal" class="flex-1 py-3 rounded-xl font-bold bg-slate-700 text-white hover:bg-slate-600 transition-colors">
                取消
              </button>
              <button
                @click="handleSynthesize"
                :disabled="maxSynthesizeTimes < 1 || synthesizing"
                class="flex-1 py-3 rounded-xl font-bold bg-gradient-to-r from-purple-500 to-pink-500 text-white shadow-lg hover:shadow-purple-500/30 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
              >
                {{ synthesizing ? '合成中...' : `确认合成 (${maxSynthesizeTimes}次)` }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Quantity Selector Modal -->
    <Teleport to="body">
      <div v-if="showQuantitySelector" class="fixed inset-0 z-[110] flex items-center justify-center p-4" @click="closeQuantitySelector">
        <div class="absolute inset-0 bg-slate-900/90 backdrop-blur-sm"></div>
        <div class="relative z-10 w-full max-w-sm bg-slate-800/90 backdrop-blur-xl rounded-3xl border border-slate-700/50 overflow-hidden" @click.stop>
          <div class="p-6">
            <div class="flex items-center gap-3 mb-4">
              <img :src="selectedAssetForQuantity?.image" class="w-16 h-16 rounded-xl object-cover" />
              <div>
                <p class="text-white font-bold">{{ selectedAssetForQuantity?.name }}</p>
                <p class="text-slate-400 text-sm">拥有: {{ selectedAssetForQuantity?.quantity }} 个</p>
              </div>
            </div>
            <div class="mb-4">
              <label class="text-slate-400 text-sm mb-2 block">选择使用数量</label>
              <div class="flex items-center gap-3">
                <button 
                  @click="quantitySelectorValue = Math.max(0, quantitySelectorValue - 1)"
                  class="w-10 h-10 rounded-lg bg-slate-700 text-white font-bold hover:bg-slate-600 transition-colors"
                >
                  -
                </button>
                <input 
                  v-model.number="quantitySelectorValue" 
                  type="number" 
                  min="0" 
                  :max="selectedAssetForQuantity?.quantity || 0"
                  class="flex-1 h-10 rounded-lg bg-slate-900 border border-slate-700 text-white text-center font-bold"
                />
                <button 
                  @click="quantitySelectorValue = Math.min(selectedAssetForQuantity?.quantity || 0, quantitySelectorValue + 1)"
                  class="w-10 h-10 rounded-lg bg-slate-700 text-white font-bold hover:bg-slate-600 transition-colors"
                >
                  +
                </button>
              </div>
              <div class="flex gap-2 mt-3">
                <button 
                  v-for="n in [5, 10, 20, 50]" 
                  :key="n"
                  @click="quantitySelectorValue = Math.min(selectedAssetForQuantity?.quantity || 0, quantitySelectorValue + n)"
                  class="flex-1 py-1.5 rounded-lg text-xs font-bold bg-slate-700 text-slate-300 hover:bg-slate-600 transition-colors"
                >
                  +{{ n }}
                </button>
              </div>
            </div>
            <div class="flex gap-3">
              <button @click="closeQuantitySelector" class="flex-1 py-3 rounded-xl font-bold bg-slate-700 text-white hover:bg-slate-600 transition-colors">
                取消
              </button>
              <button @click="confirmQuantitySelection" class="flex-1 py-3 rounded-xl font-bold bg-gradient-to-r from-emerald-500 to-teal-500 text-white shadow-lg hover:shadow-emerald-500/30 transition-all">
                确定
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Synthesis Success Modal -->
    <Teleport to="body">
      <div v-if="showSynthesisResult" class="fixed inset-0 z-[110] flex items-center justify-center p-4" @click="closeSynthesisResult">
        <div class="absolute inset-0 bg-slate-900/90 backdrop-blur-sm"></div>
        <div class="relative z-10 w-full max-w-sm bg-slate-800/90 backdrop-blur-xl rounded-3xl border border-slate-700/50 overflow-hidden" @click.stop>
          <div class="p-8 text-center">
            <div class="w-20 h-20 mx-auto mb-4 rounded-2xl flex items-center justify-center" :class="getResultGradientClass()">
              <span class="text-4xl">✨</span>
            </div>
            <h3 class="text-xl font-black text-white mb-2">合成成功！</h3>
            <p class="text-slate-400 text-sm mb-6">
              消耗{{ lastSynthesizeTimes * 10 }}个{{ synthesizeSource }}品质物品<br/>
              获得 <span class="font-bold text-white">{{ lastSynthesizeTimes }}个 {{ getResultName() }}</span>
            </p>
            <button @click="closeSynthesisResult" class="w-full py-3 rounded-xl font-bold bg-gradient-to-r from-purple-500 to-pink-500 text-white shadow-lg hover:shadow-purple-500/30 transition-all">
              确定
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useUserStore } from '~/stores/user'

const props = defineProps<{
  userPoints: number
  userFragment: number
}>()

const emit = defineEmits(['update-points'])

const userStore = useUserStore()

function getAuthHeaders(): Record<string, string> {
  return userStore.token ? { 'Authorization': `Bearer ${userStore.token}` } : {}
}

interface Asset {
  id: string
  name: string
  type: string
  rarity: string
  image: string
  status: string
  isPhysical: boolean
  quantity: number
  source: string
  acquiredAt: string
}

const tabs = [
  { id: 'all', name: '全部' },
  { id: 'UR', name: 'UR' },
  { id: 'SSR', name: 'SSR' },
  { id: 'SR', name: 'SR' },
  { id: 'R', name: 'R' },
  { id: 'N', name: 'N' }
]

const activeTab = ref('all')
const assets = ref<Asset[]>([])
const loadingAssets = ref(false)
const errorAssets = ref('')

const showAssetDetail = ref(false)
const selectedAsset = ref<Asset | null>(null)

const showSynthesize = ref(false)
const synthesizeSource = ref('R')
const synthesizing = ref(false)

const showSynthesisResult = ref(false)
const lastSynthesizeTimes = ref(0)

const selectedAssets = ref<Record<string, number>>({})

const showQuantitySelector = ref(false)
const selectedAssetForQuantity = ref<Asset | null>(null)
const quantitySelectorValue = ref(0)

const selectedTotalCount = computed(() => {
  return Object.values(selectedAssets.value).reduce((sum, count) => sum + count, 0)
})

const availableForSynthesis = computed(() => {
  return assets.value.filter(a => 
    a.rarity === synthesizeSource.value && 
    a.status === 'normal'
  )
})

const rCount = computed(() => assets.value.filter(a => a.rarity === 'R' && a.status === 'normal').reduce((sum, a) => sum + (a.quantity || 1), 0))
const srCount = computed(() => assets.value.filter(a => a.rarity === 'SR' && a.status === 'normal').reduce((sum, a) => sum + (a.quantity || 1), 0))
const ssrCount = computed(() => assets.value.filter(a => a.rarity === 'SSR' && a.status === 'normal').reduce((sum, a) => sum + (a.quantity || 1), 0))
const ssrPhysicalCount = computed(() => assets.value.filter(a => a.rarity === 'SSR' && a.isPhysical && a.status === 'normal').reduce((sum, a) => sum + (a.quantity || 1), 0))
const totalAssets = computed(() => assets.value.filter(a => a.status === 'normal').reduce((sum, a) => sum + (a.quantity || 1), 0))
const maxSynthesizeTimes = computed(() => {
  return Math.floor(selectedTotalCount.value / 10)
})

const filteredAssets = computed(() => {
  if (activeTab.value === 'all') return assets.value.filter(a => a.status === 'normal')
  return assets.value.filter(a => a.rarity === activeTab.value && a.status === 'normal')
})

async function fetchAssets() {
  loadingAssets.value = true
  errorAssets.value = ''
  try {
    const response = await $fetch<{ code: number; msg: string; data: { records: any[] } }>('/api-proxy/asset/page?pageNum=1&pageSize=100', {
      headers: getAuthHeaders()
    })
    if (response.code === 200) {
      assets.value = (response.data.records || []).map(mapApiToAsset)
    }
  } catch (e: any) {
    errorAssets.value = e.message || '获取资产失败'
  } finally {
    loadingAssets.value = false
  }
}

function mapApiToAsset(item: any): Asset {
  return {
    id: String(item.id),
    name: item.itemName || '未知物品',
    type: item.itemType || '角色',
    rarity: item.itemRarity || 'N',
    image: item.itemImage || 'https://picsum.photos/seed/default/200/200',
    status: item.status === 1 ? 'normal' : item.status === 2 ? 'locked' : 'used',
    isPhysical: item.isPhysical === 1,
    quantity: item.quantity || 1,
    source: item.acquireType || 'gacha',
    acquiredAt: item.createTime ? new Date(item.createTime).toLocaleDateString('zh-CN') : ''
  }
}

function openAssetDetail(asset: Asset) {
  selectedAsset.value = asset
  showAssetDetail.value = true
}

function closeAssetDetail() {
  showAssetDetail.value = false
  selectedAsset.value = null
}

function openSynthesizeModal(source: string) {
  synthesizeSource.value = source
  selectedAssets.value = {}
  showSynthesize.value = true
}

function closeSynthesizeModal() {
  showSynthesize.value = false
  selectedAssets.value = {}
}

function openQuantitySelector(asset: Asset) {
  selectedAssetForQuantity.value = asset
  quantitySelectorValue.value = selectedAssets.value[asset.id] || 0
  showQuantitySelector.value = true
}

function closeQuantitySelector() {
  showQuantitySelector.value = false
  selectedAssetForQuantity.value = null
  quantitySelectorValue.value = 0
}

function confirmQuantitySelection() {
  if (!selectedAssetForQuantity.value) return
  const assetId = selectedAssetForQuantity.value.id
  if (quantitySelectorValue.value <= 0) {
    delete selectedAssets.value[assetId]
  } else {
    selectedAssets.value[assetId] = quantitySelectorValue.value
  }
  closeQuantitySelector()
}

function closeSynthesisResult() {
  showSynthesisResult.value = false
  fetchAssets()
  emit('update-points')
}

function getResultName(): string {
  switch (synthesizeSource.value) {
    case 'R': return 'SR碎片'
    case 'SR': return 'SSR碎片'
    case 'SSR': return 'UR碎片'
    default: return '未知碎片'
  }
}

function getResultGradientClass(): string {
  switch (synthesizeSource.value) {
    case 'R': return 'bg-gradient-to-br from-purple-500 to-pink-500'
    case 'SR': return 'bg-gradient-to-br from-amber-500 to-orange-500'
    case 'SSR': return 'bg-gradient-to-br from-red-500 to-rose-500'
    default: return 'bg-slate-700'
  }
}

async function handleSynthesize() {
  if (synthesizing.value || maxSynthesizeTimes.value < 1) return
  synthesizing.value = true
  try {
    const times = maxSynthesizeTimes.value
    lastSynthesizeTimes.value = times
    const payload = {
      sourceRarity: synthesizeSource.value,
      selectedItems: Object.entries(selectedAssets.value).map(([id, count]) => ({
        assetId: Number(id),
        count
      })),
      times
    }
    const response = await $fetch<{ code: number; msg: string }>('/api-proxy/synthesize/execute', {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(payload)
    })
    if (response.code === 200) {
      closeSynthesizeModal()
      showSynthesisResult.value = true
      await fetchAssets()
      emit('update-points')
    } else {
      alert(response.msg || '合成失败')
    }
  } catch (e: any) {
    alert(e.data?.msg || e.message || '合成失败')
  } finally {
    synthesizing.value = false
  }
}

async function exchangeFragment() {
  if (props.userFragment < 100) {
    alert('碎片不足100')
    return
  }
  try {
    await $fetch('/api-proxy/fragment/exchange', {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ fragmentCount: 100 })
    })
    alert('兑换成功！获得10积分')
    emit('update-points')
  } catch (e: any) {
    alert(e.message || '兑换失败')
  }
}

function goToRedeem(asset: Asset) {
  closeAssetDetail()
  window.location.href = '/gacha?tab=redeem&assetId=' + asset.id
}

function getRarityBorderClass(rarity: string): string {
  const classes: Record<string, string> = {
    'UR': 'border-2 border-red-500 hover:shadow-red-500/30',
    'SSR': 'border-2 border-amber-500 hover:shadow-amber-500/30',
    'SR': 'border-2 border-purple-500 hover:shadow-purple-500/30',
    'R': 'border-2 border-blue-500 hover:shadow-blue-500/30',
    'N': 'border border-slate-700/50'
  }
  return classes[rarity] || 'border border-slate-700/50'
}

function getRarityBadgeClass(rarity: string): string {
  const classes: Record<string, string> = {
    'UR': 'bg-gradient-to-r from-red-500 to-rose-500 text-white',
    'SSR': 'bg-gradient-to-r from-amber-400 to-orange-500 text-slate-900',
    'SR': 'bg-gradient-to-r from-purple-400 to-pink-500 text-white',
    'R': 'bg-gradient-to-r from-blue-400 to-cyan-500 text-white',
    'N': 'bg-slate-600 text-slate-200'
  }
  return classes[rarity] || 'bg-slate-600 text-slate-200'
}

function getRarityTextClass(rarity: string): string {
  const classes: Record<string, string> = {
    'UR': 'text-red-400',
    'SSR': 'text-amber-400',
    'SR': 'text-purple-400',
    'R': 'text-blue-400',
    'N': 'text-slate-400'
  }
  return classes[rarity] || 'text-slate-400'
}

onMounted(() => {
  fetchAssets()
})

watch(() => props.userPoints, (newVal, oldVal) => {
  if (newVal !== oldVal && oldVal !== undefined) {
    fetchAssets()
  }
})
</script>
