# AGENTS.md

## 作用范围
本文件是仓库级全局规则入口，只保留所有目录通用的硬约束、规则优先级和文档路由。
不在本文件中维护大段项目介绍、命令清单、部署步骤或排障手册。

## 规则优先级
1. 当前目录及更深层目录中的 `AGENTS.md`
2. 仓库根目录 `AGENTS.md`
3. `docs/agent-context/` 下的文档默认仅作参考，不是硬约束；
   但被 AGENTS.md 显式标注为"必读"的文档除外，其内容与 AGENTS.md 具有同等约束力

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
- 所有文件变更（代码、文档、技能、配置）必须在 git worktree 中进行，禁止直接修改主 worktree
- 只修改与当前任务直接相关的文件，不顺手重构无关模块
- 不覆盖、不回退、不清理用户已有改动，除非用户明确要求
- 内容社区 API 路径统一使用 `/api/v1/content/` 前缀，与基础库 `/sys/` 隔离，禁止混用 `/api/v1/auth/`、`/api/v1/account-security/` 等无 `content/` 段的路径

## Agent 行为规范

> 这些规则具有**强制性**，不是建议。当规则间发生冲突时，编号靠前的规则优先级更高（规则一 > 规则二 > …），但规则七提供显式冲突处理机制。

## 规则书写约定

每条规则只保留高触发信息：
- **触发**：什么时候必须执行
- **必须**：必须完成的动作
- **禁止**：不得出现的行为
- **停止 / 验证**：什么时候暂停，或如何确认已执行

---

## 规则一：先思后码（Think Before Coding）

**触发**：收到任何编码、修改、配置、文档变更请求时，在写第一行内容前执行。

**必须**：
- 列出对需求的前提假设。
- 识别不确定点；有歧义时列出理解路径并让用户选择。
- 发现更简方案时先提出，再开始实现。

**禁止**：
- 未确认歧义就实现。
- 把未要求的缓存、持久化、重试、抽象、扩展能力做进去。

**停止**：无法判断真实意图时，输出"我不确定的是：___"，等待澄清。

---

## 规则二：简单至上（Simplicity First）

**触发**：每次选择实现方案、抽象方式、工具或依赖时执行。

**必须**：
- 优先使用标准库、已有工具函数、现有模式。
- 用最小代码满足需求文档已声明的能力。
- 提交前完成自检：
- [ ] 这个抽象是否被 **≥2 处**复用？否则删掉
- [ ] 有没有用标准库 / 已有工具函数能直接解决？优先用
- [ ] 是否实现了需求文档**没有提到**的功能？删掉
- [ ] 能用 10 行解决的，代码是否超过了 30 行？若是，重写

**禁止**：为"以防万一"新增 `config=None`、`retry`、`fallback`、`plugin`、通用框架等未被要求的扩展点。

**验证**：若需求去掉某个能力后代码仍保留相关实现，说明过度设计，必须删除。

---

## 规则三：外科手术式修改（Surgical Changes）

**触发**：每次准备修改文件、扩大 diff、提交前检查时执行。

**必须**：
- 改动范围保持最小必要集合。
- 相邻代码有问题时只记录，不动手。
- 格式化、重命名、注释整理必须有用户显式要求。

**禁止**：借修 bug、补测试、改文档之名顺手重构无关代码。

**停止**：若目标达成需要扩大范围，先说明原因并请求确认。

---

## 规则四：目标驱动执行（Goal-Driven Execution）

**触发**：开始任何多步骤任务、修复、实现、验证流程前执行。

**必须**：
- 开始时声明验收条件。
- 每次迭代后验证是否满足验收条件。
- 不满足则继续；满足后停止。

**禁止**：目标已达成后继续顺手优化、扩展范围或追加功能。

**验证**：最终回复必须说明验收条件是否满足，以及使用了什么证据。

---

## 规则五：仅将模型用于判断与裁量场景（Use Model Only for Judgment Calls）

**触发**：准备使用模型处理数据、路由、解析、重试、分类、摘要、生成前执行。

**必须**：
- 先判断是否存在确定性算法。
- 确定性任务用代码处理；裁量性任务才使用模型。

**禁止**：用模型替代 JSON 解析、字段路由、状态机、重试策略、数据转换等确定性逻辑。

