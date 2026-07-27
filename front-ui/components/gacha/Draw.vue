<template>
  <div>
    <section class="relative h-[450px] overflow-hidden">
      <div class="absolute inset-0 bg-gradient-to-b" :style="{ background: `linear-gradient(to bottom, var(--gacha-hero-from), var(--gacha-hero-via), var(--gacha-hero-to))` }"></div>
      <div class="absolute top-0 left-1/2 -translate-x-1/2 w-[800px] h-[400px] bg-blue-500/20 rounded-full blur-[120px]"></div>

      <div class="relative z-10 container mx-auto px-4 h-full flex flex-col justify-center">
        <div class="text-center mb-8">
          <span class="inline-block px-4 py-1.5 rounded-full text-xs font-bold tracking-wider gacha-badge bg-gradient-to-r from-amber-400/20 to-orange-500/20 text-amber-400 border border-amber-400/30 mb-4">
            LIMITED GACHA
          </span>
          <h1 class="text-4xl md:text-5xl font-black tracking-tight mb-4">
            <span class="gacha-hero-title" style="background: var(--gacha-title-grad, linear-gradient(to right, #A855F7, #D946EF, #F97316)); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;">
              限定奖池
            </span>
          </h1>
        </div>

        <div v-if="loadingPools" class="text-center text-slate-400">加载中...</div>
        <div v-else-if="errorPools" class="text-center text-red-400">{{ errorPools }}</div>
        <div v-else class="relative max-w-5xl mx-auto">
          <div class="flex gap-4 overflow-x-auto pb-6 snap-x snap-mandatory scrollbar-hide px-2">
            <div
              v-for="(pool, index) in gachaPools"
              :key="pool.id"
              class="flex-shrink-0 w-[280px] md:w-[320px] snap-center"
              @click="selectPool(index)"
            >
              <div
                class="relative rounded-2xl overflow-hidden cursor-pointer transition-all duration-500 hover:scale-[1.02] hover:shadow-2xl gacha-pool-card"
                :class="selectedPoolIndex === index ? 'ring-2 ring-purple-500 shadow-[0_0_15px_rgba(168,85,247,0.3)] scale-[1.02] shadow-2xl' : 'ring-1 ring-slate-700'"
              >
                <div class="relative h-[160px] md:h-[180px] overflow-hidden">
                  <img :src="pool.banner || 'https://picsum.photos/seed/default/640/320'" class="w-full h-full object-cover" />
                  <div class="absolute inset-0 gacha-banner-overlay bg-gradient-to-t from-slate-900 via-slate-900/50 to-transparent"></div>
                  <div class="absolute top-2 right-2 px-2.5 py-1 rounded-full text-xs font-bold bg-gradient-to-r from-amber-400 to-orange-500 text-slate-900 shadow-lg">
                    {{ pool.rarity }}
                  </div>
                  <div v-if="pool.endTime" class="absolute top-2 left-2 px-2.5 py-1 rounded-full text-xs font-bold bg-slate-900/80 text-slate-300 backdrop-blur-sm">
                    {{ formatEndTime(pool.endTime) }}
                  </div>
                </div>
                <div class="p-4 gacha-card-body bg-slate-900/90 backdrop-blur-md">
                  <h3 class="text-base font-bold gacha-banner-title mb-1.5" :style="{ color: index === 0 ? 'var(--banner-stellar-title)' : 'var(--banner-normal-title)' }">{{ pool.name }}</h3>
                  <div class="flex items-center justify-between text-sm">
                    <span class="gacha-banner-muted text-slate-400">剩余库存</span>
                    <span class="font-bold" :class="pool.remainingStock > 100 ? 'text-emerald-400' : pool.remainingStock > 20 ? 'text-amber-400' : 'text-rose-400'">
                      {{ pool.remainingStock.toLocaleString() }}
                    </span>
                  </div>
                  <div class="mt-2 h-1.5 gacha-progress-track bg-slate-800 rounded-full overflow-hidden">
                    <div
                      class="h-full gacha-progress-fill rounded-full transition-all duration-500"
                      :class="index === 0 ? 'gacha-progress-stellar' : 'gacha-progress-normal'"
                      :style="{ width: `${(pool.remainingStock / (pool.totalStock || 1)) * 100}%` }"
                    ></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="container mx-auto px-4 mt-4 relative z-20">
      <div class="max-w-4xl mx-auto bg-slate-800/50 backdrop-blur-xl rounded-3xl border border-slate-700/50 p-6 md:p-8">
        <div class="flex flex-col md:flex-row gap-8">
          <div class="flex-shrink-0">
            <div class="relative w-[200px] h-[200px] mx-auto">
              <div class="absolute inset-0 rounded-full bg-gradient-to-br from-amber-400/20 via-purple-500/20 to-pink-500/20 blur-xl animate-pulse"></div>
              <div class="relative w-full h-full gacha-ring rounded-full overflow-hidden border-4 shadow-2xl" style="border-color: var(--chart-ring-inner, #334155); background: linear-gradient(135deg, #1e1b4b 0%, #312e81 50%, #1e1b4b 100%);">
                <div class="absolute inset-4 gacha-ring-inner rounded-full bg-gradient-to-br from-purple-600 to-indigo-800 flex items-center justify-center">
                  <div class="text-center">
                    <span class="text-5xl">🎰</span>
                  </div>
                </div>
                <div class="absolute inset-0 bg-gradient-to-r from-transparent via-white/10 to-transparent animate-shimmer"></div>
              </div>
              <div class="absolute -top-2 -right-2 w-4 h-4">
                <div class="w-full h-full bg-amber-400 rounded-full animate-ping opacity-50"></div>
              </div>
              <div class="absolute -bottom-1 -left-3 w-3 h-3">
                <div class="w-full h-full bg-pink-400 rounded-full animate-ping opacity-50" style="animation-delay: 0.5s;"></div>
              </div>
            </div>
          </div>

          <div class="flex-1">
            <div class="mb-6">
              <h2 class="text-2xl font-bold text-white mb-2">{{ currentPool?.name || '请选择奖池' }}</h2>
              <p class="text-slate-400 text-sm">{{ currentPool?.description }}</p>
            </div>

            <div class="grid grid-cols-3 gap-4 mb-6">
              <div class="bg-gradient-to-br from-amber-500/20 to-orange-500/20 rounded-xl p-4 border border-amber-500/30">
                <p class="text-amber-400/80 text-xs mb-1">剩余积分</p>
                <p class="text-2xl font-black text-amber-400">{{ userPoints.toLocaleString() }}</p>
              </div>
              <div class="bg-slate-900/50 rounded-xl p-4">
                <p class="text-slate-500 text-xs mb-1">单抽消耗</p>
                <p class="text-xl font-bold text-amber-400">{{ currentPool?.singleCost || 0 }}</p>
              </div>
              <div class="bg-slate-900/50 rounded-xl p-4">
                <p class="text-slate-500 text-xs mb-1">十连消耗</p>
                <p class="text-xl font-bold text-amber-400">{{ currentPool?.tenCost || 0 }}</p>
              </div>
            </div>

            <div class="flex gap-4">
              <button
                @click="handleDraw(1)"
                :disabled="isDrawing || !currentPool"
                class="flex-1 py-4 rounded-xl font-bold gacha-btn-single border transition-all disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {{ isDrawing ? '抽取中...' : '单抽' }}
              </button>
              <button
                @click="handleDraw(10)"
                :disabled="isDrawing || !currentPool"
                class="flex-1 py-4 rounded-xl font-bold gacha-btn-ten transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                style="background: var(--gacha-ten-bg, linear-gradient(135deg, #8A2387 0%, #E94057 50%, #F27121 100%)); color: #fff; box-shadow: 0 4px 20px rgba(233, 64, 87, 0.35); text-shadow: 0 1px 2px rgba(0,0,0,0.25);"
              >
                {{ isDrawing ? '抽取中...' : '十连 ' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="container mx-auto px-4 mt-12">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-xl font-bold text-white">抽卡记录</h2>
        <button class="text-sm text-indigo-400 hover:text-indigo-300 transition-colors">查看全部</button>
      </div>

      <div v-if="loadingRecords" class="text-center text-slate-400 py-8">加载中...</div>
      <div v-else-if="records.length === 0" class="text-center text-slate-500 py-8">暂无抽卡记录</div>
      <div v-else class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-3">
        <div
          v-for="record in records.slice(0, 6)"
          :key="record.id"
          class="relative bg-slate-800/50 rounded-xl overflow-hidden border border-slate-700/50 group hover:scale-[1.05] transition-all duration-300"
          :class="getRarityBorderClass(record.itemRarity)"
        >
          <div class="aspect-square relative">
            <img :src="record.itemImage || 'https://picsum.photos/seed/default/200/200'" class="w-full h-full object-cover" />
            <div class="absolute inset-0 bg-gradient-to-t from-slate-900 via-transparent to-transparent"></div>
          </div>
          <div class="absolute bottom-0 left-0 right-0 p-2 bg-gradient-to-t from-slate-900 to-transparent">
            <p class="text-xs font-bold text-white truncate">{{ record.itemName }}</p>
            <p class="text-[10px]" :class="getRarityTextClass(record.itemRarity)">{{ record.itemRarity }}</p>
          </div>
        </div>
      </div>
    </section>

    <Teleport to="body">
      <transition
        enter-active-class="transition ease-out duration-300"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="transition ease-in duration-200"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <div v-if="showResult" class="fixed inset-0 z-[100] flex items-center justify-center p-4" @click="closeResult">
          <div class="absolute inset-0 bg-slate-900/90 backdrop-blur-sm"></div>

          <div class="relative z-10 w-full max-w-lg" @click.stop>
            <div class="text-center mb-6">
              <h3 class="text-2xl font-black text-white mb-2">{{ drawType === 10 ? '十连抽卡' : '单抽' }}</h3>
              <p class="text-slate-400 text-sm">恭喜获得以下奖励</p>
              <div v-if="fragmentEarned > 0" class="mt-2 inline-flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-cyan-500/20 to-blue-500/20 rounded-xl border border-cyan-500/30">
                <span class="text-lg">💎</span>
                <span class="text-cyan-400 font-bold">+{{ fragmentEarned }} 碎片</span>
              </div>
            </div>

            <div :class="drawType === 10 ? 'grid grid-cols-5 gap-2' : 'flex justify-center'">
              <div
                v-for="(card, index) in drawResult"
                :key="card.itemId || index"
                class="relative aspect-[3/4] rounded-xl overflow-hidden border-2 transition-all duration-500 hover:scale-110 hover:z-10 cursor-pointer"
                :class="[
                  getRarityBorderClass(card.itemRarity),
                  getRarityGlowClass(card.itemRarity),
                  isNewCard(index) ? 'animate-card-reveal' : ''
                ]"
                :style="{ animationDelay: `${index * 150}ms` }"
              >
                <img :src="card.itemImage || 'https://picsum.photos/seed/default/200/200'" class="w-full h-full object-cover" />
                <div class="absolute inset-0 bg-gradient-to-t from-slate-900/80 via-transparent to-transparent"></div>
                <div class="absolute bottom-0 left-0 right-0 p-2 bg-gradient-to-t from-slate-900 to-transparent">
                  <p class="text-[10px] font-bold text-white truncate">{{ card.itemName }}</p>
                  <p class="text-[8px]" :class="getRarityTextClass(card.itemRarity)">{{ card.itemRarity }}</p>
                </div>
                <div v-if="isNewCard(index)" class="absolute -top-1 -right-1 w-6 h-6 bg-rose-500 rounded-full flex items-center justify-center">
                  <span class="text-[10px] font-bold text-white">NEW</span>
                </div>
              </div>
            </div>

            <div class="flex gap-4 mt-8">
              <button
                @click="closeResult"
                class="flex-1 py-3 rounded-xl font-bold text-slate-400 bg-slate-800 hover:bg-slate-700 transition-colors"
              >
                关闭
              </button>
              <button
                @click="handleDraw(drawType)"
                class="flex-1 py-3 rounded-xl font-bold gacha-btn-continue transition-all"
                style="background: var(--gacha-ten-bg, linear-gradient(135deg, #8A2387 0%, #E94057 50%, #F27121 100%)); color: #fff; box-shadow: 0 4px 20px rgba(233, 64, 87, 0.35); text-shadow: 0 1px 2px rgba(0,0,0,0.25);"
              >
                再抽{{ drawType }}次
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
import { gachaApi, type GachaPool, type GachaRecord } from '~/composables/useV2Api'

const props = defineProps<{
  userPoints: number
  userFragment: number
}>()

const emit = defineEmits<{
  (e: 'update-points'): void
}>()

const gachaPools = ref<GachaPool[]>([])
const selectedPoolIndex = ref(0)
const currentPool = computed(() => gachaPools.value[selectedPoolIndex.value])

const loadingPools = ref(false)
const errorPools = ref('')
const loadingRecords = ref(false)
const records = ref<GachaRecord[]>([])

const isDrawing = ref(false)
const drawType = ref(1)
const showResult = ref(false)
const drawResult = ref<any[]>([])
const newCardIds = ref<number[]>([])
const fragmentEarned = ref(0)

async function fetchPools() {
  loadingPools.value = true
  errorPools.value = ''
  try {
    gachaPools.value = await gachaApi.fetchActivePools()
  } catch (e: any) {
    errorPools.value = e.message || '加载奖池失败'
  } finally {
    loadingPools.value = false
  }
}

async function fetchRecords() {
  loadingRecords.value = true
  try {
    const result = await gachaApi.fetchRecords(1, 20)
    records.value = result.records || []
  } catch (e) {
    console.error('加载抽卡记录失败', e)
  } finally {
    loadingRecords.value = false
  }
}

function formatEndTime(endTime: string): string {
  if (!endTime) return ''
  try {
    const end = new Date(endTime)
    const now = new Date()
    const diff = end.getTime() - now.getTime()
    if (diff <= 0) return '已结束'
    const days = Math.floor(diff / (1000 * 60 * 60 * 24))
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
    if (days > 0) return `还剩${days}天${hours}小时`
    if (hours > 0) return `还剩${hours}小时`
    return '即将结束'
  } catch {
    return endTime
  }
}

function selectPool(index: number) {
  selectedPoolIndex.value = index
}

function getRarityBorderClass(rarity: string): string {
  const classes: Record<string, string> = {
    'SSR': 'border-amber-400/50 hover:border-amber-400',
    'SR': 'border-purple-400/50 hover:border-purple-400',
    'R': 'border-blue-400/50 hover:border-blue-400',
    'N': 'border-slate-600/50 hover:border-slate-500'
  }
  return classes[rarity] || classes['N']
}

function getRarityTextClass(rarity: string): string {
  const classes: Record<string, string> = {
    'SSR': 'text-amber-400',
    'SR': 'text-purple-400',
    'R': 'text-blue-400',
    'N': 'text-slate-500'
  }
  return classes[rarity] || classes['N']
}

function getRarityGlowClass(rarity: string): string {
  const classes: Record<string, string> = {
    'SSR': 'shadow-amber-400/50 shadow-lg',
    'SR': 'shadow-purple-400/50 shadow-lg',
    'R': 'shadow-blue-400/50 shadow-lg',
    'N': ''
  }
  return classes[rarity] || ''
}

function isNewCard(index: number): boolean {
  return newCardIds.value.includes(drawResult.value[index]?.itemId || 0)
}

async function handleDraw(type: number) {
  if (isDrawing.value || !currentPool.value) return

  isDrawing.value = true
  drawType.value = type

  try {
    const result = await gachaApi.draw(currentPool.value.id, type)
    drawResult.value = result.records || []
    newCardIds.value = (result.records || []).map(r => r.itemId)
    fragmentEarned.value = result.fragmentCount || 0
    showResult.value = true
    emit('update-points')
  } catch (e: any) {
    alert(e.message || '抽卡失败')
    emit('update-points')
  } finally {
    isDrawing.value = false
  }
}

function closeResult() {
  showResult.value = false
  fetchRecords()
}

onMounted(() => {
  fetchPools()
  fetchRecords()
})
</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}
.animate-shimmer {
  animation: shimmer 2s infinite;
}
@keyframes card-reveal {
  0% {
    transform: scale(0.5) rotateY(180deg);
    opacity: 0;
  }
  50% {
    transform: scale(1.2) rotateY(90deg);
    opacity: 0.5;
  }
  100% {
    transform: scale(1) rotateY(0deg);
    opacity: 1;
  }
}
.animate-card-reveal {
  animation: card-reveal 0.6s ease-out forwards;
}
</style>
