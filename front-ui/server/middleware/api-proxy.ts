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
    // 从 event.headers 单独取关键头（避免 Object.fromEntries 漏字段时间复杂性）
    const authorization = event.headers.get('authorization')
    const cookie = event.headers.get('cookie')

    // 取所有头，去掉浏览器专用头（避免后端安全过滤器判定跨域失败）
    const allHeaders = Object.fromEntries(event.headers.entries())
    const { origin, referer, 'sec-fetch-site': _sfs,
      'sec-fetch-mode': _sfm, 'sec-fetch-dest': _sfd,
      'accept-encoding': _ae, host: _h,
      authorization: _a, cookie: _c,  // 也去掉这两个，后面显式赋值保证最新
      ...forwardHeaders } = allHeaders as Record<string, string>

    // 显式塞回 authorization 和 cookie（保证不被之前的解构漏掉）
    if (authorization) forwardHeaders['authorization'] = authorization
    if (cookie) forwardHeaders['cookie'] = cookie

    console.log(`[proxy] ${event.method} ${event.path} -> ${targetUrl}${queryString} | auth=${authorization ? 'YES' : 'NO'} | cookie=${cookie ? 'YES' : 'NO'}`)

    const res = await fetch(targetUrl + queryString, {
      method: event.method,
      headers: forwardHeaders,
      // POST/PUT 用 readRawBody 保留原始 JSON 字符串，避免 readBody 解析后 fetch 二次序列化不匹配
      body: event.method !== 'GET' && event.method !== 'HEAD'
        ? await readRawBody(event)
        : undefined,
    })

    // 用 .text() 替代 .arrayBuffer() — text() 对后端 JSON 解析更可靠
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
    console.error(`[proxy] ${event.method} ${event.path} failed:`, e.message)
    throw createError({
      statusCode: 502,
      statusMessage: `Bad Gateway: ${e.message || 'backend unreachable'}`,
    })
  }
})