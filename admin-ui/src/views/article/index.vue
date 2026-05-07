<template>
  <div class="app-container">
    <el-card shadow="never">
      <div class="header-actions">
        <el-form :inline="true" :model="queryParams">
          <el-form-item label="文章标题">
            <el-input v-model="queryParams.title" placeholder="请输入标题" clearable />
          </el-form-item>
          <el-form-item label="文章分类">
            <el-select v-model="queryParams.category" placeholder="请选择分类" clearable style="width: 150px;">
              <el-option label="业界资讯" value="业界资讯" />
              <el-option label="深度解析" value="深度解析" />
              <el-option label="新番导视" value="新番导视" />
              <el-option label="周边评测" value="周边评测" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 150px;" @change="handleQuery">
              <el-option label="全部" :value="undefined" />
              <el-option label="草稿" :value="0" />
              <el-option label="已发布" :value="1" />
              <el-option label="下架" :value="2" />
              <el-option label="待审核" :value="3" />
              <el-option label="已驳回" :value="4" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <div class="right-actions">
          <el-button type="success" icon="Plus" @click="handleCreate">新增文章</el-button>
        </div>
      </div>

      <el-table :data="articleList" v-loading="loading" border style="width: 100%" class="mt-20">
        <el-table-column label="序号" width="60" align="center">
          <template #default="scope">
            {{ (queryParams.pageNum - 1) * queryParams.pageSize + (scope.$index + 1) }}
          </template>
        </el-table-column>
        <el-table-column label="推荐" width="80" align="center">
          <template #default="scope">
            <el-button
              link
              :type="scope.row.isFeatured === 1 ? 'warning' : 'info'"
              @click="handleToggleFeatured(scope.row)"
              :title="scope.row.isFeatured === 1 ? '取消推荐' : '设为推荐'"
              style="font-size: 20px;"
            >
              {{ scope.row.isFeatured === 1 ? '★' : '☆' }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="封面" width="100" align="center">
          <template #default="scope">
            <el-image
              v-if="scope.row.coverUrl"
              style="width: 60px; height: 80px; border-radius: 4px;"
              :src="scope.row.coverUrl"
              fit="cover"
              :preview-src-list="[scope.row.coverUrl]"
              preview-teleported
            />
            <span v-else class="text-gray-400 text-xs">无封面</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="文章标题" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <div class="font-bold">{{ scope.row.title }}</div>
            <div class="text-gray-400 text-xs mt-1">{{ scope.row.summary }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100" align="center" />
        <el-table-column prop="authorNickname" label="作者" width="100" align="center" />
        <el-table-column prop="viewCount" label="浏览量" width="80" align="center" />
        <el-table-column prop="likeCount" label="点赞数" width="80" align="center" />
        <el-table-column prop="commentCount" label="评论数" width="80" align="center" />
        <el-table-column label="状态" width="200" align="center">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)">
              {{ statusLabel(scope.row.status) }}
            </el-tag>
            <div v-if="scope.row.status === 3" class="mt-2">
              <el-button type="success" size="small" @click="handleApprove(scope.row)">通过</el-button>
              <el-button type="danger" size="small" @click="handleReject(scope.row)">驳回</el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="VIP" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isVipOnly === 1 ? 'warning' : 'info'" size="small">
              {{ scope.row.isVipOnly === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-popconfirm
              title="确定要删除这篇文章吗？"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleDelete(scope.row.id)"
            >
              <template #reference>
                <el-button link type="danger" icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container mt-20">
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

    <el-dialog v-model="editDialogVisible" :title="editForm.id ? '编辑文章' : '新增文章'" width="700px">
      <el-form :model="editForm" label-width="100px" ref="editFormRef" :rules="editRules">
        <el-form-item label="文章标题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="文章摘要" prop="summary">
          <el-input v-model="editForm.summary" type="textarea" :rows="2" placeholder="请输入文章摘要" />
        </el-form-item>
        <el-form-item label="封面图片" prop="coverUrl">
          <ImagePickerDialog v-model="editForm.coverUrl" aspect-ratio="16/9" />
        </el-form-item>
        <el-form-item label="文章分类" prop="category">
          <el-select v-model="editForm.category" placeholder="请选择分类" style="width: 100%;">
            <el-option label="业界资讯" value="业界资讯" />
            <el-option label="深度解析" value="深度解析" />
            <el-option label="新番导视" value="新番导视" />
            <el-option label="周边评测" value="周边评测" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <AdminTagSelector v-model="editForm.tags" />
        </el-form-item>
        <el-form-item label="文章内容" prop="content">
          <AdminRichEditor v-model="editForm.content" placeholder="请输入文章内容" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-select v-model="editForm.status" placeholder="请选择状态" style="width: 100%;">
                <el-option label="草稿" :value="0" />
                <el-option label="发布" :value="1" />
                <el-option label="下架" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="VIP专享" prop="isVipOnly">
              <el-switch v-model="editForm.isVipOnly" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="推荐" prop="isFeatured">
              <el-switch v-model="editForm.isFeatured" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit" :loading="editing">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import ImagePickerDialog from '@/components/ImagePickerDialog.vue'
import AdminTagSelector from '@/components/AdminTagSelector.vue'
import AdminRichEditor from '@/components/AdminRichEditor.vue'

const statusLabel = (status: number): string => {
  const map: Record<number, string> = { 0: '草稿', 1: '已发布', 2: '下架', 3: '待审核', 4: '已驳回' }
  return map[status] ?? '未知'
}

const statusTagType = (status: number): string => {
  const map: Record<number, string> = {
    0: 'info',
    1: 'success',
    2: 'warning',
    3: 'danger',
    4: 'danger'
  }
  return map[status] ?? 'info'
}

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  title: '',
  category: '',
  status: undefined as number | undefined
})

const loading = ref(false)
const total = ref(0)
const articleList = ref<any[]>([])

const getList = async () => {
  loading.value = true
  try {
    const params: any = {
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    }
    if (queryParams.title?.trim()) params.keyword = queryParams.title.trim()
    if (queryParams.category) params.category = queryParams.category
    if (queryParams.status !== undefined) params.status = queryParams.status

    const res = await axios.get('http://localhost:8080/api/article/list', { params })
    if (res.data && res.data.code === 200) {
      const pageData = res.data.data || {}
      articleList.value = pageData.records || []
      total.value = Number(pageData.total) || 0
      if (pageData.current) queryParams.pageNum = Number(pageData.current)
      if (pageData.size) queryParams.pageSize = Number(pageData.size)
    } else {
      ElMessage.error(res.data?.msg || '获取数据失败')
      articleList.value = []
      total.value = 0
    }
  } catch (error) {
    ElMessage.error('网络错误，请检查后端服务是否启动')
    articleList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryParams.title = ''
  queryParams.category = ''
  queryParams.status = undefined
  handleQuery()
}

const editDialogVisible = ref(false)
const editing = ref(false)
const editForm = reactive({
  id: null as number | null,
  title: '',
  summary: '',
  content: '',
  coverUrl: '',
  category: '',
  tags: '',
  status: 1,
  isVipOnly: 0,
  isFeatured: 0
})

const editRules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }]
}

