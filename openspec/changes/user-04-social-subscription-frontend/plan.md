# Social Subscription Frontend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建完整的社交关系与内容订阅前端系统，包括关注体系、订阅系统、信息流和批量管理功能

**Architecture:** 基于Vue 3 + TypeScript + Ant Design Vue 4，采用Pinia状态管理，按业务域划分为关注、订阅、信息流三个独立模块，通过API层与后端交互，支持PC/平板/手机多端响应式布局

**Tech Stack:** Vue 3, TypeScript, Ant Design Vue 4, Pinia, Vue Router, Vite, Axios

---

## File Structure

```
jeecgboot-vue3/src/
├── api/
│   └── content/
│       ├── follow.ts              # 关注相关API
│       ├── subscribe.ts           # 订阅相关API
│       └── feed.ts                # 信息流相关API
├── components/
│   └── social/
│       ├── FollowButton.vue       # 关注按钮组件
│       ├── SpecialFollowButton.vue # 特别关注按钮组件
│       ├── SubscribeButton.vue    # 订阅按钮组件
│       ├── UserCard.vue           # 用户卡片组件
│       ├── SubscriptionCard.vue   # 订阅源卡片组件
│       ├── BatchOperationBar.vue  # 批量操作栏组件
│       ├── FeedCard.vue           # 动态卡片组件
│       ├── FeedFilter.vue         # 动态类型筛选组件
│       └── SpecialFeed.vue        # 特别关注动态分区组件
├── hooks/
│   └── social/
│       ├── useFollow.ts           # 关注相关hooks
│       ├── useSubscribe.ts        # 订阅相关hooks
│       └── useFeed.ts             # 信息流相关hooks
├── store/
│   └── modules/
│       ├── follow.ts              # 关注状态管理
│       ├── subscribe.ts           # 订阅状态管理
│       └── feed.ts                # 信息流状态管理
├── styles/
│   └── social.scss                # 社交模块样式
└── views/
    └── social/
        ├── follow/
        │   ├── index.vue          # 关注列表页面
        │   ├── special.vue        # 特别关注列表页面
        │   ├── group.vue          # 分组管理页面
        │   └── recommend.vue      # 关注推荐页面
        ├── feed/
        │   ├── index.vue          # 关注流页面
        │   └── components/
        │       ├── FeedCard.vue   # 动态卡片组件
        │       ├── FeedFilter.vue # 动态类型筛选组件
        │       └── SpecialFeed.vue # 特别关注动态分区组件
        └── subscribe/
            ├── index.vue          # 订阅流页面
            ├── square.vue         # 订阅广场页面
            ├── detail.vue         # 订阅源详情页面
            ├── manage.vue         # 订阅管理页面
            └── notification.vue   # 通知配置页面
```

---

## Task 1: 项目初始化与基础架构

**Files:**
- Create: `jeecgboot-vue3/src/api/content/follow.ts`
- Create: `jeecgboot-vue3/src/api/content/subscribe.ts`
- Create: `jeecgboot-vue3/src/api/content/feed.ts`
- Create: `jeecgboot-vue3/src/store/modules/follow.ts`
- Create: `jeecgboot-vue3/src/store/modules/subscribe.ts`
- Create: `jeecgboot-vue3/src/store/modules/feed.ts`
- Create: `jeecgboot-vue3/src/hooks/social/useFollow.ts`
- Create: `jeecgboot-vue3/src/hooks/social/useSubscribe.ts`
- Create: `jeecgboot-vue3/src/hooks/social/useFeed.ts`
- Create: `jeecgboot-vue3/src/styles/social.scss`

- [ ] **Step 1: 创建API目录和基础文件**

```bash
mkdir -p jeecgboot-vue3/src/api/content
mkdir -p jeecgboot-vue3/src/hooks/social
mkdir -p jeecgboot-vue3/src/styles
```

- [ ] **Step 2: 创建关注API文件**

```typescript
// jeecgboot-vue3/src/api/content/follow.ts
import { defHttp } from '/@/utils/http/axios';

enum Api {
  follow = '/content/user/relation/follow',
  unfollow = '/content/user/relation/unfollow',
  specialFollow = '/content/user/relation/special-follow',
  cancelSpecialFollow = '/content/user/relation/special-follow/cancel',
  followList = '/content/user/relation/follow-list',
  specialFollowList = '/content/user/relation/special-follow-list',
  followGroupList = '/content/user/relation/groups',
  createFollowGroup = '/content/user/relation/group/create',
  renameFollowGroup = '/content/user/relation/group/rename',
  deleteFollowGroup = '/content/user/relation/group/delete',
  moveFollowGroup = '/content/user/relation/group/move',
  removeFromGroup = '/content/user/relation/group/remove',
  recommendList = '/content/user/relation/recommendations',
  batchUnfollow = '/content/user/relation/batch/unfollow',
  batchCancelSpecial = '/content/user/relation/batch/special-follow/cancel',
}

/** 关注用户 */
export const followUser = (userId: string, data: { targetUserId: string; groupId?: string }) =>
  defHttp.post({ url: Api.follow, params: { userId }, data });

/** 取消关注 */
export const unfollowUser = (userId: string, targetUserId: string) =>
  defHttp.post({ url: Api.unfollow, params: { userId, targetUserId } });

/** 设置特别关注 */
export const setSpecialFollow = (userId: string, targetUserId: string) =>
  defHttp.post({ url: Api.specialFollow, params: { userId, targetUserId } });

/** 取消特别关注 */
export const cancelSpecialFollow = (userId: string, targetUserId: string) =>
  defHttp.post({ url: Api.cancelSpecialFollow, params: { userId, targetUserId } });

/** 获取关注列表 */
export const getFollowList = (userId: string, params?: { keyword?: string; groupId?: string; pageNo?: number; pageSize?: number }) =>
  defHttp.get({ url: Api.followList, params: { userId, ...params } });

/** 获取特别关注列表 */
export const getSpecialFollowList = (userId: string, params?: { pageNo?: number; pageSize?: number }) =>
  defHttp.get({ url: Api.specialFollowList, params: { userId, ...params } });

/** 获取关注分组列表 */
export const getFollowGroupList = (userId: string) =>
  defHttp.get({ url: Api.followGroupList, params: { userId } });

/** 创建关注分组 */
export const createFollowGroup = (userId: string, data: { name: string; sortOrder?: number }) =>
  defHttp.post({ url: Api.createFollowGroup, params: { userId }, data });

/** 重命名关注分组 */
export const renameFollowGroup = (userId: string, groupId: string, data: { name: string; sortOrder?: number }) =>
  defHttp.post({ url: Api.renameFollowGroup, params: { userId, groupId }, data });

/** 删除关注分组 */
export const deleteFollowGroup = (userId: string, groupId: string) =>
  defHttp.post({ url: Api.deleteFollowGroup, params: { userId, groupId } });

/** 移动关注对象到分组 */
export const moveFollowGroup = (userId: string, data: { targetUserIds: string[]; relationGroupId: string }) =>
  defHttp.post({ url: Api.moveFollowGroup, params: { userId }, data });

/** 移出关注分组 */
export const removeFromGroup = (userId: string, data: { targetUserIds: string[] }) =>
  defHttp.post({ url: Api.removeFromGroup, params: { userId }, data });

/** 获取推荐用户列表 */
export const getRecommendList = (params?: { page?: number; size?: number }) =>
  defHttp.get({ url: Api.recommendList, params });

/** 批量取消关注 */
export const batchUnfollow = (userId: string, data: { targetUserIds: string[] }) =>
  defHttp.post({ url: Api.batchUnfollow, params: { userId }, data });

/** 批量取消特别关注 */
export const batchCancelSpecialFollow = (userId: string, data: { targetUserIds: string[] }) =>
  defHttp.post({ url: Api.batchCancelSpecial, params: { userId }, data });
```

- [ ] **Step 3: 创建订阅API文件**

```typescript
// jeecgboot-vue3/src/api/content/subscribe.ts
import { defHttp } from '/@/utils/http/axios';

enum Api {
  subscribe = '/content/user/subscription/subscribe',
  cancel = '/content/user/subscription/cancel',
  pause = '/content/user/subscription/pause',
  resume = '/content/user/subscription/resume',
  list = '/content/user/subscription/list',
  feed = '/content/user/subscription/feed',
  plaza = '/content/user/subscription/plaza',
  sourceDetail = '/content/user/subscription/source/detail',
  sourceSubscribe = '/content/user/subscription/source/subscribe',
  sourceSave = '/content/user/subscription/source/save',
  notificationPreference = '/content/user/subscription/notification/preference',
  notificationDecision = '/content/user/subscription/notification/decision',
  batchPause = '/content/user/subscription/batch/pause',
  batchResume = '/content/user/subscription/batch/resume',
  batchCancel = '/content/user/subscription/batch/cancel',
}

/** 订阅内容源 */
export const subscribeSource = (userId: string, data: { sourceId: string; sourceType: string }) =>
  defHttp.post({ url: Api.subscribe, params: { userId }, data });

/** 取消订阅 */
export const cancelSubscription = (userId: string, subscriptionId: string) =>
  defHttp.post({ url: Api.cancel, params: { userId, subscriptionId } });

/** 暂停订阅 */
export const pauseSubscription = (userId: string, subscriptionId: string) =>
  defHttp.post({ url: Api.pause, params: { userId, subscriptionId } });

/** 恢复订阅 */
export const resumeSubscription = (userId: string, subscriptionId: string) =>
  defHttp.post({ url: Api.resume, params: { userId, subscriptionId } });

/** 获取订阅列表 */
export const getSubscribeList = (userId: string, params?: { sourceType?: string; pageNo?: number; pageSize?: number }) =>
  defHttp.get({ url: Api.list, params: { userId, ...params } });

/** 获取订阅流 */
export const getSubscribeFeed = (userId: string, params?: { sourceType?: string; pageNo?: number; pageSize?: number }) =>
  defHttp.get({ url: Api.feed, params: { userId, ...params } });

/** 获取订阅广场列表 */
export const getSubscribePlaza = (userId: string, params?: { keyword?: string; category?: string; sourceType?: string; pageNo?: number; pageSize?: number }) =>
  defHttp.get({ url: Api.plaza, params: { userId, ...params } });

/** 获取订阅源详情 */
export const getSourceDetail = (userId: string, sourceType: string, sourceId: string) =>
  defHttp.get({ url: Api.sourceDetail, params: { userId, sourceType, sourceId } });

/** 从订阅广场订阅内容源 */
export const subscribeFromPlaza = (userId: string, sourceType: string, sourceId: string) =>
  defHttp.post({ url: Api.sourceSubscribe, params: { userId, sourceType, sourceId } });

/** 写入订阅源目录 */
export const saveSource = (data: { sourceId: string; sourceType: string; name: string; category?: string }) =>
  defHttp.post({ url: Api.sourceSave, data });

/** 获取订阅级通知偏好 */
export const getNotificationPreference = (userId: string, subscriptionId: string) =>
  defHttp.get({ url: Api.notificationPreference, params: { userId, subscriptionId } });

/** 保存订阅级通知偏好 */
export const saveNotificationPreference = (userId: string, data: {
  subscriptionId: string;
  channelInsite: boolean;
  channelPush: boolean;
  channelEmail: boolean;
  frequency: 'realtime' | 'daily';
  quietStart?: string;
  quietEnd?: string;
}) => defHttp.post({ url: Api.notificationPreference, params: { userId }, data });

/** 计算订阅源更新通知决策 */
export const getNotificationDecision = (userId: string, subscriptionId: string, updateBizId: string) =>
  defHttp.get({ url: Api.notificationDecision, params: { userId, subscriptionId, updateBizId } });

/** 批量暂停订阅 */
export const batchPauseSubscribe = (userId: string, data: { subscriptionIds: string[] }) =>
  defHttp.post({ url: Api.batchPause, params: { userId }, data });

/** 批量恢复订阅 */
export const batchResumeSubscribe = (userId: string, data: { subscriptionIds: string[] }) =>
  defHttp.post({ url: Api.batchResume, params: { userId }, data });

/** 批量取消订阅 */
export const batchCancelSubscribe = (userId: string, data: { subscriptionIds: string[] }) =>
  defHttp.post({ url: Api.batchCancel, params: { userId }, data });
```

