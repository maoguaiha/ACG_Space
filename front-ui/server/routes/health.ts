/**
 * 健康检查端点 — 不依赖 SSR、不调后端 API，纯静态响应
 * Railway 用此端点代替 "/" 作为 healthcheck，避免 SSR 超时导致部署失败
 */
export default defineEventHandler(() => {
  return {
    status: 'ok',
    uptime: process.uptime(),
    timestamp: Date.now(),
    nodeVersion: process.version
  }
})
