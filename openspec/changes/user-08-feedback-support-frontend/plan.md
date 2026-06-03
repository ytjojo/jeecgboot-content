# 反馈、举报与客服支持 -- 前端实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为内容社区新增举报、申诉、帮助中心、更新日志和客服支持五大功能模块。

**Architecture:** 按功能域在 `src/views/support/` 下创建子目录（report/appeal/help/changelog/customer-service），新增 `useFeedbackStore` 集中管理状态，API 层在 `src/api/support/` 下按域拆分。路由由后端动态管理（permission mode: BACK），前端仅创建页面组件。客服实时对话使用 WebSocket。

**Tech Stack:** Vue 3 + TypeScript + Vite 6 + Ant Design Vue 4 + Pinia + defHttp + WebSocket

**源码根目录:** `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecgboot-vue3/`

---

## Task 1: API 层 -- 举报 API

**Files:**
- Create: `src/api/support/report.ts`
- Test: `src/api/support/report.spec.ts`

- [ ] **Step 1: 创建举报 API 类型定义和接口**

```typescript
// src/api/support/report.ts
import { defHttp } from '/@/utils/http/axios';

enum Api {
  create = '/content/user/support/report',
  withdraw = '/content/user/support/report/{id}/withdraw',
  list = '/content/user/support/report/list',
  detail = '/content/user/support/report/{id}',
}

export interface ReportCreateParams {
  targetType: string; // 'article' | 'comment' | 'user'
  targetId: string;
  reportType: string; // 'porn' | 'violence' | 'fraud' | 'harassment' | 'other'
  description?: string;
  evidenceUrls?: string[];
}

export interface ReportQueryParams {
  status?: string;
  reportType?: string;
  pageNo?: number;
  pageSize?: number;
}

export interface ReportItem {
  id: string;
  reportNo: string;
  targetType: string;
  targetId: string;
  targetSummary: string;
  reportType: string;
  reportTypeLabel: string;
  description: string;
  evidenceUrls: string[];
  status: string; // 'pending' | 'processing' | 'processed' | 'withdrawn'
  statusLabel: string;
  result: string;
  createTime: string;
  updateTime: string;
}

/** 创建举报 */
export const createReport = (data: ReportCreateParams) =>
  defHttp.post({ url: Api.create, data });

/** 撤回举报 */
export const withdrawReport = (id: string) =>
  defHttp.post({ url: Api.withdraw.replace('{id}', id) });

/** 查询举报列表 */
export const getReportList = (params: ReportQueryParams) =>
  defHttp.get({ url: Api.list, params });

/** 查询举报详情 */
export const getReportDetail = (id: string) =>
  defHttp.get({ url: Api.detail.replace('{id}', id) });
```

- [ ] **Step 2: 验证文件创建成功**

Run: `cat src/api/support/report.ts | head -5`
Expected: 文件存在且包含 import 语句

- [ ] **Step 3: Commit**

```bash
git add src/api/support/report.ts
git commit -m "feat(support): add report API layer"
```

---

## Task 2: API 层 -- 申诉 API

**Files:**
- Create: `src/api/support/appeal.ts`

- [ ] **Step 1: 创建申诉 API 类型定义和接口**

```typescript
// src/api/support/appeal.ts
import { defHttp } from '/@/utils/http/axios';

enum Api {
  create = '/content/user/support/appeal',
  withdraw = '/content/user/support/appeal/{id}/withdraw',
  list = '/content/user/support/appeal/list',
  detail = '/content/user/support/appeal/{id}',
}

export interface AppealCreateParams {
  appealType: string; // 'content_delete' | 'account_ban' | 'points_deduct' | 'badge_deduct'
  relatedId: string; // 关联的举报ID或处罚ID
  reason: string;
  attachmentUrls?: string[];
}

export interface AppealQueryParams {
  status?: string;
  pageNo?: number;
  pageSize?: number;
}

export interface AppealItem {
  id: string;
  appealNo: string;
  appealType: string;
  appealTypeLabel: string;
  relatedId: string;
  relatedSummary: string;
  reason: string;
  attachmentUrls: string[];
  status: string; // 'reviewing' | 'approved' | 'rejected' | 'withdrawn'
  statusLabel: string;
  auditResult: string;
  auditTime: string;
  appealCount: number; // 当前第几次申诉
  maxAppealCount: number; // 最大申诉次数
  estimatedTime: string; // 预计处理时间
  createTime: string;
}

/** 创建申诉 */
export const createAppeal = (data: AppealCreateParams) =>
  defHttp.post({ url: Api.create, data });

/** 撤回申诉 */
export const withdrawAppeal = (id: string) =>
  defHttp.post({ url: Api.withdraw.replace('{id}', id) });

/** 查询申诉列表 */
export const getAppealList = (params: AppealQueryParams) =>
  defHttp.get({ url: Api.list, params });

/** 查询申诉详情 */
export const getAppealDetail = (id: string) =>
  defHttp.get({ url: Api.detail.replace('{id}', id) });
```

- [ ] **Step 2: Commit**

```bash
git add src/api/support/appeal.ts
git commit -m "feat(support): add appeal API layer"
```

---

## Task 3: API 层 -- 帮助中心 API

**Files:**
- Create: `src/api/support/help.ts`

- [ ] **Step 1: 创建帮助中心 API**

```typescript
// src/api/support/help.ts
import { defHttp } from '/@/utils/http/axios';

enum Api {
  search = '/content/user/support/help/search',
  categories = '/content/user/support/help/categories',
  article = '/content/user/support/help/article/{id}',
  feedback = '/content/user/support/help/article/{id}/feedback',
}

export interface HelpSearchParams {
  keyword: string;
  pageNo?: number;
  pageSize?: number;
}

export interface HelpCategory {
  id: string;
  name: string;
  icon: string;
  articleCount: number;
}

export interface HelpArticle {
  id: string;
  title: string;
  content: string; // Markdown 内容
  categoryId: string;
  categoryName: string;
  viewCount: number;
  helpfulCount: number;
  unhelpfulCount: number;
  createTime: string;
  updateTime: string;
}

export interface HelpSearchResult {
  id: string;
  title: string;
  summary: string;
  categoryName: string;
}

/** 搜索帮助文章 */
export const searchHelpArticles = (params: HelpSearchParams) =>
  defHttp.get({ url: Api.search, params });

/** 获取帮助分类 */
export const getHelpCategories = () =>
  defHttp.get({ url: Api.categories });

/** 获取文章详情 */
export const getHelpArticleDetail = (id: string) =>
  defHttp.get({ url: Api.article.replace('{id}', id) });

/** 提交文章反馈 */
export const submitArticleFeedback = (id: string, data: { helpful: boolean }) =>
  defHttp.post({ url: Api.feedback.replace('{id}', id), data });
```

- [ ] **Step 2: Commit**

```bash
git add src/api/support/help.ts
git commit -m "feat(support): add help center API layer"
```

---

## Task 4: API 层 -- 更新日志 API

**Files:**
- Create: `src/api/support/changelog.ts`

- [ ] **Step 1: 创建更新日志 API**

```typescript
// src/api/support/changelog.ts
import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/content/user/support/changelog/list',
}

export interface ChangelogQueryParams {
  keyword?: string;
  pageNo?: number;
  pageSize?: number;
}

export interface ChangelogVersion {
  id: string;
  version: string;
  releaseDate: string;
  features: string[]; // 新增功能
  improvements: string[]; // 优化内容
  bugfixes: string[]; // 修复问题
}

/** 获取更新日志列表 */
export const getChangelogList = (params: ChangelogQueryParams) =>
  defHttp.get({ url: Api.list, params });
```

- [ ] **Step 2: Commit**

```bash
git add src/api/support/changelog.ts
git commit -m "feat(support): add changelog API layer"
```

---

## Task 5: API 层 -- 客服 API

**Files:**
- Create: `src/api/support/customer-service.ts`

- [ ] **Step 1: 创建客服 API**

```typescript
// src/api/support/customer-service.ts
import { defHttp } from '/@/utils/http/axios';

enum Api {
  createSession = '/content/user/support/customer-service/session',
  transfer = '/content/user/support/customer-service/session/{id}/transfer',
  sendMessage = '/content/user/support/customer-service/session/{id}/message',
  closeSession = '/content/user/support/customer-service/session/{id}/close',
  submitRating = '/content/user/support/customer-service/session/{id}/rating',
  sessionList = '/content/user/support/customer-service/sessions',
  sessionDetail = '/content/user/support/customer-service/session/{id}',
}

export interface ServiceSession {
  id: string;
  type: string; // 'bot' | 'human'
  status: string; // 'bot' | 'queuing' | 'human' | 'closed'
  agentName: string;
  queuePosition: number | null;
  estimatedWaitTime: number | null; // 秒
  createTime: string;
}

export interface ChatMessage {
  id: string;
  sessionId: string;
  senderType: string; // 'user' | 'bot' | 'agent' | 'system'
  content: string;
  messageType: string; // 'text' | 'image' | 'link'
  status: string; // 'sending' | 'sent' | 'failed'
  createTime: string;
}

export interface RatingParams {
  score: number; // 1-5
  comment?: string;
}

export interface SessionQueryParams {
  pageNo?: number;
  pageSize?: number;
}

/** 创建客服会话 */
export const createServiceSession = () =>
  defHttp.post({ url: Api.createSession });

/** 转人工客服 */
export const transferToHuman = (sessionId: string) =>
  defHttp.post({ url: Api.transfer.replace('{id}', sessionId) });

/** 发送消息 */
export const sendChatMessage = (sessionId: string, data: { content: string; messageType: string }) =>
  defHttp.post({ url: Api.sendMessage.replace('{id}', sessionId), data });

/** 结束会话 */
export const closeServiceSession = (sessionId: string) =>
  defHttp.post({ url: Api.closeSession.replace('{id}', sessionId) });

/** 提交服务评分 */
export const submitServiceRating = (sessionId: string, data: RatingParams) =>
  defHttp.post({ url: Api.submitRating.replace('{id}', sessionId), data });

/** 查询会话历史列表 */
export const getServiceSessionList = (params: SessionQueryParams) =>
  defHttp.get({ url: Api.sessionList, params });

/** 查询会话详情 */
export const getServiceSessionDetail = (sessionId: string) =>
  defHttp.get({ url: Api.sessionDetail.replace('{id}', sessionId) });
```

- [ ] **Step 2: Commit**

```bash
git add src/api/support/customer-service.ts
git commit -m "feat(support): add customer service API layer"
```

---

## Task 6: 状态管理 -- useFeedbackStore

**Files:**
- Create: `src/store/modules/feedback.ts`
- Test: `src/store/modules/feedback.spec.ts`

- [ ] **Step 1: 编写 useFeedbackStore 单元测试**

