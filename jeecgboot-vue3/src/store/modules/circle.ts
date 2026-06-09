import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { store } from '/@/store';
import type { CircleVO, MemberRole } from '/@/api/content/model/circleModel';

/** 隐私类型选项 */
export const privacyTypeOptions = [
  { label: '公开', value: 'PUBLIC' },
  { label: '私有', value: 'PRIVATE' },
  { label: '密码保护', value: 'PASSWORD' },
];

/** 加入方式选项 */
export const joinTypeOptions = [
  { label: '直接加入', value: 'DIRECT' },
  { label: '申请审核', value: 'APPROVAL' },
  { label: '邀请加入', value: 'INVITE' },
  { label: '密码加入', value: 'PASSWORD' },
];

export const useCircleStore = defineStore('circle', () => {
  // ===== State =====
  const currentCircle = ref<CircleVO | null>(null);
  const searchKeyword = ref<string>('');

  // ===== Getters =====
  /** 当前用户角色 */
  const currentRole = computed<MemberRole | null>(() => {
    return currentCircle.value?.myRole ?? null;
  });

  /** 当前用户是否为创建者 */
  const isCreator = computed(() => {
    return currentRole.value === 'CREATOR';
  });

  /** 当前用户是否为版主 */
  const isModerator = computed(() => {
    return currentRole.value === 'MODERATOR';
  });

  /** 当前用户是否为普通成员 */
  const isMember = computed(() => {
    return currentRole.value === 'MEMBER';
  });

  /** 当前用户是否已加入 */
  const isJoined = computed(() => {
    return currentCircle.value?.joined ?? false;
  });

  /** 是否可以管理成员（创建者或版主） */
  const canManageMember = computed(() => {
    return isCreator.value || isModerator.value;
  });

  /** 是否可以管理角色（仅创建者） */
  const canManageRole = computed(() => {
    return isCreator.value;
  });

  // ===== Actions =====
  function setCurrentCircle(circle: CircleVO | null) {
    currentCircle.value = circle;
  }

  function clearCurrentCircle() {
    currentCircle.value = null;
  }

  function setSearchKeyword(keyword: string) {
    searchKeyword.value = keyword;
  }

  /** 刷新圈子详情（合并更新） */
  function updateCurrentCircle(partial: Partial<CircleVO>) {
    if (currentCircle.value) {
      Object.assign(currentCircle.value, partial);
    }
  }

  /**
   * 判断是否可以禁言目标成员
   * - 创建者：可禁言所有成员和版主
   * - 版主：仅可禁言普通成员
   */
  function canMute(targetRole: MemberRole | null | undefined): boolean {
    if (!canManageMember.value) return false;
    if (isCreator.value) return true;
    // 版主只能禁言普通成员
    if (isModerator.value && targetRole === 'MEMBER') return true;
    return false;
  }

  /**
   * 判断是否可以移除目标成员
   * - 创建者：可移除所有成员和版主
   * - 版主：仅可移除普通成员
   */
  function canRemove(targetRole: MemberRole | null | undefined): boolean {
    if (!canManageMember.value) return false;
    if (isCreator.value) return true;
    // 版主只能移除普通成员
    if (isModerator.value && targetRole === 'MEMBER') return true;
    return false;
  }

  return {
    // State
    currentCircle,
    searchKeyword,
    // Getters
    currentRole,
    isCreator,
    isModerator,
    isMember,
    isJoined,
    canManageMember,
    canManageRole,
    // Actions
    setCurrentCircle,
    clearCurrentCircle,
    setSearchKeyword,
    updateCurrentCircle,
    canMute,
    canRemove,
  };
});

// Support use outside of setup
export function useCircleStoreWithOut() {
  return useCircleStore(store);
}
