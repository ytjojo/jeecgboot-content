<template>
  <div class="publish-result">
    <div class="result-header">发布结果</div>
    <div v-if="scheduledTime" class="scheduled-info">
      <ClockCircleOutlined /> 已设定发布时间：{{ scheduledTime }}
    </div>
    <div v-for="item in results" :key="item.channelId" :class="['result-item', item.status]">
      <div class="result-status">
        <CheckCircleOutlined v-if="item.status === 'success'" class="icon-success" />
        <ClockCircleOutlined v-else-if="item.status === 'review'" class="icon-review" />
        <ClockCircleOutlined v-else-if="item.status === 'pending'" class="icon-pending" />
        <CloseCircleOutlined v-else class="icon-fail" />
      </div>
      <div class="result-info">
        <span class="channel-name">{{ item.channelName }}</span>
        <span v-if="item.status === 'success'" class="status-text">已发布</span>
        <span v-else-if="item.status === 'review'" class="status-text">已提交审核，等待管理员处理</span>
        <span v-else-if="item.status === 'pending'" class="status-text">等待发布</span>
        <span v-else class="status-text fail-reason">{{ item.failReason }}</span>
      </div>
      <Button v-if="item.status === 'fail'" class="retry-btn" size="small" @click="$emit('retry', item.channelId)">
        重试
      </Button>
    </div>
    <div class="result-actions">
      <Button @click="$emit('viewContent')">查看内容</Button>
      <Button @click="$emit('backToEdit')">返回编辑</Button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { Button } from 'ant-design-vue';
import { CheckCircleOutlined, ClockCircleOutlined, CloseCircleOutlined } from '@ant-design/icons-vue';

interface ResultItem {
  channelId: string;
  channelName: string;
  status: 'success' | 'review' | 'pending' | 'fail';
  failReason?: string;
}

defineProps<{
  results: ResultItem[];
  scheduledTime?: string;
}>();

defineEmits<{
  (e: 'retry', channelId: string): void;
  (e: 'viewContent'): void;
  (e: 'backToEdit'): void;
}>();
</script>

<style lang="less" scoped>
.publish-result {
  padding: 16px;
  .result-header { font-size: 16px; font-weight: 600; margin-bottom: 12px; }
  .scheduled-info { margin-bottom: 12px; color: #1890ff; }
  .result-item {
    display: flex; align-items: center; padding: 8px 12px; margin-bottom: 8px; border-radius: 6px;
    &.success { background: #f6ffed; }
    &.review { background: #e6f7ff; }
    &.pending { background: #fffbe6; }
    &.fail { background: #fff2f0; }
    .result-status { margin-right: 8px; font-size: 18px; }
    .icon-success { color: #52c41a; }
    .icon-review { color: #1890ff; }
    .icon-pending { color: #faad14; }
    .icon-fail { color: #ff4d4f; }
    .result-info { flex: 1; }
    .channel-name { font-weight: 500; margin-right: 8px; }
    .fail-reason { color: #ff4d4f; }
  }
  .result-actions { margin-top: 16px; display: flex; gap: 8px; justify-content: flex-end; }
}
</style>
