# 社交模块组件文档

## 目录结构

```
src/views/social/
├── follow/              # 关注模块
│   ├── index.vue        # 关注列表
│   ├── special.vue      # 特别关注
│   ├── group.vue        # 分组管理
│   └── recommend.vue    # 关注推荐
├── feed/                # 信息流
│   └── index.vue        # 关注流
└── subscribe/           # 订阅模块
    ├── square.vue       # 订阅广场
    ├── manage.vue       # 订阅管理
    ├── notification.vue # 通知配置
    ├── feed.vue         # 订阅流
    └── detail.vue       # 订阅详情

src/components/social/
├── FollowButton.vue
├── SpecialFollowButton.vue
├── SubscribeButton.vue
├── UserCard.vue
├── SubscriptionCard.vue
├── FeedCard.vue
├── FeedFilter.vue
├── SpecialFeed.vue
└── BatchOperationBar.vue

src/store/modules/
├── follow.ts
├── subscribe.ts
└── feed.ts

src/api/content/
├── relation.ts
└── subscribe.ts
```

---

## 组件文档

### FollowButton

关注/取消关注按钮。已关注时 hover 显示"取消关注"，点击弹出 Popconfirm 确认。

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| userId | string | - | 当前登录用户 ID |
| targetUserId | string | - | 目标用户 ID |
| isFollowing | boolean | - | 是否已关注 |
| disabled | boolean | false | 是否禁用 |

**Events**

| 事件 | 参数 | 说明 |
|------|------|------|
| update:isFollowing | boolean | 关注状态变更（支持 v-model） |
| follow | - | 执行关注后触发 |
| unfollow | - | 执行取消关注后触发 |

**用法**

```vue
<FollowButton
  :user-id="currentUserId"
  :target-user-id="user.userId"
  v-model:is-following="user.isFollowing"
/>
```

---

### SpecialFollowButton

特别关注切换按钮。未关注时禁用（Tooltip 提示"请先关注该用户"），已关注时点击切换特别关注状态。

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| userId | string | - | 当前登录用户 ID |
| targetUserId | string | - | 目标用户 ID |
| isFollowing | boolean | - | 是否已关注（未关注则禁用） |
| isSpecial | boolean | - | 是否特别关注 |

**Events**

| 事件 | 参数 | 说明 |
|------|------|------|
| update:isSpecial | boolean | 特别关注状态变更（支持 v-model） |
| special | - | 设为特别关注后触发 |
| cancelSpecial | - | 取消特别关注后触发 |

**用法**

```vue
<SpecialFollowButton
  :user-id="currentUserId"
  :target-user-id="user.userId"
  :is-following="user.isFollowing"
  v-model:is-special="user.isSpecial"
/>
```

---

### SubscribeButton

订阅/取消订阅按钮。已订阅时 hover 显示"取消订阅"，点击弹出 Popconfirm 确认。

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| userId | string | - | 当前登录用户 ID |
| sourceId | string | - | 订阅源 ID |
| sourceType | string | - | 订阅源类型 |
| isSubscribed | boolean | - | 是否已订阅 |
| isPaused | boolean | false | 是否已暂停 |

**Events**

| 事件 | 参数 | 说明 |
|------|------|------|
| update:isSubscribed | boolean | 订阅状态变更（支持 v-model） |
| subscribe | - | 执行订阅后触发 |
| unsubscribe | - | 执行取消订阅后触发 |

**用法**

```vue
<SubscribeButton
  :user-id="currentUserId"
  :source-id="source.sourceId"
  :source-type="source.sourceType"
  v-model:is-subscribed="source.isSubscribed"
/>
```

---

### UserCard

用户信息卡片，展示头像、昵称、分组标签、简介、关注时间。桌面端直接显示操作按钮，移动端折叠为下拉菜单。

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| userId | string | - | 当前登录用户 ID |
| user | UserProp | - | 用户信息对象（见下方类型） |
| isMobile | boolean | false | 是否移动端布局 |

**UserProp 类型**

```ts
interface UserProp {
  userId: string;
  nickname: string;
  avatar: string;
  bio?: string;
  followTime?: string;
  groupName?: string;
  isSpecial?: boolean;
  lastActiveTime?: string;
}
```

**Events**

| 事件 | 参数 | 说明 |
|------|------|------|
| unfollow | string (userId) | 取消关注后触发，参数为目标用户 ID |
| specialChange | string, boolean | 特别关注状态变更，参数为 (目标用户ID, isSpecial) |
| groupChange | string | 请求调整分组，参数为目标用户 ID |

