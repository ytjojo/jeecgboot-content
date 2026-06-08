<template>
  <div class="stats-card" :class="{ 'stats-card--loading': loading }">
    <a-skeleton v-if="loading" active :paragraph="{ rows: 2 }" />
    <template v-else-if="error">
      <div class="stats-card__error">
        <a-typography-text type="danger">{{ error }}</a-typography-text>
        <a-button size="small" @click="$emit('retry')">重试</a-button>
      </div>
    </template>
    <template v-else>
      <div class="stats-card__header">
        <span class="stats-card__title">{{ title }}</span>
        <span class="stats-card__icon" v-if="icon">{{ icon }}</span>
      </div>
      <div class="stats-card__value">
        <CountTo :startVal="0" :endVal="value" :duration="1500" :decimals="decimals" :prefix="prefix" :suffix="suffix" />
      </div>
      <div class="stats-card__trend" v-if="trend !== undefined && trend !== null">
        <span :class="['stats-card__trend-arrow', trend >= 0 ? 'up' : 'down']">
          {{ trend >= 0 ? '↑' : '↓' }}
        </span>
        <span :class="['stats-card__trend-value', trend >= 0 ? 'up' : 'down']">
          {{ Math.abs(trend) }}%
        </span>
        <span class="stats-card__trend-label">较上期</span>
      </div>
    </template>
  </div>
</template>

<script lang="ts" setup>
import { CountTo } from '/@/components/CountTo';

defineProps<{
  title: string;
  value: number;
  trend?: number;
  loading?: boolean;
  error?: string | null;
  icon?: string;
  prefix?: string;
  suffix?: string;
  decimals?: number;
}>();

defineEmits<{
  retry: [];
}>();
</script>

<style lang="less" scoped>
.stats-card {
  background: var(--component-background, #fff);
  border-radius: 8px;
  padding: 20px 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.3s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  &--loading {
    min-height: 120px;
  }

  &__error {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
  }

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }

  &__title {
    font-size: 14px;
    color: var(--text-color-secondary, #666);
  }

  &__icon {
    font-size: 24px;
    opacity: 0.6;
  }

  &__value {
    font-size: 32px;
    font-weight: 600;
    color: var(--text-color, #333);
    line-height: 1.2;
    margin-bottom: 8px;
  }

  &__trend {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;

    &-arrow {
      font-weight: bold;
    }

    &-value {
      font-weight: 500;

      &.up {
        color: #52c41a;
      }

      &.down {
        color: #ff4d4f;
      }
    }

    &-label {
      color: var(--text-color-secondary, #999);
    }
  }
}
</style>
