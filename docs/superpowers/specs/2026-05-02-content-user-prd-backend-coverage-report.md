# 内容社区用户域 PRD 后端覆盖审计报告

- 审计范围：`docs/requirements/prd/内容社区-用户域-PRD.md`
- 代码范围：`jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user`
- 测试范围：`jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user`
- 审计依据：`docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-design.md`

## 0. 增量实现记录

- `2026-05-05` 已新增账号安全域闭环：`register/email` 独立邮箱注册、手机号/邮箱绑定解绑、敏感操作二次校验与账号审计留痕。
- `2026-05-02` 已完成支持治理域第一阶段：`help-center` 结构化分类推荐、`customer-service` 按用户画像分层、`appeal/list` 分页返回。
- `2026-05-02` 已完成支持治理域第二阶段的首批闭环：`support/admin/appeal/handle` 在申诉通过时联动恢复可恢复的治理状态，并新增 `GET /content/user/governance/status/history` 分页查询状态历史。
- `2026-05-02` 已新增自动解禁能力：到期的 `MUTED / RECOMMENDATION_LIMITED / FROZEN / BANNED` 状态会通过定时任务自动恢复并补审计记录。
- `2026-05-02` 已新增成长处罚恢复编排：申诉通过和到期自动恢复会联动恢复积分、成长值、等级、勋章状态与首批等级权益。
- `2026-05-02` 已新增成长处罚来源建模扩展：`governance/status/change` 与 `support/admin/report/handle` 可统一写入成长处罚主记录，并补齐 `sourceType/sourceId/sourceStatus` 来源字段与幂等建档规则。
- `2026-05-02` 已新增成长处罚真实执行引擎：治理处罚入口与举报处理入口可真实执行积分、成长值、等级、勋章和首批等级权益处罚，并生成可恢复回放的执行快照与执行审计。
- `2026-05-02` 已新增更多等级权益消费方：统一等级权益判定服务接管运行时权益判断，成长汇总返回上传大小、高清视频与话题额度能力摘要，`TOPIC` 订阅入口可真实感知额度变化。
- `2026-05-11` 已新增资料与隐私域通知设置闭环：`settings/notification` 查询与更新通知开关、渠道配置、免打扰规则，并提供发送前通知策略判定，安全类通知按白名单豁免。

## 1. 总结论

- 当前 `content/user` 目录已经形成了用户域后端的基础骨架，覆盖了 `手机号注册`、`邮箱注册`、`手机号/邮箱绑定解绑`、`密码找回`、`注销冷静期`、`资料更新`、`隐私可见性`、`通知设置`、`积分/成长记账`、`关注/拉黑/屏蔽`、`订阅管理`、`举报/申诉`、`状态治理`、`设备会话管理` 等最小能力。
- 现状以“基础聚合 + 静态规则 + 单表 service”为主，和 PRD 相比仍存在大量能力缺口，尤其集中在 `登录方式多样化`、`一步式换绑与第三方绑定`、`异常登录与风控`、`通知偏好细化`、`勋章体系行为闭环`、`关注流/推荐/粉丝/邀请`、`更细粒度等级权益规则`。
- `支持治理域` 已补齐 `help-center` 分类级客服联动、`customer-service` 分层路由、`appeal/list` 分页返回、`status/history` 分页查询，以及申诉通过/自动到期恢复后的成长处罚联动恢复。
- `成长激励域` 和 `关系订阅域` 的实体模型比服务闭环更完整，存在“有表/有字段，但无对外行为或无业务规则”的情况，需要谨慎判为“部分实现”或“未实现”。
- 测试层以 service 单测和少量 WebMvc 测试为主，已覆盖部分关键正向路径，但对于复杂 PRD 能力仍存在明显缺测。

## 2. 总览矩阵

| 子域 | 已实现 | 部分实现 | 未实现 | 待确认 | 测试缺口 |
| --- | --- | --- | --- | --- | --- |
| 账号安全域 | 6 | 3 | 10 | 1 | 4 |
| 资料与隐私域 | 5 | 3 | 4 | 2 | 3 |
| 成长激励域 | 2 | 2 | 8 | 1 | 4 |
| 关系订阅域 | 6 | 3 | 8 | 2 | 4 |
| 支持治理域 | 7 | 2 | 6 | 1 | 4 |

## 3. 账号安全域

### 已实现

