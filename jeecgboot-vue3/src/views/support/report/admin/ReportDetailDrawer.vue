<template>
  <a-drawer
    :open="visible"
    title="举报详情"
    placement="right"
    :width="480"
    @close="$emit('update:visible', false)"
  >
    <template v-if="report">
      <a-descriptions :column="1" size="small" bordered>
        <a-descriptions-item label="举报ID">{{ report.id }}</a-descriptions-item>
        <a-descriptions-item label="圈子ID">{{ report.circleId }}</a-descriptions-item>
        <a-descriptions-item label="被举报内容ID">{{ report.contentId }}</a-descriptions-item>
        <a-descriptions-item label="举报人">{{ report.reporterId }}</a-descriptions-item>
        <a-descriptions-item label="举报原因">
          <a-tag>{{ report.reason }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="statusColor">{{ statusLabel }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="处理动作">{{ report.handleAction || '-' }}</a-descriptions-item>
        <a-descriptions-item label="举报时间">{{ report.createTime }}</a-descriptions-item>
      </a-descriptions>
    </template>
    <a-empty v-else description="暂无详情" />
  </a-drawer>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { Drawer, Descriptions, Tag, Empty } from 'ant-design-vue';
import type { CircleReportVO } from '/@/api/content/circle/report';

const props = defineProps<{
  visible: boolean;
  report: CircleReportVO | null;
}>();

defineEmits<{
  'update:visible': [value: boolean];
}>();

const statusColor = computed(() => {
  switch (props.report?.status) {
    case 'PENDING': return 'orange';
    case 'RESOLVED': return 'green';
    case 'IGNORED': return 'default';
    default: return 'default';
  }
});

const statusLabel = computed(() => {
  switch (props.report?.status) {
    case 'PENDING': return '待处理';
    case 'RESOLVED': return '已处理';
    case 'IGNORED': return '已忽略';
    default: return props.report?.status ?? '-';
  }
});
</script>
