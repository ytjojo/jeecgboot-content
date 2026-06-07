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
      <Table v-if="!isMobile" :dataSource="inviteList" :columns="inviteColumns" :loading="inviteLoading" :pagination="false" style="margin-top: 12px">
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

      <!-- 移动端卡片视图 -->
      <div v-else class="mobile-card-list">
        <div v-for="item in inviteList" :key="item.id" class="mobile-card">
          <div class="mobile-card-header">
            <span class="mobile-card-code">{{ item.code || item.link }}</span>
            <Tag :color="getInviteStatusColor(item.status)">{{ getInviteStatusText(item.status) }}</Tag>
          </div>
          <div class="mobile-card-body">
            <div class="mobile-card-field">类型：{{ item.type === 'CODE' ? '邀请码' : '邀请链接' }}</div>
            <div class="mobile-card-field">有效期：{{ item.expireTime || '永久' }}</div>
            <div class="mobile-card-field">已用/总次数：{{ item.usage }}</div>
          </div>
          <div class="mobile-card-actions">
            <Button type="primary" size="small" @click="handleCopyInvite(item)">复制</Button>
            <Button danger size="small" @click="handleRevokeInvite(item)">撤销</Button>
          </div>
        </div>
      </div>

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
  import { ref, reactive, computed, watch } from 'vue';
  import { Radio, Switch, InputNumber, Button, Table, Tag, Space, Drawer, Form, DatePicker, Empty } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useBreakpoint } from '/@/hooks/event/useBreakpoint';
  import { sizeEnum } from '/@/enums/breakpointEnum';

  const { screenRef } = useBreakpoint();
  const isMobile = computed(() => {
    const s = screenRef.value;
    return s === sizeEnum.XS || s === sizeEnum.SM;
  });
  import { updateJoinMethod } from '/@/api/content/channelPrivacy';
  import { createInvite, getInviteList, revokeInvite } from '/@/api/content/channelInvite';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';

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
      createMessage.error('加入方式更新失败');
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
    copyTextToClipboard(record.code || record.link);
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
.mobile-card-list { display: flex; flex-direction: column; gap: 12px; margin-top: 12px; }
.mobile-card { background: #fff; border: 1px solid #f0f0f0; border-radius: 8px; padding: 12px; }
.mobile-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.mobile-card-code { font-size: 13px; font-family: monospace; word-break: break-all; flex: 1; margin-right: 8px; }
.mobile-card-body { margin-bottom: 8px; }
.mobile-card-field { font-size: 13px; color: #666; line-height: 1.8; }
.mobile-card-actions { display: flex; gap: 8px; justify-content: flex-end; }
@media (max-width: 575px) {
  .mobile-card .ant-btn {
    min-height: 44px;
    min-width: 44px;
  }
}
</style>
