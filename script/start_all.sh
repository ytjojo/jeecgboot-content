#!/usr/bin/env bash
# start_all.sh - 一键全栈启动（按 00→04 顺序，幂等）
# 等价于：依次执行 00_genesis.sh / 01_start_services.sh / 02_init_db.sh / 03_start_backend.sh / 04_start_frontend.sh

set -u
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# shellcheck source=lib/env.sh
source "$SCRIPT_DIR/lib/env.sh"
# shellcheck source=lib/common.sh
source "$SCRIPT_DIR/lib/common.sh"

log_step "一键启动 JeecgBoot 全栈（macOS / dev profile）"

# 链路：每个脚本可独立运行；这里串联起来即可
"$SCRIPT_DIR/00_genesis.sh"     || die "00_genesis.sh 失败"
"$SCRIPT_DIR/01_start_services.sh" || die "01_start_services.sh 失败"
"$SCRIPT_DIR/02_init_db.sh"        || die "02_init_db.sh 失败"
"$SCRIPT_DIR/03_start_backend.sh"  || die "03_start_backend.sh 失败"
"$SCRIPT_DIR/04_start_frontend.sh" || die "04_start_frontend.sh 失败"

cat <<EOF

$(log_step) 全栈启动完成
  后端:   http://localhost:${BACKEND_PORT}${BACKEND_CONTEXT}
  文档:   ${BACKEND_DOC_URL}
  前端:   http://localhost:${FRONTEND_PORT}
  停止:   ./stop_all.sh
  日志:   ${LOG_DIR}

EOF
