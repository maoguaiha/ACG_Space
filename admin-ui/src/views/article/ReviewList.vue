<template>
  <div class="p-4">
    <h2>待审核文章（占位）</h2>
    <el-table :data="list" style="width: 100%">
      <el-table-column prop="title" label="标题"></el-table-column>
      <el-table-column prop="author" label="作者"></el-table-column>
      <el-table-column prop="status" label="状态"></el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button type="primary" size="small">通过</el-button>
          <el-button type="danger" size="small">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref<any[]>([])

async function fetchList() {
  try {
    const res = await axios.get('/api/article/admin/reviewList')
    list.value = res.data?.data?.records || []
  } catch (e) {
    ElMessage.error('获取待审核列表失败')
  }
}

async function approve(row: any) {
  try {
    await axios.put('/api/article/admin/review', { id: row.id, approve: true })
    ElMessage.success('已通过')
    fetchList()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function reject(row: any) {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回', {
      confirmButtonText: '提交',
      cancelButtonText: '取消'
    })
    await axios.put('/api/article/admin/review', { id: row.id, approve: false, rejectReason: value })
    ElMessage.success('已驳回')
    fetchList()
  } catch (e) {
    // canceled or error
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>

</style>
