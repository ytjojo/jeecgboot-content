Claude Code / Codex 中 Subagent 的合理使用边界

先说结论

在 Claude Code 和 Codex 中使用 subagent，核心原则不是“能拆就拆”，而是：

只有当任务可以被清晰隔离、可以减少主上下文污染、可以并行探索，或者单个上下文窗口不足以承载完整任务时，subagent 才有净收益。

Review → Impl 模式不是错误模式，但它不是默认最佳实践。

它的合理性取决于三个条件：

1. Review agent 是否只读；
2. Impl agent 是否拥有足够完整的修改上下文；
3. 多个 Impl agent 是否做到文件级或模块级隔离。

如果只是：

Review Agent 输出精简 JSON
↓
Impl Agent 根据 file + line + desc 修改代码

这个模式风险很高。

如果是：

多个只读 Review Agent 并行审查
↓
主 Agent 汇总、去重、排序、制定修复计划
↓
按文件或模块隔离分派 Impl Agent
↓
主 Agent 统一验收、测试、解决冲突

这个模式才是更稳的 subagent 使用方式。

⸻

Subagent 的本质定位

Subagent 不是为了把一个连续推理链机械拆成多段。

它真正适合解决的问题是：

1. 上下文隔离
    避免主 agent 被大量无关探索、日志、代码细节污染。
2. 并行探索
    让多个 agent 从不同角度同时分析同一个问题。
3. 职责专门化
    让不同 agent 只关注安全、性能、测试、架构、兼容性等单一维度。
4. 文件级隔离实现
    让多个实现 agent 分别修改互不重叠的文件或模块。
5. 超大任务拆分
    当单个 agent 的上下文窗口不足以覆盖完整代码库、需求和修改计划时，subagent 是必要手段。

因此，subagent 的价值不是“多一个 agent 更聪明”，而是：

用隔离和并行降低复杂度，而不是用拆分制造新的复杂度。

⸻

Review → Impl 模式为什么有条件合理

Review → Impl 模式的基本结构是：

Review Agent
    ↓
review report
    ↓
Impl Agent
    ↓
code changes

这个结构看起来很自然，但它只有在边界设计正确时才合理。

它适合：

1. Review 结果需要人工审核；
2. Review 内容较大，需要独立沉淀为报告；
3. Impl 可以按文件、模块或问题组并行执行；
4. Review agent 是只读角色，不直接修改代码；
5. Review 输出足够完整，Impl 不需要重新猜测 reviewer 的意图。

它不适合：

1. 中小规模任务；
2. 同一批文件既要 review 又要修改；
3. 修复方案高度依赖 reviewer 的完整上下文理解；
4. 多个 Impl agent 会修改同一文件；
5. Review 输出只是精简 JSON、行号和一句描述。

⸻

这个模式真正的问题

问题 1：Review agent 和 Impl agent 面对的是同一批文件

Review 只读本身没有问题。

问题出在 Impl agent 修改文件时，如果它依赖的是 Review agent 生成的行号，那么这个行号只在 Review agent 运行的那个时刻成立。

一旦出现以下情况，行号就会漂移：

1. 主 agent 已经修改过文件；
2. 另一个 Impl agent 修改了同一文件；
3. 格式化工具重排了代码；
4. Impl agent 修复第一个问题后，后续问题的行号变化；
5. Review 报告生成后代码继续演进。

因此，Review 输出不能只依赖 line number。

更稳的定位方式应该是：

文件：src/auth/auth.ts
函数：checkToken()
代码锚点：if (!payload.userId) ...
相关调用：middleware.ts 中 authMiddleware 调用该函数
问题：未校验 token 过期字段
修复目标：增加 exp 校验
禁止修改：不要改变 catch 块中的错误处理策略
验收标准：新增 token 过期测试，原有无 token 测试继续通过

行号只能作为辅助信息，不能作为核心交付物。

⸻

问题 2：Review → Impl 会造成上下文损耗

Code review 的价值不只是发现“哪里有问题”，还包括 reviewer 在阅读代码时建立的完整上下文理解。

例如 reviewer 看到的问题可能是：

{
  "file": "auth.ts",
  "line": 42,
  "desc": "未校验 token 过期时间"
}

但这个 JSON 丢失了很多关键上下文：

1. 这个函数被谁调用；
2. 调用方是否已经做过部分校验；
3. catch 块为什么保持宽松；
4. token payload 的结构来自哪里；
5. 测试里是否已经覆盖无 token、非法 token、过期 token；
6. 修改后是否会影响现有错误码；
7. 是否存在兼容旧 token 的历史逻辑。

