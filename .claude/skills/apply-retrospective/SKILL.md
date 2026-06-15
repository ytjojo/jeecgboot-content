---
name: apply-retrospective
description: "当用户完成开发变更后需要过程复盘时使用。触发：'/apply-retrospective'、'复盘'、'retrospective'、'post-mortem'、'事后分析'、'回顾'、'回顾变更'、'项目复盘'、'经验教训'、'总结经验'、'反思'、openspec apply 完成后自动调用。"
---

# apply-retrospective — OpenSpec Apply 阶段过程复盘

## 定位

对一次已完成的技术实现进行**过程质量**回顾，关注「怎么做」而非「做成什么样」。

**关注**：计划与实际的偏差、工作流遵守情况、过程中的意外、可复用的经验
**禁止评价**：代码质量（code-review 管）、设计文档完整性（openspec-review-change 管）、测试覆盖率（verify 管）

> **字面合规等于精神合规。** 引用证据但内容空洞，等同于未引用。勾选 checklist 但未实际检查，等同于未勾选。

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
输出：`openspec/changes/<change-name>/retrospective.md`

### 通用降级模式
前置：git log 可识别提交范围。
输出：默认 `docs/review/`，可用 `--output <目录>` 覆盖（禁止临时目录）。

## 输入数据收集

### openspec 模式
- `brainstorm.md` → 初始方案 | `plan.md` → §3 基准 | `tasks.md` → §0 完成率 + §3 偏差
- `verify.md` → §0 验证状态 | `git log <base>..HEAD` → §0 量化数据

### 通用降级模式
- `git log <base>..HEAD` → §0 量化数据 | 对话上下文 → 任务目标、计划（如有）

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

> `Active hours`：从提交时间戳首尾间隔估算，标注 `(估算)`
> `Subagent dispatches`：从对话上下文提取创建的子代理次数，标注 `(上下文提取)` 或 `n/a`

按 `template.md` 中的 **§0 Evidence** 格式汇总。条目可根据实际情况动态增删。

### Step 2: 分析 → 编写 §1–§6

按 `template.md` 中对应章节格式编写：

| 章节 | 内容 | 降级模式 |
|------|------|----------|
| §1 Wins | 成功之处，每条引用 §0 证据 | 不变 |
| §2 Misses | 不足，按 🔴🟡📌 分级 | 不变 |
| §3 Plan deviations | 计划偏差对比表 | 无 plan 时写 `(无正式计划可供对比)` |
| §4 Skill compliance | apply 阶段技能合规 + 跳过三问 | 标注无 schema 跳过 |
| §5 Surprises | 被证伪的假设 | 不变 |
| §6 Promote candidates | 晋升候选（Why/How to apply） | 不变 |

**§4 注意事项**：
- 仅列出 **apply（实现）阶段**的技能，brainstorming/writing-plans 等 plan 阶段技能不在此列
- 技能列表从 openspec schema 的 apply 阶段动态获取
- 默认预期：全部 ✓。每个 ✗ 必须在 `### Deliberately Skipped Skills` 子章节回答三问（What/Why/How to prevent）
- **禁止模糊理由**："不需要""太小""没时间""纯配置""变更量小""单提交""手动等价""已知模式""没必要"。必须给出具体 trigger — 引用 `git log --stat` 输出或 AGENTS.md/CLAUDE.md 中的跳过规则原文证明满足条件
- How to prevent 选 `n/a — skip justified` 仅当 Why 中引用了 AGENTS.md/CLAUDE.md 的明确跳过规则

**§6 晋升目标**：参见 `template.md` 中的晋升目标参考表。

### Step 3: 写入输出

将完整内容写入 `retrospective.md`。具体格式参见 `template.md`。

---

## 跳过条件

以下情况可跳过完整复盘。**必须同时满足条件 2 和 (条件 1 或 条件 3)**，单一条件不构成跳过理由：

1. 单提交的琐碎修复（linter fix、typo、格式调整）
2. 变更量 < 10 行，且 `git diff` 不涉及 `*.java`/`*.ts`/`*.vue`/`*.py` 业务逻辑文件
3. 纯粹的配置修改（`.yml`/`.properties`/`.env` 中无逻辑变更的配置项调整）

跳过输出必须附带 `git diff --stat <base>..HEAD` 实际输出：

```markdown
# Retrospective: <change-name> — SKIPPED
> `git diff --stat`: <实际输出>
> 理由: <基于上述数据的明确判断>
```

---

## 边界红线 — 不可评价的领域

> 完整边界红线表、越界关键词清单和子串匹配指引见 `checklist.md` 中的「越界红线参考」和「越界扫描」段。

以下内容绝对不能出现在 retrospective.md 中：
- 代码质量、架构设计、设计文档、测试覆盖率、提交粒度、命名/风格
- 每个领域有对应的「过程视角替代」，将 "代码写得不好" 转化为 "过程哪里出了问题"
- 提交粒度判断规则：可评价「是否因未遵守 subagent 编排规则」，不可评价「提交内容本身的好坏」

复盘完成后必须执行越界关键词扫描（关键词清单在 checklist.md）。

---

## 防理性化机制

复盘本质是自我审视，AI 会本能地回避问题。执行时对照 `checklist.md` 中的：
- 「理性化自查」清单
- 「常见理性化借口对照」表
- 「Red Flags」列表

---

## 向前指针策略

事实变化时 **不重写** retrospective.md — 增加向前指针保留审计线索：
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

逐条判断未勾选 candidate：仍相关 → carry-forward / 已过时 → stale / 可执行 → 勾选 `[x]`。

---

## 约束清单

1. **证据为先** — §1/§2 每项声明引用 §0 中的具体证据（commit/file/test），不可空洞
2. **不替代 code review / openspec review** — 不评价代码质量、架构、设计文档、测试覆盖率（详见 checklist.md 越界红线）
3. **向前指针 + 仅一次** — 不重写已有 retrospective.md，追加 Update 指针；执行前 `test -f` 检测，已存在则询问用户
4. **中文输出** — 报告使用中文；无内容的章节写 `(none observed)` 占位，不跳过

## 参考文件

- `template.md` — 输出模板
- `checklist.md` — 自查清单（含越界红线表、借口对照表、Red Flags）
