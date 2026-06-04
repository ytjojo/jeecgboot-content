import { defineStore } from 'pinia';
import { ref } from 'vue';
import { store } from '/@/store';
import { getFollowingFeed } from '/@/api/content/relation';
import { getSubscribeFeed } from '/@/api/content/subscribe';

export interface FeedItem {
  id: string;
  userId: string;
  nickname: string;
  avatar: string;
  contentId: string;
  contentTitle: string;
  contentSummary: string;
  dynamicType: 'post' | 'like' | 'favorite';
  sourceType?: string;
  sourceName?: string;
  createTime: string;
  isPriority: boolean;
}

const CACHE_TTL = 5 * 60 * 1000; // 5 minutes

export const useFeedStore = defineStore('social-feed', () => {
  // ===== State =====
  const followingFeed = ref<FeedItem[]>([]);
  const subscribeFeed = ref<FeedItem[]>([]);
  const followingLoading = ref(false);
  const subscribeLoading = ref(false);
  const followingHasMore = ref(true);
  const subscribeHasMore = ref(true);
  const followingPage = ref(1);
  const subscribePage = ref(1);
  const pageSize = ref(20);
  const feedTypes = ref<string[]>(['post', 'like', 'favorite']);
  const subscribeSourceType = ref('');
  const followingLastFetch = ref(0);
  const subscribeLastFetch = ref(0);

  // ===== Methods =====
  function isCacheValid(type: 'following' | 'subscribe'): boolean {
    const lastFetch = type === 'following' ? followingLastFetch.value : subscribeLastFetch.value;
    return Date.now() - lastFetch < CACHE_TTL;
  }

  async function fetchFollowingFeed(reset = false) {
    if (reset) {
      followingPage.value = 1;
      followingFeed.value = [];
      followingHasMore.value = true;
    }
    if (!followingHasMore.value && !reset) return;

    followingLoading.value = true;
    try {
      const res = await getFollowingFeed({
        page: followingPage.value,
        size: pageSize.value,
        types: feedTypes.value.join(','),
      });
      const { priorityItems = [], items = [], total = 0 } = res;
      const allItems: FeedItem[] = [...(priorityItems || []), ...(items || [])];
      if (reset) {
        followingFeed.value = allItems;
      } else {
        followingFeed.value.push(...allItems);
      }
      followingHasMore.value = followingFeed.value.length < total;
      followingPage.value++;
      followingLastFetch.value = Date.now();
    } finally {
      followingLoading.value = false;
    }
  }

  async function fetchSubscribeFeed(reset = false) {
    if (reset) {
      subscribePage.value = 1;
      subscribeFeed.value = [];
      subscribeHasMore.value = true;
    }
    if (!subscribeHasMore.value && !reset) return;

    subscribeLoading.value = true;
    try {
      const res = await getSubscribeFeed({
        page: subscribePage.value,
        size: pageSize.value,
        sourceType: subscribeSourceType.value || undefined,
      });
      const { records = [], total = 0 } = res;
      if (reset) {
        subscribeFeed.value = records;
      } else {
        subscribeFeed.value.push(...records);
      }
      subscribeHasMore.value = subscribeFeed.value.length < total;
      subscribePage.value++;
      subscribeLastFetch.value = Date.now();
    } finally {
      subscribeLoading.value = false;
    }
  }

  function setFeedTypes(types: string[]) {
    feedTypes.value = types;
  }

  function setSubscribeSourceType(sourceType: string) {
    subscribeSourceType.value = sourceType;
  }

  function removeUserFeed(userId: string) {
    followingFeed.value = followingFeed.value.filter((item) => item.userId !== userId);
  }

  return {
    // State
    followingFeed,
    subscribeFeed,
    followingLoading,
    subscribeLoading,
    followingHasMore,
    subscribeHasMore,
    followingPage,
    subscribePage,
    pageSize,
    feedTypes,
    subscribeSourceType,
    followingLastFetch,
    subscribeLastFetch,
    // Methods
    fetchFollowingFeed,
    fetchSubscribeFeed,
    setFeedTypes,
    setSubscribeSourceType,
    removeUserFeed,
    isCacheValid,
  };
});

export function useFeedStoreWithOut() {
  return useFeedStore(store);
}
