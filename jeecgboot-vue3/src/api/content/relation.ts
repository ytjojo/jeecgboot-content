import { defHttp } from '/@/utils/http/axios';

enum Api {
  mutualStatus = '/content/user/relation/mutual-status',
  mutualFollowList = '/content/user/relation/mutual-follow-list',
  detail = '/content/user/relation/detail',
  follow = '/content/user/relation/follow',
  unfollow = '/content/user/relation/unfollow',
  specialFollow = '/content/user/relation/special-follow',
  specialFollowCancel = '/content/user/relation/special-follow/cancel',
  groups = '/content/user/relation/groups',
  groupCreate = '/content/user/relation/group/create',
  groupRename = '/content/user/relation/group/rename',
  groupDelete = '/content/user/relation/group/delete',
  groupMove = '/content/user/relation/group/move',
  groupRemove = '/content/user/relation/group/remove',
  followList = '/content/user/relation/follow-list',
  specialFollowList = '/content/user/relation/special-follow-list',
  recommendations = '/content/user/relation/recommendations',
  batchUnfollow = '/content/user/relation/batch/unfollow',
  batchSpecialFollowCancel = '/content/user/relation/batch/special-follow/cancel',
  feed = '/content/user/relation/feed',
}

/** 查询互关好友列表 */
export const getMutualFollowList = (userId: string, params?: { keyword?: string; pageNo?: number; pageSize?: number }) =>
  defHttp.get({ url: Api.mutualFollowList, params: { userId, ...params } });

/** 批量查询互关状态 */
export const getMutualStatus = (userIds: string[]) =>
  defHttp.get<Record<string, boolean>>({ url: Api.mutualStatus, params: { userIds: userIds.join(',') } });

/** 查询与目标用户的关系详情 */
export const getRelationDetail = (userId: string, targetUserId: string) =>
  defHttp.get({ url: Api.detail, params: { userId, targetUserId } });

/** 关注用户 */
export const followUser = (userId: string, data: { targetUserId: string; relationGroupId?: string }) =>
  defHttp.post({ url: Api.follow, params: { userId }, data });

/** 取消关注 */
export const unfollowUser = (userId: string, targetUserId: string) =>
  defHttp.post({ url: Api.unfollow, params: { userId, targetUserId } });

/** 设为特别关注 */
export const setSpecialFollow = (userId: string, targetUserId: string) =>
  defHttp.post({ url: Api.specialFollow, params: { userId, targetUserId } });

/** 取消特别关注 */
export const cancelSpecialFollow = (userId: string, targetUserId: string) =>
  defHttp.post({ url: Api.specialFollowCancel, params: { userId, targetUserId } });

/** 获取关注分组列表 */
export const getFollowGroupList = (userId: string) =>
  defHttp.get({ url: Api.groups, params: { userId } });

/** 创建关注分组 */
export const createFollowGroup = (userId: string, data: { name: string; sortOrder?: number }) =>
  defHttp.post({ url: Api.groupCreate, params: { userId }, data });

/** 重命名关注分组 */
export const renameFollowGroup = (userId: string, data: { groupId: string; name: string }) =>
  defHttp.post({ url: Api.groupRename, params: { userId }, data });

/** 删除关注分组 */
export const deleteFollowGroup = (userId: string, groupId: string) =>
  defHttp.post({ url: Api.groupDelete, params: { userId, groupId } });

/** 移动关注到分组 */
export const moveFollowGroup = (userId: string, data: { targetUserId: string; groupId: string }) =>
  defHttp.post({ url: Api.groupMove, params: { userId }, data });

/** 从分组移除 */
export const removeFromGroup = (userId: string, data: { targetUserId: string; groupId: string }) =>
  defHttp.post({ url: Api.groupRemove, params: { userId }, data });

/** 获取关注列表 */
export const getFollowList = (userId: string, params?: { keyword?: string; groupId?: string; pageNo?: number; pageSize?: number }) =>
  defHttp.get({ url: Api.followList, params: { userId, ...params } });

/** 获取特别关注列表 */
export const getSpecialFollowList = (userId: string, params?: { pageNo?: number; pageSize?: number }) =>
  defHttp.get({ url: Api.specialFollowList, params: { userId, ...params } });

/** 获取推荐关注 */
export const getRecommendations = (params?: { page?: number; size?: number }) =>
  defHttp.get({ url: Api.recommendations, params });

/** 批量取消关注 */
export const batchUnfollow = (userId: string, data: { targetUserIds: string[] }) =>
  defHttp.post({ url: Api.batchUnfollow, params: { userId }, data });

/** 批量取消特别关注 */
export const batchCancelSpecial = (userId: string, data: { targetUserIds: string[] }) =>
  defHttp.post({ url: Api.batchSpecialFollowCancel, params: { userId }, data });

/** 获取关注动态 Feed */
export const getFollowingFeed = (params: { page?: number; size?: number; types?: string }) =>
  defHttp.get({ url: Api.feed, params });
