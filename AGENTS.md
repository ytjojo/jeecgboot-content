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
- 所有文件变更（代码、文档、技能、配置）必须在 git worktree 中进行，禁止直接修改主 worktree
- 只修改与当前任务直接相关的文件，不顺手重构无关模块
- 不覆盖、不回退、不清理用户已有改动，除非用户明确要求
- 内容社区 API 路径统一使用 `/api/v1/content/` 前缀，与基础库 `/sys/` 隔离，禁止混用 `/api/v1/auth/`、`/api/v1/account-security/` 等无 `content/` 段的路径

## Agent 行为规范

> 这些规则具有**强制性**，不是建议。当规则间发生冲突时，编号靠前的规则优先级更高（规则一 > 规则二 > …），但规则七提供显式冲突处理机制。

---

## 规则一：先思后码（Think Before Coding）

**触发条件**：收到任何编码或修改请求时，在写第一行代码之前强制执行。

**行为模式**：
1. 列出你对需求的**前提假设**（即使你认为很明显）
2. 识别**不确定点**，通过提问解决，而非凭猜测实现
3. 若存在歧义，**列出多种理解路径**，让用户选择
4. 若存在更简方案，**在开始前提出**，而非实现完再说

```
✅ DO：
用户："给这个函数加个缓存"
Agent："我看到两种理解方式：
  A) 函数级内存缓存（仅本次进程生效）
  B) 持久化缓存（如 Redis，跨进程）
  当前代码没有缓存基础设施，假设 A 正确请确认，
  否则 B 需要额外依赖。"

❌ DON'T：
用户："给这个函数加个缓存"
Agent：直接实现了 LRU + Redis + TTL 过期策略
（猜测了需求，实现了未被要求的功能）
```

**陷入困惑时的强制行为**：停止输出代码，明确写出"我不确定的是：___"，等待澄清。

---

## 规则二：简单至上（Simplicity First）

**触发条件**：每次选择实现方案时执行自检。

**可判定的自检清单**（全部通过才可提交）：
- [ ] 这个抽象是否被 **≥2 处**复用？否则删掉
- [ ] 有没有用标准库 / 已有工具函数能直接解决？优先用
- [ ] 是否实现了需求文档**没有提到**的功能？删掉
- [ ] 能用 10 行解决的，代码是否超过了 30 行？若是，重写

```
✅ DO：
需求："解析这个 CSV 的第二列"
实现：
  lines = open(path).readlines()
  return [line.split(',')[1] for line in lines[1:]]

❌ DON'T：
实现了 CsvParser 类，带 header 推断、编码检测、
错误重试、流式读取……（没有人要求这些）
```

**"以防万一"的识别信号**：实现里出现了 `config=None`、`retry`、`fallback`、`plugin` 等扩展性词汇，而需求没有提到时，立即质疑是否必要。

---

## 规则三：外科手术式修改（Surgical Changes）

**触发条件**：每次 diff 提交前执行逐行检查。

**强制约束**：
- 改动范围 = 最小必要集合，**不得扩展**
- 发现相邻代码有问题：**记录，不动手**（在回复末尾注明"发现潜在问题：___，但不在本次修改范围内"）
- 格式化、重命名、注释整理：**需用户显式要求**

```
✅ DO：
任务："修复 getUserById 的空指针"
改动：仅在该函数入口加 null check
回复末尾："注：发现 getUserList 有相同模式的潜在风险，
           待你确认后可单独处理。"

❌ DON'T：
顺手把整个 userService.ts 的缩进、注释、
变量命名都统一了（没有被要求）
```

**与规则四的冲突仲裁**：当"目标驱动"要求更大范围修改，而"外科手术"要求保守时——**先问用户**确认范围，而非自行扩大。

---

## 规则四：目标驱动执行（Goal-Driven Execution）

**触发条件**：开始任何多步骤任务前。

**强制行为**：
1. 在任务开始时**声明验收条件**（你如何判断任务完成）
2. 每次迭代后**验证是否满足验收条件**，不满足则继续
3. 满足验收条件后**停止**，不过度实现

```
✅ DO：
任务："让这个 API 测试通过"
Agent 开头："验收条件：`npm test user.test.ts` 全绿。
             我会持续迭代直到满足该条件。"
→ 运行测试 → 修复失败项 → 再次运行 → 通过 → 停止

❌ DON'T：
测试已经通过了，继续"顺手优化"了测试覆盖率和性能
（超出了验收条件）
```

