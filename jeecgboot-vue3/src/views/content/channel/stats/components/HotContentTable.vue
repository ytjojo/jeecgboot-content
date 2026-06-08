<template>
  <div class="hot-content-table">
    <div class="hot-content-table__toolbar">
      <a-radio-group v-model:value="currentPeriod" size="small" @change="$emit('periodChange', currentPeriod)">
        <a-radio-button value="7d">近7天</a-radio-button>
        <a-radio-button value="30d">近30天</a-radio-button>
        <a-radio-button value="90d">近90天</a-radio-button>
      </a-radio-group>
    </div>
    <a-table
      :columns="columns"
      :dataSource="data"
      :loading="loading"
      :pagination="false"
      size="small"
      rowKey="id"
    >
      <template #bodyCell="{ column, record, index }">
        <template v-if="column.key === 'rank'">
          <span :class="['hot-content-table__rank', `rank-${index + 1}`]">
            {{ index + 1 }}
          </span>
        </template>
        <template v-else-if="column.key === 'title'">
          <a @click="$emit('contentClick', record)">{{ record.title }}</a>
          <div class="hot-content-table__meta">
            <span>{{ record.contentType }}</span>
            <span> · {{ record.publishTime }}</span>
          </div>
        </template>
        <template v-else-if="column.key === 'interactionCount'">
          {{ record.interactionCount.toLocaleString() }}
        </template>
      </template>
    </a-table>
    <div class="hot-content-table__error" v-if="error">
      <a-typography-text type="danger">{{ error }}</a-typography-text>
      <a-button size="small" @click="$emit('retry')">重试</a-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import type { HotContentVO } from '/@/api/content/channel/stats';

defineProps<{
  data: HotContentVO[];
  loading?: boolean;
  error?: string | null;
}>();

defineEmits<{
  periodChange: [period: string];
  contentClick: [content: HotContentVO];
  retry: [];
}>();

const currentPeriod = ref('7d');

const columns = [
  { title: '排名', key: 'rank', width: 60, align: 'center' as const },
  { title: '内容标题', key: 'title', ellipsis: true },
  { title: '有效互动', key: 'interactionCount', width: 100, align: 'right' as const },
];
</script>

<style lang="less" scoped>
.hot-content-table {
  &__toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  &__rank {
    font-weight: 600;
    font-size: 16px;

    &.rank-1 { color: #f5222d; }
    &.rank-2 { color: #fa8c16; }
    &.rank-3 { color: #fadb14; }
  }

  &__meta {
    font-size: 12px;
    color: var(--text-color-secondary, #999);
    margin-top: 2px;
  }

  &__error {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 20px;
  }
}
</style>
