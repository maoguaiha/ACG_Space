<template>
  <div class="min-h-screen pb-20">
    <!-- Sub-navigation with Points Display -->
    <div class="sticky top-16 z-40 bg-slate-900/95 backdrop-blur-xl border-b border-slate-700/50">
      <div class="container mx-auto px-4">
        <div class="flex items-center justify-between py-3">
          <!-- Tab Navigation -->
          <div class="flex gap-2 overflow-x-auto">
            <button
              v-for="tab in tabs"
              :key="tab.id"
              @click="activeTab = tab.id"
              class="px-5 py-2.5 rounded-xl text-sm font-bold whitespace-nowrap transition-all"
              :class="activeTab === tab.id
                ? 'bg-gradient-to-r from-blue-500 to-indigo-500 text-white shadow-lg'
                : 'bg-slate-800/50 text-slate-400 hover:bg-slate-700 hover:text-white'"
            >
              {{ tab.icon }} {{ tab.name }}
            </button>
          </div>

          <!-- Points & Fragment Display -->
          <div class="flex items-center gap-3">
            <div class="flex items-center gap-2 px-3 py-1.5 bg-gradient-to-r from-cyan-500/10 to-blue-500/10 rounded-xl border border-cyan-500/20">
              <span class="text-sm">💎</span>
              <span class="text-sm font-bold text-cyan-400">{{ userFragment }}</span>
            </div>
            <div class="flex items-center gap-3 px-4 py-2 bg-gradient-to-r from-amber-500/10 to-orange-500/10 rounded-xl border border-amber-500/20">
              <div class="w-8 h-8 rounded-full bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center">
                <span class="text-sm">💰</span>
              </div>
              <div>
                <p class="text-slate-400 text-xs">剩余积分</p>
                <p class="text-lg font-bold text-amber-400">{{ userPoints.toLocaleString() }}</p>
              </div>
            </div>
            <button
              @click="showRecharge = true"
              class="px-4 py-2 rounded-xl text-sm font-bold bg-gradient-to-r from-emerald-500 to-teal-500 text-white shadow-lg hover:shadow-emerald-500/30 transition-all"
            >
              充值
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content Area -->
    <div class="pt-4">
      <!-- Draw Tab -->
      <div v-show="activeTab === 'draw'">
        <GachaDraw
          :user-points="userPoints"
          :user-fragment="userFragment"
          @update-points="fetchUserData"
        />
      </div>

      <!-- Assets Tab -->
      <div v-show="activeTab === 'assets'">
        <GachaAssets
          :user-points="userPoints"
          :user-fragment="userFragment"
          @update-points="fetchUserData"
        />
      </div>

      <!-- Redeem Tab -->
      <div v-show="activeTab === 'redeem'">
        <GachaRedeem
          :user-points="userPoints"
          :user-fragment="userFragment"
          @update-points="fetchUserData"
        />
      </div>
    </div>

    <!-- Recharge Modal -->
    <RechargeModal
      v-if="showRecharge"
      @close="showRecharge = false"
      @success="handleRechargeSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { gachaApi } from '~/composables/useV2Api'
import { useUserStore } from '~/stores/user'

const userStore = useUserStore()

function getAuthHeaders(): Record<string, string> {
  return userStore.token ? { 'Authorization': `Bearer ${userStore.token}` } : {}
}

const tabs = [
  { id: 'draw', name: '抽赏', icon: '🎰' },
  { id: 'assets', name: '背包', icon: '🎒' },
  { id: 'redeem', name: '兑换', icon: '🎁' }
]

const activeTab = ref('draw')
const userPoints = ref(0)
const userFragment = ref(0)
const showRecharge = ref(false)

async function fetchUserData() {
  try {
    const points = await gachaApi.fetchUserPoints()
    userPoints.value = points
  } catch (e) {
    console.error('加载用户积分失败', e)
  }
  try {
    const response = await $fetch<{ code: number; msg: string; data: number }>('/api-proxy/fragment/my', {
      headers: getAuthHeaders()
    })
    if (response.code === 200) {
      userFragment.value = response.data
    }
  } catch (e) {
    console.error('加载碎片失败', e)
  }
}

function handleRechargeSuccess() {
  showRecharge.value = false
  fetchUserData()
}

onMounted(() => {
  fetchUserData()
})
</script>

<style scoped>
</style>