**验证**：能用明确输入输出规则表达的逻辑，不得交给模型判断。

---

## 规则六：Token 预算强制管理（Token Budget is Hard Limit）

**硬性上限**：
- 单任务：60,000 Token
- 单会话：120,000 Token

**预算感知检查点**（必须在以下时机执行）：
- 达到单任务 60,000 Token 时：**暂停，输出当前进度摘要，询问是否继续**
- 达到会话 120,000 Token 时：**执行上下文压缩，声明已重置状态**

**必须**：预算检查点包含已完成、已验证、剩余事项、是否需要继续。

**禁止**：静默超预算，或在状态不明时继续扩大任务。

---

## 规则七：显式暴露冲突，拒绝折中调和（Surface Conflicts, Don't Average）

**触发**：发现两种模式、两段代码、两条规范互相矛盾时执行。

**必须**：
```
发现冲突
  → 明确命名两者（"A 模式 vs B 模式"）
  → 选择其一（优先：更新的 / 更经测试的 / 与主干一致的）
  → 说明选择理由
  → 将另一处标记为 TODO: cleanup
  → 绝不将两者"融合"出第三种写法
```

**禁止**：把冲突双方平均、混搭、包装成第三种新范式。

**验证**：最终说明选择了哪一边、为什么、另一边如何标记或保留。

---

## 规则八：落笔前先阅读（Read Before You Write）

**触发**：向任何文件添加新代码、新配置、新接口、新文档结构前执行。

**必须按顺序阅读**：
1. 读该文件的**导出接口**（export 了什么）
2. 读**直接调用方**（谁在用这些导出）
3. 读项目内**同类功能**的已有实现（避免重复造轮子）

**禁止**：仅凭文件名、函数名、字段名相似就新增实现。

**停止**：新函数名与已有函数名相似、操作相同数据结构、处理相同业务实体时，必须先检查是否已有实现。

---

## 规则九：测试验证意图（Tests Verify Intent, Not Just Behavior）

**触发**：新增或修改测试时执行。

**必须**：
- 测试注释回答"为什么这个行为正确"。
- 使用 WHY / WHAT 结构说明业务原因和具体断言。

**测试结构模板**：
```
// WHY: [业务原因，说明此行为的必要性]
// WHAT: [具体断言]
```

**禁止**：只写"returns 200/401"一类无法解释业务意图的测试。

**验证**：改变被测函数的业务逻辑，测试应该失败；否则测试无效，需重写。

---

## 规则十：强制检查点（Checkpoint After Every Significant Step）

**触发**：每完成一个有意义步骤后执行；至少每个功能单元一次。

**必须输出三项**：
```
✅ 已完成：[具体列举，可验证的结果]
🔍 已验证：[如何确认它正确的证据]
⏳ 待办：[剩余步骤，优先级排序]
```

**禁止**：长时间连续修改而不暴露中间状态。

**停止**：
- 无法填写上述三项中的任何一项 → 立即暂停，声明不确定点
- 当前状态与初始验收条件发生偏离 → 立即暂停，重新对齐

---

## 规则十一：遵从既有规范（Match Codebase Conventions）

**触发**：新增或修改代码、测试、配置、文档结构时执行。

**必须**：
- 代码库内部一致性优先于个人技术偏好。
- 写前观察同类文件的命名、结构、错误处理、测试风格。
- 照搬既有模式；认为规范有实质危害时先提出，等待确认。

**禁止**：在局部改动中悄悄引入新的风格、框架、错误处理范式。

**验证**：新增内容能被放进同类文件中而不显得风格突兀。

---

## 规则十二：调用前先验证数据契约（Verify Data Contract Before Calling）

**触发**：调用任何 API、服务方法、操作任何数据表或跨领域实体前执行。

**必须执行三步核查**：

```
步骤一：读 Request VO
  → 每个字段的业务含义是什么？
  → 必填字段代表的实体是哪个领域对象？

步骤二：读 Response VO
  → 返回数据代表的是哪个业务实体？
  → 与当前任务操作的实体是否完全一致？

步骤三：确认目标数据表 / 存储
  → 这次调用最终写入 / 读取的是哪张表？
  → 该表的业务归属是否与当前任务一致？
```

