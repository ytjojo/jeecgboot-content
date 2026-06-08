# 项目概览

## 项目定位
JeecgBoot 是企业级 AI 低代码平台，采用前后端分离架构，支持单体和微服务两种后端部署模式。
当前仓库包含后端 `jeecg-boot/`、前端 `jeecgboot-vue3/` 以及内容社区相关业务模块。

- 后端：Spring Boot 3 + MyBatis-Plus + Shiro/JWT，提供系统管理和业务 API
- 前端：Vue 3 + Vite + Ant Design Vue + TypeScript，提供管理后台 SPA 界面

## 主要目录
- `jeecg-boot/`：Java 后端主工程
- `jeecgboot-vue3/`：Vue3 + TypeScript 前端工程
- `docs/requirements/`：需求、方案和补充说明文档
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/`：内容社区业务模块
- `openspec/changes/`：OpenSpec 变更管理目录，按业务域（user/circle/channel）+ EPIC 编号组织，每个 change 包含 proposal、design、plan、tasks 等制品。后端 change 无后缀，前端 change 以 `_frontend` 结尾。命名格式：`{domain}-{epic_number}-{name}[_frontend]`
- `openspec/specs/`：OpenSpec 领域规格定义目录，存放各子域的 spec.md（如 channel-governance、channel-member-management 等），作为 change 设计的上游参考
- `openspec/config.yaml`：OpenSpec 全局配置，包含 schema（use-tdd-plan）和 change 命名规则
- `openspec/schemas/`：OpenSpec schema 定义目录
- `docs/requirements/prd/decomposition/change-prd-mapping.yaml`：change 到 PRD 文档的映射配置，用于多 Agent 并行生成 PRD 时的精确上下文路由

## 架构模式
### 单体架构
- 适用于中小型项目
- 所有功能模块部署在一个应用中
- 常用于系统管理、示例模块和常规业务模块

### 微服务架构
- 适用于大型企业级项目
- 各服务独立部署与扩展
- 通过 Nacos 进行服务注册与发现

## 技术栈概览
### 后端
- Spring Boot 3
- Spring Cloud / Spring Cloud Alibaba
- Shiro + JWT
- MyBatis-Plus
- MySQL + Redis
- Knife4j / Swagger OpenAPI

### 前端
- Vue 3
- TypeScript
- Vite
- Ant Design Vue
- Pinia
- pnpm

## 使用建议
- 规则类信息优先查看对应目录下的 `AGENTS.md`
- 背景类信息优先查看本目录中的参考文档
- 变更具体模块前，先确认该模块是否已经有更靠近代码的局部规则
