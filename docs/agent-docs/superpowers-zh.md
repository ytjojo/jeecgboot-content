# Superpowers

Superpowers 是一套面向 coding agents 的完整软件开发方法论，它建立在一组可组合技能之上，并配有一套初始说明，用来确保你的 agent 会正确使用这些技能。

## 工作原理

它从你启动 coding agent 的那一刻就开始生效。只要它发现你要开始构建某个东西，它*不会*立刻跳进去写代码。相反，它会先退一步，问你真正想完成的是什么。

一旦它从对话中提炼出了 spec，就会分成足够短的小块展示给你，让你真的能读完并消化。

当你对设计签字确认后，agent 会整理出一份实现计划。这份计划要清晰到什么程度？要清晰到即便交给一个热情但品味一般、判断力不足、不了解项目上下文、而且还不爱写测试的初级工程师，他也能照着做下去。它强调真正的 red/green TDD、YAGNI（You Aren't Gonna Need It）以及 DRY。

接下来，一旦你说“开始”，它就会启动 *subagent-driven-development* 流程，让多个 agents 分别处理各项工程任务、检查和审查它们的工作，然后继续向前推进。Claude 连续几个小时自主工作而不偏离你制定的计划，这并不罕见。

当然，这套系统还有很多别的内容，但以上就是它的核心。而且因为这些技能会自动触发，所以你不需要做任何特殊操作。你的 coding agent 就是拥有了 Superpowers。


## 赞助

如果 Superpowers 帮你做成了能赚钱的事情，并且你也愿意支持，我会非常感谢你考虑一下 [赞助我的开源工作](https://github.com/sponsors/obra)。

谢谢！

- Jesse


## 安装

**注意：** 不同平台的安装方式不同。

### Claude Code Official Marketplace

Superpowers 可通过 [official Claude plugin marketplace](https://claude.com/plugins/superpowers) 获取。

从 Anthropic 官方 marketplace 安装插件：

```bash
/plugin install superpowers@claude-plugins-official
```

### Claude Code (Superpowers Marketplace)

Superpowers marketplace 为 Claude Code 提供了 Superpowers 以及一些其他相关插件。

在 Claude Code 中，先注册 marketplace：

```bash
/plugin marketplace add obra/superpowers-marketplace
```

然后从这个 marketplace 安装插件：

```bash
/plugin install superpowers@superpowers-marketplace
```

### OpenAI Codex CLI

- 打开插件搜索界面

```bash
/plugins
```

搜索 Superpowers

```bash
superpowers
```

选择 `Install Plugin`

### OpenAI Codex App

- 在 Codex app 中，点击侧边栏里的 Plugins。
- 你应该会在 Coding 分区看到 `Superpowers`。
- 点击 Superpowers 旁边的 `+` 并按提示操作。


### Cursor (via Plugin Marketplace)

在 Cursor Agent chat 中，从 marketplace 安装：

```text
/add-plugin superpowers
```

或者在 plugin marketplace 中搜索 `superpowers`。

### OpenCode

告诉 OpenCode：

```
Fetch and follow instructions from https://raw.githubusercontent.com/obra/superpowers/refs/heads/main/.opencode/INSTALL.md
```

**详细文档：** [docs/README.opencode.md](docs/README.opencode.md)

### GitHub Copilot CLI

```bash
copilot plugin marketplace add obra/superpowers-marketplace
copilot plugin install superpowers@superpowers-marketplace
```

### Gemini CLI

```bash
gemini extensions install https://github.com/obra/superpowers
```

更新方式：

```bash
gemini extensions update superpowers
```

## 基础工作流

1. **brainstorming** - 在写代码前激活。通过提问打磨模糊想法，探索替代方案，并分章节展示设计供你确认。会保存设计文档。

2. **using-git-worktrees** - 在设计批准后激活。会在新分支上创建隔离工作区，执行项目初始化，并验证干净的测试基线。

3. **writing-plans** - 在设计获批后激活。把工作拆成细小任务（每项 2-5 分钟）。每个任务都包含精确文件路径、完整代码和验证步骤。

4. **subagent-driven-development** 或 **executing-plans** - 在计划完成后激活。为每个任务分派全新的 subagent，并进行两阶段审查（先看 spec compliance，再看代码质量）；或者按批次执行，并设置人工检查点。

5. **test-driven-development** - 在实现过程中激活。强制执行 RED-GREEN-REFACTOR：先写失败测试，看它失败；再写最少代码，看它通过；然后提交。会删除那些在测试之前写出的代码。

6. **requesting-code-review** - 在任务之间激活。根据计划进行审查，并按严重程度报告问题。严重问题会阻止继续推进。

7. **finishing-a-development-branch** - 在任务完成时激活。验证测试结果，给出选项（merge/PR/keep/discard），并清理 worktree。

**agent 在处理任何任务前都会检查是否有相关技能可用。** 这些是强制工作流，不是建议项。

## 包含内容

### Skills Library

**Testing**
- **test-driven-development** - RED-GREEN-REFACTOR 循环（包含 testing anti-patterns 参考）

**Debugging**
- **systematic-debugging** - 4 阶段根因定位流程（包含 root-cause-tracing、defense-in-depth、condition-based-waiting 技术）
- **verification-before-completion** - 确保问题真的已经修好

**Collaboration**
- **brainstorming** - 苏格拉底式设计打磨
- **writing-plans** - 详细实现计划
- **executing-plans** - 带检查点的批量执行
- **dispatching-parallel-agents** - 并发 subagent 工作流
- **requesting-code-review** - 预审查检查清单
- **receiving-code-review** - 响应反馈
- **using-git-worktrees** - 并行开发分支
- **finishing-a-development-branch** - merge/PR 决策工作流
- **subagent-driven-development** - 带两阶段审查的快速迭代（先看 spec compliance，再看代码质量）

**Meta**
- **writing-skills** - 按最佳实践创建新技能（包含测试方法论）
- **using-superpowers** - 技能系统介绍

## 哲学

- **Test-Driven Development** - 始终先写测试
- **Systematic over ad-hoc** - 流程优先于猜测
- **Complexity reduction** - 以简化为首要目标
- **Evidence over claims** - 在宣布成功前先验证

阅读 [原始发布公告](https://blog.fsck.com/2025/10/09/superpowers/)。

## 贡献

以下是 Superpowers 的通用贡献流程。需要注意的是，我们通常不接受新增 skills 的贡献，并且对 skills 的任何更新都必须能在我们支持的所有 coding agents 上工作。

1. Fork 这个仓库
2. 切换到 `dev` 分支
3. 为你的工作创建一个分支
4. 按照 `writing-skills` skill 的要求去创建和测试新增或修改后的 skills
5. 提交 PR，并确保完整填写 pull request 模板

完整指南见 `skills/writing-skills/SKILL.md`。

## 更新

Superpowers 的更新方式会因 coding agent 不同而略有差异，但通常是自动完成的。

## 许可证

MIT License - 详情见 LICENSE 文件

## 社区

Superpowers 由 [Jesse Vincent](https://blog.fsck.com) 和 [Prime Radiant](https://primeradiant.com) 的其他成员共同构建。

- **Discord**: [Join us](https://discord.gg/35wsABTejz) 获取社区支持、提问，以及分享你正在用 Superpowers 构建的内容
- **Issues**: https://github.com/obra/superpowers/issues
- **Release announcements**: [Sign up](https://primeradiant.com/superpowers/) 以接收新版本通知
