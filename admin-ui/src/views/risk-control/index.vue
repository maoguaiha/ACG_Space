<template>
  <div class="risk-control-container">
    <el-card class="mb-20">
      <template #header>
        <div class="card-header">
          <span>风控中心 - 系统健康监控</span>
          <el-button type="primary" @click="refreshData">刷新</el-button>
        </div>
      </template>
      
      <el-row :gutter="20" class="mb-20">
        <el-col :span="12">
          <h4>熔断器状态</h4>
          <el-table :data="circuitBreakers" stripe>
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="state" label="状态" min-width="120">
              <template #default="{ row }">
                <el-tag :type="getCircuitBreakerType(row.state)">
                  {{ row.state }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="failureRate" label="失败率 (%)">
              <template #default="{ row }">
                {{ row.failureRate.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="numberOfBufferedCalls" label="缓冲调用数" />
            <el-table-column prop="numberOfFailedCalls" label="失败数" />
            <el-table-column prop="numberOfSuccessfulCalls" label="成功数" />
          </el-table>
        </el-col>
        <el-col :span="12">
          <h4>限流器状态</h4>
          <el-table :data="rateLimiters" stripe>
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="availablePermissions" label="可用许可数">
              <template #default="{ row }">
                <el-tag :type="row.availablePermissions > 0 ? 'success' : 'danger'">
                  {{ row.availablePermissions }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="numberOfWaitingThreads" label="等待线程数" />
          </el-table>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { riskControlApi, type CircuitBreakerVO, type RateLimiterVO } from '@/api/risk-control'

const circuitBreakers = ref<CircuitBreakerVO[]>([])
const rateLimiters = ref<RateLimiterVO[]>([])

const refreshData = async () => {
  try {
    const [cbRes, rlRes] = await Promise.all([
      riskControlApi.getCircuitBreakers(),
      riskControlApi.getRateLimiters()
    ])
    circuitBreakers.value = cbRes.data
    rateLimiters.value = rlRes.data
  } catch (error) {
    console.error('获取数据失败', error)
  }
}

const getCircuitBreakerType = (state: string) => {
  switch (state) {
    case 'CLOSED':
      return 'success'
    case 'OPEN':
      return 'danger'
    case 'HALF_OPEN':
      return 'warning'
    default:
      return 'info'
  }
}

onMounted(() => {
  refreshData()
})
</script>

<style scoped>
.risk-control-container {
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mb-20 {
  margin-bottom: 20px;
}
</style>
