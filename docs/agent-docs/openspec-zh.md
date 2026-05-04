<p align="center">
  <a href="https://github.com/Fission-AI/OpenSpec">
    <picture>
      <source srcset="assets/openspec_bg.png">
      <img src="assets/openspec_bg.png" alt="OpenSpec logo">
    </picture>
  </a>
</p>

<p align="center">
  <a href="https://github.com/Fission-AI/OpenSpec/actions/workflows/ci.yml"><img alt="CI" src="https://github.com/Fission-AI/OpenSpec/actions/workflows/ci.yml/badge.svg" /></a>
  <a href="https://www.npmjs.com/package/@fission-ai/openspec"><img alt="npm version" src="https://img.shields.io/npm/v/@fission-ai/openspec?style=flat-square" /></a>
  <a href="./LICENSE"><img alt="License: MIT" src="https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square" /></a>
  <a href="https://discord.gg/YctCnvvshC"><img alt="Discord" src="https://img.shields.io/discord/1411657095639601154?style=flat-square&logo=discord&logoColor=white&label=Discord&suffix=%20online" /></a>
</p>

<details>
<summary><strong>最受喜爱的规范框架。</strong></summary>

[![Stars](https://img.shields.io/github/stars/Fission-AI/OpenSpec?style=flat-square&label=Stars)](https://github.com/Fission-AI/OpenSpec/stargazers)
[![Downloads](https://img.shields.io/npm/dm/@fission-ai/openspec?style=flat-square&label=Downloads/mo)](https://www.npmjs.com/package/@fission-ai/openspec)
[![Contributors](https://img.shields.io/github/contributors/Fission-AI/OpenSpec?style=flat-square&label=Contributors)](https://github.com/Fission-AI/OpenSpec/graphs/contributors)

</details>
<p></p>
我们的理念：

```text
→ fluid not rigid
→ iterative not waterfall
→ easy not complex
→ built for brownfield not just greenfield
→ scalable from personal projects to enterprises
```

> [!TIP]
> **现已提供全新工作流！** 我们使用全新的 artifact-guided workflow 重构了 OpenSpec。
>
> Run `/opsx:propose "your idea"` to get started. → [Learn more here](docs/opsx.md)

<p align="center">
  关注 <a href="https://x.com/0xTab">@0xTab on X</a> 获取更新 · 加入 <a href="https://discord.gg/YctCnvvshC">OpenSpec Discord</a> 寻求帮助和提问。
</p>

<!-- TODO: Add GIF demo of /opsx:propose → /opsx:archive workflow -->

## 实际效果

```text
You: /opsx:propose add-dark-mode
AI:  Created openspec/changes/add-dark-mode/
     ✓ proposal.md — why we're doing this, what's changing
     ✓ specs/       — requirements and scenarios
     ✓ design.md    — technical approach
     ✓ tasks.md     — implementation checklist
     Ready for implementation!

You: /opsx:apply
AI:  Implementing tasks...
     ✓ 1.1 Add theme context provider
     ✓ 1.2 Create toggle component
     ✓ 2.1 Add CSS variables
     ✓ 2.2 Wire up localStorage
     All tasks complete!

You: /opsx:archive
AI:  Archived to openspec/changes/archive/2025-01-23-add-dark-mode/
     Specs updated. Ready for the next feature.
```

<details>
<summary><strong>OpenSpec Dashboard</strong></summary>

<p align="center">
  <img src="assets/openspec_dashboard.png" alt="OpenSpec dashboard preview" width="90%">
</p>

</details>

## 快速开始

**需要 Node.js 20.19.0 或更高版本。**

全局安装 OpenSpec：

```bash
npm install -g @fission-ai/openspec@latest
```

然后进入你的项目目录并进行初始化：

```bash
cd your-project
openspec init
```

现在告诉你的 AI：`/opsx:propose <what-you-want-to-build>`

如果你想使用扩展工作流（`/opsx:new`, `/opsx:continue`, `/opsx:ff`, `/opsx:verify`, `/opsx:bulk-archive`, `/opsx:onboard`），请通过 `openspec config profile` 选择，并用 `openspec update` 应用。

> [!NOTE]
> 不确定你的工具是否受支持？[查看完整列表](docs/supported-tools.md) - 我们已支持 25+ 工具，并且还在持续增长。
>
> 也支持 pnpm、yarn、bun 和 nix。[查看安装选项](docs/installation.md)。

## 文档

→ **[Getting Started](docs/getting-started.md)**: 入门第一步<br>
→ **[Workflows](docs/workflows.md)**: 组合方式与模式<br>
→ **[Commands](docs/commands.md)**: slash commands 与 skills<br>
→ **[CLI](docs/cli.md)**: 终端参考<br>
→ **[Supported Tools](docs/supported-tools.md)**: 工具集成与安装路径<br>
→ **[Concepts](docs/concepts.md)**: 整体如何协同工作<br>
→ **[Multi-Language](docs/multi-language.md)**: 多语言支持<br>
→ **[Customization](docs/customization.md)**: 按你的方式定制


## 社区 schemas

通过独立仓库分发的第三方 schema bundles，它们提供带有明确倾向的工作流，用于将 OpenSpec 与其他工具集成，这一点类似于 [github/spec-kit's community extension catalog](https://github.com/github/spec-kit/tree/main/extensions) 处理工具集成的方式。

→ **[Browse the catalog](docs/customization.md#community-schemas)**：在 customization 文档中查看目录。


## 为什么选择 OpenSpec？

当需求只存在于聊天记录里时，AI coding assistants 虽然强大，但结果往往不可预测。OpenSpec 增加了一层轻量的 spec 层，让你在写任何代码之前，先就“要构建什么”达成一致。

- **先达成一致，再开始构建** —— 人与 AI 在代码落地前先对 specs 对齐
- **保持有序** —— 每个变更都有独立文件夹，包含 proposal、specs、design 和 tasks
- **流畅工作** —— 可在任意时刻更新任意 artifact，没有僵硬的阶段门禁
- **使用你自己的工具** —— 通过 slash commands 支持 20+ AI assistants

### 我们如何对比

**vs. [Spec Kit](https://github.com/github/spec-kit)**（GitHub）—— 很全面，但也很重。阶段门禁严格、Markdown 很多、还需要 Python 环境。OpenSpec 更轻，也允许你自由迭代。

**vs. [Kiro](https://kiro.dev)**（AWS）—— 很强大，但你被锁定在他们的 IDE 中，而且只能使用 Claude 模型。OpenSpec 可以与你已经在使用的工具协作。

**vs. 什么都不用** —— 没有 specs 的 AI coding 往往意味着模糊的 prompts 和不可预测的结果。OpenSpec 在不增加繁文缛节的前提下带来可预测性。

## 更新 OpenSpec

**升级 package**

```bash
npm install -g @fission-ai/openspec@latest
```

**刷新 agent instructions**

在每个项目中运行下面这条命令，以重新生成 AI 指导内容，并确保最新的 slash commands 已生效：

```bash
openspec update
```

## 使用说明

**模型选择**：OpenSpec 与高推理能力模型配合效果最好。我们推荐使用 Opus 4.5 和 GPT 5.2 来完成规划与实现。

**上下文卫生**：OpenSpec 能从干净的上下文窗口中获益。开始实现前先清空上下文，并在整个 session 中保持良好的上下文卫生。

## 贡献

**小修小补** —— Bug 修复、typo 更正和小幅改进可以直接提交 PR。

**较大改动** —— 新功能、重要重构或架构变更，请先提交一个 OpenSpec change proposal，以便我们在开始实现之前先对意图和目标达成一致。

在撰写 proposal 时，请牢记 OpenSpec 的理念：我们服务于大量不同的用户，覆盖不同的 coding agents、模型和使用场景。变更应该对所有人都足够友好。

**欢迎 AI 生成的代码** —— 前提是已经完成测试和验证。包含 AI 生成代码的 PR 应注明所使用的 coding agent 和模型（例如：`Generated with Claude Code using claude-opus-4-5-20251101`）。

### 开发

- 安装依赖：`pnpm install`
- 构建：`pnpm run build`
- 测试：`pnpm test`
- 本地开发 CLI：`pnpm run dev` 或 `pnpm run dev:cli`
- Conventional commits（单行）：`type(scope): subject`

## 其他

<details>
<summary><strong>遥测</strong></summary>

OpenSpec 会收集匿名使用统计。

我们只收集命令名称和版本，用于理解使用模式。不收集参数、路径、内容或 PII。在 CI 中会自动禁用。

**退出收集：** `export OPENSPEC_TELEMETRY=0` or `export DO_NOT_TRACK=1`

</details>

<details>
<summary><strong>维护者与顾问</strong></summary>

核心维护者和项目顾问列表见 [MAINTAINERS.md](MAINTAINERS.md)。

</details>



## License

MIT
