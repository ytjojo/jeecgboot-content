# 圈子数据统计与推荐发现 前端实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现圈子数据统计面板、推荐列表、热门/新锐榜单，以及推荐曝光点击上报的前端功能。

**Architecture:** 基于 Vue 3 + TypeScript + Ant Design Vue，新增 API 层（3 个文件）、Pinia Store（2 个）、业务组件（7 个）、composable（2 个）。数据统计页为独立路由 `/circle/:id/analytics`，推荐与榜单在现有 `/circle` 列表页通过 Tab 切换展示。ECharts 已有依赖（^5.6.0），封装 `useChart` composable 复用生命周期管理。

**Tech Stack:** Vue 3, TypeScript, Ant Design Vue, Pinia, ECharts 5, Jest + @vue/test-utils

---

## File Structure

```
jeecgboot-vue3/src/
├── api/circle/
│   ├── types.ts                     # 共享 TypeScript 类型定义
│   ├── analytics.ts                 # 统计数据 API
│   ├── recommend.ts                 # 推荐 API
│   └── ranking.ts                   # 榜单 API
├── store/modules/
│   ├── circleAnalytics.ts           # useCircleAnalyticsStore
│   └── circleRecommend.ts           # useCircleRecommendStore
├── hooks/circle/
│   ├── useChart.ts                  # ECharts composable
│   └── useRecommendTracking.ts      # 曝光/点击上报 composable
├── views/circle/
│   ├── analytics/
│   │   ├── index.vue                # 数据统计页主页面
│   │   └── components/
│   │       ├── StatCard.vue         # 统计指标卡片
│   │       ├── TrendChart.vue       # 趋势折线图
│   │       ├── TimeRangeSelector.vue# 时间范围选择器
│   │       └── DataAnalyticsPanel.vue # 数据统计面板
│   └── list/
│       ├── index.vue                # 圈子列表页（增强，已有则修改）
│       └── components/
│           ├── RankCircleCard.vue   # 榜单圈子卡片
│           ├── RankList.vue         # 榜单列表
│           └── RecommendationPanel.vue # 推荐面板
└── __tests__/circle/
    ├── analytics.test.ts
    ├── recommendation.test.ts
    ├── ranking.test.ts
    └── recommendTracking.test.ts
```

---

## Task 1: TypeScript 类型定义

**Files:**
- Create: `src/api/circle/types.ts`

- [ ] **Step 1: 创建类型定义文件**

```typescript
// src/api/circle/types.ts

/** 趋势数据点 — 对应后端 CircleDataStatisticsVO.DailyTrend */
export interface DailyTrend {
  date: string;           // YYYY-MM-DD
  newMemberCount: number;
  newPostCount: number;
  activeCount: number;
}

/** 圈子统计数据响应 — 对应后端 CircleDataStatisticsVO */
export interface CircleAnalyticsVO {
  memberCount: number;     // 成员总数
  newMemberCount: number;  // 新增成员数
  postCount: number;       // 帖子总数
  newPostCount: number;    // 新增帖子数
  activeCount: number;     // 活跃用户数
  dailyTrends: DailyTrend[]; // 每日趋势数据
}

/** 推荐圈子项 — 对应后端 CircleRecommendVO.CircleRecommendItem */
export interface CircleRecommendItem {
  circleId: string;
  circleName: string;
  description: string;
  memberCount: number;
  category: string;
  privacyType: string;     // 'PUBLIC' | 'PRIVATE' | 'PASSWORD'
  sourceId: string;        // 推荐来源追踪ID
}

/** 推荐圈子响应 — 对应后端 CircleRecommendVO */
export interface CircleRecommendVO {
  items: CircleRecommendItem[];
}

/** 榜单圈子项 — 对应后端 CircleRankingVO.CircleRankingItem */
export interface CircleRankItem {
  rank: number;
  circleId: string;
  circleName: string;
  description: string;
  memberCount: number;
  category: string;
  createTime: string;
}

/** 榜单响应 — 对应后端 CircleRankingVO */
export interface CircleRankingVO {
  type: string;            // 'HOT' | 'NEW'
  items: CircleRankItem[];
}

/** 推荐曝光上报请求 */
export interface RecommendExposureReq {
  circleIds: string[];
  source: string;
}

/** 推荐点击上报请求 — 对应后端 recordClick(sourceId) */
export interface RecommendClickReq {
  sourceId: string;        // 后端参数名为 sourceId
}

/** 时间范围 */
export interface DateRange {
  startDate: string; // YYYY-MM-DD
  endDate: string;   // YYYY-MM-DD
}
```

- [ ] **Step 2: Commit**

```bash
git add src/api/circle/types.ts
git commit -m "feat(circle): add TypeScript type definitions for analytics and recommendation"
```

---

## Task 2: API 层

**Files:**
- Create: `src/api/circle/analytics.ts`
- Create: `src/api/circle/recommend.ts`
- Create: `src/api/circle/ranking.ts`

- [ ] **Step 1: 创建统计数据 API**

```typescript
// src/api/circle/analytics.ts
import { defHttp } from '/@/utils/http/axios';
import type { CircleAnalyticsVO, DateRange } from './types';

enum Api {
  statistics = '/api/circle',  // 后端基础路径
  exportCsv = '/api/circle',
}

/** 获取圈子统计数据 — 对应后端 GET /api/circle/{circleId}/data/statistics */
export const getCircleAnalytics = (circleId: string, params: DateRange) =>
  defHttp.get<CircleAnalyticsVO>({
    url: `${Api.statistics}/${circleId}/data/statistics`,
    params,
  });

/** 导出统计数据 CSV — 对应后端 GET /api/circle/{circleId}/data/export */
export const exportCircleAnalyticsCsv = (circleId: string, params: DateRange) =>
  defHttp.get({
    url: `${Api.exportCsv}/${circleId}/data/export`,
    params,
    responseType: 'blob',
  });
```

- [ ] **Step 2: 创建推荐 API**

```typescript
// src/api/circle/recommend.ts
import { defHttp } from '/@/utils/http/axios';
import type { CircleRecommendVO, RecommendExposureReq, RecommendClickReq } from './types';

enum Api {
  recommend = '/api/circle/recommend',
  exposure = '/api/circle/recommend/exposure',  // 后端待开发
  click = '/api/circle/recommend/click',
}

/** 获取推荐圈子列表 — 对应后端 GET /api/circle/recommend
 *  注意: 后端返回 CircleRecommendVO { items: [...] }，非直接数组
 */
export const getRecommendList = (params?: { limit?: number }) =>
  defHttp.get<CircleRecommendVO>({ url: Api.recommend, params });

/** 上报推荐曝光 — 后端待开发，需确认接口是否已就绪 */
export const reportRecommendExposure = (data: RecommendExposureReq) =>
  defHttp.post({ url: Api.exposure, data });

/** 上报推荐点击 — 对应后端 POST /api/circle/recommend/click
 *  注意: 后端参数为 sourceId (RequestParam)，非 JSON body
 */
export const reportRecommendClick = (sourceId: string) =>
  defHttp.post({ url: Api.click, params: { sourceId } });
```

