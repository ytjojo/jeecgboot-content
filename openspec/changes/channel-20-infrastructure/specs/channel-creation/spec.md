## ADDED Requirements

### Requirement: 系统频道创建

平台管理员 SHALL 能够在后台创建系统官方频道，频道直接进入 Active 状态，无需内容安全审核。

#### Scenario: 管理员创建系统频道成功
- **WHEN** 平台管理员在后台填写名称、简介、图标、封面、所属官方分类、置顶权重并提交
- **THEN** 系统直接创建频道，状态为 Active，默认公开、全站可见

#### Scenario: 系统频道名称不参与用户频道唯一性校验
- **WHEN** 系统频道名称与已有用户频道重名
- **THEN** 系统 SHALL 允许创建，不拦截

#### Scenario: 非管理员不可创建系统频道
- **WHEN** 普通用户尝试创建系统频道
- **THEN** 系统 SHALL 拒绝并提示无权限

---

### Requirement: 个人频道创建

普通用户 SHALL 能够创建个人频道，需满足基础账号要求，名称在用户频道范围内唯一，创建后进入审核。

#### Scenario: 用户创建个人频道成功
- **WHEN** 已登录用户满足基础账号要求，填写名称、简介、图标、隐私设置、归属分类并提交，名称在用户频道范围内唯一
- **THEN** 系统创建频道并进入 PendingReview 状态

#### Scenario: 名称冲突拦截
- **WHEN** 用户提交的频道名称在用户频道范围内已存在（PendingReview/Active/DeleteCooling/ReadonlyFrozen/Hidden/Archived 状态）
- **THEN** 系统 SHALL 提示"该频道名称已被使用，请更换"

#### Scenario: 个人频道数量上限
- **WHEN** 用户已创建 20 个个人频道，再次尝试创建
- **THEN** 系统 SHALL 提示已达上限

#### Scenario: 基础账号要求不满足
- **WHEN** 用户未完成手机号或邮箱验证，或账号被冻结，或处于禁止创建频道的风控状态
- **THEN** 系统 SHALL 提示需完成账号验证或解除限制后再创建

---

### Requirement: 组织频道创建

已认证组织的管理员 SHALL 能够为组织创建专属频道，自动绑定组织信息，创建后进入审核。

#### Scenario: 组织管理员创建组织频道成功
- **WHEN** 组织已完成认证且当前用户为组织管理员，填写频道信息并提交，名称在用户频道范围内唯一
- **THEN** 系统创建组织频道，自动绑定所属组织，进入 PendingReview 状态

#### Scenario: 组织未认证拦截
- **WHEN** 组织未完成认证，尝试创建组织频道
- **THEN** 系统 SHALL 提示"请先完成组织认证"

#### Scenario: 组织频道数量上限
- **WHEN** 组织已创建 50 个频道，再次尝试创建
- **THEN** 系统 SHALL 提示已达上限

#### Scenario: 非组织管理员拦截
- **WHEN** 当前用户不是组织管理员，尝试为该组织创建频道
- **THEN** 系统 SHALL 拒绝并提示无组织管理权限

#### Scenario: 组织绑定不可更改
- **WHEN** 组织频道已创建
- **THEN** 频道绑定的组织信息 SHALL 不可更改
