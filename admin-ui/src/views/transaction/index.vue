<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="font-bold">交易监控中心</span>
          <div class="header-tips">
            <el-tag type="danger" effect="dark">异常单据需要人工干预</el-tag>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="16" class="stats-row">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-inner">
              <div class="stat-icon-wrap bg-blue">
                <el-icon :size="28"><Wallet /></el-icon>
              </div>
              <div class="stat-info">
                <p class="stat-label">今日交易额</p>
                <p class="stat-value">¥ {{ (todayStats.amount || 0).toLocaleString() }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-inner">
              <div class="stat-icon-wrap bg-emerald">
                <el-icon :size="28"><SuccessFilled /></el-icon>
              </div>
              <div class="stat-info">
                <p class="stat-label">成功交易</p>
                <p class="stat-value text-emerald-500">{{ todayStats.successCount }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-inner">
              <div class="stat-icon-wrap bg-amber">
                <el-icon :size="28"><WarningFilled /></el-icon>
              </div>
              <div class="stat-info">
                <p class="stat-label">待处理</p>
                <p class="stat-value text-amber-500">{{ todayStats.pendingCount }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-inner">
              <div class="stat-icon-wrap bg-rose">
                <el-icon :size="28"><CircleCloseFilled /></el-icon>
              </div>
              <div class="stat-info">
                <p class="stat-label">异常单据</p>
                <p class="stat-value text-rose-500">{{ todayStats.errorCount }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 筛选 -->
      <div class="header-actions">
        <el-form :inline="true" :model="queryParams">
          <el-form-item label="订单号">
            <el-input v-model="queryParams.orderId" placeholder="请输入订单号" clearable />
          </el-form-item>
          <el-form-item label="交易状态">
            <el-select v-model="queryParams.status" placeholder="请选择" clearable>
              <el-option label="成功" value="success" />
              <el-option label="处理中" value="pending" />
              <el-option label="失败" value="failed" />
              <el-option label="回查中" value="checking" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="queryParams.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <el-table :data="transactionList" v-loading="loading" border style="width: 100%">
        <el-table-column label="订单号" width="200" align="center">
          <template #default="scope">
            <span class="font-mono text-sm">{{ scope.row.orderId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="买家" width="120" align="center">
          <template #default="scope">
            <div class="flex items-center gap-2">
              <el-avatar :size="24" :src="scope.row.buyerAvatar" />
              <span class="text-sm">{{ scope.row.buyerName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="卖家" width="120" align="center">
          <template #default="scope">
            <div class="flex items-center gap-2">
              <el-avatar :size="24" :src="scope.row.sellerAvatar" />
              <span class="text-sm">{{ scope.row.sellerName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="商品" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            <div class="flex items-center gap-2">
              <el-image
                style="width: 40px; height: 40px; border-radius: 4px;"
                :src="scope.row.itemImage"
                fit="cover"
              />
              <div>
                <div class="font-bold text-sm">{{ scope.row.itemName }}</div>
                <el-tag :type="getRarityType(scope.row.itemRarity)" size="small">{{ scope.row.itemRarity }}</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="交易金额" width="120" align="center">
          <template #default="scope">
            <span class="text-amber-500 font-bold">{{ scope.row.amount }}积分</span>
          </template>
        </el-table-column>
        <el-table-column label="手续费(1%)" width="100" align="center">
          <template #default="scope">
            <span class="text-rose-500">-{{ scope.row.fee }}积分</span>
          </template>
        </el-table-column>
        <el-table-column label="卖家实得" width="100" align="center">
          <template #default="scope">
            <span class="text-emerald-500 font-bold">{{ scope.row.sellerAmount }}积分</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160" align="center">
          <template #default="scope">
            <span class="text-gray-500 text-sm">{{ scope.row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 'failed' || scope.row.status === 'checking'"
              link
              type="primary"
              :icon="View"
              @click="handleDetail(scope.row)"
            >处理</el-button>
            <el-button
              link
              type="info"
              :icon="View"
              @click="handleDetail(scope.row)"
            >详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </el-card>

    <!-- 交易详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="交易详情"
      width="600px"
    >
      <div v-if="currentTransaction" class="transaction-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentTransaction.orderId }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentTransaction.status)">{{ getStatusLabel(currentTransaction.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="买家">{{ currentTransaction.buyerName }}</el-descriptions-item>
          <el-descriptions-item label="卖家">{{ currentTransaction.sellerName }}</el-descriptions-item>
          <el-descriptions-item label="商品">{{ currentTransaction.itemName }}</el-descriptions-item>
          <el-descriptions-item label="商品图片">
            <el-image :src="currentTransaction.itemImage" style="width: 60px; height: 60px; border-radius: 4px;" />
          </el-descriptions-item>
          <el-descriptions-item label="交易金额">{{ currentTransaction.amount }}积分</el-descriptions-item>
          <el-descriptions-item label="手续费">{{ currentTransaction.fee }}积分</el-descriptions-item>
          <el-descriptions-item label="卖家实得">{{ currentTransaction.sellerAmount }}积分</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentTransaction.createTime }}</el-descriptions-item>
          <el-descriptions-item label="完成时间" :span="2">{{ currentTransaction.completeTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="事务ID" :span="2">
            <span class="font-mono text-xs">{{ currentTransaction.transactionId || '-' }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="currentTransaction.status === 'failed'" class="mt-4 p-4 bg-rose-50 rounded-lg">
          <p class="text-rose-500 font-bold mb-2">失败原因：</p>
          <p class="text-rose-600">{{ currentTransaction.errorMsg || '未知错误，请联系技术人员' }}</p>
        </div>

        <div v-if="currentTransaction.status === 'checking'" class="mt-4 p-4 bg-amber-50 rounded-lg">
          <p class="text-amber-500 font-bold mb-2">状态说明：</p>
          <p class="text-amber-600">RocketMQ 事务消息正在等待回查，可能存在数据不一致问题。</p>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="currentTransaction?.status === 'failed'"
          type="danger"
          @click="handleCompensate"
        >人工补偿</el-button>
        <el-button
          v-if="currentTransaction?.status === 'checking'"
          type="primary"
          @click="handleRetry"
        >重试处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, View, Wallet, SuccessFilled, WarningFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import { transactionApi, type Transaction, type TransactionStats } from '@/api'

const loading = ref(false)
const total = ref(0)
const transactionList = ref<Transaction[]>([])
const detailVisible = ref(false)
const currentTransaction = ref<Transaction | null>(null)

const todayStats = reactive<TransactionStats>({
  todayAmount: 0,
  todayCount: 0,
  pendingCount: 0,
  successRate: 0
})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  orderId: '',
  status: null as number | null,
  dateRange: [] as string[]
})

async function getList() {
  loading.value = true
  try {
    const res = await transactionApi.page({
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
      orderId: queryParams.orderId || undefined,
      status: queryParams.status ?? undefined
    })
    transactionList.value = res.data.data.records
    total.value = res.data.data.total
  } catch (error) {
    ElMessage.error('获取交易列表失败')
  } finally {
    loading.value = false
  }
}

async function fetchStats() {
  try {
    const res = await transactionApi.getStats()
    Object.assign(todayStats, res.data.data)
  } catch (error) {
    // Stats fetch failed, use defaults
  }
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.orderId = ''
  queryParams.status = null
  queryParams.dateRange = []
  handleQuery()
}

function handleDetail(row: Transaction) {
  currentTransaction.value = row
  detailVisible.value = true
}

async function handleCompensate() {
  if (!currentTransaction.value?.orderId) return
  try {
    await ElMessageBox.confirm(
      '人工补偿将直接完成交易并转移资产，是否继续？',
      '确认补偿',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' }
    )
    await transactionApi.compensate(currentTransaction.value.orderId)
    ElMessage.success('补偿处理成功')
    detailVisible.value = false
    getList()
    fetchStats()
  } catch {
    // User cancelled or API error
  }
}

async function handleRetry() {
  if (!currentTransaction.value?.orderId) return
  try {
    await transactionApi.retry(currentTransaction.value.orderId)
    ElMessage.success('正在重新处理...')
    setTimeout(() => {
      ElMessage.success('处理成功')
      detailVisible.value = false
      getList()
    }, 1500)
  } catch {
    ElMessage.error('重试失败')
  }
}

function getStatusLabel(status: number | string | undefined): string {
  if (status === undefined || status === null) return '-'
  if (typeof status === 'number') {
    const labels: Record<number, string> = {
      0: '处理中',
      1: '成功',
      2: '失败',
      3: '回查中'
    }
    return labels[status] || String(status)
  }
  // string statuses
  const map: Record<string, string> = {
    success: '成功',
    pending: '处理中',
    failed: '失败',
    checking: '回查中'
  }
  return map[status] || String(status)
}

function getStatusType(status: number | string | undefined): string {
  if (status === undefined || status === null) return 'info'
  if (typeof status === 'number') {
    const types: Record<number, string> = {
      0: 'warning',
      1: 'success',
      2: 'danger',
      3: 'info'
    }
    return types[status] || 'info'
  }
  const map: Record<string, string> = {
    success: 'success',
    pending: 'warning',
    failed: 'danger',
    checking: 'info'
  }
  return map[status] || 'info'
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

getList()
fetchStats()
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-tips {
  display: flex;
  gap: 8px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-inner {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.bg-blue {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.bg-emerald {
  background: linear-gradient(135deg, #10b981, #059669);
}

.bg-amber {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.bg-rose {
  background: linear-gradient(135deg, #f43f5e, #e11d48);
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
}

.header-actions {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>