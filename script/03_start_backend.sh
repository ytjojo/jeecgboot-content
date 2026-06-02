#!/usr/bin/env bash
# 03_start_backend.sh - 启动 Java 后端（spring-boot:run -P dev 后台化）
# 前置：02_init_db.sh 成功完成

set -u
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/env.sh
source "$SCRIPT_DIR/lib/env.sh"
# shellcheck source=lib/common.sh
source "$SCRIPT_DIR/lib/common.sh"

log_step "启动 Java 后端（profile=${MVN_PROFILE}, port=${BACKEND_PORT}）"

# 前置依赖
"$SCRIPT_DIR/02_init_db.sh"

require_cmd mvn
require_cmd java
[ -d "$START_MODULE" ] || die "启动模块不存在: $START_MODULE"

# 1. 杀旧后端
log_info "清理旧后端进程"
if port_in_use "$BACKEND_PORT"; then
  old_pids=$(pid_on_port "$BACKEND_PORT")
  for pid in $old_pids; do
    cmdline=$(ps -p "$pid" -o command= 2>/dev/null || true)
    log_warn "端口 $BACKEND_PORT 被占用 pid=$pid cmd=${cmdline:0:80}"
    kill_pid "$pid" "port-${BACKEND_PORT}"
  done
fi
# 通过关键字兜底（spring-boot:run 进程 + JeecgSystemApplication）
kill_by_pattern "spring-boot:run" "spring-boot:run"
kill_by_pattern "JeecgSystemApplication" "JeecgSystemApplication"
kill_pidfile "$BACKEND_PID_FILE" "backend(pidfile)"

# 等待端口彻底释放
for _ in 1 2 3 4 5; do
  if ! port_in_use "$BACKEND_PORT"; then break; fi
  sleep 1
done
if port_in_use "$BACKEND_PORT"; then
  die "端口 $BACKEND_PORT 仍被占用，请人工排查"
fi
log_ok "端口 $BACKEND_PORT 已释放"

# 2. 启动后端
log_info "切换工作目录: $START_MODULE"
cd "$START_MODULE" || die "无法进入 $START_MODULE"

# 构造启动命令
MVN_CMD=(mvn
  spring-boot:run
  -P "$MVN_PROFILE"
  -Dspring-boot.run.main-class="$MAIN_CLASS"
  -Dspring-boot.run.fork=false
)

log_info "后台启动: ${MVN_CMD[*]}"
log_info "日志输出: $BACKEND_LOG"
: > "$BACKEND_LOG"  # truncate
nohup "${MVN_CMD[@]}" > "$BACKEND_LOG" 2>&1 &
backend_pid=$!
write_pidfile "$BACKEND_PID_FILE" "$backend_pid"
log_ok "后端进程已 spawn，pid=$backend_pid"

# 3. 等待启动完成（两阶段：端口 LISTEN → banner 输出）
log_info "等待后端启动（最多 ${BACKEND_BOOT_TIMEOUT}s）"

# 阶段 1：Tomcat 端口开始监听（说明 Spring 容器已开始启动）
if ! wait_for_port "127.0.0.1" "$BACKEND_PORT" "$BACKEND_BOOT_TIMEOUT"; then
  log_error "后端在 ${BACKEND_BOOT_TIMEOUT}s 内未监听端口 $BACKEND_PORT"
  log_warn "查看日志: tail -n 80 $BACKEND_LOG"
  exit 1
fi
log_ok "端口 $BACKEND_PORT 已 LISTEN（Tomcat 已起）"

# 阶段 2：扫描日志等到 Spring Boot 完全启动（banner 关键字）
# 注意：仅在端口 LISTEN 之后的内容中匹配，避免旧日志干扰
log_info "等待 Spring Boot 启动完成（日志关键字: \"${BACKEND_READY_PATTERN}\"）"
if wait_for_log "$BACKEND_LOG" "$BACKEND_READY_PATTERN" "$BACKEND_BOOT_TIMEOUT"; then
  log_ok "后端启动完成（banner 已输出）"
else
  log_error "后端在 ${BACKEND_BOOT_TIMEOUT}s 内未完成启动（未出现 banner）"
  log_warn "查看日志: tail -n 80 $BACKEND_LOG"
  exit 1
fi

# 阶段 3（冒烟）：访问 doc.html —— OAuth2 白名单可能 200/401 都算活
# 注：此步仅打印结果，不作为启动判据
code=$(curl -s -o /dev/null -w '%{http_code}' --max-time 5 "$BACKEND_DOC_URL" 2>/dev/null || echo "000")
log_info "冒烟探测 ${BACKEND_DOC_URL} -> HTTP ${code}"

# 4. 暴露访问信息
cat <<EOF

$(log_step) 后端启动完成
  本地访问:   http://localhost:${BACKEND_PORT}${BACKEND_CONTEXT}
  Knife4j 文档: ${BACKEND_DOC_URL}
  PID 文件:   ${BACKEND_PID_FILE}
  日志文件:   ${BACKEND_LOG}
  停止:      ./stop_all.sh  或  kill \$(cat ${BACKEND_PID_FILE})

EOF
