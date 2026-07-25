<template>
  <div class="app-container">
    <el-card shadow="never">
      <div class="header-actions">
        <el-form :inline="true" :model="queryParams">
          <el-form-item label="用户名">
            <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="queryParams.nickname" placeholder="请输入昵称" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="userList" v-loading="loading" border style="width: 100%" class="mt-20">
        <el-table-column label="序号" width="60" align="center">
          <template #default="scope">
            {{ (queryParams.pageNum - 1) * queryParams.pageSize + (scope.$index + 1) }}
          </template>
        </el-table-column>
        <el-table-column label="头像" width="80" align="center">
          <template #default="scope">
            <el-avatar :src="scope.row.avatar" :size="40">
              {{ scope.row.nickname?.charAt(0) || 'U' }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="100" show-overflow-tooltip />
        <el-table-column prop="nickname" label="昵称" min-width="100" show-overflow-tooltip />
        <el-table-column prop="vipStatus" label="VIP状态" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.vipStatus === 2" type="warning" effect="dark">SVIP</el-tag>
            <el-tag v-else-if="scope.row.vipStatus === 1" type="warning">VIP</el-tag>
            <el-tag v-else type="info">普通</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userLevel" label="等级" width="70" align="center">
          <template #default="scope">
            Lv.{{ scope.row.userLevel || 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="80" align="center" />
        <el-table-column prop="followerCount" label="粉丝" width="70" align="center" />
        <el-table-column prop="followingCount" label="关注" width="70" align="center" />
        <el-table-column label="注册时间" width="160" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
            <el-button link type="warning" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
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
        />
      </div>
    </el-card>

    <!-- 用户详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="用户详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ viewForm.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ viewForm.nickname }}</el-descriptions-item>
        <el-descriptions-item label="头像" :span="2">
          <el-avatar :src="viewForm.avatar" :size="60">{{ viewForm.nickname?.charAt(0) }}</el-avatar>
        </el-descriptions-item>
        <el-descriptions-item label="简介" :span="2">{{ viewForm.bio || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ viewForm.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="VIP状态">
          <el-tag v-if="viewForm.vipStatus === 2" type="warning" effect="dark">SVIP</el-tag>
          <el-tag v-else-if="viewForm.vipStatus === 1" type="warning">VIP</el-tag>
          <el-tag v-else type="info">普通</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户等级">Lv.{{ viewForm.userLevel || 1 }}</el-descriptions-item>
        <el-descriptions-item label="当前经验">{{ viewForm.levelExperience || 0 }}</el-descriptions-item>
        <el-descriptions-item label="VIP到期时间">{{ viewForm.vipExpireTime ? formatDateTime(viewForm.vipExpireTime) : '永久' }}</el-descriptions-item>
        <el-descriptions-item label="积分">{{ viewForm.points || 0 }}</el-descriptions-item>
        <el-descriptions-item label="粉丝数">{{ viewForm.followerCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="关注数">{{ viewForm.followingCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="注册时间" :span="2">{{ formatDateTime(viewForm.createTime) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 编辑用户对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑用户" width="600px">
      <el-form :model="editForm" label-width="80px" ref="editFormRef">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="简介" prop="bio">
          <el-input v-model="editForm.bio" type="textarea" :rows="2" placeholder="请输入个人简介" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-divider content-position="left">权限与等级</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="VIP状态" prop="vipStatus">
              <el-select v-model="editForm.vipStatus" placeholder="选择VIP状态" style="width: 100%;">
                <el-option :value="0" label="普通用户" />
                <el-option :value="1" label="VIP" />
                <el-option :value="2" label="SVIP" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="用户等级" prop="userLevel">
              <el-input-number v-model="editForm.userLevel" :min="1" :max="100" controls-position="right" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="积分" prop="points">
              <el-input-number v-model="editForm.points" :min="0" controls-position="right" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="当前经验" prop="levelExperience">
              <el-input-number v-model="editForm.levelExperience" :min="0" controls-position="right" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="VIP到期" prop="vipExpireTime">
              <el-date-picker v-model="editForm.vipExpireTime" type="datetime" placeholder="选择VIP到期时间" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit" :loading="editing">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request'

interface UserItem {
  id: number
  username: string
  nickname: string
  avatar: string
  bio: string
  email: string
  points: number
  vipStatus: number
  vipExpireTime: string
  userLevel: number
  levelExperience: number
  followerCount: number
  followingCount: number
  createTime: string
}

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  username: '',
  nickname: ''
})

const loading = ref(false)
const total = ref(0)
const userList = ref<UserItem[]>([])

const getList = async () => {
  loading.value = true
  try {
    const params: any = {
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    }
    if (queryParams.username) params.username = queryParams.username
    if (queryParams.nickname) params.nickname = queryParams.nickname

    const res = await request.get('/admin/user/page', { params })
    if (res.data && res.data.code === 200) {
      const pageData = res.data.data || {}
      userList.value = pageData.records || []
      total.value = Number(pageData.total) || 0
    } else {
      ElMessage.error(res.data?.msg || '获取数据失败')
      userList.value = []
      total.value = 0
    }
  } catch (error) {
    ElMessage.error('网络错误，请检查后端服务是否启动')
    userList.value = []
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
  queryParams.username = ''
  queryParams.nickname = ''
  handleQuery()
}

const formatDateTime = (dateStr: string): string => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const viewDialogVisible = ref(false)
const viewForm = ref<UserItem>({
  id: 0,
  username: '',
  nickname: '',
  avatar: '',
  bio: '',
  email: '',
  points: 0,
  vipStatus: 0,
  vipExpireTime: '',
  userLevel: 1,
  levelExperience: 0,
  followerCount: 0,
  followingCount: 0,
  createTime: ''
})

const handleView = (row: UserItem) => {
  viewForm.value = { ...row }
  viewDialogVisible.value = true
}

const editDialogVisible = ref(false)
const editing = ref(false)
const editFormRef = ref()
const editForm = reactive({
  id: null as number | null,
  username: '',
  nickname: '',
  bio: '',
  email: '',
  points: 0,
  vipStatus: 0,
  vipExpireTime: null as Date | null,
  userLevel: 1,
  levelExperience: 0
})

const handleEdit = (row: UserItem) => {
  editForm.id = row.id
  editForm.username = row.username
  editForm.nickname = row.nickname
  editForm.bio = row.bio || ''
  editForm.email = row.email || ''
  editForm.points = row.points || 0
  editForm.vipStatus = row.vipStatus || 0
  editForm.vipExpireTime = row.vipExpireTime ? new Date(row.vipExpireTime) : null
  editForm.userLevel = row.userLevel || 1
  editForm.levelExperience = row.levelExperience || 0
  editDialogVisible.value = true
}

const submitEdit = async () => {
  if (!editForm.nickname) {
    ElMessage.warning('昵称不能为空')
    return
  }
  if (!editForm.id) {
    ElMessage.warning('用户ID不存在')
    return
  }
  editing.value = true
  try {
    const data: any = {
      vipStatus: editForm.vipStatus,
      vipExpireTime: editForm.vipExpireTime,
      userLevel: editForm.userLevel,
      levelExperience: editForm.levelExperience,
      points: editForm.points
    }
    const res = await request.put(`/admin/user/vip/${editForm.id}`, data)
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

const handleDelete = (row: UserItem) => {
  ElMessageBox.confirm(`确定要删除用户"${row.nickname || row.username}"吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.delete(`/admin/user/${row.id}`)
      if (res.data && res.data.code === 200) {
        ElMessage.success('删除成功！')
        getList()
      } else {
        ElMessage.error(res.data?.msg || '删除失败')
      }
    } catch (error) {
      ElMessage.error('请求失败，请检查后端服务')
    }
  }).catch(() => {})
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
</style>