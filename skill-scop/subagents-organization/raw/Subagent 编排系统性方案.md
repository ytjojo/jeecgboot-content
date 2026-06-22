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

三、Subagent 角色模型

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

四、标准派发模板

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

五、并行派发模板

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

六、质量模式（Quality Mode）

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

七、派发后生命周期

⸻

1. Dispatch

创建任务

⸻

2. Execute

Subagent 执行

⸻

3. Review

主 Agent 审查

检查：

目标是否达成
范围是否越界
测试是否通过

⸻

4. Rework

不通过：

重新派发

⸻

5. Accept

通过：

进入集成

⸻

6. Ledger

记录：

任务ID
目标
执行人
结果
时间

作用：

防止：

重复派发
重复修复
上下文压缩丢失

⸻

八、派发检查清单

派发前

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

派发后

[ ] 返回格式正确
[ ] 修改范围合法
[ ] 测试通过
[ ] Lint通过
[ ] 类型检查通过
[ ] 无冲突
[ ] 已完成 Review
[ ] 已记录 Ledger

⸻

九、常见反模式

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

十、核心结论

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

最终原则：

用原则指导，而非用规则束缚。

信任 Subagent 的执行能力，
保留主 Agent 的最终决策权。




有必要，而且我认为应该保留。

但需要升级一下定位。

很多 Agent 编排文档的问题是：

* 只有原则（Policy）
* 只有约束（Constraint）
* 只有模板（Template）

缺少：

* 执行前 Gate
* 执行后 Gate

而成熟系统（Codex、Claude Code、Google Borg、Kubernetes Controller、航空检查单）都有一个共同特点：

关键节点 Checklist 化

因为真正导致 Agent 失控的，不是规则不存在，而是规则没有被执行。

⸻

建议保留，但升级名称

不要叫：

派发前检查
派发后检查

更专业的是：

Dispatch Gate
Acceptance Gate

即：

启动门
验收门

这样就形成完整生命周期：

Task
↓
Dispatch Gate
↓
Dispatch
↓
Execute
↓
Review
↓
Acceptance Gate
↓
Accept
↓
Ledger

⸻

Dispatch Gate（启动前检查）

这是主 Agent 的责任。

目的：

判断任务是否具备派发条件。

⸻

建议加入文档：

Dispatch Gate

在创建 Subagent 之前，必须通过以下检查。

Goal Check

[ ] 目标是否明确
[ ] 是否只有一个主要目标
[ ] 是否可验证完成

⸻

Scope Check

[ ] 是否定义允许修改范围
[ ] 是否定义禁止修改范围
[ ] 是否避免跨模块任务

⸻

Context Check

[ ] 是否提供必要上下文
[ ] 是否提供相关文件
[ ] 是否提供错误信息

⸻

Contract Check

[ ] 是否定义 DoD
[ ] 是否定义 STOP 条件
[ ] 是否定义输出格式

⸻

Coordination Check

[ ] 是否与其它 Subagent 冲突
[ ] 是否存在文件重叠
[ ] 是否存在职责重叠

⸻

如果有任何一项失败：

DO NOT DISPATCH

⸻

Acceptance Gate（完成后检查）

这个更重要。

很多 Agent 系统失败就在这里。

Subagent 完成 ≠ 任务完成

必须经过 Acceptance Gate。

⸻

Acceptance Gate

在接受结果之前必须通过。

⸻

Goal Validation

[ ] 原目标是否完成
[ ] 是否满足 DoD
[ ] 是否产生额外未授权功能

⸻

Scope Validation

[ ] 是否超出授权范围
[ ] 是否修改禁止区域
[ ] 是否新增未授权文件

⸻

Quality Validation

[ ] 测试通过
[ ] Lint通过
[ ] 类型检查通过
[ ] 构建通过

⸻

Safety Validation

[ ] 无敏感信息泄漏
[ ] 无危险操作
[ ] 无破坏性修改

⸻

Integration Validation

[ ] 无冲突
[ ] 能正常合并
[ ] 不影响其它模块

⸻

若失败：

REJECT

进入：

Rework

⸻

更进一步：加入 Reject 机制

这是原文还缺的一块。

生命周期建议改为：

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

这样就形成真正闭环。

⸻

最终建议

我会把文档结构调整成：

1. Principles
2. Five-Layer Constraint Model
3. Role Model
4. Dispatch Gate      ← 新增
5. Dispatch Templates
6. Execution Lifecycle
7. Acceptance Gate    ← 新增
8. Parallel Coordination
9. Quality Modes
10. Anti-Patterns
11. Checklist Summary

这样逻辑上就变成：

约束体系
    ↓
启动检查
    ↓
任务派发
    ↓
执行
    ↓
验收检查
    ↓
接受/拒绝
    ↓
归档

对于 Claude Code、Codex、Cursor Agent、OpenAI Agents 这类生产级 Agent 编排体系来说，Dispatch Gate 和 Acceptance Gate 的价值实际上高于大部分规则本身，因为它们把“原则”变成了真正会被执行的控制点。