// Nitro server middleware：动态代理 /api-proxy/* 到后端
// 解决 nuxt.config.ts 的 routeRules proxy 无法读运行时配置的问题
// 生产环境通过 NUXT_API_INTERNAL_BASE 环境变量指定后端地址
export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig()
  const baseUrl = config.apiInternalBase // http://localhost:18083 或 Railway 覆盖值

  const path = event.path.replace(/^\/api-proxy/, '')
  const targetUrl = `${baseUrl}/api${path}`

  const query = getQuery(event)
  const queryString = Object.keys(query).length > 0
    ? '?' + new URLSearchParams(query as Record<string, string>).toString()
    : ''

  try {
    const res = await fetch(targetUrl + queryString, {
      method: event.method,
      headers: {
        ...Object.fromEntries(event.headers.entries()),
        host: new URL(baseUrl).host,
      },
      body: event.method !== 'GET' && event.method !== 'HEAD'
        ? await readBody(event)
        : undefined,
    })

    // 透传响应
    const body = await res.arrayBuffer()
    setResponseHeaders(event, Object.fromEntries(res.headers.entries()))
    setResponseStatus(event, res.status)
    return body
  } catch (e: any) {
    throw createError({
      statusCode: 502,
      statusMessage: `Bad Gateway: ${e.message || 'backend unreachable'}`,
    })
  }
})
