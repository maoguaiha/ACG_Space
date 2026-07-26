// Nitro server middleware：动态代理 /api-proxy/* 到后端
// 解决 nuxt.config.ts 的 routeRules proxy 无法读运行时配置的问题
// 生产环境通过 NUXT_API_INTERNAL_BASE 环境变量指定后端地址
export default defineEventHandler(async (event) => {
  // 只拦截 /api-proxy/* 请求，其他路径（如 /health、首页 SSR）走正常路由
  if (!event.path.startsWith('/api-proxy')) return

  const config = useRuntimeConfig()
  const baseUrl = config.apiInternalBase // http://localhost:18083 或 Railway 覆盖值

  const path = event.path.replace(/^\/api-proxy/, '')
  const targetUrl = `${baseUrl}/api${path}`

  const query = getQuery(event)
  const queryString = Object.keys(query).length > 0
    ? '?' + new URLSearchParams(query as Record<string, string>).toString()
    : ''

  try {
    // 去掉浏览器专用头，避免后端 CORS 过滤器因 Origin mismatch 返回 403
    const allHeaders = Object.fromEntries(event.headers.entries())
    const { origin, referer, 'sec-fetch-site': _sfs,
      'sec-fetch-mode': _sfm, 'sec-fetch-dest': _sfd, ...cleanHeaders } = allHeaders as Record<string, string>
    const res = await fetch(targetUrl + queryString, {
      method: event.method,
      headers: {
        ...cleanHeaders,
        host: new URL(baseUrl).host,
      },
      body: event.method !== 'GET' && event.method !== 'HEAD'
        ? await readBody(event)
        : undefined,
    })

    // 透传响应（arrayBuffer() 已自动解压，需重算 Content-Length，否则会触发 HTTP/2 协议错误）
    const body = await res.arrayBuffer()
    const responseHeaders = Object.fromEntries(res.headers.entries())
    // arrayBuffer() 返回的是解压后的原始数据：
    //  - Content-Encoding/Transfer-Encoding 必须删，否则浏览器会重复解压
    //  - Content-Length 是压缩后的大小，必须删，否则 HTTP/2 帧长度对不上 → ERR_HTTP2_PROTOCOL_ERROR
    delete (responseHeaders as any)['content-encoding']
    delete (responseHeaders as any)['transfer-encoding']
    delete (responseHeaders as any)['content-length']
    setResponseHeaders(event, responseHeaders)
    setResponseStatus(event, res.status)
    return body
  } catch (e: any) {
    throw createError({
      statusCode: 502,
      statusMessage: `Bad Gateway: ${e.message || 'backend unreachable'}`,
    })
  }
})
