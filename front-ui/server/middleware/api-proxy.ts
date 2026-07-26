// Nitro server middleware：动态代理 /api-proxy/* 到后端
// 解决 nuxt.config.ts 的 routeRules proxy 无法读运行时配置的问题
// 生产环境通过 NUXT_API_INTERNAL_BASE 环境变量指定后端地址
export default defineEventHandler(async (event) => {
  // 只拦截 /api-proxy/* 请求，其他路径（如 /health、首页 SSR）走正常路由
  if (!event.path.startsWith('/api-proxy')) return

  const config = useRuntimeConfig()
  const baseUrl = config.apiInternalBase

  const path = event.path.replace(/^\/api-proxy/, '')
  const targetUrl = `${baseUrl}/api${path}`

  const query = getQuery(event)
  const queryString = Object.keys(query).length > 0
    ? '?' + new URLSearchParams(query as Record<string, string>).toString()
    : ''

  try {
    const allHeaders = Object.fromEntries(event.headers.entries())
    // 去掉浏览器专用头，避免后端 CORS/安全过滤器因 Origin mismatch 返回 403
    const { origin, referer, 'sec-fetch-site': _sfs,
      'sec-fetch-mode': _sfm, 'sec-fetch-dest': _sfd,
      'accept-encoding': _ae, host: _h, ...cleanHeaders } = allHeaders as Record<string, string>

    const res = await fetch(targetUrl + queryString, {
      method: event.method,
      headers: cleanHeaders,
      body: event.method !== 'GET' && event.method !== 'HEAD'
        ? await readBody(event)
        : undefined,
    })

    // 用 .text() 替代 .arrayBuffer() — text() 会后端 JSON 解析更可靠
    const body = await res.text()

    // 解构剔除会有问题的 Header（不依赖 delete）
    const {
      'content-encoding': _ce,
      'transfer-encoding': _te,
      'content-length': _cl,
      ...safeHeaders
    } = Object.fromEntries(res.headers.entries()) as Record<string, string>

    setResponseHeaders(event, safeHeaders)
    setResponseStatus(event, res.status)
    return body
  } catch (e: any) {
    throw createError({
      statusCode: 502,
      statusMessage: `Bad Gateway: ${e.message || 'backend unreachable'}`,
    })
  }
})
