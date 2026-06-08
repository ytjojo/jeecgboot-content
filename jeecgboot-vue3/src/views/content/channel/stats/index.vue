<template>
  <div class="channel-stats-dashboard">
    <!-- 页面标题和时间筛选 -->
    <div class="channel-stats-dashboard__header">
      <h2 class="channel-stats-dashboard__title">数据看板</h2>
      <div class="channel-stats-dashboard__toolbar">
        <a-radio-group v-model:value="timeRange" size="small" @change="handleTimeRangeChange">
          <a-radio-button value="day">日</a-radio-button>
          <a-radio-button value="week">周</a-radio-button>
          <a-radio-button value="month">月</a-radio-button>
          <a-radio-button value="custom">自定义</a-radio-button>
        </a-radio-group>
        <a-range-picker
          v-if="timeRange === 'custom'"
          v-model:value="customDateRange"
          size="small"
          style="margin-left: 8px; width: 240px"
          @change="handleCustomDateChange"
        />
      </div>
      <div class="channel-stats-dashboard__update-time" v-if="store.coreStats?.updateTime">
        数据更新时间：{{ store.coreStats.updateTime }}
      </div>
    </div>

    <!-- 核心指标卡片 (骨架屏) -->
    <a-row :gutter="16" class="channel-stats-dashboard__core-stats">
      <a-col :xs="12" :sm="12" :md="6" v-for="card in coreCards" :key="card.key">
        <a-skeleton v-if="store.coreLoading" active :paragraph="{ rows: 2 }" />
        <StatsCard
          v-else
          :title="card.title"
          :value="card.value"
          :trend="card.trend"
          :error="store.coreError"
          :icon="card.icon"
          :suffix="card.suffix"
          @retry="store.fetchCoreStats()"
        />
      </a-col>
    </a-row>

    <!-- 趋势图 -->
    <a-row :gutter="16" class="channel-stats-dashboard__section">
      <a-col :span="24">
        <a-card title="趋势数据" size="small">
          <a-skeleton v-if="store.trendLoading" active :paragraph="{ rows: 6 }" />
          <StatsTrendChart
            v-else
            :data="store.trendData"
            :loading="store.trendLoading"
            :error="store.trendError"
            @range-change="handleTrendRangeChange"
            @retry="store.fetchTrendData()"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 互动数据 -->
    <a-row :gutter="16" class="channel-stats-dashboard__section">
      <a-col :span="24">
        <a-card title="互动数据" size="small">
          <a-skeleton v-if="store.interactionLoading" active :paragraph="{ rows: 3 }" />
          <template v-else-if="store.interactionError">
            <div class="channel-stats-dashboard__error-card">
              <a-typography-text type="danger">{{ store.interactionError }}</a-typography-text>
              <a-button size="small" @click="store.fetchInteraction()">重试</a-button>
            </div>
          </template>
          <a-row :gutter="[16, 16]" v-else-if="store.interaction">
            <a-col :xs="12" :sm="8" :md="4" v-for="item in interactionItems" :key="item.key">
              <StatsCard
                :title="item.label"
                :value="item.value"
                :prefix="item.prefix"
                :suffix="item.suffix"
              />
            </a-col>
          </a-row>
        </a-card>
      </a-col>
    </a-row>

    <!-- 热门内容 -->
    <a-row :gutter="16" class="channel-stats-dashboard__section">
      <a-col :xs="24" :lg="12">
        <a-card title="热门内容排行" size="small">
          <a-skeleton v-if="store.hotContentLoading" active :paragraph="{ rows: 5 }" />
          <HotContentTable
            v-else
            :data="store.hotContent"
            :loading="store.hotContentLoading"
            :error="store.hotContentError"
            @period-change="handleHotPeriodChange"
            @retry="store.fetchHotContent()"
          />
        </a-card>
      </a-col>

      <!-- 用户分析 -->
      <a-col :xs="24" :lg="12">
        <a-card title="用户分析" size="small">
          <a-skeleton v-if="store.userAnalysisLoading" active :paragraph="{ rows: 6 }" />
          <UserAnalysisPanel
            v-else
            :data="store.userAnalysis"
            :loading="store.userAnalysisLoading"
            :error="store.userAnalysisError"
            @retry="store.fetchUserAnalysis()"
          />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import StatsCard from '/@/views/content/channel/components/StatsCard.vue';
