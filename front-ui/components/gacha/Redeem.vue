<template>
  <div class="container mx-auto px-4">
    <div class="max-w-4xl mx-auto">
      <!-- Tabs -->
      <div class="flex gap-2 mb-6">
        <button
          @click="activeTab = 'products'"
          class="px-4 py-2 rounded-xl text-sm font-medium transition-all"
          :class="activeTab === 'products'
            ? 'bg-gradient-to-r from-emerald-500 to-teal-500 text-white shadow-lg'
            : 'bg-slate-800/50 text-slate-400 hover:bg-slate-700'"
        >
          可兑换实物
        </button>
        <button
          @click="activeTab = 'orders'"
          class="px-4 py-2 rounded-xl text-sm font-medium transition-all"
          :class="activeTab === 'orders'
            ? 'bg-gradient-to-r from-emerald-500 to-teal-500 text-white shadow-lg'
            : 'bg-slate-800/50 text-slate-400 hover:bg-slate-700'"
        >
          我的订单
        </button>
      </div>

      <!-- Products Tab -->
      <div v-show="activeTab === 'products'">
        <div v-if="loadingProducts" class="text-center text-slate-400 py-8">加载中...</div>
        <div v-else-if="products.length === 0" class="text-center py-12">
          <span class="text-6xl mb-4 block">📦</span>
          <p class="text-slate-400 mb-2">暂无可兑换的实物</p>
          <p class="text-slate-500 text-sm">管理员上架商品后即可兑换</p>
        </div>
        <div v-else class="grid grid-cols-2 md:grid-cols-3 gap-4">
          <div
            v-for="(product, index) in products"
            :key="product.id"
            class="bg-slate-800/50 rounded-2xl overflow-hidden border border-slate-700/50 cursor-pointer transition-all hover:scale-[1.02] hover:shadow-xl stagger-item"
            :style="{ animationDelay: `${index * 0.08}s` }"
            @click="openRedeemModal(product)"
          >
            <div class="aspect-square relative">
              <img :src="product.image" class="w-full h-full object-cover" />
              <div class="absolute inset-0 bg-gradient-to-t from-slate-900 via-transparent to-transparent"></div>
              <div class="absolute top-2 left-2 px-2 py-0.5 rounded-full text-xs font-bold bg-emerald-500/80 text-white">
                实物
              </div>
              <div v-if="product.stock !== null && product.stock <= 5" class="absolute top-2 right-2 px-2 py-0.5 rounded-full text-xs font-bold bg-red-500/80 text-white">
                仅剩{{ product.stock }}件
              </div>
            </div>
            <div class="p-3">
              <h3 class="text-sm font-bold text-white truncate mb-1">{{ product.name }}</h3>
              <p v-if="product.description" class="text-xs text-slate-400 line-clamp-2 mb-2">{{ product.description }}</p>
              <div class="flex items-center gap-2 mb-2 text-xs">
                <span v-if="product.urFragmentCost > 0" class="px-2 py-0.5 rounded-full bg-red-500/20 text-red-400 font-bold">
                  {{ product.urFragmentCost }} UR碎片
                </span>
                <span v-if="product.pointsCost > 0" class="px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-400 font-bold">
                  {{ product.pointsCost }} 积分
                </span>
              </div>
              <button 
                class="w-full py-2 rounded-lg text-sm font-bold transition-all"
                :class="canAfford(product) 
                  ? 'bg-gradient-to-r from-emerald-500 to-teal-500 text-white' 
                  : 'bg-slate-700 text-slate-500 cursor-not-allowed'"
                :disabled="!canAfford(product)"
              >
                {{ canAfford(product) ? '立即兑换' : '资源不足' }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Orders Tab -->
      <div v-show="activeTab === 'orders'">
        <div v-if="loadingOrders" class="text-center text-slate-400 py-8">加载中...</div>
        <div v-else-if="orders.length === 0" class="text-center py-12">
          <span class="text-6xl mb-4 block">📋</span>
          <p class="text-slate-400">暂无兑换订单</p>
        </div>
        <div v-else class="space-y-4">
          <div
            v-for="(order, index) in orders"
            :key="order.id"
            class="bg-slate-800/50 rounded-2xl border border-slate-700/50 p-4 stagger-item"
            :style="{ animationDelay: `${index * 0.06}s` }"
          >
            <div class="flex items-start gap-4">
              <div class="w-20 h-20 rounded-xl overflow-hidden flex-shrink-0">
                <img :src="order.productImage || order.assetImage" class="w-full h-full object-cover" />
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between mb-2">
                  <h4 class="font-bold text-white truncate">{{ order.productName || order.assetName }}</h4>
                  <span class="px-2 py-0.5 rounded-full text-xs font-bold" :class="getStatusClass(order.status)">
                    {{ getStatusText(order.status) }}
                  </span>
                </div>
                <p class="text-xs text-slate-400 mb-1">订单号: {{ order.orderNo }}</p>
                <div class="flex gap-2 mb-1">
                  <span v-if="order.urFragmentCost > 0" class="text-xs text-red-400">消耗{{ order.urFragmentCost }}UR碎片</span>
                  <span v-if="order.pointsCost > 0" class="text-xs text-amber-400">消耗{{ order.pointsCost }}积分</span>
                </div>
                <p class="text-xs text-slate-400 mb-1">收货人: {{ order.receiver }} {{ order.phone }}</p>
                <p class="text-xs text-slate-400">地址: {{ order.province }}{{ order.city }}{{ order.district }}{{ order.address }}</p>
                <div v-if="order.status >= 1 && order.logisticsCompany" class="mt-2 p-2 bg-slate-900/50 rounded-lg">
                  <p class="text-xs text-slate-400">物流: {{ order.logisticsCompany }} - {{ order.logisticsNo }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Redeem Modal -->
    <Teleport to="body">
      <div v-if="showRedeemModal" class="fixed inset-0 z-[100] flex items-center justify-center p-4" @click="closeRedeemModal">
        <div class="absolute inset-0 bg-slate-900/90 backdrop-blur-sm"></div>
        <div class="relative z-10 w-full max-w-md bg-slate-800/90 backdrop-blur-xl rounded-3xl border border-slate-700/50 overflow-hidden" @click.stop>
          <div class="p-6">
            <div class="text-center mb-6">
              <span class="text-5xl mb-4 block">📦</span>
              <h3 class="text-xl font-black text-white mb-2">填写收货信息</h3>
              <p class="text-slate-400 text-sm">实物将邮寄到以下地址</p>
            </div>

            <div class="flex items-center gap-4 bg-slate-900/50 rounded-2xl p-4 mb-6">
              <div class="w-16 h-16 rounded-xl overflow-hidden">
                <img :src="selectedProduct?.image" class="w-full h-full object-cover" />
              </div>
              <div class="flex-1 min-w-0">
                <h4 class="font-bold text-white truncate">{{ selectedProduct?.name }}</h4>
                <div class="flex gap-2 mt-1">
                  <span v-if="selectedProduct?.urFragmentCost > 0" class="text-xs text-red-400 font-bold">{{ selectedProduct.urFragmentCost }} UR碎片</span>
                  <span v-if="selectedProduct?.pointsCost > 0" class="text-xs text-amber-400 font-bold">{{ selectedProduct.pointsCost }} 积分</span>
                </div>
              </div>
            </div>

            <div class="space-y-4 mb-6">
              <div>
                <label class="block text-sm text-slate-400 mb-2">收货人 *</label>
                <input v-model="redeemForm.receiver" type="text" class="w-full px-4 py-3 rounded-xl bg-slate-900/50 border border-slate-700 text-white placeholder-slate-500 focus:border-emerald-500 focus:outline-none transition-colors" placeholder="请输入收货人姓名" />
              </div>
              <div>
                <label class="block text-sm text-slate-400 mb-2">手机号码 *</label>
                <input v-model="redeemForm.phone" type="tel" class="w-full px-4 py-3 rounded-xl bg-slate-900/50 border border-slate-700 text-white placeholder-slate-500 focus:border-emerald-500 focus:outline-none transition-colors" placeholder="请输入手机号码" />
              </div>
              <div class="grid grid-cols-3 gap-2">
                <div>
                  <label class="block text-sm text-slate-400 mb-2">省</label>
                  <input v-model="redeemForm.province" type="text" class="w-full px-3 py-2 rounded-lg bg-slate-900/50 border border-slate-700 text-white placeholder-slate-500 focus:border-emerald-500 focus:outline-none transition-colors text-sm" placeholder="省" />
                </div>
                <div>
                  <label class="block text-sm text-slate-400 mb-2">市</label>
                  <input v-model="redeemForm.city" type="text" class="w-full px-3 py-2 rounded-lg bg-slate-900/50 border border-slate-700 text-white placeholder-slate-500 focus:border-emerald-500 focus:outline-none transition-colors text-sm" placeholder="市" />
                </div>
                <div>
                  <label class="block text-sm text-slate-400 mb-2">区</label>
                  <input v-model="redeemForm.district" type="text" class="w-full px-3 py-2 rounded-lg bg-slate-900/50 border border-slate-700 text-white placeholder-slate-500 focus:border-emerald-500 focus:outline-none transition-colors text-sm" placeholder="区" />
                </div>
              </div>
              <div>
                <label class="block text-sm text-slate-400 mb-2">详细地址 *</label>
                <textarea v-model="redeemForm.address" rows="2" class="w-full px-4 py-3 rounded-xl bg-slate-900/50 border border-slate-700 text-white placeholder-slate-500 focus:border-emerald-500 focus:outline-none transition-colors resize-none" placeholder="请输入详细地址（街道、门牌号等）"></textarea>
              </div>
            </div>

            <div class="flex gap-3">
              <button @click="closeRedeemModal" class="flex-1 py-3 rounded-xl font-bold bg-slate-700 text-white hover:bg-slate-600 transition-colors">
                取消
              </button>
              <button
                @click="handleConfirmRedeem"
                :disabled="!canSubmitRedeem || submitting"
                class="flex-1 py-3 rounded-xl font-bold bg-gradient-to-r from-emerald-500 to-teal-500 text-white shadow-lg hover:shadow-emerald-500/30 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {{ submitting ? '提交中...' : '确认兑换' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
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

interface RedeemProduct {
  id: string
  name: string
  image: string
  description: string
  urFragmentCost: number
  pointsCost: number
  stock: number
  exchangedCount: number
}

interface RedeemOrder {
  id: string
  orderNo: string
  assetId: string
  assetName: string
  assetImage: string
  productId: string
  productName: string
  productImage: string
  urFragmentCost: number
  pointsCost: number
  receiver: string
  phone: string
  province: string
  city: string
  district: string
  address: string
  status: number
  logisticsCompany: string
  logisticsNo: string
}

const activeTab = ref('products')
const loadingProducts = ref(false)
const loadingOrders = ref(false)
const products = ref<RedeemProduct[]>([])
const orders = ref<RedeemOrder[]>([])

const showRedeemModal = ref(false)
const selectedProduct = ref<RedeemProduct | null>(null)
const submitting = ref(false)

const redeemForm = ref({
  receiver: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  address: ''
})

const canSubmitRedeem = computed(() => {
  return redeemForm.value.receiver.trim() &&
         redeemForm.value.phone.trim() &&
         redeemForm.value.address.trim()
})

function canAfford(product: RedeemProduct): boolean {
  if (product.urFragmentCost > 0 && props.userFragment < product.urFragmentCost) {
    return false
  }
  if (product.pointsCost > 0 && props.userPoints < product.pointsCost) {
    return false
  }
  return true
}

async function fetchProducts() {
  loadingProducts.value = true
  try {
    const response = await $fetch<{ code: number; data: any[] }>('/api-proxy/redeem-product/list', {
      headers: getAuthHeaders()
    })
    if (response.code === 200) {
      products.value = (response.data || []).map((item: any) => ({
        id: String(item.id),
        name: item.name || '未知商品',
        image: item.image || 'https://picsum.photos/seed/default/200/200',
        description: item.description || '',
        urFragmentCost: item.urFragmentCost || 0,
        pointsCost: item.pointsCost || 0,
        stock: item.stock || 0,
        exchangedCount: item.exchangedCount || 0
      }))
    }
  } catch (e) {
    console.error('获取商品列表失败', e)
  } finally {
    loadingProducts.value = false
  }
}

async function fetchOrders() {
  loadingOrders.value = true
  try {
    const result = await $fetch<{ code: number; data: { records: any[] } }>('/api-proxy/redeem/my-orders?pageNum=1&pageSize=50', {
      headers: getAuthHeaders()
    })
    orders.value = (result.data.records || []).map((item: any) => ({
      id: String(item.id),
      orderNo: item.orderNo,
      assetId: String(item.assetId || ''),
      assetName: item.assetName || '',
      assetImage: item.assetImage || '',
      productId: String(item.productId || ''),
      productName: item.productName || '',
      productImage: item.productImage || '',
      urFragmentCost: item.urFragmentCost || 0,
      pointsCost: item.pointsCost || 0,
      receiver: item.receiver,
      phone: item.phone,
      province: item.province || '',
      city: item.city || '',
      district: item.district || '',
      address: item.address,
      status: item.status,
      logisticsCompany: item.logisticsCompany || '',
      logisticsNo: item.logisticsNo || ''
    }))
  } catch (e) {
    console.error('获取订单失败', e)
  } finally {
    loadingOrders.value = false
  }
}

function openRedeemModal(product: RedeemProduct) {
  if (!canAfford(product)) {
    alert('资源不足，无法兑换')
    return
  }
  selectedProduct.value = product
  redeemForm.value = {
    receiver: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    address: ''
  }
  showRedeemModal.value = true
}

function closeRedeemModal() {
  showRedeemModal.value = false
  selectedProduct.value = null
}

async function handleConfirmRedeem() {
  if (!selectedProduct.value || !canSubmitRedeem.value || submitting.value) return
  submitting.value = true
  try {
    // 保持productId为字符串，避免雪花ID精度丢失
    await $fetch('/api-proxy/redeem-product/redeem', {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({
        productId: selectedProduct.value.id,
        receiver: redeemForm.value.receiver,
        phone: redeemForm.value.phone,
        province: redeemForm.value.province || '',
        city: redeemForm.value.city || '',
        district: redeemForm.value.district || '',
        address: redeemForm.value.address
      })
    })
    alert('兑换成功！请等待发货')
    closeRedeemModal()
    fetchProducts()
    fetchOrders()
    emit('update-points')
  } catch (e: any) {
    alert(e.data?.msg || e.message || '兑换失败')
  } finally {
    submitting.value = false
  }
}

function getStatusClass(status: number): string {
  const classes: Record<number, string> = {
    0: 'bg-amber-500/20 text-amber-400',
    1: 'bg-blue-500/20 text-blue-400',
    2: 'bg-emerald-500/20 text-emerald-400'
  }
  return classes[status] || 'bg-slate-500/20 text-slate-400'
}

function getStatusText(status: number): string {
  const texts: Record<number, string> = {
    0: '待发货',
    1: '已发货',
    2: '已完成'
  }
  return texts[status] || '未知'
}

onMounted(() => {
  fetchProducts()
  fetchOrders()
})
</script>
