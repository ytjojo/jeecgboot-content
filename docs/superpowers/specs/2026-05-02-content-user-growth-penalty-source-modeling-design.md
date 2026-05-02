# 内容社区用户域 - 更多成长处罚来源建模扩展设计

## 1. 背景

当前 `content/user` 模块已经具备成长处罚恢复能力：

- 申诉通过后可恢复治理状态
- 到期治理状态可自动恢复
- 成长处罚恢复可联动恢复积分、成长值、等级、勋章状态，以及首批 `PRIORITY_CUSTOMER_SERVICE` 等级权益

但现有模型仍有一个明显缺口：

- `ContentUserGrowthPenaltyRecord` 已经承担“恢复依据”角色
- 恢复链路已经可用
- 处罚发生时的“建档来源”仍然不完整

当前代码里，成长处罚主记录的消费方已经存在，但稳定的创建来源还不够清晰，尤其缺少下面两类真实入口的统一建模：

- 治理处罚入口：`governance/status/change`
- 举报处理入口：`support/admin/report/handle`

这会导致后续继续扩“处罚执行引擎”时，无法稳定回答：

- 这条成长处罚记录是从哪个业务入口创建的
- 它对应的是治理处罚还是举报处理结论
- 同一个来源是否已经建档过
- 后续恢复时应该如何定位原始处罚上下文

因此，本轮需要在不改基础库、不扩散到无关模块的前提下，为成长处罚主表补齐“更多来源建模”，让恢复链路前面的处罚建档也形成稳定边界。

## 2. 目标

在 `jeecg-module-content` 模块内，为成长处罚记录补齐更多创建来源建模，并控制本轮范围为“建档 + 预留执行位”，不直接扩成完整处罚执行引擎。

本轮目标如下：

- 为 `ContentUserGrowthPenaltyRecord` 增加统一来源建模字段
- 增加统一成长处罚建档服务，承接不同业务入口
- 在 `governance/status/change` 与 `support/admin/report/handle` 两个入口接入建档
- 明确幂等语义，避免同一来源重复建档
- 为后续真正的积分/成长值/勋章/等级权益处罚执行保留可扩展快照结构

## 3. 范围

### 3.1 包含

- 扩展成长处罚主表字段，补齐来源建模信息
- 新增成长处罚建档服务契约与实现
- 在治理状态变更入口接入“处罚态建档”
- 在举报处理入口接入“处罚结论建档”
- 为建档来源与幂等行为补充单测
- 更新审计报告与阶段计划文档

### 3.2 不包含

- 不改基础库和公共 starter
- 不新增来源子表
- 不在本轮实现完整的处罚执行引擎
- 不要求本轮真正执行积分扣减、成长值扣减、勋章回收或等级权益禁用
- 不扩展到前端页面或新增展示接口
- 不顺手重构现有 `support`、`governance`、`growth` 其他分层

## 4. 约束

- 代码必须符合当前仓库阿里巴巴规范要求
- 只允许在 `jeecg-module-content` 内增量实现
- 继续使用 Spring Boot 3、JeecgBoot、MyBatis-Plus、Flyway SQL、`Result<T>` 现有风格
- 建档服务要和已有恢复服务边界保持对称，便于后续持续演进
- 本轮优先保证“来源清晰、幂等稳定、可扩执行”，而不是一次性做全所有处罚动作

## 5. 方案选择

### 5.1 方案 A：在入口内直接拼接主表记录

做法：

- 在 `ContentUserGovernanceServiceImpl.changeStatus(...)` 里直接创建成长处罚记录
- 在 `ContentUserSupportServiceImpl.handleReport(...)` 里直接创建成长处罚记录

优点：

- 改动面最小
- 上手最快

缺点：

- 来源规则散落在多个 service
- 后续扩“处罚执行引擎”时容易重复装配上下文
- 与已有恢复服务边界不对称

