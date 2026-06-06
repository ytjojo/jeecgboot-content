# AGENTS.md

## 作用范围
本文件是仓库级全局规则入口，只保留所有目录通用的硬约束、规则优先级和文档路由。
不在本文件中维护大段项目介绍、命令清单、部署步骤或排障手册。

## 规则优先级
1. 当前目录及更深层目录中的 `AGENTS.md`
2. 仓库根目录 `AGENTS.md`
3. `docs/agent-context/` 下的文档仅作参考，不是硬约束

## 路由
- 修改后端代码前，先阅读 `jeecg-boot/AGENTS.md`
- 修改前端代码前，先阅读 `jeecgboot-vue3/AGENTS.md`
- 修改内容社区模块前，先阅读 `jeecg-boot/jeecg-boot-module/jeecg-module-content/AGENTS.md`

## 环境
**首选语言**: 中文 (Chinese)
- 所有 AI Agent 交互、文档生成、代码注释和沟通都应使用中文。专业术语可以用英文
**操作系统**:macOS

## 全局硬规则
- 只修改与当前任务直接相关的文件，不顺手重构无关模块
- 不覆盖、不回退、不清理用户已有改动，除非用户明确要求


## 全局规则

除非显式覆盖，否则本规则适用于本项目中的所有任务。核心倾向：非琐碎工作，谨慎优先于速度；琐碎任务可自主判断处理。

- 规则一：先思后码（Think Before Coding）明确声明前提假设。遇不确定处，先提问而非盲目猜测。存在歧义时，列出多种可能的理解路径。若存在更简方案，应果断提出异议。陷入困惑时立即暂停，并明确指出模糊之处。
- 规则二：简单至上（Simplicity First）仅用最少代码解决问题。杜绝任何“以防万一”的猜测性实现。不实现需求之外的功能。不为仅用一次的代码强行设计抽象。自检：资深工程师是否会认为此实现过度复杂？若是，立即简化。
- 规则三：外科手术式修改（Surgical Changes）仅改动绝对必要的部分。仅清理自身引入的冗余或错误。切勿“顺手优化”相邻代码、注释或排版格式。未出问题的代码绝不重构。严格贴合项目既有风格。
- 规则四：目标驱动执行（Goal-Driven Execution）明确定义成功标准（验收条件）。持续迭代直至验证通过。不要死板遵循步骤。定义成功形态并自主迭代。清晰的成功标准赋予你独立闭环执行的能力。
- 规则五：仅将模型用于判断与裁量场景（Use the model only for judgment calls）适用于我：分类、起草、摘要总结、信息提取。切勿用于：路由分发、重试机制、确定性数据转换。若常规代码能给出答案，就由代码处理。
- 规则六：Token 预算绝非软性建议（Token budgets are not advisory）单任务上限：4,000 Token。单会话上限：30,000 Token。接近预算上限时，执行上下文摘要并重置状态。主动暴露超支。切勿静默越界消耗。
- 规则七：显式暴露冲突，拒绝折中调和（Surface conflicts, don't average them）若两种模式相互矛盾，明确择一（优先更新或更经测试的版本）。阐明选择理由。将另一处标记为待清理项。切勿强行融合冲突范式。
- 规则八：落笔前先阅读（Read before you write）添加代码前，通读该文件的导出接口、直接调用方及公共工具函数。“看似互不干涉”是最危险的判断。若不理解现有代码为何如此设计，先提问。
- 规则九：测试验证意图，而非仅验证行为（Tests verify intent, not just behavior）测试必须体现该行为*为何重要*（WHY），而非仅断言它*做了什么*（WHAT）。若业务逻辑变更时测试仍不报错，则该测试设计错误。
- 规则十：关键步骤后强制设立检查点（Checkpoint after every significant step）总结已完成事项、已验证结果及剩余待办。若无法向我清晰描述当前状态，绝不可继续推进。若丢失上下文或逻辑偏离，立即暂停并重新声明当前状态。
- 规则十一：严格遵从代码库既有规范，即便持保留意见（Match the codebase's conventions, even if you disagree）在代码库内部：规范一致性 > 个人技术偏好。若确信某规范存在实质危害，请显式提出。切勿暗中另起范式。
- 规则十二：显式失败（Fail loud）若有步骤被静默跳过，宣称”已完成”即为错误。若有测试被跳过，宣称”测试通过”即为错误。默认原则：主动暴露不确定性，绝不掩盖。

## 单元测试规范
- 后端测试规范：`docs/agent-context/springboot-testing-conventions.md`
- 前端测试规范：`docs/agent-context/frontend-testing-conventions.md`

## Git Worktree & 分支管理

