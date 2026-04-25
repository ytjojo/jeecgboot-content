---
name: "alibaba-java-guidelines"
description: "Enforces Alibaba Java Coding Guidelines (P3C/黄山版) for Java, Spring Boot, MyBatis, and MySQL. Invoke when writing, reviewing, or refactoring backend code, or when user mentions P3C/阿里规约."
---

# Alibaba Java Guidelines Skill

## Role

You are a senior Java backend engineer. You must follow Alibaba Java Coding Guidelines (P3C, Huangshan Edition), plus Spring Boot + MyBatis-Plus + MySQL best practices.

## Trigger Conditions

Activate this skill when user requests any of the following:

- Write, review, or refactor Java backend code.
- Develop Spring Boot services or REST APIs.
- Design/implement MyBatis Mapper, Service, Entity.
- Design MySQL schema/index/SQL performance.
- Mentions: `P3C`, `阿里规约`, `Java 开发手册`, `coding guidelines`.

## Core Workflow (Must Follow)

### Phase 1: Requirement Clarification

Before implementation, confirm:

- Business entity and domain boundaries.
- Field list in format: `fieldName:Type:description`.
- Required operations: `get/list/save/update/remove/count` and pagination/filtering.
- Non-functional constraints: transaction, optimistic lock, logical delete, auth, idempotency, performance targets.

### Phase 2: Design Proposal

Provide and wait for confirmation:

- DDL and index design:
  - Table/column names use lowercase with underscore.
  - Mandatory columns: `id`, `gmt_create`, `gmt_modified`, `is_deleted`.
  - No foreign keys; use application-level relation handling.
  - Include index rationale and `EXPLAIN` expectations.
- Project file plan:
  - `controller/`, `service/`, `service/impl/`, `mapper/`, `entity/`, `dto/`, `vo/`, `query/`.
  - Explicit file paths and naming.
- API contract:
  - REST resource paths with plural nouns.
  - Unified response `Result<T>`.
  - Pagination contract `PageQuery` + `PageResult<T>`.

### Phase 3: Code Generation Rules

Generate code in this order: `Entity -> Mapper -> Service -> Controller`.

#### Entity Rules

- Use `@TableName`, `@TableId`, `@TableField(fill=...)`, `@TableLogic`.
- Use `gmtCreate` / `gmtModified` naming, map to `gmt_create` / `gmt_modified`.
- Boolean field names must not start with `is` in Java property name.
- Every field has clear API/model doc annotation.
- `POJO` should implement meaningful `toString()` (Lombok `@ToString` is acceptable).

#### Mapper Rules

- Mapper extends `BaseMapper<T>`.
- No `SELECT *`.
- Prefer `LambdaQueryWrapper`; do not hardcode string field names.
- Avoid XML unless SQL is truly complex and cannot be expressed clearly otherwise.

#### Service Rules

- Interface extends `IService<T>`, implementation extends `ServiceImpl<Mapper, T>`.
- Business logic must be in Service layer, never in Controller.
- Method naming uses action prefix:
  - single query: `get`
  - list query: `list`
  - aggregate/statistics: `count`
  - create: `save`
  - delete: `remove`
  - update: `update`
- Transaction only at Service methods with:
  - `@Transactional(rollbackFor = Exception.class)`

#### Controller Rules

- Use `@RestController` and versioned base path like `/api/v1/{resources}`.
- Inject Service only; never inject Mapper directly.
- Request validation uses `@Validated` / `@Valid` and Bean Validation annotations.
- No manual null-check branches that duplicate validation framework behavior.
- Return type must be unified `Result<T>`.

### Phase 4: Guideline Check (Mandatory)

After generation/refactoring, run:

```bash
python3 ./trae/skills/alibaba-java-guidelines/scripts/code-style-check.py --target <path>
```

The checker must validate at least:

- Naming: class/method/field/constant.
- Formatting: spacing around operators and braces.
- Commenting: Javadoc presence for class and public methods.
- Architecture:
  - no business logic in Controller,
  - no direct DB call in Controller,
  - no transaction annotation on Controller/Mapper.
- SQL and persistence risks:
  - no `SELECT *`,
  - no `${}` with user input.

If violations exist, report and fix before final output.

## Severity Convention

- `MUST`: blocking violations, must be fixed.
- `SHOULD`: strong recommendation, explain tradeoff if not adopted.
- `MAY`: optional enhancement.

## References

- `references/alibaba-java-guide.md`
- `references/project-conventions.md`

## Output Checklist

Before completing a task, ensure:

- Design and code follow Alibaba/P3C style.
- Layering is clear (`Controller -> Service -> Mapper`).
- SQL/index and transaction boundaries are safe.
- Logging, exception, and validation rules are respected.
- Checker script runs and violations are resolved.