| PRD 编号 | 需求摘要 | 实现状态 | 代码证据 | 测试证据 | 缺口说明 | 实现建议 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 6 | 手机号注册 | 已实现 | `controller/ContentAccountController.java`、`service/impl/ContentAccountServiceImpl.java`、`gateway/impl/SystemUserAccountGatewayImpl.java`、`req/account/ContentRegisterReq.java` | `service/ContentAccountServiceTest.java` | 当前仍未接入邀请码或验证码链路 | 后续补邀请码与登录编排 | P0 |
| 7 | 邮箱注册 | 已实现 | `controller/ContentAccountController.java`、`service/impl/ContentAccountServiceImpl.java`、`gateway/impl/SystemUserAccountGatewayImpl.java`、`req/account/ContentEmailRegisterReq.java` | `service/ContentAccountServiceTest.java`、`controller/ContentAccountControllerWebMvcTest.java` | 已有独立邮箱注册入口，但仍未接入邀请码校验 | 后续补邀请码与登录编排 | P0 |
| 14、15 | 绑定手机号、绑定邮箱 | 已实现 | `controller/ContentAccountController.java`、`service/impl/ContentAccountServiceImpl.java`、`gateway/impl/SystemUserAccountGatewayImpl.java`、`entity/ContentUserAuditLog.java` | `service/ContentAccountServiceTest.java`、`controller/ContentAccountControllerWebMvcTest.java` | 当前只覆盖手机号/邮箱，未覆盖第三方账号绑定 | 后续补第三方绑定模型 | P0 |
| 23、24 | 通过用户ID/手机号/邮箱定位账号重置密码，并要求二次校验 | 已实现 | `controller/ContentAccountController.java`、`service/impl/ContentAccountServiceImpl.java`、`gateway/impl/SystemUserAccountGatewayImpl.java`、`req/account/ContentPasswordResetReq.java` | `service/ContentAccountServiceTest.java` | 只实现重置，不含找回流程中的验证码/身份核验编排 | 后续补独立找回流程状态机 | P0 |
| 25、26 | 注销申请、冷静期完成注销、冷静期撤销注销 | 已实现 | `controller/ContentAccountController.java`、`service/impl/ContentAccountServiceImpl.java`、`enums/ContentUserStatusEnum.java`、`entity/ContentUserStatusRecord.java` | `service/ContentAccountServiceTest.java`、`controller/ContentAccountControllerWebMvcTest.java` | 注销前置校验只覆盖待处理申诉 | 后续补未完成事项清单校验 | P0 |
| 13、21、22 | 设备会话列表与指定设备下线 | 已实现 | `controller/ContentUserGovernanceController.java`、`service/impl/ContentUserGovernanceServiceImpl.java`、`entity/ContentUserDeviceSession.java` | 无专门 WebMvc 测试；`service/ContentUserGovernanceServiceTest.java` 仅覆盖状态变更 | 会话查询/下线已存在，但缺异常登录识别与提醒 | 后续补设备会话测试与安全事件联动 | P1 |

### 部分实现

| PRD 编号 | 需求摘要 | 实现状态 | 代码证据 | 测试证据 | 缺口说明 | 实现建议 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 17 | 换绑/解绑手机号或邮箱，敏感操作二次校验 | 部分实现 | `controller/ContentAccountController.java`、`service/impl/ContentAccountServiceImpl.java`、`gateway/impl/SystemUserAccountGatewayImpl.java`、`entity/ContentUserAuditLog.java` | `service/ContentAccountServiceTest.java`、`controller/ContentAccountControllerWebMvcTest.java` | 已支持绑定、解绑和二次校验，但未提供一步式换绑编排 | 后续补换绑流程与更细粒度校验凭证 | P0 |
| 20 | 异常批量注册识别与拦截 | 部分实现 | `gateway/impl/SystemUserAccountGatewayImpl.java` | 无 | 只有手机号/邮箱/用户名去重校验，没有风控规则 | 在 service 或 biz 中补频率/来源风控 | P1 |
| 25 | 注销前校验未完成事项 | 部分实现 | `service/impl/ContentAccountServiceImpl.java` | `service/ContentAccountServiceTest.java` | 仅校验待处理申诉，未覆盖违规处理中、订单未结清等 | 扩展前置校验策略 | P0 |

### 未实现