- [ ] **Step 3: 创建榜单 API**

```typescript
// src/api/circle/ranking.ts
import { defHttp } from '/@/utils/http/axios';
import type { CircleRankingVO } from './types';

enum Api {
  hotRank = '/api/circle/ranking/hot',
  newRank = '/api/circle/ranking/new',
}

/** 获取热门榜单 — 对应后端 GET /api/circle/ranking/hot */
export const getHotRankList = (params?: { limit?: number }) =>
  defHttp.get<CircleRankingVO>({ url: Api.hotRank, params });

/** 获取新锐榜单 — 对应后端 GET /api/circle/ranking/new */
export const getNewRankList = (params?: { limit?: number }) =>
  defHttp.get<CircleRankingVO>({ url: Api.newRank, params });
```

- [ ] **Step 4: Commit**

```bash
git add src/api/circle/
git commit -m "feat(circle): add API layer for analytics, recommendation, and ranking"
```

---

## Task 3: Pinia Store — useCircleAnalyticsStore

**Files:**
- Create: `src/store/modules/circleAnalytics.ts`
- Test: `src/__tests__/circle/analytics.test.ts` (partial — store tests)

- [ ] **Step 1: 编写 store 测试**

```typescript
// src/__tests__/circle/analytics.test.ts
import { setActivePinia, createPinia } from 'pinia';
import { useCircleAnalyticsStore } from '/@/store/modules/circleAnalytics';

// Mock API
jest.mock('/@/api/circle/analytics', () => ({
  getCircleAnalytics: jest.fn(),
  exportCircleAnalyticsCsv: jest.fn(),
}));

import { getCircleAnalytics } from '/@/api/circle/analytics';

describe('useCircleAnalyticsStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('should have correct initial state', () => {
    const store = useCircleAnalyticsStore();
    expect(store.analyticsData).toBeNull();
    expect(store.loading).toBe(false);
    expect(store.error).toBeNull();
    expect(store.exporting).toBe(false);
  });

  it('should fetch analytics data successfully', async () => {
    const mockData = {
      memberCount: 100,
      newMemberCount: 10,
      postCount: 50,
      newPostCount: 5,
      activeCount: 30,
      dailyTrends: [],
    };
    (getCircleAnalytics as jest.Mock).mockResolvedValue(mockData);

    const store = useCircleAnalyticsStore();
    await store.fetchAnalytics('circle-1', { startDate: '2024-01-01', endDate: '2024-01-07' });

    expect(store.analyticsData).toEqual(mockData);
    expect(store.loading).toBe(false);
    expect(store.error).toBeNull();
  });

  it('should set error on fetch failure', async () => {
    (getCircleAnalytics as jest.Mock).mockRejectedValue(new Error('Network error'));

    const store = useCircleAnalyticsStore();
    await store.fetchAnalytics('circle-1', { startDate: '2024-01-01', endDate: '2024-01-07' });

    expect(store.analyticsData).toBeNull();
    expect(store.loading).toBe(false);
    expect(store.error).toBe('Network error');
  });

  it('should clear data', () => {
    const store = useCircleAnalyticsStore();
    store.analyticsData = {} as any;
    store.error = 'some error';
    store.clearData();
    expect(store.analyticsData).toBeNull();
    expect(store.error).toBeNull();
  });
});
```

- [ ] **Step 2: 运行测试验证失败**

Run: `npx jest src/__tests__/circle/analytics.test.ts --no-cache 2>&1 | tail -20`
Expected: FAIL — module not found `circleAnalytics`

- [ ] **Step 3: 实现 store**

```typescript
// src/store/modules/circleAnalytics.ts
import { defineStore } from 'pinia';
import { getCircleAnalytics, exportCircleAnalyticsCsv } from '/@/api/circle/analytics';
import type { CircleAnalyticsVO, DateRange } from '/@/api/circle/types';

interface CircleAnalyticsState {
  analyticsData: CircleAnalyticsVO | null;
  dateRange: DateRange;
  loading: boolean;
  error: string | null;
  exporting: boolean;
  lastFetchTime: number;
}

export const useCircleAnalyticsStore = defineStore({
  id: 'circle-analytics',
  state: (): CircleAnalyticsState => ({
    analyticsData: null,
    dateRange: { startDate: '', endDate: '' },
    loading: false,
    error: null,
    exporting: false,
    lastFetchTime: 0,
  }),
  actions: {
    async fetchAnalytics(circleId: string, dateRange: DateRange) {
      this.loading = true;
      this.error = null;
      this.dateRange = dateRange;
      try {
        this.analyticsData = await getCircleAnalytics(circleId, dateRange);
        this.lastFetchTime = Date.now();
      } catch (e: any) {
        this.error = e?.message || '请求失败';
        this.analyticsData = null;
      } finally {
        this.loading = false;
      }
    },
    async exportAnalytics(circleId: string, dateRange: DateRange) {
      this.exporting = true;
      try {
        const blob = await exportCircleAnalyticsCsv(circleId, dateRange);
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `circle_${dateRange.startDate}_${dateRange.endDate}.csv`;
        a.click();
        URL.revokeObjectURL(url);
      } finally {
        this.exporting = false;
      }
    },
    setDateRange(dateRange: DateRange) {
      this.dateRange = dateRange;
    },
    clearData() {
      this.analyticsData = null;
      this.error = null;
      this.lastFetchTime = 0;
    },
    /** 检查缓存是否过期（5 分钟） */
    isCacheExpired(): boolean {
      return Date.now() - this.lastFetchTime > 5 * 60 * 1000;
    },
  },
});
```

- [ ] **Step 4: 运行测试验证通过**

Run: `npx jest src/__tests__/circle/analytics.test.ts --no-cache 2>&1 | tail -20`
Expected: PASS — all 4 tests pass

- [ ] **Step 5: Commit**

```bash
git add src/store/modules/circleAnalytics.ts src/__tests__/circle/analytics.test.ts
git commit -m "feat(circle): add useCircleAnalyticsStore with TDD"
```

---

## Task 4: Pinia Store — useCircleRecommendStore

**Files:**
- Create: `src/store/modules/circleRecommend.ts`
- Test: `src/__tests__/circle/recommendation.test.ts` (partial — store tests)

- [ ] **Step 1: 编写 store 测试**

