<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="font-bold">抽赏配置</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">创建奖池</el-button>
        </div>
      </template>

      <!-- 筛选 -->
      <div class="header-actions">
        <el-form :inline="true" :model="queryParams">
          <el-form-item label="奖池名称">
            <el-input v-model="queryParams.name" placeholder="请输入名称" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="请选择" clearable>
              <el-option label="进行中" :value="1" />
              <el-option label="已结束" :value="2" />
              <el-option label="未开始" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 奖池列表 -->
      <div class="gacha-pool-grid">
        <div
          v-for="pool in poolList"
          :key="pool.id"
          class="gacha-pool-card"
          :class="{ 'is-active': getDisplayStatus(pool) === 1 }"
        >
          <div class="pool-banner">
            <img :src="pool.banner" class="banner-img" />
            <div class="banner-overlay">
              <span class="pool-badge" :class="getStatusClass(getDisplayStatus(pool))">
                {{ getStatusLabel(getDisplayStatus(pool)) }}
              </span>
            </div>
          </div>
          <div class="pool-content">
            <div class="pool-header">
              <h3 class="pool-name">{{ pool.name }}</h3>
              <el-tag :type="pool.rarity === 'SSR' ? 'warning' : 'success'" size="small">
                {{ pool.rarity }}限定
              </el-tag>
            </div>
            <p class="pool-desc">{{ pool.description }}</p>

            <div class="pool-stats">
              <div class="stat-item">
                <span class="stat-label">总库存</span>
                <span class="stat-value">{{ (pool.totalStock ?? 0).toLocaleString() }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">剩余</span>
                <span class="stat-value" :class="getStockClass(pool.remainingStock ?? 0, pool.totalStock ?? 0)">
                  {{ (pool.remainingStock ?? 0).toLocaleString() }}
                </span>
              </div>
            </div>

            <div class="pool-progress">
              <el-progress
                :percentage="getPercentage(pool.remainingStock ?? 0, pool.totalStock ?? 0)"
                :color="getProgressColor(pool.remainingStock ?? 0, pool.totalStock ?? 0)"
                :show-text="false"
              />
            </div>

            <div class="pool-pricing">
              <div class="price-item">
                <span class="price-label">单抽</span>
                <span class="price-value">{{ pool.singleCost }}积分</span>
              </div>
              <div class="price-item">
                <span class="price-label">十连</span>
                <span class="price-value text-amber-500">{{ pool.tenCost }}积分</span>
              </div>
            </div>

            <div class="pool-time">
              <el-icon><Clock /></el-icon>
              <span>{{ pool.startTime }} ~ {{ pool.endTime || '不限' }}</span>
            </div>

            <div class="pool-actions">
              <el-button size="small" :icon="Edit" @click="handleEdit(pool)">编辑</el-button>
              <el-button size="small" :icon="Setting" @click="handleConfig(pool)">配置</el-button>
              <el-popconfirm
                v-if="pool.status === 1"
                title="确定要结束这个奖池吗？"
                @confirm="handleEnd(pool.id)">
              >
                <template #reference>
                  <el-button size="small" type="danger" :icon="Close">结束</el-button>
                </template>
              </el-popconfirm>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[8, 16, 24, 32]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑奖池' : '创建奖池'"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="奖池名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入奖池名称" />
        </el-form-item>
        <el-form-item label="奖池描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入奖池描述" />
        </el-form-item>
        <el-form-item label="banner图" prop="banner">
          <el-input v-model="form.banner" placeholder="请输入Banner图片URL" />
        </el-form-item>
        <el-form-item label="稀有度" prop="rarity">
          <el-select v-model="form.rarity" placeholder="请选择">
            <el-option label="SSR限定" value="SSR" />
            <el-option label="SR限定" value="SR" />
            <el-option label="普通" value="normal" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="总库存" prop="totalStock">
              <el-input-number v-model="form.totalStock" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单抽价格" prop="singleCost">
              <el-input-number v-model="form.singleCost" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="十连价格" prop="tenCost">
              <el-input-number v-model="form.tenCost" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="保底">
              <el-input-number v-model="form.guaranteeCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="选择结束时间（可选）"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search, Refresh, Edit, Setting, Close, Clock } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { gachaApi, type GachaPool } from '@/api'
import { useRouter } from 'vue-router'

const router = useRouter()

const loading = ref(false)
const total = ref(0)
const poolList = ref<GachaPool[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const queryParams = reactive({
  pageNum: 1,
  pageSize: 8,
  name: '',
  status: null as number | null
})

const form = reactive({
  id: undefined as number | undefined,
  name: '',
  description: '',
  banner: '',
  rarity: 'SSR',
  totalStock: 10000,
  singleCost: 280,
  tenCost: 2600,
  guaranteeCount: 10,
  guaranteeType: 'rarity',
  startTime: '',
  endTime: ''
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入奖池名称', trigger: 'blur' }],
  totalStock: [{ required: true, message: '请输入总库存', trigger: 'blur' }],
  singleCost: [{ required: true, message: '请输入单抽价格', trigger: 'blur' }],
  tenCost: [{ required: true, message: '请输入十连价格', trigger: 'blur' }]
}

async function getList() {
  loading.value = true
  try {
    const res = await gachaApi.page({
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
      name: queryParams.name || undefined,
      status: queryParams.status ?? undefined
    })
    poolList.value = res.data.data.records
    total.value = Number(res.data.data.total) || 0
  } catch (error) {
    ElMessage.error('获取奖池列表失败')
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  if (queryParams.pageNum === 1) {
    getList()
  } else {
    queryParams.pageNum = 1
  }
}

watch(() => queryParams.pageNum, () => getList())
watch(() => queryParams.pageSize, () => {
  if (queryParams.pageNum === 1) {
    getList()
  } else {
    queryParams.pageNum = 1
  }
})

function resetQuery() {
  queryParams.name = ''
  queryParams.status = null
  handleQuery()
}

function handleAdd() {
  isEdit.value = false
  Object.assign(form, {
    id: undefined,
    name: '',
    description: '',
    banner: '',
    rarity: 'SSR',
    totalStock: 10000,
    singleCost: 280,
    tenCost: 2600,
    guaranteeCount: 10,
    guaranteeType: 'rarity',
    startTime: '',
    endTime: ''
  })
  dialogVisible.value = true
}

function handleEdit(pool: GachaPool) {
  isEdit.value = true
  Object.assign(form, pool)
  dialogVisible.value = true
}

function handleConfig(pool: GachaPool) {
  // 跳转到奖池配置页面（奖品配置）
  if (!pool.id) return
  router.push(`/gacha/config/${pool.id}`)
}

async function handleEnd(id?: number) {
  if (!id) return
  try {
    await gachaApi.endPool(id)
    ElMessage.success('奖池已结束')
    getList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

async function submitForm() {
  try {
    await formRef.value?.validate()
    if (isEdit.value && form.id) {
      await gachaApi.update(form)
      ElMessage.success('编辑成功')
    } else {
      await gachaApi.create(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) {
    ElMessage.error(isEdit.value ? '编辑失败' : '创建失败')
  }
}

function getDisplayStatus(pool: GachaPool): number {
  // 如果已经结束，直接返回结束状态
    if (pool.status === 2) return 2
  
  const now = new Date().getTime()
  const startTime = pool.startTime ? new Date(pool.startTime).getTime() : 0
  const endTime = pool.endTime ? new Date(pool.endTime).getTime() : Infinity
  
  // 根据时间判断实际状态
  if (now < startTime) return 0 // 未开始
  if (now > endTime) return 2 // 已结束
  return 1 // 进行中
}

function getStatusLabel(status: number): string {
  const labels: Record<number, string> = {
    0: '未开始',
    1: '进行中',
    2: '已结束'
  }
  return labels[status] || String(status)
}

function getStatusClass(status: number): string {
  const classes: Record<number, string> = {
    0: 'badge-pending',
    1: 'badge-active',
    2: 'badge-ended'
  }
  return classes[status] || ''
}

function getPercentage(remaining: number, total: number): number {
  if (total === 0) return 0
    return Math.round(((total - remaining) / total) * 100)
}

function getProgressColor(remaining: number, total: number): string {
  const ratio = remaining / total
  if (ratio < 0.1) return '#ef4444'
  if (ratio < 0.3) return '#f59e0b'
  return '#22c55e'
}

function getStockClass(remaining: number, total: number): string {
  const ratio = remaining / total
  if (ratio < 0.1) return 'text-rose-500'
  if (ratio < 0.3) return 'text-amber-500'
  return 'text-emerald-500'
}

getList()
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  margin-bottom: 20px;
}

.gacha-pool-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.gacha-pool-card {
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  transition: all 0.3s;
}

.gacha-pool-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.gacha-pool-card.is-active {
  border-color: #409eff;
}

.pool-banner {
  position: relative;
  height: 120px;
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.6), transparent);
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  padding: 8px;
}

.pool-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.badge-active {
  background: #22c55e;
  color: white;
}

.badge-ended {
  background: #6b7280;
  color: white;
}

.badge-pending {
  background: #f59e0b;
  color: white;
}

.pool-content {
  padding: 16px;
}

.pool-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.pool-name {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
}

.pool-desc {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 12px;
}

.pool-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-label {
  font-size: 12px;
  color: #9ca3af;
}

.stat-value {
  font-size: 14px;
  font-weight: 600;
}

.pool-progress {
  margin-bottom: 12px;
}

.pool-pricing {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.price-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.price-label {
  font-size: 12px;
  color: #9ca3af;
}

.price-value {
  font-size: 14px;
  font-weight: 600;
}

.pool-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 12px;
}

.pool-actions {
  display: flex;
  gap: 8px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>