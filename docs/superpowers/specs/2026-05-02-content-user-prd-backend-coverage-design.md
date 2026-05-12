# 内容社区用户域 PRD 后端覆盖审计与分步实现设计

## 1. 背景

当前你要求对以下范围执行一体化工作：

- 审查 `docs/requirements/prd/内容社区-用户域-PRD.md` 中的需求
- 核查 `jeecg-module-content/src/main/java/org/jeecg/modules/content/user` Java 后端代码是否已实现
- 使用并行子代理思路做需求覆盖检查
- 产出汇总文档
- 对未实现需求按步骤落地，且代码需符合阿里巴巴规范

由于该 PRD 覆盖账号安全、资料、成长、关系、订阅、支持、治理等多个独立子域，如果直接进入“全量实现”，会同时跨越多个 controller、service、entity、req、vo 与测试集合，无法稳定控制审计口径、实现边界与回归范围。

因此本次先建立一套统一的“PRD -> Java 后端 -> 测试”覆盖审计设计，再基于审计结果分步实现未完成需求。

## 2. 目标

本次设计目标不是直接定义某一个业务功能，而是定义一套可执行的审计与实施流程，确保后续工作具备以下结果：

- 能把 PRD 用户故事映射到当前 Java 后端实现
- 能明确区分已实现、部分实现、未实现与待确认需求
- 能给出每条需求的代码证据或缺口说明
- 能输出一份后续开发可直接使用的覆盖矩阵文档
- 能基于缺口按优先级选择子域，逐步实现未完成需求

## 3. 范围

### 3.1 包含

