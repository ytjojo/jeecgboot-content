#!/usr/bin/env bash
# 04_start_frontend.sh - 启动 Vue3 前端（Vite）
# 前置：03_start_backend.sh 成功完成
# 缺 node_modules 时自动 pnpm install

set -u
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/env.sh
source "$SCRIPT_DIR/lib/env.sh"
# shellcheck source=lib/common.sh
source "$SCRIPT_DIR/lib/common.sh"

log_step "启动 Vue3 前端（Vite port=${FRONTEND_PORT}）"

# 前置：探测后端端口（不强制 spawn 03，避免与 start_all 重复）
# 04 既可独立跑（前端 dev 启动不依赖后端进程），也可被 start_all 调用
if port_in_use "$BACKEND_PORT"; then
  log_ok "后端端口 ${BACKEND_PORT} 已 LISTEN（假设 03 已运行）"
else
  log_warn "后端端口 ${BACKEND_PORT} 未 LISTEN —— 03 可能未运行"
  log_warn "前端启动后 API 请求会失败；如需后端请先跑 03_start_backend.sh"
fi

require_cmd pnpm
[ -d "$FRONTEND_DIR" ] || die "前端目录不存在: $FRONTEND_DIR"
[ -f "$FRONTEND_DIR/package.json" ] || die "前端 package.json 不存在: $FRONTEND_DIR/package.json"

# 1. 缺 node_modules 时自动 install
if [ ! -d "$FRONTEND_DIR/node_modules" ]; then
  log_warn "未发现 node_modules，开始 pnpm install（首次较慢）"
  cd "$FRONTEND_DIR" || die "无法进入 $FRONTEND_DIR"
  if pnpm install 2>&1 | tee -a "$INSTALL_LOG" | tail -20; then
    log_ok "pnpm install 完成"
  else
    die "pnpm install 失败，查看 $INSTALL_LOG"
  fi
else
  log_ok "node_modules 已存在，跳过 install"
fi

# 2. 杀旧前端
log_info "清理旧前端进程"
if port_in_use "$FRONTEND_PORT"; then
  old_pids=$(pid_on_port "$FRONTEND_PORT")
  for pid in $old_pids; do
    cmdline=$(ps -p "$pid" -o command= 2>/dev/null || true)
    log_warn "端口 $FRONTEND_PORT 被占用 pid=$pid cmd=${cmdline:0:80}"
    kill_pid "$pid" "port-${FRONTEND_PORT}"
  done
fi
kill_by_pattern "vite" "vite"
kill_pidfile "$FRONTEND_PID_FILE" "frontend(pidfile)"

for _ in 1 2 3 4 5; do
  if ! port_in_use "$FRONTEND_PORT"; then break; fi
  sleep 1
done
if port_in_use "$FRONTEND_PORT"; then
  die "端口 $FRONTEND_PORT 仍被占用，请人工排查"
fi
log_ok "端口 $FRONTEND_PORT 已释放"

# 3. 启动 Vite
cd "$FRONTEND_DIR" || die "无法进入 $FRONTEND_DIR"
: > "$FRONTEND_LOG"
log_info "后台启动: pnpm dev（日志: ${FRONTEND_LOG}）"
nohup pnpm dev > "$FRONTEND_LOG" 2>&1 &
frontend_pid=$!
write_pidfile "$FRONTEND_PID_FILE" "$frontend_pid"
log_ok "前端进程已 spawn，pid=$frontend_pid"

# 4. 等待健康
log_info "等待前端首页响应（最多 90s）: ${FRONTEND_HEALTH_URL}"
if wait_for_url "$FRONTEND_HEALTH_URL" 90; then
  log_ok "前端首页响应 2xx/3xx"
else
  log_error "前端 90s 内未响应"
  log_warn "查看日志: tail -n 80 $FRONTEND_LOG"
  exit 1
fi

# 5. 暴露访问信息
cat <<EOF

$(log_step) 前端启动完成
  本地访问:   http://localhost:${FRONTEND_PORT}
  后端 API:   ${BACKEND_DOC_URL}
  PID 文件:   ${FRONTEND_PID_FILE}
  日志文件:   ${FRONTEND_LOG}
  停止:      ./stop_all.sh  或  kill \$(cat ${FRONTEND_PID_FILE})

EOF
