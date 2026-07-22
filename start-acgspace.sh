#!/usr/bin/env bash
# ============================================================
#  ACG_Space 一键启动脚本
#  用法：
#    bash start-acgspace.sh          # 启动全部（默认）
#    bash start-acgspace.sh start    # 同上
#    bash start-acgspace.sh stop     # 停止 后端 / 用户端 / 管理端（保留 Docker 基础设施）
#    bash start-acgspace.sh down     # 停止全部，含 Docker 基础设施（数据卷 acg_mysql_data 保留）
#
#  踩过的坑（已写进本脚本）：
#   1. Docker daemon 在 default context（desktop-linux 连不上），先切。
#   2. MySQL 宿主机端口用 13306（3306 被 Windows 动态保留范围 3272-3371 占用）。
#   3. 后端必须从 backend 目录用 java -jar 跑，并显式 --server.port=8080
#      （jar 内编译端口是 4602，而该端口被 WorkBuddy 占用，直接起会报端口冲突）。
#   4. 后端数据源端口用 -DDB_PORT=13306 覆盖（application-dev.yml 里是 ${DB_PORT:3306}）。
#   5. admin-ui 不能用 5173/5180（落在 Windows TCP 排除范围 5141-5240），也不能用 8081（落在 Hyper-V 保留范围 8080-8179），改用 18081。
#   6. Maven 本机损坏，直接用构建好的 jar，无需重编。
# ============================================================

set -uo pipefail

# 脚本所在目录（即 ACG_Space 根目录）
ACG_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ACG_DIR" || { echo "无法切换到 $ACG_DIR"; exit 1; }

# 端口与关键常量
# NOTE: 8080-8083 被 Windows Hyper-V/Docker 动态保留，改用 18083
DB_HOST_PORT=13306
BACKEND_PORT=18083
FRONT_PORT=3000
ADMIN_PORT=18081
MYSQL_CONTAINER=acg_mysql
JAR="target/acg-space-backend-1.0.0-SNAPSHOT.jar"
BACKEND_URL="http://127.0.0.1:${BACKEND_PORT}/"
BACKEND_API_URL="http://localhost:${BACKEND_PORT}/api"
FRONT_URL="http://localhost:${FRONT_PORT}/"
ADMIN_URL="http://localhost:${ADMIN_PORT}/"

LOG_DIR="$ACG_DIR/logs"
mkdir -p "$LOG_DIR"

# ---------- 工具函数 ----------
port_up() {                 # port_up <port>  -> 0 表示已监听
  local code
  code=$(curl -s -o /dev/null -w '%{http_code}' --max-time 4 "http://127.0.0.1:$1/" 2>/dev/null)
  [ "$code" != "000" ]
}

pid_on_port() {             # pid_on_port <port> -> 打印 LISTENING 的 PID
  netstat -ano 2>/dev/null | grep -E "[:.]$1 " | grep LISTENING | awk '{print $NF}' | head -1
}

wait_port() {               # wait_port <port> <name> <timeout秒>
  local port="$1" name="$2" max="$3" i=0
  echo -n "  等待 ${name} 端口 ${port} 就绪"
  while ! port_up "$port"; do
    i=$((i+2)); sleep 2
    echo -n "."
    if [ "$i" -ge "$max" ]; then echo " 超时!"; return 1; fi
  done
  echo " 就绪 (${i}s)"
  return 0
}

# ---------- stop / down ----------
do_stop() {
  echo "==> 停止应用服务（保留 Docker 基础设施）"
  for port in "$BACKEND_PORT" "$FRONT_PORT" "$ADMIN_PORT"; do
    pid=$(pid_on_port "$port")
    if [ -n "$pid" ]; then
      echo "  关闭端口 ${port} 的进程 PID=${pid}"
      taskkill //F //PID "$pid" >/dev/null 2>&1 || kill -9 "$pid" >/dev/null 2>&1 || true
    fi
  done
  echo "完成。MySQL/Redis/RocketMQ 仍在运行（如需停止它们，用本脚本 down）。"
}

do_down() {
  echo "==> 停止全部（含 Docker 基础设施，数据卷 acg_mysql_data 保留）"
  do_stop
  docker context use default >/dev/null 2>&1 || true
  docker compose down 2>&1 | tail -5
}

