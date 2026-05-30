# PRD Writing Guide

Use this guide when generating, completing, or rewriting a PRD.

## Section Rules

### Document Info

Include title, version, owner, date, status, related project / iteration, and change history when available.

### Background and Problem

Explain the current business problem, user pain point, supporting evidence, and why the change matters now.

### Goals and Success Metrics

Always include business goals, product goals, measurable success metrics, and baseline / target when available.

Prefer:

- `Conversion rate increases from X to Y`
- `First-screen load time <= 2s`
- `Manual processing time reduced from X min to Y min`

Avoid vague statements such as `提升体验`, `更稳定`, or `更快`.

### Scope and Non-Scope

Always split scope into:

- `In Scope`
- `Out of Scope`
- `Non-Goals`

This section is mandatory because missing scope boundaries causes requirement drift.

### Users, Roles, and Scenarios

Include target users, role differences, core use scenarios, and user stories:

`As a <role>, I want <capability>, so that <business value>.`

Consider `系统管理员` by default. If the feature has configuration, permission, audit, content governance, user management, data repair, or operational reporting needs, add administrator scenarios explicitly.

For important scenarios, add preconditions, main flow, alternate flow, exception flow, and post-conditions.

### Requirement Hierarchy: Epic / Feature / Story

Use this hierarchy when the requirement has roadmap-level scope, multiple capability groups, or needs development planning:

```markdown
## Epic

### Feature

#### Story

#### Story
```

Rules:

- Each Epic represents a large business outcome or product capability.
- If an Epic has an external source file, include `Source / Reference: [file path or link]`.
- Each Epic contains one or more Features.
- Each Feature contains one or more Stories.
- Each Story should include user value, priority, acceptance criteria, dependencies, and notable edge cases.
- Keep Feature names capability-oriented, not implementation-task-oriented.

Recommended Story fields:

```markdown
#### STORY-001 [Story Name]

- Priority: Must / Should / Could / Won't
- User / Role:
- User Value:
- Preconditions:
- Main Flow:
- Acceptance Criteria:
- Dependencies:
- Edge Cases:
```

### Business Flow and State Transitions

Use when the requirement involves approvals, orders, tasks, imports, payments, submissions, or other multi-step logic.

Capture end-to-end flow, state transitions, decision branches, timeout, retry, rollback, and compensation where needed.

### Functional Requirements

For each function, include requirement ID, name, priority, actors, trigger, preconditions, main logic, inputs / outputs, business rules, permissions, state changes, error handling, boundary conditions, and post-conditions.

Recommended mini-template:

```markdown
### FR-001 [Function Name]

- Priority: Must
- Actor:
- Trigger:
- Preconditions:
- Main Logic:
  1.
  2.
- Business Rules:
  -
- Exception / Edge Cases:
  - Empty state
  - Invalid input
  - Timeout
  - Retry
  - Duplicate submission
  - Permission denial
- Post-conditions:
```

### UI / Interaction Notes

If there is page or prototype context, include page list, key components, entry / exit points, click / submit / modal / loading / empty / error states, copywriting, and responsive differences.

Never write only `See prototype`; the PRD must still explain key logic in text.

### Data, Events, and Interfaces

Include key entities, important fields, data source / destination, event tracking, trigger timing, external interfaces, versioning, compatibility, deduplication, and retry rules when relevant.

### Non-Functional Requirements

This section must not be empty for implementation-ready requirements.

Check at least:

- Performance
- Reliability / availability
- Security / permission / compliance
- Compatibility
- Scalability
- Observability

Prefer measurable statements:

- `P95 API latency <= 200ms`
- `Availability >= 99.9%`
- `Audit logs retained for 180 days`

### Acceptance Criteria

Write criteria QA can verify directly.

Prefer `Given / When / Then`:

```markdown
- Given the user is not logged in
  When the user clicks `Buy Now`
  Then the system redirects the user to the login page
```

Acceptance criteria must be observable, pass/fail testable, cover normal and exception flow, and map back to major requirements or Stories.

### Risks, Dependencies, and Assumptions

Check external systems, third-party APIs, schedule constraints, staffing, compliance, technical uncertainty, data migration, and backward compatibility.

When possible, include risk, impact, probability, mitigation, and owner.

### Glossary

Add `词汇表 (Glossary)` near the end of the PRD.

Include:

- Domain terms
- Abbreviations
- Role names
- Business statuses
- Metrics
- Ambiguous labels that may be interpreted differently by product, engineering, or QA

### Open Questions

For each unresolved item, include question, why it blocks or affects scope, suggested owner, and suggested next step.

## Quality Checklist

Before finishing, verify:

- Every major requirement maps to at least one business goal or user scenario.
- Scope boundaries are explicit and conflict-free.
- Epic / Feature / Story hierarchy is present when the scope has multiple capability groups.
- External Epic files are referenced when provided.
- `系统管理员` has been considered and either included or explicitly marked not applicable.
- Every important user action has normal, failure, and interruption behavior.
- Edge cases include empty state, invalid input, timeout, retry, duplicate action, and permission denial where relevant.
- Non-functional requirements include measurable targets.
- Acceptance criteria are observable and testable.
- Terminology is consistent and key terms appear in the glossary.
- Priority is explicit.
- Risks and external dependencies are visible.
- Out-of-scope items are stated clearly.

## Common Omissions

Watch for these high-frequency gaps:

- Only describing the happy path
- No empty state or error state
- Missing permission matrix or data scope
- Missing administrator configuration or audit scenario
- No timeout / retry / rollback for async behavior
- No backward compatibility or migration plan
- No tracking / observability definition
- No release guardrail or rollback idea
- Prototype exists but text logic is missing
- All requirements marked highest priority
- Terms such as `有效`, `完成`, `异常`, or `管理员` are used without definition

## Common Bad Patterns

Avoid:

- `Optimize user experience`
- `System should respond quickly`
- `Support multiple scenarios`
- `Handle exceptions properly`
- `Same as competitor X`

Replace with quantified metrics, explicit rules, clear branches, and specific acceptance criteria.
