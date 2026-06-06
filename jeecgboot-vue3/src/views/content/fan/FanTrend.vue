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
import { ref, onMounted, nextTick } from 'vue';
import { useECharts } from '/@/hooks/web/useECharts';
import { getFanTrend } from '/@/api/content/fan-analytics';
import { SOCIAL_EVENTS, trackSocialEvent } from '/@/utils/social/analytics';

const props = defineProps({
  userId: {
    type: String,
    required: true,
  },
});

const emit = defineEmits<{
  (e: 'point-click', data: { date: string; value: number }): void;
}>();

const range = ref('day');
const loading = ref(false);
const hasData = ref(false);
const chartRef = ref<HTMLDivElement>();
const { setOptions, getInstance } = useECharts(chartRef);

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
  setOptions({
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
  const instance = getInstance();
  instance?.on('click', (params: any) => {
    trackSocialEvent(SOCIAL_EVENTS.FAN_TREND_POINT_CLICK, {
      date: params.name,
      value: params.value,
      period: range.value,
    });
    emit('point-click', { date: params.name, value: params.value });
  });
};

const handleRangeChange = () => {
  fetchData();
};

onMounted(() => {
  trackSocialEvent(SOCIAL_EVENTS.FAN_TREND_VIEW, { period: range.value });
  fetchData();
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
