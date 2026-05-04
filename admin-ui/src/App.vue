<template>
  <el-config-provider :size="'default'" :z-index="3000">
    <router-view />
  </el-config-provider>
</template>

<script setup lang="ts">
// Vue root component
</script>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
}
</style>
