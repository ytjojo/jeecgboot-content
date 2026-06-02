#!/usr/bin/env bash
# common.sh - 通用函数库：颜色日志、端口探测、进程管理、健康等待

# 防止重复 source
if [ -n "${__JEECG_SCRIPT_COMMON_LOADED:-}" ]; then
  return 0 2>/dev/null || true
fi
__JEECG_SCRIPT_COMMON_LOADED=1

# ========== 颜色 ==========
if [ -t 1 ]; then
  C_RED='\033[0;31m'
  C_GREEN='\033[0;32m'
  C_YELLOW='\033[0;33m'
  C_BLUE='\033[0;34m'
  C_CYAN='\033[0;36m'
  C_BOLD='\033[1m'
  C_RESET='\033[0m'
else
  C_RED=''; C_GREEN=''; C_YELLOW=''; C_BLUE=''; C_CYAN=''; C_BOLD=''; C_RESET=''
fi

# ========== 日志 ==========
_ts() { date '+%Y-%m-%d %H:%M:%S'; }

log_info()  { printf "${C_BLUE}[%s] [INFO]${C_RESET}  %s\n"  "$(_ts)" "$*"; }
log_warn()  { printf "${C_YELLOW}[%s] [WARN]${C_RESET}  %s\n"  "$(_ts)" "$*"; }
log_error() { printf "${C_RED}[%s] [ERROR]${C_RESET} %s\n"    "$(_ts)" "$*" >&2; }
log_ok()    { printf "${C_GREEN}[%s] [OK]${C_RESET}    %s\n"   "$(_ts)" "$*"; }
log_step()  { printf "${C_BOLD}${C_CYAN}==>${C_RESET} %s\n"  "$*"; }

die() {
  log_error "$*"
  exit 1
}

# ========== 命令探测 ==========
require_cmd() {
  local cmd="$1"
  local hint="${2:-}"
  command -v "$cmd" >/dev/null 2>&1 || die "缺少必要命令: $cmd${hint:+ ($hint)}"
}

# ========== 端口与进程 ==========
# 端口是否被占用（TCP LISTEN） 0=占用 1=空闲
port_in_use() {
  local port="$1"
  lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1
}

# 取占用端口的 PID（多进程行分隔输出）
pid_on_port() {
  local port="$1"
  lsof -nP -iTCP:"$port" -sTCP:LISTEN -t 2>/dev/null
}

# 等待端口可用（服务起来），超时秒数
wait_for_port() {
  local host="$1" port="$2" timeout="${3:-30}"
  local i=0
  while [ "$i" -lt "$timeout" ]; do
    if (echo > "/dev/tcp/$host/$port") >/dev/null 2>&1; then
      return 0
    fi
    sleep 1
    i=$((i + 1))
  done
  return 1
}

# 等待 URL 2xx/3xx
wait_for_url() {
  local url="$1" timeout="${2:-60}"
  local i=0
  while [ "$i" -lt "$timeout" ]; do
    local code
    code=$(curl -s -o /dev/null -w '%{http_code}' --max-time 3 "$url" 2>/dev/null || echo "000")
    case "$code" in
      2*|3*) return 0 ;;
    esac
    sleep 1
    i=$((i + 1))
  done
  return 1
}

# 优雅杀进程：TERM 等 5s → KILL
kill_pid() {
  local pid="$1" name="${2:-process}"
  if [ -z "$pid" ]; then return 0; fi
  if ! kill -0 "$pid" 2>/dev/null; then return 0; fi
  log_info "停止 $name (pid=$pid) SIGTERM"
  kill -15 "$pid" 2>/dev/null || true
  for _ in 1 2 3 4 5; do
    kill -0 "$pid" 2>/dev/null || return 0
    sleep 1
  done
  log_warn "$name 未响应 SIGTERM，发送 SIGKILL"
  kill -9 "$pid" 2>/dev/null || true
}

# 按 pidfile 停止
kill_pidfile() {
  local pidfile="$1" name="${2:-process}"
  [ -f "$pidfile" ] || return 0
  local pid
  pid=$(cat "$pidfile" 2>/dev/null || true)
  if [ -n "$pid" ]; then
    kill_pid "$pid" "$name"
  fi
  rm -f "$pidfile"
}

# 按 cmdline 关键字杀进程（如 spring-boot:run / vite）
kill_by_pattern() {
  local pattern="$1" name="${2:-process}"
  local pids
  pids=$(pgrep -f "$pattern" 2>/dev/null || true)
  if [ -z "$pids" ]; then
    return 0
  fi
  for pid in $pids; do
    kill_pid "$pid" "$name"
  done
}

# 写 PID 文件
write_pidfile() {
  local pidfile="$1" pid="$2"
  echo "$pid" > "$pidfile"
}

# ========== 简易确认 ==========
# y/N 询问（默认 N）
confirm() {
  local prompt="${1:-Continue?}"
  local ans
  read -r -p "$(printf "${C_YELLOW}%s [y/N]:${C_RESET} " "$prompt")" ans
  case "$ans" in
    [Yy]|[Yy][Ee][Ss]) return 0 ;;
    *) return 1 ;;
  esac
}
