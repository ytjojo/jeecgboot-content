# Clarification Guide

Use this guide before generating or rewriting requirements when the user input is vague, partial, or could lead to multiple product or engineering interpretations.

For `Gap Audit` or review mode, do not block the review by asking questions first. Convert ambiguity or missing information into review findings under `必改` or `建议`. Ask a separate clarification list only when the user asks to rewrite, finalize, or continue into a PRD draft.

## Internal Self-Questioning

Ask these questions silently and in order:

1. `理解完整性`: Do I fully understand the outcome the user wants?
2. `歧义排查`: Can any requirement sentence be interpreted in more than one implementation direction?
3. `遗漏排查`: Are key details missing, such as boundary conditions, exception handling, input source, output destination, permissions, data retention, or empty states?
4. `逻辑排查`: Are there contradictions, missing steps, unclear dependencies, or unexplained preconditions?
5. `假设排查`: Am I relying on hidden assumptions the user has not actually confirmed?

## When to Ask vs. Proceed

- Ask clarifying questions first when the uncertainty changes scope, user flow, data model, interface dependency, permission behavior, acceptance criteria, release risk, or implementation cost.
- Proceed with visible placeholders when the user provided enough direction to draft safely; include unresolved items as `[待确认]`, `[请补充：xxx]`, or in `开放问题 / Open Questions`.
- Do not ask a long questionnaire. Group related unknowns and ask only questions that materially affect the PRD.

## Clarification Output Format

When material uncertainty should be asked first under the rules above, output this structure before drafting or before the final rewrite:

```markdown
为确保准确实现，请确认以下几点：

1. 【歧义】你提到的"用户登录"，是指手机号+验证码，还是账号+密码？
2. 【遗漏】数据为空时，页面应展示空状态还是占位提示？
3. 【逻辑缺失】是否需要保留历史记录？如需要，保留多长时间？
4. 【依赖】该功能是否依赖已有某个接口？若没有，是否需要一并实现？
```

## Question Labels

Use these labels consistently:

- `【理解】`: desired outcome, target user, or success standard is unclear
- `【歧义】`: one phrase can map to multiple product behaviors
- `【遗漏】`: boundary, exception, input/output, permission, data, UI state, or NFR detail is absent
- `【逻辑缺失】`: process step, state transition, dependency, or pre/post-condition is missing
- `【假设】`: an unstated default assumption needs confirmation
- `【依赖】`: API, third-party system, data source, configuration, or release dependency is unclear

## Placement in PRD

If the user asks to continue despite uncertainty, keep the draft moving and place unresolved questions in `开放问题 / Open Questions` with:

- 问题
- 影响或原因
- 建议 owner
- 下一步