| PRD 编号 | 需求摘要 | 实现状态 | 代码证据 | 测试证据 | 缺口说明 | 实现建议 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 8、12 | 第三方账号登录 / 第三方快捷登录 | 未实现 | 未发现相关 controller/service/gateway | 无 | 没有第三方登录接口、绑定记录或授权模型 | 新增第三方账号绑定表与登录编排 | P0 |
| 9 | 邀请码前置校验注册 | 未实现 | `req/account/ContentRegisterReq.java` 仅有 `inviteCode` 字段 | 无 | 邀请码未参与任何校验或落库 | 增加邀请码校验服务 | P1 |
| 10、11 | 验证码登录 / 密码登录 | 未实现 | 未发现登录接口 | 无 | 当前模块只负责注册和重置，不含登录编排 | 评估是否落在平台账号域；若需本模块承接则新增登录编排接口 | P0 |
| 16 | 绑定第三方账号 | 未实现 | 未发现第三方账号实体或接口 | 无 | 无模型、无接口 | 新增绑定模型与敏感操作校验 | P1 |
| 18、19 | 异常设备/异地登录提醒、频繁失败风险拦截 | 未实现 | `entity/ContentUserDeviceSession.java` 只有会话字段 | 无 | 没有安全事件识别、提醒、限流或冻结能力 | 引入登录审计与风险策略 | P0 |
| 21 | 最近登录设备列表中的时间/地点/设备异常检测 | 未实现 | `entity/ContentUserDeviceSession.java` 有字段 | 无 | 仅能查会话，未做异常识别 | 补充风险标识与提示接口 | P1 |

### 待确认

| PRD 编号 | 需求摘要 | 实现状态 | 代码证据 | 测试证据 | 缺口说明 | 实现建议 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 15 | 邮箱绑定状态展示 | 待确认 | `entity/ContentUserProfile.java` 与系统用户邮箱字段可联动，但 profile VO 未直接体现绑定状态 | 无 | 平台账号里可能已有展示能力，但 `content/user` 目录内没有明确出参 | 如要求在用户域独立展示，新增绑定状态字段到 VO | P1 |

### 测试缺口

- `ContentAccountControllerWebMvcTest` 已补邮箱注册与绑定解绑，但仍未覆盖手机号注册和重置密码接口。
- `ContentUserGovernanceServiceTest` 没有覆盖设备会话列表、设备下线、权限检查。
- 邀请码校验、第三方绑定和异常登录风控仍缺少红灯测试。
- 没有针对异常登录/风控场景的测试基线。

## 4. 资料与隐私域

### 已实现

| PRD 编号 | 需求摘要 | 实现状态 | 代码证据 | 测试证据 | 缺口说明 | 实现建议 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 27、28、29、30、31 | 基础资料、主页背景图、主题色、模块排序、认证标识更新与展示 | 已实现 | `controller/ContentUserProfileController.java`、`service/impl/ContentUserProfileServiceImpl.java`、`req/profile/ContentUserProfileUpdateReq.java`、`vo/ContentUserProfileVO.java`、`entity/ContentUserProfile.java` | `service/ContentUserProfileServiceTest.java` | 已有更新模型与展示字段，但缺细粒度校验策略 | 后续补频率限制与审核策略 | P1 |
| 32 | 字段可见性控制 | 已实现 | `service/impl/ContentUserProfileServiceImpl.java`、`service/impl/ContentUserVisibilityPolicyServiceImpl.java`、`entity/ContentUserPrivacySetting.java` | `service/ContentUserProfileServiceTest.java`、`service/ContentUserVisibilityPolicyServiceTest.java` | 当前只在生日字段上通过 `VO.from` 显式体现 | 后续扩展到更多字段显隐 | P1 |
| 77、78 | 在线状态可见性、搜索引擎索引开关 | 已实现 | `req/profile/ContentUserPrivacyUpdateReq.java`、`service/impl/ContentUserProfileServiceImpl.java`、`entity/ContentUserPrivacySetting.java` | 无专门测试 | 已支持设置保存，但未提供专门查询与外部行为断言 | 后续增加设置读取与生效测试 | P1 |
| 80 | 账户安全设置入口中的可见性/设置能力基础 | 已实现 | `controller/ContentUserSettingsController.java` | 无 | 只覆盖隐私设置，不含设备管理/密码修改统一门户 | 后续整合设置聚合接口 | P2 |
| 74、75 | 通知开关、渠道配置与免打扰规则 | 已实现 | `controller/ContentUserSettingsController.java`、`service/impl/ContentUserNotificationSettingServiceImpl.java`、`req/settings/ContentUserNotificationUpdateReq.java`、`vo/ContentUserNotificationSettingVO.java`、`entity/ContentUserNotificationSetting.java` | `service/ContentUserNotificationSettingServiceTest.java`、`controller/ContentUserControllerWebMvcTest.java`、`req/ContentUserReqValidationTest.java` | 已支持查询/更新通知开关、渠道配置、免打扰规则，并提供发送前策略判定；尚未接入真实消息发送模块 | 后续在消息发送链路调用 `canSendNotice` | P1 |

### 部分实现

