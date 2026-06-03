# 频道内容发布与治理 - 前端实现计划

> **For agentic workers:** Use superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现频道内容发布与治理的完整前端 UI 层，包括 13 个业务组件、3 个 Pinia Store、5 个 API 模块和约 25 个 REST API 对接。

**Architecture:** 在 `src/views/channel/` 下按功能模块组织页面，治理后台使用 Tab 切换（待审区/内容管理/回收站/治理日志/公告管理）。API 层使用 `defHttp` 封装，Store 按业务域划分为 publish/review/governance 三个模块。表格组件选用 JVxeTable（复杂批量操作场景）和 Table（只读列表场景）。

**Tech Stack:** Vue3 + TypeScript + Ant Design Vue 4 + Pinia + defHttp + JVxeTable + Tinymce

---

## Task 1: API 层封装

**Files:**
- Create: `jeecgboot-vue3/src/api/content/channel/publish.ts`
- Create: `jeecgboot-vue3/src/api/content/channel/review.ts`
- Create: `jeecgboot-vue3/src/api/content/channel/governance.ts`
- Create: `jeecgboot-vue3/src/api/content/channel/announcement.ts`
- Create: `jeecgboot-vue3/src/api/content/channel/addContent.ts`

### Step 1.1: 创建 publish.ts API 模块

```bash
mkdir -p jeecgboot-vue3/src/api/content/channel
```

创建 `jeecgboot-vue3/src/api/content/channel/publish.ts`:

```typescript
import { defHttp } from '/@/utils/http/axios';

enum Api {
  available = '/api/channel/publish/available',
  submit = '/api/channel/publish/submit',
  result = '/api/channel/publish/result',
  scheduled = '/api/channel/publish/scheduled',
  scheduledList = '/api/channel/publish/scheduled/list',
  limitCheck = '/api/channel/publish/limit/check',
}

/** 获取用户可发布/投稿/管理的频道列表 */
export const getAvailableChannels = () => defHttp.get({ url: Api.available });

/** 提交内容到频道（支持多频道） */
export const submitPublish = (data: { contentId: string; channelIds: string[]; scheduledTime?: string }) =>
  defHttp.post({ url: Api.submit, data });

/** 查询发布结果 */
export const getPublishResult = (taskId: string) => defHttp.get({ url: `${Api.result}/${taskId}` });

/** 设定定时发布 */
export const createScheduledPublish = (data: { contentId: string; channelIds: string[]; scheduledTime: string }) =>
  defHttp.post({ url: Api.scheduled, data });

/** 修改定时发布时间 */
export const updateScheduledPublish = (id: string, data: { scheduledTime: string }) =>
  defHttp.put({ url: `${Api.scheduled}/${id}`, data });

/** 取消定时发布 */
export const cancelScheduledPublish = (id: string) =>
  defHttp.delete({ url: `${Api.scheduled}/${id}` });

/** 获取当前用户的定时发布任务列表 */
export const getScheduledList = () => defHttp.get({ url: Api.scheduledList });

/** 预校验发布限额 */
export const checkPublishLimit = (data: { contentId: string; channelIds: string[] }) =>
  defHttp.post({ url: Api.limitCheck, data });
```

### Step 1.2: 创建 review.ts API 模块

创建 `jeecgboot-vue3/src/api/content/channel/review.ts`:

```typescript
import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/api/channel/review/list',
  approve = '/api/channel/review/approve',
  reject = '/api/channel/review/reject',
  stats = '/api/channel/review/stats',
}

/** 获取待审区列表 */
export const getReviewList = (params: {
  channelId: string;
  contentType?: string;
  submitter?: string;
  submitTimeStart?: string;
  submitTimeEnd?: string;
  reviewStatus?: string;
  timeoutStatus?: string;
  keyword?: string;
  pageNo?: number;
  pageSize?: number;
}) => defHttp.get({ url: Api.list, params });

/** 审核通过（支持批量） */
export const approveReview = (data: { ids: string[] }) =>
  defHttp.post({ url: Api.approve, data });

/** 审核拒绝（支持批量，含原因） */
export const rejectReview = (data: { ids: string[]; reason: string }) =>
  defHttp.post({ url: Api.reject, data });

/** 待审统计 */
export const getReviewStats = (channelId: string) =>
  defHttp.get({ url: Api.stats, params: { channelId } });
```

### Step 1.3: 创建 governance.ts API 模块

创建 `jeecgboot-vue3/src/api/content/channel/governance.ts`:

```typescript
import { defHttp } from '/@/utils/http/axios';

enum Api {
  contentList = '/api/channel/governance/content/list',
  pin = '/api/channel/governance/pin',
  feature = '/api/channel/governance/feature',
  delete = '/api/channel/governance/delete',
  move = '/api/channel/governance/move',
  editAssist = '/api/channel/governance/edit-assist',
  editAssistHistory = '/api/channel/governance/edit-assist/history',
  recycleBinList = '/api/channel/governance/recycle-bin/list',
  recycleBinRestore = '/api/channel/governance/recycle-bin/restore',
  logList = '/api/channel/governance/log/list',
}

/** 频道内容列表 */
export const getGovernanceContentList = (params: {
  channelId: string;
  contentType?: string;
  status?: string;
  author?: string;
  startTime?: string;
  endTime?: string;
  sortBy?: string;
  keyword?: string;
  pageNo?: number;
  pageSize?: number;
}) => defHttp.get({ url: Api.contentList, params });

/** 置顶/取消置顶 */
export const togglePin = (data: { contentId: string; channelId: string; pin: boolean }) =>
  defHttp.post({ url: Api.pin, data });

/** 精华/取消精华 */
export const toggleFeature = (data: { contentId: string; channelId: string; feature: boolean }) =>
  defHttp.post({ url: Api.feature, data });

/** 删除内容到回收站 */
export const deleteContent = (data: { contentIds: string[]; channelId: string; reason?: string; notifyAuthor?: boolean }) =>
  defHttp.post({ url: Api.delete, data });

/** 移出频道 */
export const moveContent = (data: { contentId: string; sourceChannelId: string; targetChannelId: string }) =>
  defHttp.post({ url: Api.move, data });

/** 编辑协助 */
export const editAssist = (data: { contentId: string; channelId: string; title?: string; tags?: string[]; summary?: string; reason: string }) =>
  defHttp.post({ url: Api.editAssist, data });

/** 获取编辑协助修订历史 */
export const getEditAssistHistory = (contentId: string) =>
  defHttp.get({ url: `${Api.editAssistHistory}/${contentId}` });

/** 回收站列表 */
export const getRecycleBinList = (params: {
  channelId: string;
  contentType?: string;
  deletedBy?: string;
  startTime?: string;
  endTime?: string;
  pageNo?: number;
  pageSize?: number;
}) => defHttp.get({ url: Api.recycleBinList, params });

/** 恢复回收站内容 */
export const restoreContent = (data: { ids: string[]; channelId: string }) =>
  defHttp.post({ url: Api.recycleBinRestore, data });

/** 治理日志列表 */
export const getGovernanceLogList = (params: {
  channelId: string;
  actionType?: string;
  operator?: string;
  startTime?: string;
  endTime?: string;
  keyword?: string;
  pageNo?: number;
  pageSize?: number;
}) => defHttp.get({ url: Api.logList, params });
```

### Step 1.4: 创建 announcement.ts API 模块

创建 `jeecgboot-vue3/src/api/content/channel/announcement.ts`:

```typescript
import { defHttp } from '/@/utils/http/axios';

enum Api {
  get = '/api/channel/announcement',
  post = '/api/channel/announcement',
  delete = '/api/channel/announcement',
  preview = '/api/channel/announcement/preview',
  history = '/api/channel/announcement/history',
  restore = '/api/channel/announcement/restore',
}

/** 获取频道当前公告 */
export const getAnnouncement = (channelId: string) =>
  defHttp.get({ url: `${Api.get}/${channelId}` });

/** 发布/更新公告 */
export const saveAnnouncement = (data: { channelId: string; title: string; content: string; version?: number }) =>
  defHttp.post({ url: Api.post, data });

/** 删除公告 */
export const deleteAnnouncement = (id: string) =>
  defHttp.delete({ url: `${Api.delete}/${id}` });

/** 公告预览 */
export const previewAnnouncement = (data: { content: string }) =>
  defHttp.post({ url: Api.preview, data });

/** 获取频道公告历史版本列表 */
export const getAnnouncementHistory = (channelId: string) =>
  defHttp.get({ url: `${Api.history}/${channelId}` });

/** 恢复历史版本 */
export const restoreAnnouncementVersion = (versionId: string) =>
  defHttp.post({ url: `${Api.restore}/${versionId}` });
```

### Step 1.5: 创建 addContent.ts API 模块

创建 `jeecgboot-vue3/src/api/content/channel/addContent.ts`:

```typescript
import { defHttp } from '/@/utils/http/axios';

enum Api {
  add = '/api/channel/content/add',
  search = '/api/channel/content/add/search',
  channels = '/api/channel/content/channels',
}

/** 添加已发布内容到频道 */
export const addContentToChannel = (data: {
  contentId: string;
  channelIds: string[];
  operatorNote?: string;
}) => defHttp.post({ url: Api.add, data });

/** 搜索可添加的已发布内容 */
export const searchAddableContent = (params: { keyword: string; contentType?: string; pageNo?: number; pageSize?: number }) =>
  defHttp.get({ url: Api.search, params });

/** 查看内容所在频道列表 */
export const getContentChannels = (contentId: string) =>
  defHttp.get({ url: `${Api.channels}/${contentId}` });
```

### Step 1.6: 提交 API 层代码

```bash
git add jeecgboot-vue3/src/api/content/channel/
git commit -m "feat(channel): add API layer for channel publishing, review, governance, announcement and content management"
```

---

## Task 2: Store 层实现

**Files:**
- Create: `jeecgboot-vue3/src/store/modules/channelPublish.ts`
- Create: `jeecgboot-vue3/src/store/modules/channelReview.ts`
- Create: `jeecgboot-vue3/src/store/modules/channelGovernance.ts`

### Step 2.1: 创建 useChannelPublishStore

创建 `jeecgboot-vue3/src/store/modules/channelPublish.ts`:

```typescript
import { defineStore } from 'pinia';
import { store } from '/@/store';
import {
  getAvailableChannels,
  getScheduledList,
  updateScheduledPublish,
  cancelScheduledPublish,
} from '/@/api/content/channel/publish';

interface Channel {
  id: string;
  name: string;
  type: string;
  userRole: string;
  publishResult: string;
  publishable: boolean;
  reason?: string;
}

interface PublishResultItem {
  channelId: string;
  channelName: string;
  status: 'success' | 'review' | 'pending' | 'fail';
  failReason?: string;
}

interface ScheduledTask {
  id: string;
  contentId: string;
  contentTitle: string;
  channelName: string;
  scheduledTime: string;
  status: string;
}

interface ChannelPublishState {
  availableChannels: Channel[];
  selectedChannels: Channel[];
  publishResult: Record<string, PublishResultItem>;
  scheduledTime: string | null;
  maxChannelCount: number;
  scheduledTaskList: ScheduledTask[];
  loading: boolean;
}

export const useChannelPublishStore = defineStore({
  id: 'app-channel-publish',
  state: (): ChannelPublishState => ({
    availableChannels: [],
    selectedChannels: [],
    publishResult: {},
    scheduledTime: null,
    maxChannelCount: 5,
    scheduledTaskList: [],
    loading: false,
  }),
  actions: {
    async fetchAvailableChannels() {
      this.loading = true;
      try {
        const res = await getAvailableChannels();
        this.availableChannels = res.list || [];
        this.maxChannelCount = res.maxChannelCount || 5;
      } finally {
        this.loading = false;
      }
    },
    addChannel(channel: Channel) {
      if (this.selectedChannels.length >= this.maxChannelCount) return;
      if (this.selectedChannels.find((c) => c.id === channel.id)) return;
      this.selectedChannels.push(channel);
    },
    removeChannel(channelId: string) {
      this.selectedChannels = this.selectedChannels.filter((c) => c.id !== channelId);
    },
    setPublishResult(result: Record<string, PublishResultItem>) {
      this.publishResult = result;
    },
    clearResult() {
      this.publishResult = {};
    },
    setScheduledTime(time: string | null) {
      this.scheduledTime = time;
    },
    async fetchScheduledTasks() {
      this.loading = true;
      try {
        const res = await getScheduledList();
        this.scheduledTaskList = res || [];
      } finally {
        this.loading = false;
      }
    },
    async editScheduledTime(id: string, newTime: string) {
      await updateScheduledPublish(id, { scheduledTime: newTime });
      await this.fetchScheduledTasks();
    },
    async cancelScheduledTask(id: string) {
      await cancelScheduledPublish(id);
      this.scheduledTaskList = this.scheduledTaskList.filter((t) => t.id !== id);
    },
  },
});

export function useChannelPublishStoreWithOut() {
  return useChannelPublishStore(store);
}
```

