<template>
  <div class="governance-detail">
    <a-page-header :title="detail?.channelName || '频道治理详情'" @back="$router.back()" />

    <!-- 基本信息 -->
    <a-card title="基本信息" size="small" class="governance-detail__section">
      <a-spin :spinning="loading">
        <a-descriptions :column="3" bordered size="small">
          <a-descriptions-item label="频道名称">{{ detail?.channelName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="频道类型">{{ detail?.channelType || '-' }}</a-descriptions-item>
          <a-descriptions-item label="当前状态">
            <StatusTag v-if="detail?.status" :status="detail.status" />
          </a-descriptions-item>
          <a-descriptions-item label="订阅数">{{ detail?.subscriberCount ?? '-' }}</a-descriptions-item>
          <a-descriptions-item label="内容数">{{ detail?.contentCount ?? '-' }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ detail?.createTime || '-' }}</a-descriptions-item>
          <a-descriptions-item label="最后活跃">{{ detail?.lastActiveTime || '-' }}</a-descriptions-item>
          <a-descriptions-item label="频道简介" :span="2">{{ detail?.description || '-' }}</a-descriptions-item>
        </a-descriptions>
      </a-spin>
    </a-card>

    <!-- 操作按钮 (动态展示) -->
    <a-card title="生命周期操作" size="small" class="governance-detail__section">
      <a-space wrap>
        <!-- Active 状态 -->
        <template v-if="detail?.status === 'Active'">
          <a-button @click="openModal('freeze')">冻结</a-button>
          <a-button @click="openModal('restrict-recommend')">限制推荐</a-button>
          <a-button @click="openModal('hide')">强制隐藏</a-button>
          <a-button @click="openModal('archive')">归档</a-button>
          <a-button @click="openModal('merge')">合并</a-button>
          <a-button danger @click="openModal('close')">永久关闭</a-button>
        </template>
        <!-- Frozen 状态 -->
        <template v-else-if="detail?.status === 'ReadonlyFrozen'">
          <a-button type="primary" @click="openModal('unfreeze')">解冻</a-button>
          <a-button @click="openModal('restrict-recommend')">限制推荐</a-button>
          <a-button @click="openModal('hide')">强制隐藏</a-button>
          <a-button danger @click="openModal('close')">永久关闭</a-button>
        </template>
        <!-- Hidden 状态 -->
        <template v-else-if="detail?.status === 'Hidden'">
          <a-button type="primary" @click="openModal('restore-visibility')">恢复可见</a-button>
          <a-button danger @click="openModal('close')">永久关闭</a-button>
        </template>
        <!-- Archived 状态 -->
        <template v-else-if="detail?.status === 'Archived'">
          <a-button type="primary" @click="openModal('restore')">恢复运营</a-button>
        </template>
        <!-- Merged 状态: 展示目标频道入口 -->
        <template v-else-if="detail?.status === 'Merged'">
          <a-alert message="此频道已合并，当前不可操作" type="info" show-icon />
        </template>
        <!-- Closed 状态: 不可操作 -->
        <template v-else-if="detail?.status === 'Closed'">
          <a-alert message="此频道已永久关闭，不可恢复" type="error" show-icon />
        </template>
      </a-space>
    </a-card>

    <!-- Tab 切换 -->
    <a-card size="small" class="governance-detail__section">
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="content" tab="近期内容">
          <a-empty description="暂无内容" />
        </a-tab-pane>
        <a-tab-pane key="interaction" tab="互动数据">
          <a-empty description="暂无互动数据" />
        </a-tab-pane>
        <a-tab-pane key="penalty" tab="历史处罚">
          <a-empty description="暂无处罚记录" />
        </a-tab-pane>
        <a-tab-pane key="audit" tab="审计日志">
          <a-table
            :columns="auditColumns"
            :dataSource="auditLogs"
            :loading="auditLoading"
            :pagination="auditPagination"
            size="small"
            rowKey="id"
          />
        </a-tab-pane>
        <a-tab-pane key="appeal" tab="申诉记录">
          <a-table
            :columns="appealColumns"
            :dataSource="appeals"
            :loading="appealLoading"
            :pagination="false"
            size="small"
            rowKey="id"
          />
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 生命周期操作确认弹窗 -->
    <LifecycleActionModal
      v-model:visible="modalVisible"
      :title="modalConfig.title"
      :impactDescription="modalConfig.impactDescription"
      :isHighRisk="modalConfig.isHighRisk"
      :requireChannelNameConfirm="modalConfig.requireChannelNameConfirm"
      :channelName="detail?.channelName"
      :showMergeTarget="modalConfig.showMergeTarget"
      :confirmLoading="actionLoading"
      @confirm="handleActionConfirm"
      @cancel="modalVisible = false"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useMessage } from '/@/hooks/web/useMessage';
import StatusTag from '/@/views/content/channel/components/StatusTag.vue';
import LifecycleActionModal from '/@/views/content/channel/components/LifecycleActionModal.vue';
import {
  getGovernanceChannelDetail,
  freezeChannel,
  unfreezeChannel,
  hideChannel,
  closeChannel,
  archiveChannel,
  restrictChannelRecommend,
  restoreChannelVisibility,
  executeChannelMerge,
  validateChannelMerge,
  getLifecycleLogs,
} from '/@/api/content/channel/lifecycle';
import { getAppealList } from '/@/api/content/channel/appeal';
import type { GovernanceDetailVO } from '/@/api/content/channel/lifecycle';
import { useChannelActionSync } from '/@/hooks/channel/useChannelActionSync';

