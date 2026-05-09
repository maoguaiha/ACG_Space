<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="font-bold">交易监控中心</span>
          <div class="header-tips">
            <el-tag type="warning" effect="dark">待处理订单需要及时处理</el-tag>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="16" class="stats-row">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-inner">
              <div class="stat-icon-wrap bg-blue">
                <el-icon :size="28"><Clock /></el-icon>
              </div>
              <div class="stat-info">
                <p class="stat-label">待发货</p>
                <p class="stat-value text-blue-500">{{ stats.pending }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-inner">
              <div class="stat-icon-wrap bg-amber">
                <el-icon :size="28"><Van /></el-icon>
              </div>
              <div class="stat-info">
                <p class="stat-label">已发货</p>
                <p class="stat-value text-amber-500">{{ stats.shipped }}</p>
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
                <p class="stat-label">已完成</p>
                <p class="stat-value text-emerald-500">{{ stats.completed }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-inner">
              <div class="stat-icon-wrap bg-violet">
                <el-icon :size="28"><Box /></el-icon>
              </div>
              <div class="stat-info">
                <p class="stat-label">本月兑换</p>
                <p class="stat-value">{{ stats.monthlyRedeem }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 筛选 -->
      <div class="header-actions">
        <el-form :inline="true" :model="queryParams">
          <el-form-item label="订单号">
            <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable />
          </el-form-item>
          <el-form-item label="订单状态">
            <el-select v-model="queryParams.status" placeholder="请选择" clearable>
              <el-option label="待发货" :value="0" />
              <el-option label="已发货" :value="1" />
              <el-option label="已完成" :value="2" />
              <el-option label="已取消" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <el-table :data="orderList" v-loading="loading" border style="width: 100%">
        <el-table-column label="订单号" width="220" align="center">
          <template #default="scope">
            <span class="font-mono text-sm">{{ scope.row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用户ID" width="120" align="center">
          <template #default="scope">
            <span class="text-sm">{{ scope.row.userId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="兑换商品" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            <div class="flex items-center gap-2">
              <el-image
                style="width: 40px; height: 40px; border-radius: 4px;"
                :src="scope.row.productImage || scope.row.itemImage"
                fit="cover"
              />
              <div>
                <div class="font-bold text-sm">{{ scope.row.productName || scope.row.itemName }}</div>
                <el-tag v-if="scope.row.itemRarity" :type="getRarityType(scope.row.itemRarity)" size="small">{{ scope.row.itemRarity }}</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="收货信息" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <div class="text-sm">
              <p class="font-medium">{{ scope.row.receiver }} {{ scope.row.phone }}</p>
              <p class="text-gray-500">{{ scope.row.province }}{{ scope.row.city }}{{ scope.row.district }}{{ scope.row.address }}</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="物流信息" min-width="150" align="center">
          <template #default="scope">
            <div v-if="scope.row.logisticsCompany">
              <p class="text-sm font-medium">{{ scope.row.logisticsCompany }}</p>
              <p class="text-xs text-gray-500 font-mono">{{ scope.row.logisticsNo }}</p>
            </div>
            <span v-else class="text-gray-400 text-sm">-</span>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="160" align="center">
          <template #default="scope">
            <span class="text-gray-500 text-sm">{{ scope.row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 0"
              link
              type="primary"
              :icon="Van"
              @click="handleShip(scope.row)"
            >发货</el-button>
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

    <!-- 发货对话框 -->
    <el-dialog
      v-model="shipDialogVisible"
      title="填写物流信息"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="shipForm" :rules="shipRules" ref="shipFormRef" label-width="100px">
        <el-form-item label="收货人">
          <el-input v-model="shipForm.receiver" disabled />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="shipForm.phone" disabled />
        </el-form-item>
        <el-form-item label="收货地址">
          <el-input v-model="shipForm.address" type="textarea" disabled :rows="2" />
        </el-form-item>
        <el-form-item label="快递公司" prop="logisticsCompany">
          <el-select v-model="shipForm.logisticsCompany" placeholder="请选择快递公司" style="width: 100%">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通快递" value="圆通快递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="申通快递" value="申通快递" />
            <el-option label="京东物流" value="京东物流" />
            <el-option label="邮政EMS" value="邮政EMS" />
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号" prop="logisticsNo">
          <el-input v-model="shipForm.logisticsNo" placeholder="请输入快递单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitShip">确认发货</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="订单详情"
      width="600px"
    >
      <div v-if="currentOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentOrder.status)">{{ getStatusLabel(currentOrder.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentOrder.userId }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentOrder.phone }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ currentOrder.receiver }}</el-descriptions-item>
          <el-descriptions-item label="兑换商品">
            <div class="flex items-center gap-2">
              <el-image :src="currentOrder.productImage || currentOrder.itemImage" style="width: 40px; height: 40px; border-radius: 4px;" />
              <span>{{ currentOrder.productName || currentOrder.itemName }}</span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.province }}{{ currentOrder.city }}{{ currentOrder.district }}{{ currentOrder.address }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ currentOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ currentOrder.shipTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ currentOrder.completeTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="物流公司" :span="2">
            {{ currentOrder.logisticsCompany || '-' }}
            <span v-if="currentOrder.logisticsNo" class="ml-2 font-mono">{{ currentOrder.logisticsNo }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="currentOrder?.status === 0"
          type="primary"
          @click="handleShip(currentOrder)"
        >去发货</el-button>
        <el-button
          v-if="currentOrder?.status === 1"
          type="success"
          @click="handleComplete"
        >确认收货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, View, Van, Clock, SuccessFilled, Box } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { deliveryApi, type DeliveryOrder } from '@/api/delivery'

const loading = ref(false)
const total = ref(0)
const orderList = ref<DeliveryOrder[]>([])
const shipDialogVisible = ref(false)
const detailVisible = ref(false)
const currentOrder = ref<DeliveryOrder | null>(null)
const shipFormRef = ref<FormInstance>()

const stats = ref({
  pending: 0,
  shipped: 0,
  completed: 0,
  monthlyRedeem: 0
})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  orderNo: '',
  status: undefined as number | undefined
})

const shipForm = reactive({
  orderId: 0,
  receiver: '',
  phone: '',
  address: '',
  logisticsCompany: '',
  logisticsNo: ''
})

const shipRules: FormRules = {
  logisticsCompany: [{ required: true, message: '请选择快递公司', trigger: 'change' }],
  logisticsNo: [{ required: true, message: '请输入快递单号', trigger: 'blur' }]
}

async function getList() {
  loading.value = true
  try {
    const res = await deliveryApi.page({
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
      status: queryParams.status ?? undefined
    })
    orderList.value = res.data.data.records
    total.value = res.data.data.total
  } catch (error) {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

async function getStats() {
  try {
    const res = await deliveryApi.getStats()
    stats.value = res.data.data
  } catch (error) {
    ElMessage.error('获取统计数据失败')
  }
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.orderNo = ''
  queryParams.status = undefined
  handleQuery()
}

function handleShip(row: DeliveryOrder) {
  currentOrder.value = row
  shipForm.orderId = row.id || 0
  shipForm.receiver = row.receiver || ''
  shipForm.phone = row.phone || ''
  shipForm.address = `${row.province || ''}${row.city || ''}${row.district || ''}${row.address || ''}`
  shipForm.logisticsCompany = ''
  shipForm.logisticsNo = ''
  shipDialogVisible.value = true
}

function handleDetail(row: DeliveryOrder) {
  currentOrder.value = row
  detailVisible.value = true
}

async function submitShip() {
  shipFormRef.value?.validate(async (valid) => {
    if (valid) {
      try {
        await deliveryApi.ship({
          orderId: shipForm.orderId,
          logisticsCompany: shipForm.logisticsCompany,
          logisticsNo: shipForm.logisticsNo
        })
        ElMessage.success('发货成功')
        shipDialogVisible.value = false
        getList()
        getStats()
      } catch (error) {
        ElMessage.error('发货失败')
      }
    }
  })
}

async function handleComplete() {
  if (!currentOrder.value?.id) return
  try {
    await deliveryApi.complete(currentOrder.value.id)
    ElMessage.success('已确认收货')
    detailVisible.value = false
    getList()
    getStats()
  } catch (error) {
    ElMessage.error('确认收货失败')
  }
}

function getStatusLabel(status: number | undefined): string {
  const labels: Record<number, string> = {
    0: '待发货',
    1: '已发货',
    2: '已完成',
    3: '已取消'
  }
  return labels[status ?? -1] || String(status)
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

onMounted(() => {
  getStats()
  getList()
})
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

.bg-amber {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.bg-emerald {
  background: linear-gradient(135deg, #10b981, #059669);
}

.bg-violet {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
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
