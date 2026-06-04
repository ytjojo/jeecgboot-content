import { ref, reactive } from 'vue';
import { getFollowingFeed } from '/@/api/content/relation';
import { getSubscribeFeed } from '/@/api/content/subscribe';

/** Feed 条目最小接口 */
export interface FeedItem {
  id: string;
  [key: string]: any;
}

/**
 * 信息流 composable
 * 封装关注动态 Feed 和订阅 Feed 的加载逻辑
 * 两套独立状态，避免切换时互相污染
 */
export function useFeed() {
  const loading = ref(false);

  // ---- 关注动态 Feed 状态 ----
  const followingList = ref<FeedItem[]>([]);
  const followingPagination = reactive({ page: 1, size: 20, total: 0 });
  const followingHasMore = ref(true);

  // ---- 订阅 Feed 状态 ----
  const subscribeList = ref<FeedItem[]>([]);
  const subscribePagination = reactive({ page: 1, size: 20, total: 0 });
  const subscribeHasMore = ref(true);

  /** 加载关注动态 Feed */
  async function loadFollowingFeed(params?: { page?: number; size?: number; types?: string }) {
    loading.value = true;
    try {
      const page = params?.page ?? 1;
      const res = await getFollowingFeed({ page, size: params?.size ?? 20, types: params?.types });
      const records: FeedItem[] = res?.records ?? res ?? [];
      if (page === 1) {
        followingList.value = records;
      } else {
        followingList.value = [...followingList.value, ...records];
      }
      followingPagination.page = page;
      followingPagination.total = res?.total ?? 0;
      followingHasMore.value = followingList.value.length < followingPagination.total;
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
      const records: FeedItem[] = res?.records ?? res ?? [];
      if (page === 1) {
        subscribeList.value = records;
      } else {
        subscribeList.value = [...subscribeList.value, ...records];
      }
      subscribePagination.page = page;
      subscribePagination.total = res?.total ?? 0;
      subscribeHasMore.value = subscribeList.value.length < subscribePagination.total;
      return res;
    } finally {
      loading.value = false;
    }
  }

  /** 加载更多（关注动态） */
  async function loadMoreFollowing(types?: string) {
    if (!followingHasMore.value || loading.value) return;
    return loadFollowingFeed({ page: followingPagination.page + 1, types });
  }

  /** 加载更多（订阅 Feed） */
  async function loadMoreSubscribe(sourceType?: string) {
    if (!subscribeHasMore.value || loading.value) return;
    return loadSubscribeFeed({ page: subscribePagination.page + 1, sourceType });
  }

  /** 重置关注动态状态 */
  function resetFollowing() {
    followingList.value = [];
    followingPagination.page = 1;
    followingPagination.total = 0;
    followingHasMore.value = true;
  }

  /** 重置订阅 Feed 状态 */
  function resetSubscribe() {
    subscribeList.value = [];
    subscribePagination.page = 1;
    subscribePagination.total = 0;
    subscribeHasMore.value = true;
  }

  /** 重置全部状态 */
  function reset() {
    resetFollowing();
    resetSubscribe();
  }

  return {
    loading,
    // 关注动态
    followingList,
    followingPagination,
    followingHasMore,
    // 订阅 Feed
    subscribeList,
    subscribePagination,
    subscribeHasMore,
    // 操作
    loadFollowingFeed,
    loadSubscribeFeed,
    loadMoreFollowing,
    loadMoreSubscribe,
    resetFollowing,
    resetSubscribe,
    reset,
  };
}