```typescript
// src/__tests__/circle/recommendation.test.ts
import { setActivePinia, createPinia } from 'pinia';
import { useCircleRecommendStore } from '/@/store/modules/circleRecommend';

jest.mock('/@/api/circle/recommend', () => ({
  getRecommendList: jest.fn(),
  reportRecommendExposure: jest.fn(),
  reportRecommendClick: jest.fn(),
}));
jest.mock('/@/api/circle/ranking', () => ({
  getHotRankList: jest.fn(),
  getNewRankList: jest.fn(),
}));

import { getRecommendList } from '/@/api/circle/recommend';
import { getHotRankList } from '/@/api/circle/ranking';

describe('useCircleRecommendStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('should have correct initial state', () => {
    const store = useCircleRecommendStore();
    expect(store.recommendList).toEqual([]);
    expect(store.hotRankList).toEqual([]);
    expect(store.newRankList).toEqual([]);
    expect(store.activeTab).toBe('recommend');
    expect(store.fallbackMode).toBe(false);
  });

  it('should fetch recommend list successfully', async () => {
    const mockList = [{ id: '1', name: 'Test Circle', privacyType: 'PUBLIC' }];
    (getRecommendList as jest.Mock).mockResolvedValue(mockList);

    const store = useCircleRecommendStore();
    await store.fetchRecommendList();

    expect(store.recommendList).toEqual(mockList);
    expect(store.fallbackMode).toBe(false);
  });

  it('should enter fallback mode when recommend returns empty', async () => {
    (getRecommendList as jest.Mock).mockResolvedValue([]);
    (getHotRankList as jest.Mock).mockResolvedValue([{ id: '1', name: 'Hot Circle' }]);

    const store = useCircleRecommendStore();
    await store.fetchRecommendList();

    expect(store.fallbackMode).toBe(true);
    expect(store.hotRankList.length).toBeGreaterThan(0);
  });

  it('should enter fallback mode when recommend request fails', async () => {
    (getRecommendList as jest.Mock).mockRejectedValue(new Error('Network error'));
    (getHotRankList as jest.Mock).mockResolvedValue([{ id: '1', name: 'Hot Circle' }]);

    const store = useCircleRecommendStore();
    await store.fetchRecommendList();

    expect(store.fallbackMode).toBe(true);
  });

  it('should clear fallback mode when recommend returns data', async () => {
    const mockList = [{ id: '1', name: 'Test Circle' }];
    (getRecommendList as jest.Mock).mockResolvedValue(mockList);

    const store = useCircleRecommendStore();
    store.fallbackMode = true;
    await store.fetchRecommendList();

    expect(store.fallbackMode).toBe(false);
  });

  it('should cache tab data and not re-fetch', async () => {
    const mockHotList = [{ id: '1', name: 'Hot' }];
    (getHotRankList as jest.Mock).mockResolvedValue(mockHotList);

    const store = useCircleRecommendStore();
    await store.fetchHotRankList();
    await store.fetchHotRankList(); // second call

    expect(getHotRankList).toHaveBeenCalledTimes(1);
  });
});
```

- [ ] **Step 2: 运行测试验证失败**

Run: `npx jest src/__tests__/circle/recommendation.test.ts --no-cache 2>&1 | tail -20`
Expected: FAIL — module not found

- [ ] **Step 3: 实现 store**

```typescript
// src/store/modules/circleRecommend.ts
import { defineStore } from 'pinia';
import { getRecommendList } from '/@/api/circle/recommend';
import { getHotRankList, getNewRankList } from '/@/api/circle/ranking';
import type { CircleRecommendVO, CircleRankVO } from '/@/api/circle/types';

type TabType = 'recommend' | 'hot' | 'new';

interface CircleRecommendState {
  recommendList: CircleRecommendVO[];
  hotRankList: CircleRankVO[];
  newRankList: CircleRankVO[];
  activeTab: TabType;
  loading: Record<TabType, boolean>;
  fallbackMode: boolean;
  /** 各 Tab 是否已加载过（会话级缓存标记） */
  loaded: Record<TabType, boolean>;
}

export const useCircleRecommendStore = defineStore({
  id: 'circle-recommend',
  state: (): CircleRecommendState => ({
    recommendList: [],
    hotRankList: [],
    newRankList: [],
    activeTab: 'recommend',
    loading: { recommend: false, hot: false, new: false },
    fallbackMode: false,
    loaded: { recommend: false, hot: false, new: false },
  }),
  actions: {
    async fetchRecommendList() {
      if (this.loaded.recommend && !this.fallbackMode) return;
      this.loading.recommend = true;
      try {
        const list = await getRecommendList({ limit: 20 });
        if (list && list.length > 0) {
          this.recommendList = list;
          this.fallbackMode = false;
        } else {
          this.fallbackMode = true;
          await this.fetchHotRankList();
        }
        this.loaded.recommend = true;
      } catch {
        this.fallbackMode = true;
        await this.fetchHotRankList();
      } finally {
        this.loading.recommend = false;
      }
    },
    async fetchHotRankList() {
      if (this.loaded.hot) return;
      this.loading.hot = true;
      try {
        this.hotRankList = await getHotRankList({ limit: 20 });
        this.loaded.hot = true;
      } finally {
        this.loading.hot = false;
      }
    },
    async fetchNewRankList() {
      if (this.loaded.new) return;
      this.loading.new = true;
      try {
        this.newRankList = await getNewRankList({ limit: 20 });
        this.loaded.new = true;
      } finally {
        this.loading.new = false;
      }
    },
    setActiveTab(tab: TabType) {
      this.activeTab = tab;
    },
    /** 强制刷新推荐（降级状态下切回推荐 Tab 时用） */
    async refreshRecommend() {
      this.loaded.recommend = false;
      await this.fetchRecommendList();
    },
    clearAll() {
      this.recommendList = [];
      this.hotRankList = [];
      this.newRankList = [];
      this.fallbackMode = false;
      this.loaded = { recommend: false, hot: false, new: false };
    },
  },
});
```

- [ ] **Step 4: 运行测试验证通过**

Run: `npx jest src/__tests__/circle/recommendation.test.ts --no-cache 2>&1 | tail -20`
Expected: PASS — all 6 tests pass

- [ ] **Step 5: Commit**

```bash
git add src/store/modules/circleRecommend.ts src/__tests__/circle/recommendation.test.ts
git commit -m "feat(circle): add useCircleRecommendStore with TDD"
```

---

## Task 5: useChart composable

**Files:**
- Create: `src/hooks/circle/useChart.ts`

- [ ] **Step 1: 实现 useChart composable**

参考现有 `FanTrend.vue` 的 ECharts 使用模式，封装为 composable：

```typescript
// src/hooks/circle/useChart.ts
import { ref, onMounted, onBeforeUnmount, watch, type Ref, shallowRef } from 'vue';
import * as echarts from 'echarts';

export interface UseChartOptions {
  /** 图表配置，返回 ECharts option */
  getOption: () => echarts.EChartsOption;
  /** 数据源，变化时重新渲染 */
  data: Ref<any>;
  /** 是否加载中 */
  loading?: Ref<boolean>;
}

export function useChart(options: UseChartOptions) {
  const chartRef = ref<HTMLDivElement>();
  const chart = shallowRef<echarts.ECharts | null>(null);

  const renderChart = () => {
    if (!chartRef.value) return;
    if (!chart.value) {
      chart.value = echarts.init(chartRef.value);
    }
    chart.value.setOption(options.getOption(), true);
  };

  const handleResize = () => {
    chart.value?.resize();
  };

  watch(
    () => options.data.value,
    () => {
      renderChart();
    },
    { deep: true }
  );

  onMounted(() => {
    renderChart();
    window.addEventListener('resize', handleResize);
  });

  onBeforeUnmount(() => {
    window.removeEventListener('resize', handleResize);
    chart.value?.dispose();
    chart.value = null;
  });

  return {
    chartRef,
    chart,
    renderChart,
  };
}
```

