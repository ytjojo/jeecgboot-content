# EPIC-04 - 社交订阅 前端 PRD

## 1. 概述

### 1.1 背景

内容社区已具备基础关注、特别关注、关系分组、订阅和通知设置的后端雏形，但前端缺少完整的关注流、关注列表管理、关注推荐、订阅广场、订阅流和细粒度通知频率配置等交互界面。本次前端 PRD 覆盖 EPIC-04 要求的社交关注与多源订阅完整闭环。

### 1.2 目标

- 构建完整的关注体系前端交互：关注/取消关注、分组管理、特别关注、关注流、关注列表、关注推荐和批量管理。
- 构建完整的订阅体系前端交互：多类型内容源订阅、订阅通知配置、订阅流、订阅广场和统一管理。
- 所有接口使用 VO 返回，前端不直接消费 entity。
- 关注操作响应时间 <500ms，关注流加载时间 <2s，订阅操作响应时间 <500ms。

### 1.3 范围

- **包含**：关注/取消关注、分组管理、特别关注、关注流、关注推荐、批量管理、订阅专题/话题/栏目、通知配置、订阅广场、订阅流、统一管理。
- **不包含**：独立支付结算、付费订阅界面、复杂 ML 推荐展示。

### 1.4 依赖

- **前置依赖**：EPIC-01（用户注册登录），前端需在登录态下操作。
- **后续关联**：EPIC-06（拉黑与屏蔽）影响关注关系可见性；EPIC-07（社交关系扩展）可能展示互关标识。

### 1.5 成功指标

| 指标 | 目标值 |
|------|--------|
| 平均关注数提升 | 40% |
| 关注流点击率 | >30% |
| 订阅转化率 | >25% |
| 用户留存率提升 | 20% |

---

## 2. 用户故事

### 2.1 关注体系

| 编号 | 用户故事 | 优先级 |
|------|---------|--------|
| US-4.1.1 | 作为用户，我希望关注/取消关注其他用户，以便控制内容来源 | 高 |
| US-4.1.2 | 作为用户，我希望按分组管理关注对象，以便分类查看内容 | 中 |
| US-4.1.3 | 作为用户，我希望设置特别关注并接收强提醒，以便不遗漏重要内容 | 中 |
| US-4.1.4 | 作为用户，我希望在关注流中查看关注对象动态并配置展示维度 | 高 |
| US-4.1.5 | 作为用户，我希望查看关注列表并支持搜索和分组筛选 | 高 |
| US-4.1.6 | 作为用户，我希望查看特别关注列表并快速管理 | 中 |
| US-4.1.7 | 作为用户，我希望获得关注推荐并支持批量管理 | 中 |

### 2.2 订阅体系

| 编号 | 用户故事 | 优先级 |
|------|---------|--------|
| US-4.2.1 | 作为用户，我希望订阅专题/合集/话题/栏目等内容源 | 高 |
| US-4.2.2 | 作为用户，我希望配置订阅通知方式与频率 | 中 |
| US-4.2.3 | 作为用户，我希望在订阅广场发现和搜索订阅源 | 中 |
| US-4.2.4 | 作为用户，我希望统一管理所有订阅项 | 中 |

---

## 3. 页面设计

### 3.1 页面清单

| 页面 | 路由 | 说明 |
|------|------|------|
| 关注流 | `/feed/follow` | 首页"关注"标签页，展示关注对象动态 |
| 关注列表 | `/user/:id/following` | 我的关注列表，支持搜索、分组筛选 |
| 特别关注列表 | `/user/:id/special-following` | 特别关注用户管理 |
| 关注推荐 | `/discover/follow-recommend` | 推荐关注用户，展示推荐理由 |
| 分组管理 | `/user/:id/follow-groups` | 关注分组的创建、重命名、删除、排序 |
| 订阅流 | `/feed/subscription` | 订阅源更新内容流 |
| 订阅广场 | `/subscription/plaza` | 发现、搜索、浏览订阅源 |
| 订阅源详情 | `/subscription/:type/:id` | 订阅源介绍、订阅人数、最近更新 |
| 我的订阅 | `/user/:id/subscriptions` | 统一管理所有订阅项 |
| 通知设置 | `/user/:id/notification-settings` | 全局和订阅级通知配置 |

### 3.2 关注流页面 (`/feed/follow`)

**页面布局**：
- 顶部：Tab 切换（关注 / 推荐 / 订阅），当前选中"关注"
- 筛选栏：动态类型筛选开关（发布/点赞/收藏），设置入口
- 内容区：信息流卡片列表，特别关注用户内容置顶并标记高亮
- 空状态：无关注对象时引导用户去"发现"页面关注

**卡片类型**：
- 发布动态卡片：用户头像+昵称+时间+内容摘要+互动数据
- 点赞动态卡片："XXX 赞了" + 被点赞内容摘要
- 收藏动态卡片："XXX 收藏了" + 被收藏内容摘要
- 特别关注标识：卡片左上角显示星标或特殊边框

### 3.3 关注列表页面 (`/user/:id/following`)

