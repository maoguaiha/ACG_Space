# Workspace Memory

## 项目信息
- ACG Space: 动漫分享社区，Java 17 + Spring Boot 3 后端，Nuxt 3 用户端，Vue 3 管理端
- Railway 部署：后端和 front-ui 分别独立部署

## front-ui 部署
- 使用 Nixpacks builder，startCommand: `node .output/server/index.mjs`
- 后端地址通过 Railway Variables 设置 `NUXT_API_INTERNAL_BASE`
- .nvmrc 指定 Node 22，package.json engines >=18

## 已知问题与修复
- Railway 健康检查失败：根因是首页 SSR 时后端 API 调用无超时限制，后端不可达时无限 hanging
  - 修复：useApi.ts 添加 8s SSR 超时 + retry=0
  - 修复：index.vue useAsyncData 添加 .catch() 兜底
