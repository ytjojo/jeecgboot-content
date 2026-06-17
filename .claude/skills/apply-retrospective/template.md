# retrospective.md 输出模板

> 以下为 `apply-retrospective` 生成的 `retrospective.md` 完整格式。
> openspec 完整模式使用全部章节；通用降级模式的 §3、§4 可能简化。

---

```markdown
# Retrospective: <change-name>

> Written: <YYYY-MM-DD> (after verify passed)
> Commit range: `<base-sha>..<head-sha>`
> Worktree: <path or "merged to main">

---

## §0 Evidence

> 量化前置信息 — 后续 Wins/Misses 直接引用本节的 commit/file/test，避免每行重复 `[evidence: ...]`。

<!-- 示例：- **Diff size**: +320 / -45 lines across 12 files -->

- **Commit range**: `<base>..<head>` (N commits)
- **Diff size**: +X / -Y lines across N files
- **Tasks done**: X/Y（仅 openspec 模式）
- **Active hours**: <估算>
- **Subagent dispatches**: <次数 or "n/a">
- **New external dependencies**: <列表，含 license 和版本，or "none">
- **Bugs encountered post-merge**: <数量，一行一个，or "none">
- **OpenSpec verify state at archive**: <pass / fail / not-run>（仅 openspec 模式，通用降级模式写 n/a）
- **Test coverage signal**: <百分比 or "n/a">

Commit chain (时序) — `git log --oneline <base>..HEAD` 输出:

`<short-hash> <commit message>`
`...`
`<short-hash> <commit message>`

> §0 条目可动态调整 — 根据实际情况新增或删除。例如：涉及数据库变更时新增 `- **Schema changes**: <表名列表>`。

---

## §1 Wins

<!-- 示例格式 -->
<!-- - [evidence: commit `a1b2c3d`] 提前准备数据库迁移脚本，首次执行即成功，无回滚 -->
<!-- - [evidence: `src/utils/helper.ts`] 复用已有工具函数，避免了重复实现 -->

- [evidence: <commit/file/test>] <描述>

---

## §2 Misses

- 🔴 [blocking | evidence: ...] <描述>
- 🟡 [painful  | evidence: ...] <描述>
- 📌 [nit      | evidence: ...] <描述>

> 严重程度说明：
> - 🔴 阻塞性的 — 导致无法继续或需要回滚
> - 🟡 痛苦的 — 显著降低了效率或质量
> - 📌 小问题 — 值得记录但不影响主线

> **零问题声明**：如无 🔴 无 🟡 项，必须在此回答：
> 1. 本次变更真的没有阻塞/痛苦的问题吗？
> 2. 是否有我选择忽视的问题（因为不便记录）？
> 3. 如果有外部观察者复盘，会同意零问题吗？

---

## §3 Plan deviations

| Plan task | What changed | Why |
|-----------|--------------|-----|
| <task-id> | <变化描述>   | <原因> |

> 通用降级模式：若无正式计划，写 `(无正式计划可供对比)`。

---

## §4 Skill / workflow compliance

> 技能列表从 openspec schema 的 apply 阶段动态获取，切勿在此硬编码。
> 生成报告时替换下方占位符为实际技能行。
> 默认预期：全部 ✓。

<!-- §4 技能表格：这是输出模板，生成报告时用实际值填充。每行格式为 `| <skill-name> | ✓/✗ |`，技能名从 openspec schema apply 阶段动态获取 -->

| Skill | Used |
|-------|------|
| <skill-name> | ✓/✗ |
| <skill-name> | ✓/✗ |

### Deliberately Skipped Skills

> 每个 ✗ 必须回答以下三问。整节空白（全绿 ✓）是预期状态。

- **`<skill name>`**
  - **What was skipped**: <具体跳过了整个 skill，还是某个 sub-step>
  - **Why this cycle**: <具体 trigger（提交/日志/行为），禁止模糊理由>
  - **How to prevent recurrence**: 选一：
    - `schema graph fix` — 具体改 schema.yaml 的哪段
    - `skill description tightening` — 具体改哪个 skill 的 frontmatter/instruction
    - `CLAUDE.md trigger` — 具体加哪段判读规则
    - `scope-judgment rule` — 具体 scope 应怎么判读
    - `n/a — skip justified` — 仅当 Why 引用了 AGENTS.md/CLAUDE.md 的明确跳过规则
    - `one-off — schema boundary case` — 需说明为何是边界

> 通用降级模式：标注 `(无 openspec schema，跳过)`。

---

## §5 Surprises

- <被推翻的假设>

> 若无：写 `(none observed)`。

> 写入前自问：
> - 是否有任何我预期会发生但没发生的事？
> - 是否有任何我没预期但发生了的事？
> 两条都 No → 写 `(none observed)`

---

## §6 Promote candidates → long-term learning

> 每条 candidate 用 `- [ ]` checklist 格式。
> 未勾选 = 候选已识别但尚未执行晋升，可 carry-forward 到下个周期。

- [ ] 🔴 **<一句话规则>** → **Promote to <目标>**
  > **Why**: <触发这条学习的具体事件>
  > **How to apply**: <下次遇到同类场景时的具体行为规则>

- [x] 🟡 **<已执行的条目>** → **Promote to memory**
  > **Why**: ...
  > **How to apply**: ...

**晋升目标参考**：

| 目标 | 含义 | 何时选用 |
|------|------|----------|
| **memory** | 写入 `~/.claude/memory/` | 通用规则，跨项目适用 |
| **CLAUDE.md** | 写入项目 CLAUDE.md 加规则 | 项目特定的行为约束 |
| **schema** | 修改 openspec 工作流定义 | 流程层面的改进 |
| **skill** | 改进现有技能定义 | 技能描述不够精确 |
| **one-off** | 仅记录，不晋升 | 确认是一次性事件 |

---

## §7 Agent dispatch orchestration

> 回顾 subagent 调度编排的合理性。

### Dispatch log

| # | Agent type | Purpose | Timing | Boundary respected? | Notes |
|---|-----------|---------|--------|---------------------|-------|
| 1 | `<type>` | `<task description>` | `<phase>` | ✓/✗ | `<optional note>` |

### Analysis

- **Parallel opportunities**: <是否有多 agent 可并行但被串行化了？或 parallel 用得好？>
- **Type appropriateness**: <agent 类型选择是否合适？有无用错 agent 类型的情况？>
- **Boundary violations**: <是否有 agent 越界操作（如只读 agent 尝试写代码）？>
- **Count efficiency**: <agent 数量是否合理？有无过度拆分（N 个 agent 做一件事）或过度集中（1 个 agent 做 N 件事）？>

> 若无 agent dispatch：写 `(本次变更未使用 subagent，通过主 agent 直接完成)`。

---

## §8 Session workflow recap

> 整个 session 的高层时间线总结，为 §0 提供叙事背景。

### Timeline

| Phase | Approx time | Duration | Key events / decisions |
|-------|-------------|----------|------------------------|
| `<plan/implement/review/verify/merge>` | `<HH:MM>` | `<估算耗时>` | `<关键事件或决策点>` |

### Flow quality

- **Workflow adherence**: <是否按标准流程执行？有无跳过关键阶段？>
- **Pivots**: <过程中是否有重大方向调整或策略变更？>
- **Blockers**: <是否有阻塞事件？如何解决的？>

> **摘要**：<用 3-5 句话简述本次 session 从头到尾做了什么，什么阶段花了最多时间，是否顺利>。

---

## §9 Git worktree operations

> 回顾 worktree 生命周期操作的正确性。关注操作过程而非 commit 内容。

### Worktree lifecycle

- **Worktree name**: `<name>` (预期格式: `<描述>-<6位hex>`, 如 `circle-gov-7b9e4d`)
- **Owner file**: ✓ 已创建 / ✗ 未创建 / n/a
- **Created from branch**: `<source branch>`
- **Merged to**: `<target branch>` (预期: 与 source branch 一致，禁止跨分支合并)
- **Cleanup**: ✓ 已清理 / ✗ 未清理 / n/a（说明原因）
- **Cherry-pick operations**: <列表 or "none">

### Commit topology

```
<git log --oneline --graph <base>..HEAD 输出>
```

### Operation quality

- **Name convention**: ✓/✗ — <是否符合 `<描述>-<6位hex>` 格式>
- **Owner file compliance**: ✓/✗ — <创建 worktree 后是否立即写入 .worktree-owner>
- **Merge target correctness**: ✓/✗ — <是否合到正确分支（source branch = target branch）>
- **Cleanup completeness**: ✓/✗ — <是否遗留未清理的 worktree>
- **Commit process anomalies**: <是否有意外 rebase、amend、cherry-pick 等操作？如有，描述操作和原因>

> 若无 worktree 操作：写 `(本次变更未使用 worktree)`。
```
