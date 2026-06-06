# 频道隐私、订阅与成员管理 — 前端实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为频道模块补充隐私控制、订阅机制、成员角色和成员治理的前端能力。

**Architecture:** 基于 Vue 3 + Ant Design Vue 4 + Vben Admin 框架，新增 useChannelContext composable 按 channelId 隔离管理频道上下文，通过 defHttp 封装 API 调用，使用乐观更新策略提升操作体验。页面组件使用项目现有的 Form、Table、Modal、Drawer、JVxeTable 等组件。

**Tech Stack:** Vue 3, TypeScript, Ant Design Vue 4, Vben Admin, defHttp, Pinia, Vue Router

---

## Task 1: API 层 — 订阅相关接口

**Files:**
- Create: `jeecgboot-vue3/src/api/content/channelSubscription.ts`
- Test: `jeecgboot-vue3/src/api/content/__tests__/channelSubscription.test.ts`

- [ ] **Step 1: 创建订阅 API 文件**

```typescript
// jeecgboot-vue3/src/api/content/channelSubscription.ts
import { defHttp } from '/@/utils/http/axios';

enum Api {
  subscribe = '/channel/subscription/subscribe',
  unsubscribe = '/channel/subscription/unsubscribe',
  status = '/channel/subscription/status',  // TODO: 后端需添加此端点
  list = '/channel/subscription/list',
  groupCreate = '/channel/subscription/group/create',
  groupRename = '/channel/subscription/group/rename',  // 注意：后端使用 POST /group/rename
  groupDelete = '/channel/subscription/group/delete',
  groupList = '/channel/subscription/group/list',
  reminder = '/channel/subscription/reminder',
  // P2 功能，后续迭代实现
  // moveGroup = '/channel/subscription/move-group',
}

/** 订阅频道 */
export const subscribeChannel = (channelId: string) =>
  defHttp.post({ url: Api.subscribe, data: { channelId } });

/** 取消订阅 */
export const unsubscribeChannel = (channelId: string) =>
  defHttp.post({ url: Api.unsubscribe, data: { channelId } });

/** 查询订阅状态 */
export const getSubscriptionStatus = (channelId: string) =>
  defHttp.get({ url: `${Api.status}/${channelId}` });

/** 订阅列表 */
export const getSubscriptionList = (params?: any) =>
  defHttp.get({ url: Api.list, params });

/** 创建分组 */
export const createSubscriptionGroup = (data: { name: string }) =>
  defHttp.post({ url: Api.groupCreate, data });

/** 重命名分组 */
export const renameSubscriptionGroup = (groupId: string, newName: string) =>
  defHttp.post({ url: Api.groupRename, params: { groupId, newName } });

/** 删除分组 */
export const deleteSubscriptionGroup = (groupId: string) =>
  defHttp.delete({ url: Api.groupDelete, params: { groupId } });

/** 分组列表 */
export const getSubscriptionGroupList = () =>
  defHttp.get({ url: Api.groupList });

/** 更新提醒设置 */
export const updateSubscriptionReminder = (data: { channelId: string; enabled: boolean }) =>
  defHttp.put({ url: Api.reminder, data });

// P2 功能，后续迭代实现
// /** 移动频道到分组 */
// export const moveChannelToGroup = (data: { channelId: string; groupId: string }) =>
//   defHttp.put({ url: Api.moveGroup, data });
```

- [ ] **Step 2: 验证文件创建成功**

Run: `cat jeecgboot-vue3/src/api/content/channelSubscription.ts | head -5`
Expected: 文件存在，包含 import 和 enum 定义

- [ ] **Step 3: Commit**

```bash
git add jeecgboot-vue3/src/api/content/channelSubscription.ts
git commit -m "feat(channel): add subscription API layer"
```

---

## Task 2: API 层 — 成员相关接口

**Files:**
- Create: `jeecgboot-vue3/src/api/content/channelMember.ts`

- [ ] **Step 1: 创建成员 API 文件**

```typescript
// jeecgboot-vue3/src/api/content/channelMember.ts
import { defHttp } from '/@/utils/http/axios';

enum Api {
  joinApply = '/channel/member/join/apply',
  // applicationStatus = '/channel/member/application/status',  // TODO: 后端需添加此端点
  applicationPending = '/channel/member/applications/pending',
  applicationApprove = '/channel/member/applications/approve',
  applicationReject = '/channel/member/applications/reject',
  list = '/channel/member/list',
  search = '/channel/member/search',
  assignRole = '/channel/member/assign-role',
  // 治理相关 API 在 ChannelGovernanceController
  governanceRemove = '/channel/governance/remove',
  governanceMute = '/channel/governance/mute',
  governanceUnmute = '/channel/governance/unmute',
}

/** 提交加入申请 */
export const applyToJoin = (data: { channelId: string; reason: string }) =>
  defHttp.post({ url: Api.joinApply, data });

// TODO: 后端需添加 applicationStatus 端点后再启用
// /** 查询申请状态 */
// export const getApplicationStatus = (channelId: string) =>
//   defHttp.get({ url: `${Api.applicationStatus}/${channelId}` });

/** 待审列表 */
export const getPendingApplications = (params: { channelId: string; [key: string]: any }) =>
  defHttp.get({ url: Api.applicationPending, params });

/** 批准申请（支持批量） */
export const approveApplications = (data: { channelId: string; applicationIds: string[] }) =>
  defHttp.post({ url: Api.applicationApprove, data });

/** 拒绝申请（支持批量） */
export const rejectApplications = (data: { channelId: string; applicationIds: string[]; reason: string }) =>
  defHttp.post({ url: Api.applicationReject, data });

/** 成员列表 */
export const getMemberList = (params: { channelId: string; [key: string]: any }) =>
  defHttp.get({ url: Api.list, params });

/** 修改角色 */
export const updateMemberRole = (data: { channelId: string; memberId: string; role: string }) =>
  defHttp.put({ url: Api.assignRole, data });

/** 移除成员（支持批量） */
export const removeMembers = (data: { channelId: string; memberIds: string[]; reason: string }) =>
  defHttp.post({ url: Api.governanceRemove, data });

/** 禁言成员 */
export const muteMember = (data: { channelId: string; memberId: string; duration: string; reason: string }) =>
  defHttp.post({ url: Api.governanceMute, data });

/** 解除禁言 */
export const unmuteMember = (data: { channelId: string; memberId: string }) =>
  defHttp.post({ url: Api.governanceUnmute, data });
```

- [ ] **Step 2: 验证文件创建成功**

Run: `cat jeecgboot-vue3/src/api/content/channelMember.ts | head -5`
Expected: 文件存在，包含 import 和 enum 定义

- [ ] **Step 3: Commit**

```bash
git add jeecgboot-vue3/src/api/content/channelMember.ts
git commit -m "feat(channel): add member API layer"
```

---

## Task 3: API 层 — 黑名单、邀请、隐私、治理接口

**Files:**
- Create: `jeecgboot-vue3/src/api/content/channelBlacklist.ts`
- Create: `jeecgboot-vue3/src/api/content/channelInvite.ts`
- Create: `jeecgboot-vue3/src/api/content/channelPrivacy.ts`
- Create: `jeecgboot-vue3/src/api/content/channelGovernance.ts`

- [ ] **Step 1: 创建黑名单 API**

```typescript
// jeecgboot-vue3/src/api/content/channelBlacklist.ts
import { defHttp } from '/@/utils/http/axios';

enum Api {
  // ADVISORY-8: RESTful 规范建议使用 HTTP 方法代替路径动词：
  //   add → POST /channel/governance/blacklist
  //   remove → DELETE /channel/governance/blacklist
  // 当前后端路径需确认是否支持 RESTful 风格后再调整
  add = '/channel/governance/blacklist/add',
  remove = '/channel/governance/blacklist/remove',
  list = '/channel/governance/blacklist/list',
}

/** 加入黑名单 */
export const addToBlacklist = (data: { channelId: string; userId: string; reason: string }) =>
  defHttp.post({ url: Api.add, data });

/** 移出黑名单 */
export const removeFromBlacklist = (data: { channelId: string; userId: string }) =>
  defHttp.post({ url: Api.remove, data });

/** 黑名单列表 */
export const getBlacklist = (params: { channelId: string; [key: string]: any }) =>
  defHttp.get({ url: Api.list, params });
```

- [ ] **Step 2: 创建邀请 API**

```typescript
// jeecgboot-vue3/src/api/content/channelInvite.ts
import { defHttp } from '/@/utils/http/axios';

enum Api {
  create = '/channel/invite/create',
  list = '/channel/invite/list',
  revoke = '/channel/invite/revoke',
  use = '/channel/invite/use',
}

/** 创建邀请 */
export const createInvite = (data: { type: string; expireTime: string[]; maxUses: number }) =>
  defHttp.post({ url: Api.create, data });

/** 邀请列表 */
export const getInviteList = (params?: any) =>
  defHttp.get({ url: Api.list, params });

/** 撤销邀请 */
export const revokeInvite = (inviteId: string) =>
  defHttp.post({ url: Api.revoke, data: { inviteId } });

/** 使用邀请加入 */
export const joinByInvite = (inviteCode: string) =>
  defHttp.post({ url: Api.use, data: { inviteCode } });
```

- [ ] **Step 3: 创建隐私设置 API**

