# Subagent 编排系统性方案（最终版）

Version: 2.0

定位：

本文档定义主 Agent 如何安全、高效地委派 Subagent。

核心目标：

* 防止越界执行
* 提高任务完成率
* 降低上下文污染
* 支持大规模 Agent 协同
* 保持主 Agent 的最终决策权

⸻

一、设计原则

Principle 1：目标驱动，而非步骤驱动

主 Agent 应描述：

* 目标
* 边界
* 验收标准

而非微观控制执行过程。

正确：

修复 login 模块 3 个失败测试

错误：

先打开文件A，再修改第23行，然后运行测试…

⸻

Principle 2：约束危险操作，而非约束所有行为

不要试图控制 Subagent 的每一步。

只限制：

* 写入范围
* 高风险操作
* 架构决策
* 资源权限

允许其自主探索解决方案。

⸻

Principle 3：默认信任，保留裁决权

Subagent 负责执行。

主 Agent 负责：

* 决策
* 审核
* 集成
* 仲裁冲突

⸻

Principle 4：小任务优先

优先：

* 单目标
* 单模块
* 单责任

避免：

* 超大任务
* 多目标任务
* 跨领域任务

⸻

二、五层约束模型

⸻

Layer 1：权限边界（Permission Boundary）

定义 Subagent 能做什么。

Read Only

允许：

* 搜索
* 阅读
* 分析
* 评审

禁止：

* 修改文件
* 提交代码
* 删除资源

适用：

* Research
* Review
* Audit

⸻

Limited Write

允许：

* 修改白名单文件

禁止：

* 修改范围外文件

适用：

* Bug Fix
* Feature Implement

⸻

Full Write

允许：

* 指定目录内自由修改

适用：

* 独立模块开发

仅在高信任任务使用。

⸻

Layer 2：作用域边界（Scope Boundary）

⸻

文件范围

示例：

