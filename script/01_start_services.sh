#!/usr/bin/env bash
# 01_start_services.sh - 启动第三方服务（mysql / redis / postgresql@17）
# 幂等：端口已占用或 brew services 已 started 均判为已就绪

set -u
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/env.sh
source "$SCRIPT_DIR/lib/env.sh"
# shellcheck source=lib/common.sh
source "$SCRIPT_DIR/lib/common.sh"

[ "$(uname -s)" = "Darwin" ] || die "本脚本仅支持 macOS"
require_cmd brew
require_cmd lsof

log_step "启动第三方服务（mysql / redis / postgresql@17）"

# ========== MySQL ==========
log_info "检查 MySQL (${MYSQL_HOST}:${MYSQL_PORT})"
if port_in_use "$MYSQL_PORT"; then
  log_ok "MySQL 已在运行（端口 $MYSQL_PORT 被占用）"
else
  if ! brew list --formula 2>/dev/null | grep -qx "mysql"; then
    log_warn "MySQL 未安装，请先执行 ./00_genesis.sh"
    exit 0
  fi
  log_info "brew services start mysql"
  brew services start mysql >> "$INSTALL_LOG" 2>&1 || die "MySQL 启动失败，查看 $INSTALL_LOG"
  if wait_for_port "$MYSQL_HOST" "$MYSQL_PORT" 30; then
    log_ok "MySQL 端口已就绪"
  else
    die "MySQL 30s 内未监听 $MYSQL_PORT"
  fi
fi

# MySQL 客户端可达性 + 鉴权
log_info "验证 MySQL 鉴权 (user=$MYSQL_USER)"
if MYSQL_PWD="$MYSQL_PASS" mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -e 'SELECT VERSION();' >/dev/null 2>&1; then
  local_ver=$(MYSQL_PWD="$MYSQL_PASS" mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -N -B -e 'SELECT VERSION();' 2>/dev/null)
  log_ok "MySQL 鉴权通过 (version=$local_ver)"
else
  die "MySQL 鉴权失败，请检查账号密码（user=${MYSQL_USER}）"
fi

# ========== Redis ==========
log_info "检查 Redis (${REDIS_HOST}:${REDIS_PORT})"
if port_in_use "$REDIS_PORT"; then
  log_ok "Redis 已在运行（端口 $REDIS_PORT 被占用）"
else
  if ! brew list --formula 2>/dev/null | grep -qx "redis"; then
    log_warn "Redis 未安装，请先执行 ./00_genesis.sh"
    exit 0
  fi
  log_info "brew services start redis"
  brew services start redis >> "$INSTALL_LOG" 2>&1 || die "Redis 启动失败，查看 $INSTALL_LOG"
  if wait_for_port "$REDIS_HOST" "$REDIS_PORT" 15; then
    log_ok "Redis 端口已就绪"
  else
    die "Redis 15s 内未监听 $REDIS_PORT"
  fi
fi

# Redis ping
if command -v redis-cli >/dev/null 2>&1; then
  if pong=$(redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" ping 2>/dev/null); then
    [ "$pong" = "PONG" ] && log_ok "Redis ping: PONG" || log_warn "Redis ping 返回: $pong"
  else
    log_warn "redis-cli ping 失败（无密码或鉴权差异，跳过）"
  fi
fi

# ========== PostgreSQL（可选） ==========
log_info "检查 PostgreSQL (${PG_HOST}:${PG_PORT}) [可选，AI RAG 依赖]"
if port_in_use "$PG_PORT"; then
  log_ok "PostgreSQL 已在运行（端口 $PG_PORT 被占用）"
else
  if ! brew services list 2>/dev/null | grep -q "postgresql@17"; then
    log_warn "PostgreSQL 未安装（可选）—— 跳过；如需 AI RAG 模块请执行: brew install postgresql@17 && brew services start postgresql@17"
  else
    log_info "brew services start postgresql@17"
    brew services start postgresql@17 >> "$INSTALL_LOG" 2>&1 || log_warn "postgresql@17 启动失败，AI RAG 模块可能不可用"
    wait_for_port "$PG_HOST" "$PG_PORT" 15 || log_warn "PostgreSQL 15s 内未就绪（AI RAG 模块可能不可用）"
  fi
fi

log_step "第三方服务启动检查完成"
