import { defineStore } from 'pinia';
import { ref, reactive } from 'vue';
import { store } from '/@/store';
import { getRecommendList, reportRecommendExposure, reportRecommendClick } from '/@/api/content/circle/recommend';
import { getHotRankList, getNewRankList } from '/@/api/content/circle/ranking';
import type { CircleRecommendItem, CircleRankingItem, RecommendExposureReq } from '/@/api/content/model/circleAnalyticsModel';

export type TabType = 'recommend' | 'hot' | 'new';

export const useCircleRecommendStore = defineStore('circle-recommend', () => {
  const recommendList = ref<CircleRecommendItem[]>([]);
  const hotRankList = ref<CircleRankingItem[]>([]);
  const newRankList = ref<CircleRankingItem[]>([]);
  const activeTab = ref<TabType>('recommend');
  const fallbackMode = ref(false);

  const loading = reactive<Record<TabType, boolean>>({
    recommend: false,
    hot: false,
    new: false,
  });

  const loaded = reactive<Record<TabType, boolean>>({
    recommend: false,
    hot: false,
    new: false,
  });

  function setActiveTab(tab: TabType) {
    activeTab.value = tab;
  }

  function clearAll() {
    recommendList.value = [];
    hotRankList.value = [];
    newRankList.value = [];
    fallbackMode.value = false;
    loaded.recommend = false;
    loaded.hot = false;
    loaded.new = false;
  }

  async function fetchRecommendList(force = false) {
    if (loaded.recommend && !fallbackMode.value && !force) return;

    loading.recommend = true;
    try {
      const res = await getRecommendList();
      if (res.items && res.items.length > 0) {
        recommendList.value = res.items;
        fallbackMode.value = false;
        loaded.recommend = true;
      } else {
        fallbackMode.value = true;
        await fetchHotRankList();
      }
    } catch (e: any) {
      fallbackMode.value = true;
      await fetchHotRankList();
    } finally {
      loading.recommend = false;
    }
  }

  async function fetchHotRankList(force = false) {
    if (loaded.hot && !force) return;

    loading.hot = true;
    try {
      const res = await getHotRankList();
      hotRankList.value = res.items || [];
      loaded.hot = true;
    } catch (e: any) {
      hotRankList.value = [];
    } finally {
      loading.hot = false;
    }
  }

  async function fetchNewRankList(force = false) {
    if (loaded.new && !force) return;

    loading.new = true;
    try {
      const res = await getNewRankList();
      newRankList.value = res.items || [];
      loaded.new = true;
    } catch (e: any) {
      newRankList.value = [];
    } finally {
      loading.new = false;
    }
  }

  function refreshRecommend() {
    loaded.recommend = false;
    return fetchRecommendList(true);
  }

  async function reportExposure(data: RecommendExposureReq) {
    try {
      await reportRecommendExposure(data);
    } catch (e) {
    }
  }

  async function reportClick(sourceId: string) {
    try {
      await reportRecommendClick(sourceId);
    } catch (e) {
    }
  }

  return {
    recommendList,
    hotRankList,
    newRankList,
    activeTab,
    fallbackMode,
    loading,
    loaded,
    setActiveTab,
    clearAll,
    fetchRecommendList,
    fetchHotRankList,
    fetchNewRankList,
    refreshRecommend,
    reportExposure,
    reportClick,
  };
});

export function useCircleRecommendStoreWithOut() {
  return useCircleRecommendStore(store);
}
