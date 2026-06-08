import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { store } from '/@/store';
import { searchChannels, submitSearchFeedback } from '/@/api/content/channelDiscovery';
import type {
  ChannelSearchResultVO,
  ChannelSearchParams,
  PageResult,
  SearchFeedbackReq,
} from '/@/api/content/model/channelDiscoveryModel';

const SEARCH_HISTORY_KEY = 'channel_search_history';
const MAX_HISTORY_SIZE = 10;

/** 从 localStorage 读取搜索历史 */
function loadSearchHistory(): string[] {
  try {
    const raw = localStorage.getItem(SEARCH_HISTORY_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
}

/** 保存搜索历史到 localStorage */
function saveSearchHistory(history: string[]) {
  try {
    localStorage.setItem(SEARCH_HISTORY_KEY, JSON.stringify(history));
  } catch {
    // localStorage 满或其他异常，静默忽略
  }
}

export const useChannelSearchStore = defineStore('channelSearch', () => {
  // ===== State =====
  const keyword = ref('');
  const results = ref<ChannelSearchResultVO[]>([]);
  const total = ref(0);
  const currentPage = ref(1);
  const pageSize = ref(20);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // 筛选条件
  const channelType = ref<string | undefined>(undefined);
  const categoryId = ref<string | undefined>(undefined);
  const sortBy = ref<'relevance' | 'active' | 'subscriber' | 'created'>('relevance');

  // 搜索历史（持久化到 localStorage）
  const searchHistory = ref<string[]>(loadSearchHistory());

  // ===== Getters =====
  const hasMore = computed(() => {
    return results.value.length < total.value;
  });

  const isEmpty = computed(() => {
    return !loading.value && keyword.value && results.value.length === 0;
  });

  const filterValues = computed(() => ({
    channelType: channelType.value,
    categoryId: categoryId.value,
    sortBy: sortBy.value,
  }));

  // ===== Actions =====

  /** 执行搜索 */
  async function executeSearch(params?: Partial<ChannelSearchParams>) {
    if (params) {
      if (params.keyword !== undefined) keyword.value = params.keyword;
      if (params.channelType !== undefined) channelType.value = params.channelType;
      if (params.categoryId !== undefined) categoryId.value = params.categoryId;
      if (params.sortBy !== undefined) sortBy.value = params.sortBy;
      if (params.page !== undefined) currentPage.value = params.page;
      if (params.pageSize !== undefined) pageSize.value = params.pageSize;
    }

    if (!keyword.value.trim()) {
      results.value = [];
      total.value = 0;
      return;
    }

    loading.value = true;
    error.value = null;

    try {
      const searchParams: ChannelSearchParams = {
        keyword: keyword.value.trim(),
        channelType: channelType.value,
        categoryId: categoryId.value,
        sortBy: sortBy.value,
        page: currentPage.value,
        pageSize: pageSize.value,
      };

      const data: PageResult<ChannelSearchResultVO> = await searchChannels(searchParams);
      results.value = data.records || [];
      total.value = data.total || 0;

      // 添加到搜索历史
      addToHistory(keyword.value.trim());
    } catch (e) {
      error.value = '搜索服务繁忙';
      console.warn('[SearchStore] 搜索失败', e);
    } finally {
      loading.value = false;
    }
  }

  /** 加载更多（分页） */
  async function loadMore() {
    if (!hasMore.value || loading.value) return;

    currentPage.value += 1;
    loading.value = true;

    try {
      const searchParams: ChannelSearchParams = {
        keyword: keyword.value.trim(),
        channelType: channelType.value,
        categoryId: categoryId.value,
        sortBy: sortBy.value,
        page: currentPage.value,
        pageSize: pageSize.value,
      };

      const data = await searchChannels(searchParams);
      results.value = [...results.value, ...(data.records || [])];
      total.value = data.total || total.value;
    } catch (e) {
      currentPage.value -= 1; // 回退页码
      console.warn('[SearchStore] 加载更多失败', e);
    } finally {
      loading.value = false;
    }
  }

  /** 设置筛选条件并重新搜索 */
  async function setFilter(key: 'channelType' | 'categoryId' | 'sortBy', value: string) {
    switch (key) {
      case 'channelType':
        channelType.value = value || undefined;
        break;
      case 'categoryId':
        categoryId.value = value || undefined;
        break;
      case 'sortBy':
        sortBy.value = value as ChannelSearchParams['sortBy'];
        break;
    }
    currentPage.value = 1;
    results.value = [];
    await executeSearch();
  }

  /** 清除筛选条件 */
  function clearFilters() {
    channelType.value = undefined;
    categoryId.value = undefined;
    sortBy.value = 'relevance';
    currentPage.value = 1;
  }

  /** 搜索反馈 */
  async function feedbackSearch(helpful: boolean) {
    const req: SearchFeedbackReq = {
      keyword: keyword.value,
      helpful,
    };
    try {
      await submitSearchFeedback(req);
      return true;
    } catch {
      return false;
    }
  }

  /** 添加到搜索历史 */
  function addToHistory(term: string) {
    const history = searchHistory.value.filter((h) => h !== term);
    history.unshift(term);
    if (history.length > MAX_HISTORY_SIZE) {
      history.length = MAX_HISTORY_SIZE;
    }
    searchHistory.value = history;
    saveSearchHistory(history);
  }

  /** 清除单条搜索历史 */
  function removeHistoryItem(term: string) {
    searchHistory.value = searchHistory.value.filter((h) => h !== term);
    saveSearchHistory(searchHistory.value);
  }

  /** 清除全部搜索历史 */
  function clearHistory() {
    searchHistory.value = [];
    saveSearchHistory([]);
  }

  /** 重置搜索状态 */
  function resetSearch() {
    keyword.value = '';
    results.value = [];
    total.value = 0;
    currentPage.value = 1;
    channelType.value = undefined;
    categoryId.value = undefined;
    sortBy.value = 'relevance';
    error.value = null;
  }

  return {
    // State
    keyword,
    results,
    total,
    currentPage,
    pageSize,
    loading,
    error,
    channelType,
    categoryId,
    sortBy,
    searchHistory,
    // Getters
    hasMore,
    isEmpty,
    filterValues,
    // Actions
    executeSearch,
    loadMore,
    setFilter,
    clearFilters,
    feedbackSearch,
    addToHistory,
    removeHistoryItem,
    clearHistory,
    resetSearch,
  };
});

// Support use outside of setup
export function useChannelSearchStoreWithOut() {
  return useChannelSearchStore(store);
}
