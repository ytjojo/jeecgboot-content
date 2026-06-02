#!/usr/bin/env bash
# env.sh - 项目配置常量中心
# 单一来源：从 application-dev.yml 解析出的关键配置集中在此
# 修改端口/账号/库名请改本文件，不要散落到子脚本

# 防止重复 source
if [ -n "${__JEECG_SCRIPT_ENV_LOADED:-}" ]; then
  return 0 2>/dev/null || true
fi
__JEECG_SCRIPT_ENV_LOADED=1

# ========== 路径 ==========
# 重要：不要用 SCRIPT_DIR 覆盖调用方同名变量
# env.sh 位于 script/lib/，其父级 script/，再上一级才是仓库根
LIB_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# 仅在调用方未设置时，给一个 fallback 默认值（仓库根 = lib 的祖级）
: "${PROJECT_ROOT:=$(cd "$LIB_DIR/../.." && pwd)}"
: "${SCRIPT_DIR:=$LIB_DIR}"
BACKEND_DIR="$PROJECT_ROOT/jeecg-boot"
FRONTEND_DIR="$PROJECT_ROOT/jeecgboot-vue3"
START_MODULE="$BACKEND_DIR/jeecg-module-system/jeecg-system-start"

# ========== 后端 ==========
MAIN_CLASS="org.jeecg.JeecgSystemApplication"
MVN_PROFILE="dev"                  # 与 pom.xml <profile.name>dev</profile.name> 对齐
BACKEND_PORT=8080
BACKEND_CONTEXT="/jeecg-boot"
BACKEND_HEALTH_URL="http://127.0.0.1:${BACKEND_PORT}${BACKEND_CONTEXT}/sys/user/queryUsername"
BACKEND_DOC_URL="http://127.0.0.1:${BACKEND_PORT}${BACKEND_CONTEXT}/doc.html"

# ========== 前端 ==========
FRONTEND_PORT=3100
FRONTEND_HEALTH_URL="http://127.0.0.1:${FRONTEND_PORT}"

# ========== MySQL ==========
MYSQL_HOST=127.0.0.1
MYSQL_PORT=3306
MYSQL_USER=root
MYSQL_PASS=root
MYSQL_DB=jeecg-boot
FULL_SQL="$BACKEND_DIR/db/jeecgboot-mysql-5.7.sql"
# 探测"是否已初始化"用的标志性业务表
MYSQL_BOOTSTRAP_TABLE="sys_user"

# ========== Redis ==========
REDIS_HOST=127.0.0.1
REDIS_PORT=6379

# ========== PostgreSQL（可选，用于 AI RAG 向量库） ==========
PG_HOST=127.0.0.1
PG_PORT=5432
PG_USER=postgres
PG_PASS=postgres

# ========== brew 服务清单 ==========
# 必装
BREW_REQUIRED=(mysql redis)
# 可选
BREW_OPTIONAL=(postgresql@17)

# ========== 运行产物 ==========
# 日志与 PID 一律落在 script/logs 和 script/.pid（仓库根的可写区）
LOG_DIR="$LIB_DIR/../logs"
PID_DIR="$LIB_DIR/../.pid"
BACKEND_LOG="$LOG_DIR/backend.log"
FRONTEND_LOG="$LOG_DIR/frontend.log"
INSTALL_LOG="$LOG_DIR/install.log"
BACKEND_PID_FILE="$PID_DIR/backend.pid"
FRONTEND_PID_FILE="$PID_DIR/frontend.pid"

# 启动时创建目录（落到 script/ 下，不会污染 lib/）
mkdir -p "$LOG_DIR" "$PID_DIR"
