# DI002 Scope Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restrict `DI002` so `@RequiredArgsConstructor` is only flagged for `service` and `controller` classes and their family subclasses.

**Architecture:** Extend the current heuristic classifier in `standards-check.py` with one focused helper that combines path, naming, annotation, and inheritance signals. Reuse the helper in `check_java()` so the scope change stays isolated to `DI002`.

**Tech Stack:** Python 3, `re`, `pathlib`

---

### Task 1: Add family-scope detection

**Files:**
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.trae/skills/ai-coding-java-springboot/scripts/standards-check.py`

- [ ] **Step 1: Add regexes for inheritance detection**

```python
EXTENDS_SERVICE = re.compile(r"\bextends\s+\w*Service\b")
EXTENDS_CONTROLLER = re.compile(r"\bextends\s+\w*Controller\b")
```

- [ ] **Step 2: Add a helper for the DI002 scope**

```python
def is_service_or_controller_family(file_path: Path, content: str) -> bool:
    lower = str(file_path).lower()
    return (
        "/service/" in lower
        or "/controller/" in lower
        or file_path.name.endswith("Service.java")
        or file_path.name.endswith("Controller.java")
        or bool(SERVICE_ANNOTATION.search(content))
        or bool(REST_CONTROLLER.search(content))
        or bool(EXTENDS_SERVICE.search(content))
        or bool(EXTENDS_CONTROLLER.search(content))
    )
```

- [ ] **Step 3: Gate `DI002` on the new helper**

```python
if REQUIRED_ARGS.search(content) and is_service_or_controller_family(file_path, content):
    add(vs, file_path, None, "ERROR", "DI002", "禁止使用 @RequiredArgsConstructor（构造器注入），统一使用 @Resource 注入")
```

### Task 2: Verify the script remains healthy

**Files:**
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.trae/skills/ai-coding-java-springboot/scripts/standards-check.py`

- [ ] **Step 1: Run Python syntax validation**

Run: `python3 -m py_compile /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.trae/skills/ai-coding-java-springboot/scripts/standards-check.py`
Expected: command exits with code `0`

- [ ] **Step 2: Check editor diagnostics**

Run the IDE diagnostics for the edited file and confirm there are no new syntax or lint errors.
