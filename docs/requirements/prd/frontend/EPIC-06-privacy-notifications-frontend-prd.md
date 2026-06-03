# EPIC-06 隐私设置与通知管理 - 前端 PRD

> **史诗ID**: EPIC-06
> **域**: user（用户域）
> **变更ID**: user-06-privacy-notifications-frontend
> **版本**: 1.0
> **前置依赖**: 无
> **日期**: 2026-06-02

---

## 1. 概述

### 需求目标
- **需求名称**: 隐私设置与通知管理（EPIC-06）
- **一句话概述**: 为内容社区用户提供精细化的隐私控制与通知管理能力
- **要解决的问题**: 当前缺少细粒度隐私设置、免打扰模式、在线状态控制和第三方授权管理，导致用户隐私边界模糊且通知打扰过多
- **期望达成的结果**: 隐私设置使用率 >70%，通知关闭率降低 30%，用户满意度 >85%，通知点击率提升 20%

### 目标用户与使用场景
- **主要用户**: 所有已注册的内容社区用户
- **使用场景**: 桌面端 + 移动端
- **入口路径**: 左侧菜单"设置"分区 → "通知设置" / "隐私设置" / "第三方授权" / "账户与安全" 四个独立入口页面

### 范围定义

#### 本期范围
1. 通知偏好设置页：七类通知独立开关（点赞/评论/关注/收藏/@我/私信/订阅更新）+ 多渠道选择 + 免打扰多时段配置
2. 隐私可见性设置页：动态可见性四级控制 + 在线状态三级控制 + 搜索引擎索引控制
3. 第三方授权管理页：授权列表 + 查看详情 + 撤销授权
4. 账户安全设置页：四类安全功能统一入口 + 登录提醒开关
5. 通知退订合规：前端展示层面无需额外页面，后端强制校验（前端仅展示安全通知不可关闭的提示）

#### 非本期范围
- 数据导出功能（GDPR 工具）
- 两步验证的具体实现流程（仅提供入口导航）
- 第三方 OAuth 服务端实现（仅管理已授权应用状态）
- 通知审计日志的前端查看入口（运营后台功能）

---

## 2. 功能列表

| **功能名称** | **功能概述** | **适用角色** | **优先级** | **所属页面** |
|---|---|---|---|---|
| 通知类型独立开关 | 分别控制点赞/评论/关注/收藏/@我/私信/订阅更新七类通知 | 注册用户 | P0 | 通知设置页 |
| 通知渠道配置 | 为每类通知配置 App内/推送/短信/邮件渠道 | 注册用户 | P0 | 通知设置页 |
| 免打扰多时段配置 | 多个时段、工作日/周末分类、临时关闭、摘要模式 | 注册用户 | P1 | 通知设置页 |
| 动态可见性控制 | 浏览记录/点赞动态/收藏夹四级可见性（公开/关注者/互关/仅自己） | 注册用户 | P0 | 隐私设置页 |
| 在线状态可见性 | 公开/隐藏/仅互关可见三种模式 | 注册用户 | P1 | 隐私设置页 |
| 搜索引擎索引控制 | 控制个人主页是否被搜索引擎收录 | 注册用户 | P2 | 隐私设置页 |
| 第三方授权列表 | 查看所有已授权第三方应用 | 注册用户 | P1 | 第三方授权页 |
| 撤销第三方授权 | 撤销授权后 Token 立即失效 | 注册用户 | P1 | 第三方授权页 |
| 账户安全入口 | 设备管理/密码修改/两步验证/登录提醒统一导航 | 注册用户 | P1 | 账户与安全页 |
| 登录提醒开关 | 开启/关闭新设备登录通知 | 注册用户 | P1 | 账户与安全页 |

---

## 3. 功能详细说明

### 3.1 通知设置页（NotificationSettings.vue）

#### 页面入口
- 左侧菜单 → 设置 → 通知设置

#### 页面结构
```
Page
  └── 通知偏好设置（Card）
       └── 通知类型列表（7行，每行：类型名称 + 开关 + 渠道标签组）
  └── 免打扰设置（Card）
       └── 免打扰规则列表（可增删）
       └── 临时关闭按钮
  └── 底部保存按钮区
```

