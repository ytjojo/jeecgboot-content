<template>
  <div class="stats-trend-chart">
    <div class="stats-trend-chart__toolbar">
      <a-radio-group v-model:value="currentRange" size="small" @change="handleRangeChange">
        <a-radio-button value="day">日</a-radio-button>
        <a-radio-button value="week">周</a-radio-button>
        <a-radio-button value="month">月</a-radio-button>
        <a-radio-button value="custom" @click="customVisible = true">自定义</a-radio-button>
      </a-radio-group>
    </div>
    <a-spin :spinning="loading">
      <div ref="chartRef" class="stats-trend-chart__container"></div>
    </a-spin>
    <div class="stats-trend-chart__error" v-if="error">
      <a-typography-text type="danger">{{ error }}</a-typography-text>
      <a-button size="small" @click="$emit('retry')">重试</a-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch, nextTick } from 'vue';
import { useECharts } from '/@/hooks/web/useECharts';
import type { TrendVO } from '/@/api/content/channel/stats';

const props = defineProps<{
  data: TrendVO[];
  loading?: boolean;
  error?: string | null;
}>();

defineEmits<{
  rangeChange: [range: string];
  retry: [];
}>();

const chartRef = ref<HTMLDivElement>();
const currentRange = ref('day');
const customVisible = ref(false);
const { setOptions, echarts } = useECharts(chartRef as any);

function buildOptions() {
  if (!props.data || props.data.length === 0) return {};

  const dates = props.data.map((d) => d.date);

  return {
    tooltip: {
      trigger: 'axis',
    },
    legend: {
      data: ['订阅数', '内容数', 'PV', 'UV'],
      top: 0,
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
    },
    yAxis: {
      type: 'value',
    },
    series: [
      {
        name: '订阅数',
        type: 'line',
        data: props.data.map((d) => d.subscriberCount),
        smooth: true,
        areaStyle: { opacity: 0.1 },
      },
      {
        name: '内容数',
        type: 'line',
        data: props.data.map((d) => d.contentCount),
        smooth: true,
        areaStyle: { opacity: 0.1 },
      },
      {
        name: 'PV',
        type: 'line',
        data: props.data.map((d) => d.pv),
        smooth: true,
        areaStyle: { opacity: 0.1 },
      },
      {
        name: 'UV',
        type: 'line',
        data: props.data.map((d) => d.uv),
        smooth: true,
        areaStyle: { opacity: 0.1 },
      },
    ],
  };
}

function renderChart() {
  if (!chartRef.value) return;
  const options = buildOptions();
  setOptions(options);
}

function handleRangeChange(e: any) {
  if (e.target.value !== 'custom') {
    currentRange.value = e.target.value;
  }
}

watch(() => props.data, () => {
  nextTick(renderChart);
}, { deep: true });

onMounted(() => {
  nextTick(renderChart);
});
</script>

<style lang="less" scoped>
.stats-trend-chart {
  &__toolbar {
    display: flex;
    justify-content: flex-end;
    margin-bottom: 12px;
  }

  &__container {
    width: 100%;
    height: 350px;
  }

  &__error {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 40px;
  }
}
</style>
