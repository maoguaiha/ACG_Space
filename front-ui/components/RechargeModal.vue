<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center">
    <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="$emit('close')"></div>
    <div class="relative bg-slate-900 rounded-2xl border border-slate-700 p-6 w-full max-w-md mx-4 shadow-2xl">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-xl font-bold text-white">充值积分</h2>
        <button @click="$emit('close')" class="text-slate-400 hover:text-white transition-colors">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>

      <div class="grid grid-cols-2 gap-3 mb-6">
        <button
          v-for="pkg in packages"
          :key="pkg.amount"
          @click="selectPackage(pkg)"
          class="p-4 rounded-xl border transition-all"
          :class="selectedPackage?.amount === pkg.amount
            ? 'bg-gradient-to-r from-amber-500/20 to-orange-500/20 border-amber-500/50'
            : 'bg-slate-800/50 border-slate-700 hover:border-slate-600'"
        >
          <p class="text-2xl font-bold text-amber-400 mb-1">{{ pkg.points }}</p>
          <p class="text-xs text-slate-400">积分</p>
          <p class="text-sm font-bold text-white mt-2">¥{{ pkg.amount }}</p>
          <p v-if="pkg.bonus" class="text-xs text-emerald-400 mt-1">{{ pkg.bonus }}</p>
        </button>
      </div>

      <div v-if="loading" class="text-center text-slate-400 py-4">处理中...</div>
      <div v-else-if="orderNo" class="text-center py-4">
        <p class="text-slate-400 mb-2">订单号: {{ orderNo }}</p>
        <button
          @click="mockPay"
          class="px-6 py-3 rounded-xl font-bold bg-gradient-to-r from-emerald-500 to-teal-500 text-white shadow-lg hover:shadow-emerald-500/30 transition-all"
        >
          模拟支付 ¥{{ selectedPackage?.amount }}
        </button>
      </div>
      <button
        v-else
        @click="createOrder"
        :disabled="!selectedPackage"
        class="w-full py-3 rounded-xl font-bold text-white bg-gradient-to-r from-amber-500 to-orange-500 shadow-lg shadow-amber-500/30 hover:shadow-amber-500/50 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
      >
        立即充值
      </button>

      <p class="text-xs text-slate-500 text-center mt-4">充值即表示同意相关服务条款</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const emit = defineEmits(['close', 'success'])

const userStore = useUserStore()

function getAuthHeaders(): Record<string, string> {
  return userStore.token ? { 'Authorization': `Bearer ${userStore.token}` } : {}
}

interface RechargePackage {
  amount: number
  points: number
  bonus?: string
}

const packages: RechargePackage[] = [
  { amount: 1, points: 10 },
  { amount: 10, points: 100 },
  { amount: 50, points: 550, bonus: '送50积分' },
  { amount: 100, points: 1200, bonus: '送200积分' }
]

const selectedPackage = ref<RechargePackage | null>(null)
const loading = ref(false)
const orderNo = ref('')

function selectPackage(pkg: RechargePackage) {
  selectedPackage.value = pkg
  orderNo.value = ''
}

async function createOrder() {
  if (!selectedPackage.value) return
  loading.value = true
  try {
    const result = await $fetch<{ code: number; data: { orderNo: string } }>('/api-proxy/recharge/create', {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({
        amount: selectedPackage.value.amount,
        points: selectedPackage.value.points
      })
    })
    orderNo.value = result.data.orderNo
  } catch (e: any) {
    alert(e.message || '创建订单失败')
  } finally {
    loading.value = false
  }
}

async function mockPay() {
  if (!orderNo.value) return
  loading.value = true
  try {
    await $fetch('/api-proxy/recharge/mock-pay', {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ orderNo: orderNo.value })
    })
    alert('充值成功！')
    emit('success')
  } catch (e: any) {
    alert(e.message || '支付失败')
  } finally {
    loading.value = false
  }
}
</script>