#### 3.1.1 通知类型独立开关与渠道配置

**布局**: 表格式卡片列表，每行代表一类通知

**每行结构**:
| 列 | 内容 | 组件 |
|---|---|---|
| 通知类型 | 图标 + 名称（点赞、评论、关注、收藏、@我、私信、订阅更新） | Icon + 文本 |
| 开关 | 主开关（Switch） | `a-switch` |
| 渠道选择 | App内 / 推送 / 短信 / 邮件 四个 Checkbox | `a-checkbox-group` |

**交互规则**:
- 主开关关闭时，渠道 Checkbox 组整体置灰（disabled），但仍保留上次选择状态
- 主开关开启时，渠道 Checkbox 恢复可操作，至少保留一个渠道或允许全关
- 渠道配置异常（null 值）时，默认选中 App内 + 推送
- 安全类通知（异地登录、密码修改等）行显示"始终开启"标签，开关和渠道不可编辑，显示锁图标提示"安全通知不可关闭"

**请求字段映射**:
后端 `ContentUserNotificationUpdateReq` 包含以下字段，前端提交时需一一对应：

| 前端字段 | 后端字段 | 类型 | 说明 |
|---|---|---|---|
| 点赞通知开关 | `likeNoticeEnabled` | boolean | 点赞通知主开关 |
| 评论通知开关 | `commentNoticeEnabled` | boolean | 评论通知主开关 |
| 关注通知开关 | `followNoticeEnabled` | boolean | 关注通知主开关 |
| 收藏通知开关 | `favoriteNoticeEnabled` | boolean | 收藏通知主开关 |
| @我通知开关 | `mentionNoticeEnabled` | boolean | @我通知主开关 |
| 私信通知开关 | `messageNoticeEnabled` | boolean | 私信通知主开关 |
| 订阅更新通知开关 | `subscriptionNoticeEnabled` | boolean | 订阅更新通知主开关 |
| 渠道配置 | `channelConfig` | object | 各类型渠道配置，见下方渠道枚举 |

**保存行为**:
- 页面底部统一"保存"按钮，点击后拆分两次并发请求（`Promise.all`）：
  - 请求 1：`POST /content/user/settings/notification/update`（通知开关 + 渠道配置）
  - 请求 2：`POST /content/user/settings/notification/dnd/update`（免打扰规则，仅在免打扰配置有变更时发送）
- 保存中按钮显示 loading 并禁用
- 两次请求均成功 → 全局消息提示"通知设置已保存"，新设置立即生效
- 任一请求失败 → 全局错误提示，保留用户已修改的状态；若仅一个失败，提示具体失败项

#### 3.1.2 免打扰多时段配置

**布局**: 独立 Card 区域，规则列表 + 新增按钮

**单条规则结构**:
| 字段 | 组件 | 说明 |
|---|---|---|
| 启用状态 | `a-switch` | 控制该条规则是否生效 |
| 开始时间 | `a-time-picker` (HH:mm) | 必填 |
| 结束时间 | `a-time-picker` (HH:mm) | 必填，支持跨午夜 |
| 日期类型 | `a-select` (工作日/周末/每天) | 默认"每天" |
| 摘要模式 | `a-switch` | 时段结束后是否发送摘要通知 |
| 操作 | 删除按钮（仅当规则数 > 1 时显示） | - |

**交互规则**:
- 点击"新增时段" → 列表底部追加一条新规则，时间默认 22:00-07:00
- 开始时间等于结束时间 → 视为全天免打扰，显示提示文案
- 启用但未填写时间 → 表单校验失败，时间字段标红提示"请选择时间"
- 免打扰时段配置为空列表且无规则启用 → 免打扰功能整体关闭

**临时关闭功能**:
- 按钮文案："暂时关闭免打扰（1小时）"
- 点击后 → 按钮变为倒计时状态"免打扰已关闭，XX:XX 后恢复"
- 倒计时结束后自动恢复，按钮恢复为原始状态
- 调用接口发送 `temporaryDisable: true`
- 后端 `DndRuleVO` 提供 `temporaryDisableUntil` 字段（Unix 毫秒时间戳），记录临时关闭的截止时间

