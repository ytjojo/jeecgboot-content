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
  // 独立 loading 状态
  coreLoading: boolean;
  trendLoading: boolean;
  hotContentLoading: boolean;
  userAnalysisLoading: boolean;
  interactionLoading: boolean;
  // 独立 error 状态
  coreError: string | null;
  trendError: string | null;
  hotContentError: string | null;
  userAnalysisError: string | null;
  interactionError: string | null;
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
      this.coreLoading = true;
      this.coreError = null;
      try {
        this.coreStats = await getCoreStats({ channelId: this.channelId });
      } catch (e: any) {
        this.coreError = e?.message || '加载核心指标失败';
      } finally {
        this.coreLoading = false;
      }
    },
    async fetchTrendData() {
      this.trendLoading = true;
      this.trendError = null;
      try {
        this.trendData = await getTrendData({
          channelId: this.channelId,
          range: this.timeRange,
          ...(this.timeRange === 'custom' && this.customDateRange
            ? { startDate: this.customDateRange[0], endDate: this.customDateRange[1] }
            : {}),
        });
      } catch (e: any) {
        this.trendError = e?.message || '加载趋势数据失败';
      } finally {
        this.trendLoading = false;
      }
    },
    async fetchHotContent() {
      this.hotContentLoading = true;
      this.hotContentError = null;
      try {
        this.hotContent = await getHotContent({
          channelId: this.channelId,
          period: this.hotPeriod,
        });
      } catch (e: any) {
        this.hotContentError = e?.message || '加载热门内容失败';
      } finally {
        this.hotContentLoading = false;
      }
    },
    async fetchUserAnalysis() {
      this.userAnalysisLoading = true;
      this.userAnalysisError = null;
      try {
        this.userAnalysis = await getUserAnalysis({ channelId: this.channelId });
      } catch (e: any) {
        this.userAnalysisError = e?.message || '加载用户分析失败';
      } finally {
        this.userAnalysisLoading = false;
      }
    },
    async fetchInteraction() {
      this.interactionLoading = true;
      this.interactionError = null;
      try {
        this.interaction = await getInteraction({ channelId: this.channelId });
      } catch (e: any) {
        this.interactionError = e?.message || '加载互动数据失败';
      } finally {
        this.interactionLoading = false;
      }
    },
    /** 并行加载所有看板数据 */
    async fetchAllData() {
      const results = await Promise.allSettled([
        this.fetchCoreStats(),
        this.fetchTrendData(),
        this.fetchHotContent(),
        this.fetchUserAnalysis(),
        this.fetchInteraction(),
      ]);
      return results;
    },
  },
});

export function useChannelStatsStoreWithOut() {
  return useChannelStatsStore(store);
}
