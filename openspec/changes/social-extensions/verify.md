# Verification Report

> 此文件在 apply 完成后运行 `/opsx:verify` 时自动生成，用以确认实现与 specs / design / tasks 的一致性。

**Change**: `social-extensions`
**Verified at**: `<待填充>`
**Verifier**: `<待填充>`

---

## 1. Structural Validation (`openspec validate --all --json`)

- [ ] 全数 items `"valid": true`

**结果**：待实施后运行验证

---

## 2. Task Completion (`tasks.md`)

- [ ] 所有 `- [ ]` 已变为 `- [x]`

**未完成任务**（若有）：待实施后检查

---

## 3. Delta Spec Sync State

| Capability | Sync 状态 | 备注 |
|---|---|---|
| mutual-follow | 待 sync | 新增 capability |
| fan-analytics | 待 sync | 新增 capability |
| invite-sharing | 待 sync | 新增 capability |
| community-roles | 待 sync | 新增 capability |

---

## 4. Design / Specs Coherence Spot Check

待实施后抽样比对

---

## 5. Implementation Signal

- [ ] Worktree 内无未 staged 的文件
- [ ] 所有相关 commit 已推送

**Commit 范围**：待实施后填充

---

## Overall Decision

- [ ] PASS — 可进入 archive
- [ ] PASS WITH WARNINGS
- [ ] FAIL — 返回修正

**下一步**：运行 `/opsx:apply` 开始实施，完成后运行 `/opsx:verify` 填充此报告。
