# 内容社区用户域 - 成长处罚恢复编排设计

## 1. 背景

当前支持治理域已经具备以下恢复能力：

- 申诉通过后恢复用户治理状态
- 到期治理状态自动恢复
- 状态历史分页查询与审计留痕

但 PRD 中的处罚申诉与恢复目标不只包含治理状态，还包含：

- 被扣除积分后的恢复
- 被回收勋章后的恢复
- 等级与成长权益的恢复

目前 `content/user` 模块虽然已经有积分账本、成长值账本、勋章发放记录、`profile` 等级汇总字段，但缺少一份“处罚影响快照”和“恢复幂等依据”。这会导致后续即便知道一条申诉被通过，也无法稳定判断：

- 当时扣了多少积分
- 是否同步扣了成长值
- 回收了哪些勋章
- 这次处罚是否已经恢复过

因此本次需要在不改基础库、不扩散到无关模块的前提下，为成长处罚恢复建立强一致编排基础。

## 2. 目标

在 `content/user` 模块内建立“处罚影响可记录、恢复动作可回放、恢复过程可审计、重复触发可幂等”的最小闭环，并支撑两类触发入口：

- 管理员处理申诉并通过
- 治理处罚到期自动恢复

本次目标聚焦以下三类成长侧恢复对象：

- 积分
- 成长值与等级
- 勋章展示/有效状态

## 3. 范围

### 3.1 包含

- 新增成长处罚恢复记录模型
- 为治理处罚与成长处罚建立显式关联
- 在申诉通过链路中补成长处罚恢复编排
- 在自动恢复链路中补成长处罚恢复编排
- 为积分、成长值、等级、勋章恢复增加服务层测试
- 更新覆盖报告和阶段计划文档

### 3.2 不包含

- 不修改基础库和公共 starter
- 不新增独立成长权益中心
- 不扩展到前端接口展示
- 不一次性实现所有等级特权细粒度回补
- 不顺手重构现有 `support`、`governance`、`growth` 整体分层

## 4. 约束

- 代码必须符合当前仓库的阿里巴巴规范要求
- 不改基础库，只允许在 `jeecg-module-content` 模块内增量实现
- 继续遵守当前单体模式、`Result<T>`、MyBatis-Plus、Flyway SQL 组织方式
- 恢复逻辑必须可幂等，避免申诉恢复和自动恢复重复加分或重复恢复勋章
- 设计优先保证“可审计、可追溯、可回放”，其次才是抽象优雅

## 5. 方案选择

### 5.1 方案 A：基于现有账本弱关联恢复

做法：

- 申诉通过时，根据 `appeal.targetId`、`targetType`、`remark` 等字段反查积分账本、成长账本、勋章记录
- 找到后直接做反向恢复

优点：

- 表面上改动最小

缺点：

- 处罚与恢复缺少稳定关联键
- 恢复依据容易依赖备注文案或业务约定
- 无法保证幂等
- 后续扩到更多权益时复杂度会快速上升

### 5.2 方案 B：处罚主表 + 明细表

做法：

- 新增处罚主表记录一次处罚事件
- 新增处罚明细表记录积分、成长值、勋章等具体影响项

优点：

- 结构最清晰
- 适合后续持续扩展

缺点：

- 本轮范围偏大
- 需要同时新增更多 mapper、entity、装配逻辑
- 对当前模块而言会引入更高建模成本

### 5.3 方案 C：单表快照式强一致模型

做法：

- 新增一张成长处罚记录表
- 使用 `effect_snapshot_json` 记录本次处罚影响的积分、成长值、勋章、等级权益快照
- 使用显式恢复状态字段保证幂等
- 由 `support/governance` 触发统一恢复服务

优点：

- 比弱关联稳定
- 比主子表实现范围小
- 能满足本轮“申诉恢复 + 自动恢复”双入口闭环

缺点：

- 快照 JSON 的结构需要在代码中维护
- 如果未来成长处罚种类极度复杂，仍可能演进为主子表

### 5.4 结论

本次采用方案 C：单表快照式强一致模型。

原因：

- 满足用户要求的“强一致建模”
- 不需要改基础库
- 能控制在当前支持治理域收口范围内
- 为下一步继续扩成长权益恢复保留演进空间

