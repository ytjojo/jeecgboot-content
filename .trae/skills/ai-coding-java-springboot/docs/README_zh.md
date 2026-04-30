# Spring Boot 开发规范 — AI 编码助手技能库

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](../LICENSE)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://openjdk.org/)
[![Spring Boot 3.x](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![MyBatis Plus](https://img.shields.io/badge/MyBatis%20Plus-3.5.x-blue.svg)](https://baomidou.com/)

<div align="center">
  <a href="../README.md">&nbsp; 📖English</a> &nbsp;|&nbsp;
  <a href="./README_zh.md"> 📖简体中文</a>
</div>

一套完整的、生产级的 Spring Boot 后端项目开发规范库。专为 AI 编码助手（Claude Code、Cursor、Windsurf、Copilot、OpenCode）和开发团队设计，涵盖 RESTful API 设计、分层架构、数据库设计、异常处理、日志监控、性能优化和安全开发。

## 为什么使用本项目？

AI 编码助手在有清晰、结构化的规范指导时，能生成更高质量的代码。本项目提供 **8 个经过实战验证的技能模块**，确保生成一致、可维护、安全的 Spring Boot 代码——无论你是构建微服务、RESTful API 还是单体后端。

**这些规范是底线，不是建议**——违反规则会导致严重的可维护性问题和性能隐患。

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17+ | 语言运行时 |
| Spring Boot | 3.x | 应用框架 |
| MyBatis Plus | 3.5.x | ORM / 数据访问 |
| Redis | 6.0+ | 缓存层 |

## 技能模块

| 模块 | 适用场景 | 核心内容 |
|------|----------|----------|
| **[API 设计规范](../references/springboot-api-standards.md)** | 编写 Controller、设计 API 接口 | RESTful URL 设计、统一响应格式、分页策略、DTO/VO 规范 |
| **[编码规范](../references/springboot-coding-conventions.md)** | 编写业务逻辑、依赖注入、事务管理 | 分层架构、`@Resource` 注入、命名规范 |
| **[数据库设计规范](../references/springboot-db-design.md)** | 创建表、编写 SQL、MyBatis Mapper | 表设计、Entity 规范、索引设计、批量操作 |
| **[异常处理规范](../references/springboot-exception-handling.md)** | 异常处理、错误码定义 | 异常分类、全局异常处理器、Service 层策略 |
| **[日志监控规范](../references/springboot-logging-monitoring.md)** | 日志输出、链路追踪、Metrics 埋点 | 日志级别、结构化日志、日志脱敏 |
| **[性能优化规范](../references/springboot-performance.md)** | 数据库查询、缓存策略、高并发 | 禁止 N+1 查询、游标分页、缓存防护 |
| **[安全开发规范](../references/springboot-security-standards.md)** | 认证鉴权、用户输入、敏感数据 | SQL 注入防护、XSS 防护、BCrypt 密码、JWT 安全 |
| **[测试规范](../references/springboot-testing-standards.md)** | 单元测试、接口测试、集成测试 | JUnit 5、Mockito、AssertJ、测试分层 |

## AI 编码工具使用方法

### Claude Code

在提示词中直接引用规范：

```text
请为当前 Spring Boot 项目实现用户管理模块。
要求严格遵守 ai-coding-java-springboot-skills/skill.md 规范：
- 分层：Controller -> BizManageService -> Service -> Mapper
- 使用 @Resource 注入
- 返回统一 Result
```

### OpenCode

将本仓库与业务项目放在同一工作区：

```text
你正在为一个 Spring Boot 后端项目编写代码。
请优先遵循 ai-coding-java-springboot-skills/skill.md 中的技能规范。
```

### Cursor / Windsurf / Continue / Cline

在项目根目录创建规则文件（如 `.ai/project-rule.md`）：

```text
当前项目开发必须遵守以下规范：
../ai-coding-java-springboot-skills/skill.md
```

### 按任务选择技能

无需每次加载全部技能，按任务类型选择：

| 任务 | 技能模块 |
|------|----------|
| 编写 Controller / API | `api-standards` |
| 编写业务逻辑 | `coding-conventions` |
| 设计表 / SQL | `db-design` |
| 处理异常 | `exception-handling` |
| 优化性能 | `performance` |
| 安全控制 | `security-standards` |
| 日志监控 | `logging-monitoring` |
| 编写或修复测试 | `testing-standards` |

## 项目结构

```
ai-coding-java-springboot-skills/
├── skill.md                              # 技能合集总览（入口文件）
├── README.md                             # 英文文档
├── docs/
│   └── README_zh.md                      # 本文件（中文）
├── spec/
│   └── springboot-development-spec.md    # 完整开发规范
└── references/                           # 各技能详细文档
    ├── springboot-api-standards.md       # REST API 设计规范
    ├── springboot-coding-conventions.md  # 编码规范与分层架构
    ├── springboot-db-design.md           # 数据库设计与 MyBatis Mapper
    ├── springboot-exception-handling.md  # 异常处理与错误码
    ├── springboot-logging-monitoring.md  # 日志与监控规范
    ├── springboot-performance.md         # 性能优化
    ├── springboot-security-standards.md  # 安全开发规范
    └── springboot-testing-standards.md   # 测试规范与测试分层
```

## 适用范围

- Spring Boot + MyBatis Plus + Redis 项目
- Java 17+ 后端项目
- RESTful API 设计
- 微服务架构

## Star History
<a href="https://www.star-history.com/?repos=OliverAAAAA%2Fai-coding-java-springboot-skills&type=timeline&logscale=&legend=top-left">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/image?repos=OliverAAAAA/ai-coding-java-springboot-skills&type=timeline&theme=dark&logscale&legend=top-left" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/image?repos=OliverAAAAA/ai-coding-java-springboot-skills&type=timeline&logscale&legend=top-left" />
   <img alt="Star History Chart" src="https://api.star-history.com/image?repos=OliverAAAAA/ai-coding-java-springboot-skills&type=timeline&logscale&legend=top-left" />
 </picture>
</a>

## 贡献

欢迎贡献！请提交 Issue 或 Pull Request。


# 联系我
  <a name="联系我"></a>  <a name="联系我"></a>
  ![alt text](../img/gzhQr.png)


## 许可证

本项目采用 [MIT 许可证](../LICENSE) 开源。

---

**版本**: 1.0.0