**用法**

```vue
<UserCard
  :user-id="currentUserId"
  :user="followItem"
  :is-mobile="isMobile"
  @unfollow="handleUnfollow"
  @special-change="handleSpecialChange"
  @group-change="openGroupModal"
/>
```

---

### SubscriptionCard

订阅源信息卡片，展示图标、名称、分类标签、订阅人数、更新时间。支持暂停/恢复/取消订阅操作。

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| userId | string | - | 当前登录用户 ID |
| source | SourceProp | - | 订阅源信息（见下方类型） |
| isMobile | boolean | false | 是否移动端布局 |

**SourceProp 类型**

```ts
interface SourceProp {
  sourceId: string;
  sourceName: string;
  sourceIcon: string;
  sourceType: string;
  category: string;
  subscriberCount: number;
  lastUpdateTime: string;
  status: 'active' | 'paused';
}
```

**Events**

| 事件 | 参数 | 说明 |
|------|------|------|
| subscribe | string (sourceId) | 订阅后触发 |
| unsubscribe | string (sourceId) | 取消订阅后触发 |
| pause | string (sourceId) | 暂停订阅后触发 |
| resume | string (sourceId) | 恢复订阅后触发 |

**用法**

```vue
<SubscriptionCard
  :user-id="currentUserId"
  :source="subscribeItem"
  :is-mobile="isMobile"
  @unsubscribe="handleRemove"
/>
```

---

### FeedCard

动态信息流卡片，展示用户头像、昵称、动态类型标签（发帖/点赞/收藏）、来源标签、标题、摘要。特别关注内容高亮显示（黄色左边框 + 浅黄背景）。

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| feed | FeedProp | - | 动态数据（见下方类型） |
| isMobile | boolean | false | 是否移动端布局 |

**FeedProp 类型**

```ts
interface FeedProp {
  id: string;
  userId: string;
  nickname: string;
  avatar: string;
  contentId: string;
  contentTitle: string;
  contentSummary: string;
  dynamicType: 'post' | 'like' | 'favorite';
  sourceType?: string;
  sourceName?: string;
  createTime: string;
  isPriority: boolean;
}
```

**Events**

| 事件 | 参数 | 说明 |
|------|------|------|
| click | FeedProp | 点击卡片时触发，参数为完整 feed 对象 |

**用法**

```vue
<FeedCard
  :feed="feedItem"
  :is-mobile="isMobile"
  @click="handleFeedClick"
/>
```

---

### FeedFilter

动态类型筛选器，支持按"发帖/点赞/收藏"多选过滤。使用 v-model 双向绑定选中类型。

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| types | string[] | - | 可选类型列表，如 `['post', 'like', 'favorite']` |
| modelValue | string[] | - | 当前选中类型（支持 v-model） |

**Events**

| 事件 | 参数 | 说明 |
|------|------|------|
| update:modelValue | string[] | 选中类型变更 |

**用法**

```vue
<FeedFilter
  :types="['post', 'like', 'favorite']"
  v-model="selectedTypes"
/>
```

---

### SpecialFeed

特别关注动态列表组件。展示特别关注用户的动态，支持加载更多。

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| feeds | FeedItem[] | - | 动态列表数据 |
| loading | boolean | false | 是否加载中 |
| isMobile | boolean | false | 是否移动端布局 |
| hasMore | boolean | false | 是否有更多数据 |

**Events**

| 事件 | 参数 | 说明 |
|------|------|------|
| loadMore | - | 点击"加载更多"时触发 |

**用法**

```vue
<SpecialFeed
  :feeds="specialFeeds"
  :loading="loading"
  :has-more="hasMore"
  @load-more="fetchMore"
/>
```

---

### BatchOperationBar

批量操作底栏。选中项 > 0 时固定显示在页面底部，支持批量取消关注、暂停、恢复、取消订阅。

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| selectedCount | number | - | 已选中项数量 |
| loading | boolean | false | 操作加载中状态 |

**Events**

| 事件 | 参数 | 说明 |
|------|------|------|
| cancel | - | 取消选择 |
| batchUnfollow | - | 批量取消关注 |
| batchPause | - | 批量暂停订阅 |
| batchResume | - | 批量恢复订阅 |
| batchCancel | - | 批量取消订阅 |

**用法**

