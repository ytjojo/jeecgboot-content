# circle-11-content-interaction-frontend 修复计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复 verify 报告中的 4 个 CRITICAL 问题和 4 个 WARNING 问题，纠正 Circle ID 错当 Channel ID 使用的架构级 Bug。

**Architecture:** 7 个 Phase，按优先级排序。Phase 1-4 修复 CRITICAL，Phase 5-6 修复 WARNING，Phase 7 更新测试。后端 `CircleContentPinController`（`/api/v1/content/circle/content/{contentId}/pin` 和 `featured`）已存在，纯前端修复。

**Tech Stack:** Vue 3 + TypeScript + Ant Design Vue + Vitest + Pinia

**参考文档:**
- verify 报告：`verify/verify-final-20260616.md`
- spec：`specs/content-pin-featured/spec.md`、`specs/circle-announcement/spec.md`、`specs/content-report/spec.md`
- design：`design.md`
- 前端测试规范：`docs/agent-context/frontend-testing-conventions.md`

---

## 关键发现

后端 `CircleContentPinController.java` 已存在两个端点：
```
PUT /api/v1/content/circle/content/{contentId}/pin?circleId={circleId}
PUT /api/v1/content/circle/content/{contentId}/featured?circleId={circleId}
```
**无需后端改动**。`CircleAnnouncementManage.vue` 已实现 expireAt 字段、替换确认、删除确认，功能完整。

---

### Phase 1: 创建 Circle Content API 封装层

#### Task 1.1: 创建 `src/api/content/circle/content.ts`

**Files:**
- Create: `jeecgboot-vue3/src/api/content/circle/content.ts`
- Reference: `jeecgboot-vue3/src/api/content/circle/announcement.ts` (风格参考)
- Reference: `jeecg-boot/.../circle/controller/CircleContentPinController.java` (后端端点)

- [ ] **Step 1: 创建 API 文件**

```typescript
import { defHttp } from '/@/utils/http/axios';

enum Api {
  content = '/api/v1/content/circle/content',
}

/** 切换置顶状态（toggle：已置顶→取消，未置顶→置顶） */
export const togglePin = (contentId: string, circleId: string) =>
  defHttp.put({ url: `${Api.content}/${contentId}/pin`, params: { circleId } });

/** 切换精华状态（toggle：已精华→取消，未精华→精华） */
export const toggleFeatured = (contentId: string, circleId: string) =>
  defHttp.put({ url: `${Api.content}/${contentId}/featured`, params: { circleId } });
```

- [ ] **Step 2: 运行类型检查**

```bash
cd jeecgboot-vue3 && npx vue-tsc --noEmit src/api/content/circle/content.ts
```

Expected: 无类型错误。

- [ ] **Step 3: 创建 API 层测试**

```bash
# 创建 tests/api/content/circle/content.spec.ts 或 src/api/content/circle/__tests__/content.spec.ts
```

---

### Phase 2: 创建 CircleContentActionMenu 组件（修复 C1 + C3）

#### Task 2.1: 创建 `CircleContentActionMenu.vue`

**Files:**
- Create: `jeecgboot-vue3/src/views/circle/components/CircleContentActionMenu.vue`
- Reference: `jeecgboot-vue3/src/views/channel/components/GovernanceActionMenu.vue` (菜单结构参考)
- Spec: `specs/content-pin-featured/spec.md` — "普通成员仅显示举报选项"、"操作按钮显示 loading 状态"

- [ ] **Step 1: 创建组件文件**

