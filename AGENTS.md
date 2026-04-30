# AGENTS.md

## 作用范围
本文件是仓库级全局规则入口，只保留所有目录通用的硬约束、规则优先级和文档路由。
不在本文件中维护大段项目介绍、命令清单、部署步骤或排障手册。

## 规则优先级
1. 当前目录及更深层目录中的 `AGENTS.md`
2. 仓库根目录 `AGENTS.md`
3. `doc/agent-context/` 下的文档仅作参考，不是硬约束

## 路由
- 修改后端代码前，先阅读 `jeecg-boot/AGENTS.md`
- 修改前端代码前，先阅读 `jeecgboot-vue3/AGENTS.md`
- 修改内容社区模块前，先阅读 `jeecg-boot/jeecg-boot-module/jeecg-module-content/AGENTS.md`

## 全局硬规则
- 默认使用中文沟通，命令和路径示例按 macOS 环境编写
- 代码必须有注释，注释要中文
- 只修改与当前任务直接相关的文件，不顺手重构无关模块
- 不覆盖、不回退、不清理用户已有改动，除非用户明确要求
- 新增目录级规则时，保持文件短小，优先写“必须遵守的约束”，不要复制整份项目手册
- 易变信息，例如版本号、启动命令、环境变量、部署步骤，放到 `doc/agent-context/`，不要堆在根规则文件

## 参考文档
- 项目概览：`doc/agent-context/project-overview.md`
- 常用命令：`doc/agent-context/commands.md`
- 架构与目录：`doc/agent-context/architecture.md`
- API 与后端约定：`doc/agent-context/api-guidelines.md`
- 后端编码规范：`doc/agent-context/springboot-coding-conventions.md`
- 后端数据库设计：`doc/agent-context/springboot-db-design.md`
- 部署与排障：`doc/agent-context/deployment.md`