```typescript
// src/store/modules/feedback.spec.ts
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useFeedbackStore } from '/@/store/modules/feedback';

// Mock defHttp
vi.mock('/@/utils/http/axios', () => ({
  defHttp: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

describe('useFeedbackStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('should have correct initial state', () => {
    const store = useFeedbackStore();
    expect(store.reportList).toEqual([]);
    expect(store.reportTotal).toBe(0);
    expect(store.appealList).toEqual([]);
    expect(store.appealTotal).toBe(0);
    expect(store.currentSession).toBeNull();
    expect(store.chatMessages).toEqual([]);
    expect(store.queuePosition).toBeNull();
    expect(store.wsConnected).toBe(false);
    expect(store.reconnecting).toBe(false);
    expect(store.pendingReportCount).toBe(0);
    expect(store.pendingAppealCount).toBe(0);
  });

  it('should update chatMessages when addMessage is called', () => {
    const store = useFeedbackStore();
    const msg = { id: '1', content: 'hello', senderType: 'user', status: 'sent' };
    store.addMessage(msg as any);
    expect(store.chatMessages).toHaveLength(1);
    expect(store.chatMessages[0].content).toBe('hello');
  });

  it('should update message status', () => {
    const store = useFeedbackStore();
    store.addMessage({ id: '1', content: 'hello', senderType: 'user', status: 'sending' } as any);
    store.updateMessageStatus('1', 'sent');
    expect(store.chatMessages[0].status).toBe('sent');
  });

  it('should clear session state', () => {
    const store = useFeedbackStore();
    store.currentSession = { id: '1' } as any;
    store.chatMessages = [{ id: '1' }] as any;
    store.queuePosition = 3;
    store.clearSession();
    expect(store.currentSession).toBeNull();
    expect(store.chatMessages).toEqual([]);
    expect(store.queuePosition).toBeNull();
  });
});
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecgboot-vue3 && npx vitest run src/store/modules/feedback.spec.ts --reporter=verbose 2>&1 | tail -20`
Expected: FAIL -- 模块不存在

- [ ] **Step 3: 实现 useFeedbackStore**

```typescript
// src/store/modules/feedback.ts
import { defineStore } from 'pinia';
import { store } from '/@/store';

interface ReportItem {
  id: string;
  reportNo: string;
  targetType: string;
  targetId: string;
  targetSummary: string;
  reportType: string;
  reportTypeLabel: string;
  description: string;
  evidenceUrls: string[];
  status: string;
  statusLabel: string;
  result: string;
  createTime: string;
}

interface AppealItem {
  id: string;
  appealNo: string;
  appealType: string;
  appealTypeLabel: string;
  relatedId: string;
  relatedSummary: string;
  reason: string;
  attachmentUrls: string[];
  status: string;
  statusLabel: string;
  auditResult: string;
  auditTime: string;
  appealCount: number;
  maxAppealCount: number;
  estimatedTime: string;
  createTime: string;
}

interface ServiceSession {
  id: string;
  type: string;
  status: string;
  agentName: string;
  queuePosition: number | null;
  estimatedWaitTime: number | null;
  createTime: string;
}

interface ChatMessage {
  id: string;
  sessionId: string;
  senderType: string;
  content: string;
  messageType: string;
  status: string;
  createTime: string;
}

interface FeedbackState {
  reportList: ReportItem[];
  reportTotal: number;
  reportQuery: Record<string, any>;
  appealList: AppealItem[];
  appealTotal: number;
  appealQuery: Record<string, any>;
  currentSession: ServiceSession | null;
  chatMessages: ChatMessage[];
  queuePosition: number | null;
  wsConnected: boolean;
  reconnecting: boolean;
  pendingReportCount: number;
  pendingAppealCount: number;
}

export const useFeedbackStore = defineStore({
  id: 'app-feedback',
  state: (): FeedbackState => ({
    reportList: [],
    reportTotal: 0,
    reportQuery: {},
    appealList: [],
    appealTotal: 0,
    appealQuery: {},
    currentSession: null,
    chatMessages: [],
    queuePosition: null,
    wsConnected: false,
    reconnecting: false,
    pendingReportCount: 0,
    pendingAppealCount: 0,
  }),
  actions: {
    addMessage(msg: ChatMessage) {
      this.chatMessages.push(msg);
    },
    updateMessageStatus(id: string, status: string) {
      const msg = this.chatMessages.find((m) => m.id === id);
      if (msg) {
        msg.status = status;
      }
    },
    clearSession() {
      this.currentSession = null;
      this.chatMessages = [];
      this.queuePosition = null;
      this.wsConnected = false;
      this.reconnecting = false;
    },
    setReportList(list: ReportItem[], total: number) {
      this.reportList = list;
      this.reportTotal = total;
    },
    setAppealList(list: AppealItem[], total: number) {
      this.appealList = list;
      this.appealTotal = total;
    },
    setWsConnected(connected: boolean) {
      this.wsConnected = connected;
    },
    setReconnecting(reconnecting: boolean) {
      this.reconnecting = reconnecting;
    },
    setQueuePosition(position: number | null) {
      this.queuePosition = position;
    },
  },
});

export function useFeedbackStoreWithOut() {
  return useFeedbackStore(store);
}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecgboot-vue3 && npx vitest run src/store/modules/feedback.spec.ts --reporter=verbose 2>&1 | tail -20`
Expected: PASS -- 4 tests passed

- [ ] **Step 5: Commit**

```bash
git add src/store/modules/feedback.ts src/store/modules/feedback.spec.ts
git commit -m "feat(support): add useFeedbackStore with unit tests"
```

---

## Task 7: 组件 -- ReportModal 举报表单弹窗

**Files:**
- Create: `src/views/support/report/components/ReportModal.vue`
- Test: `src/views/support/report/components/ReportModal.spec.ts`

- [ ] **Step 1: 编写 ReportModal 单元测试**

```typescript
// src/views/support/report/components/ReportModal.spec.ts
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import ReportModal from './ReportModal.vue';

// Mock API
vi.mock('/@/api/support/report', () => ({
  createReport: vi.fn().mockResolvedValue({ reportNo: 'R20260601001' }),
}));

describe('ReportModal', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  const defaultProps = {
    visible: true,
    targetType: 'article',
    targetId: '123',
    targetSummary: '测试文章标题...',
  };

  it('should render modal when visible is true', () => {
    const wrapper = mount(ReportModal, { props: defaultProps });
    expect(wrapper.find('.ant-modal').exists()).toBe(true);
  });

  it('should display target summary', () => {
    const wrapper = mount(ReportModal, { props: defaultProps });
    expect(wrapper.text()).toContain('测试文章标题...');
  });

  it('should show report type options', () => {
    const wrapper = mount(ReportModal, { props: defaultProps });
    expect(wrapper.text()).toContain('色情内容');
    expect(wrapper.text()).toContain('暴力内容');
    expect(wrapper.text()).toContain('诈骗信息');
    expect(wrapper.text()).toContain('骚扰行为');
    expect(wrapper.text()).toContain('其他');
  });

  it('should disable submit when uploading', async () => {
    const wrapper = mount(ReportModal, { props: defaultProps });
    // 模拟上传中状态
    await wrapper.setData({ uploading: true });
    const submitBtn = wrapper.find('[data-testid="submit-btn"]');
    expect(submitBtn.attributes('disabled')).toBeDefined();
  });

  it('should emit success after successful submission', async () => {
    const wrapper = mount(ReportModal, { props: defaultProps });
    await wrapper.find('input[type="radio"]').setValue();
    await wrapper.find('textarea').setValue('违规内容描述');
    await wrapper.find('[data-testid="submit-btn"]').trigger('click');
    await wrapper.vm.$nextTick();
    expect(wrapper.emitted('success')).toBeTruthy();
  });
});
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecgboot-vue3 && npx vitest run src/views/support/report/components/ReportModal.spec.ts --reporter=verbose 2>&1 | tail -20`
Expected: FAIL -- 组件不存在

- [ ] **Step 3: 实现 ReportModal 组件**