**页面布局**：
- 顶部：关注总数统计，搜索框
- 分组 Tab：全部 + 各分组名称，支持横向滚动
- 用户列表：卡片式列表，每张卡片显示头像、昵称、简介、分组标签、关注/取消关注按钮、更多操作（设为特别关注/移动分组/取消关注）
- 底部：分页加载或无限滚动
- 空状态：无关注时引导去发现页

### 3.4 特别关注列表页面 (`/user/:id/special-following`)

**页面布局**：
- 顶部：特别关注总数，批量管理入口
- 用户卡片：头像、昵称、最新动态提示（如"2小时前发布"）、取消特别关注按钮
- 批量模式：勾选多个用户，底部操作栏（批量取消特别关注）
- 空状态：引导设置特别关注

### 3.5 关注推荐页面 (`/discover/follow-recommend`)

**页面布局**：
- 推荐列表：卡片式，每张显示头像、昵称、简介、推荐理由标签（如"与您关注的 XXX 相似"、"热门创作者"、"共同关注 N 人"）
- 操作按钮：关注 / 已关注
- 加载更多：分页或无限滚动

### 3.6 分组管理页面 (`/user/:id/follow-groups`)

**页面布局**：
- 分组列表：可拖拽排序，每项显示分组名称、成员数量
- 操作：重命名、删除（默认分组不可删除）、查看成员
- 新建分组：输入名称，校验唯一性
- 分组成员管理：移入/移出关注用户

### 3.7 订阅流页面 (`/feed/subscription`)

**页面布局**：
- 顶部：Tab 切换（关注 / 推荐 / 订阅），当前选中"订阅"
- 筛选栏：按订阅源类型筛选（专题/话题/栏目等）
- 内容区：订阅源更新内容卡片列表
- 空状态：无订阅时引导去订阅广场

### 3.8 订阅广场页面 (`/subscription/plaza`)

**页面布局**：
- 顶部：搜索框
- 分类浏览：Tab 或标签云（科技、娱乐、体育等）
- 热门推荐：按热度排序的订阅源卡片，显示名称、类型、分类、订阅人数、最近更新时间、订阅按钮
- 搜索结果：关键词匹配的订阅源列表
- 加载更多：分页

### 3.9 订阅源详情页面 (`/subscription/:type/:id`)

**页面布局**：
- 头部：订阅源名称、类型、分类、订阅人数
- 订阅按钮：订阅/已订阅/暂停中，点击可切换状态
- 通知设置入口：跳转到该订阅源的通知配置
- 介绍区域：订阅源描述
- 最近更新：该订阅源下的最近内容列表
- 相关推荐：类似订阅源推荐

### 3.10 我的订阅页面 (`/user/:id/subscriptions`)

**页面布局**：
- 顶部：订阅总数，筛选（全部/按类型）
- 订阅列表：卡片式，每张显示源名称、类型、订阅时间、最后更新时间、暂停状态、通知配置摘要
- 操作：暂停/恢复、取消订阅、通知设置、更多
- 批量操作：勾选多个，底部操作栏（批量暂停/恢复/取消）
- 空状态：引导去订阅广场

### 3.11 通知设置页面 (`/user/:id/notification-settings`)

**页面布局**：
- 全局默认设置区域：站内通知、推送、邮件的总开关
- 频率设置：实时推送 / 每日摘要
- 免打扰时段：开始时间、结束时间选择器
- 订阅级覆盖列表：按订阅源分别配置通知渠道和频率，未配置时显示"跟随全局设置"

---

## 4. 组件设计

### 4.1 通用组件

#### 4.1.1 FollowButton（关注按钮）

**功能**：关注/取消关注/已关注状态切换，支持特别关注标记
**Props**：
| 属性 | 类型 | 说明 |
|------|------|------|
| userId | string | 目标用户 ID |
| initialRelation | object | 初始关系状态 |
| showSpecialFollow | boolean | 是否显示特别关注选项 |
| size | 'small' \| 'default' \| 'large' | 按钮尺寸 |
| onFollowChange | function | 关注状态变更回调 |

**状态机**：
- 未关注 → 点击 → 调用关注 API → 已关注
- 已关注 → 点击展开菜单 → 取消关注 / 设为特别关注
- 特别关注 → 点击展开菜单 → 取消特别关注 / 取消关注

#### 4.1.2 SubscriptionButton（订阅按钮）

**功能**：订阅/取消订阅/暂停状态切换
**Props**：
| 属性 | 类型 | 说明 |
|------|------|------|
| sourceType | string | 订阅源类型（topic/tag/collection/special/column/channel） |
| sourceId | string | 订阅源 ID |
| sourceName | string | 订阅源名称 |
| initialStatus | 'subscribed' \| 'unsubscribed' \| 'paused' | 初始状态 |
| onStatusChange | function | 状态变更回调 |

**状态机**：
- 未订阅 → 点击 → 调用订阅 API → 已订阅
- 已订阅 → 点击展开菜单 → 暂停 / 取消订阅
- 暂停中 → 点击展开菜单 → 恢复 / 取消订阅

#### 4.1.3 UserCard（用户卡片）

