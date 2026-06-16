<template>
  <div class="report-list-page">
    <!-- 无权限访问 -->
    <a-result v-if="!hasPermission" status="403" title="无权限访问" sub-title="仅圈主和版主可以管理举报" />

    <!-- 有权限 -->
    <template v-else>
      <div class="report-header">
        <h3>举报处理</h3>
      </div>
      <Tabs v-model:activeKey="activeTab" @change="onTabChange">
        <Tabs.TabPane key="PENDING" tab="待处理" />
        <Tabs.TabPane key="RESOLVED" tab="已处理" />
        <Tabs.TabPane key="IGNORED" tab="已忽略" />
      </Tabs>
      <!-- 桌面端 -->
      <div class="desktop-view">
        <Table
          :dataSource="reportList"
          :columns="columns"
          :loading="loading"
          rowKey="id"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'reason'">
              <Tag>{{ record.reason }}</Tag>
            </template>
            <template v-else-if="column.key === 'status'">
              <Tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</Tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <Space>
                <Button size="small" @click="handleDetail(record)">查看</Button>
                <Button v-if="record.status === 'PENDING'" size="small" type="primary" danger @click="handleDeleteContent(record)">
                  删除内容
                </Button>
                <Button v-if="record.status === 'PENDING'" size="small" @click="handleIgnoreReport(record)">
                  忽略
                </Button>
                <Button v-if="record.status === 'PENDING'" size="small" @click="handleMuteUser(record)">
                  禁言
                </Button>
              </Space>
            </template>
          </template>
        </Table>
      </div>

      <!-- 移动端 -->
      <div class="mobile-view">
        <a-spin :spinning="loading">
          <a-empty v-if="reportList.length === 0" description="暂无举报" />
          <ReportCard
            v-for="report in reportList"
            :key="report.id"
            :report="report"
            @detail="handleDetail"
            @delete-content="handleDeleteContent"
            @ignore="handleIgnoreReport"
            @mute="handleMuteUser"
          />
        </a-spin>
      </div>

      <ReportDetailDrawer
        v-model:visible="detailVisible"
        :report="selectedReport"
      />
    </template>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue';
import { Tabs, Table, Button, Space, Tag, Modal, message } from 'ant-design-vue';
import {
  getCircleReportList,
  deleteReportContent,
  ignoreReport,
  muteReportUser,
} from '/@/api/content/circle/report';
import type { CircleReportVO } from '/@/api/content/circle/report';
import { useCircleStoreWithOut } from '/@/store/modules/circle';
import ReportCard from './ReportCard.vue';
import ReportDetailDrawer from './ReportDetailDrawer.vue';

const props = defineProps<{ circleId: string }>();

const circleStore = useCircleStoreWithOut();
const hasPermission = computed(() => circleStore.canManageMember);

const activeTab = ref('PENDING');
const reportList = ref<CircleReportVO[]>([]);
const loading = ref(false);
const detailVisible = ref(false);
const selectedReport = ref<CircleReportVO | null>(null);

const columns = [
  { title: '举报ID', dataIndex: 'id', key: 'id', width: 100, ellipsis: true },
  { title: '被举报内容ID', dataIndex: 'contentId', key: 'contentId', width: 120, ellipsis: true },
  { title: '举报人', dataIndex: 'reporterId', key: 'reporterId', width: 100, ellipsis: true },
  { title: '举报原因', key: 'reason', width: 100 },
  { title: '状态', key: 'status', width: 80 },
  { title: '举报时间', dataIndex: 'createTime', key: 'createTime', width: 160 },
  { title: '操作', key: 'action', width: 240 },
];

const statusColor = (status: string) => {
  switch (status) {
    case 'PENDING': return 'orange';
    case 'RESOLVED': return 'green';
    case 'IGNORED': return 'default';
    default: return 'default';
  }
};

const statusLabel = (status: string) => {
  switch (status) {
    case 'PENDING': return '待处理';
    case 'RESOLVED': return '已处理';
    case 'IGNORED': return '已忽略';
    default: return status;
  }
};

async function fetchList() {
  loading.value = true;
  try {
    const status = activeTab.value === 'PENDING' ? undefined : activeTab.value;
    const res = await getCircleReportList(props.circleId, status);
    reportList.value = Array.isArray(res) ? res : (res as any)?.records ?? [];
  } catch {
    message.error('加载举报列表失败');
  } finally {
    loading.value = false;
  }
}

function onTabChange() {
  fetchList();
}

function handleDetail(record: CircleReportVO) {
  selectedReport.value = record;
  detailVisible.value = true;
}

function handleDeleteContent(record: CircleReportVO) {
  Modal.confirm({
    title: '确认删除',
    content: '确认删除该被举报内容？删除后举报者将收到通知。',
    okType: 'danger',
    onOk: async () => {
      try {
        await deleteReportContent(record.id, props.circleId);
        message.success('已删除被举报内容');
        fetchList();
      } catch {
        message.error('操作失败，请重试');
      }
    },
  });
}

function handleIgnoreReport(record: CircleReportVO) {
  Modal.confirm({
    title: '确认忽略',
    content: '确认忽略该举报？举报者将收到通知。',
    onOk: async () => {
      try {
        await ignoreReport(record.id, props.circleId);
        message.success('已忽略举报');
        fetchList();
      } catch {
        message.error('操作失败，请重试');
      }
    },
  });
}

function handleMuteUser(record: CircleReportVO) {
  Modal.confirm({
    title: '确认禁言',
    content: '确认禁言该被举报用户？注意：后端 mute 端点不接受时长参数（当前默认禁言时长为后端决定）。',
    okType: 'danger',
    onOk: async () => {
      try {
        await muteReportUser(record.id, props.circleId);
        message.success('已禁言用户');
        fetchList();
      } catch {
        message.error('操作失败，请重试');
      }
    },
  });
}

onMounted(() => {
  if (hasPermission.value) {
    fetchList();
  }
});
</script>

<style lang="less" scoped>
.report-list-page {
  .report-header { margin-bottom: 16px; h3 { margin: 0; } }
  .mobile-view { display: none; }

  @media (max-width: 768px) {
    .desktop-view { display: none; }
    .mobile-view { display: block; }
  }
}
</style>