- [ ] **Step 4: 创建信息流API文件**

```typescript
// jeecgboot-vue3/src/api/content/feed.ts
import { defHttp } from '/@/utils/http/axios';

enum Api {
  followingFeed = '/content/user/relation/feed',
  subscribeFeed = '/content/user/subscription/feed',
}

/** 获取关注流 */
export const getFollowingFeed = (userId: string, params?: { pageNo?: number; pageSize?: number }) =>
  defHttp.get({ url: Api.followingFeed, params: { userId, ...params } });

/** 获取订阅流 */
export const getSubscribeFeed = (userId: string, params?: { sourceType?: string; pageNo?: number; pageSize?: number }) =>
  defHttp.get({ url: Api.subscribeFeed, params: { userId, ...params } });
```

- [ ] **Step 5: 运行TypeScript检查**

```bash
cd jeecgboot-vue3 && npx tsc --noEmit
```

Expected: 无类型错误

- [ ] **Step 6: 提交API文件**

```bash
git add src/api/content/follow.ts src/api/content/subscribe.ts src/api/content/feed.ts
git commit -m "feat: add social subscription API layer"
```

---

## Task 2: 状态管理实现

**Files:**
- Create: `jeecgboot-vue3/src/store/modules/follow.ts`
- Create: `jeecgboot-vue3/src/store/modules/subscribe.ts`
- Create: `jeecgboot-vue3/src/store/modules/feed.ts`

- [ ] **Step 1: 创建关注状态管理store**

```typescript
// jeecgboot-vue3/src/store/modules/follow.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import {
  getFollowList,
  getSpecialFollowList,
  getFollowGroupList,
  createFollowGroup,
  updateFollowGroup,
  deleteFollowGroup,
  followUser,
  unfollowUser,
  setSpecialFollow,
  cancelSpecialFollow,
} from '/@/api/content/follow';

interface FollowUser {
  id: string;
  userId: string;
  nickname: string;
  avatar: string;
  bio: string;
  followTime: string;
  groupId: string;
  isSpecial: boolean;
  lastActiveTime: string;
}

interface FollowGroup {
  id: string;
  name: string;
  sortOrder: number;
  memberCount: number;
  isDefault: boolean;
}

export const useFollowStore = defineStore('follow', () => {
  // 状态
  const followList = ref<FollowUser[]>([]);
  const specialFollowList = ref<FollowUser[]>([]);
  const followGroups = ref<FollowGroup[]>([]);
  const totalFollows = ref(0);
  const totalSpecialFollows = ref(0);
  const loading = ref(false);
  const currentPage = ref(1);
  const pageSize = ref(20);
  const hasMore = ref(true);
  const searchKeyword = ref('');
  const selectedGroupId = ref('');

  // 计算属性
  const defaultGroup = computed(() => followGroups.value.find(g => g.isDefault));
  const customGroups = computed(() => followGroups.value.filter(g => !g.isDefault));

  // 获取关注列表
  async function fetchFollowList(userId: string, reset = false) {
    if (reset) {
      currentPage.value = 1;
      followList.value = [];
      hasMore.value = true;
    }

    if (!hasMore.value || loading.value) return;

    loading.value = true;
    try {
      const res = await getFollowList(userId, {
        keyword: searchKeyword.value,
        groupId: selectedGroupId.value,
        pageNo: currentPage.value,
        pageSize: pageSize.value,
      });

      if (reset) {
        followList.value = res.records;
      } else {
        followList.value = [...followList.value, ...res.records];
      }

      totalFollows.value = res.total;
      hasMore.value = followList.value.length < res.total;
      currentPage.value++;
    } finally {
      loading.value = false;
    }
  }

  // 获取特别关注列表
  async function fetchSpecialFollowList(userId: string, reset = false) {
    if (reset) {
      currentPage.value = 1;
      specialFollowList.value = [];
      hasMore.value = true;
    }

    if (!hasMore.value || loading.value) return;

    loading.value = true;
    try {
      const res = await getSpecialFollowList(userId, {
        pageNo: currentPage.value,
        pageSize: pageSize.value,
      });

      if (reset) {
        specialFollowList.value = res.records;
      } else {
        specialFollowList.value = [...specialFollowList.value, ...res.records];
      }

      totalSpecialFollows.value = res.total;
      hasMore.value = specialFollowList.value.length < res.total;
      currentPage.value++;
    } finally {
      loading.value = false;
    }
  }

  // 获取关注分组
  async function fetchFollowGroups(userId: string) {
    const res = await getFollowGroupList(userId);
    followGroups.value = res;
  }

  // 创建分组
  async function createGroup(userId: string, name: string, sortOrder = 0) {
    await createFollowGroup(userId, { name, sortOrder });
    await fetchFollowGroups(userId);
  }

  // 更新分组
  async function updateGroup(userId: string, groupId: string, name: string, sortOrder = 0) {
    await updateFollowGroup(userId, groupId, { name, sortOrder });
    await fetchFollowGroups(userId);
  }

  // 删除分组
  async function removeGroup(userId: string, groupId: string) {
    await deleteFollowGroup(userId, groupId);
    await fetchFollowGroups(userId);
  }

  // 关注用户
  async function follow(userId: string, targetUserId: string, groupId?: string) {
    await followUser(userId, { targetUserId, groupId });
    await fetchFollowList(userId, true);
  }

  // 取消关注
  async function unfollow(userId: string, targetUserId: string) {
    await unfollowUser(userId, targetUserId);
    await fetchFollowList(userId, true);
  }

  // 设置特别关注
  async function setSpecial(userId: string, targetUserId: string) {
    await setSpecialFollow(userId, targetUserId);
    await fetchFollowList(userId, true);
    await fetchSpecialFollowList(userId, true);
  }

  // 取消特别关注
  async function cancelSpecial(userId: string, targetUserId: string) {
    await cancelSpecialFollow(userId, targetUserId);
    await fetchFollowList(userId, true);
    await fetchSpecialFollowList(userId, true);
  }

  // 设置搜索关键词
  function setSearchKeyword(keyword: string) {
    searchKeyword.value = keyword;
  }

  // 设置选中的分组
  function setSelectedGroupId(groupId: string) {
    selectedGroupId.value = groupId;
  }

  return {
    // 状态
    followList,
    specialFollowList,
    followGroups,
    totalFollows,
    totalSpecialFollows,
    loading,
    hasMore,
    searchKeyword,
    selectedGroupId,

    // 计算属性
    defaultGroup,
    customGroups,

    // 方法
    fetchFollowList,
    fetchSpecialFollowList,
    fetchFollowGroups,
    createGroup,
    updateGroup,
    removeGroup,
    follow,
    unfollow,
    setSpecial,
    cancelSpecial,
    setSearchKeyword,
    setSelectedGroupId,
  };
});
```

- [ ] **Step 2: 创建订阅状态管理store**

```typescript
// jeecgboot-vue3/src/store/modules/subscribe.ts
import { defineStore } from 'pinia';
import { ref } from 'vue';
import {
  getSubscribeList,
  subscribeSource,
  unsubscribeSource,
  pauseSubscribe,
  resumeSubscribe,
  getNotificationConfig,
  saveNotificationConfig,
  getGlobalNotificationDefault,
} from '/@/api/content/subscribe';

interface SubscribeItem {
  id: string;
  sourceId: string;
  sourceType: string;
  sourceName: string;
  sourceIcon: string;
  category: string;
  subscriberCount: number;
  lastUpdateTime: string;
  subscribeTime: string;
  status: 'active' | 'paused';
}

interface NotificationConfig {
  channelInsite: boolean;
  channelPush: boolean;
  channelEmail: boolean;
  frequency: 'realtime' | 'daily';
  quietStart?: string;
  quietEnd?: string;
}

export const useSubscribeStore = defineStore('subscribe', () => {
  // 状态
  const subscribeList = ref<SubscribeItem[]>([]);
  const totalSubscribes = ref(0);
  const loading = ref(false);
  const currentPage = ref(1);
  const pageSize = ref(20);
  const hasMore = ref(true);
  const searchKeyword = ref('');
  const selectedSourceType = ref('');

  // 通知配置
  const currentNotificationConfig = ref<NotificationConfig | null>(null);
  const globalNotificationDefault = ref<NotificationConfig | null>(null);

  // 获取订阅列表
  async function fetchSubscribeList(userId: string, reset = false) {
    if (reset) {
      currentPage.value = 1;
      subscribeList.value = [];
      hasMore.value = true;
    }

    if (!hasMore.value || loading.value) return;

    loading.value = true;
    try {
      const res = await getSubscribeList(userId, {
        keyword: searchKeyword.value,
        sourceType: selectedSourceType.value,
        pageNo: currentPage.value,
        pageSize: pageSize.value,
      });

      if (reset) {
        subscribeList.value = res.records;
      } else {
        subscribeList.value = [...subscribeList.value, ...res.records];
      }

      totalSubscribes.value = res.total;
      hasMore.value = subscribeList.value.length < res.total;
      currentPage.value++;
    } finally {
      loading.value = false;
    }
  }

  // 订阅内容源
  async function subscribe(userId: string, sourceId: string, sourceType: string) {
    await subscribeSource(userId, { sourceId, sourceType });
    await fetchSubscribeList(userId, true);
  }

  // 取消订阅
  async function unsubscribe(userId: string, sourceId: string) {
    await unsubscribeSource(userId, sourceId);
    await fetchSubscribeList(userId, true);
  }

  // 暂停订阅
  async function pause(userId: string, sourceId: string) {
    await pauseSubscribe(userId, sourceId);
    await fetchSubscribeList(userId, true);
  }

  // 恢复订阅
  async function resume(userId: string, sourceId: string) {
    await resumeSubscribe(userId, sourceId);
    await fetchSubscribeList(userId, true);
  }

  // 获取通知配置
  async function fetchNotificationConfig(userId: string, sourceId: string) {
    const res = await getNotificationConfig(userId, sourceId);
    currentNotificationConfig.value = res;
  }

  // 保存通知配置
  async function saveConfig(userId: string, sourceId: string, config: NotificationConfig) {
    await saveNotificationConfig(userId, sourceId, config);
    currentNotificationConfig.value = config;
  }

  // 获取全局默认通知配置
  async function fetchGlobalNotificationDefault() {
    if (globalNotificationDefault.value) return globalNotificationDefault.value;
    const res = await getGlobalNotificationDefault();
    globalNotificationDefault.value = res;
    return res;
  }

  // 设置搜索关键词
  function setSearchKeyword(keyword: string) {
    searchKeyword.value = keyword;
  }

  // 设置选中的内容源类型
  function setSelectedSourceType(sourceType: string) {
    selectedSourceType.value = sourceType;
  }

  return {
    // 状态
    subscribeList,
    totalSubscribes,
    loading,
    hasMore,
    searchKeyword,
    selectedSourceType,
    currentNotificationConfig,
    globalNotificationDefault,

    // 方法
    fetchSubscribeList,
    subscribe,
    unsubscribe,
    pause,
    resume,
    fetchNotificationConfig,
    saveConfig,
    fetchGlobalNotificationDefault,
    setSearchKeyword,
    setSelectedSourceType,
  };
});
```

