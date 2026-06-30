---
name: openspec-fix-all-audit
description: 自动扫描并修复 OpenSpec 审核文档中发现的全部问题。支持 plan（生成修复计划）和 fix（执行修复）两个子命令。覆盖 6 类审核文档（review-report、drift-report、verify-report、verify、verification-review、backend-issues）。当用户提到"修复审核问题"、"fix audit"、"处理 review 报告"、"批量修复 change"、"/openspec-fix_all_audit"时使用此技能。
license: MIT
compatibility: Requires openspec CLI and git worktree support
metadata:
  author: openspec
  version: "1.0"
  generatedBy: "1.4.1"
---

# openspec-fix_all_audit

自动修复 OpenSpec 审核文档中指出的问题，覆盖从计划生成到执行修复的完整流程。

## 触发判断

**触发**：用户说「修复审核文档」「fix audit」「处理 review 报告」「批量修复 change」，或指定了审核文档路径要求修复。

**不触发**：用户只是要求「review 代码」「审查某个文件」但未提到审核文档或批量修复。

## 子命令入口

用户通过参数指定子命令：

```
/openspec-fix_all_audit plan <目标路径>    → plan 阶段
/openspec-fix_all_audit fix <fix-plan路径>  → fix 阶段
```

如果用户未指定子命令，询问：需要先 `plan` 生成修复计划，还是直接 `fix` 执行已有计划？

---

# 🔴 第一层：核心约束（必须遵守，不可跳过）

> **阅读顺序**：本节是技能的最高优先级内容。Plan 和 Fix 阶段的所有 subagent 都必须以本节为输出契约。

## FixItem 必须模板

每个 FixItem **必须**使用以下统一模板。`**状态**: pending` 在标题后第一行 — **不可省略，不可放到末尾**。

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

## FixItem 必填字段自检清单

**subagent 完成 fix-plan.md 后，必须逐项自检，全部通过才能返回**：

| # | 字段 | 检查规则 |
|---|------|---------|
| 1 | `**状态**` | 始终为 `pending`（plan 阶段不动手修复）。**位于标题后第一行，不在末尾** |
| 2 | `**来源**` | 审核文档文件名，可多个（逗号分隔） |
| 3 | `**位置**` | 具体到文件路径，尽量带行号 |
| 4 | `**优先级**` | 必须从 `BLOCK/CRITICAL/FLAG/WARNING/SUGGESTION/ADVISORY` 中选择 |
| 5 | `**依赖**` | 无依赖时填 `无`，不得留空 |
| 6 | `**类型**` | 必须从 `文档修复/代码修复-前端/代码修复-后端` 中选择 |
| 7 | `**修复步骤**` | 至少 1 条，每条包含具体文件路径和操作 |
| 8 | `**验证方式**` | 至少 1 种可执行的验证方法 |

**禁止**：
- 使用 `- **id**:`、`- **来源**:` 等列表格式代替 `**字段名**:` 格式
- 将 `**状态**` 放到 `**验证方式**` 之后（那是旧模板的错误位置）
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

## 一、`plan` 子命令

**功能**：扫描审核文档，生成细粒度修复计划。

### 步骤 1：解析输入，确定 ChangePair 列表

#### 1.1 确定目标 change 列表

- **单 change 模式**：用户指定具体 change 目录 → 目标为这一个 change
- **批量模式**：用户指定 changes 根目录 → 扫描所有 change 子目录

#### 1.2 过滤 change

读取 `docs/requirements/prd/decomposition/change-prd-mapping.yaml`，过滤掉：
- `status: archived` 的 change
- 审核文档中所有 Issue 均标记为 done/completed 的 change

#### 1.3 前后端成对配对

在 `change-prd-mapping.yaml` 中查找配对：
- 后端 `{name}` → 前端配对 `{name}_frontend`
- 前端 `{name}_frontend` → 后端配对去掉 `_frontend` 后缀
- 无配对时单独成为一个 ChangePair
- 单 change 模式下自动查找配对（扫描 proposal/design + mapping 文件）

每个 ChangePair 包含：`backend_change`、`frontend_change`、`prd_path`、`frontend_prd_path`（均可为 null）。

### 步骤 2：并行 dispatch subagent 分析

为每个 ChangePair 启动一个 **只读 subagent**，并行执行。

**每个 subagent 的输入**：ChangePair 完整信息 + 该 pair 下所有审核文档文件列表。

**每个 subagent 的工作流程**：

1. 扫描 6 类审核文档：`review-report-*.md`、`drift-report-*.md`、`verify-report-*.md`、`verify.md`、`verification-review.md`、`backend-issues.md`
2. 过滤全部已标记完成的审核文档
3. 读取规范文档（proposal.md、design.md、specs/、tasks.md）和 PRD 作为上下文
4. 解析 Issue（大模型自行解析，不需要为不同格式编写不同逻辑）
5. **过滤非代码实现问题**：忽略 git 操作建议、纯流程规范等，只保留需要修改代码或规范文档的问题
6. **漂移问题精准识别同步策略**：
   - **改文档**：代码正确，文档过时 → 更新文档
   - **改代码**：文档正确，代码偏离 → 修改代码
   - **双向调整**：双方都有问题 → 分别修正
   - 判断依据：以 proposal/design 设计意图为准，优先保持向后兼容