```typescript
// jeecgboot-vue3/src/api/content/channelPrivacy.ts
import { defHttp } from '/@/utils/http/axios';

enum Api {
  // ADVISORY-8: RESTful 规范建议使用 HTTP 方法代替路径动词：
  //   updatePrivacy → PUT /channel/privacy
  //   updateJoinMethod → PUT /channel/join-method
  // 当前后端路径需确认是否支持 RESTful 风格后再调整
  updatePrivacy = '/channel/privacy/update',
  updateJoinMethod = '/channel/join-method/update',
}

/** 更新隐私设置 */
export const updateChannelPrivacy = (data: { channelId: string; privacyType: 'PUBLIC' | 'PRIVATE' }) =>
  defHttp.put({ url: Api.updatePrivacy, data });

/** 更新加入方式 */
export const updateJoinMethod = (data: { channelId: string; joinMethod: string; config?: any }) =>
  defHttp.put({ url: Api.updateJoinMethod, data });
```

- [ ] **Step 4: 创建治理日志 API**

```typescript
// jeecgboot-vue3/src/api/content/channelGovernance.ts
import { defHttp } from '/@/utils/http/axios';

enum Api {
  log = '/channel/governance/log',
}

/** 治理日志列表 */
export const getGovernanceLog = (params: { channelId: string; [key: string]: any }) =>
  defHttp.get({ url: Api.log, params });
```

- [ ] **Step 5: 验证所有 API 文件**

Run: `ls jeecgboot-vue3/src/api/content/channel*.ts`
Expected: 列出 channelBlacklist.ts, channelGovernance.ts, channelInvite.ts, channelMember.ts, channelPrivacy.ts, channelSubscription.ts

- [ ] **Step 6: Commit**

```bash
git add jeecgboot-vue3/src/api/content/channelBlacklist.ts jeecgboot-vue3/src/api/content/channelInvite.ts jeecgboot-vue3/src/api/content/channelPrivacy.ts jeecgboot-vue3/src/api/content/channelGovernance.ts
git commit -m "feat(channel): add blacklist, invite, privacy, governance API layers"
```

---

## Task 4: useChannelContext Composable

**Files:**
- Create: `jeecgboot-vue3/src/composables/useChannelContext.ts`

- [ ] **Step 1: 创建 composables 目录和 useChannelContext 文件**

```typescript
// jeecgboot-vue3/src/composables/useChannelContext.ts
import { ref, computed, type Ref, provide, inject } from 'vue';
import { getChannelInfo, getUserChannelRelation } from '/@/api/content/channel';

export interface ChannelInfo {
  id: string;
  name: string;
  privacyType: 'PUBLIC' | 'PRIVATE';
  joinMethod: 'FREE' | 'REVIEW' | 'INVITE';
  isSystem: boolean;
  [key: string]: any;
}

export interface UserChannelRelation {
  isSubscribed: boolean;
  role: string | null;
  isMuted: boolean;
  isBlacklisted: boolean;
  [key: string]: any;
}

const CHANNEL_CONTEXT_KEY = Symbol('channelContext');

export function useChannelContext(channelId: Ref<string>) {
  const channelInfo = ref<ChannelInfo | null>(null);
  const userRelation = ref<UserChannelRelation | null>(null);
  const privacyType = ref<'PUBLIC' | 'PRIVATE'>('PUBLIC');
  const joinMethod = ref<'FREE' | 'REVIEW' | 'INVITE'>('FREE');
  const isSubscribed = ref(false);
  const memberRole = ref<string | null>(null);
  const isMuted = ref(false);
  const isBlacklisted = ref(false);
  const loading = ref(false);
  const channelNotFound = ref(false);
  const loadError = ref(false);

  const canManageMembers = computed(() => {
    const role = userRelation.value?.role;
    return role === 'OWNER' || role === 'ADMIN';
  });

  const canPublish = computed(() => {
    return !!memberRole.value && !isMuted.value && !isBlacklisted.value;
  });

  async function loadContext() {
    loading.value = true;
    channelNotFound.value = false;
    loadError.value = false;
    try {
      const [info, relation] = await Promise.all([
        getChannelInfo(channelId.value),
        getUserChannelRelation(channelId.value),
      ]);
      channelInfo.value = info;
      userRelation.value = relation;
      privacyType.value = info.privacyType;
      joinMethod.value = info.joinMethod;
      isSubscribed.value = relation.isSubscribed;
      memberRole.value = relation.role;
      isMuted.value = relation.isMuted;
      isBlacklisted.value = relation.isBlacklisted;
    } catch (error: any) {
      if (error?.response?.status === 404) {
        channelNotFound.value = true;
      } else {
        loadError.value = true;
      }
    } finally {
      loading.value = false;
    }
  }

  function resetContext() {
    channelInfo.value = null;
    userRelation.value = null;
    privacyType.value = 'PUBLIC';
    joinMethod.value = 'FREE';
    isSubscribed.value = false;
    memberRole.value = null;
    isMuted.value = false;
    isBlacklisted.value = false;
    channelNotFound.value = false;
    loadError.value = false;
  }

  const context = {
    channelInfo, userRelation, privacyType, joinMethod,
    isSubscribed, memberRole, isMuted, isBlacklisted, loading,
    channelNotFound, loadError,
    canManageMembers, canPublish,
    loadContext, resetContext,
  };

  provide(CHANNEL_CONTEXT_KEY, context);

  return context;
}

export function useChannelContextInject() {
  const context = inject<ReturnType<typeof useChannelContext>>(CHANNEL_CONTEXT_KEY);
  if (!context) {
    throw new Error('useChannelContextInject must be used within a component that provides channel context');
  }
  return context;
}
```

- [ ] **Step 2: 验证文件创建成功**

Run: `cat jeecgboot-vue3/src/composables/useChannelContext.ts | head -10`
Expected: 文件存在，包含 import 和 interface 定义

- [ ] **Step 3: Commit**

```bash
git add jeecgboot-vue3/src/composables/useChannelContext.ts
git commit -m "feat(channel): add useChannelContext composable"
```

---

## Task 5: 乐观更新 Hook

**Files:**
- Create: `jeecgboot-vue3/src/hooks/web/useChannelOperation.ts`

- [ ] **Step 1: 创建乐观更新 hook**

```typescript
// jeecgboot-vue3/src/hooks/web/useChannelOperation.ts
import { ref } from 'vue';
import { useMessage } from '/@/hooks/web/useMessage';

interface OptimisticOperationOptions<T> {
  /** 实际执行的 API 调用 */
  apiCall: () => Promise<T>;
  /** 乐观更新：立即执行的状态变更 */
  onOptimistic: () => void;
  /** 成功回调 */
  onSuccess?: (result: T) => void;
  /** 失败回滚 */
  onRollback: () => void;
  /** 成功消息 */
  successMessage?: string;
  /** 失败消息 */
  errorMessage?: string;
}

export function useChannelOperation() {
  const { createMessage } = useMessage();
  const operating = ref(false);

  async function optimisticExecute<T>(options: OptimisticOperationOptions<T>) {
    if (operating.value) return;
    operating.value = true;

    // 乐观更新：立即执行
    options.onOptimistic();

    try {
      const result = await options.apiCall();
      if (options.successMessage) {
        createMessage.success(options.successMessage);
      }
      options.onSuccess?.(result);
    } catch (error) {
      // 失败回滚
      options.onRollback();
      createMessage.error(options.errorMessage || '操作失败，请重试');
    } finally {
      operating.value = false;
    }
  }

  return { operating, optimisticExecute };
}
```

- [ ] **Step 2: 验证文件创建成功**

Run: `cat jeecgboot-vue3/src/hooks/web/useChannelOperation.ts | head -5`
Expected: 文件存在

- [ ] **Step 3: Commit**

```bash
git add jeecgboot-vue3/src/hooks/web/useChannelOperation.ts
git commit -m "feat(channel): add optimistic update hook"
```

---

## Task 6: 隐私设置组件

**Files:**
- Create: `jeecgboot-vue3/src/views/channel/settings/PrivacySettings.vue`

- [ ] **Step 1: 创建隐私设置组件**