**临时关闭状态恢复逻辑**:
- 页面加载时，检查 `DndRuleVO.temporaryDisableUntil` 字段
- 若 `temporaryDisableUntil > 当前时间`（说明仍在临时关闭期间）：
  - 按钮自动进入倒计时状态
  - 剩余时间 = `temporaryDisableUntil - 当前时间`
  - 倒计时结束后自动恢复按钮为原始状态
- 若 `temporaryDisableUntil <= 当前时间` 或为 `null`：
  - 按钮显示为原始状态"暂时关闭免打扰（1小时）"

**安全通知提示**:
- 免打扰区域底部显示提示文案："安全类通知（异地登录、密码修改等）不受免打扰影响"

### 3.2 隐私设置页（PrivacySettings.vue）

#### 页面入口
- 左侧菜单 → 设置 → 隐私设置

#### 页面结构
```
Page
  └── 动态可见性（Card）
       └── 可见性设置列表（3项：浏览记录、点赞动态、收藏夹）
  └── 在线状态（Card）
       └── 在线状态可见性（Radio 组）
  └── 搜索引擎（Card）
       └── 允许搜索引擎索引（Switch）
  └── 底部保存按钮区
```

#### 3.2.1 动态可见性四级控制

**布局**: 每项一行，左侧标签 + 右侧 `a-select` 下拉选择器

**选项值**:
| 值 | 显示文案 | 说明 |
|---|---|---|
| `PUBLIC` | 公开 | 所有用户可见 |
| `FOLLOWERS_ONLY` | 仅关注者可见 | 仅关注该用户的用户可见 |
| `MUTUAL_ONLY` | 仅互关可见 | 仅互相关注的用户可见 |
| `PRIVATE` | 仅自己可见 | 仅自己可查看 |

**交互规则**:
- 选择"仅自己可见"时，该项下方显示提示文案："其他用户将无法在你的主页看到此内容"
- 选择后无需单独保存，统一在页面底部保存
- 字段为 null 时默认显示"公开"

**数据项**:
| 字段标签 | 字段 key | 默认值 |
|---|---|---|
| 浏览记录可见性 | `browsingHistoryVisibility` | `PUBLIC` |
| 点赞动态可见性 | `likeActivityVisibility` | `PUBLIC` |
| 收藏夹可见性 | `favoritesVisibility` | `PUBLIC` |

#### 3.2.2 在线状态可见性

**布局**: `a-radio-group` 横向排列三个选项

**选项值**:
| 值 | 显示文案 | 说明 |
|---|---|---|
| `PUBLIC` | 公开 | 所有用户可看到在线标识 |
| `HIDDEN` | 隐藏 | 对其他用户显示为离线 |
| `MUTUAL_ONLY` | 仅互关可见 | 仅互关好友可看到在线状态 |

**交互规则**:
- 选择"隐藏"时显示提示："其他用户将看到你为离线状态"
- 选择"仅互关可见"时显示提示："仅与你互关的好友可看到你的在线状态"
- 默认值为 `PUBLIC`

#### 3.2.3 搜索引擎索引控制

**布局**: 单行 Switch + 说明文案

**交互规则**:
- 开启 → "允许搜索引擎收录你的个人主页"
- 关闭 → "搜索引擎将不再收录你的个人主页"
- 认证用户/创作者默认建议开启（显示推荐标签）
- 默认关闭（保护隐私优先）

**保存行为**:
- 点击"保存" → 调用 `POST /content/user/settings/privacy/update`
- 保存成功 → 全局消息提示"隐私设置已保存"
- 缓存 5 分钟内失效，对其他用户即时生效

### 3.3 第三方授权管理页（ThirdPartyAuth.vue）

#### 页面入口
- 左侧菜单 → 设置 → 第三方授权

#### 页面结构
```
Page
  └── 授权列表（Table 或 CardList）
       └── 每行：应用图标 + 应用名称 + 授权时间 + 授权范围 + 查看详情 + 撤销按钮
  └── 空状态（无授权记录时）
```

#### 3.3.1 授权列表

**布局**: 使用 `Table` 组件展示，列配置如下：

