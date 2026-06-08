# AGENTS.md

## 作用范围
本文件适用于 `jeecg-boot/jeecg-boot-module/jeecg-module-content/` 内容社区模块。

## 模块硬规则
- 目录结构遵循 `controller / biz / service / mapper / entity / req / vo / dto` 分层
- `biz` 仅用于多表、跨聚合、跨领域编排；单表逻辑留在 `service`
- Controller 负责协议边界，不承载复杂业务拼装
- 接口请求对象放 `req`，必要时按 `query`、`create`、`update` 子目录拆分
- 接口响应对象放 `vo`，内部传输对象放 `dto`
- 表结构变更时，同步更新 `entity`、`mapper`、Flyway SQL 和相关测试
- 复杂设计、历史方案、外部库研究放 `docs/`，不要持续膨胀本文件

## 本模块文档路由
- 模块说明：`README.md`
- 模块架构概览：`../../../docs/agent-context/content-module-architecture.md`
- JeecgBoot 框架规范：`../../../docs/agent-context/springboot-jeecgboot-conventions.md`
- 设计与规范文档：`docs/`
- 