```vue
<!-- src/views/support/report/components/ReportModal.vue -->
<template>
  <a-modal
    :open="visible"
    title="举报"
    :width="520"
    :confirm-loading="submitting"
    :mask-closable="false"
    @cancel="handleClose"
    @ok="handleSubmit"
  >
    <div class="report-modal">
      <!-- 被举报对象摘要 -->
      <div class="target-summary">
        <span class="label">举报对象：</span>
        <span class="value">{{ targetSummary }}</span>
      </div>

      <!-- 举报类型 -->
      <a-form :model="formData" layout="vertical">
        <a-form-item label="举报类型" name="reportType" required>
          <a-radio-group v-model:value="formData.reportType" @change="handleTypeChange">
            <a-radio v-for="item in reportTypes" :key="item.value" :value="item.value" class="report-type-radio">
              <div>
                <div>{{ item.label }}</div>
                <div class="type-desc">{{ item.description }}</div>
              </div>
            </a-radio>
          </a-radio-group>
        </a-form-item>

        <!-- 举报说明 -->
        <a-form-item label="举报说明">
          <a-textarea
            v-model:value="formData.description"
            placeholder="请补充说明（选填）"
            :maxlength="500"
            :rows="3"
            show-count
          />
        </a-form-item>

        <!-- 证据上传 -->
        <a-form-item label="证据上传">
          <a-upload
            v-model:file-list="fileList"
            :before-upload="beforeUpload"
            :custom-request="handleUpload"
            accept="image/*,video/*"
            list-type="text"
          >
            <a-button>
              <upload-outlined />
              上传文件
            </a-button>
            <template #tip>
              <div class="upload-tip">支持图片/视频，单文件最大 10MB，最多 5 个文件</div>
            </template>
          </a-upload>
        </a-form-item>
      </a-form>
    </div>

    <template #footer>
      <a-button @click="handleClose">取消</a-button>
      <a-button
        type="primary"
        :loading="submitting"
        :disabled="uploading"
        data-testid="submit-btn"
        @click="handleSubmit"
      >
        提交举报
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue';
import { UploadOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { createReport, type ReportCreateParams } from '/@/api/support/report';
import { uploadFile } from '/@/api/sys/upload';

const props = defineProps<{
  visible: boolean;
  targetType: string;
  targetId: string;
  targetSummary: string;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'success', reportNo: string): void;
}>();

const reportTypes = [
  { value: 'porn', label: '色情内容', description: '包含色情、裸露等不当内容' },
  { value: 'violence', label: '暴力内容', description: '包含血腥、恐怖等暴力内容' },
  { value: 'fraud', label: '诈骗信息', description: '涉及欺诈、钓鱼等行为' },
  { value: 'harassment', label: '骚扰行为', description: '包含辱骂、威胁、骚扰等' },
  { value: 'other', label: '其他', description: '其他违规行为' },
];

const formData = reactive<ReportCreateParams>({
  targetType: props.targetType,
  targetId: props.targetId,
  reportType: '',
  description: '',
  evidenceUrls: [],
});

const fileList = ref<any[]>([]);
const submitting = ref(false);
const uploading = ref(false);

watch(
  () => props.visible,
  (val) => {
    if (val) {
      formData.targetType = props.targetType;
      formData.targetId = props.targetId;
      formData.reportType = '';
      formData.description = '';
      formData.evidenceUrls = [];
      fileList.value = [];
    }
  }
);

const handleTypeChange = () => {
  // 类型变更时的处理
};

const beforeUpload = (file: File) => {
  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    message.error('文件大小不能超过 10MB');
    return false;
  }
  if (fileList.value.length >= 5) {
    message.error('最多上传 5 个文件');
    return false;
  }
  return true;
};

const handleUpload = async (options: any) => {
  uploading.value = true;
  try {
    const res = await uploadFile(options.file);
    formData.evidenceUrls.push(res.result.url);
    options.onSuccess(res);
  } catch (err) {
    options.onError(err);
  } finally {
    uploading.value = false;
  }
};

const handleSubmit = async () => {
  if (!formData.reportType) {
    message.warning('请选择举报类型');
    return;
  }
  submitting.value = true;
  try {
    const res = await createReport(formData);
    message.success('举报已提交，我们将尽快处理');
    emit('success', res.result.reportNo);
    handleClose();
  } catch (err: any) {
    if (err?.code === 'DUPLICATE_REPORT') {
      message.warning('您已举报过该内容，请勿重复举报');
    } else {
      message.error('提交失败，请重试');
    }
  } finally {
    submitting.value = false;
  }
};

const handleClose = () => {
  emit('close');
};
</script>

<style scoped lang="less">
.report-modal {
  .target-summary {
    background: #f5f5f5;
    padding: 12px;
    border-radius: 6px;
    margin-bottom: 16px;

    .label {
      color: #666;
    }
    .value {
      color: #333;
      font-weight: 500;
    }
  }

  .report-type-radio {
    display: block;
    margin-bottom: 8px;

    .type-desc {
      font-size: 12px;
      color: #999;
      margin-top: 2px;
    }
  }

  .upload-tip {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }
}
</style>
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecgboot-vue3 && npx vitest run src/views/support/report/components/ReportModal.spec.ts --reporter=verbose 2>&1 | tail -20`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/views/support/report/components/ReportModal.vue src/views/support/report/components/ReportModal.spec.ts
git commit -m "feat(support): add ReportModal component with tests"
```

---

## Task 8: 组件 -- ReportDetailDrawer 举报详情抽屉

**Files:**
- Create: `src/views/support/report/components/ReportDetailDrawer.vue`

- [ ] **Step 1: 实现 ReportDetailDrawer**

```vue
<!-- src/views/support/report/components/ReportDetailDrawer.vue -->
<template>
  <a-drawer
    :open="visible"
    title="举报详情"
    :width="480"
    @close="handleClose"
  >
    <template v-if="detail">
      <a-descriptions :column="1" bordered size="small">
        <a-descriptions-item label="举报编号">{{ detail.reportNo }}</a-descriptions-item>
        <a-descriptions-item label="举报对象">{{ detail.targetSummary }}</a-descriptions-item>
        <a-descriptions-item label="举报类型">{{ detail.reportTypeLabel }}</a-descriptions-item>
        <a-descriptions-item label="举报说明">{{ detail.description || '无' }}</a-descriptions-item>
        <a-descriptions-item label="提交时间">{{ detail.createTime }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="statusColor(detail.status)">
            {{ detail.statusLabel }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item v-if="detail.status === 'processed'" label="处理结果">
          {{ detail.result }}
        </a-descriptions-item>
      </a-descriptions>

      <!-- 证据预览 -->
      <div v-if="detail.evidenceUrls?.length" class="evidence-section">
        <h4>证据材料</h4>
        <a-image-preview-group>
          <a-space>
            <a-image
              v-for="(url, index) in detail.evidenceUrls"
              :key="index"
              :src="url"
              :width="80"
              :height="80"
              style="object-fit: cover; border-radius: 4px"
            />
          </a-space>
        </a-image-preview-group>
      </div>

      <!-- 申诉入口 -->
      <div v-if="detail.status === 'processed'" class="appeal-entry">
        <a-divider />
        <a-alert
          message="对处理结果不满意？"
          description="您可以发起申诉，请求复核处理结果。"
          type="info"
          show-icon
        >
          <template #action>
            <a-button size="small" type="primary" @click="handleAppeal">
              发起申诉
            </a-button>
          </template>
        </a-alert>
      </div>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { getReportDetail, type ReportItem } from '/@/api/support/report';

const props = defineProps<{
  visible: boolean;
  reportId: string;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
}>();

const router = useRouter();
const detail = ref<ReportItem | null>(null);
const loading = ref(false);

watch(
  () => props.visible,
  async (val) => {
    if (val && props.reportId) {
      loading.value = true;
      try {
        const res = await getReportDetail(props.reportId);
        detail.value = res.result;
      } finally {
        loading.value = false;
      }
    }
  }
);

const statusColor = (status: string) => {
  const map: Record<string, string> = {
    pending: 'orange',
    processing: 'blue',
    processed: 'green',
    withdrawn: 'default',
  };
  return map[status] || 'default';
};

const handleAppeal = () => {
  router.push({ path: '/user/appeals/create', query: { reportId: props.reportId } });
  handleClose();
};

const handleClose = () => {
  emit('close');
};
</script>

<style scoped lang="less">
.evidence-section {
  margin-top: 16px;

  h4 {
    margin-bottom: 12px;
    font-weight: 500;
  }
}

.appeal-entry {
  margin-top: 16px;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add src/views/support/report/components/ReportDetailDrawer.vue
git commit -m "feat(support): add ReportDetailDrawer component"
```

---

## Task 9: 页面 -- 我的举报列表页

**Files:**
- Create: `src/views/support/report/index.vue`

- [ ] **Step 1: 实现我的举报列表页**

```vue
<!-- src/views/support/report/index.vue -->
<template>
  <div class="report-list-page">
    <a-card title="我的举报">
      <!-- 查询表单 -->
      <a-form layout="inline" :model="queryParams" class="query-form">
        <a-form-item label="举报状态">
          <a-select v-model:value="queryParams.status" placeholder="全部" allow-clear style="width: 140px">
            <a-select-option value="">全部</a-select-option>
            <a-select-option value="pending">待处理</a-select-option>
            <a-select-option value="processing">处理中</a-select-option>
            <a-select-option value="processed">已处理</a-select-option>
            <a-select-option value="withdrawn">已撤回</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="举报类型">
          <a-select v-model:value="queryParams.reportType" placeholder="全部" allow-clear style="width: 140px">
            <a-select-option value="">全部</a-select-option>
            <a-select-option value="porn">色情</a-select-option>
            <a-select-option value="violence">暴力</a-select-option>
            <a-select-option value="fraud">诈骗</a-select-option>
            <a-select-option value="harassment">骚扰</a-select-option>
            <a-select-option value="other">其他</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- 数据表格 -->
      <a-table
        :columns="columns"
        :data-source="reportList"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'status'">
            <a-tag :color="statusColor(record.status)">
              {{ record.statusLabel }}
            </a-tag>
          </template>
          <template v-if="column.dataIndex === 'action'">
            <a-space>
              <a @click="handleViewDetail(record)">查看详情</a>
              <a-popconfirm
                v-if="record.status === 'pending'"
                title="确认撤回该举报？撤回后不可恢复"
                @confirm="handleWithdraw(record)"
              >
                <a>撤回</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>

      <!-- 空状态 -->
      <a-empty v-if="!loading && reportList.length === 0" description="暂无举报记录">
        <a-button type="primary" @click="handleGuide">遇到违规内容？点击举报</a-button>
      </a-empty>
    </a-card>

    <!-- 举报详情抽屉 -->
    <ReportDetailDrawer
      :visible="drawerVisible"
      :report-id="selectedReportId"
      @close="drawerVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { getReportList, withdrawReport, type ReportItem, type ReportQueryParams } from '/@/api/support/report';
import ReportDetailDrawer from './components/ReportDetailDrawer.vue';

const columns = [
  { title: '举报编号', dataIndex: 'reportNo', width: 160 },
  { title: '举报对象', dataIndex: 'targetSummary', ellipsis: true },
  { title: '举报类型', dataIndex: 'reportTypeLabel', width: 100 },
  { title: '提交时间', dataIndex: 'createTime', width: 180 },
  { title: '状态', dataIndex: 'status', width: 100 },
  { title: '操作', dataIndex: 'action', width: 150, fixed: 'right' as const },
];

const loading = ref(false);
const reportList = ref<ReportItem[]>([]);
const total = ref(0);
const drawerVisible = ref(false);
const selectedReportId = ref('');

const queryParams = reactive<ReportQueryParams>({
  status: undefined,
  reportType: undefined,
  pageNo: 1,
  pageSize: 20,
});

const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
});

const fetchList = async () => {
  loading.value = true;
  try {
    const res = await getReportList(queryParams);
    reportList.value = res.result.records || [];
    total.value = res.result.total || 0;
    pagination.total = total.value;
    pagination.current = queryParams.pageNo || 1;
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  queryParams.pageNo = 1;
  fetchList();
};

const handleReset = () => {
  queryParams.status = undefined;
  queryParams.reportType = undefined;
  queryParams.pageNo = 1;
  fetchList();
};

const handleTableChange = (pag: any) => {
  queryParams.pageNo = pag.current;
  queryParams.pageSize = pag.pageSize;
  fetchList();
};

const handleViewDetail = (record: ReportItem) => {
  selectedReportId.value = record.id;
  drawerVisible.value = true;
};

const handleWithdraw = async (record: ReportItem) => {
  try {
    await withdrawReport(record.id);
    message.success('撤回成功');
    fetchList();
  } catch {
    message.error('撤回失败');
  }
};

const handleGuide = () => {
  message.info('请在内容详情页点击"举报"按钮');
};

const statusColor = (status: string) => {
  const map: Record<string, string> = {
    pending: 'orange',
    processing: 'blue',
    processed: 'green',
    withdrawn: 'default',
  };
  return map[status] || 'default';
};

onMounted(() => {
  fetchList();
});
</script>

<style scoped lang="less">
.report-list-page {
  padding: 16px;

  .query-form {
    margin-bottom: 16px;
  }
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add src/views/support/report/index.vue
git commit -m "feat(support): add report list page"
```

---

## Task 10: 组件与页面 -- 申诉系统

**Files:**
- Create: `src/views/support/appeal/components/AppealDetailDrawer.vue`
- Create: `src/views/support/appeal/create.vue`
- Test: `src/views/support/appeal/create.spec.ts`
- Create: `src/views/support/appeal/index.vue`

- [ ] **Step 1: 编写申诉提交页测试**

```typescript
// src/views/support/appeal/create.spec.ts
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import AppealCreate from './create.vue';

vi.mock('/@/api/support/appeal', () => ({
  createAppeal: vi.fn().mockResolvedValue({ appealNo: 'A20260601001' }),
  getAppealDetail: vi.fn().mockResolvedValue({ result: { appealCount: 1, maxAppealCount: 3 } }),
}));

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
  useRoute: () => ({ query: { reportId: '123' } }),
}));

