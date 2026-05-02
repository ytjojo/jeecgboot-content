# 内容社区用户域 - 更多等级权益消费方落地设计

## 1. 背景

当前 `content/user` 模块已经补齐了等级权益的第一段闭环能力：

- `PRIORITY_CUSTOMER_SERVICE` 已有真实消费点，`customer-service` 路由可以显式感知权益启用与禁用
- 成长处罚恢复编排已经可以恢复首批等级权益
- 成长处罚真实执行引擎已经可以禁用首批等级权益
- 等级权益子表 `content_user_level_benefit_penalty_record` 已经具备记录、恢复和幂等能力

但当前仍然存在一个明确缺口：

- 运行时真实消费显式权益的代码仍然只有 `PRIORITY_CUSTOMER_SERVICE`
- PRD 中已提出的其他等级权益，如“更大文件上传”“更高清视频权限”“创建更多话题”，还没有稳定的消费方
- 这会导致成长处罚虽然能够禁用这些权益编码，但运行时业务侧并不会真正感知禁用结果

因此需要在不漂移到外部上传模块、不新造不存在业务链路的前提下，把“更多等级权益消费方落地”推进为当前模块内可验证、可测试、可扩展的最小闭环。

## 2. 目标

在 `jeecg-module-content` 模块内，为更多等级权益补齐统一判定与消费落点，满足以下目标：

- 统一计算用户当前可用的等级权益能力
- 将运行时权益查询从恢复编排服务中解耦出来
- 为 `UPLOAD_EXPANDED`、`HD_VIDEO`、`TOPIC_QUOTA_EXPANDED` 提供明确能力输出
- 至少为其中一个当前模块内真实存在的业务入口提供准入判定
- 让成长处罚禁用与恢复结果可以被运行时业务即时感知

本轮目标不是直接建设上传中心或视频发布中心，而是在现有 `content/user` 边界内，为这些权益提供统一能力模型和可验证消费方。

## 3. 范围

### 3.1 包含

- 新增统一等级权益判定服务
- 在成长汇总接口中返回等级权益能力摘要
- 将 `customer-service` 显式权益判断切换到新统一服务
- 在订阅服务中为 `TOPIC` 类型引入数量上限判定，作为“更多话题”真实消费方
- 引入以下权益编码：
  - `UPLOAD_EXPANDED`
  - `HD_VIDEO`
  - `TOPIC_QUOTA_EXPANDED`
  - `PRIORITY_CUSTOMER_SERVICE`
- 为默认规则、显式权益覆盖、处罚禁用优先和订阅上限补充测试
- 更新覆盖报告与阶段计划文档

### 3.2 不包含

- 不新增上传 controller、上传 service 或外部媒体模块改造
- 不新增视频发布接口
- 不建设真正的话题创建模块
- 不新增等级权益主表
- 不修改成长处罚主记录和等级权益处罚子表结构
- 不重构现有成长、支持、治理模块的整体分层

## 4. 约束

- 继续保持 `content/user` 模块内最小增量实现
- 保持当前 Spring Boot 3、JeecgBoot、MyBatis-Plus、`Result<T>` 风格不变
- 运行时权益查询与处罚恢复编排职责必须分离
- 权益禁用后的运行时判定必须优先于等级默认规则，不能让高等级回退规则绕过处罚
- 用户画像缺失时返回最保守默认能力，不因只读查询抛出异常
- 代码与测试必须符合当前仓库阿里巴巴规范要求

## 5. 方案选择

### 5.1 方案 A：新增统一权益判定服务并提供最小消费落点

做法：

- 新增专门的等级权益判定服务，统一计算用户当前权益和能力摘要
- 在 `growth/summary` 中暴露上传大小、高清视频开关、话题额度等能力
- 在 `ContentUserSubscriptionServiceImpl` 中对 `TOPIC` 类型订阅数量做上限判定
- 现有 `customer-service` 路由改为依赖新服务，而不是继续直接查询恢复服务

优点：

- 职责清晰，运行时判定与恢复编排解耦
- 符合当前模块边界，不强行入侵不存在的上传/视频模块
- 成长处罚禁用与恢复结果可以被运行时业务即时感知
- 便于后续继续扩新的权益消费方

