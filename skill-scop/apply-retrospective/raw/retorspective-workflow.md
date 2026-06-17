verify
    generates: verify.md
    description: Post-implementation verification against specs, design, and tasks
    template: verify.md
    instruction: |
      PRECHECK — implementation evidence:
      Before producing verify.md, run BOTH commands. If either
      returns 0, STOP and tell the user that apply phase has not
      yet produced reviewable changes.

      1. Commit evidence (must return > 0):
         git log --oneline $(git merge-base HEAD origin/main 2>/dev/null || git merge-base HEAD origin/master 2>/dev/null)..HEAD | wc -l

      2. Task progress (must return > 0):
         grep -c '^- \[x\]' openspec/changes/<change-name>/tasks.md

      Only after BOTH return positive numbers, proceed to invoke
      the verification skill below.

      Use the Skill tool to invoke **openspec-verify-change** (the
      `/opsx:verify` slash command is its user-facing equivalent).

      IMPORTANT timing note:
      - Unlike other artifacts, verify.md is produced AFTER the apply
        phase completes, NOT during planning. The `requires: [plan]`
        edge exists only for schema graph purposes — the actual
        verification step MUST run on a completed implementation.

      The verify step MUST perform the following checks and record
      results in verify.md using the template:

      1. **Structural validation**: Run `openspec validate --all --json`
         in the repository root and confirm every item returns
         `"valid": true`. If any item fails, record the issues and
         return to fix the underlying artifact before proceeding.

      2. **Task completion**: Confirm every checkbox in tasks.md is
         `- [x]`. For any `- [ ]` remaining, document the reason
         (e.g. manual / out-of-scope / blocked) and whether it blocks
         archive.

      3. **Delta spec sync state**: For each directory under
         `openspec/changes/<name>/specs/`, compare against the
         corresponding `openspec/specs/<capability>/spec.md` and
         record one of:
         - ✓ Already synced
         - ✗ Needs sync (list the capabilities)
         - N/A (no delta specs produced)

      4. **Design/specs coherence**: Spot-check that design.md
         decisions reference or align with the requirements listed
         in specs/. Record any drift as a warning (non-blocking).

      5. **Implementation signal**: Confirm all code changes are
         committed (no unstaged files in the worktree). Cite the
         commit range if known.

      6. **Front-door routing leak detector** (warning, non-blocking):

         For projects using superpowers-bridge, design output
         should never land in `docs/superpowers/specs/` — the
         brainstorm artifact's output redirection (see this
         schema's brainstorm.instruction) routes it to
         `openspec/changes/<name>/brainstorm.md`. Run:

           ls docs/superpowers/specs/*.md 2>/dev/null

         If any files exist, record a WARNING: "Front-door routing
         leak — design output found at docs/superpowers/specs/...;
         these belong in openspec/changes/<name>/brainstorm.md or
         design.md. Move/delete after confirming content is captured
         in the change directory."

         Non-blocking — adopters may have legitimate non-schema use
         of that directory predating schema install. Surfaces the
         leak so the user can decide whether to clean up.

      7. **Deferred dogfood vs automated-test equivalence**:

         If plan.md has any tasks marked `[~]` deferred (manual smoke /
         dogfood / live-environment checks that weren't run in this
         cycle), enumerate each in verify.md §7 and identify the
         equivalent automated test that covers the same assertions.

         If no equivalent automated test exists for a deferred manual
         check, that row is a real coverage gap — record in
         retrospective Misses with a follow-up plan, not silently
         deferred.

         The decision table makes "deferred-but-covered" vs "deferred-
         and-gapped" visible at archive time, so reviewers can audit
         completeness without re-deriving the rationale from plan +
         test files.

         Non-blocking on its own — Overall Decision remains PASS even
         with identified gaps, provided they're recorded as follow-up.
         Blocks only if §7 is empty AND plan.md has `[~]` rows (means
         the gap analysis was skipped, not that gaps don't exist).

      Verify MAY be re-run multiple times as the implementation is
      refined — each run overwrites verify.md with current state.

      If `openspec-verify-change` skill is unavailable, fall back to
      running the numbered checks above manually and recording results
      in verify.md.