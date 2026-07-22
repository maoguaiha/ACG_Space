<template>
  <div class="app-container">
    <el-card shadow="never">
      <!-- 搜索与操作区域 -->
      <div class="header-actions">
        <el-form :inline="true" :model="queryParams">
          <el-form-item label="番剧名称">
            <el-input v-model="queryParams.title" placeholder="请输入名称" clearable />
          </el-form-item>
          <el-form-item label="开播年份">
            <el-date-picker v-model="queryParams.year" type="year" placeholder="选择年份" value-format="YYYY" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        
        <div class="right-actions">
          <el-button type="primary" icon="Download" @click="handleSyncBangumi">从 Bangumi 同步</el-button>
          <el-button type="success" icon="Plus">手动新增</el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table :data="animeList" v-loading="loading" border style="width: 100%" class="mt-20">
        <el-table-column label="序号" width="60" align="center">
          <template #default="scope">
            {{ (queryParams.pageNum - 1) * queryParams.pageSize + (scope.$index + 1) }}
          </template>
        </el-table-column>
        <el-table-column label="推荐" width="80" align="center">
          <template #default="scope">
            <el-button
              link
              :type="scope.row.featured === 1 ? 'warning' : 'info'"
              @click="handleToggleFeatured(scope.row)"
              :title="scope.row.featured === 1 ? '取消首页推荐' : '设为首页推荐'"
              style="font-size: 20px;"
            >
              {{ scope.row.featured === 1 ? '★' : '☆' }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="海报" width="100" align="center">
          <template #default="scope">
            <el-image 
              style="width: 60px; height: 80px; border-radius: 4px;" 
              :src="scope.row.coverUrl" 
              fit="cover"
              :preview-src-list="[scope.row.coverUrl]"
              preview-teleported
            />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="番剧名称" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            <div class="font-bold">{{ scope.row.title }}</div>
            <div class="text-gray-400 text-xs mt-1">{{ scope.row.titleOriginal }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="bgmId" label="BGM ID" width="100" align="center" />
        <el-table-column prop="publishYear" label="年份" width="80" align="center" />
        <el-table-column prop="totalEpisodes" label="总集数" width="80" align="center" />
        <el-table-column prop="rating" label="评分" width="80" align="center">
          <template #default="scope">
            <el-tag type="warning" effect="dark" round size="small">{{ scope.row.rating }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : scope.row.status === 2 ? 'warning' : 'info'">
              {{ statusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-popconfirm
              title="确定要删除这部番剧吗？"
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

      <!-- 分页 -->
      <div class="pagination-container mt-20">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        />
      </div>
    </el-card>

    <!-- Bangumi 同步对话框 -->
    <el-dialog v-model="syncDialogVisible" title="通过 Bangumi ID 同步数据" width="400px">
      <el-form label-width="100px">
        <el-form-item label="Subject ID:">
          <el-input v-model="syncBgmId" placeholder="例如: 328609" />
          <div class="text-xs text-gray-400 mt-1">请从 bgm.tv 网址中获取对应条目ID</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="syncDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitSync" :loading="syncing">
            开始抓取
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑番剧对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑番剧信息" width="600px">
      <el-form :model="editForm" label-width="100px" ref="editFormRef">
        <el-form-item label="番剧名称" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入番剧名称" />
        </el-form-item>
        <el-form-item label="原版名称" prop="titleOriginal">
          <el-input v-model="editForm.titleOriginal" placeholder="请输入原版名称" />
        </el-form-item>
        <el-form-item label="海报链接" prop="coverUrl">
          <el-input v-model="editForm.coverUrl" placeholder="请输入海报图片URL" />
        </el-form-item>
        <el-form-item label="剧情简介" prop="summary">
          <el-input v-model="editForm.summary" type="textarea" :rows="4" placeholder="请输入剧情简介" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="BGM ID" prop="bgmId">
              <el-input-number v-model="editForm.bgmId" :min="0" controls-position="right" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="总集数" prop="totalEpisodes">
              <el-input-number v-model="editForm.totalEpisodes" :min="0" controls-position="right" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="年份" prop="publishYear">
              <el-input-number v-model="editForm.publishYear" :min="1970" :max="2030" controls-position="right" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="评分" prop="rating">
              <el-input-number v-model="editForm.rating" :min="0" :max="10" :precision="1" :step="0.1" controls-position="right" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="editForm.status" placeholder="请选择状态" style="width: 100%;">
                <el-option label="连载中" :value="0" />
                <el-option label="已完结" :value="1" />
                <el-option label="未开播" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit" :loading="editing">
            保存修改
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 番剧状态映射
const statusLabel = (status: number): string => {
  const map: Record<number, string> = { 0: '连载中', 1: '已完结', 2: '未开播' }
  return map[status] ?? '未知'
}

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  title: '',
  year: null
})

const loading = ref(false)
const total = ref(0)
const animeList = ref<any[]>([])

// 真实获取分页数据
const getList = async () => {
  loading.value = true
  try {
    // 构造清理后的参数，避免传递空字符串导致后端解析失败
    const params: any = {
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    }
    if (queryParams.title && queryParams.title.trim().length > 0) params.title = queryParams.title.trim()
    if (queryParams.year) params.year = queryParams.year

    const res = await axios.get('/api/anime/page', { params })
    if (res.data && res.data.code === 200) {
      // 支持后端 MyBatis-Plus Page 结构：{ records, total, current, size }
      const pageData = res.data.data || {}
      animeList.value = pageData.records || []
      // total 可能为字符串或数字，确保为 Number
      total.value = Number(pageData.total) || (Array.isArray(pageData.records) ? pageData.records.length : 0)
      // 同步页码与页大小（以后端返回为准，保证分页组件状态一致）
      if (pageData.current) queryParams.pageNum = Number(pageData.current)
      if (pageData.size) queryParams.pageSize = Number(pageData.size)
    } else {
      ElMessage.error(res.data?.msg || '获取数据失败')
      animeList.value = []
      total.value = 0
    }
  } catch (error) {
    ElMessage.error('网络错误，请检查后端服务是否启动')
    animeList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
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

const resetQuery = () => {
  queryParams.title = ''
  queryParams.year = null
  handleQuery()
}

// ====== Bangumi 同步相关 ======
const syncDialogVisible = ref(false)
const syncBgmId = ref('')
const syncing = ref(false)

const handleSyncBangumi = () => {
  syncBgmId.value = ''
  syncDialogVisible.value = true
}

const submitSync = async () => {
  if (!syncBgmId.value) {
    ElMessage.warning('请输入 Bangumi Subject ID')
    return
  }
  
  syncing.value = true
  try {
    const res = await axios.post(`/api/anime/sync/${syncBgmId.value}`)
    if (res.data && res.data.code === 200) {
      ElMessage.success('同步成功！')
      syncDialogVisible.value = false
      getList()
    } else {
      ElMessage.error(res.data?.msg || '同步失败')
    }
  } catch (error) {
    ElMessage.error('请求失败，请检查后端服务是否启动')
  } finally {
    syncing.value = false
  }
}

// ====== 编辑番剧相关 ======
const editDialogVisible = ref(false)
const editing = ref(false)
const editForm = reactive({
  id: null as number | null,
  bgmId: null as number | null,
  title: '',
  titleOriginal: '',
  coverUrl: '',
  summary: '',
  totalEpisodes: null as number | null,
  publishYear: null as number | null,
  status: 0,
  rating: null as number | null
})

/** 点击编辑按钮，将当前行数据填充到表单 */
const handleEdit = (row: any) => {
  editForm.id = row.id
  editForm.bgmId = row.bgmId
  editForm.title = row.title
  editForm.titleOriginal = row.titleOriginal
  editForm.coverUrl = row.coverUrl
  editForm.summary = row.summary
  editForm.totalEpisodes = row.totalEpisodes
  editForm.publishYear = row.publishYear
  editForm.status = row.status
  editForm.rating = row.rating
  editDialogVisible.value = true
}

/** 提交编辑表单 */
const submitEdit = async () => {
  if (!editForm.title) {
    ElMessage.warning('番剧名称不能为空')
    return
  }
  editing.value = true
  try {
    const res = await axios.put('/api/anime', editForm)
    if (res.data && res.data.code === 200) {
      ElMessage.success('修改成功！')
      editDialogVisible.value = false
      getList()
    } else {
      ElMessage.error(res.data?.msg || '修改失败')
    }
  } catch (error) {
    ElMessage.error('请求失败，请检查后端服务')
  } finally {
    editing.value = false
  }
}

// ====== 删除番剧 ======
const handleDelete = async (id: number) => {
  try {
    const res = await axios.delete(`/api/anime/${id}`)
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

// ====== 切换首页轮播推荐 ======
const handleToggleFeatured = async (row: any) => {
  try {
    const res = await axios.put(`/api/anime/featured/${row.id}`)
    if (res.data && res.data.code === 200) {
      ElMessage.success(row.featured === 1 ? '已取消推荐' : '已设为首页推荐')
      getList()
    } else {
      ElMessage.warning(res.data?.msg || '操作失败')
    }
  } catch (error) {
    ElMessage.error('请求失败，请检查后端服务')
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
.text-xs {
  font-size: 12px;
}
</style>