```vue
<!-- jeecgboot-vue3/src/views/channel/settings/PrivacySettings.vue -->
<template>
  <div class="privacy-settings">
    <div class="setting-label">频道隐私</div>
    <Skeleton :loading="loading" active :paragraph="{ rows: 2 }">
      <div>
        <Radio.Group v-model:value="currentPrivacy" :disabled="isSystem || saving" @change="handlePrivacyChange">
          <Radio value="PUBLIC">公开</Radio>
          <Radio value="PRIVATE">私有</Radio>
        </Radio.Group>
        <Alert
          v-if="isSystem"
          type="info"
          message="系统频道必须公开，不允许设置为私有"
          show-icon
          :style="{ marginTop: '8px' }"
        />
        <div class="privacy-desc">
          {{ currentPrivacy === 'PUBLIC' ? '频道内容对所有人可见，可被搜索和推荐' : '仅频道成员可浏览受限内容' }}
        </div>
      </div>
    </Skeleton>

    <Modal
      v-model:open="confirmModalVisible"
      :title="confirmTitle"
      :confirmLoading="saving"
      @ok="handleConfirm"
      @cancel="confirmModalVisible = false"
    >
      <p>{{ confirmContent}}</p>
      <template #footer>
        <Button @click="confirmModalVisible = false">取消</Button>
        <Button type="primary" danger :loading="saving" @click="handleConfirm">确认</Button>
      </template>
    </Modal>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, watch } from 'vue';
  import { Radio, Alert, Modal, Button, Skeleton } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { updateChannelPrivacy } from '/@/api/content/channelPrivacy';

  const props = defineProps<{
    channelId: string;
    initialPrivacy: 'PUBLIC' | 'PRIVATE';
    isSystem: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'updated', privacy: 'PUBLIC' | 'PRIVATE'): void;
  }>();

  const { createMessage } = useMessage();
  const currentPrivacy = ref<'PUBLIC' | 'PRIVATE'>(props.initialPrivacy);
  const pendingPrivacy = ref<'PUBLIC' | 'PRIVATE'>('PUBLIC');
  const confirmModalVisible = ref(false);
  const saving = ref(false);
  const loading = ref(false);

  const confirmTitle = computed(() =>
    pendingPrivacy.value === 'PRIVATE' ? '确认设为私有频道？' : '确认设为公开频道？',
  );
  const confirmContent = computed(() =>
    pendingPrivacy.value === 'PRIVATE'
      ? '频道将退出公开搜索和推荐，非成员将无法浏览受限内容。当前订阅者不受影响。'
      : '频道内容将对所有人可见，可被搜索和推荐。',
  );

  watch(() => props.initialPrivacy, (val) => {
    currentPrivacy.value = val;
  });

  function handlePrivacyChange(e: any) {
    pendingPrivacy.value = e.target.value;
    confirmModalVisible.value = true;
  }

  async function handleConfirm() {
    saving.value = true;
    try {
      await updateChannelPrivacy({ channelId: props.channelId, privacyType: pendingPrivacy.value });
      currentPrivacy.value = pendingPrivacy.value;
      confirmModalVisible.value = false;
      createMessage.success('隐私设置已更新');
      emit('updated', currentPrivacy.value);
    } catch {
      // 保留用户选择，不回滚 currentPrivacy
    } finally {
      saving.value = false;
    }
  }
</script>

<style scoped>
.privacy-settings {
  padding: 16px 0;
}
.setting-label {
  font-weight: 500;
  margin-bottom: 12px;
}
.privacy-desc {
  color: #999;
  margin-top: 8px;
  font-size: 13px;
}
</style>
```

- [ ] **Step 2: 验证组件创建成功**

Run: `cat jeecgboot-vue3/src/views/channel/settings/PrivacySettings.vue | head -5`
Expected: 文件存在，包含 `<template>` 标签

- [ ] **Step 3: Commit**

```bash
git add jeecgboot-vue3/src/views/channel/settings/PrivacySettings.vue
git commit -m "feat(channel): add privacy settings component"
```

---

## Task 7: 加入方式配置组件

**Files:**
- Create: `jeecgboot-vue3/src/views/channel/settings/JoinMethodSettings.vue`

- [ ] **Step 1: 创建加入方式配置组件**

```vue
<!-- jeecgboot-vue3/src/views/channel/settings/JoinMethodSettings.vue -->
<template>
  <div class="join-method-settings">
    <div class="setting-label">加入方式</div>
    <Radio.Group v-model:value="currentMethod" :disabled="saving" @change="handleMethodChange">
      <Radio value="FREE">自由加入</Radio>
      <Radio value="REVIEW">审核加入</Radio>
      <Radio value="INVITE">邀请加入</Radio>
    </Radio.Group>

    <!-- 审核加入配置 -->
    <div v-if="currentMethod === 'REVIEW'" class="method-config">
      <div class="config-item">
        <span>允许被拒绝后再次申请</span>
        <Switch v-model:checked="reviewConfig.allowReapply" />
      </div>
      <div v-if="reviewConfig.allowReapply" class="config-item">
        <span>再次申请间隔（小时）</span>
        <InputNumber v-model:value="reviewConfig.reapplyInterval" :min="1" :max="720" />
      </div>
    </div>

    <!-- 邀请加入配置 -->
    <div v-if="currentMethod === 'INVITE'" class="method-config">
      <Button type="primary" @click="inviteDrawerVisible = true">创建邀请</Button>
      <Table :dataSource="inviteList" :columns="inviteColumns" :loading="inviteLoading" :pagination="false" style="margin-top: 12px">
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'status'">
            <Tag :color="getInviteStatusColor(record.status)">{{ getInviteStatusText(record.status) }}</Tag>
          </template>
          <template v-if="column.dataIndex === 'action'">
            <Space>
              <Button type="link" size="small" @click="handleCopyInvite(record)">复制</Button>
              <Button type="link" size="small" danger @click="handleRevokeInvite(record)">撤销</Button>
            </Space>
          </template>
        </template>
      </Table>
      <Empty v-if="!inviteLoading && inviteList.length === 0" description="暂无邀请，点击上方按钮创建" />
    </div>

    <!-- 邀请创建 Drawer -->
    <Drawer v-model:open="inviteDrawerVisible" title="创建邀请" :width="400">
      <Form :model="inviteForm" layout="vertical">
        <Form.Item label="邀请类型">
          <Radio.Group v-model:value="inviteForm.type">
            <Radio value="CODE">邀请码</Radio>
            <Radio value="LINK">邀请链接</Radio>
          </Radio.Group>
        </Form.Item>
        <Form.Item label="有效期">
          <DatePicker.RangePicker v-model:value="inviteForm.expireTime" style="width: 100%" />
        </Form.Item>
        <Form.Item label="可用次数">
          <InputNumber v-model:value="inviteForm.maxUses" :min="1" style="width: 100%" />
        </Form.Item>
      </Form>
      <template #footer>
        <Button @click="inviteDrawerVisible = false">取消</Button>
        <Button type="primary" :loading="creatingInvite" @click="handleCreateInvite">确认创建</Button>
      </template>
    </Drawer>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, watch } from 'vue';
  import { Radio, Switch, InputNumber, Button, Table, Tag, Space, Drawer, Form, DatePicker, Empty } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { updateJoinMethod } from '/@/api/content/channelPrivacy';
  import { createInvite, getInviteList, revokeInvite } from '/@/api/content/channelInvite';
  import { copyToClipboard } from '/@/hooks/web/useCopyToClipboard';

  const props = defineProps<{
    channelId: string;
    initialMethod: 'FREE' | 'REVIEW' | 'INVITE';
  }>();

  const { createMessage } = useMessage();
  const currentMethod = ref(props.initialMethod);
  const saving = ref(false);

  const reviewConfig = reactive({ allowReapply: true, reapplyInterval: 24 });

  const inviteDrawerVisible = ref(false);
  const creatingInvite = ref(false);
  const inviteLoading = ref(false);
  const inviteList = ref<any[]>([]);
  const inviteForm = reactive({ type: 'CODE', expireTime: null, maxUses: 1 });

  const inviteColumns = [
    { title: '邀请码/链接', dataIndex: 'code', key: 'code' },
    { title: '类型', dataIndex: 'type', key: 'type' },
    { title: '有效期', dataIndex: 'expireTime', key: 'expireTime' },
    { title: '已用/总次数', dataIndex: 'usage', key: 'usage' },
    { title: '状态', dataIndex: 'status', key: 'status' },
    { title: '操作', dataIndex: 'action', key: 'action' },
  ];

  watch(() => props.initialMethod, (val) => { currentMethod.value = val; });

  function getInviteStatusColor(status: string) {
    const map: Record<string, string> = { ACTIVE: 'green', EXPIRED: 'default', USED_UP: 'default', REVOKED: 'orange' };
    return map[status] || 'default';
  }

  function getInviteStatusText(status: string) {
    const map: Record<string, string> = { ACTIVE: '有效', EXPIRED: '已过期', USED_UP: '已用完', REVOKED: '已撤销' };
    return map[status] || status;
  }

  async function handleMethodChange() {
    saving.value = true;
    try {
      await updateJoinMethod({ channelId: props.channelId, joinMethod: currentMethod.value });
      createMessage.success('加入方式已更新');
    } catch {
      // 保留当前选择
    } finally {
      saving.value = false;
    }
  }

  async function loadInvites() {
    inviteLoading.value = true;
    try {
      inviteList.value = await getInviteList({ channelId: props.channelId });
    } finally {
      inviteLoading.value = false;
    }
  }

  async function handleCreateInvite() {
    creatingInvite.value = true;
    try {
      await createInvite(inviteForm);
      createMessage.success('邀请创建成功');
      inviteDrawerVisible.value = false;
      await loadInvites();
    } finally {
      creatingInvite.value = false;
    }
  }

  function handleCopyInvite(record: any) {
    copyToClipboard(record.code || record.link);
    createMessage.success('已复制到剪贴板');
  }

  async function handleRevokeInvite(record: any) {
    await revokeInvite(record.id);
    createMessage.success('邀请已撤销');
    await loadInvites();
  }

  watch(currentMethod, (val) => {
    if (val === 'INVITE') loadInvites();
  }, { immediate: true });
</script>

<style scoped>
.join-method-settings { padding: 16px 0; }
.setting-label { font-weight: 500; margin-bottom: 12px; }
.method-config { margin-top: 16px; padding: 12px; background: #fafafa; border-radius: 4px; }
.config-item { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
</style>
```

- [ ] **Step 2: 验证组件创建成功**

Run: `cat jeecgboot-vue3/src/views/channel/settings/JoinMethodSettings.vue | head -5`
Expected: 文件存在

- [ ] **Step 3: Commit**

```bash
git add jeecgboot-vue3/src/views/channel/settings/JoinMethodSettings.vue
git commit -m "feat(channel): add join method settings component"
```

---

## Task 8: 订阅按钮组件

**Files:**
- Create: `jeecgboot-vue3/src/views/channel/components/SubscribeButton.vue`

- [ ] **Step 1: 创建订阅按钮状态机组件**

