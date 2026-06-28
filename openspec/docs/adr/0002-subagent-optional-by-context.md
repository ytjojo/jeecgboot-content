# ADR-0002: Subagent 审核从强制改为上下文感知可选

- **日期**: 2026-06-28
- **状态**: accepted
- **决策者**: @jiulongteng

## 背景

`openspec-review-change` SKILL.md 原规定：

> 审核操作必须通过 subagent 执行（避免 context window 溢出和主流程干扰）

但在实际使用中发现：简单 change（artifacts 少、无配对 change、无 plan.md）的主 agent 上下文完全足够，subagent 的 spawn 开销和上下文复制成本反而高于直接审核。核心原则是"审核者 ≠ 创建者"的认知隔离，而非"必须是 subagent"。

## 决策

审核执行方式改为分层策略：

```
简单 change（artifacts < 1500 行，无配对 change，无 plan.md）
  → 主 agent 直接审核

复杂 change（artifacts > 1500 行，或有配对 change + plan.md）
  → subagent 拆分审核，避免上下文溢出
```

`1500 行`为初始参考阈值，需根据实际使用反馈校准。

## 后果

- 简单 change 的审核延迟降低（无 subagent spawn 开销）
- 复杂 change 仍然享受上下文隔离和并行度收益
- subagent 拆分决策依据从"必须"变为"上下文窗口触发"，与 `subagent-spawn-decision-rules.md` 决策树 Q2 对齐

## 备选方案

保留"必须 subagent"不变。被拒绝原因：简单 change 下 subagent 的收益低于成本，且项目 `subagent-spawn-decision-rules.md` 已有"不推荐为拆而拆"原则。
