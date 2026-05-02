# 内容社区用户域 - 等级权益细粒度恢复设计

## 1. 背景

当前 `content/user` 模块已经完成成长处罚恢复第一阶段闭环：

- 申诉通过后可恢复治理状态
- 到期治理状态可自动恢复
- 成长处罚可恢复积分、成长值、等级与勋章状态

但“等级权益”仍停留在粗粒度层面。现状里只有：

- `ContentUserProfile.level` 作为等级汇总值
- `ContentUserSupportServiceImpl` 基于 `level/growthValue` 临时推导高等级客服优先
- 现有成长处罚快照只覆盖积分、成长值、勋章

这意味着一旦后续需要表达“某次处罚临时剥夺了等级权益，申诉通过或处罚到期后应精确恢复”，当前模型无法回答：

- 被影响的是哪一项等级权益
- 处罚前该权益是否处于启用状态
- 该权益是否已经恢复过
- 恢复后哪些对外行为应显式感知恢复结果

因此需要在不改基础库、不扩散到外部业务模块的前提下，为等级权益恢复补一层可记录、可恢复、可审计的强一致模型。

## 2. 目标

在 `content/user` 模块内，为成长处罚恢复补齐“等级权益细粒度恢复”的最小闭环，满足以下目标：

- 一次成长处罚可关联多条等级权益影响记录
- 申诉通过与治理到期恢复继续复用统一恢复编排
- 权益恢复具备明确幂等语义和审计落点
- 对外至少有一个真实消费点能够感知恢复结果

本次只落地当前模块内最接近真实消费点的等级权益，不假设外部模块已具备上传、高清视频、分发加权等消费能力。

## 3. 范围

### 3.1 包含

- 新增等级权益处罚子表
- 新增等级权益恢复服务
- 将等级权益恢复接入 `ContentUserGrowthPenaltyRecoveryServiceImpl`
- 在 `customer-service` 路由中引入显式权益判定
- 为申诉恢复、自动恢复、幂等跳过补充测试
- 更新覆盖报告和阶段计划文档

### 3.2 不包含

- 不改基础库和公共 starter
- 不扩展到内容分发、上传中心、视频权限等外部模块
- 不一次性实现所有 PRD 中的等级权益消费方
- 不重构现有成长处罚主表模型
- 不新增前端专用管理页面

## 4. 约束

- 继续保持 `content/user` 模块内最小增量实现
- 保持当前 Spring Boot 3、MyBatis-Plus、Flyway SQL、`Result<T>` 约定不变
- 新增列表接口时遵守“用户历史/管理列表默认分页，静态小集合不分页”的原则
- 权益恢复必须具备幂等性，避免申诉恢复和自动恢复重复生效
- 恢复结果必须可审计、可追溯，不能只靠 `level` 二次推导猜测

## 5. 方案选择

### 5.1 方案 A：继续扩 `effect_snapshot_json`

做法：

- 继续在 `ContentUserGrowthPenaltyRecord.effectSnapshotJson` 中增加 `levelBenefitEffects`
- 恢复时解析 JSON 并直接回放权益状态

优点：

- 改动最小
- 和现有成长处罚恢复模型一致

缺点：

- 权益项一多后 JSON 结构会持续膨胀
- 权益级幂等和审计可读性较差
- 后续若补更多等级权益，查询和定位不直观

### 5.2 方案 B：独立权益子表

做法：

- 保留现有 `ContentUserGrowthPenaltyRecord` 作为处罚主表
- 新增等级权益处罚子表，记录每条受影响权益的前态、现态和恢复状态
- 恢复时由统一恢复服务按子表逐条回放

优点：

- 权益粒度清晰，后续扩展更顺
- 权益级幂等与审计更明确
- 不需要把所有权益细节都塞进快照 JSON

缺点：

- 比纯快照多一张表和一层 service/mapper
- 首批可闭环的权益仍然有限

### 5.3 方案 C：纯规则重算

做法：

- 不记录任何权益快照或子表
- 恢复时仅根据当前 `level/growthValue` 重新推导等级权益

优点：

- 数据模型最简单

缺点：

- 不符合当前已确认的“强一致建模”
- 无法精确表达处罚前权益是否已启用
- 不能稳定支持幂等与审计

