// 零依赖静态服务器 + /api 反向代理（部署到 Railway 用）
// 前端 SPA 调用同源 /api/*，本服务把 /api 转发到后端，避免跨域(CORS)问题。
const http = require('http')
const fs = require('fs')
const path = require('path')
const url = require('url')

const PORT = process.env.PORT || 8080
const BACKEND = process.env.BACKEND_URL || 'http://localhost:18083'
const DIST = path.join(__dirname, 'dist')

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
  '.ttf': 'font/ttf',
  '.map': 'application/json'
}

const server = http.createServer((req, res) => {
  const parsed = url.parse(req.url)

  // 1) /api 请求 -> 反向代理到后端（服务端转发，无 CORS 限制）
  if (parsed.pathname.startsWith('/api')) {
    const target = url.parse(BACKEND)
    const options = {
      hostname: target.hostname,
      port: target.port || 80,
      path: parsed.path,
      method: req.method,
      headers: { ...req.headers, host: target.host }
    }
    const proxyReq = http.request(options, (proxyRes) => {
      // 透传除 chunked 外的响应头
      const headers = { ...proxyRes.headers }
      res.writeHead(proxyRes.statusCode, headers)
      proxyRes.pipe(res)
    })
    proxyReq.on('error', (e) => {
      res.writeHead(502, { 'Content-Type': 'application/json' })
      res.end(JSON.stringify({ code: 502, msg: 'Bad gateway: ' + e.message, data: null }))
    })
    req.pipe(proxyReq)
    return
  }

  // 2) 静态资源
  const safePath = parsed.pathname === '/' ? 'index.html' : parsed.pathname
  const filePath = path.join(DIST, safePath)
  // 防目录穿越
  if (!filePath.startsWith(DIST)) {
    res.writeHead(403); res.end('Forbidden'); return
  }
  fs.readFile(filePath, (err, data) => {
    if (!err) {
      const ext = path.extname(filePath).toLowerCase()
      res.writeHead(200, { 'Content-Type': MIME[ext] || 'application/octet-stream' })
      res.end(data)
      return
    }
    // 3) SPA 兜底：未匹配路径返回 index.html
    fs.readFile(path.join(DIST, 'index.html'), (e2, html) => {
      if (e2) { res.writeHead(404); res.end('Not found'); return }
      res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' })
      res.end(html)
    })
  })
})

server.listen(PORT, () => {
  console.log(`[admin-ui] serving dist on :${PORT}, proxy /api -> ${BACKEND}`)
})
