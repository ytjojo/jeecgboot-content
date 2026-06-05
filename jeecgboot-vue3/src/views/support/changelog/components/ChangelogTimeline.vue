<template>
  <div class="changelog-timeline">
    <a-timeline>
      <a-timeline-item v-for="version in versions" :key="version.version" color="blue">
        <template #dot>
          <clock-circle-outlined style="font-size: 16px" />
        </template>
        <VersionCard :version="version" :search-keyword="searchKeyword" />
      </a-timeline-item>
    </a-timeline>
    <a-empty v-if="versions.length === 0" description="暂无更新记录" />
  </div>
</template>

<script setup lang="ts">
import { ClockCircleOutlined } from '@ant-design/icons-vue';
import { type ChangelogVersion } from '/@/api/support/changelog';
import VersionCard from './VersionCard.vue';

defineProps<{
  versions: ChangelogVersion[];
  searchKeyword?: string;
}>();
</script>

<style scoped lang="less">
.changelog-timeline {
  padding: 16px 0;
}

@media (max-width: 768px) {
  .changelog-timeline :deep(.ant-timeline) {
    padding-left: 0;
  }
}
</style>
