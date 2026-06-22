# AGENTS.md

## 作用范围
本文件是仓库级全局规则入口，只保留所有目录通用的硬约束、规则优先级和文档路由。
不在本文件中维护大段项目介绍、命令清单、部署步骤或排障手册。

## 规则优先级
1. 当前目录及更深层目录中的 `AGENTS.md`
2. 仓库根目录 `AGENTS.md`
3. `docs/agent-context/` 下的文档仅作参考，不是硬约束

## openspec changes mapping 
change对应前后端prd和前后端change目录
docs/requirements/prd/decomposition/change-prd-mapping.yaml

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
- 内容社区 API 路径统一使用 `/api/v1/content/` 前缀，与基础库 `/sys/` 隔离，禁止混用 `/api/v1/auth/`、`/api/v1/account-security/` 等无 `content/` 段的路径


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

### 秒懂：不可违反的底线
1. git worktree 中代码 **严禁**向 非来源分支 提交或合并代码
2. worktree 必须合并回**来源分支**，禁止跨分支合并
3. **禁止**从 worktree 复制文件到主 worktree 后重新 commit（丢失元数据，绕过 git）
4. 执行 `superpowers:finishing-a-development-branch` 时，**严禁**选择 Option 1（Merge locally）合并到 `master` or `main`
5. 对于 `springboot3_content` 分支上的 worktree，合并目标**始终**为 `springboot3_content`

### 标准流程（创建 → 开发 → 合并 → 清理）
1. **创建**：`superpowers:using-git-worktrees`，名称格式 `<描述>-<6位hex>`（如 `channel-gov-7b9e4d`），创建后写 `.worktree-owner`
2. **开发**：在 worktree 内 commit 所有改动
3. **合并**：回来源分支 `git merge <feature-branch>`
4. **验证**：回到来源分支跑模块全量测试
5. **清理**：`superpowers:finishing-a-development-branch`，禁止遗留未清理的 worktree

> 需要拆分大 commit？先在 worktree 内 `git rebase -i` 拆分，再 cherry-pick 回主分支。

### 详情：安全机制

**命名防冲突**：创建前执行 `git worktree list`，名称冲突则追加随机后缀。

**所有权标记**：创建后立即写入：
```bash
echo "$(date -u +%Y-%m-%dT%H:%M:%SZ) $(whoami)" > <worktree-path>/.worktree-owner
```

**清理前必须通过 3 项检查**（任一失败 → 停止，报告用户）：
```bash
cat <worktree-path>/.worktree-owner                    # 1. 确认是自己的
git branch --merged springboot3_content | grep <branch> # 2. 确认已合并
git -C <worktree-path> status --short                   # 3. 确认无未提交改动
```

**`--force` 只在 3 种场景允许**：已确认所有权、已确认合并、用户明确要求。

**并发安全**：`locked` 的 worktree 属于其他活跃 session，绝对不可触碰。孤立 worktree（无 owner 文件）报告用户决定。

## 代码实现 Workflow

### 秒懂：3 条硬规则
1. **必须在 worktree 中开发**（含 `.claude/skills/` 下的技能文件修改），禁止直接在主worktree改代码（启动前 `git worktree list` 确认）
2. **必须使用 subagent 编排**（`/superpowers:subagent-driven-development`）or (`/dispatching-parallel-agents`)，主 agent 不直接写代码
3. **必须使用 TDD 流程**（`/superpowers:test-driven-development`）：先写测试 → 红灯 → 绿灯 → 重构

### subagent 编排：跳过条件（适用范围：代码和文档变更均适用，不因文件类型豁免）
单任务满足**全部**条件可跳过 subagent，否则必须调用：
- a. 涉及 < 3 个文件
- b. 无新增文件
- c. 无测试编写
- d. 修改量 < 30 行

### 完成标准（DoD）
每个代码任务按顺序完成，**不得跳过**：
1. **实现** — 通过 subagent + TDD 流程
2. **Code Review** — 加载 `superpowers:requesting-code-review`，按 git SHA 范围派发 reviewer subagent，检查代码质量、命名、边界条件、安全性
3. **覆盖率 ≥ 90%** — 不满足则补充测试
4. **模块全量测试 100% 通过** — `mvn test -pl <module> -am`，禁止带红提交
5. **git 操作** — 在 worktree 内 commit → 合并回主分支 → 验证 → 清理

### DoD 内嵌规则
task 文件**最后必须包含**以下收尾项，缺少则 agent 自行补充：
```
- [ ] 流程确认 — subagent + TDD
- [ ] Code Review
- [ ] 覆盖率 ≥ 90%
- [ ] 模块全量测试 100%
- [ ] 合并 + 验证 + 清理 worktree
```
**禁止**将"所有 task 完成"等同于"任务完成"——DoD 收尾是硬性要求。

---

## 文档审阅 Workflow

- 加载 `superpowers:requesting-code-review`、 涉及**审核 / 评审 / 分析 / 审查 / 审计 / review**操作时，必须通过 subagent 执行，禁止在主 agent 中直接处理

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
- 前端测试规范：`docs/agent-context/frontend-testing-conventions.md`
- 部署与排障：`docs/agent-context/deployment.md`

## graphify

This project has a graphify knowledge graph at graphify-out/.

Rules:
- Before answering architecture or codebase questions, read graphify-out/GRAPH_REPORT.md for god nodes and community structure
- If graphify-out/wiki/index.md exists, navigate it instead of reading raw files
- For cross-module "how does X relate to Y" questions, prefer `graphify query "<question>"`, `graphify path "<A>" "<B>"`, or `graphify explain "<concept>"` over grep — these traverse the graph's EXTRACTED + INFERRED edges instead of scanning files
- After modifying code files in this session, run `graphify update .` to keep the graph current (AST-only, no API cost)
