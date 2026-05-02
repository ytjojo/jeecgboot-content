# 内容社区用户域 - 成长处罚真实执行引擎设计

## 1. 背景

当前 `content/user` 模块已经完成两段关键基础能力：

- 成长处罚恢复编排已经落地，可在申诉通过和治理到期自动恢复时回补积分、成长值、等级、勋章状态以及首批等级权益
- 成长处罚来源建模已经落地，`governance/status/change` 与 `support/admin/report/handle` 可以统一创建成长处罚主记录，并补齐来源字段与幂等依据

但当前链路仍然存在一个核心缺口：

- 处罚发生时虽然已经可以“建档”
- 恢复发生时虽然已经可以“回放”
- 但处罚发生本身还没有成为一套真实执行引擎

也就是说，现在的 `ContentUserGrowthPenaltyRecord` 更多仍是“处罚预留记录”：

- `effect_snapshot_json` 仍以计划占位为主
- 处罚发生时不会真正扣积分
- 不会真正扣成长值
- 不会真正重算等级
- 不会真正回收/隐藏勋章
- 不会真正禁用首批等级权益

这会导致：

- 恢复链路的“回放依据”与真实处罚行为之间仍有缝隙
- 成长处罚记录不能完整表达一次处罚究竟执行了什么
- 后续扩更多等级权益消费方时，处罚与恢复仍不完全闭环

因此，本轮需要把“来源建档”推进为“真实处罚执行引擎”，形成：

- 处罚发生时可执行
- 处罚记录可追溯
- 恢复时可回放
- 同一来源可幂等

的完整闭环。

## 2. 目标

在 `jeecg-module-content` 模块内，将现有成长处罚建档服务升级为真实执行编排服务，使治理处罚入口与举报处理入口在命中处罚条件时，能够完成以下真实处罚动作：

- 扣积分
- 扣成长值
- 重算等级
- 回收或隐藏勋章
- 禁用首批等级权益

并把真实执行结果写回成长处罚主记录与等级权益子表，供后续恢复服务稳定回放。

本轮目标如下：

- 保留当前 `ContentUserGrowthPenaltyRecord` 作为唯一处罚主记录
- 保留等级权益独立子表方案，不回退到纯快照法
- 将 `IContentUserGrowthPenaltyRecordService` 从“建档服务”升级为“建档 + 执行编排服务”
- 将 `effect_snapshot_json` 从计划快照升级为真实执行结果快照
- 为执行过程补齐幂等、事务与冲突保护
- 保证现有恢复服务无需推翻重做，只需消费更完整的快照

## 3. 范围

### 3.1 包含

- 升级成长处罚建档服务为真实执行编排服务
- 在治理处罚入口执行积分、成长值、等级、勋章、首批等级权益处罚
- 在举报处理入口执行积分、成长值、等级、勋章、首批等级权益处罚
- 将真实执行结果写入 `effect_snapshot_json`
- 在首批等级权益处罚时写入 `content_user_level_benefit_penalty_record`
- 为执行和恢复闭环补充测试
- 更新覆盖报告与阶段计划文档

### 3.2 不包含

- 不改基础库和公共 starter
- 不新增新的处罚主表
- 不新拆独立处罚中心模块
- 不一次性扩展更多等级权益消费方
- 不改前端接口
- 不重构现有 `growth/support/governance` 全部分层

## 4. 约束

- 代码必须符合当前仓库阿里巴巴规范要求
- 仅允许在 `jeecg-module-content` 中做增量实现
- 继续使用 Spring Boot 3、JeecgBoot、MyBatis-Plus、Flyway SQL、`Result<T>` 现有风格
- 执行与恢复必须保持可幂等，避免重复扣减或重复恢复
- 执行与主记录落库必须保持事务一致，避免半成品处罚记录
- 设计优先保证“强一致、可回放、可审计”，其次才是抽象优雅

## 5. 方案选择