- [ ] **Step 3: 创建信息流状态管理store**

```typescript
// jeecgboot-vue3/src/store/modules/feed.ts
import { defineStore } from 'pinia';
import { ref } from 'vue';
import { getFollowingFeed, getSubscribeFeed } from '/@/api/content/feed';

interface FeedItem {
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

interface FeedResponse {
  priorityItems: FeedItem[];
  items: FeedItem[];
  hasMore: boolean;
  nextCursor?: string;
}

export const useFeedStore = defineStore('feed', () => {
  // 状态
  const followingFeed = ref<FeedItem[]>([]);
  const subscribeFeed = ref<FeedItem[]>([]);
  const followingLoading = ref(false);
  const subscribeLoading = ref(false);
  const followingHasMore = ref(true);
  const subscribeHasMore = ref(true);
  const followingPage = ref(1);
  const subscribePage = ref(1);
  const pageSize = ref(20);

  // 关注流筛选
  const feedTypes = ref<string[]>(['post', 'like', 'favorite']);

  // 订阅流筛选
  const subscribeSourceType = ref('');

  // 缓存时间戳
  const followingLastFetch = ref(0);
  const subscribeLastFetch = ref(0);

  // 获取关注流
  async function fetchFollowingFeed(reset = false) {
    if (reset) {
      followingPage.value = 1;
      followingFeed.value = [];
      followingHasMore.value = true;
    }

    if (!followingHasMore.value || followingLoading.value) return;

    followingLoading.value = true;
    try {
      const res: FeedResponse = await getFollowingFeed({
        page: followingPage.value,
        size: pageSize.value,
        types: feedTypes.value.join(','),
      });

      const allItems = [...res.priorityItems, ...res.items];

      if (reset) {
        followingFeed.value = allItems;
      } else {
        followingFeed.value = [...followingFeed.value, ...allItems];
      }

      followingHasMore.value = res.hasMore;
      followingPage.value++;
      followingLastFetch.value = Date.now();
    } finally {
      followingLoading.value = false;
    }
  }

  // 获取订阅流
  async function fetchSubscribeFeed(reset = false) {
    if (reset) {
      subscribePage.value = 1;
      subscribeFeed.value = [];
      subscribeHasMore.value = true;
    }

    if (!subscribeHasMore.value || subscribeLoading.value) return;

    subscribeLoading.value = true;
    try {
      const res = await getSubscribeFeed({
        page: subscribePage.value,
        size: pageSize.value,
        sourceType: subscribeSourceType.value,
      });

      if (reset) {
        subscribeFeed.value = res.records;
      } else {
        subscribeFeed.value = [...subscribeFeed.value, ...res.records];
      }

      subscribeHasMore.value = res.hasMore;
      subscribePage.value++;
      subscribeLastFetch.value = Date.now();
    } finally {
      subscribeLoading.value = false;
    }
  }

  // 设置关注流类型筛选
  function setFeedTypes(types: string[]) {
    feedTypes.value = types;
  }

  // 设置订阅流内容源类型
  function setSubscribeSourceType(sourceType: string) {
    subscribeSourceType.value = sourceType;
  }

  // 移除用户的所有动态（取消关注时）
  function removeUserFeed(userId: string) {
    followingFeed.value = followingFeed.value.filter(item => item.userId !== userId);
  }

  // 检查缓存是否有效（5分钟）
  function isCacheValid(type: 'following' | 'subscribe'): boolean {
    const now = Date.now();
    const lastFetch = type === 'following' ? followingLastFetch.value : subscribeLastFetch.value;
    return now - lastFetch < 5 * 60 * 1000;
  }

  return {
    // 状态
    followingFeed,
    subscribeFeed,
    followingLoading,
    subscribeLoading,
    followingHasMore,
    subscribeHasMore,
    feedTypes,
    subscribeSourceType,

    // 方法
    fetchFollowingFeed,
    fetchSubscribeFeed,
    setFeedTypes,
    setSubscribeSourceType,
    removeUserFeed,
    isCacheValid,
  };
});
```

- [ ] **Step 4: 运行TypeScript检查**

```bash
cd jeecgboot-vue3 && npx tsc --noEmit
```

Expected: 无类型错误

- [ ] **Step 5: 提交状态管理文件**

```bash
git add src/store/modules/follow.ts src/store/modules/subscribe.ts src/store/modules/feed.ts
git commit -m "feat: add social subscription Pinia stores"
```

---

## Task 3: 基础组件开发

**Files:**
- Create: `jeecgboot-vue3/src/components/social/FollowButton.vue`
- Create: `jeecgboot-vue3/src/components/social/SpecialFollowButton.vue`
- Create: `jeecgboot-vue3/src/components/social/SubscribeButton.vue`
- Create: `jeecgboot-vue3/src/components/social/UserCard.vue`
- Create: `jeecgboot-vue3/src/components/social/SubscriptionCard.vue`
- Create: `jeecgboot-vue3/src/components/social/BatchOperationBar.vue`
- Create: `jeecgboot-vue3/src/components/social/FeedCard.vue`
- Create: `jeecgboot-vue3/src/components/social/FeedFilter.vue`
- Create: `jeecgboot-vue3/src/components/social/SpecialFeed.vue`

- [ ] **Step 1: 创建FollowButton组件**

```vue
<!-- jeecgboot-vue3/src/components/social/FollowButton.vue -->
<template>
  <a-button
    :type="isFollowing ? 'default' : 'primary'"
    :class="['follow-button', { 'is-following': isFollowing }]"
    :loading="loading"
    :disabled="disabled"
    @click="handleClick"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
  >
    <template #icon>
      <PlusOutlined v-if="!isFollowing" />
      <CheckOutlined v-else />
    </template>
    {{ buttonText }}
  </a-button>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { PlusOutlined, CheckOutlined } from '@ant-design/icons-vue';
import { message, Popconfirm } from 'ant-design-vue';
import { useFollowStore } from '/@/store/modules/follow';
import { useUserStore } from '/@/store/modules/user';

const props = defineProps<{
  userId: string;
  targetUserId: string;
  isFollowing: boolean;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:isFollowing', value: boolean): void;
  (e: 'follow'): void;
  (e: 'unfollow'): void;
}>();

const followStore = useFollowStore();
const userStore = useUserStore();
const loading = ref(false);
const isHovering = ref(false);

const isSelf = computed(() => props.userId === props.targetUserId);

const buttonText = computed(() => {
  if (isSelf.value) return '自己';
  if (props.isFollowing && isHovering.value) return '取消关注';
  if (props.isFollowing) return '已关注';
  return '关注';
});

const disabled = computed(() => {
  return props.disabled || isSelf.value || loading.value;
});

let debounceTimer: NodeJS.Timeout | null = null;

async function handleClick() {
  if (isSelf.value) return;

  if (props.isFollowing) {
    // 取消关注需要二次确认
    Popconfirm.confirm({
      title: '确定取消关注该用户？',
      onOk: () => doUnfollow(),
    });
  } else {
    await doFollow();
  }
}

async function doFollow() {
  if (debounceTimer) return;

  debounceTimer = setTimeout(() => {
    debounceTimer = null;
  }, 500);

  // 乐观更新
  emit('update:isFollowing', true);
  emit('follow');

  loading.value = true;
  try {
    await followStore.follow(props.userId, props.targetUserId);
    message.success('关注成功');
  } catch (error) {
    // 回滚
    emit('update:isFollowing', false);
    emit('unfollow');
    message.error('操作失败，请重试');
  } finally {
    loading.value = false;
  }
}

async function doUnfollow() {
  if (debounceTimer) return;

  debounceTimer = setTimeout(() => {
    debounceTimer = null;
  }, 500);

  // 乐观更新
  emit('update:isFollowing', false);
  emit('unfollow');

  loading.value = true;
  try {
    await followStore.unfollow(props.userId, props.targetUserId);
    message.success('已取消关注');
  } catch (error) {
    // 回滚
    emit('update:isFollowing', true);
    emit('follow');
    message.error('操作失败，请重试');
  } finally {
    loading.value = false;
  }
}

function handleMouseEnter() {
  if (props.isFollowing) {
    isHovering.value = true;
  }
}

function handleMouseLeave() {
  isHovering.value = false;
}
</script>

<style scoped lang="less">
.follow-button {
  min-width: 80px;

  &.is-following {
    &:hover {
      color: #ff4d4f;
      border-color: #ff4d4f;
    }
  }
}
</style>
```

- [ ] **Step 2: 创建SpecialFollowButton组件**

```vue
<!-- jeecgboot-vue3/src/components/social/SpecialFollowButton.vue -->
<template>
  <a-tooltip :title="tooltipTitle" :disabled="!isDisabled">
    <a-button
      type="text"
      :class="['special-follow-button', { 'is-special': isSpecial }]"
      :disabled="isDisabled"
      @click="handleClick"
    >
      <template #icon>
        <StarFilled v-if="isSpecial" style="color: #faad14" />
        <StarOutlined v-else />
      </template>
    </a-button>
  </a-tooltip>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { StarFilled, StarOutlined } from '@ant-design/icons-vue';
import { message, Popconfirm } from 'ant-design-vue';
import { useFollowStore } from '/@/store/modules/follow';

const props = defineProps<{
  userId: string;
  targetUserId: string;
  isFollowing: boolean;
  isSpecial: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:isSpecial', value: boolean): void;
  (e: 'special'): void;
  (e: 'cancelSpecial'): void;
}>();

const followStore = useFollowStore();

const isDisabled = computed(() => !props.isFollowing);

const tooltipTitle = computed(() => {
  if (!props.isFollowing) return '请先关注该用户';
  return '';
});

async function handleClick() {
  if (!props.isFollowing) return;

  if (props.isSpecial) {
    // 取消特别关注
    Popconfirm.confirm({
      title: '确定取消特别关注？',
      content: '取消后该用户动态将不再置顶显示',
      onOk: () => doCancelSpecial(),
    });
  } else {
    // 设置特别关注
    Popconfirm.confirm({
      title: '设为特别关注',
      content: '设为特别关注后，该用户动态将置顶显示并开启强提醒',
      onOk: () => doSetSpecial(),
    });
  }
}

async function doSetSpecial() {
  emit('update:isSpecial', true);
  emit('special');

  try {
    await followStore.setSpecial(props.userId, props.targetUserId);
    message.success('已设为特别关注');
  } catch (error) {
    emit('update:isSpecial', false);
    emit('cancelSpecial');
    message.error('操作失败，请重试');
  }
}

async function doCancelSpecial() {
  emit('update:isSpecial', false);
  emit('cancelSpecial');

  try {
    await followStore.cancelSpecial(props.userId, props.targetUserId);
    message.success('已取消特别关注');
  } catch (error) {
    emit('update:isSpecial', true);
    emit('special');
    message.error('操作失败，请重试');
  }
}
</script>

<style scoped lang="less">
.special-follow-button {
  padding: 4px 8px;

  &.is-special {
    color: #faad14;
  }
}
</style>
```

