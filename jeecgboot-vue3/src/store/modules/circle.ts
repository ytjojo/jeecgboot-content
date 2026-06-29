import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { store } from '/@/store';
import { getCircleDetail } from '/@/api/content/circle';
import type { CircleVO, MemberRole } from '/@/api/content/model/circleModel';

const CACHE_TTL_MS = 5 * 60 * 1000;

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
  const lastFetchTime = ref<number>(0);
  const lastFetchCircleId = ref<string | null>(null);

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
    if (circle) {
      lastFetchTime.value = Date.now();
      lastFetchCircleId.value = circle.id;
    } else {
      lastFetchTime.value = 0;
      lastFetchCircleId.value = null;
    }
  }

  function clearCurrentCircle() {
    currentCircle.value = null;
    lastFetchTime.value = 0;
    lastFetchCircleId.value = null;
  }

  function clearCache() {
    lastFetchTime.value = 0;
    lastFetchCircleId.value = null;
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
   * 获取圈子详情（带5分钟缓存）
   * - 相同circleId且5分钟内：直接返回缓存
   * - 超过5分钟或ID不同：重新请求
   * - force=true：强制刷新
   */
  async function fetchCircleDetail(circleId: string, force = false): Promise<CircleVO> {
    const now = Date.now();
    const isCacheValid =
      !force &&
      currentCircle.value &&
      lastFetchCircleId.value === circleId &&
      now - lastFetchTime.value < CACHE_TTL_MS;

    if (isCacheValid) {
      return currentCircle.value!;
    }

    const circle = await getCircleDetail(circleId);
    setCurrentCircle(circle);
    return circle;
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
    lastFetchTime,
    lastFetchCircleId,
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
    clearCache,
    setSearchKeyword,
    updateCurrentCircle,
    fetchCircleDetail,
    canMute,
    canRemove,
  };
});

// Support use outside of setup
export function useCircleStoreWithOut() {
  return useCircleStore(store);
}
