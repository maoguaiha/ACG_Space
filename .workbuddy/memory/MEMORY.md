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
- 后端中文 JSON 接口在 Git Bash + curl 下偶发 fastjson2 `malformed input` 500（报 "around byte 16"）
  - 根因：终端把内联中文请求体以非 UTF-8 编码发送，fastjson2 按 UTF-8 解析字节失败。这是**测试客户端编码问题，非后端 bug**；FastjsonConfig 已显式 UTF-8，浏览器 fetch / Python 均正常。
  - 验证含中文请求体务必用 UTF-8 客户端：Python `urllib` 显式 `.encode('utf-8')`，或 curl `--data-binary @utf8file`（避免把中文直接写进 `-d` 字面量）。

## 系统通知
- `IBizMessageService.sendSystemNotification(toUserId, content)` — fromUserId=1 (系统管理员)
- 自动通知触发点：
  - `AdminArticleController.review()` — 审核通过/拒绝时通知作者
  - `BizArticleController.delete()` — 删除文章时通知作者
  - `BizCommentController.delete()` — 删除评论时通知评论作者（截取30字）
  - `BizRedeemOrderServiceImpl.createOrder()` — 订单创建成功时通知用户
