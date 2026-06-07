import { defHttp } from '/@/utils/http/axios';

enum Api {
  create = '/api/v1/content/channel/invite/create',
  list = '/api/v1/content/channel/invite/list',
  revoke = '/api/v1/content/channel/invite/revoke',
  use = '/api/v1/content/channel/invite/use',
}

/** 创建邀请 */
export const createInvite = (data: { type: string; expireTime: string[]; maxUses: number }) =>
  defHttp.post({ url: Api.create, data });

/** 邀请列表 */
export const getInviteList = (params?: { pageNo?: number; pageSize?: number; status?: string }) =>
  defHttp.get({ url: Api.list, params });

/** 撤销邀请 */
export const revokeInvite = (inviteId: string) =>
  defHttp.post({ url: Api.revoke, data: { inviteId } });

/** 使用邀请加入 */
export const joinByInvite = (inviteCode: string) =>
  defHttp.post({ url: Api.use, data: { inviteCode } });