### 5.2 方案 B：统一建档服务 + 主表扩来源字段

做法：

- 保留 `ContentUserGrowthPenaltyRecord` 作为唯一主记录
- 扩充主表来源字段
- 新增统一建档服务承接治理入口和举报入口
- 本轮只做建档，不执行真实处罚动作

优点：

- 来源边界清晰
- 和现有恢复服务形成前后对称结构
- 后续接处罚执行时复用成本低
- 本轮复杂度可控

缺点：

- 会多一个服务契约和实现
- 比入口内硬写多一点装配成本

### 5.3 方案 C：主表 + 来源子表

做法：

- 在主表之外再建“来源明细表”记录 `GOVERNANCE_STATUS_CHANGE`、`REPORT_HANDLE` 等来源

优点：

- 关系表达最显式

缺点：

- 本轮收益有限
- 表和 mapper 增加较多
- 对当前阶段偏重

### 5.4 结论

本次采用方案 B：`统一建档服务 + 主表扩来源字段`。

原因：

- 能覆盖用户已确认的“两入口一起扩来源”
- 仍然保持本轮为“建档 + 预留执行位”的可控范围
- 与已实现的恢复服务形成清晰对称结构
- 为后续处罚执行引擎留下稳定扩展面

## 6. 数据模型设计

### 6.1 主表继续保留

继续使用现有主表 `content_user_growth_penalty_record`，不新增来源子表。

已有字段继续保留：

- `id`
- `user_id`
- `governance_record_id`
- `appeal_id`
- `penalty_type`
- `effect_snapshot_json`
- `status`
- `recover_trigger`
- `recover_reason`
- `recovered_by`
- `recovered_at`

### 6.2 新增来源字段

建议新增以下字段：

- `source_type`：处罚来源类型
- `source_id`：来源业务主键
- `source_status`：来源状态快照，可为空

首批 `source_type` 取值：

- `GOVERNANCE_STATUS_CHANGE`
- `REPORT_HANDLE`

字段语义：

- `source_type` 用于表达“这条成长处罚记录从哪个业务入口创建”
- `source_id` 用于表达“这个入口对应的业务记录主键”
- `source_status` 用于保留来源当时的状态语义，方便后续做处罚执行和审计排查

### 6.3 `governance_record_id` 继续保留

虽然引入了通用 `source_type/source_id`，但 `governance_record_id` 仍然保留，不被替代。

原因：

- 当前自动恢复链路已经直接依赖 `governance_record_id`
- 它更像“治理恢复关联键”，不是通用来源键
- 如果移除或弱化它，会增加现有恢复链路回归风险

因此，字段职责拆分如下：

- `governance_record_id`：服务于治理恢复链路
- `source_type/source_id/source_status`：服务于处罚来源建模

## 7. 快照设计

### 7.1 本轮快照定位

`effect_snapshot_json` 本轮不再强制要求必须写入完整扣减结果，而是允许先使用“可执行前置快照”。

本轮快照主要承载：

- 来源上下文
- 执行计划占位
- 后续执行引擎所需的可扩展结构

### 7.2 建议结构

```json
{
  "operatorUserId": "admin-1",
  "reason": "违规处理",
  "ruleCode": "RULE-1",
  "sourceStatus": "RESOLVED",
  "plannedEffects": []
}
```

说明：

- `operatorUserId`：记录处罚操作者
- `reason`：记录处罚原因
- `ruleCode`：治理入口可带入规则编码，举报入口可为空
- `sourceStatus`：来源记录状态快照
- `plannedEffects`：后续接真实处罚执行时填入积分、成长值、勋章、等级权益等计划项；本轮允许为空数组

### 7.3 与恢复语义的关系

本轮建档后，恢复服务仍然只消费“已有可恢复记录”。

这意味着：

