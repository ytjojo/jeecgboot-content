<template>
  <a-tag :color="color">{{ label }}</a-tag>
</template>

<script setup lang="ts">
  import { computed } from 'vue';
  import type { ChannelStatus } from '/@/api/content/channel/model/channelModel';

  const props = defineProps<{
    status: ChannelStatus;
  }>();

  const configMap: Record<ChannelStatus, { color: string; label: string }> = {
    DRAFT: { color: 'default', label: '草稿' },
    PENDING_REVIEW: { color: 'orange', label: '待审核' },
    ACTIVE: { color: 'green', label: '已激活' },
    REJECTED: { color: 'red', label: '已拒绝' },
    DELETE_COOLING: { color: 'volcano', label: '删除冷静期' },
    DELETED: { color: 'default', label: '已删除' },
  };

  const config = computed(() => configMap[props.status] ?? { color: 'default', label: props.status });
  const color = computed(() => config.value.color);
  const label = computed(() => config.value.label);
</script>