| 列名 | 字段 | 宽度 | 说明 |
|---|---|---|---|
| 应用名称 | `appName` | 25% | 为 null 时显示"未知应用" |
| 授权时间 | `authTime` | 20% | 格式：YYYY-MM-DD HH:mm |
| 授权范围 | `scopes` | 30% | 多个范围用 Tag 标签展示 |
| 操作 | - | 25% | "查看详情" + "撤销授权"按钮 |

**交互规则**:
- 点击"查看详情" → 打开 `Modal` 弹窗，展示该应用可访问的数据范围清单（个人资料、发布内容、联系人等）
- 授权范围为空时 → 显示"未知权限"并标记为可疑授权（红色 Tag）
- 列表为空时 → 显示空状态插图 + 文案"暂无已授权的第三方应用"

#### 3.3.2 撤销授权

**交互流程**:
1. 用户点击某行"撤销授权"按钮
2. 弹出确认弹窗 `Modal`：标题"撤销授权"，内容"撤销后该应用将无法访问你的数据，是否确认？"
3. 用户点击"确认撤销" → 调用 `DELETE /content/user/auth/third-party/{authId}`
4. 请求中按钮显示 loading
5. 成功 → 全局提示"授权已撤销"，该行从列表中移除
6. 失败 → 全局错误提示，保留该行

**异常处理**:
- 授权记录不存在 → 提示"授权记录不存在"
- 越权操作 → 提示"权限不足"（403）

### 3.4 账户安全设置页（AccountSecurity.vue）

#### 页面入口
- 左侧菜单 → 设置 → 账户与安全

#### 页面结构
```
Page
  └── 安全功能入口（4个 Card 卡片，2x2 网格布局）
       └── 卡片1: 设备管理（图标 + 名称 + 状态 + 箭头）
       └── 卡片2: 密码修改（图标 + 名称 + 状态 + 箭头）
       └── 卡片3: 两步验证（图标 + 名称 + 状态 + 箭头）
       └── 卡片4: 登录提醒（图标 + 名称 + Switch 开关）
```

#### 3.4.1 安全功能入口卡片

**卡片结构**:
| 区域 | 内容 | 说明 |
|---|---|---|
| 图标 | 安全相关 SVG 图标 | 每个功能不同图标 |
| 功能名称 | 设备管理 / 密码修改 / 两步验证 / 登录提醒 | 粗体标题 |
| 状态描述 | "已启用"/"未启用" | 从 `/content/user/settings/security` 获取，后端仅返回 Boolean |
| 操作区域 | 箭头图标（跳转）或 Switch（登录提醒） | 点击整张卡片也可跳转 |

**交互规则**:
- 点击"设备管理"卡片 → 跳转到设备管理页面（EPIC-01 已实现）
- 点击"密码修改"卡片 → 跳转到密码修改流程（EPIC-01 已实现）
- 点击"两步验证"卡片 → 跳转到两步验证绑定流程（EPIC-01 已实现）
- "登录提醒"卡片右侧为 Switch 开关，无需跳转，直接切换
- Switch 切换 → 调用接口更新，成功后状态描述更新
- 安全功能状态为 null 时 → 默认显示"已启用"（安全优先）

**数据加载**:
- 页面加载时调用 `GET /content/user/settings/security`
- 后端 `ContentUserSecuritySettingVO` 为扁平结构，字段映射如下：

| 后端字段 | 类型 | 页面展示 | 卡片位置 |
|---|---|---|---|
| `deviceManagementEnabled` | boolean | 设备管理状态（已启用/未启用） | 卡片1 |
| `passwordChangeEnabled` | boolean | 密码修改状态（已启用/未启用） | 卡片2 |
| `twoFactorEnabled` | boolean | 两步验证状态（已启用/未启用） | 卡片3 |
| `loginAlertEnabled` | boolean | 登录提醒开关状态 | 卡片4 Switch |

> 注意：后端不返回 `lastActive`（设备最后活跃时间）、`lastModified`（密码最后修改时间）、`method`（两步验证方式）等详情字段。前端卡片状态描述简化为"已启用"/"未启用"。null 值默认显示"已启用"（安全优先）。