**一致性判定**：三步全部与当前业务场景一致，才允许调用。**任何一步存疑，先问，不猜。**

**禁止**：凭名称相似、字段相同、操作感觉等价就调用。

**高风险场景识别**（遇到以下任一情况，强制执行三步核查）：
- 项目中存在**同名方法**分布在不同 Service/Module
- 字段名称相同但所属领域不同（如多处都有 `userId`、`points`、`level`）
- 任务描述中出现了**两个以上业务实体**（用户、圈子、订单、课程…）

---

## 规则十三：执行前声明工作边界，越界前必须确认（Declare Scope Before Acting）

**触发**：任务开始前，以及执行中发现需要操作工作区外资源时执行。

**必须声明工作边界**：

```
在开始执行前，明确声明：
  工作目录：[绝对路径，精确到项目根目录]
  允许操作的范围：[具体子目录或文件列表]
  明确排除的范围：[不会触碰的目录/文件]
```

**必须在每次文件操作前检测**：

```
目标路径是否在声明的工作目录内？
├── 是 → 继续
└── 否 → 强制暂停
          输出："[越界警告] 即将操作 <路径>，
                 该路径在声明的工作边界之外。
                 原因：<为什么任务需要它>
                 请确认是否授权此操作。"
          等待用户显式授权，不得自行判断"应该没问题"
```

**禁止**：
- 未声明边界就创建、修改、删除、移动文件。
- 自行扩展批量操作文件列表。
- 在声明工作目录外创建目录，除非先获得用户授权。

**特别约束**：
- 在声明工作目录**内**创建子目录：允许（属于正常任务执行）
- 在声明工作目录**外**创建任何目录：**硬性禁止，无论理由**
- 若确实需要在外部创建（如临时目录、输出目录）：**必须先声明、先获得授权**
- 翻译、格式化、重命名等批量操作，必须在任务开始时**明确列出受影响文件列表**
- 文件列表须经用户确认后才能执行，不得在执行中自行扩展列表

---

## 规则间冲突仲裁速查表

| 冲突场景 | 优先规则 | 处理方式 |
|---|---|---|
| 目标达成需要大范围修改（四）vs 外科手术（三） | 三 | 先问用户确认修改范围 |
| 简单实现（二）vs 遵从复杂的既有模式（十一） | 十一 | 遵从既有模式，可附注说明 |
| 阅读成本高影响 Token 预算（八）vs 预算限制（六） | 六 | 优先读关键接口，跳过实现细节 |
| 发现现有代码有 bug，但不在本次任务范围（三）vs 目标驱动（四） | 三 | 记录 + 告知，不动手 |
| 完成目标需要操作边界外资源（四）vs 工作边界（十三） | 十三 | 暂停，声明越界原因，等待授权 |
| 两个 API 功能相近难以区分（八）vs 快速交付（四） | 十二 | 强制三步契约核查，不以效率为由跳过 |

详细示例见 docs/agent-context/agent-rule-examples.md；示例用于解释，不削弱本节强制规则。

---



## 单元测试规范
- 后端测试规范：`docs/agent-context/springboot-testing-conventions.md`
- 前端测试规范：`docs/agent-context/frontend-testing-conventions.md`

## Git Worktree & 分支管理

> ⚠️ 执行细节见 `docs/agent-context/worktree-rules.md`，**主 agent 启动后必须首先阅读**，再做任何 worktree 操作。

### 秒懂：不可违反的底线
1. git worktree 中代码 **严禁**向非来源分支提交或合并代码
2. worktree 必须合并回**来源分支**，禁止跨分支合并
3. **禁止**从 worktree 复制文件到主 worktree 后重新 commit（丢失元数据，绕过 git）
4. 执行 `superpowers:finishing-a-development-branch` 时，**严禁**选择 Option 1（Merge locally）合并到 `master` or `main`
5. 对于 `BRANCH_A` 分支上的 worktree，合并目标**始终**为 `BRANCH_A`
6. EnterWorktree 后，文件路径**必须**用 worktree 的相对路径写入，**严禁**用绝对路径写回主仓库