### Step 2.2: 创建 useChannelReviewStore

创建 `jeecgboot-vue3/src/store/modules/channelReview.ts`:

```typescript
import { defineStore } from 'pinia';
import { store } from '/@/store';
import { getReviewList, approveReview, rejectReview, getReviewStats } from '/@/api/content/channel/review';

interface ReviewItem {
  id: string;
  title: string;
  contentType: string;
  submitter: string;
  submitTime: string;
  sourceScene: string;
  hitRule: string;
  isTimeout: boolean;
}

interface ReviewStats {
  total: number;
  timeoutCount: number;
}

interface ReviewFilter {
  channelId: string;
  contentType?: string;
  submitter?: string;
  submitTimeStart?: string;
  submitTimeEnd?: string;
  reviewStatus?: string;
  timeoutStatus?: string;
  keyword?: string;
  pageNo: number;
  pageSize: number;
}

interface ChannelReviewState {
  reviewList: ReviewItem[];
  filterParams: ReviewFilter;
  selectedIds: string[];
  stats: ReviewStats;
  total: number;
  loading: boolean;
}

export const useChannelReviewStore = defineStore({
  id: 'app-channel-review',
  state: (): ChannelReviewState => ({
    reviewList: [],
    filterParams: {
      channelId: '',
      pageNo: 1,
      pageSize: 20,
    },
    selectedIds: [],
    stats: { total: 0, timeoutCount: 0 },
    total: 0,
    loading: false,
  }),
  actions: {
    async fetchList() {
      this.loading = true;
      try {
        const res = await getReviewList(this.filterParams);
        this.reviewList = res.records || [];
        this.total = res.total || 0;
      } finally {
        this.loading = false;
      }
    },
    async fetchStats(channelId: string) {
      const res = await getReviewStats(channelId);
      this.stats = res || { total: 0, timeoutCount: 0 };
    },
    setFilter(params: Partial<ReviewFilter>) {
      this.filterParams = { ...this.filterParams, ...params };
    },
    setSelectedIds(ids: string[]) {
      this.selectedIds = ids;
    },
    async approve(ids: string[]) {
      await approveReview({ ids });
      await this.fetchList();
    },
    async reject(ids: string[], reason: string) {
      await rejectReview({ ids, reason });
      await this.fetchList();
    },
    async batchApprove() {
      await this.approve(this.selectedIds);
      this.selectedIds = [];
    },
    async batchReject(reason: string) {
      await this.reject(this.selectedIds, reason);
      this.selectedIds = [];
    },
  },
});

export function useChannelReviewStoreWithOut() {
  return useChannelReviewStore(store);
}
```

### Step 2.3: 创建 useChannelGovernanceStore

创建 `jeecgboot-vue3/src/store/modules/channelGovernance.ts`:

```typescript
import { defineStore } from 'pinia';
import { store } from '/@/store';
import {
  getGovernanceContentList,
  togglePin,
  toggleFeature,
  deleteContent,
  moveContent,
  editAssist,
  getRecycleBinList,
  restoreContent,
  getGovernanceLogList,
} from '/@/api/content/channel/governance';

interface ContentItem {
  id: string;
  title: string;
  contentType: string;
  author: string;
  publishTime: string;
  status: string;
  isPinned: boolean;
  isFeatured: boolean;
}

interface RecycleBinItem {
  id: string;
  title: string;
  contentType: string;
  originalAuthor: string;
  deletedBy: string;
  deleteTime: string;
  deleteReason: string;
  remainingDays: number;
}

interface LogItem {
  id: string;
  time: string;
  operator: string;
  actionType: string;
  targetTitle: string;
  result: string;
  remark: string;
}

interface GovernanceFilter {
  channelId: string;
  contentType?: string;
  status?: string;
  author?: string;
  startTime?: string;
  endTime?: string;
  sortBy?: string;
  keyword?: string;
  pageNo: number;
  pageSize: number;
}

interface ChannelGovernanceState {
  contentList: ContentItem[];
  filterParams: GovernanceFilter;
  total: number;
  recycleBinList: RecycleBinItem[];
  recycleBinTotal: number;
  governanceLogList: LogItem[];
  governanceLogTotal: number;
  loading: boolean;
}

export const useChannelGovernanceStore = defineStore({
  id: 'app-channel-governance',
  state: (): ChannelGovernanceState => ({
    contentList: [],
    filterParams: { channelId: '', pageNo: 1, pageSize: 20 },
    total: 0,
    recycleBinList: [],
    recycleBinTotal: 0,
    governanceLogList: [],
    governanceLogTotal: 0,
    loading: false,
  }),
  actions: {
    async fetchList() {
      this.loading = true;
      try {
        const res = await getGovernanceContentList(this.filterParams);
        this.contentList = res.records || [];
        this.total = res.total || 0;
      } finally {
        this.loading = false;
      }
    },
    setFilter(params: Partial<GovernanceFilter>) {
      this.filterParams = { ...this.filterParams, ...params };
    },
    async pin(contentId: string, channelId: string, pin: boolean) {
      await togglePin({ contentId, channelId, pin });
      await this.fetchList();
    },
    async feature(contentId: string, channelId: string, isFeatured: boolean) {
      await toggleFeature({ contentId, channelId, feature: isFeatured });
      await this.fetchList();
    },
    async deleteContent(contentIds: string[], channelId: string, reason?: string, notifyAuthor?: boolean) {
      await deleteContent({ contentIds, channelId, reason, notifyAuthor });
      await this.fetchList();
    },
    async moveContent(contentId: string, sourceChannelId: string, targetChannelId: string) {
      await moveContent({ contentId, sourceChannelId, targetChannelId });
      await this.fetchList();
    },
    async editAssist(data: { contentId: string; channelId: string; title?: string; tags?: string[]; summary?: string; reason: string }) {
      await editAssist(data);
    },
    async fetchRecycleBin(channelId: string, params?: { contentType?: string; deletedBy?: string; startTime?: string; endTime?: string; pageNo?: number; pageSize?: number }) {
      this.loading = true;
      try {
        const res = await getRecycleBinList({ channelId, ...params });
        this.recycleBinList = res.records || [];
        this.recycleBinTotal = res.total || 0;
      } finally {
        this.loading = false;
      }
    },
    async restore(ids: string[], channelId: string) {
      await restoreContent({ ids, channelId });
    },
    async fetchGovernanceLog(channelId: string, params?: { actionType?: string; operator?: string; startTime?: string; endTime?: string; keyword?: string; pageNo?: number; pageSize?: number }) {
      this.loading = true;
      try {
        const res = await getGovernanceLogList({ channelId, ...params });
        this.governanceLogList = res.records || [];
        this.governanceLogTotal = res.total || 0;
      } finally {
        this.loading = false;
      }
    },
  },
});

export function useChannelGovernanceStoreWithOut() {
  return useChannelGovernanceStore(store);
}
```

### Step 2.4: 提交 Store 层代码

```bash
git add jeecgboot-vue3/src/store/modules/channelPublish.ts \
        jeecgboot-vue3/src/store/modules/channelReview.ts \
        jeecgboot-vue3/src/store/modules/channelGovernance.ts
git commit -m "feat(channel): add Pinia stores for publish, review and governance"
```

---

## Task 3: 频道选择组件 ChannelSelector

**Files:**
- Create: `jeecgboot-vue3/src/views/channel/publish/ChannelSelector.vue`
- Test: `jeecgboot-vue3/src/views/channel/__tests__/ChannelSelector.test.ts`

### Step 3.1: 编写 ChannelSelector 测试

创建 `jeecgboot-vue3/src/views/channel/__tests__/ChannelSelector.test.ts`:

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import ChannelSelector from '../publish/ChannelSelector.vue';

vi.mock('/@/api/content/channel/publish', () => ({
  getAvailableChannels: vi.fn().mockResolvedValue({
    list: [
      { id: '1', name: '频道A', type: 'system', userRole: 'admin', publishResult: 'direct', publishable: true },
      { id: '2', name: '频道B', type: 'personal', userRole: 'member', publishResult: 'review', publishable: true },
      { id: '3', name: '频道C', type: 'org', userRole: 'member', publishResult: 'blocked', publishable: false, reason: '仅管理员可发布' },
    ],
    maxChannelCount: 5,
  }),
}));

describe('ChannelSelector', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('应加载频道列表并按分组展示', async () => {
    const wrapper = mount(ChannelSelector);
    await vi.dynamicImportSettled();
    expect(wrapper.text()).toContain('频道A');
    expect(wrapper.text()).toContain('频道B');
  });

  it('不可发布频道应置灰展示', async () => {
    const wrapper = mount(ChannelSelector);
    await vi.dynamicImportSettled();
    const blockedChannel = wrapper.find('[data-channel-id="3"]');
    expect(blockedChannel.classes()).toContain('disabled');
  });

  it('达到上限时应阻止继续选择', async () => {
    const wrapper = mount(ChannelSelector, { props: { maxChannelCount: 1 } });
    await vi.dynamicImportSettled();
    // 选择第一个频道
    await wrapper.find('[data-channel-id="1"]').trigger('click');
    // 第二个频道应不可选
    const secondChannel = wrapper.find('[data-channel-id="2"]');
    expect(secondChannel.classes()).toContain('disabled');
  });

  it('已选频道应支持移除', async () => {
    const wrapper = mount(ChannelSelector);
    await vi.dynamicImportSettled();
    await wrapper.find('[data-channel-id="1"]').trigger('click');
    expect(wrapper.findAll('.selected-tag')).toHaveLength(1);
    await wrapper.find('.selected-tag .remove-btn').trigger('click');
    expect(wrapper.findAll('.selected-tag')).toHaveLength(0);
  });
});
```

### Step 3.2: 运行测试验证失败

```bash
cd jeecgboot-vue3 && npx vitest run src/views/channel/__tests__/ChannelSelector.test.ts
```

预期: FAIL — 组件不存在

### Step 3.3: 实现 ChannelSelector 组件

创建 `jeecgboot-vue3/src/views/channel/publish/ChannelSelector.vue`:

```vue
<template>
  <Modal v-model:visible="visible" title="选择频道" :width="640" @cancel="handleClose">
    <div class="channel-selector">
      <InputSearch
        v-model:value="keyword"
        placeholder="搜索频道"
        :loading="searching"
        @search="handleSearch"
        class="channel-search"
      />
      <div v-if="selectedChannels.length > 0" class="selected-tags">
        <Tag v-for="ch in selectedChannels" :key="ch.id" closable @close="removeChannel(ch.id)">
          {{ ch.name }}
        </Tag>
        <span class="selected-count">已选 {{ selectedChannels.length }}/{{ maxChannelCount }}</span>
      </div>
      <Spin :spinning="loading">
        <div v-if="filteredChannels.length === 0 && !loading" class="empty-state">
          <Empty description="暂无可发布的频道，去加入或创建频道" />
        </div>
        <div v-else>
          <div v-for="group in channelGroups" :key="group.label" class="channel-group">
            <div class="group-label">{{ group.label }}</div>
            <div class="channel-list">
              <div
                v-for="ch in group.channels"
                :key="ch.id"
                :data-channel-id="ch.id"
                :class="['channel-card', { disabled: !ch.publishable || isMaxReached, selected: isSelected(ch.id) }]"
                @click="handleSelect(ch)"
              >
                <div class="channel-name">{{ ch.name }}</div>
                <div class="channel-meta">
                  <Tag size="small">{{ ch.type }}</Tag>
                  <Tag size="small">{{ ch.userRole }}</Tag>
                  <span v-if="!ch.publishable" class="blocked-reason">{{ ch.reason }}</span>
                  <span v-if="isSelected(ch.id)" class="selected-icon">已选</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </Spin>
    </div>
    <template #footer>
      <Button @click="handleClose">取消</Button>
      <Button type="primary" :disabled="selectedChannels.length === 0" @click="handleConfirm">
        确认选择 ({{ selectedChannels.length }})
      </Button>
    </template>
  </Modal>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, watch } from 'vue';
