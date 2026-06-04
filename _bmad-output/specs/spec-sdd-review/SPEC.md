---
id: SPEC-sdd-review
companions:
  - review-checklist.md
  - review-workflow.md
  - review-metrics.md
  - api-naming-rules.md
sources: []
---

> **Canonical contract.** This SPEC and the files in `companions:` are the complete, preservation-validated contract for what to build, test, and validate.

# SDD 规范文档审核框架

## Why

SDD（Software Design Document）是前后端开发的唯一契约。当前痛点：SDD 生成后直接进入开发，缺乏系统化审核，导致——
- 后端已完成但前端发现缺少接口（契约单向而非双向）
- API 命名语义混乱（如 `blackList` 表示单用户拉黑），污染全栈代码
- 前后端职责边界模糊，开发中频繁返工

本框架在 SDD 生成后、开发启动前，提供多维度审核机制，区分前端和后端视角，确保契约完整、命名规范、边界清晰。

## Capabilities

- id: CAP-1
  intent: 审核者可以对 SDD 执行结构完整性检查，验证五字段内核和 Companion 文件是否完整
  success: 检查清单逐项通过率 = 100%，零 BLOCKER 级结构缺陷

- id: CAP-2
  intent: 审核者可以从前端视角验证 SDD 中每个页面/组件的 API 需求是否被后端 API 完整覆盖
  success: 页面-接口映射表中零"悬空需求"（前端需要但后端未声明的接口）

- id: CAP-3
  intent: 审核者可以从后端视角验证 SDD 中每个 API 的命名是否符合语义规范，是否与已有实现兼容
  success: 零命名违规（路径中无技术实现术语、资源名用复数、动作用 HTTP method）

- id: CAP-4
  intent: 审核者可以识别 SDD 中前后端职责边界的模糊地带，并产出明确的归属决策
  success: 每个业务逻辑点有且仅有一个端（前端或后端）负责，无歧义

- id: CAP-5
  intent: 审核者可以在后端已部分完成的场景下，标注 API 的就绪状态（已就绪/待开发/需重构），支撑前端并行开发计划
  success: 每个 API 有明确的就绪状态标签，前端可据此制定 mock 策略

- id: CAP-6
  intent: 审核者可以对 SDD 执行对抗性审查，产出按严重级别分类的发现列表，并提示用户启动 subagent 修复
  success: BLOCKER 发现 = 0 且 HIGH 发现 ≤ 2 时判定通过；否则提示用户启动 subagent 修复后重新审核

- id: CAP-7
  intent: 审核者可以对每个 API 的错误路径和边界条件做穷举检查
  success: 每个 API 声明了错误码映射和前端处理策略，边界缺口率 ≤ 10%

- id: CAP-8
  intent: 审核者可以识别 SDD 依赖的外部模块（前端其他模块、后端未实现接口），并产出依赖清单，提示用户启动 subagent 单独完善依赖后再重新审核
  success: 每个依赖项有明确的归属模块、缺失内容描述、修复建议；依赖全部满足后才判定 SDD 可进入开发阶段

## Constraints

- 审核流程必须区分前端审核和后端审核两个独立通道，不能合并为单一审核
- 后端已有实现（已上线 API）在 SDD 中标记为"已就绪"约束，审核时不得建议修改其接口签名，除非有 CRITICAL 级命名违规
- 审核产出必须是可操作的（有具体文件路径、行号、修复建议），不接受泛泛建议
- API 命名规范基于 RESTful 语义，不接受 RPC 风格路径（如 `/getUserList`、`/blackList`）
- 审核阶段只产出发现和修复建议，不直接修改 SDD 或代码；修复通过用户启动 subagent 执行
- SDD 判定"可开发"前，必须完成：审核通过 → 确认 → 修复规范文档 → 修复依赖模块 → 再审核通过

## Non-goals

- 不替代 SDD 的生成过程（由 bmad-spec 负责）
- 不执行代码实现审查（本框架审核的是文档，不是代码）
- 不定义业务需求（需求由上游 PRD/GDD 定义）
- 不处理 UI/UX 设计审核（由 bmad-ux 负责）

## Success signal

SDD 经过完整审核迭代后，开发团队可以：
1. 直接基于 SDD 开始前后端并行开发，无需中途补充接口定义
2. 前端代码中零出现非 RESTful 命名的 API 调用
3. 后端不因"前端发现缺接口"而中断开发去做计划外 API
4. 所有依赖模块（前端其他模块、后端待实现接口）已就绪或有明确的 mock 策略

## Assumptions

- SDD 由 bmad-spec 生成，已通过 Spec Law 自审
- 后端已有 API 清单可通过代码扫描或 API 文档工具获取
- 审核团队包含至少一名前端开发者和一名后端开发者
- 审核阶段只产出发现，不直接修改文档或代码；所有修复由用户启动 subagent 执行
- 后端已有 API 的命名违规在本轮审核中记录，审核完成后提示用户启动 subagent 修复

## Open Questions

<!-- 已解决，无待确认项 -->
