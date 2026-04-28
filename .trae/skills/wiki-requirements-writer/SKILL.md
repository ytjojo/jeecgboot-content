---
name: "wiki-requirements-writer"
description: "Generates wiki-structured requirements documents with templates and acceptance criteria. Invoke ONLY when the user explicitly requests wiki-style structure/format (e.g., 'wiki-style', 'wiki结构', Confluence/Notion wiki, hierarchical headings, cross-links)."
---

# Wiki Requirements Writer

Generate and refine requirement documents in wiki structure for product, platform, and engineering collaboration.

## When to Invoke

Invoke this skill ONLY when the user explicitly requests wiki-style requirements output, for example:
- The user explicitly asks to write a wiki-style requirement document (PRD/MRD/BRD) or a "wiki page" version.
- The user explicitly asks for wiki-style organization (hierarchical headings/TOC and cross-links).
- The user explicitly asks to convert/normalize an existing draft into a wiki-style standard template.
- The user explicitly asks for acceptance criteria, scope boundaries, or delivery milestones in wiki-style format.

## Output Principles
- Follow wiki-style organization (hierarchical headings and cross-links).
- Use concise, implementation-ready language.
- Keep one topic per section and avoid duplicate statements.
- Mark uncertain items as `TBD` with owner and expected date.
- Include measurable acceptance criteria for each major capability.
- Explicitly separate `In Scope` and `Out of Scope`.

## Recommended Wiki Structure

Use the following section order by default:

1. Background
2. Goals and Non-Goals
3. Scope
4. Personas and Roles
5. User Stories
6. Business Rules
7. Functional Requirements
8. Non-Functional Requirements
9. Data and Interfaces
10. Workflow / State Transitions
11. Acceptance Criteria
12. Milestones and Plan
13. Risks and Dependencies
14. Open Questions

## Section Template

For each section:
- `Objective`: why this section exists
- `Content`: key requirement statements
- `Constraints`: boundaries, assumptions, dependencies
- `Acceptance`: how completion is verified

## Requirement Statement Pattern

Prefer normalized requirement sentences:

- `The system SHALL ...`
- `The system SHALL NOT ...`
- `The system SHOULD ...` (non-mandatory optimization)

## User Story Pattern

Use:

`As a <role>, I want <capability>, so that <business value>.`

And attach:
- Preconditions
- Main flow
- Alternate flows
- Exceptions

## Acceptance Criteria Pattern

Use testable criteria:

- Given ...
- When ...
- Then ...

Requirements:
- Observable result
- Measurable threshold where applicable
- Clear pass/fail judgment

## Wiki Linking Conventions

- Use stable page names: `Module - Topic - Version`
- Add index page with backlinks to all child pages
- Keep glossary terms centralized and referenced, not duplicated

## Prompt Inputs To Ask For

If key context is missing, ask for:
- Target users and roles
- Core business process
- Deadline and milestone constraints
- Existing system boundaries and external dependencies
- Compliance/security/performance constraints

## Example Skeleton

```markdown
# Project X Requirements

## 1. Background
...

## 2. Goals and Non-Goals
### 2.1 Goals
...
### 2.2 Non-Goals
...

## 3. Scope
### 3.1 In Scope
...
### 3.2 Out of Scope
...
```

## Quality Checklist

Before finishing, verify:
- Every functional item maps to at least one acceptance criterion.
- Non-functional requirements include measurable targets.
- Scope boundaries are explicit and conflict-free.
- Risks have mitigation owner and trigger condition.
- Open questions have owner and due date.
