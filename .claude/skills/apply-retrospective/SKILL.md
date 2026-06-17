---
name: apply-retrospective
description: "当用户完成开发变更后需要过程复盘时使用。触发：'/apply-retrospective'、'复盘'、'retrospective'、'post-mortem'、'事后分析'、'回顾'、'回顾变更'、'项目复盘'、'经验教训'、'总结经验'、'反思'、openspec apply 完成后自动调用。"
---

# apply-retrospective — OpenSpec Apply 阶段过程复盘

## 定位

对已完成的技术实现做**过程质量**回顾，关注「怎么做」而非「做成什么样」。

**关注**：计划与实际的偏差、工作流遵守情况、过程中的意外、可复用的经验
**禁止评价**：代码质量（code-review 管）、设计文档完整性（openspec-review-change 管）、测试覆盖率（verify 管）


## 触发方式

| 方式 | 说明 |
|------|------|
| 手动调用 | `/apply-retrospective <change-name>` 或 `/apply-retrospective <change-name> --output <目录>` |
| 工作流自动 | openspec apply 阶段完成后自动触发 |
| 关键词触发 | 用户说「复盘」「retrospective」「回顾这次变更」。若无明确变更标识，先追问 `基于哪个 commit range？` |

## 模式选择

1. 检测 `openspec/changes/<name>/` 是否存在
2. 检测 `verify.md` 是否存在且不是 ❌ FAIL
   → 均满足：openspec 完整模式
   → 任一不满足：通用降级模式

### openspec 完整模式
前置校验 (`test -f` + `! grep -q 'FAIL'` verify.md) → 任一失败停止。
输出：`openspec/changes/<name>/retrospective.md`

### 通用降级模式
前置：git log 可识别提交范围。
输出：默认 `docs/review/`，`--output` 覆盖。禁止临时目录。

## 输入数据收集

### openspec 模式
- `brainstorm.md` → 初始方案 | `plan.md` → §3 基准 | `tasks.md` → §0 完成率 + §3 偏差
- `verify.md` → §0 验证状态 | `git log <base>..HEAD` → §0 量化数据

### 通用降级模式
- `git log <base>..HEAD` → §0 量化数据 | 对话上下文 → 任务目标、计划（如有）+ §7 调度记录 + §8 阶段信息 + §9 操作序列

> 所有 openspec 文件路径前缀为 `openspec/changes/<name>/`。

---

## 执行流程

### Step 1: 收集证据 → 填写 §0

运行命令收集量化数据：

```bash
git log --oneline <base>..HEAD
git diff --stat <base>..HEAD
grep -cE '^\s*- \[x\]' openspec/changes/<name>/tasks.md   # 仅 openspec
grep -cE '^\s*- \[ \]' openspec/changes/<name>/tasks.md   # 仅 openspec
```

# §7 agent dispatch: 从对话上下文提取 subagent 调度记录（agent 类型、目的、时序、边界遵守情况）
# §8 session workflow: 从对话上下文 + commit 时间戳提取阶段信息
# §9 git worktree:
git worktree list
git log --oneline --graph <base>..HEAD

> `Active hours`：从提交时间戳首尾间隔估算，标注 `(估算)`
> `Subagent dispatches`：从对话上下文提取创建的子代理次数，标注 `(上下文提取)` 或 `n/a`

按 `template.md` 中的 **§0 Evidence** 格式汇总。条目可根据实际情况动态增删。

### Step 2: 分析 → 编写 §1–§9

按 `template.md` 中对应章节格式编写：

| 章节 | 内容 | 降级模式 |
|------|------|----------|
| §1 Wins | 成功之处，每条引用 §0 证据 | 不变 |
| §2 Misses | 不足，按 🔴🟡📌 分级 | 不变 |
| §3 Plan deviations | 计划偏差对比表 | 无 plan 时写 `(无正式计划可供对比)` |
| §4 Skill compliance | apply 阶段技能合规 + 跳过三问 | 标注无 schema 跳过 |
| §5 Surprises | 被证伪的假设 | 不变 |
| §6 Promote candidates | 晋升候选（Why/How to apply） | 不变 |
| §7 Agent dispatch | agent 调度编排回顾（dispatch log + 四维分析） | 无 agent dispatch 时写占位 |
| §8 Session recap | 整个 session 的高层时间线总结 + 摘要 | 不变 |
| §9 Git worktree | worktree 生命周期操作正确性 | 无 worktree 时写占位 |

**§4 注意事项**：仅列出 apply 阶段技能（从 schema 动态获取）。每个 ✗ 回答三问（What/Why/How to prevent）。Why 必须引用具体 trigger（git log --stat 或规则原文），禁止模糊理由（完整禁止清单见 checklist.md §4）。

**§6 晋升目标**：见 `template.md` 晋升目标参考表。

### Step 3: 写入输出

写入 `retrospective.md`，格式见 `template.md`。

---

## 跳过条件

可跳过完整复盘（须条件 2 + (条件 1 或 3)）：

1. 单提交的琐碎修复（linter fix、typo、格式调整）
2. 变更量 < 10 行，且 `git diff` 不涉及 `*.java`/`*.ts`/`*.vue`/`*.py` 业务逻辑文件
3. 纯粹的配置修改（`.yml`/`.properties`/`.env` 中无逻辑变更的配置项调整）

跳过时必须输出 `git diff --stat` 结果：

```markdown
# Retrospective: <change-name> — SKIPPED
> `git diff --stat`: <实际输出>
> 理由: <基于上述数据的明确判断>
```

---

## 边界红线 — 不可评价的领域

> 完整边界红线表、越界关键词清单和子串匹配指引见 `checklist.md` 中的「越界红线参考」和「越界扫描」段。

禁止内容：
- 代码质量、架构设计、设计文档、测试覆盖率、提交粒度、命名/风格
- 每个领域有对应的「过程视角替代」，将 "代码写得不好" 转化为 "过程哪里出了问题"
- 提交粒度判断规则：可评价「是否因未遵守 subagent 编排规则」，不可评价「提交内容本身的好坏」

复盘完成后必须执行越界关键词扫描（关键词清单在 checklist.md）。

---

## 防理性化机制

复盘本质是 AI 自我审视，易合理化。对照 `checklist.md` 中的理性化自查清单、借口对照表、Red Flags 列表。

---

## 向前指针策略

不重写已有 retrospective.md，追加 Update 指针：
```markdown
> **Update YYYY-MM-DD**：§X 中的 <声明> 已被 <链接> 取代
```

## Carry-forward 机制

```bash
# openspec 模式
grep -A 5 '^- \[ \]' openspec/changes/archive/*/retrospective.md
# 通用降级模式
grep -A 5 '^- \[ \]' docs/review/*/retrospective.md
```

未勾选 candidate 判断：仍相关 → carry-forward / 已过时 → stale / 可执行 → 勾选 `[x]`。

---

## 约束清单

1. **证据为先** — §1/§2 每项声明引用 §0 中的具体证据（commit/file/test），不可空洞
2. **不替代 code review / openspec review** — 不评价代码质量、架构、设计文档、测试覆盖率（详见 checklist.md 越界红线）
3. **向前指针 + 仅一次** — 不重写已有 retrospective.md，追加 Update 指针；执行前 `test -f` 检测，已存在则询问用户
4. **中文输出** — 报告使用中文；无内容的章节写 `(none observed)` 占位，不跳过

## 参考文件

- `template.md` — 输出模板
- `checklist.md` — 自查清单（含越界红线表、借口对照表、Red Flags）
