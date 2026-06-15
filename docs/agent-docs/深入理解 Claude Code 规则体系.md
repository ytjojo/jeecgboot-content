#### 文章目录

## 规则体系概述

Rules（规则）是 Claude Code 的规范性约束系统，旨在通过定义编码标准、行业惯例与质量检查清单，对 Claude Code 在辅助软件开发过程中的行为提供明确的指导依据。该系统以 Markdown 文件为载体，存放于 `.claude/rules/` 目录下，由 Claude Code 在运行时自动加载并强制遵循。

规则与技能的职责划分：

| 类别 | 定义范围 | 典型示例 |
|------------|------------|---------------------|
| Rules（规则） | 规定"应当遵循什么" | 80% 最低测试覆盖率、禁止硬编码密钥 |
| Skills（技能） | 规定"如何具体实施" | Python 测试模式、Go 并发范式 |

二者形成互补关系：语言特定的规则文件在适当时机引用相应的 Skill，以确保规范性约束与实操指导之间的有效衔接。

## 目录组织结构

```bash
rules/
├── README.md                # 规则体系总体说明
├── common/                  # 语言无关的通用规则层（所有项目必须安装）
│   ├── agents.md            # Agent 编排与调度规范
│   ├── code-review.md       # 代码审查标准与流程
│   ├── coding-style.md      # 编码风格通用原则
│   ├── development-workflow.md # 功能开发全流程规范
│   ├── git-workflow.md      # Git 版本控制工作流
│   ├── hooks.md             # Hooks 生命周期系统
│   ├── patterns.md          # 通用设计模式
│   ├── performance.md       # 性能优化策略与模型选择
│   ├── security.md          # 安全规范与合规要求
│   └── testing.md           # 测试覆盖率与质量要求
├── zh/                      # common 规则的中文本地化版本
├── cpp/                     # C++ 语言特定规则
├── csharp/                  # C# 语言特定规则
├── dart/                    # Dart 语言特定规则
├── golang/                  # Go 语言特定规则
├── java/                    # Java 语言特定规则
├── kotlin/                  # Kotlin 语言特定规则
├── perl/                    # Perl 语言特定规则
├── php/                     # PHP 语言特定规则
├── python/                  # Python 语言特定规则
├── rust/                    # Rust 语言特定规则
├── swift/                   # Swift 语言特定规则
├── typescript/              # TypeScript 语言特定规则
└── web/                     # Web 与前端特定规则（含额外的 design-quality.md、performance.md）
```

各语言目录均遵循统一的五文件标准规范：

| 标准文件 | 职责范围 |
|-----------------|------------------------------------|
| coding-style.md | 格式化工具配置、命名惯例、错误处理范式、语言特性使用规范 |
| testing.md | 测试框架选型、覆盖率工具配置、测试组织与命名规范 |
| patterns.md | 语言特定的设计模式与架构范式 |
| hooks.md | PostToolUse 钩子配置（格式化器、Linter、编译校验） |
| security.md | 密钥管理策略、安全扫描工具、语言特定安全实践 |

注：web/ 目录因其领域特性，额外包含 design-quality.md（UI 设计质量规范）与 performance.md（前端 性能优化 规范），以覆盖前端开发中特有的视觉一致性与运行时性能要求。

通用规则层各文件职责说明：

| 文件 | 职责说明 |
|-------------------------|--------------------------------------------------|
| coding-style.md | 编码风格原则：不可变性、KISS/DRY/YAGNI、命名规范、文件组织、错误处理、输入校验 |
| testing.md | 测试质量要求：80% 覆盖率阈值、单元/集成/E2E 三类测试、TDD 流程、AAA 模式 |
| security.md | 安全合规规范：密钥管理、SQL 注入/XSS/CSRF 防护、认证授权、速率限制、安全事件响应 |
| patterns.md | 通用设计模式：Skeleton 项目策略、Repository 模式、API 响应信封 |
| hooks.md | Hooks 生命周期：PreToolUse/PostToolUse/Stop 三类触发时机与职责 |
| git-workflow.md | 版本控制规范：Conventional Commits 格式、Pull Request 流程 |
| development-workflow.md | 功能开发流程：调研复用 → 规划 → TDD → 代码审查 → 提交推送五阶段 |
| code-review.md | 代码审查规范：审查触发条件、CRITICAL/HIGH/MEDIUM/LOW 分级体系 |
| agents.md | Agent 编排调度：内置 Agent 职责定位、并行 Task 执行原则 |
| performance.md | 性能优化策略：Haiku/Sonnet/Opus 模型选择、上下文窗口管理 |

