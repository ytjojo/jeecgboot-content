import { defHttp } from '/@/utils/http/axios';

enum Api {
  add = '/channel/governance/blacklist/add',
  remove = '/channel/governance/blacklist/remove',
  list = '/channel/governance/blacklist/list',
}

/** 加入黑名单 */
export const addToBlacklist = (data: { channelId: string; userId: string; reason: string }) =>
  defHttp.post({ url: Api.add, data });

/** 移出黑名单 */
export const removeFromBlacklist = (data: { channelId: string; userId: string }) =>
  defHttp.post({ url: Api.remove, data });

/** 黑名单列表 */
export const getBlacklist = (params: { channelId: string; [key: string]: any }) =>
  defHttp.get({ url: Api.list, params });