### 禁止事项
- **严禁**向 `master` 分支提交或合并任何代码

### 分支规则
- 项目活跃分支：`springboot3_content`
- `master` 与 `springboot3_content` 为**并行分支**，基础库不同，不可互相合并

### Worktree 合并规则
- worktree 必须合并回**其来源分支**，禁止跨分支合并
- 示例：从 `springboot3_content` 创建的 worktree → 只能合并回 `springboot3_content`

### Worktree 提交规则（禁止复制文件重新提交）
- **在 worktree 内完成 commit**，然后通过 `git cherry-pick` 或 `git merge` 合并回主分支
- **禁止**从 worktree 复制文件到主 worktree 后重新 commit — 这会丢失 commit 元数据（author、timestamp）且绕过 git 合并机制
- 如需拆分大 commit 为多个逻辑提交：先在 worktree 内用 `git rebase -i` 或 `git reset` 拆分，再 cherry-pick 到主分支

### Worktree 命名规则（防冲突）

**硬规则：worktree 名称必须唯一，禁止使用通用名（如 `p0-apis`、`feature`、`fix`）。**

命名格式：`<简短描述>-<6位随机hex>`
- 示例：`p0-apis-a3f2c1`、`channel-gov-7b9e4d`、`user-status-f1a8c2`
- 创建前**必须**执行 `git worktree list` 检查名称是否已存在
- 若名称冲突，追加随机后缀而非复用

### Worktree 所有权标记

**硬规则：创建 worktree 后立即写入所有权文件。**

```bash
# 创建后立即执行
echo "$(date -u +%Y-%m-%dT%H:%M:%SZ) $(whoami)" > <worktree-path>/.worktree-owner
```

用途：清理前读取此文件确认所有权，避免误删其他 agent 的 worktree。

### Worktree 生命周期（创建 → 完成 → 清理）

**硬规则：创建 worktree 的 agent 负责其完整生命周期，包括最终清理。**

流程：
1. **创建**：`/using-git-worktrees` or  `EnterWorktree` 生成 worktree + 分支
   - 名称必须符合命名规则（含随机后缀）
   - 创建前执行 `git worktree list` 确认无冲突
   - 创建后立即写入 `.worktree-owner` 文件
2. **开发**：在 worktree 内 commit 所有改动
3. **合并**：回主分支执行 `git merge <feature-branch>`
4. **验证**：在主分支跑模块全量测试，确认通过
5. **清理**：`git worktree remove <path>` + `git worktree prune` + `git branch -d <feature-branch>`

**禁止**：开发完成后遗留未清理的 worktree。合并回主分支后必须立即删除 worktree 和 feature 分支。

### Worktree 清理安全规则

**硬规则：禁止 `git worktree remove --force`，除非满足以下条件之一。**

允许 `--force` 的场景：
1. **已确认所有权**：读取 `.worktree-owner` 确认是当前 agent 创建的
2. **已确认合并**：`git branch --merged` 显示该分支已合并到来源分支
3. **用户明确要求**：用户直接指示删除该 worktree

清理前检查清单：
```bash
# 1. 检查是否是自己的 worktree
cat <worktree-path>/.worktree-owner

# 2. 检查分支是否已合并
git branch --merged springboot3_content | grep <branch-name>

# 3. 检查 worktree 内是否有未提交的改动
git -C <worktree-path> status --short
```

若以上任一检查失败 → **停止清理，报告给用户**。

### Worktree 并发安全

**场景**：多个 agent session 同时运行，各自创建 worktree。

- `git worktree list` 中以 `locked` 标记的 worktree 属于其他活跃 session，**绝对不可触碰**
- 未锁定的 worktree 也需通过 `.worktree-owner` 确认所有权后再操作
- 若发现孤立 worktree（无 owner 文件、分支已合并、无活跃 session），报告给用户决定是否清理

## 代码实现 Workflow

### 硬规则：必须使用 worktree
- **所有代码开发任务必须在 worktree 中进行**，禁止直接在主分支上修改代码
- **启动前检查**：开始编码前必须执行 `git worktree list` 确认当前是否在 worktree 中，不在则立即创建

### 硬规则：代码开发必须使用 `/superpowers:subagent-driven-development`

**所有代码开发任务默认使用 `/superpowers:subagent-driven-development` 进行编排，不得由主 agent 直接编写代码。**

跳过条件（必须**全部**满足才可跳过）：
- a. 单文件修改（涉及 < 3 个文件）
- b. 无新增文件
- c. 无测试编写
- d. 修改量 < 30 行

不满足上述任一条件 → 必须调用 `/superpowers:subagent-driven-development`。