- [ ] **Step 2: Commit**

```bash
git add src/hooks/circle/useChart.ts
git commit -m "feat(circle): add useChart composable for ECharts lifecycle management"
```

---

## Task 6: StatCard 组件

**Files:**
- Create: `src/views/circle/analytics/components/StatCard.vue`

- [ ] **Step 1: 实现 StatCard 组件**

```vue
<!-- src/views/circle/analytics/components/StatCard.vue -->
<template>
  <a-card class="stat-card" :bordered="false">
    <a-skeleton :loading="loading" :paragraph="{ rows: 1 }" :title="false" active>
      <div class="stat-card__title">{{ title }}</div>
      <div class="stat-card__value">
        <span v-if="prefix" class="stat-card__prefix">{{ prefix }}</span>
        <span class="stat-card__number">{{ formattedValue }}</span>
        <span v-if="suffix" class="stat-card__suffix">{{ suffix }}</span>
      </div>
      <div class="stat-card__change" :class="changeClass">
        <span v-if="change > 0" class="stat-card__arrow">&#9650;</span>
        <span v-else-if="change < 0" class="stat-card__arrow">&#9660;</span>
        <span v-else class="stat-card__arrow">—</span>
        <span v-if="change > 0" class="stat-card__change-text">+{{ change.toFixed(1) }}%</span>
        <span v-else-if="change < 0" class="stat-card__change-text">{{ change.toFixed(1) }}%</span>
        <span v-else class="stat-card__change-text">--</span>
      </div>
    </a-skeleton>
  </a-card>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  title: string;
  value: number;
  change: number;
  prefix?: string;
  suffix?: string;
  loading?: boolean;
}>();

const formattedValue = computed(() => {
  if (props.value === 0 && props.change === 0) return '--';
  return props.value.toLocaleString();
});

const changeClass = computed(() => {
  if (props.change > 0) return 'stat-card__change--up';
  if (props.change < 0) return 'stat-card__change--down';
  return 'stat-card__change--zero';
});
</script>

<style scoped>
.stat-card {
  text-align: center;
}
.stat-card__title {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.45);
  margin-bottom: 8px;
}
.stat-card__value {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
}
.stat-card__prefix,
.stat-card__suffix {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.45);
  margin: 0 4px;
}
.stat-card__change {
  font-size: 14px;
}
.stat-card__change--up {
  color: #52c41a;
}
.stat-card__change--down {
  color: #ff4d4f;
}
.stat-card__change--zero {
  color: rgba(0, 0, 0, 0.25);
}
.stat-card__arrow {
  margin-right: 4px;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add src/views/circle/analytics/components/StatCard.vue
git commit -m "feat(circle): add StatCard component for analytics metrics"
```

---

## Task 7: TrendChart 组件

**Files:**
- Create: `src/views/circle/analytics/components/TrendChart.vue`

- [ ] **Step 1: 实现 TrendChart 组件**

```vue
<!-- src/views/circle/analytics/components/TrendChart.vue -->
<template>
  <a-card :title="title" :bordered="false" class="trend-chart">
    <a-skeleton :loading="loading" :paragraph="{ rows: 4 }" active>
      <template v-if="hasData">
        <div ref="chartRef" class="trend-chart__canvas" />
      </template>
      <a-empty v-else description="暂无数据" />
    </a-skeleton>
  </a-card>
</template>

<script setup lang="ts">
import { computed, toRef } from 'vue';
import { useChart } from '/@/hooks/circle/useChart';
import type { TrendDataPoint } from '/@/api/circle/types';

const props = withDefaults(
  defineProps<{
    title: string;
    data: TrendDataPoint[];
    xField?: string;
    yField?: string;
    color?: string;
    loading?: boolean;
  }>(),
  {
    xField: 'date',
    yField: 'value',
    color: '#1890ff',
    loading: false,
  }
);

const hasData = computed(() => props.data && props.data.length > 0);

const { chartRef } = useChart({
  data: toRef(props, 'data'),
  loading: toRef(props, 'loading'),
  getOption: () => ({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: props.data.map((d) => d.date),
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.3 },
        data: props.data.map((d) => d.value),
        itemStyle: { color: props.color },
      },
    ],
    grid: { left: 50, right: 20, bottom: 30, top: 30 },
  }),
});
</script>

<style scoped>
.trend-chart__canvas {
  width: 100%;
  height: 300px;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add src/views/circle/analytics/components/TrendChart.vue
git commit -m "feat(circle): add TrendChart component with ECharts"
```

---

## Task 8: TimeRangeSelector 组件

**Files:**
- Create: `src/views/circle/analytics/components/TimeRangeSelector.vue`

- [ ] **Step 1: 实现 TimeRangeSelector 组件**

```vue
<!-- src/views/circle/analytics/components/TimeRangeSelector.vue -->
<template>
  <div class="time-range-selector">
    <a-space>
      <a-button
        v-for="preset in presets"
        :key="preset.label"
        :type="isActivePreset(preset) ? 'primary' : 'default'"
        size="small"
        @click="handlePresetClick(preset)"
      >
        {{ preset.label }}
      </a-button>
      <a-range-picker
        :value="datePickerValue"
        :disabled-date="disabledDate"
        format="YYYY-MM-DD"
        @change="handleDateChange"
      />
    </a-space>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import dayjs, { type Dayjs } from 'dayjs';
import type { DateRange } from '/@/api/circle/types';

interface Preset {
  label: string;
  startDate: string;
  endDate: string;
}

const props = withDefaults(
  defineProps<{
    value: DateRange;
    maxRangeDays?: number;
  }>(),
  {
    maxRangeDays: 90,
  }
);

const emit = defineEmits<{
  change: [range: DateRange];
}>();

const presets: Preset[] = [
  {
    label: '近 7 天',
    startDate: dayjs().subtract(6, 'day').format('YYYY-MM-DD'),
    endDate: dayjs().format('YYYY-MM-DD'),
  },
  {
    label: '近 30 天',
    startDate: dayjs().subtract(29, 'day').format('YYYY-MM-DD'),
    endDate: dayjs().format('YYYY-MM-DD'),
  },
];

const activePresetLabel = ref('近 7 天');

const datePickerValue = computed(() => {
  if (!props.value.startDate || !props.value.endDate) return null;
  return [dayjs(props.value.startDate), dayjs(props.value.endDate)] as [Dayjs, Dayjs];
});

const isActivePreset = (preset: Preset) => {
  return preset.startDate === props.value.startDate && preset.endDate === props.value.endDate;
};

const handlePresetClick = (preset: Preset) => {
  activePresetLabel.value = preset.label;
  emit('change', { startDate: preset.startDate, endDate: preset.endDate });
};

const handleDateChange = (dates: [Dayjs, Dayjs] | null) => {
  if (!dates) return;
  activePresetLabel.value = '';
  emit('change', {
    startDate: dates[0].format('YYYY-MM-DD'),
    endDate: dates[1].format('YYYY-MM-DD'),
  });
};

const disabledDate = (current: Dayjs) => {
  if (!props.value.startDate) return false;
  const start = dayjs(props.value.startDate);
  return current.isBefore(start.subtract(props.maxRangeDays, 'day')) || current.isAfter(start.add(props.maxRangeDays, 'day'));
};
</script>
```

