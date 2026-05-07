<template>
  <div class="min-h-screen pb-20">
    <!-- Header Section -->
    <section class="relative py-12 overflow-hidden">
      <!-- Background Glow -->
      <div class="absolute inset-0 bg-gradient-to-b from-purple-900/20 via-slate-900 to-slate-900"></div>
      <div class="absolute top-20 left-1/4 w-[400px] h-[400px] bg-amber-600/10 rounded-full blur-[120px]"></div>
      <div class="absolute top-20 right-1/4 w-[400px] h-[400px] bg-purple-600/10 rounded-full blur-[120px]"></div>

      <div class="container mx-auto px-4 relative z-10">
        <div class="max-w-4xl mx-auto">
          <!-- Title -->
          <div class="text-center mb-8">
            <span class="inline-flex items-center gap-2 px-4 py-1.5 rounded-full text-xs font-bold tracking-wider bg-gradient-to-r from-amber-400/20 to-orange-500/20 text-amber-400 border border-amber-400/30 mb-4">
              <span class="text-lg">🎒</span>
              DIGITAL ASSETS
            </span>
            <h1 class="text-4xl font-black text-white mb-2">我的背包</h1>
            <p class="text-slate-400">管理你的数字资产，随时进行交易或实体核销</p>
          </div>

          <!-- Stats Cards -->
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
            <div class="bg-slate-800/50 backdrop-blur-sm rounded-2xl p-4 border border-slate-700/50">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center">
                  <span class="text-lg">📦</span>
                </div>
                <div>
                  <p class="text-slate-500 text-xs">总资产</p>
                  <p class="text-xl font-bold text-white">{{ totalAssets }}</p>
                </div>
              </div>
            </div>
            <div class="bg-slate-800/50 backdrop-blur-sm rounded-2xl p-4 border border-slate-700/50">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-purple-400 to-pink-500 flex items-center justify-center">
                  <span class="text-lg">✨</span>
                </div>
                <div>
                  <p class="text-slate-500 text-xs">成品SSR</p>
                  <p class="text-xl font-bold text-white">{{ ssrCount }}</p>
                </div>
              </div>
            </div>
            <div class="bg-slate-800/50 backdrop-blur-sm rounded-2xl p-4 border border-slate-700/50">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-400 to-cyan-500 flex items-center justify-center">
                  <span class="text-lg">💎</span>
                </div>
                <div>
                  <p class="text-slate-500 text-xs">碎片数</p>
                  <p class="text-xl font-bold text-white">{{ fragmentCount }}</p>
                </div>
              </div>
            </div>
            <div class="bg-slate-800/50 backdrop-blur-sm rounded-2xl p-4 border border-slate-700/50">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-emerald-400 to-teal-500 flex items-center justify-center">
                  <span class="text-lg">🏆</span>
                </div>
                <div>
                  <p class="text-slate-500 text-xs">图鉴完成</p>
                  <p class="text-xl font-bold text-white">{{ collectionProgress }}%</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Tabs Section -->
    <section class="container mx-auto px-4">
      <div class="max-w-4xl mx-auto">
        <!-- Filter Tabs -->
        <div class="flex items-center justify-between mb-6">
          <div class="flex gap-2 overflow-x-auto pb-2">
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
          <div class="flex gap-2">
            <button
              @click="viewMode = 'grid'"
              class="p-2 rounded-lg transition-all"
              :class="viewMode === 'grid' ? 'bg-indigo-500/20 text-indigo-400' : 'bg-slate-800/50 text-slate-400 hover:bg-slate-700'"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>
            </button>
            <button
              @click="viewMode = 'list'"
              class="p-2 rounded-lg transition-all"
              :class="viewMode === 'list' ? 'bg-indigo-500/20 text-indigo-400' : 'bg-slate-800/50 text-slate-400 hover:bg-slate-700'"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg>
            </button>
          </div>
        </div>

        <!-- Loading State -->
        <div v-if="loadingAssets" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          <div v-for="i in 8" :key="i" class="bg-slate-800/50 backdrop-blur-sm rounded-2xl overflow-hidden border border-slate-700/50 animate-pulse">
            <div class="aspect-square bg-slate-700/50"></div>
            <div class="p-3 space-y-2">
              <div class="h-4 bg-slate-700/50 rounded w-3/4"></div>
              <div class="h-3 bg-slate-700/50 rounded w-1/2"></div>
            </div>
          </div>
        </div>

        <!-- Error State -->
        <div v-else-if="errorAssets" class="text-center py-20">
          <div class="w-24 h-24 mx-auto mb-6 rounded-full bg-red-500/10 flex items-center justify-center">
            <span class="text-5xl">❌</span>
          </div>
          <h3 class="text-xl font-bold text-white mb-2">加载失败</h3>
          <p class="text-slate-400 mb-6">{{ errorAssets }}</p>
          <button @click="fetchAssets" class="inline-flex items-center gap-2 px-6 py-3 rounded-xl font-bold bg-gradient-to-r from-indigo-500 to-purple-500 text-white shadow-lg hover:shadow-indigo-500/30 transition-all">
            <span>重新加载</span>
          </button>
        </div>

        <!-- Grid View -->
        <div v-else-if="viewMode === 'grid'" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          <div
            v-for="asset in filteredAssets"
            :key="asset.id"
            class="group relative bg-slate-800/50 backdrop-blur-sm rounded-2xl overflow-hidden border border-slate-700/50 hover:border-opacity-100 transition-all duration-300 hover:scale-[1.02] hover:shadow-xl cursor-pointer"
            :class="getRarityBorderClass(asset.rarity)"
            @click="openAssetDetail(asset)"
          >
            <!-- Asset Image -->
            <div class="aspect-square relative overflow-hidden">
              <img :src="asset.image" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
              <div class="absolute inset-0 bg-gradient-to-t from-slate-900 via-transparent to-transparent"></div>
              <!-- Status Badge -->
              <div v-if="asset.status !== 'normal'" class="absolute top-2 left-2 px-2 py-0.5 rounded-full text-[10px] font-bold"
                :class="getStatusBadgeClass(asset.status)">
                {{ getStatusText(asset.status) }}
              </div>
              <!-- Rarity Badge -->
              <div
                class="absolute top-2 right-2 px-2 py-0.5 rounded-full text-[10px] font-bold"
                :class="getRarityBadgeClass(asset.rarity)"
              >
                {{ asset.rarity }}
              </div>
            </div>
            <!-- Asset Info -->
            <div class="p-3">
              <h4 class="text-sm font-bold text-white truncate">{{ asset.name }}</h4>
              <p class="text-xs text-slate-500 truncate">{{ asset.type }}</p>
              <div class="flex items-center justify-between mt-2">
                <span class="text-[10px] text-slate-600">{{ asset.source }}</span>
                <button
                  v-if="asset.status === 'normal' && asset.type === '角色'"
                  @click.stop="openSynthesize(asset)"
                  class="text-xs text-amber-400 hover:text-amber-300"
                >
                  合成 →
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- List View -->
        <div v-else class="space-y-3">
          <div
            v-for="asset in filteredAssets"
            :key="asset.id"
            class="flex items-center gap-4 bg-slate-800/50 backdrop-blur-sm rounded-2xl p-4 border border-slate-700/50 hover:border-slate-600 transition-all cursor-pointer"
            :class="getRarityBorderClass(asset.rarity)"
            @click="openAssetDetail(asset)"
          >
            <div class="w-16 h-16 rounded-xl overflow-hidden flex-shrink-0">
              <img :src="asset.image" class="w-full h-full object-cover" />
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <h4 class="text-sm font-bold text-white truncate">{{ asset.name }}</h4>
                <span class="px-2 py-0.5 rounded-full text-[10px] font-bold" :class="getRarityBadgeClass(asset.rarity)">
                  {{ asset.rarity }}
                </span>
                <span v-if="asset.status !== 'normal'" class="px-2 py-0.5 rounded-full text-[10px] font-bold" :class="getStatusBadgeClass(asset.status)">
                  {{ getStatusText(asset.status) }}
                </span>
              </div>
              <p class="text-xs text-slate-500">{{ asset.type }} · {{ asset.source }}</p>
            </div>
            <div class="flex gap-2">
              <button
                v-if="asset.status === 'normal' && asset.type === '碎片'"
                @click.stop="openSynthesize(asset)"
                class="px-3 py-1.5 rounded-lg text-xs font-medium bg-amber-500/20 text-amber-400 hover:bg-amber-500/30 transition-colors"
              >
                合成
              </button>
              <button
                v-if="asset.status === 'normal' && asset.rarity === 'SSR'"
                @click.stop="openRedeem(asset)"
                class="px-3 py-1.5 rounded-lg text-xs font-medium bg-emerald-500/20 text-emerald-400 hover:bg-emerald-500/30 transition-colors"
              >
                核销
              </button>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div v-if="!loadingAssets && !errorAssets && filteredAssets.length === 0" class="text-center py-20">
          <div class="w-24 h-24 mx-auto mb-6 rounded-full bg-slate-800/50 flex items-center justify-center">
            <span class="text-5xl">📦</span>
          </div>
          <h3 class="text-xl font-bold text-white mb-2">背包空空如也</h3>
          <p class="text-slate-400 mb-6">快去抽赏中心试试手气吧</p>
          <NuxtLink to="/gacha" class="inline-flex items-center gap-2 px-6 py-3 rounded-xl font-bold bg-gradient-to-r from-amber-500 to-orange-500 text-white shadow-lg hover:shadow-amber-500/30 transition-all">
            <span>去抽赏</span>
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
          </NuxtLink>
        </div>
      </div>
    </section>

    <!-- Asset Detail Modal -->
    <Teleport to="body">
      <transition
        enter-active-class="transition ease-out duration-300"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="transition ease-in duration-200"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <div v-if="showAssetDetail" class="fixed inset-0 z-[100] flex items-center justify-center p-4" @click="closeAssetDetail">
          <div class="absolute inset-0 bg-slate-900/90 backdrop-blur-sm"></div>

          <div class="relative z-10 w-full max-w-md bg-slate-800/90 backdrop-blur-xl rounded-3xl border border-slate-700/50 overflow-hidden" @click.stop>
            <!-- Header Image -->
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
              </div>
            </div>

            <!-- Content -->
            <div class="p-6">
              <h3 class="text-xl font-black text-white mb-1">{{ selectedAsset?.name }}</h3>
              <p class="text-sm text-slate-400 mb-4">{{ selectedAsset?.type }} · {{ selectedAsset?.source }}</p>

              <!-- Properties -->
              <div class="space-y-3 mb-6">
                <div class="flex items-center justify-between py-2 border-b border-slate-700/50">
                  <span class="text-slate-500 text-sm">稀有度</span>
                  <span class="font-bold" :class="getRarityTextClass(selectedAsset?.rarity || '')">{{ selectedAsset?.rarity }}</span>
                </div>
                <div class="flex items-center justify-between py-2 border-b border-slate-700/50">
                  <span class="text-slate-500 text-sm">状态</span>
                  <span class="font-bold" :class="getStatusTextClass(selectedAsset?.status || 'normal')">{{ getStatusText(selectedAsset?.status || 'normal') }}</span>
                </div>
                <div class="flex items-center justify-between py-2 border-b border-slate-700/50">
                  <span class="text-slate-500 text-sm">获取时间</span>
                  <span class="text-white text-sm">{{ selectedAsset?.acquiredAt }}</span>
                </div>
              </div>

              <!-- Actions -->
              <div class="space-y-3">
                <button
                  v-if="selectedAsset?.status === 'normal' && selectedAsset?.type === '碎片'"
                  @click="openSynthesize(selectedAsset)"
                  class="w-full py-3 rounded-xl font-bold bg-gradient-to-r from-amber-500 to-orange-500 text-white shadow-lg hover:shadow-amber-500/30 transition-all"
                >
                  前往合成
                </button>
                <button
                  v-if="selectedAsset?.status === 'normal' && selectedAsset?.rarity === 'SSR'"
                  @click="openRedeem(selectedAsset)"
                  class="w-full py-3 rounded-xl font-bold bg-gradient-to-r from-emerald-500 to-teal-500 text-white shadow-lg hover:shadow-emerald-500/30 transition-all"
                >
                  申请实体核销
                </button>
                <button
                  v-if="selectedAsset?.status === 'normal'"
                  @click="openListForSale(selectedAsset)"
                  class="w-full py-3 rounded-xl font-bold bg-slate-700 text-white hover:bg-slate-600 transition-colors"
                >
                  上架出售
                </button>
                <button
                  v-if="selectedAsset?.status === 'listing'"
                  class="w-full py-3 rounded-xl font-bold bg-rose-500/20 text-rose-400 border border-rose-500/30 cursor-not-allowed"
                  disabled
                >
                  出售中（无法操作）
                </button>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </Teleport>

    <!-- Synthesize Modal -->
    <Teleport to="body">
      <transition
        enter-active-class="transition ease-out duration-300"
        enter-from-class="opacity-0 scale-95"
        enter-to-class="opacity-100 scale-100"
        leave-active-class="transition ease-in duration-200"
        leave-from-class="opacity-100 scale-100"
        leave-to-class="opacity-0 scale-95"
      >
        <div v-if="showSynthesize" class="fixed inset-0 z-[100] flex items-center justify-center p-4" @click="closeSynthesize">
          <div class="absolute inset-0 bg-slate-900/90 backdrop-blur-sm"></div>

          <div class="relative z-10 w-full max-w-lg bg-slate-800/90 backdrop-blur-xl rounded-3xl border border-slate-700/50 overflow-hidden" @click.stop>
            <div class="p-6">
              <div class="text-center mb-6">
                <span class="text-5xl mb-4 block">⚗️</span>
                <h3 class="text-xl font-black text-white mb-2">碎片合成</h3>
                <p class="text-slate-400 text-sm">集齐所有碎片即可合成完整角色</p>
              </div>

              <!-- Required Fragments -->
              <div class="bg-slate-900/50 rounded-2xl p-4 mb-6">
                <div class="grid grid-cols-3 gap-3">
                  <div
                    v-for="(frag, index) in requiredFragments"
                    :key="index"
                    class="relative aspect-square rounded-xl overflow-hidden border-2 transition-all"
                    :class="hasFragment(frag.id) ? 'border-emerald-400' : 'border-slate-700 opacity-50'"
                  >
                    <img :src="frag.image" class="w-full h-full object-cover" />
                    <div v-if="hasFragment(frag.id)" class="absolute inset-0 bg-emerald-400/20 flex items-center justify-center">
                      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#34d399" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg>
                    </div>
                    <div v-else class="absolute inset-0 bg-slate-900/50 flex items-center justify-center">
                      <span class="text-xs text-slate-500">缺少</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Progress -->
              <div class="flex items-center justify-between mb-6">
                <span class="text-slate-400 text-sm">合成进度</span>
                <span class="text-sm font-bold text-emerald-400">{{ collectedCount }}/{{ requiredFragments.length }}</span>
              </div>
              <div class="h-2 bg-slate-700 rounded-full overflow-hidden mb-6">
                <div
                  class="h-full bg-gradient-to-r from-emerald-400 to-teal-400 rounded-full transition-all duration-500"
                  :style="{ width: `${(collectedCount / requiredFragments.length) * 100}%` }"
                ></div>
              </div>

              <!-- Actions -->
              <div class="flex gap-3">
                <button
                  @click="closeSynthesize"
                  class="flex-1 py-3 rounded-xl font-bold bg-slate-700 text-white hover:bg-slate-600 transition-colors"
                >
                  关闭
                </button>
                <button
                  v-if="canSynthesize"
                  @click="handleSynthesize"
                  class="flex-1 py-3 rounded-xl font-bold bg-gradient-to-r from-amber-500 to-orange-500 text-white shadow-lg hover:shadow-amber-500/30 transition-all"
                >
                  立即合成
                </button>
                <button
                  v-else
                  class="flex-1 py-3 rounded-xl font-bold bg-slate-700 text-slate-400 cursor-not-allowed"
                  disabled
                >
                  碎片不足
                </button>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </Teleport>

    <!-- Redeem Modal -->
    <Teleport to="body">
      <transition
        enter-active-class="transition ease-out duration-300"
        enter-from-class="opacity-0 scale-95"
        enter-to-class="opacity-100 scale-100"
        leave-active-class="transition ease-in duration-200"
        leave-from-class="opacity-100 scale-100"
        leave-to-class="opacity-0 scale-95"
      >
        <div v-if="showRedeem" class="fixed inset-0 z-[100] flex items-center justify-center p-4" @click="closeRedeem">
          <div class="absolute inset-0 bg-slate-900/90 backdrop-blur-sm"></div>

          <div class="relative z-10 w-full max-w-md bg-slate-800/90 backdrop-blur-xl rounded-3xl border border-slate-700/50 overflow-hidden" @click.stop>
            <div class="p-6">
              <div class="text-center mb-6">
                <span class="text-5xl mb-4 block">📦</span>
                <h3 class="text-xl font-black text-white mb-2">申请实体核销</h3>
                <p class="text-slate-400 text-sm">将数字资产转化为实体商品发货到家</p>
              </div>

              <!-- Asset Preview -->
              <div class="flex items-center gap-4 bg-slate-900/50 rounded-2xl p-4 mb-6">
                <div class="w-16 h-16 rounded-xl overflow-hidden">
                  <img :src="selectedAsset?.image" class="w-full h-full object-cover" />
                </div>
                <div>
                  <h4 class="font-bold text-white">{{ selectedAsset?.name }}</h4>
                  <p class="text-sm text-slate-400">{{ selectedAsset?.rarity }} · {{ selectedAsset?.type }}</p>
                </div>
              </div>

              <!-- Warning -->
              <div class="bg-amber-500/10 border border-amber-500/20 rounded-xl p-4 mb-6">
                <p class="text-amber-400 text-sm">
                  <strong>注意：</strong>实体核销后将扣除该资产，且不可撤销。资产将被标记为"已核销/待发货"状态。
                </p>
              </div>

              <!-- Address Form -->
              <div class="space-y-4 mb-6">
                <div>
                  <label class="block text-sm text-slate-400 mb-2">收货人</label>
                  <input v-model="redeemForm.receiver" type="text" class="w-full px-4 py-3 rounded-xl bg-slate-900/50 border border-slate-700 text-white placeholder-slate-500 focus:border-indigo-500 focus:outline-none transition-colors" placeholder="请输入收货人姓名" />
                </div>
                <div>
                  <label class="block text-sm text-slate-400 mb-2">手机号码</label>
                  <input v-model="redeemForm.phone" type="tel" class="w-full px-4 py-3 rounded-xl bg-slate-900/50 border border-slate-700 text-white placeholder-slate-500 focus:border-indigo-500 focus:outline-none transition-colors" placeholder="请输入手机号码" />
                </div>
                <div>
                  <label class="block text-sm text-slate-400 mb-2">收货地址</label>
                  <textarea v-model="redeemForm.address" rows="2" class="w-full px-4 py-3 rounded-xl bg-slate-900/50 border border-slate-700 text-white placeholder-slate-500 focus:border-indigo-500 focus:outline-none transition-colors resize-none" placeholder="请输入详细收货地址"></textarea>
                </div>
              </div>

              <!-- Actions -->
              <div class="flex gap-3">
                <button
                  @click="closeRedeem"
                  class="flex-1 py-3 rounded-xl font-bold bg-slate-700 text-white hover:bg-slate-600 transition-colors"
                >
                  取消
                </button>
                <button
                  @click="handleRedeem"
                  :disabled="!canSubmitRedeem"
                  class="flex-1 py-3 rounded-xl font-bold bg-gradient-to-r from-emerald-500 to-teal-500 text-white shadow-lg hover:shadow-emerald-500/30 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  确认核销
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
import { assetApi, type UserAsset } from '~/composables/useV2Api'
import { useAuthGuard } from '~/composables/useAuthGuard'