启动流程：
1. 创建 worktree（`/using-git-worktrees` 或 `EnterWorktree`）
2. 调用 `/superpowers:subagent-driven-development` 编排实现任务
3. 内部管理 subagent 分配、代码编写、测试编写

### 硬规则：代码开发必须使用 `/superpowers:test-driven-development`

**所有代码开发任务必须使用 `/superpowers:test-driven-development` 流程。**

适用范围：
- 所有 `/opsx:apply`、`/openspec-apply-change` 操作
- 所有多步代码任务（3+ 步骤）
- 所有涉及新增文件或修改 3+ 文件的代码任务

调用方式：在 `/superpowers:subagent-driven-development` 编排中或独立调用 `/superpowers:test-driven-development`

流程要求：
1. **先写测试** — 测试定义行为预期
2. **红灯** — 运行测试，确认失败（测试有效）
3. **绿灯** — 写最少代码让测试通过
4. **重构** — 优化代码，保持测试通过
5. **覆盖率验证** — 变更代码行覆盖率 ≥ 90%

### 完成标准（Definition of Done）
每个代码任务必须按顺序完成以下步骤，**不得跳过**：

0. **流程检查** — 确认是否使用了 `/superpowers:subagent-driven-development` 和 `/superpowers:test-driven-development`（符合跳过条件的除外）
1. **实现** — 完成功能代码（通过 `/superpowers:subagent-driven-development` 编排和 `/superpowers:test-driven-development` 流程）
2. **Code Review** — 检查代码质量、命名、边界条件、安全性
3. **测试覆盖率** — 检查变更代码的行覆盖率，**必须 ≥ 90%**。不满足则补充测试代码，直至达标且全量测试通过
4. **单元测试** — 执行**模块级全量测试**（`mvn test -pl <module> -am`），确保 **100% 通过**，禁止带红测试提交
   - 不能只跑修改的测试类，必须跑模块全量，发现 mock 泄漏和桩冲突
   - 测试写完必须立即执行验证，不能"写完就算完成"
5. **git 操作** - 单元测试通过后,分步git commit,如果当前工作区在worktree中 参考 Git Worktree & 分支管理 中要求操作(合并、验证、清理)

### 硬规则：DoD 必须内嵌到任务列表
- 任何 apply/实现 操作的 task 文件，**最后必须包含 DoD 收尾 tasks**：
  - `[ ] 流程确认 — 确认使用了 /superpowers:subagent-driven-development 和 /superpowers:test-driven-development`
  - `[ ] Code Review — subagent 执行代码质量审查`
  - `[ ] 测试覆盖率检查 — 变更代码行覆盖率 ≥ 90%`
  - `[ ] 全量单元测试 — 模块级 100% 通过`
  - `[ ] 合并回主分支 + 验证 + 清理 worktree`
- **如果 task 文件缺少这些步骤，agent 必须自行补充并执行**，不能以"task 列表已跑完"为由停止
- **禁止**将"所有 task 完成"等同于"任务完成"。task 列表只覆盖实现步骤，DoD 收尾是硬性要求
- 合并前必须通过 Code Review，合并后必须跑全量测试，测试通过后必须清理 worktree

---

## 文档审阅 Workflow

- 涉及**审核 / 评审 / 分析 / 审查 / 审计 / review**等操作时，必须通过 subagent 执行，禁止在主 agent 中直接处理

### 适用范围
- 文档（PRD、设计文档、接口文档、README、需求文档、规范文档 等）
- Skill 定义文件（`.md` skill 文件）

## 参考文档

- 项目概览：`docs/agent-context/project-overview.md`
- 常用命令：`docs/agent-context/commands.md`
- 架构与目录：`docs/agent-context/architecture.md`
- API 与后端约定：`docs/agent-context/api-guidelines.md`
- 后端编码规范：`docs/agent-context/springboot-coding-conventions.md`
- 后端数据库设计：`docs/agent-context/springboot-db-design.md`
- 后端测试规范：`docs/agent-context/springboot-testing-conventions.md`
- 部署与排障：`docs/agent-context/deployment.md`

## graphify

This project has a graphify knowledge graph at graphify-out/.

Rules:
- Before answering architecture or codebase questions, read graphify-out/GRAPH_REPORT.md for god nodes and community structure
- If graphify-out/wiki/index.md exists, navigate it instead of reading raw files
- For cross-module "how does X relate to Y" questions, prefer `graphify query "<question>"`, `graphify path "<A>" "<B>"`, or `graphify explain "<concept>"` over grep — these traverse the graph's EXTRACTED + INFERRED edges instead of scanning files
- After modifying code files in this session, run `graphify update .` to keep the graph current (AST-only, no API cost)
