# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

JeecgBoot SASS v3.9.1 — enterprise low-code platform. Monorepo with Java backend (Spring Boot 3.5.5, Java 17) and Vue 3 frontend (Vite 6, Ant Design Vue 4). Supports both monolith and Spring Cloud Alibaba microservice deployment modes.

## Build & Run Commands

### Backend

```bash
# Build all (monolith mode, tests skipped by default)
cd jeecg-boot && mvn clean package -DskipTests

# Run monolith (port 8080, context-path /jeecg-boot)
cd jeecg-boot/jeecg-module-system/jeecg-system-start && mvn spring-boot:run

# Build microservices
cd jeecg-boot && mvn clean package -PSpringCloud -DskipTests

# Run tests (disabled by default in surefire)
mvn test -DskipTests=false
```

Maven profiles: `dev` (default), `test`, `docker`, `prod`. Config files in `jeecg-system-start/src/main/resources/application-{profile}.yml`.

### Frontend

```bash
cd jeecgboot-vue3
pnpm install
pnpm dev          # Dev server on port 3100, proxies to localhost:8080/jeecg-boot
pnpm build        # Production build
pnpm batch:prettier  # Format all files
```

Requires Node 18+ or 20+, pnpm.

## Architecture

### Backend Module Hierarchy

```
jeecg-boot/
├── jeecg-boot-base-core/        # Core: base classes, config, security, interceptors
├── jeecg-module-system/
│   ├── jeecg-system-api/        # Dual-mode API interfaces
│   │   ├── jeecg-system-local-api/    # Monolith (direct calls)
│   │   └── jeecg-system-cloud-api/    # Microservice (Feign clients)
│   ├── jeecg-system-biz/        # System business logic (user, role, auth, dict...)
│   └── jeecg-system-start/      # Monolith entry point
├── jeecg-boot-module/
│   ├── jeecg-module-demo/       # Demo module
│   ├── jeecg-boot-module-airag/ # AI RAG (LLM, vector store, MCP)
│   └── jeecg-module-content/    # Content management
└── jeecg-server-cloud/          # Microservice infra (Gateway, Nacos, Sentinel, XXL-Job)
```

Dependency chain (monolith): `jeecg-system-start` → `jeecg-system-biz` → `jeecg-system-local-api` → `jeecg-boot-base-core`.

### Key Base Classes (jeecg-boot-base-core)

- **Entity**: `JeecgEntity` — provides `id` (ASSIGN_ID/Snowflake), `createBy`, `createTime`, `updateBy`, `updateTime`
- **Controller**: `JeecgController<T, S extends IService<T>>` — auto-wires service, provides Excel import/export
- **Service**: `JeecgServiceImpl<M, T>` extending MyBatis-Plus `ServiceImpl`
- **Response**: `Result<T>` — unified response with `Result.OK()`, `Result.error()`. Success code 200, error 500, no-auth 510
- **Query**: `QueryGenerator` — auto-builds query conditions from request parameters
- **Security**: OAuth2 with Spring Authorization Server. Custom grant types: password, phone, social, app, self

### Backend Package Structure Convention

```
org.jeecg.modules.{module}/
├── controller/    # REST API, delegates to biz/service
├── biz/           # Cross-table orchestration (BizManageService pattern)
├── service/       # Single-table CRUD (extends ServiceImpl)
│   └── impl/
├── mapper/        # MyBatis-Plus mappers
│   └── xml/       # Mapper XML
├── entity/        # DB entities (extend JeecgEntity)
├── req/           # Request DTOs (input params)
├── vo/            # Response VOs (output params)
└── dto/           # Internal transfer objects
```

Dependency direction: Controller → BizManageService → Service → Mapper. No skipping layers, no reverse dependencies.

### Frontend Architecture

- **Permission mode**: BACK — routes/menus fetched from backend API, dynamically registered
- **State**: Pinia stores in `src/store/modules/` (user, permission, app, locale, multipleTab)
- **API**: Custom Axios wrapper (`defHttp`) in `src/utils/http/axios/`, MD5 request signing
- **Path aliases**: `/@/` → `src/`, `/#/` → `types/`
- **Auto-import**: Ant Design Vue components auto-imported via unplugin
- **External packages**: `@jeecg/online` (online forms), `@jeecg/aiflow` (AI flow designer)

## Coding Rules

### Global (from AGENTS.md hierarchy)

- Default communication in Chinese; code comments must be in Chinese
- Only modify files directly related to the current task
- Never overwrite or revert user's existing changes without explicit request
- AGENTS.md files cascade: deeper directories override parent rules

### Backend

- Controller returns `Result<T>` only — no raw objects, Maps, or unwrapped pagination
- Use `@Resource` for DI (not `@Autowired` or constructor injection)
- BizManageService for cross-table orchestration with `@Transactional(rollbackFor = Exception.class)`
- API paths: `/api/v1/{resources}` with actions `list`, `detail`, `save`, `update`, `delete`
- All writes use POST (save/update). Deletes use DELETE with comma-separated IDs
- Use `@Valid`/`@Validated` on controller params with descriptive `message` on constraint annotations
- Pagination params: `current` (1-based), `size` (default 10, max 100)
- Don't return entity objects directly — use VOs
- Don't modify system-level base modules unless explicitly required

### Frontend

- Use pnpm, not npm/yarn
- Use `/@/` path alias consistently
- Reuse existing API layer (`src/api/`), components, hooks, and stores
- Don't break the permission/routing chain without understanding backend dependency
- Dynamic imports for non-critical modules (avoid top-level static imports for heavy deps)

## Infrastructure

- **DB**: MySQL default (also supports PostgreSQL, Oracle, SQL Server, DM8, Kingbase8)
- **ORM**: MyBatis-Plus 3.5.12 with `ASSIGN_ID` primary key strategy
- **Cache**: Redis (localhost:6379 in dev)
- **Multi-tenancy**: Tenant isolation via `TenantContext` and MyBatis interceptor (`MybatisPlusSaasConfig`)
- **DB migrations**: Flyway 7.15.0
- **Auth**: Spring Authorization Server (OAuth2), not Shiro
- **API docs**: Knife4j 4.5.0 (Swagger)

## Reference Documents

Detailed docs in `docs/agent-context/`:
- `project-overview.md` — project background
- `commands.md` — command reference
- `architecture.md` — directory and layering details
- `api-guidelines.md` — REST API conventions and DTO/VO rules
- `springboot-coding-conventions.md` — Java coding standards, DI, transactions, Redis, Lombok
- `springboot-db-design.md` — database design conventions
- `deployment.md` — deployment and troubleshooting
