## Context

内容社区前端需要构建完整的社交关系与内容订阅系统。当前系统缺少用户关注体系和多维度内容订阅能力，用户难以高效管理信息流。本次开发将基于现有JeecgBoot Vue3框架和Ant Design Vue 4组件库，新增关注、订阅、信息流等功能模块。

**技术栈约束**:
- 前端框架: Vue 3 + TypeScript
- UI组件库: Ant Design Vue 4（已集成，自动导入）
- 状态管理: Pinia
- 路由: Vue Router
- 构建工具: Vite
- HTTP客户端: Axios

**现有系统依赖**:
- 用户系统: 用户信息、头像、昵称等基础数据
- 内容系统: 专题、话题、栏目等内容源
- 通知系统: 站内通知、推送通知
- 权限系统: 用户认证和授权

## Goals / Non-Goals

**Goals:**
- 构建完整的用户关注体系，支持关注/取消关注、特别关注、关注分组
- 实现多维度内容订阅能力，支持专题/话题/栏目等内容源订阅
- 开发关注流和订阅流，展示关注对象和订阅源的更新内容
- 提供批量管理功能，高效管理大量关注和订阅关系
- 实现响应式布局，支持PC/平板/手机多端适配
- 满足性能要求：关注流首次加载<2s，操作响应<500ms

**Non-Goals:**
- 不实现付费订阅功能（仅保留状态扩展点）
- 不引入机器学习推荐算法（使用可解释规则）
- 不重构拉黑与屏蔽语义（仅在查询中尊重现有状态）
- 不迁移 `/api/v1/*` 路径到 `/content/user/*`
- 不实现国际化支持（默认中文界面）

## Decisions

### 1. 状态管理方案

**决策**: 使用Pinia store分别管理关注状态和订阅状态

**理由**:
- 关注和订阅是独立的业务域，状态分离便于维护
- Pinia是Vue 3官方推荐的状态管理方案，支持TypeScript
- 独立store可以按需加载，优化首屏性能

**替代方案**:
- 单一store管理所有社交状态：可能导致store过大，维护困难
- 使用Vuex：Vue 3官方已推荐Pinia，Vuex即将进入维护模式

### 2. 关注流实现模式

**决策**: 采用读扩散模式，实时查询聚合

**理由**:
- 实现简单，不需要维护消息扇出机制
- 数据实时性好，用户发布内容后立即可见
- 适合当前用户规模，性能可接受

**替代方案**:
- 写扩散模式：发布时推送到所有粉丝的收件箱，实时性好但实现复杂
- 混合模式：大V使用写扩散，普通用户使用读扩散，实现复杂度高

**性能优化策略**:
- 关注数>500时，仅聚合最近7天动态
- 关注数>2000时，仅聚合最近3天动态
- 单次聚合查询时间预算2s，超时降级返回最近N小时动态

### 3. 特别关注置顶方案

**决策**: 后端分两个区域返回数据，前端拼接展示

**理由**:
- 后端负责数据筛选和排序，逻辑清晰
- 前端无需二次排序，减少计算开销
- 接口返回 `{ priorityItems: [...], items: [...] }` 结构明确

**替代方案**:
- 前端排序：需要前端维护排序逻辑，增加复杂度
- 单一列表+标记：前端需要遍历列表进行分区，性能较差

### 4. 批量操作结果处理

**决策**: 后端返回成功数、失败数和失败原因明细，前端按明细展示结果

**理由**:
- 后端负责业务校验和错误分类，逻辑清晰
- 前端专注于展示和交互，职责明确
- 支持可重试失败和不可重试失败的区分处理

**失败分类**:
- 可重试失败（网络超时、服务端500等）：标橙色，显示"可重试"标签
- 不可重试失败（目标用户已注销、已被拉黑等）：标红色，显示具体原因

### 5. 响应式布局策略

**决策**: 采用断点系统 + 组件级响应式适配

**理由**:
- 断点系统统一管理不同设备的布局规则
- 组件级适配可以精细控制每个组件在不同设备下的表现
- 使用Ant Design Vue的响应式工具，与组件库风格一致

**断点定义**:
- xs: <576px（手机竖屏）
- sm: 576-767px（手机横屏）
- md: 768-991px（平板）
- lg: 992-1199px（小桌面）
- xl: >=1200px（桌面）

### 6. 订阅通知配置层级

**决策**: 订阅级配置覆盖全局默认值，未配置时回退全局设置

