import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { store } from '/@/store';
import {
  getFollowList,
  getSpecialFollowList,
  getFollowGroupList,
  createFollowGroup,
  renameFollowGroup,
  deleteFollowGroup,
  followUser,
  unfollowUser,
  setSpecialFollow,
  cancelSpecialFollow,
  moveFollowGroup,
  removeFromGroup,
  getRecommendations,
  batchUnfollow,
  batchCancelSpecial,
} from '/@/api/content/relation';

export interface FollowItem {
  id: string;
  userId: string;
  nickname: string;
  avatar: string;
  bio: string;
  followTime: string;
  groupId: string;
  isSpecial: boolean;
  lastActiveTime: string;
  latestActivityHint?: string;
}

export interface FollowGroup {
  id: string;
  name: string;
  sortOrder: number;
  memberCount: number;
  isDefault: boolean;
}

export interface RecommendationItem {
  userId: string;
  nickname: string;
  avatar: string;
  bio?: string;
  reason?: string;
  mutualFollowCount?: number;
}

export const useFollowStore = defineStore('social-follow', () => {
  // ===== State =====
  const followList = ref<FollowItem[]>([]);
  const specialFollowList = ref<FollowItem[]>([]);
  const followGroups = ref<FollowGroup[]>([]);
  const totalFollows = ref(0);
  const totalSpecialFollows = ref(0);
  const followListLoading = ref(false);
  const specialFollowLoading = ref(false);
  const recommendationsLoading = ref(false);
  const currentPage = ref(1);
  const pageSize = ref(20);
  const hasMore = ref(true);
  const specialCurrentPage = ref(1);
  const specialHasMore = ref(true);
  const recommendations = ref<RecommendationItem[]>([]);
  const recommendationsPage = ref(1);
  const recommendationsHasMore = ref(true);
  const searchKeyword = ref('');
  const selectedGroupId = ref('');

  // ===== Computed =====
  const loading = computed(() => followListLoading.value || specialFollowLoading.value);
  const defaultGroup = computed(() => followGroups.value.find((g) => g.isDefault));
  const customGroups = computed(() => followGroups.value.filter((g) => !g.isDefault));

  // ===== Methods =====
  async function fetchFollowList(userId: string, reset = false) {
    if (reset) {
      currentPage.value = 1;
      followList.value = [];
      hasMore.value = true;
    }
    if (!hasMore.value && !reset) return;

    followListLoading.value = true;
    try {
      const res = await getFollowList(userId, {
        keyword: searchKeyword.value || undefined,
        groupId: selectedGroupId.value || undefined,
        pageNo: currentPage.value,
        pageSize: pageSize.value,
      });
      const { records = [], total = 0 } = res;
      if (reset) {
        followList.value = records;
      } else {
        followList.value.push(...records);
      }
      totalFollows.value = total;
      hasMore.value = followList.value.length < total;
      currentPage.value++;
    } finally {
      followListLoading.value = false;
    }
  }

  async function fetchSpecialFollowList(userId: string, reset = false) {
    if (reset) {
      specialCurrentPage.value = 1;
      specialFollowList.value = [];
      specialHasMore.value = true;
    }
    if (!specialHasMore.value && !reset) return;

    specialFollowLoading.value = true;
    try {
      const res = await getSpecialFollowList(userId, {
        pageNo: specialCurrentPage.value,
        pageSize: pageSize.value,
      });
      const { records = [], total = 0 } = res;
      if (reset) {
        specialFollowList.value = records;
      } else {
        specialFollowList.value.push(...records);
      }
      totalSpecialFollows.value = total;
      specialHasMore.value = specialFollowList.value.length < total;
      specialCurrentPage.value++;
    } finally {
      specialFollowLoading.value = false;
    }
  }

  async function fetchFollowGroups(userId: string) {
    try {
      const res = await getFollowGroupList(userId);
      followGroups.value = res || [];
    } catch (error) {
      console.error('[FollowStore] fetchFollowGroups failed:', error);
      throw error;
    }
  }

  async function createGroup(userId: string, name: string, sortOrder?: number) {
    await createFollowGroup(userId, { name, sortOrder });
    await fetchFollowGroups(userId);
  }

  async function updateGroup(userId: string, groupId: string, name: string, _sortOrder?: number) {
    await renameFollowGroup(userId, { groupId, name });
    await fetchFollowGroups(userId);
  }

  async function removeGroup(userId: string, groupId: string) {
    await deleteFollowGroup(userId, groupId);
    await fetchFollowGroups(userId);
  }

  async function follow(userId: string, targetUserId: string, groupId?: string) {
    await followUser(userId, { targetUserId, relationGroupId: groupId });
    await fetchFollowList(userId, true);
  }

  async function unfollow(userId: string, targetUserId: string) {
    await unfollowUser(userId, targetUserId);
    await fetchFollowList(userId, true);
  }

  async function setSpecial(userId: string, targetUserId: string) {
    await setSpecialFollow(userId, targetUserId);
    await fetchFollowList(userId, true);
    await fetchSpecialFollowList(userId, true);
  }

  async function cancelSpecial(userId: string, targetUserId: string) {
    await cancelSpecialFollow(userId, targetUserId);
    await fetchFollowList(userId, true);
    await fetchSpecialFollowList(userId, true);
  }

  function setSearchKeyword(keyword: string) {
    searchKeyword.value = keyword;
  }

  function setSelectedGroupId(groupId: string) {
    selectedGroupId.value = groupId;
  }

  async function moveToGroup(userId: string, targetUserId: string, groupId: string) {
    await moveFollowGroup(userId, { targetUserId, groupId });
    await fetchFollowList(userId, true);
  }

  async function removeUserFromGroup(userId: string, targetUserId: string, groupId: string) {
    await removeFromGroup(userId, { targetUserId, groupId });
    await fetchFollowList(userId, true);
  }

  async function fetchRecommendations(reset = false) {
    if (reset) {
      recommendationsPage.value = 1;
      recommendations.value = [];
      recommendationsHasMore.value = true;
    }
    if (!recommendationsHasMore.value && !reset) return;

    recommendationsLoading.value = true;
    try {
      const res = await getRecommendations({ page: recommendationsPage.value, size: pageSize.value });
      const { records = [], total = 0 } = res;
      if (reset) {
        recommendations.value = records;
      } else {
        recommendations.value.push(...records);
      }
      recommendationsHasMore.value = recommendations.value.length < total;
      recommendationsPage.value++;
    } finally {
      recommendationsLoading.value = false;
    }
  }

  function dismissRecommendation(userId: string) {
    recommendations.value = recommendations.value.filter((r) => r.userId !== userId);
  }

  async function batchUnfollowUsers(userId: string, targetUserIds: string[]) {
    await batchUnfollow(userId, { targetUserIds });
    await fetchFollowList(userId, true);
  }

  async function batchCancelSpecialUsers(userId: string, targetUserIds: string[]) {
    await batchCancelSpecial(userId, { targetUserIds });
    await fetchFollowList(userId, true);
    await fetchSpecialFollowList(userId, true);
  }

  return {
    // State
    followList,
    specialFollowList,
    followGroups,
    totalFollows,
    totalSpecialFollows,
    loading,
    followListLoading,
    specialFollowLoading,
    recommendationsLoading,
    currentPage,
    pageSize,
    hasMore,
    specialCurrentPage,
    specialHasMore,
    recommendations,
    recommendationsPage,
    recommendationsHasMore,
    searchKeyword,
    selectedGroupId,
    // Computed
    defaultGroup,
    customGroups,
    // Methods
    fetchFollowList,
    fetchSpecialFollowList,
    fetchFollowGroups,
    createGroup,
    updateGroup,
    removeGroup,
    follow,
    unfollow,
    setSpecial,
    cancelSpecial,
    moveToGroup,
    removeUserFromGroup,
    fetchRecommendations,
    dismissRecommendation,
    batchUnfollowUsers,
    batchCancelSpecialUsers,
    setSearchKeyword,
    setSelectedGroupId,
  };
});

export function useFollowStoreWithOut() {
  return useFollowStore(store);
}
