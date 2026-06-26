<template>
  <div class="leaderboard-tabs">
    <a-tabs :active-key="dimension" @change="handleDimensionChange">
      <a-tab-pane key="experience" tab="经验值" />
      <a-tab-pane key="contribution" tab="贡献值" />
      <a-tab-pane key="posts" tab="发帖数" />
    </a-tabs>
    <div class="period-selector">
      <a-segmented v-model:value="periodValue" :options="periodOptions" @change="handlePeriodChange" />
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue';

  type Dimension = 'experience' | 'contribution' | 'posts';
  type Period = 'week' | 'month' | 'all';

  const props = defineProps<{
    dimension: Dimension;
    period: Period;
  }>();

  const emit = defineEmits<{
    'update:dimension': [value: Dimension];
    'update:period': [value: Period];
  }>();

  const periodOptions = [
    { label: '本周', value: 'week' },
    { label: '本月', value: 'month' },
    { label: '累计', value: 'all' },
  ];

  const periodValue = computed({
    get: () => props.period,
    set: (val: Period) => {
      emit('update:period', val);
    },
  });

  function handleDimensionChange(key: string) {
    emit('update:dimension', key as Dimension);
  }

  function handlePeriodChange(val: string | number) {
    emit('update:period', val as Period);
  }
</script>

<style lang="less" scoped>
  .leaderboard-tabs {
    background: #fff;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 16px;
  }

  .period-selector {
    display: flex;
    justify-content: flex-end;
    margin-top: -8px;
  }

  @media (max-width: 767px) {
    .leaderboard-tabs {
      padding: 12px;
    }

    .period-selector {
      justify-content: flex-start;
      margin-top: 8px;

      :deep(.ant-segmented) {
        width: 100%;
      }

      :deep(.ant-segmented-item) {
        flex: 1;
        text-align: center;
      }
    }
  }
</style>
