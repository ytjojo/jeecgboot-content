---
name: "wiki-requirements-writer"
description: "Use when the user explicitly asks for wiki风格需求文档, Confluence/Notion/飞书/语雀层级页面, cross-linked specs, or BR/UR/SR/DR traceable requirements."
---

# Wiki Requirements Writer

## Overview

Write and normalize requirement documents in wiki style so they become a navigable, cross-linked, layered knowledge base.

Core principle:

`This skill owns wiki packaging, hierarchy, page boundaries, cross-links, and traceability. It does not own generic PRD completeness by default.`

## When to Use

Use this skill when:

- The user explicitly asks for `wiki` style requirements, wiki pages, or Confluence/Notion/飞书/语雀 style structure.
- The user wants layered requirement decomposition with parent-child relationships.
- The user wants requirements organized with cross-links, page indexes, reverse links, or traceability matrices.
- The user wants an existing PRD or draft normalized into a navigable wiki knowledge base.
- The user wants requirements optimized for AI agents, with explicit schema, rules, states, exceptions, and references to existing systems.

Do NOT use this skill when:

- The user only wants a normal linear PRD, sectioned requirement doc, or single-document spec without wiki hierarchy.
- The user only wants a technical design or implementation plan.
- The user only wants a short feature summary with no traceability requirement.

## Boundary With `prd-requirements-writer`

Use `wiki-requirements-writer` when the primary problem is document shape:

- Multi-page hierarchy
- Parent-child requirement decomposition
- Cross-links and reverse links
- Knowledge-base navigation
- BR/UR/SR/DR traceability
- Confluence/Notion/飞书/语雀 style page organization

Prefer `prd-requirements-writer` when the primary problem is content quality inside a mostly linear document:

- Scope clarity
- Edge cases
- NFR completeness
- Acceptance criteria quality
- Ambiguity reduction in a normal PRD

If the user wants both, use this skill only when wiki structure is explicitly required; otherwise prefer `prd-requirements-writer`.

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

## Wiki-Specific Required Elements

For each wiki requirement set, make these wiki-specific elements explicit:

### 1. Index And Navigation

- Entry page or index page
- Page list or domain map
- Stable naming convention
- Shared links to glossary, NFR page, or error matrix when relevant

### 2. Page Boundaries

- One topic per page
- Clear page purpose
- No mixed layers on the same page unless the user explicitly wants a compact output

### 3. Parent-Child Traceability

- Parent page lists `下级实现`
- Child page declares `上级来源`
- Cross-links use stable IDs
- Global traceability matrix exists when the set spans multiple pages or levels

### 4. Shared Canonical Pages

- Glossary page for terms
- Global NFR page for shared constraints
- Error matrix page for reusable error handling
- Index page for navigation and reverse links

### 5. Layering Strategy

- Lightweight wiki hierarchy for smaller scopes
- Full `BR/UR/SR/DR` hierarchy for large, cross-team, or traceable initiatives
- No fake decomposition just to satisfy the four-level pattern

For section-level completeness inside each page, reuse the quality bar from `prd-requirements-writer`. This skill focuses on how pages are split, linked, and navigated.

## Agent-Ready Additions

When the requirement is meant to guide coding agents, explicitly add:

- Domain entities or schema definitions
- State machine or lifecycle transitions
- Error code / exception matrix
- Business assertions that must never be violated
- Existing code or module references to follow

These additions are optional in normal wiki pages, but become important when the wiki will be used directly by coding agents.

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
- Keep wiki-specific value high; do not duplicate long linear PRD prose across every page.
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
5. Borrow the content-quality bar from `prd-requirements-writer` only where needed
6. Run `checklists/wiki-quality-checklist.md` before finalizing

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
4. Fill only the minimum content required to make each page navigable and traceable.
5. Add agent-ready sections when the document will be used for coding or code generation.
6. If the user also needs stronger linear requirement completeness, prefer `prd-requirements-writer`.
7. Run the quality checklist before finalizing.

## Quality Checklist

Before finishing, verify:

- The hierarchy is explicit and not contradictory.
- Parent-child references are bidirectional where needed.
- Every major page has a clear role in the hierarchy.
- Shared information is centralized instead of duplicated everywhere.
- Scope boundaries are explicit.
- IDs are unique and stable.
- Open questions have owner or next step.
- Terms, labels, and statuses are consistent.

## Common Failures

Watch for these problems:

- A long wiki page with headings but no real hierarchy
- Child requirements listed without parent references
- Long linear PRD prose copied into every child page
- No clear separation between index pages, parent pages, and atomic requirement pages
- No links to design, API, or tests
- Agent-facing docs that omit schema, assertions, or error handling

## Final Reminder

Good wiki requirements are discoverable, traceable, and navigable.

Let `prd-requirements-writer` own generic requirement completeness.
Let this skill own wiki structure and traceability.
