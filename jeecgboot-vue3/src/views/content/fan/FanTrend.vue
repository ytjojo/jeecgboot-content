<template>
  <div class="fan-trend">
    <div class="page-header">
      <h3>粉丝趋势</h3>
      <a-radio-group v-model:value="range" button-style="solid" size="small" @change="handleRangeChange">
        <a-radio-button value="day">近7天</a-radio-button>
        <a-radio-button value="week">近4周</a-radio-button>
        <a-radio-button value="month">近6月</a-radio-button>
      </a-radio-group>
    </div>

    <a-spin :spinning="loading">
      <div ref="chartRef" class="chart-container" />
      <a-empty v-if="!loading && !hasData" description="暂无趋势数据" />
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue';
import * as echarts from 'echarts';
import { getFanTrend } from '/@/api/content/fan-analytics';
import { SOCIAL_EVENTS, trackSocialEvent } from '/@/utils/social/analytics';

const props = defineProps({
  userId: {
    type: String,
    required: true,
  },
});

const range = ref('day');
const loading = ref(false);
const hasData = ref(false);
const chartRef = ref<HTMLDivElement>();
let chart: echarts.ECharts | null = null;

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getFanTrend(props.userId, { period: range.value });
    const dates: string[] = [];
    const counts: number[] = [];
    (res || []).forEach((item: any) => {
      dates.push(item.date);
      counts.push(item.newFollowerCount || 0);
    });
    hasData.value = dates.length > 0;
    await nextTick();
    renderChart(dates, counts);
  } finally {
    loading.value = false;
  }
};

const renderChart = (dates: string[], counts: number[]) => {
  if (!chartRef.value) return;
  if (!chart) {
    chart = echarts.init(chartRef.value);
  }
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '新增粉丝',
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.3 },
        data: counts,
      },
    ],
    grid: { left: 50, right: 20, bottom: 30, top: 30 },
  });
  chart.on('click', (params: any) => {
    trackSocialEvent(SOCIAL_EVENTS.FAN_TREND_POINT_CLICK, {
      date: params.name,
      value: params.value,
      period: range.value,
    });
  });
};

const handleRangeChange = () => {
  fetchData();
};

const handleResize = () => {
  chart?.resize();
};

onMounted(() => {
  trackSocialEvent(SOCIAL_EVENTS.FAN_TREND_VIEW, { period: range.value });
  fetchData();
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  chart?.dispose();
  chart = null;
});
</script>

<style scoped>
.fan-trend {
  padding: 16px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.chart-container {
  width: 100%;
  height: 360px;
}
</style>
