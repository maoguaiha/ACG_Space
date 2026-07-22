<template>
  <div class="min-h-screen" :class="['theme-bg']">
    <div class="container mx-auto px-4 py-8 max-w-5xl">
      <!-- 返回按钮 -->
      <div class="mb-6">
        <NuxtLink to="/community" class="inline-flex items-center gap-2 text-sm transition-colors" :class="['theme-back-link']">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m15 18-6-6 6-6"/></svg>
          返回社区
        </NuxtLink>
      </div>

      <!-- 标题 -->
      <h1 class="text-2xl font-bold mb-6 theme-text-main">我的订单</h1>

      <!-- 状态筛选 -->
      <div class="flex gap-3 mb-6">
        <button
          v-for="tab in statusTabs"
          :key="tab.value"
          @click="handleStatusChange(tab.value)"
          class="px-4 py-2 text-sm rounded-xl transition-all"
          :class="queryParams.status === tab.value ? 'theme-btn-primary' : 'theme-btn-secondary'"
        >
          {{ tab.label }}
        </button>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="flex flex-col items-center justify-center py-20">
        <div class="w-12 h-12 rounded-full border-4 border-indigo-500/30 border-t-indigo-500 animate-spin mb-4"></div>
        <p class="theme-text-muted">加载中…</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="orderList.length === 0" class="flex flex-col items-center justify-center py-20">
        <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="mb-4 theme-text-muted">
          <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z"/>
          <line x1="3" y1="6" x2="21" y2="6"/>
          <path d="M16 10a4 4 0 01-8 0"/>
        </svg>
        <p class="text-lg theme-text-muted">暂无订单记录</p>
      </div>

      <!-- 订单列表 -->
      <div v-else class="space-y-4">
        <div
          v-for="order in orderList"
          :key="order.id"
          class="rounded-xl border p-5 transition-all hover:shadow-lg"
          :class="['theme-card']"
        >
          <div class="flex items-start justify-between mb-4">
            <div>
              <p class="text-sm theme-text-muted">订单号</p>
              <p class="font-mono text-sm theme-text-main">{{ order.orderNo }}</p>
            </div>
            <el-tag :type="getStatusType(order.status)" size="small">{{ getStatusLabel(order.status) }}</el-tag>
          </div>

          <div class="flex items-center gap-4 mb-4">
            <el-image
              :src="order.productImage || order.itemImage"
              fit="cover"
              class="w-20 h-20 rounded-lg flex-shrink-0"
            />
            <div class="flex-1 min-w-0">
              <p class="font-bold theme-text-main">{{ order.productName || order.itemName }}</p>
              <p v-if="order.itemRarity" class="text-sm mt-1">
                <el-tag :type="getRarityType(order.itemRarity)" size="small">{{ order.itemRarity }}</el-tag>
              </p>
              <p class="text-sm mt-2 theme-text-muted">
                收货人：{{ order.receiver }} {{ order.phone }}
              </p>
              <p class="text-sm theme-text-muted">
                地址：{{ order.province }}{{ order.city }}{{ order.district }}{{ order.address }}
              </p>
            </div>
          </div>

          <!-- 物流信息 -->
          <div v-if="order.logisticsCompany" class="mt-4 p-3 rounded-lg" :class="['theme-bg-muted']">
            <p class="text-sm font-medium theme-text-main">
              {{ order.logisticsCompany }} - {{ order.logisticsNo }}
            </p>
            <p v-if="order.shipTime" class="text-xs mt-1 theme-text-muted">
              发货时间：{{ formatTime(order.shipTime) }}
            </p>
          </div>

          <div class="flex items-center justify-between mt-4 pt-4 border-t" :class="['theme-border']">
            <p class="text-xs theme-text-muted">
              申请时间：{{ formatTime(order.createTime) }}
            </p>
            <NuxtLink
              :to="`/orders/${order.id}`"
              class="px-4 py-2 text-sm rounded-lg transition-all"
              :class="['theme-btn-primary']"
            >
              查看详情
            </NuxtLink>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > queryParams.pageSize" class="flex justify-center mt-8">
        <div class="flex items-center gap-2">
          <button
            @click="handlePageChange(queryParams.pageNum - 1)"
            :disabled="queryParams.pageNum <= 1"
            class="px-3 py-2 text-sm rounded-lg transition-all disabled:opacity-50"
            :class="['theme-btn-secondary']"
          >
            上一页
          </button>
          <span class="text-sm theme-text-muted">
            第 {{ queryParams.pageNum }} / {{ Math.ceil(total / queryParams.pageSize) }} 页
          </span>
          <button
            @click="handlePageChange(queryParams.pageNum + 1)"
            :disabled="queryParams.pageNum >= Math.ceil(total / queryParams.pageSize)"
            class="px-3 py-2 text-sm rounded-lg transition-all disabled:opacity-50"
            :class="['theme-btn-secondary']"
          >
            下一页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
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

const userStore = useUserStore()
const loading = ref(false)
const total = ref(0)
const orderList = ref<Order[]>([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  status: undefined as number | undefined
})

const statusTabs = [
  { label: '全部', value: undefined },
  { label: '待发货', value: 0 },
  { label: '已发货', value: 1 },
  { label: '已完成', value: 2 },
  { label: '已取消', value: 3 }
]

async function fetchOrders() {
  loading.value = true
  try {
    const token = userStore.token
    const params = new URLSearchParams({
      pageNum: String(queryParams.pageNum),
      pageSize: String(queryParams.pageSize)
    })
    if (queryParams.status !== undefined) {
      params.append('status', String(queryParams.status))
    }

    const res = await $fetch(`/api-proxy/redeem/my-orders?${params}`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })

    if (res.code === 200 && res.data) {
      orderList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error: any) {
    console.error('获取订单列表失败:', error)
  } finally {
    loading.value = false
  }
}

function handleStatusChange(status: number | undefined) {
  queryParams.status = status
  queryParams.pageNum = 1
  fetchOrders()
}

function handlePageChange(page: number) {
  if (page < 1 || page > Math.ceil(total.value / queryParams.pageSize)) return
  queryParams.pageNum = page
  fetchOrders()
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
    SR: 'danger',
    R: 'primary',
    N: 'info'
  }
  return types[rarity] || 'info'
}

function formatTime(time: string | undefined): string {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

definePageMeta({
  middleware: 'auth'
})

onMounted(() => {
  fetchOrders()
})
</script>
