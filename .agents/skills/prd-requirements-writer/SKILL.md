---
name: "prd-requirements-writer"
description: "Use when the user wants to create, complete, review, or normalize a PRD or 中文需求文档, especially when Epic/Feature/Story decomposition, scope, exceptions, NFRs, acceptance criteria, glossary, or quality gaps must be made explicit."
---

# PRD Requirements Writer

Generate or refine high-quality PRD and requirement documents that are complete, testable, measurable, and directly usable by product, design, engineering, and QA.

Core principle:

`A good PRD lets development avoid guessing, lets QA verify objectively, and lets the business measure outcomes.`

This skill owns linear requirement completeness. Use `wiki-requirements-writer` instead when the user explicitly wants wiki page topology, cross-links, or BR/UR/SR/DR page decomposition.

## When to Use

Use this skill when the user wants to write, complete, review, or normalize:

- PRD, MRD, BRD, feature spec, 产品需求, or structured 需求文档
- scattered notes, chat context, prototype notes, or rough ideas into a usable PRD
- audit results for ambiguity, missing edge cases, weak NFRs, or weak acceptance criteria
- implementation-ready requirements with Epic / Feature / Story hierarchy

Do not use this skill for pure technical design, schema design, or implementation plans without a product requirement layer.

## Minimum Input Rule

Before generating a full PRD, confirm there is at least:

- Product / feature name or topic
- Business goal, product goal, or problem statement

If the user only says `写一份 PRD` or `写需求文档`, ask only for the essentials: product / feature name, problem to solve, target users, and core goal or success metric.

If the user provides a partial draft, do not block on missing details. Generate or rewrite with visible placeholders such as `[请补充：xxx]`, `[待确认]`, or `TBD`.

## Progressive Disclosure

Read supporting files only when needed:

- `templates/prd-template.md`: copy-and-fill scaffold for full PRDs
- `references/writing-guide.md`: detailed section rules, Epic / Feature / Story guidance, quality checklist, and common bad patterns
- `references/review-guide.md`: gap-audit mode and review output rules
- `examples/simple-prd-demo.md`: minimal worked example
- `scripts/check-prd-sections.sh`: optional Markdown structure checker for review mode

Default workflow:

1. Identify intent: `Full PRD`, `Lean Spec`, `Rewrite`, or `Gap Audit`.
2. For generation or rewrite, load `templates/prd-template.md` and `references/writing-guide.md`.
3. For review mode, run `scripts/check-prd-sections.sh <path>` when a local Markdown file is provided, then load `references/review-guide.md`.
4. Normalize known information first; mark unknowns as placeholders instead of omitting them.
5. Ensure each major requirement has scope, priority, business rules, exceptions, NFRs, acceptance criteria, and glossary terms where needed.

## Hard Rules

- Output Markdown by default.
- Keep `In Scope` and `Out of Scope` explicit.
- Include `Non-Goals` when it prevents scope creep.
- Use Epic / Feature / Story hierarchy when requirements contain multiple capabilities or roadmap-level scope.
- If an Epic has an external source file, include a file reference under that Epic.
- Each Epic contains one or more Features; each Feature contains one or more Stories.
- Consider `系统管理员` as a role by default, especially for configuration, permission, audit, moderation, and operational management needs.
- Cover normal flow, failure flow, and interruption flow.
- Make edge cases explicit: empty state, extreme input, timeout, retry, duplicate action, concurrency, permission, compatibility.
- Use measurable statements instead of vague wording.
- Ensure every major function or Story has acceptance criteria.
- Add `词汇表 (Glossary)` near the end of the PRD for domain terms, abbreviations, statuses, and ambiguous labels.

## Recommended Structure

Use this order by default unless the user requests another template:

1. Document Info
2. Background and Problem
3. Goals and Success Metrics
4. Scope and Non-Scope
5. Users, Roles, and Scenarios
6. Requirement Hierarchy: Epic / Feature / Story
7. Business Flow and State Transitions
8. Functional Requirements
9. UI / Interaction Notes
10. Data, Events, and Interfaces
11. Non-Functional Requirements
12. Acceptance Criteria
13. Risks, Dependencies, and Assumptions
14. Milestones or Release Plan
15. 词汇表 (Glossary)
16. Open Questions

## Output Modes

- `Full PRD`: new feature or project requirements
- `Lean Spec`: smaller features where a compact but testable format is enough
- `Rewrite`: turn rough notes into a standard requirement document
- `Gap Audit`: review an existing PRD and list omissions before rewriting unless the user asks for a rewrite

## Skill Maintenance

- Keep this `SKILL.md` short; move long templates, examples, and checklists into `templates/`, `examples/`, or `references/`.
- Keep `description` aligned with real trigger phrases such as PRD, 需求文档, 产品需求, 评审 PRD, Epic, Feature, Story, and 按模板写需求.
- Prefer small scripts for mechanical checks; leave judgment-heavy issues to the review guide.