- [ ] **Step 3: 创建SubscribeButton组件**

```vue
<!-- jeecgboot-vue3/src/components/social/SubscribeButton.vue -->
<template>
  <a-button
    :type="isSubscribed ? 'default' : 'primary'"
    :class="['subscribe-button', { 'is-subscribed': isSubscribed }]"
    :loading="loading"
    @click="handleClick"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
  >
    {{ buttonText }}
  </a-button>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { message, Popconfirm } from 'ant-design-vue';
import { useSubscribeStore } from '/@/store/modules/subscribe';

const props = defineProps<{
  userId: string;
  sourceId: string;
  sourceType: string;
  isSubscribed: boolean;
  isPaused?: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:isSubscribed', value: boolean): void;
  (e: 'subscribe'): void;
  (e: 'unsubscribe'): void;
}>();

const subscribeStore = useSubscribeStore();
const loading = ref(false);
const isHovering = ref(false);

const buttonText = computed(() => {
  if (props.isSubscribed && isHovering.value) return '取消订阅';
  if (props.isSubscribed) return '已订阅';
  return '订阅';
});

async function handleClick() {
  if (props.isSubscribed) {
    Popconfirm.confirm({
      title: '取消订阅后将不再接收该来源的更新',
      onOk: () => doUnsubscribe(),
    });
  } else {
    await doSubscribe();
  }
}

async function doSubscribe() {
  emit('update:isSubscribed', true);
  emit('subscribe');

  loading.value = true;
  try {
    await subscribeStore.subscribe(props.userId, props.sourceId, props.sourceType);
    message.success('订阅成功');
  } catch (error) {
    emit('update:isSubscribed', false);
    emit('unsubscribe');
    message.error('操作失败，请重试');
  } finally {
    loading.value = false;
  }
}

async function doUnsubscribe() {
  emit('update:isSubscribed', false);
  emit('unsubscribe');

  loading.value = true;
  try {
    await subscribeStore.unsubscribe(props.userId, props.sourceId);
    message.success('已取消订阅');
  } catch (error) {
    emit('update:isSubscribed', true);
    emit('subscribe');
    message.error('操作失败，请重试');
  } finally {
    loading.value = false;
  }
}

function handleMouseEnter() {
  if (props.isSubscribed) {
    isHovering.value = true;
  }
}

function handleMouseLeave() {
  isHovering.value = false;
}
</script>

<style scoped lang="less">
.subscribe-button {
  min-width: 80px;

  &.is-subscribed {
    &:hover {
      color: #ff4d4f;
      border-color: #ff4d4f;
    }
  }
}
</style>
```

- [ ] **Step 4: 创建UserCard组件**

```vue
<!-- jeecgboot-vue3/src/components/social/UserCard.vue -->
<template>
  <div :class="['user-card', { 'is-mobile': isMobile }]">
    <div class="user-card__avatar" @click="goToProfile">
      <a-avatar :src="user.avatar" :size="isMobile ? 40 : 48">
        {{ user.nickname?.charAt(0) }}
      </a-avatar>
      <div v-if="user.isSpecial" class="special-badge">
        <StarFilled style="color: #faad14" />
      </div>
    </div>

    <div class="user-card__content">
      <div class="user-card__header">
        <span class="nickname" @click="goToProfile">{{ user.nickname }}</span>
        <span v-if="user.groupName" class="group-tag">{{ user.groupName }}</span>
      </div>

      <div v-if="!isMobile && user.bio" class="user-card__bio">
        {{ user.bio }}
      </div>

      <div v-if="!isMobile" class="user-card__meta">
        <span class="follow-time">关注于 {{ user.followTime }}</span>
      </div>
    </div>

    <div class="user-card__actions">
      <slot name="actions">
        <FollowButton
          :user-id="userId"
          :target-user-id="user.userId"
          :is-following="true"
          @update:is-following="handleUnfollow"
        />
        <SpecialFollowButton
          :user-id="userId"
          :target-user-id="user.userId"
          :is-following="true"
          :is-special="user.isSpecial"
          @update:is-special="handleSpecialChange"
        />
        <a-dropdown v-if="isMobile" :trigger="['click']">
          <a-button type="text" size="small">
            <MoreOutlined />
          </a-button>
          <template #overlay>
            <a-menu @click="handleMenuClick">
              <a-menu-item key="group">调整分组</a-menu-item>
              <a-menu-item key="profile">查看主页</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { StarFilled, MoreOutlined } from '@ant-design/icons-vue';
import { useRouter } from 'vue-router';
import FollowButton from './FollowButton.vue';
import SpecialFollowButton from './SpecialFollowButton.vue';

interface User {
  userId: string;
  nickname: string;
  avatar: string;
  bio: string;
  followTime: string;
  groupName: string;
  isSpecial: boolean;
  lastActiveTime: string;
}

const props = defineProps<{
  userId: string;
  user: User;
  isMobile?: boolean;
}>();

const emit = defineEmits<{
  (e: 'unfollow', userId: string): void;
  (e: 'specialChange', userId: string, isSpecial: boolean): void;
  (e: 'groupChange', userId: string): void;
}>();

const router = useRouter();

const isMobile = computed(() => props.isMobile);

function goToProfile() {
  router.push(`/user/profile/${props.user.userId}`);
}

function handleUnfollow(isFollowing: boolean) {
  if (!isFollowing) {
    emit('unfollow', props.user.userId);
  }
}

function handleSpecialChange(isSpecial: boolean) {
  emit('specialChange', props.user.userId, isSpecial);
}

function handleMenuClick({ key }: { key: string }) {
  if (key === 'group') {
    emit('groupChange', props.user.userId);
  } else if (key === 'profile') {
    goToProfile();
  }
}
</script>

<style scoped lang="less">
.user-card {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  transition: box-shadow 0.3s;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  }

  &.is-mobile {
    padding: 12px;

    .user-card__content {
      flex: 1;
      min-width: 0;
    }
  }

  &__avatar {
    position: relative;
    cursor: pointer;
    margin-right: 12px;

    .special-badge {
      position: absolute;
      bottom: -2px;
      right: -2px;
      font-size: 12px;
    }
  }

  &__content {
    flex: 1;
    min-width: 0;
  }

  &__header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 4px;

    .nickname {
      font-weight: 500;
      cursor: pointer;

      &:hover {
        color: var(--j-global-primary-color);
      }
    }

    .group-tag {
      padding: 2px 8px;
      font-size: 12px;
      color: #666;
      background: #f5f5f5;
      border-radius: 4px;
    }
  }

  &__bio {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    margin-bottom: 4px;
    font-size: 14px;
    color: #666;
    line-height: 1.5;
  }

  &__meta {
    font-size: 12px;
    color: #999;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-left: 12px;
  }
}
</style>
```

- [ ] **Step 5: 创建FeedCard组件**

```vue
<!-- jeecgboot-vue3/src/components/social/FeedCard.vue -->
<template>
  <div :class="['feed-card', { 'is-priority': feed.isPriority, 'is-mobile': isMobile }]">
    <div v-if="feed.isPriority" class="priority-indicator"></div>

    <div class="feed-card__header">
      <a-avatar :src="feed.avatar" :size="36" @click="goToProfile">
        {{ feed.nickname?.charAt(0) }}
      </a-avatar>
      <div class="user-info">
        <span class="nickname" @click="goToProfile">{{ feed.nickname }}</span>
        <span class="dynamic-type">{{ dynamicTypeLabel }}</span>
      </div>
      <span class="time">{{ feed.createTime }}</span>
    </div>

    <div class="feed-card__content" @click="goToContent">
      <div v-if="feed.sourceName" class="source-tag">
        来自 {{ feed.sourceName }}
      </div>
      <h4 class="title">{{ feed.contentTitle }}</h4>
      <p v-if="!isMobile && feed.contentSummary" class="summary">
        {{ feed.contentSummary }}
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';

interface Feed {
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

const props = defineProps<{
  feed: Feed;
  isMobile?: boolean;
}>();

const router = useRouter();

const dynamicTypeLabel = computed(() => {
  const labels: Record<string, string> = {
    post: '发布了',
    like: '点赞了',
    favorite: '收藏了',
  };
  return labels[props.feed.dynamicType] || '';
});

function goToProfile() {
  router.push(`/user/profile/${props.feed.userId}`);
}

function goToContent() {
  router.push(`/content/detail/${props.feed.contentId}`);
}
</script>

<style scoped lang="less">
.feed-card {
  position: relative;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 12px;
  transition: box-shadow 0.3s;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  }

  &.is-priority {
    border-left: 3px solid #faad14;
  }

  &.is-mobile {
    padding: 12px;

    .feed-card__content {
      .summary {
        display: none;
      }
    }
  }

  .priority-indicator {
    position: absolute;
    top: 0;
    left: 0;
    width: 3px;
    height: 100%;
    background: #faad14;
  }

  &__header {
    display: flex;
    align-items: center;
    margin-bottom: 12px;

    .ant-avatar {
      cursor: pointer;
    }

    .user-info {
      flex: 1;
      margin-left: 12px;

      .nickname {
        font-weight: 500;
        cursor: pointer;

        &:hover {
          color: var(--j-global-primary-color);
        }
      }

      .dynamic-type {
        margin-left: 8px;
        font-size: 14px;
        color: #666;
      }
    }

    .time {
      font-size: 12px;
      color: #999;
    }
  }

  &__content {
    cursor: pointer;

    .source-tag {
      display: inline-block;
      padding: 2px 8px;
      margin-bottom: 8px;
      font-size: 12px;
      color: #666;
      background: #f5f5f5;
      border-radius: 4px;
    }

    .title {
      margin: 0 0 8px;
      font-size: 16px;
      font-weight: 500;
      line-height: 1.5;
    }

    .summary {
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      margin: 0;
      font-size: 14px;
      color: #666;
      line-height: 1.5;
    }
  }
}
</style>
```

- [ ] **Step 6: 创建剩余组件**

继续创建 SubscriptionCard、BatchOperationBar、FeedFilter、SpecialFeed 组件...

- [ ] **Step 7: 运行TypeScript检查**

```bash
cd jeecgboot-vue3 && npx tsc --noEmit
```

Expected: 无类型错误

- [ ] **Step 8: 提交组件文件**

```bash
git add src/components/social/
git commit -m "feat: add social subscription base components"
```

---

## Task 4: 关注模块页面开发

**Files:**
- Create: `jeecgboot-vue3/src/views/social/follow/index.vue`
- Create: `jeecgboot-vue3/src/views/social/follow/special.vue`
- Create: `jeecgboot-vue3/src/views/social/follow/group.vue`
- Create: `jeecgboot-vue3/src/views/social/follow/recommend.vue`

- [ ] **Step 1: 创建关注列表页面**

