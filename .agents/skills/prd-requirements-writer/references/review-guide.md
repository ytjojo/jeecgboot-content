# PRD Review Guide

Use this guide when the user provides an existing PRD and asks to review, audit, normalize, or check whether it follows the template.

## Review Workflow

1. If the user provides a local Markdown file path, run `scripts/check-prd-sections.sh <path>` when available.
2. Merge mechanical missing-section output into `必改`.
3. Review for ambiguity, missing edge cases, weak metrics, weak acceptance criteria, and missing operational roles.
4. Do not rewrite immediately unless the user asks for a rewrite.

## Gap Report Structure

Group findings by:

- `必改`: blocks implementation, review, QA, or scope agreement
- `建议`: improves clarity, delivery quality, or traceability
- `可选`: useful polish or team-specific enhancement

For every finding, include:

- Location or section
- Problem
- Suggested fix

## Required Review Coverage

Check whether the PRD includes:

- Document info
- Background and problem
- Goals and measurable success metrics
- In Scope, Out of Scope, and Non-Goals
- Users, roles, scenarios, and administrator coverage where applicable
- Epic / Feature / Story hierarchy when the scope is multi-capability
- External Epic file references when such files exist
- Business flow and state transitions for multi-step logic
- Functional requirements with priority, rules, permissions, edge cases, and post-conditions
- UI states: loading, empty, error, success, interruption
- Data, events, interfaces, and observability
- Non-functional requirements with measurable targets
- Acceptance criteria in observable pass/fail form
- Risks, dependencies, assumptions, milestones, rollback, and open questions
- 词汇表 (Glossary)

## Review Heuristics

Mark as `必改` when:

- Scope boundaries are missing or contradictory.
- Success metrics are absent or not measurable.
- Functional requirements lack business rules or acceptance criteria.
- Permission or data-scope behavior is unclear.
- A multi-capability requirement lacks Epic / Feature / Story decomposition.
- Terms are repeatedly used but undefined, causing implementation or QA ambiguity.

Mark as `建议` when:

- Acceptance criteria exist but do not cover exception paths.
- Edge cases miss timeout, retry, duplicate action, empty state, or permission denial.
- Administrator scenarios are likely needed but only end-user scenarios are described.
- NFRs are present but need clearer thresholds.
- Risks or dependencies are visible but lack owners or mitigation.

Mark as `可选` when:

- The structure is usable but could improve traceability.
- A glossary term is obvious to the current team but useful for onboarding.
- A milestone or rollout plan would help coordination but does not block delivery.
