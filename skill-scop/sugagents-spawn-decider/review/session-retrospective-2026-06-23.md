# Session 复盘报告

> **Session 目标**：对 `subagent-spawn-decision-rules.md` 进行多维度多角度审核，审核文档保存到 `review/` 目录。
> **复盘日期**：2026-06-23
> **结果**：❌ 目标未达成，review 目录为空，文档未产出

---

## 一、操作时间线

| 阶段 | 操作 | 结果 |
|------|------|------|
| 1 | 读取目标文档（198 行） | ✅ |
| 2 | 加载 `ce-doc-review` skill | ✅ |
| 3 | 创建 worktree `doc-review-19d140` | ✅ |
| 4 | 读取 5 个 persona 文件 + 模板 + schema + synthesis 参考（共 ~800 行额外材料） | ✅ |
| 5 | **并行派发 5 个审核 subagent**（coherence / feasibility / product-lens / scope-guardian / adversarial） | ❌ 4 个被用户拒绝，1 个完成 |
| 6 | 用户反馈"这次执行真是灾难" | — |
| 7 | 提出 2 个简化方案，用户选择方案 B（2 agent） | ✅ |
| 8 | **并行派发 2 个通用 agent 后台运行**（结构一致性 / 内容深度） | ❌ 返回空结果，追问后也被中断 |
| 9 | 用户询问报告目录 | — |
| 10 | 写入本复盘报告 | 进行中 |

---

## 二、根因分析

### 根因 1：Prompt 膨胀——每个 agent prompt 超过 4000 token

**问题**：严格按照 `ce-doc-review` skill 模板，每个 agent prompt 包含：
- 完整 persona 定义（~100 行）
- 完整 JSON Schema（~60 行）
- 完整 confidence rubric
- 完整文档内容（198 行）
- 合成规则引用

**后果**：
- 5 个 prompt × 4000+ token = 20000+ token 一次性消费
- 用户看到 5 个巨型审批对话框，无从判断内容差异
- 审批疲劳 → 4/5 被拒

**违反原则**：AGENTS.md 规则六「Token 预算绝非软性建议 — 单任务上限：4,000 Token」

### 根因 2：ce-doc-review 协议过度——198 行文档不值得 5 个专项 persona

**问题**：`ce-doc-review` skill 设计目标是大规模规划文档（PRD、设计文档，通常 500-2000 行），默认派发 5-7 个专项 agent。本文档仅 198 行，是**规范/规则类文档**而非大型规划文档。

**为何误判**：skill 的 Phase 1 分类规则基于文档特征计数（"5+ requirements"、"architectural decisions"、"new abstractions"），这些规则对任何结构化文档都会触发。

### 根因 3：并行审批 UX 灾难

**问题**：5 个 agent 同时弹出审批对话框，用户无法逐个评估每个 agent 的必要性。

**后果**：用户一次性拒绝 4 个，仅 product-lens 通过（因它排在第三位，恰好被点开）。

### 根因 4：第二轮修复半心半意

**问题**：方案 B 将 agent 数降到 2，但：
- Agent prompt 仍然过长
- 用了 `general-purpose` 而非专用 reviewer agent
- 两个 agent 都返回了"任务已完成"空结果
- 追问（SendMessage）后 agent 被用户中断

**根本原因**：没有从第一轮失败中真正吸取教训——应该放弃 subagent 编排，直接在主会话中完成审核。

### 根因 5：AGENTS.md 规则被机械执行

**AGENTS.md 原文**：
> 涉及**审核 / 评审 / 分析 / 审查 / 审计 / review**操作时，必须通过 subagent 执行，禁止在主 agent 中直接处理

**问题**：这条规则的设计意图是防止主 agent 在审核长文档时耗尽上下文。但对 198 行的短文档，subagent 的调度成本（prompt 构建、审批、等待）远超直接审核成本。**规则适用需要判断力，不应机械套用**。

### 根因 6：Worktree 遗产未清理

Session 创建了 worktree `doc-review-19d140`，写入 registry 但随后被移除（`git worktree list` 已无该项），registry 文件仍保留过期条目。残留条目：
```
.claude/worktrees/doc-review-19d140
```

---

## 三、应该怎么做

### 正确做法：单 agent 直接审核

对 198 行的规则文档，不需要 subagent 编排：

```
1. 主 agent 直接读取文档（1 次 Read，198 行）
2. 主 agent 按维度逐一分析（结构、内容、边界、缺失）
3. 主 agent 输出审核报告到 review/ 目录
4. 总耗时：3-5 轮对话，~3000-5000 token
```

### 如果确实要用 subagent

```
1. 最多 1 个 agent
2. Prompt 精简：只包含指令 + 文档路径（让 agent 自己读）
3. 不用 ce-doc-review 重度协议
4. 不嵌入文档全文
```

### 决策启发式

| 文档规模 | 审核方式 |
|----------|----------|
| < 500 行 | 主 agent 直接审核 |
| 500-1500 行 | 1 个 subagent |
| > 1500 行 + 多章节 | ce-doc-review 协议（2-3 persona） |
| > 3000 行 + 架构决策 | ce-doc-review 完整协议（5+ persona） |

---

## 四、改进项

| # | 改进 | 优先级 |
|---|------|--------|
| 1 | AGENTS.md 审核规则增加豁免条款：< 500 行文档可由主 agent 直接审核 | P1 |
| 2 | 使用 subagent 时，prompt 中不嵌入文档全文，让 agent 自己 Read | P1 |
| 3 | 并行 subagent 上限：审核类任务默认 ≤ 2 个 | P2 |
| 4 | 清理 worktree registry 残留条目 | P2 |
| 5 | ce-doc-review skill 增加文档规模门控（< 500 行建议降级） | P3 |

---

## 五、当前状态

- **目标文档**：`subagent-spawn-decision-rules.md` —— 未审核
- **review/ 目录**：已创建，仅含本复盘报告
- **Worktree 残留**：`doc-review-19d140` registry 条目待清理
- **Token 消耗**：本 session 约 60000+ token（含 subagent），零产出

---

> **结论**：这次 session 是"流程压倒判断"的典型案例。为了遵守"审核必须用 subagent"的规则，启动了 7 个 agent（5 + 2），全部失败。正确的做法是：评估文档规模（198 行）→ 判断 subagent 开销 > 收益 → 直接审核。**规则是工具，不是主人。**