const { requireAuth } = useAuthGuard()

interface Asset {
  id: string
  name: string
  type: string
  rarity: string
  image: string
  source: string
  status: 'normal' | 'listing' | 'synthesizing' | 'redeeming' | 'redeemed'
  acquiredAt: string
  assetId: number
}

interface Fragment {
  id: string
  name: string
  image: string
  needed: boolean
}

const tabs = [
  { id: 'all', name: '全部' },
  { id: 'character', name: '角色' },
  { id: 'weapon', name: '武器' },
  { id: 'fragment', name: '碎片' },
  { id: 'material', name: '材料' }
]

const activeTab = ref('all')
const viewMode = ref<'grid' | 'list'>('grid')

const assets = ref<Asset[]>([])
const loadingAssets = ref(false)
const errorAssets = ref('')

function mapUserAssetToAsset(userAsset: UserAsset): Asset {
  let status: Asset['status'] = 'normal'
  if (userAsset.status === 1) status = 'listing'
  else if (userAsset.status === 2) status = 'synthesizing'
  else if (userAsset.status === 3) status = 'redeeming'
  else if (userAsset.status === 4) status = 'redeemed'

  return {
    id: String(userAsset.id),
    name: userAsset.itemName,
    type: userAsset.itemType,
    rarity: userAsset.itemRarity,
    image: userAsset.itemImage,
    source: userAsset.acquireType || '抽赏获得',
    status,
    acquiredAt: userAsset.createTime ? new Date(userAsset.createTime).toLocaleDateString('zh-CN') : '',
    assetId: userAsset.id
  }
}