```vue
<!-- jeecgboot-vue3/src/views/channel/components/SubscribeButton.vue -->
<template>
  <div class="subscribe-button-wrapper">
    <!-- 未订阅公开频道 -->
    <Button v-if="state === 'idle'" type="primary" :loading="operating" @click="handleSubscribe">
      订阅
    </Button>

    <!-- 已订阅 -->
    <Dropdown v-else-if="state === 'subscribed'" :trigger="['hover']">
      <Button :loading="operating">已订阅</Button>
      <template #overlay>
        <Menu @click="handleUnsubscribeMenu">
          <Menu.Item key="unsubscribe">取消订阅</Menu.Item>
        </Menu>
      </template>
    </Dropdown>

    <!-- 私有频道非成员：申请加入 -->
    <Button v-else-if="state === 'apply'" type="primary" @click="$emit('applyJoin')">
      申请加入
    </Button>

    <!-- 待审核 -->
    <Tooltip v-else-if="state === 'pending'" title="您的申请正在审核中">
      <Button disabled>待审核</Button>
    </Tooltip>

    <!-- 冷却期 -->
    <Tooltip v-else-if="state === 'cooldown'" :title="`冷却期剩余 ${cooldownDays} 天`">
      <Button disabled>冷却期剩余 {{ cooldownDays }} 天</Button>
    </Tooltip>

    <!-- 被黑名单 -->
    <span v-else-if="state === 'blacklisted'" class="blacklisted-text">您无法加入此频道</span>

    <!-- 已禁言 -->
    <div v-else-if="state === 'muted'" class="muted-state">
      <Tag color="orange">已禁言</Tag>
      <Button v-if="!isSubscribed" type="primary" size="small" @click="handleSubscribe">订阅</Button>
    </div>

    <!-- 取消订阅确认 Modal -->
    <Modal
      v-model:open="unsubscribeModalVisible"
      title="确认取消订阅？"
      @ok="handleConfirmUnsubscribe"
      :confirmLoading="operating"
    >
      <p>取消后您将不再收到该频道的更新推送。</p>
    </Modal>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed } from 'vue';
  import { Button, Dropdown, Menu, Tooltip, Tag, Modal } from 'ant-design-vue';
  import { useChannelOperation } from '/@/hooks/web/useChannelOperation';
  import { subscribeChannel, unsubscribeChannel } from '/@/api/content/channelSubscription';

  type ButtonState = 'idle' | 'subscribed' | 'apply' | 'pending' | 'cooldown' | 'blacklisted' | 'muted';

  const props = defineProps<{
    channelId: string;
    isSubscribed: boolean;
    isMember: boolean;
    isBlacklisted: boolean;
    isMuted: boolean;
    isPrivate: boolean;
    applicationStatus?: string | null;
    cooldownDays?: number;
  }>();

  const emit = defineEmits<{
    (e: 'subscribeChange', subscribed: boolean): void;
    (e: 'applyJoin'): void;
  }>();

  const { operating, optimisticExecute } = useChannelOperation();
  const unsubscribeModalVisible = ref(false);

  const state = computed<ButtonState>(() => {
    if (props.isBlacklisted) return 'blacklisted';
    if (props.isMuted) return 'muted';
    if (props.isSubscribed) return 'subscribed';
    if (props.applicationStatus === 'PENDING') return 'pending';
    if (props.applicationStatus === 'REJECTED' && props.cooldownDays && props.cooldownDays > 0) return 'cooldown';
    if (props.isPrivate && !props.isMember) return 'apply';
    return 'idle';
  });

  function handleSubscribe() {
    optimisticExecute({
      apiCall: () => subscribeChannel(props.channelId),
      onOptimistic: () => emit('subscribeChange', true),
      onRollback: () => emit('subscribeChange', false),
      successMessage: '订阅成功',
      errorMessage: '订阅失败，请重试',
    });
  }

  function handleUnsubscribeMenu({ key }: { key: string }) {
    if (key === 'unsubscribe') {
      unsubscribeModalVisible.value = true;
    }
  }

  function handleConfirmUnsubscribe() {
    optimisticExecute({
      apiCall: () => unsubscribeChannel(props.channelId),
      onOptimistic: () => emit('subscribeChange', false),
      onRollback: () => emit('subscribeChange', true),
      successMessage: '已取消订阅',
      errorMessage: '取消订阅失败，请重试',
    });
    unsubscribeModalVisible.value = false;
  }
</script>

<style scoped>
.subscribe-button-wrapper { display: inline-flex; align-items: center; }
.blacklisted-text { color: #999; font-size: 13px; }
.muted-state { display: flex; align-items: center; gap: 8px; }
</style>
```

- [ ] **Step 2: 验证组件创建成功**

Run: `cat jeecgboot-vue3/src/views/channel/components/SubscribeButton.vue | head -5`
Expected: 文件存在

- [ ] **Step 3: Commit**

```bash
git add jeecgboot-vue3/src/views/channel/components/SubscribeButton.vue
git commit -m "feat(channel): add subscribe button state machine component"
```

---

## Task 9: 申请加入 Modal 组件

**Files:**
- Create: `jeecgboot-vue3/src/views/channel/components/JoinApplyModal.vue`

- [ ] **Step 1: 创建申请加入 Modal**

```vue
<!-- jeecgboot-vue3/src/views/channel/components/JoinApplyModal.vue -->
<template>
  <Modal
    v-model:open="visible"
    :title="`申请加入 ${channelName}`"
    :confirmLoading="submitting"
    :okButtonProps="{ disabled: !isValid }"
    @ok="handleSubmit"
    @cancel="visible = false"
  >
    <Form layout="vertical">
      <Form.Item label="申请理由" :validateStatus="validateStatus" :help="validateHelp">
        <Input.TextArea
          v-model:value="reason"
          :maxlength="200"
          :minlength="10"
          :rows="4"
          placeholder="请输入申请理由（10-200字）"
          @input="handleInput"
        />
        <div class="char-count">{{ reason.length }} / 200</div>
      </Form.Item>
    </Form>
  </Modal>
</template>

<script setup lang="ts">
  import { ref, computed } from 'vue';
  import { Modal, Form, Input } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { applyToJoin } from '/@/api/content/channelMember';

  const props = defineProps<{
    channelId: string;
    channelName: string;
  }>();

  const emit = defineEmits<{
    (e: 'applied'): void;
  }>();

  const { createMessage } = useMessage();
  const visible = ref(false);
  const reason = ref('');
  const submitting = ref(false);

  const isValid = computed(() => reason.value.length >= 10 && reason.value.length <= 200);
  const validateStatus = computed(() => {
    if (reason.value.length === 0) return '';
    return isValid.value ? 'success' : 'error';
  });
  const validateHelp = computed(() => {
    if (reason.value.length === 0) return '';
    if (reason.value.length < 10) return '申请理由至少 10 个字';
    if (reason.value.length > 200) return '申请理由不能超过 200 个字';
    return '';
  });

  function handleInput() {
    // 实时字数统计，无需额外处理
  }

  async function handleSubmit() {
    if (!isValid.value) return;
    submitting.value = true;
    try {
      await applyToJoin({ channelId: props.channelId, reason: reason.value });
      createMessage.success('申请已提交');
      visible.value = false;
      reason.value = '';
      emit('applied');
    } catch {
      createMessage.error('申请提交失败，请重试');
    } finally {
      submitting.value = false;
    }
  }

  function open() {
    visible.value = true;
    reason.value = '';
  }

  defineExpose({ open });
</script>

<style scoped>
.char-count {
  text-align: right;
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}
</style>
```

- [ ] **Step 2: 验证组件创建成功**

Run: `cat jeecgboot-vue3/src/views/channel/components/JoinApplyModal.vue | head -5`
Expected: 文件存在

- [ ] **Step 3: Commit**

```bash
git add jeecgboot-vue3/src/views/channel/components/JoinApplyModal.vue
git commit -m "feat(channel): add join apply modal component"
```

---

## Task 10: 待审队列页面

**Files:**
- Create: `jeecgboot-vue3/src/views/channel/members/PendingApplications.vue`

- [ ] **Step 1: 创建待审队列页面**