# ---------- start ----------
do_start() {
  echo "=================================================="
  echo "  ACG_Space 一键启动"
  echo "=================================================="

  # 1) Docker context
  echo "[1/5] 切换 Docker context -> default"
  docker context use default >/dev/null 2>&1 || true
  if ! docker ps >/dev/null 2>&1; then
    echo "  ✗ Docker 引擎连不上，请先启动 Docker Desktop 后重试。"
    exit 1
  fi
  echo "  ✓ Docker 引擎正常"

  # 2) 基础设施
  echo "[2/5] 启动 Docker 基础设施 (MySQL/Redis/RocketMQ)"
  docker compose up -d 2>&1 | tail -6

  # 3) 等 MySQL 就绪
  echo "[3/5] 等待 MySQL 就绪"
  local i=0
  until docker exec "$MYSQL_CONTAINER" mysqladmin ping -uroot -p123456 >/dev/null 2>&1; do
    i=$((i+2)); sleep 2
    if [ "$i" -ge 90 ]; then echo "  ✗ MySQL 90s 内未就绪，请查 docker logs $MYSQL_CONTAINER"; exit 1; fi
  done
  echo "  ✓ MySQL ready (${i}s)"

  # 4) 后端
  echo "[4/5] 启动后端 (Spring Boot, 端口 ${BACKEND_PORT})"
  if port_up "$BACKEND_PORT"; then
    echo "  • 后端已在运行，跳过"
  else
    ( cd "$ACG_DIR/backend" && nohup java -DDB_PORT="$DB_HOST_PORT" \
        -jar "$JAR" --spring.profiles.active=dev --server.port="$BACKEND_PORT" \
        --logging.file.name=logs/acg-backend.log \
        > "$LOG_DIR/backend.out" 2>&1 & )
    echo "  • 后端已后台启动（日志: backend/logs/acg-backend.log）"
  fi

  # 5) 前端
  echo "[5/5] 启动前端 (用户端 ${FRONT_PORT} / 管理端 ${ADMIN_PORT})"
  if port_up "$FRONT_PORT"; then
    echo "  • 用户端已在运行，跳过"
  else
    ( nohup npm --prefix "$ACG_DIR/front-ui" run dev \
        > "$LOG_DIR/front.out" 2>&1 & )
    echo "  • 用户端已后台启动（日志: logs/front.out）"
  fi
  if port_up "$ADMIN_PORT"; then
    echo "  • 管理端已在运行，跳过"
  else
    ( nohup npm --prefix "$ACG_DIR/admin-ui" run dev -- --port "$ADMIN_PORT" \
        > "$LOG_DIR/admin.out" 2>&1 & )
    echo "  • 管理端已后台启动（日志: logs/admin.out）"
  fi

  # ---- 探活 ----
  echo "--------------------------------------------------"
  echo "等待服务探活..."
  wait_port "$BACKEND_PORT" "后端" 90
  wait_port "$FRONT_PORT"  "用户端" 90
  wait_port "$ADMIN_PORT"   "管理端" 90

  echo "=================================================="
  echo "  ✅ ACG_Space 启动完成"
  echo "  用户端:  $FRONT_URL"
  echo "  管理端:  $ADMIN_URL"
  echo "  后端API: ${BACKEND_API_URL}   (根路径 / 返回 403 是 Spring Security 正常拦截)"
  echo "=================================================="
  echo "说明：封面图(Bangumi CDN)在本机无公网出口时加载失败会显示占位图，属正常；"
  echo "      后端同步 Bangumi 时报 Connection reset 也属同一网络限制，不影响本地业务。"
  echo "停止：bash start-acgspace.sh stop"
}

# ---------- 入口 ----------
case "${1:-start}" in
  start) do_start ;;
  stop)  do_stop ;;
  down)  do_down ;;
  *)     echo "未知参数: $1  (可用: start | stop | down)"; exit 1 ;;
esac

# 交互终端（如双击 .bat 打开的 Git Bash）下停留，便于查看输出；非 TTY（如 CI/代理）直接退出
if [ -t 0 ]; then
  echo
  read -r -p "按 Enter 关闭窗口..."
fi
