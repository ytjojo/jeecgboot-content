<template>
  <a-tag
    v-if="mutualFollow"
    color="green"
    class="mutual-follow-badge"
    :class="{
      'mutual-follow-badge--small': size === 'small',
      'mutual-follow-badge--inline': inline,
    }"
  >
    <template #icon><swap-outlined /></template>
    互关
  </a-tag>
</template>

<script setup lang="ts">
import { SwapOutlined } from '@ant-design/icons-vue';

withDefaults(
  defineProps<{
    mutualFollow?: boolean;
    size?: 'default' | 'small';
    inline?: boolean;
  }>(),
  {
    mutualFollow: false,
    size: 'default',
    inline: false,
  },
);
</script>

<style scoped>
.mutual-follow-badge {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
}
.mutual-follow-badge--small {
  font-size: 10px;
  padding: 0 4px;
}
.mutual-follow-badge--inline {
  display: inline;
}
</style>