### 5.1 方案 A：入口内直接执行处罚

做法：

- 在 `ContentUserGovernanceServiceImpl.changeStatus(...)` 中直接执行积分、成长值、勋章和等级权益处罚
- 在 `ContentUserSupportServiceImpl.handleReport(...)` 中直接执行同一套处罚逻辑

优点：

- 表面改动最少
- 少一个中间服务

缺点：

- 处罚逻辑散落在两个入口
- 幂等、快照、事务边界会被复制
- 与现有恢复服务边界不对称

### 5.2 方案 B：升级现有建档服务为“建档 + 执行编排服务”

做法：

- 继续保留 `IContentUserGrowthPenaltyRecordService`
- 在该服务内统一完成来源判定、幂等校验、真实处罚执行、快照落库
- 入口层只负责“是否需要处罚”的业务判定和上下文传入

优点：

- 与现有恢复服务前后对称
- 不额外引入新的抽象层
- 延续当前最小增量实现路径
- 方便后续继续扩更多处罚项

缺点：

- 服务内部职责会从“建档”升级为“编排”
- 本轮实现量略高于纯建档

### 5.3 方案 C：新增独立处罚执行服务

做法：

- `RecordService` 只负责创建主记录
- 再新增 `PenaltyExecutionService` 专门执行处罚

优点：

- 分层最清楚
- 长期演进最工整

缺点：

- 对当前阶段偏重
- 文件与装配会明显增加
- 容易把本轮范围做大

### 5.4 结论

本次采用方案 B：升级现有 `IContentUserGrowthPenaltyRecordService` 为“建档 + 真实处罚执行编排服务”。

原因：

- 最贴合当前已完成的来源建模扩展
- 与 `IContentUserGrowthPenaltyRecoveryService` 形成前后对称结构
- 不需要再引入一层独立处罚中心
- 能在当前工作树与模块边界下保持最小增量

## 6. 组件设计

### 6.1 主记录继续保留

继续使用：

- `content_user_growth_penalty_record`
- `ContentUserGrowthPenaltyRecord`

不新增新的处罚主表。

当前已存在的来源字段继续保留并继续使用：

- `source_type`
- `source_id`
- `source_status`
- `governance_record_id`

当前已存在的恢复字段继续保留并继续使用：

- `status`
- `recover_trigger`
- `recover_reason`
- `recovered_by`
- `recovered_at`

### 6.2 等级权益继续保留独立子表方案

等级权益处罚与恢复继续沿用已经确认并落地的独立子表法：

- 主表记录一次处罚事件
- `content_user_level_benefit_penalty_record` 记录本次处罚影响到的等级权益项

本轮不把等级权益重新并回纯快照法。

### 6.3 服务边界

本轮完成后，服务职责如下：

- `IContentUserGrowthPenaltyRecordService`
  - 负责处罚来源判定后的真实处罚执行
  - 负责处罚幂等校验
  - 负责真实执行结果快照落库
  - 负责等级权益子记录写入
- `IContentUserGrowthPenaltyRecoveryService`
  - 负责处罚解除后的恢复回放
  - 负责积分、成长值、等级、勋章、等级权益恢复

这样可以保持清晰的前后对称：

- 处罚发生时由 `RecordService` 执行
- 处罚解除时由 `RecoveryService` 恢复

## 7. 入口编排

### 7.1 治理状态变更入口

位置：

- `ContentUserGovernanceServiceImpl.changeStatus(...)`

顺序：

1. 校验状态流转
2. 写 `ContentUserStatusRecord`
3. 更新 `profile.status`
4. 调用成长处罚执行服务
5. 写治理审计日志

入口职责：

- 只负责判断目标状态是否属于处罚态
- 只负责传递治理记录、规则编码、操作者、原因等上下文
- 不负责自己拼装积分或勋章处罚细节

首批处罚态继续限定：

- `MUTED`
- `RECOMMENDATION_LIMITED`
- `FROZEN`
- `BANNED`

