import { defHttp } from '/@/utils/http/axios';

enum Api {
  generate = '/content/user/invite/generate',
  bind = '/content/user/invite/bind',
  records = '/content/user/invite/records',
  stats = '/content/user/invite/stats',
}

/** 生成或获取邀请码 */
export const generateInviteCode = (userId: string) =>
  defHttp.post({ url: Api.generate, params: { userId } });

/** 绑定邀请关系（注册时调用） */
export const bindInviteRelation = (inviteCode: string, inviteeUserId: string) =>
  defHttp.post({ url: Api.bind, params: { inviteCode, inviteeUserId } });

/** 查询邀请记录列表 */
export const listInviteRecords = (userId: string, params?: { pageNo?: number; pageSize?: number }) =>
  defHttp.get({ url: Api.records, params: { userId, ...params } });

/** 查询邀请统计 */
export const getInviteStats = (userId: string) =>
  defHttp.get({ url: Api.stats, params: { userId } });
