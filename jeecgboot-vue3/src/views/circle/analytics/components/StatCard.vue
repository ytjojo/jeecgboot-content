<template>
  <a-card :bordered="false" class="stat-card">
    <a-skeleton :loading="loading" active :paragraph="{ rows: 2 }">
      <div class="stat-card__content">
        <div class="stat-card__title">{{ title }}</div>
        <div class="stat-card__value">
          <span v-if="prefix" class="stat-card__prefix">{{ prefix }}</span>
          <span class="stat-card__number">{{ displayValue }}</span>
          <span v-if="suffix" class="stat-card__suffix">{{ suffix }}</span>
        </div>
        <div class="stat-card__change" :class="changeClass">
          <template v-if="change === 'new'">
            <span class="stat-card__new">新增</span>
          </template>
          <template v-else-if="change && change > 0">
            <ArrowUpOutlined />
            <span>+{{ change.toFixed(1) }}%</span>
          </template>
          <template v-else-if="change && change < 0">
            <ArrowDownOutlined />
            <span>{{ change.toFixed(1) }}%</span>
          </template>
          <template v-else>
            <MinusOutlined />
          </template>
        </div>
      </div>
    </a-skeleton>
  </a-card>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { ArrowUpOutlined, ArrowDownOutlined, MinusOutlined } from '@ant-design/icons-vue';

const props = withDefaults(defineProps<{
  title: string;
  value: number;
  change: number | 'new' | null;
  prefix?: string;
  suffix?: string;
  loading?: boolean;
}>(), {
  prefix: '',
  suffix: '',
  loading: false,
});

const displayValue = computed(() => {
  if (props.value === 0 && (props.change === 0 || props.change === null)) {
    return '--';
  }
  return props.value.toLocaleString();
});

const changeClass = computed(() => {
  if (props.change === 'new') return 'stat-card__change--new';
  if (typeof props.change === 'number') {
    if (props.change > 0) return 'stat-card__change--up';
    if (props.change < 0) return 'stat-card__change--down';
  }
  return 'stat-card__change--neutral';
});
</script>

<style lang="less" scoped>
.stat-card {
  :deep(.ant-card-body) {
    padding: 20px 24px;
  }

  &__content {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  &__title {
    font-size: 14px;
    color: var(--text-color-secondary, #666);
  }

  &__value {
    display: flex;
    align-items: baseline;
    gap: 4px;
  }

  &__prefix {
    font-size: 16px;
    color: var(--text-color, #333);
  }

  &__number {
    font-size: 28px;
    font-weight: 600;
    color: var(--text-color, #333);
    line-height: 1.2;
  }

  &__suffix {
    font-size: 14px;
    color: var(--text-color-secondary, #666);
  }

  &__change {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;

    &--new {
      color: #1890ff;
    }

    &--up {
      color: #52c41a;
    }

    &--down {
      color: #ff4d4f;
    }

    &--neutral {
      color: var(--text-color-tertiary, #999);
    }
  }

  &__new {
    font-weight: 500;
  }
}
</style>
