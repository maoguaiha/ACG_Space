<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <span>评论审核管理</span>
      </template>

      <el-form :inline="true" :model="queryForm" class="mb-4">
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="请输入评论内容关键词" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="用户" width="150">
          <template #default="{ row }">
            <el-avatar :size="32" :src="row.avatar" />
            <span class="ml-2">{{ row.nickname || row.username }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="animeId" label="番剧ID" width="100" />
        <el-table-column prop="content" label="评论内容" show-overflow-tooltip />
        <el-table-column prop="parentId" label="父评论ID" width="100">
          <template #default="{ row }">
            {{ row.parentId === 0 ? '-' : row.parentId }}
          </template>
        </el-table-column>
        <el-table-column prop="likes" label="点赞数" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="{ row }">
            <el-button type="info" size="small" @click="handleDetail(row)">查看详情</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
      />
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="评论详情" width="600px">
      <el-descriptions :column="2" border v-if="currentComment">
        <el-descriptions-item label="评论ID">{{ currentComment.id }}</el-descriptions-item>
        <el-descriptions-item label="番剧ID">{{ currentComment.animeId }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentComment.userId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentComment.username }}</el-descriptions-item>
        <el-descriptions-item label="用户昵称">{{ currentComment.nickname }}</el-descriptions-item>
        <el-descriptions-item label="点赞数">{{ currentComment.likes }}</el-descriptions-item>
        <el-descriptions-item label="父评论ID">{{ currentComment.parentId === 0 ? '无' : currentComment.parentId }}</el-descriptions-item>
        <el-descriptions-item label="回复目标用户">{{ currentComment.replyToNickname || '无' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentComment.createTime }}</el-descriptions-item>
        <el-descriptions-item label="评论内容" :span="2">
          <div class="comment-content">{{ currentComment.content }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button v-if="currentComment" type="danger" @click="handleDelete(currentComment?.id)">删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import request from '../../api/request'
import { ElMessage, ElMessageBox } from 'element-plus'

interface CommentItem {
  id: number
  animeId: number
  userId: number
  content: string
  parentId: number
  likes: number
  createTime: string
  username: string
  nickname: string
  avatar: string
  replyToUserId?: number
  replyToNickname?: string
}

const queryForm = ref({
  pageNum: 1,
  pageSize: 10,
  keyword: ''
})

const tableData = ref<CommentItem[]>([])
const total = ref(0)
const detailDialogVisible = ref(false)
const currentComment = ref<CommentItem | null>(null)

const fetchData = async () => {
  try {
    const { data } = await request.get('/comment/admin/page', {
      params: queryForm.value
    })
    if (data.code === 200) {
      tableData.value = data.data.records
      total.value = Number(data.data.total) || 0
    }
  } catch (error) {
    console.error('获取评论数据失败', error)
    ElMessage.error('获取评论数据失败')
  }
}

const handleSearch = () => {
  if (queryForm.value.pageNum === 1) {
    fetchData()
  } else {
    queryForm.value.pageNum = 1
  }
}

watch(() => queryForm.value.pageNum, () => fetchData())
watch(() => queryForm.value.pageSize, () => {
  if (queryForm.value.pageNum === 1) {
    fetchData()
  } else {
    queryForm.value.pageNum = 1
  }
})

const handleReset = () => {
  queryForm.value.keyword = ''
  handleSearch()
}

const handleDetail = (row: CommentItem) => {
  currentComment.value = row
  detailDialogVisible.value = true
}

const handleDelete = async (id?: number) => {
  if (!id) return
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const { data } = await request.delete(`/comment/${id}`)
    if (data.code === 200) {
      ElMessage.success('删除成功')
      detailDialogVisible.value = false
      handleSearch()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败', error)
      ElMessage.error('删除评论失败')
    }
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.app-container {
  padding: 10px;
}
.mb-4 {
  margin-bottom: 16px;
}
.ml-2 {
  margin-left: 8px;
}
.comment-content {
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