- 加载中显示骨架屏

---

## 4. 组件选型

基于 `frontend-standards.md` 的组件清单，各页面推荐组件如下：

### 页面级组件

| 页面 | 容器组件 | 核心组件 | 说明 |
|---|---|---|---|
| 通知设置页 | `Page` | `Form` + `a-switch` + `a-checkbox-group` + `a-time-picker` | 配置式表单 |
| 隐私设置页 | `Page` | `Form` + `a-select` + `a-radio-group` + `a-switch` | 配置式表单 |
| 第三方授权页 | `Page` | `Table` + `Modal` + `Description` | 列表 + 弹窗详情 |
| 账户安全页 | `Page` | `CardList` + `a-switch` | 卡片网格 |

### 基础组件映射

| 用途 | 推荐组件 | 路径 |
|---|---|---|
| 页面容器 | `Page` | `src/components/Page/` |
| 表单配置 | `Form`（schema 驱动） | `src/components/Form/` |
| 开关切换 | `a-switch`（Ant Design Vue） | 自动导入 |
| 渠道多选 | `a-checkbox-group`（Ant Design Vue） | 自动导入 |
| 时间选择 | `a-time-picker`（Ant Design Vue） | 自动导入 |
| 下拉选择 | `a-select`（Ant Design Vue） | 自动导入 |
| 单选组 | `a-radio-group`（Ant Design Vue） | 自动导入 |
| 数据表格 | `Table` | `src/components/Table/` |
| 确认弹窗 | `Modal` | `src/components/Modal/` |
| 详情展示 | `Description` | `src/components/Description/` |
| 卡片列表 | `CardList` | `src/components/CardList/` |
| 消息提示 | `useMessage` | `src/hooks/web/` |
| 标签展示 | `a-tag`（Ant Design Vue） | 自动导入 |
| 空状态 | `a-empty`（Ant Design Vue） | 自动导入 |
| 骨架屏 | `a-skeleton`（Ant Design Vue） | 自动导入 |

### Hooks 使用

| Hook | 用途 | 场景 |
|---|---|---|
| `useForm` | 表单逻辑封装 | 通知设置、隐私设置的表单校验与提交 |
| `useModal` | 弹窗逻辑封装 | 撤销授权确认弹窗、授权详情弹窗 |
| `useMessage` | 消息提示 | 操作成功/失败的全局提示 |

---

## 5. API 对接

### 通知渠道枚举值

前端提交渠道配置时，需使用以下字符串标识值：

| 枚举值 | 显示文案 | 说明 |
|---|---|---|
| `IN_APP` | App 内 | 应用内通知中心 |
| `PUSH` | 推送 | 系统推送通知 |
| `SMS` | 短信 | 短信通知 |
| `EMAIL` | 邮件 | 邮件通知 |

各通知类型的渠道字段对应关系：

| 通知类型 | 渠道字段名 | 类型 |
|---|---|---|
| 点赞 | `likeChannels` | `string[]` |
| 评论 | `commentChannels` | `string[]` |
| 关注 | `followChannels` | `string[]` |
| 收藏 | `favoriteChannels` | `string[]` |
| @我 | `mentionChannels` | `string[]` |
| 私信 | `messageChannels` | `string[]` |
| 订阅更新 | `subscriptionChannels` | `string[]` |

### authId 参数说明

第三方授权接口中的 `authId` 参数为后端自增 ID，类型为 `number`（前端路由参数传递时转为 `string`，API 调用时需转回 `number`）。

### 接口清单

| 接口 | 方法 | 路径 | 用途 | 调用页面 |
|---|---|---|---|---|
| 获取通知设置 | GET | `/content/user/settings/notification` | 加载通知偏好和免打扰配置 | 通知设置页 |
| 更新通知设置 | POST | `/content/user/settings/notification/update` | 保存通知开关和渠道配置 | 通知设置页 |
| 更新免打扰规则 | POST | `/content/user/settings/notification/dnd/update` | 保存免打扰多时段配置 | 通知设置页 |
| 获取隐私设置 | GET | `/content/user/settings/privacy` | 加载可见性配置 | 隐私设置页 |
| 更新隐私设置 | POST | `/content/user/settings/privacy/update` | 保存可见性配置 | 隐私设置页 |

