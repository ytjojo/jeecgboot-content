<template>
  <div class="channel-manage">
    <a-page-header :title="channel?.name || '频道管理'" :back-icon="true" @back="goBack">
      <template #tags>
        <ChannelStatusTag v-if="channel" :status="channel.status" />
      </template>
    </a-page-header>

    <!-- DeleteCooling 通知条 -->
    <a-alert
      v-if="channel?.status === 'DELETE_COOLING'"
      type="warning"
      banner
      class="channel-manage__alert"
    >
      <template #message>
        <span>
          频道正在删除冷静期中，剩余 {{ coolingDays }} 天。
          <a @click="handleCancelDelete">撤销删除</a>
        </span>
      </template>
    </a-alert>

    <!-- 审核拒绝通知条 -->
    <a-alert
      v-if="channel?.status === 'REJECTED' && rejectReason"
      type="error"
      :message="`审核被拒：${rejectReason}`"
      show-icon
      class="channel-manage__alert"
    >
      <template #action>
        <a-button size="small" type="primary" @click="activeTab = 'edit'">重新编辑</a-button>
      </template>
    </a-alert>

    <!-- 审核中通知条 -->
    <a-alert
      v-if="hasPendingReview"
      type="info"
      message="当前有审核中的修改，请审核完成后再编辑"
      show-icon
      class="channel-manage__alert"
    />

    <a-card :bordered="false">
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="overview" tab="概览">
          <a-descriptions :column="{ xs: 1, sm: 2 }" bordered size="small">
            <a-descriptions-item label="频道名称">{{ channel?.name }}</a-descriptions-item>
            <a-descriptions-item label="频道类型">
              <ChannelTypeTag v-if="channel" :type="channel.channelType" />
            </a-descriptions-item>
            <a-descriptions-item label="频道状态">
              <ChannelStatusTag v-if="channel" :status="channel.status" />
            </a-descriptions-item>
            <a-descriptions-item label="分类">{{ channel?.categoryName }}</a-descriptions-item>
            <a-descriptions-item label="简介" :span="2">{{ channel?.description }}</a-descriptions-item>
            <a-descriptions-item label="创建时间">{{ channel?.createdTime }}</a-descriptions-item>
            <a-descriptions-item label="更新时间">{{ channel?.updatedTime }}</a-descriptions-item>
          </a-descriptions>
        </a-tab-pane>

        <a-tab-pane key="edit" tab="编辑信息" :disabled="hasPendingReview">
          <ChannelForm
            ref="editFormRef"
            :channel-type="channel?.channelType || 'personal'"
            :initial-values="channel || {}"
            :is-edit="true"
            :is-system-channel="channel?.channelType === 'system'"
            @submit="handleEditSubmit"
            @cancel="activeTab = 'overview'"
          />
        </a-tab-pane>

        <a-tab-pane key="settings" tab="设置">
          <a-space direction="vertical" style="width: 100%" :size="16">
            <!-- 转让 -->
            <a-card title="频道转让" size="small" v-if="channel?.channelType !== 'system'">
              <p>将频道转让给其他用户，转让后您将降为管理员。</p>
              <a-button @click="showTransferModal = true" :disabled="hasPendingTransfer">发起转让</a-button>
              <span v-if="hasPendingTransfer" class="channel-manage__pending-tip">已有待处理的转让请求</span>
            </a-card>

            <!-- 转让历史 -->
            <a-card title="转让历史" size="small" v-if="transferHistory.length > 0">
              <a-timeline>
                <a-timeline-item
                  v-for="item in transferHistory"
                  :key="item.id"
                  :color="getTransferColor(item.status)"
                >
                  <p>{{ item.fromUserName }} → {{ item.toUserName }}</p>
                  <p class="channel-manage__time">{{ item.createdTime }} · {{ getTransferStatusLabel(item.status) }}</p>
                </a-timeline-item>
              </a-timeline>
            </a-card>

            <!-- 删除 -->
            <a-card title="危险操作" size="small">
              <p>删除频道后将进入冷静期，冷静期内可撤销。</p>
              <a-popconfirm
                v-if="channel?.channelType !== 'system'"
                title="确定要删除此频道吗？"
                @confirm="handleDelete"
              >
                <a-button danger>删除频道</a-button>
              </a-popconfirm>
            </a-card>
          </a-space>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 转让 Modal -->
    <TransferConfirmModal
      v-if="channel"
      v-model:open="showTransferModal"
      :channel="channel"
      @success="onTransferSuccess"
    />

    <!-- 删除 Modal -->
    <DeleteConfirmModal
      v-if="channel"
      v-model:open="showDeleteModal"
      :channel="channel"
      @success="onDeleteSuccess"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, onMounted } from 'vue';
  import { useRouter, useRoute } from 'vue-router';
  import { message } from 'ant-design-vue';
  import ChannelTypeTag from '/@/components/jeecg/channel/ChannelTypeTag.vue';
  import ChannelStatusTag from '/@/components/jeecg/channel/ChannelStatusTag.vue';
  import ChannelForm from '/@/components/jeecg/channel/ChannelForm.vue';
  import TransferConfirmModal from './TransferConfirmModal.vue';
  import DeleteConfirmModal from './DeleteConfirmModal.vue';
  import {
    getChannelDetail,
    updateChannel,
    cancelDeleteChannel,
    getTransferHistory,
    getPendingTransfer,
    deleteChannel,
  } from '/@/api/content/channel';
  import type { ChannelVO, ChannelTransferVO, ChannelCreateReq } from '/@/api/content/channel/model/channelModel';

  const router = useRouter();
  const route = useRoute();
  const channel = ref<ChannelVO | null>(null);
  const activeTab = ref('overview');
  const editFormRef = ref<InstanceType<typeof ChannelForm>>();
  const showTransferModal = ref(false);
  const showDeleteModal = ref(false);
  const transferHistory = ref<ChannelTransferVO[]>([]);
  const hasPendingTransfer = ref(false);
  const rejectReason = ref('');

  const channelId = computed(() => route.params.id as string);

  const hasPendingReview = computed(() => {
    return channel.value?.status === 'PENDING_REVIEW';
  });

  const coolingDays = computed(() => {
    // 计算冷静期剩余天数，简化实现
    return 7;
  });

  async function loadChannel() {
    try {
      channel.value = await getChannelDetail(channelId.value);
      // 加载转让信息
      if (channel.value?.channelType !== 'system') {
        const [history, pending] = await Promise.all([
          getTransferHistory(channelId.value),
          getPendingTransfer(channelId.value),
        ]);
        transferHistory.value = history;
        hasPendingTransfer.value = !!pending;
      }
    } catch {
      message.error('加载频道详情失败');
    }
  }

  async function handleEditSubmit(data: ChannelCreateReq) {
    try {
      await updateChannel(channelId.value, data);
      message.success('保存成功');
      await loadChannel();
      activeTab.value = 'overview';
    } catch (e: any) {
      message.error(e?.message || '保存失败');
    }
  }

  async function handleCancelDelete() {
    try {
      await cancelDeleteChannel(channelId.value);
      message.success('已撤销删除');
      await loadChannel();
    } catch {
      message.error('撤销失败');
    }
  }

  async function handleDelete() {
    showDeleteModal.value = true;
  }

  function onDeleteSuccess() {
    loadChannel();
  }

  function onTransferSuccess() {
    loadChannel();
  }

  function getTransferColor(status: string) {
    const map: Record<string, string> = {
      PENDING: 'blue',
      ACCEPTED: 'green',
      REJECTED: 'red',
      EXPIRED: 'gray',
    };
    return map[status] || 'gray';
  }

  function getTransferStatusLabel(status: string) {
    const map: Record<string, string> = {
      PENDING: '待确认',
      ACCEPTED: '已接受',
      REJECTED: '已拒绝',
      EXPIRED: '已过期',
    };
    return map[status] || status;
  }

  function goBack() {
    router.push('/content/channel/list');
  }

  onMounted(loadChannel);
</script>

<style scoped lang="less">
  .channel-manage {
    padding: 24px;

    &__alert {
      margin-bottom: 16px;
    }

    &__pending-tip {
      margin-left: 12px;
      color: #faad14;
      font-size: 13px;
    }

    &__time {
      color: #8c8c8c;
      font-size: 12px;
    }
  }
</style>
