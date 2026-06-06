<template>
  <div class="p-4">
    <a-card title="审计日志">
      <a-form :layout="isMobile ? 'vertical' : 'inline'" :model="queryForm" class="mb-4">
        <a-form-item label="用户ID">
          <a-input v-model:value="queryForm.userId" placeholder="用户ID" allow-clear />
        </a-form-item>
        <a-form-item label="时间范围">
          <a-range-picker v-model:value="queryForm.dateRange" :style="isMobile ? { width: '100%' } : {}" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleQuery">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
            <a-button @click="handleExport">导出</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table :columns="isMobile ? mobileColumns : columns" :data-source="dataSource" :loading="loading" :pagination="pagination"
        row-key="id" @change="handleTableChange" :scroll="{ x: isMobile ? 600 : 1000 }">
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'fromStatus'">
            <StatusTag :status="record.fromStatus" />
          </template>
          <template v-if="column.dataIndex === 'toStatus'">
            <StatusTag :status="record.toStatus" />
          </template>
          <template v-if="column.dataIndex === 'actionBtn'">
            <a-button type="link" size="small" @click="handleViewDetail(record)">详情</a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- Desktop: modal detail; Mobile: fullscreen drawer -->
    <AuditLogDetailModal v-if="!isMobile" v-model:open="detailModalVisible" :detail="selectedLog" />
    <a-drawer v-else v-model:open="detailModalVisible" title="审计日志详情" placement="right" :width="'100%'" :body-style="{ padding: '16px' }">
      <a-descriptions :column="1" bordered size="small">
        <a-descriptions-item label="用户ID">{{ selectedLog?.userId }}</a-descriptions-item>
        <a-descriptions-item label="用户名">{{ selectedLog?.userName }}</a-descriptions-item>
        <a-descriptions-item label="操作">{{ selectedLog?.action }}</a-descriptions-item>
        <a-descriptions-item label="原状态"><StatusTag :status="selectedLog?.fromStatus" /></a-descriptions-item>
        <a-descriptions-item label="目标状态"><StatusTag :status="selectedLog?.toStatus" /></a-descriptions-item>
        <a-descriptions-item label="原因">{{ selectedLog?.reason }}</a-descriptions-item>
        <a-descriptions-item label="操作人">{{ selectedLog?.operatorName }}</a-descriptions-item>
        <a-descriptions-item label="操作时间">{{ selectedLog?.createdAt }}</a-descriptions-item>
        <a-descriptions-item label="IP地址">{{ selectedLog?.ipAddress }}</a-descriptions-item>
        <a-descriptions-item label="备注">{{ selectedLog?.remark }}</a-descriptions-item>
      </a-descriptions>
    </a-drawer>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { getAuditLogList, exportAuditLogs } from '/@/api/content/userStatus';
import StatusTag from '/@/components/jeecg/UserStatus/StatusTag.vue';
import AuditLogDetailModal from '/@/components/jeecg/UserStatus/AuditLogDetailModal.vue';
import { useBreakpoint } from '/@/hooks/event/useBreakpoint';
import dayjs from 'dayjs';

const { screenRef } = useBreakpoint();
const isMobile = computed(() => {
  const screen = screenRef.value;
  return screen === 'xs' || screen === 'sm';
});

const columns = [
  { title: '用户ID', dataIndex: 'userId', width: 120 },
  { title: '用户名', dataIndex: 'userName', width: 120 },
  { title: '操作', dataIndex: 'action', width: 120 },
  { title: '原状态', dataIndex: 'fromStatus', width: 100 },
  { title: '目标状态', dataIndex: 'toStatus', width: 100 },
  { title: '原因', dataIndex: 'reason', ellipsis: true },
  { title: '操作人', dataIndex: 'operatorName', width: 100 },
  { title: '操作时间', dataIndex: 'createdAt', width: 180 },
  { title: '操作', dataIndex: 'actionBtn', width: 80, fixed: 'right' as const },
];

const mobileColumns = [
  { title: '用户', dataIndex: 'userName', width: 100 },
  { title: '操作', dataIndex: 'action', width: 100 },
  { title: '状态变更', dataIndex: 'fromStatus', width: 160 },
  { title: '操作时间', dataIndex: 'createdAt', width: 150 },
  { title: '操作', dataIndex: 'actionBtn', width: 70, fixed: 'right' as const },
];

const queryForm = reactive({ userId: '', dateRange: null as any });
const dataSource = ref<any[]>([]);
const loading = ref(false);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });
const detailModalVisible = ref(false);
const selectedLog = ref<any>({});

async function fetchData() {
  loading.value = true;
  try {
    const res = await getAuditLogList({
      userId: queryForm.userId || undefined,
      startTime: queryForm.dateRange?.[0] ? dayjs(queryForm.dateRange[0]).format('YYYY-MM-DD') : undefined,
      endTime: queryForm.dateRange?.[1] ? dayjs(queryForm.dateRange[1]).format('YYYY-MM-DD') : undefined,
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
  queryForm.dateRange = null;
  handleQuery();
}

function handleTableChange(pag: any) {
  pagination.value.current = pag.current;
  pagination.value.pageSize = pag.pageSize;
  fetchData();
}

function handleViewDetail(record: any) {
  selectedLog.value = record;
  detailModalVisible.value = true;
}

async function handleExport() {
  try {
    const blob = await exportAuditLogs({
      userId: queryForm.userId || undefined,
      startTime: queryForm.dateRange?.[0] ? dayjs(queryForm.dateRange[0]).format('YYYY-MM-DD') : undefined,
      endTime: queryForm.dateRange?.[1] ? dayjs(queryForm.dateRange[1]).format('YYYY-MM-DD') : undefined,
      format: 'excel',
    });
    const url = window.URL.createObjectURL(blob as any);
    const link = document.createElement('a');
    link.href = url;
    link.download = `audit-log-${dayjs().format('YYYY-MM-DD')}.xlsx`;
    link.click();
    window.URL.revokeObjectURL(url);
  } catch {
    message.error('导出失败，请稍后重试');
  }
}

onMounted(fetchData);
</script>
