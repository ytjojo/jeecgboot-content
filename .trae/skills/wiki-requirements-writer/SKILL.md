---
name: "wiki-requirements-writer"
description: "Use when the user explicitly asks for wiki风格需求文档, Confluence/Notion/飞书/语雀层级页面, cross-linked specs, or BR/UR/SR/DR traceable requirements."
---

# Wiki Requirements Writer

## Overview

Write and normalize requirement documents in wiki style so they are easy to navigate, easy to trace, and precise enough for product, engineering, QA, and AI coding agents.

Core principle:

`A wiki requirement is not just a document. It is a linked knowledge structure with explicit hierarchy, traceability, constraints, and testable acceptance criteria.`

## When to Use

Use this skill when:

- The user explicitly asks for `wiki` style requirements, wiki pages, or Confluence/Notion/飞书/语雀 style structure.
- The user wants layered requirement decomposition with parent-child relationships.
- The user wants requirements organized with cross-links, page indexes, reverse links, or traceability matrices.
- The user wants an existing PRD or draft normalized into a navigable wiki knowledge base.
- The user wants requirements optimized for AI agents, with explicit schema, rules, states, exceptions, and references to existing systems.

Do NOT use this skill when:

- The user only wants a normal linear PRD without wiki hierarchy.
- The user only wants a technical design or implementation plan.
- The user only wants a short feature summary with no traceability requirement.

## Common Chinese Triggers

Typical user phrases that should trigger this skill:

- `帮我写 wiki 风格需求文档`
- `整理成 Confluence 页面结构`
- `转成 Notion/飞书/语雀知识库格式`
- `按 BR/UR/SR/DR 分层输出`
- `做一个可追溯的需求 wiki`
- `把 PRD 改成分层、可跳转、可追踪的需求文档`
- `生成适合 AI Agent 理解的 wiki 需求`

## Output Principles

- Prefer hierarchical pages over one long narrative.
- Keep one topic per page or section.
- Make parent-child relationships explicit in both directions.
- Separate `In Scope` and `Out of Scope`.
- Use measurable, testable statements instead of vague language.
- Mark unknown items as `TBD` with owner and next action.
- Ensure major requirements map to acceptance criteria.
- Prefer stable IDs, tables, and predictable headings for agent parsing.

## Recommended Hierarchy

Use one of these two patterns depending on the user's granularity:

### Lightweight Wiki Pattern

1. Domain
2. Feature Area
3. Feature
4. Requirement Item

Example:

```text
Order Domain
└── Checkout
    ├── Coupon Settlement
    └── Address Validation
```

### Four-Level Requirement Pattern

1. `BR-XXX`: Business Requirement
2. `UR-XXX`: User Requirement
3. `SR-XXX`: System Requirement
4. `DR-XXX`: Detailed Requirement

Default mapping rules:

- One `BR` should decompose into `3-5` `UR`
- One `UR` should decompose into `2-4` `SR`
- One `SR` should decompose into `3-6` `DR`

If the user's material does not support this depth, keep the structure shallower rather than inventing fake layers.

## Traceability Rules

Every wiki requirement page should support both directions:

- Parent to child: list child pages under `下级实现` or `Child Requirements`
- Child to parent: declare `上级来源` or `Parent Source` near the top
- Cross reference format: `[[ID|Title]]`
- Add a global traceability matrix when the document spans multiple pages or levels

Minimum traceability expectations:

- Requirement to requirement
- Requirement to design/prototype
- Requirement to API/data contract
- Requirement to test/acceptance
- Requirement to bug/change request when relevant

## Required Sections

For each major requirement page, include these blocks unless the user explicitly wants a lean version:

### 1. Metadata

Include:

- Requirement ID
- Title
- Requirement type
- Priority
- Status
- Owner / proposer
- Stakeholders
- Version
- Create / update date

### 2. Requirement Description

Include:

- Background and pain point
- Quantified goal or value
- User role or affected actor
- User story: `As a <role>, I want <capability>, so that <value>.`
- Preconditions
- Main flow
- Alternate / exception flow
- Business rules

### 3. Acceptance

Prefer `Given / When / Then`.

Each major function should have:

- Observable result
- Clear pass/fail judgment
- Measurable threshold when applicable

### 4. Relationships

Include:

- Dependencies
- Conflicts
- Impacted modules / tables / APIs / pages
- Related documents

### 5. Management

Include:

- Change history
- Review record
- Risk assessment
- Open questions

### 6. Appendix

Include as needed:

- Glossary
- Assumptions
- Constraints
- Reference links

## Agent-Ready Additions

When the requirement is meant to guide coding agents, explicitly add:

