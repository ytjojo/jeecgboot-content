Subagent 编排系统性方案

Version: 2.0

⸻

文档目标

本文档定义主 Agent 如何安全、高效地派发和管理 Subagent。

核心目标：

* 防止越界执行
* 提高任务完成率
* 降低上下文污染
* 提升并行协作效率
* 保持主 Agent 的最终决策权
* 建立可审计、可扩展的 Agent 治理体系

适用于：

* Claude Code
* Codex
* OpenAI Agents
* Cursor Agent
* Trae Agent
* Roo Code
* Continue
* 自研 Agent 系统

⸻

一、核心设计原则

Principle 1：目标驱动，而非步骤驱动

主 Agent 应描述：

* 目标（Goal）
* 边界（Boundary）
* 验收标准（DoD）

避免描述具体执行过程。

正确

修复 login 模块中 3 个失败测试

错误

先打开A文件
然后修改第23行
再运行测试

⸻

Principle 2：约束危险行为，而非约束所有行为

不要控制 Subagent 的每一步。

只约束：

* 写入权限
* 修改范围
* 高风险操作
* 架构决策
* 资源访问

允许其自主探索实现路径。

⸻

Principle 3：默认信任，保留裁决权

Subagent 负责：

执行
分析
实现
审查

主 Agent 负责：

决策
协调
审核
集成
验收

⸻

Principle 4：小任务优先

优先：

单目标
单模块
单责任

避免：

超大任务
多目标任务
跨领域任务

⸻

Principle 5：所有任务必须可验证

禁止：

优化系统
提升质量
改进架构

允许：

修复3个失败测试
消除2个Lint错误
实现指定API

⸻

二、五层约束模型

⸻

Layer 1：权限边界（Permission Boundary）

定义 Subagent 能做什么。

Read Only

允许：

搜索
阅读
分析
审查

禁止：

修改文件
提交代码
删除资源

适用：

Research
Audit
Review

⸻

Limited Write

允许：

修改授权文件
创建授权文件

禁止：

修改授权范围外文件

适用：

Bug Fix
Feature Development
Refactor

⸻

Full Write

允许：

指定目录内自由修改

适用：

独立模块开发
沙箱环境开发

仅在高信任场景使用。

⸻

Layer 2：作用域边界（Scope Boundary）

⸻

文件范围