**理由**:
- 全局默认值提供基础配置，减少用户配置成本
- 订阅级配置允许用户针对特定订阅源进行个性化设置
- 配置层级清晰，易于理解和维护

**数据流**:
1. 用户登录后请求全局默认配置，缓存在Pinia store
2. 页面加载时优先读取缓存，无需重复请求
3. 订阅级配置通过独立接口获取，覆盖全局默认值

## Risks / Trade-offs

### 风险1: 关注流聚合查询性能

**风险**: 当用户关注数较多时，关注流聚合查询可能性能较差

**缓解措施**:
- 实施大关注量用户降级策略（关注数>500/2000时减少聚合时间范围）
- 设置单次聚合查询时间预算（2s），超时降级
- 考虑引入缓存机制，缓存热点用户的动态数据

### 风险2: 批量操作数据一致性

**风险**: 批量操作过程中可能出现部分成功、部分失败的情况

**缓解措施**:
- 后端返回详细的成功/失败明细，前端按明细展示
- 支持可重试失败的重试机制（最多重试2轮）
- 前端乐观更新UI，失败时回滚

### 风险3: 移动端性能

**风险**: 移动端设备性能有限，长列表滚动可能卡顿

**缓解措施**:
- 实施虚拟滚动，只渲染可见区域的卡片
- 使用骨架屏提升加载体验
- 优化图片加载，使用懒加载和适当压缩

### 风险4: 多端适配复杂度

**风险**: PC/平板/手机多端适配增加开发和测试工作量

**缓解措施**:
- 建立统一的响应式断点系统
- 使用组件级响应式适配，减少全局样式冲突
- 优先保证核心功能在所有设备下的可用性

### 权衡1: 读扩散 vs 写扩散

**权衡**: 选择读扩散模式牺牲了部分实时性，换取了实现简单性

**影响**: 大V发布内容后，粉丝可能需要几秒到几十秒才能在关注流中看到

### 权衡2: 前端排序 vs 后端排序

**权衡**: 特别关注置顶采用后端排序，增加了接口复杂度，但减少了前端计算

**影响**: 接口返回数据结构更复杂，但前端逻辑更简单

## API Endpoints

### 用户关注相关 API

**Controller**: `ContentUserRelationController`
**Base Path**: `/content/user/relation`

| 端点 | 方法 | 说明 | 对应 Spec |
|------|------|------|-----------|
| `/follow` | POST | 关注用户 | user-follow-system Req 1 |
| `/unfollow` | POST | 取消关注 | user-follow-system Req 1 |
| `/special-follow` | POST | 特别关注用户 | user-follow-system Req 2 |
| `/special-follow/cancel` | POST | 取消特别关注 | user-follow-system Req 2 |
| `/groups` | GET | 查询关注分组 | user-follow-system Req 3 |
| `/group/create` | POST | 创建关注分组 | user-follow-system Req 3 |
| `/group/rename` | POST | 重命名关注分组 | user-follow-system Req 3 |
| `/group/delete` | POST | 删除关注分组 | user-follow-system Req 3 |
| `/group/move` | POST | 移动关注对象到分组 | user-follow-system Req 3 |
| `/group/remove` | POST | 移出关注分组 | user-follow-system Req 3 |
| `/follow-list` | GET | 分页查询关注列表 | user-follow-system Req 4 |
| `/special-follow-list` | GET | 分页查询特别关注列表 | user-follow-system Req 5 |
| `/recommendations` | GET | 分页查询关注推荐 | user-follow-system Req 6 |
| `/batch/unfollow` | POST | 批量取消关注 | user-follow-system Req 7 |
| `/batch/special-follow/cancel` | POST | 批量取消特别关注 | user-follow-system Req 7 |
| `/feed` | GET | 分页查询关注流 | social-feed Req 1 |
| `/mutual-follow-list` | GET | 分页查询互关好友列表 | - |
| `/detail` | GET | 查询关系 | - |
| `/block` | POST | 拉黑用户 | - |
| `/unblock` | POST | 解除拉黑 | - |
| `/mute` | POST | 屏蔽用户 | - |
| `/mute/cancel` | POST | 解除屏蔽 | - |
| `/blacklist` | GET | 分页查询黑名单 | - |
| `/block-mute/help` | GET | 获取拉黑/屏蔽帮助说明 | - |

### 内容订阅相关 API

**Controller**: `ContentUserSubscriptionController`
**Base Path**: `/content/user/subscription`

