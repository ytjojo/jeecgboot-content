---
name: openspec-fix-all-audit
description: Use when user says "修复审核问题", "fix audit", "处理 review 报告", "批量修复 change", or "/openspec-fix_all_audit". Scans and fixes issues from OpenSpec audit documents (review-report, drift-report, verify-report, verify, verification-review, backend-issues) across plan and fix phases.
license: MIT
compatibility: Requires openspec CLI and git worktree support
metadata:
  author: openspec
  version: "1.0"
  generatedBy: "1.4.1"
---

# openspec-fix_all_audit

自动修复 OpenSpec 审核文档中指出的问题。

## 触发判断

**触发**：用户说「修复审核文档」「fix audit」「处理 review 报告」「批量修复 change」，或指定了审核文档路径要求修复。

**不触发**：用户只是要求「review 代码」「审查某个文件」但未提到审核文档或批量修复。

## 子命令入口

```
/openspec-fix_all_audit plan <目标路径>    → plan 阶段
/openspec-fix_all_audit fix <fix-plan路径>  → fix 阶段
```

未指定子命令时，询问用户选择 plan 还是 fix。

---

# 🔴 第一层：核心约束（必须遵守，不可跳过）

> 本节是技能的最高优先级内容。所有 subagent 必须以本节为输出契约。

## FixItem 必须模板

每个 FixItem **必须**使用以下模板。**`**状态**: pending` 在标题后第一行 — 不可省略，不可放到末尾**。

```markdown
### {问题编号} - {问题简述}

**状态**: pending
**来源**: {审核文档文件名}
**位置**: {文件路径}:{行号（可选）}
**优先级**: {BLOCK/CRITICAL/FLAG/WARNING/SUGGESTION/ADVISORY}
**依赖**: {依赖的其他问题编号，无则填"无"}
**类型**: {文档修复 | 代码修复-前端 | 代码修复-后端}

**修复步骤**:
1. {具体步骤 1：文件路径 + 修改内容}
2. {具体步骤 2}

**验证方式**:
- {测试命令 / lint 检查 / 人工检查项}
```

> 完整模板含示例见 `templates/fix-plan.md`

## FixItem 必填字段自检清单

**subagent 完成 fix-plan.md 后，必须逐项自检，全部通过才能返回**：

| # | 字段 | 检查规则 |
|---|------|---------|
| 1 | `**状态**` | 始终为 `pending`。**位于标题后第一行，不在末尾** |
| 2 | `**来源**` | 审核文档文件名，可多个（逗号分隔） |
| 3 | `**位置**` | 具体到文件路径，尽量带行号 |
| 4 | `**优先级**` | 必须从 `BLOCK/CRITICAL/FLAG/WARNING/SUGGESTION/ADVISORY` 中选择 |
| 5 | `**依赖**` | 无依赖时填 `无`，不得留空 |
| 6 | `**类型**` | 必须从 `文档修复/代码修复-前端/代码修复-后端` 中选择 |
| 7 | `**修复步骤**` | 至少 1 条，每条包含具体文件路径和操作 |
| 8 | `**验证方式**` | 至少 1 种可执行的验证方法 |

**禁止**：
- 使用 `- **id**:`、`- **来源**:` 等列表格式代替 `**字段名**:` 格式
- 将 `**状态**` 放到 `**验证方式**` 之后
- 省略任何一个必填字段

## 全局硬规则

1. **所有文件变更在 git worktree 中进行**
2. **代码修复走 TDD**：先写测试 → 实现 → 重构
3. **修复后必须编译通过**
4. **plan 阶段的 subagent 只读**，不得修改任何文件
5. **fix 阶段的文件级分区是确定性逻辑**（字符串匹配文件路径分组），不交给模型判断
6. **dispatch subagent 时必须显式告知**：来源分支、worktree 路径、ownership 类型
7. **遵循 AGENTS.md 中所有 13 条 Agent 行为规范**

---

# 📋 第二层：子命令流程

## `plan` 子命令

**功能**：扫描审核文档，生成细粒度修复计划。

**流程概述**：
1. 解析输入 → ChangePair 列表（过滤 archived + 已完成，前后端成对配对）
2. 每个 ChangePair 启动只读 subagent → 扫描 6 类审核文档 → 解析 Issue → 按上述模板生成 FixItem → 执行自检清单
3. 主 agent 汇总 → 跨 pair 冲突检测 → 全局排序
4. 输出 fix-plan → 等待用户确认

> **详细步骤**：`references/plan-workflow.md`
> **输出格式**：`templates/fix-plan.md`

## `fix` 子命令

**功能**：执行已确认的修复计划。

**流程概述**：
1. 读取 fix-plan → 按文件路径确定性分组
2. 创建共享 worktree
3. 每个文件组启动 fix subagent 并行执行 → TDD/TDD 修复 → 更新 FixItem 状态
4. 独立 Code Review
5. 主 agent 汇总：全部 done → 自动 commit；关键失败 → 暂停等决策
6. 生成复盘报告
7. 清理 worktree

> **详细步骤**：`references/fix-workflow.md`
> **复盘模板**：`templates/retrospective.md`

---

# 📖 第三层：参考细节（按需查阅）

| 内容 | 文件 |
|------|------|
| plan 子命令完整步骤 | `references/plan-workflow.md` |
| fix 子命令完整步骤 | `references/fix-workflow.md` |
| 跨 change 处理、FixItem 合并规则、上下文策略、Worktree 管理、失败处理 | `references/edge-cases.md` |
| FixItem 模板 + fix-plan.md 输出格式 + 示例 | `templates/fix-plan.md` |
| 复盘报告模板 | `templates/retrospective.md` |
