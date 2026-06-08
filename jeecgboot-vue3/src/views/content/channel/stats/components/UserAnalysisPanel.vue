<template>
  <div class="user-analysis-panel">
    <a-spin :spinning="loading">
      <a-row :gutter="16">
        <!-- 订阅趋势图 -->
        <a-col :xs="24" :lg="12">
          <a-card title="订阅趋势" size="small">
            <div ref="subscribeTrendRef" class="user-analysis-panel__chart"></div>
          </a-card>
        </a-col>

        <!-- 活跃度分布饼图 -->
        <a-col :xs="24" :lg="12">
          <a-card title="活跃度分布" size="small">
            <div ref="activityChartRef" class="user-analysis-panel__chart"></div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 贡献排行 -->
      <a-card title="贡献排行" size="small" class="user-analysis-panel__contribution">
        <a-table
          :columns="contributionColumns"
          :dataSource="data?.contributionRank || []"
          :pagination="false"
          size="small"
          rowKey="userId"
        >
          <template #bodyCell="{ column, record, index }">
            <template v-if="column.key === 'rank'">
              <span :class="['user-analysis-panel__rank', `rank-${index + 1}`]">
                {{ index + 1 }}
              </span>
            </template>
          </template>
        </a-table>
      </a-card>
    </a-spin>
    <div class="user-analysis-panel__error" v-if="error">
      <a-typography-text type="danger">{{ error }}</a-typography-text>
      <a-button size="small" @click="$emit('retry')">重试</a-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch, nextTick } from 'vue';
import { useECharts } from '/@/hooks/web/useECharts';
import type { UserAnalysisVO } from '/@/api/content/channel/stats';

const props = defineProps<{
  data: UserAnalysisVO | null;
  loading?: boolean;
  error?: string | null;
}>();

defineEmits<{
  retry: [];
}>();

const subscribeTrendRef = ref<HTMLDivElement>();
const activityChartRef = ref<HTMLDivElement>();
const { setOptions: setSubscribeOptions } = useECharts(subscribeTrendRef as any);
const { setOptions: setActivityOptions } = useECharts(activityChartRef as any);

function renderSubscribeTrend() {
  if (!props.data?.subscribeTrend?.length) return;
  const dates = props.data.subscribeTrend.map((d) => d.date);
  setSubscribeOptions({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增订阅', '流失订阅'], top: 0 },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [
      { name: '新增订阅', type: 'line', data: props.data.subscribeTrend.map((d) => d.subscribe), smooth: true },
      { name: '流失订阅', type: 'line', data: props.data.subscribeTrend.map((d) => d.unsubscribe), smooth: true },
    ],
  });
}

function renderActivityChart() {
  if (!props.data?.activityDistribution?.length) return;
  setActivityOptions({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      type: 'pie',
      radius: '60%',
      data: props.data.activityDistribution.map((d) => ({ name: d.level, value: d.count })),
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } },
    }],
  });
}

const contributionColumns = [
  { title: '排名', key: 'rank', width: 60, align: 'center' as const },
  { title: '用户', dataIndex: 'userName', ellipsis: true },
  { title: '贡献分', dataIndex: 'contribution', width: 100, align: 'right' as const },
];

watch(() => props.data, () => {
  nextTick(() => {
    renderSubscribeTrend();
    renderActivityChart();
  });
}, { deep: true });

onMounted(() => {
  nextTick(() => {
    renderSubscribeTrend();
    renderActivityChart();
  });
});
</script>

<style lang="less" scoped>
.user-analysis-panel {
  &__chart {
    width: 100%;
    height: 280px;
  }

  &__contribution {
    margin-top: 16px;
  }

  &__rank {
    font-weight: 600;
    &.rank-1 { color: #f5222d; }
    &.rank-2 { color: #fa8c16; }
    &.rank-3 { color: #fadb14; }
  }

  &__error {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 20px;
  }
}
</style>
