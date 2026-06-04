import { ref, reactive } from 'vue';
import {
  followUser,
  unfollowUser,
  setSpecialFollow,
  cancelSpecialFollow,
  getFollowGroupList,
  createFollowGroup,
  renameFollowGroup,
  deleteFollowGroup,
  moveFollowGroup,
  removeFromGroup,
  getFollowList,
  getSpecialFollowList,
  getRecommendations,
  batchUnfollow,
  batchCancelSpecial,
} from '/@/api/content/relation';
import { useUserStore } from '/@/store/modules/user';

/**
 * 关注模块 composable
 * 封装关注相关 API 操作，提供响应式状态
 */
export function useFollow() {
  const userStore = useUserStore();
  const userId = userStore.getUserInfo?.userId ?? '';

  const loading = ref(false);
  const followList = ref<any[]>([]);
  const specialFollowList = ref<any[]>([]);
  const groupList = ref<any[]>([]);
  const recommendations = ref<any[]>([]);
  const pagination = reactive({ pageNo: 1, pageSize: 20, total: 0 });

  /** 关注用户 */
  async function follow(targetUserId: string, relationGroupId?: string) {
    return followUser(userId, { targetUserId, relationGroupId });
  }

  /** 取消关注 */
  async function unfollow(targetUserId: string) {
    return unfollowUser(userId, targetUserId);
  }

  /** 设为特别关注 */
  async function setSpecial(targetUserId: string) {
    return setSpecialFollow(userId, targetUserId);
  }

  /** 取消特别关注 */
  async function cancelSpecial(targetUserId: string) {
    return cancelSpecialFollow(userId, targetUserId);
  }

  /** 加载关注列表 */
  async function loadFollowList(params?: { keyword?: string; groupId?: string; pageNo?: number; pageSize?: number }) {
    loading.value = true;
    try {
      const res = await getFollowList(userId, params);
      followList.value = res?.records ?? res ?? [];
      pagination.total = res?.total ?? 0;
      if (params?.pageNo) pagination.pageNo = params.pageNo;
      if (params?.pageSize) pagination.pageSize = params.pageSize;
      return res;
    } finally {
      loading.value = false;
    }
  }

  /** 加载特别关注列表 */
  async function loadSpecialFollowList(params?: { pageNo?: number; pageSize?: number }) {
    loading.value = true;
    try {
      const res = await getSpecialFollowList(userId, params);
      specialFollowList.value = res?.records ?? res ?? [];
      return res;
    } finally {
      loading.value = false;
    }
  }

  /** 加载分组列表 */
  async function loadGroups() {
    const res = await getFollowGroupList(userId);
    groupList.value = res ?? [];
    return res;
  }

  /** 创建分组 */
  async function addGroup(name: string, sortOrder?: number) {
    const res = await createFollowGroup(userId, { name, sortOrder });
    await loadGroups();
    return res;
  }

  /** 重命名分组 */
  async function renameGroup(groupId: string, name: string) {
    const res = await renameFollowGroup(userId, { groupId, name });
    await loadGroups();
    return res;
  }

  /** 删除分组 */
  async function removeGroup(groupId: string) {
    const res = await deleteFollowGroup(userId, groupId);
    await loadGroups();
    return res;
  }

  /** 移动关注到分组 */
  async function moveToGroup(targetUserId: string, groupId: string) {
    return moveFollowGroup(userId, { targetUserId, groupId });
  }

  /** 从分组移除 */
  async function removeFromGroupAction(targetUserId: string, groupId: string) {
    return removeFromGroup(userId, { targetUserId, groupId });
  }

  /** 加载推荐关注 */
  async function loadRecommendations(params?: { page?: number; size?: number }) {
    loading.value = true;
    try {
      const res = await getRecommendations(params);
      recommendations.value = res?.records ?? res ?? [];
      return res;
    } finally {
      loading.value = false;
    }
  }

  /** 批量取消关注 */
  async function batchUnfollowAction(targetUserIds: string[]) {
    return batchUnfollow(userId, { targetUserIds });
  }

  /** 批量取消特别关注 */
  async function batchCancelSpecialAction(targetUserIds: string[]) {
    return batchCancelSpecial(userId, { targetUserIds });
  }

  return {
    loading,
    followList,
    specialFollowList,
    groupList,
    recommendations,
    pagination,
    follow,
    unfollow,
    setSpecial,
    cancelSpecial,
    loadFollowList,
    loadSpecialFollowList,
    loadGroups,
    addGroup,
    renameGroup,
    removeGroup,
    moveToGroup,
    removeFromGroup: removeFromGroupAction,
    loadRecommendations,
    batchUnfollow: batchUnfollowAction,
    batchCancelSpecial: batchCancelSpecialAction,
  };
}
