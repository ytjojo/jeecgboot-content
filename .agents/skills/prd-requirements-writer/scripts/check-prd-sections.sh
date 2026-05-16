#!/usr/bin/env bash

# 用法: ./scripts/check-prd-sections.sh <prd.md 路径>
# 作用: 检查 PRD Markdown 是否包含关键二级标题，输出缺失节供评审模式合并到「必改」。

set -u

FILE="${1:-}"

if [ -z "$FILE" ] || [ ! -f "$FILE" ]; then
  echo "Usage: $0 <path-to-prd.md>"
  exit 1
fi

echo "=== PRD 必选节检查 ==="

required_sections=(
  "文档信息"
  "背景与问题"
  "目标与成功指标"
  "范围"
  "用户与场景"
  "功能需求"
  "验收标准"
)

for section in "${required_sections[@]}"; do
  if grep -qE "^##[[:space:]]*([0-9]+\\.[[:space:]]*)?${section}" "$FILE" 2>/dev/null; then
    echo "✓ ${section}"
  else
    echo "✗ 缺失: ${section}"
  fi
done
