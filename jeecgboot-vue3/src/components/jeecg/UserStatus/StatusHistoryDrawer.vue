<template>
  <BasicDrawer v-bind="$attrs" title="状态变更历史" :width="600" @close="handleClose">
    <a-table :columns="columns" :data-source="dataSource" :loading="loading" :pagination="pagination"
      size="small" row-key="id" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'fromStatus'">
          <StatusTag :status="record.fromStatus" />
        </template>
        <template v-if="column.dataIndex === 'toStatus'">
          <StatusTag :status="record.toStatus" />
        </template>
      </template>
    </a-table>
  </BasicDrawer>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue';
import { BasicDrawer } from '/@/components/Drawer';
import StatusTag from './StatusTag.vue';
import { useUserStatusStore } from '/@/store/modules/userStatus';

const props = defineProps({
  userId: { type: String, required: true },
});

const userStatusStore = useUserStatusStore();
const loading = ref(false);
const dataSource = ref<any[]>([]);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

const columns = [
  { title: '原状态', dataIndex: 'fromStatus', width: 100 },
  { title: '目标状态', dataIndex: 'toStatus', width: 100 },
  { title: '原因', dataIndex: 'reason', ellipsis: true },
  { title: '操作人', dataIndex: 'operatorName', width: 100 },
  { title: '操作时间', dataIndex: 'createdAt', width: 180 },
];

async function fetchData() {
  loading.value = true;
  try {
    const res = await userStatusStore.fetchStatusHistory(props.userId, {
      page: pagination.value.current,
      pageSize: pagination.value.pageSize,
    });
    if (res) {
      dataSource.value = Array.isArray(res) ? res : (res as any).records || [];
      pagination.value.total = Array.isArray(res) ? res.length : (res as any).total || 0;
    }
  } finally {
    loading.value = false;
  }
}

function handleTableChange(pag: any) {
  pagination.value.current = pag.current;
  pagination.value.pageSize = pag.pageSize;
  fetchData();
}

function handleClose() {
  dataSource.value = [];
  pagination.value.current = 1;
}

watch(() => props.userId, (id) => {
  if (id) fetchData();
}, { immediate: true });
</script>
