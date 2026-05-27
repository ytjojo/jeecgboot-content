import { defHttp } from '/@/utils/http/axios';

enum Api {
  mutualFollowList = '/content/user/relation/mutual-follow-list',
  detail = '/content/user/relation/detail',
  follow = '/content/user/relation/follow',
  unfollow = '/content/user/relation/unfollow',
}

/** 查询互关好友列表 */
export const getMutualFollowList = (userId: string, params?: { keyword?: string; pageNo?: number; pageSize?: number }) =>
  defHttp.get({ url: Api.mutualFollowList, params: { userId, ...params } });

/** 查询与目标用户的关系详情 */
export const getRelationDetail = (userId: string, targetUserId: string) =>
  defHttp.get({ url: Api.detail, params: { userId, targetUserId } });

/** 关注用户 */
export const followUser = (userId: string, data: { targetUserId: string; relationGroupId?: string }) =>
  defHttp.post({ url: Api.follow, params: { userId }, data });

/** 取消关注 */
export const unfollowUser = (userId: string, targetUserId: string) =>
  defHttp.post({ url: Api.unfollow, params: { userId, targetUserId } });
