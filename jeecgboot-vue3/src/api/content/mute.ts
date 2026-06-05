import { defHttp } from '/@/utils/http/axios';

enum Api {
  mute = '/content/user/relation/mute',
  unmute = '/content/user/relation/mute/cancel',
  muteList = '/content/user/relation/mute-list',
}

/** 屏蔽用户 */
export const muteUser = (userId: string, targetUserId: string) =>
  defHttp.post<void>({ url: Api.mute, params: { userId, targetUserId } });

/** 取消屏蔽 */
export const unmuteUser = (userId: string, targetUserId: string) =>
  defHttp.post<void>({ url: Api.unmute, params: { userId, targetUserId } });

/** 查询屏蔽列表分页 */
export const getMuteList = (userId: string, pageNo: number, pageSize: number) =>
  defHttp.get<MuteListPageVO>({ url: Api.muteList, params: { userId, pageNo, pageSize } });

/** 屏蔽列表分页 VO */
export interface MuteListPageVO {
  records: MuteListItemVO[];
  total: number;
}

/** 屏蔽列表项 */
export interface MuteListItemVO {
  userId: string;
  nickname: string;
  avatar: string;
  mutedAt: string;
  muteType: string;
  expiresAt?: string;
}