async function fetchAssets() {
  loadingAssets.value = true
  errorAssets.value = ''
  try {
    const result = await assetApi.fetchUserAssets(1, 100)
    assets.value = (result.records || []).map(mapUserAssetToAsset)
  } catch (e: any) {
    errorAssets.value = e.message || '获取资产失败'
    console.error('fetchAssets error:', e)
  } finally {
    loadingAssets.value = false
  }
}

onMounted(() => {
  fetchAssets()
})

// Stats
const totalAssets = computed(() => assets.value.length)
const ssrCount = computed(() => assets.value.filter(a => a.rarity === 'SSR').length)
const fragmentCount = computed(() => assets.value.filter(a => a.type === '碎片').length)
const collectionProgress = computed(() => Math.round((ssrCount.value / 10) * 100))

// Filter
const filteredAssets = computed(() => {
  if (activeTab.value === 'all') return assets.value
  return assets.value.filter(a => {
    if (activeTab.value === 'character') return a.type === '角色'
    if (activeTab.value === 'weapon') return a.type === '武器'
    if (activeTab.value === 'fragment') return a.type === '碎片'
    if (activeTab.value === 'material') return a.type === '材料'
    return true
  })
})

// Asset Detail Modal
const showAssetDetail = ref(false)
const selectedAsset = ref<Asset | null>(null)

