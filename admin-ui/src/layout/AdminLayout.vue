<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon :size="24" color="#409EFC"><Monitor /></el-icon>
        <span class="title">ACG Space Admin</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="menu"
        background-color="#1f2937"
        text-color="#9ca3af"
        active-text-color="#ffffff"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>数据大盘</span>
        </el-menu-item>
        <el-menu-item index="/anime">
          <el-icon><VideoCamera /></el-icon>
          <span>番剧库管理</span>
        </el-menu-item>
        <el-menu-item index="/article">
          <el-icon><Document /></el-icon>
          <span>文章管理</span>
        </el-menu-item>
        <el-menu-item index="/comment">
          <el-icon><ChatDotRound /></el-icon>
          <span>评论审核</span>
        </el-menu-item>
        <el-menu-item index="/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-divider border-style="dashed" />
        <div class="menu-section-title">V2.0 数字资产</div>
        <el-menu-item index="/item">
          <el-icon><Box /></el-icon>
          <span>商品图鉴</span>
        </el-menu-item>
        <el-menu-item index="/gacha">
          <el-icon><Present /></el-icon>
          <span>抽赏配置</span>
        </el-menu-item>
        <el-menu-item index="/transaction">
          <el-icon><Wallet /></el-icon>
          <span>交易监控</span>
        </el-menu-item>
        <el-menu-item index="/delivery">
          <el-icon><Van /></el-icon>
          <span>物流调度</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-avatar size="small" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
          <span class="username">Admin</span>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title as string)
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.aside {
  background-color: #1f2937;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: white;
  border-bottom: 1px solid #374151;
}

.title {
  font-weight: 600;
  font-size: 16px;
}

.menu {
  flex: 1;
  border-right: none;
}

.menu-section-title {
  padding: 10px 20px;
  font-size: 12px;
  color: #6b7280;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

:deep(.el-menu-item.is-active) {
  background-color: #374151;
  border-left: 4px solid #409eff;
}

:deep(.el-divider) {
  margin: 8px 0;
  border-color: #374151;
}

.header {
  background-color: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  font-size: 14px;
  color: #374151;
}

.main-content {
  background-color: #f3f4f6;
  padding: 20px;
}

/* 页面切换动画 */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.3s;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
