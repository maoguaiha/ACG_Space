/**
 * Nuxt Nitro API 路由：将客户端 POST /api/agent/chat 的 SSE 请求流式代理到 Java 后端。
 *
 * 与 server/middleware/api-proxy.ts 的区别：
 *   该中间件会 `await res.text()` 读取完整响应后再返回，无法边收边推。
 *   而此路由直接返回上游的 ReadableStream，实现真正的字节流转发。
 *
 * Java 后端地址通过 runtimeConfig.apiInternalBase 注入（与 api-proxy 一致），
 * 部署时由 NUXT_API_INTERNAL_BASE 环境变量覆盖。
 */
export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig()
  const targetUrl = `${config.apiInternalBase}/api/agent/chat`

  // 透传原始请求体（{ conversationId, message }）
  const rawBody = await readRawBody(event)

  // 透传 Authorization header——Java 侧 SecurityUtils.getUserId() 依赖 JWT
  const authorization = event.headers.get('authorization')

  const response = await fetch(targetUrl, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(authorization ? { Authorization: authorization } : {}),
    },
    body: rawBody,
  })

  if (!response.ok || !response.body) {
    throw createError({
      statusCode: response.status,
      statusMessage: `AI 服务暂不可用（${response.status}）`,
    })
  }

  // SSE 流式响应头——禁止 nginx/浏览器缓冲，确保 token 逐块到达客户端
  setHeader(event, 'Content-Type', 'text/event-stream')
  setHeader(event, 'Cache-Control', 'no-cache')
  setHeader(event, 'Connection', 'keep-alive')
  setHeader(event, 'X-Accel-Buffering', 'no')

  // Nitro 支持直接返回 ReadableStream，内部会 pipe 到客户端
  return response.body
})
