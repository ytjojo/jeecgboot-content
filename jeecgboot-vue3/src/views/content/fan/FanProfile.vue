<template>
  <div class="fan-profile">
    <div class="page-header">
      <h3>粉丝画像</h3>
    </div>

    <a-spin :spinning="loading">
      <a-alert
        v-if="profileData?.tip"
        :message="profileData.tip"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />

      <a-row :gutter="16" v-if="hasData">
        <a-col :span="12">
          <a-card title="兴趣分布" size="small">
            <div ref="interestChartRef" class="chart-container" />
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card title="地域分布" size="small">
            <div ref="regionChartRef" class="chart-container" />
          </a-card>
        </a-col>
      </a-row>

      <a-card title="活跃时段" size="small" style="margin-top: 16px" v-if="hasData">
        <div ref="heatmapChartRef" class="chart-container" />
      </a-card>

      <a-empty v-if="!loading && !hasData && !profileData?.tip" description="暂无画像数据" />
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue';
import * as echarts from 'echarts';
import { getFanProfile } from '/@/api/content/fan-analytics';

const props = defineProps({
  userId: {
    type: String,
    required: true,
  },
});

const loading = ref(false);
const hasData = ref(false);
const profileData = ref<any>(null);
const interestChartRef = ref<HTMLDivElement>();
const regionChartRef = ref<HTMLDivElement>();
const heatmapChartRef = ref<HTMLDivElement>();
let interestChart: echarts.ECharts | null = null;
let regionChart: echarts.ECharts | null = null;
let heatmapChart: echarts.ECharts | null = null;

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getFanProfile(props.userId);
    profileData.value = res;
    hasData.value = !!(res?.interestDistribution?.length || res?.regionDistribution?.length || res?.activeTimeDistribution?.length);
    await nextTick();
    if (res?.interestDistribution?.length) renderInterestChart(res.interestDistribution);
    if (res?.regionDistribution?.length) renderRegionChart(res.regionDistribution);
    if (res?.activeTimeDistribution?.length) renderHeatmapChart(res.activeTimeDistribution);
  } finally {
    loading.value = false;
  }
};

const renderInterestChart = (data: any[]) => {
  if (!interestChartRef.value) return;
  if (!interestChart) interestChart = echarts.init(interestChartRef.value);
  interestChart.setOption({
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        data: data.map((d) => ({ name: d.label, value: d.count })),
      },
    ],
  });
};

const renderRegionChart = (data: any[]) => {
  if (!regionChartRef.value) return;
  if (!regionChart) regionChart = echarts.init(regionChartRef.value);
  regionChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.map((d) => d.label) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        type: 'bar',
        data: data.map((d) => d.count),
      },
    ],
    grid: { left: 50, right: 20, bottom: 30, top: 20 },
  });
};

const renderHeatmapChart = (data: any[]) => {
  if (!heatmapChartRef.value) return;
  if (!heatmapChart) heatmapChart = echarts.init(heatmapChartRef.value);

  const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`);
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
  const heatData: [number, number, number][] = [];
  data.forEach((d) => {
    heatData.push([d.hour, d.dayOfWeek, d.count]);
  });

  heatmapChart.setOption({
    tooltip: {
      formatter: (p: any) => `${days[p.value[1]]} ${hours[p.value[0]]}: ${p.value[2]}人`,
    },
    xAxis: { type: 'category', data: hours, splitArea: { show: true } },
    yAxis: { type: 'category', data: days },
    visualMap: {
      min: 0,
      max: Math.max(...heatData.map((d) => d[2]), 1),
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: 0,
    },
    series: [
      {
        type: 'heatmap',
        data: heatData,
        label: { show: false },
      },
    ],
    grid: { left: 60, right: 20, bottom: 60, top: 10 },
  });
};

const handleResize = () => {
  interestChart?.resize();
  regionChart?.resize();
  heatmapChart?.resize();
};

onMounted(() => {
  fetchData();
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  interestChart?.dispose();
  regionChart?.dispose();
  heatmapChart?.dispose();
  interestChart = null;
  regionChart = null;
  heatmapChart = null;
});
</script>

<style scoped>
.fan-profile {
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
  height: 280px;
}
</style>
