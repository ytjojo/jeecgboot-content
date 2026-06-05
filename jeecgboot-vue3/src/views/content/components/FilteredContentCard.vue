<template>
  <div class="filtered-content-card">
    <a-alert type="info" banner>
      <template #description>
        <span>该内容包含屏蔽词，已折叠</span>
        <a-button type="link" size="small" @click="expanded = !expanded">
          {{ expanded ? '收起' : '展开' }}
        </a-button>
      </template>
    </a-alert>
    <div v-if="expanded" class="filtered-content-body">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

defineProps({
  matchedKeywords: { type: Array as () => string[], default: () => [] },
});

const expanded = ref(false);
</script>

<style scoped>
.filtered-content-card {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 12px;
}
.filtered-content-card :deep(.ant-alert) {
  border-radius: 0;
}
.filtered-content-body {
  padding: 12px;
}
</style>
