import { defHttp } from '/@/utils/http/axios';

enum Api {
  subscribe = '/channel/subscription/subscribe',
  unsubscribe = '/channel/subscription/unsubscribe',
  status = '/channel/subscription/status',
  list = '/channel/subscription/list',
  groupCreate = '/channel/subscription/group/create',
  groupRename = '/channel/subscription/group/rename',
  groupDelete = '/channel/subscription/group/delete',
  groupList = '/channel/subscription/group/list',
  reminder = '/channel/subscription/reminder',
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
export const getSubscriptionList = (params?: any) =>
  defHttp.get({ url: Api.list, params });

/** 创建分组 */
export const createSubscriptionGroup = (data: { name: string }) =>
  defHttp.post({ url: Api.groupCreate, data });

/** 重命名分组 */
export const renameSubscriptionGroup = (groupId: string, newName: string) =>
  defHttp.post({ url: Api.groupRename, params: { groupId, newName } });

/** 删除分组 */
export const deleteSubscriptionGroup = (groupId: string) =>
  defHttp.post({ url: Api.groupDelete, params: { groupId } });

/** 分组列表 */
export const getSubscriptionGroupList = () =>
  defHttp.get({ url: Api.groupList });

/** 更新提醒设置 */
export const updateSubscriptionReminder = (data: { channelId: string; enabled: boolean }) =>
  defHttp.put({ url: Api.reminder, data });