### 7.2 举报处理入口

位置：

- `ContentUserSupportServiceImpl.handleReport(...)`

顺序：

1. 校验举报处理状态
2. 更新 `ContentUserReport`
3. 调用成长处罚执行服务
4. 写举报处理审计日志

入口职责：

- 只负责判断举报处理结果是否属于处罚性结果
- 只负责传递举报记录、处理结果、操作者、原因等上下文
- 不负责自己执行积分扣减、成长扣减、勋章处理或等级权益禁用

首批处罚性结果仍按白名单处理：

- `CONFIRMED`

## 8. 真实处罚执行主流程

### 8.1 治理来源执行流程

`createFromGovernanceRecord(...)` 升级后的建议主流程：

1. 校验是否命中处罚态
2. 按 `governance_record_id` 查询是否已存在未取消处罚记录
3. 若已存在，则直接返回，不重复执行
4. 读取用户画像、勋章、首批等级权益现状
5. 计算本次应执行的处罚项
6. 执行真实处罚
7. 组装真实执行结果快照
8. 写入成长处罚主记录
9. 如涉及等级权益，写入等级权益处罚子表

### 8.2 举报来源执行流程

`createFromReportHandle(...)` 升级后的建议主流程：

1. 校验是否命中处罚性举报结果
2. 按 `source_type=REPORT_HANDLE + source_id=reportId` 查询是否已存在未取消处罚记录
3. 若已存在，则直接返回，不重复执行
4. 读取用户画像、勋章、首批等级权益现状
5. 计算本次应执行的处罚项
6. 执行真实处罚
7. 组装真实执行结果快照
8. 写入成长处罚主记录
9. 如涉及等级权益，写入等级权益处罚子表

### 8.3 事务要求

每次处罚执行都必须放在一个事务内，保证：

- 扣积分与主记录写入一致
- 扣成长值与等级重算一致
- 勋章处理与快照落库一致
- 等级权益子表与主记录一致

如果任一步骤失败：

- 整体回滚
- 不留下半条处罚主记录
- 不留下半条等级权益子记录
- 不留下半条不完整处罚效果

## 9. 快照设计

### 9.1 从计划快照升级为真实结果快照

`effect_snapshot_json` 不再只记录预留计划，而升级为真实执行结果快照。

建议结构：

```json
{
  "operatorUserId": "admin-1",
  "reason": "违规处理",
  "ruleCode": "RULE-1",
  "sourceStatus": "MUTED",
  "plannedEffects": [
    "POINT_DEDUCT",
    "GROWTH_DEDUCT",
    "BADGE_DISABLE",
    "LEVEL_BENEFIT_DISABLE"
  ],
  "pointEffect": {
    "delta": -20,
    "balanceBefore": 100,
    "balanceAfter": 80
  },
  "growthEffect": {
    "delta": -30,
    "growthBefore": 260,
    "growthAfter": 230,
    "levelBefore": 3,
    "levelAfter": 3
  },
  "badgeEffects": [
    {
      "badgeGrantId": "badge-1",
      "badgeCode": "CREATOR_STAR",
      "previousStatus": "ACTIVE",
      "previousDisplaying": true,
      "currentStatus": "RECYCLED",
      "currentDisplaying": false
    }
  ],
  "benefitEffects": [
    {
      "benefitCode": "PRIORITY_CUSTOMER_SERVICE",
      "previousEnabled": true,
      "currentEnabled": false
    }
  ]
}
```

### 9.2 快照字段语义

- `operatorUserId`：处罚操作者
- `reason`：处罚原因
- `ruleCode`：治理入口规则编码，举报入口可为空
- `sourceStatus`：来源状态快照
- `plannedEffects`：本次实际执行的处罚项列表
- `pointEffect`：积分处罚前后值与实际扣减值
- `growthEffect`：成长值与等级处罚前后值及实际扣减值
- `badgeEffects`：本次勋章处罚项明细
- `benefitEffects`：本次等级权益处罚项明细

