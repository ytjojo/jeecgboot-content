<template>
  <div class="audit-log-page">
    <h2 class="audit-log-page__title">审计日志</h2>

    <!-- 筛选条件 -->
    <a-card size="small" class="audit-log-page__filter">
      <a-form layout="inline">
        <a-form-item label="频道名称">
          <a-input v-model:value="filters.channelName" allowClear placeholder="搜索频道" />
        </a-form-item>
        <a-form-item label="操作人">
          <a-input v-model:value="filters.operator" allowClear placeholder="操作人" />
        </a-form-item>
        <a-form-item label="操作类型">
          <a-select v-model:value="filters.operationType" allowClear placeholder="全部" style="width: 140px">
            <a-select-option value="FREEZE">冻结</a-select-option>
            <a-select-option value="UNFREEZE">解冻</a-select-option>
            <a-select-option value="HIDE">隐藏</a-select-option>
            <a-select-option value="CLOSE">关闭</a-select-option>
            <a-select-option value="ARCHIVE">归档</a-select-option>
            <a-select-option value="RESTRICT_RECOMMEND">限制推荐</a-select-option>
            <a-select-option value="RESTORE_VISIBILITY">恢复可见</a-select-option>
            <a-select-option value="MERGE">合并</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="操作时间">
          <a-range-picker v-model:value="dateRange" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">查询</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 日志列表 -->
    <a-card size="small">
      <a-table
        :columns="columns"
        :dataSource="logList"
        :loading="loading"
        :pagination="{ total, pageSize, current: currentPage, onChange: handlePageChange }"
        rowKey="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'operationType'">
            <StatusTag :status="record.operationType" size="small" />
          </template>
          <template v-else-if="column.key === 'statusChange'">
            <span v-if="record.beforeStatus || record.afterStatus">
              <StatusTag v-if="record.beforeStatus" :status="record.beforeStatus" size="small" />
              <span style="margin: 0 4px">→</span>
              <StatusTag v-if="record.afterStatus" :status="record.afterStatus" size="small" />
            </span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button size="small" type="link" @click="handleViewDetail(record)">详情</a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 日志详情 Modal -->
    <a-modal
      v-model:open="detailVisible"
      title="审计日志详情"
      :footer="null"
      width="600px"
    >
      <a-descriptions :column="1" bordered size="small" v-if="currentLog">
        <a-descriptions-item label="操作时间">{{ currentLog.operateTime }}</a-descriptions-item>
        <a-descriptions-item label="操作人">{{ currentLog.operator }}</a-descriptions-item>
        <a-descriptions-item label="操作对象">{{ currentLog.channelName }}</a-descriptions-item>
        <a-descriptions-item label="操作类型">
          <StatusTag :status="currentLog.operationType" size="small" />
        </a-descriptions-item>
        <a-descriptions-item label="操作前状态">
          <StatusTag v-if="currentLog.beforeStatus" :status="currentLog.beforeStatus" size="small" />
          <span v-else>-</span>
        </a-descriptions-item>
        <a-descriptions-item label="操作后状态">
          <StatusTag v-if="currentLog.afterStatus" :status="currentLog.afterStatus" size="small" />
          <span v-else>-</span>
        </a-descriptions-item>
        <a-descriptions-item label="原因">{{ currentLog.reason || '-' }}</a-descriptions-item>
        <a-descriptions-item label="影响范围">{{ currentLog.impactScope || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { getAuditLogList } from '/@/api/content/channel/auditLog';
import type { AuditLogVO } from '/@/api/content/channel/auditLog';
import StatusTag from '/@/views/content/channel/components/StatusTag.vue';

const logList = ref<AuditLogVO[]>([]);
const loading = ref(false);
const total = ref(0);
const pageSize = ref(20);
const currentPage = ref(1);
const dateRange = ref<any>(null);

const detailVisible = ref(false);
const currentLog = ref<AuditLogVO | null>(null);

const filters = reactive({
  channelName: undefined as string | undefined,
  operator: undefined as string | undefined,
  operationType: undefined as string | undefined,
});

const columns = [
  { title: '操作时间', dataIndex: 'operateTime', width: 170, defaultSortOrder: 'descend' as const },
  { title: '频道名称', dataIndex: 'channelName', width: 160 },
  { title: '操作人', dataIndex: 'operator', width: 120 },
  { title: '操作类型', key: 'operationType', width: 100 },
  { title: '状态变更', key: 'statusChange', width: 200 },
  { title: '原因', dataIndex: 'reason', ellipsis: true },
  { title: '操作', key: 'action', width: 80, fixed: 'right' as const },
];

async function fetchList() {
  loading.value = true;
  try {
    const res = await getAuditLogList({
      ...filters,
      operateTimeStart: dateRange.value?.[0],
      operateTimeEnd: dateRange.value?.[1],
      current: currentPage.value,
      size: pageSize.value,
    });
    logList.value = res?.records || [];
    total.value = res?.total || 0;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  currentPage.value = 1;
  fetchList();
}

function handlePageChange(page: number) {
  currentPage.value = page;
  fetchList();
}

function handleViewDetail(record: AuditLogVO) {
  currentLog.value = record;
  detailVisible.value = true;
}

onMounted(() => {
  fetchList();
});
</script>

<style lang="less" scoped>
.audit-log-page {
  padding: 16px;

  &__title {
    margin: 0 0 16px;
    font-size: 20px;
    font-weight: 600;
  }

  &__filter {
    margin-bottom: 16px;
  }
}
</style>