- [ ] **Step 2: Commit**

```bash
git add src/views/circle/analytics/components/TimeRangeSelector.vue
git commit -m "feat(circle): add TimeRangeSelector with presets and 90-day limit"
```

---

## Task 9: DataAnalyticsPanel 组件

**Files:**
- Create: `src/views/circle/analytics/components/DataAnalyticsPanel.vue`

- [ ] **Step 1: 实现 DataAnalyticsPanel 组件**

```vue
<!-- src/views/circle/analytics/components/DataAnalyticsPanel.vue -->
<template>
  <div class="data-analytics-panel">
    <!-- 顶部操作区 -->
    <div class="panel-header">
      <div class="panel-header__left">
        <a-button @click="handleBack" size="small">
          <template #icon><arrow-left-outlined /></template>
          返回
        </a-button>
        <span class="panel-header__title">圈子数据统计</span>
      </div>
      <div class="panel-header__right">
        <TimeRangeSelector :value="dateRange" @change="handleDateRangeChange" />
        <a-button
          type="primary"
          size="small"
          :loading="store.exporting"
          :disabled="store.loading || !store.analyticsData"
          @click="handleExport"
        >
          导出数据
        </a-button>
      </div>
    </div>

    <!-- 错误状态 -->
    <a-result
      v-if="store.error"
      status="error"
      title="数据加载失败"
      sub-title="请检查网络连接后重试"
    >
      <template #extra>
        <a-button type="primary" @click="handleRetry">重试</a-button>
      </template>
    </a-result>

    <!-- 正常内容 -->
    <template v-else>
      <!-- 核心指标卡片 -->
      <a-row :gutter="16" class="stat-cards">
        <a-col :xs="24" :sm="12" :lg="6" v-for="card in statCards" :key="card.title">
          <StatCard
            :title="card.title"
            :value="card.value"
            :change="card.change"
            :suffix="card.suffix"
            :loading="store.loading"
          />
        </a-col>
      </a-row>

      <!-- 趋势图表 -->
      <a-row :gutter="16" class="trend-charts">
        <a-col :xs="24" :lg="8" v-for="chart in trendCharts" :key="chart.title">
          <TrendChart
            :title="chart.title"
            :data="chart.data"
            :color="chart.color"
            :loading="store.loading"
          />
        </a-col>
      </a-row>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowLeftOutlined } from '@ant-design/icons-vue';
import { useCircleAnalyticsStore } from '/@/store/modules/circleAnalytics';
import type { DateRange } from '/@/api/circle/types';
import StatCard from './StatCard.vue';
import TrendChart from './TrendChart.vue';
import TimeRangeSelector from './TimeRangeSelector.vue';

const route = useRoute();
const router = useRouter();
const store = useCircleAnalyticsStore();

const circleId = computed(() => route.params.id as string);

const dateRange = computed(() => store.dateRange);

const statCards = computed(() => {
  const d = store.analyticsData;
  if (!d) return [];
  return [
    { title: '成员总数', value: d.memberTotal, change: d.memberChange, suffix: '人' },
    { title: '新增成员', value: d.memberNew, change: d.memberChange, suffix: '人' },
    { title: '发帖总数', value: d.postTotal, change: d.postChange, suffix: '篇' },
    { title: '活跃用户', value: d.activeUserCount, change: d.activeUserChange, suffix: '人' },
  ];
});

const trendCharts = computed(() => {
  const d = store.analyticsData;
  if (!d) return [];
  return [
    { title: '成员增长趋势', data: d.memberTrend, color: '#1890ff' },
    { title: '内容发布趋势', data: d.postTrend, color: '#52c41a' },
    { title: '活跃度趋势', data: d.activeTrend, color: '#faad14' },
  ];
});

const handleBack = () => {
  router.push(`/circle/${circleId.value}`);
};

const handleDateRangeChange = (range: DateRange) => {
  store.fetchAnalytics(circleId.value, range);
};

const handleExport = () => {
  store.exportAnalytics(circleId.value, store.dateRange);
};

const handleRetry = () => {
  store.fetchAnalytics(circleId.value, store.dateRange);
};

onMounted(() => {
  const defaultRange: DateRange = {
    startDate: new Date(Date.now() - 6 * 86400000).toISOString().slice(0, 10),
    endDate: new Date().toISOString().slice(0, 10),
  };
  store.fetchAnalytics(circleId.value, defaultRange);
});
</script>

<style scoped>
.data-analytics-panel {
  padding: 16px;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 12px;
}
.panel-header__left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.panel-header__title {
  font-size: 18px;
  font-weight: 600;
}
.panel-header__right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.stat-cards {
  margin-bottom: 24px;
}
.trend-charts {
  margin-bottom: 24px;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add src/views/circle/analytics/components/DataAnalyticsPanel.vue
git commit -m "feat(circle): add DataAnalyticsPanel with stats, charts, and export"
```

---

## Task 10: 数据统计页主页面与路由

**Files:**
- Create: `src/views/circle/analytics/index.vue`

- [ ] **Step 1: 实现数据统计页**

```vue
<!-- src/views/circle/analytics/index.vue -->
<template>
  <div class="circle-analytics-page">
    <!-- 权限不足状态 -->
    <a-result
      v-if="!hasPermission"
      status="403"
      title="权限不足"
      sub-title="仅创建者和版主可查看数据统计"
    >
      <template #extra>
        <a-button type="primary" @click="handleBack">返回</a-button>
      </template>
    </a-result>

    <!-- 正常内容 -->
    <DataAnalyticsPanel v-else />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '/@/store/modules/user';
import DataAnalyticsPanel from './components/DataAnalyticsPanel.vue';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const hasPermission = ref(true);

const handleBack = () => {
  router.push(`/circle/${route.params.id}`);
};

onMounted(() => {
  // 权限校验：实际应检查当前用户是否为圈子创建者/版主
  // 此处简化，实际需调用后端接口校验
  // TODO: 对接后端权限校验接口
});
</script>

<style scoped>
.circle-analytics-page {
  min-height: 100vh;
  background: #f0f2f5;
}
</style>
```

- [ ] **Step 2: 注册路由（如项目使用动态路由则在后端配置，此处仅标注）**

路由路径: `/circle/:id/analytics`
路由名称: `CircleAnalytics`
权限码: `circle:analytics:view`

- [ ] **Step 3: Commit**

```bash
git add src/views/circle/analytics/
git commit -m "feat(circle): add analytics page with permission guard"
```

---

## Task 11: RankCircleCard 组件

**Files:**
- Create: `src/views/circle/list/components/RankCircleCard.vue`