**隐私设置 GET 响应字段映射**:

后端 `ContentUserPrivacySetting` 实体包含 17 个字段，前端仅提取页面展示的 5 项，映射关系如下：

| 后端字段 | 前端字段 key | 页面展示 | 默认值 |
|---|---|---|---|
| `browseHistoryVisibility` | `browseHistoryVisibility` | 浏览记录可见性 | `PUBLIC` |
| `likeActivityVisibility` | `likeActivityVisibility` | 点赞动态可见性 | `PUBLIC` |
| `favoriteVisibility` | `favoritesVisibility` | 收藏夹可见性 | `PUBLIC` |
| `onlineStatusVisibility` | `onlineStatusVisibility` | 在线状态可见性 | `PUBLIC` |
| `allowSearchEngineIndex` | `allowSearchEngineIndex` | 搜索引擎索引 | `false` |

> 注意：后端字段 `favoriteVisibility` 与前端展示字段 `favoritesVisibility` 存在命名差异（无 `s`），前端需在接口响应处理时做映射转换。其余后端字段（生日/性别/地区/职业等）前端忽略。

| 获取安全设置 | GET | `/content/user/settings/security` | 加载安全功能状态 | 账户安全页 |
| 获取授权列表 | GET | `/content/user/auth/third-party` | 加载已授权应用列表 | 第三方授权页 |
| 查看授权详情 | GET | `/content/user/auth/third-party/{authId}` | 获取单个应用授权详情 | 第三方授权页 |
| 撤销授权 | DELETE | `/content/user/auth/third-party/{authId}` | 撤销指定应用授权 | 第三方授权页 |

### userId 传递方式

后端设置类接口（通知设置、隐私设置、安全设置）均通过 `@RequestParam("userId")` 接收 userId，即 URL 查询参数。前端通过全局请求拦截器自动注入 userId，无需在每个接口调用中手动传递。

拦截器实现逻辑：从用户登录态（Token / Store）中获取当前 userId，自动拼接到 GET 请求的 query 参数和 POST 请求的 params 中。

### API 封装规范

```typescript
import { defHttp } from '/@/utils/http/axios';

// 响应格式: { code: 200, result: any, message: string, success: boolean }
// userId 由全局请求拦截器自动注入，无需手动传递

// 获取通知设置
export const getNotificationSettings = () =>
  defHttp.get({ url: '/content/user/settings/notification' });

// 更新通知设置
export const updateNotificationSettings = (data: NotificationSettingsReq) =>
  defHttp.post({ url: '/content/user/settings/notification/update', data });

// 更新免打扰规则
export const updateDndRules = (data: DndRulesReq) =>
  defHttp.post({ url: '/content/user/settings/notification/dnd/update', data });

// 获取隐私设置
export const getPrivacySettings = () =>
  defHttp.get({ url: '/content/user/settings/privacy' });

// 更新隐私设置
export const updatePrivacySettings = (data: PrivacySettingsReq) =>
  defHttp.post({ url: '/content/user/settings/privacy/update', data });

// 获取安全设置
export const getSecuritySettings = () =>
  defHttp.get({ url: '/content/user/settings/security' });

// 获取第三方授权列表
export const getThirdPartyAuthList = () =>
  defHttp.get({ url: '/content/user/auth/third-party' });

// 查看授权详情
export const getThirdPartyAuthDetail = (authId: number) =>
  defHttp.get({ url: `/content/user/auth/third-party/${authId}` });

// 撤销授权
export const revokeThirdPartyAuth = (authId: number) =>
  defHttp.delete({ url: `/content/user/auth/third-party/${authId}` });
```

---

## 6. 状态管理

### Store 设计

本模块的状态管理以页面级局部状态为主，无需新建全局 Pinia Store。数据通过 API 实时获取和提交，不需跨页面共享状态。