## 6. 数据模型设计

### 6.1 新增表

新增 `content_user_growth_penalty_record`，用于记录一次成长处罚及其恢复状态。

建议字段：

- `id`：主键
- `user_id`：用户 ID
- `governance_record_id`：关联的治理状态记录 ID，可为空
- `appeal_id`：关联申诉 ID，恢复后可回写，初始可为空
- `penalty_type`：处罚类型，例如 `POINT_DEDUCT`、`GROWTH_DEDUCT`、`BADGE_RECYCLE`、`COMPOSITE`
- `effect_snapshot_json`：处罚影响快照
- `status`：记录状态，建议值 `PENDING_RECOVER`、`RECOVERED`、`CANCELLED`
- `recover_trigger`：恢复触发来源，建议值 `APPEAL_APPROVED`、`AUTO_EXPIRE_RECOVER`
- `recover_reason`：恢复原因
- `recovered_by`：恢复操作人，自动恢复时为 `system`
- `recovered_at`：恢复时间
- `create_by/create_time/update_by/update_time`：通用审计字段

### 6.2 快照结构

`effect_snapshot_json` 使用单表 JSON 快照表达本次处罚影响，建议结构如下：

```json
{
  "pointDelta": -20,
  "growthDelta": -10,
  "badgeEffects": [
    {
      "badgeGrantId": "badge-grant-1",
      "badgeCode": "CREATOR_STAR",
      "previousStatus": "ACTIVE",
      "previousDisplaying": true
    }
  ],
  "levelEffect": {
    "previousLevel": 6
  }
}
```

说明：

- `pointDelta`、`growthDelta` 记录处罚时已执行的扣减值，恢复时取反回补
- `badgeEffects` 记录被回收或隐藏的勋章状态，恢复时按快照还原
- `levelEffect` 不直接强推等级数值，仍以恢复后的成长值重新计算等级，`previousLevel` 仅作为审计辅助

### 6.3 幂等语义

同一条成长处罚记录在以下条件下视为已恢复，不允许重复执行：

- `status = RECOVERED`
- `recovered_at` 非空

申诉恢复和自动恢复都只认这一份记录，不再直接根据账本或勋章记录猜测是否处理过。

## 7. 组件设计

### 7.1 新增服务

新增服务契约，例如：

- `IContentUserGrowthPenaltyRecoveryService`

核心职责：

- 根据治理记录或申诉上下文查询待恢复的成长处罚记录
- 对积分、成长值、等级、勋章执行恢复
- 写恢复账本、更新 `profile` 汇总、更新处罚记录状态
- 记录审计日志

建议核心方法：

- `recoverByAppeal(ContentUserAppeal appeal, String operatorUserId, Date executeTime, String reason)`
- `recoverByGovernanceRecord(ContentUserStatusRecord record, String operatorUserId, Date executeTime, String reason)`

### 7.2 触发边界

#### 申诉通过链路

位置：

- `ContentUserSupportServiceImpl.handleAppeal(...)`

顺序：

1. 更新申诉状态
2. 恢复治理状态
3. 调用成长处罚恢复服务
4. 写申诉处理审计日志

说明：

- 保持“治理状态恢复”在前，“成长处罚恢复”在后
- 这样可以保证跨域恢复的主语义仍由支持治理域驱动

#### 自动恢复链路

位置：

- `ContentUserGovernanceServiceImpl.autoRecoverExpiredStatuses(...)`

顺序：

1. 扫描到期治理状态记录
2. 恢复用户治理状态
3. 写治理恢复记录与审计日志
4. 调用成长处罚恢复服务

说明：

- 只有治理状态确实从处罚态恢复成功后，才触发成长处罚恢复
- 当前自动恢复范围继续限定为 `MUTED / RECOMMENDATION_LIMITED / FROZEN / BANNED`

## 8. 恢复规则

### 8.1 积分恢复

恢复逻辑：

- 从快照读取处罚时的 `pointDelta`
- 若值小于 0，则写一条正向积分账本
- 同步回补 `ContentUserProfile.pointBalance`

账本约定：

- `sourceType` 建议使用 `PENALTY_RECOVER`
- `bizId` 关联成长处罚记录 ID
- `remark` 写明 `APPEAL_APPROVED` 或 `AUTO_EXPIRE_RECOVER`

