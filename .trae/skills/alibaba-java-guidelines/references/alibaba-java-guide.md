# Alibaba Java Guide (Backend Excerpt)

## 1. Baseline Stack

- Java: `17+` (production on LTS).
- Spring Boot: `3.2+`.
- MyBatis-Plus: `3.5.7+`.
- MySQL: `8.0+` with `utf8mb4` and `InnoDB`.
- Tooling: IDEA P3C plugin + SonarQube quality gate.

## 2. Naming and Layering Rules

### MUST

- Table and column names use lowercase with underscore (`user_account`, `gmt_create`).
- Java class names use UpperCamelCase (`OrderServiceImpl`).
- Method/variable names use lowerCamelCase and start with verb for actions.
- Constant names use UPPER_CASE_UNDERSCORE.
- Keep responsibilities separate:
  - `DO` for persistence mapping,
  - `DTO` for API data transfer,
  - `VO` for view layer.
- Layered architecture:
  - `controller` handles request/response only,
  - `service` contains business logic,
  - `mapper` handles persistence access.

### SHOULD

- `POJO` classes provide readable `toString()`.
- Keep package structure stable and domain-oriented.

## 3. Spring Boot Rules

### MUST

- Use profile-based config via `application.yml` + `spring.profiles.active`.
- Avoid hardcoded values; bind with `@ConfigurationProperties`.
- Sensitive config (password/key/token) must not be committed.
- REST API should use plural resources and semantic HTTP verbs.
- Unified response body uses `Result<T>`.
- Validation uses `@Validated` + Bean Validation annotations.
- Convert validation errors by global exception handler.

### SHOULD

- Async methods use dedicated thread pool.
- Scheduled jobs include idempotency/distributed lock control.

## 4. MyBatis-Plus Rules

### MUST

- Entity uses `@TableName`, `@TableId`, `@TableLogic`, `@TableField(fill=...)`.
- Must implement `MetaObjectHandler` for `gmt_create` and `gmt_modified`.
- Mapper extends `BaseMapper<T>`.
- Prohibit `SELECT *`.
- Prefer `LambdaQueryWrapper` over string-based field names.
- Service uses `IService<T>` and `ServiceImpl<Mapper, T>`.
- Business logic must stay in Service layer.
- Transactions only on Service methods with `rollbackFor = Exception.class`.
- Do not put `@Transactional` on Controller or Mapper.

### SHOULD

- Use batch operations (`saveBatch`, `updateBatchById`) for large writes.
- Keep batch size in `500~1000` range based on benchmark and DB pressure.

## 5. MySQL Rules

### MUST

- Mandatory columns: `id`, `gmt_create`, `gmt_modified`, `is_deleted`.
- No foreign key constraints (manage relations in application).
- Money fields use `DECIMAL`, not `FLOAT/DOUBLE`.
- One table should keep index count controlled (typically <= 5).
- New index proposal must include `EXPLAIN`.
- Avoid function expressions on indexed columns in `WHERE`.
- Avoid deep pagination without optimization (seek pagination preferred).

## 6. Exception and Logging

### MUST

- Use custom business exception with error code enum.
- Global handler with `@RestControllerAdvice`.
- Never swallow exceptions.
- Never use `printStackTrace()`.
- Use SLF4J parameterized logging.
- Do not print sensitive data in clear text.

## 7. Security and Performance

### MUST

- Prevent SQL injection: do not use `${}` for user input.
- Prevent N+1 queries in loops.
- Ensure write operations are idempotent when required.
- Avoid thread-unsafe classes in concurrent context (`SimpleDateFormat`).

### SHOULD

- Add trace ID to log context for distributed tracing.
- Use cache protection patterns for penetration/breakdown/snowball risks.

## 8. Quality Gate Checklist

- P3C: no blocking violations before commit.
- SonarQube: pass configured quality gate.
- Unit tests cover critical service branches.
- Review checklist covers SQL plan, transaction boundary, naming, and log safety.