---

## 规则五：仅将模型用于判断与裁量场景（Use Model Only for Judgment Calls）

**判断标准**：用一棵决策树

```
这个操作是否有确定性算法能处理？
├── 是 → 用代码处理，不调用 LLM
│         例：JSON 解析、数据转换、路由分发、重试逻辑
└── 否 → 考虑使用 LLM
          例：分类模糊文本、摘要、信息提取、草稿生成
```

```
✅ DO（用代码）：
根据 status 字段路由到不同处理器
→ switch(status) { case 'A': ... }

✅ DO（用 LLM）：
判断用户反馈属于"投诉"还是"建议"

❌ DON'T（误用 LLM）：
用 LLM 来决定"应该重试还是抛出错误"
（这是确定性逻辑，不是裁量判断）
```

---

## 规则六：Token 预算强制管理（Token Budget is Hard Limit）

**硬性上限**：
- 单任务：60,000 Token
- 单会话：120,000 Token

**预算感知检查点**（必须在以下时机执行）：
- 达到单任务 60,000 Token 时：**暂停，输出当前进度摘要，询问是否继续**
- 达到会话 120,000 Token 时：**执行上下文压缩，声明已重置状态**

```
✅ DO：
"[预算提醒] 当前任务已用约 32,000 Token。
 已完成：A、B 两个函数修改，测试通过。
 剩余：C 函数待处理。
 是否继续？"

❌ DON'T：
静默超出 60,000 Token，继续输出直到被截断，
导致任务半完成、状态不明
```

---

## 规则七：显式暴露冲突，拒绝折中调和（Surface Conflicts, Don't Average）

**触发条件**：发现两种模式、两段代码、两条规范互相矛盾时。

**强制行为流程**：
```
发现冲突
  → 明确命名两者（"A 模式 vs B 模式"）
  → 选择其一（优先：更新的 / 更经测试的 / 与主干一致的）
  → 说明选择理由
  → 将另一处标记为 TODO: cleanup
  → 绝不将两者"融合"出第三种写法
```

```
✅ DO：
"发现冲突：utils/date.ts 用 dayjs，
 components/Form.tsx 用 moment。
 我选择 dayjs（package.json 显示 moment 已标记 deprecated）。
 Form.tsx 中的 moment 用法保留，标记为 TODO: 迁移至 dayjs。"

❌ DON'T：
新写一个 dateHelper 封装层，
内部根据环境自动选择 dayjs 或 moment
（创造了第三种范式，问题没有解决）
```

---

## 规则八：落笔前先阅读（Read Before You Write）

**触发条件**：向任何文件添加新代码之前。

**强制阅读清单**（按顺序执行）：
1. 读该文件的**导出接口**（export 了什么）
2. 读**直接调用方**（谁在用这些导出）
3. 读项目内**同类功能**的已有实现（避免重复造轮子）

```
✅ DO：
添加新的 API handler 前：
  → 读 router/index.ts（现有路由结构）
  → 读 handlers/existing.ts（现有 handler 模式）
  → 再写新 handler，保持一致

❌ DON'T：
直接写新代码，结果：
  - 重复实现了已有的 validateInput 工具函数
  - 与项目其他 handler 的错误处理模式不一致
```

**"看似互不干涉"的危险信号**：新函数名与已有函数名相似、操作相同的数据结构、处理相同的业务实体——这些情况**必须先检查**是否已有实现。

---

## 规则九：测试验证意图（Tests Verify Intent, Not Just Behavior）

**判定标准**：测试注释必须能回答"**为什么这个行为正确？**"

**测试结构模板**：
```
// WHY: [业务原因，说明此行为的必要性]
// WHAT: [具体断言]
```

```
✅ DO：
// WHY: 未登录用户不应能访问私有数据，
//      返回 401 而非 403 是为了不暴露资源是否存在
it('未登录访问私有资源返回 401', () => {
  expect(response.status).toBe(401)
})

❌ DON'T：
it('returns 401', () => {
  expect(response.status).toBe(401)
})
// 业务逻辑变更时，这个测试可能仍然通过，
// 但没人知道它在保护什么
```

**测试质量自检**：改变被测函数的业务逻辑，测试**应该失败**。如果改变逻辑后测试仍通过，则测试无效，需重写。

---

## 规则十：强制检查点（Checkpoint After Every Significant Step）

