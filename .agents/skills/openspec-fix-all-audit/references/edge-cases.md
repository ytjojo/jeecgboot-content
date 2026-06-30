# 参考细节：边界情况与规则

> 从 `SKILL.md` 第三层引用。执行中遇到边界情况时按需查阅。

---

## 跨 change 问题处理

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

## FixItem 合并判断标准

| 条件 | 策略 |
|------|------|
| 多个 Issue 指向同一文件同一行/同一逻辑块 | 合并为一个 FixItem |
| 多个 Issue 指向同一文件不同位置 | 保持独立 FixItem |
| 多个 Issue 有依赖关系 | 保持独立，按依赖排序 |
| 多个 Issue 是同类问题模式（如同样命名错误在不同文件中） | 合并为一个 FixItem |
| 多个 Issue 来自不同 change | 保持独立（在各自 change 下） |

---

## 上下文读取策略

- plan 阶段：前后端 change 成对读取，提供完整视角
- 多个 ChangePair 分配 subagent 并行处理
- 无配对 change 单独处理

---

## Worktree 管理

- 一个 worktree 放所有修复（文档 + 代码）
- 文件名不冲突的 FixItem 并行，冲突的串行
- Worktree 命名规则由 AGENTS.md 统一管理

---

## 失败处理

- 单个修复项失败：标记 `failed`，继续执行其他项
- 关键依赖项失败：暂停，等待用户决策
- 自动提交策略见 `references/fix-workflow.md` 步骤 5
