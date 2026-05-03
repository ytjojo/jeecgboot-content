---
name: "prd-requirements-writer"
description: "Use when the user wants to create, complete, review, or normalize a PRD or 中文需求文档, especially when scope, exceptions, edge cases, NFRs, acceptance criteria, or quality gaps must be made explicit."
---

# PRD Requirements Writer

## Overview

Generate or refine high-quality PRD and requirement documents that are complete, testable, measurable, and directly usable by product, design, engineering, and QA.

Core principle:

`A good PRD lets development avoid guessing, lets QA verify objectively, and lets the business measure outcomes.`

## When to Use

Use this skill when:

- The user wants to write a PRD, MRD, BRD, requirement document, feature spec, or structured需求文档.
- The user has scattered notes, chat context, prototype notes, or rough ideas and wants them normalized into a usable requirement document.
- The user wants to audit an existing PRD for completeness, ambiguity, missing edge cases, missing NFRs, or weak acceptance criteria.
- The user explicitly asks what a high-quality requirement document should contain.
- The user wants requirements that are implementation-ready and testable, not just a conceptual summary.

Do NOT use this skill when:

- The user explicitly requires wiki-style output with hierarchical wiki conventions and cross-links. In that case, prefer `wiki-requirements-writer`.
- The user only wants a pure technical design, schema design, or implementation plan without a product requirement layer.

## Output Principles

- Prefer clear, concrete, implementation-ready language.
- Separate `In Scope` and `Out of Scope`.
- Cover normal flow, failure flow, and interruption flow.
- Make edge cases explicit: empty state, extreme input, timeout, retry, concurrency, permission, compatibility.
- Use measurable statements instead of vague wording.
- Ensure every major function has acceptance criteria.
- Mark unknown items as `TBD` with owner or clarification question.

## Recommended Structure

Use this section order by default unless the user requests a different template:

1. Document Info
2. Background and Problem
3. Goals and Success Metrics
4. Scope and Non-Scope
5. Users, Roles, and Scenarios
6. Business Flow and State Transitions
7. Functional Requirements
8. UI / Interaction Notes
9. Data, Events, and Interfaces
10. Non-Functional Requirements
11. Acceptance Criteria
12. Risks, Dependencies, and Assumptions
13. Milestones or Release Plan
14. Open Questions

## Required Content by Section

### 1. Document Info

Include:

- Document title
- Version
- Author / owner
- Date
- Status
- Change history if available

### 2. Background and Problem

Describe:

- What business problem exists now
- What user pain point exists now
- What evidence supports the need
- Why this change matters now

### 3. Goals and Success Metrics

Always include:

- Business goals
- Product goals
- Measurable success metrics
- Baseline and target when available

Prefer:

- `Conversion rate increases from X to Y`
- `First-screen load time <= 2s`
- `Manual processing time reduced from X min to Y min`

### 4. Scope and Non-Scope

Always split into:

- `In Scope`
- `Out of Scope`

This section is mandatory because missing scope boundaries causes requirement drift.

### 5. Users, Roles, and Scenarios

Include:

- Target users
- Role differences
- Core use scenarios
- User stories in this form:

`As a <role>, I want <capability>, so that <business value>.`

For important scenarios, add:

- Preconditions
- Main flow
- Alternate flow
- Exception flow
- Post-conditions

### 6. Business Flow and State Transitions

Use when the requirement involves approvals, orders, tasks, imports, payments, submissions, or any multi-step logic.

Capture:

- End-to-end business flow
- State transitions
- Decision branches
- Timeout / retry / rollback / compensation where needed

### 7. Functional Requirements

This is the core section.

For each function, include:

- Requirement ID
- Requirement name
- Priority (`Must / Should / Could / Won't` or `P0 / P1 / P2`)
- Description
- Actors / roles
- Trigger
- Preconditions
- Main logic
- Inputs / outputs
- Business rules
- Permissions and data scope
- State changes
- Error handling
- Boundary conditions
- Post-conditions

Recommended mini-template:

```markdown
### FR-001 Login by SMS Code

- Priority: Must
- Actor: Visitor
- Trigger: User clicks `Login`
- Preconditions: User is not logged in
- Main Logic:
  1. User enters mobile number
  2. System validates format
  3. System sends verification code
  4. User submits code
  5. System authenticates and creates session
- Business Rules:
  - Verification code expires in 60 seconds
  - Max 5 failed attempts before temporary lock
- Exception / Edge Cases:
  - Invalid mobile number
  - Code expired
  - SMS provider timeout
  - Duplicate submission
- Post-conditions:
  - User is logged in successfully
```