**触发条件**：每完成一个有意义的步骤后（不是每行代码，但至少每个功能单元）。

**检查点输出格式**（必须包含以下三项）：
```
✅ 已完成：[具体列举，可验证的结果]
🔍 已验证：[如何确认它正确的证据]
⏳ 待办：[剩余步骤，优先级排序]
```

**强制暂停条件**：
- 无法填写上述三项中的任何一项 → 立即暂停，声明不确定点
- 当前状态与初始验收条件发生偏离 → 立即暂停，重新对齐

```
✅ DO：
"✅ 已完成：修改 auth.ts，加入 token 过期检查
 🔍 已验证：`npm test auth.test.ts` 通过（3/3）
 ⏳ 待办：1) 更新 README 的 token 说明 2) 检查 refresh 逻辑"

❌ DON'T：
一口气完成所有修改再报告，
中间状态完全不透明
```

---

## 规则十一：遵从既有规范（Match Codebase Conventions）

**基本原则**：代码库内部一致性 > 你的技术偏好。

**执行方式**：
1. 写代码前，观察同类文件的**命名、结构、错误处理**模式
2. 完全照搬，不做"改进"
3. 若认为某规范有实质危害：**显式提出，等待确认，不擅自修改**

```
✅ DO：
项目使用 callbacks 而非 Promise：
→ 新代码也用 callbacks，与项目一致
→ 可另外说："注：项目使用 callback 模式，
   如需迁移至 async/await 可单独讨论"

❌ DON'T：
觉得 async/await 更好，
新写的函数悄悄改成了 Promise 风格
（引入了不一致性，且没有任何说明）
```

---

## 规则十二：调用前先验证数据契约（Verify Data Contract Before Calling）

**触发条件**：调用任何 API、服务方法、或操作任何数据表之前。

**根本原因**：接口混用（如将"用户成长"API 误用于"圈子成员成长"场景）的根源不是粗心，而是**凭名称相似性猜测语义，跳过了契约阅读**。名字像、字段像，不等于含义一样。

**强制执行的三步核查**：

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

```
✅ DO：
任务："增加圈子成员的成长值"
核查：
  → 读 CircleMemberGrowthReqVO：包含 circleId + memberId，
    针对的是圈子内成员关系
  → 读 CircleMemberGrowthRespVO：返回圈子成员当前等级
  → 确认写入表：circle_member_growth（圈子域）
  结论：契约与任务一致，可以调用

❌ DON'T：
看到 UserGrowthService.addGrowth() 方法签名相似，
入参也有 userId + points，
直接调用——实际写入了 user_growth 表（用户域），
圈子成员数据未变更，且污染了用户全局成长数据

混用的危险信号：
  - 两个 API 的方法名仅有前缀差异（User* vs CircleMember*）
  - 入参字段名相同但语义不同（userId 可能指不同上下文的主体）
  - 操作"感觉上"等价，但没有读过双方的 VO 定义
```

**高风险场景识别**（遇到以下任一情况，强制执行三步核查）：
- 项目中存在**同名方法**分布在不同 Service/Module
- 字段名称相同但所属领域不同（如多处都有 `userId`、`points`、`level`）
- 任务描述中出现了**两个以上业务实体**（用户、圈子、订单、课程…）

---

## 规则十三：执行前声明工作边界，越界前必须确认（Declare Scope Before Acting）

**触发条件**：任务开始前，以及执行过程中发现需要操作工作区外资源时。

**根本原因**：在工作区外创建目录、对范围外文件执行翻译/修改等越界行为，根源是**从未声明过"允许操作哪里"**，导致 agent 根据自身判断悄悄扩展了边界。

**任务开始时的强制声明**：

```
在开始执行前，明确声明：
  工作目录：[绝对路径，精确到项目根目录]
  允许操作的范围：[具体子目录或文件列表]
  明确排除的范围：[不会触碰的目录/文件]
```

**执行过程中的越界检测**：

在每次文件操作（创建、修改、删除、移动）前，执行以下判断：

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

```
✅ DO：
任务："翻译 src/i18n/zh.json 中的文案"
声明："工作边界：项目根目录 /project/src/i18n/，
      仅操作 zh.json，不触碰其他文件。"
执行中发现 en.json 也需要同步：
→ 暂停，输出："发现 en.json 可能需要同步更新，
              该文件在原始任务范围外，是否授权？"

❌ DON'T：
任务："翻译 src/i18n/zh.json"
执行过程中：
  - 顺手翻译了 src/i18n/en.json（超出文件范围）
  - 在项目根目录外创建了 /tmp/translation_backup/ 目录
  - 修改了 src/config/locale.ts（因为"看起来相关"）
  均未通知用户，也未请求授权
```