**功能**：展示用户信息和关系状态
**Props**：
| 属性 | 类型 | 说明 |
|------|------|------|
| user | UserVO | 用户信息 |
| relation | RelationVO | 关系状态 |
| showGroup | boolean | 是否显示分组信息 |
| showActivityHint | boolean | 是否显示最新动态提示 |
| mode | 'list' \| 'grid' \| 'compact' | 展示模式 |
| actions | string[] | 可用操作列表 |

**展示内容**：头像、昵称、简介、粉丝数、分组标签、关系状态标识、操作按钮。

#### 4.1.4 SubscriptionSourceCard（订阅源卡片）

**功能**：展示订阅源信息
**Props**：
| 属性 | 类型 | 说明 |
|------|------|------|
| source | SubscriptionSourceVO | 订阅源信息 |
| subscribed | boolean | 当前用户是否已订阅 |
| showDetail | boolean | 是否可点击进入详情 |
| compact | boolean | 紧凑模式（用于列表） |

**展示内容**：源图标、名称、类型标签、分类、订阅人数、最近更新时间、订阅按钮。

#### 4.1.5 ActivityCard（动态卡片）

**功能**：展示关注流中的动态
**Props**：
| 属性 | 类型 | 说明 |
|------|------|------|
| activity | ActivityVO | 动态数据 |
| type | 'publish' \| 'like' \| 'favorite' | 动态类型 |
| isSpecialFollow | boolean | 是否特别关注用户的动态 |

**展示内容**：用户头像+昵称、动态类型文案、内容摘要、时间、互动数据、特别关注高亮标识。

#### 4.1.6 GroupSelector（分组选择器）

**功能**：选择或创建关注分组
**Props**：
| 属性 | 类型 | 说明 |
|------|------|------|
| groups | Group[] | 分组列表 |
| selectedGroupId | string | 当前选中分组 |
| allowCreate | boolean | 是否允许新建 |
| onChange | function | 选中变更回调 |

#### 4.1.7 RecommendReasonTag（推荐理由标签）

**功能**：展示关注推荐的理由
**Props**：
| 属性 | 类型 | 说明 |
|------|------|------|
| reason | string | 推荐理由文案 |
| type | 'common_follow' \| 'interest' \| 'popular' \| 'similar' | 理由类型 |

#### 4.1.8 NotificationConfigPanel（通知配置面板）

**功能**：配置通知渠道、频率和免打扰
**Props**：
| 属性 | 类型 | 说明 |
|------|------|------|
| subscriptionId | string | 订阅 ID（可选，为空时表示全局设置） |
| initialConfig | NotificationConfig | 初始配置 |
| isGlobal | boolean | 是否为全局设置 |
| onSave | function | 保存回调 |

**配置项**：
- 通知渠道开关：站内通知、推送、邮件
- 频率选择：实时推送、每日摘要
- 免打扰时段：时间范围选择器

#### 4.1.9 BatchOperationBar（批量操作栏）

**功能**：批量管理操作底部栏
**Props**：
| 属性 | 类型 | 说明 |
|------|------|------|
| selectedCount | number | 已选数量 |
| operations | Operation[] | 可用操作列表 |
| onExecute | function | 执行操作回调 |
| onClear | function | 清除选择回调 |

#### 4.1.10 EmptyState（空状态组件）

**功能**：展示列表为空时的引导
**Props**：
| 属性 | 类型 | 说明 |
|------|------|------|
| type | 'no_follow' \| 'no_special_follow' \| 'no_subscription' \| 'no_feed' | 空状态类型 |
| actionText | string | 引导操作文案 |
| onAction | function | 引导操作回调 |

### 4.2 页面组件

#### 4.2.1 FollowFeedView（关注流视图）

**子组件**：ActivityCard 列表、动态类型筛选器、设置入口
**功能**：加载关注流数据、特别关注置顶、分页加载、下拉刷新、空状态引导

#### 4.2.2 FollowingListView（关注列表视图）

**子组件**：UserCard 列表、搜索框、分组 Tab、分页器
**功能**：加载关注列表、按分组筛选、按昵称搜索、取消关注、移动分组、分页

#### 4.2.3 SpecialFollowingView（特别关注列表视图）

**子组件**：UserCard 列表（含活动提示）、批量操作栏
**功能**：加载特别关注列表、取消特别关注、批量管理、空状态引导

#### 4.2.4 FollowRecommendView（关注推荐视图）

**子组件**：UserCard 列表（含推荐理由标签）
**功能**：加载推荐列表、一键关注、分页加载

#### 4.2.5 GroupManageView（分组管理视图）

**子组件**：分组列表（可拖拽）、分组编辑弹窗、成员管理
**功能**：创建/重命名/删除分组、拖拽排序、查看和管理分组成员

#### 4.2.6 SubscriptionFeedView（订阅流视图）

**子组件**：内容卡片列表、源类型筛选器
**功能**：加载订阅流、按源类型筛选、分页加载、空状态引导

#### 4.2.7 SubscriptionPlazaView（订阅广场视图）

**子组件**：搜索框、分类 Tab、SubscriptionSourceCard 列表
**功能**：搜索订阅源、分类浏览、热度排序、一键订阅、进入详情

#### 4.2.8 SubscriptionDetailView（订阅源详情视图）