const handleCreate = () => {
  editForm.id = null
  editForm.title = ''
  editForm.summary = ''
  editForm.content = ''
  editForm.coverUrl = ''
  editForm.category = ''
  editForm.tags = ''
  editForm.status = 1
  editForm.isVipOnly = 0
  editForm.isFeatured = 0
  editDialogVisible.value = true
}

const handleEdit = (row: any) => {
  editForm.id = row.id
  editForm.title = row.title
  editForm.summary = row.summary
  editForm.content = row.content
  editForm.coverUrl = row.coverUrl
  editForm.category = row.category
  editForm.tags = row.tags
  editForm.status = row.status
  editForm.isVipOnly = row.isVipOnly
  editForm.isFeatured = row.isFeatured
  editDialogVisible.value = true
}

const submitEdit = async () => {
  if (!editForm.title) {
    ElMessage.warning('文章标题不能为空')
    return
  }
  editing.value = true
  try {
    const url = editForm.id ? 'http://localhost:8080/api/article' : 'http://localhost:8080/api/article'
    const method = editForm.id ? 'put' : 'post'
    const res = await axios[method](url, editForm)
    if (res.data && res.data.code === 200) {
      ElMessage.success(editForm.id ? '修改成功！' : '创建成功！')
      editDialogVisible.value = false
      getList()
    } else {
      ElMessage.error(res.data?.msg || '操作失败')
    }
  } catch (error) {
    ElMessage.error('请求失败，请检查后端服务')
  } finally {
    editing.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    const res = await axios.delete(`http://localhost:8080/api/article/${id}`)
    if (res.data && res.data.code === 200) {
      ElMessage.success('删除成功！')
      getList()
    } else {
      ElMessage.error(res.data?.msg || '删除失败')
    }
  } catch (error) {
    ElMessage.error('请求失败，请检查后端服务')
  }
}

const handleToggleFeatured = async (row: any) => {
  try {
    const res = await axios.put(`http://localhost:8080/api/article/featured/${row.id}`)
    if (res.data && res.data.code === 200) {
      ElMessage.success(row.isFeatured === 1 ? '已取消推荐' : '已设为推荐')
      getList()
    } else {
      ElMessage.warning(res.data?.msg || '操作失败')
    }
  } catch (error) {
    ElMessage.error('请求失败，请检查后端服务')
  }
}

const handleApprove = async (row: any) => {
  try {
    const res = await axios.put('http://localhost:8080/api/article/admin/review', { id: row.id, approve: true })
    if (res.data && res.data.code === 200) {
      ElMessage.success('审核通过')
      getList()
    } else {
      ElMessage.warning(res.data?.msg || '操作失败')
    }
  } catch (error) {
    ElMessage.error('请求失败，请检查后端服务')
  }
}

const handleReject = async (row: any) => {
  try {
    const { value: rejectReason } = await ElMessageBox.prompt('请输入驳回原因', '驳回文章', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入原因...'
    })
    if (rejectReason === undefined) return
    const res = await axios.put('http://localhost:8080/api/article/admin/review', { id: row.id, approve: false, rejectReason })
    if (res.data && res.data.code === 200) {
      ElMessage.success('已驳回')
      getList()
    } else {
      ElMessage.warning(res.data?.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('请求失败，请检查后端服务')
    }
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.app-container {
  padding: 10px;
}
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.mt-20 {
  margin-top: 20px;
}
.pagination-container {
  display: flex;
  justify-content: flex-end;
}
.font-bold {
  font-weight: bold;
}
.text-gray-400 {
  color: #9ca3af;
}
.mt-2 {
  margin-top: 8px;
}
</style>