| PRD 编号 | 需求摘要 | 实现状态 | 代码证据 | 测试证据 | 缺口说明 | 实现建议 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 33 | 昵称/头像历史记录与回溯展示 | 部分实现 | `entity/ContentUserProfile.java` 存在 `nicknameHistoryJson`、`avatarHistoryJson` 字段 | 无 | 更新资料时未维护历史，也未在 VO 中返回 | 在更新 service 中补历史写入与展示规则 | P1 |
| 76 | 动态/收藏等可见性 | 部分实现 | `entity/ContentUserPrivacySetting.java` 中有 `dynamicVisibility` | 无 | 仅存储字段，没有明确查询策略落地到内容查询 | 补 visibility service 与对外接口 | P1 |
| 112 | 通知关闭后不再发送 | 部分实现 | `service/impl/ContentUserNotificationSettingServiceImpl.java` | `service/ContentUserNotificationSettingServiceTest.java` | 已提供按通知类型、渠道、免打扰和安全类豁免的发送前判定，但真实消息发送模块尚未接入 | 在站内信、推送、邮件等发送入口统一调用通知策略服务 | P0 |

### 未实现

| PRD 编号 | 需求摘要 | 实现状态 | 代码证据 | 测试证据 | 缺口说明 | 实现建议 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 30 | 手机号/邮箱绑定状态展示 | 未实现 | `vo/ContentUserProfileVO.java` 未见绑定状态字段 | 无 | 资料 VO 未输出绑定状态 | 补充绑定状态 VO 字段 | P1 |
| 79 | 第三方授权列表与撤销授权 | 未实现 | 未发现第三方授权实体、接口或 service | 无 | 完全缺失 | 新增授权记录聚合与撤销接口 | P1 |
| 76 | 浏览记录、点赞动态、收藏夹显隐 | 未实现 | 未发现对应字段或策略接口 | 无 | 当前隐私模型未覆盖这些对象级显隐 | 扩展隐私模型 | P2 |
| 111 | 隐私即时生效与缓存失效 | 未实现 | 未发现缓存失效或跨模块消费侧策略实现 | 无 | 当前只有配置存储和可见性服务，缓存页面失效链路未体现 | 需要跨模块联动实现 | P0 |

### 待确认

| PRD 编号 | 需求摘要 | 实现状态 | 代码证据 | 测试证据 | 缺口说明 | 实现建议 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 77 | 在线状态展示控制 | 待确认 | `ContentUserPrivacySetting` 存储了 `onlineStatusVisible` | 无 | 仅看到存储，没有对应展示接口 | 如果前端依赖用户资料接口，需补 VO 或状态查询 | P2 |
| 78 | 搜索引擎索引控制 | 待确认 | `ContentUserPrivacySetting` 存储了 `allowSearchEngineIndex` | 无 | 需要站点地图/索引侧消费，当前目录未体现 | 标注为跨模块实现点 | P2 |

### 测试缺口

- `ContentUserProfileServiceTest` 仅覆盖“生日不可见”一条路径，没有覆盖资料更新、主页背景图、主题色、认证字段。
- 没有 `ContentUserProfileController` 与 `ContentUserSettingsController` 的 WebMvc 测试。
- `ContentUserNotificationSetting` 已补 service/controller/请求校验测试，但真实通知发送模块接入仍缺集成测试。
- 昵称/头像历史、在线状态、搜索索引设置都缺行为测试。

## 5. 成长激励域

### 已实现

| PRD 编号 | 需求摘要 | 实现状态 | 代码证据 | 测试证据 | 缺口说明 | 实现建议 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 46、82 | 积分与成长值分账、明细账本独立 | 已实现 | `controller/ContentUserGrowthController.java`、`service/impl/ContentUserGrowthServiceImpl.java`、`entity/ContentUserPointLedger.java`、`entity/ContentUserGrowthLedger.java` | `service/ContentUserGrowthServiceTest.java` | 仅记录流水，查询明细接口缺失 | 后续补明细查询 API | P1 |
| 81 | 成长值驱动等级摘要 | 已实现 | `service/impl/ContentUserGrowthServiceImpl.java`、`vo/ContentUserGrowthVO.java`、`entity/ContentUserProfile.java` | `controller/ContentUserGrowthController.java` 暴露 summary；无 summary 单测 | 等级算法简单按 100 递增，没有权益规则 | 后续抽离等级策略 | P1 |

### 部分实现

| PRD 编号 | 需求摘要 | 实现状态 | 代码证据 | 测试证据 | 缺口说明 | 实现建议 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 39、40、41、42 | 多来源行为获得积分/成长值 | 部分实现 | `controller/ContentUserGrowthController.java`、`service/impl/ContentUserGrowthServiceImpl.java` | `service/ContentUserGrowthServiceTest.java` | 通过通用 `recordBehavior` 支持记账，但没有来源规则、日上限与任务体系 | 补行为规则策略与限额 | P1 |
| 34、37、38 | 勋章定义/发放/回收数据模型 | 部分实现 | `entity/ContentUserBadgeDefinition.java`、`entity/ContentUserBadgeGrant.java`、对应 mapper | 无 | 只有表模型，没有 controller/service/发放回收逻辑 | 新增勋章服务与审计 | P1 |

