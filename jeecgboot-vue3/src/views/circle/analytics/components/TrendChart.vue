<template>
  <a-card :bordered="false" class="trend-chart">
    <template #title>{{ title }}</template>
    <a-skeleton :loading="loading" active :paragraph="{ rows: 6 }">
      <a-empty v-if="!data || data.length === 0" description="暂无数据" />
      <div v-else ref="chartRef" class="trend-chart__container"></div>
    </a-skeleton>
  </a-card>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch, nextTick } from 'vue';
import { useECharts } from '/@/hooks/web/useECharts';
import type { DailyTrend } from '/@/api/content/model/circleAnalyticsModel';

type YField = 'newMemberCount' | 'newPostCount' | 'activeCount';

const props = withDefaults(defineProps<{
  title: string;
  data: DailyTrend[];
  yField: YField;
  color?: string;
  loading?: boolean;
}>(), {
  color: '#1890ff',
  loading: false,
});

const chartRef = ref<HTMLDivElement>();
const { setOptions } = useECharts(chartRef as any);

function buildOptions() {
  if (!props.data || props.data.length === 0) return {};

  const dates = props.data.map((d) => d.date);
  const values = props.data.map((d) => d[props.yField]);

  return {
    tooltip: {
      trigger: 'axis',
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
    },
    series: [
      {
        name: props.title,
        type: 'line',
        smooth: true,
        data: values,
        itemStyle: {
          color: props.color,
        },
        lineStyle: {
          color: props.color,
          width: 2,
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: props.color + '33' },
              { offset: 1, color: props.color + '05' },
            ],
          },
        },
      },
    ],
  };
}

function renderChart() {
  if (!chartRef.value || !props.data || props.data.length === 0) return;
  const options = buildOptions();
  setOptions(options);
}

watch(() => props.data, () => {
  nextTick(renderChart);
}, { deep: true });

watch(() => props.yField, () => {
  nextTick(renderChart);
});

watch(() => props.color, () => {
  nextTick(renderChart);
});

onMounted(() => {
  nextTick(renderChart);
});
</script>

<style lang="less" scoped>
.trend-chart {
  :deep(.ant-card-head) {
    min-height: 48px;
  }

  :deep(.ant-card-body) {
    padding: 0 24px 24px;
  }

  &__container {
    width: 100%;
    height: 300px;
  }
}
</style>