```vue
<!-- jeecgboot-vue3/src/views/social/follow/index.vue -->
<template>
  <div class="follow-list-page">
    <div class="page-header">
      <div class="stats">
        <span class="count">
          关注总数：<span class="number">{{ totalFollows }}</span>
        </span>
      </div>

      <div class="search-bar">
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="搜索用户名/昵称"
          :loading="searching"
          @search="handleSearch"
          @change="handleSearchChange"
        />
      </div>

      <div class="group-filter">
        <a-radio-group v-model:value="selectedGroupId" @change="handleGroupChange">
          <a-radio-button value="">全部</a-radio-button>
          <a-radio-button
            v-for="group in followGroups"
            :key="group.id"
            :value="group.id"
          >
            {{ group.name }}
          </a-radio-button>
        </a-radio-group>
      </div>

      <div class="actions">
        <a-button @click="goToBatchManage">批量管理</a-button>
        <a-button @click="goToGroupManage">分组管理</a-button>
      </div>
    </div>

    <div class="follow-list">
      <a-spin :spinning="loading">
        <div v-if="followList.length === 0 && !loading" class="empty-state">
          <a-empty description="还没有关注任何人">
            <a-button type="primary" @click="goToRecommend">推荐关注</a-button>
          </a-empty>
        </div>

        <UserCard
          v-for="user in followList"
          :key="user.userId"
          :user-id="currentUserId"
          :user="user"
          :is-mobile="isMobile"
          @unfollow="handleUnfollow"
          @special-change="handleSpecialChange"
          @group-change="handleGroupChange"
        />

        <div v-if="hasMore" class="load-more">
          <a-button :loading="loading" @click="loadMore">加载更多</a-button>
        </div>
      </a-spin>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useFollowStore } from '/@/store/modules/follow';
import { useUserStore } from '/@/store/modules/user';
import UserCard from '/@/components/social/UserCard.vue';

const router = useRouter();
const followStore = useFollowStore();
const userStore = useUserStore();

const currentUserId = computed(() => userStore.getUserInfo?.userId);
const isMobile = ref(false);

const {
  followList,
  followGroups,
  totalFollows,
  loading,
  hasMore,
  searchKeyword,
  selectedGroupId,
} = followStore;

let searchDebounce: NodeJS.Timeout | null = null;

onMounted(async () => {
  if (currentUserId.value) {
    await Promise.all([
      followStore.fetchFollowList(currentUserId.value, true),
      followStore.fetchFollowGroups(currentUserId.value),
    ]);
  }
});

function handleSearch(value: string) {
  followStore.setSearchKeyword(value);
  if (currentUserId.value) {
    followStore.fetchFollowList(currentUserId.value, true);
  }
}

function handleSearchChange(e: Event) {
  const value = (e.target as HTMLInputElement).value;
  if (searchDebounce) {
    clearTimeout(searchDebounce);
  }
  searchDebounce = setTimeout(() => {
    handleSearch(value);
  }, 300);
}

function handleGroupChange(groupId: string) {
  followStore.setSelectedGroupId(groupId);
  if (currentUserId.value) {
    followStore.fetchFollowList(currentUserId.value, true);
  }
}

async function handleUnfollow(userId: string) {
  if (currentUserId.value) {
    await followStore.unfollow(currentUserId.value, userId);
  }
}

async function handleSpecialChange(userId: string, isSpecial: boolean) {
  if (currentUserId.value) {
    if (isSpecial) {
      await followStore.setSpecial(currentUserId.value, userId);
    } else {
      await followStore.cancelSpecial(currentUserId.value, userId);
    }
  }
}

function loadMore() {
  if (currentUserId.value) {
    followStore.fetchFollowList(currentUserId.value);
  }
}

function goToBatchManage() {
  router.push('/social/follow/batch');
}

function goToGroupManage() {
  router.push('/social/follow/group');
}

function goToRecommend() {
  router.push('/social/follow/recommend');
}
</script>

<style scoped lang="less">
.follow-list-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;

  .page-header {
    margin-bottom: 24px;

    .stats {
      margin-bottom: 16px;

      .count {
        font-size: 16px;

        .number {
          font-weight: 600;
          color: var(--j-global-primary-color);
        }
      }
    }

    .search-bar {
      margin-bottom: 16px;
    }

    .group-filter {
      margin-bottom: 16px;
      overflow-x: auto;
      white-space: nowrap;
    }

    .actions {
      display: flex;
      gap: 8px;
    }
  }

  .empty-state {
    padding: 48px 0;
    text-align: center;
  }

  .load-more {
    text-align: center;
    margin-top: 24px;
  }
}
</style>
```

- [ ] **Step 2: 创建特别关注列表页面**

继续创建 special.vue、group.vue、recommend.vue 页面...

- [ ] **Step 3: 配置路由**

在路由配置中添加社交模块路由：

```typescript
// 在路由配置中添加
{
  path: '/social',
  name: 'Social',
  component: () => import('/@/layouts/default/index.vue'),
  children: [
    {
      path: 'follow',
      name: 'FollowList',
      component: () => import('/@/views/social/follow/index.vue'),
      meta: { title: '我的关注' },
    },
    {
      path: 'follow/special',
      name: 'SpecialFollow',
      component: () => import('/@/views/social/follow/special.vue'),
      meta: { title: '特别关注' },
    },
    {
      path: 'follow/group',
      name: 'FollowGroup',
      component: () => import('/@/views/social/follow/group.vue'),
      meta: { title: '分组管理' },
    },
    {
      path: 'follow/recommend',
      name: 'FollowRecommend',
      component: () => import('/@/views/social/follow/recommend.vue'),
      meta: { title: '推荐关注' },
    },
    {
      path: 'feed',
      name: 'FollowingFeed',
      component: () => import('/@/views/social/feed/index.vue'),
      meta: { title: '关注流' },
    },
    {
      path: 'subscribe',
      name: 'SubscribeFeed',
      component: () => import('/@/views/social/subscribe/index.vue'),
      meta: { title: '订阅流' },
    },
    {
      path: 'subscribe/square',
      name: 'SubscribeSquare',
      component: () => import('/@/views/social/subscribe/square.vue'),
      meta: { title: '订阅广场' },
    },
    {
      path: 'subscribe/manage',
      name: 'SubscribeManage',
      component: () => import('/@/views/social/subscribe/manage.vue'),
      meta: { title: '订阅管理' },
    },
  ],
},
```

- [ ] **Step 4: 运行TypeScript检查**

```bash
cd jeecgboot-vue3 && npx tsc --noEmit
```

Expected: 无类型错误

- [ ] **Step 5: 提交页面文件**

```bash
git add src/views/social/follow/ src/router/
git commit -m "feat: add follow module pages and routes"
```

---

## Task 5: 订阅模块页面开发

**Files:**
- Create: `jeecgboot-vue3/src/views/social/subscribe/index.vue`
- Create: `jeecgboot-vue3/src/views/social/subscribe/square.vue`
- Create: `jeecgboot-vue3/src/views/social/subscribe/detail.vue`
- Create: `jeecgboot-vue3/src/views/social/subscribe/manage.vue`
- Create: `jeecgboot-vue3/src/views/social/subscribe/notification.vue`

- [ ] **Step 1: 创建订阅广场页面**

```vue
<!-- jeecgboot-vue3/src/views/social/subscribe/square.vue -->
<template>
  <div class="subscribe-square-page">
    <div class="page-header">
      <a-input-search
        v-model:value="searchKeyword"
        placeholder="搜索专题/话题/栏目"
        :loading="searching"
        @search="handleSearch"
        @change="handleSearchChange"
      />

      <div class="category-filter">
        <a-radio-group v-model:value="selectedCategory" @change="handleCategoryChange">
          <a-radio-button value="">全部</a-radio-button>
          <a-radio-button value="tech">科技</a-radio-button>
          <a-radio-button value="entertainment">娱乐</a-radio-button>
          <a-radio-button value="sports">体育</a-radio-button>
          <a-radio-button value="finance">财经</a-radio-button>
          <a-radio-button value="education">教育</a-radio-button>
        </a-radio-group>
      </div>
    </div>

    <div class="square-grid">
      <a-spin :spinning="loading">
        <div v-if="subscribeList.length === 0 && !loading" class="empty-state">
          <a-empty description="未找到匹配的内容源" />
        </div>

        <a-row :gutter="[16, 16]">
          <a-col
            v-for="item in subscribeList"
            :key="item.sourceId"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <SubscriptionCard
              :item="item"
              :user-id="currentUserId"
              @subscribe="handleSubscribe"
              @unsubscribe="handleUnsubscribe"
            />
          </a-col>
        </a-row>

        <div v-if="hasMore" class="load-more">
          <a-button :loading="loading" @click="loadMore">加载更多</a-button>
        </div>
      </a-spin>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useSubscribeStore } from '/@/store/modules/subscribe';
import { useUserStore } from '/@/store/modules/user';
import SubscriptionCard from '/@/components/social/SubscriptionCard.vue';

const subscribeStore = useSubscribeStore();
const userStore = useUserStore();

const currentUserId = computed(() => userStore.getUserInfo?.userId);

const {
  subscribeList,
  loading,
  hasMore,
  searchKeyword,
  selectedSourceType: selectedCategory,
} = subscribeStore;

let searchDebounce: NodeJS.Timeout | null = null;

onMounted(() => {
  subscribeStore.fetchSubscribeList(currentUserId.value, true);
});

function handleSearch(value: string) {
  subscribeStore.setSearchKeyword(value);
  subscribeStore.fetchSubscribeList(currentUserId.value, true);
}

function handleSearchChange(e: Event) {
  const value = (e.target as HTMLInputElement).value;
  if (searchDebounce) {
    clearTimeout(searchDebounce);
  }
  searchDebounce = setTimeout(() => {
    handleSearch(value);
  }, 300);
}

function handleCategoryChange(e: Event) {
  const value = (e.target as HTMLInputElement).value;
  subscribeStore.setSelectedSourceType(value);
  subscribeStore.fetchSubscribeList(currentUserId.value, true);
}

async function handleSubscribe(sourceId: string, sourceType: string) {
  await subscribeStore.subscribe(currentUserId.value, sourceId, sourceType);
}

async function handleUnsubscribe(sourceId: string) {
  await subscribeStore.unsubscribe(currentUserId.value, sourceId);
}

function loadMore() {
  subscribeStore.fetchSubscribeList(currentUserId.value);
}
</script>

<style scoped lang="less">
.subscribe-square-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;

  .page-header {
    margin-bottom: 24px;

    .category-filter {
      margin-top: 16px;
      overflow-x: auto;
      white-space: nowrap;
    }
  }

  .empty-state {
    padding: 48px 0;
    text-align: center;
  }

  .load-more {
    text-align: center;
    margin-top: 24px;
  }
}
</style>
```

- [ ] **Step 2: 创建其他订阅页面**

继续创建 index.vue、detail.vue、manage.vue、notification.vue 页面...

- [ ] **Step 3: 运行TypeScript检查**

```bash
cd jeecgboot-vue3 && npx tsc --noEmit
```

Expected: 无类型错误

- [ ] **Step 4: 提交页面文件**

```bash
git add src/views/social/subscribe/
git commit -m "feat: add subscription module pages"
```

---

## Task 6: 信息流模块开发

**Files:**
- Create: `jeecgboot-vue3/src/views/social/feed/index.vue`