## UI / Interaction Notes

If there is page or prototype context, include:

- Page list
- Key components
- Entry and exit points
- Click / submit / modal / loading / empty / error states
- Copywriting or system prompts
- Responsive or terminal-specific differences if relevant

Never write only:

- `See prototype`

The PRD must still explain key logic in text.

## Data, Events, and Interfaces

Include when relevant:

- Key entities and important fields
- Data source and destination
- Event tracking / analytics
- Trigger timing for events
- External interfaces and dependencies
- Versioning / compatibility constraints

For tracking, define:

- Event name or ID
- Trigger timing
- Required properties
- Deduplication or retry rules if needed

## Non-Functional Requirements

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

Avoid vague statements like:

- `Fast`
- `User-friendly`
- `Stable`
- `Support many scenarios`

## Acceptance Criteria

Write criteria that QA can verify directly.

Prefer `Given / When / Then`:

```markdown
- Given the user is not logged in
  When the user clicks `Buy Now`
  Then the system redirects the user to the login page
```

Rules:

- Must be observable
- Must be pass/fail testable
- Should cover normal flow and exception flow
- Should map back to major functional requirements

## Risks, Dependencies, and Assumptions

Always check:

- External systems
- Third-party APIs
- Schedule and staffing constraints
- Compliance constraints
- Technical uncertainty
- Data migration or backward compatibility risk

When possible, include:

- Risk
- Impact
- Probability
- Mitigation
- Owner

## Open Questions

Use this section instead of hiding ambiguity.

For each unresolved item, include:

- Question
- Why it blocks or affects scope
- Suggested owner
- Suggested next step

## Quality Checklist

Before finishing, verify:

- Every major requirement maps to at least one business goal or user scenario.
- Scope boundaries are explicit and conflict-free.
- Every important user action has normal, failure, and interruption behavior.
- Edge cases include empty state, invalid input, timeout, retry, duplicate action, and permission denial where relevant.
- Non-functional requirements include measurable targets.
- Acceptance criteria are observable and testable.
- Terminology is consistent across the document.
- Priority is explicit.
- Risks and external dependencies are visible.
- Out-of-scope items are stated clearly.

## Common Omissions

Watch for these high-frequency gaps:

- Only describing the happy path
- No empty state or error state
- Missing permission matrix or data scope
- No timeout / retry / rollback for async behavior
- No backward compatibility or migration plan
- No tracking / observability definition
- No release guardrail or rollback idea
- Prototype exists but text logic is missing
- All requirements marked highest priority

## Common Bad Patterns

Avoid:

- `Optimize user experience`
- `System should respond quickly`
- `Support multiple scenarios`
- `Handle exceptions properly`
- `Same as competitor X`

Replace with:

- Quantified metrics
- Explicit rules
- Clear branches
- Specific acceptance criteria

## Working Method

When using this skill:

1. Identify whether the user wants generation,补全,审校, or重构 existing requirements.
2. Collect missing essentials only if they materially block quality:
   - target users
   - business goal
   - scope boundary
   - external dependency
   - deadline or milestone
3. Normalize the requirement into the recommended structure.
4. Expand each major function with rules, exceptions, and edge cases.
5. Add NFRs, acceptance criteria, risks, and open questions.
6. Run the quality checklist before finalizing.

## Output Modes

Choose the response shape based on user intent:

- `Full PRD`: for new feature or project requirements
- `Gap Audit`: for reviewing an existing PRD and listing omissions
- `Lean Spec`: for smaller features where a compact but testable format is enough
- `Rewrite`: for turning rough notes into a standard requirement document

## Supporting Files

Use these files for fast reuse:

- `templates/prd-template.md`: standard PRD scaffold for direct copy-and-fill
- `examples/simple-prd-demo.md`: minimal worked example showing the expected output shape

Suggested usage:

1. Start from `templates/prd-template.md`
2. Fill known information first
3. Use `examples/simple-prd-demo.md` to understand the target level of detail
4. Expand edge cases, NFRs, and acceptance criteria before finalizing

## Final Reminder

A requirement document is not high quality because it is long.

It is high quality when it is:

- complete enough for delivery,
- precise enough for implementation,
- testable enough for QA,
- and measurable enough for business review.
