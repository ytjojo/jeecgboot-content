<template>
  <div class="changelog-page">
    <a-card title="更新日志">
      <a-input-search
        v-model:value="keyword"
        placeholder="搜索功能名称..."
        style="max-width: 400px; margin-bottom: 24px"
        allow-clear
      />

      <ChangelogTimeline :versions="filteredVersions" :search-keyword="keyword" />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { getChangelogList, type ChangelogVersion } from '/@/api/support/changelog';
import ChangelogTimeline from './components/ChangelogTimeline.vue';

const versions = ref<ChangelogVersion[]>([]);
const keyword = ref('');

const filteredVersions = computed(() => {
  if (!keyword.value.trim()) return versions.value;
  const kw = keyword.value.trim().toLowerCase();
  return versions.value.filter(
    (v) =>
      v.additions?.some((a) => a.toLowerCase().includes(kw)) ||
      v.improvements?.some((i) => i.toLowerCase().includes(kw)) ||
      v.fixes?.some((f) => f.toLowerCase().includes(kw)),
  );
});

onMounted(async () => {
  const res = await getChangelogList();
  versions.value = res.result || [];
});
</script>

<style scoped lang="less">
.changelog-page {
  padding: 16px;
}

@media (max-width: 768px) {
  .changelog-page {
    padding: 8px;
  }
}
</style>