import StatsTrendChart from './components/StatsTrendChart.vue';
import HotContentTable from './components/HotContentTable.vue';
import UserAnalysisPanel from './components/UserAnalysisPanel.vue';
import { useChannelStatsStore } from '/@/store/modules/channelStats';

const route = useRoute();
const store = useChannelStatsStore();

const timeRange = ref<'day' | 'week' | 'month' | 'custom'>('day');
const customDateRange = ref<any>(null);
const currentPeriod = ref<'7d' | '30d' | '90d'>('7d');

// 核心指标卡片配置
const coreCards = computed(() => [
  {
    key: 'subscriber',
    title: '订阅数',
    value: store.coreStats?.subscriberCount || 0,
    trend: store.coreStats?.subscriberTrend,
    icon: '👥',
  },
  {
    key: 'content',
    title: '内容数',
    value: store.coreStats?.contentCount || 0,
    trend: store.coreStats?.contentTrend,
    icon: '📄',
  },
  {
    key: 'pv',
    title: 'PV',
    value: store.coreStats?.pv || 0,
    trend: store.coreStats?.pvTrend,
    icon: '👁️',
  },
  {
    key: 'uv',
    title: 'UV',
    value: store.coreStats?.uv || 0,
    trend: store.coreStats?.uvTrend,
    icon: '👤',
  },
]);

// 互动数据项
const interactionItems = computed(() => [
  { key: 'like', label: '点赞', value: store.interaction?.likeCount || 0 },
  { key: 'comment', label: '评论', value: store.interaction?.commentCount || 0 },
  { key: 'favorite', label: '收藏', value: store.interaction?.favoriteCount || 0 },
  { key: 'share', label: '分享', value: store.interaction?.shareCount || 0 },
  { key: 'visit', label: '有效访问', value: store.interaction?.visitCount || 0 },
]);

function handleTimeRangeChange() {
  if (timeRange.value !== 'custom') {
    customDateRange.value = null;
    store.setTimeRange(timeRange.value);
    store.fetchTrendData();
  }
}

function handleCustomDateChange(_: any, dateStrings: [string, string]) {
  store.setCustomDateRange(dateStrings);
  store.fetchTrendData();
}

function handleTrendRangeChange(range: string) {
  store.setTimeRange(range as any);
  store.fetchTrendData();
}

function handleHotPeriodChange(period: string) {
  currentPeriod.value = period as any;
  store.setHotPeriod(period as any);
  store.fetchHotContent();
}

// 页面初始化：并行加载所有数据 (Promise.allSettled)
async function loadAllData() {
  const channelId = (route.query.channelId as string) || '';
  store.setChannelId(channelId);

  await store.fetchAllData();
}

onMounted(() => {
  loadAllData();
});

onUnmounted(() => {
  // 无需清理
});
</script>

<style lang="less" scoped>
.channel-stats-dashboard {
  padding: 16px;

  &__header {
    margin-bottom: 16px;
  }

  &__title {
    margin: 0 0 12px;
    font-size: 20px;
    font-weight: 600;
  }

  &__toolbar {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
  }

  &__update-time {
    font-size: 12px;
    color: var(--text-color-secondary, #999);
  }

  &__core-stats {
    margin-bottom: 16px;
  }

  &__section {
    margin-bottom: 16px;
  }

  &__error-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 30px;
  }
}

// 响应式适配
@media screen and (max-width: 768px) {
  .channel-stats-dashboard {
    padding: 12px;

    &__title {
      font-size: 16px;
    }
  }
}
</style>