- [ ] **Step 1: 实现 RankCircleCard 组件**

```vue
<!-- src/views/circle/list/components/RankCircleCard.vue -->
<template>
  <div class="rank-circle-card" @click="handleClick">
    <div class="rank-circle-card__rank">
      <span v-if="rankType === 'hot'" :class="rankClass">{{ rank }}</span>
      <span v-else class="rank-circle-card__time">{{ formattedTime }}</span>
    </div>
    <div class="rank-circle-card__content">
      <a-avatar :src="iconUrl" :size="40" />
      <div class="rank-circle-card__info">
        <div class="rank-circle-card__name">{{ name }}</div>
        <div class="rank-circle-card__meta">
          <span>{{ memberCount }} 成员</span>
          <a-tag v-if="category" size="small">{{ category }}</a-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import dayjs from 'dayjs';

const props = defineProps<{
  id: string;
  rank: number;
  rankType: 'hot' | 'new';
  name: string;
  iconUrl: string;
  memberCount: number;
  category: string;
  createTime?: string;
  source: string;
}>();

const router = useRouter();

const rankClass = computed(() => {
  if (props.rank === 1) return 'rank-circle-card__rank--gold';
  if (props.rank === 2) return 'rank-circle-card__rank--silver';
  if (props.rank === 3) return 'rank-circle-card__rank--bronze';
  return '';
});

const formattedTime = computed(() => {
  if (!props.createTime) return '';
  return dayjs(props.createTime).format('YYYY-MM-DD');
});

const handleClick = () => {
  router.push({ path: `/circle/${props.id}`, query: { source: props.source } });
};
</script>

<style scoped>
.rank-circle-card {
  display: flex;
  align-items: center;
  padding: 12px;
  cursor: pointer;
  transition: background 0.2s;
}
.rank-circle-card:hover {
  background: #fafafa;
}
.rank-circle-card__rank {
  width: 40px;
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  margin-right: 12px;
}
.rank-circle-card__rank--gold {
  color: #faad14;
}
.rank-circle-card__rank--silver {
  color: #8c8c8c;
}
.rank-circle-card__rank--bronze {
  color: #d48806;
}
.rank-circle-card__time {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}
.rank-circle-card__content {
  display: flex;
  align-items: center;
  flex: 1;
}
.rank-circle-card__info {
  margin-left: 12px;
  flex: 1;
}
.rank-circle-card__name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}
.rank-circle-card__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add src/views/circle/list/components/RankCircleCard.vue
git commit -m "feat(circle): add RankCircleCard with gold/silver/bronze ranking"
```

---

## Task 12: RankList 组件

**Files:**
- Create: `src/views/circle/list/components/RankList.vue`

- [ ] **Step 1: 实现 RankList 组件**

```vue
<!-- src/views/circle/list/components/RankList.vue -->
<template>
  <div class="rank-list">
    <a-spin :spinning="loading">
      <template v-if="data.length > 0">
        <RankCircleCard
          v-for="(item, index) in data"
          :key="item.id"
          :id="item.id"
          :rank="index + 1"
          :rank-type="type"
          :name="item.name"
          :icon-url="item.iconUrl"
          :member-count="item.memberCount"
          :category="item.category"
          :create-time="item.createTime"
          :source="type === 'hot' ? 'hot_rank' : 'new_rank'"
        />
      </template>
      <a-empty v-else description="暂无符合条件的圈子" />
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import type { CircleRankVO } from '/@/api/circle/types';
import RankCircleCard from './RankCircleCard.vue';

defineProps<{
  type: 'hot' | 'new';
  data: CircleRankVO[];
  loading: boolean;
}>();
</script>

<style scoped>
.rank-list {
  padding: 8px 0;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add src/views/circle/list/components/RankList.vue
git commit -m "feat(circle): add RankList component for hot/new rankings"
```

---

## Task 13: RecommendationPanel 组件

**Files:**
- Create: `src/views/circle/list/components/RecommendationPanel.vue`

- [ ] **Step 1: 实现 RecommendationPanel 组件**

```vue
<!-- src/views/circle/list/components/RecommendationPanel.vue -->
<template>
  <div class="recommendation-panel">
    <!-- 降级提示 -->
    <a-alert
      v-if="store.fallbackMode"
      message="暂无推荐，为您展示热门圈子"
      type="info"
      show-icon
      closable
      style="margin-bottom: 16px"
    />

    <a-spin :spinning="store.loading.recommend">
      <template v-if="displayList.length > 0">
        <a-row :gutter="[16, 16]">
          <a-col
            v-for="item in displayList"
            :key="item.id"
            :xs="24"
            :sm="12"
            :lg="8"
          >
            <a-card hoverable class="recommend-card" @click="handleCardClick(item)">
              <template #cover>
                <div class="recommend-card__cover">
                  <a-avatar :src="item.iconUrl" :size="64" />
                </div>
              </template>
              <a-card-meta :title="item.name">
                <template #description>
                  <div class="recommend-card__desc">{{ item.description }}</div>
                  <div class="recommend-card__meta">
                    <span>{{ item.memberCount }} 成员</span>
                    <a-tag v-if="item.category" size="small">{{ item.category }}</a-tag>
                    <a-tag v-if="item.privacyType === 'PRIVATE'" size="small" color="orange">私有</a-tag>
                    <a-tag v-if="item.privacyType === 'PASSWORD'" size="small" color="blue">密码</a-tag>
                  </div>
                </template>
              </a-card-meta>
              <div class="recommend-card__action">
                <a-button
                  type="primary"
                  size="small"
                  @click.stop="handleJoin(item)"
                >
                  {{ item.privacyType === 'PRIVATE' ? '申请加入' : '加入' }}
                </a-button>
              </div>
            </a-card>
          </a-col>
        </a-row>
      </template>
      <a-empty v-else-if="!store.loading.recommend" description="暂无推荐圈子" />
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useCircleRecommendStore } from '/@/store/modules/circleRecommend';
import type { CircleRecommendVO } from '/@/api/circle/types';

const store = useCircleRecommendStore();
const router = useRouter();

const displayList = computed(() => {
  if (store.fallbackMode) {
    return store.hotRankList.map((item) => ({
      ...item,
      description: '',
      privacyType: 'PUBLIC' as const,
      recommendSource: 'hot_rank',
    }));
  }
  return store.recommendList;
});

const handleCardClick = (item: CircleRecommendVO) => {
  router.push({ path: `/circle/${item.id}`, query: { source: 'recommend' } });
};

const handleJoin = (item: CircleRecommendVO) => {
  // 复用 EPIC-10 的加入流程
  // TODO: 对接 JoinModal
  router.push({ path: `/circle/${item.id}`, query: { source: 'recommend' } });
};
</script>

<style scoped>
.recommend-card {
  height: 100%;
}
.recommend-card__cover {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px 0;
  background: #fafafa;
}
.recommend-card__desc {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 8px;
}
.recommend-card__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}
.recommend-card__action {
  margin-top: 12px;
  text-align: right;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add src/views/circle/list/components/RecommendationPanel.vue
git commit -m "feat(circle): add RecommendationPanel with fallback to hot ranking"
```

