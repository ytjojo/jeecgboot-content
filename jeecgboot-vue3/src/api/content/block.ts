import { defHttp } from '/@/utils/http/axios';

enum Api {
  block = '/api/v1/content/user/relation/block',
  unblock = '/api/v1/content/user/relation/unblock',
  blacklist = '/api/v1/content/user/relation/blacklist',
  detail = '/api/v1/content/user/relation/detail',
  help = '/api/v1/content/user/relation/block-mute/help',
}

/** 拉黑用户 */
export const blockUser = (userId: string, targetUserId: string) =>
  defHttp.post<void>({ url: Api.block, params: { userId, targetUserId } });

/** 解除拉黑 */
export const unblockUser = (userId: string, targetUserId: string) =>
  defHttp.post<void>({ url: Api.unblock, params: { userId, targetUserId } });

/** 查询黑名单分页 */
export const getBlacklist = (userId: string, pageNo: number, pageSize: number) =>
  defHttp.get<BlacklistPageVO>({ url: Api.blacklist, params: { userId, pageNo, pageSize } });

/** 查询与目标用户的关系详情 */
export const checkRelation = (userId: string, targetUserId: string) =>
  defHttp.get<RelationDetailVO>({ url: Api.detail, params: { userId, targetUserId } });

/** 获取拉黑与屏蔽帮助说明 */
export const getBlockMuteHelp = () =>
  defHttp.get<BlockMuteHelpVO>({ url: Api.help });

/** 黑名单分页 VO */
export interface BlacklistPageVO {
  records: BlacklistItemVO[];
  total: number;
}

/** 黑名单列表项 */
export interface BlacklistItemVO {
  userId: string;
  nickname: string;
  avatar: string;
  blockedAt: string;
}

/** 关系详情 VO */
export interface RelationDetailVO {
  isBlocked: boolean;
  isMuted: boolean;
  isBlockedBy: boolean;
}

/** 帮助说明 VO */
export interface BlockMuteHelpVO {
  blockDescription: string;
  muteDescription: string;
  differences: { aspect: string; block: string; mute: string }[];
}
