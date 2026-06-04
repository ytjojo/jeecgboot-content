<template>
  <div :class="['growth-progress', `growth-progress--${status}`]">
    <!-- 数值标签 -->
    <div v-if="showLabel" class="growth-progress__label">
      <span class="growth-progress__current">{{ current }}</span>
      <span class="growth-progress__sep">/</span>
      <span class="growth-progress__target">{{ target }}</span>
    </div>

    <!-- 进度条 -->
    <a-progress
      :percent="percent"
      :show-info="false"
      :stroke-color="strokeColor"
      :trail-color="trailColor"
      :stroke-width="strokeWidth"
      class="growth-progress__bar"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue';

  const props = withDefaults(
    defineProps<{
      current: number;
      target: number;
      showLabel?: boolean;
      status?: 'normal' | 'warning' | 'danger';
    }>(),
    {
      showLabel: true,
      status: 'normal',
    }
  );

  const percent = computed(() => {
    if (props.target <= 0) return 0;
    return Math.min(Math.round((props.current / props.target) * 100), 100);
  });

  const strokeWidth = 10;

  const strokeColor = computed(() => {
    switch (props.status) {
      case 'danger':
        return '#ff4d4f';
      case 'warning':
        return '#faad14';
      default:
        return '#1890ff';
    }
  });

  const trailColor = computed(() => {
    switch (props.status) {
      case 'danger':
        return 'rgba(255, 77, 79, 0.1)';
      case 'warning':
        return 'rgba(250, 173, 20, 0.1)';
      default:
        return 'rgba(24, 144, 255, 0.1)';
    }
  });
</script>

<style scoped lang="less">
  .growth-progress {
    &__label {
      margin-bottom: 6px;
      font-size: 13px;
      display: flex;
      align-items: baseline;
    }

    &__current {
      font-size: 18px;
      font-weight: 600;
      color: rgba(0, 0, 0, 0.85);
      transition: color 0.3s;
    }

    &__sep {
      margin: 0 4px;
      color: rgba(0, 0, 0, 0.25);
    }

    &__target {
      color: rgba(0, 0, 0, 0.45);
    }

    &__bar {
      transition: all 0.3s;
    }

    // status 变体
    &--warning &__current {
      color: #faad14;
    }

    &--danger &__current {
      color: #ff4d4f;
    }
  }
</style>