describe('AppealCreate', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('should render appeal form', () => {
    const wrapper = mount(AppealCreate);
    expect(wrapper.text()).toContain('提交申诉');
  });

  it('should show appeal count indicator', () => {
    const wrapper = mount(AppealCreate);
    expect(wrapper.text()).toContain('第');
    expect(wrapper.text()).toContain('次申诉');
  });

  it('should disable submit when reason is empty', () => {
    const wrapper = mount(AppealCreate);
    const submitBtn = wrapper.find('[data-testid="submit-btn"]');
    expect(submitBtn.attributes('disabled')).toBeDefined();
  });

  it('should show confirmation dialog on 3rd appeal', async () => {
    const wrapper = mount(AppealCreate);
    await wrapper.setData({ appealCount: 3 });
    await wrapper.find('textarea').setValue('申诉理由');
    await wrapper.find('[data-testid="submit-btn"]').trigger('click');
    expect(wrapper.text()).toContain('这是最后一次申诉机会');
  });
});
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecgboot-vue3 && npx vitest run src/views/support/appeal/create.spec.ts --reporter=verbose 2>&1 | tail -20`
Expected: FAIL

- [ ] **Step 3: 实现 AppealDetailDrawer**

```vue
<!-- src/views/support/appeal/components/AppealDetailDrawer.vue -->
<template>
  <a-drawer :open="visible" title="申诉详情" :width="480" @close="handleClose">
    <template v-if="detail">
      <a-descriptions :column="1" bordered size="small">
        <a-descriptions-item label="申诉编号">{{ detail.appealNo }}</a-descriptions-item>
        <a-descriptions-item label="申诉类型">{{ detail.appealTypeLabel }}</a-descriptions-item>
        <a-descriptions-item label="关联处罚">{{ detail.relatedSummary }}</a-descriptions-item>
        <a-descriptions-item label="申诉理由">{{ detail.reason }}</a-descriptions-item>
        <a-descriptions-item label="提交时间">{{ detail.createTime }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="statusColor(detail.status)">{{ detail.statusLabel }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item v-if="detail.status === 'reviewing'" label="预计处理时间">
          {{ detail.estimatedTime || '预计 1-3 个工作日' }}
        </a-descriptions-item>
        <a-descriptions-item v-if="detail.status !== 'reviewing'" label="审核结果">
          {{ detail.auditResult }}
        </a-descriptions-item>
        <a-descriptions-item v-if="detail.auditTime" label="审核时间">
          {{ detail.auditTime }}
        </a-descriptions-item>
      </a-descriptions>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { getAppealDetail, type AppealItem } from '/@/api/support/appeal';

const props = defineProps<{ visible: boolean; appealId: string }>();
const emit = defineEmits<{ (e: 'close'): void }>();

const detail = ref<AppealItem | null>(null);

watch(
  () => props.visible,
  async (val) => {
    if (val && props.appealId) {
      const res = await getAppealDetail(props.appealId);
      detail.value = res.result;
    }
  }
);

const statusColor = (status: string) => {
  const map: Record<string, string> = {
    reviewing: 'blue',
    approved: 'green',
    rejected: 'red',
    withdrawn: 'default',
  };
  return map[status] || 'default';
};

const handleClose = () => emit('close');
</script>
```

- [ ] **Step 4: 实现申诉提交页**

```vue
<!-- src/views/support/appeal/create.vue -->
<template>
  <div class="appeal-create-page">
    <a-card title="提交申诉">
      <a-breadcrumb>
        <a-breadcrumb-item>个人中心</a-breadcrumb-item>
        <a-breadcrumb-item>我的申诉</a-breadcrumb-item>
        <a-breadcrumb-item>提交申诉</a-breadcrumb-item>
      </a-breadcrumb>

      <a-form :model="formData" layout="vertical" class="appeal-form">
        <a-form-item label="申诉类型" required>
          <a-select v-model:value="formData.appealType" placeholder="请选择申诉类型">
            <a-select-option value="content_delete">内容删除</a-select-option>
            <a-select-option value="account_ban">账号封禁</a-select-option>
            <a-select-option value="points_deduct">积分扣除</a-select-option>
            <a-select-option value="badge_deduct">勋章扣除</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="关联记录">
          <a-input :value="relatedSummary" disabled />
        </a-form-item>

        <a-form-item label="申诉理由" required>
          <a-textarea
            v-model:value="formData.reason"
            placeholder="请详细描述您的申诉理由"
            :maxlength="1000"
            :rows="5"
            show-count
          />
        </a-form-item>

        <a-form-item label="附件上传">
          <a-upload
            v-model:file-list="fileList"
            :before-upload="beforeUpload"
            :custom-request="handleUpload"
            accept="image/*,video/*"
          >
            <a-button>
              <upload-outlined />
              上传附件
            </a-button>
          </a-upload>
        </a-form-item>

        <!-- 申诉次数提示 -->
        <div class="appeal-count-tip">
          <a-alert
            :message="`本次为第 ${appealCount}/3 次申诉`"
            :type="appealCount >= 3 ? 'warning' : 'info'"
            show-icon
          />
        </div>
      </a-form>

      <div class="form-actions">
        <a-space>
          <a-button @click="handleCancel">取消</a-button>
          <a-button
            type="primary"
            :loading="submitting"
            :disabled="!formData.reason"
            data-testid="submit-btn"
            @click="handleSubmit"
          >
            提交申诉
          </a-button>
        </a-space>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { UploadOutlined } from '@ant-design/icons-vue';
import { Modal, message } from 'ant-design-vue';
import { createAppeal } from '/@/api/support/appeal';
import { uploadFile } from '/@/api/sys/upload';

const router = useRouter();
const route = useRoute();

const formData = reactive({
  appealType: undefined as string | undefined,
  relatedId: (route.query.reportId || route.query.punishmentId || '') as string,
  reason: '',
  attachmentUrls: [] as string[],
});

const relatedSummary = ref('从处罚通知或举报详情自动带入');
const fileList = ref<any[]>([]);
const submitting = ref(false);
const appealCount = ref(1); // 从 API 获取

const beforeUpload = (file: File) => {
  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    message.error('文件大小不能超过 10MB');
    return false;
  }
  return true;
};

const handleUpload = async (options: any) => {
  try {
    const res = await uploadFile(options.file);
    formData.attachmentUrls.push(res.result.url);
    options.onSuccess(res);
  } catch (err) {
    options.onError(err);
  }
};

const handleSubmit = async () => {
  if (!formData.appealType) {
    message.warning('请选择申诉类型');
    return;
  }
  if (!formData.reason) {
    message.warning('请填写申诉理由');
    return;
  }

  // 第 3 次申诉确认
  if (appealCount.value >= 3) {
    const confirmed = await new Promise((resolve) => {
      Modal.confirm({
        title: '确认提交',
        content: '这是最后一次申诉机会，确认提交？',
        onOk: () => resolve(true),
        onCancel: () => resolve(false),
      });
    });
    if (!confirmed) return;
  }

  submitting.value = true;
  try {
    await createAppeal(formData);
    message.success('申诉已提交');
    router.push('/user/appeals');
  } catch {
    message.error('提交失败');
  } finally {
    submitting.value = false;
  }
};

const handleCancel = () => {
  router.back();
};

onMounted(() => {
  // TODO: 从 API 获取当前申诉次数
});
</script>

<style scoped lang="less">
.appeal-create-page {
  padding: 16px;

  .appeal-form {
    max-width: 600px;
    margin-top: 16px;
  }

  .appeal-count-tip {
    margin-bottom: 16px;
  }

  .form-actions {
    margin-top: 24px;
  }
}
</style>
```

- [ ] **Step 5: 运行测试确认通过**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecgboot-vue3 && npx vitest run src/views/support/appeal/create.spec.ts --reporter=verbose 2>&1 | tail -20`
Expected: PASS

- [ ] **Step 6: 实现我的申诉列表页**

```vue
<!-- src/views/support/appeal/index.vue -->
<template>
  <div class="appeal-list-page">
    <a-card title="我的申诉">
      <a-form layout="inline" :model="queryParams" class="query-form">
        <a-form-item label="申诉状态">
          <a-select v-model:value="queryParams.status" placeholder="全部" allow-clear style="width: 140px">
            <a-select-option value="">全部</a-select-option>
            <a-select-option value="reviewing">审核中</a-select-option>
            <a-select-option value="approved">已通过</a-select-option>
            <a-select-option value="rejected">已驳回</a-select-option>
            <a-select-option value="withdrawn">已撤回</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table :columns="columns" :data-source="appealList" :loading="loading" :pagination="pagination" row-key="id" @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'status'">
            <a-tag :color="statusColor(record.status)">{{ record.statusLabel }}</a-tag>
          </template>
          <template v-if="column.dataIndex === 'action'">
            <a-space>
              <a @click="handleViewDetail(record)">查看详情</a>
              <a-popconfirm v-if="record.status === 'reviewing'" title="确认撤回该申诉？撤回后不可恢复" @confirm="handleWithdraw(record)">
                <a>撤回</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>

      <a-empty v-if="!loading && appealList.length === 0" description="暂无申诉记录" />
    </a-card>

    <AppealDetailDrawer :visible="drawerVisible" :appeal-id="selectedAppealId" @close="drawerVisible = false" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { getAppealList, withdrawAppeal, type AppealItem, type AppealQueryParams } from '/@/api/support/appeal';
import AppealDetailDrawer from './components/AppealDetailDrawer.vue';

const columns = [
  { title: '申诉编号', dataIndex: 'appealNo', width: 160 },
  { title: '申诉类型', dataIndex: 'appealTypeLabel', width: 120 },
  { title: '关联处罚', dataIndex: 'relatedSummary', ellipsis: true },
  { title: '提交时间', dataIndex: 'createTime', width: 180 },
  { title: '状态', dataIndex: 'status', width: 100 },
  { title: '操作', dataIndex: 'action', width: 150, fixed: 'right' as const },
];

const loading = ref(false);
const appealList = ref<AppealItem[]>([]);
const drawerVisible = ref(false);
const selectedAppealId = ref('');

const queryParams = reactive<AppealQueryParams>({ status: undefined, pageNo: 1, pageSize: 20 });
const pagination = reactive({ current: 1, pageSize: 20, total: 0, showSizeChanger: true, showTotal: (t: number) => `共 ${t} 条` });

const fetchList = async () => {
  loading.value = true;
  try {
    const res = await getAppealList(queryParams);
    appealList.value = res.result.records || [];
    pagination.total = res.result.total || 0;
    pagination.current = queryParams.pageNo || 1;
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => { queryParams.pageNo = 1; fetchList(); };
const handleReset = () => { queryParams.status = undefined; queryParams.pageNo = 1; fetchList(); };
const handleTableChange = (pag: any) => { queryParams.pageNo = pag.current; queryParams.pageSize = pag.pageSize; fetchList(); };
const handleViewDetail = (record: AppealItem) => { selectedAppealId.value = record.id; drawerVisible.value = true; };
const handleWithdraw = async (record: AppealItem) => {
  try { await withdrawAppeal(record.id); message.success('撤回成功'); fetchList(); } catch { message.error('撤回失败'); }
};
const statusColor = (status: string) => ({ reviewing: 'blue', approved: 'green', rejected: 'red', withdrawn: 'default' }[status] || 'default');

onMounted(() => fetchList());
</script>

<style scoped lang="less">
.appeal-list-page { padding: 16px; .query-form { margin-bottom: 16px; } }
</style>
```

- [ ] **Step 7: Commit**

```bash
git add src/views/support/appeal/
git commit -m "feat(support): add appeal system (create, list, detail drawer)"
```

---

## Task 11: 组件与页面 -- 帮助中心

**Files:**
- Create: `src/views/support/help/components/HelpSearch.vue`
- Test: `src/views/support/help/components/HelpSearch.spec.ts`
- Create: `src/views/support/help/components/ArticleFeedback.vue`
- Test: `src/views/support/help/components/ArticleFeedback.spec.ts`
- Create: `src/views/support/help/index.vue`
- Create: `src/views/support/help/article.vue`

- [ ] **Step 1: 编写 HelpSearch 测试**

```typescript
// src/views/support/help/components/HelpSearch.spec.ts
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import HelpSearch from './HelpSearch.vue';

describe('HelpSearch', () => {
  beforeEach(() => { setActivePinia(createPinia()); });

  it('should render search input', () => {
    const wrapper = mount(HelpSearch);
    expect(wrapper.find('input').exists()).toBe(true);
  });

  it('should emit search event on enter', async () => {
    const wrapper = mount(HelpSearch);
    await wrapper.find('input').setValue('如何修改密码');
    await wrapper.find('input').trigger('keydown.enter');
    expect(wrapper.emitted('search')).toBeTruthy();
  });

  it('should debounce search input', async () => {
    vi.useFakeTimers();
    const wrapper = mount(HelpSearch);
    await wrapper.find('input').setValue('a');
    await wrapper.find('input').setValue('ab');
    await wrapper.find('input').setValue('abc');
    vi.advanceTimersByTime(300);
    expect(wrapper.emitted('search')).toHaveLength(1);
    vi.useRealTimers();
  });
});
```

- [ ] **Step 2: 编写 ArticleFeedback 测试**

```typescript
// src/views/support/help/components/ArticleFeedback.spec.ts
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import ArticleFeedback from './ArticleFeedback.vue';

vi.mock('/@/api/support/help', () => ({
  submitArticleFeedback: vi.fn().mockResolvedValue({}),
}));

describe('ArticleFeedback', () => {
  beforeEach(() => { setActivePinia(createPinia()); });

  it('should render helpful/unhelpful buttons', () => {
    const wrapper = mount(ArticleFeedback, { props: { articleId: '1' } });
    expect(wrapper.text()).toContain('有用');
    expect(wrapper.text()).toContain('无用');
  });

  it('should show selected state after clicking helpful', async () => {
    const wrapper = mount(ArticleFeedback, { props: { articleId: '1' } });
    await wrapper.find('[data-testid="helpful-btn"]').trigger('click');
    expect(wrapper.find('[data-testid="helpful-btn"]').classes()).toContain('selected');
  });

  it('should show contact CS prompt after clicking unhelpful', async () => {
    const wrapper = mount(ArticleFeedback, { props: { articleId: '1' } });
    await wrapper.find('[data-testid="unhelpful-btn"]').trigger('click');
    expect(wrapper.text()).toContain('联系客服');
  });

  it('should not allow duplicate feedback', async () => {
    const wrapper = mount(ArticleFeedback, { props: { articleId: '1' } });
    await wrapper.find('[data-testid="helpful-btn"]').trigger('click');
    await wrapper.find('[data-testid="helpful-btn"]').trigger('click');
    // 只应调用一次 API
    const { submitArticleFeedback } = await import('/@/api/support/help');
    expect(submitArticleFeedback).toHaveBeenCalledTimes(1);
  });
});
```

- [ ] **Step 3: 运行测试确认失败**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecgboot-vue3 && npx vitest run src/views/support/help/components/ --reporter=verbose 2>&1 | tail -20`
Expected: FAIL

- [ ] **Step 4: 实现 HelpSearch 组件**

```vue
<!-- src/views/support/help/components/HelpSearch.vue -->
<template>
  <div class="help-search">
    <a-input-search
      v-model:value="keyword"
      placeholder="搜索帮助文章..."
      size="large"
      enter-button="搜索"
      @search="handleSearch"
      @change="handleInput"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const emit = defineEmits<{ (e: 'search', keyword: string): void }>();

const keyword = ref('');
let debounceTimer: ReturnType<typeof setTimeout> | null = null;

const handleSearch = () => {
  emit('search', keyword.value);
};

const handleInput = () => {
  if (debounceTimer) clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => {
    emit('search', keyword.value);
  }, 300);
};
</script>

