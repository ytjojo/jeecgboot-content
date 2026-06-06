<template>
  <div class="p-4">
    <a-card title="我的账号状态">
      <a-spin :spinning="loading">
        <a-descriptions :column="isMobile ? 1 : 1" bordered v-if="statusDetail" :size="isMobile ? 'small' : 'default'">
          <a-descriptions-item label="当前状态">
            <StatusTag :status="statusDetail.status" />
          </a-descriptions-item>
          <a-descriptions-item v-if="statusDetail.reason" label="原因">
            {{ statusDetail.reason }}
          </a-descriptions-item>
          <a-descriptions-item v-if="statusDetail.endTime && statusDetail.status !== 'NORMAL'" label="剩余时间">
            <StatusCountdown :end-time="statusDetail.endTime" :on-expired="handleExpired" />
          </a-descriptions-item>
          <a-descriptions-item v-if="statusDetail.operatorName" label="操作人">
            {{ statusDetail.operatorName }}
          </a-descriptions-item>
        </a-descriptions>
        <a-empty v-else description="暂无状态信息" />
      </a-spin>
    </a-card>

    <a-card title="状态变更历史" class="mt-4">
      <!-- Desktop: table view -->
      <a-table v-if="!isMobile" :columns="historyColumns" :data-source="historyData" :loading="historyLoading"
        :pagination="pagination" row-key="id" size="small" @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'fromStatus'">
            <StatusTag :status="record.fromStatus" />
          </template>
          <template v-if="column.dataIndex === 'toStatus'">
            <StatusTag :status="record.toStatus" />
          </template>
        </template>
      </a-table>

      <!-- Mobile: timeline view -->
      <div v-else>
        <a-spin :spinning="historyLoading">
          <a-timeline v-if="historyData.length > 0">
            <a-timeline-item v-for="item in historyData" :key="item.id" :color="getTimelineColor(item)">
              <div class="text-sm">
                <div>
                  <StatusTag :status="item.fromStatus" />
                  <span class="mx-1">→</span>
                  <StatusTag :status="item.toStatus" />
                </div>
                <div v-if="item.reason" class="text-gray-500 mt-1">{{ item.reason }}</div>
                <div class="text-gray-400 text-xs mt-1">
                  {{ item.operatorName }} · {{ item.createdAt }}
                </div>
              </div>
            </a-timeline-item>
          </a-timeline>
          <a-empty v-else description="暂无变更记录" />
          <div v-if="pagination.total > pagination.pageSize" class="text-center mt-4">
            <a-button size="small" @click="loadMore">加载更多</a-button>
          </div>
        </a-spin>
      </div>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue';
import { useUserStore } from '/@/store/modules/user';
import { useUserStatusStore } from '/@/store/modules/userStatus';
import StatusTag from '/@/components/jeecg/UserStatus/StatusTag.vue';
import StatusCountdown from '/@/components/jeecg/UserStatus/StatusCountdown.vue';
import { useBreakpoint } from '/@/hooks/event/useBreakpoint';

const { screenRef } = useBreakpoint();
const isMobile = computed(() => {
  const screen = screenRef.value;
  return screen === 'xs' || screen === 'sm';
});

const userStore = useUserStore();
const userStatusStore = useUserStatusStore();

const loading = ref(false);
const statusDetail = ref<any>(null);
const historyData = ref<any[]>([]);
const historyLoading = ref(false);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

const historyColumns = [
  { title: '原状态', dataIndex: 'fromStatus', width: 100 },
  { title: '目标状态', dataIndex: 'toStatus', width: 100 },
  { title: '原因', dataIndex: 'reason', ellipsis: true },
  { title: '操作人', dataIndex: 'operatorName', width: 100 },
  { title: '操作时间', dataIndex: 'createdAt', width: 180 },
];

function getTimelineColor(item: any) {
  if (item.toStatus === 'NORMAL') return 'green';
  if (item.toStatus === 'FROZEN' || item.toStatus === 'BANNED') return 'red';
  if (item.toStatus === 'MUTED') return 'orange';
  return 'blue';
}

function loadMore() {
  pagination.value.current++;
  fetchHistory();
}

async function fetchStatus() {
  const userId = userStore.getUserInfo?.id;
  if (!userId) return;
  loading.value = true;
  try {
    await userStatusStore.fetchCurrentStatus(userId);
    statusDetail.value = userStatusStore.statusDetail;
  } finally {
    loading.value = false;
  }
}

async function fetchHistory() {
  const userId = userStore.getUserInfo?.id;
  if (!userId) return;
  historyLoading.value = true;
  try {
    const res = await userStatusStore.fetchStatusHistory(userId, {
      page: pagination.value.current,
      pageSize: pagination.value.pageSize,
    });
    if (res) {
      historyData.value = Array.isArray(res) ? res : (res as any).records || [];
      pagination.value.total = Array.isArray(res) ? res.length : (res as any).total || 0;
    }
  } finally {
    historyLoading.value = false;
  }
}

function handleExpired() {
  fetchStatus();
}

function handleTableChange(pag: any) {
  pagination.value.current = pag.current;
  pagination.value.pageSize = pag.pageSize;
  fetchHistory();
}

onMounted(() => {
  fetchStatus();
  fetchHistory();
});
</script>
