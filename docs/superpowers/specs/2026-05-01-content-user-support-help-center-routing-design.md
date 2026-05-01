# 内容社区用户域 - 支持域 Help Center 分类级客服联动设计

## 1. 背景

当前支持域用户侧已具备两个公开入口：

- `GET /content/user/support/help-center`
- `GET /content/user/support/customer-service`

其中：

- `customer-service` 已经补齐了基于用户等级、成长值和治理状态的入口分层
- `help-center` 仍返回简单字符串列表，只能表达“有哪些内容”，无法表达“当前分类应该引导用户走哪个客服入口”

PRD 对支持域的要求不仅包括帮助中心与客服入口本身，还要求支持：

- 智能客服 + 人工客服入口
- 高活跃 / 高等级用户优先支持
- 举报 / 申诉 / 账号治理问题的明确处理路径

因此本次需要把 `help-center` 从静态内容元数据升级为“分类级内容推荐 + 客服联动提示”，与 `customer-service` 形成一致的支持入口闭环。

## 2. 目标

在不引入数据库、不扩大到 admin 端、不重构基础分层的前提下，完成以下最小闭环：

- `help-center` 返回结构化分类元数据
- FAQ 分类和指南条目具备分类级客服推荐信息
- 分类级推荐规则与 `customer-service` 共享同一套用户路由语义
- 默认用户、高等级用户、治理异常状态用户都能拿到不同的帮助引导结果

## 3. 范围

### 3.1 包含

- 调整 `GET /content/user/support/help-center` 接口签名，新增 `userId` 入参
- 将 `ContentHelpCenterVO` 从字符串列表升级为结构化条目列表
- 新增帮助中心分类条目 VO
- 在 service 中按用户画像生成分类级推荐客服入口
- 补齐 service 与 controller 测试

### 3.2 不包含

- 不新增帮助中心数据库表
- 不做 FAQ 明细页 / 搜索 / 多语言
- 不改 admin 支持域接口
- 不改现有 `customer-service` 路由规则的大方向
- 不顺手做 BizManageService 重构或 `ServiceImpl` 体系调整

## 4. 方案选择

### 4.1 方案 A：继续返回字符串列表

优点：

- 改动最小

缺点：

- 无法表达分类推荐入口
- 前端只能硬编码客服引导规则
- 与已实现的 `customer-service` 分层价值无法联动

### 4.2 方案 B：全局帮助中心中内嵌一份统一客服入口

优点：

- 前端可以直接读取一份推荐入口

缺点：

- 只能表达“全局推荐”，不能表达“不同分类不同入口”
- 不满足分类级联动目标

### 4.3 方案 C：分类级推荐入口摘要

能力：

- FAQ 分类和指南条目可表达自己的推荐客服入口
- 返回结构比完整嵌套客服对象更轻
- 可与现有 `customer-service` 规则保持一致

代价：

- 需要把帮助中心列表升级为结构化对象

### 4.4 结论

本次采用方案 C：分类级推荐入口摘要。

## 5. 设计

### 5.1 接口调整

将用户侧帮助中心接口从：

- `GET /content/user/support/help-center`

调整为：

- `GET /content/user/support/help-center?userId=...`

说明：

- `help-center` 需要按用户状态输出分类级推荐入口，因此必须感知用户上下文
- 当前项目用户侧 `support` 其他查询接口也使用显式 `userId` 入参，本次保持同一风格，不引入额外鉴权上下文改造

### 5.2 返回结构

`ContentHelpCenterVO` 调整为结构化列表：

- `faqCategories`
- `guideEntries`
- `releaseNotes`

其中：

- `faqCategories` 和 `guideEntries` 的元素类型升级为新的帮助中心条目 VO
- `releaseNotes` 也统一使用同一个条目 VO，但不附带分类级客服推荐

帮助中心条目 VO 包含以下字段：

- `code`
- `title`
- `description`
- `recommendedRouteType`
- `recommendedRouteTitle`
- `manualSupported`

说明：

- `recommendedRouteType` 使用与 `customer-service` 一致的值域，例如：
  - `SMART_FIRST`
  - `MANUAL_PRIORITY`
  - `APPEAL_PRIORITY`
