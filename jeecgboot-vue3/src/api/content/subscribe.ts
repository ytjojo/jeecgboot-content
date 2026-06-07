import { defHttp } from '/@/utils/http/axios';

enum Api {
  subscribe = '/api/v1/content/user/subscription/subscribe',
  cancel = '/api/v1/content/user/subscription/cancel',
  pause = '/api/v1/content/user/subscription/pause',
  resume = '/api/v1/content/user/subscription/resume',
  list = '/api/v1/content/user/subscription/list',
  feed = '/api/v1/content/user/subscription/feed',
  plaza = '/api/v1/content/user/subscription/plaza',
  sourceDetail = '/api/v1/content/user/subscription/source/detail',
  sourceSubscribe = '/api/v1/content/user/subscription/source/subscribe',
  batchPause = '/api/v1/content/user/subscription/batch/pause',
  batchResume = '/api/v1/content/user/subscription/batch/resume',
  batchCancel = '/api/v1/content/user/subscription/batch/cancel',
  notificationPreference = '/api/v1/content/user/subscription/notification/preference',
}

/** 订阅来源 */
export const subscribeSource = (userId: string, data: { sourceId: string; sourceType: string }) =>
  defHttp.post({ url: Api.subscribe, params: { userId }, data });

/** 取消订阅 */
export const cancelSubscription = (userId: string, sourceId: string) =>
  defHttp.post({ url: Api.cancel, params: { userId, sourceId } });

/** 暂停订阅 */
export const pauseSubscription = (userId: string, sourceId: string) =>
  defHttp.post({ url: Api.pause, params: { userId, sourceId } });

/** 恢复订阅 */
export const resumeSubscription = (userId: string, sourceId: string) =>
  defHttp.post({ url: Api.resume, params: { userId, sourceId } });

/** 获取订阅列表 */
export const getSubscribeList = (userId: string, params?: { keyword?: string; sourceType?: string; pageNo?: number; pageSize?: number }) =>
  defHttp.get({ url: Api.list, params: { userId, ...params } });

/** 获取订阅 Feed */
export const getSubscribeFeed = (params: { page?: number; size?: number; sourceType?: string }) =>
  defHttp.get({ url: Api.feed, params });

/** 获取订阅广场 */
export const getSubscribePlaza = (params?: { keyword?: string; category?: string; page?: number; size?: number; sort?: string }) =>
  defHttp.get({ url: Api.plaza, params });

/** 获取订阅源详情 */
export const getSubscribeSourceDetail = (sourceId: string) =>
  defHttp.get({ url: Api.sourceDetail, params: { sourceId } });

/** 从广场订阅 */
export const subscribeFromPlaza = (userId: string, data: { sourceId: string; sourceType: string }) =>
  defHttp.post({ url: Api.sourceSubscribe, params: { userId }, data });

/** 批量暂停订阅 */
export const batchPauseSubscribe = (userId: string, data: { sourceIds: string[] }) =>
  defHttp.post({ url: Api.batchPause, params: { userId }, data });

/** 批量恢复订阅 */
export const batchResumeSubscribe = (userId: string, data: { sourceIds: string[] }) =>
  defHttp.post({ url: Api.batchResume, params: { userId }, data });

/** 批量取消订阅 */
export const batchCancelSubscribe = (userId: string, data: { sourceIds: string[] }) =>
  defHttp.post({ url: Api.batchCancel, params: { userId }, data });

/** 获取通知偏好 */
export const getNotificationPreference = (userId: string, sourceId: string) =>
  defHttp.get({ url: Api.notificationPreference, params: { userId, sourceId } });

/** 保存通知偏好 */
export const saveNotificationPreference = (
  userId: string,
  sourceId: string,
  data: { channelInApp?: boolean; channelPush?: boolean; channelEmail?: boolean; frequency?: string; quietStart?: string; quietEnd?: string },
) => defHttp.post({ url: Api.notificationPreference, params: { userId, sourceId }, data });
