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

> 通用模式：若无正式计划，写 `(无正式计划可供对比)`。

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

> 通用模式：标注 `(无 openspec schema，跳过)`。

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
```