- 历史已存在完整快照的记录，恢复逻辑继续按现有实现工作
- 本轮新建的“来源扩展记录”可以先作为执行前置记录存在
- 后续处罚执行引擎接入后，再把 `plannedEffects` 扩成真实处罚影响快照

## 8. 组件设计

### 8.1 新增服务契约

建议新增：

- `IContentUserGrowthPenaltyRecordService`

建议核心方法：

- `createFromGovernanceRecord(ContentUserStatusRecord record, ContentUserStatusChangeReq req, Date executeTime)`
- `createFromReportHandle(ContentUserReport report, ContentReportHandleReq req, String governanceRecordId, Date executeTime)`

### 8.2 职责边界

统一建档服务职责：

- 判断来源是否需要建档
- 做来源级幂等校验
- 组装 `penalty_type`
- 组装 `effect_snapshot_json`
- 写入成长处罚主表

它不负责：

- 真正执行积分扣减
- 真正执行成长值扣减
- 真正执行勋章回收
- 真正执行等级权益禁用

### 8.3 服务与恢复侧关系

本轮完成后，服务边界形成如下结构：

- `IContentUserGrowthPenaltyRecordService`：负责“处罚发生时建档”
- `IContentUserGrowthPenaltyRecoveryService`：负责“处罚解除时恢复”

这样可以让后续真正接入处罚执行时，继续围绕建档服务扩展，而不是把逻辑重新散回 `support/governance` 两个入口中。

## 9. 入口编排设计

### 9.1 治理状态变更入口

位置：

- `ContentUserGovernanceServiceImpl.changeStatus(...)`

接入原则：

- 只在“进入处罚态”时尝试建档
- 首批处罚态仍限定：
  - `MUTED`
  - `RECOMMENDATION_LIMITED`
  - `FROZEN`
  - `BANNED`

建议顺序：

1. 校验状态流转
2. 写 `ContentUserStatusRecord`
3. 更新 `profile.status`
4. 调用成长处罚建档服务
5. 写治理审计日志

说明：

- 先写治理记录，建档服务才能拿到稳定的 `statusRecord.id`
- 审计顺序放在建档之后，便于后续如需把处罚记录 ID 写入审计扩展字段时继续演进

### 9.2 举报处理入口

位置：

- `ContentUserSupportServiceImpl.handleReport(...)`

接入原则：

- 只在“举报处理结论等价于处罚”时尝试建档
- 这轮不要求举报处理入口自己执行真实处罚动作

建议顺序：

1. 校验举报处理状态
2. 更新 `ContentUserReport`
3. 调用成长处罚建档服务
4. 写举报处理审计日志

### 9.3 举报入口的处罚判定

由于当前 `ContentReportHandleReq` 只有：

- `status`
- `resultStatus`
- `resultNote`
- `progressNote`

没有显式“处罚类型”字段，因此本轮不引入新请求字段，而采用“结果状态白名单 + 预留扩展”的方式：

- 先由 service 内部维护一组“处罚性结果状态”白名单
- 只有命中白名单时才建档
- 如果当前代码库里没有稳定可复用的处罚性结果状态集合，则先将该集合固定在建档服务中，避免散落在多个入口

这部分是本轮已知边界，不在 spec 中模糊化处理。

### 9.4 举报入口与治理记录的关联

举报处理入口本轮可能拿不到稳定的治理记录 ID，因此允许以下两种情况：

- 如果当前流程能拿到治理记录 ID，则同时写入 `governance_record_id`
- 如果拿不到，则仍然允许只写：
  - `source_type = REPORT_HANDLE`
  - `source_id = reportId`

这样可以保证：

- 来源建模先成立
- 不把举报入口强行耦合到治理记录必须存在
- 后续如果举报处理补了“处罚执行 + 治理联动”，可以继续增强关联

## 10. 幂等语义

### 10.1 治理入口幂等

同一条治理状态记录，只允许生成一条未取消的成长处罚主记录。

业务判定条件：