### 标准流程（创建 → 开发 → 合并 → 清理）
1. **创建**：`superpowers:using-git-worktrees`，名称格式 `<描述>-<6位hex>`（如 `channel-gov-7b9e4d`），创建后写 `.worktree-owner`
2. **开发**：在 worktree 内 commit 所有改动
3. **合并**：回来源分支 `git merge <feature-branch>`
4. **验证**：回到来源分支跑模块全量测试
5. **清理**：由 ownership 模式决定（见下方），禁止遗留未清理的 worktree

> 需要拆分大 commit？先在 worktree 内 `git rebase -i` 拆分，再 cherry-pick 回主分支。

### 清理职责：ownership 模式决定谁来清理

**核心原则**：清理职责由 worktree 的**所有权模式**决定，而非"谁创建"。

| ownership 值 | 含义 | 清理责任 | 典型场景 |
|---|---|---|---|
| `exclusive` | 仅一个 subagent 独占使用 | **subagent 自己清理** | 并行任务各自独立 worktree |
| `shared` | 多个 subagent 共用 / 主 agent 管理 | **主 agent 清理** | 串行 subagent 共用同一 worktree |
| `existing` | 会话启动前已存在 | **不清理**（用户自行管理） | 进入已有 worktree 工作 |

**dispatch subagent 时，必须在 prompt 中显式声明**：
```
"这个 worktree 是你独占的（ownership=exclusive），完成后你负责清理。"
  或
"这个 worktree 是共享的（ownership=shared），完成后只写注销标记，不要清理。"
```

**主 agent 始终兜底**：无论 subagent 是否完成清理，会话结束时主 agent 按 `.claude/worktree-registry.txt` 扫描所有 `created:` 条目，对遗漏未清理的 worktree 执行兜底处理。详见 `docs/agent-context/worktree-rules.md`。

## 代码实现 Workflow

### 秒懂：4 条硬规则
1. **必须在 worktree 中开发**——见全局硬规则，适用所有文件变更（代码、文档、技能、配置），禁止直接在主 worktree 修改任何文件（启动前 `git worktree list` 确认）
2. **必须使用 subagent 编排**（`/superpowers:subagent-driven-development`）or (`/dispatching-parallel-agents`)，主 agent 不直接写代码
3. **必须使用 TDD 流程**（`/superpowers:test-driven-development`）：先写测试 → 红灯 → 绿灯 → 重构
4. **subagent 必须知晓来源分支**：dispatch subagent 时，父 agent 必须在 prompt 中显式告知来源分支（worktree 创建时所在分支），subagent 以此为合并目标。subagent 可读取 `.worktree-owner` 中的 `source=` 字段交叉验证

### 完成标准（DoD）
每个代码任务按顺序完成，**不得跳过**：
1. **实现** — 通过 subagent + TDD 流程
2. **Code Review** — 加载 `superpowers:requesting-code-review`，按 git SHA 范围派发 reviewer subagent，检查代码质量、命名、边界条件、安全性
3. **覆盖率 ≥ 90%** — 不满足则补充测试
4. **模块全量测试 100% 通过** — `mvn test -pl <module> -am`，禁止带红提交
5. **git 操作** — 在 worktree 内 commit → 合并回来源分支 → 验证 → 清理

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

## subagent 启动决策规则

> ⚠️ 主 agent 启动后**必须首先阅读**，再做任何任务拆分或 subagent dispatch 决策。

规则文档：`docs/agent-context/subagent-spawn-decision-rules.md`


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
- subagent启动决策规则: `docs/agent-context/subagent-spawn-decision-rules.md`
- Git Worktree 详细规则：`docs/agent-context/worktree-rules.md`

## graphify

This project has a graphify knowledge graph at graphify-out/.

Rules:
- Before answering architecture or codebase questions, read graphify-out/GRAPH_REPORT.md for god nodes and community structure
- If graphify-out/wiki/index.md exists, navigate it instead of reading raw files
- For cross-module "how does X relate to Y" questions, prefer `graphify query "<question>"`, `graphify path "<A>" "<B>"`, or `graphify explain "<concept>"` over grep — these traverse the graph's EXTRACTED + INFERRED edges instead of scanning files
- After modifying code files in this session, run `graphify update .` to keep the graph current (AST-only, no API cost)