---

## Task 14: useRecommendTracking composable

**Files:**
- Create: `src/hooks/circle/useRecommendTracking.ts`
- Test: `src/__tests__/circle/recommendTracking.test.ts`

- [ ] **Step 1: 编写曝光上报测试**

```typescript
// src/__tests__/circle/recommendTracking.test.ts
import { useRecommendTracking } from '/@/hooks/circle/useRecommendTracking';

jest.mock('/@/api/circle/recommend', () => ({
  reportRecommendExposure: jest.fn().mockResolvedValue(undefined),
  reportRecommendClick: jest.fn().mockResolvedValue(undefined),
}));

import { reportRecommendExposure, reportRecommendClick } from '/@/api/circle/recommend';

// Mock IntersectionObserver
const mockObserve = jest.fn();
const mockDisconnect = jest.fn();
const mockUnobserve = jest.fn();
let observerCallback: IntersectionObserverCallback;

(window as any).IntersectionObserver = class {
  constructor(callback: IntersectionObserverCallback) {
    observerCallback = callback;
  }
  observe = mockObserve;
  disconnect = mockDisconnect;
  unobserve = mockUnobserve;
};

// Mock sendBeacon
navigator.sendBeacon = jest.fn();

describe('useRecommendTracking', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    mockObserve.mockClear();
    mockDisconnect.mockClear();
    mockUnobserve.mockClear();
    (reportRecommendExposure as jest.Mock).mockClear();
    (reportRecommendClick as jest.Mock).mockClear();
    (navigator.sendBeacon as jest.Mock).mockClear();
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('should create observer and observe elements', () => {
    const { observeCard } = useRecommendTracking();
    const el = document.createElement('div');
    observeCard(el, 'circle-1');
    expect(mockObserve).toHaveBeenCalledWith(el);
  });

  it('should report exposure when card enters viewport', () => {
    const { observeCard } = useRecommendTracking();
    const el = document.createElement('div');
    observeCard(el, 'circle-1');

    // Simulate intersection
    observerCallback(
      [{ isIntersecting: true, target: el } as IntersectionObserverEntry],
      {} as IntersectionObserver
    );

    jest.advanceTimersByTime(500);

    expect(reportRecommendExposure).toHaveBeenCalledWith({
      circleIds: ['circle-1'],
      source: 'recommend',
    });
  });

  it('should deduplicate exposure for same circle', () => {
    const { observeCard } = useRecommendTracking();
    const el = document.createElement('div');
    observeCard(el, 'circle-1');

    // Enter viewport
    observerCallback(
      [{ isIntersecting: true, target: el } as IntersectionObserverEntry],
      {} as IntersectionObserver
    );
    jest.advanceTimersByTime(500);

    // Enter again
    observerCallback(
      [{ isIntersecting: true, target: el } as IntersectionObserverEntry],
      {} as IntersectionObserver
    );
    jest.advanceTimersByTime(500);

    expect(reportRecommendExposure).toHaveBeenCalledTimes(1);
  });

  it('should report click event', async () => {
    const { reportClick } = useRecommendTracking();
    await reportClick('circle-1', 'recommend');
    expect(reportRecommendClick).toHaveBeenCalledWith({
      circleId: 'circle-1',
      source: 'recommend',
    });
  });

  it('should flush on page hide using sendBeacon', () => {
    const { observeCard, cleanup } = useRecommendTracking();
    const el = document.createElement('div');
    observeCard(el, 'circle-1');

    // Enter viewport but don't wait for debounce
    observerCallback(
      [{ isIntersecting: true, target: el } as IntersectionObserverEntry],
      {} as IntersectionObserver
    );

    // Simulate page hide
    Object.defineProperty(document, 'visibilityState', { value: 'hidden', writable: true });
    document.dispatchEvent(new Event('visibilitychange'));

    expect(navigator.sendBeacon).toHaveBeenCalled();
    cleanup();
  });

  it('should handle exposure report failure silently', () => {
    (reportRecommendExposure as jest.Mock).mockRejectedValue(new Error('Network'));
    const consoleSpy = jest.spyOn(console, 'log').mockImplementation();

    const { observeCard } = useRecommendTracking();
    const el = document.createElement('div');
    observeCard(el, 'circle-1');

    observerCallback(
      [{ isIntersecting: true, target: el } as IntersectionObserverEntry],
      {} as IntersectionObserver
    );

    jest.advanceTimersByTime(500);

    // Should not throw
    expect(reportRecommendExposure).toHaveBeenCalled();
    consoleSpy.mockRestore();
  });
});
```

- [ ] **Step 2: 运行测试验证失败**

Run: `npx jest src/__tests__/circle/recommendTracking.test.ts --no-cache 2>&1 | tail -20`
Expected: FAIL — module not found

- [ ] **Step 3: 实现 useRecommendTracking composable**

