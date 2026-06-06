<template>
  <a-tag :color="color">{{ label }}</a-tag>
</template>

<script setup lang="ts">
  import { computed } from 'vue';
  import type { ChannelType } from '/@/api/content/channel/model/channelModel';

  const props = defineProps<{
    type: ChannelType;
  }>();

  const colorMap: Record<ChannelType, string> = {
    system: 'blue',
    personal: 'green',
    organization: 'purple',
  };

  const labelMap: Record<ChannelType, string> = {
    system: '系统频道',
    personal: '个人频道',
    organization: '组织频道',
  };

  const color = computed(() => colorMap[props.type] ?? 'default');
  const label = computed(() => labelMap[props.type] ?? props.type);
</script>