- [ ] **Step 1: 创建关注流页面**

```vue
<!-- jeecgboot-vue3/src/views/social/feed/index.vue -->
<template>
  <div class="feed-page">
    <div class="feed-header">
      <div class="feed-filter">
        <span class="filter-label">动态类型：</span>
        <a-switch
          v-model:checked="showPost"
          checked-children="发布"
          un-checked-children="发布"
          @change="handleFilterChange"
        />
        <a-switch
          v-model:checked="showLike"
          checked-children="点赞"
          un-checked-children="点赞"
          @change="handleFilterChange"
        />
        <a-switch
          v-model:checked="showFavorite"
          checked-children="收藏"
          un-checked-children="收藏"
          @change="handleFilterChange"
        />
      </div>
    </div>

    <div class="feed-content">
      <a-spin :spinning="loading">
        <div v-if="followingFeed.length === 0 && !loading" class="empty-state">
          <a-empty description="关注感兴趣的创作者，精彩内容将在这里展示">
            <a-button type="primary" @click="goToRecommend">推荐关注</a-button>
          </a-empty>
        </div>

        <!-- 特别关注动态分区 -->
        <div v-if="priorityItems.length > 0" class="priority-section">
          <h3 class="section-title">
            <StarFilled style="color: #faad14" />
            特别关注
          </h3>
          <FeedCard
            v-for="feed in priorityItems"
            :key="feed.id"
            :feed="feed"
            :is-mobile="isMobile"
          />
        </div>

        <!-- 常规动态列表 -->
        <div class="normal-section">
          <FeedCard
            v-for="feed in normalItems"
            :key="feed.id"
            :feed="feed"
            :is-mobile="isMobile"
          />
        </div>

        <div v-if="hasMore" class="load-more">
          <a-button :loading="loading" @click="loadMore">加载更多</a-button>
        </div>
      </a-spin>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { StarFilled } from '@ant-design/icons-vue';
import { useRouter } from 'vue-router';
import { useFeedStore } from '/@/store/modules/feed';
import FeedCard from '/@/components/social/FeedCard.vue';

const router = useRouter();
const feedStore = useFeedStore();

const {
  followingFeed,
  followingLoading: loading,
  followingHasMore: hasMore,
} = feedStore;

const isMobile = ref(false);

// 动态类型筛选
const showPost = ref(true);
const showLike = ref(true);
const showFavorite = ref(true);

// 分区数据
const priorityItems = computed(() =>
  followingFeed.filter(item => item.isPriority)
);

const normalItems = computed(() =>
  followingFeed.filter(item => !item.isPriority)
);

onMounted(() => {
  feedStore.fetchFollowingFeed(true);
});

function handleFilterChange() {
  const types: string[] = [];
  if (showPost.value) types.push('post');
  if (showLike.value) types.push('like');
  if (showFavorite.value) types.push('favorite');

  if (types.length === 0) {
    // 至少保留一种类型
    showPost.value = true;
    types.push('post');
  }

  feedStore.setFeedTypes(types);
  feedStore.fetchFollowingFeed(true);
}

function loadMore() {
  feedStore.fetchFollowingFeed();
}

function goToRecommend() {
  router.push('/social/follow/recommend');
}
</script>

<style scoped lang="less">
.feed-page {
  max-width: 680px;
  margin: 0 auto;
  padding: 24px;

  .feed-header {
    margin-bottom: 24px;

    .feed-filter {
      display: flex;
      align-items: center;
      gap: 12px;

      .filter-label {
        font-size: 14px;
        color: #666;
      }
    }
  }

  .priority-section {
    margin-bottom: 24px;

    .section-title {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 16px;
      font-size: 16px;
      font-weight: 500;
    }
  }

  .empty-state {
    padding: 48px 0;
    text-align: center;
  }

  .load-more {
    text-align: center;
    margin-top: 24px;
  }
}
</style>
```

- [ ] **Step 2: 运行TypeScript检查**

```bash
cd jeecgboot-vue3 && npx tsc --noEmit
```

Expected: 无类型错误

- [ ] **Step 3: 提交页面文件**

```bash
git add src/views/social/feed/
git commit -m "feat: add feed module pages"
```

---

## Task 7: 响应式布局适配

**Files:**
- Create: `jeecgboot-vue3/src/styles/social.scss`
- Modify: 各页面和组件的样式

- [ ] **Step 1: 创建响应式样式文件**

```scss
// jeecgboot-vue3/src/styles/social.scss
@import 'ant-design-vue/lib/style/index.less';

// 断点变量
@screen-xs: 576px;
@screen-sm: 768px;
@screen-md: 992px;
@screen-lg: 1200px;
@screen-xl: 1600px;

// 响应式工具类
.hide-on-mobile {
  @media (max-width: @screen-xs) {
    display: none !important;
  }
}

.hide-on-desktop {
  @media (min-width: @screen-lg) {
    display: none !important;
  }
}

// 响应式容器
.responsive-container {
  width: 100%;
  margin: 0 auto;
  padding: 0 16px;

  @media (min-width: @screen-sm) {
    max-width: 720px;
    padding: 0 24px;
  }

  @media (min-width: @screen-md) {
    max-width: 960px;
  }

  @media (min-width: @screen-lg) {
    max-width: 1200px;
  }
}

// 响应式网格
.responsive-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: 1fr;

  @media (min-width: @screen-xs) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (min-width: @screen-sm) {
    grid-template-columns: repeat(3, 1fr);
  }

  @media (min-width: @screen-lg) {
    grid-template-columns: repeat(4, 1fr);
  }
}

// 响应式卡片
.responsive-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;

  @media (max-width: @screen-xs) {
    padding: 12px;
  }
}

// 响应式表格转卡片
.table-to-card {
  @media (max-width: @screen-xs) {
    .ant-table {
      display: none;
    }

    .card-list {
      display: block;
    }
  }

  @media (min-width: @screen-xs) {
    .ant-table {
      display: table;
    }

    .card-list {
      display: none;
    }
  }
}

// 响应式弹窗
.modal-responsive {
  @media (max-width: @screen-xs) {
    .ant-modal {
      max-width: 100%;
      top: 0;
      padding-bottom: 0;
      margin: 0;
    }

    .ant-modal-content {
      border-radius: 0;
    }

    .ant-modal-body {
      padding: 16px;
    }
  }
}

// 触摸目标大小
.touch-target {
  min-height: 44px;
  min-width: 44px;
}

// 响应式操作按钮
.action-buttons-responsive {
  @media (max-width: @screen-xs) {
    .desktop-actions {
      display: none;
    }

    .mobile-menu {
      display: block;
    }
  }

  @media (min-width: @screen-xs) {
    .desktop-actions {
      display: flex;
      gap: 8px;
    }

    .mobile-menu {
      display: none;
    }
  }
}

// 骨架屏
.skeleton-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;

  .skeleton-header {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
  }

  .skeleton-content {
    .skeleton-line {
      height: 16px;
      background: #f0f0f0;
      border-radius: 4px;
      margin-bottom: 8px;

      &:last-child {
        width: 60%;
      }
    }
  }
}

// 空状态
.empty-state-responsive {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  text-align: center;

  .empty-image {
    width: 120px;
    height: 120px;
    margin-bottom: 24px;
  }

  .empty-text {
    font-size: 16px;
    color: #666;
    margin-bottom: 24px;
  }

  .empty-action {
    margin-top: 16px;
  }
}

// 批量操作栏
.batch-operation-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.09);
  padding: 16px;
  z-index: 100;

  @media (min-width: @screen-lg) {
    left: 256px; // 侧边栏宽度
  }

  .batch-content {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .batch-info {
    font-size: 14px;
    color: #666;
  }

  .batch-actions {
    display: flex;
    gap: 8px;

    @media (max-width: @screen-xs) {
      flex-direction: column;
      width: 100%;

      .ant-btn {
        width: 100%;
      }
    }
  }
}

// 加载更多
.load-more-responsive {
  text-align: center;
  padding: 24px;

  .ant-btn {
    min-width: 120px;
  }
}
```

- [ ] **Step 2: 应用响应式样式到组件**

更新各组件的样式，使用响应式工具类...

- [ ] **Step 3: 运行TypeScript检查**

```bash
cd jeecgboot-vue3 && npx tsc --noEmit
```

Expected: 无类型错误

- [ ] **Step 4: 提交样式文件**

```bash
git add src/styles/social.scss
git commit -m "feat: add responsive layout styles for social module"
```

---

## Task 8: 交互体验优化

**Files:**
- Modify: 各组件和页面

- [ ] **Step 1: 实现加载状态（骨架屏）**

在各列表页面添加骨架屏组件...

- [ ] **Step 2: 实现空状态**

在各页面添加空状态插图和引导文案...

- [ ] **Step 3: 实现错误反馈**

统一错误处理和Toast提示...

- [ ] **Step 4: 实现表单校验**

在分组管理、通知配置等表单页面添加校验逻辑...

- [ ] **Step 5: 实现防重复提交**

在提交按钮上添加loading状态和禁用逻辑...

- [ ] **Step 6: 实现危险操作确认**

在取消关注、取消订阅等操作添加Popconfirm二次确认...

- [ ] **Step 7: 实现成功反馈**

使用useMessage统一成功提示...

- [ ] **Step 8: 实现防抖和节流**

在搜索输入添加300ms防抖，滚动加载添加200ms节流...

- [ ] **Step 9: 实现乐观更新**

在关注/订阅操作先更新UI，失败时回滚...

- [ ] **Step 10: 实现触摸目标**

确保移动端可点击元素最小44px x 44px...

- [ ] **Step 11: 运行TypeScript检查**

```bash
cd jeecgboot-vue3 && npx tsc --noEmit
```

Expected: 无类型错误

- [ ] **Step 12: 提交交互优化**

```bash
git add -A
git commit -m "feat: add interaction optimizations for social module"
```

---

## Task 9: 性能优化

**Files:**
- Modify: 各组件和页面

- [ ] **Step 1: 实现关注流性能优化**

在关注流API中添加大关注量用户降级策略...

- [ ] **Step 2: 实现信息流缓存策略**

在Pinia store中实现缓存和增量更新逻辑...

- [ ] **Step 3: 实现虚拟滚动**

在长列表中使用虚拟滚动优化性能...

- [ ] **Step 4: 实现图片懒加载**

在用户头像和内容图片添加懒加载...

- [ ] **Step 5: 实现代码分割**

使用动态import实现按模块懒加载...

- [ ] **Step 6: 运行TypeScript检查**

```bash
cd jeecgboot-vue3 && npx tsc --noEmit
```

Expected: 无类型错误

- [ ] **Step 7: 运行构建检查**

```bash
cd jeecgboot-vue3 && pnpm build
```

Expected: 构建成功，无错误

- [ ] **Step 8: 提交性能优化**

```bash
git add -A
git commit -m "perf: add performance optimizations for social module"
```

---

## Task 10: 单元测试

**Files:**
- Create: `jeecgboot-vue3/tests/unit/components/social/FollowButton.spec.ts`
- Create: `jeecgboot-vue3/tests/unit/components/social/SpecialFollowButton.spec.ts`
- Create: `jeecgboot-vue3/tests/unit/components/social/SubscribeButton.spec.ts`
- Create: `jeecgboot-vue3/tests/unit/store/follow.spec.ts`
- Create: `jeecgboot-vue3/tests/unit/store/subscribe.spec.ts`
- Create: `jeecgboot-vue3/tests/unit/store/feed.spec.ts`

