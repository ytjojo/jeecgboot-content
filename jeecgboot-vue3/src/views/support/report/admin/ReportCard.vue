<template>
  <div class="report-card" :class="{ 'is-pending': report.status === 'PENDING' }">
    <div class="report-card-header">
      <span class="report-card-no">#{{ report.id?.slice(0, 8) }}</span>
      <Tag :color="statusColor">{{ statusLabel }}</Tag>
    </div>
    <div class="report-card-body">
      <div class="report-card-summary">内容: {{ report.contentId }}</div>
      <div class="report-card-meta">
        <Tag>{{ report.reason }}</Tag>
        <span>{{ report.createTime }}</span>
      </div>
    </div>
    <div class="report-card-actions">
      <Button size="small" @click="$emit('detail', report)">查看</Button>
      <Button v-if="report.status === 'PENDING'" size="small" type="primary" danger @click="$emit('deleteContent', report)">
        删除内容
      </Button>
      <Button v-if="report.status === 'PENDING'" size="small" @click="$emit('ignore', report)">
        忽略
      </Button>
      <Button v-if="report.status === 'PENDING'" size="small" @click="$emit('mute', report)">
        禁言
      </Button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { Button, Tag } from 'ant-design-vue';
import type { CircleReportVO } from '/@/api/content/circle/report';

const props = defineProps<{ report: CircleReportVO }>();

defineEmits<{
  detail: [report: CircleReportVO];
  deleteContent: [report: CircleReportVO];
  ignore: [report: CircleReportVO];
  mute: [report: CircleReportVO];
}>();

const statusColor = computed(() => {
  switch (props.report.status) {
    case 'PENDING': return 'orange';
    case 'RESOLVED': return 'green';
    case 'IGNORED': return 'default';
    default: return 'default';
  }
});

const statusLabel = computed(() => {
  switch (props.report.status) {
    case 'PENDING': return '待处理';
    case 'RESOLVED': return '已处理';
    case 'IGNORED': return '已忽略';
    default: return props.report.status;
  }
});
</script>

<style lang="less" scoped>
.report-card {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 10px;

  &.is-pending { border-left: 3px solid #fa8c16; }

  &-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
  }
  &-no { font-size: 12px; color: #999; font-family: monospace; }
  &-body { margin-bottom: 10px; }
  &-summary { font-size: 14px; font-weight: 500; margin-bottom: 6px; }
  &-meta { display: flex; align-items: center; gap: 12px; font-size: 12px; color: #999; }
  &-actions { display: flex; flex-wrap: wrap; gap: 8px; justify-content: flex-end; }
}
</style>
