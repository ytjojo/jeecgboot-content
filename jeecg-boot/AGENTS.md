# AGENTS.md

## 作用范围
本文件适用于 `jeecg-boot/` 下的后端代码。
更具体的模块规则以下层 `AGENTS.md` 为准。

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



## Infrastructure

- **DB**: MySQL default (also supports PostgreSQL, Oracle, SQL Server, DM8, Kingbase8)
- **ORM**: MyBatis-Plus 3.5.12 with `ASSIGN_ID` primary key strategy
- **Cache**: Redis (localhost:6379 in dev)
- **Multi-tenancy**: Tenant isolation via `TenantContext` and MyBatis interceptor (`MybatisPlusSaasConfig`)
- **DB migrations**: Flyway 7.15.0
- **Auth**: Spring Authorization Server (OAuth2), not Shiro
- **API docs**: Knife4j 4.5.0 (Swagger)


## 后端代码硬规则
- 基于 Spring Boot 3、MyBatis-Plus 和 JeecgBoot 既有模式开发，优先复用现有基础设施
- 接口入参优先定义在 `req`，接口出参优先定义在 `vo`，内部传输使用 `dto`
- 单表或单聚合逻辑优先放 `service`，多表或跨聚合编排逻辑放 `biz`
- 数据库结构变更时，同步更新实体、Mapper、SQL 脚本以及相关请求/响应对象
- 不为了单次需求随意改动系统级基础模块或公共配置，除非任务明确要求
- Controller returns `Result<T>` only — no raw objects, Maps, or unwrapped pagination
- Use `@Resource` for DI (not `@Autowired` or constructor injection)
- BizManageService for cross-table orchestration with `@Transactional(rollbackFor = Exception.class)`
- API paths: `/api/v1/{resources}` with actions `list`, `detail`, `save`, `update`, `delete`
- All writes use POST (save/update). Deletes use DELETE with comma-separated IDs
- Use `@Valid`/`@Validated` on controller params with descriptive `message` on constraint annotations
- Pagination params: `pageNo` (1-based), `pageSize` (default 10, max 100)
- Don't return entity objects directly — use VOs
- Don't modify system-level base modules unless explicitly required

## 路由
- 内容社区模块：查看 `jeecg-boot-module/jeecg-module-content/AGENTS.md`
- 其他后端背景资料：查看 `../docs/agent-context/api-guidelines.md` 和 `../docs/agent-context/architecture.md`
- 后端编码规范：`../docs/agent-context/springboot-coding-conventions.md`
- JeecgBoot 框架特有规范：`../docs/agent-context/springboot-jeecgboot-conventions.md`
- 后端数据库设计：`../docs/agent-context/springboot-db-design.md`