**子组件**：源信息头部、订阅按钮、通知设置入口、最近内容列表
**功能**：展示源详情、订阅/取消/暂停、配置通知、查看最近更新

#### 4.2.9 MySubscriptionsView（我的订阅视图）

**子组件**：SubscriptionSourceCard 列表、筛选器、批量操作栏
**功能**：加载所有订阅、按类型筛选、暂停/恢复/取消、通知设置、批量管理

#### 4.2.10 NotificationSettingsView（通知设置视图）

**子组件**：全局设置面板、订阅级覆盖列表
**功能**：配置全局通知默认值、按订阅源覆盖配置、免打扰时段设置

---

## 5. API 对接

### 5.1 关注关系 API

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 关注用户 | POST | `/content/user/relation/follow` | body: `{ targetUserId, groupId? }` |
| 取消关注 | POST | `/content/user/relation/unfollow` | body: `{ targetUserId }` |
| 设为特别关注 | POST | `/content/user/relation/special-follow` | body: `{ targetUserId }` |
| 取消特别关注 | POST | `/content/user/relation/cancel-special-follow` | body: `{ targetUserId }` |
| 查询关系详情 | GET | `/content/user/relation/detail` | params: `{ targetUserId }` |
| 关注列表 | GET | `/content/user/relation/following` | params: `{ userId, groupId?, keyword?, page, size }` |
| 特别关注列表 | GET | `/content/user/relation/special-following` | params: `{ userId, page, size }` |
| 批量取消关注 | POST | `/content/user/relation/batch-unfollow` | body: `{ targetUserIds[] }` |
| 批量取消特别关注 | POST | `/content/user/relation/batch-cancel-special-follow` | body: `{ targetUserIds[] }` |
| 批量移动分组 | POST | `/content/user/relation/batch-move-group` | body: `{ targetUserIds[], groupId }` |

**关键 VO 结构**：

```typescript
interface RelationDetailVO {
  followed: boolean;
  specialFollowed: boolean;
  muted: boolean;
  blacklisted: boolean;
  groupId: string;
  groupName: string;
  followTime: string;
  specialFollowTime: string;
}

interface FollowingItemVO {
  userId: string;
  nickname: string;
  avatar: string;
  bio: string;
  followerCount: number;
  relation: RelationDetailVO;
  latestActivityHint: string; // 如"2小时前发布"
}

interface BatchOperationResultVO {
  successCount: number;
  failureCount: number;
  failures: Array<{ targetUserId: string; reason: string }>;
}
```

### 5.2 关注分组 API

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取分组列表 | GET | `/content/user/relation-group/list` | params: `{ userId }` |
| 创建分组 | POST | `/content/user/relation-group/create` | body: `{ name, sortOrder }` |
| 重命名分组 | POST | `/content/user/relation-group/rename` | body: `{ groupId, name }` |
| 删除分组 | POST | `/content/user/relation-group/delete` | body: `{ groupId }` |

**关键 VO 结构**：

```typescript
interface RelationGroupVO {
  groupId: string;
  name: string;
  isDefault: boolean;
  sortOrder: number;
  memberCount: number;
}
```

### 5.3 关注流 API

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取关注流 | GET | `/content/user/feed/follow` | params: `{ userId, page, size }` |
| 获取关注流设置 | GET | `/content/user/feed/setting` | params: `{ userId }` |
| 更新关注流设置 | POST | `/content/user/feed/setting/update` | body: `{ activityTypes[] }` |

**关键 VO 结构**：

```typescript
interface FollowFeedItemVO {
  activityId: string;
  type: 'publish' | 'like' | 'favorite';
  userId: string;
  userNickname: string;
  userAvatar: string;
  contentId: string;
  contentSummary: string;
  contentType: string;
  activityTime: string;
  isSpecialFollow: boolean;
}

interface FeedSettingVO {
  userId: string;
  activityTypes: ('publish' | 'like' | 'favorite')[];
}
```

### 5.4 关注推荐 API

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取推荐列表 | GET | `/content/user/relation/recommend` | params: `{ userId, interestTags?, page, size }` |

**关键 VO 结构**：

```typescript
interface FollowRecommendItemVO {
  userId: string;
  nickname: string;
  avatar: string;
  bio: string;
  followerCount: number;
  reason: string; // 如"与您关注的 XXX 相似"
  reasonType: 'common_follow' | 'interest' | 'popular' | 'similar';
  rankingScore: number;
  relation: RelationDetailVO;
}
```

### 5.5 订阅 API

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 订阅内容源 | POST | `/content/user/subscription/subscribe` | body: `{ sourceType, sourceId, sourceName }` |
| 取消订阅 | POST | `/content/user/subscription/unsubscribe` | body: `{ subscriptionId }` |
| 暂停订阅 | POST | `/content/user/subscription/pause` | body: `{ subscriptionId }` |
| 恢复订阅 | POST | `/content/user/subscription/resume` | body: `{ subscriptionId }` |
| 我的订阅列表 | GET | `/content/user/subscription/list` | params: `{ userId, sourceType?, page, size }` |
| 订阅流 | GET | `/content/user/subscription/feed` | params: `{ userId, sourceType?, page, size }` |
| 批量暂停 | POST | `/content/user/subscription/batch-pause` | body: `{ subscriptionIds[] }` |
| 批量恢复 | POST | `/content/user/subscription/batch-resume` | body: `{ subscriptionIds[] }` |
| 批量取消 | POST | `/content/user/subscription/batch-cancel` | body: `{ subscriptionIds[] }` |