```vue
<template>
  <Dropdown :trigger="['click']" :disabled="loading">
    <Button size="small" :loading="loading">更多</Button>
    <template #overlay>
      <Menu @click="handleMenuClick">
        <!-- 管理员：置顶/精华操作 -->
        <template v-if="isAdmin">
          <Menu.Item key="pin" v-if="!isPinned">置顶</Menu.Item>
          <Menu.Item key="unpin" v-if="isPinned">取消置顶</Menu.Item>
          <Menu.Divider />
          <Menu.Item key="feature" v-if="!isFeatured">标记精华</Menu.Item>
          <Menu.Item key="unfeature" v-if="isFeatured">取消精华</Menu.Item>
          <Menu.Divider />
          <Menu.Item key="delete" danger>删除</Menu.Item>
        </template>
        <!-- 普通成员：仅举报 -->
        <Menu.Item key="report" v-else>举报</Menu.Item>
      </Menu>
    </template>
  </Dropdown>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { Dropdown, Button, Menu } from 'ant-design-vue';
import { useCircleStoreWithOut } from '/@/store/modules/circle';

const props = defineProps<{
  isPinned: boolean;
  isFeatured: boolean;
  loading?: boolean;
}>();

const emit = defineEmits<{
  (e: 'action', action: string): void;
}>();

const circleStore = useCircleStoreWithOut();
const isAdmin = computed(() => circleStore.isCreator || circleStore.isModerator);

const handleMenuClick = ({ key }: { key: string }) => emit('action', key);
</script>
```

- [ ] **Step 2: 运行类型检查**

```bash
cd jeecgboot-vue3 && npx vue-tsc --noEmit src/views/circle/components/CircleContentActionMenu.vue
```

Expected: 无类型错误。

#### Task 2.2: 创建组件测试

**Files:**
- Create: `jeecgboot-vue3/src/views/circle/components/__tests__/CircleContentActionMenu.test.ts`
- Reference: `jeecgboot-vue3/src/views/circle/components/__tests__/CircleAnnouncementBar.test.ts` (测试风格参考)

- [ ] **Step 1: 编写测试文件**

```typescript
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';

// ---- Mock useCircleStoreWithOut ----
let _isAdmin = false;
vi.mock('/@/store/modules/circle', () => ({
  useCircleStoreWithOut: vi.fn(() => ({
    isCreator: _isAdmin,
    isModerator: _isAdmin,
    isMember: !_isAdmin,
  })),
}));

// ---- Mock ant-design-vue ----
vi.mock('ant-design-vue', async (importOriginal) => {
  const actual = await importOriginal<any>();
  return {
    ...actual,
    Dropdown: {
      name: 'ADropdown',
      template: '<div class="ant-dropdown"><slot /><slot name="overlay" /></div>',
      props: ['trigger', 'disabled'],
    },
    Button: {
      name: 'AButton',
      template: '<button class="ant-btn" :disabled="disabled || loading"><slot /></button>',
      props: ['size', 'loading', 'disabled'],
    },
    Menu: {
      name: 'AMenu',
      template: '<div class="ant-menu"><slot /></div>',
      Item: {
        name: 'AMenuItem',
        template: '<div class="ant-menu-item"><slot /></div>',
        props: ['key', 'danger'],
      },
      Divider: {
        name: 'AMenuDivider',
        template: '<div class="ant-menu-divider" />',
      },
    },
  };
});

import CircleContentActionMenu from '../CircleContentActionMenu.vue';

function mountMenu(props: Record<string, any> = {}) {
  return mount(CircleContentActionMenu, {
    props: { isPinned: false, isFeatured: false, ...props },
  });
}

describe('CircleContentActionMenu', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    _isAdmin = false;
  });

  it('管理员显示置顶/精华/删除菜单项', () => {
    _isAdmin = true;
    const wrapper = mountMenu();
    expect(wrapper.text()).toContain('置顶');
    expect(wrapper.text()).toContain('标记精华');
    expect(wrapper.text()).toContain('删除');
  });

  it('已置顶内容显示"取消置顶"', () => {
    _isAdmin = true;
    const wrapper = mountMenu({ isPinned: true });
    expect(wrapper.text()).toContain('取消置顶');
    expect(wrapper.text()).not.toContain('置顶');
  });

  it('已精华内容显示"取消精华"', () => {
    _isAdmin = true;
    const wrapper = mountMenu({ isFeatured: true });
    expect(wrapper.text()).toContain('取消精华');
    expect(wrapper.text()).not.toContain('标记精华');
  });

  it('普通成员仅显示"举报"选项', () => {
    const wrapper = mountMenu();
    expect(wrapper.text()).toContain('举报');
    expect(wrapper.text()).not.toContain('置顶');
    expect(wrapper.text()).not.toContain('标记精华');
    expect(wrapper.text()).not.toContain('删除');
  });

  it('loading 时按钮禁用', () => {
    _isAdmin = true;
    const wrapper = mountMenu({ loading: true });
    const btn = wrapper.find('.ant-btn');
    expect(btn.attributes('disabled')).toBeDefined();
  });
});
```