- `recommendedRouteTitle` 用于前端直接展示引导文案，避免前端自己映射
- `manualSupported` 明确该分类是否可转人工，减少歧义

### 5.3 用户级路由规则

`getCustomerServiceEntry(userId)` 继续作为用户级最终入口，规则保持已有实现：

- 治理异常状态用户：`APPEAL_PRIORITY`
- 高等级 / 高成长用户：`MANUAL_PRIORITY`
- 其他用户：`SMART_FIRST`

帮助中心分类级联动不重复定义另一套独立规则，而是在同一套用户优先级语义上做分类映射。

### 5.4 分类级映射规则

#### 默认用户

- `账号安全` -> `SMART_FIRST`
- `举报申诉` -> `SMART_FIRST`
- `隐私设置` -> `SMART_FIRST`
- `新手指南` -> `SMART_FIRST`
- `功能使用说明` -> `SMART_FIRST`
- `社区规范` -> `SMART_FIRST`

#### 高等级 / 高成长用户

- `账号安全` -> `MANUAL_PRIORITY`
- `举报申诉` -> `MANUAL_PRIORITY`
- `隐私设置` -> `SMART_FIRST`
- `新手指南` -> `SMART_FIRST`
- `功能使用说明` -> `SMART_FIRST`
- `社区规范` -> `SMART_FIRST`

#### 治理异常状态用户

- `账号安全` -> `APPEAL_PRIORITY`
- `举报申诉` -> `APPEAL_PRIORITY`
- `社区规范` -> `APPEAL_PRIORITY`
- `隐私设置` -> `SMART_FIRST`
- `新手指南` -> `SMART_FIRST`
- `功能使用说明` -> `SMART_FIRST`

说明：

- 治理异常状态高于高等级优先级
- 并不是所有分类都要强制引导到人工或申诉入口，只有治理与安全相关分类需要更强引导
- 普通帮助类仍保持智能客服优先，避免过度把流量打到人工客服

### 5.5 Release Notes 规则

`releaseNotes` 保持内容展示属性，不做分类级客服推荐：

- `recommendedRouteType = null`
- `recommendedRouteTitle = null`
- `manualSupported = null`

这样可以避免把“产品更新日志”错误建模为支持入口。

## 6. 测试设计

### 6.1 Service 层

新增 / 调整以下测试：

- 默认用户获取帮助中心时，FAQ / 指南分类返回 `SMART_FIRST`
- 高等级用户获取帮助中心时，`账号安全`、`举报申诉` 返回 `MANUAL_PRIORITY`
- 治理异常状态用户获取帮助中心时，`账号安全`、`举报申诉`、`社区规范` 返回 `APPEAL_PRIORITY`
- `releaseNotes` 不带分类级客服推荐字段

### 6.2 Controller 层

新增 / 调整以下测试：

- `help-center` 接口要求携带 `userId`
- `help-center` 返回结构化 JSON 列表
- 分类项中的 `recommendedRouteType`、`recommendedRouteTitle`、`manualSupported` 输出正确

### 6.3 回归要求

回归范围保持聚焦：

- `ContentUserSupportServiceTest`
- `ContentUserSupportControllerWebMvcTest`
- `ContentUserSupportAdminControllerWebMvcTest`

## 7. 风险与边界

- `help-center` 接口新增 `userId` 入参后，现有调用方需要同步调整
- 由于本次不接数据库，帮助中心内容仍是模块内静态构造，后续如需运营化配置，需要再做数据化改造
- 当前分类级推荐属于“引导语义”，不是工单系统或客服系统的真实排队能力承诺

## 8. 验收标准

- `help-center` 接口支持 `userId` 入参
- `ContentHelpCenterVO` 成功升级为结构化列表模型
- FAQ / 指南分类带有分类级客服推荐摘要
- 默认用户、高等级用户、治理异常状态用户的分类推荐结果符合设计规则
- `releaseNotes` 不携带分类级客服推荐字段
- 聚焦测试通过，且不引入生产数据库结构变更
