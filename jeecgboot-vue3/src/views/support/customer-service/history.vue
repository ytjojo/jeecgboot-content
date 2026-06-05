<template>
  <div class="service-history-page">
    <a-card title="客服记录">
      <a-table :columns="columns" :data-source="sessionList" :loading="loading" :pagination="pagination" row-key="id" @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'type'">
            <a-tag :color="record.type === 'bot' ? 'blue' : 'green'">
              {{ record.type === 'bot' ? '智能客服' : '人工客服' }}
            </a-tag>
          </template>
          <template v-if="column.dataIndex === 'status'">
            <a-tag :color="record.status === 'closed' ? 'default' : 'processing'">
              {{ record.status === 'closed' ? '已结束' : '进行中' }}
            </a-tag>
          </template>
          <template v-if="column.dataIndex === 'action'">
            <a-space>
              <a @click="handleViewDetail(record)">查看详情</a>
              <a @click="handleContinue(record)">继续咨询</a>
            </a-space>
          </template>
        </template>
      </a-table>
      <a-empty v-if="!loading && sessionList.length === 0" description="暂无客服记录" />
    </a-card>
    <a-drawer :open="drawerVisible" title="会话详情" :width="480" @close="drawerVisible = false">
      <template v-if="selectedSession">
        <div v-if="isExpired(selectedSession.createTime)" class="expired-tip">
          <a-alert message="历史记录仅保留 30 天" type="warning" show-icon />
        </div>
        <a-timeline>
          <a-timeline-item v-for="msg in sessionMessages" :key="msg.id">
            <div :class="{ 'system-msg': msg.senderType === 'system' }">
              <strong v-if="msg.senderType === 'user'">我：</strong>
              <strong v-else-if="msg.senderType !== 'system'">客服：</strong>
              {{ msg.content }}
            </div>
          </a-timeline-item>
        </a-timeline>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { getServiceSessionList, getServiceSessionDetail, type ServiceSession, type ChatMessage } from '/@/api/support/customer-service';

  const router = useRouter();
  const columns = [
    { title: '会话时间', dataIndex: 'createTime', width: 180 },
    { title: '客服类型', dataIndex: 'type', width: 120 },
    { title: '问题摘要', dataIndex: 'summary', ellipsis: true },
    { title: '状态', dataIndex: 'status', width: 100 },
    { title: '操作', dataIndex: 'action', width: 150, fixed: 'right' as const },
  ];

  const loading = ref(false);
  const sessionList = ref<ServiceSession[]>([]);
  const drawerVisible = ref(false);
  const selectedSession = ref<ServiceSession | null>(null);
  const sessionMessages = ref<ChatMessage[]>([]);
  const pagination = reactive({ current: 1, pageSize: 20, total: 0, showSizeChanger: true, showTotal: (t: number) => `共 ${t} 条` });

  const fetchList = async () => {
    loading.value = true;
    try {
      const res = await getServiceSessionList({ pageNo: pagination.current, pageSize: pagination.pageSize });
      sessionList.value = res.result?.records || [];
      pagination.total = res.result?.total || 0;
    } finally {
      loading.value = false;
    }
  };

  const handleTableChange = (pag: any) => {
    pagination.current = pag.current;
    pagination.pageSize = pag.pageSize;
    fetchList();
  };

  const handleViewDetail = async (record: ServiceSession) => {
    selectedSession.value = record;
    const res = await getServiceSessionDetail(record.id);
    sessionMessages.value = res.result?.messages || [];
    drawerVisible.value = true;
  };

  const handleContinue = (record: ServiceSession) => {
    router.push({ path: '/customer-service', query: { sessionId: record.id } });
  };

  const isExpired = (createTime: string) => {
    const diff = Date.now() - new Date(createTime).getTime();
    return diff > 30 * 24 * 60 * 60 * 1000;
  };

  onMounted(() => fetchList());
</script>

<style scoped lang="less">
  .service-history-page {
    padding: 16px;
    .expired-tip {
      margin-bottom: 16px;
    }
    .system-msg {
      color: #999;
      text-align: center;
    }
  }
</style>
