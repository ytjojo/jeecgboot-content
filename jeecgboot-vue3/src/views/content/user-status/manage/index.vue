<template>
  <div class="p-4">
    <a-card title="用户状态管理">
      <a-form :layout="isMobile ? 'vertical' : 'inline'" :model="queryForm" class="mb-4">
        <a-form-item label="用户ID">
          <a-input v-model:value="queryForm.userId" placeholder="用户ID" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="queryForm.status" placeholder="全部状态" allow-clear :style="{ width: isMobile ? '100%' : '150px' }">
            <a-select-option v-for="(label, key) in statusLabelMap" :key="key" :value="key">{{ label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleQuery">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- Desktop: table view -->
      <a-table v-if="!isMobile" :columns="columns" :data-source="dataSource" :loading="loading" :pagination="pagination"
        row-key="userId" @change="handleTableChange" :scroll="{ x: 800 }">
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'status'">
            <StatusTag :status="record.status" />
          </template>
          <template v-if="column.dataIndex === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleChangeStatus(record)">变更</a-button>
              <a-button type="link" size="small" @click="handleRelease(record)"
                :disabled="record.status === 'NORMAL'">解禁</a-button>
              <a-button type="link" size="small" @click="handleViewHistory(record)">历史</a-button>
            </a-space>
          </template>
        </template>
      </a-table>

      <!-- Mobile: card list view -->
      <div v-else>
        <a-spin :spinning="loading">
          <a-list :data-source="dataSource" :pagination="pagination.total > 0 ? pagination : undefined">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta :title="item.userName || item.userId">
                  <template #description>
                    <div>
                      <StatusTag :status="item.status" />
                      <span v-if="item.reason" class="ml-2 text-gray-500">{{ item.reason }}</span>
                    </div>
                    <div v-if="item.endTime" class="text-gray-400 text-xs mt-1">到期: {{ item.endTime }}</div>
                  </template>
                </a-list-item-meta>
                <template #actions>
                  <a-dropdown>
                    <a-button type="link" size="small">操作</a-button>
                    <template #overlay>
                      <a-menu>
                        <a-menu-item @click="handleChangeStatus(item)">变更</a-menu-item>
                        <a-menu-item :disabled="item.status === 'NORMAL'" @click="handleRelease(item)">解禁</a-menu-item>
                        <a-menu-item @click="handleViewHistory(item)">历史</a-menu-item>
                      </a-menu>
                    </template>
                  </a-dropdown>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </a-spin>
      </div>
    </a-card>

    <StatusChangeModal v-model:open="changeModalVisible" :userId="selectedUserId" :currentStatus="selectedStatus"
      @success="handleQuery" />
    <StatusReleaseModal v-model:open="releaseModalVisible" :userId="selectedUserId" @success="handleQuery" />
    <StatusHistoryDrawer v-model:open="historyDrawerVisible" :userId="selectedUserId" />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { getStatusList } from '/@/api/content/userStatus';
import { useBreakpoint } from '/@/hooks/event/useBreakpoint';
import StatusTag from '/@/components/jeecg/UserStatus/StatusTag.vue';
import StatusChangeModal from '/@/components/jeecg/UserStatus/StatusChangeModal.vue';
import StatusReleaseModal from '/@/components/jeecg/UserStatus/StatusReleaseModal.vue';
import StatusHistoryDrawer from '/@/components/jeecg/UserStatus/StatusHistoryDrawer.vue';

const { screenRef } = useBreakpoint();
const isMobile = computed(() => {
  const screen = screenRef.value;
  return screen === 'xs' || screen === 'sm';
});

const statusLabelMap: Record<string, string> = {
  GUEST: '游客', REGISTERED_INCOMPLETE: '注册未完善', NORMAL: '正常', MUTED: '禁言',
  RESTRICTED_RECOMMEND: '限制推荐', FROZEN: '冻结', BANNED: '封禁', DEACTIVATING: '注销中', DEACTIVATED: '已注销',
};

const columns = [
  { title: '用户ID', dataIndex: 'userId', width: 120 },
  { title: '用户名', dataIndex: 'userName', width: 120 },
  { title: '状态', dataIndex: 'status', width: 120 },
  { title: '原因', dataIndex: 'reason', ellipsis: true },
  { title: '到期时间', dataIndex: 'endTime', width: 180 },
  { title: '操作', dataIndex: 'action', width: 200, fixed: 'right' as const },
];

const queryForm = reactive({ userId: '', status: undefined as string | undefined });
const dataSource = ref<any[]>([]);
const loading = ref(false);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });
const changeModalVisible = ref(false);
const releaseModalVisible = ref(false);
const historyDrawerVisible = ref(false);
const selectedUserId = ref('');
const selectedStatus = ref('');

async function fetchData() {
  loading.value = true;
  try {
    const res = await getStatusList({
      userId: queryForm.userId || undefined,
      status: queryForm.status || undefined,
      page: pagination.value.current,
      pageSize: pagination.value.pageSize,
    });
    if (res) {
      dataSource.value = (res as any).records || [];
      pagination.value.total = (res as any).total || 0;
    }
  } finally {
    loading.value = false;
  }
}

function handleQuery() {
  pagination.value.current = 1;
  fetchData();
}

function handleReset() {
  queryForm.userId = '';
  queryForm.status = undefined;
  handleQuery();
}

function handleTableChange(pag: any) {
  pagination.value.current = pag.current;
  pagination.value.pageSize = pag.pageSize;
  fetchData();
}

function handleChangeStatus(record: any) {
  selectedUserId.value = record.userId;
  selectedStatus.value = record.status;
  changeModalVisible.value = true;
}

function handleRelease(record: any) {
  selectedUserId.value = record.userId;
  releaseModalVisible.value = true;
}

function handleViewHistory(record: any) {
  selectedUserId.value = record.userId;
  historyDrawerVisible.value = true;
}

onMounted(fetchData);
</script>