<style scoped lang="less">
.help-search {
  max-width: 600px;
  margin: 0 auto 32px;
}
</style>
```

- [ ] **Step 5: 实现 ArticleFeedback 组件**

```vue
<!-- src/views/support/help/components/ArticleFeedback.vue -->
<template>
  <div class="article-feedback">
    <div class="feedback-question">这篇文章有帮助吗？</div>
    <a-space>
      <a-button
        :type="selected === 'helpful' ? 'primary' : 'default'"
        :class="{ selected: selected === 'helpful' }"
        data-testid="helpful-btn"
        @click="handleFeedback(true)"
      >
        <like-outlined /> 有用
      </a-button>
      <a-button
        :type="selected === 'unhelpful' ? 'primary' : 'default'"
        :class="{ selected: selected === 'unhelpful' }"
        data-testid="unhelpful-btn"
        @click="handleFeedback(false)"
      >
        <dislike-outlined /> 无用
      </a-button>
    </a-space>
    <div v-if="selected === 'unhelpful'" class="contact-cs">
      <a-alert message="是否联系客服？" type="info" show-icon>
        <template #action>
          <a-button size="small" type="primary" @click="goToCS">联系客服</a-button>
        </template>
      </a-alert>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { LikeOutlined, DislikeOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { submitArticleFeedback } from '/@/api/support/help';

const props = defineProps<{ articleId: string }>();
const router = useRouter();

const selected = ref<string | null>(null);

const handleFeedback = async (helpful: boolean) => {
  if (selected.value) return; // 已反馈
  selected.value = helpful ? 'helpful' : 'unhelpful';
  try {
    await submitArticleFeedback(props.articleId, { helpful });
    message.success('感谢您的反馈');
  } catch {
    // 静默处理
  }
};

const goToCS = () => {
  router.push('/customer-service');
};
</script>

<style scoped lang="less">
.article-feedback {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
  text-align: center;

  .feedback-question {
    font-size: 16px;
    margin-bottom: 16px;
    color: #333;
  }

  .contact-cs {
    margin-top: 16px;
  }

  .selected {
    pointer-events: none;
  }
}
</style>
```

- [ ] **Step 6: 实现帮助中心首页**

```vue
<!-- src/views/support/help/index.vue -->
<template>
  <div class="help-center-page">
    <a-card>
      <h2 style="text-align: center; margin-bottom: 24px">帮助中心</h2>

      <HelpSearch @search="handleSearch" />

      <!-- 分类导航 -->
      <div v-if="!searchMode" class="categories">
        <a-row :gutter="[16, 16]">
          <a-col :xs="12" :sm="8" :md="6" v-for="cat in categories" :key="cat.id">
            <a-card hoverable @click="handleCategoryClick(cat)">
              <a-card-meta :title="cat.name" :description="`${cat.articleCount} 篇文章`">
                <template #avatar>
                  <a-avatar :icon="cat.icon" style="background-color: #1890ff" />
                </template>
              </a-card-meta>
            </a-card>
          </a-col>
        </a-row>
      </div>

      <!-- 搜索结果 -->
      <div v-if="searchMode" class="search-results">
        <a-spin :spinning="searchLoading">
          <a-list :data-source="searchResults" v-if="searchResults.length > 0">
            <template #renderItem="{ item }">
              <a-list-item>
                <a @click="goToArticle(item.id)">
                  <span v-html="highlightKeyword(item.title, searchKeyword)"></span>
                </a>
                <template #extra>
                  <span style="color: #999">{{ item.categoryName }}</span>
                </template>
              </a-list-item>
            </template>
          </a-list>
          <a-empty v-else description="未找到相关文章">
            <a-button type="primary" @click="goToCS">联系客服</a-button>
          </a-empty>
        </a-spin>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { searchHelpArticles, getHelpCategories, type HelpCategory, type HelpSearchResult } from '/@/api/support/help';
import HelpSearch from './components/HelpSearch.vue';

const router = useRouter();

const categories = ref<HelpCategory[]>([]);
const searchResults = ref<HelpSearchResult[]>([]);
const searchMode = ref(false);
const searchLoading = ref(false);
const searchKeyword = ref('');

const fetchCategories = async () => {
  const res = await getHelpCategories();
  categories.value = res.result || [];
};

const handleSearch = async (keyword: string) => {
  if (!keyword.trim()) {
    searchMode.value = false;
    return;
  }
  searchMode.value = true;
  searchKeyword.value = keyword;
  searchLoading.value = true;
  try {
    const res = await searchHelpArticles({ keyword });
    searchResults.value = res.result?.records || [];
  } finally {
    searchLoading.value = false;
  }
};

const handleCategoryClick = (cat: HelpCategory) => {
  // 进入分类文章列表
  router.push({ path: '/help', query: { category: cat.id } });
};

const goToArticle = (id: string) => {
  router.push(`/help/article/${id}`);
};

const goToCS = () => {
  router.push('/customer-service');
};

const highlightKeyword = (text: string, keyword: string) => {
  if (!keyword) return text;
  const regex = new RegExp(`(${keyword})`, 'gi');
  return text.replace(regex, '<mark>$1</mark>');
};

onMounted(() => fetchCategories());
</script>

<style scoped lang="less">
.help-center-page {
  padding: 16px;
  max-width: 960px;
  margin: 0 auto;

  .categories {
    margin-top: 24px;
  }

  .search-results {
    margin-top: 24px;

    mark {
      background-color: #fffbe6;
      padding: 0 2px;
    }
  }
}
</style>
```

- [ ] **Step 7: 实现帮助文章详情页**

```vue
<!-- src/views/support/help/article.vue -->
<template>
  <div class="help-article-page">
    <a-card>
      <a-breadcrumb>
        <a-breadcrumb-item><a @click="router.push('/help')">帮助中心</a></a-breadcrumb-item>
        <a-breadcrumb-item>{{ article?.categoryName }}</a-breadcrumb-item>
        <a-breadcrumb-item>{{ article?.title }}</a-breadcrumb-item>
      </a-breadcrumb>

      <a-spin :spinning="loading">
        <template v-if="article">
          <h1>{{ article.title }}</h1>
          <div class="article-content" v-html="renderedContent"></div>

          <ArticleFeedback :article-id="articleId" />
        </template>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getHelpArticleDetail, type HelpArticle } from '/@/api/support/help';
import ArticleFeedback from './components/ArticleFeedback.vue';

const route = useRoute();
const router = useRouter();
const articleId = route.params.id as string;

const article = ref<HelpArticle | null>(null);
const loading = ref(false);

const renderedContent = computed(() => {
  // 简单 Markdown 渲染（实际项目应使用 markdown-it 或类似库）
  return article.value?.content || '';
});

