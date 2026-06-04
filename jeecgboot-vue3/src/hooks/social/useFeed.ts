import { ref, reactive } from 'vue';
import { getFollowingFeed } from '/@/api/content/relation';
import { getSubscribeFeed } from '/@/api/content/subscribe';

/**
 * 信息流 composable
 * 封装关注动态 Feed 和订阅 Feed 的加载逻辑
 */
export function useFeed() {
  const loading = ref(false);
  const feedList = ref<any[]>([]);
  const pagination = reactive({ page: 1, size: 20, total: 0 });
  const hasMore = ref(true);

  /** 加载关注动态 Feed */
  async function loadFollowingFeed(params?: { page?: number; size?: number; types?: string }) {
    loading.value = true;
    try {
      const page = params?.page ?? 1;
      const res = await getFollowingFeed({ page, size: params?.size ?? 20, types: params?.types });
      const records = res?.records ?? res ?? [];
      if (page === 1) {
        feedList.value = records;
      } else {
        feedList.value = [...feedList.value, ...records];
      }
      pagination.page = page;
      pagination.total = res?.total ?? 0;
      hasMore.value = feedList.value.length < pagination.total;
      return res;
    } finally {
      loading.value = false;
    }
  }

  /** 加载订阅 Feed */
  async function loadSubscribeFeed(params?: { page?: number; size?: number; sourceType?: string }) {
    loading.value = true;
    try {
      const page = params?.page ?? 1;
      const res = await getSubscribeFeed({ page, size: params?.size ?? 20, sourceType: params?.sourceType });
      const records = res?.records ?? res ?? [];
      if (page === 1) {
        feedList.value = records;
      } else {
        feedList.value = [...feedList.value, ...records];
      }
      pagination.page = page;
      pagination.total = res?.total ?? 0;
      hasMore.value = feedList.value.length < pagination.total;
      return res;
    } finally {
      loading.value = false;
    }
  }

  /** 加载更多（关注动态） */
  async function loadMoreFollowing(types?: string) {
    if (!hasMore.value || loading.value) return;
    return loadFollowingFeed({ page: pagination.page + 1, types });
  }

  /** 加载更多（订阅 Feed） */
  async function loadMoreSubscribe(sourceType?: string) {
    if (!hasMore.value || loading.value) return;
    return loadSubscribeFeed({ page: pagination.page + 1, sourceType });
  }

  /** 重置状态 */
  function reset() {
    feedList.value = [];
    pagination.page = 1;
    pagination.total = 0;
    hasMore.value = true;
  }

  return {
    loading,
    feedList,
    pagination,
    hasMore,
    loadFollowingFeed,
    loadSubscribeFeed,
    loadMoreFollowing,
    loadMoreSubscribe,
    reset,
  };
}