| 端点 | 方法 | 说明 | 对应 Spec |
|------|------|------|-----------|
| `/subscribe` | POST | 订阅内容源 | content-subscription Req 1 |
| `/cancel` | POST | 取消订阅 | content-subscription Req 1 |
| `/pause` | POST | 暂停订阅 | content-subscription Req 2 |
| `/resume` | POST | 恢复订阅 | content-subscription Req 2 |
| `/list` | GET | 查询订阅列表 | content-subscription Req 5 |
| `/feed` | GET | 查询订阅流 | social-feed Req 2 |
| `/plaza` | GET | 查询订阅广场 | content-subscription Req 4 |
| `/source/detail` | GET | 查询订阅源详情 | content-subscription Req 4 |
| `/source/subscribe` | POST | 从订阅广场订阅内容源 | content-subscription Req 4 |
| `/source/save` | POST | 写入订阅源目录 | - |
| `/batch/pause` | POST | 批量暂停订阅 | content-subscription Req 5 |
| `/batch/resume` | POST | 批量恢复订阅 | content-subscription Req 5 |
| `/batch/cancel` | POST | 批量取消订阅 | content-subscription Req 5 |
| `/notification/preference` | POST | 保存订阅级通知偏好 | content-subscription Req 3 |
| `/notification/preference` | GET | 查询订阅级有效通知偏好 | content-subscription Req 3 |
| `/notification/decision` | GET | 计算订阅源更新通知决策 | - |

### API 请求/响应格式

**请求格式**:
- 所有 POST 请求使用 `application/json` 格式
- 请求参数通过 `@RequestBody` 传递
- 用户 ID 通过 `@RequestParam("userId")` 传递

**响应格式**:
- 所有响应使用 `Result<T>` 包装
- 成功响应: `Result.OK(data)`
- 失败响应: `Result.error(message)`

**示例**:
```typescript
// 关注用户请求
POST /content/user/relation/follow?userId=current_user_id
Content-Type: application/json

{
  "targetUserId": "target_user_id",
  "relationGroupId": "group_id"  // 可选
}

// 成功响应
{
  "success": true,
  "message": "关注成功",
  "code": 200,
  "result": "关注成功"
}
```

## File Structure

```
src/
├── views/
│   └── social/
│       ├── follow/
│       │   ├── index.vue              # 关注列表页面
│       │   ├── special.vue            # 特别关注列表页面
│       │   ├── group.vue              # 分组管理页面
│       │   └── recommend.vue          # 关注推荐页面
│       ├── feed/
│       │   ├── index.vue              # 关注流页面
│       │   └── components/
│       │       ├── FeedCard.vue       # 动态卡片组件
│       │       ├── FeedFilter.vue     # 动态类型筛选组件
│       │       └── SpecialFeed.vue    # 特别关注动态分区组件
│       └── subscribe/
│           ├── index.vue              # 订阅流页面
│           ├── square.vue             # 订阅广场页面
│           ├── detail.vue             # 订阅源详情页面
│           ├── manage.vue             # 订阅管理页面
│           └── notification.vue       # 通知配置页面
├── components/
│   └── social/
│       ├── FollowButton.vue           # 关注按钮组件
│       ├── SpecialFollowButton.vue    # 特别关注按钮组件
│       ├── SubscribeButton.vue        # 订阅按钮组件
│       ├── UserCard.vue               # 用户卡片组件
│       ├── SubscriptionCard.vue       # 订阅源卡片组件
│       └── BatchOperationBar.vue      # 批量操作栏组件
├── stores/
│   ├── follow.ts                      # 关注状态管理
│   ├── subscribe.ts                   # 订阅状态管理
│   └── feed.ts                        # 信息流状态管理
├── api/
│   ├── follow.ts                      # 关注相关API
│   ├── subscribe.ts                   # 订阅相关API
│   └── feed.ts                        # 信息流相关API
├── hooks/
│   ├── useFollow.ts                   # 关注相关hooks
│   ├── useSubscribe.ts                # 订阅相关hooks
│   └── useFeed.ts                     # 信息流相关hooks
└── styles/
    └── social.scss                    # 社交模块样式
```

## Test Strategy

### 单元测试

**关注按钮组件测试** (`FollowButton.spec.ts`):
- 测试关注/取消关注状态切换
- 测试禁止自关注逻辑
- 测试防抖功能（500ms内不重复请求）
- 测试乐观更新和失败回滚
- 测试加载状态和禁用状态

