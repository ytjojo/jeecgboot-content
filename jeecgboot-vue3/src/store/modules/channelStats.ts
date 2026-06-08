import { defineStore } from 'pinia';
import { store } from '/@/store';
import {
  getCoreStats,
  getTrendData,
  getHotContent,
  getUserAnalysis,
  getInteraction,
} from '/@/api/content/channel/stats';
import type {
  CoreStatsVO,
  TrendVO,
  HotContentVO,
  UserAnalysisVO,
  InteractionVO,
  TrendQuery,
  HotContentQuery,
} from '/@/api/content/channel/stats';

interface ChannelStatsState {
  channelId: string;
  coreStats: CoreStatsVO | null;
  trendData: TrendVO[];
  hotContent: HotContentVO[];
  userAnalysis: UserAnalysisVO | null;
  interaction: InteractionVO | null;
  timeRange: TrendQuery['range'];
  customDateRange: [string, string] | null;
  hotPeriod: HotContentQuery['period'];
  coreLoading: boolean;
  trendLoading: boolean;
  hotContentLoading: boolean;
  userAnalysisLoading: boolean;
  interactionLoading: boolean;
  coreError: string | null;
  trendError: string | null;
  hotContentError: string | null;
  userAnalysisError: string | null;
  interactionError: string | null;
}

/** 消除 5 个 fetch 方法的重复模板代码 */
type DataKey = 'core' | 'trend' | 'hotContent' | 'userAnalysis' | 'interaction';

async function guardedFetch<T>(
  ctx: {
    set: (key: DataKey, loading: boolean, error: string | null, data?: T) => void;
    key: DataKey;
  },
  fn: () => Promise<T>,
) {
  ctx.set(ctx.key, true, null);
  try {
    const data = await fn();
    ctx.set(ctx.key, false, null, data);
  } catch (e: any) {
    ctx.set(ctx.key, false, e?.message || '加载失败');
  }
}

function setter(state: ChannelStatsState) {
  return (key: DataKey, loading: boolean, error: string | null, data?: any) => {
    switch (key) {
      case 'core':
        state.coreLoading = loading;
        state.coreError = error;
        if (data !== undefined) state.coreStats = data;
        break;
      case 'trend':
        state.trendLoading = loading;
        state.trendError = error;
        if (data !== undefined) state.trendData = data;
        break;
      case 'hotContent':
        state.hotContentLoading = loading;
        state.hotContentError = error;
        if (data !== undefined) state.hotContent = data;
        break;
      case 'userAnalysis':
        state.userAnalysisLoading = loading;
        state.userAnalysisError = error;
        if (data !== undefined) state.userAnalysis = data;
        break;
      case 'interaction':
        state.interactionLoading = loading;
        state.interactionError = error;
        if (data !== undefined) state.interaction = data;
        break;
    }
  };
}

export const useChannelStatsStore = defineStore({
  id: 'app-channel-stats',
  state: (): ChannelStatsState => ({
    channelId: '',
    coreStats: null,
    trendData: [],
    hotContent: [],
    userAnalysis: null,
    interaction: null,
    timeRange: 'day',
    customDateRange: null,
    hotPeriod: '7d',
    coreLoading: false,
    trendLoading: false,
    hotContentLoading: false,
    userAnalysisLoading: false,
    interactionLoading: false,
    coreError: null,
    trendError: null,
    hotContentError: null,
    userAnalysisError: null,
    interactionError: null,
  }),
  actions: {
    setChannelId(channelId: string) {
      this.channelId = channelId;
    },
    setTimeRange(range: TrendQuery['range']) {
      this.timeRange = range;
    },
    setCustomDateRange(range: [string, string] | null) {
      this.customDateRange = range;
    },
    setHotPeriod(period: HotContentQuery['period']) {
      this.hotPeriod = period;
    },
    async fetchCoreStats() {
      const set = setter(this);
      await guardedFetch({ set, key: 'core' }, () =>
        getCoreStats({ channelId: this.channelId }),
      );
    },
    async fetchTrendData() {
      const set = setter(this);
      await guardedFetch({ set, key: 'trend' }, () =>
        getTrendData({
          channelId: this.channelId,
          range: this.timeRange,
          ...(this.timeRange === 'custom' && this.customDateRange
            ? { startDate: this.customDateRange[0], endDate: this.customDateRange[1] }
            : {}),
        }),
      );
    },
    async fetchHotContent() {
      const set = setter(this);
      await guardedFetch({ set, key: 'hotContent' }, () =>
        getHotContent({
          channelId: this.channelId,
          period: this.hotPeriod,
        }),
      );
    },
    async fetchUserAnalysis() {
      const set = setter(this);
      await guardedFetch({ set, key: 'userAnalysis' }, () =>
        getUserAnalysis({ channelId: this.channelId }),
      );
    },
    async fetchInteraction() {
      const set = setter(this);
      await guardedFetch({ set, key: 'interaction' }, () =>
        getInteraction({ channelId: this.channelId }),
      );
    },
    /** 并行加载所有看板数据 */
    async fetchAllData() {
      await Promise.allSettled([
        this.fetchCoreStats(),
        this.fetchTrendData(),
        this.fetchHotContent(),
        this.fetchUserAnalysis(),
        this.fetchInteraction(),
      ]);
    },
  },
});

export function useChannelStatsStoreWithOut() {
  return useChannelStatsStore(store);
}