function openAssetDetail(asset: Asset) {
  selectedAsset.value = asset
  showAssetDetail.value = true
}

function closeAssetDetail() {
  showAssetDetail.value = false
}

// Synthesize Modal
const showSynthesize = ref(false)
const requiredFragments = ref([
  { id: '2', name: '碎片-A', image: 'https://picsum.photos/seed/prize3/200/200', needed: true },
  { id: '3', name: '碎片-B', image: 'https://picsum.photos/seed/prize4/200/200', needed: true },
  { id: '4', name: '碎片-C', image: 'https://picsum.photos/seed/prize5/200/200', needed: true },
])

function hasFragment(id: string): boolean {
  return assets.value.some(a => a.id === id && a.status === 'normal')
}

const collectedCount = computed(() => requiredFragments.value.filter(f => hasFragment(f.id)).length)
const canSynthesize = computed(() => collectedCount.value === requiredFragments.value.length)

function openSynthesize(asset: Asset | null) {
  const authorized = requireAuth()
  if (!authorized) return
  closeAssetDetail()
  showSynthesize.value = true
}

function closeSynthesize() {
  showSynthesize.value = false
}

function handleSynthesize() {
  // TODO: Implement synthesize logic
  alert('合成成功！')
  closeSynthesize()
}

// Redeem Modal
const showRedeem = ref(false)
const redeemForm = ref({
  receiver: '',
  phone: '',
  address: ''
})

