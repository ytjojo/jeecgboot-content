<template>
  <a-tag v-if="mutualFollow" color="blue" class="mutual-follow-badge">
    <template #icon><swap-outlined /></template>
    互关
  </a-tag>
</template>

<script setup lang="ts">
import { SwapOutlined } from '@ant-design/icons-vue';

defineProps({
  mutualFollow: {
    type: Boolean,
    default: false,
  },
});
</script>

<style scoped>
.mutual-follow-badge {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
}
</style>