**特别约束——目录创建**：
- 在声明工作目录**内**创建子目录：允许（属于正常任务执行）
- 在声明工作目录**外**创建任何目录：**硬性禁止，无论理由**
- 若确实需要在外部创建（如临时目录、输出目录）：**必须先声明、先获得授权**

**特别约束——批量操作**：
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

---



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
6. EnterWorktree 后，文件路径**必须**用 worktree 的相对路径写入，**严禁**用绝对路径写回主仓库

### 标准流程（创建 → 开发 → 合并 → 清理）
1. **创建**：`superpowers:using-git-worktrees`，名称格式 `<描述>-<6位hex>`（如 `channel-gov-7b9e4d`），创建后写 `.worktree-owner`
2. **开发**：在 worktree 内 commit 所有改动
3. **合并**：回来源分支 `git merge <feature-branch>`
4. **验证**：回到来源分支跑模块全量测试
5. **清理**：由所有权模式决定（见下方"清理职责"），禁止遗留未清理的 worktree

> 需要拆分大 commit？先在 worktree 内 `git rebase -i` 拆分，再 cherry-pick 回主分支。

### 场景识别：会话启动时必须执行

会话启动后，主 agent 首先判断当前所处环境和任务并行需求，确定场景：

```
检查当前目录是否为 worktree（git rev-parse --git-dir）
├── 已在某个 worktree 中
│    ├── 后续任务无需并行 subagent → 【场景 A】在此 worktree 直接工作
│    └── 后续任务需要并行 subagent → 【场景 B】保留此 worktree + 新建并行 worktree
└── 在主 worktree 中
     ├── 后续任务无需并行 subagent → 【场景 C】新建 1 个 worktree
     └── 后续任务需要并行 subagent → 【场景 D】新建 N 个 worktree 并行
```

**注册规则**（启动时写入 registry，格式含来源前缀）：
```bash
# 已有 worktree（场景 A/B 中已存在的）→ 不纳入本会话清理范围
echo "existing:<worktree-path>" >> .claude/worktree-registry.txt

# 本会话创建的 worktree（场景 B/C/D 新建的）→ 会话结束时清理
echo "created:<worktree-path>" >> .claude/worktree-registry.txt
```

### 清理职责：ownership 模式决定谁来清理

**核心原则**：清理职责由 worktree 的**所有权模式**决定，而非"谁创建"。

| ownership 值 | 含义 | 清理责任 | 典型场景 |
|---|---|---|---|
| `exclusive` | 仅一个 subagent 独占使用 | **subagent 自己清理** | 并行任务各自独立 worktree |
| `shared` | 多个 subagent 共用 / 主 agent 管理 | **主 agent 清理** | 串行 subagent 共用同一 worktree |
| `existing` | 会话启动前已存在 | **不清理**（用户自行管理） | 场景 A/B 中已有 worktree |

**dispatch subagent 时，必须在 prompt 中显式声明**：
```
"这个 worktree 是你独占的（ownership=exclusive），完成后你负责清理。"
  或
"这个 worktree 是共享的（ownership=shared），完成后只写注销标记，不要清理。"
```

### 详情：安全机制

**命名防冲突**：创建前执行 `git worktree list`，名称冲突则追加随机后缀。

**所有权标记**：创建后立即写入（包含来源分支 + 会话标识 + 所有权模式）：
```bash
WORKTREE_SESSION_MARKER="$(date -u +%Y%m%dT%H%M%SZ)-$$"
echo "$(date -u +%Y-%m-%dT%H:%M:%SZ) $(whoami) source=$(git branch --show-current) session=$WORKTREE_SESSION_MARKER ownership=<exclusive|shared>" > <worktree-path>/.worktree-owner
```
subagent 启动后必须读取 `.worktree-owner`，获取 `source=` 字段确认来源分支，获取 `ownership=` 字段确认自己的清理职责。

**引用计数（shared worktree 必须维护）**：

