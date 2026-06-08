<template>
  <a-tag :color="statusColor" :class="['status-tag', size]">
    {{ statusLabel }}
  </a-tag>
</template>

<script lang="ts" setup>
import { computed } from 'vue';

const STATUS_MAP: Record<string, { color: string; label: string }> = {
  Active: { color: 'green', label: '运营中' },
  ReadonlyFrozen: { color: 'orange', label: '已冻结' },
  Hidden: { color: 'red', label: '已隐藏' },
  Archived: { color: 'default', label: '已归档' },
  Merged: { color: 'blue', label: '已合并' },
  Closed: { color: '', label: '已关闭' },
  Pending: { color: 'processing', label: '审核中' },
  Rejected: { color: 'error', label: '已拒绝' },
  Draft: { color: 'default', label: '草稿' },
  // 审核状态
  pending: { color: 'processing', label: '待审核' },
  approved: { color: 'success', label: '已通过' },
  rejected: { color: 'error', label: '已拒绝' },
  // 申诉状态
  submitted: { color: 'processing', label: '处理中' },
  resolved_restore: { color: 'success', label: '已恢复' },
  resolved_maintain: { color: 'warning', label: '维持原判' },
};

const props = withDefaults(defineProps<{
  status: string;
  size?: 'small' | 'default';
}>(), {
  size: 'default',
});

const statusConfig = computed(() => {
  return STATUS_MAP[props.status] || { color: 'default', label: props.status };
});

const statusColor = computed(() => statusConfig.value.color);
const statusLabel = computed(() => statusConfig.value.label);
</script>

<style lang="less" scoped>
.status-tag {
  &.small {
    font-size: 12px;
    line-height: 20px;
  }
}
</style>
