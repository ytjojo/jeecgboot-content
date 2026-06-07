import { defHttp } from '/@/utils/http/axios';

enum Api {
  subscribe = '/api/v1/content/channel/subscription/subscribe',
  unsubscribe = '/api/v1/content/channel/subscription/unsubscribe',
  status = '/api/v1/content/channel/subscription/status',
  list = '/api/v1/content/channel/subscription/list',
  groupCreate = '/api/v1/content/channel/subscription/group/create',
  groupRename = '/api/v1/content/channel/subscription/group/rename',
  groupDelete = '/api/v1/content/channel/subscription/group/delete',
  groupList = '/api/v1/content/channel/subscription/group/list',
  reminder = '/api/v1/content/channel/subscription/reminder',
}

/** 订阅频道 */
export const subscribeChannel = (channelId: string) =>
  defHttp.post({ url: Api.subscribe, data: { channelId } });

/** 取消订阅 */
export const unsubscribeChannel = (channelId: string) =>
  defHttp.post({ url: Api.unsubscribe, data: { channelId } });

/** 查询订阅状态 */
export const getSubscriptionStatus = (channelId: string) =>
  defHttp.get({ url: `${Api.status}/${channelId}` });

/** 订阅列表 */
export const getSubscriptionList = (params?: { pageNo?: number; pageSize?: number; keyword?: string }) =>
  defHttp.get({ url: Api.list, params });

/** 创建分组 */
export const createSubscriptionGroup = (data: { name: string }) =>
  defHttp.post({ url: Api.groupCreate, data });

/** 重命名分组 */
export const renameSubscriptionGroup = (groupId: string, newName: string) =>
  defHttp.post({ url: Api.groupRename, data: { groupId, newName } });

/** 删除分组 */
export const deleteSubscriptionGroup = (groupId: string) =>
  defHttp.post({ url: Api.groupDelete, data: { groupId } });

/** 分组列表 */
export const getSubscriptionGroupList = () =>
  defHttp.get({ url: Api.groupList });

/** 更新提醒设置 */
export const updateSubscriptionReminder = (data: { channelId: string; enabled: boolean }) =>
  defHttp.put({ url: Api.reminder, data });