import { Modal, InputSearch, Tag, Spin, Empty, Button } from 'ant-design-vue';
import { getAvailableChannels } from '/@/api/content/channel/publish';
import { useChannelPublishStore } from '/@/store/modules/channelPublish';
import { useDebounceFn } from '@vueuse/core';

interface Channel {
  id: string;
  name: string;
  type: string;
  userRole: string;
  publishResult: string;
  publishable: boolean;
  reason?: string;
}

const props = defineProps<{
  modelValue: boolean;
  maxChannelCount?: number;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'confirm', channels: Channel[]): void;
}>();

const store = useChannelPublishStore();
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const keyword = ref('');
const searching = ref(false);
const loading = ref(false);
const allChannels = ref<Channel[]>([]);
const selectedChannels = ref<Channel[]>([]);
const maxChannelCount = computed(() => props.maxChannelCount || store.maxChannelCount || 5);
const isMaxReached = computed(() => selectedChannels.value.length >= maxChannelCount.value);

const filteredChannels = computed(() => {
  if (!keyword.value) return allChannels.value;
  return allChannels.value.filter((ch) => ch.name.includes(keyword.value));
});

const channelGroups = computed(() => {
  const groups = [
    { label: '推荐频道', channels: [] as Channel[] },
    { label: '我管理的频道', channels: [] as Channel[] },
    { label: '我加入的频道', channels: [] as Channel[] },
  ];
  filteredChannels.value.forEach((ch) => {
    if (ch.userRole === 'admin' || ch.userRole === 'owner') {
      groups[1].channels.push(ch);
    } else {
      groups[2].channels.push(ch);
    }
  });
  return groups.filter((g) => g.channels.length > 0);
});

const isSelected = (id: string) => selectedChannels.value.some((ch) => ch.id === id);

const handleSelect = (ch: Channel) => {
  if (!ch.publishable || isMaxReached.value) return;
  if (isSelected(ch.id)) {
    selectedChannels.value = selectedChannels.value.filter((c) => c.id !== ch.id);
  } else {
    selectedChannels.value.push(ch);
  }
};

const removeChannel = (id: string) => {
  selectedChannels.value = selectedChannels.value.filter((c) => c.id !== id);
};

const handleSearch = useDebounceFn(async () => {
  searching.value = true;
  try {
    // 搜索逻辑已在 computed 中处理
  } finally {
    searching.value = false;
  }
}, 300);

const handleConfirm = () => {
  emit('confirm', selectedChannels.value);
  visible.value = false;
};

const handleClose = () => {
  visible.value = false;
};

onMounted(async () => {
  loading.value = true;
  try {
    const res = await getAvailableChannels();
    allChannels.value = res.list || [];
    store.maxChannelCount = res.maxChannelCount || 5;
  } finally {
    loading.value = false;
  }
});
</script>