**关键 VO 结构**：

```typescript
interface SubscriptionItemVO {
  subscriptionId: string;
  sourceType: 'topic' | 'tag' | 'collection' | 'special' | 'column' | 'channel';
  sourceId: string;
  sourceName: string;
  sourceIcon: string;
  sourceCategory: string;
  subscriptionTime: string;
  lastUpdateTime: string;
  status: 'active' | 'paused';
  notificationConfig: SubscriptionNotificationConfigVO | null; // null 表示跟随全局
}

interface SubscriptionFeedItemVO {
  contentId: string;
  contentSummary: string;
  contentType: string;
  sourceType: string;
  sourceName: string;
  publishTime: string;
}
```

### 5.6 订阅广场 API

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 广场列表 | GET | `/content/user/subscription/plaza` | params: `{ category?, keyword?, page, size }` |
| 订阅源详情 | GET | `/content/user/subscription/source-detail` | params: `{ sourceType, sourceId }` |

**关键 VO 结构**：

```typescript
interface SubscriptionSourceVO {
  sourceType: string;
  sourceId: string;
  sourceName: string;
  description: string;
  category: string;
  subscriberCount: number;
  latestUpdateTime: string;
  heatScore: number;
  subscribed: boolean;
  recentContent: Array<{ contentId: string; summary: string; publishTime: string }>;
}
```

### 5.7 通知设置 API

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取全局通知设置 | GET | `/content/user/notification-setting/global` | params: `{ userId }` |
| 更新全局通知设置 | POST | `/content/user/notification-setting/global/update` | body: 通知配置 |
| 获取订阅通知设置 | GET | `/content/user/notification-setting/subscription` | params: `{ subscriptionId }` |
| 更新订阅通知设置 | POST | `/content/user/notification-setting/subscription/update` | body: 通知配置 |

**关键 VO 结构**：

```typescript
interface SubscriptionNotificationConfigVO {
  subscriptionId: string;
  channels: ('in_app' | 'push' | 'email')[];
  frequency: 'realtime' | 'daily';
  doNotDisturbStart: string; // HH:mm
  doNotDisturbEnd: string;   // HH:mm
}
```

---

## 6. 状态管理

### 6.1 Store 设计

使用 Pinia 进行状态管理，按领域拆分 store。

#### 6.1.1 useRelationStore（关注关系 Store）

```typescript
interface RelationState {
  // 关注列表
  followingList: FollowingItemVO[];
  followingTotal: number;
  followingLoading: boolean;
  followingFilters: { groupId: string | null; keyword: string; page: number; size: number };

  // 特别关注列表
  specialFollowingList: FollowingItemVO[];
  specialFollowingTotal: number;
  specialFollowingLoading: boolean;

  // 分组列表
  groups: RelationGroupVO[];

  // 关注流
  feedItems: FollowFeedItemVO[];
  feedLoading: boolean;
  feedHasMore: boolean;
  feedPage: number;
  feedSetting: FeedSettingVO | null;

  // 推荐列表
  recommendList: FollowRecommendItemVO[];
  recommendLoading: boolean;
  recommendHasMore: boolean;
  recommendPage: number;

  // 关系缓存（key: targetUserId）
  relationCache: Map<string, RelationDetailVO>;
}
```

**核心 Actions**：
- `fetchFollowing(filters)` - 加载关注列表
- `fetchSpecialFollowing()` - 加载特别关注列表
- `fetchGroups()` - 加载分组列表
- `fetchFollowFeed()` - 加载关注流
- `fetchRecommendations()` - 加载推荐列表
- `follow(targetUserId, groupId?)` - 关注
- `unfollow(targetUserId)` - 取消关注
- `specialFollow(targetUserId)` - 特别关注
- `cancelSpecialFollow(targetUserId)` - 取消特别关注
- `updateFeedSetting(types)` - 更新关注流设置
- `batchUnfollow(targetUserIds)` - 批量取消关注
- `batchMoveGroup(targetUserIds, groupId)` - 批量移动分组
- `createGroup(name, sortOrder)` - 创建分组
- `renameGroup(groupId, name)` - 重命名分组
- `deleteGroup(groupId)` - 删除分组
- `getRelationDetail(targetUserId)` - 获取关系详情（带缓存）

#### 6.1.2 useSubscriptionStore（订阅 Store）

```typescript
interface SubscriptionState {
  // 我的订阅列表
  mySubscriptions: SubscriptionItemVO[];
  mySubscriptionsTotal: number;
  mySubscriptionsLoading: boolean;
  mySubscriptionsFilters: { sourceType: string | null; page: number; size: number };

  // 订阅流
  subscriptionFeedItems: SubscriptionFeedItemVO[];
  subscriptionFeedLoading: boolean;
  subscriptionFeedHasMore: boolean;
  subscriptionFeedPage: number;

  // 订阅广场
  plazaSources: SubscriptionSourceVO[];
  plazaLoading: boolean;
  plazaHasMore: boolean;
  plazaPage: number;
  plazaFilters: { category: string | null; keyword: string };

  // 订阅源详情
  currentSourceDetail: SubscriptionSourceVO | null;

  // 订阅通知配置
  globalNotificationConfig: SubscriptionNotificationConfigVO | null;
  subscriptionNotificationConfigs: Map<string, SubscriptionNotificationConfigVO>;
}
```

