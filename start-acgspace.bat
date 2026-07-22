@echo off
chcp 65001 >nul 2>&1
REM ============================================================
REM  ACG_Space 一键启动（Windows 双击入口）
REM  用法：直接双击本文件即可启动；也可带参数：
REM    start-acgspace.bat stop   停止应用服务
REM    start-acgspace.bat down   停止全部（含 Docker 基础设施）
REM ============================================================
set "ACG_DIR=C:\Users\Administrator\Desktop\材料\学习\ai\study\java\ACG_Space"

IF NOT EXIST "%ACG_DIR%\start-acgspace.sh" (
  echo 未找到 %ACG_DIR%\start-acgspace.sh
  pause
  exit /b 1
)

start "" "C:\Program Files\Git\git-bash.exe" "%ACG_DIR%\start-acgspace.sh" %*