### 未实现

| PRD 编号 | 需求摘要 | 实现状态 | 代码证据 | 测试证据 | 缺口说明 | 实现建议 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 34、35、36 | 勋章分类、条件进度、佩戴展示 | 未实现 | 未发现 badge service/controller/VO | 无 | 勋章没有对外行为 | 新增 badge 聚合接口 | P1 |
| 39 | 每日上限防刷分 | 未实现 | 未发现限额规则 | 无 | 记账不做上限控制 | 补日维度限额校验 | P1 |
| 43、44、45 | 积分兑换、解锁功能、虚拟礼物 | 未实现 | 未发现积分消耗与权益接口 | 无 | 完全缺失 | 增加积分消耗场景与流水 | P1 |
| 46 | 积分明细按时间/类型筛选查询 | 未实现 | 仅有实体与 mapper | 无 | 缺对外查询接口 | 新增 ledger 查询接口 | P1 |
| 83、84、85 | 等级标识特权、功能特权、内容分发加权 | 部分实现 | `service/impl/ContentUserLevelBenefitServiceImpl.java`、`service/impl/ContentUserGrowthServiceImpl.java`、`service/impl/ContentUserSupportServiceImpl.java`、`service/impl/ContentUserSubscriptionServiceImpl.java`、`vo/ContentUserLevelBenefitSummaryVO.java`、`vo/ContentUserGrowthVO.java` | `service/ContentUserLevelBenefitServiceTest.java`、`service/ContentUserGrowthServiceTest.java`、`service/ContentUserSupportServiceTest.java`、`service/ContentUserSubscriptionServiceTest.java` | 已补统一等级权益判定、上传大小/高清视频/话题额度能力摘要与 `TOPIC` 订阅额度消费；内容分发加权和真实上传/视频链路仍未接入 | 后续继续接入发布、上传和分发模块 | P1 |
| 86、87 | 升级提示、经验衰减、降级保护 | 未实现 | 未发现事件或定时策略 | 无 | 完全缺失 | 增加成长策略服务 | P2 |

### 待确认

| PRD 编号 | 需求摘要 | 实现状态 | 代码证据 | 测试证据 | 缺口说明 | 实现建议 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 38、113 | 敏感积分扣减/勋章回收审计 | 待确认 | `ContentUserGrowthServiceImpl.java` 会写行为奖励审计，`ContentUserAuditLog.java` 有审计模型 | 无 | 奖励类有审计，但扣减/回收未实现 | 后续在消耗与回收场景统一留痕 | P1 |

### 测试缺口

- `ContentUserGrowthServiceTest` 只验证双账本写入，没有覆盖等级计算、余额保护、审计日志。
- 没有 `ContentUserGrowthController` 的 WebMvc 测试。
- 勋章相关完全没有测试基线。
- 没有积分明细查询与消耗场景测试。

## 6. 关系订阅域

### 已实现

| PRD 编号 | 需求摘要 | 实现状态 | 代码证据 | 测试证据 | 缺口说明 | 实现建议 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 47、48 | 关注 / 取消关注 | 已实现 | `controller/ContentUserRelationController.java`、`service/impl/ContentUserRelationServiceImpl.java`、`entity/ContentUserRelation.java` | `service/ContentUserRelationServiceTest.java` | 仅维护关系，不含关注流 | 后续补 feed 能力 | P1 |
| 51 | 特别关注 / 取消特别关注 | 已实现 | `controller/ContentUserRelationController.java`、`service/impl/ContentUserRelationServiceImpl.java` | `service/ContentUserRelationServiceTest.java` | 无提醒与优先展示能力 | 后续补通知联动 | P1 |
| 61、63、64、66、67 | 拉黑、双向取消关注、解除拉黑、无通知对方 | 已实现 | `service/impl/ContentUserRelationServiceImpl.java`、`service/impl/ContentUserVisibilityPolicyServiceImpl.java` | `service/ContentUserRelationServiceTest.java`、`service/ContentUserVisibilityPolicyServiceTest.java` | 行为边界基础已在关系与可见性服务中体现 | 后续补黑名单列表接口 | P0 |
| 68、72、73 | 屏蔽用户、可恢复、区分拉黑与屏蔽边界 | 已实现 | `controller/ContentUserRelationController.java`、`service/impl/ContentUserRelationServiceImpl.java`、`service/impl/ContentUserVisibilityPolicyServiceImpl.java` | `service/ContentUserRelationServiceTest.java` | 当前只支持按用户屏蔽 | 后续补内容/话题/关键词屏蔽 | P1 |
| 54、55、56、58、60 | 订阅内容源、通知渠道/频率字段、统一列表、暂停/取消 | 已实现 | `controller/ContentUserSubscriptionController.java`、`service/impl/ContentUserSubscriptionServiceImpl.java`、`entity/ContentUserSubscription.java` | `service/ContentUserSubscriptionServiceTest.java`、`controller/ContentUserSubscriptionControllerWebMvcTest.java` | 没有订阅广场/发现能力 | 后续补发现与推荐 | P1 |
| 107、108 | 拉黑高于关注，屏蔽只影响本人消费视图 | 已实现 | `service/impl/ContentUserRelationServiceImpl.java`、`service/impl/ContentUserVisibilityPolicyServiceImpl.java` | `service/ContentUserRelationServiceTest.java` | 已体现规则优先级 | 持续补回归测试 | P0 |