### 5.4 结论

本次采用 **方案 B：独立权益子表**。

原因：

- 满足强一致建模要求
- 保持在 `content/user` 模块内最小闭环
- 比 JSON 快照更适合后续继续扩等级权益
- 不需要提前改动外部真实消费模块

## 6. 数据模型设计

### 6.1 主从关系

保留现有成长处罚主表：

- `content_user_growth_penalty_record`

新增等级权益子表：

- `content_user_level_benefit_penalty_record`

关系约定：

- 一条 `growth_penalty_record` 可关联 0..N 条 `level_benefit_penalty_record`
- 子表按 `penalty_record_id` 关联主表
- 子表只记录“等级权益影响”，不重复记录积分、成长值、勋章

### 6.2 子表字段建议

- `id`：主键
- `penalty_record_id`：成长处罚主表 ID
- `user_id`：用户 ID
- `benefit_code`：权益编码
- `previous_enabled`：处罚前是否启用
- `current_enabled`：处罚后是否启用
- `recover_status`：恢复状态，建议值 `PENDING_RECOVER`、`RECOVERED`
- `recover_reason`：恢复原因
- `recovered_by`：恢复操作人
- `recovered_at`：恢复时间
- `create_by/create_time/update_by/update_time`：通用审计字段

索引建议：

- `idx_content_user_level_benefit_penalty_record_penalty`：`(penalty_record_id, recover_status)`
- `idx_content_user_level_benefit_penalty_record_user`：`(user_id, benefit_code, recover_status)`

### 6.3 权益编码

本次仅引入一个首批权益编码：

- `PRIORITY_CUSTOMER_SERVICE`

原因：

- `customer-service` 路由已经在 `content/user` 模块内闭环
- 当前已有高等级优先人工客服的规则落点
- 恢复后可立即体现为一个可见、可测、可审计的行为变化

以下 PRD 权益暂不纳入本次范围：

- 更大文件上传
- 更高清视频权限
- 创建更多话题
- 内容分发小幅加权

这些权益当前没有 `content/user` 内可直接消费的真实状态落点，强行实现会变成只有数据、没有真实行为的“假闭环”。

## 7. 组件设计

### 7.1 新增实体与 Mapper

新增：

- `ContentUserLevelBenefitPenaltyRecord`
- `ContentUserLevelBenefitPenaltyRecordMapper`

实体职责：

- 表达一次成长处罚对某项等级权益的影响
- 表达该权益是否已恢复

### 7.2 新增服务

新增服务契约，例如：

- `IContentUserLevelBenefitRecoveryService`

建议职责：

- 查询某条成长处罚对应的待恢复权益记录
- 按权益编码执行逐条恢复
- 维护子表恢复状态
- 统计已恢复权益数量并返回给统一恢复服务

建议方法：

- `int recoverByPenaltyRecord(ContentUserGrowthPenaltyRecord record, String operatorUserId, Date executeTime, String reason)`
- `boolean hasEnabledBenefit(String userId, String benefitCode)`

说明：

- `recoverByPenaltyRecord(...)` 用于统一恢复编排内部调用
- `hasEnabledBenefit(...)` 用于 `customer-service` 路由判断时读取显式权益状态

### 7.3 对统一恢复服务的改造

位置：

- `ContentUserGrowthPenaltyRecoveryServiceImpl`

改造点：

- 在恢复单条成长处罚时，除积分、成长值、勋章外，再调用等级权益恢复服务
- 主表仍保持 `RECOVERED` 作为整条处罚记录的总恢复状态
- 子表单独维护权益级 `recover_status`

建议恢复顺序：

1. 恢复积分
2. 恢复成长值
3. 重算等级
4. 恢复勋章
5. 恢复等级权益子表
6. 更新主表恢复状态
7. 写审计日志

这样可以保持当前主链路基本不变，只在最后插入一段独立职责的权益恢复。

## 8. 对外行为设计

### 8.1 `customer-service` 路由

位置：

- `ContentUserSupportServiceImpl.getCustomerServiceEntry(...)`
- `ContentUserSupportServiceImpl.shouldRouteToManualPriority(...)`

当前规则：

- `level >= 5` 或 `growthValue >= 400` 时，进入人工优先客服

