<template>
  <div class="p-4">
    <h2>待审核文章</h2>
    <el-table :data="list" style="width: 100%">
      <el-table-column prop="title" label="标题"></el-table-column>
      <el-table-column prop="authorNickname" label="作者"></el-table-column>
      <el-table-column label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 3 ? 'warning' : 'danger'">
            {{ row.status === 1 ? '已发布' : row.status === 3 ? '待审核' : row.status === 4 ? '已驳回' : '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button type="success" size="small" @click="approve(row)">通过</el-button>
          <el-button type="danger" size="small" @click="reject(row)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '../../api/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref<any[]>([])

async function fetchList() {
  try {
    const res = await request.get('/article/admin/reviewList')
    list.value = res.data?.data?.records || []
  } catch (e) {
    ElMessage.error('获取待审核列表失败')
  }
}

async function approve(row: any) {
  try {
    await request.put('/article/admin/review', { id: row.id, approve: true })
    ElMessage.success('已通过')
    fetchList()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function reject(row: any) {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回文章', {
      confirmButtonText: '提交',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：内容不符合社区规范、涉嫌抄袭、图片无法加载...'
    })
    if (value === undefined || value === null) return
    await request.put('/article/admin/review', { id: row.id, approve: false, rejectReason: value })
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