const route = useRoute();
const router = useRouter();
const { createMessage } = useMessage();
const { afterLifecycleAction } = useChannelActionSync();

const channelId = computed(() => route.params.channelId as string);

const detail = ref<GovernanceDetailVO | null>(null);
const loading = ref(false);
const activeTab = ref('content');

// 操作弹窗
const modalVisible = ref(false);
const actionLoading = ref(false);
const modalConfig = reactive({
  title: '',
  impactDescription: '',
  isHighRisk: false,
  requireChannelNameConfirm: false,
  showMergeTarget: false,
});
const currentAction = ref('');

// 审计日志
const auditLogs = ref<any[]>([]);
const auditLoading = ref(false);
const auditPagination = reactive({ total: 0, current: 1, pageSize: 20 });

const auditColumns = [
  { title: '时间', dataIndex: 'operateTime', width: 160 },
  { title: '操作人', dataIndex: 'operator', width: 100 },
  { title: '操作类型', dataIndex: 'operationType', width: 120 },
  { title: '原因', dataIndex: 'reason', ellipsis: true },
];

// 申诉记录
const appeals = ref<any[]>([]);
const appealLoading = ref(false);
const appealColumns = [
  { title: '申诉编号', dataIndex: 'appealNo', width: 120 },
  { title: '处罚类型', dataIndex: 'penaltyType', width: 100 },
  { title: '提交时间', dataIndex: 'submitTime', width: 160 },
  { title: '状态', dataIndex: 'status', width: 100 },
];

// 操作按钮动态配置
const actionConfigs: Record<string, any> = {
  freeze: { title: '冻结频道', impactDescription: '冻结后频道将变为只读状态，用户无法发布新内容', isHighRisk: false, requireChannelNameConfirm: false },
  unfreeze: { title: '解冻频道', impactDescription: '解冻后频道恢复正常运营', isHighRisk: false, requireChannelNameConfirm: false },
  hide: { title: '强制隐藏', impactDescription: '隐藏后频道对外不可见', isHighRisk: true, requireChannelNameConfirm: false },
  'restrict-recommend': { title: '限制推荐', impactDescription: '限制后频道内容不进入公共推荐流', isHighRisk: false, requireChannelNameConfirm: false },
  close: { title: '永久关闭', impactDescription: '关闭后频道不可恢复，所有数据将被保留但不对外展示', isHighRisk: true, requireChannelNameConfirm: true },
  archive: { title: '归档频道', impactDescription: '归档后频道从发现入口消失', isHighRisk: false, requireChannelNameConfirm: false },
  merge: { title: '合并频道', impactDescription: '合并后源频道状态变为已合并，数据迁移至目标频道', isHighRisk: true, requireChannelNameConfirm: false, showMergeTarget: true },
  'restore-visibility': { title: '恢复可见', impactDescription: '恢复后频道重新对外可见', isHighRisk: false, requireChannelNameConfirm: false },
  restore: { title: '恢复运营', impactDescription: '恢复后频道重新进入运营状态', isHighRisk: false, requireChannelNameConfirm: false },
};

function openModal(action: string) {
  Object.assign(modalConfig, actionConfigs[action] || {});
  currentAction.value = action;
  modalVisible.value = true;
}

async function handleActionConfirm(data: { reason: string; channelNameConfirm?: string; targetChannelId?: string }) {
  actionLoading.value = true;
  try {
    switch (currentAction.value) {
      case 'freeze': await freezeChannel({ channelId: channelId.value, reason: data.reason }); break;
      case 'unfreeze': await unfreezeChannel({ channelId: channelId.value, reason: data.reason }); break;
      case 'hide': await hideChannel({ channelId: channelId.value, reason: data.reason }); break;
      case 'close': await closeChannel({ channelId: channelId.value, channelNameConfirm: data.channelNameConfirm || '', reason: data.reason }); break;
      case 'archive': await archiveChannel({ channelId: channelId.value, reason: data.reason }); break;
      case 'restrict-recommend': await restrictChannelRecommend({ channelId: channelId.value, reason: data.reason }); break;
      case 'restore-visibility': await restoreChannelVisibility({ channelId: channelId.value, reason: data.reason }); break;
      case 'merge': {
        if (data.targetChannelId) {
          await executeChannelMerge({ sourceChannelId: channelId.value, targetChannelId: data.targetChannelId, reason: data.reason });
        }
        break;
      }
    }
    createMessage.success('操作成功');
    modalVisible.value = false;
    await afterLifecycleAction({
      refreshGovernance: fetchDetail,
      refreshAuditLog: fetchAuditLogs,
    });
  } catch {
    // 错误由 handleApiError 统一处理
  } finally {
    actionLoading.value = false;
  }
}

async function fetchDetail() {
  loading.value = true;
  try {
    detail.value = await getGovernanceChannelDetail(channelId.value);
  } finally {
    loading.value = false;
  }
}

async function fetchAuditLogs(page = 1) {
  auditLoading.value = true;
  try {
    const res = await getLifecycleLogs({ channelId: channelId.value, current: page, size: auditPagination.pageSize });
    auditLogs.value = res?.records || [];
    auditPagination.total = res?.total || 0;
  } finally {
    auditLoading.value = false;
  }
}

async function fetchAppeals() {
  appealLoading.value = true;
  try {
    const res = await getAppealList({ channelId: channelId.value });
    appeals.value = res?.records || [];
  } finally {
    appealLoading.value = false;
  }
}

onMounted(() => {
  fetchDetail();
  fetchAuditLogs();
});
</script>

<style lang="less" scoped>
.governance-detail {
  padding: 16px;

  &__section {
    margin-bottom: 16px;
  }
}
</style>
