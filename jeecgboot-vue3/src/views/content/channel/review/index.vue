<template>
  <div class="channel-review">
    <a-page-header title="审核队列" :back-icon="false" />

    <a-card :bordered="false">
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'channelName'">
            <a @click="showDetail(record)">{{ record.channelName }}</a>
          </template>
          <template v-if="column.key === 'channelType'">
            <ChannelTypeTag :type="record.channelType" />
          </template>
          <template v-if="column.key === 'status'">
            <ChannelStatusTag :status="record.status" />
            <a-tag v-if="record.isTimeout" color="error" size="small" style="margin-left: 4px">超时</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="showDetail(record)">查看</a-button>
              <a-button
                v-if="record.status === 'PENDING_REVIEW'"
                type="link"
                size="small"
                @click="handleApprove(record)"
              >通过</a-button>
              <a-button
                v-if="record.status === 'PENDING_REVIEW'"
                type="link"
                size="small"
                danger
                @click="showRejectModal(record)"
              >拒绝</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 拒绝原因弹窗 -->
    <a-modal v-model:open="rejectModalVisible" title="拒绝审核" @ok="handleReject">
      <a-form layout="vertical">
        <a-form-item label="拒绝原因">
          <a-textarea v-model:value="rejectNote" :maxlength="200" show-count placeholder="请输入拒绝原因" />
        </a-form-item>
      </a-form>
    </a-modal>

    <ChannelDetailDrawer ref="detailDrawerRef" />
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import ChannelTypeTag from '/@/components/jeecg/channel/ChannelTypeTag.vue';
  import ChannelStatusTag from '/@/components/jeecg/channel/ChannelStatusTag.vue';
  import ChannelDetailDrawer from '../admin/ChannelDetailDrawer.vue';
  import { getReviewList, reviewAction } from '/@/api/content/channel';
  import type { ChannelReviewVO } from '/@/api/content/channel/model/channelModel';
  import type { TablePaginationConfig } from 'ant-design-vue';

  const loading = ref(false);
  const dataSource = ref<ChannelReviewVO[]>([]);
  const detailDrawerRef = ref<InstanceType<typeof ChannelDetailDrawer>>();
  const rejectModalVisible = ref(false);
  const rejectNote = ref('');
  const rejectingRecord = ref<ChannelReviewVO | null>(null);

  const pagination = reactive<TablePaginationConfig>({
    current: 1,
    pageSize: 20,
    total: 0,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  });

  const columns = [
    { title: '频道名称', key: 'channelName', dataIndex: 'channelName' },
    { title: '类型', key: 'channelType', width: 100 },
    { title: '状态', key: 'status', width: 120 },
    { title: '提交人', dataIndex: 'submitterName', key: 'submitterName', width: 120 },
    { title: '提交时间', dataIndex: 'submitTime', key: 'submitTime', width: 170 },
    { title: '操作', key: 'action', width: 180, fixed: 'right' as const },
  ];

  async function loadData() {
    loading.value = true;
    try {
      const result = await getReviewList({
        current: pagination.current!,
        pageSize: pagination.pageSize!,
      });
      dataSource.value = result.records || [];
      pagination.total = result.total || 0;
    } catch {
      message.error('加载审核队列失败');
    } finally {
      loading.value = false;
    }
  }

  function handleTableChange(pag: TablePaginationConfig) {
    pagination.current = pag.current;
    pagination.pageSize = pag.pageSize;
    loadData();
  }

  function showDetail(record: ChannelReviewVO) {
    detailDrawerRef.value?.open(record as any);
  }

  async function handleApprove(record: ChannelReviewVO) {
    Modal.confirm({
      title: '通过审核',
      content: `确定通过频道 "${record.channelName}" 的审核吗？`,
      onOk: async () => {
        try {
          await reviewAction({ channelId: record.channelId, action: 'APPROVE' });
          message.success('审核通过');
          loadData();
        } catch {
          message.error('操作失败');
        }
      },
    });
  }

  function showRejectModal(record: ChannelReviewVO) {
    rejectingRecord.value = record;
    rejectNote.value = '';
    rejectModalVisible.value = true;
  }

  async function handleReject() {
    if (!rejectingRecord.value) return;
    try {
      await reviewAction({
        channelId: rejectingRecord.value.channelId,
        action: 'REJECT',
        note: rejectNote.value,
      });
      message.success('已拒绝');
      rejectModalVisible.value = false;
      loadData();
    } catch {
      message.error('操作失败');
    }
  }

  onMounted(loadData);
</script>

<style scoped lang="less">
  .channel-review {
    padding: 24px;
  }
</style>
