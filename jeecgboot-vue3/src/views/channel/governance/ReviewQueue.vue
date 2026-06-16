<template>
  <div class="review-queue">
    <div class="queue-header">
      <span>待审区</span>
      <Tag color="red" v-if="stats.timeoutCount > 0">超时 {{ stats.timeoutCount }}</Tag>
    </div>
    <!-- 桌面端：筛选栏 + 表格 -->
    <div class="desktop-view">
      <div class="filter-bar">
        <Space>
          <Select v-model:value="filter.contentType" placeholder="内容类型" allowClear style="width: 120px">
            <Select.Option value="article">文章</Select.Option>
            <Select.Option value="post">图文帖子</Select.Option>
            <Select.Option value="video">视频</Select.Option>
            <Select.Option value="note">笔记</Select.Option>
            <Select.Option value="question">问答问题</Select.Option>
          </Select>
          <Input v-model:value="filter.keyword" placeholder="搜索" style="width: 200px" allowClear />
        </Space>
      </div>
      <Table
        :dataSource="reviewList"
        :columns="columns"
        :loading="loading"
        :rowSelection="{ selectedRowKeys: selectedIds, onChange: onSelectChange }"
        rowKey="id"
        :row-class-name="rowClassName"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <Space>
              <Button size="small" type="primary" @click="handleApprove(record.id)">通过</Button>
              <Button size="small" danger class="reject-btn" @click="handleReject(record.id)">拒绝</Button>
            </Space>
          </template>
        </template>
      </Table>
    </div>

    <!-- 移动端：下拉筛选 + 卡片列表 -->
    <div class="mobile-view">
      <div class="mobile-filter">
        <Select v-model:value="filter.contentType" placeholder="筛选类型" allowClear style="width: 100%">
          <Select.Option value="article">文章</Select.Option>
          <Select.Option value="post">图文帖子</Select.Option>
          <Select.Option value="video">视频</Select.Option>
          <Select.Option value="note">笔记</Select.Option>
          <Select.Option value="question">问答问题</Select.Option>
        </Select>
      </div>
      <a-spin :spinning="loading">
        <div v-if="reviewList.length === 0" class="mobile-empty">
          <a-empty description="暂无待审内容" />
        </div>
        <div v-for="record in reviewList" :key="record.id" class="review-card">
          <div class="review-card-header">
            <span class="review-card-title">{{ record.title }}</span>
            <Tag color="orange" v-if="record.isTimeout">超时</Tag>
          </div>
          <div class="review-card-meta">
            <span>{{ record.contentType || '-' }}</span>
            <span>{{ record.submitter || '-' }}</span>
            <span>{{ record.submitTime || '-' }}</span>
          </div>
          <div class="review-card-actions">
            <Button size="small" type="primary" @click="handleApprove(record.id)">通过</Button>
            <Button size="small" danger @click="handleReject(record.id)">拒绝</Button>
          </div>
        </div>
      </a-spin>
    </div>

    <!-- 底部固定操作栏 -->
    <div class="batch-bar" v-if="selectedIds.length > 0">
      <Space>
        <span>已选 {{ selectedIds.length }} 条</span>
        <Button type="primary" @click="handleBatchApprove">批量通过</Button>
        <Button danger @click="handleBatchReject">批量拒绝</Button>
      </Space>
    </div>
    <RejectReasonModal v-model:visible="rejectVisible" @confirm="handleRejectConfirm" />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import { Table, Button, Space, Select, Input, Tag, Modal, message } from 'ant-design-vue';
import { useChannelReviewStore } from '/@/store/modules/channelReview';
import RejectReasonModal from '../components/RejectReasonModal.vue';
import { storeToRefs } from 'pinia';
import { track } from '/@/utils/track';

const props = defineProps<{ channelId: string }>();
const store = useChannelReviewStore();
const { reviewList, selectedIds, stats, loading } = storeToRefs(store);

const filter = reactive({ contentType: undefined as string | undefined, keyword: '' });
const rejectVisible = ref(false);
const rejectingIds = ref<string[]>([]);
let statsTimer: ReturnType<typeof setInterval>;