- Domain entities or schema definitions
- State machine or lifecycle transitions
- Error code / exception matrix
- Business assertions that must never be violated
- Existing code or module references to follow

This reduces ambiguity and helps the agent generate code that matches the current system instead of guessing.

## Prompt Inputs To Ask For

If critical context is missing, ask only for information that materially affects the wiki structure:

- Target users and roles
- Business goal and measurable success metric
- Desired hierarchy depth: lightweight wiki or full `BR/UR/SR/DR`
- Existing system boundaries and external dependencies
- Key rules, state transitions, and exception handling
- Whether the output is for humans only or for AI-agent coding consumption

## Writing Rules

- Use IDs consistently; never reuse retired IDs.
- Avoid vague words like `optimize`, `fast`, `friendly`, `support multiple scenarios`, `properly handle exceptions`.
- Quantify NFRs such as latency, availability, audit retention, concurrency, and security constraints.
- Keep terminology consistent across all pages.
- If a page references global NFRs or glossary terms, link to the canonical page instead of duplicating text.

## Page Conventions

Recommended page naming:

- `[BR-001] Improve Checkout Conversion`
- `[UR-003] Quick Order Submission`
- `[SR-002] Cart Service`
- `[DR-014] Coupon Validation Rule`

Recommended labels:

- Layer labels: `L1`, `L2`, `L3`, `L4`
- Status labels: `draft`, `reviewed`, `approved`, `implemented`, `deprecated`
- Priority labels: `must`, `should`, `could`, `wont`
- Type labels: `functional`, `non-functional`, `data`, `ui`, `business-rule`

## Suggested Skeleton

```markdown
---
标签: functional, approved, must
上级来源: [[UR-001|快速下单流程]]
---

# SR-001 购物车管理

## 目标与背景
...

## 范围
### In Scope
...
### Out of Scope
...

## 业务规则
...

## 子需求列表
- [[DR-001|商品加入购物车校验规则]]
- [[DR-002|购物车数量修改逻辑]]

## 验收标准
- Given ...
  When ...
  Then ...

## 关联文档
- [[购物车原型图]]
- [[购物车API设计]]
```

## Supporting Files

Use these files for fast reuse:

- `index.md`: entry point for humans and agents to understand the package structure and recommended usage
- `templates/wiki-requirements-template.md`: standard wiki requirement scaffold with index page, layered pages, traceability, and agent-ready sections
- `examples/simple-wiki-demo.md`: minimal worked example showing BR/UR/SR/DR decomposition and wiki linking
- `checklists/wiki-quality-checklist.md`: reusable review checklist for auditing completeness, traceability, testability, NFRs, and agent-readiness

Suggested usage:

1. Start from `index.md` to choose the right mode
2. Use `templates/wiki-requirements-template.md` to draft the structure
3. Determine whether the material needs lightweight hierarchy or full `BR/UR/SR/DR`
4. Use `examples/simple-wiki-demo.md` to match the expected page shape and traceability style
5. Run `checklists/wiki-quality-checklist.md` before finalizing

## Output Modes

Choose the response shape based on user intent:

- `Wiki Full Spec`: for new projects, complex modules, or multi-page layered requirements
- `Wiki Rewrite`: for converting rough notes or a linear PRD into wiki structure
- `Wiki Gap Audit`: for reviewing an existing wiki or PRD and listing structure, traceability, and quality gaps
- `Wiki Lean Spec`: for smaller features that still need IDs, cross-links, scope boundaries, and acceptance criteria

## Working Method

When using this skill:

1. Confirm whether the user wants generation, rewrite, or audit of wiki requirements.
2. Determine the required hierarchy depth from the source material.
3. Normalize naming, IDs, headings, and traceability links.
4. Fill missing essentials: scope, actors, rules, exceptions, NFRs, acceptance.
5. Add agent-ready sections when the document will be used for coding or code generation.
6. Run the quality checklist before finalizing.

## Quality Checklist

Before finishing, verify:

- The hierarchy is explicit and not contradictory.
- Parent-child references are bidirectional where needed.
- Every major function has acceptance criteria.
- NFRs are measurable, not vague.
- Rules, exceptions, and edge cases are visible.
- Scope boundaries are explicit.
- IDs are unique and stable.
- Open questions have owner or next step.
- Terms, labels, and statuses are consistent.

## Common Failures

Watch for these problems:

- A long wiki page with headings but no real hierarchy
- Child requirements listed without parent references
- Only happy path is described
- No exception handling or no lifecycle/state rules
- No links to design, API, or tests
- Agent-facing docs that omit schema, assertions, or error handling

## Final Reminder

Good wiki requirements are discoverable, traceable, and executable.

They let humans navigate quickly and let AI agents implement with fewer guesses.
