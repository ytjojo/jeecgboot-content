# Compound Engineering

[![Build Status](https://github.com/EveryInc/compound-engineering-plugin/actions/workflows/ci.yml/badge.svg)](https://github.com/EveryInc/compound-engineering-plugin/actions/workflows/ci.yml)
[![npm](https://img.shields.io/npm/v/@every-env/compound-plugin)](https://www.npmjs.com/package/@every-env/compound-plugin)

让每一单位工程工作都比上一单位更容易的一套 AI skills 和 agents。

## 理念

**每一单位工程工作，都应该让后续工作更容易，而不是更困难。**

传统开发会不断累积技术债。每个功能都会增加复杂度。每次修 bug，都会留下更多局部知识，等着后来的人再重新摸索一次。代码库越来越大，上下文越来越难以掌握，下一次改动也就越来越慢。

Compound engineering 反过来做这件事。80% 在规划和评审，20% 在执行：

- 在写代码前，用 `/ce-brainstorm` 和 `/ce-plan` 做充分规划
- 用 `/ce-code-review` 和 `/ce-doc-review` 做评审，以发现问题并校准判断
- 用 `/ce-compound` 把知识固化下来，使其可复用
- 保持高质量，让未来的改动更容易

重点不是流程仪式感，重点是杠杆效应。一次高质量的 brainstorm 会让 plan 更锐利；一份高质量的 plan 会让执行范围更小；一次高质量的 review 抓住的是模式，而不仅仅是某个 bug；一条高质量的 compound note 会让下一个 agent 不必从头再学一遍同样的教训。

**了解更多**

- [Full component reference](plugins/compound-engineering/README.md) - 所有 agents 和 skills
- [Compound engineering: how Every codes with agents](https://every.to/chain-of-thought/compound-engineering-how-every-codes-with-agents)
- [The story behind compounding engineering](https://every.to/source-code/my-ai-had-already-fixed-the-code-before-i-saw-it)

## 工作流

`/ce-strategy` 位于这个循环的上游，它会把产品的目标问题、方法、persona、指标和 tracks 记录为 `STRATEGY.md` 中一个简短且可持续使用的锚点。当它存在时，ideate、brainstorm 和 plan 都会把它作为基础背景来读取，因此战略层的选择会自然流入功能构思、优先级排序和 spec。

核心循环是：先对需求做 brainstorm，再做实现 plan，按计划推进工作，review 结果，compound 学到的内容，然后带着更好的上下文再次重复。

如果你希望 agent 在进入这个循环前，先生成并批判性评估更大的方向性想法，再从中挑出一个进入 brainstorming，请先使用 `/ce-ideate`。它产出的是排序后的 ideation artifact，而不是 requirements、plans 或代码。

| Skill | Purpose |
|-------|---------|
| `/ce-strategy` | 创建或维护 `STRATEGY.md`，内容包括产品的目标问题、方法、persona、关键指标和 tracks。会被 ideate、brainstorm 和 plan 作为基础背景读取 |
| `/ce-ideate` | 可选的大图景构思：生成并批判性评估有依据的想法，然后把最强的那个引导进 brainstorming |
| `/ce-brainstorm` | 通过交互式问答，梳理某个功能或问题，并在规划前写出尺寸合适的 requirements doc |
| `/ce-plan` | 将功能想法转化为详细的实现 plan |
| `/ce-work` | 使用 worktrees 和任务跟踪执行 plans |
| `/ce-debug` | 系统化复现故障、追踪根因并实现修复 |
| `/ce-code-review` | 合并前的多 agent 代码评审 |
| `/ce-compound` | 记录经验，让未来的工作更轻松 |
| `/ce-product-pulse` | 生成一页式、按时间窗口统计的 pulse report，覆盖使用情况、性能、错误和后续动作。保存到 `docs/pulse-reports/` |

`/ce-product-pulse` 是这个体系在 read-side 的配套能力，它会生成一个按时间窗口汇总的报告，描述用户在某段时间内（24h、7d 等）实际经历了什么，以及产品在这段时间里的表现如何。报告保存到 `docs/pulse-reports/`，因此以往的 pulse 会形成一条可浏览的用户结果时间线。下一次 strategy 更新和下一次 brainstorm 就能以真实信号为锚点。

每一轮循环都会产生复利：brainstorm 让后续 plan 更准确，plan 让未来 plan 更成熟，review 捕获更多问题，模式被文档化沉淀下来。

## 快速示例

一个典型循环会先把粗略想法转成 requirements doc，再基于该文档做 plan，然后把执行交给 `/ce-work`：

```text
/ce-brainstorm "make background job retries safer"
/ce-plan docs/brainstorms/background-job-retry-safety-requirements.md
/ce-work
/ce-code-review
/ce-compound
```

如果是聚焦的 bug 排查：

```text
/ce-debug "the checkout webhook sometimes creates duplicate invoices"
/ce-code-review
/ce-compound
```

## 开始使用

安装完成后，在任意项目中运行 `/ce-setup`。它会检查你的环境、安装缺失工具并初始化项目配置。

`compound-engineering` plugin 当前包含 37 个 skills 和 51 个 agents。完整清单请查看 [full component reference](plugins/compound-engineering/README.md)。

---

## 安装

### Claude Code

```text
/plugin marketplace add EveryInc/compound-engineering-plugin
/plugin install compound-engineering
```

### Cursor

在 Cursor Agent chat 中，从 plugin marketplace 安装：

```text
/add-plugin compound-engineering
```

或者在 plugin marketplace 中搜索 `compound engineering`。

### Codex

共三步：注册 marketplace、安装 agent 集合，然后通过 Codex 的 TUI 安装插件。

1. **向 Codex 注册 marketplace：**

   ```bash
   codex plugin marketplace add EveryInc/compound-engineering-plugin
   ```

2. **安装 Compound Engineering agents**（Codex 当前的 plugin spec 还不能注册自定义 agents）：

   ```bash
   bunx @every-env/compound-plugin install compound-engineering --to codex
   ```

3. **通过 Codex 的 TUI 安装插件：** 启动 `codex`，运行 `/plugins`，找到 **Compound Engineering** marketplace，选择 **compound-engineering** 插件，然后点击 **Install**。安装完成后重启 Codex。Codex 的 CLI 目前还没有从已添加 marketplace 安装插件的子命令，因此 `/plugins` TUI 是标准流程。

这三步都需要。marketplace 注册加上 TUI 安装负责 skills；Bun 这一步会安装 `$ce-code-review`、`$ce-plan` 和 `$ce-work` 这类 skills 在 Codex 中会拉起的 review、research 和 workflow agents。缺少 agent 安装步骤时，这些委派类 skills 会报告缺失 agents。

> **注意：** 一旦 Codex 的原生 plugin spec 支持自定义 agents，Bun 这一步就不再需要。届时只用 TUI 安装就够了。

如果你之前用过仅依赖 Bun 的 Codex 安装方式，在切换前请先备份旧的 CE artifacts：

```bash
bunx @every-env/compound-plugin cleanup --target codex
```

### GitHub Copilot

对于 **VS Code Copilot Agent Plugins**：

1. 在 VS Code 命令面板中运行 `Chat: Install Plugin from Source`
2. 仓库填写 `EveryInc/compound-engineering-plugin`
3. 当 VS Code 展示该仓库中的 plugins 时，选择 `compound-engineering`

对于 **Copilot CLI**，使用：

在 Copilot CLI 内部：

```text
/plugin marketplace add EveryInc/compound-engineering-plugin
/plugin install compound-engineering@compound-engineering-plugin
```

在带有 `copilot` 二进制的 shell 中：

```bash
copilot plugin marketplace add EveryInc/compound-engineering-plugin
copilot plugin install compound-engineering@compound-engineering-plugin
```

Copilot CLI 会读取现有兼容 Claude 的 plugin manifests，因此不需要额外的 Bun 安装步骤。

如果你之前使用的是旧版 Bun Copilot 安装方式，在切换到原生 plugin 前请先备份旧的 CE artifacts：

```bash
bunx @every-env/compound-plugin cleanup --target copilot
```

### Factory Droid

在带有 `droid` 二进制的 shell 中执行：

```bash
droid plugin marketplace add https://github.com/EveryInc/compound-engineering-plugin
droid plugin install compound-engineering@compound-engineering-plugin
```

Droid 使用 `plugin@marketplace` 形式的 plugin ID；这里 `compound-engineering` 是插件名，而 `compound-engineering-plugin` 是 marketplace 名。Droid 会安装现有兼容 Claude Code 的插件，并自动转换格式，因此不需要 Bun 安装步骤。

如果你之前使用的是旧版 Bun Droid 安装方式，在切换到原生 plugin 前请先备份旧的 CE artifacts：

```bash
bunx @every-env/compound-plugin cleanup --target droid
```

### Qwen Code

```bash
qwen extensions install EveryInc/compound-engineering-plugin:compound-engineering
```

Qwen Code 可直接从 GitHub 安装兼容 Claude Code 的插件，并在安装过程中自动转换 plugin 格式，因此不需要 Bun 安装步骤。

如果你之前使用的是旧版 Bun Qwen 安装方式，在切换到原生 extension 前请先备份旧的 CE artifacts：

```bash
bunx @every-env/compound-plugin cleanup --target qwen
```

### OpenCode, Pi, Gemini, and Kiro

这个仓库包含一个 Bun/TypeScript 安装器，可将 Compound Engineering plugin 转换给 OpenCode、Pi、Gemini CLI 和 Kiro CLI 使用。

```bash
bunx @every-env/compound-plugin install compound-engineering --to opencode
bunx @every-env/compound-plugin install compound-engineering --to pi
bunx @every-env/compound-plugin install compound-engineering --to gemini
bunx @every-env/compound-plugin install compound-engineering --to kiro
```

**Pi prerequisites。** Pi 不自带原生 subagent 原语，因此 Pi 安装依赖 [nicobailon/pi-subagents](https://github.com/nicobailon/pi-subagents)（必需），并推荐安装 [edlsh/pi-ask-user](https://github.com/edlsh/pi-ask-user) 以获得更丰富的阻塞式用户提问能力：

```bash
pi install npm:pi-subagents    # required — provides the `subagent` tool used by skills that dispatch parallel agents
pi install npm:pi-ask-user     # recommended — provides the `ask_user` tool; skills fall back to numbered options in chat when it is missing
```

如需自动检测自定义安装目标并一次性安装到全部目标：

```bash
bunx @every-env/compound-plugin install compound-engineering --to all
```

这些自定义安装目标会在安装期间执行 CE legacy cleanup。若需手动针对某个特定目标执行 cleanup：

```bash
bunx @every-env/compound-plugin cleanup --target codex
bunx @every-env/compound-plugin cleanup --target opencode
bunx @every-env/compound-plugin cleanup --target pi
bunx @every-env/compound-plugin cleanup --target gemini
bunx @every-env/compound-plugin cleanup --target kiro
bunx @every-env/compound-plugin cleanup --target copilot   # old Bun installs only
bunx @every-env/compound-plugin cleanup --target droid     # old Bun installs only
bunx @every-env/compound-plugin cleanup --target qwen      # old Bun installs only
bunx @every-env/compound-plugin cleanup --target windsurf  # deprecated legacy installs only
```

cleanup 会把已知的 CE artifacts 移动到目标根目录下的 `compound-engineering/legacy-backup/` 目录中。

---

## 本地开发

```bash
bun install
bun test
bun run release:validate
```

### 从你的本地 checkout 运行

适合活跃开发场景，修改 plugin 源码后可立即生效。

**Claude Code** —— 添加一个 shell alias，让本地副本和你的常规插件一起加载：

```bash
alias cce='claude --plugin-dir ~/Code/compound-engineering-plugin/plugins/compound-engineering'
```

使用 `cce` 替代 `claude` 来测试你的改动。你的生产安装不会受到影响。

**Codex and other targets** —— 直接针对你的 checkout 运行本地 CLI：

```bash
# from the repo root
bun run src/index.ts install ./plugins/compound-engineering --to codex

# same pattern for other targets
bun run src/index.ts install ./plugins/compound-engineering --to opencode
```

### 从已推送分支运行

适合测试别人的分支，或者测试你自己某个 worktree 上的分支，而无需切换当前 checkout。这里通过 `--branch` 把该分支克隆到一个确定性的缓存目录。

> **Unpushed local branches：** 如果某个分支只存在于本地 worktree，尚未推送，请改为直接把 `--plugin-dir` 指向该 worktree 路径（例如 `claude --plugin-dir /path/to/worktree/plugins/compound-engineering`）。

**Claude Code** —— 使用 `plugin-path` 获取缓存 clone 路径：

```bash
# from the repo root
bun run src/index.ts plugin-path compound-engineering --branch feat/new-agents
# Output:
#   claude --plugin-dir ~/.cache/compound-engineering/branches/compound-engineering-feat~new-agents/plugins/compound-engineering
```

这个缓存路径是确定性的。重复运行会把 checkout 更新到该分支的最新提交。

**Codex, OpenCode, and other targets** —— 在 `install` 时传入 `--branch`：

```bash
# from the repo root
bun run src/index.ts install compound-engineering --to codex --branch feat/new-agents

# works with any target
bun run src/index.ts install compound-engineering --to opencode --branch feat/new-agents

# combine with --also for multiple targets
bun run src/index.ts install compound-engineering --to codex --also opencode --branch feat/new-agents
```

这两项能力都使用 `COMPOUND_PLUGIN_GITHUB_SOURCE` env var 来解析仓库地址，默认值为 `https://github.com/EveryInc/compound-engineering-plugin`。

### Shell aliases

把下面内容加入 `~/.zshrc` 或 `~/.bashrc`。所有 alias 都使用本地 CLI，因此不依赖 npm 发布。`plugin-path` 只会把路径打印到 stdout，因此可以和 `$()` 组合使用。

```bash
CE_REPO=~/Code/compound-engineering-plugin

ce-cli() { bun run "$CE_REPO/src/index.ts" "$@"; }

# --- Local checkout (active development) ---
alias cce='claude --plugin-dir $CE_REPO/plugins/compound-engineering'

codex-ce() {
  ce-cli install "$CE_REPO/plugins/compound-engineering" --to codex "$@"
}

# --- Pushed branch (testing PRs, worktree workflows) ---
ccb() {
  claude --plugin-dir "$(ce-cli plugin-path compound-engineering --branch "$1")" "${@:2}"
}

codex-ceb() {
  ce-cli install compound-engineering --to codex --branch "$1" "${@:2}"
}
```

用法：

```bash
cce                              # local checkout with Claude Code
codex-ce                         # install local checkout to Codex
ccb feat/new-agents              # test a pushed branch with Claude Code
ccb feat/new-agents --verbose    # extra flags forwarded to claude
codex-ceb feat/new-agents        # install a pushed branch to Codex
```

Codex 安装会把生成出来的 plugin skills 隔离在 `~/.codex/skills/compound-engineering/` 下，不会向 `~/.agents` 写入新文件。当安装器能够证明旧的 CE 管理型 `.agents/skills` symlinks 指回的是 CE 的 Codex 管理存储时，它会移除这些 symlinks，从而避免陈旧的 Codex 安装遮蔽 Copilot 的原生 plugin 安装。

## 故障排查

### Codex skills 能用，但 review 或 research delegation 失败

执行 agent 安装步骤：

```bash
bunx @every-env/compound-plugin install compound-engineering --to codex
```

Codex 原生 plugin 安装只处理 skills。Bun 这一步负责安装这些 skills 所委派使用的自定义 agents。

### Codex 显示陈旧或重复的 CE skills

切换到原生 Codex plugin 流程前，请先备份旧的 Bun 安装 artifacts：

```bash
bunx @every-env/compound-plugin cleanup --target codex
```

### Copilot、Droid 或 Qwen 加载了陈旧的 CE skills

在改用原生 plugin 路径之前，请先备份旧的 Bun 安装 artifacts：

```bash
bunx @every-env/compound-plugin cleanup --target copilot
bunx @every-env/compound-plugin cleanup --target droid
bunx @every-env/compound-plugin cleanup --target qwen
```

## 限制

Codex 当前的原生 plugin 安装只处理 skills，不处理自定义 agents。在 Codex 的原生 plugin spec 支持 agents 之前，文档中的 Bun 补充步骤仍然是必须的。

OpenCode、Pi、Gemini 和 Kiro 的安装依赖转换器支持，随着这些目标格式演进，安装方式也可能发生变化。

版本发布由 release automation 管理。常规功能 PR 不应手动提升 plugin 或 marketplace manifest 的版本号。

## FAQ

### Claude Code 需要 Bun 吗？

不需要。Claude Code 直接从 plugin marketplace 安装。只有转换器支持的目标、Codex 当前的 agent 补充安装、本地开发，以及清理旧版转换安装时，才需要 Bun。

### 为什么 Codex 需要额外的 Bun 步骤？

Codex 的原生 plugin 流程会从 Codex plugin manifest 安装 skills，但目前不会安装 Compound Engineering skills 所委派使用的自定义 reviewer、researcher 和 workflow agents。Bun 这一步正是用来补上这个缺口的。

### 我在哪里可以看到所有可用的 skills 和 agents？

阅读 [Compound Engineering plugin README](plugins/compound-engineering/README.md)。里面列出了当前的 skill 和 agent 清单。

### 发布历史在哪里看？

GitHub Releases 是正式的 release notes 展示面。根目录下的 [`CHANGELOG.md`](CHANGELOG.md) 指向这段历史。

## 关于贡献

*About Contributions:* 请不要误会，但我不接受任何项目的外部贡献。我实在没有足够的精力去 review 这些内容，而且项目挂的是我的名字，所以出了任何问题都要由我负责；从我的角度看，这里的风险和收益极不对称。我还得考虑其他“stakeholders”，而对于这些我大多是免费为自己做的工具来说，这似乎并不明智。你当然可以提交 issue，甚至也可以提交 PR 来说明你建议的修复方式，但请理解，我不会直接 merge。相反，我会让 Claude 或 Codex 通过 `gh` 去 review 提交内容，再独立决定是否以及如何处理。尤其欢迎 bug reports。若这让你不快，我先说声抱歉，但我想避免浪费时间和伤感情。我知道这和鼓励社区贡献的主流开源 ethos 并不一致，但这是我能在保持当前速度的同时维持理智的唯一方式。

## 许可证

[MIT](LICENSE)