```vue
<BatchOperationBar
  :selected-count="selectedIds.length"
  :loading="batchLoading"
  @batch-unfollow="handleBatchUnfollow"
  @batch-pause="handleBatchPause"
  @cancel="clearSelection"
/>
```

---

## Store 文档

### useFollowStore

关注状态管理，位于 `src/store/modules/follow.ts`。

**State**

| 属性 | 类型 | 说明 |
|------|------|------|
| followList | FollowItem[] | 关注列表 |
| specialFollowList | FollowItem[] | 特别关注列表 |
| followGroups | FollowGroup[] | 关注分组列表 |
| totalFollows | number | 关注总数 |
| totalSpecialFollows | number | 特别关注总数 |
| loading | boolean (computed) | 合并加载状态 |
| followListLoading | boolean | 关注列表加载中 |
| specialFollowLoading | boolean | 特别关注列表加载中 |
| recommendationsLoading | boolean | 推荐列表加载中 |
| currentPage / pageSize / hasMore | - | 关注列表分页 |
| specialCurrentPage / specialHasMore | - | 特别关注列表分页 |
| recommendations | RecommendationItem[] | 推荐关注列表 |
| searchKeyword | string | 搜索关键词 |
| selectedGroupId | string | 当前选中分组 ID |
| defaultGroup | FollowGroup (computed) | 默认分组 |
| customGroups | FollowGroup[] (computed) | 自定义分组列表 |

**Methods**

| 方法 | 参数 | 说明 |
|------|------|------|
| fetchFollowList | userId, reset? | 获取关注列表（分页/重置） |
| fetchSpecialFollowList | userId, reset? | 获取特别关注列表 |
| fetchFollowGroups | userId | 获取分组列表 |
| createGroup | userId, name, sortOrder? | 创建分组 |
| updateGroup | userId, groupId, name | 重命名分组 |
| removeGroup | userId, groupId | 删除分组 |
| follow | userId, targetUserId, groupId? | 关注用户 |
| unfollow | userId, targetUserId | 取消关注 |
| setSpecial | userId, targetUserId | 设为特别关注 |
| cancelSpecial | userId, targetUserId | 取消特别关注 |
| moveToGroup | userId, targetUserId, groupId | 移动到分组 |
| removeUserFromGroup | userId, targetUserId, groupId | 从分组移除 |
| fetchRecommendations | reset? | 获取推荐关注列表 |
| dismissRecommendation | userId | 本地移除推荐项 |
| batchUnfollowUsers | userId, targetUserIds[] | 批量取消关注 |
| batchCancelSpecialUsers | userId, targetUserIds[] | 批量取消特别关注 |

---

### useSubscribeStore

订阅状态管理，位于 `src/store/modules/subscribe.ts`。

**State**

| 属性 | 类型 | 说明 |
|------|------|------|
| subscribeList | SubscribeItem[] | 订阅列表 |
| totalSubscribes | number | 订阅总数 |
| loading | boolean | 加载状态 |
| currentPage / pageSize / hasMore | - | 分页状态 |
| searchKeyword | string | 搜索关键词 |
| selectedSourceType | string | 按来源类型筛选 |
| currentNotificationConfig | NotificationConfig | 当前通知配置 |
| globalNotificationDefault | NotificationConfig | 全局默认通知配置 |

**Methods**

| 方法 | 参数 | 说明 |
|------|------|------|
| fetchSubscribeList | userId, reset? | 获取订阅列表 |
| subscribe | userId, sourceId, sourceType | 订阅来源 |
| unsubscribe | userId, sourceId | 取消订阅 |
| pause | userId, sourceId | 暂停订阅 |
| resume | userId, sourceId | 恢复订阅 |
| fetchNotificationConfig | userId, sourceId | 获取通知偏好 |
| saveConfig | userId, sourceId, config | 保存通知偏好 |
| fetchGlobalNotificationDefault | forceRefresh? | 获取全局默认通知配置 |
| fetchPlaza | params? | 获取订阅广场列表 |
| fetchSourceDetail | sourceId | 获取订阅源详情 |
| subscribeFromPlaza | userId, sourceId, sourceType | 从广场订阅 |
| batchPause | userId, sourceIds[] | 批量暂停 |
| batchResume | userId, sourceIds[] | 批量恢复 |
| batchCancel | userId, sourceIds[] | 批量取消订阅 |

---

### useFeedStore

信息流状态管理，位于 `src/store/modules/feed.ts`。

