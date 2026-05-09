<template>
  <div class="min-h-screen" :class="['theme-bg']">
    <div class="container mx-auto px-4 py-8 max-w-4xl">
      <!-- 返回按钮 -->
      <div class="mb-6">
        <NuxtLink to="/orders" class="inline-flex items-center gap-2 text-sm transition-colors" :class="['theme-back-link']">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m15 18-6-6 6-6"/></svg>
          返回订单列表
        </NuxtLink>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="flex flex-col items-center justify-center py-20">
        <div class="w-12 h-12 rounded-full border-4 border-indigo-500/30 border-t-indigo-500 animate-spin mb-4"></div>
        <p class="theme-text-muted">加载中…</p>
      </div>

      <!-- 订单详情 -->
      <div v-else-if="order" class="space-y-6">
        <!-- 订单状态卡片 -->
        <div class="rounded-xl border p-6" :class="['theme-card']">
          <div class="flex items-center justify-between mb-4">
            <h1 class="text-2xl font-bold theme-text-main">订单详情</h1>
            <el-tag :type="getStatusType(order.status)" size="large">{{ getStatusLabel(order.status) }}</el-tag>
          </div>

          <div class="grid grid-cols-2 gap-4 text-sm">
            <div>
              <p class="theme-text-muted">订单号</p>
              <p class="font-mono font-medium theme-text-main">{{ order.orderNo }}</p>
            </div>
            <div>
              <p class="theme-text-muted">下单时间</p>
              <p class="font-medium theme-text-main">{{ formatTime(order.createTime) }}</p>
            </div>
          </div>
        </div>

        <!-- 商品信息 -->
        <div class="rounded-xl border p-6" :class="['theme-card']">
          <h2 class="text-lg font-bold mb-4 theme-text-main">商品信息</h2>
          <div class="flex items-center gap-4">
            <el-image
              :src="order.productImage || order.itemImage"
              fit="cover"
              class="w-24 h-24 rounded-lg flex-shrink-0"
            />
            <div class="flex-1">
              <p class="font-bold text-lg theme-text-main">{{ order.productName || order.itemName }}</p>
              <p v-if="order.itemRarity" class="text-sm mt-2">
                <el-tag :type="getRarityType(order.itemRarity)" size="small">{{ order.itemRarity }}</el-tag>
              </p>
              <div class="grid grid-cols-2 gap-2 mt-3 text-sm">
                <div>
                  <span class="theme-text-muted">UR碎片消耗：</span>
                  <span class="font-medium theme-text-main">{{ order.urFragmentCost || 0 }}</span>
                </div>
                <div>
                  <span class="theme-text-muted">积分消耗：</span>
                  <span class="font-medium theme-text-main">{{ order.pointsCost || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 收货信息 -->
        <div class="rounded-xl border p-6" :class="['theme-card']">
          <h2 class="text-lg font-bold mb-4 theme-text-main">收货信息</h2>
          <div class="space-y-2 text-sm">
            <div class="flex">
              <span class="theme-text-muted w-24">收货人：</span>
              <span class="font-medium theme-text-main">{{ order.receiver }}</span>
            </div>
            <div class="flex">
              <span class="theme-text-muted w-24">联系电话：</span>
              <span class="font-medium theme-text-main">{{ order.phone }}</span>
            </div>
            <div class="flex">
              <span class="theme-text-muted w-24">收货地址：</span>
              <span class="font-medium theme-text-main">
                {{ order.province }}{{ order.city }}{{ order.district }}{{ order.address }}
              </span>
            </div>
          </div>
        </div>

        <!-- 物流信息 -->
        <div v-if="order.logisticsCompany" class="rounded-xl border p-6" :class="['theme-card']">
          <h2 class="text-lg font-bold mb-4 theme-text-main">物流信息</h2>
          <div class="space-y-3">
            <div class="flex items-center gap-3 p-4 rounded-lg" :class="['theme-bg-muted']">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="theme-text-main">
                <rect x="1" y="3" width="15" height="13"/>
                <polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/>
                <circle cx="5.5" cy="18.5" r="2.5"/>
                <circle cx="18.5" cy="18.5" r="2.5"/>
              </svg>
              <div class="flex-1">
                <p class="font-bold theme-text-main">{{ order.logisticsCompany }}</p>
                <p class="font-mono text-sm theme-text-muted">{{ order.logisticsNo }}</p>
              </div>
            </div>
            <div v-if="order.shipTime" class="text-sm">
              <span class="theme-text-muted">发货时间：</span>
              <span class="font-medium theme-text-main">{{ formatTime(order.shipTime) }}</span>
            </div>
            <div v-if="order.completeTime" class="text-sm">
              <span class="theme-text-muted">完成时间：</span>
              <span class="font-medium theme-text-main">{{ formatTime(order.completeTime) }}</span>
            </div>
          </div>
        </div>

        <!-- 订单状态时间线 -->
        <div class="rounded-xl border p-6" :class="['theme-card']">
          <h2 class="text-lg font-bold mb-4 theme-text-main">订单进度</h2>
          <div class="space-y-4">
            <div class="flex gap-4">
              <div class="flex flex-col items-center">
                <div class="w-3 h-3 rounded-full" :class="order.status >= 0 ? 'bg-green-500' : 'theme-bg-muted'"></div>
                <div class="w-0.5 h-12" :class="order.status >= 0 ? 'bg-green-500' : 'theme-bg-muted'"></div>
              </div>
              <div>
                <p class="font-medium theme-text-main">订单已提交</p>
                <p class="text-sm theme-text-muted">{{ formatTime(order.createTime) }}</p>
              </div>
            </div>
            <div class="flex gap-4">
              <div class="flex flex-col items-center">
                <div class="w-3 h-3 rounded-full" :class="order.status >= 1 ? 'bg-green-500' : 'theme-bg-muted'"></div>
                <div class="w-0.5 h-12" :class="order.status >= 2 ? 'bg-green-500' : 'theme-bg-muted'"></div>
              </div>
              <div>
                <p class="font-medium" :class="order.status >= 1 ? 'theme-text-main' : 'theme-text-muted'">
                  {{ order.status >= 1 ? '已发货' : '待发货' }}
                </p>
                <p v-if="order.shipTime" class="text-sm theme-text-muted">{{ formatTime(order.shipTime) }}</p>
              </div>
            </div>
            <div class="flex gap-4">
              <div class="flex flex-col items-center">
                <div class="w-3 h-3 rounded-full" :class="order.status >= 2 ? 'bg-green-500' : 'theme-bg-muted'"></div>
              </div>
              <div>
                <p class="font-medium" :class="order.status >= 2 ? 'theme-text-main' : 'theme-text-muted'">
                  {{ order.status >= 2 ? '已完成' : '待完成' }}
                </p>
                <p v-if="order.completeTime" class="text-sm theme-text-muted">{{ formatTime(order.completeTime) }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="flex flex-col items-center justify-center py-20">
        <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="mb-4 theme-text-muted">
          <circle cx="12" cy="12" r="10"/>
          <line x1="12" y1="8" x2="12" y2="12"/>
          <line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        <p class="text-lg theme-text-muted">订单不存在</p>
        <NuxtLink to="/orders" class="mt-4 px-6 py-2 text-sm rounded-lg transition-all" :class="['theme-btn-primary']">
          返回订单列表
        </NuxtLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '~/stores/user'

interface Order {
  id: number
  orderNo: string
  userId: number
  assetId: number
  itemId: number
  itemName: string
  itemImage: string
  itemRarity: string
  productId: number
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
  shipTime: string
  completeTime: string
  createTime: string
  updateTime: string
}

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const order = ref<Order | null>(null)

async function fetchOrderDetail() {
  const orderId = route.params.id
  if (!orderId) return

  loading.value = true
  try {
    const token = userStore.token
    const res = await $fetch(`/api-proxy/redeem/order/id/${orderId}`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })

    if (res.code === 200 && res.data) {
      order.value = res.data as Order
    }
  } catch (error: any) {
    console.error('获取订单详情失败:', error)
  } finally {
    loading.value = false
  }
}

function getStatusLabel(status: number | undefined): string {
  const labels: Record<number, string> = {
    0: '待发货',
    1: '已发货',
    2: '已完成',
    3: '已取消'
  }
  return labels[status ?? -1] || '未知'
}

function getStatusType(status: number | undefined): string {
  const types: Record<number, string> = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'info'
  }
  return types[status ?? -1] || 'info'
}

function getRarityType(rarity: string): string {
  const types: Record<string, string> = {
    SSR: 'warning',
    SR: 'purple',
    R: '',
    N: 'info'
  }
  return types[rarity] || ''
}

function formatTime(time: string | undefined): string {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

definePageMeta({
  middleware: 'auth'
})

onMounted(() => {
  fetchOrderDetail()
})
</script>