- `docs/requirements/prd/内容社区-用户域-PRD.md`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user`
- 需求与代码、测试之间的映射与结论文档
- 基于审计结论的分步实现顺序建议

### 3.2 不包含

- 前端页面与前端交互闭环核查
- `content/user` 目录之外模块的扩展式联动改造
- 一次性并发实现所有未完成需求
- 与当前 PRD 无关的架构性重构

## 4. 审计方法

### 4.1 总体方法

采用“PRD 分域覆盖矩阵”方案，而不是纯接口清单或纯数据模型清单。

核心原因：

- PRD 关注的是用户可观察能力与流程闭环，不只是 API 是否存在
- 仅看 controller 容易漏掉“PRD 有要求但接口根本不存在”的能力
- 仅看 entity 又容易高估“已有字段但无业务行为”的实现程度

因此采用以下链路审计：

- 先按 PRD 能力域拆分审计单元
- 再逐单元检查 controller / service / entity / req / vo / test
- 最后回填到统一覆盖矩阵

### 4.2 并行子代理策略

审计执行阶段使用并行子代理思路，每个子代理只处理一个独立子域，避免上下文污染与交叉误判。

计划拆分为以下五个独立审计单元：

- 账号安全域
- 资料与隐私域
- 成长激励域
- 关系订阅域
- 支持治理域

每个子代理只输出本子域的：

- 已实现能力
- 部分实现能力
- 未实现能力
- 测试缺口
- 推荐后续实现项

主代理负责统一格式、去重、合并结论并生成总文档。

## 5. 分域设计

### 5.1 账号安全域

对应 PRD 中：

- 注册登录
- 验证码登录 / 密码登录 / 第三方登录
- 绑定解绑
- 设备管理
- 异常登录与风险拦截
- 找回密码
- 注销与冷静期

预期重点核查：

- `ContentAccountController`
- `ContentAccountServiceImpl`
- 账户相关 `req / gateway / entity / test`

### 5.2 资料与隐私域

对应 PRD 中：

- 基础资料
- 主页个性化
- 认证标识展示
- 字段可见性
- 头像 / 昵称历史
- 通知偏好
- 在线状态
- 搜索引擎索引
- 第三方授权管理

预期重点核查：

- `ContentUserProfileController`
- `ContentUserSettingsController`
- `ContentUserProfileServiceImpl`
- `ContentUserVisibilityPolicyServiceImpl`
- 隐私与通知相关 entity / test

### 5.3 成长激励域

对应 PRD 中：

- 等级与成长值
- 积分流水
- 勋章体系
- 权益差异
- 升级反馈
- 经验衰减与降级保护

预期重点核查：

- `ContentUserGrowthController`
- `ContentUserGrowthServiceImpl`
- `ContentUserGrowthLedger`
- `ContentUserPointLedger`
- `ContentUserBadgeDefinition`
- `ContentUserBadgeGrant`
- 相关测试

### 5.4 关系订阅域

对应 PRD 中：

- 关注 / 取消关注
- 分组管理
- 特别关注
- 关注流
- 关注推荐
- 订阅与通知频率
- 拉黑与屏蔽
- 粉丝管理
- 邀请分享

预期重点核查：

- `ContentUserRelationController`
- `ContentUserSubscriptionController`
- `ContentUserRelationServiceImpl`
- `ContentUserSubscriptionServiceImpl`
- 关系、分组、订阅相关 entity / test

### 5.5 支持治理域

对应 PRD 中：

- 举报
- 申诉
- 帮助中心
- 客服入口
- 用户状态流转
- 处罚恢复
- 审计留痕

预期重点核查：

- `ContentUserSupportController`
- `ContentUserSupportAdminController`
- `ContentUserGovernanceController`
- `ContentUserSupportServiceImpl`
- `ContentUserGovernanceServiceImpl`
- `ContentUserAppeal`
- `ContentUserReport`
- `ContentUserStatusRecord`
- `ContentUserAuditLog`
- 相关测试

## 6. 判定标准

每条 PRD 需求统一按以下四档判定：

### 6.1 已实现

同时满足以下条件中的大部分：

- 有明确 API 或对外服务行为
- 有 service 业务逻辑支撑
- 有必要的数据模型或状态表达
- 能形成用户可观察闭环
- 有测试证据，或代码行为非常明确

### 6.2 部分实现

常见情形：

- 只覆盖了 PRD 中的一部分场景
- 有 API 但规则不完整
- 有模型但缺关键流程
- 有正向能力但缺异常分支 / 状态流转 / 进度查询

### 6.3 未实现

常见情形：

- PRD 明确要求该能力，但代码中没有对应后端能力
- 没有接口、没有 service 闭环，也没有可复用的行为支撑

### 6.4 待确认

常见情形：

- 有字段、有对象或有零散逻辑，但无法确认是否对外生效
- 存在疑似支撑点，但缺测试或缺行为证据

## 7. 审计表结构

覆盖矩阵文档中，每条需求采用统一字段：

- `PRD 编号`
- `需求摘要`
- `子域`
- `实现状态`
- `代码证据`
- `测试证据`
- `缺口说明`
- `实现建议`
- `优先级`

其中：

- `代码证据` 尽量精确到 controller / service / entity / test 文件
- `测试证据` 单独列出，避免“有代码无测试”被误判为闭环
- `实现建议` 只写最小落地路径，不写泛化重构建议

## 8. 优先级规则

### 8.1 P0

直接影响 PRD 主链路闭环：

- 注册登录
- 找回密码
- 绑定解绑
- 状态治理
- 举报申诉
- 帮助中心
- 客服入口
- 审计留痕

### 8.2 P1

已有基础设施但业务规则不完整：

- 通知偏好
- 等级权益
- 关注与订阅管理
- 资料可见性
- 设备管理细节

### 8.3 P2

偏增强或展示类能力：

- 粉丝画像
- 邀请收益追踪
- 勋章展示细节
- 更新日志运营化

## 9. 审计产出

执行阶段将输出两类文档：

### 9.1 审计设计文档

即当前文档，用于约束审计口径与后续实施顺序。

### 9.2 覆盖矩阵结果文档

建议输出路径：

- `docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md`

建议结构：

- 审计范围与结论摘要
- 五大子域覆盖汇总
- 逐条需求覆盖矩阵
- 未实现需求清单
- 测试缺口清单
- 后续分步实现建议

## 10. 后续实施顺序

在审计完成后，不直接全量开发，而是按以下顺序逐域落地：

1. 支持治理域
2. 账号安全域
3. 关系订阅域
4. 资料与隐私域
5. 成长激励域

排序依据：

- 先处理 P0 主链路能力
- 优先做“已有局部实现、但最容易补成闭环”的子域
- 控制每次改动的回归范围

### 10.1 阶段进展

- `2026-05-11` 已按第四阶段补齐资料与隐私域中的通知设置后端闭环：新增通知设置查询/更新、渠道配置、免打扰规则和发送前策略判定，后续资料与隐私域优先继续处理昵称/头像历史、第三方授权、隐私缓存失效与真实通知发送链路接入。

## 11. 风险与边界

- 当前工作区已有未提交改动，后续实现阶段必须避免覆盖用户现有修改
- PRD 部分需求属于平台级能力，可能依赖 `content/user` 目录外基础设施；审计时需要明确标注“当前目录未实现”与“系统其他模块可能承担”的边界
- 部分能力即使已有 entity，也可能没有真正的外部行为闭环，必须谨慎区分“模型存在”和“需求已实现”
- 审计结论应以代码证据为准，不能仅凭命名推断

## 12. 验收标准

本设计被采纳后，后续审计结果应满足：

- 能覆盖 PRD 用户故事的主要后端能力范围
- 每个子域都有独立审计结果
- 每条需求都有实现状态，不留空白
- 关键结论附带代码证据
- 未实现需求有明确优先级和最小实现建议
- 可基于审计结果直接进入下一阶段的分步实现
