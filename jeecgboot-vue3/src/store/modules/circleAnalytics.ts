import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { store } from '/@/store';
import { getCircleStatistics, exportCircleStatisticsCsv } from '/@/api/content/circle/analytics';
import type { CircleDataStatisticsVO, DateRange } from '/@/api/content/model/circleAnalyticsModel';

export const useCircleAnalyticsStore = defineStore('circle-analytics', () => {
  const analyticsData = ref<CircleDataStatisticsVO | null>(null);
  const dateRange = ref<DateRange | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);
  const exporting = ref(false);

  const memberChange = computed<number | null>(() => {
    const trends = analyticsData.value?.dailyTrends;
    if (!trends || trends.length < 2) return null;
    const mid = Math.floor(trends.length / 2);
    const prev = trends.slice(0, mid).reduce((sum, t) => sum + t.newMemberCount, 0);
    const curr = trends.slice(mid).reduce((sum, t) => sum + t.newMemberCount, 0);
    if (prev === 0) return curr > 0 ? 100 : 0;
    return ((curr - prev) / prev) * 100;
  });

  const postChange = computed<number | null>(() => {
    const trends = analyticsData.value?.dailyTrends;
    if (!trends || trends.length < 2) return null;
    const mid = Math.floor(trends.length / 2);
    const prev = trends.slice(0, mid).reduce((sum, t) => sum + t.newPostCount, 0);
    const curr = trends.slice(mid).reduce((sum, t) => sum + t.newPostCount, 0);
    if (prev === 0) return curr > 0 ? 100 : 0;
    return ((curr - prev) / prev) * 100;
  });

  const activeChange = computed<number | null>(() => {
    const trends = analyticsData.value?.dailyTrends;
    if (!trends || trends.length < 2) return null;
    const mid = Math.floor(trends.length / 2);
    const prev = trends.slice(0, mid).reduce((sum, t) => sum + t.activeCount, 0);
    const curr = trends.slice(mid).reduce((sum, t) => sum + t.activeCount, 0);
    if (prev === 0) return curr > 0 ? 100 : 0;
    return ((curr - prev) / prev) * 100;
  });

  function setDateRange(range: DateRange | null) {
    dateRange.value = range;
  }

  function clearData() {
    analyticsData.value = null;
    error.value = null;
  }

  function isNoDataError(e: any): boolean {
    const code = e?.code ?? e?.response?.data?.code;
    if (code === 404002) return true;
    const msg = (e?.message ?? e?.response?.data?.message ?? '') as string;
    return msg.includes('暂无数据') || msg.includes('没有数据');
  }

  async function fetchAnalytics(circleId: string, params: DateRange) {
    loading.value = true;
    error.value = null;
    dateRange.value = params;
    try {
      const data = await getCircleStatistics(circleId, params);
      analyticsData.value = data;
    } catch (e: any) {
      analyticsData.value = null;
      if (isNoDataError(e)) {
        error.value = null;
      } else {
        error.value = e?.message || '加载失败';
      }
    } finally {
      loading.value = false;
    }
  }

  async function exportAnalytics(circleId: string, params: DateRange, filename?: string) {
    exporting.value = true;
    try {
      const blob = await exportCircleStatisticsCsv(circleId, params);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = filename || `circle-statistics-${circleId}-${params.startDate}-${params.endDate}.csv`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch (e: any) {
      error.value = e?.message || '导出失败';
      throw e;
    } finally {
      exporting.value = false;
    }
  }

  return {
    analyticsData,
    dateRange,
    loading,
    error,
    exporting,
    memberChange,
    postChange,
    activeChange,
    setDateRange,
    clearData,
    fetchAnalytics,
    exportAnalytics,
  };
});

export function useCircleAnalyticsStoreWithOut() {
  return useCircleAnalyticsStore(store);
}