### 部分实现

| PRD 编号 | 需求摘要 | 实现状态 | 代码证据 | 测试证据 | 缺口说明 | 实现建议 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 49 | 关注分组管理 | 部分实现 | `entity/ContentUserRelationGroup.java`、`mapper/ContentUserRelationGroupMapper.java`、`req/relation/ContentFollowReq.java` | `service/ContentUserRelationServiceTest.java` 仅断言 groupId 可挂载 | 有分组字段和实体，但没有分组 CRUD 接口 | 增加分组管理接口 | P1 |
| 57、58 | 会员/付费系列订阅与渠道频率配置 | 部分实现 | `ContentSubscriptionReq.java`、`ContentUserSubscription.java` | `service/ContentUserSubscriptionServiceTest.java` | 订阅模型通用支持，但无付费/会员能力约束 | 后续加 sourceType 策略 | P2 |
| 69、71 | 不感兴趣、话题阶段性屏蔽 | 部分实现 | `entity/ContentUserRelation.java`、`ContentUserSubscription.java` 有推荐理由等扩展字段 | 无 | 只有用户屏蔽，不含内容/话题级降噪 | 新增内容降噪模型 | P1 |

### 未实现

| PRD 编号 | 需求摘要 | 实现状态 | 代码证据 | 测试证据 | 缺口说明 | 实现建议 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 50 | 关注流查看关注对象动态 | 未实现 | 未发现 feed 相关 controller/service | 无 | 只维护关系，没有关注流查询 | 新增 follow feed 接口 | P1 |
| 52 | 关注推荐与推荐理由 | 未实现 | `entity/ContentUserRelation.java` 有 `recommendationReason` 字段 | 无 | 只有字段，无推荐算法与接口 | 新增推荐服务 | P1 |
| 53 | 批量取消关注/移入移出分组 | 未实现 | `req/relation/ContentBatchRelationReq.java` 存在 | 无 | 没有 controller/service 消费该请求 | 增加批量管理接口 | P1 |
| 59 | 订阅广场发现 | 未实现 | 未发现 discovery API | 无 | 完全缺失 | 新增订阅广场接口 | P2 |
| 65 | @ 功能对双方失效 | 未实现 | `IContentUserVisibilityPolicyService` 有 `canMention`，但无 controller/service 使用证据 | 无 | 规则函数存在，未形成外部闭环 | 在互动服务接入提及校验 | P1 |
| 68、69、70、71 | 内容不感兴趣、关键词屏蔽、话题屏蔽 | 未实现 | 未发现相关 entity/controller/service | 无 | 仅用户级 mute | 补降噪配置模型 | P1 |
| 90、91、92、93、94 | 粉丝列表/趋势/画像、邀请码收益、评论区角色标签、版主管理扩展 | 未实现 | 未发现相关接口 | 无 | 完全缺失 | 分阶段落地，先粉丝列表与角色标签 | P2 |

### 待确认

| PRD 编号 | 需求摘要 | 实现状态 | 代码证据 | 测试证据 | 缺口说明 | 实现建议 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 54、55、56 | 订阅专题/Tag/栏目 | 待确认 | `ContentSubscriptionReq.java` 用 `sourceType/sourceId` 抽象来源 | `service/ContentUserSubscriptionServiceTest.java` 只以 `TOPIC` 为例 | 模型上支持多来源，但未明确枚举边界 | 后续加 sourceType 枚举约束 | P2 |
| 88、89 | 互关标识与仅互关可见 | 待确认 | `ContentUserVisibilityPolicyServiceImpl.java` 支持 `MUTUAL_ONLY` | `service/ContentUserVisibilityPolicyServiceTest.java` 未覆盖互关场景 | 可见性语义存在，但没有专门对外展示接口 | 增加 relation VO 与可见性测试 | P1 |

