# `fix` 子命令详细流程

> 从 `SKILL.md` 第二层引用。主 agent 执行 fix 子命令时读取。

---

## 步骤 1：读取和分区 FixItem

读取 fix-plan.md，按文件路径**确定性分组**：
- **同一个文件的所有 FixItem** → 分配给同一个 fix subagent
- **不同文件** → 可并行到不同 fix subagent
- 按文件路径分组是**纯确定性逻辑**（字符串匹配），不交给模型判断

按依赖排序：先处理无依赖的 FixItem 组，有跨组依赖的串行化。

---

## 步骤 2：创建共享 worktree

所有修复共用一个 git worktree。

调用 `superpowers:using-git-worktrees` 创建 worktree，命名规则遵循 AGENTS.md 中的 worktree 管理规范。

**dispatch fix subagent 时必须显式告知**：
- 来源分支（worktree 创建时所在分支）
- worktree 路径
- ownership 类型

---

## 步骤 3：并行 dispatch fix subagent

为每个文件组启动一个 fix subagent，并行执行。每个 subagent 独占自己负责的文件列表（文件级隔离，避免并行写冲突）。

### 每个 subagent 的输入

- 分配到的 FixItem 列表（只包含其负责的文件）
- 共享 worktree 路径
- 来源分支信息
- 修复策略说明（见下方）

### 修复策略

- **文档修复**：直接修改 Markdown 文件
- **代码修复**：走 TDD 流程 — 先写测试 → 红灯 → 实现 → 绿灯 → 重构
- **自动验证**：每个 FixItem 完成后跑测试、lint，**确保编译通过**
- 修复完成后更新 fix-plan.md 中对应项的状态：`pending` → `done` / `skipped` / `failed`
- 如果 FixItem 失败但不阻塞其他项：标记 `failed`，继续执行
- 如果关键依赖项失败导致下游无法执行：停止，返回失败报告

### 每个 subagent 输出

- 改动文件列表 + diff 摘要
- 各 FixItem 的最终状态
- 失败原因（如有）

---

## 步骤 4：独立 Code Review

所有 fix subagent 完成后，派一个独立的 reviewer subagent（`code-reviewer` type），对**所有代码改动**进行 code review。

Review 检查项：
- 代码质量和命名规范
- 边界条件处理
- 安全性
- 是否与修复计划一致

Review 发现的问题，由主 agent 决定：自动修复（小问题）或报告给用户（大问题）。

---

## 步骤 5：主 agent 汇总

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

---

## 步骤 6：生成复盘报告

使用 `templates/retrospective.md` 模板生成，包含：
1. 修复统计（总问题数 / 已修复 / 跳过 / 失败，按优先级分布）
2. 常见问题类型分析（命名冲突、目录结构、API 缺失、文档不一致、代码实现）
3. 改进建议（高频问题预防措施、流程优化建议）
4. Code Review 摘要

---

## 步骤 7：清理 worktree

按照 AGENTS.md 中的 worktree 清理规则处理。
