# AGENTS.md

## 作用范围
本文件适用于 `jeecg-boot/` 下的后端代码。
更具体的模块规则以下层 `AGENTS.md` 为准。

## 后端硬规则
- 基于 Spring Boot 3、MyBatis-Plus 和 JeecgBoot 既有模式开发，优先复用现有基础设施
- Controller 统一返回 `org.jeecg.common.api.vo.Result<T>`
- 接口入参优先定义在 `req`，接口出参优先定义在 `vo`，内部传输使用 `dto`
- 单表或单聚合逻辑优先放 `service`，多表或跨聚合编排逻辑放 `biz`
- 数据库结构变更时，同步更新实体、Mapper、SQL 脚本以及相关请求/响应对象
- 不为了单次需求随意改动系统级基础模块或公共配置，除非任务明确要求

## 路由
- 内容社区模块：查看 `jeecg-boot-module/jeecg-module-content/AGENTS.md`
- 其他后端背景资料：查看 `../docs/agent-context/api-guidelines.md` 和 `../docs/agent-context/architecture.md`
- 后端编码规范：`../docs/agent-context/springboot-coding-conventions.md`
- 后端数据库设计：`../docs/agent-context/springboot-db-design.md`
