<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="font-bold">商品图鉴管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增物品</el-button>
        </div>
      </template>

      <!-- 搜索与筛选 -->
      <div class="header-actions">
        <el-form :inline="true" :model="queryParams">
          <el-form-item label="物品名称">
            <el-input v-model="queryParams.name" placeholder="请输入名称" clearable />
          </el-form-item>
          <el-form-item label="物品标识">
            <el-input v-model="queryParams.itemKey" placeholder="请输入唯一标识" clearable />
          </el-form-item>
          <el-form-item label="稀有度">
            <el-select v-model="queryParams.rarity" placeholder="请选择" clearable>
              <el-option label="SSR" value="SSR" />
              <el-option label="SR" value="SR" />
              <el-option label="R" value="R" />
              <el-option label="N" value="N" />
            </el-select>
          </el-form-item>
          <el-form-item label="物品类型">
            <el-select v-model="queryParams.type" placeholder="请选择" clearable>
              <el-option label="角色" value="character" />
              <el-option label="武器" value="weapon" />
              <el-option label="服装" value="skin" />
              <el-option label="材料" value="material" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <el-table :data="itemList" v-loading="loading" border style="width: 100%">
        <el-table-column label="物品图片" width="100" align="center">
          <template #default="scope">
            <el-image
              style="width: 60px; height: 60px; border-radius: 8px;"
              :src="scope.row.image"
              fit="cover"
              :preview-src-list="[scope.row.image]"
              preview-teleported
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="物品名称" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            <div class="font-bold">{{ scope.row.name }}</div>
            <div class="text-gray-400 text-xs">{{ scope.row.itemKey }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="itemKey" label="物品标识" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.itemKey)">{{ getTypeLabel(scope.row.itemKey) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.type)">{{ getTypeLabel(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rarity" label="稀有度" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getRarityTagType(scope.row.rarity)" effect="dark">{{ scope.row.rarity }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="totalStock" label="总库存" width="100" align="center" />
        <el-table-column prop="remainingStock" label="剩余库存" width="100" align="center">
          <template #default="scope">
            <span :class="getStockClass(scope.row.remainingStock, scope.row.totalStock)">
              {{ scope.row.remainingStock }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="参考价格" width="100" align="center">
          <template #default="scope">
            <span class="text-amber-500 font-bold">{{ scope.row.price }}积分</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" :icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-popconfirm
              title="确定要删除这个物品吗？"
              @confirm="handleDelete(scope.row.id)"
            >
              <template #reference>
                <el-button link type="danger" :icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
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
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑物品' : '新增物品'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="物品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入物品名称" />
        </el-form-item>
        <el-form-item label="物品标识" prop="itemKey">
          <el-input v-model="form.itemKey" placeholder="请输入唯一标识，如 item_star_001" />
        </el-form-item>
        <el-form-item label="物品类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型">
            <el-option label="角色" value="character" />
            <el-option label="武器" value="weapon" />
            <el-option label="服装" value="skin" />
            <el-option label="材料" value="material" />
          </el-select>
        </el-form-item>
        <el-form-item label="稀有度" prop="rarity">
          <el-select v-model="form.rarity" placeholder="请选择稀有度">
            <el-option label="SSR" value="SSR" />
            <el-option label="SR" value="SR" />
            <el-option label="R" value="R" />
            <el-option label="N" value="N" />
          </el-select>
        </el-form-item>
        <el-form-item label="物品图片" prop="image">
          <ImagePickerDialog v-model="form.image" :aspect-ratio="1" />
        </el-form-item>
        <el-form-item label="总库存" prop="totalStock">
          <el-input-number v-model="form.totalStock" :min="0" />
        </el-form-item>
        <el-form-item label="参考价格" prop="price">
          <el-input-number v-model="form.price" :min="0" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入物品描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { itemApi, type Item } from '@/api'
import ImagePickerDialog from '@/components/ImagePickerDialog.vue'

const loading = ref(false)
const total = ref(0)
const itemList = ref<Item[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  rarity: '',
  type: '',
  itemKey:''
})

const form = reactive({
  id: undefined as number | undefined,
  name: '',
  itemKey: '',
  type: '',
  rarity: '',
  image: '',
  description: '',
  totalStock: 0,
  price: 0
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入物品名称', trigger: 'blur' }],
  itemKey: [{ required: true, message: '请输入物品标识', trigger: 'blur' }],
  type: [{ required: true, message: '请选择物品类型', trigger: 'change' }],
  rarity: [{ required: true, message: '请选择稀有度', trigger: 'change' }]
}

async function getList() {
  loading.value = true
  try {
    const res = await itemApi.page({
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
      name: queryParams.name || undefined,
      rarity: queryParams.rarity || undefined,
      type: queryParams.type || undefined,
      itemKey: queryParams.itemKey || undefined
    })
    itemList.value = res.data.data.records
    total.value = Number(res.data.data.total) || 0
  } catch (error) {
    ElMessage.error('获取物品列表失败')
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

function resetQuery() {
  queryParams.name = ''
  queryParams.rarity = ''
  queryParams.type = ''
  queryParams.itemKey = ''
  handleQuery()
}

function handleAdd() {
  isEdit.value = false
  Object.assign(form, {
    id: undefined,
    name: '',
    itemKey: '',
    type: '',
    rarity: '',
    image: '',
    description: '',
    totalStock: 0,
    price: 0
  })
  dialogVisible.value = true
}

function handleEdit(row: Item) {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

async function handleDelete(id: number) {
  try {
    await itemApi.delete(id)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

async function submitForm() {
  try {
    await formRef.value?.validate()
    if (isEdit.value && form.id) {
      await itemApi.update(form)
      ElMessage.success('编辑成功')
    } else {
      await itemApi.create(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) {
    ElMessage.error(isEdit.value ? '编辑失败' : '新增失败')
  }
}

function getTypeLabel(type: string): string {
  const labels: Record<string, string> = {
    character: '角色',
    weapon: '武器',
    skin: '服装',
    material: '材料'
  }
  return labels[type] || type
}

function getTypeTagType(type: string): string {
  const types: Record<string, string> = {
    character: 'primary',
    weapon: 'warning',
    skin: 'success',
    material: 'info'
  }
  return types[type] || 'info'
}

function getRarityTagType(rarity: string): string {
  const types: Record<string, string> = {
    SSR: 'warning',
    SR: 'danger',
    R: 'info',
    N: 'info'
  }
  return types[rarity] || 'info'
}

function getStockClass(remaining: number, total: number): string {
  const ratio = remaining / total
  if (ratio < 0.1) return 'text-rose-500 font-bold'
  if (ratio < 0.3) return 'text-amber-500 font-bold'
  return 'text-emerald-500'
}

getList()

// 分页变化监听（替代废弃的 @size-change / @current-change）
watch(() => queryParams.pageNum, () => getList())
watch(() => queryParams.pageSize, () => {
  if (queryParams.pageNum === 1) {
    getList()
  } else {
    queryParams.pageNum = 1  // pageNum watcher 会自动触发 getList
  }
})
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>