允许：
src/auth/**
禁止：
src/payment/**

⸻

功能范围

正确

修复 Token 过期判断逻辑

错误

顺便优化整个认证系统

⸻

决策范围

Subagent 不得自主：

修改架构
修改数据库Schema
修改公共API
修改服务边界

除非明确授权。

⸻

Layer 3：任务契约（Task Contract）

⸻

Goal

必须明确。

错误

优化项目

正确

修复 login.test.ts 中 3 个失败测试

⸻

Context

仅提供必要上下文。

推荐：

错误日志
相关文件
需求文档
接口定义

避免：

全部会话历史
整个项目介绍
无关上下文

⸻

Definition of Done

必须明确。

- 3个测试全部通过
- 无新增失败测试
- 修改范围符合授权

⸻

STOP 协议（强制）

出现以下情况必须停止执行。

BLOCKED

无法继续执行。

例如：

文件不存在
编译失败
权限不足

⸻

NEEDS_CONTEXT

缺少必要信息。

例如：

需求不完整
找不到关键代码
上下文缺失

⸻

NEEDS_DECISION

需要主 Agent 决策。

例如：

存在多个可行方案
涉及架构变更
涉及公共API变更

⸻

DONE_WITH_CONCERNS

任务完成但存在风险。

例如：

临时方案
技术债
潜在副作用

⸻

Layer 4：输出契约（Output Contract）

所有 Subagent 必须返回统一格式。

STATUS:
DONE
BLOCKED
NEEDS_CONTEXT
NEEDS_DECISION
DONE_WITH_CONCERNS
SUMMARY:
...
FILES_CHANGED:
...
TEST_RESULTS:
...
RISKS:
...

⸻

Research Agent

输出：

发现
分析
建议

禁止：

提交实现代码

⸻

Implement Agent

输出：

修改内容
测试结果
风险说明

⸻

Review Agent

输出：

问题列表
风险等级
修复建议

⸻

Layer 5：协调规则（Coordination Rules）

⸻

主 Agent 保留最终裁决权

Subagent 不得：

扩大任务范围
修改任务目标
改变架构方向

⸻

冲突处理机制

Step 1：检测冲突

FILE_OVERLAP
DIFF_OVERLAP
API_OVERLAP

⸻

Step 2：主 Agent 仲裁

A优先
B优先
手工合并
重新派发

⸻

Step 3：重新验证

测试
Lint
类型检查
构建检查

⸻

并发限制建议

项目规模	最大并发
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
输出方案

权限：

Read Only

⸻

Implementer

职责：

实现
修复
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

Coordinator（可选）

职责：

任务拆分
结果汇总
冲突协调

权限：

Read Only

⸻

四、Dispatch Gate（启动前检查）

在创建 Subagent 之前必须通过。

⸻

Goal Check

[ ] 目标明确
[ ] 目标可验证
[ ] 只有一个主要目标

⸻

Scope Check

[ ] 定义允许修改范围
[ ] 定义禁止修改范围
[ ] 无跨模块污染

⸻

Context Check

[ ] 提供必要上下文
[ ] 提供相关文件
[ ] 提供错误信息

⸻

Contract Check

[ ] 定义DoD
[ ] 定义STOP规则
[ ] 定义输出格式

⸻

Coordination Check

[ ] 无职责冲突
[ ] 无文件冲突
[ ] 无资源冲突

⸻

Dispatch Gate 结果

若任何检查失败：

DO NOT DISPATCH

⸻

五、标准派发模板

单任务模板

ROLE:
Implementer
GOAL:
[具体目标]
CONTEXT:
- 需求文档
- 错误日志
- 相关文件
PERMISSION:
Limited Write
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

六、并行派发模板

主任务
├── Subagent A
├── Subagent B
├── Subagent C

要求：

文件范围互斥
职责互斥
输出独立

⸻

七、执行生命周期

Task
↓
Dispatch Gate
↓
Dispatch
↓
Execute
↓
Collect
↓
Review
↓
Acceptance Gate
├── PASS → Accept
└── FAIL → Reject
               ↓
            Rework
               ↓
         Re-dispatch
↓
Ledger

⸻

八、Acceptance Gate（完成后检查）

Subagent 完成不等于任务完成。

必须通过 Acceptance Gate。

⸻

Goal Validation

[ ] 原目标已完成
[ ] 满足DoD
[ ] 无额外未授权功能

⸻

Scope Validation

[ ] 未超出授权范围
[ ] 未修改禁止区域
[ ] 未新增未授权文件

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

[ ] 可正常合并
[ ] 无冲突
[ ] 不影响其它模块

⸻

Acceptance Gate 结果

全部通过：

PASS

否则：

REJECT

进入：

Rework

⸻

九、质量模式（Quality Mode）

质量模式属于执行策略。

不是编排模式。

⸻

Standard Mode

普通实现模式。

⸻

TDD Mode

执行流程：

RED
↓
GREEN
↓
REFACTOR

⸻

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

额外功能开发

⸻

REFACTOR

允许：

优化实现

禁止：

改变外部行为

⸻

十、任务账本（Ledger）

所有任务应记录：

任务ID
目标
角色
执行时间
状态
结果
风险

作用：

防止重复派发
防止重复修复
支持审计追踪
支持上下文压缩恢复

⸻

十一、反模式（Anti-Patterns）

⸻

反模式 1

❌

修复所有问题

✅

修复 login.test.ts 中3个失败测试

⸻

反模式 2

❌

优化整个模块

✅

优化 cache.ts
禁止修改公共API

⸻

反模式 3

❌

遇到问题自行猜测

✅

返回 NEEDS_CONTEXT

⸻

反模式 4

❌

顺手修改无关文件

✅

严格遵守授权范围

⸻

反模式 5

❌

多个Subagent修改同一文件

✅

文件范围互斥

⸻

十二、总结

Subagent 编排的核心不是控制执行过程。

而是建立：

权限边界
↓
作用域边界
↓
任务契约
↓
输出契约
↓
协调规则
↓
启动门（Dispatch Gate）
↓
执行生命周期
↓
验收门（Acceptance Gate）
↓
任务账本（Ledger）

最终原则：

用原则指导执行
用约束控制风险
用检查单保证质量
用主Agent保留最终决策权