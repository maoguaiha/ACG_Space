# ====== Front-UI (Nuxt 3) Dockerfile ======

# ---- Build Stage ----
FROM node:22-alpine AS builder

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# ---- Runtime Stage (Debian slim, 避免 alpine musl 兼容问题) ----
FROM node:22-slim

WORKDIR /app

COPY --from=builder /app/.output ./.output

EXPOSE 3000

# 启动脚本：显式绑定 HOST=0.0.0.0 + PORT，加启动诊断日志
RUN printf '#!/bin/sh\necho "[startup] Node $(node -v), PORT=${PORT:-3000}"\nexport HOST=0.0.0.0\nexport NITRO_HOST=0.0.0.0\nexport PORT=${PORT:-3000}\nexport NITRO_PORT=${PORT}\necho "[startup] Listening on ${HOST}:${PORT}"\nexec node .output/server/index.mjs\n' > /app/start.sh \
  && chmod +x /app/start.sh

CMD ["/app/start.sh"]