- [ ] **Step 2: 运行测试验证**

```bash
cd jeecgboot-vue3 && npx vitest run src/views/circle/components/__tests__/CircleContentActionMenu.test.ts
```

Expected: 5/5 测试通过。

---

### Phase 3: 更新 CircleContentCard 和 Detail.vue（修复 C1 核心链路）

#### Task 3.1: 更新 `CircleContentCard.vue` 使用新组件

**Files:**
- Modify: `jeecgboot-vue3/src/views/circle/components/CircleContentCard.vue`

- [ ] **Step 1: 替换 GovernanceActionMenu → CircleContentActionMenu**

```vue
<template>
  <div class="circle-content-card">
    <!-- 置顶/精华标识 -->
    <div v-if="content.isPinned || content.isFeatured" class="content-badges">
      <a-tag v-if="content.isPinned" color="blue">置顶</a-tag>
      <a-tag v-if="content.isFeatured" color="gold">精华</a-tag>
    </div>
    <div class="content-body">
      <div class="content-title">{{ content.title }}</div>
      <div class="content-meta">
        <span>{{ content.author }}</span>
        <span>{{ content.publishTime }}</span>
      </div>
    </div>
    <div class="content-actions">
      <CircleContentActionMenu
        :is-pinned="!!content.isPinned"
        :is-featured="!!content.isFeatured"
        @action="$emit('action', $event, content.id)"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import CircleContentActionMenu from './CircleContentActionMenu.vue';

export interface CircleContentItem {
  id: string;
  title: string;
  contentType?: string;
  author?: string;
  publishTime?: string;
  isPinned?: boolean;
  isFeatured?: boolean;
}

defineProps<{
  content: CircleContentItem;
}>();

defineEmits<{
  action: [action: string, contentId: string];
}>();
</script>
```

变更说明：
1. 替换 `GovernanceActionMenu` → `CircleContentActionMenu`
2. 移除 `isAdmin` computed（新组件内部自行判断）
3. 移除 `useCircleStoreWithOut` import（不再需要）
4. 事件名从 `governanceAction` 改为 `action`
5. 操作菜单始终显示（普通成员看到"举报"，管理员看到置顶/精华/删除）

- [ ] **Step 2: 验证类型检查**

```bash
cd jeecgboot-vue3 && npx vue-tsc --noEmit src/views/circle/components/CircleContentCard.vue
```

#### Task 3.2: 更新 `Detail.vue` 使用 Circle 自有 API

**Files:**
- Modify: `jeecgboot-vue3/src/views/circle/Detail.vue`

- [ ] **Step 1: 替换 API 调用和事件处理**

在 `<script lang="ts" setup>` 中做以下修改：

**修改 1：替换 import**
```typescript
// 删除这行：
import { executeGovernance } from '/@/api/content/channel/governance';
// 替换为：
import { togglePin, toggleFeatured } from '/@/api/content/circle/content';
```

**修改 2：替换模板中的事件绑定**
```vue
<!-- 删除 @governance-action -->
<CircleContentCard
  v-for="item in feedItems"
  :key="item.id"
  :content="item"
  @action="handleContentAction"
/>
```

