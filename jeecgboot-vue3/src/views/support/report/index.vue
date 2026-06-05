<!-- src/views/support/report/index.vue -->
<template>
  <div class="report-list-page">
    <a-card title="我的举报">
      <!-- 查询表单 -->
      <a-form layout="inline" :model="queryParams" class="query-form">
        <a-form-item label="举报状态">
          <a-select v-model:value="queryParams.status" placeholder="全部" allow-clear style="width: 140px">
            <a-select-option value="">全部</a-select-option>
            <a-select-option value="pending">待处理</a-select-option>
            <a-select-option value="processing">处理中</a-select-option>
            <a-select-option value="processed">已处理</a-select-option>
            <a-select-option value="withdrawn">已撤回</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="举报类型">
          <a-select v-model:value="queryParams.reportType" placeholder="全部" allow-clear style="width: 140px">
            <a-select-option value="">全部</a-select-option>
            <a-select-option value="porn">色情</a-select-option>
            <a-select-option value="violence">暴力</a-select-option>
            <a-select-option value="fraud">诈骗</a-select-option>
            <a-select-option value="harassment">骚扰</a-select-option>
            <a-select-option value="other">其他</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- 数据表格 -->
      <a-table
        :columns="columns"
        :data-source="reportList"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'status'">
            <a-tag :color="statusColor(record.status)">
              {{ record.statusLabel }}
            </a-tag>
          </template>
          <template v-if="column.dataIndex === 'action'">
            <a-space>
              <a @click="handleViewDetail(record)">查看详情</a>
              <a-popconfirm
                v-if="record.status === 'pending'"
                title="确认撤回该举报？撤回后不可恢复"
                @confirm="handleWithdraw(record)"
              >
                <a>撤回</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>

      <!-- 空状态 -->
      <a-empty v-if="!loading && reportList.length === 0" description="暂无举报记录">
        <a-button type="primary" @click="handleGuide">遇到违规内容？点击举报</a-button>
      </a-empty>
    </a-card>

    <!-- 举报详情抽屉 -->
    <ReportDetailDrawer
      :visible="drawerVisible"
      :report-id="selectedReportId"
      @close="drawerVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { getReportList, withdrawReport, type ReportItem, type ReportQueryParams } from '/@/api/support/report';
import ReportDetailDrawer from './components/ReportDetailDrawer.vue';

const columns = [
  { title: '举报编号', dataIndex: 'reportNo', width: 160 },
  { title: '举报对象', dataIndex: 'targetSummary', ellipsis: true },
  { title: '举报类型', dataIndex: 'reportTypeLabel', width: 100 },
  { title: '提交时间', dataIndex: 'createTime', width: 180 },
  { title: '状态', dataIndex: 'status', width: 100 },
  { title: '操作', dataIndex: 'action', width: 150, fixed: 'right' as const },
];

const loading = ref(false);
const reportList = ref<ReportItem[]>([]);
const total = ref(0);
const drawerVisible = ref(false);
const selectedReportId = ref('');

const queryParams = reactive<ReportQueryParams>({
  status: undefined,
  reportType: undefined,
  pageNo: 1,
  pageSize: 20,
});

const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
});

const fetchList = async () => {
  loading.value = true;
  try {
    const res = await getReportList(queryParams);
    reportList.value = res.result.records || [];
    total.value = res.result.total || 0;
    pagination.total = total.value;
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
  queryParams.reportType = undefined;
  queryParams.pageNo = 1;
  fetchList();
};

const handleTableChange = (pag: any) => {
  queryParams.pageNo = pag.current;
  queryParams.pageSize = pag.pageSize;
  fetchList();
};

const handleViewDetail = (record: ReportItem) => {
  selectedReportId.value = record.id;
  drawerVisible.value = true;
};

const handleWithdraw = async (record: ReportItem) => {
  try {
    await withdrawReport(record.id);
    message.success('撤回成功');
    fetchList();
  } catch {
    message.error('撤回失败');
  }
};

const handleGuide = () => {
  message.info('请在内容详情页点击"举报"按钮');
};

const statusColor = (status: string) => {
  const map: Record<string, string> = {
    pending: 'orange',
    processing: 'blue',
    processed: 'green',
    withdrawn: 'default',
  };
  return map[status] || 'default';
};

onMounted(() => {
  fetchList();
});
</script>

<style scoped lang="less">
.report-list-page {
  padding: 16px;

  .query-form {
    margin-bottom: 16px;
  }
}
</style>
