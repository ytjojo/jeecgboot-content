# Spring Boot Development Standards for AI Coding Assistants

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://openjdk.org/)
[![Spring Boot 3.x](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![MyBatis Plus](https://img.shields.io/badge/MyBatis%20Plus-3.5.x-blue.svg)](https://baomidou.com/)

<div align="center">
  <a href="./README.md"> &nbsp;📖English</a> &nbsp;|&nbsp; <a href="./docs/README_zh.md">📖简体中文</a>
</div>
A comprehensive, production-ready development standards library for Spring Boot backend projects. Designed as a skill reference for AI coding assistants (Claude Code, Cursor, Windsurf, Copilot, OpenCode) and development teams — covering RESTful API design, layered architecture, database design, exception handling, logging, performance optimization, and security best practices.

## Why Use This?

AI coding assistants generate better code when given clear, structured standards. This project provides **8 battle-tested skill modules** that enforce consistent, maintainable, and secure Spring Boot code — whether you're building microservices, RESTful APIs, or monolithic backends.

**These are not suggestions — they are baseline requirements.** Violations lead to serious maintainability and performance issues in production.

## Tech Stack

| Technology | Version | Purpose |
|-----------|---------|---------|
| Java | 17+ | Language runtime |
| Spring Boot | 3.x | Application framework |
| MyBatis Plus | 3.5.x | ORM / Data access |
| Redis | 6.0+ | Caching layer |

## Skill Modules

| Module | When to Use | Key Standards |
|--------|------------|---------------|
| **[API Standards](references/springboot-api-standards.md)** | Writing Controllers, designing endpoints | RESTful URLs, unified response format, pagination, DTO/VO patterns |
| **[Coding Conventions](references/springboot-coding-conventions.md)** | Writing business logic, DI, transactions | Layered architecture, `@Resource` injection, naming conventions |
| **[Database Design](references/springboot-db-design.md)** | Creating tables, writing SQL, MyBatis mappers | Table design, entity standards, index design, batch operations |
| **[Exception Handling](references/springboot-exception-handling.md)** | Error handling, error code design | Exception hierarchy, global handler, service-layer strategies |
| **[Logging & Monitoring](references/springboot-logging-monitoring.md)** | Log output, tracing, metrics | Log levels, structured logging, sensitive data masking |
| **[Performance](references/springboot-performance.md)** | Query optimization, caching, high concurrency | N+1 prevention, cursor pagination, cache protection |
| **[Security](references/springboot-security-standards.md)** | Auth, user input, sensitive data | SQL injection prevention, XSS, BCrypt passwords, JWT security |
| **[Testing Standards](references/springboot-testing-standards.md)** | Unit tests, integration tests, test layering | JUnit 5, Mockito, AssertJ, slice tests |

## How to Use with AI Coding Tools

### Claude Code

Reference the standards directly in your prompts:

```text
Implement the user management module for this Spring Boot project.
Strictly follow ai-coding-java-springboot-skills/skill.md:
- Layering: Controller -> BizManageService -> Service -> Mapper
- Use @Resource injection
- Return unified Result
```

### OpenCode

Place this repo alongside your project in the same workspace:

```text
You are writing code for a Spring Boot backend project.
Follow the standards in ai-coding-java-springboot-skills/skill.md.
```

### Cursor / Windsurf / Continue / Cline

Create a rules file (e.g., `.ai/project-rule.md`) in your project root:

```text
This project must follow these development standards:
../ai-coding-java-springboot-skills/skill.md
```

### Task-Based Skill Selection

Load only what you need for the current task:

| Task | Skill Module |
|------|-------------|
| Writing Controllers / APIs | `api-standards` |
| Writing business logic | `coding-conventions` |
| Designing tables / SQL | `db-design` |
| Handling exceptions | `exception-handling` |
| Optimizing performance | `performance` |
| Security controls | `security-standards` |
| Logging & monitoring | `logging-monitoring` |
| Writing or fixing tests | `testing-standards` |

## Project Structure

```
ai-coding-java-springboot-skills/
├── skill.md                              # Skill collection overview (entry point)
├── README.md                             # This file (English)
├── docs/
│   └── README_zh.md                      # 中文文档
├── spec/
│   └── springboot-development-spec.md    # Complete development specification
└── references/                           # Detailed technical documentation
    ├── springboot-api-standards.md       # REST API design & documentation
    ├── springboot-coding-conventions.md  # Coding standards & layered architecture
    ├── springboot-db-design.md           # Database design & MyBatis Mapper
    ├── springboot-exception-handling.md  # Exception handling & error codes
    ├── springboot-logging-monitoring.md  # Logging & monitoring standards
    ├── springboot-performance.md         # Performance optimization
    ├── springboot-security-standards.md  # Security best practices
    └── springboot-testing-standards.md   # Testing conventions & test layering
```

## Applicable Scenarios

- Spring Boot + MyBatis Plus + Redis projects
- Java 17+ backend applications
- RESTful API design
- Microservice architectures

## Star History

<a href="https://www.star-history.com/?repos=OliverAAAAA%2Fai-coding-java-springboot-skills&type=timeline&logscale=&legend=top-left">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/image?repos=OliverAAAAA/ai-coding-java-springboot-skills&type=timeline&theme=dark&logscale&legend=top-left" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/image?repos=OliverAAAAA/ai-coding-java-springboot-skills&type=timeline&logscale&legend=top-left" />
   <img alt="Star History Chart" src="https://api.star-history.com/image?repos=OliverAAAAA/ai-coding-java-springboot-skills&type=timeline&logscale&legend=top-left" />
 </picture>
</a>

## Contributing

Contributions are welcome! Please open an issue or submit a pull request.




# contact me
  <a name="联系我"></a>  <a name="联系我"></a>
  ![alt text](./img/gzhQr.png)

## License

This project is licensed under the [MIT License](LICENSE).

---

**Version**: 1.0.0