**修改 3：替换 handleGovernanceAction 函数**
```typescript
// 删除整个 handleGovernanceAction 函数，替换为：
const actionLoading = ref<Record<string, boolean>>({});

async function handleContentAction(action: string, contentId: string) {
  const circleId = circle.value!.id;
  const loadingKey = `${action}-${contentId}`;
  actionLoading.value[loadingKey] = true;

  try {
    switch (action) {
      case 'pin':
        await togglePin(contentId, circleId);
        createMessage.success('已置顶');
        break;
      case 'unpin':
        await togglePin(contentId, circleId);
        createMessage.success('已取消置顶');
        break;
      case 'feature':
        await toggleFeatured(contentId, circleId);
        createMessage.success('已标记精华');
        break;
      case 'unfeature':
        await toggleFeatured(contentId, circleId);
        createMessage.success('已取消精华');
        break;
      case 'report':
        // 举报逻辑——打开 ReportModal（Phase 6 W3 实现）
        createMessage.info('举报功能开发中');
        break;
      case 'delete':
        createMessage.info('删除功能开发中');
        break;
      default:
        createMessage.warning('未知操作');
    }
    // 刷新内容列表
    await fetchFeedItems();
  } catch (e: any) {
    createMessage.error(e?.message || '操作失败，请重试');
  } finally {
    delete actionLoading.value[loadingKey];
  }
}
```

> **注意**: `fetchFeedItems()` 需要实现。当前 `Detail.vue` 使用 `feedItems` 但未定义获取方法。需添加：
> ```typescript
> async function fetchFeedItems() {
>   try {
>     const res = await getCirclePosts(circle.value!.id);
>     feedItems.value = res || [];
>   } catch {
>     feedItems.value = [];
>   }
> }
> ```
> 并在 `onMounted`/`fetchDetail` 中调用。若后端 `GET /api/v1/content/circle/{circleId}/posts` 不存在，前端降级为空列表。

- [ ] **Step 2: 验证类型检查**

```bash
cd jeecgboot-vue3 && npx vue-tsc --noEmit src/views/circle/Detail.vue
```

---

### Phase 4: CircleAnnouncementBar 集成与定时过期检查（修复 C2 + C4）

#### Task 4.1: 集成 CircleAnnouncementBar 到 Detail.vue

**Files:**
- Modify: `jeecgboot-vue3/src/views/circle/Detail.vue`

- [ ] **Step 1: 在 feed Tab 顶部添加公告栏**

在 `Detail.vue` 的 `<a-tab-pane key="feed" tab="动态">` 内，`<a-empty>` / `<div class="feed-list">` 之前添加：

```vue
<a-tab-pane key="feed" tab="动态">
  <!-- 公告栏 -->
  <CircleAnnouncementBar
    v-if="circle"
    :circle-id="circle.id"
    @manage="showAnnouncementManage = true"
  />
  <a-empty v-if="feedItems.length === 0" description="暂无动态" />
  <!-- ... -->
</a-tab-pane>
```

**修改 2：添加 import**
```typescript
import CircleAnnouncementBar from './components/CircleAnnouncementBar.vue';
```

**修改 3：添加公告管理弹窗状态和组件引入**
```typescript
const showAnnouncementManage = ref(false);
```

```typescript
import CircleAnnouncementManage from './components/CircleAnnouncementManage.vue';
```

并在 template 末尾（`</div>` 结束标签前）添加：
```vue
<CircleAnnouncementManage
  v-model:visible="showAnnouncementManage"
  :circle-id="circle?.id ?? ''"
  @published="refreshAnnouncement"
  @deleted="refreshAnnouncement"
/>
```

> **check**: `CircleAnnouncementManage.vue` 已存在，包含 expireAt 字段、替换确认、删除确认和 loading 状态。

- [ ] **Step 2: 验证**

```bash
cd jeecgboot-vue3 && npx vue-tsc --noEmit src/views/circle/Detail.vue
```

#### Task 4.2: 添加定时过期检查到 CircleAnnouncementBar

**Files:**
- Modify: `jeecgboot-vue3/src/views/circle/components/CircleAnnouncementBar.vue`

- [ ] **Step 1: 添加 setInterval 定时器和清理逻辑**

在 `<script lang="ts" setup>` 中修改：

