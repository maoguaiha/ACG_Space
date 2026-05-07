<template>
  <div class="min-h-screen pb-20">
    <!-- Header Section -->
    <section class="relative py-12 overflow-hidden">
      <!-- Background Glow -->
      <div class="absolute inset-0 bg-gradient-to-b from-purple-900/20 via-slate-900 to-slate-900"></div>
      <div class="absolute top-0 left-1/2 -translate-x-1/2 w-[600px] h-[500px] bg-purple-600/10 rounded-full blur-[150px]"></div>

      <div class="container mx-auto px-4 relative z-10">
        <div class="max-w-4xl mx-auto">
          <div class="text-center mb-8">
            <span class="inline-flex items-center gap-2 px-4 py-1.5 rounded-full text-xs font-bold tracking-wider bg-gradient-to-r from-purple-400/20 to-pink-500/20 text-purple-400 border border-purple-400/30 mb-4">
              <span class="text-lg">⚗️</span>
              MEMORY WORKSHOP
            </span>
            <h1 class="text-4xl font-black text-white mb-2">记忆工坊</h1>
            <p class="text-slate-400">收集碎片，合成完整角色，解锁专属立绘</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Recipes Section -->
    <section class="container mx-auto px-4">
      <div class="max-w-4xl mx-auto">
        <!-- Progress Overview -->
        <div class="bg-slate-800/50 backdrop-blur-sm rounded-3xl border border-slate-700/50 p-6 mb-8">
          <h3 class="text-lg font-bold text-white mb-4">图鉴进度</h3>
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div
              v-for="category in categories"
              :key="category.id"
              class="text-center p-4 bg-slate-900/50 rounded-2xl border border-slate-700/50"
            >
              <p class="text-2xl font-black text-white mb-1">{{ category.completed }}/{{ category.total }}</p>
              <p class="text-xs text-slate-400">{{ category.name }}</p>
              <div class="h-1.5 bg-slate-700 rounded-full mt-2 overflow-hidden">
                <div
                  class="h-full bg-gradient-to-r from-purple-500 to-pink-500 rounded-full transition-all duration-500"
                  :style="{ width: `${(category.completed / category.total) * 100}%` }"
                ></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Recipe Cards -->
        <div class="space-y-6">
          <div
            v-for="recipe in recipes"
            :key="recipe.id"
            class="bg-slate-800/50 backdrop-blur-sm rounded-3xl border border-slate-700/50 overflow-hidden"
            :class="{ 'border-emerald-500/50 shadow-lg shadow-emerald-500/10': recipe.canSynthesize }"
          >
            <!-- Recipe Header -->
            <div class="p-6 border-b border-slate-700/50">
              <div class="flex items-start gap-6">
                <!-- Result Preview -->
                <div class="relative flex-shrink-0">
                  <div class="w-28 h-28 rounded-2xl overflow-hidden border-2 transition-all"
                    :class="recipe.canSynthesize ? 'border-emerald-400 shadow-lg shadow-emerald-400/30' : 'border-slate-700'"
                  >
                    <img :src="recipe.resultImage" class="w-full h-full object-cover" />
                  </div>
                  <div v-if="recipe.canSynthesize" class="absolute -top-2 -right-2 w-8 h-8 rounded-full bg-emerald-500 flex items-center justify-center shadow-lg">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg>
                  </div>
                </div>

                <!-- Recipe Info -->
                <div class="flex-1">
                  <div class="flex items-center gap-2 mb-2">
                    <h3 class="text-xl font-black text-white">{{ recipe.resultName }}</h3>
                    <span class="px-2 py-0.5 rounded-full text-xs font-bold" :class="getRarityBadgeClass(recipe.rarity)">
                      {{ recipe.rarity }}
                    </span>
                  </div>
                  <p class="text-sm text-slate-400 mb-4">{{ recipe.description }}</p>

                  <!-- Progress -->
                  <div class="flex items-center gap-4">
                    <div class="flex-1">
                      <div class="flex items-center justify-between text-xs mb-1">
                        <span class="text-slate-500">收集进度</span>
                        <span class="text-emerald-400 font-bold">{{ recipe.collected }}/{{ recipe.totalFragments }}</span>
                      </div>
                      <div class="h-2 bg-slate-700 rounded-full overflow-hidden">
                        <div
                          class="h-full bg-gradient-to-r from-purple-500 to-pink-500 rounded-full transition-all duration-500"
                          :style="{ width: `${(recipe.collected / recipe.totalFragments) * 100}%` }"
                        ></div>
                      </div>
                    </div>
                    <button
                      v-if="recipe.canSynthesize"
                      @click="openSynthesizeModal(recipe)"
                      class="px-6 py-2.5 rounded-xl font-bold bg-gradient-to-r from-emerald-500 to-teal-500 text-white shadow-lg hover:shadow-emerald-500/30 transition-all"
                    >
                      立即合成
                    </button>
                    <NuxtLink
                      v-else
                      to="/market"
                      class="px-6 py-2.5 rounded-xl font-bold bg-slate-700 text-white hover:bg-slate-600 transition-colors"
                    >
                      去获取
                    </NuxtLink>
                  </div>
                </div>
              </div>
            </div>

            <!-- Fragments Grid -->
            <div class="p-6 bg-slate-900/30">
              <p class="text-xs text-slate-500 mb-3">所需碎片</p>
              <div class="grid grid-cols-4 md:grid-cols-6 gap-3">
                <div
                  v-for="(fragment, index) in recipe.fragments"
                  :key="fragment.id"
                  class="relative aspect-square rounded-xl overflow-hidden border-2 transition-all"
                  :class="fragment.owned ? 'border-emerald-400' : 'border-slate-700 opacity-50'"
                >
                  <img :src="fragment.image" class="w-full h-full object-cover" />
                  <div class="absolute inset-0 bg-gradient-to-t from-slate-900/80 via-transparent to-transparent"></div>
                  <div v-if="fragment.owned" class="absolute inset-0 bg-emerald-400/20 flex items-center justify-center">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#34d399" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg>
                  </div>
                  <div v-else class="absolute inset-0 bg-slate-900/50 flex items-center justify-center">
                    <span class="text-xs text-slate-500">缺少</span>
                  </div>
                  <div class="absolute bottom-1 left-1 right-1">
                    <p class="text-[10px] font-medium text-white truncate text-center">{{ fragment.name }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div v-if="!loading && recipes.length === 0" class="text-center py-20">
          <div class="w-24 h-24 mx-auto mb-6 rounded-full bg-slate-800/50 flex items-center justify-center">
            <span class="text-5xl">📋</span>
          </div>
          <h3 class="text-xl font-bold text-white mb-2">暂无合成配方</h3>
          <p class="text-slate-400 mb-6">官方将陆续推出更多合成配方</p>
        </div>

        <!-- Loading State -->
        <div v-if="loading" class="text-center py-20">
          <div class="w-16 h-16 mx-auto mb-6 rounded-full border-4 border-purple-500 border-t-transparent animate-spin"></div>
          <p class="text-slate-400">加载中...</p>
        </div>
      </div>
    </section>

    <!-- Synthesize Confirmation Modal -->
    <Teleport to="body">
      <transition
        enter-active-class="transition ease-out duration-300"
        enter-from-class="opacity-0 scale-95"
        enter-to-class="opacity-100 scale-100"
        leave-active-class="transition ease-in duration-200"
        leave-from-class="opacity-100 scale-100"
        leave-to-class="opacity-0 scale-95"
      >
        <div v-if="showSynthesizeModal" class="fixed inset-0 z-[100] flex items-center justify-center p-4" @click="closeSynthesizeModal">
          <div class="absolute inset-0 bg-slate-900/90 backdrop-blur-sm"></div>

          <div class="relative z-10 w-full max-w-lg bg-slate-800/90 backdrop-blur-xl rounded-3xl border border-slate-700/50 overflow-hidden" @click.stop>
            <div class="p-8">
              <!-- Animation Area -->
              <div class="relative h-40 mb-6 flex items-center justify-center">
                <!-- Glow Effect -->
                <div class="absolute inset-0 flex items-center justify-center">
                  <div class="w-32 h-32 rounded-full bg-purple-500/20 blur-2xl animate-pulse"></div>
                </div>

                <!-- Fragments Animation -->
                <div v-if="isSynthesizing" class="flex items-center gap-4">
                  <div
                    v-for="(frag, index) in synthesizingRecipe?.fragments.slice(0, 3)"
                    :key="frag.id"
                    class="w-16 h-16 rounded-xl overflow-hidden animate-float"
                    :style="{ animationDelay: `${index * 200}ms` }"
                  >
                    <img :src="frag.image" class="w-full h-full object-cover" />
                  </div>
                  <div class="text-4xl animate-bounce">→</div>
                  <div
                    v-if="synthesizeSuccess"
                    class="w-20 h-20 rounded-xl overflow-hidden border-2 border-emerald-400 shadow-lg shadow-emerald-400/50 animate-scale-in"
                  >
                    <img :src="synthesizingRecipe?.resultImage" class="w-full h-full object-cover" />
                  </div>
                  <div v-else class="w-20 h-20 rounded-xl bg-slate-700 animate-pulse"></div>
                </div>

                <!-- Success Result -->
                <div v-else-if="synthesizeSuccess" class="text-center">
                  <div class="w-24 h-24 mx-auto rounded-2xl overflow-hidden border-2 border-emerald-400 shadow-lg shadow-emerald-400/50 mb-4">
                    <img :src="synthesizingRecipe?.resultImage" class="w-full h-full object-cover" />
                  </div>
                  <p class="text-xl font-black text-emerald-400">合成成功！</p>
                </div>
              </div>

              <!-- Info -->
              <div v-if="!isSynthesizing && !synthesizeSuccess" class="text-center mb-6">
                <h3 class="text-xl font-black text-white mb-2">确认合成</h3>
                <p class="text-slate-400 text-sm">是否消耗以下碎片合成 <strong class="text-white">{{ synthesizingRecipe?.resultName }}</strong>？</p>
              </div>

              <!-- Fragments Required -->
              <div v-if="!isSynthesizing && !synthesizeSuccess" class="bg-slate-900/50 rounded-2xl p-4 mb-6">
                <div class="grid grid-cols-3 gap-3">
                  <div
                    v-for="frag in synthesizingRecipe?.fragments"
                    :key="frag.id"
                    class="text-center"
                  >
                    <div class="w-16 h-16 mx-auto rounded-xl overflow-hidden border border-emerald-400/50 mb-2">
                      <img :src="frag.image" class="w-full h-full object-cover opacity-50" />
                    </div>
                    <p class="text-xs text-slate-400">{{ frag.name }}</p>
                  </div>
                </div>
              </div>

              <!-- Actions -->
              <div v-if="!isSynthesizing && !synthesizeSuccess" class="flex gap-3">
                <button
                  @click="closeSynthesizeModal"
                  class="flex-1 py-3 rounded-xl font-bold bg-slate-700 text-white hover:bg-slate-600 transition-colors"
                >
                  取消
                </button>
                <button
                  @click="confirmSynthesize"
                  class="flex-1 py-3 rounded-xl font-bold bg-gradient-to-r from-purple-500 to-pink-500 text-white shadow-lg hover:shadow-purple-500/30 transition-all"
                >
                  确认合成
                </button>
              </div>

              <div v-if="isSynthesizing" class="text-center">
                <p class="text-slate-400">合成中，请稍候...</p>
              </div>

              <div v-if="synthesizeError" class="text-center mb-4">
                <p class="text-red-400 text-sm">{{ synthesizeError }}</p>
              </div>

              <div v-if="synthesizeSuccess" class="flex gap-3">
                <button
                  @click="closeSynthesizeModal"
                  class="flex-1 py-3 rounded-xl font-bold bg-slate-700 text-white hover:bg-slate-600 transition-colors"
                >
                  关闭
                </button>
                <NuxtLink
                  to="/assets"
                  class="flex-1 py-3 rounded-xl font-bold bg-gradient-to-r from-emerald-500 to-teal-500 text-white shadow-lg hover:shadow-emerald-500/30 transition-all text-center"
                >
                  查看背包
                </NuxtLink>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { synthesizeApi, type SynthesizeRecipe, type SynthesizeResult } from '~/composables/useV2Api'
import { assetApi } from '~/composables/useV2Api'

interface Fragment {
  id: number
  name: string
  image: string
  owned: boolean
}

interface Recipe {
  id: number
  resultName: string
  resultImage: string
  rarity: string
  description: string
  fragments: Fragment[]
  totalFragments: number
  collected: number
  canSynthesize: boolean
}

const categories = ref([
  { id: 'character', name: '角色', total: 12, completed: 5 },
  { id: 'weapon', name: '武器', total: 8, completed: 3 },
  { id: 'skin', name: '皮肤', total: 6, completed: 0 },
  { id: 'material', name: '材料', total: 15, completed: 8 },
])

const recipes = ref<Recipe[]>([])
const loading = ref(false)
const userAssets = ref<any[]>([])

const showSynthesizeModal = ref(false)
const synthesizingRecipe = ref<Recipe | null>(null)
const isSynthesizing = ref(false)
const synthesizeSuccess = ref(false)
const synthesizeError = ref('')

onMounted(async () => {
  await loadData()
})

async function loadData() {
  loading.value = true
  try {
    const [recipesRes, assetsRes] = await Promise.all([
      synthesizeApi.fetchRecipes(1, 100),
      assetApi.fetchUserAssets(1, 1000).catch(() => ({ records: [] }))
    ])

    userAssets.value = assetsRes.records || []

    const itemMap = new Map<number, any>()
    userAssets.value.forEach(asset => {
      const existing = itemMap.get(asset.itemId)
      if (existing) {
        existing.owned += asset.quantity
      } else {
        itemMap.set(asset.itemId, {
          itemId: asset.itemId,
          itemName: asset.itemName,
          itemImage: asset.itemImage,
          owned: asset.quantity
        })
      }
    })

    recipes.value = recipesRes.records.map((recipe: SynthesizeRecipe) => {
      const costItems = typeof recipe.costItems === 'string'
        ? JSON.parse(recipe.costItems)
        : recipe.costItems

      const fragments = costItems.map((cost: any, index: number) => {
        const userAsset = itemMap.get(cost.itemId)
        return {
          id: cost.itemId,
          name: cost.itemName || `材料${index + 1}`,
          image: cost.itemImage || 'https://picsum.photos/seed/default/200/200',
          owned: userAsset ? userAsset.owned >= cost.count : false,
          needCount: cost.count,
          userCount: userAsset ? userAsset.owned : 0
        }
      })

      const collected = fragments.filter((f: Fragment & { owned: boolean }) => f.owned).length
      const totalFragments = fragments.length
      const canSynthesize = fragments.every((f: Fragment & { owned: boolean }) => f.owned)

      return {
        id: recipe.id,
        resultName: recipe.resultItemName,
        resultImage: recipe.resultItemImage || 'https://picsum.photos/seed/result/200/200',
        rarity: recipe.resultItemRarity || 'N',
        description: recipe.description,
        fragments,
        totalFragments,
        collected,
        canSynthesize,
        recipe: recipe
      }
    })
  } catch (error: any) {
    console.error('加载合成数据失败:', error)
  } finally {
    loading.value = false
  }
}

function openSynthesizeModal(recipe: Recipe) {
  synthesizingRecipe.value = recipe
  showSynthesizeModal.value = true
  synthesizeSuccess.value = false
  synthesizeError.value = ''
}

function closeSynthesizeModal() {
  showSynthesizeModal.value = false
  isSynthesizing.value = false
  synthesizeSuccess.value = false
  synthesizeError.value = ''
}

async function confirmSynthesize() {
  if (!synthesizingRecipe.value) return

  isSynthesizing.value = true
  synthesizeError.value = ''

  try {
    const result: SynthesizeResult = await synthesizeApi.do(synthesizingRecipe.value.id)

    if (result.success) {
      synthesizeSuccess.value = true
      await loadData()
    } else {
      synthesizeError.value = result.message || '合成失败'
    }
  } catch (error: any) {
    synthesizeError.value = error.message || '合成失败，请稍后再试'
  } finally {
    isSynthesizing.value = false
  }
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

<style scoped>
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.animate-float {
  animation: float 1s ease-in-out infinite;
}

@keyframes scale-in {
  0% { transform: scale(0); opacity: 0; }
  50% { transform: scale(1.2); }
  100% { transform: scale(1); opacity: 1; }
}

.animate-scale-in {
  animation: scale-in 0.5s ease-out forwards;
}
</style>