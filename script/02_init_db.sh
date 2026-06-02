#!/usr/bin/env bash
# 02_init_db.sh - 创建 jeecg-boot 库 + 导入全量 SQL（幂等）
# 注意：项目 dev profile 下 spring.flyway.enabled=false，故不依赖 Flyway

set -u
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/env.sh
source "$SCRIPT_DIR/lib/env.sh"
# shellcheck source=lib/common.sh
source "$SCRIPT_DIR/lib/common.sh"

log_step "数据库初始化（MySQL: ${MYSQL_HOST}:${MYSQL_PORT} / db=${MYSQL_DB}）"

# 前置：确保第三方服务在跑
"$SCRIPT_DIR/01_start_services.sh"

require_cmd mysql
[ -f "$FULL_SQL" ] || die "全量 SQL 不存在: $FULL_SQL"

# 1. 库是否存在
db_exists=$(MYSQL_PWD="$MYSQL_PASS" mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -N -B -e \
  "SELECT COUNT(*) FROM information_schema.SCHEMATA WHERE SCHEMA_SCHEMA='${MYSQL_DB}';" 2>/dev/null || echo "0")

if [ "${db_exists:-0}" -ge 1 ]; then
  log_ok "库 ${MYSQL_DB} 已存在"
else
  log_info "创建库 ${MYSQL_DB}"
  MYSQL_PWD="$MYSQL_PASS" mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" \
    -e "CREATE DATABASE IF NOT EXISTS \`${MYSQL_DB}\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;" \
    2>&1 | tee -a "$INSTALL_LOG" || die "创建库失败"
  log_ok "库 ${MYSQL_DB} 创建成功"
fi

# 2. 探测表是否已初始化（用 sys_user 作为业务标记）
table_exists=$(MYSQL_PWD="$MYSQL_PASS" mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -N -B \
  -e "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='${MYSQL_DB}' AND TABLE_NAME='${MYSQL_BOOTSTRAP_TABLE}';" 2>/dev/null || echo "0")

if [ "${table_exists:-0}" -ge 1 ]; then
  table_count=$(MYSQL_PWD="$MYSQL_PASS" mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -N -B \
    -e "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='${MYSQL_DB}';" 2>/dev/null || echo "?")
  log_ok "库已初始化（标志性表 ${MYSQL_BOOTSTRAP_TABLE} 已存在，共 ${table_count} 张表）—— 跳过全量导入"
else
  # 3. 导入全量 SQL
  sql_size=$(du -h "$FULL_SQL" | awk '{print $1}')
  log_info "开始导入全量 SQL: $FULL_SQL (${sql_size})，首次较慢请耐心等待"
  start_ts=$(date +%s)
  if MYSQL_PWD="$MYSQL_PASS" mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" \
        --default-character-set=utf8mb4 \
        "$MYSQL_DB" < "$FULL_SQL" 2>>"$INSTALL_LOG"; then
    end_ts=$(date +%s)
    cost=$((end_ts - start_ts))
    log_ok "全量 SQL 导入完成，耗时 ${cost}s"
  else
    die "全量 SQL 导入失败，查看 $INSTALL_LOG 末尾"
  fi

  # 4. 校验
  table_count=$(MYSQL_PWD="$MYSQL_PASS" mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -N -B \
    -e "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='${MYSQL_DB}';" 2>/dev/null || echo "0")
  if [ "${table_count:-0}" -ge 1 ]; then
    log_ok "库内表数量: ${table_count}"
  else
    die "导入完成但未发现任何表，请检查 $INSTALL_LOG"
  fi
fi

log_step "数据库初始化完成"