**核心 Actions**：
- `fetchMySubscriptions(filters)` - 加载我的订阅
- `fetchSubscriptionFeed(filters)` - 加载订阅流
- `fetchPlaza(filters)` - 加载订阅广场
- `fetchSourceDetail(sourceType, sourceId)` - 加载订阅源详情
- `subscribe(sourceType, sourceId, sourceName)` - 订阅
- `unsubscribe(subscriptionId)` - 取消订阅
- `pauseSubscription(subscriptionId)` - 暂停订阅
- `resumeSubscription(subscriptionId)` - 恢复订阅
- `batchPause(subscriptionIds)` - 批量暂停
- `batchResume(subscriptionIds)` - 批量恢复
- `batchCancel(subscriptionIds)` - 批量取消
- `fetchGlobalNotificationConfig()` - 获取全局通知设置
- `updateGlobalNotificationConfig(config)` - 更新全局通知设置
- `fetchSubscriptionNotificationConfig(subscriptionId)` - 获取订阅级通知设置
- `updateSubscriptionNotificationConfig(subscriptionId, config)` - 更新订阅级通知设置

### 6.2 缓存策略

| 数据 | 缓存位置 | 失效策略 |
|------|---------|---------|
| 关系详情 | relationCache (Map) | 操作后立即更新 |
| 分组列表 | groups 数组 | 操作后刷新 |
| 关注流 | feedItems 数组 | 下拉刷新重置 |
| 推荐列表 | recommendList 数组 | 页面切换刷新 |
| 订阅状态 | sourceDetail 中的 subscribed 字段 | 操作后立即更新 |
| 通知配置 | subscriptionNotificationConfigs Map | 保存后更新 |

### 6.3 乐观更新

以下操作采用乐观更新策略，先更新 UI 再调用 API，失败时回滚：

- 关注/取消关注：立即切换按钮状态
- 特别关注/取消特别关注：立即更新标识
- 订阅/取消订阅/暂停/恢复：立即更新按钮和状态
- 移动分组：立即更新分组标签

---

## 7. 交互设计

### 7.1 关注操作交互

**关注按钮状态流转**：
1. **未关注**：蓝色"关注"按钮
2. **已关注**：灰色"已关注"按钮，hover 显示下拉菜单（取消关注 / 设为特别关注）
3. **特别关注**：金色"特别关注"按钮，hover 显示下拉菜单（取消特别关注 / 取消关注）

**关注确认**：取消关注时弹出二次确认弹窗"确定取消关注该用户？"

**禁止自关注**：用户自己的主页不显示关注按钮，或显示为禁用态。

### 7.2 分组管理交互

**创建分组**：
1. 点击"新建分组"按钮
2. 弹出输入弹窗，输入分组名称（实时校验唯一性和长度）
3. 确认后创建，列表刷新

**移动分组**：
1. 在用户卡片上点击"移动分组"
2. 弹出分组选择器（含"新建分组"选项）
3. 选择目标分组后确认，显示操作结果

**删除分组**：
1. 在分组管理页点击删除
2. 弹出二次确认"删除分组后，组内关注对象将移回默认分组"
3. 确认后删除，列表刷新

### 7.3 关注流交互

**动态类型筛选**：
- 筛选开关使用 Toggle 组件，默认全部开启
- 切换后立即刷新关注流数据

**特别关注置顶**：
- 特别关注用户的动态在每页顶部展示
- 使用特殊背景色或边框区分

**下拉刷新**：
- 下拉触发刷新，加载最新动态
- 无新内容时显示"暂无新内容"提示

**无限滚动**：
- 滚动到底部自动加载下一页
- 加载中显示骨架屏或 loading 动画

### 7.4 批量管理交互

**进入批量模式**：
1. 点击"批量管理"按钮
2. 列表项左侧出现勾选框
3. 底部出现批量操作栏

**批量操作**：
1. 勾选目标项
2. 底部操作栏显示已选数量和可用操作
3. 点击操作按钮执行
4. 操作完成后弹出 Toast 显示结果（如"成功取消关注 5 个，失败 1 个"）
5. 失败项可展开查看失败原因

**退出批量模式**：点击"完成"或"取消"退出批量模式。

### 7.5 订阅操作交互

**订阅按钮状态流转**：
1. **未订阅**：蓝色"订阅"按钮
2. **已订阅**：灰色"已订阅"按钮，hover 显示下拉菜单（暂停 / 取消订阅）
3. **暂停中**：橙色"已暂停"按钮，hover 显示下拉菜单（恢复 / 取消订阅）

### 7.6 通知设置交互

**全局设置**：
- 渠道开关：Switch 组件
- 频率选择：Radio 组件（实时推送 / 每日摘要）
- 免打扰时段：两个 TimePicker（开始时间、结束时间）