### 测试缺口

- 缺少 `ContentUserRelationController` WebMvc 测试。
- `ContentUserRelationServiceTest` 没有覆盖 follow/unfollow 基本路径、group 管理、批量操作。
- 缺少订阅列表过滤、sourceType 边界、通知频率校验测试。
- 内容级屏蔽、关注流、推荐、粉丝等能力没有测试基线。

## 7. 支持治理域

### 已实现

| PRD 编号 | 需求摘要 | 实现状态 | 代码证据 | 测试证据 | 缺口说明 | 实现建议 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 95 | 用户举报内容/评论/用户并上传证据 | 已实现 | `controller/ContentUserSupportController.java`、`service/impl/ContentUserSupportServiceImpl.java`、`req/support/ContentReportCreateReq.java`、`entity/ContentUserReport.java`、`entity/ContentUserGrowthPenaltyRecord.java` | `service/ContentUserSupportServiceTest.java`、`controller/ContentUserSupportControllerWebMvcTest.java` | 创建与进度查询已具备，`report/handle` 已补处罚性结果的成长处罚建档 | 后续补举报类型枚举与上传策略 | P0 |
| 96 | 处罚申诉创建、进度跟踪、结果回写 | 已实现 | `controller/ContentUserSupportController.java`、`controller/ContentUserSupportAdminController.java`、`service/impl/ContentUserSupportServiceImpl.java`、`entity/ContentUserAppeal.java` | `service/ContentUserSupportServiceTest.java`、`controller/ContentUserSupportAdminControllerWebMvcTest.java` | 已有用户侧与管理侧闭环，并在申诉通过时联动恢复治理状态与成长处罚 | 后续补更丰富的处罚执行项 | P0 |
| 99、100、105、106 | 用户状态定义、状态变更记录、审计留痕、人工恢复基础 | 已实现 | `controller/ContentUserGovernanceController.java`、`service/impl/ContentUserGovernanceServiceImpl.java`、`entity/ContentUserStatusRecord.java`、`entity/ContentUserAuditLog.java`、`enums/ContentUserStatusEnum.java` | `service/ContentUserGovernanceServiceTest.java` | 已实现状态记录、历史分页、自动恢复与审计日志 | 后续补更细筛选条件与设备/权限联动测试 | P0 |
| 101、102、103、104 | 状态对行为权限的限制 | 已实现 | `service/impl/ContentUserGovernanceServiceImpl.java` | 无专门测试 | 已覆盖禁言、限制推荐、冻结、封禁、注销中的行为限制 | 增加权限检查测试 | P0 |
| 113 | 敏感治理操作留痕 | 已实现 | `ContentUserSupportServiceImpl.java`、`ContentUserGovernanceServiceImpl.java`、`ContentUserGrowthServiceImpl.java` | `service/ContentUserSupportServiceTest.java`、`service/ContentUserGovernanceServiceTest.java` | 目前覆盖举报/申诉/状态变更/奖励行为 | 后续补更多敏感操作场景 | P0 |
| 97 | 帮助中心入口存在 | 已实现 | `controller/ContentUserSupportController.java`、`vo/ContentHelpCenterVO.java`、`vo/ContentHelpCenterEntryVO.java` | `service/ContentUserSupportServiceTest.java`、`controller/ContentUserSupportControllerWebMvcTest.java` | 已支持结构化分类推荐与分类级客服联动 | 后续补 FAQ 明细页与搜索 | P0 |
| 98 | 客服入口存在 | 已实现 | `controller/ContentUserSupportController.java`、`vo/ContentCustomerServiceVO.java`、`service/impl/ContentUserSupportServiceImpl.java` | `service/ContentUserSupportServiceTest.java` | 已支持默认/高等级/治理异常用户分层路由 | 后续补更细优先级与负载策略 | P0 |
| 96、106 | 申诉恢复后保留历史并驱动处罚恢复 | 已实现 | `service/impl/ContentUserSupportServiceImpl.java`、`service/impl/ContentUserGovernanceServiceImpl.java`、`service/impl/ContentUserGrowthPenaltyRecoveryServiceImpl.java`、`service/impl/ContentUserGrowthPenaltyRecordServiceImpl.java`、`entity/ContentUserGrowthPenaltyRecord.java`、`entity/ContentUserLevelBenefitPenaltyRecord.java` | `service/ContentUserSupportServiceTest.java`、`service/ContentUserGovernanceServiceTest.java`、`service/ContentUserGrowthPenaltyRecoveryServiceTest.java`、`service/ContentUserGrowthPenaltyRecordServiceTest.java` | 已补齐治理状态、积分、成长值、等级、勋章状态以及首批 `PRIORITY_CUSTOMER_SERVICE` 等级权益恢复编排，并补齐治理处罚与举报处理的来源建档 | 后续补更多等级权益消费方与处罚执行细项 | P0 |

