<template>
  <a-card class="version-card" size="small">
    <div class="version-header">
      <a-tag color="blue">v{{ version.version }}</a-tag>
      <span class="release-date">{{ version.releaseDate }}</span>
    </div>

    <div v-if="version.additions?.length" class="change-section">
      <a-tag color="green">新增功能</a-tag>
      <ul>
        <li v-for="(item, i) in version.additions" :key="i">
          <span v-if="searchKeyword" v-html="highlightKeyword(item)" />
          <span v-else>{{ item }}</span>
        </li>
      </ul>
    </div>

    <div v-if="version.improvements?.length" class="change-section">
      <a-tag color="blue">优化内容</a-tag>
      <ul>
        <li v-for="(item, i) in version.improvements" :key="i">
          <span v-if="searchKeyword" v-html="highlightKeyword(item)" />
          <span v-else>{{ item }}</span>
        </li>
      </ul>
    </div>

    <div v-if="version.fixes?.length" class="change-section">
      <a-tag color="orange">修复问题</a-tag>
      <ul>
        <li v-for="(item, i) in version.fixes" :key="i">
          <span v-if="searchKeyword" v-html="highlightKeyword(item)" />
          <span v-else>{{ item }}</span>
        </li>
      </ul>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { type ChangelogVersion } from '/@/api/support/changelog';

const props = defineProps<{
  version: ChangelogVersion;
  searchKeyword?: string;
}>();

const highlightKeyword = (text: string) => {
  if (!props.searchKeyword) return text;
  const regex = new RegExp(`(${props.searchKeyword})`, 'gi');
  return text.replace(regex, '<span style="color: #1890ff; font-weight: bold">$1</span>');
};
</script>

<style scoped lang="less">
.version-card {
  margin-bottom: 16px;

  .version-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;

    .release-date {
      color: #999;
      font-size: 13px;
    }
  }

  .change-section {
    margin-bottom: 8px;

    ul {
      margin-top: 4px;
      padding-left: 20px;

      li {
        line-height: 1.8;
      }
    }
  }
}

@media (max-width: 768px) {
  .version-card {
    .version-header {
      flex-direction: column;
      align-items: flex-start;
    }
  }
}
</style>