- [ ] **Step 1: 创建测试目录**

```bash
mkdir -p jeecgboot-vue3/tests/unit/components/social
mkdir -p jeecgboot-vue3/tests/unit/store
```

- [ ] **Step 2: 编写FollowButton组件测试**

```typescript
// jeecgboot-vue3/tests/unit/components/social/FollowButton.spec.ts
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import FollowButton from '/@/components/social/FollowButton.vue';

describe('FollowButton', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('should render follow button when not following', () => {
    const wrapper = mount(FollowButton, {
      props: {
        userId: 'user1',
        targetUserId: 'user2',
        isFollowing: false,
      },
    });

    expect(wrapper.text()).toContain('关注');
    expect(wrapper.find('button').classes()).not.toContain('is-following');
  });

  it('should render following button when following', () => {
    const wrapper = mount(FollowButton, {
      props: {
        userId: 'user1',
        targetUserId: 'user2',
        isFollowing: true,
      },
    });

    expect(wrapper.text()).toContain('已关注');
    expect(wrapper.find('button').classes()).toContain('is-following');
  });

  it('should show "自己" when target is self', () => {
    const wrapper = mount(FollowButton, {
      props: {
        userId: 'user1',
        targetUserId: 'user1',
        isFollowing: false,
      },
    });

    expect(wrapper.text()).toContain('自己');
    expect(wrapper.find('button').attributes('disabled')).toBeDefined();
  });

  it('should emit follow event when clicking follow button', async () => {
    const wrapper = mount(FollowButton, {
      props: {
        userId: 'user1',
        targetUserId: 'user2',
        isFollowing: false,
      },
    });

    await wrapper.find('button').trigger('click');

    expect(wrapper.emitted('follow')).toBeTruthy();
    expect(wrapper.emitted('update:isFollowing')).toBeTruthy();
  });

  it('should show confirm dialog when unfollowing', async () => {
    const wrapper = mount(FollowButton, {
      props: {
        userId: 'user1',
        targetUserId: 'user2',
        isFollowing: true,
      },
    });

    await wrapper.find('button').trigger('click');

    // Popconfirm should be triggered
    expect(wrapper.find('.ant-popconfirm').exists()).toBe(true);
  });

  it('should debounce follow action', async () => {
    const wrapper = mount(FollowButton, {
      props: {
        userId: 'user1',
        targetUserId: 'user2',
        isFollowing: false,
      },
    });

    // Click multiple times rapidly
    await wrapper.find('button').trigger('click');
    await wrapper.find('button').trigger('click');
    await wrapper.find('button').trigger('click');

    // Should only emit once due to debounce
    expect(wrapper.emitted('follow')).toHaveLength(1);
  });
});
```

- [ ] **Step 3: 编写其他组件测试**

继续编写 SpecialFollowButton、SubscribeButton 等组件测试...

- [ ] **Step 4: 编写store测试**

```typescript
// jeecgboot-vue3/tests/unit/store/follow.spec.ts
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useFollowStore } from '/@/store/modules/follow';
import * as followApi from '/@/api/content/follow';

vi.mock('/@/api/content/follow');

describe('FollowStore', () => {
  let store: ReturnType<typeof useFollowStore>;

  beforeEach(() => {
    setActivePinia(createPinia());
    store = useFollowStore();
  });

  it('should fetch follow list', async () => {
    const mockData = {
      records: [
        { userId: 'user1', nickname: 'User 1', avatar: '', bio: '', followTime: '2024-01-01', groupId: '', isSpecial: false, lastActiveTime: '2024-01-01' },
      ],
      total: 1,
    };

    vi.mocked(followApi.getFollowList).mockResolvedValue(mockData);

    await store.fetchFollowList('currentUserId', true);

    expect(store.followList).toHaveLength(1);
    expect(store.totalFollows).toBe(1);
  });

  it('should follow user', async () => {
    vi.mocked(followApi.followUser).mockResolvedValue({});
    vi.mocked(followApi.getFollowList).mockResolvedValue({ records: [], total: 0 });

    await store.follow('currentUserId', 'targetUserId');

    expect(followApi.followUser).toHaveBeenCalledWith('currentUserId', { targetUserId: 'targetUserId', groupId: undefined });
  });

  it('should unfollow user', async () => {
    vi.mocked(followApi.unfollowUser).mockResolvedValue({});
    vi.mocked(followApi.getFollowList).mockResolvedValue({ records: [], total: 0 });

    await store.unfollow('currentUserId', 'targetUserId');

    expect(followApi.unfollowUser).toHaveBeenCalledWith('currentUserId', 'targetUserId');
  });

  it('should set special follow', async () => {
    vi.mocked(followApi.setSpecialFollow).mockResolvedValue({});
    vi.mocked(followApi.getFollowList).mockResolvedValue({ records: [], total: 0 });
    vi.mocked(followApi.getSpecialFollowList).mockResolvedValue({ records: [], total: 0 });

    await store.setSpecial('currentUserId', 'targetUserId');

    expect(followApi.setSpecialFollow).toHaveBeenCalledWith('currentUserId', 'targetUserId');
  });

  it('should handle search keyword', () => {
    store.setSearchKeyword('test');
    expect(store.searchKeyword).toBe('test');
  });

  it('should handle group selection', () => {
    store.setSelectedGroupId('group1');
    expect(store.selectedGroupId).toBe('group1');
  });
});
```

- [ ] **Step 5: 运行测试**

```bash
cd jeecgboot-vue3 && npx vitest run tests/unit
```

Expected: 所有测试通过

- [ ] **Step 6: 提交测试文件**

```bash
git add tests/unit/
git commit -m "test: add unit tests for social subscription components and stores"
```

---

## Task 11: 集成测试

**Files:**
- Create: `jeecgboot-vue3/tests/integration/social/follow.spec.ts`
- Create: `jeecgboot-vue3/tests/integration/social/subscribe.spec.ts`
- Create: `jeecgboot-vue3/tests/integration/social/feed.spec.ts`

- [ ] **Step 1: 创建集成测试目录**

```bash
mkdir -p jeecgboot-vue3/tests/integration/social
```

- [ ] **Step 2: 编写关注列表页面集成测试**

```typescript
// jeecgboot-vue3/tests/integration/social/follow.spec.ts
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createRouter, createWebHistory } from 'vue-router';
import FollowListPage from '/@/views/social/follow/index.vue';
import * as followApi from '/@/api/content/follow';

vi.mock('/@/api/content/follow');

describe('FollowListPage Integration', () => {
  let router: ReturnType<typeof createRouter>;

  beforeEach(() => {
    setActivePinia(createPinia());
    router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/social/follow', component: FollowListPage },
        { path: '/user/profile/:id', component: { template: '<div>User Profile</div>' } },
      ],
    });
  });

  it('should load and display follow list', async () => {
    const mockData = {
      records: [
        { userId: 'user1', nickname: 'User 1', avatar: '', bio: 'Bio 1', followTime: '2024-01-01', groupId: '', isSpecial: false, lastActiveTime: '2024-01-01' },
        { userId: 'user2', nickname: 'User 2', avatar: '', bio: 'Bio 2', followTime: '2024-01-02', groupId: '', isSpecial: true, lastActiveTime: '2024-01-02' },
      ],
      total: 2,
    };

    vi.mocked(followApi.getFollowList).mockResolvedValue(mockData);
    vi.mocked(followApi.getFollowGroupList).mockResolvedValue([]);

    const wrapper = mount(FollowListPage, {
      global: {
        plugins: [router],
      },
    });

    await vi.waitFor(() => {
      expect(wrapper.findAll('.user-card')).toHaveLength(2);
    });

    expect(wrapper.find('.stats .number').text()).toBe('2');
  });

  it('should search follow list', async () => {
    vi.mocked(followApi.getFollowList).mockResolvedValue({ records: [], total: 0 });
    vi.mocked(followApi.getFollowGroupList).mockResolvedValue([]);

    const wrapper = mount(FollowListPage, {
      global: {
        plugins: [router],
      },
    });

    const searchInput = wrapper.find('.ant-input-search input');
    await searchInput.setValue('test');
    await searchInput.trigger('change');

    await vi.waitFor(() => {
      expect(followApi.getFollowList).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({ keyword: 'test' })
      );
    });
  });

  it('should filter by group', async () => {
    const mockGroups = [
      { id: 'group1', name: 'Group 1', sortOrder: 0, memberCount: 5, isDefault: false },
    ];

    vi.mocked(followApi.getFollowList).mockResolvedValue({ records: [], total: 0 });
    vi.mocked(followApi.getFollowGroupList).mockResolvedValue(mockGroups);

    const wrapper = mount(FollowListPage, {
      global: {
        plugins: [router],
      },
    });

    await vi.waitFor(() => {
      expect(wrapper.find('.ant-radio-button[value="group1"]').exists()).toBe(true);
    });

    await wrapper.find('.ant-radio-button[value="group1"]').trigger('click');

    await vi.waitFor(() => {
      expect(followApi.getFollowList).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({ groupId: 'group1' })
      );
    });
  });

  it('should show empty state when no follows', async () => {
    vi.mocked(followApi.getFollowList).mockResolvedValue({ records: [], total: 0 });
    vi.mocked(followApi.getFollowGroupList).mockResolvedValue([]);

    const wrapper = mount(FollowListPage, {
      global: {
        plugins: [router],
      },
    });

    await vi.waitFor(() => {
      expect(wrapper.find('.empty-state').exists()).toBe(true);
      expect(wrapper.find('.empty-state').text()).toContain('还没有关注任何人');
    });
  });
});
```

- [ ] **Step 3: 编写其他集成测试**

继续编写订阅、信息流等集成测试...

- [ ] **Step 4: 运行集成测试**

```bash
cd jeecgboot-vue3 && npx vitest run tests/integration
```

Expected: 所有测试通过

- [ ] **Step 5: 提交集成测试**

```bash
git add tests/integration/
git commit -m "test: add integration tests for social subscription pages"
```

---

## Task 12: E2E测试

**Files:**
- Create: `jeecgboot-vue3/tests/e2e/social/follow.spec.ts`
- Create: `jeecgboot-vue3/tests/e2e/social/subscribe.spec.ts`

- [ ] **Step 1: 创建E2E测试目录**

```bash
mkdir -p jeecgboot-vue3/tests/e2e/social
```

- [ ] **Step 2: 编写关注用户完整流程E2E测试**

