<template>
  <div class="data-analytics-panel">
    <div class="panel-header">
      <div class="header-left">
        <a-button type="text" @click="goBack">
          <template #icon><ArrowLeftOutlined /></template>
          返回
        </a-button>
        <h2 class="panel-title">圈子数据统计</h2>
      </div>
      <div class="header-right">
        <TimeRangeSelector :value="store.dateRange ?? defaultRange" @change="handleRangeChange" />
        <a-button
          type="primary"
          :loading="store.exporting"
          :disabled="store.loading || !store.analyticsData"
          @click="handleExport"
        >
          <template #icon><ExportOutlined /></template>
          导出
        </a-button>
      </div>
    </div>

    <a-result
      v-if="store.error"
      status="error"
      title="加载失败"
      :sub-title="store.error"
    >
      <template #extra>
        <a-button type="primary" @click="retry">重试</a-button>
      </template>
    </a-result>

    <template v-else>
      <a-row :gutter="[16, 16]" class="stats-row">
        <a-col :xs="24" :sm="12" :lg="6">
          <StatCard
            title="成员总数"
            :value="store.analyticsData?.memberCount ?? 0"
            :change="null"
            suffix="人"
            :loading="store.loading"
          />
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <StatCard
            title="新增成员"
            :value="store.analyticsData?.newMemberCount ?? 0"
            :change="store.memberChange"
            suffix="人"
            :loading="store.loading"
          />
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <StatCard
            title="发帖总数"
            :value="store.analyticsData?.postCount ?? 0"
            :change="store.postChange"
            suffix="篇"
            :loading="store.loading"
          />
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <StatCard
            title="活跃用户"
            :value="store.analyticsData?.activeCount ?? 0"
            :change="store.activeChange"
            suffix="人"
            :loading="store.loading"
          />
        </a-col>
      </a-row>

      <a-row :gutter="[16, 16]" class="charts-row">
        <a-col :xs="24" :lg="8">
          <TrendChart
            title="成员增长趋势"
            :data="store.analyticsData?.dailyTrends ?? []"
            y-field="newMemberCount"
            color="#1890ff"
            :loading="store.loading"
          />
        </a-col>
        <a-col :xs="24" :lg="8">
          <TrendChart
            title="内容发布趋势"
            :data="store.analyticsData?.dailyTrends ?? []"
            y-field="newPostCount"
            color="#52c41a"
            :loading="store.loading"
          />
        </a-col>
        <a-col :xs="24" :lg="8">
          <TrendChart
            title="活跃度趋势"
            :data="store.analyticsData?.dailyTrends ?? []"
            y-field="activeCount"
            color="#faad14"
            :loading="store.loading"
          />
        </a-col>
      </a-row>
    </template>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowLeftOutlined, ExportOutlined } from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import { useCircleAnalyticsStoreWithOut } from '/@/store/modules/circleAnalytics';
import StatCard from './StatCard.vue';
import TrendChart from './TrendChart.vue';
import TimeRangeSelector from './TimeRangeSelector.vue';

const route = useRoute();
const router = useRouter();
const store = useCircleAnalyticsStoreWithOut();

const circleId = computed(() => route.params.id as string);

const defaultRange = computed(() => {
  const end = dayjs();
  const start = end.subtract(6, 'day');
  return {
    startDate: start.format('YYYY-MM-DD'),
    endDate: end.format('YYYY-MM-DD'),
  };
});

function goBack() {
  router.push(`/circle/${circleId.value}`);
}

function handleRangeChange(range: { startDate: string; endDate: string }) {
  store.fetchAnalytics(circleId.value, range);
}

function retry() {
  const range = store.dateRange ?? defaultRange.value;
  store.fetchAnalytics(circleId.value, range);
}

async function handleExport() {
  if (!store.dateRange) return;
  try {
    await store.exportAnalytics(circleId.value, store.dateRange);
  } catch {
    // export failure is handled by store (sets error)
  }
}

onMounted(() => {
  store.fetchAnalytics(circleId.value, defaultRange.value);
});
</script>

<style lang="less" scoped>
.data-analytics-panel {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.panel-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-color, #333);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.stats-row {
  margin-bottom: 16px;
}

.charts-row {
  margin-top: 16px;
}

@media (max-width: 768px) {
  .data-analytics-panel {
    padding: 16px 12px;
  }

  .panel-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-right {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
