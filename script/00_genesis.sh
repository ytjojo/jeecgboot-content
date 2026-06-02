#!/usr/bin/env bash
# 00_genesis.sh - 创世纪：检测并安装本机缺失的依赖服务
# 范围：macOS only，依赖 Homebrew
# 必装：mysql、redis；可选：postgresql@17（AI RAG 向量库）

set -u
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/env.sh
source "$SCRIPT_DIR/lib/env.sh"
# shellcheck source=lib/common.sh
source "$SCRIPT_DIR/lib/common.sh"

log_step "创世纪：环境依赖检测（macOS / Homebrew）"

# 平台守卫
[ "$(uname -s)" = "Darwin" ] || die "本脚本仅支持 macOS，当前: $(uname -s)"

# 1. Homebrew
require_cmd brew "请先安装 Homebrew: https://brew.sh"
log_ok "Homebrew: $(brew --version | head -1)"

# 2. 工具链（脚本自身依赖）
for cmd in lsof mysql redis-cli pnpm mvn java; do
  if command -v "$cmd" >/dev/null 2>&1; then
    log_ok "已安装: $cmd -> $(command -v "$cmd")"
  else
    log_warn "未安装: $cmd（运行服务时按需补装）"
  fi
done

# 3. 必装 brew 公式
install_if_missing() {
  local formula="$1"
  if brew list --formula 2>/dev/null | grep -qx "$formula"; then
    log_ok "brew 已安装: $formula"
  else
    log_info "正在安装: $formula （首次安装可能耗时较长）"
    if brew install "$formula" 2>&1 | tee -a "$INSTALL_LOG" | tail -5; then
      log_ok "安装完成: $formula"
    else
      die "安装失败: $formula，请查看 $INSTALL_LOG"
    fi
  fi
}

for formula in "${BREW_REQUIRED[@]}"; do
  install_if_missing "$formula"
done

# 4. 可选 brew 公式（postgresql@17 用于 AI RAG，缺失时仅警告）
for formula in "${BREW_OPTIONAL[@]}"; do
  if brew list --formula 2>/dev/null | grep -qx "$formula"; then
    log_ok "brew 已安装: $formula"
  else
    log_warn "未安装（可选）: $formula —— 跳过；如需 AI RAG 模块请运行: brew install $formula"
  fi
done

log_step "环境就绪：必装项已全部就位"
cat <<EOF

后续步骤:
  ./01_start_services.sh    # 启动 mysql/redis/pg
  ./02_init_db.sh           # 创建库 + 导入全量 SQL
  ./03_start_backend.sh     # 启动 Java 后端
  ./04_start_frontend.sh    # 启动 Vue3 前端
或一键执行:
  ./start_all.sh

EOF
