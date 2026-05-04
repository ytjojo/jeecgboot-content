 "I don't think I've typed like a line of code probably since December, basically, which is an extremely large change." — [Andrej Karpathy](https://fortune.com/2026/03/21/andrej-karpathy-openai-cofounder-ai-agents-coding-state-of-psychosis-openclaw/), No Priors podcast, March 2026

当我听到 Karpathy 这么说时，我想知道这是怎么做到的。一个人怎么能像二十人的团队一样持续交付？Peter Steinberger 基本上靠自己加上 AI agents 打造了 [OpenClaw](https://github.com/openclaw/openclaw) ，拿到了 247K GitHub stars。革命已经来了。一个拥有合适工具的个人开发者，移动速度可以超过传统团队。

我是 [Garry Tan](https://x.com/garrytan)，[Y Combinator](https://www.ycombinator.com/) 的 President & CEO。我和成千上万家创业公司合作过，包括 Coinbase、Instacart、Rippling，当时它们都还只是车库里的一两个人。在 YC 之前，我曾是 Palantir 最早的一批 eng/PM/designer 之一，联合创办了 Posterous（后卖给 Twitter），还构建了 YC 的内部社交网络 Bookface。

**gstack 就是我的答案。** 我做产品已经二十年了，而现在我交付的产品比以往任何时候都多。过去 60 天里：3 个生产服务、40+ 已上线功能，还是在全职运营 YC 的同时兼职完成。按逻辑代码变更量衡量，而不是会被 AI 注水的原始 LOC，我在 2026 年的节奏是 **2013 年的约 ~810 倍**（11,417 对 14 logical lines/day）。截至今年（到 4 月 18 日），2026 年已经产出了 **2013 全年的 240 倍**。统计口径覆盖 40 个公开和私有的 `garrytan/*` 仓库，包括 Bookface，并排除了一个 demo 仓库。大部分代码由 AI 编写。重点不在于是谁敲下了这些字，而在于最终交付了什么。

> 那些批评 LOC 的人并没错，原始行数在 AI 时代确实会膨胀。他们错在于认为剔除膨胀因素后，我的生产力反而下降了。实际上我高得多。完整的方法论、注意事项和复现脚本见：**[On the LOC Controversy](docs/ON_THE_LOC_CONTROVERSY.md)**。

**2026 年 —— 1,237 次贡献，仍在继续：**

![GitHub contributions 2026 — 1,237 contributions, massive acceleration in Jan-Mar](docs/images/github-2026.png)

**2013 年 —— 我在 YC 构建 Bookface 的时候（772 次贡献）：**

![GitHub contributions 2013 — 772 contributions building Bookface at YC](docs/images/github-2013.png)

同一个人。不同的时代。差别在于工具。

**gstack 就是我实现这一切的方式。** 它把 Claude Code 变成一个虚拟工程团队：重新思考产品的 CEO、锁定架构的工程经理、识别 AI 低质输出的设计师、发现生产级 bug 的 reviewer、能打开真实浏览器的 QA 负责人、执行 OWASP + STRIDE 审计的安全官，以及负责发 PR 的发布工程师。23 个专家角色、8 个强力工具，全部是 slash commands，全部是 Markdown，全部免费，MIT license。

这是我的开源软件工厂。我每天都在用它。我把它分享出来，因为这些工具应该人人都能用到。

Fork 它。改进它。把它变成你自己的工具。如果你就是想吐槽免费开源软件，也欢迎，但我更希望你先试试。

**适合谁：**
- **Founders and CEOs** —— 特别是那些还想亲自交付的技术型创始人
- **First-time Claude Code users** —— 不再面对空白 prompt，而是有结构化角色分工
- **Tech leads and staff engineers** —— 每个 PR 都具备严格的 review、QA 和发布自动化

## Quick start

1. 安装 gstack（30 秒，见下文）
2. 运行 `/office-hours` —— 描述你正在构建什么
3. 对任意功能想法运行 `/plan-ceo-review`
4. 对任意有变更的分支运行 `/review`
5. 对你的 staging URL 运行 `/qa`
6. 到这里就够了。你会知道这是不是适合你。

## Install — 30 seconds

**Requirements：** [Claude Code](https://docs.anthropic.com/en/docs/claude-code), [Git](https://git-scm.com/), [Bun](https://bun.sh/) v1.0+, [Node.js](https://nodejs.org/)（仅 Windows）

### Step 1: Install on your machine

打开 Claude Code，把下面这段贴进去。剩下的 Claude 会处理。

> Install gstack: run **`git clone --single-branch --depth 1 https://github.com/garrytan/gstack.git ~/.claude/skills/gstack && cd ~/.claude/skills/gstack && ./setup`** then add a "gstack" section to CLAUDE.md that says to use the /browse skill from gstack for all web browsing, never use mcp__claude-in-chrome__* tools, and lists the available skills: /office-hours, /plan-ceo-review, /plan-eng-review, /plan-design-review, /design-consultation, /design-shotgun, /design-html, /review, /ship, /land-and-deploy, /canary, /benchmark, /browse, /connect-chrome, /qa, /qa-only, /design-review, /setup-browser-cookies, /setup-deploy, /setup-gbrain, /retro, /investigate, /document-release, /codex, /cso, /autoplan, /plan-devex-review, /devex-review, /careful, /freeze, /guard, /unfreeze, /gstack-upgrade, /learn. Then ask the user if they also want to add gstack to the current project so teammates get it.

### Step 2: Team mode — auto-update for shared repos (recommended)

在你的仓库内部，把下面这段贴进去。它会切换到 team mode，引导仓库完成初始化，让队友自动获得 gstack，并提交这次改动：

```bash
(cd ~/.claude/skills/gstack && ./setup --team) && ~/.claude/skills/gstack/bin/gstack-team-init required && git add .claude/ CLAUDE.md && git commit -m "require gstack for AI-assisted work"
```

你的仓库里不会 vendored 任何文件，没有版本漂移，也不需要手动升级。每次 Claude Code session 启动时，都会先做一次快速自动更新检查（最多每小时一次、网络失败安全、完全静默）。

如果你更想提醒队友而不是阻止他们，可以把 `required` 换成 `optional`。

### OpenClaw

[OpenClaw](https://github.com/openclaw/openclaw) 通过 ACP 拉起 Claude Code session，所以只要 Claude Code 安装了 gstack，每个 gstack skill 都能直接工作。把下面这段贴给你的 OpenClaw agent：

> Install gstack: run `git clone --single-branch --depth 1 https://github.com/garrytan/gstack.git ~/.claude/skills/gstack && cd ~/.claude/skills/gstack && ./setup` to install gstack for Claude Code. Then add a "Coding Tasks" section to AGENTS.md that says: when spawning Claude Code sessions for coding work, tell the session to use gstack skills. Include these examples — security audit: "Load gstack. Run /cso", code review: "Load gstack. Run /review", QA test a URL: "Load gstack. Run /qa https://...", build a feature end-to-end: "Load gstack. Run /autoplan, implement the plan, then run /ship", plan before building: "Load gstack. Run /office-hours then /autoplan. Save the plan, don't implement."

**配置完成后，直接像平常一样和你的 OpenClaw agent 对话：**

| You say | What happens |
|---------|-------------|
| "Fix the typo in README" | 简单场景 —— Claude Code session 即可，不需要 gstack |
| "Run a security audit on this repo" | 拉起 Claude Code，并执行 `Run /cso` |
| "Build me a notifications feature" | 拉起 Claude Code，流程是 /autoplan → implement → /ship |
| "Help me plan the v2 API redesign" | 拉起 Claude Code，流程是 /office-hours → /autoplan，并保存 plan |

关于更高级的分发路由和 gstack-lite/gstack-full prompt 模板，见 [docs/OPENCLAW.md](docs/OPENCLAW.md)。

### Native OpenClaw Skills (via ClawHub)

四个可直接运行在 OpenClaw agent 中的方法论技能，不需要 Claude Code
session。通过 ClawHub 安装：

```
clawhub install gstack-openclaw-office-hours gstack-openclaw-ceo-review gstack-openclaw-investigate gstack-openclaw-retro
```

| Skill | What it does |
|-------|-------------|
| `gstack-openclaw-office-hours` | 用 6 个强制问题做产品拷问 |
| `gstack-openclaw-ceo-review` | 用 4 种范围模式发起战略挑战 |
| `gstack-openclaw-investigate` | 根因调试方法论 |
| `gstack-openclaw-retro` | 每周工程复盘 |

这些都是对话式 skills。你的 OpenClaw agent 会直接通过聊天运行它们。

### Other AI Agents

gstack 不只支持 Claude，而是能跑在 10 个 AI coding agents 上。安装程序会自动检测你已经安装了哪些 agent：

```bash
git clone --single-branch --depth 1 https://github.com/garrytan/gstack.git ~/gstack
cd ~/gstack && ./setup
```

或者使用 `./setup --host <name>` 指定某个特定 agent：

| Agent | Flag | Skills install to |
|-------|------|-------------------|
| OpenAI Codex CLI | `--host codex` | `~/.codex/skills/gstack-*/` |
| OpenCode | `--host opencode` | `~/.config/opencode/skills/gstack-*/` |
| Cursor | `--host cursor` | `~/.cursor/skills/gstack-*/` |
| Factory Droid | `--host factory` | `~/.factory/skills/gstack-*/` |
| Slate | `--host slate` | `~/.slate/skills/gstack-*/` |
| Kiro | `--host kiro` | `~/.kiro/skills/gstack-*/` |
| Hermes | `--host hermes` | `~/.hermes/skills/gstack-*/` |
| GBrain (mod) | `--host gbrain` | `~/.gbrain/skills/gstack-*/` |

**想为另一个 agent 添加支持？** 参见 [docs/ADDING_A_HOST.md](docs/ADDING_A_HOST.md)。
只需要一个 TypeScript 配置文件，零代码改动。

## See it work

```
You:    I want to build a daily briefing app for my calendar.
You:    /office-hours
Claude: [asks about the pain — specific examples, not hypotheticals]

You:    Multiple Google calendars, events with stale info, wrong locations.
        Prep takes forever and the results aren't good enough...

Claude: I'm going to push back on the framing. You said "daily briefing
        app." But what you actually described is a personal chief of
        staff AI.
        [extracts 5 capabilities you didn't realize you were describing]
        [challenges 4 premises — you agree, disagree, or adjust]
        [generates 3 implementation approaches with effort estimates]
        RECOMMENDATION: Ship the narrowest wedge tomorrow, learn from
        real usage. The full vision is a 3-month project — start with
        the daily briefing that actually works.
        [writes design doc → feeds into downstream skills automatically]

You:    /plan-ceo-review
        [reads the design doc, challenges scope, runs 10-section review]

You:    /plan-eng-review
        [ASCII diagrams for data flow, state machines, error paths]
        [test matrix, failure modes, security concerns]

You:    Approve plan. Exit plan mode.
        [writes 2,400 lines across 11 files. ~8 minutes.]

You:    /review
        [AUTO-FIXED] 2 issues. [ASK] Race condition → you approve fix.

You:    /qa https://staging.myapp.com
        [opens real browser, clicks through flows, finds and fixes a bug]

You:    /ship
        Tests: 42 → 51 (+9 new). PR: github.com/you/app/pull/42
```

你说的是“daily briefing app”。agent 回应的是“你其实在构建一个 chief of staff AI”，因为它听的是你的痛点，而不是你的功能请求。八条命令，从头到尾。这不是 copilot。这是一个团队。

## The sprint

gstack 是一套流程，而不是一组工具。skills 的运行顺序与 sprint 的推进顺序一致：

**Think → Plan → Build → Review → Test → Ship → Reflect**

每个 skill 都会把结果传递给下一个。`/office-hours` 会写出一个 design doc，供 `/plan-ceo-review` 读取；`/plan-eng-review` 会写出一个 test plan，让 `/qa` 接着执行；`/review` 会发现 bug，而 `/ship` 会验证它们是否已修复。因为每一步都知道前一步发生了什么，所以没有任何事情会漏掉。

| Skill | Your specialist | What they do |
|-------|----------------|--------------|
| `/office-hours` | **YC Office Hours** | 从这里开始。在你写代码之前，用 6 个强制问题重构你的产品认知。它会反驳你的 framing、质疑前提，并生成实现备选方案。产出的 design doc 会流入所有下游 skills。 |
| `/plan-ceo-review` | **CEO / Founder** | 重新定义问题。找到隐藏在需求里的 10-star 产品。四种模式：Expansion、Selective Expansion、Hold Scope、Reduction。 |
| `/plan-eng-review` | **Eng Manager** | 锁定架构、数据流、图示、边界情况和测试。逼迫隐藏假设浮出水面。 |
| `/plan-design-review` | **Senior Designer** | 给每个设计维度打 0-10 分，解释什么才算 10 分，然后直接修改 plan 以达到那个标准。AI Slop 检测。交互式流程，每个设计决策只问一个 AskUserQuestion。 |
| `/plan-devex-review` | **Developer Experience Lead** | 交互式 DX 评审：探索开发者画像，对标竞品的 TTHW，设计你的 magical moment，逐步追踪摩擦点。三种模式：DX EXPANSION、DX POLISH、DX TRIAGE。20-45 个强制问题。 |
| `/design-consultation` | **Design Partner** | 从零构建完整设计系统。研究设计版图，提出有创造性的风险，并生成逼真的产品 mockups。 |
| `/review` | **Staff Engineer** | 找出那些能过 CI 却会在生产环境爆炸的 bug。显而易见的问题会自动修掉，也会标记完整性缺口。 |
| `/investigate` | **Debugger** | 系统化根因调试。铁律：不调查，不修复。它会追踪数据流、验证假设，连续 3 次修复失败后就停止。 |
| `/design-review` | **Designer Who Codes** | 与 `/plan-design-review` 相同的审计方式，但会直接把发现的问题修掉。使用原子提交，并给出 before/after 截图。 |
| `/devex-review` | **DX Tester** | 真实开发者体验审计。它真的会测试你的 onboarding：翻文档、走 getting started 流程、计时 TTHW、截图错误。还会对比 `/plan-devex-review` 的评分，形成回旋镖式验证，看看你的计划是否符合现实。 |
| `/design-shotgun` | **Design Explorer** | “给我看更多选项。” 生成 4-6 个 AI mockup 变体，在浏览器里打开对比面板，收集你的反馈并继续迭代。taste memory 会学习你的偏好。直到你真正喜欢，再交给 `/design-html`。 |
| `/design-html` | **Design Engineer** | 把 mockup 变成真正可工作的生产级 HTML。使用 Pretext 计算布局：文字会自动重排，高度会随内容变化，布局是动态的。30KB，零依赖。自动识别 React/Svelte/Vue，并按设计类型（landing page、dashboard、form）智能选择 API 路由。输出的是可上线的成品，不是 demo。 |
| `/qa` | **QA Lead** | 测试你的应用、找出 bug、用原子提交修复，再重新验证。每个修复都会自动生成回归测试。 |
| `/qa-only` | **QA Reporter** | 与 `/qa` 采用相同方法论，但只报告，不改代码。纯粹的 bug report。 |
| `/pair-agent` | **Multi-Agent Coordinator** | 把你的浏览器共享给任意 AI agent。一个命令，一次粘贴，就能连上。支持 OpenClaw、Hermes、Codex、Cursor，或者任何能 `curl` 的东西。每个 agent 都有自己的 tab。自动以 headed 模式启动，方便你全程观察。还会为远程 agents 自动启动 ngrok tunnel。作用域 token、tab 隔离、限流、活动归因，一应俱全。 |
| `/cso` | **Chief Security Officer** | OWASP Top 10 + STRIDE 威胁建模。零噪音：17 个误报排除、8/10+ 置信门槛、每个发现都做独立验证。每条发现都附带具体的利用场景。 |
| `/ship` | **Release Engineer** | 同步 `main`、跑测试、审计覆盖率、推送、打开 PR。即使你还没有测试框架，它也会帮你初始化。 |
| `/land-and-deploy` | **Release Engineer** | 合并 PR，等待 CI 和部署完成，并验证生产环境健康。一个命令，从“已批准”走到“生产验证完成”。 |
| `/canary` | **SRE** | 部署后的监控循环。观察 console errors、性能回退和页面故障。 |
| `/benchmark` | **Performance Engineer** | 建立页面加载时间、Core Web Vitals 和资源体积的基线。每个 PR 都能比较 before/after。 |
| `/document-release` | **Technical Writer** | 更新项目中所有文档，使其与你刚刚发布的内容一致。会自动发现 README 过期问题。 |
| `/retro` | **Eng Manager** | 团队感知式周复盘。支持按人拆分、交付 streak、测试健康趋势和成长机会。`/retro global` 会跨你所有项目和 AI 工具（Claude Code、Codex、Gemini）运行。 |
| `/browse` | **QA Engineer** | 给 agent 装上眼睛。真实的 Chromium 浏览器、真实点击、真实截图。每条命令约 100ms。`/open-gstack-browser` 会启动带侧栏、反机器人隐身和自动模型路由的 GStack Browser。 |
| `/setup-browser-cookies` | **Session Manager** | 把你真实浏览器（Chrome、Arc、Brave、Edge）里的 cookies 导入到 headless session。可测试已登录页面。 |
| `/autoplan` | **Review Pipeline** | 一个命令，得到完整审阅过的 plan。自动按 CEO → design → eng review 流程运行，并编码决策原则。只把需要你拍板的 taste 决策浮出来。 |
| `/learn` | **Memory** | 管理 gstack 在多次会话中学到的内容。可审阅、搜索、裁剪和导出项目级模式、坑点与偏好。学习结果会跨会话累积，让 gstack 对你的代码库越来越聪明。 |

### Which review should I use?

| Building for... | Plan stage (before code) | Live audit (after shipping) |
|-----------------|--------------------------|----------------------------|
| **End users** (UI, web app, mobile) | `/plan-design-review` | `/design-review` |
| **Developers** (API, CLI, SDK, docs) | `/plan-devex-review` | `/devex-review` |
| **Architecture** (data flow, perf, tests) | `/plan-eng-review` | `/review` |
| **All of the above** | `/autoplan`（自动执行 CEO → design → eng → DX，并识别哪些步骤适用） | — |

### Power tools

| Skill | What it does |
|-------|-------------|
| `/codex` | **Second Opinion** —— 来自 OpenAI Codex CLI 的独立代码评审。三种模式：review（pass/fail gate）、adversarial challenge、open consultation。当 `/review` 和 `/codex` 都跑过后，还会给出跨模型分析。 |
| `/careful` | **Safety Guardrails** —— 在破坏性命令前发出警告（`rm -rf`、`DROP TABLE`、force-push）。说一句 “be careful” 就能激活。任何警告都可覆盖。 |
| `/freeze` | **Edit Lock** —— 将文件编辑限制在某个目录内。调试时防止误改范围外文件。 |
| `/guard` | **Full Safety** —— 一条命令同时启用 `/careful` + `/freeze`。适合生产环境工作。 |
| `/unfreeze` | **Unlock** —— 移除 `/freeze` 边界。 |
| `/open-gstack-browser` | **GStack Browser** —— 启动带侧栏、反机器人隐身、自动模型路由（Sonnet 负责动作，Opus 负责分析）、一键 cookie 导入与 Claude Code 集成的 GStack Browser。可清理页面、做智能截图、编辑 CSS，并把信息传回终端。 |
| `/setup-deploy` | **Deploy Configurator** —— 为 `/land-and-deploy` 做一次性初始化。自动检测你的平台、生产 URL 和部署命令。 |
| `/setup-gbrain` | **GBrain Onboarding** —— 从零到跑起 gbrain，不到 5 分钟。支持本地 PGLite、已有 Supabase URL，或通过 Management API 自动创建新的 Supabase 项目。还会为 Claude Code 注册 MCP，并按仓库设置 trust triad（read-write/read-only/deny）。[Full guide](USING_GBRAIN_WITH_GSTACK.md)。 |
| `/gstack-upgrade` | **Self-Updater** —— 把 gstack 升级到最新版本。可识别全局安装还是 vendored 安装，并同步两边，展示变更内容。 |

### New binaries (v0.19)

除了 slash-command skills 之外，gstack 还提供了适合在 session 外运行的独立 CLI：

| Command | What it does |
|---------|-------------|
| `gstack-model-benchmark` | **Cross-model benchmark** —— 让同一个 prompt 同时跑 Claude、GPT（通过 Codex CLI）和 Gemini，对比延迟、token、成本，以及可选的 LLM-judge 质量评分。会按 provider 自动检测认证，缺失的 provider 会被优雅跳过。输出可为 table、JSON 或 markdown。`--dry-run` 可在不消耗 API 调用的情况下验证参数与认证。 |
| `gstack-taste-update` | **Design taste learning** —— 将 `/design-shotgun` 中的接受与拒绝写入持久化的项目级 taste profile。每周衰减 5%。这些数据会反馈到后续 variant 生成中，让系统逐渐学会你真正会选什么。 |

### Continuous checkpoint mode (opt-in, local by default)

设置 `gstack-config set checkpoint_mode continuous` 后，skills 会在你工作过程中自动提交，提交信息以 `WIP:` 为前缀，并带上结构化的 `[gstack-context]` 内容（决策、剩余工作、失败路径）。即使崩溃或切换上下文也能保住进度。`/context-restore` 会读取这些提交来重建 session 状态。`/ship` 会在发 PR 之前把 WIP commits 过滤压缩（保留非 WIP commits），这样 `bisect` 仍然保持干净。是否推送由 `checkpoint_push=true` 控制，默认只在本地，因此不会因为每一个 WIP commit 都触发 CI。

### Domain skills + raw CDP escape hatch

两个新的浏览器原语会让 gstack agent 随着时间推移持续增强：

- **`$B domain-skill save`** —— agent 为某个站点保存一条按域名生效的笔记（例如“LinkedIn 的 Apply 按钮在一个 iframe 里”），下次访问该 hostname 时会自动触发。经历 quarantined → active（连续成功 3 次）→ 通过 `$B domain-skill promote-to-global` 可选提升为跨项目全局规则。存储位置与 `/learn` 的项目级学习文件放在一起。完整说明见：**[docs/domain-skills.md](docs/domain-skills.md)**。
- **`$B cdp <Domain.method>`** —— 当封装好的命令覆盖不到时，可以直接逃逸到原始 Chrome DevTools Protocol。默认拒绝：方法必须显式添加到 `browse/src/cdp-allowlist.ts`，并附上一行理由说明。双层 mutex 会把浏览器级 CDP 调用与每个 tab 的工作串行化。对于数据外泄类方法，其输出会被包裹在 UNTRUSTED envelope 中。 |

> 想要完全无护栏、无 allowlist、无 daemon 的 raw CDP，只做 agent 到 Chrome 的轻量传输？[browser-use/browser-harness-js](https://github.com/browser-use/browser-harness-js) 是另一种哲学（agent 自写 helpers，而不是 gstack 的 curated commands）。如果你不想要 gstack 的安全栈，它会很合适。两者可以共存：gstack 的 `$B cdp` 和 harness 都能通过 Playwright 的 `newCDPSession` 连接到同一个 Chrome。

**[Deep dives with examples and philosophy for every skill →](docs/skills.md)**

### Karpathy's four failure modes? Already covered.

Andrej Karpathy 的 [AI coding rules](https://github.com/forrestchang/andrej-karpathy-skills)（17K stars）精准命中了四种失败模式：错误假设、过度复杂、正交编辑、命令式优先于声明式。gstack 的 workflow skills 对这四点都有强制约束。`/office-hours` 会在写代码前把假设逼出来；Confusion Protocol 会阻止 Claude 在架构决策上瞎猜；`/review` 会抓出不必要的复杂性和顺手乱改；`/ship` 会把任务转成可验证的目标，并以 test-first 的方式执行。如果你已经在用 Karpathy 风格的 `CLAUDE.md` 规则，那么 gstack 就是把这些规则贯彻到整轮 sprint 的工作流执行层，而不只是某一个 prompt。

## Parallel sprints

gstack 在单个 sprint 下已经很好用。十个 sprint 同时跑起来时，事情才真正有意思。

**设计是核心。** `/design-consultation` 会从零构建设计系统，研究现有方案，提出有创造性的风险，并写出 `DESIGN.md`。但真正的魔法在于 shotgun-to-HTML 这条流水线。

**`/design-shotgun` 是探索方式。** 你描述你想要什么。它用 GPT Image 生成 4-6 个 AI mockup 变体，然后在浏览器里打开一个对比面板，把所有变体并排展示。你挑出喜欢的版本，给反馈（“更多留白”“标题更大胆”“去掉渐变”），它再生成新一轮。重复，直到你真的喜欢为止。几轮之后，taste memory 会开始偏向你实际偏好的方向。不再是用语言描述视觉想法然后祈祷 AI 理解，而是真正看到选项、挑出好的、用视觉方式迭代。

**`/design-html` 让它真正落地。** 把已经批准的 mockup（来自 `/design-shotgun`、CEO plan、design review，或者只是一个描述）转换成生产质量的 HTML/CSS。不是那种在某个 viewport 看起来还行、换个尺寸就全碎的 AI HTML。这里使用 Pretext 做计算式文本布局：文本在 resize 时会真实重排，高度会根据内容调整，布局是动态的。30KB 开销，零依赖。它会检测你的框架（React、Svelte、Vue），并输出正确格式。智能 API 路由会根据页面类型（landing page、dashboard、form、card layout）选择不同的 Pretext 模式。输出是真能发版的东西，不是 demo。

**`/qa` 是一个巨大的解锁点。** 它让我能把并行 worker 数量从 6 提升到 12。Claude Code 先说 *"I SEE THE ISSUE"*，然后真的去修问题、生成回归测试、验证修复结果，这改变了我的工作方式。agent 现在有眼睛了。

**智能评审路由。** 就像一家运转良好的创业公司一样：CEO 不需要看基础设施 bug 修复，design review 也没必要插手纯后端变更。gstack 会跟踪哪些 review 已经执行、判断什么评审适合当前改动，然后直接做正确的事。Review Readiness Dashboard 会在你发版前告诉你当前站位。

**什么都要测。** 如果你的项目还没有测试框架，`/ship` 会从零帮你搭起来。每次 `/ship` 都会产出覆盖率审计。每次 `/qa` 修 bug 都会生成一个回归测试。目标是 100% test coverage —— 测试会把 vibe coding 变成安全可控，而不是 yolo coding。

**`/document-release` 是你从未拥有过的工程师。** 它会读取项目中的每一份文档，对照 diff 做交叉核对，并更新所有已经漂移的内容。README、ARCHITECTURE、CONTRIBUTING、CLAUDE.md、TODOS —— 都能自动保持最新。而现在 `/ship` 还会自动调用它，所以文档无需额外命令也能保持同步。

**真实浏览器模式。** `/open-gstack-browser` 会启动 GStack Browser，一个由 AI 控制的 Chromium，带反机器人隐身、自定义品牌和内置侧边栏扩展。Google、NYTimes 这类网站也能不碰验证码直接工作。菜单栏显示的是 “GStack Browser”，而不是 “Chrome for Testing”。你的日常 Chrome 保持完全不受影响。所有已有 browse 命令都无需修改。`$B disconnect` 可返回 headless 模式。只要窗口开着，浏览器就会一直活着，不会因为 idle timeout 在你工作时被杀掉。

**Sidebar agent —— 你的 AI 浏览器助手。** 在 Chrome 侧边栏里输入自然语言，一个子 Claude 实例就会帮你执行。“打开设置页并截图。”“填这个表单，用测试数据。”“遍历这个列表里的每一项，把价格抓出来。” 侧边栏会自动路由到合适的模型：Sonnet 负责快速动作（点击、跳转、截图），Opus 负责阅读和分析。每个任务最多 5 分钟。Sidebar agent 运行在隔离 session 中，不会干扰你的主 Claude Code 窗口。侧栏底部还能一键导入 cookie。

**个人自动化。** Sidebar agent 不只适用于开发工作流。例子：“打开我孩子学校的家长门户，把所有其他家长的姓名、电话和照片加入我的 Google Contacts。” 有两种认证方式：（1）在 headed browser 里登录一次，session 会持续保留；（2）点击侧栏底部的 “cookies” 按钮，从你的真实 Chrome 导入 cookies。认证完成后，Claude 就会自动导航目录、提取数据并创建联系人。

**Prompt injection 防御。** 恶意网页会试图劫持你的 sidebar agent。gstack 提供分层防御：浏览器内置一个 22MB 的 ML classifier，在本地扫描每个页面和工具输出；Claude Haiku transcript check 会对整段对话形态投票；系统 prompt 中的随机 canary token 会捕捉跨文本、工具参数、URL 和文件写入的 session exfil 尝试；最终 verdict combiner 需要两个 classifier 同时同意才会阻断（避免单模型在 Stack Overflow 风格说明页上误报）。侧栏头部的盾牌图标会显示状态（green/amber/red）。如果想启用 721MB 的 DeBERTa-v3 ensemble，可设置 `GSTACK_SECURITY_ENSEMBLE=deberta`，采用 2-of-3 agreement。紧急关闭开关：`GSTACK_SECURITY_OFF=1`。完整机制见 [ARCHITECTURE.md](ARCHITECTURE.md#prompt-injection-defense-sidebar-agent)。

**当 AI 卡住时的浏览器接管。** 遇到 CAPTCHA、认证墙或 MFA 提示？`$B handoff` 会在完全相同的页面上打开一个可见的 Chrome，保留你所有 cookies 和 tabs。你手动处理掉问题后，告诉 Claude 你完成了，`$B resume` 就会从中断处继续。连续失败 3 次后，agent 甚至会自动建议你这样做。

**`/pair-agent` 是跨 agent 协作。** 你在 Claude Code 里，同时也开着 OpenClaw，或者 Hermes，或者 Codex。你希望它们一起看同一个网站。输入 `/pair-agent`，选中目标 agent，GStack Browser 就会打开，你可以全程旁观。skill 会打印出一段说明文字。把它粘贴到另一个 agent 的聊天窗口中，它会用一次性 setup key 换取 session token，创建自己的 tab，然后开始浏览。你会看到两个 agent 在同一个浏览器中工作，各自拥有自己的 tab，彼此无法干扰。如果系统装了 ngrok，tunnel 会自动启动，因此另一个 agent 甚至可以在完全不同的机器上。对于同机 agent，还提供零摩擦捷径，可直接写入凭据。这是第一次，不同厂商的 AI agents 能通过共享浏览器并在真实安全约束下协作：作用域 token、tab 隔离、限流、域名限制、活动归因。

**多 AI second opinion。** `/codex` 会从 OpenAI 的 Codex CLI 获得一份独立评审，也就是一个完全不同的 AI 针对同一个 diff 给出意见。三种模式：带 pass/fail gate 的代码评审、主动尝试搞坏你代码的 adversarial challenge，以及带会话连续性的开放咨询。当 `/review`（Claude）和 `/codex`（OpenAI）都审过同一个分支后，你会得到一份跨模型分析，显示哪些发现是重叠的，哪些是各自独有的。

**按需启用安全护栏。** 说一句 “be careful”，`/careful` 就会在所有破坏性命令前提醒你 —— `rm -rf`、`DROP TABLE`、force-push、`git reset --hard`。`/freeze` 会在调试时把编辑锁定到某个目录，防止 Claude “顺手”去修无关代码。`/guard` 会同时开启两者。`/investigate` 在调查过程中会自动冻结到当前模块。

**主动技能建议。** gstack 会感知你当前所处阶段 —— brainstorming、reviewing、debugging、testing —— 然后推荐合适的 skill。不喜欢？说一句 “stop suggesting”，它会跨会话记住。

## 10-15 parallel sprints

gstack 在一个 sprint 上就很强大，同时跑十个时则是变革性的。

[Conductor](https://conductor.build) 可以并行运行多个 Claude Code sessions —— 每个都在各自隔离的 workspace 里。一个 session 在新想法上跑 `/office-hours`，另一个在 PR 上跑 `/review`，第三个实现功能，第四个在 staging 上跑 `/qa`，另外六个还在其他分支上。全部同时进行。我经常会同时跑 10-15 个 parallel sprints —— 这是目前比较现实的上限。

正是 sprint 结构让并行真正可行。没有流程，十个 agents 就是十个混乱源。只有有了流程 —— think、plan、build、review、test、ship —— 每个 agent 才知道自己该做什么，以及什么时候停下。你管理它们的方式，和 CEO 管理团队很像：关注关键决策，其他部分放手让它运转。

### Voice input (AquaVoice, Whisper, etc.)

gstack skills 提供了适合语音输入的触发短语。你可以自然地说出你的需求 ——
“run a security check”、“test the website”、“do an engineering review” —— 系统会自动激活对应 skill。你不需要记住 slash command 名称或缩写。

## Uninstall

### Option 1: Run the uninstall script

如果 gstack 已安装在你的机器上：

```bash
~/.claude/skills/gstack/bin/gstack-uninstall
```

它会处理 skills、symlinks、全局状态（`~/.gstack/`）、项目本地状态、browse daemons 和临时文件。使用 `--keep-state` 可以保留配置和分析数据。使用 `--force` 可以跳过确认。

### Option 2: Manual removal (no local repo)

如果你没有保留仓库克隆（例如通过 Claude Code 粘贴安装，后来又把 clone 删除了）：

```bash
# 1. Stop browse daemons
pkill -f "gstack.*browse" 2>/dev/null || true

# 2. Remove per-skill symlinks pointing into gstack/
find ~/.claude/skills -maxdepth 1 -type l 2>/dev/null | while read -r link; do
  case "$(readlink "$link" 2>/dev/null)" in gstack/*|*/gstack/*) rm -f "$link" ;; esac
done

# 3. Remove gstack
rm -rf ~/.claude/skills/gstack

# 4. Remove global state
rm -rf ~/.gstack

# 5. Remove integrations (skip any you never installed)
rm -rf ~/.codex/skills/gstack* 2>/dev/null
rm -rf ~/.factory/skills/gstack* 2>/dev/null
rm -rf ~/.kiro/skills/gstack* 2>/dev/null
rm -rf ~/.openclaw/skills/gstack* 2>/dev/null

# 6. Remove temp files
rm -f /tmp/gstack-* 2>/dev/null

# 7. Per-project cleanup (run from each project root)
rm -rf .gstack .gstack-worktrees .claude/skills/gstack 2>/dev/null
rm -rf .agents/skills/gstack* .factory/skills/gstack* 2>/dev/null
```

### Clean up CLAUDE.md

卸载脚本不会修改 `CLAUDE.md`。在每个曾经接入 gstack 的项目里，请手动删除 `## gstack` 和 `## Skill routing` 这两个 section。

### Playwright

`~/Library/Caches/ms-playwright/`（macOS）会被保留下来，因为其他工具可能也在共用它。如果没有别的工具需要，可以自行删除。

---

免费、MIT licensed、开源。没有 premium tier，没有 waitlist。

我把自己构建软件的方法开源了。你可以 fork 它，并把它变成你自己的。

> **We're hiring.** Want to ship real products at AI-coding speed and help harden gstack?
> Come work at YC — [ycombinator.com/software](https://ycombinator.com/software)
> Extremely competitive salary and equity. San Francisco, Dogpatch District.

## GBrain — persistent knowledge for your coding agent

[GBrain](https://github.com/garrytan/gbrain) 是 AI agents 的持久化知识库 —— 你可以把它理解成 agent 真正能在多次会话间保留下来的记忆。GStack 提供了一条从零到“已经跑起来，agent 能调用它”的一键路径。

```bash
/setup-gbrain
```

三条路径，任选其一：

- **Supabase, existing URL** —— 你的云端 agent 已经 provision 好了一个 brain；把 Session Pooler URL 粘过来，这台电脑就能使用同一份数据。
- **Supabase, auto-provision** —— 粘贴一个 Supabase Personal Access Token；skill 会创建一个新项目，轮询直到健康，获取 pooler URL，再交给 `gbrain init`。端到端约 90 秒。
- **PGLite local** —— 零账号、零网络、约 30 秒。只在这台 Mac 上存在的隔离 brain。非常适合先试用；之后可以通过 `/setup-gbrain --switch` 迁移到 Supabase。

初始化之后，这个 skill 会询问你是否要把 gbrain 注册为 Claude Code 的 MCP server（`claude mcp add gbrain -- gbrain serve`），这样 `gbrain search`、`gbrain put_page` 等就会作为一等 typed tools 出现，而不是 bash shell-out。

**按 remote 维度的信任策略。** 你机器上的每个仓库都可以配置为三种级别之一：

- `read-write` —— agent 既可以搜索 brain，也可以从当前仓库写入新的页面
- `read-only` —— agent 可以搜索但绝不写入（非常适合服务多个客户的顾问：搜索共享 brain，但在 Client B 的仓库里不把 Client A 的工作污染进去）
- `deny` —— 完全禁止与 gbrain 交互

这个 skill 每个 repo 只会问一次。对于同一个 remote 的不同 worktree 和 branch，这个决定会持续生效。

**GStack memory sync（不同功能，但复用了同一套私有仓库基础设施）。** 它可选择把你的 gstack state（learnings、CEO plans、design docs、retros、developer profile）推送到一个私有 git 仓库，这样你的记忆就能跟随你在不同机器之间移动。流程中会弹出一次隐私确认（全部 allowlisted / 仅 artifacts / 关闭），并带有 defense-in-depth secret scanner，在数据离开你的机器之前拦截 AWS keys、tokens、PEM blocks 和 JWTs。

```bash
gstack-brain-init
```

**最完整版本 —— 所有场景、所有 flag、所有 bin helper、所有排障步骤：** [USING_GBRAIN_WITH_GSTACK.md](USING_GBRAIN_WITH_GSTACK.md)

其他参考： [docs/gbrain-sync.md](docs/gbrain-sync.md)（sync 专项指南） • [docs/gbrain-sync-errors.md](docs/gbrain-sync-errors.md)（错误索引）

## Docs

| Doc | What it covers |
|-----|---------------|
| [Skill Deep Dives](docs/skills.md) | 每个 skill 的理念、示例与工作流（包含 Greptile integration） |
| [Builder Ethos](ETHOS.md) | 构建者哲学：Boil the Lake、Search Before Building、三层知识体系 |
| [Using GBrain with GStack](USING_GBRAIN_WITH_GSTACK.md) | `/setup-gbrain` 的所有路径、flag、bin helper 和排障步骤 |
| [GBrain Sync](docs/gbrain-sync.md) | 跨机器记忆设置、隐私模式、故障排查 |
| [Architecture](ARCHITECTURE.md) | 设计决策与系统内部机制 |
| [Browser Reference](BROWSER.md) | `/browse` 的完整命令参考 |
| [Contributing](CONTRIBUTING.md) | 开发环境、测试、contributor mode 与 dev mode |
| [Changelog](CHANGELOG.md) | 每个版本的新内容 |

## Privacy & Telemetry

gstack 包含 **opt-in** 的使用遥测，用于帮助改进项目。下面是完整说明：

- **默认关闭。** 除非你明确同意，否则不会向任何地方发送任何数据。
- **首次运行时，** gstack 会询问你是否愿意分享匿名使用数据。你可以拒绝。
- **会发送什么（如果你选择开启）：** skill 名称、执行时长、成功/失败、gstack 版本、操作系统。仅此而已。
- **绝不会发送什么：** 代码、文件路径、仓库名、分支名、prompts，或任何用户生成内容。
- **随时可改：** `gstack-config set telemetry off` 会立即关闭全部遥测。

数据存储在 [Supabase](https://supabase.com)（开源版 Firebase 替代方案）中。schema 位于 [`supabase/migrations/`](supabase/migrations/) —— 你可以自行验证到底收集了什么。仓库中的 Supabase publishable key 是公开 key（类似 Firebase API key）；row-level security policies 会阻止任何直接访问。遥测数据通过经过验证的 edge functions 流转，这些函数会强制执行 schema checks、event type allowlists 和字段长度限制。

**本地分析始终可用。** 运行 `gstack-analytics` 即可从本地 JSONL 文件查看你自己的使用面板，不需要任何远程数据。

## Troubleshooting

**Skill 不显示？** `cd ~/.claude/skills/gstack && ./setup`

**`/browse` 失败？** `cd ~/.claude/skills/gstack && bun install && bun run build`

**安装已陈旧？** 运行 `/gstack-upgrade` —— 或在 `~/.gstack/config.yaml` 中设置 `auto_upgrade: true`

**想要更短的命令？** `cd ~/.claude/skills/gstack && ./setup --no-prefix` —— 从 `/gstack-qa` 切换为 `/qa`。这个选择会在后续升级中被记住。

**想要命名空间命令？** `cd ~/.claude/skills/gstack && ./setup --prefix` —— 从 `/qa` 切换为 `/gstack-qa`。如果你会同时使用其他 skill packs，这会很有用。

**Codex 提示 “Skipped loading skill(s) due to invalid SKILL.md”？** 你的 Codex skill 描述已经过期。修复方式：`cd ~/.codex/skills/gstack && git pull && ./setup --host codex` —— 如果是 repo-local 安装，则运行：`cd "$(readlink -f .agents/skills/gstack)" && git pull && ./setup --host codex`

**Windows 用户：** gstack 可在 Windows 11 上通过 Git Bash 或 WSL 运行。除 Bun 之外还需要 Node.js —— Bun 在 Windows 上的 Playwright pipe transport 有已知问题（[bun#4253](https://github.com/oven-sh/bun/issues/4253)）。browse server 会自动回退到 Node.js。请确保 `bun` 和 `node` 都在你的 PATH 中。

**Claude 说它看不到这些 skills？** 确保你的项目 `CLAUDE.md` 里有 gstack section。加入下面这段：

```
## gstack
Use /browse from gstack for all web browsing. Never use mcp__claude-in-chrome__* tools.
Available skills: /office-hours, /plan-ceo-review, /plan-eng-review, /plan-design-review,
/design-consultation, /design-shotgun, /design-html, /review, /ship, /land-and-deploy,
/canary, /benchmark, /browse, /open-gstack-browser, /qa, /qa-only, /design-review,
/setup-browser-cookies, /setup-deploy, /setup-gbrain, /retro, /investigate, /document-release,
/codex, /cso, /autoplan, /pair-agent, /careful, /freeze, /guard, /unfreeze, /gstack-upgrade, /learn.
```

## License

MIT。永久免费。去构建点什么吧。