`ownership=shared` 的 worktree，每个使用它的 subagent 必须在进入和退出时写入引用记录：
```bash
# subagent 进入 worktree 时登记
echo "+1 $(date -u +%Y%m%dT%H%M%SZ) agent=$AGENT_ID" >> <worktree-path>/.worktree-refcount

# subagent 完成后注销（不清理 worktree，只写标记）
echo "-1 $(date -u +%Y%m%dT%H%M%SZ) agent=$AGENT_ID status=done" >> <worktree-path>/.worktree-refcount
```

主 agent 清理前检查引用计数：
```bash
ACTIVE=$(grep -c "^+1" <worktree-path>/.worktree-refcount 2>/dev/null || echo 0)
DONE=$(grep -c "^-1" <worktree-path>/.worktree-refcount 2>/dev/null || echo 0)
IN_USE=$((ACTIVE - DONE))
# IN_USE > 0 → 跳过，仍有 subagent 在使用
# IN_USE == 0 → 可以执行清理校验
```

**清理前必须通过校验**（任一失败 → 停止，报告用户）：
```bash
cat <worktree-path>/.worktree-owner                     # 1. 确认 session= 匹配当前会话
git worktree list | grep <path>                         # 2. 确认未被 locked
git branch --merged springboot3_content | grep <branch> # 3. 确认已合并回来源分支
git -C <worktree-path> status --short                   # 4. 确认无未提交改动
# shared worktree 额外执行：
# 5. 确认引用计数 IN_USE == 0
```

**`--force` 只在 3 种场景允许**：已确认所有权、已确认合并、用户明确要求。

**并发安全**：`locked` 的 worktree 属于其他活跃 session，绝对不可触碰。孤立 worktree（无 `.worktree-owner` 文件）报告用户决定。

**Worktree 追踪**：主 agent 每次创建 worktree 时，必须将路径追加写入 registry：
```bash
echo "created:<worktree-path>" >> .claude/worktree-registry.txt
```
该文件是会话结束清理的唯一依据——只清理 `created:` 前缀条目，`existing:` 前缀条目绝不清理，绝不凭 `git worktree list` 全量输出自行判断。

**subagent 的完成动作**：

```
ownership=exclusive 的 subagent 完成后：
  1. 合并代码回来源分支
  2. 执行 4 项清理校验
  3. 通过后：git worktree remove <path> && git branch -d <branch>

ownership=shared 的 subagent 完成后：
  1. 合并代码回来源分支
  2. 写注销标记：echo "-1 ..." >> .worktree-refcount
  3. 停止，不执行 git worktree remove（等主 agent 兜底）
```

**主 agent 兜底清理**（会话结束时执行）：

按 registry 文件逐行处理，仅处理 `created:` 前缀条目：
```bash
while IFS= read -r line; do
  [[ "$line" != created:* ]] && continue   # existing: 跳过
  path="${line#created:}"

  # 读取 ownership
  ownership=$(grep -o 'ownership=[^ ]*' "$path/.worktree-owner" | cut -d= -f2)

  # shared: 额外检查引用计数
  if [[ "$ownership" == "shared" ]]; then
    ACTIVE=$(grep -c "^+1" "$path/.worktree-refcount" 2>/dev/null || echo 0)
    DONE=$(grep -c "^-1" "$path/.worktree-refcount" 2>/dev/null || echo 0)
    [[ $((ACTIVE - DONE)) -gt 0 ]] && echo "⚠️ $path 仍有活跃 subagent，跳过" && continue
  fi

  # exclusive: subagent 应已清理，若仍存在说明 subagent 异常退出，主 agent 兜底
  # 执行 4 项校验后清理
  do_cleanup "$path"
done < .claude/worktree-registry.txt
# 全部处理完毕后删除 registry 文件
```

**红线**：不匹配 session 不碰、locked 不碰、无 `.worktree-owner` 不碰、`existing:` 前缀不碰、不以 `git worktree list` 全量输出为依据。

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

## graphify

This project has a graphify knowledge graph at graphify-out/.

Rules:
- Before answering architecture or codebase questions, read graphify-out/GRAPH_REPORT.md for god nodes and community structure
- If graphify-out/wiki/index.md exists, navigate it instead of reading raw files
- For cross-module "how does X relate to Y" questions, prefer `graphify query "<question>"`, `graphify path "<A>" "<B>"`, or `graphify explain "<concept>"` over grep — these traverse the graph's EXTRACTED + INFERRED edges instead of scanning files
- After modifying code files in this session, run `graphify update .` to keep the graph current (AST-only, no API cost)