onMounted(async () => {
  loading.value = true;
  try {
    const res = await getHelpArticleDetail(articleId);
    article.value = res.result;
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped lang="less">
.help-article-page {
  padding: 16px;
  max-width: 800px;
  margin: 0 auto;

  h1 {
    margin: 24px 0 16px;
    font-size: 24px;
  }

  .article-content {
    line-height: 1.8;
    font-size: 15px;

    :deep(img) {
      max-width: 100%;
      cursor: pointer;
      border-radius: 4px;
    }
  }
}
</style>
```

- [ ] **Step 8: 运行测试确认通过**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecgboot-vue3 && npx vitest run src/views/support/help/components/ --reporter=verbose 2>&1 | tail -20`
Expected: PASS

- [ ] **Step 9: Commit**

```bash
git add src/views/support/help/
git commit -m "feat(support): add help center (search, feedback, index, article)"
```

---

## Task 12: 组件与页面 -- 更新日志

**Files:**
- Create: `src/views/support/changelog/components/ChangelogTimeline.vue`
- Create: `src/views/support/changelog/components/VersionCard.vue`
- Create: `src/views/support/changelog/index.vue`

- [ ] **Step 1: 实现 ChangelogTimeline 组件**

```vue
<!-- src/views/support/changelog/components/ChangelogTimeline.vue -->
<template>
  <a-timeline>
    <a-timeline-item v-for="version in versions" :key="version.id" :color="getTimelineColor(version)">
      <VersionCard :version="version" :keyword="keyword" />
    </a-timeline-item>
  </a-timeline>
</template>

<script setup lang="ts">
import type { ChangelogVersion } from '/@/api/support/changelog';
import VersionCard from './VersionCard.vue';

defineProps<{
  versions: ChangelogVersion[];
  keyword?: string;
}>();

const getTimelineColor = (_version: ChangelogVersion) => 'blue';
</script>
```

- [ ] **Step 2: 实现 VersionCard 组件**

```vue
<!-- src/views/support/changelog/components/VersionCard.vue -->
<template>
  <div class="version-card">
    <div class="version-header">
      <a-tag color="blue">{{ version.version }}</a-tag>
      <span class="release-date">{{ version.releaseDate }}</span>
    </div>

    <div v-if="version.features.length" class="version-section">
      <a-tag color="green">新增功能</a-tag>
      <ul>
        <li v-for="(item, i) in version.features" :key="i" v-html="highlightText(item)"></li>
      </ul>
    </div>

    <div v-if="version.improvements.length" class="version-section">
      <a-tag color="blue">优化内容</a-tag>
      <ul>
        <li v-for="(item, i) in version.improvements" :key="i" v-html="highlightText(item)"></li>
      </ul>
    </div>

    <div v-if="version.bugfixes.length" class="version-section">
      <a-tag color="orange">修复问题</a-tag>
      <ul>
        <li v-for="(item, i) in version.bugfixes" :key="i" v-html="highlightText(item)"></li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ChangelogVersion } from '/@/api/support/changelog';

const props = defineProps<{
  version: ChangelogVersion;
  keyword?: string;
}>();

const highlightText = (text: string) => {
  if (!props.keyword) return text;
  const regex = new RegExp(`(${props.keyword})`, 'gi');
  return text.replace(regex, '<mark>$1</mark>');
};
</script>

<style scoped lang="less">
.version-card {
  .version-header {
    margin-bottom: 12px;
    display: flex;
    align-items: center;
    gap: 8px;

    .release-date {
      color: #999;
      font-size: 14px;
    }
  }

  .version-section {
    margin-bottom: 8px;

    ul {
      margin-top: 4px;
      padding-left: 20px;

      li {
        line-height: 1.8;
        :deep(mark) {
          background-color: #fffbe6;
          padding: 0 2px;
        }
      }
    }
  }
}
</style>
```

- [ ] **Step 3: 实现更新日志页面**

```vue
<!-- src/views/support/changelog/index.vue -->
<template>
  <div class="changelog-page">
    <a-card>
      <h2 style="text-align: center; margin-bottom: 24px">更新日志</h2>

      <div class="search-area">
        <a-input-search
          v-model:value="keyword"
          placeholder="搜索功能名称..."
          style="max-width: 400px; margin: 0 auto 24px"
          @search="handleSearch"
        />
      </div>

      <a-spin :spinning="loading">
        <ChangelogTimeline :versions="filteredVersions" :keyword="keyword" />
        <a-empty v-if="!loading && filteredVersions.length === 0" description="暂无更新记录" />
      </a-spin>
    </a-card>

    <!-- 新版本提示弹窗 -->
    <a-modal v-model:open="showNewVersionModal" title="新版本更新" @ok="goToChangelog" :footer="null">
      <p>有新版本发布，是否查看更新内容？</p>
      <a-button type="primary" block @click="goToChangelog">查看最新版本更新</a-button>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { getChangelogList, type ChangelogVersion } from '/@/api/support/changelog';
import ChangelogTimeline from './components/ChangelogTimeline.vue';

const versions = ref<ChangelogVersion[]>([]);
const loading = ref(false);
const keyword = ref('');
const showNewVersionModal = ref(false);

const filteredVersions = computed(() => {
  if (!keyword.value) return versions.value;
  return versions.value.filter(
    (v) =>
      v.features.some((f) => f.includes(keyword.value)) ||
      v.improvements.some((f) => f.includes(keyword.value)) ||
      v.bugfixes.some((f) => f.includes(keyword.value))
  );
});

const fetchVersions = async () => {
  loading.value = true;
  try {
    const res = await getChangelogList({});
    versions.value = res.result?.records || [];
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {};

const goToChangelog = () => {
  showNewVersionModal.value = false;
};

onMounted(() => {
  fetchVersions();
  // 检查是否有新版本（首次登录提示）
  const hasSeenNewVersion = sessionStorage.getItem('hasSeenNewVersion');
  if (!hasSeenNewVersion) {
    // TODO: 从 API 检查是否有新版本
    // showNewVersionModal.value = true;
    sessionStorage.setItem('hasSeenNewVersion', 'true');
  }
});
</script>

<style scoped lang="less">
.changelog-page {
  padding: 16px;
  max-width: 800px;
  margin: 0 auto;
}
</style>
```

- [ ] **Step 4: Commit**

```bash
git add src/views/support/changelog/
git commit -m "feat(support): add changelog page with timeline and search"
```

---

## Task 13: 组件 -- ChatMessage 消息气泡

**Files:**
- Create: `src/views/support/customer-service/components/ChatMessage.vue`
- Test: `src/views/support/customer-service/components/ChatMessage.spec.ts`

- [ ] **Step 1: 编写 ChatMessage 测试**

```typescript
// src/views/support/customer-service/components/ChatMessage.spec.ts
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import ChatMessage from './ChatMessage.vue';

describe('ChatMessage', () => {
  beforeEach(() => { setActivePinia(createPinia()); });

  it('should render user message on right', () => {
    const wrapper = mount(ChatMessage, {
      props: { message: { id: '1', senderType: 'user', content: '你好', status: 'sent', messageType: 'text', createTime: '2026-06-01' } },
    });
    expect(wrapper.find('.message-right').exists()).toBe(true);
  });

  it('should render agent message on left', () => {
    const wrapper = mount(ChatMessage, {
      props: { message: { id: '1', senderType: 'agent', content: '您好', status: 'sent', messageType: 'text', createTime: '2026-06-01' } },
    });
    expect(wrapper.find('.message-left').exists()).toBe(true);
  });

  it('should render system message centered', () => {
    const wrapper = mount(ChatMessage, {
      props: { message: { id: '1', senderType: 'system', content: '人工客服已接入', status: 'sent', messageType: 'text', createTime: '2026-06-01' } },
    });
    expect(wrapper.find('.message-system').exists()).toBe(true);
  });

  it('should show retry button for failed messages', () => {
    const wrapper = mount(ChatMessage, {
      props: { message: { id: '1', senderType: 'user', content: '你好', status: 'failed', messageType: 'text', createTime: '2026-06-01' } },
    });
    expect(wrapper.find('[data-testid="retry-btn"]').exists()).toBe(true);
  });

  it('should show loading for sending messages', () => {
    const wrapper = mount(ChatMessage, {
      props: { message: { id: '1', senderType: 'user', content: '你好', status: 'sending', messageType: 'text', createTime: '2026-06-01' } },
    });
    expect(wrapper.find('.message-sending').exists()).toBe(true);
  });

  it('should emit retry on retry button click', async () => {
    const wrapper = mount(ChatMessage, {
      props: { message: { id: '1', senderType: 'user', content: '你好', status: 'failed', messageType: 'text', createTime: '2026-06-01' } },
    });
    await wrapper.find('[data-testid="retry-btn"]').trigger('click');
    expect(wrapper.emitted('retry')).toBeTruthy();
  });
});
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecgboot-vue3 && npx vitest run src/views/support/customer-service/components/ChatMessage.spec.ts --reporter=verbose 2>&1 | tail -20`
Expected: FAIL

- [ ] **Step 3: 实现 ChatMessage 组件**

```vue
<!-- src/views/support/customer-service/components/ChatMessage.vue -->
<template>
  <div :class="messageClass">
    <!-- 系统消息 -->
    <div v-if="message.senderType === 'system'" class="system-content">
      {{ message.content }}
    </div>

    <!-- 普通消息 -->
    <template v-else>
      <div class="bubble" :class="{ 'bubble-sending': message.status === 'sending' }">
        <div class="content">{{ message.content }}</div>
      </div>

      <!-- 消息状态 -->
      <div v-if="message.senderType === 'user'" class="message-status">
        <loading-outlined v-if="message.status === 'sending'" spin />
        <exclamation-circle-filled
          v-if="message.status === 'failed'"
          style="color: #ff4d4f"
        />
        <span v-if="message.status === 'failed'" class="retry-btn" data-testid="retry-btn" @click="$emit('retry')">
          重试
        </span>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { LoadingOutlined, ExclamationCircleFilled } from '@ant-design/icons-vue';

interface MessageData {
  id: string;
  senderType: string;
  content: string;
  status: string;
  messageType: string;
  createTime: string;
}

const props = defineProps<{ message: MessageData }>();
defineEmits<{ (e: 'retry'): void }>();

const messageClass = computed(() => ({
  'chat-message': true,
  'message-left': ['bot', 'agent'].includes(props.message.senderType),
  'message-right': props.message.senderType === 'user',
  'message-system': props.message.senderType === 'system',
  'message-sending': props.message.status === 'sending',
}));
</script>

<style scoped lang="less">
.chat-message {
  display: flex;
  margin-bottom: 12px;
  padding: 0 16px;

  &.message-left {
    justify-content: flex-start;
    .bubble {
      background: #f5f5f5;
      color: #333;
    }
  }

  &.message-right {
    justify-content: flex-end;
    .bubble {
      background: #1890ff;
      color: #fff;
    }
  }

  &.message-system {
    justify-content: center;
    .system-content {
      color: #999;
      font-size: 12px;
      padding: 4px 12px;
      background: #fafafa;
      border-radius: 12px;
    }
  }

  &.message-sending .bubble {
    opacity: 0.6;
  }

  .bubble {
    max-width: 70%;
    padding: 10px 14px;
    border-radius: 12px;
    word-break: break-word;
    line-height: 1.5;
  }

  .message-status {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-top: 4px;
    font-size: 12px;

    .retry-btn {
      color: #1890ff;
      cursor: pointer;
      &:hover {
        text-decoration: underline;
      }
    }
  }
}
</style>
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecgboot-vue3 && npx vitest run src/views/support/customer-service/components/ChatMessage.spec.ts --reporter=verbose 2>&1 | tail -20`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/views/support/customer-service/components/ChatMessage.vue src/views/support/customer-service/components/ChatMessage.spec.ts
git commit -m "feat(support): add ChatMessage component with tests"
```

---

## Task 14: 组件 -- ChatPanel 客服对话面板

**Files:**
- Create: `src/views/support/customer-service/components/ChatPanel.vue`
- Test: `src/views/support/customer-service/components/ChatPanel.spec.ts`

- [ ] **Step 1: 编写 ChatPanel 测试**

```typescript
// src/views/support/customer-service/components/ChatPanel.spec.ts
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import ChatPanel from './ChatPanel.vue';

vi.mock('/@/api/support/customer-service', () => ({
  createServiceSession: vi.fn().mockResolvedValue({ result: { id: 's1', type: 'bot', status: 'bot' } }),
  transferToHuman: vi.fn().mockResolvedValue({ result: { queuePosition: 3 } }),
  sendChatMessage: vi.fn().mockResolvedValue({}),
  closeServiceSession: vi.fn().mockResolvedValue({}),
}));

describe('ChatPanel', () => {
  beforeEach(() => { setActivePinia(createPinia()); });

  it('should auto-start bot session on mount', async () => {
    mount(ChatPanel);
    await new Promise((r) => setTimeout(r, 100));
    const { createServiceSession } = await import('/@/api/support/customer-service');
    expect(createServiceSession).toHaveBeenCalled();
  });

  it('should show transfer button during bot session', () => {
    const wrapper = mount(ChatPanel);
    expect(wrapper.find('[data-testid="transfer-btn"]').exists()).toBe(true);
  });

  it('should show connection lost banner on disconnect', async () => {
    const wrapper = mount(ChatPanel);
    const store = useFeedbackStore();
    store.setWsConnected(false);
    store.setReconnecting(true);
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('连接已断开');
  });

  it('should show queue position when queuing', async () => {
    const wrapper = mount(ChatPanel);
    const store = useFeedbackStore();
    store.queuePosition = 3;
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('排队中');
  });
});
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecgboot-vue3 && npx vitest run src/views/support/customer-service/components/ChatPanel.spec.ts --reporter=verbose 2>&1 | tail -20`
Expected: FAIL

- [ ] **Step 3: 实现 ChatPanel 组件**

```vue
<!-- src/views/support/customer-service/components/ChatPanel.vue -->
<template>
  <div class="chat-panel">
    <!-- 连接状态横幅 -->
    <div v-if="!wsConnected && reconnecting" class="connection-banner banner-warning">
      <warning-outlined /> 连接已断开，正在重连...
    </div>
    <div v-if="!wsConnected && !reconnecting && reconnectTimeout" class="connection-banner banner-error">
      <close-circle-outlined /> 连接失败，请刷新页面重试
      <a-button size="small" type="link" @click="handleRefresh">刷新页面</a-button>
    </div>

    <!-- 顶部栏 -->
    <div class="chat-header">
      <div class="header-info">
        <span v-if="session?.type === 'bot'">智能客服</span>
        <span v-else-if="session?.type === 'human'">{{ session.agentName || '人工客服' }}</span>
        <span v-if="queuePosition !== null" class="queue-badge">排队中 #{{ queuePosition }}</span>
      </div>
      <a-button type="text" @click="handleClose">
        <close-outlined />
      </a-button>
    </div>

    <!-- 消息区域 -->
    <div class="chat-messages" ref="messagesRef">
      <ChatMessage
        v-for="msg in chatMessages"
        :key="msg.id"
        :message="msg"
        @retry="handleRetry(msg)"
      />
    </div>

    <!-- 快捷回复（智能客服阶段） -->
    <div v-if="session?.status === 'bot'" class="quick-replies">
      <a-space wrap>
        <a-button v-for="reply in quickReplies" :key="reply" size="small" @click="handleQuickReply(reply)">
          {{ reply }}
        </a-button>
      </a-space>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input">
      <a-input
        v-model:value="inputText"
        placeholder="输入消息..."
        :disabled="!wsConnected"
        @pressEnter="handleSend"
      />
      <a-button type="primary" :disabled="!inputText.trim() || !wsConnected" @click="handleSend">
        发送
      </a-button>
      <a-button
        v-if="session?.status === 'bot'"
        data-testid="transfer-btn"
        @click="handleTransfer"
      >
        转人工
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { WarningOutlined, CloseCircleOutlined, CloseOutlined } from '@ant-design/icons-vue';
import { message, Modal } from 'ant-design-vue';
import { useFeedbackStore } from '/@/store/modules/feedback';
import { createServiceSession, transferToHuman, sendChatMessage, closeServiceSession } from '/@/api/support/customer-service';
import ChatMessage from './ChatMessage.vue';

const props = defineProps<{ sessionId?: string }>();

const feedbackStore = useFeedbackStore();
const messagesRef = ref<HTMLElement>();
const inputText = ref('');
const session = ref<any>(null);
const reconnectTimeout = ref(false);
let ws: WebSocket | null = null;
let reconnectTimer: ReturnType<typeof setTimeout> | null = null;

const chatMessages = computed(() => feedbackStore.chatMessages);
const wsConnected = computed(() => feedbackStore.wsConnected);
const reconnecting = computed(() => feedbackStore.reconnecting);
const queuePosition = computed(() => feedbackStore.queuePosition);

const quickReplies = ['如何修改密码', '如何举报内容', '积分规则', '联系人工客服'];

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight;
    }
  });
};

