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
  visibility?: 'PUBLIC' | 'MUTUAL_FOLLOW';
}

export const useFeedStore = defineStore('social-feed', () => {
  // ===== State =====
  const followFeedList = ref<FeedItem[]>([]);
  const subscribeFeedList = ref<FeedItem[]>([]);
  const priorityItems = ref<FeedItem[]>([]);
  const followLoading = ref(false);
  const subscribeLoading = ref(false);
  const followPage = ref(1);
  const followHasMore = ref(true);
  const subscribePage = ref(1);
  const subscribeHasMore = ref(true);
  const followTypes = ref<string[]>(['post', 'like', 'favorite']);
  const subscribeSourceType = ref('');
  const pageSize = ref(20);

  // ===== Methods =====
  async function fetchFollowFeed(reset = false) {
    if (reset) {
      followPage.value = 1;
      followFeedList.value = [];
      priorityItems.value = [];
      followHasMore.value = true;
    }
    if (!followHasMore.value && !reset) return;

    followLoading.value = true;
    try {
      const res = await getFollowingFeed({
        page: followPage.value,
        size: pageSize.value,
        types: followTypes.value.join(','),
      });
      const { priorityItems: pItems = [], items = [], total = 0 } = res;
      if (reset) {
        priorityItems.value = pItems || [];
        followFeedList.value = items || [];
      } else {
        followFeedList.value.push(...(items || []));
      }
      followHasMore.value = followFeedList.value.length < total;
      followPage.value++;
    } finally {
      followLoading.value = false;
    }
  }

  async function fetchSubscribeFeed(reset = false) {
    if (reset) {
      subscribePage.value = 1;
      subscribeFeedList.value = [];
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
        subscribeFeedList.value = records;
      } else {
        subscribeFeedList.value.push(...records);
      }
      subscribeHasMore.value = subscribeFeedList.value.length < total;
      subscribePage.value++;
    } finally {
      subscribeLoading.value = false;
    }
  }

  function setFollowTypes(types: string[]) {
    const prev = followTypes.value;
    if (types.length === 0) {
      followTypes.value = prev;
      return;
    }
    followTypes.value = types;
    fetchFollowFeed(true).catch(console.error);
  }

  function setSubscribeSourceType(type: string) {
    subscribeSourceType.value = type;
    fetchSubscribeFeed(true).catch(console.error);
  }

  function removeUserFeeds(userId: string): { items: FeedItem[]; indices: number[] } {
    const removed: { items: FeedItem[]; indices: number[] } = { items: [], indices: [] };
    followFeedList.value = followFeedList.value.filter((item, index) => {
      if (item.userId === userId) {
        removed.items.push(item);
        removed.indices.push(index);
        return false;
      }
      return true;
    });
    return removed;
  }

  function restoreUserFeeds(data: { items: FeedItem[]; indices: number[] }) {
    const list = [...followFeedList.value];
    data.items.forEach((item, i) => {
      const insertAt = Math.min(data.indices[i], list.length);
      list.splice(insertAt, 0, item);
    });
    followFeedList.value = list;
  }

  return {
    // State
    followFeedList,
    subscribeFeedList,
    priorityItems,
    followLoading,
    subscribeLoading,
    followPage,
    followHasMore,
    subscribePage,
    subscribeHasMore,
    followTypes,
    subscribeSourceType,
    pageSize,
    // Methods
    fetchFollowFeed,
    fetchSubscribeFeed,
    setFollowTypes,
    setSubscribeSourceType,
    removeUserFeeds,
    restoreUserFeeds,
  };
});

export function useFeedStoreWithOut() {
  return useFeedStore(store);
}