```typescript
// src/hooks/circle/useRecommendTracking.ts
import { onBeforeUnmount } from 'vue';
import { reportRecommendExposure, reportRecommendClick } from '/@/api/circle/recommend';

export function useRecommendTracking(source = 'recommend') {
  const reportedIds = new Set<string>();
  const pendingIds = new Set<string>();
  let debounceTimer: ReturnType<typeof setTimeout> | null = null;

  // IntersectionObserver 回调
  const handleIntersection: IntersectionObserverCallback = (entries) => {
    for (const entry of entries) {
      if (entry.isIntersecting) {
        const circleId = (entry.target as HTMLElement).dataset.circleId;
        if (circleId && !reportedIds.has(circleId)) {
          pendingIds.add(circleId);
          scheduleFlush();
        }
      }
    }
  };

  const observer = new IntersectionObserver(handleIntersection, { threshold: 0.5 });

  const scheduleFlush = () => {
    if (debounceTimer) clearTimeout(debounceTimer);
    debounceTimer = setTimeout(flush, 500);
  };

  const flush = async () => {
    if (pendingIds.size === 0) return;
    const ids = Array.from(pendingIds);
    ids.forEach((id) => reportedIds.add(id));
    pendingIds.clear();
    try {
      await reportRecommendExposure({ circleIds: ids, source });
    } catch {
      // 静默处理，仅开发环境日志
      if (import.meta.env.DEV) {
        console.log('[RecommendTracking] exposure report failed:', ids);
      }
    }
  };

  const flushWithBeacon = () => {
    if (pendingIds.size === 0) return;
    const ids = Array.from(pendingIds);
    ids.forEach((id) => reportedIds.add(id));
    pendingIds.clear();
    const data = JSON.stringify({ circleIds: ids, source });
    navigator.sendBeacon('/content/circle/recommend/exposure', data);
  };

  const observeCard = (el: HTMLElement, circleId: string) => {
    el.dataset.circleId = circleId;
    observer.observe(el);
  };

  const reportClick = async (circleId: string, clickSource: string) => {
    try {
      await reportRecommendClick({ circleId, source: clickSource });
    } catch {
      if (import.meta.env.DEV) {
        console.log('[RecommendTracking] click report failed:', circleId);
      }
    }
  };

  // 页面离开保底
  const handleVisibilityChange = () => {
    if (document.visibilityState === 'hidden') {
      flushWithBeacon();
    }
  };

  const handleBeforeUnload = () => {
    flushWithBeacon();
  };

  document.addEventListener('visibilitychange', handleVisibilityChange);
  window.addEventListener('beforeunload', handleBeforeUnload);

  const cleanup = () => {
    observer.disconnect();
    if (debounceTimer) clearTimeout(debounceTimer);
    document.removeEventListener('visibilitychange', handleVisibilityChange);
    window.removeEventListener('beforeunload', handleBeforeUnload);
  };

  onBeforeUnmount(cleanup);

  return {
    observeCard,
    reportClick,
    cleanup,
  };
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `npx jest src/__tests__/circle/recommendTracking.test.ts --no-cache 2>&1 | tail -20`
Expected: PASS — all 6 tests pass

- [ ] **Step 5: Commit**

```bash
git add src/hooks/circle/useRecommendTracking.ts src/__tests__/circle/recommendTracking.test.ts
git commit -m "feat(circle): add useRecommendTracking with IntersectionObserver and sendBeacon"
```

---

## Task 15: 圈子列表页增强（Tab 切换）

**Files:**
- Modify: `src/views/circle/list/index.vue` (已有则修改，否则新建)

- [ ] **Step 1: 实现圈子列表页 Tab 切换**

在现有圈子列表页基础上增加推荐/热门榜/新锐榜 Tab 切换：

```vue
<!-- src/views/circle/list/index.vue -->
<template>
  <div class="circle-list-page">
    <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
      <!-- 推荐 Tab（仅已登录用户可见） -->
      <a-tab-pane v-if="isLoggedIn" key="recommend" tab="推荐">
        <RecommendationPanel />
      </a-tab-pane>

      <!-- 热门榜 Tab -->
      <a-tab-pane key="hot" tab="热门榜">
        <RankList type="hot" :data="store.hotRankList" :loading="store.loading.hot" />
      </a-tab-pane>

      <!-- 新锐榜 Tab -->
      <a-tab-pane key="new" tab="新锐榜">
        <RankList type="new" :data="store.newRankList" :loading="store.loading.new" />
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useUserStore } from '/@/store/modules/user';
import { useCircleRecommendStore } from '/@/store/modules/circleRecommend';
import RecommendationPanel from './components/RecommendationPanel.vue';
import RankList from './components/RankList.vue';

const userStore = useUserStore();
const store = useCircleRecommendStore();

const isLoggedIn = computed(() => !!userStore.getToken);
const activeTab = ref(isLoggedIn.value ? 'recommend' : 'hot');

const handleTabChange = (key: string) => {
  store.setActiveTab(key as any);
  if (key === 'recommend') {
    if (store.fallbackMode) {
      store.refreshRecommend();
    } else {
      store.fetchRecommendList();
    }
  } else if (key === 'hot') {
    store.fetchHotRankList();
  } else if (key === 'new') {
    store.fetchNewRankList();
  }
};

onMounted(() => {
  handleTabChange(activeTab.value);
});
</script>

<style scoped>
.circle-list-page {
  padding: 16px;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add src/views/circle/list/
git commit -m "feat(circle): enhance circle list page with recommend/hot/new tabs"
```

---

## Task 16: 测试补充与集成测试

**Files:**
- Update: `src/__tests__/circle/analytics.test.ts`
- Update: `src/__tests__/circle/recommendation.test.ts`
- Create: `src/__tests__/circle/ranking.test.ts`

- [ ] **Step 1: 补充 ranking 测试**

```typescript
// src/__tests__/circle/ranking.test.ts
import { setActivePinia, createPinia } from 'pinia';
import { useCircleRecommendStore } from '/@/store/modules/circleRecommend';

jest.mock('/@/api/circle/recommend', () => ({
  getRecommendList: jest.fn(),
  reportRecommendExposure: jest.fn(),
  reportRecommendClick: jest.fn(),
}));
jest.mock('/@/api/circle/ranking', () => ({
  getHotRankList: jest.fn(),
  getNewRankList: jest.fn(),
}));

import { getHotRankList, getNewRankList } from '/@/api/circle/ranking';

describe('Ranking', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('should fetch hot rank list', async () => {
    const mockList = [
      { id: '1', name: 'Circle 1', memberCount: 100 },
      { id: '2', name: 'Circle 2', memberCount: 50 },
    ];
    (getHotRankList as jest.Mock).mockResolvedValue(mockList);

    const store = useCircleRecommendStore();
    await store.fetchHotRankList();

    expect(store.hotRankList).toEqual(mockList);
    expect(store.loaded.hot).toBe(true);
  });

  it('should fetch new rank list', async () => {
    const mockList = [{ id: '1', name: 'New Circle', createTime: '2024-01-01' }];
    (getNewRankList as jest.Mock).mockResolvedValue(mockList);

    const store = useCircleRecommendStore();
    await store.fetchNewRankList();

    expect(store.newRankList).toEqual(mockList);
    expect(store.loaded.new).toBe(true);
  });

  it('should not re-fetch if already loaded', async () => {
    (getHotRankList as jest.Mock).mockResolvedValue([{ id: '1' }]);

    const store = useCircleRecommendStore();
    await store.fetchHotRankList();
    await store.fetchHotRankList();

    expect(getHotRankList).toHaveBeenCalledTimes(1);
  });
});
```

- [ ] **Step 2: 运行全量测试**

Run: `npx jest src/__tests__/circle/ --no-cache 2>&1 | tail -30`
Expected: ALL PASS

- [ ] **Step 3: Commit**

```bash
git add src/__tests__/circle/
git commit -m "test(circle): add ranking tests and verify all circle tests pass"
```

---

## Task 17: 最终验证

- [ ] **Step 1: 运行全量测试确保 100% 通过**

Run: `npx jest src/__tests__/circle/ --no-cache --verbose 2>&1`
Expected: All tests PASS

- [ ] **Step 2: 验证文件结构完整性**

Run: `find src/api/circle src/store/modules/circle* src/hooks/circle src/views/circle -type f 2>/dev/null | sort`
Expected: 所有设计文档中的文件均已创建

- [ ] **Step 3: Code Review 检查清单**

- [ ] 命名一致性：类型、API、Store、组件名称与设计文档一致
- [ ] 错误处理：API 调用均有 try/catch，上报失败静默处理
- [ ] 响应式布局：使用 Ant Design Vue 的 Row/Col 栅格系统
- [ ] 权限控制：数据统计页有 403 权限校验
- [ ] 缓存策略：统计数据 5 分钟过期，推荐/榜单会话级缓存
- [ ] 曝光去重：Set<string> 记录已上报 ID
- [ ] 页面离开保底：visibilitychange + sendBeacon

- [ ] **Step 4: Final Commit**

```bash
git add -A
git commit -m "feat(circle): complete analytics and discovery frontend implementation"
```