const initSession = async () => {
  try {
    const res = await createServiceSession();
    session.value = res.result;
    feedbackStore.addMessage({
      id: Date.now().toString(),
      sessionId: session.value.id,
      senderType: 'bot',
      content: '您好！我是智能客服，请问有什么可以帮助您的？',
      messageType: 'text',
      status: 'sent',
      createTime: new Date().toISOString(),
    });
    connectWebSocket();
  } catch {
    message.error('创建会话失败');
  }
};

const connectWebSocket = () => {
  // WebSocket 连接逻辑（实际实现需根据后端 WebSocket 地址）
  feedbackStore.setWsConnected(true);
  feedbackStore.setReconnecting(false);
};

const handleSend = async () => {
  if (!inputText.value.trim()) return;

  const msgContent = inputText.value;
  inputText.value = '';

  const msgId = Date.now().toString();
  feedbackStore.addMessage({
    id: msgId,
    sessionId: session.value?.id || '',
    senderType: 'user',
    content: msgContent,
    messageType: 'text',
    status: 'sending',
    createTime: new Date().toISOString(),
  });
  scrollToBottom();

  try {
    await sendChatMessage(session.value.id, { content: msgContent, messageType: 'text' });
    feedbackStore.updateMessageStatus(msgId, 'sent');
  } catch {
    feedbackStore.updateMessageStatus(msgId, 'failed');
  }
};

const handleQuickReply = (reply: string) => {
  inputText.value = reply;
  handleSend();
};

const handleTransfer = async () => {
  try {
    const res = await transferToHuman(session.value.id);
    session.value.status = 'queuing';
    feedbackStore.setQueuePosition(res.result.queuePosition);
    feedbackStore.addMessage({
      id: Date.now().toString(),
      sessionId: session.value.id,
      senderType: 'system',
      content: '正在为您转接人工客服，请稍候...',
      messageType: 'text',
      status: 'sent',
      createTime: new Date().toISOString(),
    });
    scrollToBottom();
  } catch {
    message.error('转接失败');
  }
};

const handleRetry = (msg: any) => {
  feedbackStore.updateMessageStatus(msg.id, 'sending');
  handleSend();
};

const handleClose = () => {
  Modal.confirm({
    title: '确认关闭',
    content: '确认结束当前会话？',
    onOk: async () => {
      await closeServiceSession(session.value.id);
      feedbackStore.clearSession();
    },
  });
};

const handleRefresh = () => {
  window.location.reload();
};

watch(
  () => feedbackStore.chatMessages.length,
  () => scrollToBottom()
);

onMounted(() => {
  if (props.sessionId) {
    // 恢复已有会话
  } else {
    initSession();
  }
});

onUnmounted(() => {
  if (ws) ws.close();
  if (reconnectTimer) clearTimeout(reconnectTimer);
});
</script>

<style scoped lang="less">
.chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;

  .connection-banner {
    padding: 8px 16px;
    text-align: center;
    font-size: 13px;

    &.banner-warning {
      background: #fffbe6;
      color: #d48806;
    }
    &.banner-error {
      background: #fff2f0;
      color: #cf1322;
    }
  }

  .chat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    border-bottom: 1px solid #f0f0f0;
    background: #fafafa;

    .queue-badge {
      margin-left: 8px;
      padding: 2px 8px;
      background: #fff7e6;
      border: 1px solid #ffd591;
      border-radius: 4px;
      font-size: 12px;
      color: #d48806;
    }
  }

  .chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 16px 0;
  }

  .quick-replies {
    padding: 8px 16px;
    border-top: 1px solid #f0f0f0;
  }

  .chat-input {
    display: flex;
    gap: 8px;
    padding: 12px 16px;
    border-top: 1px solid #f0f0f0;
    background: #fafafa;
  }
}
</style>
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecgboot-vue3 && npx vitest run src/views/support/customer-service/components/ChatPanel.spec.ts --reporter=verbose 2>&1 | tail -20`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/views/support/customer-service/components/ChatPanel.vue src/views/support/customer-service/components/ChatPanel.spec.ts
git commit -m "feat(support): add ChatPanel component with WebSocket and queue management"
```

---

## Task 15: 组件 -- RatingModal 评分弹窗

**Files:**
- Create: `src/views/support/customer-service/components/RatingModal.vue`
- Test: `src/views/support/customer-service/components/RatingModal.spec.ts`

- [ ] **Step 1: 编写 RatingModal 测试**

```typescript
// src/views/support/customer-service/components/RatingModal.spec.ts
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import RatingModal from './RatingModal.vue';

vi.mock('/@/api/support/customer-service', () => ({
  submitServiceRating: vi.fn().mockResolvedValue({}),
}));

describe('RatingModal', () => {
  beforeEach(() => { setActivePinia(createPinia()); });

  it('should render rating modal', () => {
    const wrapper = mount(RatingModal, { props: { visible: true, sessionId: 's1' } });
    expect(wrapper.find('.ant-modal').exists()).toBe(true);
    expect(wrapper.text()).toContain('服务评价');
  });

  it('should have star rating component', () => {
    const wrapper = mount(RatingModal, { props: { visible: true, sessionId: 's1' } });
    expect(wrapper.find('.ant-rate').exists()).toBe(true);
  });

  it('should emit rated after submission', async () => {
    const wrapper = mount(RatingModal, { props: { visible: true, sessionId: 's1' } });
    // 设置评分
    await wrapper.setData({ score: 5 });
    await wrapper.find('[data-testid="submit-rating"]').trigger('click');
    await wrapper.vm.$nextTick();
    expect(wrapper.emitted('rated')).toBeTruthy();
  });
});
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecgboot-vue3 && npx vitest run src/views/support/customer-service/components/RatingModal.spec.ts --reporter=verbose 2>&1 | tail -20`
Expected: FAIL

