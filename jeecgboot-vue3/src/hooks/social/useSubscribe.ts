import { ref, reactive } from 'vue';
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

/**
 * 订阅模块 composable
 * 封装订阅相关 API 操作，提供响应式状态
 */
export function useSubscribe() {
  const userStore = useUserStore();
  const userId = userStore.getUserInfo?.userId ?? '';

  const loading = ref(false);
  const subscribeList = ref<any[]>([]);
  const plazaList = ref<any[]>([]);
  const pagination = reactive({ pageNo: 1, pageSize: 20, total: 0 });

  /** 订阅来源 */
  async function subscribe(sourceId: string, sourceType: string) {
    return subscribeSource(userId, { sourceId, sourceType });
  }

  /** 取消订阅 */
  async function cancel(sourceId: string) {
    return cancelSubscription(userId, sourceId);
  }

  /** 暂停订阅 */
  async function pause(sourceId: string) {
    return pauseSubscription(userId, sourceId);
  }

  /** 恢复订阅 */
  async function resume(sourceId: string) {
    return resumeSubscription(userId, sourceId);
  }

  /** 加载订阅列表 */
  async function loadList(params?: { keyword?: string; sourceType?: string; pageNo?: number; pageSize?: number }) {
    loading.value = true;
    try {
      const res = await getSubscribeList(userId, params);
      subscribeList.value = res?.records ?? res ?? [];
      pagination.total = res?.total ?? 0;
      if (params?.pageNo) pagination.pageNo = params.pageNo;
      if (params?.pageSize) pagination.pageSize = params.pageSize;
      return res;
    } finally {
      loading.value = false;
    }
  }

  /** 加载订阅广场 */
  async function loadPlaza(params?: { keyword?: string; category?: string; page?: number; size?: number }) {
    loading.value = true;
    try {
      const res = await getSubscribePlaza(params);
      plazaList.value = res?.records ?? res ?? [];
      return res;
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
    return subscribeFromPlaza(userId, { sourceId, sourceType });
  }

  /** 批量暂停 */
  async function batchPause(sourceIds: string[]) {
    return batchPauseSubscribe(userId, { sourceIds });
  }

  /** 批量恢复 */
  async function batchResume(sourceIds: string[]) {
    return batchResumeSubscribe(userId, { sourceIds });
  }

  /** 批量取消 */
  async function batchCancel(sourceIds: string[]) {
    return batchCancelSubscribe(userId, { sourceIds });
  }

  /** 获取通知偏好 */
  async function getNotification(sourceId: string) {
    return getNotificationPreference(userId, sourceId);
  }

  /** 保存通知偏好 */
  async function saveNotification(sourceId: string, data: any) {
    return saveNotificationPreference(userId, sourceId, data);
  }

  return {
    loading,
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