### 9.3 快照设计原则

- 每个处罚项必须能表达 `before/after`
- 恢复服务必须可以只依赖快照和子表进行回放
- 快照中实际扣减值应以“真正执行的结果”为准，而不是以计划值为准

## 10. 真实处罚规则

### 10.1 积分处罚

规则：

- 只在本次来源规则命中扣分时执行
- 写一条负向 `ContentUserPointLedger`
- 更新 `ContentUserProfile.pointBalance`

约束：

- 不允许把积分扣成负数
- 实际扣减值以用户当前可扣余额为准

快照要求：

- 写入 `delta`
- 写入 `balanceBefore`
- 写入 `balanceAfter`

### 10.2 成长值处罚与等级重算

规则：

- 只在本次来源规则命中扣成长时执行
- 写一条负向 `ContentUserGrowthLedger`
- 更新 `ContentUserProfile.growthValue`
- 按当前既有公式重算 `level`

约束：

- 不允许把成长值扣成负数
- 等级继续使用现有 `growthValue -> level` 公式

快照要求：

- 写入 `delta`
- 写入 `growthBefore/growthAfter`
- 写入 `levelBefore/levelAfter`

### 10.3 勋章处罚

本轮不做全库扫描式勋章处罚。

本轮规则：

- 只处理当前用户已发放、仍处于可用状态的勋章
- 将其调整为不可展示或回收态
- 恢复时依赖快照恢复

建议最小处理方式：

- 查询当前用户勋章发放记录
- 对 `ACTIVE` 且 `displaying=true` 的记录执行处罚
- 将 `status` 调整为 `RECYCLED`
- 将 `displaying` 调整为 `false`

冲突保护：

- 已失效的勋章不重复处理
- 已不可展示的勋章不重复处理
- 不存在的记录不报错，直接跳过

### 10.4 首批等级权益处罚

首批只落已经存在真实消费点的等级权益：

- `PRIORITY_CUSTOMER_SERVICE`

执行方式：

- 判断当前该权益是否启用
- 若启用，则写一条 `content_user_level_benefit_penalty_record`
- 将 `previousEnabled=true`
- 将 `currentEnabled=false`
- `recoverStatus` 初始化为 `PENDING_RECOVER`

约束：

- 已禁用的权益不重复写子记录
- 当前没有对应消费权益的，不扩范围处理

### 10.5 处罚项组合

本轮允许一次处罚同时触发：

- 积分处罚
- 成长值处罚
- 勋章处罚
- 等级权益处罚

因此主记录 `penalty_type` 不再只表达来源语义，建议升级成组合处罚语义，例如：

- `COMPOSITE`

若为了兼容现有代码保留来源判定，则来源语义由以下字段承接：

- `source_type`
- `source_id`
- `source_status`

## 11. 幂等与冲突保护

### 11.1 来源幂等

治理入口幂等条件：

- 同一 `governance_record_id`
- 且主记录状态不是 `CANCELLED`

举报入口幂等条件：

- 同一 `source_type = REPORT_HANDLE`
- 同一 `source_id = reportId`
- 且主记录状态不是 `CANCELLED`

满足条件时，直接返回，不重复执行处罚。

### 11.2 子项幂等

即使入口层误重复触发，也要保证各处罚项不重复执行：

- 已写过的处罚主记录不重复扣积分
- 已写过的处罚主记录不重复扣成长值
- 已失效的勋章不重复处理
- 已禁用的等级权益不重复写子记录

### 11.3 恢复与执行的衔接

主记录状态语义继续使用：

- `PENDING_RECOVER`：处罚已真实执行，等待恢复
- `RECOVERED`：处罚已恢复
- `CANCELLED`：记录作废

只有真实处罚执行成功后，才允许落成 `PENDING_RECOVER`。

### 11.4 恢复兼容性

本轮真实执行引擎落地后，恢复服务应能兼容：

