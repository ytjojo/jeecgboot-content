---
name: "agents-rule-architecture"
description: "Architecture decision framework for AGENTS.md rule placement (inline vs external). Invoke when designing/refactoring agent rules, optimizing rule discoverability, or deciding where a rule should live."
---

# AGENTS Rule Architecture — Inline vs External Decision Framework

This skill provides a systematic framework for deciding whether a rule should be **inlined in AGENTS.md** or **externalized to a separate document**, how to design anti-skip references, and how to structure the optimal rule hierarchy.

---

## When to Invoke

- Designing a new AGENTS.md or restructuring an existing one
- Deciding where to place a new agent rule
- Audit existing rules for compliance vs discoverability trade-offs
- Experiencing "rule skip" problems (agents not following externalized rules)
- Refactoring rule hierarchy to reduce cognitive load

---

## 1. Three-Dimensional Assessment Model

Evaluate every rule across three orthogonal dimensions:

| Dimension | Inline Signal | External Signal |
|-----------|---------------|-----------------|
| **Timing** | Needed *before* making decisions | Only needed *during* execution |
| **Frequency** | Triggered on every task | Triggered occasionally in specific scenarios |
| **Consequence Asymmetry** | Violation cost is high (data loss, architectural corruption, wrong merges) | Violation cost is low (format issues, easily fixable) |

### The Consequence Asymmetry Test

The single most important question:

> "If the agent completes the task without knowing this rule, what is the worst-case outcome?"

- **Irreversible outcome** (wrong merge, destructive change, data corruption) → **INLINE**
- **Fixable outcome** (format error, missing comment, style issue) → **External candidate**

---

## 2. Additional Decision Dimensions

### 2.1 Activation Condition Breadth

- **Unconditional activation** ("before modifying any file") → **INLINE**
- **Narrow condition** ("when modifying Android Views") → **External to module-level**

Unconditional rules stay inline so agents don't waste tokens judging applicability. Narrow rules in the main file create noise — agents spend attention deciding "am I modifying Android Views right now?"

### 2.2 Audience of the Rule

- **Used by main agent during orchestration decisions** → AGENTS.md
- **Used by subagent during specific task execution** → Module-level or task-level injection
- **Used by both** → Main file writes **executable summary**, external file writes full text

**Executable summary principle**: Never write "see docs/xxx.md". Instead write "branch format must be `feat/xxx`, full spec at docs/xxx.md". The summary must be directly actionable without jumping.

### 2.3 Change Frequency

- **Stable** (architectural constraints, tech stack choices) → **INLINE**
- **Volatile** (API interfaces, format conventions) → **EXTERNAL**

Volatile rules in the main file have extremely high maintenance cost; version inconsistency is more dangerous than rule absence.

---

## 3. Anti-Skip Reference Language Design

### ❌ Skip-Prone Reference Pattern

```markdown
See docs/agent-context/git-workflow.md for details
```

This fails because: no cost signal, no summary, agent can "reasonably infer" to skip it.

### ✅ Anti-Skip Reference — Four Required Elements

```markdown
## Git Workflow [BLOCKING]

**Minimum required rules (execute directly):**
- Branch format: `feat/xxx` or `fix/xxx`
- Must pass CI before merge
- No direct pushes to main

**Full specification (must confirm before execution):**
→ docs/agent-context/git-workflow.md
  Contains: conflict resolution / worktree switching rules / auto-commit triggers
```

| Element | Purpose |
|---------|---------|
| **Severity tag** (`[BLOCKING]`/`[REQUIRED]`/`[ADVISORY]`) | Enables tiered triage under token pressure |
| **Executable minimum summary** | Covers 80% of scenarios without jumping |
| **Jump value declaration** | Tells agent what's inside, helping them judge "do I need this for my task?" |
| **Trigger condition upfront** | Don't bury "read this when you encounter X" at the end; state it directly at the reference |

---

## 4. Optimal Hierarchy Depth: 2.5 Layers

Theoretical three-layer structure (AGENTS.md → module-level → external docs) gives agents two skip opportunities. Recommended structure:

```
AGENTS.md (main file)
├── Global constraints: inlined directly, no references
├── Module constraint summaries + [REQUIRED] references
│   └── docs/agent-context/xxx.md (single hop, flat)
└── Task-level context (injected via hooks, not counted as "references")
```

### Key Design Decisions

1. **Main file to external files: one hop only** — external files must be self-contained and must not reference further documents
2. **External docs are "terminals"** — write them to be self-sufficient; no reader should need to chase further files
3. **Task-level context via hook injection** — bypasses the multi-hop problem entirely because agents receive it passively, not through active reading

### What "2.5 Layers" Means

- **Layer 1**: AGENTS.md core constraints (directly executable)
- **Layer 2**: Module-level documents (one hop away)
- **Layer 0.5**: Hook-injected task context (doesn't count as a "hop" because it's passively received)

Hook injection is far more effective than optimizing reference language for combating multi-hop issues — because it eliminates the need for agents to actively read at all.

---

## 5. Practical Classifier — Three Questions

For any rule, ask in order:

**Q1: Will the agent make incorrect architectural/process decisions without knowing this rule?**
- YES → **INLINE**

**Q2: Is the violation consequence irreversible?**
- YES → **INLINE**

**Q3: Does this rule activate in every task?**
- YES + rule is short → **INLINE**
- YES + rule is long → Write executable summary in main file, externalize full text
- NO → **External to module-level**, inject via hooks on demand

---

## Root Cause Diagnosis for "Skip Rate" Problems

When agents fail to follow externalized rules, the root cause is almost always:

> These rules satisfy Q2 (high consequence) but have been incorrectly placed in positions requiring multiple hops to activate.

Treating consequence asymmetry as a forced-inline trigger is the most direct fix.

---

## Output Format

When applying this framework to audit or restructure rules, produce a structured output:

```
Rule Audit Report
=================

Rule: [rule name]
- Q1 (Decision-critical?): YES/NO → recommendation
- Q2 (Irreversible consequence?): YES/NO → recommendation
- Q3 (Every-task activation?): YES/NO → recommendation
- Final placement: INLINE / SUMMARY+EXTERNAL / EXTERNAL
- Severity tag: [BLOCKING] / [REQUIRED] / [ADVISORY]
- Executable summary (if externalized): [the 2-3 line actionable version]
- Reference: [path to external doc, with value declaration]
```