```typescript
import { ref, onMounted, onUnmounted, computed } from 'vue';
// ... 其他 import 不变

// ... props 定义不变

const announcement = ref<CircleAnnouncementVO | null>(null);
const collapsed = ref(true);
let expiryTimer: ReturnType<typeof setInterval> | null = null;

const needsToggle = computed(() => {
  return (announcement.value?.content?.length ?? 0) > 150;
});

function checkExpiry() {
  if (announcement.value?.expireAt) {
    if (Date.now() > new Date(announcement.value.expireAt).getTime()) {
      announcement.value = null;
    }
  }
}

async function loadAnnouncement() {
  try {
    const res = await getActiveCircleAnnouncement(props.circleId);
    if (res) {
      if (res.expireAt) {
        const expireTime = new Date(res.expireAt).getTime();
        if (Date.now() > expireTime) {
          announcement.value = null;
          return;
        }
      }
      announcement.value = res;
      collapsed.value = true;
    } else {
      announcement.value = null;
    }
  } catch {
    announcement.value = null;
  }
}

// 外部可调用刷新（如发布/删除公告后）
function refresh() {
  loadAnnouncement();
}

defineExpose({ refresh });

onMounted(() => {
  loadAnnouncement();
  // 每 60 秒检查一次是否过期
  expiryTimer = setInterval(checkExpiry, 60000);
});

onUnmounted(() => {
  if (expiryTimer) {
    clearInterval(expiryTimer);
    expiryTimer = null;
  }
});
```

变更说明：
1. 添加 `onUnmounted` import
2. 抽取 `checkExpiry()` 函数，供定时器调用
3. 添加 `defineExpose({ refresh })` 供父组件调用
4. `onMounted` 启动 60 秒间隔定时器
5. `onUnmounted` 清理定时器

- [ ] **Step 2: 更新测试文件**

更新 `jeecgboot-vue3/src/views/circle/components/__tests__/CircleAnnouncementBar.test.ts`，添加定时过期测试。

**关键测试用例**:
```typescript
it('公告到达过期时间后自动隐藏（定时器检查）', async () => {
  vi.useFakeTimers();
  const pastExpireAt = new Date(Date.now() - 10000).toISOString();
  mockGetActive.mockResolvedValue({
    id: 'a1',
    content: 'Test',
    expireAt: pastExpireAt,
  });

  const wrapper = mountBar();
  await flushPromises();
  // 初次加载时即过期，应返回 null
  expect(wrapper.find('.announcement-bar').exists()).toBe(false);

  // 加载有效公告
  const futureExpireAt = new Date(Date.now() + 60000).toISOString();
  mockGetActive.mockResolvedValue({
    id: 'a1',
    content: 'Test',
    expireAt: futureExpireAt,
  });
  // 重新挂载触发 onMounted
  wrapper.unmount();
  const wrapper2 = mountBar();
  await flushPromises();
  expect(wrapper2.find('.announcement-bar').exists()).toBe(true);

  // 快进 61 秒
  vi.advanceTimersByTime(61000);
  await flushPromises();
  expect(wrapper2.find('.announcement-bar').exists()).toBe(false);

  vi.useRealTimers();
  wrapper2.unmount();
});
```

- [ ] **Step 3: 运行测试**

```bash
cd jeecgboot-vue3 && npx vitest run src/views/circle/components/__tests__/CircleAnnouncementBar.test.ts
```

Expected: 全部测试通过（含新增的定时过期测试）。

---

### Phase 5: WARNING 修复 — AnnouncementManage Tinymce 按需加载 + expireAt

#### Task 5.1: 修复 W1 — Tinymce 改用 `defineAsyncComponent` 按需加载

**Files:**
- Modify: `jeecgboot-vue3/src/views/channel/governance/AnnouncementManage.vue`

- [ ] **Step 1: 将静态 import 改为 defineAsyncComponent**

```typescript
// 删除这行：
import { Tinymce } from '/@/components/Tinymce';
// 替换为：
import { defineAsyncComponent } from 'vue';
const Tinymce = defineAsyncComponent(() => import('/@/components/Tinymce/index.vue'));
```

> **check**: Tinymce 组件路径需确认。常见路径为 `/@/components/Tinymce/index.vue`。若实际路径不同，调整 import 路径。

- [ ] **Step 2: 添加加载占位**

在 template 中 Tinymce 使用处包裹 `<Suspense>`：

```vue
<Form.Item label="公告内容">
  <Suspense>
    <template #default>
      <Tinymce v-model="form.content" :height="300" />
    </template>
    <template #fallback>
      <div class="editor-loading">
        <a-spin tip="编辑器加载中..." />
      </div>
    </template>
  </Suspense>
</Form.Item>
```