```vue
<!-- jeecgboot-vue3/src/views/channel/members/PendingApplications.vue -->
<template>
  <div class="pending-applications">
    <div class="page-header">
      <h3>待审队列 <span class="count">共 {{ total }} 条待审</span></h3>
    </div>

    <div class="filter-bar">
      <RangePicker v-model:value="dateRange" :placeholder="['开始时间', '结束时间']" @change="loadData" />
      <div v-if="selectedRowKeys.length > 0" class="batch-actions">
        <span>已选 {{ selectedRowKeys.length }} 项</span>
        <Button type="link" @click="handleBatchApprove">批量批准</Button>
        <Button type="link" danger @click="handleBatchReject">批量拒绝</Button>
      </div>
    </div>

    <Table
      :dataSource="applicationList"
      :columns="columns"
      :loading="loading"
      :pagination="pagination"
      :rowSelection="{ selectedRowKeys, onChange: onSelectChange }"
      rowKey="id"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'applicant'">
          <Space>
            <Avatar :src="record.avatar" size="small" />
            <span>{{ record.nickname }}</span>
          </Space>
        </template>
        <template v-if="column.dataIndex === 'timeout'">
          <Tag v-if="record.isTimeout" color="orange">超时</Tag>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <Space>
            <Button type="link" size="small" @click="handleApprove(record)">批准</Button>
            <Button type="link" size="small" danger @click="handleReject(record)">拒绝</Button>
          </Space>
        </template>
      </template>
    </Table>

    <!-- 拒绝原因 Modal -->
    <Modal v-model:open="rejectModalVisible" title="拒绝申请" :confirmLoading="rejecting" @ok="handleRejectConfirm">
      <Form layout="vertical">
        <Form.Item label="拒绝原因" required>
          <Input.TextArea v-model:value="rejectReason" :rows="3" placeholder="请输入拒绝原因" />
        </Form.Item>
      </Form>
    </Modal>

    <!-- 批量操作结果 Modal -->
    <Modal v-model:open="resultModalVisible" title="操作结果" :footer="null">
      <p>成功 {{ batchResult.success }} 条，失败 {{ batchResult.failed }} 条</p>
      <ul v-if="batchResult.details.length > 0">
        <li v-for="item in batchResult.details" :key="item.id">
          <span>{{ item.nickname }}</span>
          <Tag :color="item.success ? 'green' : 'red'">{{ item.success ? '成功' : '失败' }}</Tag>
          <span v-if="!item.success" class="error-msg">{{ item.errorMessage }}</span>
        </li>
      </ul>
    </Modal>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue';
  import { Table, Button, Space, Avatar, Tag, Modal, Form, Input, RangePicker, message } from 'ant-design-vue';
  import { getPendingApplications, approveApplications, rejectApplications } from '/@/api/content/channelMember';

  const props = defineProps<{ channelId: string }>();

  const loading = ref(false);
  const applicationList = ref<any[]>([]);
  const total = ref(0);
  const selectedRowKeys = ref<string[]>([]);
  const dateRange = ref<any>(null);

  const pagination = reactive({ current: 1, pageSize: 20, total: 0 });

  const columns = [
    { title: '申请人', dataIndex: 'applicant', key: 'applicant', width: 200 },
    { title: '申请理由', dataIndex: 'reason', key: 'reason', ellipsis: true },
    { title: '申请时间', dataIndex: 'applyTime', key: 'applyTime', width: 180 },
    { title: '超时', dataIndex: 'timeout', key: 'timeout', width: 80 },
    { title: '操作', dataIndex: 'action', key: 'action', width: 150 },
  ];

  const rejectModalVisible = ref(false);
  const rejecting = ref(false);
  const rejectReason = ref('');
  const rejectTarget = ref<any>(null);

  const resultModalVisible = ref(false);
  const batchResult = reactive({ success: 0, failed: 0, details: [] as any[] });

  async function loadData() {
    loading.value = true;
    try {
      const params: any = {
        channelId: props.channelId,
        pageNo: pagination.current,
        pageSize: pagination.pageSize,
      };
      if (dateRange.value) {
        params.startTime = dateRange.value[0];
        params.endTime = dateRange.value[1];
      }
      const res = await getPendingApplications(params);
      applicationList.value = res.records || res;
      total.value = res.total || applicationList.value.length;
      pagination.total = total.value;
    } catch (error: any) {
      if (error?.response?.status === 404) {
        applicationList.value = [];
        total.value = 0;
        message.warning('频道不存在或已被删除');
      } else {
        message.error('加载待审列表失败，请重试');
      }
    } finally {
      loading.value = false;
    }
  }

  function onSelectChange(keys: string[]) {
    selectedRowKeys.value = keys;
  }

  function handleTableChange(pag: any) {
    pagination.current = pag.current;
    pagination.pageSize = pag.pageSize;
    loadData();
  }

  async function handleApprove(record: any) {
    await approveApplications({ channelId: props.channelId, applicationIds: [record.id] });
    message.success('已批准');
    loadData();
  }

  function handleReject(record: any) {
    rejectTarget.value = record;
    rejectReason.value = '';
    rejectModalVisible.value = true;
  }

  async function handleRejectConfirm() {
    if (!rejectReason.value.trim()) {
      message.warning('请输入拒绝原因');
      return;
    }
    rejecting.value = true;
    try {
      await rejectApplications({
        channelId: props.channelId,
        applicationIds: [rejectTarget.value.id],
        reason: rejectReason.value,
      });
      message.success('已拒绝');
      rejectModalVisible.value = false;
      loadData();
    } finally {
      rejecting.value = false;
    }
  }

  const batchOperating = ref(false);

  async function handleBatchApprove() {
    if (batchOperating.value) return;
    batchOperating.value = true;
    try {
      const res = await approveApplications({
        channelId: props.channelId,
        applicationIds: selectedRowKeys.value,
      });
      batchResult.success = res.success;
      batchResult.failed = res.failed;
      batchResult.details = res.details || [];
      resultModalVisible.value = true;
      selectedRowKeys.value = [];
      loadData();
    } finally {
      batchOperating.value = false;
    }
  }

  async function handleBatchReject() {
    rejectTarget.value = null;
    rejectReason.value = '';
    rejectModalVisible.value = true;
  }

  onMounted(loadData);
</script>

<style scoped>
.pending-applications { padding: 16px; }
.page-header { margin-bottom: 16px; }
.count { font-size: 14px; color: #999; font-weight: normal; }
.filter-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.batch-actions { display: flex; align-items: center; gap: 8px; }
.error-msg { color: #f5222d; font-size: 12px; margin-left: 8px; }
</style>
```

- [ ] **Step 2: 验证组件创建成功**

Run: `cat jeecgboot-vue3/src/views/channel/members/PendingApplications.vue | head -5`
Expected: 文件存在

- [ ] **Step 3: Commit**

```bash
git add jeecgboot-vue3/src/views/channel/members/PendingApplications.vue
git commit -m "feat(channel): add pending applications page"
```

---

## Task 11: 成员列表页面

**Files:**
- Create: `jeecgboot-vue3/src/views/channel/members/MemberList.vue`
- Create: `jeecgboot-vue3/src/views/channel/members/RoleAssignModal.vue`
- Create: `jeecgboot-vue3/src/views/channel/members/RemoveMemberModal.vue`
- Create: `jeecgboot-vue3/src/views/channel/members/MuteModal.vue`

- [ ] **Step 1: 创建角色分配 Modal**

```vue
<!-- jeecgboot-vue3/src/views/channel/members/RoleAssignModal.vue -->
<template>
  <Modal v-model:open="visible" title="修改角色" :confirmLoading="loading" @ok="handleConfirm">
    <p>确认将 <strong>{{ memberName }}</strong> 的角色从 <Tag>{{ currentRole }}</Tag> 变更为：</p>
    <Select v-model:value="newRole" style="width: 100%">
      <Select.Option value="ADMIN">管理员</Select.Option>
      <Select.Option value="EDITOR">内容编辑</Select.Option>
      <Select.Option value="MEMBER">普通成员</Select.Option>
    </Select>
  </Modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Modal, Select, Tag } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { updateMemberRole } from '/@/api/content/channelMember';

  const props = defineProps<{ channelId: string }>();
  const emit = defineEmits<{ (e: 'updated'): void }>();

  const { createMessage } = useMessage();
  const visible = ref(false);
  const loading = ref(false);
  const memberId = ref('');
  const memberName = ref('');
  const currentRole = ref('');
  const newRole = ref('ADMIN');

  function open(member: any) {
    memberId.value = member.id;
    memberName.value = member.nickname;
    currentRole.value = member.role;
    newRole.value = member.role;
    visible.value = true;
  }

  async function handleConfirm() {
    loading.value = true;
    try {
      await updateMemberRole({ channelId: props.channelId, memberId: memberId.value, role: newRole.value });
      createMessage.success('角色已更新');
      visible.value = false;
      emit('updated');
    } finally {
      loading.value = false;
    }
  }

  defineExpose({ open });
</script>
```

- [ ] **Step 2: 创建移除成员 Modal**

```vue
<!-- jeecgboot-vue3/src/views/channel/members/RemoveMemberModal.vue -->
<template>
  <Modal v-model:open="visible" title="移除成员" :confirmLoading="loading" @ok="handleConfirm">
    <p>确认将 <strong>{{ memberName }}</strong> 移出频道？移除后 7 天内该用户无法再次加入。</p>
    <Form layout="vertical">
      <Form.Item label="移除原因" required>
        <Input.TextArea v-model:value="reason" :rows="3" placeholder="请输入移除原因" />
      </Form.Item>
    </Form>
  </Modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Modal, Form, Input } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { removeMembers } from '/@/api/content/channelMember';

  const props = defineProps<{ channelId: string }>();
  const emit = defineEmits<{ (e: 'removed'): void }>();

  const { createMessage } = useMessage();
  const visible = ref(false);
  const loading = ref(false);
  const memberIds = ref<string[]>([]);
  const memberName = ref('');
  const reason = ref('');

  function open(members: { id: string; nickname: string }[]) {
    memberIds.value = members.map((m) => m.id);
    memberName.value = members.map((m) => m.nickname).join('、');
    reason.value = '';
    visible.value = true;
  }

  async function handleConfirm() {
    if (!reason.value.trim()) return;
    loading.value = true;
    try {
      await removeMembers({ channelId: props.channelId, memberIds: memberIds.value, reason: reason.value });
      createMessage.success('成员已移除');
      visible.value = false;
      emit('removed');
    } finally {
      loading.value = false;
    }
  }

  defineExpose({ open });
</script>
```

- [ ] **Step 3: 创建禁言 Modal**