缺点：

- `UPLOAD_EXPANDED` 和 `HD_VIDEO` 本轮只落能力输出，不会直接变成外部上传链路的强校验
- 相比直接复用旧服务，会多一个 service 和若干 VO 字段

### 5.2 方案 B：继续扩恢复服务职责

做法：

- 直接在 `IContentUserLevelBenefitRecoveryService` 里追加运行时判定方法
- 所有业务服务继续依赖该恢复服务查询当前权益

优点：

- 改动文件少
- 复用当前依赖关系

缺点：

- 恢复编排和运行时判定耦合，职责会越来越脏
- 后续权益越来越多时，恢复服务会退化成万能服务
- 不利于测试隔离与后续演进

### 5.3 方案 C：只补 VO，不做真实消费判定

做法：

- 只在成长或用户摘要接口里返回权益能力信息
- 不在任何业务入口中真实消费这些权益

优点：

- 实现最快

缺点：

- 无法满足“消费方落地”目标
- 处罚禁用与恢复无法形成真实约束
- 会留下“只有说明、没有行为”的假闭环

### 5.4 结论

本次采用 **方案 A：新增统一权益判定服务并提供最小消费落点**。

原因：

- 满足当前 gap 的真实目标，而不是停留在说明层
- 保持当前模块边界稳定，不扩散到不存在的上传和视频发布链路
- 可以在 `TOPIC` 订阅入口形成一个真实可测的消费方
- 可以为后续上传与视频模块接入提供统一能力出口

## 6. 能力模型设计

### 6.1 新增权益编码

本轮在运行时判定层统一支持以下权益编码：

- `PRIORITY_CUSTOMER_SERVICE`
- `UPLOAD_EXPANDED`
- `HD_VIDEO`
- `TOPIC_QUOTA_EXPANDED`

说明：

- `PRIORITY_CUSTOMER_SERVICE` 是已落地首批权益，需要纳入统一服务，避免继续保留两套判断逻辑
- `UPLOAD_EXPANDED`、`HD_VIDEO`、`TOPIC_QUOTA_EXPANDED` 是本轮补齐的更多等级权益消费方

### 6.2 能力摘要对象

建议新增一个统一能力摘要对象，例如：

- `ContentUserLevelBenefitSummaryVO`

建议字段：

- `uploadSizeLimitMb`
- `hdVideoEnabled`
- `topicQuota`
- `enabledBenefitCodes`

作用：

- 作为运行时权益能力的唯一汇总输出
- 供成长汇总接口和后续其他业务服务复用

### 6.3 成长汇总返回

`ContentUserGrowthVO` 从当前的：

- `userId`
- `pointBalance`
- `growthValue`
- `level`

扩展为额外包含：

- `levelBenefitSummary`

这样 `GET /content/user/growth/summary` 可直接对外返回：

- 当前等级
- 当前积分成长值
- 当前可用上传大小
- 当前是否支持高清视频
- 当前可用话题额度
- 当前显式启用的权益编码

## 7. 规则设计

### 7.1 默认等级规则

本轮继续复用现有高等级口径，避免额外引入第二套门槛：

- `level >= 5` 视为高等级
- 或 `growthValue >= 400` 视为高等级

在此基础上给出最小默认能力：

- 普通用户：
  - `uploadSizeLimitMb = 100`
  - `hdVideoEnabled = false`
  - `topicQuota = 10`
- 高等级用户：
  - `uploadSizeLimitMb = 500`
  - `hdVideoEnabled = true`
  - `topicQuota = 30`

### 7.2 显式权益覆盖规则

如果用户存在显式启用的权益，则优先按显式权益覆盖默认等级规则：

- `UPLOAD_EXPANDED` 启用时，上传大小按增强值返回
- `HD_VIDEO` 启用时，高清视频能力返回 `true`
- `TOPIC_QUOTA_EXPANDED` 启用时，话题额度按增强值返回
- `PRIORITY_CUSTOMER_SERVICE` 启用时，客服路由返回人工优先

