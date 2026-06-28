# OpenSpec 领域术语表

> 本文件定义 OpenSpec 系统的核心领域术语（ubiquitous language），供 propose、review、apply、verify、archive 等工作流共同引用。
> 仅收录领域概念，不包含实现细节。

---

## 核心概念

### Change（变更工作单元）

一个需求变更的工作单元，按端（前端/后端）拆分。当前处于 0-1 建设阶段，未来支持已有功能的增量修改。

- **命名格式**：`{domain}-{epic_number}-{name}[-frontend]`
- **后端 change**：无后缀，如 `user-01-authentication`
- **前端 change**：带 `-frontend` 后缀，如 `user-01-authentication-frontend`
- **纯 UI 前端 change**：前端 change 不涉及新增后端 API，proposal.md Impact 章节无 API 依赖清单
- **识别方式**：通过 `openspec/changes/` 目录下的 `.openspec.yaml` 和 `change-prd-mapping.yaml` 注册

### Capability（功能能力）

change 内部的独立功能单元。一个 change 包含 1 到多个 Capability。

- **命名规则**：小写英文，短横线分隔，如 `circle-creation`
- **与 specs/ 的关系**：Capability 名称与 `specs/` 子目录名 **严格 1:1 对应**
- **粒度建议**：单个 Capability 覆盖的子功能 ≤ 5 个（超出建议拆分，ADVISORY 级别提醒）

### Schema（文档组织方式）

change 的 SDD 文档组织模式，记录在 `.openspec.yaml` 的 `schema` 字段中。

| Schema | 含义 | plan.md |
|--------|------|---------|
| `spec-driven` | 以 spec 为驱动的文档结构 | 无 |
| `use-tdd-plan` | 包含 TDD 实施计划的结构 | 有，必须审核 |

若 `schema` 声明与实际文件不一致（如声明 `spec-driven` 但存在 `plan.md`）→ FLAG。

### 配对 Change（Paired Change）

同一个 EPIC 下后端 change 和前端 change 的关联关系。通过 `change-prd-mapping.yaml` 中的 `backend_change`/`frontend_change` 字段记录。

- 配对 change 用于前后端衔接审计
- 文件系统实际存在优先于 YAML 声明
- YAML 声明与实际不符 → ADVISORY

---

## Change 生命周期

```
propose → review → fix → apply → verify → archive
```

| 阶段 | 操作 | 产出 |
|------|------|------|
| propose | 创建 change 规范文档 | proposal.md, design.md, specs/, tasks.md, plan.md(可选) |
| review | 审核规范文档 | review-report-{timestamp}.md |
| apply | 执行实现 | 代码变更 |
| verify | 验证实现 | verify-report-{timestamp}.md |
| archive | 归档完成 | archived-review/ 目录 |

### Change 状态（mapping YAML）

| 状态 | 含义 |
|------|------|
| 活跃（无 status 字段或 null） | 可审核、可实现 |
| `archived` | 已归档，跳过审核 |

---

## 审核体系

### 审核模式

基于 change 文档完成状态判决：

| 模式 | 条件 | 审核范围 |
|------|------|---------|
| A | 后端 change 文档完成，前端 change **不存在** | 后端全量审核（维度 1-11/12），跳过衔接审计 |
| B | 后端 change 文档完成，前端 change 文档**也存在** | 后端全量审核 + 衔接审计 |
| C | 仅前端 change 完成（纯 UI 改动） | 前端独立审核（维度 1-4, 6 前端部分），跳过 API 契约/衔接审计 |
| — | 仅前端 change 完成（需后端对接） | **不审核**，提示先完成后端 change |

### 严重级别

| 级别 | 含义 | 门禁影响 |
|------|------|---------|
| **BLOCK** | 必须修复才能 apply | BLOCK > 0 → REJECTED |
| **FLAG** | 应该修复 | FLAG > 3 → CONDITIONAL |
| **ADVISORY** | 建议改进 | 不影响门禁 |

**犹豫规则**：审核者无法在 30 秒内确定是否为 BLOCK → 标记为 FLAG 并注明 `[NEEDS-HUMAN-REVIEW]`。

### 门禁判定

```
Step 1 规范审核: BLOCK > 0 → REJECTED
               FLAG > 3 → CONDITIONAL
               否则 → 进入 Step 2
Step 2 依赖检查: P0 依赖 > 0 → NEEDS_DEPENDENCIES
               否则 → APPROVED
```

### 综合得分

满分 100~120 分（按 change 类型和是否有 plan.md 动态计算）。得分作为质量趋势指标，不直接作为硬门禁条件。低于 70 分时在结论中增加 ADVISORY 提醒。

### 审核迭代

- 默认最多 **3 轮**迭代
- 第 3 轮后仍不 APPROVED → 输出"终止审核摘要"，由人工判断是否"带伤 apply"
- 每次迭代只修复上一轮的 BLOCK + 高优先级 FLAG

---

## 衔接审计

### 接口对齐矩阵

subagent 自动填充矩阵的结构部分（接口名称、HTTP 方法、路径、前端使用场景），人工补充处理决策、Owner、截止日期。

### 衔接审计触发条件

| 场景 | 行为 |
|------|------|
| 配对 change 目录存在 | 执行完整衔接审计 |
| 配对 change 目录不存在，但前端 PRD 存在 | 加载前端 PRD，检查后端 design.md API 覆盖情况 |
| 配对 change 目录不存在，前端 PRD 也不存在 | 跳过衔接审计，注明"无配对 change" |

---

## 关联文档

- Change-PRD 映射：`docs/requirements/prd/decomposition/change-prd-mapping.yaml`
- 审核清单：`.claude/skills/openspec-review-change/references/review-checklist.md`
- 门禁规则：`.claude/skills/openspec-review-change/references/review-gate-rules.md`
- ADR 目录：`openspec/docs/adr/`