```vue
<!-- jeecgboot-vue3/src/views/channel/members/MuteModal.vue -->
<template>
  <Modal v-model:open="visible" title="禁言成员" :confirmLoading="loading" @ok="handleConfirm">
    <Form layout="vertical">
      <!-- 注意：禁言时长值需与后端对齐，后端若使用小时格式则改为 1h/24h/168h/720h -->
      <Form.Item label="禁言时长" required>
        <Select v-model:value="duration" style="width: 100%">
          <Select.Option value="1h">1 小时</Select.Option>
          <Select.Option value="24h">24 小时</Select.Option>
          <Select.Option value="7d">7 天</Select.Option>
          <Select.Option value="30d">30 天</Select.Option>
          <Select.Option value="permanent">永久</Select.Option>
        </Select>
      </Form.Item>
      <Form.Item label="禁言原因" required>
        <Input.TextArea v-model:value="reason" :rows="3" placeholder="请输入禁言原因" />
      </Form.Item>
    </Form>
  </Modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Modal, Form, Input, Select } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { muteMember } from '/@/api/content/channelMember';

  const props = defineProps<{ channelId: string }>();
  const emit = defineEmits<{ (e: 'muted'): void }>();

  const { createMessage } = useMessage();
  const visible = ref(false);
  const loading = ref(false);
  const memberId = ref('');
  const duration = ref('1h');
  const reason = ref('');

  function open(member: { id: string; nickname: string }) {
    memberId.value = member.id;
    duration.value = '1h';
    reason.value = '';
    visible.value = true;
  }

  async function handleConfirm() {
    if (!reason.value.trim()) return;
    loading.value = true;
    try {
      await muteMember({ channelId: props.channelId, memberId: memberId.value, duration: duration.value, reason: reason.value });
      createMessage.success('已禁言');
      visible.value = false;
      emit('muted');
    } finally {
      loading.value = false;
    }
  }

  defineExpose({ open });
</script>
```

- [ ] **Step 4: 创建成员列表页面**

```vue
<!-- jeecgboot-vue3/src/views/channel/members/MemberList.vue -->
<template>
  <div class="member-list">
    <div class="page-header">
      <h3>成员管理 <span class="count">共 {{ total }} 位成员</span></h3>
    </div>

    <div class="filter-bar">
      <Space>
        <Select v-model:value="roleFilter" placeholder="角色筛选" style="width: 120px" @change="loadData" allowClear>
          <Select.Option value="">全部</Select.Option>
          <Select.Option value="OWNER">频道主</Select.Option>
          <Select.Option value="ADMIN">管理员</Select.Option>
          <Select.Option value="EDITOR">内容编辑</Select.Option>
          <Select.Option value="MEMBER">普通成员</Select.Option>
        </Select>
        <Input.Search v-model:value="searchKeyword" placeholder="搜索成员" style="width: 200px" @search="loadData" @change="onSearchChange" />
        <Select v-model:value="sortOrder" style="width: 150px" @change="loadData">
          <Select.Option value="desc">加入时间倒序</Select.Option>
          <Select.Option value="asc">加入时间正序</Select.Option>
        </Select>
      </Space>
      <div v-if="selectedRowKeys.length > 0" class="batch-actions">
        <span>已选 {{ selectedRowKeys.length }} 项</span>
        <Button type="link" danger @click="handleBatchRemove">批量移除</Button>
        <Button type="link" @click="handleBatchMute">批量禁言</Button>
      </div>
    </div>

    <Table
      :dataSource="memberList"
      :columns="columns"
      :loading="loading"
      :pagination="pagination"
      :rowSelection="{ selectedRowKeys, onChange: onSelectChange }"
      rowKey="id"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'member'">
          <Space>
            <Avatar :src="record.avatar" size="small" />
            <span>{{ record.nickname }}</span>
          </Space>
        </template>
        <template v-if="column.dataIndex === 'role'">
          <Tag :color="getRoleColor(record.role)">{{ getRoleText(record.role) }}</Tag>
        </template>
        <template v-if="column.dataIndex === 'governanceStatus'">
          <Tag v-if="record.isMuted" color="orange">已禁言 {{ record.muteEndTime }}</Tag>
          <Tag v-else-if="record.coolingEndTime" color="default">冷却期中</Tag>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <Dropdown v-if="canOperate(record)">
            <Button type="link" size="small">操作</Button>
            <template #overlay>
              <Menu @click="({ key }) => handleAction(key, record)">
                <Menu.Item v-if="canChangeRole" key="changeRole">修改角色</Menu.Item>
                <Menu.Item key="remove">移除</Menu.Item>
                <Menu.Item key="mute">禁言</Menu.Item>
                <Menu.Item key="blacklist">加入黑名单</Menu.Item>
              </Menu>
            </template>
          </Dropdown>
        </template>
      </template>
    </Table>

    <RoleAssignModal ref="roleAssignModalRef" :channelId="channelId" @updated="loadData" />
    <RemoveMemberModal ref="removeMemberModalRef" :channelId="channelId" @removed="loadData" />
    <MuteModal ref="muteModalRef" :channelId="channelId" @muted="loadData" />
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue';
  import { Table, Button, Space, Avatar, Tag, Select, Input, Dropdown, Menu } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getMemberList } from '/@/api/content/channelMember';
  import RoleAssignModal from './RoleAssignModal.vue';
  import RemoveMemberModal from './RemoveMemberModal.vue';
  import MuteModal from './MuteModal.vue';

  const props = defineProps<{
    channelId: string;
    currentRole: string; // 当前用户在频道中的角色
  }>();

  const { createMessage } = useMessage();
  const loading = ref(false);
  const memberList = ref<any[]>([]);
  const total = ref(0);
  const selectedRowKeys = ref<string[]>([]);
  const roleFilter = ref('');
  const searchKeyword = ref('');
  const sortOrder = ref('desc');
  const pagination = reactive({ current: 1, pageSize: 20, total: 0 });

  const roleAssignModalRef = ref();
  const removeMemberModalRef = ref();
  const muteModalRef = ref();

  const canChangeRole = props.currentRole === 'OWNER';

  const columns = [
    { title: '成员', dataIndex: 'member', key: 'member', width: 200 },
    { title: '角色', dataIndex: 'role', key: 'role', width: 120 },
    { title: '加入时间', dataIndex: 'joinTime', key: 'joinTime', width: 180 },
    { title: '贡献数', dataIndex: 'contribution', key: 'contribution', width: 100 },
    { title: '治理状态', dataIndex: 'governanceStatus', key: 'governanceStatus', width: 150 },
    { title: '操作', dataIndex: 'action', key: 'action', width: 100 },
  ];

  function getRoleColor(role: string) {
    const map: Record<string, string> = { OWNER: 'purple', ADMIN: 'blue', EDITOR: 'green', MEMBER: 'default' };
    return map[role] || 'default';
  }

  function getRoleText(role: string) {
    const map: Record<string, string> = { OWNER: '频道主', ADMIN: '管理员', EDITOR: '内容编辑', MEMBER: '普通成员' };
    return map[role] || role;
  }

  function canOperate(record: any) {
    if (record.role === 'OWNER') return false;
    if (props.currentRole === 'EDITOR' || props.currentRole === 'MEMBER') return false;
    return true;
  }

  let searchTimer: any;
  function onSearchChange() {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(loadData, 300);
  }

  async function loadData() {
    loading.value = true;
    try {
      const res = await getMemberList({
        channelId: props.channelId,
        role: roleFilter.value || undefined,
        keyword: searchKeyword.value || undefined,
        sort: sortOrder.value,
        pageNo: pagination.current,
        pageSize: pagination.pageSize,
      });
      memberList.value = res.records || res;
      total.value = res.total || memberList.value.length;
      pagination.total = total.value;
    } catch (error: any) {
      if (error?.response?.status === 404) {
        memberList.value = [];
        total.value = 0;
        createMessage.warning('频道不存在或已被删除');
      } else {
        createMessage.error('加载成员列表失败，请重试');
      }
    } finally {
      loading.value = false;
    }
  }

  function onSelectChange(keys: string[]) {
    selectedRowKeys.value = keys;
  }

  function handleTableChange(pag: any) {
    pagination.current = pag.current;
    pagination.pageSize = pag.pageSize;
    loadData();
  }

  function handleAction(key: string, record: any) {
    switch (key) {
      case 'changeRole':
        roleAssignModalRef.value.open(record);
        break;
      case 'remove':
        removeMemberModalRef.value.open([{ id: record.id, nickname: record.nickname }]);
        break;
      case 'mute':
        muteModalRef.value.open({ id: record.id, nickname: record.nickname });
        break;
      case 'blacklist':
        // TODO: implement blacklist from member list
        break;
    }
  }

  function handleBatchRemove() {
    const members = memberList.value
      .filter((m) => selectedRowKeys.value.includes(m.id))
      .map((m) => ({ id: m.id, nickname: m.nickname }));
    removeMemberModalRef.value.open(members);
  }

  const batchOperating = ref(false);

  function handleBatchMute() {
    if (batchOperating.value) return;
    // TODO: implement batch mute — when implemented, wrap in batchOperating guard:
    // batchOperating.value = true;
    // try { ... } finally { batchOperating.value = false; }
  }

  onMounted(loadData);
</script>

<style scoped>
.member-list { padding: 16px; }
.page-header { margin-bottom: 16px; }
.count { font-size: 14px; color: #999; font-weight: normal; }
.filter-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.batch-actions { display: flex; align-items: center; gap: 8px; }
</style>
```

- [ ] **Step: Commit 成员管理相关组件**

```bash
git add jeecgboot-vue3/src/views/channel/members/
git commit -m "feat(channel): add member list page with role assign, remove, mute modals"
```

