<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="font-bold">兑换商品管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增商品</el-button>
        </div>
      </template>

      <!-- 搜索与筛选 -->
      <div class="header-actions">
        <el-form :inline="true" :model="queryParams">
          <el-form-item label="商品名称">
            <el-input v-model="queryParams.name" placeholder="请输入名称" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="请选择" clearable>
              <el-option label="上架" :value="1" />
              <el-option label="下架" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <el-table :data="productList" v-loading="loading" border style="width: 100%">
        <el-table-column label="商品图片" width="100" align="center">
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
        <el-table-column prop="name" label="商品名称" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            <div class="font-bold">{{ scope.row.name }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="兑换条件" width="200" align="center">
          <template #default="scope">
            <div v-if="scope.row.urFragmentCost > 0" class="text-red-500 font-bold text-sm">
              {{ scope.row.urFragmentCost }} UR碎片
            </div>
            <div v-if="scope.row.pointsCost > 0" class="text-amber-500 font-bold text-sm">
              {{ scope.row.pointsCost }} 积分
            </div>
            <div v-if="scope.row.urFragmentCost === 0 && scope.row.pointsCost === 0" class="text-gray-400 text-sm">
              未设置
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" align="center" />
        <el-table-column prop="exchangedCount" label="已兑换" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" :icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-popconfirm
              title="确定要删除这个商品吗？"
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
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品图片" prop="image">
          <SquareImageUploader v-model="form.image" />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
        </el-form-item>
        <el-form-item label="UR碎片数量">
          <el-input-number v-model="form.urFragmentCost" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="积分数量">
          <el-input-number v-model="form.pointsCost" :min="0" :max="99999" />
        </el-form-item>
        <el-form-item label="库存数量" prop="stock">
          <el-input-number v-model="form.stock" :min="0" :max="99999" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus, Search, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import { listProducts, createProduct, updateProduct, deleteProduct } from '@/api/redeem-product'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import SquareImageUploader from '@/components/SquareImageUploader.vue'

const loading = ref(false)
const submitting = ref(false)
const productList = ref<any[]>([])
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  status: undefined as number | undefined
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEditMode = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  id: undefined as number | undefined,
  name: '',
  image: '',
  description: '',
  urFragmentCost: 0,
  pointsCost: 0,
  stock: 0,
  sortOrder: 0,
  status: 1
})

function resetForm() {
  form.id = undefined
  form.name = ''
  form.image = ''
  form.description = ''
  form.urFragmentCost = 0
  form.pointsCost = 0
  form.stock = 0
  form.sortOrder = 0
  form.status = 1
}

const rules: FormRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存数量', trigger: 'blur' }]
}

async function fetchList() {
  loading.value = true
  try {
    console.log('=== 兑换商品列表查询开始 ===')
    console.log('查询参数:', JSON.stringify(queryParams))
    const res = await listProducts(queryParams)
    console.log('API响应:', res)
    console.log('res.data:', res.data)
    console.log('res.data.data:', res.data?.data)
    console.log('记录数:', res.data?.data?.records?.length || 0)
    console.log('总数:', res.data?.data?.total || 0)
    productList.value = res.data?.data?.records || []
    total.value = res.data?.data?.total || 0
    console.log('productList:', productList.value)
    console.log('=== 兑换商品列表查询结束 ===')
  } catch (e: any) {
    console.error('获取商品列表失败:', e)
    console.error('错误详情:', e.response?.data)
    ElMessage.error('获取商品列表失败: ' + (e.response?.data?.msg || e.message))
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  fetchList()
}

function resetQuery() {
  queryParams.name = ''
  queryParams.status = undefined
  queryParams.pageNum = 1
  fetchList()
}

function handleAdd() {
  dialogTitle.value = '新增商品'
  isEditMode.value = false
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: any) {
  dialogTitle.value = '编辑商品'
  isEditMode.value = true
  resetForm()
  form.id = row.id
  form.name = row.name
  form.image = row.image || ''
  form.description = row.description || ''
  form.urFragmentCost = row.urFragmentCost || 0
  form.pointsCost = row.pointsCost || 0
  form.stock = row.stock || 0
  form.sortOrder = row.sortOrder || 0
  form.status = row.status
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEditMode.value) {
        await updateProduct(form)
        ElMessage.success('修改成功')
      } else {
        await createProduct(form)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      queryParams.pageNum = 1
      await fetchList()
    } catch (e: any) {
      ElMessage.error(e.response?.data?.msg || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

async function handleDelete(id: number) {
  console.log('=== 删除商品开始 ===')
  console.log('删除ID:', id)
  try {
    const res = await deleteProduct(id)
    console.log('删除响应:', res)
    console.log('删除响应data:', res.data)
    ElMessage.success('删除成功')
    console.log('开始刷新列表...')
    await fetchList()
    console.log('列表刷新完成')
    console.log('=== 删除商品结束 ===')
  } catch (e: any) {
    console.error('删除失败:', e)
    console.error('错误详情:', e.response?.data)
    ElMessage.error('删除失败: ' + (e.response?.data?.msg || e.message))
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-actions {
  margin-bottom: 16px;
}
.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
