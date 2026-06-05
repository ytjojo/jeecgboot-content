<!-- src/views/support/appeal/index.vue -->
<template>
  <div class="appeal-list-page">
    <a-card title="我的申诉">
      <a-form layout="inline" :model="queryParams" class="query-form">
        <a-form-item label="申诉状态">
          <a-select v-model:value="queryParams.status" placeholder="全部" allow-clear style="width: 140px">
            <a-select-option value="">全部</a-select-option>
            <a-select-option value="reviewing">审核中</a-select-option>
            <a-select-option value="approved">已通过</a-select-option>
            <a-select-option value="rejected">已驳回</a-select-option>
            <a-select-option value="withdrawn">已撤回</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table
        :columns="columns"
        :data-source="appealList"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'status'">
            <a-tag :color="statusColor(record.status)">
              <template #icon>
                <sync-outlined v-if="record.status === 'reviewing'" spin />
                <check-circle-outlined v-if="record.status === 'approved'" />
                <close-circle-outlined v-if="record.status === 'rejected'" />
                <undo-outlined v-if="record.status === 'withdrawn'" />
              </template>
              {{ record.statusLabel }}
            </a-tag>
          </template>
          <template v-if="column.dataIndex === 'action'">
            <a-space>
              <a @click="handleViewDetail(record)">查看详情</a>
              <a-popconfirm
                v-if="record.status === 'reviewing'"
                title="确认撤回该申诉？撤回后不可恢复"
                @confirm="handleWithdraw(record)"
              >
                <a>撤回</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>

      <a-empty v-if="!loading && appealList.length === 0" description="暂无申诉记录" />
    </a-card>

    <AppealDetailDrawer
      :visible="drawerVisible"
      :appeal-id="selectedAppealId"
      @close="drawerVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { SyncOutlined, CheckCircleOutlined, CloseCircleOutlined, UndoOutlined } from '@ant-design/icons-vue';
import { getAppealList, withdrawAppeal, type AppealItem, type AppealQueryParams } from '/@/api/support/appeal';
import AppealDetailDrawer from './components/AppealDetailDrawer.vue';

const columns = [
  { title: '申诉编号', dataIndex: 'appealNo', width: 160 },
  { title: '申诉类型', dataIndex: 'appealTypeLabel', width: 120 },
  { title: '关联处罚', dataIndex: 'relatedSummary', ellipsis: true },
  { title: '提交时间', dataIndex: 'createTime', width: 180, sorter: true, defaultSortOrder: 'descend' },
  { title: '状态', dataIndex: 'status', width: 100 },
  { title: '操作', dataIndex: 'action', width: 150, fixed: 'right' as const },
];

const loading = ref(false);
const appealList = ref<AppealItem[]>([]);
const drawerVisible = ref(false);
const selectedAppealId = ref('');

const queryParams = reactive<AppealQueryParams>({ status: undefined, pageNo: 1, pageSize: 20 });
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (t: number) => `共 ${t} 条`,
});

const fetchList = async () => {
  loading.value = true;
  try {
    const res = await getAppealList(queryParams);
    appealList.value = res.result.records || [];
    pagination.total = res.result.total || 0;
    pagination.current = queryParams.pageNo || 1;
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  queryParams.pageNo = 1;
  fetchList();
};

const handleReset = () => {
  queryParams.status = undefined;
  queryParams.pageNo = 1;
  fetchList();
};

const handleTableChange = (pag: any) => {
  queryParams.pageNo = pag.current;
  queryParams.pageSize = pag.pageSize;
  fetchList();
};

const handleViewDetail = (record: AppealItem) => {
  selectedAppealId.value = record.id;
  drawerVisible.value = true;
};

const handleWithdraw = async (record: AppealItem) => {
  try {
    await withdrawAppeal(record.id);
    message.success('撤回成功');
    fetchList();
  } catch {
    message.error('撤回失败');
  }
};

const statusColor = (status: string) =>
  ({ reviewing: 'blue', approved: 'green', rejected: 'red', withdrawn: 'default' }[status] || 'default');

onMounted(() => fetchList());
</script>

<style scoped lang="less">
.appeal-list-page {
  padding: 16px;

  .query-form {
    margin-bottom: 16px;
  }
}
</style>