- [ ] **Step 3: 实现 RatingModal**

```vue
<!-- src/views/support/customer-service/components/RatingModal.vue -->
<template>
  <a-modal :open="visible" title="服务评价" :footer="null" @cancel="handleClose">
    <div class="rating-modal">
      <div class="rating-section">
        <p>请对本次服务进行评价：</p>
        <a-rate v-model:value="score" :count="5" />
      </div>
      <div class="comment-section">
        <a-textarea v-model:value="comment" placeholder="请输入评价（选填）" :rows="3" :maxlength="200" show-count />
      </div>
      <div class="submit-section">
        <a-button type="primary" :loading="submitting" :disabled="score === 0" data-testid="submit-rating" block @click="handleSubmit">
          提交评价
        </a-button>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { message } from 'ant-design-vue';
import { submitServiceRating } from '/@/api/support/customer-service';

const props = defineProps<{ visible: boolean; sessionId: string }>();
const emit = defineEmits<{ (e: 'close'): void; (e: 'rated'): void }>();

const score = ref(0);
const comment = ref('');
const submitting = ref(false);

const handleSubmit = async () => {
  submitting.value = true;
  try {
    await submitServiceRating(props.sessionId, { score: score.value, comment: comment.value });
    message.success('感谢您的评价');
    emit('rated');
    handleClose();
  } catch {
    message.error('提交失败');
  } finally {
    submitting.value = false;
  }
};

const handleClose = () => emit('close');
</script>

<style scoped lang="less">
.rating-modal {
  .rating-section {
    text-align: center;
    margin-bottom: 16px;

    p {
      margin-bottom: 12px;
      color: #666;
    }
  }

  .comment-section {
    margin-bottom: 16px;
  }
}
</style>
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecgboot-vue3 && npx vitest run src/views/support/customer-service/components/RatingModal.spec.ts --reporter=verbose 2>&1 | tail -20`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/views/support/customer-service/components/RatingModal.vue src/views/support/customer-service/components/RatingModal.spec.ts
git commit -m "feat(support): add RatingModal component with tests"
```

---

## Task 16: 页面 -- 客服对话页与历史记录页

**Files:**
- Create: `src/views/support/customer-service/index.vue`
- Create: `src/views/support/customer-service/history.vue`

- [ ] **Step 1: 实现客服对话页**

```vue
<!-- src/views/support/customer-service/index.vue -->
<template>
  <div class="customer-service-page" :class="{ 'mobile-fullscreen': isMobile }">
    <ChatPanel />
    <RatingModal :visible="showRating" :session-id="closedSessionId" @close="showRating = false" @rated="handleRated" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useFeedbackStore } from '/@/store/modules/feedback';
import ChatPanel from './components/ChatPanel.vue';
import RatingModal from './components/RatingModal.vue';

const feedbackStore = useFeedbackStore();
const showRating = ref(false);
const closedSessionId = ref('');
const isMobile = ref(window.innerWidth < 768);

const handleResize = () => {
  isMobile.value = window.innerWidth < 768;
};

const handleRated = () => {
  showRating.value = false;
};

onMounted(() => {
  window.addEventListener('resize', handleResize);

  // 监听会话结束事件
  const unwatch = feedbackStore.$onAction(({ name }) => {
    if (name === 'clearSession') {
      closedSessionId.value = feedbackStore.currentSession?.id || '';
      showRating.value = true;
    }
  });

  onUnmounted(() => {
    window.removeEventListener('resize', handleResize);
    unwatch();
  });
});
</script>

<style scoped lang="less">
.customer-service-page {
  height: calc(100vh - 120px);
  padding: 16px;

  &.mobile-fullscreen {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 1000;
    padding: 0;
    background: #fff;
  }
}
</style>
```

- [ ] **Step 2: 实现客服历史记录页**

```vue
<!-- src/views/support/customer-service/history.vue -->
<template>
  <div class="service-history-page">
    <a-card title="客服记录">
      <a-table :columns="columns" :data-source="sessionList" :loading="loading" :pagination="pagination" row-key="id" @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'type'">
            <a-tag :color="record.type === 'bot' ? 'blue' : 'green'">
              {{ record.type === 'bot' ? '智能客服' : '人工客服' }}
            </a-tag>
          </template>
          <template v-if="column.dataIndex === 'status'">
            <a-tag :color="record.status === 'closed' ? 'default' : 'processing'">
              {{ record.status === 'closed' ? '已结束' : '进行中' }}
            </a-tag>
          </template>
          <template v-if="column.dataIndex === 'action'">
            <a-space>
              <a @click="handleViewDetail(record)">查看详情</a>
              <a @click="handleContinue(record)">继续咨询</a>
            </a-space>
          </template>
        </template>
      </a-table>

      <a-empty v-if="!loading && sessionList.length === 0" description="暂无客服记录" />
    </a-card>

    <!-- 会话详情抽屉 -->
    <a-drawer :open="drawerVisible" title="会话详情" :width="480" @close="drawerVisible = false">
      <template v-if="selectedSession">
        <div v-if="isExpired(selectedSession.createTime)" class="expired-tip">
          <a-alert message="历史记录仅保留 30 天" type="warning" show-icon />
        </div>
        <a-timeline>
          <a-timeline-item v-for="msg in sessionMessages" :key="msg.id">
            <div :class="{ 'system-msg': msg.senderType === 'system' }">
              <strong v-if="msg.senderType === 'user'">我：</strong>
              <strong v-else-if="msg.senderType !== 'system'">客服：</strong>
              {{ msg.content }}
            </div>
          </a-timeline-item>
        </a-timeline>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getServiceSessionList, getServiceSessionDetail, type ServiceSession, type ChatMessage } from '/@/api/support/customer-service';

const router = useRouter();

const columns = [
  { title: '会话时间', dataIndex: 'createTime', width: 180 },
  { title: '客服类型', dataIndex: 'type', width: 120 },
  { title: '问题摘要', dataIndex: 'summary', ellipsis: true },
  { title: '状态', dataIndex: 'status', width: 100 },
  { title: '操作', dataIndex: 'action', width: 150, fixed: 'right' as const },
];

const loading = ref(false);
const sessionList = ref<ServiceSession[]>([]);
const drawerVisible = ref(false);
const selectedSession = ref<ServiceSession | null>(null);
const sessionMessages = ref<ChatMessage[]>([]);

const pagination = reactive({ current: 1, pageSize: 20, total: 0, showSizeChanger: true, showTotal: (t: number) => `共 ${t} 条` });

const fetchList = async () => {
  loading.value = true;
  try {
    const res = await getServiceSessionList({ pageNo: pagination.current, pageSize: pagination.pageSize });
    sessionList.value = res.result?.records || [];
    pagination.total = res.result?.total || 0;
  } finally {
    loading.value = false;
  }
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchList();
};

const handleViewDetail = async (record: ServiceSession) => {
  selectedSession.value = record;
  const res = await getServiceSessionDetail(record.id);
  sessionMessages.value = res.result?.messages || [];
  drawerVisible.value = true;
};

const handleContinue = (record: ServiceSession) => {
  router.push({ path: '/customer-service', query: { sessionId: record.id } });
};

const isExpired = (createTime: string) => {
  const diff = Date.now() - new Date(createTime).getTime();
  return diff > 30 * 24 * 60 * 60 * 1000;
};

onMounted(() => fetchList());
</script>

<style scoped lang="less">
.service-history-page {
  padding: 16px;

  .expired-tip {
    margin-bottom: 16px;
  }

  .system-msg {
    color: #999;
    text-align: center;
  }
}
</style>
```

- [ ] **Step 3: Commit**

```bash
git add src/views/support/customer-service/index.vue src/views/support/customer-service/history.vue
git commit -m "feat(support): add customer service page and history page"
```

---

## Task 17: 客服入口悬浮按钮

**Files:**
- Create: `src/components/CustomerServiceFloatButton.vue`

- [ ] **Step 1: 实现客服入口悬浮按钮**

```vue
<!-- src/components/CustomerServiceFloatButton.vue -->
<template>
  <div class="cs-float-button" @click="handleClick">
    <a-badge :count="queuePosition" :offset="[-5, 5]">
      <a-button type="primary" shape="circle" size="large">
        <template #icon>
          <customer-service-outlined />
        </template>
      </a-button>
    </a-badge>
    <div v-if="hasActiveSession" class="active-dot"></div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { CustomerServiceOutlined } from '@ant-design/icons-vue';
import { useFeedbackStore } from '/@/store/modules/feedback';

const router = useRouter();
const feedbackStore = useFeedbackStore();

const queuePosition = computed(() => feedbackStore.queuePosition || 0);
const hasActiveSession = computed(() => feedbackStore.currentSession !== null);

const handleClick = () => {
  router.push('/customer-service');
};
</script>

<style scoped lang="less">
.cs-float-button {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 1000;
  cursor: pointer;

  .active-dot {
    position: absolute;
    top: 5px;
    right: 5px;
    width: 10px;
    height: 10px;
    background: #52c41a;
    border-radius: 50%;
    border: 2px solid #fff;
  }
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add src/components/CustomerServiceFloatButton.vue
git commit -m "feat(support): add customer service float button"
```

---

## Task 18: 全量测试与验证

**Files:**
- Modify: 全部已创建文件

- [ ] **Step 1: 运行全量单元测试**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecgboot-vue3 && npx vitest run src/store/modules/feedback.spec.ts src/views/support/ --reporter=verbose 2>&1 | tail -30`
Expected: ALL PASS

- [ ] **Step 2: 验证 TypeScript 编译**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecgboot-vue3 && npx vue-tsc --noEmit 2>&1 | tail -20`
Expected: No errors (or only pre-existing errors)

- [ ] **Step 3: 验证文件结构完整性**

Run: `find /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecgboot-vue3/src/views/support -name "*.vue" -o -name "*.ts" | sort`
Expected: 列出所有已创建的文件

- [ ] **Step 4: Commit 最终状态**

```bash
git add -A
git commit -m "feat(support): complete feedback support frontend implementation

- Report system (modal, list, detail drawer)
- Appeal system (create, list, detail drawer)
- Help center (search, article, feedback)
- Changelog (timeline, search, new version prompt)
- Customer service (chat panel, rating, history)
- useFeedbackStore for state management
- Customer service float button"
```

---

## 验证清单

- [ ] 8.1 全量单元测试 100% 通过
- [ ] 8.2 TypeScript 编译无新增错误
- [ ] 8.3 所有页面组件文件结构完整
- [ ] 8.4 API 层覆盖所有 21 个接口
- [ ] 8.5 useFeedbackStore 状态管理逻辑正确
- [ ] 8.6 组件 Props/Events 接口与 PRD 一致
- [ ] 8.7 响应式布局断点正确（PC >= 1200px, 移动端 < 768px）
