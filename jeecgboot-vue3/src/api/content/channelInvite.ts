import { defHttp } from '/@/utils/http/axios';

enum Api {
  create = '/channel/invite/create',
  list = '/channel/invite/list',
  revoke = '/channel/invite/revoke',
  use = '/channel/invite/use',
}

/** 创建邀请 */
export const createInvite = (data: { type: string; expireTime: string[]; maxUses: number }) =>
  defHttp.post({ url: Api.create, data });

/** 邀请列表 */
export const getInviteList = (params?: any) =>
  defHttp.get({ url: Api.list, params });

/** 撤销邀请 */
export const revokeInvite = (inviteId: string) =>
  defHttp.post({ url: Api.revoke, data: { inviteId } });

/** 使用邀请加入 */
export const joinByInvite = (inviteCode: string) =>
  defHttp.post({ url: Api.use, data: { inviteCode } });