允许：
src/auth/**
禁止：
src/payment/**

⸻

功能范围

正确：

修复 Token 过期判断逻辑

错误：

顺便优化整个认证模块

⸻

决策范围

Subagent 不得自主：

* 修改架构
* 修改公共 API
* 修改数据库 Schema

除非明确授权。

⸻

Layer 3：任务契约（Task Contract）

定义任务目标。

⸻

Goal

必须具体。

错误：

优化系统

正确：

修复 login.test.ts 中 3 个失败测试

⸻

Context

仅提供必要上下文。

推荐：

BRIEF_FILE
ERROR_LOG
相关文件

避免：

整个项目背景
全部会话历史

⸻

Definition of Done

必须明确。

示例：

- 3 个测试全部通过
- 无新增失败测试
- 修改限制在白名单范围

⸻

STOP 协议（必须）

Subagent 必须在以下情况停止：

BLOCKED

无法继续。

例如：

* 文件不存在
* 编译失败
* 权限不足

⸻

NEEDS_CONTEXT

缺少信息。

例如：

* 需求不完整
* 找不到相关代码

⸻

NEEDS_DECISION

需要主 Agent 决策。

例如：

* 两种架构方案均可
* 需要修改公共接口
* 根因超出授权范围，需主 Agent 裁定：扩大 SCOPE 或调整方案

⸻

DONE_WITH_CONCERNS

已完成但存在风险。

例如：

* 临时方案
* 技术债
* 潜在副作用

⸻

Layer 4：输出契约（Output Contract）

⸻

所有 Subagent 返回：

STATUS:
DONE | BLOCKED | NEEDS_CONTEXT | NEEDS_DECISION | DONE_WITH_CONCERNS
SUMMARY:
...
FILES_CHANGED:
...
TEST_RESULTS:
...
RISKS:
...

⸻

Read Only Agent

返回：

发现
分析
建议

不得提交实现代码。

⸻

Implement Agent

必须返回：

修改内容
测试结果
风险说明

⸻

Layer 5：协调规则（Coordination Rules）

⸻

主 Agent 保留最终裁决权

Subagent 不得：

* 自行扩大范围
* 自行修改目标
* 自行决定架构方向

⸻

冲突处理原则

若多个 Subagent 修改同一文件：

Step1

检测冲突

FILE_OVERLAP
DIFF_OVERLAP
API_OVERLAP

⸻

Step2

主 Agent 仲裁

选择：

A优先
B优先
手工合并
重新派发

⸻

Step3

重新验证

测试
Lint
类型检查

⸻

并发限制

建议：

任务规模	最大并发
小项目	3
中项目	5
大项目	8

超过后采用 Wave 调度。

⸻

三、角色模型

⸻

Analyst

职责：

研究
分析
定位问题

权限：

Read Only

输出：

报告
建议

⸻

Implementer

职责：

修复
开发
重构

权限：

Limited Write

⸻

Reviewer

职责：

代码审查
架构审查
质量审查

权限：

Read Only

⸻

四、Dispatch Gate（启动门）

⸻

成熟 Agent 编排系统（Codex、Claude Code、Google Borg、Kubernetes Controller、航空检查单）的共同特点：关键节点 Checklist 化。导致 Agent 失控的，往往不是规则不存在，而是规则没有被执行。

Dispatch Gate 是主 Agent 的责任。目的：判断任务是否具备派发条件。在创建 Subagent 之前，必须通过以下检查。

⸻

Goal Check（目标检查）

[ ] 目标是否明确
[ ] 是否只有一个主要目标
[ ] 是否可验证完成
[ ] 目标是否具体可衡量

⸻

Scope Check（范围检查）

[ ] 是否定义允许修改范围
[ ] 是否定义禁止修改范围
[ ] 是否避免跨模块任务

⸻

Context Check（上下文检查）

[ ] 是否提供必要上下文
[ ] 是否提供相关文件
[ ] 是否提供错误信息
[ ] 是否提供项目规范入口（AGENTS.md / RULES.md）

⸻

Contract Check（契约检查）

[ ] 是否指定角色
[ ] 是否定义 DoD
[ ] 是否定义 STOP 条件
[ ] 是否定义输出格式

⸻

Coordination Check（协调检查）

[ ] 是否与其它 Subagent 冲突
[ ] 是否存在文件重叠
[ ] 是否存在职责重叠
[ ] 是否存在并发冲突风险

⸻

若任何一项检查失败：DO NOT DISPATCH。

⸻

五、派发模板

⸻

Template A：单任务派发

ROLE:
Implementer
GOAL:
[具体目标]
CONTEXT:
- BRIEF_FILE
- ERROR_LOG
- 相关文件
SCOPE:
允许修改：
...
禁止修改：
...
DOD:
...
STOP_RULE:
BLOCKED
NEEDS_CONTEXT
NEEDS_DECISION
DONE_WITH_CONCERNS
OUTPUT:
标准输出格式

⸻

六、并行协调

⸻

并行派发模板

主任务
├─ Subagent A
├─ Subagent B
├─ Subagent C

要求：

文件范围互斥
职责互斥
输出独立

⸻

并行集成流程

Dispatch
↓
Execute
↓
Collect
↓
Conflict Detection
↓
Merge
↓
Validation
↓
Review
↓
Accept

⸻

七、质量模式

注意：

质量模式不是编排模式。

⸻

Standard

普通实现。

⸻

TDD

流程：

RED
↓
GREEN
↓
REFACTOR

约束：

RED

允许：

编写失败测试

禁止：

实现业务逻辑

⸻

GREEN

允许：

最小实现

禁止：

额外功能

⸻

REFACTOR

允许：

优化实现

禁止：

改变外部行为

⸻

八、执行生命周期

⸻

完整生命周期：

Dispatch Gate
↓
Dispatch
↓
Execute
↓
Review
↓
Acceptance Gate
├─ PASS → Accept
└─ FAIL → Reject
             ↓
          Rework
             ↓
       Re-dispatch

⸻

1. Dispatch Gate

派发前强制检查。未通过不得派发。详见第四章 Dispatch Gate。

⸻

2. Dispatch

创建任务，派发至 Subagent。

⸻

3. Execute

Subagent 执行任务。

⸻

4. Review

主 Agent 审查结果。

检查：

目标是否达成
范围是否越界
测试是否通过

⸻

5. Acceptance Gate

验收门检查。详见第九章 Acceptance Gate。

PASS：结果被接受，进入 Accept。
FAIL：结果被拒绝，进入 Reject。

⸻

6. Accept

通过验收，进入集成。

⸻

7. Reject → Rework → Re-dispatch

验收失败时：

Reject：拒绝当前结果。
Rework：分析失败原因，调整任务定义。
Re-dispatch：重新通过 Dispatch Gate 派发。

形成完整闭环，确保质量问题不会被遗漏。

⸻

8. Ledger

记录：

任务ID
目标
执行人
结果
时间

作用：

防止重复派发
防止重复修复
防止上下文压缩丢失

⸻

九、Acceptance Gate（验收门）

⸻

Acceptance Gate 是 Subagent 编排中最关键的控制点。

Subagent 完成 ≠ 任务完成。

在接受结果之前必须通过以下验证。

⸻

Goal Validation（目标验证）

[ ] 原目标是否完成
[ ] 是否满足 DoD
[ ] 是否产生额外未授权功能
[ ] 返回格式是否正确

⸻

Scope Validation（范围验证）

[ ] 是否超出授权范围
[ ] 是否修改禁止区域
[ ] 是否新增未授权文件
[ ] 修改范围是否合法

⸻

Quality Validation（质量验证）

[ ] 测试通过
[ ] Lint 通过
[ ] 类型检查通过
[ ] 构建通过

⸻

Safety Validation（安全验证）

[ ] 无敏感信息泄漏
[ ] 无危险操作
[ ] 无破坏性修改

⸻

Integration Validation（集成验证）

[ ] 无冲突
[ ] 能正常合并
[ ] 不影响其它模块
[ ] 已完成 Review

⸻

若任何一项验证失败：

REJECT

进入 Rework → Re-dispatch 闭环。

⸻

十、常见反模式

❌ 修复所有问题

✅ 修复 login.test.ts 中 3 个失败测试

⸻

❌ 优化整个模块

✅ 优化 cache.ts，禁止修改公共 API

⸻

❌ 遇到问题自行猜测

✅ 返回 NEEDS_CONTEXT

⸻

❌ 顺手修改无关文件

✅ 严格遵守白名单范围

⸻

十一、检查清单汇总

⸻

Dispatch Gate（派发前）

[ ] 目标是否具体可衡量
[ ] 是否指定角色
[ ] 是否定义文件范围
[ ] 是否定义 DoD
[ ] 是否定义 STOP 条件
[ ] 是否提供必要上下文
[ ] 是否提供项目规范入口
    (AGENTS.md / RULES.md)
[ ] 是否存在并发冲突风险

⸻

Acceptance Gate（派发后）

[ ] 返回格式正确
[ ] 修改范围合法
[ ] 测试通过
[ ] Lint通过
[ ] 类型检查通过
[ ] 无冲突
[ ] 已完成 Review
[ ] 已记录 Ledger

⸻

十二、核心结论

Subagent 编排的本质不是控制执行过程，而是建立：

权限边界
↓
作用域边界
↓
任务契约
↓
输出契约
↓
协调规则

Dispatch Gate
↓
Acceptance Gate

最终原则：

用原则指导，而非用规则束缚。

信任 Subagent 的执行能力，
保留主 Agent 的最终决策权。
