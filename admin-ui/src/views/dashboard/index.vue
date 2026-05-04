<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6" v-for="card in statCards" :key="card.title">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>{{ card.title }}</span>
              <el-tag :type="card.type" size="small">{{ card.tag }}</el-tag>
            </div>
          </template>
          <div class="card-content">
            <span class="value">{{ card.value }}</span>
            <el-icon :size="40" :color="card.color" class="icon"><component :is="card.icon" /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="mt-20">
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <span>系统状态</span>
          </template>
          <el-empty description="图表加载中..." />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <span>待处理事项</span>
          </template>
          <div class="todo-list">
            <el-alert title="有 5 条新评论待审核" type="warning" show-icon :closable="false" class="mb-10" />
            <el-alert title="昨日同步失败番剧 1 部" type="error" show-icon :closable="false" class="mb-10" />
            <el-alert title="服务器 CPU 使用率正常" type="success" show-icon :closable="false" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const statCards = ref([
  { title: '番剧总数', value: '1,284', tag: '日', type: 'primary', icon: 'VideoCamera', color: '#409EFF' },
  { title: '新增评论', value: '8,521', tag: '日', type: 'success', icon: 'ChatDotRound', color: '#67C23A' },
  { title: '活跃用户', value: '45,892', tag: '月', type: 'warning', icon: 'User', color: '#E6A23C' },
  { title: '积分发放', value: '254k', tag: '月', type: 'danger', icon: 'Coin', color: '#F56C6C' }
])
</script>

<style scoped>
.dashboard-container {
  padding: 10px;
}

.stat-card {
  border-radius: 8px;
  transition: all 0.3s;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.icon {
  opacity: 0.8;
}

.mt-20 {
  margin-top: 20px;
}

.mb-10 {
  margin-bottom: 10px;
}
</style>