- `governance_record_id = 当前治理记录ID`
- 且记录状态不是 `CANCELLED`

满足条件时，不重复建档。

### 10.2 举报入口幂等

同一条举报处理记录，只允许生成一条未取消的成长处罚主记录。

业务判定条件：

- `source_type = REPORT_HANDLE`
- `source_id = reportId`
- 且记录状态不是 `CANCELLED`

满足条件时，不重复建档。

### 10.3 本轮不强推数据库唯一索引

本轮优先采用业务幂等，不强制新增唯一索引。

原因：

- 当前工作树已有较多增量改动
- 先以 service 层规则收口，范围更可控
- 后续若处罚来源明显增多，再评估是否追加数据库唯一约束

## 11. `penalty_type` 语义调整

本轮为了让“来源建模”与“具体处罚效果”解耦，建议首批按来源语义写入：

- 治理入口：`GOVERNANCE_PENALTY`
- 举报入口：`REPORT_PENALTY`

这样做的目的不是弱化处罚效果，而是避免当前仍未落地的真实扣减动作，把 `penalty_type` 误绑定成某个尚未稳定的执行结果。

后续如果接入真实执行，可以继续通过：

- `plannedEffects`
- 或额外细分字段

表达真正的积分、成长值、勋章、等级权益处罚项。

## 12. 测试设计

### 12.1 治理服务测试

在 `ContentUserGovernanceServiceTest` 中新增：

- 处罚态状态变更后会调用成长处罚建档服务
- 非处罚态状态流转不会建档
- 普通恢复态流转不会误建档

### 12.2 支持服务测试

在 `ContentUserSupportServiceTest` 中新增：

- 举报处理为处罚性结论时会调用成长处罚建档服务
- 举报处理为非处罚性结论时不会建档

### 12.3 建档服务测试

新增独立单测，至少覆盖：

- 治理来源幂等
- 举报来源幂等
- 主表字段写入正确：
  - `source_type`
  - `source_id`
  - `source_status`
  - `governance_record_id`
  - `penalty_type`
  - `effect_snapshot_json`
  - `status`

### 12.4 测试边界说明

本轮测试只锁定“来源建模闭环”和“幂等规则”，不把测试写成：

- 已真实扣积分
- 已真实扣成长值
- 已真实回收勋章
- 已真实禁用等级权益

因为用户已明确选择本轮只做“建档 + 预留执行位”，测试必须和设计边界保持一致，避免提前把未来执行实现钉死。

## 13. 实施顺序

1. 先补失败测试，锁定治理入口和举报入口都会正确委托建档服务
2. 扩展 `content_user_growth_penalty_record` Flyway SQL 与实体字段
3. 新增 `IContentUserGrowthPenaltyRecordService` 与实现
4. 在 `ContentUserGovernanceServiceImpl` 接入处罚态建档
5. 在 `ContentUserSupportServiceImpl.handleReport(...)` 接入处罚性结论建档
6. 补建档服务测试与文档更新

## 14. 风险与边界

- 举报处理当前没有显式处罚类型字段，本轮只能依赖结果状态白名单和固定服务规则判断，语义强度弱于治理入口
- 本轮不执行真实处罚动作，因此新建档记录中的 `plannedEffects` 可能为空，这属于设计内行为，不视为缺陷
- 如果未来需要让恢复服务直接消费这批新建档记录，必须先补真实处罚执行或补充更完整快照
- 当前工作树已有较多未提交改动，本轮设计与实现都要坚持最小增量，避免回漂到“完整处罚中心”建设

## 15. 结论

本次“更多成长处罚来源建模扩展”选择通过“统一建档服务 + 主表扩来源字段”的方式推进。

这样可以在不改基础库、不一次性扩成完整处罚执行引擎的前提下，把成长处罚链路从“只有恢复侧”推进到“来源侧也可稳定建模”，并为下一轮继续接入真实处罚执行打下清晰边界。