### 8.2 成长值与等级恢复

恢复逻辑：

- 从快照读取处罚时的 `growthDelta`
- 若值小于 0，则写一条正向成长值账本
- 同步回补 `ContentUserProfile.growthValue`
- 按当前 `ContentUserGrowthServiceImpl` 既有公式重算 `level`

说明：

- 不额外引入新的等级计算规则
- 不绕过现有 `profile` 汇总字段

### 8.3 勋章恢复

恢复逻辑：

- 从快照中读取受影响的 `badgeGrantId`
- 查询对应 `ContentUserBadgeGrant`
- 将 `status`、`displaying`、`recycledAt` 恢复到处罚前可接受状态

恢复原则：

- 只恢复当前处罚影响过的勋章，不做全量扫描
- 若勋章之后被其他流程再次回收，则以当前数据库状态为准，不强制覆盖明显冲突的后置操作

冲突保护：

- 如果勋章记录不存在，则跳过该项并保留审计日志
- 如果勋章当前状态已是可用态，则视为已恢复，不重复更新

### 8.4 等级权益恢复

本轮不单建等级权益表。

处理方式：

- 以恢复后的 `growthValue` 和 `level` 作为等级权益恢复依据
- 将“等级权益恢复”落在已有等级计算结果上
- 更细粒度的等级特权回补留到后续成长域专项实现

## 9. 审计与日志

成长处罚恢复必须写敏感操作审计日志，延续 PRD-113 要求。

建议事件类型：

- `USER_GROWTH_PENALTY_RECOVERED`

建议事件内容：

- 恢复触发来源
- 恢复的积分值
- 恢复的成长值
- 恢复的勋章数量
- 关联治理记录 ID 或申诉 ID

说明：

- 保持与现有 `ContentUserAuditLog` 用法一致
- 不额外引入新的审计基础设施

## 10. 测试设计

### 10.1 Service 层

新增或补齐以下测试：

- 申诉通过后，存在待恢复成长处罚记录时，恢复积分、成长值并重算等级
- 申诉通过后，存在勋章处罚快照时，恢复勋章状态与展示状态
- 自动恢复治理状态成功后，联动恢复对应成长处罚
- 同一处罚记录重复触发恢复时，不重复写账本、不重复恢复勋章
- 快照中部分勋章记录缺失时，其余恢复项仍可继续执行

### 10.2 现有测试扩展点

- `ContentUserSupportServiceTest`
- `ContentUserGovernanceServiceTest`
- `ContentUserGrowthServiceTest`

如需单独测试恢复服务，可新增：

- `ContentUserGrowthPenaltyRecoveryServiceTest`

### 10.3 回归要求

至少覆盖以下回归：

- 支持域申诉处理回归
- 治理域自动恢复回归
- 成长账本分账回归
- WebMvc 中已有状态历史接口不回归破坏

## 11. 实施顺序

1. 先补失败测试，锁定“申诉恢复成长处罚”和“自动恢复成长处罚”两个入口
2. 新增成长处罚记录实体、mapper、Flyway SQL
3. 新增成长处罚恢复服务并实现幂等恢复
4. 在 `ContentUserSupportServiceImpl` 与 `ContentUserGovernanceServiceImpl` 中接入编排
5. 补审计日志与必要文档更新

## 12. 风险与边界

- 当前历史数据中如果缺少处罚快照，旧数据无法自动恢复成长处罚；本轮只保证新模型接入后的强一致链路
- 勋章恢复依赖 `badgeGrantId` 等快照字段，如果历史处罚未记录具体勋章，无法精确恢复
- 自动恢复与申诉恢复可能同时命中同一条记录，必须以处罚记录状态做幂等保护
- 规范脚本中的仓库级历史 warning 仍可能存在，但不应引入新的本轮阻断问题

## 13. 结论

本次设计选择“单表快照式强一致模型”，在不改基础库的前提下，为成长处罚恢复提供稳定的关联依据和幂等恢复能力。

这样可以把当前支持治理域第二阶段剩余缺口收敛为可落地的后端增量实现，并为后续继续扩展积分、勋章、等级权益治理恢复打下统一基础。