**State**

| 属性 | 类型 | 说明 |
|------|------|------|
| followFeedList | FeedItem[] | 关注动态列表 |
| subscribeFeedList | FeedItem[] | 订阅动态列表 |
| priorityItems | FeedItem[] | 优先/置顶动态 |
| followLoading | boolean | 关注流加载中 |
| subscribeLoading | boolean | 订阅流加载中 |
| followPage / followHasMore | - | 关注流分页 |
| subscribePage / subscribeHasMore | - | 订阅流分页 |
| followTypes | string[] | 当前筛选的动态类型 |
| subscribeSourceType | string | 当前筛选的订阅源类型 |

**Methods**

| 方法 | 参数 | 说明 |
|------|------|------|
| fetchFollowFeed | reset? | 获取关注动态流 |
| fetchSubscribeFeed | reset? | 获取订阅动态流 |
| setFollowTypes | types[] | 设置动态类型筛选并刷新 |
| setSubscribeSourceType | type | 设置订阅源类型筛选并刷新 |
| removeUserFeeds | userId | 本地移除某用户的所有动态（返回被移除项，用于撤销） |
| restoreUserFeeds | { items, indices } | 恢复被移除的动态到原位置 |

---

## API 文档

### relation.ts

关注相关接口，位于 `src/api/content/relation.ts`。

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| getMutualFollowList | GET | /content/user/relation/mutual-follow-list | 查询互关好友列表 |
| getRelationDetail | GET | /content/user/relation/detail | 查询与目标用户的关系详情 |
| followUser | POST | /content/user/relation/follow | 关注用户 |
| unfollowUser | POST | /content/user/relation/unfollow | 取消关注 |
| setSpecialFollow | POST | /content/user/relation/special-follow | 设为特别关注 |
| cancelSpecialFollow | POST | /content/user/relation/special-follow/cancel | 取消特别关注 |
| getFollowGroupList | GET | /content/user/relation/groups | 获取关注分组列表 |
| createFollowGroup | POST | /content/user/relation/group/create | 创建关注分组 |
| renameFollowGroup | POST | /content/user/relation/group/rename | 重命名关注分组 |
| deleteFollowGroup | POST | /content/user/relation/group/delete | 删除关注分组 |
| moveFollowGroup | POST | /content/user/relation/group/move | 移动关注到分组 |
| removeFromGroup | POST | /content/user/relation/group/remove | 从分组移除 |
| getFollowList | GET | /content/user/relation/follow-list | 获取关注列表 |
| getSpecialFollowList | GET | /content/user/relation/special-follow-list | 获取特别关注列表 |
| getRecommendations | GET | /content/user/relation/recommendations | 获取推荐关注 |
| batchUnfollow | POST | /content/user/relation/batch/unfollow | 批量取消关注 |
| batchCancelSpecial | POST | /content/user/relation/batch/special-follow/cancel | 批量取消特别关注 |
| getFollowingFeed | GET | /content/user/relation/feed | 获取关注动态 Feed |

### subscribe.ts

订阅相关接口，位于 `src/api/content/subscribe.ts`。

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| subscribeSource | POST | /content/user/subscription/subscribe | 订阅来源 |
| cancelSubscription | POST | /content/user/subscription/cancel | 取消订阅 |
| pauseSubscription | POST | /content/user/subscription/pause | 暂停订阅 |
| resumeSubscription | POST | /content/user/subscription/resume | 恢复订阅 |
| getSubscribeList | GET | /content/user/subscription/list | 获取订阅列表 |
| getSubscribeFeed | GET | /content/user/subscription/feed | 获取订阅 Feed |
| getSubscribePlaza | GET | /content/user/subscription/plaza | 获取订阅广场 |
| getSubscribeSourceDetail | GET | /content/user/subscription/source/detail | 获取订阅源详情 |
| subscribeFromPlaza | POST | /content/user/subscription/source/subscribe | 从广场订阅 |
| batchPauseSubscribe | POST | /content/user/subscription/batch/pause | 批量暂停 |
| batchResumeSubscribe | POST | /content/user/subscription/batch/resume | 批量恢复 |
| batchCancelSubscribe | POST | /content/user/subscription/batch/cancel | 批量取消订阅 |
| getNotificationPreference | GET | /content/user/subscription/notification/preference | 获取通知偏好 |
| saveNotificationPreference | POST | /content/user/subscription/notification/preference | 保存通知偏好 |
