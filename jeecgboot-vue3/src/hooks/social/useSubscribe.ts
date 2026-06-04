import { ref, reactive, computed } from 'vue';
import {
  subscribeSource,
  cancelSubscription,
  pauseSubscription,
  resumeSubscription,
  getSubscribeList,
  getSubscribePlaza,
  getSubscribeSourceDetail,
  subscribeFromPlaza,
  batchPauseSubscribe,
  batchResumeSubscribe,
  batchCancelSubscribe,
  getNotificationPreference,
  saveNotificationPreference,
} from '/@/api/content/subscribe';
import { useUserStore } from '/@/store/modules/user';

/** 订阅条目 */
export interface SubscribeItem {
  sourceId: string;
  sourceName?: string;
  sourceType?: string;
  status?: string;
  [key: string]: any;
}

/** 订阅广场条目 */
export interface PlazaItem {
  sourceId: string;
  sourceName?: string;
  sourceType?: string;
  category?: string;
  [key: string]: any;
}

/** 通知偏好 */
export interface NotificationPreference {
  sourceId: string;
  enabled?: boolean;
  [key: string]: any;
}

/**
 * 订阅模块 composable
 * 封装订阅相关 API 操作，提供响应式状态
 */
export function useSubscribe() {
  const userStore = useUserStore();
  const userId = computed(() => userStore.getUserInfo?.userId ?? '');

  const loading = ref(false);
  const error = ref<Error | null>(null);
  const subscribeList = ref<SubscribeItem[]>([]);
  const plazaList = ref<PlazaItem[]>([]);
  const pagination = reactive({ pageNo: 1, pageSize: 20, total: 0 });

  /** 订阅来源 */
  async function subscribe(sourceId: string, sourceType: string) {
    error.value = null;
    try {
      return await subscribeSource(userId.value, { sourceId, sourceType });
    } catch (e: any) {
      error.value = e;
      throw e;
    }
  }

  /** 取消订阅 */
  async function cancel(sourceId: string) {
    error.value = null;
    try {
      return await cancelSubscription(userId.value, sourceId);
    } catch (e: any) {
      error.value = e;
      throw e;
    }
  }

  /** 暂停订阅 */
  async function pause(sourceId: string) {
    error.value = null;
    try {
      return await pauseSubscription(userId.value, sourceId);
    } catch (e: any) {
      error.value = e;
      throw e;
    }
  }

  /** 恢复订阅 */
  async function resume(sourceId: string) {
    error.value = null;
    try {
      return await resumeSubscription(userId.value, sourceId);
    } catch (e: any) {
      error.value = e;
      throw e;
    }
  }

  /** 加载订阅列表 */
  async function loadList(params?: { keyword?: string; sourceType?: string; pageNo?: number; pageSize?: number }) {
    loading.value = true;
    error.value = null;
    try {
      const res = await getSubscribeList(userId.value, params);
      subscribeList.value = res?.records ?? res ?? [];
      pagination.total = res?.total ?? 0;
      if (params?.pageNo) pagination.pageNo = params.pageNo;
      if (params?.pageSize) pagination.pageSize = params.pageSize;
      return res;
    } catch (e: any) {
      error.value = e;
      throw e;
    } finally {
      loading.value = false;
    }
  }

  /** 加载订阅广场 */
  async function loadPlaza(params?: { keyword?: string; category?: string; page?: number; size?: number }) {
    loading.value = true;
    error.value = null;
    try {
      const res = await getSubscribePlaza(params);
      plazaList.value = res?.records ?? res ?? [];
      return res;
    } catch (e: any) {
      error.value = e;
      throw e;
    } finally {
      loading.value = false;
    }
  }

  /** 获取订阅源详情 */
  async function getSourceDetail(sourceId: string) {
    return getSubscribeSourceDetail(sourceId);
  }

  /** 从广场订阅 */
  async function subscribeFromPlazaAction(sourceId: string, sourceType: string) {
    error.value = null;
    try {
      return await subscribeFromPlaza(userId.value, { sourceId, sourceType });
    } catch (e: any) {
      error.value = e;
      throw e;
    }
  }

  /** 批量暂停 */
  async function batchPause(sourceIds: string[]) {
    error.value = null;
    try {
      return await batchPauseSubscribe(userId.value, { sourceIds });
    } catch (e: any) {
      error.value = e;
      throw e;
    }
  }

  /** 批量恢复 */
  async function batchResume(sourceIds: string[]) {
    error.value = null;
    try {
      return await batchResumeSubscribe(userId.value, { sourceIds });
    } catch (e: any) {
      error.value = e;
      throw e;
    }
  }

  /** 批量取消 */
  async function batchCancel(sourceIds: string[]) {
    error.value = null;
    try {
      return await batchCancelSubscribe(userId.value, { sourceIds });
    } catch (e: any) {
      error.value = e;
      throw e;
    }
  }

  /** 获取通知偏好 */
  async function getNotification(sourceId: string) {
    return getNotificationPreference(userId.value, sourceId);
  }

  /** 保存通知偏好 */
  async function saveNotification(sourceId: string, data: NotificationPreference) {
    error.value = null;
    try {
      return await saveNotificationPreference(userId.value, sourceId, data);
    } catch (e: any) {
      error.value = e;
      throw e;
    }
  }

  return {
    loading,
    error,
    subscribeList,
    plazaList,
    pagination,
    subscribe,
    cancel,
    pause,
    resume,
    loadList,
    loadPlaza,
    getSourceDetail,
    subscribeFromPlaza: subscribeFromPlazaAction,
    batchPause,
    batchResume,
    batchCancel,
    getNotification,
    saveNotification,
  };
}