---

## Task 12: 订阅列表页面

**Files:**
- Create: `jeecgboot-vue3/src/views/channel/subscription/SubscriptionCard.vue`
- Create: `jeecgboot-vue3/src/views/channel/subscription/SubscriptionList.vue`

- [ ] **Step 1: 创建订阅卡片组件**

```vue
<!-- jeecgboot-vue3/src/views/channel/subscription/SubscriptionCard.vue -->
<template>
  <div class="subscription-card">
    <div class="card-main">
      <Avatar :src="channel.avatar" :size="48" />
      <div class="card-info">
        <div class="card-name">
          {{ channel.name }}
          <Tag v-if="channel.isSystem" color="blue">系统推荐</Tag>
          <Tag v-if="channel.source" color="default">{{ channel.source }}</Tag>
        </div>
        <div class="card-summary">{{ channel.latestSummary }}</div>
      </div>
    </div>
    <div class="card-actions">
      <Switch :checked="channel.reminderEnabled" size="small" @change="handleReminderChange" />
      <Button type="link" size="small" danger @click="handleUnsubscribe">取消订阅</Button>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { Avatar, Tag, Switch, Button } from 'ant-design-vue';

  const props = defineProps<{
    channel: {
      id: string;
      name: string;
      avatar: string;
      latestSummary: string;
      source?: string;
      isSystem: boolean;
      reminderEnabled: boolean;
    };
  }>();

  const emit = defineEmits<{
    (e: 'toggleReminder', channelId: string, enabled: boolean): void;
    (e: 'unsubscribe', channelId: string): void;
  }>();

  function handleReminderChange(checked: boolean) {
    emit('toggleReminder', props.channel.id, checked);
  }

  function handleUnsubscribe() {
    emit('unsubscribe', props.channel.id);
  }
</script>

<style scoped>
.subscription-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin-bottom: 8px;
}
.card-main { display: flex; align-items: center; gap: 12px; flex: 1; }
.card-info { flex: 1; }
.card-name { font-weight: 500; }
.card-summary { color: #999; font-size: 13px; margin-top: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.card-actions { display: flex; align-items: center; gap: 8px; }
</style>
```

- [ ] **Step 2: 创建订阅列表页面**

```vue
<!-- jeecgboot-vue3/src/views/channel/subscription/SubscriptionList.vue -->
<template>
  <div class="subscription-list">
    <div class="page-header">
      <h3>我的订阅</h3>
      <Input.Search v-model:value="searchKeyword" placeholder="搜索频道" style="width: 240px" />
    </div>

    <div class="group-tabs">
      <Tabs v-model:activeKey="activeGroup" @change="loadData">
        <Tabs.TabPane key="all" tab="全部" />
        <Tabs.TabPane key="default" tab="默认分组" />
        <Tabs.TabPane v-for="group in groups" :key="group.id" :tab="group.name" />
      </Tabs>
      <Button type="link" size="small" @click="showCreateGroupModal = true">新建分组</Button>
    </div>

    <Skeleton :loading="loading" active :paragraph="{ rows: 5 }">
      <div v-if="filteredChannels.length === 0" class="empty-state">
        <Empty description="暂无订阅频道">
          <Button type="primary">去发现频道</Button>
        </Empty>
      </div>
      <div v-else class="channel-list">
        <SubscriptionCard
          v-for="channel in filteredChannels"
          :key="channel.id"
          :channel="channel"
          @toggleReminder="handleToggleReminder"
          @unsubscribe="handleUnsubscribe"
        />
      </div>
    </Skeleton>

    <!-- 新建分组 Modal -->
    <Modal v-model:open="showCreateGroupModal" title="新建分组" @ok="handleCreateGroup">
      <Input v-model:value="newGroupName" placeholder="请输入分组名称" />
    </Modal>

    <!-- 取消订阅确认 Modal -->
    <Modal v-model:open="unsubscribeModalVisible" title="确认取消订阅？" @ok="handleConfirmUnsubscribe">
      <p>取消后您将不再收到该频道的更新推送。</p>
    </Modal>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, onMounted } from 'vue';
  import { Tabs, Button, Input, Empty, Modal, Skeleton } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getSubscriptionList, getSubscriptionGroupList, createSubscriptionGroup, updateSubscriptionReminder, unsubscribeChannel } from '/@/api/content/channelSubscription';
  import SubscriptionCard from './SubscriptionCard.vue';

  const { createMessage } = useMessage();
  const loading = ref(false);
  const channels = ref<any[]>([]);
  const groups = ref<any[]>([]);
  const searchKeyword = ref('');
  const activeGroup = ref('all');

  const showCreateGroupModal = ref(false);
  const newGroupName = ref('');

  const unsubscribeModalVisible = ref(false);
  const unsubscribeTarget = ref('');

  const filteredChannels = computed(() => {
    let list = channels.value;
    if (activeGroup.value !== 'all') {
      list = list.filter((c) => c.groupId === activeGroup.value);
    }
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase();
      list = list.filter((c) => c.name.toLowerCase().includes(keyword));
    }
    return list;
  });

  async function loadData() {
    loading.value = true;
    try {
      const [subs, grps] = await Promise.all([
        getSubscriptionList({ groupId: activeGroup.value === 'all' ? undefined : activeGroup.value }),
        getSubscriptionGroupList(),
      ]);
      channels.value = subs;
      groups.value = grps;
    } finally {
      loading.value = false;
    }
  }

  async function handleToggleReminder(channelId: string, enabled: boolean) {
    await updateSubscriptionReminder({ channelId, enabled });
    createMessage.success(enabled ? '已开启提醒' : '已关闭提醒');
  }

  function handleUnsubscribe(channelId: string) {
    unsubscribeTarget.value = channelId;
    unsubscribeModalVisible.value = true;
  }

  async function handleConfirmUnsubscribe() {
    await unsubscribeChannel(unsubscribeTarget.value);
    createMessage.success('已取消订阅');
    unsubscribeModalVisible.value = false;
    loadData();
  }

  async function handleCreateGroup() {
    if (!newGroupName.value.trim()) return;
    await createSubscriptionGroup({ name: newGroupName.value });
    createMessage.success('分组已创建');
    showCreateGroupModal.value = false;
    newGroupName.value = '';
    loadData();
  }

  onMounted(loadData);
</script>

<style scoped>
.subscription-list { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.group-tabs { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.empty-state { padding: 48px 0; text-align: center; }
</style>
```

- [ ] **Step 3: Commit**

```bash
git add jeecgboot-vue3/src/views/channel/subscription/
git commit -m "feat(channel): add subscription list page with cards and groups"
```

---

## Task 13: 黑名单与治理日志页面

**Files:**
- Create: `jeecgboot-vue3/src/views/channel/blacklist/BlacklistPage.vue`
- Create: `jeecgboot-vue3/src/views/channel/governance/GovernanceLog.vue`
- Create: `jeecgboot-vue3/src/views/channel/governance/GovernanceDetailDrawer.vue`

- [ ] **Step 1: 创建黑名单页面**

```vue
<!-- jeecgboot-vue3/src/views/channel/blacklist/BlacklistPage.vue -->
<template>
  <div class="blacklist-page">
    <div class="page-header">
      <h3>黑名单 <span class="count">共 {{ total }} 人</span></h3>
    </div>

    <Table :dataSource="blacklist" :columns="columns" :loading="loading" :pagination="pagination" rowKey="id" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'user'">
          <Space>
            <Avatar :src="record.avatar" size="small" />
            <span>{{ record.nickname }}</span>
          </Space>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <Button type="link" size="small" @click="handleRemove(record)">移出黑名单</Button>
        </template>
      </template>
    </Table>

    <Modal v-model:open="removeModalVisible" title="移出黑名单" :confirmLoading="removing" @ok="handleConfirmRemove">
      <p>确认将 <strong>{{ removeTarget?.nickname }}</strong> 移出黑名单？移出后该用户可按频道当前加入规则重新申请或加入。</p>
    </Modal>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue';
  import { Table, Button, Space, Avatar, Modal } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getBlacklist, removeFromBlacklist } from '/@/api/content/channelBlacklist';

  const props = defineProps<{ channelId: string }>();
  const { createMessage } = useMessage();

  const loading = ref(false);
  const blacklist = ref<any[]>([]);
  const total = ref(0);
  const pagination = reactive({ current: 1, pageSize: 20, total: 0 });

  const removeModalVisible = ref(false);
  const removing = ref(false);
  const removeTarget = ref<any>(null);

  const columns = [
    { title: '用户', dataIndex: 'user', key: 'user', width: 200 },
    { title: '拉黑时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
    { title: '操作人', dataIndex: 'operator', key: 'operator', width: 120 },
    { title: '原因', dataIndex: 'reason', key: 'reason', ellipsis: true },
    { title: '操作', dataIndex: 'action', key: 'action', width: 120 },
  ];

  async function loadData() {
    loading.value = true;
    try {
      const res = await getBlacklist({ channelId: props.channelId, pageNo: pagination.current, pageSize: pagination.pageSize });
      blacklist.value = res.records || res;
      total.value = res.total || blacklist.value.length;
      pagination.total = total.value;
    } finally {
      loading.value = false;
    }
  }

  function handleTableChange(pag: any) {
    pagination.current = pag.current;
    pagination.pageSize = pag.pageSize;
    loadData();
  }

  function handleRemove(record: any) {
    removeTarget.value = record;
    removeModalVisible.value = true;
  }

  async function handleConfirmRemove() {
    removing.value = true;
    try {
      await removeFromBlacklist({ channelId: props.channelId, userId: removeTarget.value.userId });
      createMessage.success('已移出黑名单');
      removeModalVisible.value = false;
      loadData();
    } finally {
      removing.value = false;
    }
  }

  onMounted(loadData);
</script>

<style scoped>
.blacklist-page { padding: 16px; }
.page-header { margin-bottom: 16px; }
.count { font-size: 14px; color: #999; font-weight: normal; }
</style>
```