## 分层架构与优先级机制

Rules 采用通用层 + 语言层的两级架构设计：

1.  通用规则层（common/）：提供跨语言适用的普适性原则，不含任何语言特定的代码示例，为所有项目奠定统一的质量基线。
2.  语言规则层（语言目录）：在通用规则基础上进行扩展，补充框架特定的设计模式、工具链配置及代码示例。

各语言规则文件均于开头显式声明其继承关系：

```shell
> 本文件在 [common/xxx.md](../common/xxx.md) 基础上扩展 Java 特定内容。
```

当通用规则与语言特定规则产生冲突时，语言特定规则享有优先权，即"特定覆盖通用"。此原则遵循业界通行的分层配置范式，其行为模式类似于 CSS 特异性规则或 .gitignore 优先级机制。

优先级覆盖示例： common/coding\-style.md 将不可变性确立为默认原则，而 golang/coding-style.md 可基于 Go 语言惯用范式对此进行覆盖：

> Go 语言惯用做法使用指针接收者进行结构体修改 — 参见 common/coding-style.md 中的通用原则，但此处以 Go 惯用的修改方式为准。

通用规则中可能受语言规则覆盖的条款，均以标准注记形式明确标识：

> 语言注记：此规则可能被语言特定规则覆盖，当该模式在该语言中不符合惯用范式时适用。

## 语言特定规则扩展示例（以 Java 为例）

以下以 Java 语言规则为例，说明语言层在通用层基础上的扩展内容：

| 规范领域 | Java 扩展内容 |
|--------------|--------------------------------------------------------------------------------------------------------|
| coding-style | google-java-format 格式化、record 不可变类型、sealed class 封闭类型层次、pattern matching instanceof 模式匹配、Optional 规范用法 |
| testing | JUnit 5 + AssertJ + Mockito 测试框架组合、Testcontainers 容器化集成测试、@DisplayName 语义标注、JaCoCo 80% 覆盖率目标 |
| patterns | Repository 接口抽象、Service 层业务编排、构造器注入范式、record DTO 映射、Builder 模式、sealed 域模型、ApiResponse 统一信封 |
| security | System.getenv() 环境变量密钥管理、PreparedStatement 参数化查询防注入、Bean Validation 声明式校验、bcrypt/Argon2 密码哈希存储 |
| hooks | google-java-format 自动格式化、checkstyle 风格校验、mvnw/gradlew 编译验证 |

各语言目录的 `hooks.md` 文件通过 YAML frontmatter 中的 `paths` 字段声明触发路径范围。例如 Java 的 hooks 规则同时匹配 `**/*.java`、`**/pom.xml` 及 `**/build.gradle`，确保对 Java 项目中所有关键文件的变更均触发相应的自动化校验流程。

## 安装与部署

手动安装：

```bash
# 通用规则层（所有项目必须安装）
cp -r rules/common ~/.claude/rules/common

# 依据项目技术栈安装语言规则层
cp -r rules/java ~/.claude/rules/java
cp -r rules/python ~/.claude/rules/python
```

重要约束： 安装时须以完整目录为单元进行复制，严禁使用 `/*` 通配符进行展平操作。通用层与语言层存在同名文件，展平合并将导致语言层文件覆盖通用层文件，且会破坏语言层文件中依赖的 `../common/` 相对路径引用关系。

## 扩展新语言规则集

如需为尚未覆盖的编程语言添加规则支持（以 Rust 为例），须遵循以下标准化流程：

1.  创建目标语言目录 `rules/rust/`
2.  添加五项标准规范文件：coding-style.md、testing.md、patterns.md、hooks.md、security.md
3.  各文件开头须声明继承关系：`> This file extends [common/xxx.md](../common/xxx.md) with Rust-specific content.`
4.  视实际需要，于 `skills/` 目录下创建配套的 Skill 定义文件

对于非语言类型的领域规则集（如 `web/`），当该领域具备足够数量的可复用领域特定指导内容时，即可遵循相同的分层架构模式创建独立规则集。

## 规则文件的路径匹配机制

各规则文件的 YAML frontmatter 中包含 `paths` 字段，用于声明该规则适用的文件路径范围：

```yaml
---
paths:
  - "**/*.java"
  - "**/pom.xml"
  - "**/build.gradle"
---
```

Claude Code 运行时将依据当前操作涉及的文件路径，自动匹配并加载对应的规则文件，确保规范性约束仅在相关上下文中生效，避免无关规则对非匹配场景产生干扰。