<style lang="less" scoped>
.channel-selector {
  .channel-search { margin-bottom: 12px; }
  .selected-tags { margin-bottom: 12px; display: flex; flex-wrap: wrap; gap: 4px; align-items: center;
    .selected-count { margin-left: 8px; color: #999; font-size: 12px; }
  }
  .channel-group { margin-bottom: 16px;
    .group-label { font-weight: 600; margin-bottom: 8px; color: #666; }
  }
  .channel-list { display: flex; flex-wrap: wrap; gap: 8px; }
  .channel-card {
    padding: 8px 12px; border: 1px solid #e8e8e8; border-radius: 6px; cursor: pointer; min-width: 180px;
    &:hover:not(.disabled) { border-color: #1890ff; }
    &.selected { border-color: #1890ff; background: #e6f7ff; }
    &.disabled { opacity: 0.5; cursor: not-allowed; }
    .channel-name { font-weight: 500; margin-bottom: 4px; }
    .channel-meta { display: flex; gap: 4px; align-items: center; font-size: 12px; }
    .blocked-reason { color: #ff4d4f; font-size: 12px; }
  }
}
</style>
```

### Step 3.4: 运行测试验证通过

```bash
cd jeecgboot-vue3 && npx vitest run src/views/channel/__tests__/ChannelSelector.test.ts
```

预期: PASS

### Step 3.5: 提交

```bash
git add jeecgboot-vue3/src/views/channel/publish/ChannelSelector.vue \
        jeecgboot-vue3/src/views/channel/__tests__/ChannelSelector.test.ts
git commit -m "feat(channel): add ChannelSelector component with search, multi-select and permission preview"
```

---

## Task 4: 发布结果组件 PublishResult

**Files:**
- Create: `jeecgboot-vue3/src/views/channel/publish/PublishResult.vue`
- Test: `jeecgboot-vue3/src/views/channel/__tests__/PublishResult.test.ts`

### Step 4.1: 编写 PublishResult 测试

创建 `jeecgboot-vue3/src/views/channel/__tests__/PublishResult.test.ts`:

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import PublishResult from '../publish/PublishResult.vue';

describe('PublishResult', () => {
  beforeEach(() => { setActivePinia(createPinia()); });

  it('应逐频道展示发布结果', () => {
    const wrapper = mount(PublishResult, {
      props: {
        results: [
          { channelId: '1', channelName: '频道A', status: 'success' },
          { channelId: '2', channelName: '频道B', status: 'review' },
          { channelId: '3', channelName: '频道C', status: 'fail', failReason: '发布限额已达上限' },
        ],
      },
    });
    expect(wrapper.text()).toContain('频道A');
    expect(wrapper.text()).toContain('已发布');
    expect(wrapper.text()).toContain('待审核');
    expect(wrapper.text()).toContain('发布限额已达上限');
  });

  it('失败项应展示重试按钮', () => {
    const wrapper = mount(PublishResult, {
      props: {
        results: [
          { channelId: '1', channelName: '频道A', status: 'fail', failReason: '限额' },
        ],
      },
    });
    expect(wrapper.find('.retry-btn').exists()).toBe(true);
  });

  it('定时发布应展示发布时间', () => {
    const wrapper = mount(PublishResult, {
      props: {
        results: [],
        scheduledTime: '2026-06-15 10:00',
      },
    });
    expect(wrapper.text()).toContain('已设定发布时间：2026-06-15 10:00');
  });
});
```

### Step 4.2: 运行测试验证失败

```bash
cd jeecgboot-vue3 && npx vitest run src/views/channel/__tests__/PublishResult.test.ts
```

预期: FAIL

### Step 4.3: 实现 PublishResult 组件

创建 `jeecgboot-vue3/src/views/channel/publish/PublishResult.vue`:

```vue
<template>
  <div class="publish-result">
    <div class="result-header">发布结果</div>
    <div v-if="scheduledTime" class="scheduled-info">
      <ClockCircleOutlined /> 已设定发布时间：{{ scheduledTime }}
    </div>
    <div v-for="item in results" :key="item.channelId" :class="['result-item', item.status]">
      <div class="result-status">
        <CheckCircleOutlined v-if="item.status === 'success'" class="icon-success" />
        <ClockCircleOutlined v-else-if="item.status === 'review'" class="icon-review" />
        <CloseCircleOutlined v-else class="icon-fail" />
      </div>
      <div class="result-info">
        <span class="channel-name">{{ item.channelName }}</span>
        <span v-if="item.status === 'success'" class="status-text">已发布</span>
        <span v-else-if="item.status === 'review'" class="status-text">已提交审核，等待管理员处理</span>
        <span v-else class="status-text fail-reason">{{ item.failReason }}</span>
      </div>
      <Button v-if="item.status === 'fail'" class="retry-btn" size="small" @click="$emit('retry', item.channelId)">
        重试
      </Button>
    </div>
    <div class="result-actions">
      <Button @click="$emit('viewContent')">查看内容</Button>
      <Button @click="$emit('backToEdit')">返回编辑</Button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { Button } from 'ant-design-vue';
import { CheckCircleOutlined, ClockCircleOutlined, CloseCircleOutlined } from '@ant-design/icons-vue';

interface ResultItem {
  channelId: string;
  channelName: string;
  status: 'success' | 'review' | 'pending' | 'fail';
  failReason?: string;
}

defineProps<{
  results: ResultItem[];
  scheduledTime?: string;
}>();

defineEmits<{
  (e: 'retry', channelId: string): void;
  (e: 'viewContent'): void;
  (e: 'backToEdit'): void;
}>();
</script>

<style lang="less" scoped>
.publish-result {
  padding: 16px;
  .result-header { font-size: 16px; font-weight: 600; margin-bottom: 12px; }
  .scheduled-info { margin-bottom: 12px; color: #1890ff; }
  .result-item {
    display: flex; align-items: center; padding: 8px 12px; margin-bottom: 8px; border-radius: 6px;
    &.success { background: #f6ffed; }
    &.review { background: #e6f7ff; }
    &.fail { background: #fff2f0; }
    .result-status { margin-right: 8px; font-size: 18px; }
    .icon-success { color: #52c41a; }
    .icon-review { color: #1890ff; }
    .icon-fail { color: #ff4d4f; }
    .result-info { flex: 1; }
    .channel-name { font-weight: 500; margin-right: 8px; }
    .fail-reason { color: #ff4d4f; }
  }
  .result-actions { margin-top: 16px; display: flex; gap: 8px; justify-content: flex-end; }
}
</style>
```

### Step 4.4: 运行测试验证通过

```bash
cd jeecgboot-vue3 && npx vitest run src/views/channel/__tests__/PublishResult.test.ts
```

### Step 4.5: 提交

```bash
git add jeecgboot-vue3/src/views/channel/publish/PublishResult.vue \
        jeecgboot-vue3/src/views/channel/__tests__/PublishResult.test.ts
git commit -m "feat(channel): add PublishResult component with per-channel status and retry"
```

---

## Task 5: 定时发布管理 ScheduledPublish

**Files:**
- Create: `jeecgboot-vue3/src/views/channel/publish/ScheduledPublish.vue`

### Step 5.1: 实现 ScheduledPublish 组件

创建 `jeecgboot-vue3/src/views/channel/publish/ScheduledPublish.vue`:

```vue
<template>
  <div class="scheduled-publish">
    <div class="scheduled-header">
      <span>我的定时发布</span>
      <Button size="small" @click="refresh">刷新</Button>
    </div>
    <Table :dataSource="scheduledTaskList" :columns="columns" :loading="loading" rowKey="id" size="small">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <Space>
            <Button size="small" type="link" @click="handleEditTime(record)">编辑时间</Button>
            <Button size="small" type="link" danger @click="handleCancel(record)">取消发布</Button>
          </Space>
        </template>
      </template>
    </Table>
    <Modal v-model:visible="editVisible" title="编辑定时发布时间" @ok="handleSaveTime">
      <DatePicker v-model:value="newTime" show-time format="YYYY-MM-DD HH:mm" :disabledDate="disabledDate" style="width: 100%" />
    </Modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { Table, Button, Space, Modal, DatePicker, message } from 'ant-design-vue';
import { useChannelPublishStore } from '/@/store/modules/channelPublish';

const store = useChannelPublishStore();
const { scheduledTaskList, loading } = storeToRefs(store);

const editVisible = ref(false);
const editingId = ref('');
const newTime = ref(null);

const columns = [
  { title: '标题', dataIndex: 'contentTitle', key: 'contentTitle' },
  { title: '目标频道', dataIndex: 'channelName', key: 'channelName' },
  { title: '计划发布时间', dataIndex: 'scheduledTime', key: 'scheduledTime' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '操作', key: 'action', width: 160 },
];

const disabledDate = (current: any) => current && current < Date.now();

const handleEditTime = (record: any) => {
  editingId.value = record.id;
  newTime.value = null;
  editVisible.value = true;
};

const handleSaveTime = async () => {
  if (!newTime.value) return;
  await store.editScheduledTime(editingId.value, newTime.value);
  editVisible.value = false;
  message.success('定时发布时间已更新');
};

const handleCancel = (record: any) => {
  Modal.confirm({
    title: '确认取消',
    content: `确认取消《${record.contentTitle}》的定时发布？`,
    onOk: async () => {
      await store.cancelScheduledTask(record.id);
      message.success('定时发布已取消');
    },
  });
};

const refresh = () => store.fetchScheduledTasks();

onMounted(() => store.fetchScheduledTasks());
</script>

<style lang="less" scoped>
.scheduled-publish {
  .scheduled-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; font-weight: 600; }
}
</style>
```

### Step 5.2: 提交

```bash
git add jeecgboot-vue3/src/views/channel/publish/ScheduledPublish.vue
git commit -m "feat(channel): add ScheduledPublish management component"
```

---

## Task 6: 发布权限配置 PublishPermission

**Files:**
- Create: `jeecgboot-vue3/src/views/channel/settings/PublishPermission.vue`
- Test: `jeecgboot-vue3/src/views/channel/__tests__/PublishPermission.test.ts`

### Step 6.1: 编写 PublishPermission 测试

创建 `jeecgboot-vue3/src/views/channel/__tests__/PublishPermission.test.ts`:

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import PublishPermission from '../settings/PublishPermission.vue';

describe('PublishPermission', () => {
  beforeEach(() => { setActivePinia(createPinia()); });

  it('应展示四种权限模型选项', () => {
    const wrapper = mount(PublishPermission, { props: { channelId: '1' } });
    expect(wrapper.text()).toContain('仅管理员可发布');
    expect(wrapper.text()).toContain('所有成员可发布');
    expect(wrapper.text()).toContain('公开投稿');
    expect(wrapper.text()).toContain('先审后发');
  });

  it('应展示限额配置表单', () => {
    const wrapper = mount(PublishPermission, { props: { channelId: '1' } });
    expect(wrapper.text()).toContain('每小时发布上限');
    expect(wrapper.text()).toContain('每日发布上限');
    expect(wrapper.text()).toContain('内容字数下限');
  });

  it('切换权限模型应弹出影响说明', async () => {
    const wrapper = mount(PublishPermission, { props: { channelId: '1' } });
    const radio = wrapper.find('input[value="open_submission"]');
    await radio.trigger('change');
    expect(wrapper.find('.impact-description').exists()).toBe(true);
  });
});
```

### Step 6.2: 运行测试验证失败

```bash
cd jeecgboot-vue3 && npx vitest run src/views/channel/__tests__/PublishPermission.test.ts
```

### Step 6.3: 实现 PublishPermission 组件

创建 `jeecgboot-vue3/src/views/channel/settings/PublishPermission.vue`:

```vue
<template>
  <div class="publish-permission">
    <Card title="发布权限配置">
      <Form :model="formData" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <Form.Item label="发布权限模型" name="publishModel" required>
          <RadioGroup v-model:value="formData.publishModel" @change="handleModelChange">
            <Space direction="vertical">
              <Radio value="admin_only">
                <div>仅管理员可发布</div>
                <div class="radio-desc">频道主、管理员和内容编辑可发布</div>
              </Radio>
              <Radio value="all_members">
                <div>所有成员可发布</div>
                <div class="radio-desc">频道成员可直接发布</div>
              </Radio>
              <Radio value="open_submission">
                <div>公开投稿</div>
                <div class="radio-desc">非成员可投稿，需审核通过后展示</div>
              </Radio>
              <Radio value="review_first">
                <div>先审后发</div>
                <div class="radio-desc">所有内容均需审核通过后展示</div>
              </Radio>
            </Space>
          </RadioGroup>
        </Form.Item>
        <Form.Item v-if="impactText" class="impact-description">
          <Alert :message="impactText" type="info" show-icon />
        </Form.Item>
        <Divider />
        <Form.Item label="每小时发布上限" name="hourlyLimit">
          <InputNumber v-model:value="formData.hourlyLimit" :min="0" placeholder="0 表示不限制" style="width: 200px" />
        </Form.Item>
        <Form.Item label="每日发布上限" name="dailyLimit">
          <InputNumber v-model:value="formData.dailyLimit" :min="0" placeholder="0 表示不限制" style="width: 200px" />
        </Form.Item>
        <Form.Item label="内容字数下限" name="minWordCount">
          <InputNumber v-model:value="formData.minWordCount" :min="0" placeholder="0 表示不限制" style="width: 200px" />
        </Form.Item>
        <Form.Item :wrapper-col="{ offset: 6, span: 16 }">
          <Space>
            <Button type="primary" :loading="saving" @click="handleSave">保存</Button>
            <Button @click="handleCancel">取消</Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { Card, Form, RadioGroup, Radio, Space, InputNumber, Divider, Button, Alert, Modal, message } from 'ant-design-vue';
import { defHttp } from '/@/utils/http/axios';

const props = defineProps<{ channelId: string }>();

const impactText = ref('');
const saving = ref(false);
const formData = reactive({
  publishModel: 'all_members',
  hourlyLimit: 0,
  dailyLimit: 0,
  minWordCount: 0,
});

const impactMap: Record<string, string> = {
  admin_only: '切换后，普通成员和非成员将无法在本频道发布内容，仅频道主、管理员和内容编辑可发布。',
  all_members: '切换后，频道成员可直接发布内容，无需审核。',
  open_submission: '切换后，非成员也可投稿，但需审核通过后才会在频道中展示。',
  review_first: '切换后，所有内容（包括管理员发布的）均需审核通过后才会展示。',
};

const handleModelChange = (e: any) => {
  impactText.value = impactMap[e.target.value] || '';
};

const handleSave = () => {
  Modal.confirm({
    title: '确认保存',
    content: `确认将发布权限模型切换为"${impactMap[formData.publishModel]}"？`,
    onOk: async () => {
      saving.value = true;
      try {
        await defHttp.post({ url: '/api/channel/publish/permission', data: { channelId: props.channelId, ...formData } });
        message.success('发布权限配置已保存');
      } finally {
        saving.value = false;
      }
    },
  });
};

const handleCancel = () => {
  // 重置表单
};

onMounted(async () => {
  const res = await defHttp.get({ url: `/api/channel/publish/permission/${props.channelId}` });
  if (res) Object.assign(formData, res);
});
</script>

<style lang="less" scoped>
.publish-permission {
  .radio-desc { color: #999; font-size: 12px; margin-top: 2px; }
}
</style>
```

### Step 6.4: 运行测试验证通过

```bash
cd jeecgboot-vue3 && npx vitest run src/views/channel/__tests__/PublishPermission.test.ts
```

### Step 6.5: 提交

```bash
git add jeecgboot-vue3/src/views/channel/settings/PublishPermission.vue \
        jeecgboot-vue3/src/views/channel/__tests__/PublishPermission.test.ts
git commit -m "feat(channel): add PublishPermission config page with four models and limit settings"
```

---

## Task 7: 拒绝原因弹窗 RejectReasonModal

**Files:**
- Create: `jeecgboot-vue3/src/views/channel/components/RejectReasonModal.vue`
- Test: `jeecgboot-vue3/src/views/channel/__tests__/RejectReasonModal.test.ts`

### Step 7.1: 编写 RejectReasonModal 测试

创建 `jeecgboot-vue3/src/views/channel/__tests__/RejectReasonModal.test.ts`:

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import RejectReasonModal from '../components/RejectReasonModal.vue';

describe('RejectReasonModal', () => {
  beforeEach(() => { setActivePinia(createPinia()); });

  it('应展示预设原因标签', () => {
    const wrapper = mount(RejectReasonModal, { props: { visible: true } });
    expect(wrapper.text()).toContain('违反社区规范');
    expect(wrapper.text()).toContain('内容重复');
  });

  it('拒绝原因不足10字应校验失败', async () => {
    const wrapper = mount(RejectReasonModal, { props: { visible: true } });
    const textarea = wrapper.find('textarea');
    await textarea.setValue('太短');
    await wrapper.find('.confirm-btn').trigger('click');
    expect(wrapper.text()).toContain('拒绝原因至少需要10个字');
  });

  it('点击预设原因应自动填充', async () => {
    const wrapper = mount(RejectReasonModal, { props: { visible: true } });
    await wrapper.find('.preset-tag').trigger('click');
    const textarea = wrapper.find('textarea');
    expect((textarea.element as HTMLTextAreaElement).value).toContain('违反社区规范');
  });
});
```

### Step 7.2: 运行测试验证失败

```bash
cd jeecgboot-vue3 && npx vitest run src/views/channel/__tests__/RejectReasonModal.test.ts
```

### Step 7.3: 实现 RejectReasonModal 组件

创建 `jeecgboot-vue3/src/views/channel/components/RejectReasonModal.vue`:

```vue
<template>
  <Modal v-model:visible="visible" title="拒绝原因" @cancel="handleClose" :width="480">
    <div class="reject-reason-modal">
      <div class="preset-reasons">
        <div class="preset-label">快捷选择：</div>
        <Space wrap>
          <Tag v-for="reason in presetReasons" :key="reason" class="preset-tag" @click="handlePresetClick(reason)">
            {{ reason }}
          </Tag>
        </Space>
      </div>
      <div class="custom-reason">
        <div class="reason-label">拒绝原因（必填，至少10字）：</div>
        <Input.TextArea
          v-model:value="reason"
          :rows="4"
          placeholder="请输入拒绝原因..."
          :status="showError ? 'error' : ''"
        />
        <div v-if="showError" class="error-text">拒绝原因至少需要10个字</div>
      </div>
    </div>
    <template #footer>
      <Button @click="handleClose">取消</Button>
      <Button class="confirm-btn" type="primary" danger :loading="submitting" @click="handleConfirm">
        确认拒绝
      </Button>
    </template>
  </Modal>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';
import { Modal, Input, Tag, Space, Button } from 'ant-design-vue';

const props = defineProps<{ visible: boolean }>();
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'confirm', reason: string): void;
}>();

const reason = ref('');
const showError = ref(false);
const submitting = ref(false);

const presetReasons = ['违反社区规范', '内容重复', '与频道主题不符', '低质量内容', '涉嫌广告'];

const handlePresetClick = (preset: string) => {
  reason.value = preset;
  showError.value = false;
};

const handleConfirm = () => {
  if (reason.value.length < 10) {
    showError.value = true;
    return;
  }
  emit('confirm', reason.value);
};

const handleClose = () => {
  emit('update:visible', false);
  reason.value = '';
  showError.value = false;
};
</script>

<style lang="less" scoped>
.reject-reason-modal {
  .preset-reasons { margin-bottom: 16px;
    .preset-label { margin-bottom: 8px; color: #666; }
    .preset-tag { cursor: pointer; }
  }
  .custom-reason {
    .reason-label { margin-bottom: 8px; color: #666; }
    .error-text { color: #ff4d4f; font-size: 12px; margin-top: 4px; }
  }
}
</style>
```

### Step 7.4: 运行测试验证通过

```bash
cd jeecgboot-vue3 && npx vitest run src/views/channel/__tests__/RejectReasonModal.test.ts
```

### Step 7.5: 提交

```bash
git add jeecgboot-vue3/src/views/channel/components/RejectReasonModal.vue \
        jeecgboot-vue3/src/views/channel/__tests__/RejectReasonModal.test.ts
git commit -m "feat(channel): add RejectReasonModal with preset reasons and 10-char validation"
```

---

## Task 8: 待审区管理页 ReviewQueue

**Files:**
- Create: `jeecgboot-vue3/src/views/channel/governance/ReviewQueue.vue`
- Test: `jeecgboot-vue3/src/views/channel/__tests__/ReviewQueue.test.ts`

### Step 8.1: 编写 ReviewQueue 测试

创建 `jeecgboot-vue3/src/views/channel/__tests__/ReviewQueue.test.ts`:

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import ReviewQueue from '../governance/ReviewQueue.vue';

vi.mock('/@/api/content/channel/review', () => ({
  getReviewList: vi.fn().mockResolvedValue({
    records: [
      { id: '1', title: '测试文章', contentType: 'article', submitter: '张三', submitTime: '2026-06-01 10:00', sourceScene: '公开投稿', hitRule: '先审后发规则', isTimeout: false },
    ],
    total: 1,
  }),
  getReviewStats: vi.fn().mockResolvedValue({ total: 5, timeoutCount: 2 }),
  approveReview: vi.fn().mockResolvedValue({}),
  rejectReview: vi.fn().mockResolvedValue({}),
}));

describe('ReviewQueue', () => {
  beforeEach(() => { setActivePinia(createPinia()); });

  it('应加载待审列表', async () => {
    const wrapper = mount(ReviewQueue, { props: { channelId: '1' } });
    await vi.dynamicImportSettled();
    expect(wrapper.text()).toContain('测试文章');
    expect(wrapper.text()).toContain('张三');
  });

  it('点击拒绝应弹出原因弹窗', async () => {
    const wrapper = mount(ReviewQueue, { props: { channelId: '1' } });
    await vi.dynamicImportSettled();
    await wrapper.find('.reject-btn').trigger('click');
    expect(wrapper.findComponent({ name: 'RejectReasonModal' }).exists()).toBe(true);
  });

  it('超时内容应有高亮标识', async () => {
    const wrapper = mount(ReviewQueue, { props: { channelId: '1' } });
    await vi.dynamicImportSettled();
    // 超时内容应有特殊样式类
    expect(wrapper.find('.timeout-row').exists()).toBe(false); // 测试数据中无超时
  });
});
```

### Step 8.2: 运行测试验证失败

```bash
cd jeecgboot-vue3 && npx vitest run src/views/channel/__tests__/ReviewQueue.test.ts
```

### Step 8.3: 实现 ReviewQueue 组件

创建 `jeecgboot-vue3/src/views/channel/governance/ReviewQueue.vue`:

```vue
<template>
  <div class="review-queue">
    <div class="queue-header">
      <span>待审区</span>
      <Badge :count="stats.timeoutCount" :overflow-count="99">
        <Tag color="red" v-if="stats.timeoutCount > 0">超时 {{ stats.timeoutCount }}</Tag>
      </Badge>
    </div>
    <div class="filter-bar">
      <Space>
        <Select v-model:value="filter.contentType" placeholder="内容类型" allowClear style="width: 120px">
          <Select.Option value="article">文章</Select.Option>
          <Select.Option value="post">图文帖子</Select.Option>
          <Select.Option value="video">视频</Select.Option>
          <Select.Option value="note">笔记</Select.Option>
          <Select.Option value="question">问答问题</Select.Option>
        </Select>
        <Input v-model:value="filter.keyword" placeholder="搜索" style="width: 200px" allowClear />
      </Space>
    </div>
    <Table
      :dataSource="reviewList"
      :columns="columns"
      :loading="loading"
      :rowSelection="{ selectedRowKeys: selectedIds, onChange: onSelectChange }"
      rowKey="id"
      :row-class-name="rowClassName"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <Space>
            <Button size="small" type="primary" @click="handleApprove(record.id)">通过</Button>
            <Button size="small" danger class="reject-btn" @click="handleReject(record.id)">拒绝</Button>
          </Space>
        </template>
      </template>
    </Table>
    <div class="batch-bar" v-if="selectedIds.length > 0">
      <Space>
        <span>已选 {{ selectedIds.length }} 条</span>
        <Button type="primary" @click="handleBatchApprove">批量通过</Button>
        <Button danger @click="handleBatchReject">批量拒绝</Button>
      </Space>
    </div>
    <RejectReasonModal v-model:visible="rejectVisible" @confirm="handleRejectConfirm" />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import { Table, Button, Space, Select, Input, Tag, Badge, Modal, message } from 'ant-design-vue';
import { useChannelReviewStore } from '/@/store/modules/channelReview';
import RejectReasonModal from '../components/RejectReasonModal.vue';
import { storeToRefs } from 'pinia';

const props = defineProps<{ channelId: string }>();
const store = useChannelReviewStore();
const { reviewList, selectedIds, stats, loading } = storeToRefs(store);

const filter = reactive({ contentType: undefined as string | undefined, keyword: '' });
const rejectVisible = ref(false);
const rejectingIds = ref<string[]>([]);
let statsTimer: ReturnType<typeof setInterval>;

const columns = [
  { title: '标题', dataIndex: 'title', key: 'title' },
  { title: '类型', dataIndex: 'contentType', key: 'contentType', width: 80 },
  { title: '提交者', dataIndex: 'submitter', key: 'submitter', width: 100 },
  { title: '提交时间', dataIndex: 'submitTime', key: 'submitTime', width: 160 },
  { title: '来源场景', dataIndex: 'sourceScene', key: 'sourceScene', width: 100 },
  { title: '命中规则', dataIndex: 'hitRule', key: 'hitRule', width: 120 },
  { title: '操作', key: 'action', width: 140 },
];

const rowClassName = (record: any) => record.isTimeout ? 'timeout-row' : '';

const onSelectChange = (keys: string[]) => store.setSelectedIds(keys);

const handleApprove = async (id: string) => {
  await store.approve([id]);
  message.success('审核通过');
};

const handleReject = (id: string) => {
  rejectingIds.value = [id];
  rejectVisible.value = true;
};

const handleRejectConfirm = async (reason: string) => {
  await store.reject(rejectingIds.value, reason);
  rejectVisible.value = false;
  message.success('已拒绝并通知提交者');
};

const handleBatchApprove = () => {
  Modal.confirm({
    title: '批量通过',
    content: `确认通过选中的 ${selectedIds.value.length} 条内容？通过后内容将在频道中展示。`,
    onOk: async () => { await store.batchApprove(); message.success('批量通过完成'); },
  });
};

const handleBatchReject = () => {
  rejectingIds.value = [...selectedIds.value];
  rejectVisible.value = true;
};

onMounted(() => {
  store.setFilter({ channelId: props.channelId });
  store.fetchList();
  store.fetchStats(props.channelId);
  statsTimer = setInterval(() => store.fetchStats(props.channelId), 60000);
});

onUnmounted(() => clearInterval(statsTimer));
</script>

<style lang="less" scoped>
.review-queue {
  .queue-header { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; font-weight: 600; }
  .filter-bar { margin-bottom: 12px; }
  .batch-bar { position: sticky; bottom: 0; background: #fff; padding: 12px; border-top: 1px solid #e8e8e8; display: flex; justify-content: flex-end; }
  :deep(.timeout-row) { background: #fff2f0; }
}
</style>
```

### Step 8.4: 运行测试验证通过

```bash
cd jeecgboot-vue3 && npx vitest run src/views/channel/__tests__/ReviewQueue.test.ts
```

### Step 8.5: 提交

```bash
git add jeecgboot-vue3/src/views/channel/governance/ReviewQueue.vue \
        jeecgboot-vue3/src/views/channel/__tests__/ReviewQueue.test.ts
git commit -m "feat(channel): add ReviewQueue page with approve/reject, batch ops and timeout highlight"
```

---

## Task 9: 治理操作菜单与弹窗组件

**Files:**
- Create: `jeecgboot-vue3/src/views/channel/components/GovernanceActionMenu.vue`
- Create: `jeecgboot-vue3/src/views/channel/components/MoveChannelDialog.vue`
- Create: `jeecgboot-vue3/src/views/channel/components/EditAssistDrawer.vue`

### Step 9.1: 实现 GovernanceActionMenu

创建 `jeecgboot-vue3/src/views/channel/components/GovernanceActionMenu.vue`:

```vue
<template>
  <Dropdown :trigger="['click']">
    <Button size="small">更多</Button>
    <template #overlay>
      <Menu @click="handleMenuClick">
        <Menu.Item key="pin" v-if="!isPinned">置顶</Menu.Item>
        <Menu.Item key="unpin" v-if="isPinned">取消置顶</Menu.Item>
        <Menu.Item key="feature" v-if="!isFeatured">标记精华</Menu.Item>
        <Menu.Item key="unfeature" v-if="isFeatured">取消精华</Menu.Item>
        <Menu.Item key="move">移出频道</Menu.Item>
        <Menu.Item key="editAssist">编辑协助</Menu.Item>
        <Menu.Item key="delete" danger>删除</Menu.Item>
      </Menu>
    </template>
  </Dropdown>
</template>

<script lang="ts" setup>
import { Dropdown, Button, Menu } from 'ant-design-vue';

defineProps<{
  isPinned: boolean;
  isFeatured: boolean;
}>();

const emit = defineEmits<{
  (e: 'action', action: string): void;
}>();

const handleMenuClick = ({ key }: { key: string }) => emit('action', key);
</script>
```

### Step 9.2: 实现 MoveChannelDialog

创建 `jeecgboot-vue3/src/views/channel/components/MoveChannelDialog.vue`:

```vue
<template>
  <Modal v-model:visible="visible" title="移出频道" :width="560" @cancel="handleClose">
    <div class="move-channel-dialog">
      <div class="section-label">选择目标频道：</div>
      <Select v-model:value="targetChannelId" placeholder="选择目标频道" style="width: 100%" :options="channelOptions" />
      <div v-if="expectedResult" class="expected-result">
        <InfoCircleOutlined /> 预期结果：{{ expectedResult }}
      </div>
    </div>
    <template #footer>
      <Button @click="handleClose">取消</Button>
      <Button type="primary" :disabled="!targetChannelId" :loading="submitting" @click="handleConfirm">确认移出</Button>
    </template>
  </Modal>
</template>

<script lang="ts" setup>
import { ref, computed, watch } from 'vue';
import { Modal, Select, Button, message } from 'ant-design-vue';
import { InfoCircleOutlined } from '@ant-design/icons-vue';
import { useChannelGovernanceStore } from '/@/store/modules/channelGovernance';

const props = defineProps<{
  visible: boolean;
  contentId: string;
  sourceChannelId: string;
}>();

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'moved'): void;
}>();

const store = useChannelGovernanceStore();
const targetChannelId = ref<string | undefined>(undefined);
const submitting = ref(false);
const channelOptions = ref<{ label: string; value: string; publishResult: string }[]>([]);

const expectedResult = computed(() => {
  const ch = channelOptions.value.find((c) => c.value === targetChannelId.value);
  if (!ch) return '';
  return ch.publishResult === 'direct' ? '将直接展示' : '将进入目标频道待审区';
});

const handleConfirm = async () => {
  if (!targetChannelId.value) return;
  submitting.value = true;
  try {
    await store.moveContent(props.contentId, props.sourceChannelId, targetChannelId.value);
    message.success('已移出');
    emit('moved');
    emit('update:visible', false);
  } finally {
    submitting.value = false;
  }
};

const handleClose = () => emit('update:visible', false);
</script>

<style lang="less" scoped>
.move-channel-dialog {
  .section-label { margin-bottom: 8px; color: #666; }
  .expected-result { margin-top: 12px; color: #1890ff; }
}
</style>
```

### Step 9.3: 实现 EditAssistDrawer

创建 `jeecgboot-vue3/src/views/channel/components/EditAssistDrawer.vue`:

```vue
<template>
  <Drawer v-model:visible="visible" title="编辑协助" :width="480" @close="handleClose">
    <div class="edit-assist-drawer">
      <div class="author-info">
        <div>原作者：{{ content.author }}</div>
        <div>原标题：{{ content.title }}</div>
      </div>
      <Form :model="formData" layout="vertical">
        <Form.Item label="标题" name="title">
          <Input v-model:value="formData.title" />
        </Form.Item>
        <Form.Item label="标签" name="tags">
          <Select v-model:value="formData.tags" mode="tags" placeholder="输入标签" />
        </Form.Item>
        <Form.Item label="摘要" name="summary">
          <Input.TextArea v-model:value="formData.summary" :rows="3" />
        </Form.Item>
        <Form.Item label="修改原因（必填）" name="reason" required>
          <Input.TextArea v-model:value="formData.reason" :rows="2" placeholder="请说明修改原因" />
        </Form.Item>
      </Form>
      <Divider />
      <div class="history-section">
        <div class="section-title">修订历史</div>
        <Timeline v-if="history.length > 0">
          <Timeline.Item v-for="item in history" :key="item.id">
            <div>{{ item.operator }} 修改了 {{ item.field }}</div>
            <div class="history-time">{{ item.time }}</div>
            <div class="history-reason">{{ item.reason }}</div>
          </Timeline.Item>
        </Timeline>
        <Empty v-else description="暂无修订记录" />
      </div>
    </div>
    <template #footer>
      <Space>
        <Button @click="handleClose">取消</Button>
        <Button type="primary" :loading="saving" @click="handleSave">保存</Button>
      </Space>
    </template>
  </Drawer>
</template>

<script lang="ts" setup>
import { ref, reactive, watch } from 'vue';
import { Drawer, Form, Input, Select, Button, Space, Divider, Timeline, Empty, message } from 'ant-design-vue';
import { useChannelGovernanceStore } from '/@/store/modules/channelGovernance';
import { getEditAssistHistory } from '/@/api/content/channel/governance';

const props = defineProps<{
  visible: boolean;
  contentId: string;
  channelId: string;
  content: { title: string; author: string };
}>();

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'saved'): void;
}>();

const store = useChannelGovernanceStore();
const saving = ref(false);
const history = ref<any[]>([]);
const formData = reactive({
  title: '',
  tags: [] as string[],
  summary: '',
  reason: '',
});

watch(() => props.visible, async (val) => {
  if (val && props.contentId) {
    formData.title = props.content.title;
    const res = await getEditAssistHistory(props.contentId);
    history.value = res || [];
  }
});

const handleSave = async () => {
  if (!formData.reason) { message.warning('请填写修改原因'); return; }
  saving.value = true;
  try {
    await store.editAssist({ contentId: props.contentId, channelId: props.channelId, title: formData.title, tags: formData.tags, summary: formData.summary, reason: formData.reason });
    message.success('编辑协助已保存');
    emit('saved');
    emit('update:visible', false);
  } finally {
    saving.value = false;
  }
};

const handleClose = () => emit('update:visible', false);
</script>

<style lang="less" scoped>
.edit-assist-drawer {
  .author-info { background: #f5f5f5; padding: 12px; border-radius: 6px; margin-bottom: 16px; }
  .history-section { .section-title { font-weight: 600; margin-bottom: 12px; } }
  .history-time { color: #999; font-size: 12px; }
  .history-reason { color: #666; font-size: 12px; }
}
</style>
```

### Step 9.4: 提交

```bash
git add jeecgboot-vue3/src/views/channel/components/GovernanceActionMenu.vue \
        jeecgboot-vue3/src/views/channel/components/MoveChannelDialog.vue \
        jeecgboot-vue3/src/views/channel/components/EditAssistDrawer.vue
git commit -m "feat(channel): add GovernanceActionMenu, MoveChannelDialog and EditAssistDrawer components"
```

---

## Task 10: 内容治理页 ContentManage

**Files:**
- Create: `jeecgboot-vue3/src/views/channel/governance/ContentManage.vue`
- Test: `jeecgboot-vue3/src/views/channel/__tests__/ContentManage.test.ts`

### Step 10.1: 编写 ContentManage 测试

创建 `jeecgboot-vue3/src/views/channel/__tests__/ContentManage.test.ts`:

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import ContentManage from '../governance/ContentManage.vue';

vi.mock('/@/api/content/channel/governance', () => ({
  getGovernanceContentList: vi.fn().mockResolvedValue({
    records: [
      { id: '1', title: 'Vue3最佳实践', contentType: 'article', author: '王五', publishTime: '2026-06-01 10:00', status: 'published', isPinned: true, isFeatured: false },
    ],
    total: 1,
  }),
  togglePin: vi.fn().mockResolvedValue({}),
  toggleFeature: vi.fn().mockResolvedValue({}),
  deleteContent: vi.fn().mockResolvedValue({}),
}));

describe('ContentManage', () => {
  beforeEach(() => { setActivePinia(createPinia()); });

  it('应加载内容列表', async () => {
    const wrapper = mount(ContentManage, { props: { channelId: '1' } });
    await vi.dynamicImportSettled();
    expect(wrapper.text()).toContain('Vue3最佳实践');
    expect(wrapper.text()).toContain('王五');
  });

  it('应展示置顶标识', async () => {
    const wrapper = mount(ContentManage, { props: { channelId: '1' } });
    await vi.dynamicImportSettled();
    expect(wrapper.text()).toContain('置顶');
  });
});
```

### Step 10.2: 运行测试验证失败

```bash
cd jeecgboot-vue3 && npx vitest run src/views/channel/__tests__/ContentManage.test.ts
```

### Step 10.3: 实现 ContentManage 组件

创建 `jeecgboot-vue3/src/views/channel/governance/ContentManage.vue`:

```vue
<template>
  <div class="content-manage">
    <div class="manage-header">
      <span>内容管理</span>
      <Space>
        <Button size="small" @click="$emit('goRecycleBin')">回收站</Button>
        <Button size="small" @click="$emit('goLog')">日志</Button>
      </Space>
    </div>
    <div class="filter-bar">
      <Space wrap>
        <Input v-model:value="filter.keyword" placeholder="搜索" style="width: 200px" allowClear />
        <Select v-model:value="filter.contentType" placeholder="内容类型" allowClear style="width: 120px">
          <Select.Option value="article">文章</Select.Option>
          <Select.Option value="post">图文帖子</Select.Option>
          <Select.Option value="video">视频</Select.Option>
          <Select.Option value="note">笔记</Select.Option>
        </Select>
        <Select v-model:value="filter.status" placeholder="状态" allowClear style="width: 120px">
          <Select.Option value="published">已发布</Select.Option>
          <Select.Option value="pinned">已置顶</Select.Option>
          <Select.Option value="featured">精华</Select.Option>
        </Select>
        <Select v-model:value="filter.sortBy" placeholder="排序" style="width: 140px">
          <Select.Option value="latest">最新发布</Select.Option>
          <Select.Option value="likes">最多点赞</Select.Option>
        </Select>
      </Space>
    </div>
    <Table
      :dataSource="contentList"
      :columns="columns"
      :loading="loading"
      :rowSelection="{ selectedRowKeys: selectedIds, onChange: onSelectChange }"
      rowKey="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <Tag v-if="record.isPinned" color="blue">置顶</Tag>
          <Tag v-if="record.isFeatured" color="gold">精华</Tag>
        </template>
        <template v-if="column.key === 'action'">
          <GovernanceActionMenu
            :is-pinned="record.isPinned"
            :is-featured="record.isFeatured"
            @action="(action) => handleAction(action, record)"
          />
        </template>
      </template>
    </Table>
    <div class="batch-bar" v-if="selectedIds.length > 0">
      <Space>
        <span>已选 {{ selectedIds.length }} 条</span>
        <Button danger @click="handleBatchDelete">批量删除</Button>
        <Button @click="handleBatchPin">批量置顶</Button>
        <Button @click="handleBatchFeature">批量精华</Button>
      </Space>
    </div>
    <MoveChannelDialog v-model:visible="moveVisible" :content-id="movingContentId" :source-channel-id="channelId" @moved="refresh" />
    <EditAssistDrawer v-model:visible="editVisible" :content-id="editingContentId" :channel-id="channelId" :content="editingContent" @saved="refresh" />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { Table, Button, Space, Select, Input, Tag, Modal, message } from 'ant-design-vue';
import { useChannelGovernanceStore } from '/@/store/modules/channelGovernance';
import { storeToRefs } from 'pinia';
import GovernanceActionMenu from '../components/GovernanceActionMenu.vue';
import MoveChannelDialog from '../components/MoveChannelDialog.vue';
import EditAssistDrawer from '../components/EditAssistDrawer.vue';

const props = defineProps<{ channelId: string }>();
defineEmits(['goRecycleBin', 'goLog']);

const store = useChannelGovernanceStore();
const { contentList, loading } = storeToRefs(store);

const filter = reactive({ keyword: '', contentType: undefined as string | undefined, status: undefined as string | undefined, sortBy: 'latest' });
const selectedIds = ref<string[]>([]);
const moveVisible = ref(false);
const movingContentId = ref('');
const editVisible = ref(false);
const editingContentId = ref('');
const editingContent = ref({ title: '', author: '' });

const columns = [
  { title: '标题', dataIndex: 'title', key: 'title' },
  { title: '类型', dataIndex: 'contentType', key: 'contentType', width: 80 },
  { title: '作者', dataIndex: 'author', key: 'author', width: 100 },
  { title: '发布时间', dataIndex: 'publishTime', key: 'publishTime', width: 160 },
  { title: '状态', key: 'status', width: 120 },
  { title: '操作', key: 'action', width: 80 },
];

const onSelectChange = (keys: string[]) => { selectedIds.value = keys; };

const handleAction = async (action: string, record: any) => {
  switch (action) {
    case 'pin': await store.pin(record.id, props.channelId, true); message.success('已置顶'); break;
    case 'unpin': await store.pin(record.id, props.channelId, false); message.success('已取消置顶'); break;
    case 'feature': await store.feature(record.id, props.channelId, true); message.success('已标记精华'); break;
    case 'unfeature': await store.feature(record.id, props.channelId, false); message.success('已取消精华'); break;
    case 'move': movingContentId.value = record.id; moveVisible.value = true; break;
    case 'editAssist': editingContentId.value = record.id; editingContent.value = { title: record.title, author: record.author }; editVisible.value = true; break;
    case 'delete':
      Modal.confirm({
        title: '确认删除',
        content: `确认将《${record.title}》从频道删除？内容将进入回收站，30天内可恢复。`,
        onOk: async () => { await store.deleteContent([record.id], props.channelId); message.success('已删除并移入回收站'); },
      });
      break;
  }
};

const handleBatchDelete = () => {
  Modal.confirm({
    title: '批量删除',
    content: `确认删除选中的 ${selectedIds.value.length} 条内容？`,
    onOk: async () => { await store.deleteContent(selectedIds.value, props.channelId); selectedIds.value = []; message.success('批量删除完成'); },
  });
};

const handleBatchPin = async () => {
  for (const id of selectedIds.value) await store.pin(id, props.channelId, true);
  selectedIds.value = [];
  message.success('批量置顶完成');
};

const handleBatchFeature = async () => {
  for (const id of selectedIds.value) await store.feature(id, props.channelId, true);
  selectedIds.value = [];
  message.success('批量精华完成');
};

const refresh = () => store.fetchList();

onMounted(() => { store.setFilter({ channelId: props.channelId }); store.fetchList(); });
</script>

<style lang="less" scoped>
.content-manage {
  .manage-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; font-weight: 600; }
  .filter-bar { margin-bottom: 12px; }
  .batch-bar { position: sticky; bottom: 0; background: #fff; padding: 12px; border-top: 1px solid #e8e8e8; display: flex; justify-content: flex-end; }
}
</style>
```

### Step 10.4: 运行测试验证通过

```bash
cd jeecgboot-vue3 && npx vitest run src/views/channel/__tests__/ContentManage.test.ts
```

### Step 10.5: 提交

```bash
git add jeecgboot-vue3/src/views/channel/governance/ContentManage.vue \
        jeecgboot-vue3/src/views/channel/__tests__/ContentManage.test.ts
git commit -m "feat(channel): add ContentManage page with governance actions, batch ops and filters"
```

---

## Task 11: 回收站与治理日志

**Files:**
- Create: `jeecgboot-vue3/src/views/channel/governance/RecycleBin.vue`
- Create: `jeecgboot-vue3/src/views/channel/governance/GovernanceLog.vue`

### Step 11.1: 实现 RecycleBin 组件

创建 `jeecgboot-vue3/src/views/channel/governance/RecycleBin.vue`:

```vue
<template>
  <div class="recycle-bin">
    <div class="bin-header">
      <span>回收站</span>
      <Button size="small" @click="$emit('back')">返回管理</Button>
    </div>
    <div class="filter-bar">
      <Space>
        <Input v-model:value="filter.keyword" placeholder="搜索" style="width: 200px" allowClear />
        <Select v-model:value="filter.contentType" placeholder="内容类型" allowClear style="width: 120px" />
      </Space>
    </div>
    <Table
      :dataSource="recycleBinList"
      :columns="columns"
      :loading="loading"
      :rowSelection="{ selectedRowKeys: selectedIds, onChange: onSelectChange }"
      rowKey="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'remaining'">
          <span :class="{ expired: record.remainingDays <= 0 }">
            {{ record.remainingDays > 0 ? `${record.remainingDays}天` : '已过保留期' }}
          </span>
        </template>
        <template v-if="column.key === 'action'">
          <Button size="small" type="link" :disabled="record.remainingDays <= 0" @click="handleRestore(record.id)">
            恢复
          </Button>
        </template>
      </template>
    </Table>
    <div class="batch-bar" v-if="selectedIds.length > 0">
      <Space>
        <span>已选 {{ selectedIds.length }} 条</span>
        <Button type="primary" @click="handleBatchRestore">批量恢复</Button>
      </Space>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { Table, Button, Space, Select, Input, Modal, message } from 'ant-design-vue';
import { useChannelGovernanceStore } from '/@/store/modules/channelGovernance';
import { storeToRefs } from 'pinia';

const props = defineProps<{ channelId: string }>();
defineEmits(['back']);

const store = useChannelGovernanceStore();
const { recycleBinList, loading } = storeToRefs(store);

const filter = reactive({ keyword: '', contentType: undefined as string | undefined });
const selectedIds = ref<string[]>([]);

const columns = [
  { title: '标题', dataIndex: 'title', key: 'title' },
  { title: '类型', dataIndex: 'contentType', key: 'contentType', width: 80 },
  { title: '原作者', dataIndex: 'originalAuthor', key: 'originalAuthor', width: 100 },
  { title: '删除人', dataIndex: 'deletedBy', key: 'deletedBy', width: 100 },
  { title: '删除时间', dataIndex: 'deleteTime', key: 'deleteTime', width: 160 },
  { title: '删除原因', dataIndex: 'deleteReason', key: 'deleteReason' },
  { title: '剩余天数', key: 'remaining', width: 100 },
  { title: '操作', key: 'action', width: 80 },
];

const onSelectChange = (keys: string[]) => { selectedIds.value = keys; };

const handleRestore = (id: string) => {
  Modal.confirm({
    title: '确认恢复',
    content: '确认恢复此内容到频道？',
    onOk: async () => { await store.restore([id], props.channelId); message.success('已恢复'); },
  });
};

const handleBatchRestore = () => {
  Modal.confirm({
    title: '批量恢复',
    content: `确认恢复选中的 ${selectedIds.value.length} 条内容？`,
    onOk: async () => { await store.restore(selectedIds.value, props.channelId); selectedIds.value = []; message.success('批量恢复完成'); },
  });
};

onMounted(() => store.fetchRecycleBin(props.channelId));
</script>

<style lang="less" scoped>
.recycle-bin {
  .bin-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; font-weight: 600; }
  .filter-bar { margin-bottom: 12px; }
  .batch-bar { position: sticky; bottom: 0; background: #fff; padding: 12px; border-top: 1px solid #e8e8e8; display: flex; justify-content: flex-end; }
  .expired { color: #999; }
}
</style>
```

### Step 11.2: 实现 GovernanceLog 组件

创建 `jeecgboot-vue3/src/views/channel/governance/GovernanceLog.vue`:

```vue
<template>
  <div class="governance-log">
    <div class="log-header">治理日志</div>
    <div class="filter-bar">
      <Space>
        <Select v-model:value="filter.actionType" placeholder="操作类型" allowClear style="width: 140px">
          <Select.Option value="pin">置顶</Select.Option>
          <Select.Option value="unpin">取消置顶</Select.Option>
          <Select.Option value="feature">标记精华</Select.Option>
          <Select.Option value="delete">删除</Select.Option>
          <Select.Option value="restore">恢复</Select.Option>
          <Select.Option value="move">移出</Select.Option>
          <Select.Option value="edit_assist">编辑协助</Select.Option>
          <Select.Option value="announcement">公告变更</Select.Option>
        </Select>
        <Input v-model:value="filter.keyword" placeholder="搜索内容" style="width: 200px" allowClear />
      </Space>
    </div>
    <Table :dataSource="governanceLogList" :columns="columns" :loading="loading" rowKey="id">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'target'">
          <a @click="handleViewContent(record.contentId)">{{ record.targetTitle }}</a>
        </template>
      </template>
    </Table>
  </div>
</template>

<script lang="ts" setup>
import { reactive, onMounted } from 'vue';
import { Table, Space, Select, Input } from 'ant-design-vue';
import { useChannelGovernanceStore } from '/@/store/modules/channelGovernance';
import { storeToRefs } from 'pinia';
import { useRouter } from 'vue-router';

const props = defineProps<{ channelId: string }>();
const router = useRouter();
const store = useChannelGovernanceStore();
const { governanceLogList, loading } = storeToRefs(store);

const filter = reactive({ actionType: undefined as string | undefined, keyword: '' });

const columns = [
  { title: '时间', dataIndex: 'time', key: 'time', width: 160 },
  { title: '操作者', dataIndex: 'operator', key: 'operator', width: 100 },
  { title: '操作类型', dataIndex: 'actionType', key: 'actionType', width: 100 },
  { title: '操作对象', key: 'target' },
  { title: '结果', dataIndex: 'result', key: 'result', width: 80 },
  { title: '原因/备注', dataIndex: 'remark', key: 'remark' },
];

const handleViewContent = (contentId: string) => {
  router.push({ path: `/content/detail/${contentId}` });
};

onMounted(() => store.fetchGovernanceLog(props.channelId));
</script>

<style lang="less" scoped>
.governance-log {
  .log-header { font-weight: 600; margin-bottom: 12px; }
  .filter-bar { margin-bottom: 12px; }
}
</style>
```

### Step 11.3: 提交

```bash
git add jeecgboot-vue3/src/views/channel/governance/RecycleBin.vue \
        jeecgboot-vue3/src/views/channel/governance/GovernanceLog.vue
git commit -m "feat(channel): add RecycleBin and GovernanceLog pages"
```

---

## Task 12: 公告管理 AnnouncementManage

**Files:**
- Create: `jeecgboot-vue3/src/views/channel/governance/AnnouncementManage.vue`
- Test: `jeecgboot-vue3/src/views/channel/__tests__/AnnouncementManage.test.ts`

### Step 12.1: 实现 AnnouncementManage 组件

创建 `jeecgboot-vue3/src/views/channel/governance/AnnouncementManage.vue`:

```vue
<template>
  <div class="announcement-manage">
    <div class="announce-header">频道公告</div>
    <div class="announce-status">
      当前公告状态：<Tag :color="announcement ? 'green' : 'default'">{{ announcement ? '已发布' : '未发布' }}</Tag>
    </div>
    <Form layout="vertical">
      <Form.Item label="公告内容">
        <Tinymce v-model:value="content" :height="300" />
      </Form.Item>
    </Form>
    <div class="preview-section" v-if="previewContent">
      <div class="preview-title">公告预览</div>
      <div class="preview-content" v-html="previewContent"></div>
    </div>
    <div class="announce-actions">
      <Space>
        <Button @click="handlePreview">预览</Button>
        <Button @click="handleSaveDraft">保存草稿</Button>
        <Button type="primary" @click="handlePublish">发布公告</Button>
        <Button danger v-if="announcement" @click="handleDelete">删除公告</Button>
      </Space>
    </div>
    <Divider />
    <div class="history-section">
      <div class="section-title">公告历史</div>
      <Table :dataSource="historyList" :columns="historyColumns" :loading="historyLoading" rowKey="id" size="small">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <Button size="small" type="link" @click="handleRestoreVersion(record.id)">恢复此版本</Button>
          </template>
        </template>
      </Table>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { Form, Button, Space, Tag, Divider, Table, Modal, message } from 'ant-design-vue';
import Tinymce from '/@/components/Tinymce/index.vue';
import {
  getAnnouncement,
  saveAnnouncement,
  deleteAnnouncement,
  previewAnnouncement,
  getAnnouncementHistory,
  restoreAnnouncementVersion,
} from '/@/api/content/channel/announcement';

const props = defineProps<{ channelId: string }>();

const content = ref('');
const announcement = ref<any>(null);
const previewContent = ref('');
const historyList = ref<any[]>([]);
const historyLoading = ref(false);

const historyColumns = [
  { title: '版本号', dataIndex: 'version', key: 'version', width: 80 },
  { title: '修改人', dataIndex: 'modifier', key: 'modifier', width: 100 },
  { title: '修改时间', dataIndex: 'modifyTime', key: 'modifyTime', width: 160 },
  { title: '操作', key: 'action', width: 120 },
];

const handlePreview = async () => {
  if (!content.value) return;
  const res = await previewAnnouncement({ content: content.value });
  previewContent.value = res || '';
};

const handleSaveDraft = async () => {
  await saveAnnouncement({ channelId: props.channelId, title: '', content: content.value, version: announcement.value?.version });
  message.success('草稿已保存');
};

const handlePublish = () => {
  Modal.confirm({
    title: '确认发布',
    content: '确认发布此公告？发布后将展示在频道顶部。',
    onOk: async () => {
      await saveAnnouncement({ channelId: props.channelId, title: '', content: content.value, version: announcement.value?.version });
      message.success('公告已发布');
      loadAnnouncement();
    },
  });
};

const handleDelete = () => {
  Modal.confirm({
    title: '确认删除',
    content: '确认删除公告？删除后频道顶部不再展示。',
    onOk: async () => {
      if (announcement.value) {
        await deleteAnnouncement(announcement.value.id);
        announcement.value = null;
        content.value = '';
        message.success('公告已删除');
        loadHistory();
      }
    },
  });
};

const handleRestoreVersion = async (versionId: string) => {
  await restoreAnnouncementVersion(versionId);
  message.success('已恢复历史版本');
  loadAnnouncement();
};

const loadAnnouncement = async () => {
  const res = await getAnnouncement(props.channelId);
  announcement.value = res;
  if (res) content.value = res.content || '';
};

const loadHistory = async () => {
  historyLoading.value = true;
  try {
    const res = await getAnnouncementHistory(props.channelId);
    historyList.value = res || [];
  } finally {
    historyLoading.value = false;
  }
};

onMounted(() => { loadAnnouncement(); loadHistory(); });
</script>

<style lang="less" scoped>
.announcement-manage {
  .announce-header { font-weight: 600; margin-bottom: 12px; }
  .announce-status { margin-bottom: 16px; }
  .preview-section { margin: 16px 0; border: 1px solid #e8e8e8; border-radius: 6px; padding: 16px;
    .preview-title { font-weight: 600; margin-bottom: 8px; }
  }
  .announce-actions { margin: 16px 0; display: flex; justify-content: flex-end; }
  .history-section { .section-title { font-weight: 600; margin-bottom: 12px; } }
}
</style>
```

### Step 12.2: 提交

```bash
git add jeecgboot-vue3/src/views/channel/governance/AnnouncementManage.vue
git commit -m "feat(channel): add AnnouncementManage page with Tinymce editor, preview and version history"
```

---

## Task 13: 添加内容弹窗 AddContentDialog

**Files:**
- Create: `jeecgboot-vue3/src/views/channel/components/AddContentDialog.vue`
- Test: `jeecgboot-vue3/src/views/channel/__tests__/AddContentDialog.test.ts`

### Step 13.1: 实现 AddContentDialog 组件

创建 `jeecgboot-vue3/src/views/channel/components/AddContentDialog.vue`:

```vue
<template>
  <Modal v-model:visible="visible" title="添加内容到频道" :width="640" @cancel="handleClose">
    <div class="add-content-dialog">
      <div class="section">
        <div class="section-label">选择内容：</div>
        <InputSearch v-model:value="searchKeyword" placeholder="搜索已发布内容" @search="handleSearchContent" />
        <div v-if="searchResults.length > 0" class="search-results">
          <div
            v-for="item in searchResults"
            :key="item.id"
            :class="['result-item', { selected: selectedContent?.id === item.id, disabled: !item.addable }]"
            @click="handleSelectContent(item)"
          >
            <div class="item-title">{{ item.title }}</div>
            <div class="item-meta">{{ item.contentType }} | {{ item.author }} | {{ item.publishTime }}</div>
            <div v-if="!item.addable" class="not-addable">内容不可添加</div>
          </div>
        </div>
      </div>
      <div v-if="selectedContent" class="selected-preview">
        <div class="preview-title">已选内容预览</div>
        <div>标题：{{ selectedContent.title }}</div>
        <div>类型：{{ selectedContent.contentType }}</div>
        <div>作者：{{ selectedContent.author }}</div>
        <div>发布时间：{{ selectedContent.publishTime }}</div>
      </div>
      <div v-if="entryType === 'system'" class="extra-fields">
        <Form layout="vertical">
          <Form.Item label="添加原因（必填）" required>
            <Input.TextArea v-model:value="operatorNote" placeholder="请说明添加原因" />
          </Form.Item>
        </Form>
      </div>
      <div class="section">
        <div class="section-label">选择目标频道：</div>
        <ChannelSelectorInline v-model:selected="selectedChannels" :max-count="5" />
      </div>
      <div v-if="expectedResults.length > 0" class="expected-results">
        <div class="section-label">预期结果：</div>
        <div v-for="r in expectedResults" :key="r.channelId" class="expected-item">
          {{ r.channelName }}: {{ r.result }}
        </div>
      </div>
    </div>
    <template #footer>
      <Button @click="handleClose">取消</Button>
      <Button type="primary" :disabled="!canSubmit" :loading="submitting" @click="handleSubmit">
        添加到频道
      </Button>
    </template>
  </Modal>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';
import { Modal, InputSearch, Form, Input, Button, message } from 'ant-design-vue';
import { searchAddableContent, addContentToChannel } from '/@/api/content/channel/addContent';

const props = defineProps<{
  visible: boolean;
  entryType?: 'system' | 'author' | 'owner';
}>();

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'added'): void;
}>();

const searchKeyword = ref('');
const searchResults = ref<any[]>([]);
const selectedContent = ref<any>(null);
const selectedChannels = ref<any[]>([]);
const operatorNote = ref('');
const submitting = ref(false);

const canSubmit = computed(() => selectedContent.value && selectedChannels.value.length > 0 && (props.entryType !== 'system' || operatorNote.value));

const expectedResults = computed(() =>
  selectedChannels.value.map((ch: any) => ({
    channelId: ch.id,
    channelName: ch.name,
    result: ch.publishResult === 'direct' ? '直接展示' : '进入待审区',
  }))
);

const handleSearchContent = async () => {
  if (!searchKeyword.value) return;
  const res = await searchAddableContent({ keyword: searchKeyword.value });
  searchResults.value = res || [];
};

const handleSelectContent = (item: any) => {
  if (!item.addable) return;
  selectedContent.value = item;
};

const handleSubmit = async () => {
  if (!canSubmit.value) return;
  submitting.value = true;
  try {
    await addContentToChannel({
      contentId: selectedContent.value.id,
      channelIds: selectedChannels.value.map((ch: any) => ch.id),
      operatorNote: operatorNote.value,
    });
    message.success('已添加到频道');
    emit('added');
    emit('update:visible', false);
  } finally {
    submitting.value = false;
  }
};

const handleClose = () => emit('update:visible', false);
</script>

<style lang="less" scoped>
.add-content-dialog {
  .section { margin-bottom: 16px; }
  .section-label { margin-bottom: 8px; color: #666; }
  .search-results { margin-top: 8px; max-height: 200px; overflow-y: auto;
    .result-item { padding: 8px 12px; border: 1px solid #e8e8e8; border-radius: 6px; margin-bottom: 4px; cursor: pointer;
      &:hover:not(.disabled) { border-color: #1890ff; }
      &.selected { border-color: #1890ff; background: #e6f7ff; }
      &.disabled { opacity: 0.5; cursor: not-allowed; }
      .item-title { font-weight: 500; }
      .item-meta { color: #999; font-size: 12px; }
      .not-addable { color: #ff4d4f; font-size: 12px; }
    }
  }
  .selected-preview { background: #f5f5f5; padding: 12px; border-radius: 6px; margin-bottom: 16px; }
  .expected-results { margin-top: 16px;
    .expected-item { margin-bottom: 4px; }
  }
}
</style>
```

### Step 13.2: 提交

```bash
git add jeecgboot-vue3/src/views/channel/components/AddContentDialog.vue
git commit -m "feat(channel): add AddContentDialog with content search, channel selection and three entry types"
```

---

## Task 14: 治理后台容器与路由

**Files:**
- Create: `jeecgboot-vue3/src/views/channel/governance/index.vue`
- Modify: 路由配置文件

### Step 14.1: 实现治理后台 Tab 容器

创建 `jeecgboot-vue3/src/views/channel/governance/index.vue`:

```vue
<template>
  <PageWrapper dense contentFullHeight>
    <Tabs v-model:activeKey="activeTab" @change="handleTabChange">
      <Tabs.TabPane key="review" tab="待审区">
        <ReviewQueue v-if="activeTab === 'review'" :channel-id="channelId" />
      </Tabs.TabPane>
      <Tabs.TabPane key="content" tab="内容管理">
        <ContentManage v-if="activeTab === 'content'" :channel-id="channelId" @go-recycle-bin="activeTab = 'recycle'" @go-log="activeTab = 'log'" />
      </Tabs.TabPane>
      <Tabs.TabPane key="recycle" tab="回收站">
        <RecycleBin v-if="activeTab === 'recycle'" :channel-id="channelId" @back="activeTab = 'content'" />
      </Tabs.TabPane>
      <Tabs.TabPane key="log" tab="治理日志">
        <GovernanceLog v-if="activeTab === 'log'" :channel-id="channelId" />
      </Tabs.TabPane>
      <Tabs.TabPane key="announcement" tab="公告管理">
        <AnnouncementManage v-if="activeTab === 'announcement'" :channel-id="channelId" />
      </Tabs.TabPane>
    </Tabs>
  </PageWrapper>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';
import { Tabs } from 'ant-design-vue';
import { PageWrapper } from '/@/components/Page';
import { useRoute } from 'vue-router';
import ReviewQueue from './ReviewQueue.vue';
import ContentManage from './ContentManage.vue';
import RecycleBin from './RecycleBin.vue';
import GovernanceLog from './GovernanceLog.vue';
import AnnouncementManage from './AnnouncementManage.vue';

const route = useRoute();
const channelId = computed(() => route.params.channelId as string || route.query.channelId as string || '');
const activeTab = ref('review');

const handleTabChange = (key: string) => { activeTab.value = key; };
</script>
```

### Step 14.2: 提交

```bash
git add jeecgboot-vue3/src/views/channel/governance/index.vue
git commit -m "feat(channel): add governance tab container with review, content, recycle, log and announcement tabs"
```

---

## Task 15: 埋点集成

**Files:**
- Modify: 各组件中添加埋点调用

### Step 15.1: 集成发布埋点

在 `ChannelSelector.vue`、`PublishResult.vue`、`PublishPermission.vue` 中添加：

```typescript
import { track } from '/@/utils/track';

// ChannelSelector 加载完成
track('channel_selector_load', { load_time_ms: elapsed, channel_count: channels.length });

// 发布提交
track('channel_publish_submit', { channel_count: selectedChannels.length, content_type, is_scheduled, scheduled_time });

// 发布结果
track('channel_publish_result', { channel_id, status, fail_reason_type, latency_ms });

// 重试
track('channel_publish_retry', { channel_id, retry_count, original_fail_reason });

// 权限保存
track('publish_permission_save', { old_model, new_model, channel_id });
```

### Step 15.2: 集成审核埋点

在 `ReviewQueue.vue` 中添加：

```typescript
track('review_approve', { channel_id, is_batch, batch_count, time_since_submit_ms });
track('review_reject', { channel_id, is_batch, batch_count, reason_type, time_since_submit_ms });
track('review_timeout_alert', { channel_id, timeout_count, max_timeout_hours });
```

### Step 15.3: 集成治理埋点

在 `ContentManage.vue`、`RecycleBin.vue` 中添加：

```typescript
track('governance_action', { action_type, channel_id, is_batch, batch_count });
track('governance_undo', { original_action, channel_id });
track('recycle_bin_restore', { channel_id, days_deleted, is_batch, batch_count });
```

### Step 15.4: 提交

```bash
git add jeecgboot-vue3/src/views/channel/
git commit -m "feat(channel): integrate tracking events for publish, review and governance flows"
```

---

## Task 16: 验证

### Step 16.1: 运行全量测试

```bash
cd jeecgboot-vue3 && npx vitest run src/views/channel/__tests__/ --reporter=verbose
```

预期: 全部 PASS

### Step 16.2: 构建验证

```bash
cd jeecgboot-vue3 && pnpm build
```

预期: 构建成功，无错误

### Step 16.3: 提交最终验证

```bash
git add -A
git commit -m "test(channel): add comprehensive tests for channel content governance frontend"
```