### 部分实现

| PRD 编号 | 需求摘要 | 实现状态 | 代码证据 | 测试证据 | 缺口说明 | 实现建议 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 105 | 记录操作人、触发原因、开始/结束时间与备注 | 部分实现 | `ContentUserStatusRecord.java`、`ContentUserGovernanceServiceImpl.java`、`ContentUserGovernanceController.java` | `service/ContentUserGovernanceServiceTest.java`、`controller/ContentUserControllerWebMvcTest.java` | 已支持历史分页查询，但缺更完整的筛选条件与设备/权限联动测试 | 继续补筛选能力与测试矩阵 | P1 |

### 未实现

| PRD 编号 | 需求摘要 | 实现状态 | 代码证据 | 测试证据 | 缺口说明 | 实现建议 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 99、100 | 游客到注册未完善资料的自动流转展示 | 未实现 | `ContentUserStatusEnum` 有状态，`ContentAccountServiceImpl` 会设 `REGISTERED_INCOMPLETE` | 无 | 没有用户侧“生命周期阶段”接口或引导闭环 | 可在 profile/status 接口中补足展示 | P2 |

### 待确认

| PRD 编号 | 需求摘要 | 实现状态 | 代码证据 | 测试证据 | 缺口说明 | 实现建议 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 95 | 举报目标支持 `用户 / 内容 / 评论` 的完整枚举约束 | 待确认 | `ContentReportCreateReq.java`、`ContentUserReport.java` 使用字符串 `targetType` | 无 | 模型可表达，但未见枚举约束 | 后续补枚举与校验测试 | P2 |

### 测试缺口

- `ContentUserSupportControllerWebMvcTest` 与 `ContentUserSupportServiceTest` 仍围绕旧版 `help-center` / 固定 `customer-service` 行为，尚未覆盖设计文档要求。
- `ContentUserGovernanceServiceTest` 仍没有覆盖 `canExecuteAction`、设备会话管理，但已补上状态历史查询和自动解禁基线。
- 缺少举报/申诉与治理恢复联动测试。
- 客服分层基线已补齐，仍缺更完整的自动恢复跨域联动测试。

## 8. 未实现需求优先级

### P0

- `PRD-8、10、11、12`：第三方登录、验证码登录、密码登录。
- `PRD-17`：手机号/邮箱绑定解绑与敏感操作二次校验。
- `PRD-18、19`：异常登录识别、风险拦截、限流或冻结。
- `PRD-97、98`：帮助中心分类级客服联动、高等级/治理异常用户优先客服。
- `PRD-96、106`：申诉结果驱动处罚恢复、自动/人工恢复闭环。
- `PRD-111`：隐私即时生效与缓存失效。

### P1

- `PRD-33`：昵称/头像历史维护与展示。
- `PRD-34-46、83-87`：勋章服务、积分明细查询、等级权益、升级提示、经验衰减。
- `PRD-49、50、52、53、59、68-71、90-94`：分组管理、关注流、推荐、订阅广场、内容降噪、粉丝与邀请。
- `PRD-79`：第三方授权管理。
- `PRD-105`：状态历史查询。

### P2

- `PRD-57`：会员/付费系列订阅。
- `PRD-59`：订阅广场发现。
- `PRD-86、87`：升级提示与经验衰减策略。
- `PRD-99、100`：生命周期阶段化展示增强。

## 9. 后续实现建议

1. 第一阶段：先补 `支持治理域` 中 `help-center 分类级客服联动 + customer-service 分层`，并同步完成列表接口分页评估：`help-center` 属于静态小集合，不分页；`appeal/list` 属于用户历史列表，应升级为分页；`admin report list` 保持现有分页模型不变。
2. 第二阶段：补 `账号安全域` 的 `邮箱注册、绑定解绑、登录方式、风险拦截`，形成真正的账号安全链路。
3. 第三阶段：补 `关系订阅域` 的 `分组管理、批量操作、关注流、推荐、内容降噪`，把现有关系表从“数据维护”升级为“用户体验”能力。
4. 第四阶段：继续补 `资料与隐私域` 的 `历史记录、第三方授权、隐私缓存失效与真实通知发送链路接入`，让设置类需求形成完整闭环。
5. 第五阶段：补 `成长激励域` 的 `勋章服务、积分消耗、等级权益、升级提示与衰减规则`，在现有账本模型上做增量扩展。