**订阅级覆盖**：
- 列表展示每个订阅源的当前通知配置
- 点击进入编辑弹窗，配置项同全局设置
- 未配置时显示"跟随全局设置"，可点击"自定义"展开配置

### 7.7 搜索交互

**关注列表搜索**：
- 搜索框支持实时搜索（防抖 300ms）
- 输入关键词后实时过滤列表
- 无结果时显示"未找到匹配的关注用户"

**订阅广场搜索**：
- 搜索框支持回车搜索
- 搜索结果替换广场默认列表
- 支持清空搜索回到默认视图

### 7.8 错误处理交互

| 错误场景 | 交互方式 |
|---------|---------|
| 关注自己 | Toast 提示"不能关注自己" |
| 关注已被拉黑用户 | Toast 提示"无法关注该用户" |
| 分组名重复 | 输入框下方红色提示"分组名已存在" |
| 批量操作部分失败 | Toast + 可展开的失败详情 |
| 网络错误 | Toast 提示"网络错误，请重试" + 重试按钮 |
| 加载失败 | 页面级错误状态 + 重试按钮 |

---

## 8. 响应式设计

### 8.1 断点定义

| 断点 | 宽度 | 设备 |
|------|------|------|
| xs | <576px | 手机竖屏 |
| sm | 576-767px | 手机横屏 |
| md | 768-991px | 平板 |
| lg | 992-1199px | 小桌面 |
| xl | >=1200px | 大桌面 |

### 8.2 各页面响应式策略

#### 关注流
- **xs/sm**：单列卡片流，卡片全宽
- **md**：单列卡片流，两侧留白
- **lg/xl**：双列瀑布流或单列等宽

#### 关注列表
- **xs/sm**：卡片式列表，每行 1 个用户
- **md**：卡片式列表，每行 2 个用户
- **lg/xl**：卡片式列表，每行 3 个用户

#### 订阅广场
- **xs/sm**：单列卡片，搜索框全宽
- **md**：双列网格
- **lg/xl**：三列或四列网格

#### 通知设置
- **xs/sm**：纵向堆叠，全宽输入
- **md+**：表单布局，左右分栏

### 8.3 移动端特殊处理

- 分组 Tab 使用横向滚动，不换行
- 批量操作栏在移动端固定底部
- 弹窗在移动端改为全屏或底部抽屉
- 搜索框在移动端可折叠为图标，点击展开

---

## 9. 性能要求

### 9.1 加载性能

| 指标 | 目标值 |
|------|--------|
| 关注流首次加载 | <2s |
| 关注/取消关注操作响应 | <500ms |
| 订阅/取消订阅操作响应 | <500ms |
| 关注列表加载 | <1.5s |
| 订阅广场加载 | <2s |
| 通知设置保存 | <1s |

### 9.2 优化策略

#### 9.2.1 数据加载优化

- **分页加载**：所有列表使用分页或无限滚动，避免一次加载全部数据
- **防抖搜索**：搜索输入防抖 300ms，减少无效请求
- **关系缓存**：已加载的关系详情缓存在 Map 中，避免重复请求
- **乐观更新**：关注/订阅操作先更新 UI 再请求，提升感知速度

#### 9.2.2 渲染优化

- **虚拟滚动**：关注流和订阅流等长列表使用虚拟滚动，只渲染可见区域
- **骨架屏**：列表加载时显示骨架屏，减少布局抖动
- **图片懒加载**：用户头像和内容图片使用懒加载
- **组件懒加载**：页面组件使用路由懒加载

#### 9.2.3 网络优化

- **请求合并**：同一页面的多个 API 请求尽量并行发起
- **缓存策略**：推荐列表、广场列表等变化频率低的数据使用适当缓存
- **错误重试**：网络失败自动重试 1 次，仍失败则提示用户

### 9.3 核心 Web 指标

| 指标 | 目标值 |
|------|--------|
| LCP (Largest Contentful Paint) | <2.5s |
| FID (First Input Delay) | <100ms |
| CLS (Cumulative Layout Shift) | <0.1 |

---

## 10. 测试要点

### 10.1 功能测试

#### 10.1.1 关注关系

| 测试项 | 测试场景 | 预期结果 |
|--------|---------|---------|
| 关注成功 | 点击关注按钮 | 按钮变为"已关注"，关系建立 |
| 取消关注 | 已关注状态下取消关注 | 二次确认后按钮恢复"关注" |
| 禁止自关注 | 查看自己的主页 | 不显示关注按钮或为禁用态 |
| 特别关注 | 已关注用户设为特别关注 | 标识变为金色，关注流置顶 |
| 取消特别关注 | 取消特别关注 | 恢复普通关注状态 |
| 被拉黑用户 | 尝试关注已拉黑用户 | 提示"无法关注该用户" |
| 关系详情 | 查询关系详情 | 返回完整关系状态字段 |

#### 10.1.2 分组管理