- 历史的旧快照记录
- 本轮新的真实结果快照记录

如果新快照增加了 `pointEffect/growthEffect/benefitEffects` 等结构，恢复服务需要优先读取新结构；若缺失，再兼容旧结构字段。

## 12. 审计要求

### 12.1 新增处罚执行审计

本轮除现有恢复审计外，建议新增处罚执行审计日志。

建议事件类型：

- `USER_GROWTH_PENALTY_EXECUTED`

建议事件内容：

- 来源类型
- 处罚记录 ID
- 实际扣减积分
- 实际扣减成长值
- 处理勋章数量
- 禁用等级权益数量

### 12.2 保持现有恢复审计

现有：

- `USER_GROWTH_PENALTY_RECOVERED`

继续保留。

这样形成成对审计：

- 处罚执行有日志
- 处罚恢复也有日志

## 13. 测试设计

### 13.1 入口层测试

在 `ContentUserGovernanceServiceTest` 中新增或调整：

- 处罚态状态变更后会调用成长处罚执行服务
- 非处罚态状态流转不会执行处罚

在 `ContentUserSupportServiceTest` 中新增或调整：

- 举报处理为处罚性结论时会调用成长处罚执行服务
- 举报处理为非处罚性结论时不会执行处罚

### 13.2 执行服务测试

在 `ContentUserGrowthPenaltyRecordServiceTest` 中至少覆盖：

- 治理来源首次处罚时会真实扣积分、成长值并写快照
- 举报来源首次处罚时会真实扣积分、成长值并写快照
- 扣成长值后会重算等级
- 勋章仍为有效态时会被处罚处理
- 勋章已失效时不会重复处理
- 等级权益启用时会写子表记录
- 等级权益已禁用时不会重复写子表
- 命中相同来源时不会重复执行处罚

### 13.3 恢复服务测试

在 `ContentUserGrowthPenaltyRecoveryServiceTest` 中补齐：

- 针对真实执行结果快照可恢复积分
- 针对真实执行结果快照可恢复成长值并重算等级
- 针对真实执行结果快照可恢复勋章
- 针对真实执行结果快照可恢复首批等级权益

### 13.4 回归要求

至少执行以下回归：

- `ContentUserGrowthPenaltyRecordServiceTest`
- `ContentUserGrowthPenaltyRecoveryServiceTest`
- `ContentUserGovernanceServiceTest`
- `ContentUserSupportServiceTest`

并补启动模块编译验证与规范检查。

## 14. 实施顺序

1. 先补失败测试，锁定真实处罚执行行为
2. 升级 `ContentUserGrowthPenaltyRecordServiceImpl`，补齐真实处罚编排
3. 调整主记录快照结构和 `penalty_type` 语义
4. 补齐等级权益处罚子记录写入
5. 调整恢复服务兼容新快照结构
6. 补处罚执行审计日志
7. 更新覆盖报告和阶段计划文档
8. 执行聚焦回归、启动模块编译和规范检查

## 15. 风险与边界

- 勋章处罚规则当前仍然是最小闭环规则，不等于完整勋章治理中心
- 举报入口当前只有 `CONFIRMED` 白名单，处罚粒度仍弱于治理入口
- 新旧快照结构并存时，恢复服务必须先做兼容，避免破坏历史测试
- 当前工作树已有较多未提交改动，本轮设计与实现必须继续坚持最小增量，避免回漂到独立处罚中心建设

## 16. 结论

本次“成长处罚真实执行引擎”选择在现有 `IContentUserGrowthPenaltyRecordService` 基础上升级为“建档 + 真实处罚执行编排服务”，并保留现有恢复服务和等级权益子表方案。

这样可以把成长处罚链路从“来源建档 + 恢复回放”推进到“真实处罚执行 + 可恢复回放”的完整闭环，同时保持当前模块边界稳定，并为后续继续扩更多等级权益消费方留下清晰演进面。