const columns = [
  { title: '标题', dataIndex: 'title', key: 'title' },
  { title: '类型', dataIndex: 'contentType', key: 'contentType', width: 80 },
  { title: '提交者', dataIndex: 'submitter', key: 'submitter', width: 100 },
  { title: '提交时间', dataIndex: 'submitTime', key: 'submitTime', width: 160 },
  { title: '来源场景', dataIndex: 'sourceScene', key: 'sourceScene', width: 100 },
  { title: '命中规则', dataIndex: 'hitRule', key: 'hitRule', width: 120 },
  { title: '操作', key: 'action', width: 140 },
];

const rowClassName = (record: any) => record.isTimeout ? 'timeout-row' : '';

const onSelectChange = (keys: string[]) => store.setSelectedIds(keys);

const handleApprove = async (id: string) => {
  try {
    await store.approve(id);
    track('review_approve', { channel_id: props.channelId, is_batch: false });
    message.success('审核通过');
  } catch {
    message.error('操作失败，请重试');
  }
};

const handleReject = (id: string) => {
  rejectingIds.value = [id];
  rejectVisible.value = true;
};

const handleRejectConfirm = async (reason: string) => {
  try {
    const isBatch = rejectingIds.value.length > 1;
    if (isBatch) {
      store.setSelectedIds(rejectingIds.value);
      await store.batchReject(reason);
    } else {
      await store.reject(rejectingIds.value[0], reason);
    }
    track('review_reject', { channel_id: props.channelId, is_batch: isBatch, batch_count: rejectingIds.value.length, reason_type: reason });
    rejectVisible.value = false;
    message.success('已拒绝并通知提交者');
  } catch {
    message.error('操作失败，请重试');
  }
};

const handleBatchApprove = () => {
  Modal.confirm({
    title: '批量通过',
    content: `确认通过选中的 ${selectedIds.value.length} 条内容？通过后内容将在频道中展示。`,
    onOk: async () => {
      try {
        await store.batchApprove();
        track('review_approve', { channel_id: props.channelId, is_batch: true, batch_count: selectedIds.value.length });
        message.success('批量通过完成');
      } catch {
        message.error('操作失败，请重试');
      }
    },
  });
};

const handleBatchReject = () => {
  rejectingIds.value = [...selectedIds.value];
  rejectVisible.value = true;
};

onMounted(() => {
  store.setFilter({ channelId: props.channelId });
  store.fetchList();
  store.fetchStats(props.channelId).then(() => {
    if (stats.value.timeoutCount > 0) {
      track('review_timeout_alert', { channel_id: props.channelId, timeout_count: stats.value.timeoutCount });
    }
  });
  statsTimer = setInterval(() => store.fetchStats(props.channelId), 60000);
});

onUnmounted(() => clearInterval(statsTimer));
</script>

<style lang="less" scoped>
.review-queue {
  .queue-header { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; font-weight: 600; }
  .filter-bar { margin-bottom: 12px; }
  .batch-bar { position: sticky; bottom: 0; background: #fff; padding: 12px; border-top: 1px solid #e8e8e8; display: flex; justify-content: flex-end; z-index: 10; }
  :deep(.timeout-row) { background: #fff2f0; }

  .mobile-view { display: none; }

  @media (max-width: 768px) {
    .desktop-view { display: none; }
    .mobile-view { display: block; }

    .mobile-filter { margin-bottom: 12px; }

    .review-card {
      background: #fff;
      border: 1px solid #e8e8e8;
      border-radius: 8px;
      padding: 12px;
      margin-bottom: 10px;

      &-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 8px;
      }
      &-title {
        font-size: 15px;
        font-weight: 500;
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      &-meta {
        display: flex;
        gap: 12px;
        font-size: 12px;
        color: #999;
        margin-bottom: 10px;
      }
      &-actions {
        display: flex;
        gap: 8px;
        justify-content: flex-end;
      }
    }

    .mobile-empty { padding: 40px 0; }

    .batch-bar {
      position: fixed;
      bottom: 0;
      left: 0;
      right: 0;
      padding: 12px 16px;
      box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.08);
    }
  }
}
</style>
