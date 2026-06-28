# CONTEXT — openspec-fix_all_audit 领域术语表

> 本文档是领域术语的精确定义，不含实现细节。术语一旦确定，所有设计文档和代码必须一致使用。

## 核心实体

| 术语 | 定义 |
|------|------|
| **Change** | OpenSpec 中的一个变更单元，对应 `openspec/changes/{name}/` 目录。包含 proposal.md、design.md、specs/、tasks.md 等规范文档。后端 change 无后缀（如 `user-03-badges-points-growth`），前端 change 带 `_frontend` 后缀（如 `user-03-badges-points-growth_frontend`）。前后端 change 通过 `change-prd-mapping.yaml` 配对。 |
| **ChangePair** | 一个后端 Change 与其配对的前端 Change（如有）的组合。是 plan 阶段 subagent 的基本工作单元。无配对时单独成为一个 ChangePair。 |
| **审核报告 (AuditReport)** | 6 类审核流程产出的 Markdown 文档，包含对规范文档或代码实现的审核发现。 |
| **发现问题 (Issue)** | 审核报告中记录的单条问题，包含：编号、位置（文件路径 + 行号）、问题描述、影响、修复建议。 |
| **修复项 (FixItem)** | fix-plan 中的基本执行单元。一个 Issue 可映射为多个 FixItem（1:N）；多个同类 Issue 可合并为一个 FixItem（N:1，按实际情况判断）。每个 FixItem 包含：文件路径、修改内容、依赖关系、验证方式、状态。 |
| **修复计划 (FixPlan)** | 单个 ChangePair 内所有 FixItem 的有序集合。输出为各 change 目录下的 `fix-plan.md`。批量模式下额外生成全局汇总版 `openspec/changes/fix-plan.md`。 |

## 审核报告类型

| 文件模式 | 审核类型 | 问题分级 |
|---------|---------|---------|
| `review-report-*.md` | 规范文档审核 | BLOCK / FLAG / ADVISORY |
| `drift-report-*.md` | 代码漂移检测 | CRITICAL / WARNING |
| `verify-report-*.md` | 实现验证 | CRITICAL / WARNING / SUGGESTION |
| `verify.md` | 实现验证（简版） | 按维度评估 |
| `verification-review.md` | 验证审核 | 按维度评估 |
| `backend-issues.md` | 后端遗留问题 | P0 / P1 |

## 同步策略（漂移修复方向）

| 策略 | 含义 | 判断依据 |
|------|------|---------|
| **改文档** | 代码实现正确，文档描述过时 | 以 proposal/design 中的设计意图为准 |
| **改代码** | 文档规范正确，代码实现偏离 | 考虑代码实际可运行性和测试覆盖 |
| **双向调整** | 双方都有问题 | 优先保持向后兼容 |

## 过滤规则

| 规则 | 适用阶段 |
|------|---------|
| 已归档的 change（mapping 中 `status: archived`）不参与扫描 | plan |
| 审核文档中所有 Issue 均已标记为 done/completed 的，不再处理 | plan |
| 与代码实现无关的问题（如 git 操作、流程规范）被忽略 | plan |

## 子命令

| 命令 | 职责 | 输入 | 输出 |
|------|------|------|------|
| **plan** | 扫描审核文档，生成修复计划 | Change 路径 或 changes 根目录 | fix-plan.md（各 change + 可选的全局汇总版） |
| **fix** | 按依赖顺序执行所有修复项 | 已确认的 fix-plan.md | 已修复的代码/文档 + 状态更新 + 复盘报告 |
