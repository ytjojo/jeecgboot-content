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
}

export interface FollowGroup {
  id: string;
  name: string;
  sortOrder: number;
  memberCount: number;
  isDefault: boolean;
}

export const useFollowStore = defineStore('social-follow', () => {
  // ===== State =====
  const followList = ref<FollowItem[]>([]);
  const specialFollowList = ref<FollowItem[]>([]);
  const followGroups = ref<FollowGroup[]>([]);
  const totalFollows = ref(0);
  const totalSpecialFollows = ref(0);
  const loading = ref(false);
  const currentPage = ref(1);
  const pageSize = ref(20);
  const hasMore = ref(true);
  const searchKeyword = ref('');
  const selectedGroupId = ref('');

  // ===== Computed =====
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

    loading.value = true;
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
      loading.value = false;
    }
  }

  async function fetchSpecialFollowList(userId: string, reset = false) {
    if (reset) {
      currentPage.value = 1;
      specialFollowList.value = [];
      hasMore.value = true;
    }
    if (!hasMore.value && !reset) return;

    loading.value = true;
    try {
      const res = await getSpecialFollowList(userId, {
        pageNo: currentPage.value,
        pageSize: pageSize.value,
      });
      const { records = [], total = 0 } = res;
      if (reset) {
        specialFollowList.value = records;
      } else {
        specialFollowList.value.push(...records);
      }
      totalSpecialFollows.value = total;
      hasMore.value = specialFollowList.value.length < total;
      currentPage.value++;
    } finally {
      loading.value = false;
    }
  }

  async function fetchFollowGroups(userId: string) {
    const res = await getFollowGroupList(userId);
    followGroups.value = res || [];
  }

  async function createGroup(userId: string, name: string, sortOrder?: number) {
    await createFollowGroup(userId, { name, sortOrder });
    await fetchFollowGroups(userId);
  }

  async function updateGroup(userId: string, groupId: string, name: string, sortOrder?: number) {
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

  return {
    // State
    followList,
    specialFollowList,
    followGroups,
    totalFollows,
    totalSpecialFollows,
    loading,
    currentPage,
    pageSize,
    hasMore,
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
    setSearchKeyword,
    setSelectedGroupId,
  };
});

export function useFollowStoreWithOut() {
  return useFollowStore(store);
}
