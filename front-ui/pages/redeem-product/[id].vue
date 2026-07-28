<template>
  <div class="min-h-screen py-8">
    <div class="container mx-auto px-4 max-w-4xl">
      <NuxtLink to="/gacha" class="inline-flex items-center gap-2 mb-6 px-4 py-2 rounded-xl text-sm bg-slate-800/50 text-slate-300 hover:bg-slate-700">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m15 18-6-6 6-6"/></svg> 返回抽赏
      </NuxtLink>

      <div v-if="loading" class="text-center py-12">
        <div class="w-12 h-12 border-4 border-indigo-500/30 border-t-indigo-500 rounded-full animate-spin mx-auto"></div>
        <p class="mt-4 text-slate-400">加载中...</p>
      </div>
      <div v-else-if="!product" class="text-center py-12"><p class="text-slate-400 text-lg">商品不存在</p></div>
      <div v-else class="rounded-3xl overflow-hidden border border-slate-700/50 bg-slate-800/50 backdrop-blur-xl">
        <div class="md:flex">
          <div class="md:w-1/2 aspect-square bg-slate-900 relative">
            <img :src="product.image" class="w-full h-full object-cover" @error="imgErr" />
            <div class="absolute top-3 left-3 px-3 py-1 rounded-full text-xs font-bold bg-emerald-500/90 text-white">实物</div>
            <div v-if="product.stock===0" class="absolute top-3 right-3 px-3 py-1 rounded-full text-xs font-bold bg-slate-500/90 text-white">已下架</div>
            <div v-else-if="product.stock<=5" class="absolute top-3 right-3 px-3 py-1 rounded-full text-xs font-bold bg-red-500/90 text-white">仅剩{{ product.stock }}件</div>
          </div>
          <div class="md:w-1/2 p-6 md:p-8">
            <h1 class="text-2xl md:text-3xl font-black mb-4 text-slate-100">{{ product.name }}</h1>
            <p v-if="product.description" class="text-sm leading-relaxed mb-6 text-slate-400">{{ product.description }}</p>
            <div class="space-y-3 mb-6">
              <div class="flex justify-between p-3 rounded-xl bg-slate-900/40"><span class="text-slate-400">UR碎片</span><span class="text-red-400 font-bold">{{ product.urFragmentCost }}</span></div>
              <div class="flex justify-between p-3 rounded-xl bg-slate-900/40"><span class="text-slate-400">积分</span><span class="text-amber-400 font-bold">{{ product.pointsCost }}</span></div>
              <div class="flex justify-between p-3 rounded-xl bg-slate-900/40"><span class="text-slate-400">已兑换</span><span class="font-bold">{{ product.exchangedCount }}</span></div>
              <div class="flex justify-between p-3 rounded-xl bg-slate-900/40"><span class="text-slate-400">剩余</span><span class="font-bold" :style="{color:product.stock>5?'#10B981':product.stock>0?'#F59E0B':'#EF4444'}">{{ product.stock }}</span></div>
            </div>
            <div class="flex gap-3">
              <button @click="$router.push('/gacha')" class="flex-1 py-3 rounded-xl font-bold bg-slate-700 text-white hover:bg-slate-600">返回</button>
              <button :disabled="product.stock<=0||!$userStore.isLoggedIn"
                :class="product.stock>0?'flex-1 py-3 rounded-xl font-bold bg-gradient-to-r from-emerald-500 to-teal-500 text-white shadow-lg disabled:opacity-50':'flex-1 py-3 rounded-xl font-bold bg-slate-700 text-slate-500'">
                {{ product.stock<=0?'已售罄':!$userStore.isLoggedIn?'请先登录':'立即兑换' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
const route = useRoute()
const product = ref(null)
const loading = ref(false)
async function load() {
  loading.value = true
  try {
    const { fetchRedeemProduct } = await import('~/composables/useApi')
    const res = await fetchRedeemProduct(route.params.id)
    if (res.code === 200) product.value = res.data
  } catch(e) {} finally { loading.value = false }
}
function imgErr(e) {
  const img = e.target
  if (img) img.src = 'data:image/svg+xml;base64,' + btoa('<svg xmlns="http://www.w3.org/2000/svg" width="600" height="600"><rect width="600" height="600" fill="#6366F1"/><text x="300" y="310" text-anchor="middle" fill="white" font-size="32" font-weight="bold">' + ((product.value?.name||'').slice(0,8)) + '</text></svg>')
}
onMounted(load)
</script>