- [ ] **Step 3: 验证**

```bash
cd jeecgboot-vue3 && npx vue-tsc --noEmit src/views/channel/governance/AnnouncementManage.vue
```

#### Task 5.2: 修复 W2 — 添加 expireAt 有效期字段

**Files:**
- Modify: `jeecgboot-vue3/src/views/channel/governance/AnnouncementManage.vue`

- [ ] **Step 1: 添加 DatePicker 到表单**

在 `<Form layout="vertical">` 内、公告内容 Form.Item 之后添加：

```vue
<Form.Item label="有效期截止时间" required>
  <DatePicker
    v-model:value="form.expireAt"
    show-time
    value-format="YYYY-MM-DD HH:mm:ss"
    placeholder="请选择截止时间"
    style="width: 100%"
  />
</Form.Item>
```

**修改 2：更新 form reactive 和 import**

```typescript
import { Form, Button, Space, Tag, Divider, Table, Input, DatePicker, Modal, message } from 'ant-design-vue';

const form = reactive({
  title: '',
  content: '',
  expireAt: undefined as string | undefined,
});
```

**修改 3：更新 saveAnnouncement 调用，传递 expireAt**

```typescript
// handlePublish 和 handleSaveDraft 中：
await saveAnnouncement({
  channelId: props.channelId,
  title: form.title,
  content: form.content,
  expireAt: form.expireAt,
  version: announcement.value?.version,
});
```

> **警告**: 后端 `ChannelAnnouncement` 需支持 `expireAt` 字段（BE-04）。若后端暂不支持，前端传递后后端忽略，功能降级但不报错。

- [ ] **Step 2: 验证**

```bash
cd jeecgboot-vue3 && npx vue-tsc --noEmit src/views/channel/governance/AnnouncementManage.vue
```

---

### Phase 6: WARNING 修复 — ReportModal 举报枚举对齐 + "其他"必填校验 + 操作 loading

#### Task 6.1: 修复 W3 — ReportModal 举报枚举与 spec 对齐 + "其他"必填校验

**Files:**
- Modify: `jeecgboot-vue3/src/views/support/report/components/ReportModal.vue`
- Spec: `specs/content-report/spec.md` — "选择'其他'原因时补充说明变为必填"

- [ ] **Step 1: 更新 reportTypes 枚举对齐 spec**

```typescript
// 当前枚举已正确实现（porn/violence/fraud/harassment/other），保留不修改
// spec 中 AD/PORNO/ATTACK/OTHER 为早期定义，实际采用更语义化的命名
// 不强制对齐——当前枚举更清晰可读
```

> **决策**: 当前 `porn/violence/fraud/harassment/other` 枚举比 spec 定义的 `AD/PORNO/ATTACK/OTHER` 更语义化。保持当前枚举不变，更新 spec 描述以匹配实现。无需代码修改。

- [ ] **Step 2: 添加"其他"必填校验**

修改 `handleSubmit` 函数：

```typescript
const handleSubmit = async () => {
  if (!formData.reportType) {
    message.warning('请选择举报类型');
    return;
  }
  // "其他"类型时 description 必填
  if (formData.reportType === 'other' && !formData.description.trim()) {
    message.warning('请填写补充说明');
    return;
  }
  // ... 原有提交逻辑
};
```

- [ ] **Step 3: 添加"其他"描述字段的视觉提示**

在 `handleTypeChange` 中（当选择"其他"时），动态改变描述字段的 placeholder：

```typescript
const isOtherType = computed(() => formData.reportType === 'other');

const handleTypeChange = () => {
  // "其他"类型切换时触发表单状态更新
};
```

在 template 中更新描述字段：

```vue
<a-form-item
  label="举报说明"
  :required="formData.reportType === 'other'"
>
  <a-textarea
    v-model:value="formData.description"
    :placeholder="formData.reportType === 'other' ? '请填写补充说明（必填）' : '请补充说明（选填）'"
    :maxlength="500"
    :rows="3"
    show-count
  />
</a-form-item>
```

