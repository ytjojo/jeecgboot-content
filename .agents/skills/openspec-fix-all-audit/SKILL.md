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

**FixItem 8 个必填字段清单**（输出前逐项自检）：
1. ✅ `**状态**` — 始终为 `pending`（plan 阶段不动手修复）
2. ✅ `**来源**` — 审核文档文件名
3. ✅ `**位置**` — 文件路径:行号
4. ✅ `**优先级**` — BLOCK/CRITICAL/FLAG/WARNING/SUGGESTION/ADVISORY
5. ✅ `**依赖**` — 依赖编号，无则填"无"
6. ✅ `**类型**` — 文档修复/代码修复-前端/代码修复-后端
7. ✅ `**修复步骤**` — 至少 1 条具体步骤
8. ✅ `**验证方式**` — 至少 1 种验证方法

**禁止**：使用 `- **id**:` 列表格式或任何非标准模板格式。所有 FixItem 必须严格使用上述 `**字段名**:` 格式。

### 步骤 3：主 agent 汇总

收集所有 subagent 产出的元数据摘要，执行跨 pair 冲突检测：

1. **跨 pair 同名文件检测**：两个不同 ChangePair 的 FixItem 涉及同一文件路径 → 标记为跨 pair 冲突，需串行化
2. **跨 pair 同名实体检测**：两个不同 ChangePair 的 FixItem 涉及同一实体名（如 Store 名称、API 路径、表名）→ 标记为「疑似跨 pair 依赖」，供用户确认
3. **依赖关系全局排序**：先修上游，再修下游

### 步骤 4：输出 fix-plan

- 批量模式：生成全局汇总 `openspec/changes/fix-plan.md`
- 每个 change 目录下生成独立的 `fix-plan.md`
- 向用户展示修复统计摘要（总 Issue 数、按优先级分布、跨 change 依赖数）
- **等待用户确认后**，进入 fix 阶段

---

## 二、`fix` 子命令

**功能**：执行已确认的修复计划。

### 步骤 1：读取和分区 FixItem

读取 fix-plan.md，按文件路径分组 FixItem：

- **同一个文件的所有 FixItem** → 分配给同一个 fix subagent
- **不同文件** → 可并行到不同 fix subagent
- 按文件路径分组是**纯确定性逻辑**（字符串匹配），不交给模型判断

按依赖排序：先处理无依赖的 FixItem 组，有跨组依赖的串行化。

### 步骤 2：创建共享 worktree

所有修复共用一个 git worktree。

调用 `superpowers:using-git-worktrees` 创建 worktree，命名规则遵循 AGENTS.md 中的 worktree 管理规范。

**重要**：dispatch fix subagent 时，必须显式告知：
- 来源分支（worktree 创建时所在分支）
- worktree 路径
- ownership 类型

### 步骤 3：并行 dispatch fix subagent

为每个文件组启动一个 fix subagent，并行执行。每个 subagent 独占自己负责的文件列表（文件级隔离，避免并行写冲突）。

**每个 subagent 的输入**：
- 分配到的 FixItem 列表（只包含其负责的文件）
- 共享 worktree 路径
- 来源分支信息
- 修复策略说明（见下方）

**修复策略**：
- **文档修复**：直接修改 Markdown 文件
- **代码修复**：走 TDD 流程 — 先写测试 → 红灯 → 实现 → 绿灯 → 重构
- **自动验证**：每个 FixItem 完成后跑测试、lint，**确保编译通过**
- 修复完成后更新 fix-plan.md 中对应项的状态：`pending` → `done` / `skipped` / `failed`
- 如果 FixItem 失败但不阻塞其他项：标记 `failed`，继续执行
- 如果关键依赖项失败导致下游无法执行：停止，返回失败报告

**每个 subagent 输出**：
- 改动文件列表 + diff 摘要
- 各 FixItem 的最终状态
- 失败原因（如有）

### 步骤 4：独立 Code Review

