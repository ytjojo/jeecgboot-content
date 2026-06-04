import { ref, reactive, computed } from 'vue';
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

/** 关注用户条目 */
export interface FollowItem {
  targetUserId: string;
  nickname?: string;
  avatar?: string;
  [key: string]: any;
}

/** 关注分组 */
export interface FollowGroup {
  groupId: string;
  name: string;
  sortOrder?: number;
  [key: string]: any;
}

/** 推荐关注条目 */
export interface RecommendItem {
  userId: string;
  nickname?: string;
  avatar?: string;
  [key: string]: any;
}

/**
 * 关注模块 composable
 * 封装关注相关 API 操作，提供响应式状态
 */
export function useFollow() {
  const userStore = useUserStore();
  const userId = computed(() => userStore.getUserInfo?.userId ?? '');

  const loading = ref(false);
  const error = ref<Error | null>(null);
  const followList = ref<FollowItem[]>([]);
  const specialFollowList = ref<FollowItem[]>([]);
  const groupList = ref<FollowGroup[]>([]);
  const recommendations = ref<RecommendItem[]>([]);
  const pagination = reactive({ pageNo: 1, pageSize: 20, total: 0 });

  /** 关注用户 */
  async function follow(targetUserId: string, relationGroupId?: string) {
    error.value = null;
    try {
      return await followUser(userId.value, { targetUserId, relationGroupId });
    } catch (e: any) {
      error.value = e;
      throw e;
    }
  }

  /** 取消关注 */
  async function unfollow(targetUserId: string) {
    error.value = null;
    try {
      return await unfollowUser(userId.value, targetUserId);
    } catch (e: any) {
      error.value = e;
      throw e;
    }
  }

  /** 设为特别关注 */
  async function setSpecial(targetUserId: string) {
    error.value = null;
    try {
      return await setSpecialFollow(userId.value, targetUserId);
    } catch (e: any) {
      error.value = e;
      throw e;
    }
  }

  /** 取消特别关注 */
  async function cancelSpecial(targetUserId: string) {
    error.value = null;
    try {
      return await cancelSpecialFollow(userId.value, targetUserId);
    } catch (e: any) {
      error.value = e;
      throw e;
    }
  }

  /** 加载关注列表 */
  async function loadFollowList(params?: { keyword?: string; groupId?: string; pageNo?: number; pageSize?: number }) {
    loading.value = true;
    error.value = null;
    try {
      const res = await getFollowList(userId.value, params);
      followList.value = res?.records ?? res ?? [];
      pagination.total = res?.total ?? 0;
      if (params?.pageNo) pagination.pageNo = params.pageNo;
      if (params?.pageSize) pagination.pageSize = params.pageSize;
      return res;
    } catch (e: any) {
      error.value = e;
      throw e;
    } finally {
      loading.value = false;
    }
  }

  /** 加载特别关注列表 */
  async function loadSpecialFollowList(params?: { pageNo?: number; pageSize?: number }) {
    loading.value = true;
    error.value = null;
    try {
      const res = await getSpecialFollowList(userId.value, params);
      specialFollowList.value = res?.records ?? res ?? [];
      return res;
    } catch (e: any) {
      error.value = e;
      throw e;
    } finally {
      loading.value = false;
    }
  }

  /** 加载分组列表 */
  async function loadGroups() {
    const res = await getFollowGroupList(userId.value);
    groupList.value = res ?? [];
    return res;
  }

  /** 创建分组 */
  async function addGroup(name: string, sortOrder?: number) {
    error.value = null;
    try {
      const res = await createFollowGroup(userId.value, { name, sortOrder });
      await loadGroups();
      return res;
    } catch (e: any) {
      error.value = e;
      throw e;
    }
  }

  /** 重命名分组 */
  async function renameGroup(groupId: string, name: string) {
    error.value = null;
    try {
      const res = await renameFollowGroup(userId.value, { groupId, name });
      await loadGroups();
      return res;
    } catch (e: any) {
      error.value = e;
      throw e;
    }
  }

  /** 删除分组 */
  async function removeGroup(groupId: string) {
    error.value = null;
    try {
      const res = await deleteFollowGroup(userId.value, groupId);
      await loadGroups();
      return res;
    } catch (e: any) {
      error.value = e;
      throw e;
    }
  }

  /** 移动关注到分组 */
  async function moveToGroup(targetUserId: string, groupId: string) {
    error.value = null;
    try {
      return await moveFollowGroup(userId.value, { targetUserId, groupId });
    } catch (e: any) {
      error.value = e;
      throw e;
    }
  }

  /** 从分组移除 */
  async function removeFromGroupAction(targetUserId: string, groupId: string) {
    error.value = null;
    try {
      return await removeFromGroup(userId.value, { targetUserId, groupId });
    } catch (e: any) {
      error.value = e;
      throw e;
    }
  }

  /** 加载推荐关注 */
  async function loadRecommendations(params?: { page?: number; size?: number }) {
    loading.value = true;
    error.value = null;
    try {
      const res = await getRecommendations(params);
      recommendations.value = res?.records ?? res ?? [];
      return res;
    } catch (e: any) {
      error.value = e;
      throw e;
    } finally {
      loading.value = false;
    }
  }

  /** 批量取消关注 */
  async function batchUnfollowAction(targetUserIds: string[]) {
    error.value = null;
    try {
      return await batchUnfollow(userId.value, { targetUserIds });
    } catch (e: any) {
      error.value = e;
      throw e;
    }
  }

  /** 批量取消特别关注 */
  async function batchCancelSpecialAction(targetUserIds: string[]) {
    error.value = null;
    try {
      return await batchCancelSpecial(userId.value, { targetUserIds });
    } catch (e: any) {
      error.value = e;
      throw e;
    }
  }

  return {
    loading,
    error,
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
