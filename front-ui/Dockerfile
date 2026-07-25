# ====== Front-UI (Nuxt 3) Dockerfile ======
# 多阶段构建：build 阶段用 Node 22 编译，runtime 阶段用精简 alpine 镜像运行

# ---- Build Stage ----
FROM node:22-alpine AS builder

WORKDIR /app

# 先复制依赖文件，利用 Docker 缓存层加速重复构建
COPY package.json package-lock.json ./

# 安装全部依赖（含 devDependencies，Nuxt 模块构建需要）
RUN npm ci

# 复制源码
COPY . .

# Nuxt build → 生成 .output/server/index.mjs（node-server preset）
RUN npm run build

# ---- Runtime Stage ----
FROM node:22-alpine

WORKDIR /app

# 只复制构建产物
COPY --from=builder /app/.output ./.output

# 暴露端口（Railway 通过 PORT 环境变量动态分配，这里只是文档标注）
EXPOSE 3000

# 健康检查：由 Railway 层路由处理，不在 Dockerfile 里写 HEALTHCHECK

# Node.js 内存限制：Railway 免费层 512MB，给 V8 分配 384MB 留出系统余量
ENV NODE_OPTIONS="--max-old-space-size=384"

# 启动命令
CMD ["node", ".output/server/index.mjs"]