| 状态 | 管理方式 | 说明 |
|---|---|---|
| 通知偏好配置 | 页面级 `ref` / `reactive` | 表单数据，提交后丢弃 |
| 免打扰规则列表 | 页面级 `ref[]` | 支持增删改 |
| 隐私可见性设置 | 页面级 `reactive` | 表单数据 |
| 第三方授权列表 | 页面级 `ref[]` | 列表数据，撤销后刷新 |
| 安全功能状态 | 页面级 `reactive` | 一次性加载 |

### 缓存策略

- 隐私设置和通知偏好在后端 Redis 缓存（TTL 300 秒），前端不需额外缓存
- 前端每次进入设置页面时从 API 获取最新数据
- 设置变更后后端主动删除 Redis 缓存，确保即时生效

---

## 7. 交互设计

### 通用交互规则

以下规则适用于所有四个页面：

| 场景 | 交互行为 |
|---|---|
| 页面加载 | 显示骨架屏（`a-skeleton`），数据就绪后替换为实际内容 |
| 数据保存中 | 保存按钮显示 loading 并禁用，表单项禁止编辑 |
| 保存成功 | 全局消息提示（绿色），2 秒后自动关闭 |
| 保存失败 | 全局错误提示（红色），保留用户已修改的状态 |
| 表单校验失败 | 对应字段标红，下方显示错误文案 |
| 未登录访问 | 路由守卫拦截，跳转登录页（由全局权限处理） |
| 网络异常 | 全局错误提示 + "重试"按钮 |

### 高危操作确认

| 操作 | 确认方式 | 确认文案 |
|---|---|---|
| 撤销第三方授权 | 二次确认弹窗 | "撤销后该应用将无法访问你的数据，是否确认？" |
| 关闭所有通知渠道 | 无额外确认 | 保存时提示"你已关闭所有通知渠道，将不会收到任何通知" |

### 悬停与聚焦反馈

- 卡片悬停时显示阴影提升效果
- 开关、选择器等交互组件遵循 Ant Design Vue 默认悬停/聚焦样式
- 可点击卡片显示 `cursor: pointer`

---

## 8. 响应式设计

### PC 端（>= 1024px）

| 页面 | 布局 |
|---|---|
| 通知设置页 | 单列布局，最大宽度 800px 居中，通知列表为表格行 |
| 隐私设置页 | 单列布局，最大宽度 800px 居中，Card 分区 |
| 第三方授权页 | 单列布局，Table 展示 |
| 账户安全页 | 2x2 网格卡片布局 |

### 移动端（< 768px）

| 页面 | 布局变化 |
|---|---|
| 通知设置页 | 通知类型列表从表格行转为卡片堆叠，每张卡片内包含类型名、开关和渠道选择；免打扰规则列表转为卡片堆叠 |
| 隐私设置页 | Card 区域全宽堆叠，Radio 组纵向排列 |
| 第三方授权页 | Table 转为卡片列表，每张卡片展示应用名、授权时间、操作按钮；授权范围折叠，点击展开 |
| 账户安全页 | 卡片从 2x2 网格变为单列堆叠（1x4） |

### 平板端（768px - 1023px）

| 页面 | 布局变化 |
|---|---|
| 通知设置页 | 同 PC 端布局，宽度自适应 |
| 隐私设置页 | 同 PC 端布局 |
| 第三方授权页 | Table 保持，列宽自适应 |
| 账户安全页 | 2x2 网格保持 |

---

## 9. 性能要求

| 指标 | 目标值 | 说明 |
|---|---|---|
| 首屏加载时间 | < 2s（WiFi）/ < 4s（4G） | 设置页面为低频访问页，可接受略高加载时间 |
| 接口响应时间 | < 500ms | 设置类接口无复杂查询 |
| 保存操作响应 | < 1s | 包含缓存清除 |
| 表单校验 | 即时 | 前端校验，不依赖后端 |
| 页面切换 | 无白屏 | 使用 Vue Router 懒加载 + 骨架屏 |

### 优化策略
- 四个设置页面使用路由懒加载，按需加载
- 表单数据在页面加载时一次性获取，不轮询
- 第三方授权列表分页加载（如有大量授权记录）
- 撤销授权后仅刷新列表，不重新加载整个页面

---

## 10. 测试要点

### 功能测试

