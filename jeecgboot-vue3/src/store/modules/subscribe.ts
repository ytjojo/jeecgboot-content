import { defineStore } from 'pinia';
import { ref } from 'vue';
import { store } from '/@/store';
import {
  getSubscribeList,
  subscribeSource,
  cancelSubscription,
  pauseSubscription,
  resumeSubscription,
  getNotificationPreference,
  saveNotificationPreference,
} from '/@/api/content/subscribe';

export interface SubscribeItem {
  id: string;
  sourceId: string;
  sourceType: string;
  sourceName: string;
  sourceIcon: string;
  category: string;
  subscriberCount: number;
  lastUpdateTime: string;
  subscribeTime: string;
  status: 'active' | 'paused';
}

export interface NotificationConfig {
  channelInsite: boolean;
  channelPush: boolean;
  channelEmail: boolean;
  frequency: 'realtime' | 'daily';
  quietStart?: string;
  quietEnd?: string;
}

export const useSubscribeStore = defineStore('social-subscribe', () => {
  // ===== State =====
  const subscribeList = ref<SubscribeItem[]>([]);
  const totalSubscribes = ref(0);
  const loading = ref(false);
  const currentPage = ref(1);
  const pageSize = ref(20);
  const hasMore = ref(true);
  const searchKeyword = ref('');
  const selectedSourceType = ref('');
  const currentNotificationConfig = ref<NotificationConfig | null>(null);
  const globalNotificationDefault = ref<NotificationConfig | null>(null);

  // ===== Methods =====
  async function fetchSubscribeList(userId: string, reset = false) {
    if (reset) {
      currentPage.value = 1;
      subscribeList.value = [];
      hasMore.value = true;
    }
    if (!hasMore.value && !reset) return;

    loading.value = true;
    try {
      const res = await getSubscribeList(userId, {
        keyword: searchKeyword.value || undefined,
        sourceType: selectedSourceType.value || undefined,
        pageNo: currentPage.value,
        pageSize: pageSize.value,
      });
      const { records = [], total = 0 } = res;
      if (reset) {
        subscribeList.value = records;
      } else {
        subscribeList.value.push(...records);
      }
      totalSubscribes.value = total;
      hasMore.value = subscribeList.value.length < total;
      currentPage.value++;
    } finally {
      loading.value = false;
    }
  }

  async function subscribe(userId: string, sourceId: string, sourceType: string) {
    await subscribeSource(userId, { sourceId, sourceType });
    await fetchSubscribeList(userId, true);
  }

  async function unsubscribe(userId: string, sourceId: string) {
    await cancelSubscription(userId, sourceId);
    await fetchSubscribeList(userId, true);
  }

  async function pause(userId: string, sourceId: string) {
    await pauseSubscription(userId, sourceId);
    await fetchSubscribeList(userId, true);
  }

  async function resume(userId: string, sourceId: string) {
    await resumeSubscription(userId, sourceId);
    await fetchSubscribeList(userId, true);
  }

  async function fetchNotificationConfig(userId: string, sourceId: string) {
    const res = await getNotificationPreference(userId, sourceId);
    currentNotificationConfig.value = res || null;
  }

  async function saveConfig(userId: string, sourceId: string, config: NotificationConfig) {
    await saveNotificationPreference(userId, sourceId, config);
    currentNotificationConfig.value = config;
  }

  async function fetchGlobalNotificationDefault() {
    if (globalNotificationDefault.value) return;
    const res = await getNotificationPreference('global', 'default');
    globalNotificationDefault.value = res || null;
  }

  function setSearchKeyword(keyword: string) {
    searchKeyword.value = keyword;
  }

  function setSelectedSourceType(sourceType: string) {
    selectedSourceType.value = sourceType;
  }

  return {
    // State
    subscribeList,
    totalSubscribes,
    loading,
    currentPage,
    pageSize,
    hasMore,
    searchKeyword,
    selectedSourceType,
    currentNotificationConfig,
    globalNotificationDefault,
    // Methods
    fetchSubscribeList,
    subscribe,
    unsubscribe,
    pause,
    resume,
    fetchNotificationConfig,
    saveConfig,
    fetchGlobalNotificationDefault,
    setSearchKeyword,
    setSelectedSourceType,
  };
});

export function useSubscribeStoreWithOut() {
  return useSubscribeStore(store);
}