7. 生成 FixItem 列表，按依赖排序
8. **严格使用第一层定义的 FixItem 模板**，将 FixItem 写入各 change 目录下的 `fix-plan.md`
9. **执行第一层的「必填字段自检清单」**，全部通过后才能返回
10. 输出**元数据摘要**（FixItem 编号、文件路径、实体名、优先级、依赖列表）

### 步骤 3：主 agent 汇总

收集 subagent 元数据摘要，执行跨 pair 冲突检测：

1. **跨 pair 同名文件** → 标记冲突，需串行化
2. **跨 pair 同名实体**（Store/API 路径/表名）→ 标记「疑似跨 pair 依赖」
3. **依赖关系全局排序**：先修上游，再修下游

### 步骤 4：输出 fix-plan

- 批量模式：生成全局 `openspec/changes/fix-plan.md`
- 每个 change 目录生成独立的 `fix-plan.md`
- 展示修复统计摘要（总 Issue 数、按优先级分布、跨 change 依赖数）
- **等待用户确认后**，进入 fix 阶段

---

## 二、`fix` 子命令

**功能**：执行已确认的修复计划。

### 步骤 1：读取和分区 FixItem

读取 fix-plan.md，按文件路径**确定性分组**：
- 同一文件的所有 FixItem → 同一 fix subagent
- 不同文件 → 可并行到不同 fix subagent
- 有跨组依赖的串行化

### 步骤 2：创建共享 worktree

调用 `superpowers:using-git-worktrees` 创建 worktree。
Dispatch fix subagent 时必须显式告知：来源分支、worktree 路径、ownership 类型。

### 步骤 3：并行 dispatch fix subagent

每个文件组一个 fix subagent，并行执行（文件级隔离）。

**修复策略**：
- 文档修复：直接修改 Markdown
- 代码修复：走 TDD → 测试 → 红灯 → 实现 → 绿灯 → 重构
- 每个 FixItem 完成后跑测试/lint，确保编译通过
- 更新 fix-plan.md 状态：`pending` → `done`/`skipped`/`failed`
- 失败但不阻塞其他项 → 标记 `failed`，继续
- 关键依赖项失败 → 停止，返回失败报告

### 步骤 4：独立 Code Review

派独立 reviewer subagent（`code-reviewer` type）检查：代码质量、边界条件、安全性、与修复计划一致性。

### 步骤 5：主 agent 汇总

| 情况 | 行为 |
|------|------|
| 全部 done | 自动 commit |
| 有 failed 但不阻塞 | 跳过 failed，commit 成功的 |
| 关键依赖项 failed | 暂停，等待用户决策 |

Commit 格式：`fix(audit): 修复 {change-pair} 审核问题`

### 步骤 6：生成复盘报告

### 步骤 7：清理 worktree

---

# 📖 第三层：参考细节（按需查阅）

## 跨 change 问题处理

- **依赖识别**：基于 `{文件路径, 实体名}` 元组匹配 + 关键词兜底
- **执行顺序**：先修上游，再修下游；修改 A 影响 B 时自动同步
- **全局汇总**：批量模式生成全局 fix-plan.md

## FixItem 合并判断标准

| 条件 | 策略 |
|------|------|
| 多个 Issue → 同一文件同一行/同一逻辑块 | 合并为一个 FixItem |
| 多个 Issue → 同一文件不同位置 | 保持独立 |
| 多个 Issue 有依赖关系 | 保持独立，按依赖排序 |
| 同类问题模式（如相同命名错误在不同文件） | 合并为一个 |
| 多个 Issue 来自不同 change | 保持独立（在各自 change 下） |

## 上下文读取策略

- plan 阶段：前后端 change 成对读取
- 多个 ChangePair 并行分配 subagent
- 无配对 change 单独处理

## Worktree 管理

- 一个 worktree 放所有修复
- 文件名不冲突的 FixItem 并行，冲突的串行
- 命名规则由 AGENTS.md 统一管理

## 失败处理

- 单修复项失败：标记 `failed`，继续
- 关键依赖项失败：暂停，等待用户决策

## 输出物完整模板

### fix-plan.md

```markdown
# 修复计划 — {change-name}

**生成时间**: {timestamp}
**审核文档数**: {count}
**总问题数**: {total}

## 修复项

### {问题编号} - {问题简述}

**状态**: pending
**来源**: {审核文档文件名}
**位置**: {文件路径}:{行号}
**优先级**: {优先级}
**依赖**: {依赖编号，无则填"无"}
**类型**: {文档修复 | 代码修复-前端 | 代码修复-后端}

**修复步骤**:
1. {具体步骤}
2. ...

**验证方式**:
- {验证方法}
```

### 复盘报告

```markdown
# 回顾复盘报告

## 修复统计
| 指标 | 数量 |
|------|------|
| 总问题数 | N |
| 已修复 | N |
| 已跳过 | N |
| 已失败 | N |

## 按优先级分布
| 优先级 | 数量 | 已修复 |
|--------|------|--------|
| BLOCK | N | N |

## 常见问题类型
| 类型 | 数量 |
|------|------|
| 命名冲突 | N |

## 改进建议
- ...
```