### 7.3 处罚禁用优先规则

如果某项权益曾显式启用，但当前因成长处罚被禁用，则按“禁用优先”处理：

- 该项权益不能因为用户等级高而重新自动生效
- 运行时能力必须回退到普通默认能力
- 只有当恢复服务将对应权益恢复后，能力才重新回到启用态

这是本轮最关键的闭环约束，用于确保成长处罚的禁用效果不会被等级默认规则绕过。

### 7.4 画像缺失规则

当用户画像缺失时：

- `uploadSizeLimitMb = 100`
- `hdVideoEnabled = false`
- `topicQuota = 10`
- `enabledBenefitCodes = []`

只读查询不抛异常。

## 8. 组件设计

### 8.1 新增服务

新增服务契约：

- `IContentUserLevelBenefitService`

新增实现：

- `ContentUserLevelBenefitServiceImpl`

职责：

- 根据 `userId` 查询当前可用权益
- 计算能力摘要
- 判断某项权益当前是否可用
- 为其他服务提供统一运行时判定入口

建议方法：

- `ContentUserLevelBenefitSummaryVO getBenefitSummary(String userId)`
- `boolean hasEnabledBenefit(String userId, String benefitCode)`
- `int resolveTopicQuota(String userId)`

### 8.2 与恢复服务的职责边界

保留现有：

- `IContentUserLevelBenefitRecoveryService`

继续只负责：

- 按成长处罚记录恢复权益
- 维护子表恢复状态

不再继续承担：

- 运行时权益查询
- 能力摘要构造
- 业务入口判定

这样可以避免恢复编排服务和运行时服务相互污染。

### 8.3 客服路由改造

`ContentUserSupportServiceImpl` 中现有的 `PRIORITY_CUSTOMER_SERVICE` 判断改为依赖新统一服务。

新语义：

- 治理优先仍然最高
- 非治理场景下，如果统一权益服务判断 `PRIORITY_CUSTOMER_SERVICE` 已启用，则走人工优先
- 如果未显式启用，则继续回退到高等级默认规则

### 8.4 话题额度消费落点

`ContentUserSubscriptionServiceImpl.subscribe(...)` 新增对 `TOPIC` 类型的数量上限校验。

规则：

- 仅 `sourceType = TOPIC` 时校验额度
- 当前已存在的 `TOPIC` 订阅数达到 `topicQuota` 时拒绝新增
- 同一 `TOPIC` 重复订阅命中“更新已有记录”逻辑，不算新增超限
- 其他类型订阅不受本轮规则影响

报错文案建议：

- `当前等级可订阅话题数已达上限`

说明：

- 虽然 PRD 原文更偏向“创建更多话题”，但当前模块没有话题创建服务
- `TOPIC` 订阅是本模块中唯一稳定存在、且和话题能力直接相关的真实业务入口
- 采用其作为最小真实消费方，既不造假模块，也能形成可验证行为变化

## 9. 数据流

### 9.1 成长汇总查询

1. `growth/summary` 接收 `userId`
2. 查询用户画像
3. 调用统一权益服务计算能力摘要
4. 将 `pointBalance/growthValue/level` 与 `levelBenefitSummary` 一并返回

### 9.2 客服路由查询

1. `customer-service` 接收 `userId`
2. 查询用户画像
3. 先判断治理优先状态
4. 如非治理优先，则调用统一权益服务判断 `PRIORITY_CUSTOMER_SERVICE`
5. 若权益启用，则走人工优先；否则回退到高等级默认规则

### 9.3 话题订阅创建

1. `subscribe(userId, req)` 接收订阅请求
2. 若命中同一唯一键，走已有记录恢复/更新逻辑
3. 若 `sourceType != TOPIC`，直接沿用原逻辑
4. 若 `sourceType = TOPIC`，调用统一权益服务获取 `topicQuota`
5. 查询当前用户已有 `TOPIC` 类型订阅数量
6. 超限则抛业务异常，不落库
7. 未超限则创建订阅记录

## 10. 数据来源和判定语义

### 10.1 显式权益启用语义

本轮不新增等级权益主表，继续复用现有等级权益处罚子表的既有语义：

