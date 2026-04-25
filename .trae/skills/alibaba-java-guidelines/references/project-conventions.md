# Project Conventions (Template)

## 1. Package Prefix

- Root package: `com.company.project` (replace with actual group and domain).
- Suggested module layout:
  - `controller`
  - `service`
  - `service.impl`
  - `mapper`
  - `entity`
  - `dto`
  - `vo`
  - `query`
  - `exception`
  - `config`
  - `constant`

## 2. API Conventions

- Base path versioning: `/api/v1`.
- Resource naming: plural noun (`/users`, `/orders`).
- Unified response:
  - `Result.success(data)`
  - `Result.fail(code, msg)`
- Pagination:
  - Request: `PageQuery`
  - Response: `PageResult<T>`

## 3. Field and Time Conventions

- DB common fields:
  - `id BIGINT`
  - `gmt_create DATETIME`
  - `gmt_modified DATETIME`
  - `is_deleted TINYINT(1)`
- Java entity common fields:
  - `id: Long`
  - `gmtCreate: LocalDateTime`
  - `gmtModified: LocalDateTime`
  - `isDeleted: Integer`

## 4. Validation Conventions

- Input uses DTO object rather than many primitive params.
- DTO fields use standard validation annotations.
- Controller method parameter uses `@Validated`/`@Valid`.
- Validation error handled by global exception handler.

## 5. Transaction and Concurrency Conventions

- Put `@Transactional(rollbackFor = Exception.class)` at Service method level.
- Split long transactions and avoid external RPC in transaction block.
- For high-concurrency write paths, require at least one:
  - unique index idempotency,
  - optimistic lock,
  - state machine guard.

## 6. SQL and Index Conventions

- No `SELECT *`.
- Always define selected fields.
- New/changed SQL must provide `EXPLAIN`.
- `IN` list length should be limited; split batch if needed.
- Avoid function wrapping indexed columns in filtering conditions.

## 7. Logging and Exception Conventions

- Use SLF4J logger.
- Parameterized message placeholders only (`{}`).
- Prohibit sensitive data exposure in logs.
- Business exception should carry stable error code.

## 8. Optional Toolchain

- P3C plugin for local IDE check.
- Git pre-commit hook for static scan.
- CI gate with SonarQube and style checker script.
