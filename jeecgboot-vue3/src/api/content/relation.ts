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

/** 设为特别关注 */
export const setSpecialFollow = (userId: string, targetUserId: string) =>
  defHttp.post({ url: '/content/user/relation/special-follow', params: { userId, targetUserId } });

/** 取消特别关注 */
export const cancelSpecialFollow = (userId: string, targetUserId: string) =>
  defHttp.post({ url: '/content/user/relation/special-follow/cancel', params: { userId, targetUserId } });

/** 获取关注分组列表 */
export const getFollowGroupList = (userId: string) =>
  defHttp.get({ url: '/content/user/relation/groups', params: { userId } });

/** 创建关注分组 */
export const createFollowGroup = (userId: string, data: { name: string; sortOrder?: number }) =>
  defHttp.post({ url: '/content/user/relation/group/create', params: { userId }, data });

/** 重命名关注分组 */
export const renameFollowGroup = (userId: string, data: { groupId: string; name: string }) =>
  defHttp.post({ url: '/content/user/relation/group/rename', params: { userId }, data });

/** 删除关注分组 */
export const deleteFollowGroup = (userId: string, groupId: string) =>
  defHttp.post({ url: '/content/user/relation/group/delete', params: { userId, groupId } });

/** 移动关注到分组 */
export const moveFollowGroup = (userId: string, data: { targetUserId: string; groupId: string }) =>
  defHttp.post({ url: '/content/user/relation/group/move', params: { userId }, data });

/** 从分组移除 */
export const removeFromGroup = (userId: string, data: { targetUserId: string; groupId: string }) =>
  defHttp.post({ url: '/content/user/relation/group/remove', params: { userId }, data });

/** 获取关注列表 */
export const getFollowList = (userId: string, params?: { keyword?: string; groupId?: string; pageNo?: number; pageSize?: number }) =>
  defHttp.get({ url: '/content/user/relation/follow-list', params: { userId, ...params } });

/** 获取特别关注列表 */
export const getSpecialFollowList = (userId: string, params?: { pageNo?: number; pageSize?: number }) =>
  defHttp.get({ url: '/content/user/relation/special-follow-list', params: { userId, ...params } });

/** 获取推荐关注 */
export const getRecommendations = (params?: { page?: number; size?: number }) =>
  defHttp.get({ url: '/content/user/relation/recommendations', params });

/** 批量取消关注 */
export const batchUnfollow = (userId: string, data: { targetUserIds: string[] }) =>
  defHttp.post({ url: '/content/user/relation/batch/unfollow', params: { userId }, data });

/** 批量取消特别关注 */
export const batchCancelSpecial = (userId: string, data: { targetUserIds: string[] }) =>
  defHttp.post({ url: '/content/user/relation/batch/special-follow/cancel', params: { userId }, data });

/** 获取关注动态 Feed */
export const getFollowingFeed = (params: { page?: number; size?: number; types?: string }) =>
  defHttp.get({ url: '/content/user/relation/feed', params });