```typescript
// jeecgboot-vue3/tests/e2e/social/follow.spec.ts
import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { chromium, Browser, Page } from 'playwright';

describe('Follow User E2E', () => {
  let browser: Browser;
  let page: Page;

  beforeAll(async () => {
    browser = await chromium.launch();
    page = await browser.newPage();
    await page.goto('http://localhost:3100');
    // 登录逻辑...
  });

  afterAll(async () => {
    await browser.close();
  });

  it('should complete follow user flow', async () => {
    // 1. 进入推荐关注页面
    await page.click('text=推荐关注');
    await page.waitForSelector('.recommend-list');

    // 2. 点击关注按钮
    await page.click('.user-card:first-child .follow-button');
    await page.waitForSelector('.ant-message-success');

    // 3. 进入关注列表页面
    await page.click('text=我的关注');
    await page.waitForSelector('.follow-list');

    // 4. 验证用户已在关注列表中
    const userCards = await page.$$('.user-card');
    expect(userCards.length).toBeGreaterThan(0);

    // 5. 设置特别关注
    await page.click('.user-card:first-child .special-follow-button');
    await page.waitForSelector('.ant-popconfirm');
    await page.click('.ant-popconfirm-buttons .ant-btn-primary');
    await page.waitForSelector('.ant-message-success');

    // 6. 进入特别关注列表
    await page.click('text=特别关注');
    await page.waitForSelector('.special-follow-list');

    // 7. 验证用户在特别关注列表中
    const specialCards = await page.$$('.user-card');
    expect(specialCards.length).toBeGreaterThan(0);

    // 8. 查看关注流
    await page.click('text=关注流');
    await page.waitForSelector('.feed-list');

    // 9. 验证特别关注用户动态置顶
    const priorityItems = await page.$$('.feed-card.is-priority');
    expect(priorityItems.length).toBeGreaterThan(0);

    // 10. 批量管理
    await page.click('text=我的关注');
    await page.click('text=批量管理');
    await page.waitForSelector('.batch-operation-bar');

    // 11. 选择用户
    await page.click('.user-card:first-child .ant-checkbox-input');

    // 12. 批量取消关注
    await page.click('text=批量取消关注');
    await page.waitForSelector('.ant-modal');
    await page.click('.ant-modal-footer .ant-btn-primary');
    await page.waitForSelector('.result-modal');

    // 13. 验证操作结果
    const resultText = await page.textContent('.result-modal');
    expect(resultText).toContain('成功');
  });
});
```

- [ ] **Step 3: 编写订阅流程E2E测试**

继续编写订阅内容源完整流程E2E测试...

- [ ] **Step 4: 运行E2E测试**

```bash
cd jeecgboot-vue3 && npx playwright test tests/e2e
```

Expected: 所有测试通过

- [ ] **Step 5: 提交E2E测试**

```bash
git add tests/e2e/
git commit -m "test: add E2E tests for social subscription flows"
```

---

## Task 13: 性能测试

**Files:**
- Create: `jeecgboot-vue3/tests/performance/social/feed.spec.ts`

- [ ] **Step 1: 创建性能测试目录**

```bash
mkdir -p jeecgboot-vue3/tests/performance/social
```

- [ ] **Step 2: 编写关注流性能测试**

```typescript
// jeecgboot-vue3/tests/performance/social/feed.spec.ts
import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { chromium, Browser, Page } from 'playwright';

describe('Feed Performance', () => {
  let browser: Browser;
  let page: Page;

  beforeAll(async () => {
    browser = await chromium.launch();
    page = await browser.newPage();
    await page.goto('http://localhost:3100');
    // 登录逻辑...
  });

  afterAll(async () => {
    await browser.close();
  });

  it('should load following feed within 2 seconds', async () => {
    const startTime = Date.now();

    await page.click('text=关注流');
    await page.waitForSelector('.feed-list');

    const endTime = Date.now();
    const loadTime = endTime - startTime;

    expect(loadTime).toBeLessThan(2000);
    console.log(`Following feed load time: ${loadTime}ms`);
  });

  it('should handle scroll loading smoothly', async () => {
    await page.click('text=关注流');
    await page.waitForSelector('.feed-list');

    // 模拟滚动加载
    for (let i = 0; i < 5; i++) {
      await page.evaluate(() => {
        window.scrollTo(0, document.body.scrollHeight);
      });
      await page.waitForTimeout(500);
    }

    // 验证滚动流畅，无卡顿
    const feedItems = await page.$$('.feed-card');
    expect(feedItems.length).toBeGreaterThan(20);
  });

  it('should handle large follow list efficiently', async () => {
    await page.click('text=我的关注');
    await page.waitForSelector('.follow-list');

    // 模拟加载大量数据
    const startTime = Date.now();

    // 滚动加载多次
    for (let i = 0; i < 10; i++) {
      await page.evaluate(() => {
        window.scrollTo(0, document.body.scrollHeight);
      });
      await page.waitForTimeout(300);
    }

    const endTime = Date.now();
    const loadTime = endTime - startTime;

    // 1000+ 条数据加载应该在合理时间内完成
    expect(loadTime).toBeLessThan(10000);
    console.log(`Large follow list load time: ${loadTime}ms`);
  });

  it('should handle batch operations within 3 seconds', async () => {
    await page.click('text=我的关注');
    await page.click('text=批量管理');

    // 选择100个用户
    await page.click('.select-all-checkbox');

    const startTime = Date.now();

    // 执行批量操作
    await page.click('text=批量取消关注');
    await page.waitForSelector('.ant-modal');
    await page.click('.ant-modal-footer .ant-btn-primary');
    await page.waitForSelector('.result-modal');

    const endTime = Date.now();
    const operationTime = endTime - startTime;

    expect(operationTime).toBeLessThan(3000);
    console.log(`Batch operation time: ${operationTime}ms`);
  });
});
```

- [ ] **Step 3: 运行性能测试**

```bash
cd jeecgboot-vue3 && npx playwright test tests/performance
```

Expected: 所有性能指标达标

- [ ] **Step 4: 提交性能测试**

```bash
git add tests/performance/
git commit -m "test: add performance tests for social subscription features"
```

---

## Task 14: 文档与验收

**Files:**
- Create: `jeecgboot-vue3/docs/social/components.md`
- Create: `jeecgboot-vue3/docs/social/api.md`
- Create: `jeecgboot-vue3/docs/social/deployment.md`

- [ ] **Step 1: 创建文档目录**

```bash
mkdir -p jeecgboot-vue3/docs/social
```

- [ ] **Step 2: 编写组件使用文档**

```markdown
# 社交订阅组件使用文档

## FollowButton 关注按钮

### 属性
| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| userId | string | - | 当前用户ID |
| targetUserId | string | - | 目标用户ID |
| isFollowing | boolean | false | 是否已关注 |
| disabled | boolean | false | 是否禁用 |

### 事件
| 事件 | 说明 |
|------|------|
| update:isFollowing | 关注状态变更 |
| follow | 关注成功 |
| unfollow | 取消关注成功 |

### 示例
```vue
<FollowButton
  :user-id="currentUserId"
  :target-user-id="targetUserId"
  :is-following="isFollowing"
  @update:is-following="isFollowing = $event"
/>
```

## SpecialFollowButton 特别关注按钮

### 属性
| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| userId | string | - | 当前用户ID |
| targetUserId | string | - | 目标用户ID |
| isFollowing | boolean | false | 是否已关注 |
| isSpecial | boolean | false | 是否特别关注 |

### 事件
| 事件 | 说明 |
|------|------|
| update:isSpecial | 特别关注状态变更 |
| special | 设置特别关注成功 |
| cancelSpecial | 取消特别关注成功 |

### 示例
```vue
<SpecialFollowButton
  :user-id="currentUserId"
  :target-user-id="targetUserId"
  :is-following="isFollowing"
  :is-special="isSpecial"
  @update:is-special="isSpecial = $event"
/>
```

## SubscribeButton 订阅按钮

### 属性
| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| userId | string | - | 当前用户ID |
| sourceId | string | - | 内容源ID |
| sourceType | string | - | 内容源类型 |
| isSubscribed | boolean | false | 是否已订阅 |
| isPaused | boolean | false | 是否已暂停 |

### 事件
| 事件 | 说明 |
|------|------|
| update:isSubscribed | 订阅状态变更 |
| subscribe | 订阅成功 |
| unsubscribe | 取消订阅成功 |

### 示例
```vue
<SubscribeButton
  :user-id="currentUserId"
  :source-id="sourceId"
  source-type="topic"
  :is-subscribed="isSubscribed"
  @update:is-subscribed="isSubscribed = $event"
/>
```

## UserCard 用户卡片

### 属性
| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| userId | string | - | 当前用户ID |
| user | object | - | 用户信息对象 |
| isMobile | boolean | false | 是否移动端 |

### 用户对象结构
```typescript
interface User {
  userId: string;
  nickname: string;
  avatar: string;
  bio: string;
  followTime: string;
  groupName: string;
  isSpecial: boolean;
  lastActiveTime: string;
}
```

### 事件
| 事件 | 说明 |
|------|------|
| unfollow | 取消关注 |
| specialChange | 特别关注状态变更 |
| groupChange | 分组变更 |

### 示例
```vue
<UserCard
  :user-id="currentUserId"
  :user="user"
  :is-mobile="isMobile"
  @unfollow="handleUnfollow"
  @special-change="handleSpecialChange"
/>
```

## FeedCard 动态卡片

### 属性
| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| feed | object | - | 动态信息对象 |
| isMobile | boolean | false | 是否移动端 |

### 动态对象结构
```typescript
interface Feed {
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

### 示例
```vue
<FeedCard
  :feed="feed"
  :is-mobile="isMobile"
/>
```

## BatchOperationBar 批量操作栏

### 属性
| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| selectedCount | number | 0 | 已选数量 |
| loading | boolean | false | 加载状态 |

### 事件
| 事件 | 说明 |
|------|------|
| batchUnfollow | 批量取消关注 |
| batchMoveGroup | 批量移动分组 |
| batchCancelSpecial | 批量取消特别关注 |
| cancel | 取消批量模式 |

### 示例
```vue
<BatchOperationBar
  :selected-count="selectedCount"
  :loading="batchLoading"
  @batch-unfollow="handleBatchUnfollow"
  @batch-move-group="handleBatchMoveGroup"
  @cancel="exitBatchMode"
/>
```
```

- [ ] **Step 3: 编写API接口文档**

继续编写API接口文档...

- [ ] **Step 4: 编写部署文档**

继续编写部署文档...

- [ ] **Step 5: 功能验收测试**

按照验收标准逐项测试所有功能...

- [ ] **Step 6: 性能验收测试**

测试所有性能指标是否达标...

- [ ] **Step 7: 兼容性验收测试**

在Chrome/Firefox/Safari/Edge浏览器中测试...

- [ ] **Step 8: 提交文档**

```bash
git add docs/social/
git commit -m "docs: add social subscription documentation"
```

---

## Task 15: 代码审查与优化

- [ ] **Step 1: 代码审查**

审查代码质量、命名规范、边界条件...

- [ ] **Step 2: 性能优化**

基于测试结果优化性能...

- [ ] **Step 3: 安全审查**

检查XSS防护、CSRF防护、权限校验...

- [ ] **Step 4: 代码重构**

消除重复代码、优化代码结构...

- [ ] **Step 5: 运行全量测试**

```bash
cd jeecgboot-vue3 && pnpm test
```

Expected: 所有测试通过

- [ ] **Step 6: 运行构建**

```bash
cd jeecgboot-vue3 && pnpm build
```

Expected: 构建成功

- [ ] **Step 7: 最终提交**

```bash
git add -A
git commit -m "refactor: code review and optimization for social subscription module"
```

---

## Final Checklist

- [ ] 所有功能按照PRD要求实现
- [ ] 所有验收标准通过
- [ ] 所有性能指标达标
- [ ] 所有测试通过（单元、集成、E2E、性能）
- [ ] 代码审查完成
- [ ] 文档完整
- [ ] 构建成功
- [ ] 准备合并到主分支