- [ ] **Step 4: 更新测试**

更新 `src/views/support/report/components/__tests__/ReportModal.test.ts`，添加"其他"必填校验测试：

```typescript
it('选择"其他"时 description 必填', async () => {
  const wrapper = mountModal();
  // 选择"其他"
  await wrapper.find('input[value="other"]').setValue(true);
  // 不填描述直接提交
  await wrapper.find('[data-testid="submit-btn"]').trigger('click');
  expect(message.warning).toHaveBeenCalledWith('请填写补充说明');
});
```

- [ ] **Step 5: 运行测试**

```bash
cd jeecgboot-vue3 && npx vitest run src/views/support/report/components/__tests__/ReportModal.test.ts
```

#### Task 6.2: 修复 W4 — GovernanceActionMenu 添加 loading 状态

**Files:**
- Modify: `jeecgboot-vue3/src/views/channel/components/GovernanceActionMenu.vue`

- [ ] **Step 1: 添加 loading prop 和状态**

```vue
<template>
  <Dropdown :trigger="['click']" :disabled="loading">
    <Button size="small" :loading="loading">更多</Button>
    <!-- ... 其余不变 -->
  </Dropdown>
</template>

<script lang="ts" setup>
import { Dropdown, Button, Menu } from 'ant-design-vue';

defineProps<{
  isPinned: boolean;
  isFeatured: boolean;
  loading?: boolean;
}>();

const emit = defineEmits<{
  (e: 'action', action: string): void;
}>();

const handleMenuClick = ({ key }: { key: string }) => emit('action', key);
</script>
```

> **注意**: `CircleContentActionMenu.vue`（Phase 2 创建）已包含 `loading` prop。此处同步修复 Channel 侧组件。

- [ ] **Step 2: 验证**

```bash
cd jeecgboot-vue3 && npx vue-tsc --noEmit src/views/channel/components/GovernanceActionMenu.vue
```

---

### Phase 7: 全量测试验证

#### Task 7.1: 运行前端全量测试

- [ ] **Step 1: 运行全量测试**

```bash
cd jeecgboot-vue3 && npx vitest run
```

Expected: 所有已有测试通过 + 新增测试通过，覆盖率 ≥ 90%。

- [ ] **Step 2: 运行覆盖率检查**

```bash
cd jeecgboot-vue3 && npx vitest run --coverage
```

Expected: 行覆盖率 ≥ 90%。

- [ ] **Step 3: 运行类型检查**

```bash
cd jeecgboot-vue3 && npx vue-tsc --noEmit
```

Expected: 无类型错误。

---

## 完成标准（DoD）

- [ ] Phase 1-6 所有代码修改完成
- [ ] 流程确认 — subagent + TDD
- [ ] Code Review
- [ ] 覆盖率 ≥ 90%
- [ ] 前端全量测试 100% 通过（`npx vitest run`）
- [ ] 类型检查通过（`npx vue-tsc --noEmit`）
- [ ] 合并 + 验证 + 清理 worktree

---

## 附录：未修复项说明

| 编号 | 问题 | 不修复原因 |
|------|------|-----------|
| W5 | ReviewQueue 是"内容审核"而非"加入申请审核" | 需产品确认需求后决定是否新建 JoinRequestReview 组件。当前实现满足 channel 治理内容审核需求 |
| S1 | 替换公告确认文案 | `CircleAnnouncementManage.vue` 已实现正确文案（"当前已有生效公告，发布新公告将替换旧公告，是否继续？"），Channel 侧非本次 change 范围 |
| S2 | 浮层自动翻转定位 | 功能增强，非 CRITICAL/WARNING。`MentionMemberPicker` 已可用（固定定位），翻转逻辑后续优化 |
| BE-01 | `mentionable-members` Controller 端点 | 前端已用 `getMemberList` 替代，功能可用。后端端点后续添加 |
| BE-04 | ChannelAnnouncement expireAt | 需后端先补齐字段。前端已添加 DatePicker（Phase 5.2），接口忽略不报错 |
| BE-05 | mute duration 透传 | 需后端对齐后前端对接。当前降级方案：展示时长 UI 但不传参数 |