| 编号 | 测试场景 | 预期结果 |
|---|---|---|
| F-01 | 关闭点赞通知开关 | 保存后不再收到点赞通知 |
| F-01a | 关闭订阅更新通知开关 | 保存后不再收到订阅更新通知，`subscriptionNoticeEnabled` 为 false |
| F-02 | 仅保留 App 内渠道接收评论通知 | 评论通知仅在 App 内展示 |
| F-03 | 设置免打扰 23:00-07:00 | 该时段内不收到非安全通知 |
| F-04 | 免打扰时段结束 + 摘要模式开启 | 收到免打扰期间的通知摘要 |
| F-05 | 点击"暂时关闭免打扰" | 1 小时内正常接收通知，倒计时结束后恢复 |
| F-05a | 页面加载时 `temporaryDisableUntil` 大于当前时间 | 自动进入倒计时状态，显示剩余恢复时间 |
| F-06 | 设置浏览记录为"仅自己可见" | 其他用户无法在主页看到浏览记录 |
| F-06a | 设置浏览记录为"仅互关可见"（MUTUAL_ONLY） | 仅互关好友可在主页看到浏览记录 |
| F-07 | 设置在线状态为"隐藏" | 其他用户看到该用户为离线状态 |
| F-08 | 禁止搜索引擎索引 | 个人主页返回 noindex 标签 |
| F-09 | 查看第三方授权列表 | 展示所有已授权应用及详情 |
| F-10 | 撤销第三方授权 | Token 立即失效，列表刷新 |
| F-11 | 进入账户安全页 | 展示四个安全功能入口及状态 |
| F-12 | 开启/关闭登录提醒 | 开关状态即时更新 |

### 异常测试

| 编号 | 测试场景 | 预期结果 |
|---|---|---|
| E-01 | 通知开关字段为 null | 默认为开启状态 |
| E-02 | 可见性字段为 null | 默认为"公开" |
| E-03 | 搜索引擎索引为 null | 默认为关闭（保护隐私） |
| E-04 | 在线状态为 null | 默认为"公开" |
| E-05 | 授权应用名称为 null | 显示"未知应用" |
| E-06 | 授权范围为 null | 显示"未知权限"，标记为可疑 |
| E-07 | 免打扰启用但未填时间 | 校验失败，拒绝保存 |
| E-08 | 撤销不存在的授权 | 提示"授权记录不存在" |
| E-09 | 未登录访问设置页 | 跳转登录页 |
| E-10 | 网络断开时保存 | 全局错误提示，保留已修改状态 |

### 响应式测试

| 编号 | 测试场景 | 预期结果 |
|---|---|---|
| R-01 | 通知设置页在 375px 宽度 | 通知类型卡片堆叠，渠道选择纵向排列 |
| R-02 | 第三方授权页在 768px 宽度 | Table 保持展示，列宽自适应 |
| R-03 | 账户安全页在 375px 宽度 | 卡片单列堆叠 |
| R-04 | 隐私设置页在 375px 宽度 | Radio 组纵向排列，Card 全宽 |

---

## 11. 待确认问题 / 默认假设

### 已确认
1. 免打扰使用系统时区，不做自定义时区选择
2. 第三方授权撤销后不通知第三方应用（webhook 回调不做）
3. 通知审计日志保留敏感信息（用户 ID 脱敏仅用于前端展示，后端存储完整）

### 默认假设
1. **通知类型图标**: 假设使用 Ant Design Vue 内置图标或项目已有图标，无需自定义 SVG
2. **免打扰最大规则数**: 假设最多支持 5 条免打扰规则
3. **第三方授权列表分页**: 假设单用户授权数量 < 100，不做前端分页，一次加载全部
4. **账户安全入口状态**: 假设后端返回各功能是否已启用的布尔状态，前端仅做展示
5. **渠道配置最小值**: 假设允许用户关闭所有渠道（不做"至少保留一个"的强制校验）
6. **免打扰临时关闭**: 假设"暂时关闭"固定为 1 小时，不支持自定义时长
7. **搜索引擎索引默认值**: 假设默认关闭（隐私优先），认证用户/创作者页面显示推荐开启标签
8. **设置页面无草稿**: 假设用户未保存直接离开页面时，不保留草稿，不弹离开确认