改造后规则：

1. 如果命中治理异常优先路由，仍然返回 `APPEAL_PRIORITY`
2. 否则如果存在显式启用的 `PRIORITY_CUSTOMER_SERVICE`，返回 `MANUAL_PRIORITY`
3. 否则再走现有 `level/growthValue` 阈值推导
4. 最后回落到 `SMART_FIRST`

这样可以同时兼容：

- 旧数据只有等级值、没有权益子表记录
- 新数据在处罚恢复后能通过显式权益判定体现“已恢复”

### 8.2 幂等语义

子表的幂等判断：

- `recover_status = RECOVERED` 时跳过
- 同一条子表记录只允许被恢复一次

主表的幂等判断继续沿用现有语义：

- `ContentUserGrowthPenaltyRecord.status = RECOVERED` 时整条主记录跳过

说明：

- 正常路径下，主表和子表都会从 `PENDING_RECOVER` 进入 `RECOVERED`
- 如果未来出现部分权益恢复失败，可在实现时决定是否保留“全部成功才更新主表”策略
- 本次为了保持事务一致性，仍建议在同一事务中执行，任何一步失败则整体回滚

## 9. 审计设计

当前已有：

- `ContentUserAuditLog.growthPenaltyRecovered(...)`

本次建议扩展两种方式之一：

- 方式 1：在现有 `growthPenaltyRecovered` 的 `extraDataJson` 中补 `recoveredBenefitCount`
- 方式 2：新增一条 `USER_LEVEL_BENEFIT_RECOVERED` 审计事件

本次优先建议方式 1，原因：

- 改动更小
- 保持成长处罚恢复审计聚合在一条事件内
- 不会额外增加多条审计噪音

## 10. 测试设计

### 10.1 单元测试

新增或扩展：

- `ContentUserGrowthPenaltyRecoveryServiceTest`
- `ContentUserSupportServiceTest`
- `ContentUserGovernanceServiceTest`
- 视需要新增 `ContentUserLevelBenefitRecoveryServiceTest`

核心场景：

- 申诉通过后恢复 `PRIORITY_CUSTOMER_SERVICE`
- 自动恢复治理状态后恢复 `PRIORITY_CUSTOMER_SERVICE`
- 子表已恢复时幂等跳过
- 子表缺失时不影响积分/成长值/勋章恢复主链路
- `customer-service` 在显式权益启用时命中 `MANUAL_PRIORITY`
- 没有权益记录时仍兼容旧的 `level/growthValue` 规则

### 10.2 回归重点

- 不能破坏现有 `help-center` 结构化返回
- 不能破坏现有 `customer-service` 的治理异常优先级
- 不能破坏现有成长处罚主表恢复逻辑
- 不能让列表接口从分页退回非分页

## 11. 风险与取舍

### 11.1 风险

- 首批只有 `PRIORITY_CUSTOMER_SERVICE` 一个真实权益，表面上会显得模型偏“重”
- 老数据没有子表记录，只能继续依赖现有等级阈值规则
- 如果未来把更多等级权益接进来，需要明确各自真实消费方

### 11.2 取舍

- 接受“首批收益有限”，换取后续扩展的清晰边界
- 接受“老数据双轨兼容”，避免为了回填历史数据扩大本轮范围
- 接受“只落一个真实权益”，避免做没有消费方的伪能力

## 12. 实施建议

按以下顺序推进最稳妥：

1. 先写失败测试，锁定 `PRIORITY_CUSTOMER_SERVICE` 的恢复与路由行为
2. 新增子表、实体、Mapper、Flyway SQL
3. 实现等级权益恢复服务
4. 接入 `ContentUserGrowthPenaltyRecoveryServiceImpl`
5. 接入 `ContentUserSupportServiceImpl` 的显式权益判定
6. 跑聚焦回归并同步审计/覆盖文档

## 13. 结论

本次采用“成长处罚主表 + 等级权益子表”的强一致模型，在不改基础库、不扩散到外部模块的前提下，先把 `PRIORITY_CUSTOMER_SERVICE` 做成首个真实可恢复的等级权益。这样既能延续当前成长处罚恢复闭环，又为后续扩展更多等级权益保留明确的数据边界和恢复编排入口。
