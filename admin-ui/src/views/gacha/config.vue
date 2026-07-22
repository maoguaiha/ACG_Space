<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="font-bold">奖池配置 - {{ poolName }}</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">添加奖品</el-button>
        </div>
      </template>

      <!-- 奖品列表 -->
      <div class="prize-list">
        <div
          v-for="prize in prizeList"
          :key="prize.id"
          class="prize-item"
        >
          <div class="prize-image">
            <img :src="prize.itemImage" />
          </div>
          <div class="prize-info">
            <div class="prize-header">
              <span class="prize-name">{{ prize.itemName }}</span>
              <el-tag :type="getRarityType(prize.rarity)">{{ prize.rarity }}</el-tag>
            </div>
            <div class="prize-stats">
              <span>数量: {{ prize.quantity }}</span>
              <span>权重: {{ prize.weight }}</span>
              <span>概率: {{ getProbability(prize.weight) }}%</span>
            </div>
            <div class="prize-actions">
              <el-button size="small" :icon="Edit" @click="handleEdit(prize)">编辑</el-button>
              <el-button size="small" type="danger" :icon="Delete" @click="handleDelete(prize.id)">删除</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 新增/编辑对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑奖品' : '添加奖品'"
        width="500px"
        :close-on-click-modal="false"
      >
        <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
          <el-form-item label="物品名称" prop="itemName">
            <el-input v-model="form.itemName" placeholder="请输入物品名称" />
          </el-form-item>
          <el-form-item label="物品图片" prop="itemImage">
            <ImagePickerDialog v-model="form.itemImage" aspect-ratio="1" />
          </el-form-item>
          <el-form-item label="稀有度" prop="rarity">
            <el-select v-model="form.rarity" placeholder="请选择">
              <el-option label="SSR" value="SSR" />
              <el-option label="SR" value="SR" />
              <el-option label="R" value="R" />
              <el-option label="N" value="N" />
            </el-select>
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="数量" prop="quantity">
                <el-input-number v-model="form.quantity" :min="1" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="权重" prop="weight">
                <el-input-number v-model="form.weight" :min="1" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { gachaApi, type GachaPrize } from '@/api'
import ImagePickerDialog from '@/components/ImagePickerDialog.vue'

const route = useRoute()
const router = useRouter()

const poolId = Number(route.params.poolId)
const poolName = ref('')
const prizeList = ref<GachaPrize[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  id: undefined as number | undefined,
  itemName: '',
  itemImage: '',
  rarity: 'SSR',
  quantity: 1,
  weight: 100
})

const rules: FormRules = {
  itemName: [{ required: true, message: '请输入物品名称', trigger: 'blur' }],
  itemImage: [{ required: true, message: '请选择物品图片', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  weight: [{ required: true, message: '请输入权重', trigger: 'blur' }]
}

const totalWeight = computed(() => {
  return prizeList.value.reduce((sum, prize) => sum + prize.weight, 0)
})

async function getPrizeList() {
  try {
    console.log('获取奖品列表, poolId:', poolId)
    const res = await gachaApi.getPrizes(poolId)
    console.log('奖品列表响应:', res)
    prizeList.value = res.data.data || []
    // 获取奖池信息
    const poolRes = await gachaApi.getPool(poolId)
    console.log('奖池信息响应:', poolRes)
    if (poolRes.data && poolRes.data.data) {
      poolName.value = poolRes.data.data.name || '未知奖池'
    } else {
      poolName.value = '未知奖池'
    }
  } catch (error: any) {
    console.error('获取奖品列表失败:', error)
    console.error('错误详情:', error.response?.data || error.message)
    ElMessage.error('获取奖品列表失败: ' + (error.message || '未知错误'))
  }
}

function handleAdd() {
  isEdit.value = false
  Object.assign(form, {
    id: undefined,
    itemName: '',
    itemImage: '',
    rarity: 'SSR',
    quantity: 1,
    weight: 100
  })
  dialogVisible.value = true
}

function handleEdit(prize: GachaPrize) {
  isEdit.value = true
  Object.assign(form, prize)
  dialogVisible.value = true
}

async function handleDelete(id?: number) {
  if (!id) return
  try {
    await gachaApi.deletePrize(id)
    ElMessage.success('删除成功')
    getPrizeList()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

async function submitForm() {
  try {
    console.log('提交表单, isEdit:', isEdit.value, 'form:', form)
    await formRef.value?.validate()
    console.log('表单验证通过')
    if (isEdit.value && form.id) {
      const res = await gachaApi.updatePrize(form)
      console.log('编辑奖品响应:', res)
      ElMessage.success('编辑成功')
    } else {
      const res = await gachaApi.createPrize(poolId, form)
      console.log('添加奖品响应:', res)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    getPrizeList()
  } catch (error: any) {
    console.error('提交表单失败:', error)
    console.error('错误详情:', error.response?.data || error.message)
    ElMessage.error(isEdit.value ? '编辑失败' : '添加失败')
  }
}

function getRarityType(rarity: string): string {
  const types: Record<string, string> = {
    SSR: 'warning',
    SR: 'success',
    R: 'info',
    N: 'info'
  }
  return types[rarity] || 'info'
}

function getProbability(weight: number): string {
  if (totalWeight.value === 0) return '0.00'
  return ((weight / totalWeight.value) * 100).toFixed(2)
}

onMounted(() => {
  getPrizeList()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.prize-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.prize-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.prize-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.prize-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.prize-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.prize-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.prize-name {
  font-size: 16px;
  font-weight: 600;
}

.prize-stats {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #6b7280;
}

.prize-actions {
  display: flex;
  gap: 8px;
}
</style>