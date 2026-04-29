# Javadoc Annotation Gap Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Improve `check_javadoc()` so class and `public` method declarations are recognized as documented when Javadoc is separated from the declaration by annotations and blank lines.

**Architecture:** Keep the checker lightweight by adding a small backward-scan helper in `standards-check.py`. Replace the current one-line `*/` check for class and `public` method declarations with the helper so both code paths share the same annotation-aware logic.

**Tech Stack:** Python 3, `re`, `pathlib`

---

### Task 1: Add annotation-aware backward scan helpers

**Files:**
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.trae/skills/ai-coding-java-springboot/scripts/standards-check.py`

- [ ] **Step 1: Add regex helpers for ignorable annotation lines**

```python
ANNOTATION_LINE = re.compile(r"^\s*@")
ANNOTATION_CONTINUATION = re.compile(r"^\s*[\w.]+\s*=|^\s*[)\]}]\s*,?\s*$|^\s*[,({].*")
```

- [ ] **Step 2: Add a predicate for ignorable lines between Javadoc and declarations**

```python
def is_ignorable_javadoc_gap_line(line: str) -> bool:
    stripped = line.strip()
    if not stripped:
        return True
    return bool(ANNOTATION_LINE.search(line) or ANNOTATION_CONTINUATION.search(line))
```

- [ ] **Step 3: Add a backward scan helper**

```python
def has_javadoc_before_declaration(lines: List[str], idx: int) -> bool:
    cursor = idx - 1
    while cursor >= 0:
        stripped = lines[cursor].strip()
        if stripped == "*/":
            return True
        if is_ignorable_javadoc_gap_line(lines[cursor]):
            cursor -= 1
            continue
        return False
    return False
```

### Task 2: Reuse the helper in `check_javadoc()`

**Files:**
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.trae/skills/ai-coding-java-springboot/scripts/standards-check.py`

- [ ] **Step 1: Replace the class-level previous-line check**

```python
if CLASS_DECL.search(line):
    if not has_javadoc_before_declaration(lines, idx):
        errors.append(f"{file_path}:{idx + 1}: 类声明前缺少 Javadoc")
    break
```

- [ ] **Step 2: Replace the public-method previous-line check**

```python
if re.search(r"\bpublic\b", line) and METHOD_DECL.search(line):
    if not has_javadoc_before_declaration(lines, idx):
        errors.append(f"{file_path}:{idx + 1}: public 方法前缺少 Javadoc")
```

### Task 3: Verify syntax and diagnostics

**Files:**
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.trae/skills/ai-coding-java-springboot/scripts/standards-check.py`

- [ ] **Step 1: Run Python syntax validation**

Run: `python3 -m py_compile /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.trae/skills/ai-coding-java-springboot/scripts/standards-check.py`
Expected: command exits with code `0`

- [ ] **Step 2: Check IDE diagnostics**

Run IDE diagnostics on the edited file and confirm there are no new syntax or lint errors.
