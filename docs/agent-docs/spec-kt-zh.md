<div align="center">
    <img src="./media/logo_large.webp" alt="Spec Kit Logo" width="200" height="200"/>
    <h1>🌱 Spec Kit</h1>
    <h3><em>更快构建高质量软件。</em></h3>
</div>

<p align="center">
    <strong>一个开源工具包，让你专注于产品场景和可预测结果，而不是从零开始对每一部分都进行 vibe coding。</strong>
</p>

<p align="center">
    <a href="https://github.com/github/spec-kit/releases/latest"><img src="https://img.shields.io/github/v/release/github/spec-kit" alt="Latest Release"/></a>
    <a href="https://github.com/github/spec-kit/stargazers"><img src="https://img.shields.io/github/stars/github/spec-kit?style=social" alt="GitHub stars"/></a>
    <a href="https://github.com/github/spec-kit/blob/main/LICENSE"><img src="https://img.shields.io/github/license/github/spec-kit" alt="License"/></a>
    <a href="https://github.github.io/spec-kit/"><img src="https://img.shields.io/badge/docs-GitHub_Pages-blue" alt="Documentation"/></a>
</p>

---

## Table of Contents

- [Table of Contents](#table-of-contents)
- [🤔 What is Spec-Driven Development?](#-what-is-spec-driven-development)
- [⚡ Get Started](#-get-started)
  - [1. Install Specify CLI](#1-install-specify-cli)
    - [Option 1: Persistent Installation (Recommended)](#option-1-persistent-installation-recommended)
    - [Option 2: One-time Usage](#option-2-one-time-usage)
    - [Option 3: Enterprise / Air-Gapped Installation](#option-3-enterprise--air-gapped-installation)
  - [2. Establish project principles](#2-establish-project-principles)
  - [3. Create the spec](#3-create-the-spec)
  - [4. Create a technical implementation plan](#4-create-a-technical-implementation-plan)
  - [5. Break down into tasks](#5-break-down-into-tasks)
  - [6. Execute implementation](#6-execute-implementation)
- [📽️ Video Overview](#️-video-overview)
- [🧩 Community Extensions](#-community-extensions)
- [🎨 Community Presets](#-community-presets)
- [🚶 Community Walkthroughs](#-community-walkthroughs)
- [🛠️ Community Friends](#️-community-friends)
- [🤖 Supported AI Coding Agent Integrations](#-supported-ai-coding-agent-integrations)
- [Available Slash Commands](#available-slash-commands)
    - [Core Commands](#core-commands)
    - [Optional Commands](#optional-commands)
- [🔧 Specify CLI Reference](#-specify-cli-reference)
- [🧩 Making Spec Kit Your Own: Extensions \& Presets](#-making-spec-kit-your-own-extensions--presets)
  - [Extensions — Add New Capabilities](#extensions--add-new-capabilities)
  - [Presets — Customize Existing Workflows](#presets--customize-existing-workflows)
  - [When to Use Which](#when-to-use-which)
- [📚 Core Philosophy](#-core-philosophy)
- [🌟 Development Phases](#-development-phases)
- [🎯 Experimental Goals](#-experimental-goals)
  - [Technology independence](#technology-independence)
  - [Enterprise constraints](#enterprise-constraints)
  - [User-centric development](#user-centric-development)
  - [Creative \& iterative processes](#creative--iterative-processes)
- [🔧 Prerequisites](#-prerequisites)
- [📖 Learn More](#-learn-more)
- [📋 Detailed Process](#-detailed-process)
  - [**STEP 1:** Establish project principles](#step-1-establish-project-principles)
  - [**STEP 2:** Create project specifications](#step-2-create-project-specifications)
  - [**STEP 3:** Functional specification clarification (required before planning)](#step-3-functional-specification-clarification-required-before-planning)
  - [**STEP 4:** Generate a plan](#step-4-generate-a-plan)
  - [**STEP 5:** Have Claude Code validate the plan](#step-5-have-claude-code-validate-the-plan)
  - [**STEP 6:** Generate task breakdown with /speckit.tasks](#step-6-generate-task-breakdown-with-speckittasks)
  - [**STEP 7:** Implementation](#step-7-implementation)
- [🔍 Troubleshooting](#-troubleshooting)
  - [Git Credential Manager on Linux](#git-credential-manager-on-linux)
- [💬 Support](#-support)
- [🙏 Acknowledgements](#-acknowledgements)
- [📄 License](#-license)

## 🤔 什么是 Spec-Driven Development？

Spec-Driven Development **颠覆了** 传统软件开发的剧本。几十年来，代码一直是核心，而规范文档往往只是脚手架，一旦真正开始编码，这些文档就被丢到一边。Spec-Driven Development 改变了这一点：**规范本身变得可执行**，不再只是指导实现，而是可以直接生成可工作的实现。

## ⚡ 快速开始

### 1. 安装 Specify CLI

选择你偏好的安装方式：

> **重要：** Spec Kit 唯一官方且持续维护的包，均由这个 GitHub 仓库发布。任何在 PyPI 上出现的同名包都**不**属于本项目，也不是由 Spec Kit 维护者维护。请务必像下方示例一样直接从 GitHub 安装。

#### Option 1: Persistent Installation (Recommended)

安装一次，到处使用。为稳定性建议固定到特定发布标签（最新版本见 [Releases](https://github.com/github/spec-kit/releases)）：

```bash
# Install a specific stable release (recommended — replace vX.Y.Z with the latest tag)
uv tool install specify-cli --from git+https://github.com/github/spec-kit.git@vX.Y.Z

# Or install latest from main (may include unreleased changes)
uv tool install specify-cli --from git+https://github.com/github/spec-kit.git

# Alternative: using pipx (also works)
pipx install git+https://github.com/github/spec-kit.git@vX.Y.Z
pipx install git+https://github.com/github/spec-kit.git
```

然后验证安装的是正确版本：

```bash
specify version
```

接着直接使用该工具：

```bash
# Create new project
specify init <PROJECT_NAME>

# Or initialize in existing project
specify init . --integration copilot
# or
specify init --here --integration copilot

# Check installed tools
specify check
```

如需升级 Specify，请查看 [Upgrade Guide](./docs/upgrade.md) 获取详细说明。快速升级方式如下：

```bash
uv tool install specify-cli --force --from git+https://github.com/github/spec-kit.git@vX.Y.Z
# pipx users: pipx install --force git+https://github.com/github/spec-kit.git@vX.Y.Z
```

#### Option 2: One-time Usage

无需安装，直接运行：

```bash
# Create new project (pinned to a stable release — replace vX.Y.Z with the latest tag)
uvx --from git+https://github.com/github/spec-kit.git@vX.Y.Z specify init <PROJECT_NAME>

# Or initialize in existing project
uvx --from git+https://github.com/github/spec-kit.git@vX.Y.Z specify init . --integration copilot
# or
uvx --from git+https://github.com/github/spec-kit.git@vX.Y.Z specify init --here --integration copilot
```

**持久化安装的优势：**

- 工具会持续安装并可直接从 PATH 使用
- 无需创建 shell aliases
- 可通过 `uv tool list`、`uv tool upgrade`、`uv tool uninstall` 更好地管理工具
- shell 配置更干净

#### Option 3: Enterprise / Air-Gapped Installation

如果你的环境无法访问 PyPI 或 GitHub，请查看 [Enterprise / Air-Gapped Installation](./docs/installation.md#enterprise--air-gapped-installation) 指南，了解如何在联网机器上使用 `pip download` 创建可移植、按操作系统区分的 wheel bundles。

### 2. 建立项目原则

在项目目录中启动你的 coding agent。多数 agent 会把 spec-kit 暴露为 `/speckit.*` slash commands；而 Codex CLI 的 skills mode 则使用 `$speckit-*`。

使用 **`/speckit.constitution`** 命令创建项目的治理原则和开发指南，以指导后续所有开发。

```bash
/speckit.constitution Create principles focused on code quality, testing standards, user experience consistency, and performance requirements
```

### 3. 创建 spec

使用 **`/speckit.specify`** 命令描述你想构建什么。聚焦 **what** 和 **why**，不要聚焦技术栈。

```bash
/speckit.specify Build an application that can help me organize my photos in separate photo albums. Albums are grouped by date and can be re-organized by dragging and dropping on the main page. Albums are never in other nested albums. Within each album, photos are previewed in a tile-like interface.
```

### 4. 创建技术实现计划

使用 **`/speckit.plan`** 命令提供你的技术栈和架构选择。

```bash
/speckit.plan The application uses Vite with minimal number of libraries. Use vanilla HTML, CSS, and JavaScript as much as possible. Images are not uploaded anywhere and metadata is stored in a local SQLite database.
```

### 5. 拆解为任务

使用 **`/speckit.tasks`** 根据实现计划创建可执行的任务清单。

```bash
/speckit.tasks
```

### 6. 执行实现

使用 **`/speckit.implement`** 执行所有任务，并按计划构建你的功能。

```bash
/speckit.implement
```

如果你想看详细的逐步说明，请参阅我们的 [comprehensive guide](./spec-driven.md)。

## 📽️ 视频概览

想看看 Spec Kit 的实际效果？可以观看我们的 [video overview](https://www.youtube.com/watch?v=a9eR1xsfvHg&pp=0gcJCckJAYcqIYzv)！

[![Spec Kit video header](/media/spec-kit-video-header.jpg)](https://www.youtube.com/watch?v=a9eR1xsfvHg&pp=0gcJCckJAYcqIYzv)

## 🧩 Community Extensions

> [!NOTE]
> Community extensions 由各自作者独立创建和维护。GitHub 和 Spec Kit 维护者可能会审核将条目加入 community catalog 的 pull request，以检查格式、目录结构或策略合规性，但他们**不会审核、审计、背书或支持扩展本身的代码**。Community Extensions 网站同样也是第三方资源。安装前请先查看扩展源码，并自行决定是否使用。

🔍 **可在 [Community Extensions website](https://speckit-community.github.io/extensions/) 浏览和搜索 community extensions。**

以下 community-contributed extensions 可在 [`catalog.community.json`](extensions/catalog.community.json) 中找到：

**分类：**

- `docs` —— 读取、校验或生成 spec artifacts
- `code` —— 评审、校验或修改源代码
- `process` —— 编排跨阶段工作流
- `integration` —— 与外部平台同步
- `visibility` —— 汇报项目健康状况或进度

**效果：**

- `Read-only` —— 只生成报告，不修改文件
- `Read+Write` —— 会修改文件、创建 artifacts 或更新 specs

| Extension | Purpose | Category | Effect | URL |
|-----------|---------|----------|--------|-----|
| Agent Assign | 为 spec-kit 任务分配专门的 Claude Code agents，以进行针对性执行 | `process` | Read+Write | [spec-kit-agent-assign](https://github.com/xymelon/spec-kit-agent-assign) |
| AI-Driven Engineering (AIDE) | 一个结构化的 7 步工作流，用于借助 AI assistants 从零构建新项目，从愿景到实现全覆盖 | `process` | Read+Write | [aide](https://github.com/mnriem/spec-kit-extensions/tree/main/aide) |
| Architect Impact Previewer | 在实现前预测提议变更的架构影响、复杂度和风险。 | `visibility` | Read-only | [spec-kit-architect-preview](https://github.com/UmmeHabiba1312/spec-kit-architect-preview) |
| Archive Extension | 将已合并功能归档到主项目记忆中。 | `docs` | Read+Write | [spec-kit-archive](https://github.com/stn1slv/spec-kit-archive) |
| Azure DevOps Integration | 通过 OAuth 认证，将 user stories 和 tasks 同步到 Azure DevOps work items | `integration` | Read+Write | [spec-kit-azure-devops](https://github.com/pragya247/spec-kit-azure-devops) |
| Blueprint | 在 AI 驱动开发中保持对代码的可读性：在 `/speckit.implement` 运行前，先基于 spec artifacts 审查每个任务的完整代码蓝图 | `docs` | Read+Write | [spec-kit-blueprint](https://github.com/chordpli/spec-kit-blueprint) |
| Branch Convention | 为 `/specify` 提供可配置的分支和文件夹命名规范，支持预设和自定义模式 | `process` | Read+Write | [spec-kit-branch-convention](https://github.com/Quratulain-bilal/spec-kit-branch-convention) |
| Brownfield Bootstrap | 为已有代码库引导启用 spec-kit，自动发现架构并逐步采用 SDD | `process` | Read+Write | [spec-kit-brownfield](https://github.com/Quratulain-bilal/spec-kit-brownfield) |
| Bugfix Workflow | 结构化缺陷修复工作流，记录 bug、追溯到 spec artifacts，并对 specs 做外科式修补 | `process` | Read+Write | [spec-kit-bugfix](https://github.com/Quratulain-bilal/spec-kit-bugfix) |
| Canon | 增加 canon-driven（baseline-driven）工作流：spec-first、code-first、spec-drift。需要先安装 Canon Core preset。 | `process` | Read+Write | [spec-kit-canon](https://github.com/maximiliamus/spec-kit-canon/tree/master/extension) |
| Catalog CI | 针对 spec-kit community catalog 条目的自动化校验，包括结构、URL、diff 和 linting | `process` | Read-only | [spec-kit-catalog-ci](https://github.com/Quratulain-bilal/spec-kit-catalog-ci) |
| CI Guard | 面向 CI/CD 的 spec 合规门禁，验证 spec 是否存在、检查漂移，并在存在缺口时阻止合并 | `process` | Read-only | [spec-kit-ci-guard](https://github.com/Quratulain-bilal/spec-kit-ci-guard) |
| Checkpoint Extension | 在实现过程中提交中间改动，避免最终只留下一个超大的提交 | `code` | Read+Write | [spec-kit-checkpoint](https://github.com/aaronrsun/spec-kit-checkpoint) |
| Cleanup Extension | 实现后的质量门禁：审查改动、修复小问题（scout rule）、为中等问题创建任务，并为大问题生成分析 | `code` | Read+Write | [spec-kit-cleanup](https://github.com/dsrednicki/spec-kit-cleanup) |
| Conduct Extension | 通过子 agent 委派编排 spec-kit 阶段，以减少上下文污染。 | `process` | Read+Write | [spec-kit-conduct-ext](https://github.com/twbrandon7/spec-kit-conduct-ext) |
| Confluence Extension | 在 Confluence 中创建文档，汇总规格和规划文件 | `integration` | Read+Write | [spec-kit-confluence](https://github.com/aaronrsun/spec-kit-confluence) |
| DocGuard — CDD Enforcement | Canonical-Driven Development 执行器。通过自动检查、AI 驱动工作流和 spec-kit hooks 对项目文档进行校验、评分与追踪。零 NPM 运行时依赖。 | `docs` | Read+Write | [spec-kit-docguard](https://github.com/raccioly/docguard) |
| Extensify | 创建并校验 extensions 及 extension catalogs | `process` | Read+Write | [extensify](https://github.com/mnriem/spec-kit-extensions/tree/main/extensify) |
| Fix Findings | 自动化的 analyze-fix-reanalyze 循环，持续修复 spec findings 直到干净为止 | `code` | Read+Write | [spec-kit-fix-findings](https://github.com/Quratulain-bilal/spec-kit-fix-findings) |
| FixIt Extension | 感知 spec 的 bug 修复：将 bug 映射到 spec artifacts、提出计划，并应用最小变更 | `code` | Read+Write | [spec-kit-fixit](https://github.com/speckit-community/spec-kit-fixit) |
| Fleet Orchestrator | 通过 human-in-the-loop 门禁，编排覆盖所有 SpecKit 阶段的完整功能生命周期 | `process` | Read+Write | [spec-kit-fleet](https://github.com/sharathsatish/spec-kit-fleet) |
| GitHub Issues Integration 1 | 从 GitHub Issues 生成 spec artifacts，导入 issue、同步更新，并保持双向可追踪性 | `integration` | Read+Write | [spec-kit-github-issues](https://github.com/Fatima367/spec-kit-github-issues) |
| GitHub Issues Integration 2 | 从已有 GitHub issue 创建并同步本地 specs | `integration` | Read+Write | [spec-kit-issue](https://github.com/aaronrsun/spec-kit-issue) |
| Iterate | 使用双阶段 define-and-apply 工作流迭代 spec 文档，在实现中途精炼 specs，然后直接回到构建流程 | `docs` | Read+Write | [spec-kit-iterate](https://github.com/imviancagrace/spec-kit-iterate) |
| Jira Integration | 从 spec-kit 规格和任务拆解中创建 Jira Epics、Stories 和 Issues，支持可配置层级与自定义字段 | `integration` | Read+Write | [spec-kit-jira](https://github.com/mbachorik/spec-kit-jira) |
| Learning Extension | 从实现中生成教学指南，并为澄清过程加入导师式上下文 | `docs` | Read+Write | [spec-kit-learn](https://github.com/imviancagrace/spec-kit-learn) |
| MAQA — Multi-Agent & Quality Assurance | Coordinator → feature → QA agent 工作流，支持基于并行 worktree 的实现。语言无关。自动检测已安装的 board plugins。可选 CI gate。 | `process` | Read+Write | [spec-kit-maqa-ext](https://github.com/GenieRobot/spec-kit-maqa-ext) |
| MAQA Azure DevOps Integration | 面向 MAQA 的 Azure DevOps Boards 集成，在功能推进过程中同步 User Stories 和 Task children | `integration` | Read+Write | [spec-kit-maqa-azure-devops](https://github.com/GenieRobot/spec-kit-maqa-azure-devops) |
| MAQA CI/CD Gate | 自动检测 GitHub Actions、CircleCI、GitLab CI 和 Bitbucket Pipelines。在 pipeline 变绿前阻止 QA handoff。 | `process` | Read+Write | [spec-kit-maqa-ci](https://github.com/GenieRobot/spec-kit-maqa-ci) |
| MAQA GitHub Projects Integration | 面向 MAQA 的 GitHub Projects v2 集成，在功能推进过程中同步 draft issues 和 Status 列 | `integration` | Read+Write | [spec-kit-maqa-github-projects](https://github.com/GenieRobot/spec-kit-maqa-github-projects) |
| MAQA Jira Integration | 面向 MAQA 的 Jira 集成，在功能沿看板推进时同步 Stories 和 Subtasks | `integration` | Read+Write | [spec-kit-maqa-jira](https://github.com/GenieRobot/spec-kit-maqa-jira) |
| MAQA Linear Integration | 面向 MAQA 的 Linear 集成，在功能推进过程中跨工作流状态同步 issues 和 sub-issues | `integration` | Read+Write | [spec-kit-maqa-linear](https://github.com/GenieRobot/spec-kit-maqa-linear) |
| MAQA Trello Integration | 面向 MAQA 的 Trello 看板集成，从 specs 填充看板、移动卡片、实时勾选 checklist | `integration` | Read+Write | [spec-kit-maqa-trello](https://github.com/GenieRobot/spec-kit-maqa-trello) |
| MarkItDown Document Converter | 将文档（PDF、Word、PowerPoint、Excel 等）转换成 Markdown，用作 spec 参考材料 | `docs` | Read+Write | [spec-kit-markitdown](https://github.com/BenBtg/spec-kit-markitdown) |
| Memory Loader | 在生命周期命令运行前加载 `.specify/memory/` 文件，让 LLM agents 获得项目治理上下文 | `docs` | Read-only | [spec-kit-memory-loader](https://github.com/KevinBrown5280/spec-kit-memory-loader) |
| Memory MD | 面向 Spec Kit 项目的仓库原生持久记忆 | `docs` | Read+Write | [spec-kit-memory-hub](https://github.com/DyanGalih/spec-kit-memory-hub) |
| MemoryLint | agent 记忆治理工具：自动审计并修复 `AGENTS.md` 与 constitution 之间的边界冲突。 | `process` | Read+Write | [memorylint](https://github.com/RbBtSn0w/spec-kit-extensions/tree/main/memorylint) |
| Microsoft 365 Integration | 将 Teams 消息、会议转录和 SharePoint/OneDrive 文件提取为本地 Markdown，以供生成 spec | `integration` | Read+Write | [spec-kit-m365](https://github.com/BenBtg/spec-kit-m365) |
| Onboard | 为新接触 spec-kit 项目的开发者提供带上下文的 onboarding 与渐进成长。解释 specs、映射依赖、验证理解并引导下一步 | `process` | Read+Write | [spec-kit-onboard](https://github.com/dmux/spec-kit-onboard) |
| Optimize | 审计并优化面向 AI 的治理效率，包括 token 预算、规则健康度、可解释性、压缩、连贯性与回声检测 | `process` | Read+Write | [spec-kit-optimize](https://github.com/sakitA/spec-kit-optimize) |
| OWASP LLM Threat Model | 对 agent artifacts 执行 OWASP Top 10 for LLM Applications 2025 威胁分析 | `code` | Read-only | [spec-kit-threatmodel](https://github.com/NaviaSamal/spec-kit-threatmodel) |
| Plan Review Gate | 要求 `spec.md` 和 `plan.md` 必须先通过 MR/PR 合并后，才允许生成任务 | `process` | Read-only | [spec-kit-plan-review-gate](https://github.com/luno/spec-kit-plan-review-gate) |
| PR Bridge | 从 spec artifacts 自动生成 pull request 描述、checklists 和摘要 | `process` | Read-only | [spec-kit-pr-bridge-](https://github.com/Quratulain-bilal/spec-kit-pr-bridge-) |
| Presetify | 创建并校验 presets 与 preset catalogs | `process` | Read+Write | [presetify](https://github.com/mnriem/spec-kit-extensions/tree/main/presetify) |
| Product Forge | 从研究到发布覆盖完整产品生命周期，支持 portfolio、lite mode、monorepo 和可选 V-Model | `process` | Read+Write | [speckit-product-forge](https://github.com/VaiYav/speckit-product-forge) |
| Project Health Check | 诊断 Spec Kit 项目，并汇报结构、agents、features、scripts、extensions 和 git 方面的健康问题 | `visibility` | Read-only | [spec-kit-doctor](https://github.com/KhawarHabibKhan/spec-kit-doctor) |
| Project Status | 展示当前 SDD 工作流进度，包括 active feature、artifact 状态、任务完成度、工作流阶段和扩展摘要 | `visibility` | Read-only | [spec-kit-status](https://github.com/KhawarHabibKhan/spec-kit-status) |
| QA Testing Extension | 通过浏览器驱动或 CLI 驱动方式，系统性验证 spec 中的验收标准 | `code` | Read-only | [spec-kit-qa](https://github.com/arunt14/spec-kit-qa) |
| Ralph Loop | 使用 AI agent CLI 的自治实现循环 | `code` | Read+Write | [spec-kit-ralph](https://github.com/Rubiss/spec-kit-ralph) |
| Reconcile Extension | 通过外科式更新 feature artifacts 来调和实现漂移。 | `docs` | Read+Write | [spec-kit-reconcile](https://github.com/stn1slv/spec-kit-reconcile) |
| Red Team | 在 `/speckit.plan` 之前对 specs 做对抗式评审，通过并行镜头 agents 揭示 clarify/analyze 在结构上看不到的风险（prompt injection、完整性缺口、跨 spec 漂移、静默失败）。产出结构化 findings 报告；不会自动修改 specs。 | `docs` | Read+Write | [spec-kit-red-team](https://github.com/ashbrener/spec-kit-red-team) |
| Repository Index | 为现有仓库生成概览、架构和模块层级索引。 | `docs` | Read-only | [spec-kit-repoindex](https://github.com/liuyiyu/spec-kit-repoindex) |
| Retro Extension | 带指标、spec 准确性评估和改进建议的 sprint retrospective 分析 | `process` | Read+Write | [spec-kit-retro](https://github.com/arunt14/spec-kit-retro) |
| Retrospective Extension | 实现后的 retrospective，包含 spec 遵循度打分、漂移分析和人工门控的 spec 更新 | `docs` | Read+Write | [spec-kit-retrospective](https://github.com/emi-dm/spec-kit-retrospective) |
| Review Extension | 实现后的综合代码评审，借助专门 agents 检查代码质量、注释、测试、错误处理、类型设计和简化空间 | `code` | Read-only | [spec-kit-review](https://github.com/ismaelJimenez/spec-kit-review) |
| Ripple | 在实现后检测测试无法捕获的副作用，基于 delta 的分析覆盖 9 个领域无关类别 | `code` | Read+Write | [spec-kit-ripple](https://github.com/chordpli/spec-kit-ripple) |
| SDD Utilities | 恢复中断工作流、校验项目健康状态，并验证 spec 到 task 的可追踪性 | `process` | Read+Write | [speckit-utils](https://github.com/mvanhorn/speckit-utils) |
| Security Review | 面向整个项目的 secure-by-design 安全审计，以及分阶段、分支/PR、plan、task、follow-up 和 apply 评审 | `code` | Read+Write | [spec-kit-security-review](https://github.com/DyanGalih/spec-kit-security-review) |
| SFSpeckit | 面向企业 Salesforce SDLC 的完整 SDD 生命周期 18 条命令。 | `process` | Read+Write | [spec-kit-sf](https://github.com/ysumanth06/spec-kit-sf) |
| Ship Release Extension | 自动化发布流水线，包括预检、分支同步、changelog 生成、CI 校验和 PR 创建 | `process` | Read+Write | [spec-kit-ship](https://github.com/arunt14/spec-kit-ship) |
| Spec Reference Loader | 读取 feature spec 中的 `## References` 部分，并仅把列出的文档加载进上下文 | `docs` | Read-only | [spec-kit-spec-reference-loader](https://github.com/KevinBrown5280/spec-kit-spec-reference-loader) |
| Spec Critique Extension | 从产品策略和工程风险两个视角，对 spec 和 plan 进行双镜头批判性评审 | `docs` | Read-only | [spec-kit-critique](https://github.com/arunt14/spec-kit-critique) |
| Spec Diagram | 自动生成 SDD 工作流状态、feature 进度和任务依赖关系的 Mermaid 图 | `visibility` | Read-only | [spec-kit-diagram-](https://github.com/Quratulain-bilal/spec-kit-diagram-) |
| Spec Orchestrator | 跨 feature 编排，跟踪状态、选择任务并检测并行 specs 之间的冲突 | `process` | Read-only | [spec-kit-orchestrator](https://github.com/Quratulain-bilal/spec-kit-orchestrator) |
| Spec Refine | 原地更新 specs，把变更传播到 plan 和 tasks，并对各类 artifacts 的影响做 diff | `process` | Read+Write | [spec-kit-refine](https://github.com/Quratulain-bilal/spec-kit-refine) |
| Spec Scope | 工作量估算与范围跟踪：估算工作、检测范围蔓延，并为每个阶段做时间预算 | `process` | Read-only | [spec-kit-scope-](https://github.com/Quratulain-bilal/spec-kit-scope-) |
| Spec Sync | 检测并解决 specs 与实现之间的漂移。AI 辅助处理，需人工批准 | `docs` | Read+Write | [spec-kit-sync](https://github.com/bgervin/spec-kit-sync) |
| Spec Validate | 面向 spec-kit artifacts 的理解校验、评审门禁和审批状态，包含分阶段测验、peer review SLA，以及 `/speckit.implement` 前的硬门禁 | `process` | Read+Write | [spec-kit-spec-validate](https://github.com/aeltayeb/spec-kit-spec-validate) |
| Spec2Cloud | 面向 Azure 发布优化的 spec-driven 工作流 | `process` | Read+Write | [spec2cloud](https://github.com/Azure-Samples/Spec2Cloud) |
| SpecTest | 根据 spec 标准自动生成测试脚手架、映射覆盖率并发现未测试需求 | `code` | Read+Write | [spec-kit-spectest](https://github.com/Quratulain-bilal/spec-kit-spectest) |
| Squad Bridge | 根据你的 Speckit spec 和 tasks，引导并同步一个 Squad agent 团队 | `process` | Read+Write | [spec-kit-squad](https://github.com/jwill824/spec-kit-squad) |
| Staff Review Extension | Staff-engineer 级别代码评审，验证实现是否符合 spec，并检查安全性、性能和测试覆盖率 | `code` | Read-only | [spec-kit-staff-review](https://github.com/arunt14/spec-kit-staff-review) |
| Status Report | 面向 spec-driven 工作流的项目状态、feature 进度和下一步建议 | `visibility` | Read-only | [Open-Agent-Tools/spec-kit-status](https://github.com/Open-Agent-Tools/spec-kit-status) |
| Superpowers Bridge | 在完整生命周期中，将 obra/superpowers skills 编排进 spec-kit SDD 工作流（clarification、TDD、review、verification、critique、debugging、branch completion） | `process` | Read+Write | [superpowers-bridge](https://github.com/RbBtSn0w/spec-kit-extensions/tree/main/superpowers-bridge) |
| Superpowers Bridge (WangX0111) | 将 spec-kit 与 obra/superpowers（brainstorming、TDD、subagent、code-review）桥接为统一、可恢复的工作流，并具备优雅降级与 session 进度跟踪 | `process` | Read+Write | [superspec](https://github.com/WangX0111/superspec) |
| TinySpec | 面向小任务的轻量级单文件工作流，可跳过厚重的多步骤 SDD 流程 | `process` | Read+Write | [spec-kit-tinyspec](https://github.com/Quratulain-bilal/spec-kit-tinyspec) |
| Token Consumption Analyzer | 采集、分析并比较 SDD 工作流中的 token 消耗 | `visibility` | Read-only | [spec-kit-token-analyzer](https://github.com/coderandhiker/spec-kit-token-analyzer) |
| V-Model Extension Pack | 强制使用 V-Model 成对生成开发 specs 和测试 specs，并保证完整可追踪性 | `docs` | Read+Write | [spec-kit-v-model](https://github.com/leocamello/spec-kit-v-model) |
| Verify Extension | 实现后的质量门禁，校验已实现代码是否符合 specification artifacts | `code` | Read-only | [spec-kit-verify](https://github.com/ismaelJimenez/spec-kit-verify) |
| Verify Tasks Extension | 检测伪完成：`tasks.md` 中标记为 `[X]` 但实际上并没有真正实现的任务 | `code` | Read-only | [spec-kit-verify-tasks](https://github.com/datastone-inc/spec-kit-verify-tasks) |
| Version Guard | 在规划和实现前，根据实时 npm registries 校验技术栈版本 | `process` | Read-only | [spec-kit-version-guard](https://github.com/KevinBrown5280/spec-kit-version-guard) |
| What-if Analysis | 在真正提交需求变更之前，预览其下游影响（复杂度、工作量、任务、风险） | `visibility` | Read-only | [spec-kit-whatif](https://github.com/DevAbdullah90/spec-kit-whatif) |
| Wireframe Visual Feedback Loop | 用于 spec-driven development 的 SVG 线框图生成、评审与签署流程。已批准的线框图会成为 `/speckit.plan`、`/speckit.tasks` 和 `/speckit.implement` 必须遵守的 spec 约束 | `visibility` | Read+Write | [spec-kit-extension-wireframe](https://github.com/TortoiseWolfe/spec-kit-extension-wireframe) |
| Work IQ | 将 Microsoft 365 组织知识整合进 spec-driven development 工作流 | `integration` | Read-only | [spec-kit-workiq](https://github.com/sakitA/spec-kit-workiq) |
| Worktree Isolation | 为并行 feature 开发创建隔离的 git worktrees，无需来回切换 checkout | `process` | Read+Write | [spec-kit-worktree](https://github.com/Quratulain-bilal/spec-kit-worktree) |
| Worktrees | 默认开启的 worktree 隔离，服务于并行 agents，支持 sibling 或 nested 布局 | `process` | Read+Write | [spec-kit-worktree-parallel](https://github.com/dango85/spec-kit-worktree-parallel) |

如果你想提交自己的 extension，请参阅 [Extension Publishing Guide](extensions/EXTENSION-PUBLISHING-GUIDE.md)。

## 🎨 Community Presets

Community-contributed presets 用于自定义 Spec Kit 的行为，可覆盖模板、命令和术语，而无需修改任何工具本身。完整列表见 [Community Presets](https://github.github.io/spec-kit/community/presets.html) 页面。

> [!NOTE]
> Community presets 属于第三方贡献，并非由 Spec Kit 团队维护。使用前请认真检查，完整免责声明见上方文档页面。

如果你想提交自己的 preset，请参阅 [Presets Publishing Guide](presets/PUBLISHING.md)。

## 🚶 Community Walkthroughs

通过 community-contributed walkthroughs，查看 Spec-Driven Development 在不同场景中的实际效果；完整列表见 [Community Walkthroughs](https://github.github.io/spec-kit/community/walkthroughs.html) 页面。

## 🛠️ Community Friends

这些社区项目在 Spec Kit 之上进行了扩展、可视化或二次构建。完整列表见 [Community Friends](https://github.github.io/spec-kit/community/friends.html) 页面。

## 🤖 支持的 AI Coding Agent 集成

Spec Kit 可与 30+ AI coding agents 配合使用，既包括 CLI 工具，也包括基于 IDE 的助手。完整列表、说明和使用细节见 [Supported AI Coding Agent Integrations](https://github.github.io/spec-kit/reference/integrations.html) 指南。

运行 `specify integration list` 可查看当前已安装版本中所有可用集成。

## 可用 Slash Commands

执行 `specify init` 后，你的 AI coding agent 将获得以下 slash commands，以支持结构化开发。对于支持 skills mode 的集成，传入 `--integration <agent> --integration-options="--skills"` 会安装 agent skills，而不是 slash-command prompt files。

#### Core Commands

Spec-Driven Development 工作流中的核心命令：

| Command                  | Agent Skill            | Description                                                                |
| ------------------------ | ---------------------- | -------------------------------------------------------------------------- |
| `/speckit.constitution`  | `speckit-constitution` | 创建或更新项目治理原则与开发指南 |
| `/speckit.specify`       | `speckit-specify`      | 定义你要构建什么（需求和用户故事） |
| `/speckit.plan`          | `speckit-plan`         | 基于你选择的技术栈创建技术实现计划 |
| `/speckit.tasks`         | `speckit-tasks`        | 生成可执行的实现任务清单 |
| `/speckit.taskstoissues` | `speckit-taskstoissues`| 将生成的任务清单转换为 GitHub issues 以便跟踪和执行 |
| `/speckit.implement`     | `speckit-implement`    | 按照计划执行所有任务并完成功能构建 |

#### Optional Commands

用于增强质量与校验的附加命令：

| Command              | Agent Skill            | Description                                                                                                                          |
| -------------------- | ---------------------- | ------------------------------------------------------------------------------------------------------------------------------------ |
| `/speckit.clarify`   | `speckit-clarify`      | 澄清描述不足的区域（建议在 `/speckit.plan` 前使用；此前名为 `/quizme`） |
| `/speckit.analyze`   | `speckit-analyze`      | 跨 artifact 的一致性与覆盖率分析（在 `/speckit.tasks` 之后、`/speckit.implement` 之前运行） |
| `/speckit.checklist` | `speckit-checklist`    | 生成自定义质量检查清单，用于验证需求的完整性、清晰度和一致性（相当于“针对英文需求的单元测试”） |

## 🔧 Specify CLI Reference

如需完整命令细节、选项和示例，请参阅 [CLI Reference](https://github.github.io/spec-kit/reference/overview.html)。

## 🧩 让 Spec Kit 变成你的样子：Extensions 与 Presets

Spec Kit 可以通过两套互补机制进行定制：**extensions** 和 **presets**，另外还支持项目本地 override 用于一次性调整：

| Priority | Component Type                                    | Location                         |
| -------: | ------------------------------------------------- | -------------------------------- |
|      ⬆ 1 | Project-Local Overrides                           | `.specify/templates/overrides/`  |
|        2 | Presets — Customize core & extensions             | `.specify/presets/templates/`    |
|        3 | Extensions — Add new capabilities                 | `.specify/extensions/templates/` |
|      ⬇ 4 | Spec Kit Core — Built-in SDD commands & templates | `.specify/templates/`            |

- **Templates** 在**运行时**解析。Spec Kit 会自上而下遍历这条栈，并使用第一个命中的版本。
- 项目本地 overrides（`.specify/templates/overrides/`）允许你在单个项目里做一次性调整，而不必创建完整 preset。
- **Extension/preset commands** 在**安装时**生效。运行 `specify extension add` 或 `specify preset add` 时，命令文件会写入 agent 目录（例如 `.claude/commands/`）。
- 如果多个 presets 或 extensions 提供同一个命令，则优先级最高的版本生效。移除后，会自动恢复次高优先级版本。
- 如果没有任何 overrides 或自定义配置，Spec Kit 会使用其核心默认值。

### Extensions — Add New Capabilities

当你需要超出 Spec Kit 核心能力的功能时，请使用 **extensions**。Extensions 会引入新的命令和模板，例如增加内置 SDD 命令未覆盖的领域特定工作流、与外部工具集成，或新增整个开发阶段。它们扩展的是 *Spec Kit 能做什么*。

```bash
# Search available extensions
specify extension search

# Install an extension
specify extension add <extension-name>
```

例如，extensions 可以增加 Jira 集成、实现后的代码评审、V-Model 测试可追踪性，或项目健康诊断能力。

完整命令说明请参阅 [Extensions reference](https://github.github.io/spec-kit/reference/extensions.html)。可用扩展可浏览上面的 [community extensions](#-community-extensions)。

### Presets — Customize Existing Workflows

当你想改变 Spec Kit 的工作方式，而不是增加新能力时，请使用 **presets**。Presets 会覆盖核心以及已安装 extensions 自带的模板和命令，例如强制采用以合规为导向的 spec 格式、使用领域特定术语，或把组织标准应用到 plans 和 tasks 中。它们定制的是 Spec Kit 及其 extensions 产出的 artifacts 和 instructions。

```bash
# Search available presets
specify preset search

# Install a preset
specify preset add <preset-name>
```

例如，presets 可以重构 spec 模板以强制要求监管可追踪性、让工作流适配你正在使用的方法论（如 Agile、Kanban、Waterfall、jobs-to-be-done 或 domain-driven design）、为 plans 增加强制安全评审门禁、强制测试优先的任务顺序，或将整个工作流本地化到另一种语言。[pirate-speak demo](https://github.com/mnriem/spec-kit-pirate-speak-preset-demo) 展示了这种定制能力究竟可以有多深。多个 presets 可以按优先级叠加使用。

完整命令说明及解析顺序、优先级叠加规则，请参阅 [Presets reference](https://github.github.io/spec-kit/reference/presets.html)。

### When to Use Which

| Goal | Use |
| --- | --- |
| 增加一个全新的命令或工作流 | Extension |
| 自定义 specs、plans 或 tasks 的格式 | Preset |
| 集成外部工具或服务 | Extension |
| 强制执行组织级或监管级标准 | Preset |
| 交付可复用的领域模板 | 二者皆可，模板覆盖用 preset，带新命令的模板打包用 extension |

## 📚 核心理念

Spec-Driven Development 是一个结构化过程，强调：

- **意图驱动开发**，先由 specification 定义 "*what*"，再处理 "*how*"
- 借助护栏和组织原则进行**高质量 specification 创建**
- 采用**多步精炼**，而不是从 prompts 一次性生成代码
- **高度依赖** 高级 AI 模型对 specifications 的理解能力

## 🌟 开发阶段

| Phase                                    | Focus                    | Key Activities                                                                                                                                                     |
| ---------------------------------------- | ------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **0-to-1 Development** ("Greenfield")    | 从零生成 | <ul><li>从高层需求开始</li><li>生成 specifications</li><li>规划实现步骤</li><li>构建可用于生产的应用</li></ul> |
| **Creative Exploration**                 | 并行实现 | <ul><li>探索多样化解决方案</li><li>支持多种技术栈与架构</li><li>试验不同 UX 模式</li></ul> |
| **Iterative Enhancement** ("Brownfield") | 存量系统现代化 | <ul><li>迭代添加功能</li><li>改造遗留系统</li><li>适配流程</li></ul> |

## 🎯 实验目标

我们的研究与实验重点包括：

### Technology independence

- 使用不同技术栈创建应用
- 验证这样一个假设：Spec-Driven Development 是一种过程，不绑定具体技术、编程语言或框架

### Enterprise constraints

- 展示关键任务型应用的开发方式
- 纳入组织约束（云厂商、技术栈、工程实践）
- 支持企业设计系统和合规要求

### User-centric development

- 面向不同用户群体及偏好构建应用
- 支持多种开发方式（从 vibe-coding 到 AI-native development）

### Creative & iterative processes

- 验证并行实现探索这一概念
- 提供健壮的迭代式功能开发工作流
- 将流程扩展到升级和现代化任务

## 🔧 前置条件

- **Linux/macOS/Windows**
- [Supported](#-supported-ai-coding-agent-integrations) AI coding agent
- 推荐使用 [uv](https://docs.astral.sh/uv/) 做包管理，或使用 [pipx](https://pypa.github.io/pipx/) 做持久化安装
- [Python 3.11+](https://www.python.org/downloads/)
- [Git](https://git-scm.com/downloads)

如果你在某个 agent 上遇到问题，请提 issue，以便我们持续改进集成效果。

## 📖 进一步了解

- **[Complete Spec-Driven Development Methodology](./spec-driven.md)** - 深入了解完整流程
- **[Detailed Walkthrough](#-detailed-process)** - 逐步实现指南

---

## 📋 详细流程

<details>
<summary>点击展开详细的逐步 walkthrough</summary>

你可以使用 Specify CLI 为项目完成 bootstrap，这会把所需 artifacts 带入当前环境。执行：

```bash
specify init <project_name>
```

或者在当前目录中初始化：

```bash
specify init .
# or use the --here flag
specify init --here
# Skip confirmation when the directory already has files
specify init . --force
# or
specify init --here --force
```

![Specify CLI bootstrapping a new project in the terminal](./media/specify_cli.gif)

系统会提示你选择当前正在使用的 coding agent 集成。你也可以直接在终端里显式指定：

```bash
specify init <project_name> --integration copilot
specify init <project_name> --integration gemini
specify init <project_name> --integration codex

# Or in current directory:
specify init . --integration copilot
specify init . --integration codex --integration-options="--skills"

# or use --here flag
specify init --here --integration copilot
specify init --here --integration codex --integration-options="--skills"

# Force merge into a non-empty current directory
specify init . --force --integration copilot

# or
specify init --here --force --integration copilot
```

CLI 会检查你是否安装了 Claude Code、Gemini CLI、Cursor CLI、Qwen CLI、opencode、Codex CLI、Qoder CLI、Tabnine CLI、Kiro CLI、Pi、Forge、Goose 或 Mistral Vibe。如果你没有安装，或者你只是想获取模板而不检查本地工具，可以在命令中加入 `--ignore-agent-tools`：

```bash
specify init <project_name> --integration copilot --ignore-agent-tools
```

### **STEP 1:** 建立项目原则

进入项目目录并运行你的 coding agent。我们的示例中使用的是 `claude`。

![Bootstrapping Claude Code environment](./media/bootstrap-claude-code.gif)

如果你能看到 `/speckit.constitution`、`/speckit.specify`、`/speckit.plan`、`/speckit.tasks` 和 `/speckit.implement` 这些命令，就说明配置正确。

第一步应该是通过 `/speckit.constitution` 建立项目治理原则。这有助于在后续所有开发阶段中保持一致的决策方式：

```text
/speckit.constitution Create principles focused on code quality, testing standards, user experience consistency, and performance requirements. Include governance for how these principles should guide technical decisions and implementation choices.
```

这一步会创建或更新 `.specify/memory/constitution.md` 文件，其中包含项目的基础性指南，coding agent 会在 specification、planning 和 implementation 阶段引用它。

### **STEP 2:** 创建项目规格

当项目原则建立好之后，你就可以开始创建功能规格。使用 `/speckit.specify` 命令，然后提供你希望开发的项目的具体需求。

> [!IMPORTANT]
> 尽可能明确地说明你想构建的 *what* 和 *why*。**此时不要聚焦技术栈**。

一个示例 prompt：

```text
Develop Taskify, a team productivity platform. It should allow users to create projects, add team members,
assign tasks, comment and move tasks between boards in Kanban style. In this initial phase for this feature,
let's call it "Create Taskify," let's have multiple users but the users will be declared ahead of time, predefined.
I want five users in two different categories, one product manager and four engineers. Let's create three
different sample projects. Let's have the standard Kanban columns for the status of each task, such as "To Do,"
"In Progress," "In Review," and "Done." There will be no login for this application as this is just the very
first testing thing to ensure that our basic features are set up. For each task in the UI for a task card,
you should be able to change the current status of the task between the different columns in the Kanban work board.
You should be able to leave an unlimited number of comments for a particular card. You should be able to, from that task
card, assign one of the valid users. When you first launch Taskify, it's going to give you a list of the five users to pick
from. There will be no password required. When you click on a user, you go into the main view, which displays the list of
projects. When you click on a project, you open the Kanban board for that project. You're going to see the columns.
You'll be able to drag and drop cards back and forth between different columns. You will see any cards that are
assigned to you, the currently logged in user, in a different color from all the other ones, so you can quickly
see yours. You can edit any comments that you make, but you can't edit comments that other people made. You can
delete any comments that you made, but you can't delete comments anybody else made.
```

输入这个 prompt 后，你应该会看到 Claude Code 开始进行规划和 spec 起草。Claude Code 还会触发一些内置脚本来初始化仓库。

这一步完成后，你应该会得到一个新分支（例如 `001-create-taskify`），以及位于 `specs/001-create-taskify` 目录下的新 specification。

生成的 specification 应包含模板中定义的一组 user stories 和 functional requirements。

在这个阶段，项目目录内容大致应如下所示：

```text
└── .specify
    ├── memory
    │  └── constitution.md
    ├── scripts
    │  ├── check-prerequisites.sh
    │  ├── common.sh
    │  ├── create-new-feature.sh
    │  ├── setup-plan.sh
    │  └── update-claude-md.sh
    ├── specs
    │  └── 001-create-taskify
    │      └── spec.md
    └── templates
        ├── plan-template.md
        ├── spec-template.md
        └── tasks-template.md
```

### **STEP 3:** 功能规格澄清（规划前必须完成）

有了基础 specification 后，你可以继续澄清第一次生成时没有准确捕获的需求。

你应该在创建技术计划**之前**运行结构化澄清工作流，以减少后续返工。

推荐顺序：

1. 先使用 `/speckit.clarify`（结构化）进行按覆盖率推进的顺序提问，并将答案记录在 Clarifications 部分。
2. 如果还有模糊之处，再选择性地补充自由形式 refinement。

如果你有意跳过澄清（例如做 spike 或探索性原型），请明确说明，这样 agent 就不会因为缺少澄清而阻塞。

自由形式 refinement prompt 示例（如果 `/speckit.clarify` 后仍有需要）：

```text
For each sample project or project that you create there should be a variable number of tasks between 5 and 15
tasks for each one randomly distributed into different states of completion. Make sure that there's at least
one task in each stage of completion.
```

你还应该让 Claude Code 校验 **Review & Acceptance Checklist**，把满足要求的项勾选上，不满足的保持未勾选。可以使用下面这个 prompt：

```text
Read the review and acceptance checklist, and check off each item in the checklist if the feature spec meets the criteria. Leave it empty if it does not.
```

要把和 Claude Code 的交互当作澄清与追问 specification 的机会，**不要把它的第一次产出当成最终稿**。

### **STEP 4:** 生成计划

现在你可以明确技术栈和其他技术要求了。可以使用项目模板内置的 `/speckit.plan` 命令，并配合下面这样的 prompt：

```text
We are going to generate this using .NET Aspire, using Postgres as the database. The frontend should use
Blazor server with drag-and-drop task boards, real-time updates. There should be a REST API created with a projects API,
tasks API, and a notifications API.
```

这一步的输出将包括若干实现细节文档，此时目录树大致会变成这样：

```text
.
├── CLAUDE.md
├── memory
│  └── constitution.md
├── scripts
│  ├── check-prerequisites.sh
│  ├── common.sh
│  ├── create-new-feature.sh
│  ├── setup-plan.sh
│  └── update-claude-md.sh
├── specs
│  └── 001-create-taskify
│      ├── contracts
│      │  ├── api-spec.json
│      │  └── signalr-spec.md
│      ├── data-model.md
│      ├── plan.md
│      ├── quickstart.md
│      ├── research.md
│      └── spec.md
└── templates
    ├── CLAUDE-template.md
    ├── plan-template.md
    ├── spec-template.md
    └── tasks-template.md
```

请检查 `research.md`，确认其中使用的技术栈符合你的要求。如果某些组件看起来不对，你可以要求 Claude Code 做 refinement，甚至让它检查你本地安装的平台/框架版本（例如 .NET）。

此外，如果所选技术栈变化很快（例如 .NET Aspire、JS frameworks），你可能还想让 Claude Code 进一步研究相关细节，可以使用如下 prompt：

```text
I want you to go through the implementation plan and implementation details, looking for areas that could
benefit from additional research as .NET Aspire is a rapidly changing library. For those areas that you identify that
require further research, I want you to update the research document with additional details about the specific
versions that we are going to be using in this Taskify application and spawn parallel research tasks to clarify
any details using research from the web.
```

在这个过程中，你可能会发现 Claude Code 研究错了方向。这时你可以用类似下面的 prompt 把它拉回正确轨道：

```text
I think we need to break this down into a series of steps. First, identify a list of tasks
that you would need to do during implementation that you're not sure of or would benefit
from further research. Write down a list of those tasks. And then for each one of these tasks,
I want you to spin up a separate research task so that the net results is we are researching
all of those very specific tasks in parallel. What I saw you doing was it looks like you were
researching .NET Aspire in general and I don't think that's gonna do much for us in this case.
That's way too untargeted research. The research needs to help you solve a specific targeted question.
```

> [!NOTE]
> Claude Code 可能会过度积极，擅自加入你并未要求的组件。请要求它解释这么做的理由以及变更来源。

### **STEP 5:** 让 Claude Code 校验计划

有了计划后，你应该让 Claude Code 通读一遍，确认没有遗漏。可以使用类似下面的 prompt：

```text
Now I want you to go and audit the implementation plan and the implementation detail files.
Read through it with an eye on determining whether or not there is a sequence of tasks that you need
to be doing that are obvious from reading this. Because I don't know if there's enough here. For example,
when I look at the core implementation, it would be useful to reference the appropriate places in the implementation
details where it can find the information as it walks through each step in the core implementation or in the refinement.
```

这有助于进一步打磨实现计划，并避免 Claude Code 在其规划周期中漏掉潜在盲点。第一次 refinement 完成后，再让 Claude Code 过一遍 checklist，然后再进入实现阶段。

如果你安装了 [GitHub CLI](https://docs.github.com/en/github-cli/github-cli)，你也可以让 Claude Code 直接从当前分支向 `main` 创建一个带详细描述的 pull request，以确保这项工作被正确跟踪。

> [!NOTE]
> 在真正让 agent 开始实现之前，也值得提示 Claude Code 交叉检查细节，看看是否存在过度设计的部分。要记住，它可能会过度积极。如果存在过度设计的组件或决策，你可以让 Claude Code 进行收敛。请确保 Claude Code 在建立计划时遵循 [constitution](base/memory/constitution.md)，并将其作为必须遵守的基础约束。

### **STEP 6:** 使用 /speckit.tasks 生成任务拆解

在实现计划通过验证后，你就可以把计划拆解为按正确顺序执行的具体可操作任务。使用 `/speckit.tasks` 命令，可根据实现计划自动生成详细的任务拆解：

```text
/speckit.tasks
```

这一步会在 feature specification 目录下创建一个 `tasks.md` 文件，其中包括：

- **按 user story 组织的任务拆解** - 每个 user story 都会成为单独的实现阶段，并拥有自己的一组任务
- **依赖管理** - 任务顺序会尊重组件之间的依赖关系（例如 models 先于 services，services 先于 endpoints）
- **并行执行标记** - 可并行执行的任务会标记为 `[P]`，以优化开发工作流
- **文件路径说明** - 每个任务都包含应当实施改动的精确文件路径
- **测试驱动开发结构** - 如果要求编写测试，则会包含测试任务，并安排在实现之前编写
- **检查点验证** - 每个 user story 阶段都会包含检查点，用于验证该阶段可独立运行

生成出来的 `tasks.md` 为 `/speckit.implement` 提供清晰路线图，确保实现过程系统化、代码质量可控，并支持按 user stories 增量交付。

### **STEP 7:** 实现

准备就绪后，使用 `/speckit.implement` 执行你的实现计划：

```text
/speckit.implement
```

`/speckit.implement` 命令会：

- 校验所有前置条件是否齐备（constitution、spec、plan 和 tasks）
- 从 `tasks.md` 解析任务拆解
- 按正确顺序执行任务，同时遵守依赖和并行执行标记
- 遵循任务计划中定义的 TDD 方式
- 提供进度更新，并妥善处理错误

> [!IMPORTANT]
> coding agent 会执行本地 CLI 命令（例如 `dotnet`、`npm` 等），请确保你的机器已安装所需工具。

实现完成后，请测试应用，并解决那些在 CLI 日志中不一定看得见的运行时错误（例如浏览器控制台错误）。你可以把这些错误复制粘贴回 coding agent，让它继续处理。

</details>

---

## 🔍 故障排查

### Git Credential Manager on Linux

如果你在 Linux 上遇到 Git 认证问题，可以安装 Git Credential Manager：

```bash
#!/usr/bin/env bash
set -e
echo "Downloading Git Credential Manager v2.6.1..."
wget https://github.com/git-ecosystem/git-credential-manager/releases/download/v2.6.1/gcm-linux_amd64.2.6.1.deb
echo "Installing Git Credential Manager..."
sudo dpkg -i gcm-linux_amd64.2.6.1.deb
echo "Configuring Git to use GCM..."
git config --global credential.helper manager
echo "Cleaning up..."
rm gcm-linux_amd64.2.6.1.deb
```

## 💬 支持

如需支持，请提交一个 [GitHub issue](https://github.com/github/spec-kit/issues/new)。我们欢迎 bug 报告、功能请求，以及关于如何使用 Spec-Driven Development 的问题。

## 🙏 致谢

本项目深受 [John Lam](https://github.com/jflam) 的工作与研究影响，并在此基础上发展而来。

## 📄 许可证

本项目基于 MIT 开源许可证发布。完整条款请参阅 [LICENSE](./LICENSE) 文件。