- 某权益被恢复后，如果存在 `currentEnabled = true` 且 `recoverStatus = RECOVERED` 的记录，可视为当前显式启用

### 10.2 显式权益禁用语义

如果存在尚未恢复的禁用记录，例如：

- `currentEnabled = false`
- `recoverStatus = PENDING_RECOVER`

则该权益视为当前显式禁用。

### 10.3 判定优先级

统一权益服务按以下优先级判断：

1. 显式禁用
2. 显式启用
3. 等级默认规则
4. 普通默认能力

这样可以保证成长处罚禁用优先级最高。

## 11. 测试设计

### 11.1 统一权益服务测试

新增 `ContentUserLevelBenefitServiceTest`，至少覆盖：

- 用户画像缺失时返回保守默认能力
- 普通用户返回基础上传/视频/话题额度
- 高等级用户返回增强上传/视频/话题额度
- 显式启用权益时覆盖默认等级规则
- 显式禁用权益时优先于高等级默认规则
- `enabledBenefitCodes` 与实际判定结果一致

### 11.2 成长汇总测试

补充 `ContentUserGrowthServiceTest`，验证：

- `summary` 返回新增的 `levelBenefitSummary`
- 字段值与统一权益服务输出一致

### 11.3 客服路由测试

更新 `ContentUserSupportServiceTest`，验证：

- `PRIORITY_CUSTOMER_SERVICE` 的显式权益判断改为走统一权益服务
- 原有治理优先、高等级默认优先语义不回归

### 11.4 订阅服务测试

更新 `ContentUserSubscriptionServiceTest`，验证：

- 普通用户 `TOPIC` 订阅达到 10 个后拒绝新增
- 高等级用户 `TOPIC` 订阅可扩到 30 个
- 显式启用 `TOPIC_QUOTA_EXPANDED` 时即便非高等级也可扩到 30 个
- 显式禁用 `TOPIC_QUOTA_EXPANDED` 时，即便高等级也回退到 10 个
- 非 `TOPIC` 类型订阅不受此限制
- 同一 `TOPIC` 重复订阅更新已有记录，不误判为超限

## 12. 验收标准

- 已新增统一等级权益判定服务，并可独立测试
- `growth/summary` 能返回等级权益能力摘要
- `customer-service` 显式权益判定改走统一服务且行为不回归
- `TOPIC` 订阅数量能根据权益能力动态放宽或收紧
- 成长处罚禁用后的运行时能力会立即收紧
- 申诉恢复或处罚到期恢复后，运行时能力会重新放开

## 13. 风险与后续演进

### 13.1 当前风险

- `UPLOAD_EXPANDED` 与 `HD_VIDEO` 本轮仍然只是统一能力输出，还没有直接接入真实上传或视频发布链路
- “更多话题”本轮落在 `TOPIC` 订阅额度，而不是话题创建额度，属于受当前模块边界限制下的最小真实落点

### 13.2 后续演进

后续如果内容发布、上传、视频模块具备稳定入口，可直接复用本轮统一权益服务继续接入：

- 上传模块读取 `uploadSizeLimitMb`
- 视频发布模块读取 `hdVideoEnabled`
- 话题创建模块读取 `topicQuota`
- 分发模块继续扩 `DISTRIBUTION_WEIGHT_LIGHT` 等后续权益编码

## 14. 结论

本次“更多等级权益消费方落地”选择在当前 `content/user` 模块内新增统一等级权益判定服务，并把等级权益能力对外收敛为统一摘要和统一判断入口。

这样可以在不漂移到外部模块的前提下：

- 把 `PRIORITY_CUSTOMER_SERVICE` 纳入统一运行时判定
- 为 `UPLOAD_EXPANDED`、`HD_VIDEO` 提供明确能力输出
- 为 `TOPIC_QUOTA_EXPANDED` 提供一个当前模块内真实存在的消费方
- 让成长处罚禁用与恢复结果真正进入运行时行为判定

从而把“等级权益只会被记录和恢复”推进到“等级权益可以被更多真实消费方感知”的下一阶段闭环。
