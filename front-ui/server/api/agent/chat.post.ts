/**
 * Nuxt Nitro API 路由：将客户端 POST /api/agent/chat 的 SSE 请求流式代理到 Java 后端。
 *
 * 与 server/middleware/api-proxy.ts 的区别：
 *   该中间件会 `await res.text()` 读取完整响应后再返回，无法边收边推。
 *   而此路由用「手动 pump」方式真正把 Web ReadableStream 逐字节转发给客户端，
 *   并在流自然结束 / 客户端断开 / 上游异常三条路径上都正确关闭响应。
 *
 * 关键点：h3 的 sendStream + undici 的 ReadableStream.fromWeb 在某些组合下会
 * 出现 EOF 漏传——客户端 reader 永远拿不到 done=true，导致前端 isStreaming 永远
 * true、停止按钮卡死。所以这里改用「getReader().read() → nodeRes.write() →
 * nodeRes.end()」的手动 pump 模式，确保每条路径都能正确关闭响应。
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
      'Accept': 'text/event-stream',
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
  setHeader(event, 'Cache-Control', 'no-cache, no-transform')
  setHeader(event, 'Connection', 'keep-alive')
  setHeader(event, 'X-Accel-Buffering', 'no')

  const nodeRes = event.node.res
  // 如果客户端已断开（AbortController 触发），不要再写
  let closed = false
  const safeEnd = () => {
    if (closed) return
    closed = true
    try { nodeRes.end() } catch { /* 已被关闭 */ }
  }
  // 监听客户端断开
  event.node.req.on('close', safeEnd)
  event.node.req.on('aborted', safeEnd)

  // 手动 pump：从 undici Web ReadableStream 逐 chunk 写到 Node response
  const reader = response.body.getReader()
  ;(async () => {
    try {
      while (!closed) {
        const { done, value } = await reader.read()
        if (done) {
          safeEnd()
          return
        }
        if (closed) return
        // 写入并等 backpressure 解除
        const ok = nodeRes.write(Buffer.from(value))
        if (!ok) {
          await new Promise<void>((resolve) => nodeRes.once('drain', resolve))
        }
      }
    } catch (e) {
      // 上游异常 / 客户端断开 → 都要 end 响应
      safeEnd()
    }
  })()

  // 返回一个永不 resolve 的 promise，让 h3 知道我们手动管理响应生命周期
  return new Promise(() => {})
})
