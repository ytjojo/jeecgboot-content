import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { store } from '/@/store';
import {
  getDiscoveryHome,
  getRecommendationList,
  getColdStartRecommendations,
  getHotRanking,
  getNewRanking,
  getSystemRanking,
  getEditorialPickList,
  markNotInterested,
} from '/@/api/content/channelDiscovery';
import type {
  DiscoveryHomeVO,
  ChannelRecommendationVO,
  ChannelRankingItemVO,
  ChannelEditorialPickVO,
  CategoryTreeVO,
} from '/@/api/content/model/channelDiscoveryModel';

/** 缓存 TTL：5 分钟 */
const CACHE_TTL = 5 * 60 * 1000;

export const useChannelDiscoveryStore = defineStore('channelDiscovery', () => {
  // ===== State =====
  const recommendations = ref<ChannelRecommendationVO[]>([]);
  const hotRanking = ref<ChannelRankingItemVO[]>([]);
  const newRanking = ref<ChannelRankingItemVO[]>([]);
  const systemRanking = ref<ChannelRankingItemVO[]>([]);
  const editorialPicks = ref<ChannelEditorialPickVO[]>([]);
  const categories = ref<CategoryTreeVO[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);
  const lastFetchTime = ref<number>(0);

  // ===== Getters =====
  const isCacheValid = computed(() => {
    return Date.now() - lastFetchTime.value < CACHE_TTL;
  });

  const hasData = computed(() => {
    return recommendations.value.length > 0 || hotRanking.value.length > 0;
  });

  // ===== Actions =====

  /** 加载发现页聚合数据 */
  async function fetchDiscoveryHome(forceRefresh = false) {
    if (!forceRefresh && isCacheValid.value && hasData.value) {
      return;
    }

    loading.value = true;
    error.value = null;

    try {
      const data = await getDiscoveryHome();
      applyDiscoveryData(data);
      lastFetchTime.value = Date.now();
    } catch (e) {
      // 聚合接口失败，降级为并行调用独立接口
      console.warn('[DiscoveryStore] 聚合接口失败，降级为独立接口', e);
      await fallbackFetch();
    } finally {
      loading.value = false;
    }
  }

  /** 降级：并行调用独立接口 */
  async function fallbackFetch() {
    try {
      const results = await Promise.allSettled([
        getRecommendationList({ page: 1, pageSize: 20 }),
        getHotRanking({ page: 1, pageSize: 5 }),
        getNewRanking({ page: 1, pageSize: 5 }),
        getSystemRanking({ page: 1, pageSize: 5 }),
        getEditorialPickList(),
      ]);

      if (results[0].status === 'fulfilled') {
        recommendations.value = results[0].value.records || [];
      }
      if (results[1].status === 'fulfilled') {
        hotRanking.value = results[1].value.records || [];
      }
      if (results[2].status === 'fulfilled') {
        newRanking.value = results[2].value.records || [];
      }
      if (results[3].status === 'fulfilled') {
        systemRanking.value = results[3].value.records || [];
      }
      if (results[4].status === 'fulfilled') {
        editorialPicks.value = results[4].value || [];
      }

      lastFetchTime.value = Date.now();
    } catch (e) {
      error.value = '加载失败，请重试';
    }
  }

  /** 冷启动推荐（未登录用户） */
  async function fetchColdStart() {
    loading.value = true;
    error.value = null;

    try {
      const data = await getColdStartRecommendations();
      recommendations.value = data || [];
      lastFetchTime.value = Date.now();
    } catch (e) {
      error.value = '加载失败，请重试';
    } finally {
      loading.value = false;
    }
  }

  /** 应用聚合数据 */
  function applyDiscoveryData(data: DiscoveryHomeVO) {
    recommendations.value = data.recommendations || [];
    hotRanking.value = data.hotRanking || [];
    newRanking.value = data.newRanking || [];
    systemRanking.value = data.systemRanking || [];
    editorialPicks.value = data.editorialPicks || [];
    categories.value = data.categories || [];
  }

  /** 不感兴趣反馈 */
  async function feedbackNotInterested(channelId: string, reason?: string) {
    try {
      await markNotInterested({ channelId, reason });
      // 从推荐列表移除
      recommendations.value = recommendations.value.filter((c) => c.id !== channelId);
    } catch (e) {
      console.warn('[DiscoveryStore] 不感兴趣反馈失败', e);
    }
  }

  /** 标记缓存失效 */
  function invalidateCache() {
    lastFetchTime.value = 0;
  }

  /** 刷新推荐列表 */
  async function refreshRecommendations() {
    try {
      const result = await getRecommendationList({ page: 1, pageSize: 20 });
      recommendations.value = result.records || [];
      // 刷新最后拉取时间（但不影响缓存有效性判断）
    } catch (e) {
      console.warn('[DiscoveryStore] 刷新推荐失败', e);
    }
  }

  return {
    // State
    recommendations,
    hotRanking,
    newRanking,
    systemRanking,
    editorialPicks,
    categories,
    loading,
    error,
    lastFetchTime,
    // Getters
    isCacheValid,
    hasData,
    // Actions
    fetchDiscoveryHome,
    fallbackFetch,
    fetchColdStart,
    applyDiscoveryData,
    feedbackNotInterested,
    invalidateCache,
    refreshRecommendations,
  };
});

// Support use outside of setup
export function useChannelDiscoveryStoreWithOut() {
  return useChannelDiscoveryStore(store);
}