所有 fix subagent 完成后，由主 agent 派一个独立的 reviewer subagent（使用 `code-reviewer` agent type），对**所有代码改动**进行 code review。

Review 检查项：
- 代码质量和命名规范
- 边界条件处理
- 安全性
- 是否与修复计划一致

Review 发现的问题，由主 agent 决定：自动修复（小问题）或报告给用户（大问题）。

### 步骤 5：主 agent 汇总

根据所有 subagent 的结果，执行三級自动策略：

| 情况 | 行为 |
|------|------|
| 全部 done | 自动 commit，无需用户介入 |
| 有 failed 但不阻塞其他项 | 自动跳过 failed，commit 成功的 |
| 关键依赖项 failed | **暂停**，展示阻塞原因，等待用户决策 |

**Commit 消息格式**：
```
fix(audit): 修复 {change-pair 名称} 审核问题

- 已修复: {done 数量} 项
- 已跳过: {skipped 数量} 项
- 已失败: {failed 数量} 项
```

### 步骤 6：生成复盘报告

输出回顾复盘报告，包含：

1. **修复统计**
   - 总问题数 / 已修复 / 跳过 / 失败
   - 按优先级分布

2. **常见问题类型分析**
   - 命名冲突类
   - 目录结构类
   - API 缺失类
   - 文档不一致类
   - 代码实现类

3. **改进建议**
   - 针对高频问题的预防措施
   - 流程优化建议

4. **Code Review 摘要**（步骤 4 的产出）

### 步骤 7：清理 worktree

按照 AGENTS.md 中的 worktree 清理规则处理。

---

## 三、跨 change 问题处理

### 依赖识别（plan 阶段）

基于 `{文件路径, 实体名}` 元组匹配：
- 两个 Issue 来自不同 change 但引用同一实体名 → 标记为跨 change 依赖
- 关键词匹配作为兜底补充

### 执行顺序（fix 阶段）

- 按依赖关系排序：先修上游，再修下游
- 修改 A change 影响 B change 时，自动同步更新

### 全局汇总

- 批量模式生成全局 `openspec/changes/fix-plan.md`
- 各 change 独立执行

---

## 四、关键约束

### 必须遵循

1. **所有文件变更必须在 git worktree 中进行**（AGENTS.md 全局硬规则）
2. **代码修复走 TDD 流程**：先写测试 → 实现 → 重构
3. **修复后必须编译通过**
4. **遵循 AGENTS.md 中所有 13 条 Agent 行为规范**
5. **plan 阶段的 subagent 必须只读**，不得修改任何文件
6. **fix 阶段的文件级分区是确定性逻辑**，不交给模型判断
7. **Issue → FixItem 映射**：1:N 始终支持；N:1 按实际情况判断

### FixItem 合并判断标准

| 条件 | 策略 |
|------|------|
| 多个 Issue 指向同一文件同一行/同一逻辑块 | 合并为一个 FixItem |
| 多个 Issue 指向同一文件不同位置 | 保持独立 FixItem |
| 多个 Issue 有依赖关系 | 保持独立，按依赖排序 |
| 多个 Issue 是同类问题模式（如同样命名错误在不同文件中） | 合并为一个 FixItem |
| 多个 Issue 来自不同 change | 保持独立（在各自 change 下） |

### 上下文读取策略

- plan 阶段：前后端 change 成对读取，提供完整视角
- 多个 ChangePair 分配 subagent 并行处理
- 无配对 change 单独处理

### Worktree 管理

- 一个 worktree 放所有修复（文档 + 代码）
- 文件名不冲突的 FixItem 并行，冲突的串行
- Worktree 命名规则由 AGENTS.md 统一管理

### 失败处理

- 单个修复项失败：标记 `failed`，继续执行其他项
- 关键依赖项失败：暂停，等待用户决策
- 自动提交策略见 fix 步骤 5

---

## 五、输出物模板

### fix-plan.md 格式

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

### 复盘报告格式

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