Impl agent 拿到精简报告后，往往还需要重新读代码、重新推理、重新判断。

这时拆分并没有减少工作，反而制造了信息损耗。

⸻

问题 3：串行依赖削弱了 subagent 的并行价值

Review → Impl 是强串行关系：

Review 不完成，Impl 不能开始
Impl 修改后，Review 的行号和上下文可能失效

如果整个过程只能串行执行，那么它并没有充分利用多 agent 的并行优势。

真正适合并行的是：

Security Reviewer
Performance Reviewer
Test Reviewer
Architecture Reviewer

它们可以同时读取代码，从不同维度独立审查。

或者：

Impl-A：只改 auth/*
Impl-B：只改 api/*
Impl-C：只改 tests/*

它们可以在明确文件边界下并行实现。

⸻

判断 Review → Impl 是否值得拆分

可以用一句话判断：

如果一个不熟悉这段代码的工程师，仅凭 Review 报告就能正确、安全地完成修改，那么 Review → Impl 拆分是合理的。
如果他还必须重新读大量代码才能知道怎么改，那就不如让同一个 agent 一气呵成。

也可以用下面这张表判断：

判断条件	合理拆分	不宜拆分
Review 是否只读	是	否
Impl 是否有完整上下文	是	否
是否可以并行	可以按文件/模块并行	必须严格串行
是否修改同一文件	否	是
是否需要人工门控	是	否
单 agent 上下文是否足够	不足	足够
Review 输出是否可执行	是	只是摘要
测试与验收是否明确	是	否

⸻

更推荐的默认模式：单 Agent Review + Fix

对于中小规模 code review + fix 任务，默认推荐单 agent 完成：

单 Agent：
1. 读取所有相关文件
2. 建立完整上下文
3. 在内存中完成 review
4. 制定修复计划
5. 修改代码
6. 运行测试
7. 输出修改摘要

这不是偷懒，而是减少信息损耗。

适合场景：

1. 文件数量不多；
2. 修改集中在同一模块；
3. review 和 fix 高度相关；
4. 修复方案依赖完整调用链；
5. 不需要人工审核中间报告。

推荐指令：

请直接完成本次 code review + fix，不要拆分 subagent。
要求：
1. 先读取相关文件，建立完整上下文；
2. 在内部完成 review，不需要输出冗长中间报告；
3. 修改前给出简短修复计划；
4. 修改时保持最小变更；
5. 修改后运行相关测试；
6. 最后输出：
   - 修复了哪些问题；
   - 修改了哪些文件；
   - 运行了哪些验证；
   - 是否还有遗留风险。

⸻

更推荐的 Subagent 模式一：并行只读 Review

这是最稳的 subagent 使用方式。

主 Agent
   ↓
并行启动多个只读 Review Agent
   ↓
Security / Bug / Test / Architecture / Performance
   ↓
主 Agent 汇总、去重、排序
   ↓
主 Agent 决定是否修改

示例指令：

请使用多个只读 subagent 并行 review 当前代码。
subagent 分工：
1. security-reviewer：只检查认证、授权、输入校验、敏感信息、越权风险；
2. bug-reviewer：只检查空指针、边界条件、异常处理、并发问题、状态错误；
3. test-reviewer：只检查测试缺口、回归风险、边界用例缺失；
4. architecture-reviewer：只检查模块边界、依赖方向、职责混乱、过度耦合；
5. performance-reviewer：只检查明显性能问题、重复查询、大对象、循环中的重操作。
约束：
1. 所有 subagent 只读，禁止修改文件；
2. 每个问题必须包含：
   - 文件；
   - 函数/类/代码锚点；
   - 问题描述；
   - 影响；
   - 建议修复方式；
   - 验证方式；
3. 不要只给行号；
4. 不要输出泛泛建议；
5. 不要重复报告同一问题；
6. 所有 subagent 完成后，由主 agent 汇总、去重、按风险排序；
7. 修改代码前，先输出最终修复计划。

这种模式的优点是：

1. 并行收益真实存在；
2. 每个 reviewer 上下文独立；
3. 主 agent 保持决策权；
4. review 阶段不污染 working tree；
5. 适合人工审核。

⸻

更推荐的 Subagent 模式二：Review 后按文件并行 Impl

当 Review 已经完成，并且问题可以按文件或模块隔离时，可以并行 Impl。

合理结构：

Review Report
    ↓
主 Agent 拆分任务
    ↓
Impl-A：只改 auth/*
Impl-B：只改 api/*
Impl-C：只改 tests/*
    ↓
主 Agent 汇总 diff
    ↓
主 Agent 运行测试
    ↓
主 Agent 解决冲突

关键约束：

1. 每个 Impl agent 必须拥有独立文件范围；
2. 禁止多个 Impl agent 修改同一文件；
3. 禁止 Impl agent 扩大任务范围；
4. 禁止顺手重构无关代码；
5. 每个 Impl agent 必须输出修改摘要和验证结果；
6. 主 Agent 必须统一 review diff；
7. 最终测试由主 Agent 统一执行。

示例指令：

根据 review 结果拆分实现任务。
拆分规则：
1. 只能按文件或模块拆分；
2. 不允许两个 Impl agent 修改同一文件；
3. 如果某个问题跨多个模块，由主 agent 处理，不分派给 subagent；
4. 每个 Impl agent 只处理自己负责范围内的问题；
5. 每个 Impl agent 必须遵守最小变更原则；
6. 每个 Impl agent 不得重构无关代码；
7. 每个 Impl agent 完成后输出：
   - 修改文件；
   - 修复问题；
   - 关键改动；
   - 测试结果；
   - 未解决风险。
最后由主 agent：
1. 汇总所有 diff；
2. 检查是否有冲突；
3. 运行完整测试；
4. 做最终代码审查；
5. 输出最终变更报告。

⸻

更推荐的 Subagent 模式三：竞争性调试

当 bug 原因不明确时，可以让多个 subagent 并行提出假设。

Debug Agent A：从最近变更分析
Debug Agent B：从日志和异常栈分析
Debug Agent C：从数据流和状态机分析
Debug Agent D：从依赖版本和环境差异分析

要求：

1. 所有 debug agent 默认只读；
2. 每个 agent 必须输出根因假设；
3. 每个假设必须有证据；
4. 每个假设必须给出验证方法；
5. 不允许直接修改代码；
6. 主 agent 选择最可信假设后再修复。

示例指令：

请并行启动多个只读 debug subagent 分析该问题。
分工：
1. change-history-debugger：从最近代码变更和 diff 角度分析；
2. log-debugger：从日志、异常栈和错误信息角度分析；
3. dataflow-debugger：从输入、状态流转、边界条件角度分析；
4. dependency-debugger：从依赖版本、环境配置、构建差异角度分析。
每个 subagent 输出：
1. 最可能根因；
2. 支持证据；
3. 反证或不确定点；
4. 最小验证步骤；
5. 建议修复方向。
所有 subagent 禁止修改代码。
由主 agent 汇总后选择修复方案。

⸻

更推荐的 Subagent 模式四：大任务模块化实现

当任务天然可以拆成多个互不冲突的模块时，可以使用实现型 subagent。

适合：

1. 前端页面 A / 页面 B / 页面 C；
2. 后端 user / order / payment 模块；
3. Android 不同独立组件；
4. 文档不同章节；
5. 测试文件与业务文件分离；
6. 迁移脚本与业务代码分离。

不适合：

1. 同一个核心类需要多人修改；
2. 同一个配置文件需要多人修改；
3. 同一个数据库 schema 被多人同时改；
4. 一个全局类型定义被多人同时改；
5. 修改必须严格按顺序推演。

推荐指令：

请将该任务拆成多个文件级隔离的实现 subagent。
拆分要求：
1. 每个 subagent 负责一个明确模块；
2. 每个 subagent 必须声明可修改文件范围；
3. 不允许跨范围修改；
4. 遇到需要修改公共文件、全局类型、配置文件时，停止并交给主 agent；
5. 每个 subagent 完成后输出修改摘要；
6. 主 agent 最后统一处理公共文件、集成逻辑和测试。

⸻

Review 报告的正确格式

不要让 Review agent 只输出：

{
  "file": "auth.ts",
  "line": 42,
  "desc": "未校验 token 过期时间"
}

这种格式信息不足。

更推荐：

## Fix #1：Token 过期时间未校验
文件：src/auth/auth.ts
位置：
- 函数：checkToken()
- 代码锚点：`jwt.verify(token, secret)`
- 相关调用：`src/middleware/authMiddleware.ts` 中调用该函数
问题：
当前逻辑只验证 token 签名，没有显式处理 payload.exp 过期字段。
如果 jwt 库配置或调用方式变化，可能导致过期 token 被接受。
影响：
用户可能使用过期 token 访问受保护接口。
建议修复：
在解析 payload 后检查 `payload.exp`。
如果 `payload.exp <= Date.now() / 1000`，返回统一的认证失败结果。
注意事项：
1. 不要修改 authMiddleware 的响应格式；
2. 不要改变 catch 块的宽松错误处理；
3. 不要引入新的全局异常类型；
4. 保持现有测试兼容。
验收标准：
1. 新增过期 token 测试；
2. 原有无 token 测试通过；
3. 原有非法 token 测试通过；
4. 认证失败响应格式不变。

一个合格的 Review 输出必须包含：

字段	目的
文件	定位修改范围
函数/类/代码锚点	避免行号漂移
相关调用方	保留上下文
问题描述	说明为什么是问题
影响	判断优先级
建议修复	降低 Impl 二次猜测
禁止修改项	防止扩大范围
验收标准	保证可验证
风险等级	帮助排序

⸻

Impl Agent 的执行契约

Impl agent 不是自由发挥的开发者，而是受限执行者。

每个 Impl agent 必须遵守：

1. 只修改被分配的文件；
2. 只修复被分配的问题；
3. 不做顺手重构；
4. 不改公共 API，除非任务明确要求；
5. 不改无关格式；
6. 不引入不必要依赖；
7. 遇到跨文件依赖时停止并报告；
8. 遇到需求不清时输出阻塞点；
9. 修改后运行可用的最小测试；
10. 输出变更摘要和验证结果。

推荐 Impl 指令：

你是受限 Impl agent。
你的任务：
只修复下面 review item 中的问题。
允许修改范围：
- src/auth/auth.ts
- src/auth/auth.test.ts
禁止：
1. 修改 middleware 文件；
2. 修改接口响应格式；
3. 重构无关代码；
4. 引入新依赖；
5. 修改未授权文件；
6. 修复报告以外的问题。
执行要求：
1. 先读取允许范围内文件；
2. 根据 review item 制定最小修改方案；
3. 修改代码；
4. 补充或更新测试；
5. 运行相关测试；
6. 输出：
   - 修改了什么；
   - 为什么这样改；
   - 测试结果；
   - 是否存在未解决风险。

⸻

主 Agent 的职责

使用 subagent 时，主 agent 不能只是转发器。

主 agent 必须承担：

1. 拆分任务；
2. 定义边界；
3. 分配文件范围；
4. 汇总结果；
5. 去重冲突；
6. 判断优先级；
7. 决定是否修改；
8. 统一处理公共文件；
9. 统一运行测试；
10. 最终验收。

主 agent 不应该把最终判断完全交给 subagent。

尤其是以下事项必须由主 agent 控制：

1. 是否扩大修改范围；
2. 是否改公共 API；
3. 是否调整数据库 schema；
4. 是否修改全局配置；
5. 是否接受破坏性变更；
6. 是否忽略某个 review item；
7. 是否需要人工确认。

⸻

启动 Subagent 前检查清单

启动任何 subagent 前，先检查：

1. 这个任务是否真的需要 subagent？
2. 单 agent 是否已经足够？
3. 任务是否可以明确拆分？
4. 每个 subagent 的输入是否完整？
5. 每个 subagent 的输出是否可验证？
6. 是否存在多个 agent 修改同一文件？
7. 是否存在强串行依赖？
8. 是否会因为拆分造成上下文损耗？
9. 是否需要人工审核门控？
10. 主 agent 是否保留最终决策权？

如果以下任一条件成立，优先不要拆：

1. 任务很小；
2. 修改集中在同一文件；
3. 修复依赖完整上下文；
4. agent 之间必须严格串行；
5. review 输出不足以直接指导修改；
6. 只是为了“看起来更工程化”而拆分。

⸻

Subagent 完成后检查清单

每个 subagent 完成后，主 agent 必须检查：

1. 是否越权修改了文件？
2. 是否修改了未授权范围？
3. 是否做了无关重构？
4. 是否引入新依赖？
5. 是否改变公共 API？
6. 是否破坏原有行为？
7. 是否有测试结果？
8. 是否存在未解决风险？
9. 多个 subagent 的 diff 是否冲突？
10. 最终结果是否通过统一测试？

对于 Review agent，还要检查：

1. 是否给出可执行修复建议？
2. 是否只给了行号，没有代码锚点？
3. 是否缺少调用链上下文？
4. 是否缺少影响说明？
5. 是否缺少验证方式？
6. 是否存在重复问题？
7. 是否存在泛泛建议？

对于 Impl agent，还要检查：

1. 是否严格按照 review item 修改？
2. 是否扩大了修复范围？
3. 是否跳过了测试？
4. 是否留下 TODO？
5. 是否改变了不该改变的行为？
6. 是否需要主 agent 进一步集成？

⸻

Claude Code 中的推荐用法

在 Claude Code 中，推荐将 subagent 用于：

1. 并行代码审查；
2. 多角度 bug 分析；
3. 大代码库探索；
4. 独立模块实现；
5. 测试补齐；
6. 文档审查；
7. 迁移影响分析。

不推荐用于：

1. 小任务机械拆分；
2. 同文件多人修改；
3. Review JSON → Impl 的低上下文流水线；
4. 强依赖串行任务；
5. 主 agent 不做最终验收的自动化修复。

Claude Code 中可以这样使用：

请启动多个 subagent 并行分析，但所有 subagent 只读。
主 agent 负责最终修改。
分工：
1. security-reviewer；
2. bug-reviewer；
3. test-reviewer；
4. architecture-reviewer。
要求：
1. subagent 不得修改文件；
2. 输出必须包含代码锚点和验证方式；
3. 主 agent 汇总后先给修复计划；
4. 由主 agent 执行最终修改和测试。

如果要让 subagent 修改代码：

请只在文件级隔离明确时启动 Impl subagent。
规则：
1. 每个 Impl subagent 必须声明允许修改文件；
2. 不允许多个 Impl subagent 修改同一文件；
3. 公共文件、配置文件、schema 文件由主 agent 修改；
4. 所有 subagent 完成后，主 agent 统一 review diff 并运行测试。

⸻

Codex 中的推荐用法

在 Codex 中，subagent 更适合显式调用，而不是默认自动拆分。

推荐用于：

1. 大型任务并行探索；
2. 多个实现方案比较；
3. 多角度 review；
4. 独立模块实现；
5. 测试生成；
6. 迁移影响评估。

Codex 中尤其要注意：

1. subagent 会消耗更多 token；
2. subagent 不应被用于小任务；
3. 需要明确告诉 Codex 何时使用 subagent；
4. 需要明确每个 subagent 的权限边界；
5. 需要由主 agent 统一合并和验证。

推荐指令：

Use subagents only where the work can be parallelized or isolated.
Start read-only subagents for:
1. security review;
2. bug review;
3. test gap analysis;
4. architecture risk analysis.
Do not allow subagents to edit files during review.
After all subagents finish:
1. deduplicate findings;
2. rank by risk;
3. produce a repair plan;
4. apply changes in the main agent unless files can be safely isolated;
5. run tests;
6. summarize remaining risks.

如果要并行实现：

Use implementation subagents only with file-level isolation.
Rules:
1. no two subagents may edit the same file;
2. each subagent must receive explicit allowed files;
3. each subagent must make minimal changes;
4. shared files are reserved for the main agent;
5. each subagent must report changes and tests;
6. the main agent must review all diffs and run the final test suite.

⸻

推荐工作流

工作流一：中小任务

主 Agent 单独完成：
读代码 → review → plan → fix → test → summary

适合：

1. 修改范围小；
2. 文件数量少；
3. 同一模块内聚；
4. 需要完整上下文；
5. 没有人工审核门控。

⸻

工作流二：中大型 Review

主 Agent
↓
并行只读 Review Agent
↓
汇总问题
↓
人工或主 Agent 决策
↓
主 Agent 修改
↓
测试

适合：

1. 代码量大；
2. 需要多维度审查；
3. 需要安全/性能/测试/架构分工；
4. review 结果可能需要人工确认。

⸻

工作流三：大型模块化实现

主 Agent 拆分任务
↓
Impl-A 负责模块 A
Impl-B 负责模块 B
Impl-C 负责模块 C
↓
主 Agent 统一集成
↓
测试
↓
总结

适合：

1. 模块边界清晰；
2. 文件范围不重叠；
3. 每个模块可以独立完成；
4. 主 agent 能统一集成。

⸻

工作流四：复杂 Bug 定位

主 Agent
↓
多个只读 Debug Agent 并行提出根因假设
↓
主 Agent 汇总证据
↓
选择最可信假设
↓
主 Agent 修复
↓
回归测试

适合：

1. bug 原因不明确；
2. 日志、代码、数据流、依赖都可能相关；
3. 需要多角度竞争性分析。

⸻

最终原则

可以把 Claude Code / Codex 中的 subagent 使用压缩成下面几条原则：

并行 review，可以拆。
串行修复，慎重拆。
同文件修改，不要拆。
跨模块独立实现，可以拆。
中小任务，单 agent 通常更好。
只读 subagent 最安全。
实现型 subagent 必须文件级隔离。
Review 输出必须可执行，不能只是摘要。
主 agent 必须保留最终决策权。
所有 subagent 结果必须统一验收。

最重要的一条是：

subagent 的目标不是增加 agent 数量，而是降低任务复杂度。
如果拆分后上下文损耗、冲突风险和协调成本高于收益，就不应该拆。