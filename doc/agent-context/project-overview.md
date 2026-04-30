# 项目概览

## 项目定位
JeecgBoot 是企业级 AI 低代码平台，采用前后端分离架构，支持单体和微服务两种后端部署模式。
当前仓库包含后端 `jeecg-boot/`、前端 `jeecgboot-vue3/` 以及内容社区相关业务模块。

## 主要目录
- `jeecg-boot/`：Java 后端主工程
- `jeecgboot-vue3/`：Vue3 + TypeScript 前端工程
- `doc/`：需求、方案和补充说明文档
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/`：内容社区业务模块

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