| 测试项 | 测试场景 | 预期结果 |
|--------|---------|---------|
| 默认分组 | 新用户首次关注 | 自动创建默认分组 |
| 创建分组 | 输入有效名称创建 | 分组列表新增一项 |
| 分组名重复 | 输入已存在的分组名 | 提示"分组名已存在" |
| 删除默认分组 | 尝试删除默认分组 | 操作被禁止 |
| 移动分组 | 将用户移入自定义分组 | 用户分组标签更新 |
| 移出分组 | 将用户从自定义分组移出 | 用户回到默认分组 |

#### 10.1.3 关注流

| 测试项 | 测试场景 | 预期结果 |
|--------|---------|---------|
| 动态展示 | 进入关注流页面 | 显示关注对象的发布/点赞/收藏动态 |
| 特别关注置顶 | 特别关注用户有新动态 | 动态显示在列表顶部 |
| 类型筛选 | 关闭"点赞"和"收藏"开关 | 仅显示发布动态 |
| 分页加载 | 滚动到底部 | 自动加载下一页 |
| 空状态 | 无关注对象 | 显示引导页面 |
| 取消关注后刷新 | 取消关注后下拉刷新 | 该用户动态不再显示 |

#### 10.1.4 关注推荐

| 测试项 | 测试场景 | 预期结果 |
|--------|---------|---------|
| 推荐列表 | 进入推荐页面 | 显示推荐用户和推荐理由 |
| 已关注排除 | 推荐列表不包含已关注用户 | 已关注用户不出现在列表 |
| 一键关注 | 点击推荐用户的关注按钮 | 按钮变为"已关注" |
| 推荐理由 | 查看推荐理由标签 | 显示如"与您关注的 XXX 相似" |

#### 10.1.5 订阅

| 测试项 | 测试场景 | 预期结果 |
|--------|---------|---------|
| 订阅成功 | 点击订阅按钮 | 按钮变为"已订阅" |
| 取消订阅 | 二次确认后取消 | 按钮恢复"订阅" |
| 暂停订阅 | 暂停已订阅的源 | 状态变为"已暂停"，不再接收更新 |
| 恢复订阅 | 恢复暂停的订阅 | 状态恢复"已订阅" |
| 订阅流 | 进入订阅流 | 显示订阅源的新内容 |
| 源类型筛选 | 按类型筛选订阅流 | 仅显示匹配类型的内容 |

#### 10.1.6 订阅广场

| 测试项 | 测试场景 | 预期结果 |
|--------|---------|---------|
| 热门列表 | 进入订阅广场 | 显示按热度排序的订阅源 |
| 分类浏览 | 选择分类 | 显示该分类下的订阅源 |
| 关键词搜索 | 输入关键词搜索 | 显示匹配的订阅源 |
| 源详情 | 点击订阅源 | 进入详情页，显示介绍和最近更新 |
| 广场订阅 | 在广场点击订阅 | 订阅成功，按钮状态更新 |

#### 10.1.7 通知设置

| 测试项 | 测试场景 | 预期结果 |
|--------|---------|---------|
| 全局设置 | 修改全局通知渠道 | 设置保存成功 |
| 频率设置 | 切换实时/每日摘要 | 设置保存成功 |
| 免打扰时段 | 设置免打扰时段 | 时段内不发送通知 |
| 订阅级覆盖 | 为单个订阅源自定义通知 | 该订阅源使用自定义配置 |
| 继承全局 | 订阅级未配置时 | 使用全局默认设置 |

#### 10.1.8 批量操作

| 测试项 | 测试场景 | 预期结果 |
|--------|---------|---------|
| 批量取消关注 | 选择多个用户取消关注 | 显示成功/失败数量 |
| 批量移动分组 | 选择多个用户移动分组 | 显示成功/失败数量 |
| 批量暂停订阅 | 选择多个订阅源暂停 | 显示成功/失败数量 |
| 部分失败 | 批量操作部分失败 | 显示失败详情和原因 |

### 10.2 兼容性测试

| 浏览器 | 版本 |
|--------|------|
| Chrome | 最新 2 个版本 |
| Firefox | 最新 2 个版本 |
| Safari | 最新 2 个版本 |
| Edge | 最新 2 个版本 |
| 微信内置浏览器 | 最新版 |
| iOS Safari | 最新版 |
| Android Chrome | 最新版 |

### 10.3 性能测试

| 测试项 | 测试方法 | 达标标准 |
|--------|---------|---------|
| 关注流加载 | 首屏加载时间测量 | <2s |
| 长列表滚动 | 1000+ 条数据滚动流畅度 | 60fps 无卡顿 |
| 批量操作 | 50 条批量操作响应时间 | <3s |
| 关注操作 | 点击到 UI 更新时间 | <500ms |
| 订阅操作 | 点击到 UI 更新时间 | <500ms |

### 10.4 异常场景测试

| 测试项 | 测试场景 | 预期结果 |
|--------|---------|---------|
| 网络断开 | 操作时断网 | 提示网络错误，支持重试 |
| Token 过期 | 操作时 Token 过期 | 自动跳转登录页 |
| 接口超时 | 接口响应超时 | 提示超时，支持重试 |
| 并发操作 | 快速连续点击关注/取消 | 防抖处理，最终状态正确 |
| 数据为空 | 各列表数据为空 | 显示正确的空状态引导 |
| 大数据量 | 关注列表 10000+ 条 | 分页加载正常，无性能问题 |