- [ ] **Step 2: 创建治理详情 Drawer**

```vue
<!-- jeecgboot-vue3/src/views/channel/governance/GovernanceDetailDrawer.vue -->
<template>
  <Drawer v-model:open="visible" title="治理详情" :width="400">
    <Descriptions :column="1" bordered>
      <Descriptions.Item label="操作类型">
        <Tag :color="getActionColor(record?.action)">{{ getActionText(record?.action) }}</Tag>
      </Descriptions.Item>
      <Descriptions.Item label="操作者">{{ record?.operatorName }}</Descriptions.Item>
      <Descriptions.Item label="目标用户">{{ record?.targetUserName }}</Descriptions.Item>
      <Descriptions.Item label="操作时间">{{ record?.createTime }}</Descriptions.Item>
      <Descriptions.Item label="原因">{{ record?.reason || '无' }}</Descriptions.Item>
      <Descriptions.Item label="操作前状态">{{ record?.beforeState }}</Descriptions.Item>
      <Descriptions.Item label="操作后状态">{{ record?.afterState }}</Descriptions.Item>
    </Descriptions>
  </Drawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Drawer, Descriptions, Tag } from 'ant-design-vue';

  const visible = ref(false);
  const record = ref<any>(null);

  function getActionColor(action: string) {
    const map: Record<string, string> = { REMOVE: 'red', MUTE: 'orange', UNMUTE: 'green', BLACKLIST_ADD: 'default', BLACKLIST_REMOVE: 'blue' };
    return map[action] || 'default';
  }

  function getActionText(action: string) {
    const map: Record<string, string> = { REMOVE: '移除', MUTE: '禁言', UNMUTE: '解除禁言', BLACKLIST_ADD: '加入黑名单', BLACKLIST_REMOVE: '移出黑名单' };
    return map[action] || action;
  }

  function open(data: any) {
    record.value = data;
    visible.value = true;
  }

  defineExpose({ open });
</script>
```

- [ ] **Step 3: 创建治理日志页面**

```vue
<!-- jeecgboot-vue3/src/views/channel/governance/GovernanceLog.vue -->
<template>
  <div class="governance-log">
    <div class="page-header">
      <h3>治理日志</h3>
    </div>

    <div class="filter-bar">
      <Space>
        <Select v-model:value="actionFilter" placeholder="操作类型" style="width: 150px" @change="loadData" allowClear>
          <Select.Option value="">全部</Select.Option>
          <Select.Option value="REMOVE">移除</Select.Option>
          <Select.Option value="MUTE">禁言</Select.Option>
          <Select.Option value="UNMUTE">解除禁言</Select.Option>
          <Select.Option value="BLACKLIST_ADD">加入黑名单</Select.Option>
          <Select.Option value="BLACKLIST_REMOVE">移出黑名单</Select.Option>
        </Select>
        <RangePicker v-model:value="dateRange" @change="loadData" />
        <Input.Search v-model:value="operatorSearch" placeholder="搜索操作者" style="width: 180px" @search="loadData" />
      </Space>
    </div>

    <Table :dataSource="logList" :columns="columns" :loading="loading" :pagination="pagination" rowKey="id" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <Tag :color="getActionColor(record.action)">{{ getActionText(record.action) }}</Tag>
        </template>
        <template v-if="column.dataIndex === 'detail'">
          <Button type="link" size="small" @click="handleViewDetail(record)">查看</Button>
        </template>
      </template>
    </Table>

    <GovernanceDetailDrawer ref="detailDrawerRef" />
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue';
  import { Table, Button, Space, Tag, Select, Input, RangePicker } from 'ant-design-vue';
  import { getGovernanceLog } from '/@/api/content/channelGovernance';
  import GovernanceDetailDrawer from './GovernanceDetailDrawer.vue';

  const props = defineProps<{ channelId: string }>();

  const loading = ref(false);
  const logList = ref<any[]>([]);
  const actionFilter = ref('');
  const dateRange = ref<any>(null);
  const operatorSearch = ref('');
  const pagination = reactive({ current: 1, pageSize: 20, total: 0 });
  const detailDrawerRef = ref();

  const columns = [
    { title: '操作类型', dataIndex: 'action', key: 'action', width: 120 },
    { title: '操作者', dataIndex: 'operatorName', key: 'operatorName', width: 120 },
    { title: '目标用户', dataIndex: 'targetUserName', key: 'targetUserName', width: 120 },
    { title: '时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
    { title: '原因', dataIndex: 'reason', key: 'reason', ellipsis: true },
    { title: '详情', dataIndex: 'detail', key: 'detail', width: 80 },
  ];

  function getActionColor(action: string) {
    const map: Record<string, string> = { REMOVE: 'red', MUTE: 'orange', UNMUTE: 'green', BLACKLIST_ADD: 'default', BLACKLIST_REMOVE: 'blue' };
    return map[action] || 'default';
  }

  function getActionText(action: string) {
    const map: Record<string, string> = { REMOVE: '移除', MUTE: '禁言', UNMUTE: '解除禁言', BLACKLIST_ADD: '加入黑名单', BLACKLIST_REMOVE: '移出黑名单' };
    return map[action] || action;
  }

  async function loadData() {
    loading.value = true;
    try {
      const params: any = {
        channelId: props.channelId,
        pageNo: pagination.current,
        pageSize: pagination.pageSize,
      };
      if (actionFilter.value) params.action = actionFilter.value;
      if (dateRange.value) {
        params.startTime = dateRange.value[0];
        params.endTime = dateRange.value[1];
      }
      if (operatorSearch.value) params.operator = operatorSearch.value;
      const res = await getGovernanceLog(params);
      logList.value = res.records || res;
      pagination.total = res.total || logList.value.length;
    } finally {
      loading.value = false;
    }
  }

  function handleTableChange(pag: any) {
    pagination.current = pag.current;
    pagination.pageSize = pag.pageSize;
    loadData();
  }

  function handleViewDetail(record: any) {
    detailDrawerRef.value.open(record);
  }

  onMounted(loadData);
</script>

<style scoped>
.governance-log { padding: 16px; }
.page-header { margin-bottom: 16px; }
.filter-bar { margin-bottom: 16px; }
</style>
```

- [ ] **Step 4: Commit**

```bash
git add jeecgboot-vue3/src/views/channel/blacklist/ jeecgboot-vue3/src/views/channel/governance/
git commit -m "feat(channel): add blacklist and governance log pages"
```

---

## Task 14: ChannelContextProvider 与路由集成

**Files:**
- Create: `jeecgboot-vue3/src/views/channel/components/ChannelContextProvider.vue`
- Modify: 路由配置文件（根据项目实际路由结构注册新路由）

- [ ] **Step 1: 创建 ChannelContextProvider 组件**

```vue
<!-- jeecgboot-vue3/src/views/channel/components/ChannelContextProvider.vue -->
<template>
  <div class="channel-context-provider">
    <slot />
  </div>
</template>

<script setup lang="ts">
  import { toRef, watch, onMounted } from 'vue';
  import { useRoute } from 'vue-router';
  import { useChannelContext } from '/@/composables/useChannelContext';

  const props = defineProps<{ channelId: string }>();

  const channelIdRef = toRef(props, 'channelId');
  const { loadContext, resetContext } = useChannelContext(channelIdRef);

  // 初始加载
  onMounted(loadContext);

  // channelId 变化时重新加载
  watch(channelIdRef, () => {
    resetContext();
    loadContext();
  });
</script>
```

- [ ] **Step 2: 验证组件创建成功**

Run: `cat jeecgboot-vue3/src/views/channel/components/ChannelContextProvider.vue | head -5`
Expected: 文件存在

- [ ] **Step 3: Commit**

```bash
git add jeecgboot-vue3/src/views/channel/components/ChannelContextProvider.vue
git commit -m "feat(channel): add channel context provider component"
```

---

## Task 15: 验证与集成测试

**Files:**
- Test: 各组件和 composable 的测试文件

- [ ] **Step 1: 验证所有文件已创建**

Run: `find jeecgboot-vue3/src/views/channel -name "*.vue" | sort && find jeecgboot-vue3/src/api/content -name "channel*.ts" | sort && ls jeecgboot-vue3/src/composables/useChannelContext.ts jeecgboot-vue3/src/hooks/web/useChannelOperation.ts`
Expected: 列出所有创建的文件

- [ ] **Step 2: 运行 lint 检查**

Run: `cd jeecgboot-vue3 && npx eslint src/views/channel/ src/api/content/channel*.ts src/composables/ src/hooks/web/useChannelOperation.ts --no-error-on-unmatched-pattern 2>&1 | tail -20`
Expected: 无错误或仅有 warning

- [ ] **Step 3: 验证 TypeScript 编译**

Run: `cd jeecgboot-vue3 && npx vue-tsc --noEmit 2>&1 | tail -20`
Expected: 无类型错误

- [ ] **Step 4: 最终 Commit**

```bash
git add -A
git commit -m "feat(channel): complete channel privacy, subscription, and member management frontend"
```
