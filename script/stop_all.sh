#!/usr/bin/env bash
# stop_all.sh - 统一停止：前端 → 后端（第三方服务保留）
# 第三方服务请使用 ./stop_services.sh

set -u
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/env.sh
source "$SCRIPT_DIR/lib/env.sh"
# shellcheck source=lib/common.sh
source "$SCRIPT_DIR/lib/common.sh"

log_step "停止 JeecgBoot 全栈（前端 + 后端）"

# 1. 前端
log_info "停止前端"
kill_by_pattern "vite" "vite"
kill_pidfile "$FRONTEND_PID_FILE" "frontend(pidfile)"
if port_in_use "$FRONTEND_PORT"; then
  for pid in $(pid_on_port "$FRONTEND_PORT"); do
    kill_pid "$pid" "port-${FRONTEND_PORT}"
  done
fi
log_ok "前端已停止"

# 2. 后端
log_info "停止后端"
kill_by_pattern "spring-boot:run" "spring-boot:run"
kill_by_pattern "JeecgSystemApplication" "JeecgSystemApplication"
kill_pidfile "$BACKEND_PID_FILE" "backend(pidfile)"
if port_in_use "$BACKEND_PORT"; then
  for pid in $(pid_on_port "$BACKEND_PORT"); do
    kill_pid "$pid" "port-${BACKEND_PORT}"
  done
fi
log_ok "后端已停止"

# 3. 兜底确认
sleep 1
remaining=""
port_in_use "$FRONTEND_PORT" && remaining="$remaining $FRONTEND_PORT"
port_in_use "$BACKEND_PORT"  && remaining="$remaining $BACKEND_PORT"
if [ -n "$remaining" ]; then
  log_warn "以下端口仍被占用:$remaining —— 请人工排查（lsof -nP -iTCP:<port> -sTCP:LISTEN）"
  exit 1
fi

cat <<EOF

$(log_step) 已停止
  第三方服务（mysql/redis/pg）保留运行
  如需停止第三方服务，请执行: ./stop_services.sh

EOF
