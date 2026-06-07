<!-- jeecgboot-vue3/src/views/channel/members/PendingApplications.vue -->
<template>
  <div class="pending-applications">
    <div class="page-header">
      <h3>待审队列 <span class="count">共 {{ total }} 条待审</span></h3>
    </div>

    <div class="filter-bar">
      <RangePicker v-model:value="dateRange" :placeholder="['开始时间', '结束时间']" @change="loadData" />
      <div v-if="selectedRowKeys.length > 0" class="batch-actions">
        <span>已选 {{ selectedRowKeys.length }} 项</span>
        <Button type="link" @click="handleBatchApprove">批量批准</Button>
        <Button type="link" danger @click="handleBatchReject">批量拒绝</Button>
      </div>
    </div>

    <Table
      :dataSource="applicationList"
      :columns="columns"
      :loading="loading"
      :pagination="pagination"
      :rowSelection="{ selectedRowKeys, onChange: onSelectChange }"
      rowKey="id"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'applicant'">
          <Space>
            <Avatar :src="record.avatar" size="small" />
            <span>{{ record.nickname }}</span>
          </Space>
        </template>
        <template v-if="column.dataIndex === 'timeout'">
          <Tag v-if="record.isTimeout" color="orange">超时</Tag>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <Space>
            <Button type="link" size="small" @click="handleApprove(record)">批准</Button>
            <Button type="link" size="small" danger @click="handleReject(record)">拒绝</Button>
          </Space>
        </template>
      </template>
    </Table>

    <!-- 拒绝原因 Modal -->
    <Modal v-model:open="rejectModalVisible" title="拒绝申请" :confirmLoading="rejecting" @ok="handleRejectConfirm">
      <Form layout="vertical">
        <Form.Item label="拒绝原因" required>
          <Input.TextArea v-model:value="rejectReason" :rows="3" placeholder="请输入拒绝原因" />
        </Form.Item>
      </Form>
    </Modal>

    <!-- 批量操作结果 Modal -->
    <Modal v-model:open="resultModalVisible" title="操作结果" :footer="null">
      <p>成功 {{ batchResult.success }} 条，失败 {{ batchResult.failed }} 条</p>
      <ul v-if="batchResult.details.length > 0">
        <li v-for="item in batchResult.details" :key="item.id">
          <span>{{ item.nickname }}</span>
          <Tag :color="item.success ? 'green' : 'red'">{{ item.success ? '成功' : '失败' }}</Tag>
          <span v-if="!item.success" class="error-msg">{{ item.errorMessage }}</span>
        </li>
      </ul>
    </Modal>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue';
  import { Table, Button, Space, Avatar, Tag, Modal, Form, Input, RangePicker, message } from 'ant-design-vue';
  import { getPendingApplications, approveApplications, rejectApplications } from '/@/api/content/channelMember';

  const props = defineProps<{ channelId: string }>();

  const loading = ref(false);
  const applicationList = ref<any[]>([]);
  const total = ref(0);
  const selectedRowKeys = ref<string[]>([]);
  const dateRange = ref<any>(null);

  const pagination = reactive({ current: 1, pageSize: 20, total: 0 });

  const columns = [
    { title: '申请人', dataIndex: 'applicant', key: 'applicant', width: 200 },
    { title: '申请理由', dataIndex: 'reason', key: 'reason', ellipsis: true },
    { title: '申请时间', dataIndex: 'applyTime', key: 'applyTime', width: 180 },
    { title: '超时', dataIndex: 'timeout', key: 'timeout', width: 80 },
    { title: '操作', dataIndex: 'action', key: 'action', width: 150 },
  ];

  const rejectModalVisible = ref(false);
  const rejecting = ref(false);
  const rejectReason = ref('');
  const rejectTarget = ref<any>(null);

  const resultModalVisible = ref(false);
  const batchResult = reactive({ success: 0, failed: 0, details: [] as any[] });

  async function loadData() {
    loading.value = true;
    try {
      const params: any = {
        channelId: props.channelId,
        pageNo: pagination.current,
        pageSize: pagination.pageSize,
      };
      if (dateRange.value) {
        params.startTime = dateRange.value[0];
        params.endTime = dateRange.value[1];
      }
      const res = await getPendingApplications(params);
      applicationList.value = res.records || res;
      total.value = res.total || applicationList.value.length;
      pagination.total = total.value;
    } catch (error: any) {
      if (error?.response?.status === 404) {
        applicationList.value = [];
        total.value = 0;
        message.warning('频道不存在或已被删除');
      } else {
        message.error('加载待审列表失败，请重试');
      }
    } finally {
      loading.value = false;
    }
  }

  function onSelectChange(keys: string[]) {
    selectedRowKeys.value = keys;
  }

  function handleTableChange(pag: any) {
    pagination.current = pag.current;
    pagination.pageSize = pag.pageSize;
    loadData();
  }

  async function handleApprove(record: any) {
    await approveApplications({ channelId: props.channelId, applicationIds: [record.id] });
    message.success('已批准');
    loadData();
  }

  function handleReject(record: any) {
    rejectTarget.value = record;
    rejectReason.value = '';
    rejectModalVisible.value = true;
  }

  async function handleRejectConfirm() {
    if (!rejectReason.value.trim()) {
      message.warning('请输入拒绝原因');
      return;
    }
    rejecting.value = true;
    try {
      await rejectApplications({
        channelId: props.channelId,
        applicationIds: [rejectTarget.value.id],
        reason: rejectReason.value,
      });
      message.success('已拒绝');
      rejectModalVisible.value = false;
      loadData();
    } finally {
      rejecting.value = false;
    }
  }

  const batchOperating = ref(false);

  async function handleBatchApprove() {
    if (batchOperating.value) return;
    batchOperating.value = true;
    try {
      const res = await approveApplications({
        channelId: props.channelId,
        applicationIds: selectedRowKeys.value,
      });
      batchResult.success = res.success;
      batchResult.failed = res.failed;
      batchResult.details = res.details || [];
      resultModalVisible.value = true;
      selectedRowKeys.value = [];
      loadData();
    } finally {
      batchOperating.value = false;
    }
  }

  async function handleBatchReject() {
    rejectTarget.value = null;
    rejectReason.value = '';
    rejectModalVisible.value = true;
  }

  onMounted(loadData);
</script>

<style scoped>
.pending-applications { padding: 16px; }
.page-header { margin-bottom: 16px; }
.count { font-size: 14px; color: #999; font-weight: normal; }
.filter-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.batch-actions { display: flex; align-items: center; gap: 8px; }
.error-msg { color: #f5222d; font-size: 12px; margin-left: 8px; }
</style>
