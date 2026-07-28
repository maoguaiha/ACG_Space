<template>
  <div class="min-h-screen py-8">
    <div class="container mx-auto px-4 max-w-4xl">
      <NuxtLink to="/gacha" class="inline-flex items-center gap-2 mb-6 px-4 py-2 rounded-xl text-sm bg-slate-800/50 text-slate-300 hover:bg-slate-700">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m15 18-6-6 6-6"/></svg> 返回抽赏
      </NuxtLink>
      <div v-if="loading" class="text-center py-12">加载中...</div>
      <div v-else-if="!order" class="text-center py-12">订单不存在</div>
      <div v-else class="rounded-3xl border border-slate-700/50 bg-slate-800/50 backdrop-blur-xl overflow-hidden">
        <div class="p-6 md:p-8">
          <div class="flex items-center justify-between mb-6">
            <h1 class="text-xl font-black text-slate-100">订单详情</h1>
            <span class="px-3 py-1 rounded-full text-sm font-bold" :class="sc">{{ st }}</span>
          </div>
          <div class="flex gap-4 mb-6 p-4 rounded-2xl bg-slate-900/50">
            <div class="w-20 h-20 rounded-xl overflow-hidden bg-slate-800 flex-shrink-0">
              <img :src="order.productImage||order.assetImage" class="w-full h-full object-cover" />
            </div>
            <div>
              <h2 class="font-bold text-slate-100">{{ order.productName||order.assetName }}</h2>
              <p class="text-xs text-slate-500 mt-1">{{ order.orderNo }}</p>
            </div>
          </div>
          <div class="grid grid-cols-2 gap-3 mb-6">
            <div class="p-3 rounded-xl bg-slate-900/40"><span class="text-xs text-slate-500">UR碎片</span><p class="font-bold text-red-400">{{ order.urFragmentCost }}</p></div>
            <div class="p-3 rounded-xl bg-slate-900/40"><span class="text-xs text-slate-500">积分</span><p class="font-bold text-amber-400">{{ order.pointsCost }}</p></div>
          </div>
          <div class="mb-6">
            <h3 class="font-bold text-slate-300 mb-2">收货信息</h3>
            <p class="text-sm text-slate-400">{{ order.receiver }} {{ order.phone }}</p>
            <p class="text-sm text-slate-400">{{ order.province }}{{ order.city }}{{ order.district }}{{ order.address }}</p>
          </div>
          <div v-if="order.status>=1&&order.logisticsCompany" class="p-4 rounded-2xl bg-slate-900/50">
            <p class="text-sm text-slate-400">{{ order.logisticsCompany }} - {{ order.logisticsNo }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
const route = useRoute()
const order = ref(null)
const loading = ref(false)
const sc = computed(() => ({0:'bg-amber-500/20 text-amber-400',1:'bg-blue-500/20 text-blue-400',2:'bg-emerald-500/20 text-emerald-400'}[order.value?.status]||'bg-slate-500/20 text-slate-400'))
const st = computed(() => ({0:'待发货',1:'已发货',2:'已完成'}[order.value?.status]||'未知'))
onMounted(async () => {
  loading.value = true
  try {
    const { fetchRedeemOrder } = await import('~/composables/useApi')
    const res = await fetchRedeemOrder(route.params.id)
    if (res.code === 200) order.value = res.data
  } catch(e) {} finally { loading.value = false }
})
</script>
