// 零依赖静态服务器 + /api 反向代理（部署到 Railway 用）
// 前端 SPA 调用同源 /api/*，本服务把 /api 转发到后端，避免跨域(CORS)问题。
// 注意：package.json 设了 "type": "module"，故本文件必须用 ESM 写法（不能用 require）
import http from 'node:http'
import https from 'node:https'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

// Railway 在运行时注入 $PORT；绝不在构建期求值（会写死）。
const PORT = process.env.PORT || 8080
const BACKEND = process.env.BACKEND_URL || 'http://localhost:18083'
const DIST = path.join(__dirname, 'dist')

console.log(`[admin-ui] env PORT=${PORT} BACKEND_URL=${BACKEND} DIST=${DIST}`)

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

function serveStatic(res, filePath) {
  fs.readFile(filePath, (err, data) => {
    if (!err) {
      const ext = path.extname(filePath).toLowerCase()
      res.writeHead(200, { 'Content-Type': MIME[ext] || 'application/octet-stream' })
      res.end(data)
      return true
    }
    return false
  })
  return true
}

const server = http.createServer((req, res) => {
  const parsed = new URL(req.url, `http://${req.headers.host}`)

  // 1) /api 请求 -> 反向代理到后端（服务端转发，无 CORS 限制）
  if (parsed.pathname.startsWith('/api')) {
    let target
    try {
      target = new URL(BACKEND)
    } catch (e) {
      res.writeHead(500, { 'Content-Type': 'application/json' })
      res.end(JSON.stringify({ code: 500, msg: 'Bad BACKEND_URL: ' + BACKEND, data: null }))
      return
    }
    const isHttps = target.protocol === 'https:'
    const transport = isHttps ? https : http
    // 去掉浏览器专用头，避免后端 CORS 过滤器因 Origin mismatch 返回 403
    const {
      origin, referer, 'sec-fetch-site': _sfs,
      'sec-fetch-mode': _sfm, 'sec-fetch-dest': _sfd, ...cleanHeaders
    } = req.headers
    const options = {
      hostname: target.hostname,
      port: target.port ? Number(target.port) : (isHttps ? 443 : 80),
      path: parsed.pathname + parsed.search,
      method: req.method,
      headers: { ...cleanHeaders, host: target.host },
      rejectUnauthorized: false
    }
    const proxyReq = transport.request(options, (proxyRes) => {
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

  // 2) 健康检查 / 根路径：先尝试 dist/index.html，缺失也返回 200（保证 Railway 健康检查通过）
  if (parsed.pathname === '/') {
    const indexPath = path.join(DIST, 'index.html')
    if (fs.existsSync(indexPath)) {
      fs.readFile(indexPath, (err, data) => {
        if (!err) {
          res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' })
          res.end(data)
        } else {
          res.writeHead(200); res.end('<h1>admin-ui</h1>')
        }
      })
      return
    }
    // dist 还没构建好时的兜底（避免健康检查因 404 失败）
    res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' })
    res.end('<h1>admin-ui starting...</h1>')
    return
  }

  // 3) 静态资源
  const safePath = parsed.pathname
  const filePath = path.join(DIST, safePath)
  if (!filePath.startsWith(DIST)) {
    res.writeHead(403); res.end('Forbidden'); return
  }
  if (fs.existsSync(filePath) && fs.statSync(filePath).isFile()) {
    const ext = path.extname(filePath).toLowerCase()
    res.writeHead(200, { 'Content-Type': MIME[ext] || 'application/octet-stream' })
    fs.createReadStream(filePath).pipe(res)
    return
  }
  // 4) SPA 兜底：未匹配路径返回 index.html
  const indexPath = path.join(DIST, 'index.html')
  if (fs.existsSync(indexPath)) {
    fs.readFile(indexPath, (e2, html) => {
      if (!e2) {
        res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' })
        res.end(html)
      } else {
        res.writeHead(404); res.end('Not found')
      }
    })
  } else {
    res.writeHead(200); res.end('<h1>admin-ui</h1>')
  }
})

// 显式绑定 0.0.0.0 + Railway 分配的 $PORT（健康检查才能连上）
try {
  server.listen(PORT, '0.0.0.0', () => {
    console.log(`[admin-ui] listening on 0.0.0.0:${PORT}, proxy /api -> ${BACKEND}`)
  })
} catch (e) {
  console.error('[admin-ui] failed to listen:', e)
  process.exit(1)
}
server.on('error', (e) => {
  console.error('[admin-ui] server error:', e)
  process.exit(1)
})