**特别关注按钮组件测试** (`SpecialFollowButton.spec.ts`):
- 测试未关注时禁用状态
- 测试已关注时星标切换
- 测试二次确认弹窗逻辑
- 测试空状态引导

**订阅按钮组件测试** (`SubscribeButton.spec.ts`):
- 测试订阅/取消订阅状态切换
- 测试暂停/恢复订阅功能
- 测试重复订阅处理

**批量操作栏组件测试** (`BatchOperationBar.spec.ts`):
- 测试多选和全选功能
- 测试操作按钮禁用状态
- 测试结果弹窗展示
- 测试重试失败项逻辑

### 集成测试

**关注列表页面测试** (`follow/index.spec.ts`):
- 测试列表分页加载
- 测试搜索功能（300ms防抖）
- 测试分组筛选
- 测试批量管理入口

**关注流页面测试** (`feed/index.spec.ts`):
- 测试动态类型筛选
- 测试特别关注置顶展示
- 测试滚动加载更多
- 测试下拉刷新

**订阅广场页面测试** (`subscribe/square.spec.ts`):
- 测试分类浏览
- 测试搜索功能
- 测试订阅操作
- 测试响应式布局

### E2E测试

**完整用户流程测试** (`social-e2e.spec.ts`):
- 测试关注用户 → 设置特别关注 → 查看关注流 → 批量管理完整流程
- 测试订阅内容源 → 配置通知 → 查看订阅流 → 管理订阅完整流程
- 测试PC/平板/手机三种设备下的布局和交互

### 性能测试

**关注流性能测试**:
- 测试首次加载时间（目标<2s）
- 测试滚动加载流畅度
- 测试大关注量用户（500+/2000+）的降级策略

**批量操作性能测试**:
- 测试100条批量操作的响应时间（目标<3s）
- 测试批量操作的结果展示性能

## Migration Plan

### 部署顺序

1. **后端API部署**:
   - 部署用户关注相关API
   - 部署内容订阅相关API
   - 部署信息流相关API
   - 部署批量操作API

2. **数据库迁移**:
   - 创建user_follow表
   - 创建user_follow_group表
   - 创建user_subscribe表
   - 创建subscribe_notification_config表
   - 创建相关索引

3. **前端部署**:
   - 部署关注模块页面和组件
   - 部署订阅模块页面和组件
   - 部署信息流模块页面和组件
   - 更新路由配置

### 回滚策略

**后端回滚**:
- 保留旧版本API，新版本API使用新路径（如/v2/follow）
- 数据库迁移脚本支持回滚
- 监控API错误率，异常时快速回滚

**前端回滚**:
- 使用特性开关控制新功能显示
- 保留旧版本页面，新功能使用新路由
- 监控前端错误日志，异常时禁用新功能

### 验收条件

**功能验收**:
- 所有功能列表中的功能正常工作
- 所有验收标准通过
- 所有边界情况正确处理

**性能验收**:
- 关注流首次加载<2s
- 关注/订阅操作响应<500ms
- 长列表（1000+条）滚动流畅
- 批量操作（100条）<3s完成

**兼容性验收**:
- Chrome/Firefox/Safari/Edge浏览器兼容
- PC/平板/手机三种设备布局正确
- 触摸目标>=44px

## Open Questions

1. **关注流中的点赞/收藏动态是否需要受内容作者隐私或内容可见范围二次过滤？**
   - 当前假设：必须过滤，不展示被屏蔽内容
   - 需要与后端确认过滤逻辑的实现位置

2. **订阅源（专题、合集、话题、栏目）当前是否都有稳定的内容主表？**
   - 当前假设：需要先定义轻量目录表承载广场展示
   - **前置依赖**：订阅广场的分类浏览和搜索功能依赖统一的订阅源目录表数据模型
   - 在本PRD落地前，必须先完成后端数据模型设计评审

3. **邮件摘要由内容社区自建定时任务发送，还是接入系统现有消息中心统一发送？**
   - 当前假设：暂不在前端处理，后端统一调度
   - 需要与后端确认邮件发送的技术方案

4. **关注列表和订阅列表是否需要同步展示粉丝数、订阅人数等计数快照？**
   - 当前假设：列表VO聚合返回，前端直接展示
   - 需要确认计数数据的更新频率和一致性保证

5. **大关注量用户的关注流降级策略是否需要对用户透明？**
   - 当前假设：降级对用户无感知，仅影响数据时效性
   - 需要确认是否需要向用户展示降级提示
