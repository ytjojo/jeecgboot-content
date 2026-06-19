# 同步审核报告模板

> **单文件输出**。漂移检测 + 架构审核合并为一份完整报告。

```markdown
# 同步审核报告: {change-name}

> 生成时间: {timestamp}
> Change 类型: {change-type}
> 配对 Change: {paired-change}
> 检测范围: {scope-description}

---

## 门禁判定

| 判定 | 状态 |
|------|------|
| **门禁结果** | ✅ 通过 / ❌ 阻断 |

{gate-details}

---

## 得分总览

| 维度 | 检查项 | CRITICAL | WARNING | SUGGESTION | 得分 | 得分率 |
|------|--------|----------|---------|------------|------|--------|
{all-dimension-scores}

| 领域 | 满分 | 得分 | 得分率 |
|------|------|------|--------|
| 漂移检测（后端） | {backend-drift-max} | {backend-drift-score} | {backend-drift-pct}% |
| 漂移检测（前端） | {frontend-drift-max} | {frontend-drift-score} | {frontend-drift-pct}% |
| 架构审核 | {arch-max} | {arch-score} | {arch-pct}% |
| **综合** | **{total-max}** | **{total-score}** | **{total-pct}%** |

---

## 第一部分：漂移检测

### 概览

| # | 类别 | 漂移类型 | 严重级别 | 规范描述 | 实际实现 | 评价 | 修复方向 |
|---|------|---------|---------|---------|---------|------|---------|
{drift-issue-rows}

### 后端漂移详情

#### B-1: API 端点对比

| # | 端点 | 严重级别 | 文档描述 | 实际实现 | 差异 | 修复方向 |
|---|------|---------|---------|---------|------|---------|
{api-comparison-rows}

#### B-2: VO/DTO 字段完整性

| # | VO 类 | 严重级别 | spec 字段 | 实际字段 | 赋值状态 | 差异 |
|---|------|---------|----------|---------|---------|------|
{vo-field-rows}

#### B-3: 业务规则覆盖

| # | Scenario | 覆盖级别 | 期望行为 | 实际代码 | 缺失描述 |
|---|----------|---------|---------|---------|---------|
{scenario-coverage-rows}

#### B-4: 设计决策遵循

| # | 决策 | 判定 | 决策内容 | 实际实现 | 建议 |
|---|------|------|---------|---------|------|
{design-decision-rows}

#### B-5: 数据一致性

| # | 检查项 | 严重级别 | 文档值 | 代码值 | 差异 |
|---|--------|---------|--------|--------|------|
{data-consistency-rows}

#### B-6: 上下游引用

| # | 引用对象 | 严重级别 | 引用描述 | 问题 |
|---|---------|---------|---------|------|
{reference-rows}

### 前端漂移详情

#### F-1: 完整性检查

{frontend-completeness}

#### F-2: 前后端接口一致性

| # | 前端调用 | 后端端点 | 严重级别 | 差异 |
|---|---------|---------|---------|------|
{api-consistency-rows}

#### F-3: VO/DTO 字段级对齐

| # | 前端类型 | 后端 VO 字段 | 严重级别 | 差异 |
|---|---------|-------------|---------|------|
{vo-alignment-rows}

#### F-4: 设计决策有效性

| # | 决策 | 实际实现 | 判定 | 建议 |
|---|------|---------|------|------|
{frontend-design-rows}

#### F-5: 降级策略验证

| # | 降级项 | 后端字段状态 | 前端降级实现 | 可行性 |
|---|--------|-------------|-------------|--------|
{degradation-rows}

### 漂移类型分布

| 漂移类型 | 数量 | 占比 |
|---------|------|------|
| 负向漂移（代码落后文档） | {negative-count} | {negative-pct}% |
| 正向漂移（代码超越文档） | {positive-count} | {positive-pct}% |
| 冲突漂移（双向矛盾） | {conflict-count} | {conflict-pct}% |
| 完成度漂移（代码缺失） | {completeness-count} | {completeness-pct}% |

### 同步方案

#### 需要更新文档 ({update-doc-count} 项)
{update-doc-items}

#### 需要修复代码 ({fix-code-count} 项)
{fix-code-items}

#### 需要人工确认 ({manual-confirm-count} 项)
{manual-confirm-items}

### 根因分析

| 根因类别 | 数量 | 占比 |
|---------|------|------|
| 文档问题（写错/模糊/过时） | {doc-cause-count} | {doc-cause-pct}% |
| 代码问题（未看文档/理解偏差/重构未更新） | {code-cause-count} | {code-cause-pct}% |
| 流程问题（未通知/缺 Review） | {process-cause-count} | {process-cause-pct}% |

---

## 第二部分：架构审核

### 概览

| # | 维度 | 检查项 | 严重级别 | 位置 | 问题描述 | 修复建议 |
|---|------|--------|---------|------|---------|---------|
{arch-issue-rows}

### 维度 A: 分层架构合规性

| # | 问题 | 位置 | 严重级别 | 描述 | 修复建议 |
|---|------|------|---------|------|---------|
{layer-compliance-rows}

**调用链分析**: {call-chain-analysis}

### 维度 B: 模块边界隔离

| # | 问题 | 位置 | 严重级别 | 跨模块引用 | 修复建议 |
|---|------|------|---------|-----------|---------|
{module-boundary-rows}

### 维度 C: 依赖方向正确性

| # | 问题 | 位置 | 严重级别 | 依赖关系 | 修复建议 |
|---|------|------|---------|---------|---------|
{dependency-direction-rows}

### 维度 D: 命名与组织规范

| # | 问题 | 位置 | 严重级别 | 描述 | 规范要求 |
|---|------|------|---------|------|---------|
{naming-rows}

### 维度 E: 过度工程化检测

| # | 问题 | 位置 | 严重级别 | 描述 | 建议 |
|---|------|------|---------|------|------|
{over-engineering-rows}

### 维度 F: 安全架构基线

| # | 问题 | 位置 | 严重级别 | 风险描述 | 修复建议 |
|---|------|------|---------|---------|---------|
{security-rows}

### 维度 G: 可观测性架构

| # | 问题 | 位置 | 严重级别 | 描述 | 修复建议 |
|---|------|------|---------|------|---------|
{observability-rows}

### 架构健康度

| 指标 | 当前值 | 目标 | 状态 |
|------|--------|------|------|
| 分层合规率 | {layer-compliance}% | 100% | {layer-status} |
| 模块隔离度 | {module-isolation}% | 100% | {module-status} |
| 循环依赖数 | {circular-deps} | 0 | {circular-status} |
| 命名规范率 | {naming-compliance}% | 100% | {naming-status} |
| 安全基线通过率 | {security-compliance}% | 100% | {security-status} |

---

## 问题合并统计

| 严重级别 | 漂移检测 | 架构审核 | 合计 |
|---------|---------|---------|------|
| CRITICAL | {drift-critical} | {arch-critical} | {total-critical} |
| WARNING | {drift-warning} | {arch-warning} | {total-warning} |
| SUGGESTION | {drift-suggestion} | {arch-advisory} | {total-suggestion} |
| **合计** | {drift-total} | {arch-total} | {total-all} |

---

## 阻断问题 (CRITICAL)

{blocking-issues}

> 以上问题修复后可重新验证。

---

## 修复优先级

### P0 - 立即修复 ({p0-count} 项)
{p0-items}

### P1 - 本 Sprint 内 ({p1-count} 项)
{p1-items}

### P2 - Tech Debt ({p2-count} 项)
{p2-items}

---

## 量化指标

| 指标 | 当前值 | 目标值 | 状态 |
|------|--------|--------|------|
| 文档准确率 | {accuracy}% | ≥ 95% | {accuracy-status} |
| CRITICAL 漂移数 | {critical} | 0 | {critical-status} |
| 分层合规率 | {layer-compliance}% | 100% | {layer-status} |
| 循环依赖数 | {circular-deps} | 0 | {circular-status} |
| 安全基线通过率 | {security-compliance}% | 100% | {security-status} |

---

## 后续步骤

- [ ] 修复 CRITICAL 问题
- [ ] 重新运行 `openspec-code-drift-sync`
- [ ] 门禁通过后归档 change
- [ ] 将 WARNING 问题加入 Backlog
```