const canSubmitRedeem = computed(() => {
  return redeemForm.value.receiver.trim() && redeemForm.value.phone.trim() && redeemForm.value.address.trim()
})

function openRedeem(asset: Asset | null) {
  const authorized = requireAuth()
  if (!authorized) return
  closeAssetDetail()
  showRedeem.value = true
}

function closeRedeem() {
  showRedeem.value = false
  redeemForm.value = { receiver: '', phone: '', address: '' }
}

function handleRedeem() {
  // TODO: Implement redeem logic
  alert('核销申请已提交！')
  closeRedeem()
}

function openListForSale(asset: Asset | null) {
  const authorized = requireAuth()
  if (!authorized) return
  closeAssetDetail()
  alert('前往上架页面...')
}

// Helper Functions
function getRarityBorderClass(rarity: string): string {
  const classes: Record<string, string> = {
    'SSR': 'border-amber-400/50',
    'SR': 'border-purple-400/50',
    'R': 'border-blue-400/50',
    'N': 'border-slate-600/50'
  }
  return classes[rarity] || classes['N']
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

function getRarityTextClass(rarity: string): string {
  const classes: Record<string, string> = {
    'SSR': 'text-amber-400',
    'SR': 'text-purple-400',
    'R': 'text-blue-400',
    'N': 'text-slate-500'
  }
  return classes[rarity] || classes['N']
}

function getStatusBadgeClass(status: string): string {
  const classes: Record<string, string> = {
    'listing': 'bg-amber-500/20 text-amber-400',
    'synthesizing': 'bg-purple-500/20 text-purple-400',
    'redeeming': 'bg-emerald-500/20 text-emerald-400',
    'redeemed': 'bg-slate-500/20 text-slate-400'
  }
  return classes[status] || ''
}

function getStatusTextClass(status: string): string {
  const classes: Record<string, string> = {
    'listing': 'text-amber-400',
    'synthesizing': 'text-purple-400',
    'redeeming': 'text-emerald-400',
    'redeemed': 'text-slate-400'
  }
  return classes[status] || ''
}

function getStatusText(status: string): string {
  const texts: Record<string, string> = {
    'normal': '正常',
    'listing': '出售中',
    'synthesizing': '合成锁定',
    'redeeming': '核销中',
    'redeemed': '已核销'
  }
  return texts[status] || status
}
</script>