import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { store } from '/@/store';
import {
  getCircleLevelInfo,
  getMemberGrowth,
  getAchievements,
  getLeaderboard,
  getParticipationDays,
  type CircleLevelVO,
  type MemberGrowthVO,
  type AchievementVO,
  type LeaderboardResponse,
} from '/@/api/content/circle/growth';

/**
 * 圈子成长缓存数据
 */
interface CircleGrowthCache {
  /** 圈子等级信息 */
  circleLevel: CircleLevelVO | null;
  /** 成员成长信息（按 userId 缓存） */
  memberGrowth: Record<string, MemberGrowthVO>;
  /** 成就徽章列表（按 userId 缓存） */
  achievements: Record<string, AchievementVO[]>;
  /** 连续参与天数（按 userId 缓存） */
  participationDays: Record<string, { days: number; streakDetail?: boolean[] }>;
  /** 排行榜（按 dimension_period 缓存） */
  leaderboard: Record<string, LeaderboardResponse>;
  /** 最后加载时间 */
  lastLoaded: {
    circleLevel?: number;
    memberGrowth: Record<string, number>;
    achievements: Record<string, number>;
    participationDays: Record<string, number>;
    leaderboard: Record<string, number>;
  };
}

/**
 * 圈子成长 Store 状态结构
 */
interface CircleGrowthState {
  /** 按 circleId 缓存的数据 */
  cache: Record<string, CircleGrowthCache>;
  /** 加载状态 */
  loading: {
    circleLevel: Record<string, boolean>;
    memberGrowth: Record<string, boolean>;
    achievements: Record<string, boolean>;
    participationDays: Record<string, boolean>;
    leaderboard: Record<string, boolean>;
  };
}

/** 缓存过期时间（5分钟） */
const CACHE_TTL = 5 * 60 * 1000;

/**
 * 生成排行榜缓存键
 */
function getLeaderboardKey(dimension: string, period: string): string {
  return `${dimension}_${period}`;
}

/**
 * 检查缓存是否有效
 */
function isCacheValid(timestamp?: number): boolean {
  if (!timestamp) return false;
  return Date.now() - timestamp < CACHE_TTL;
}

export const useCircleGrowthStore = defineStore('circleGrowth', () => {
  // ===== State =====
  const cache = ref<Record<string, CircleGrowthCache>>({});
  const loading = ref<CircleGrowthState['loading']>({
    circleLevel: {},
    memberGrowth: {},
    achievements: {},
    participationDays: {},
    leaderboard: {},
  });

  // ===== Getters =====

  /**
   * 获取指定圈子的等级信息
   */
  const getCircleLevel = computed(() => {
    return (circleId: string): CircleLevelVO | null => {
      return cache.value[circleId]?.circleLevel ?? null;
    };
  });

  /**
   * 获取指定用户在指定圈子的成长信息
   */
  const getMemberGrowthForUser = computed(() => {
    return (circleId: string, userId: string): MemberGrowthVO | null => {
      return cache.value[circleId]?.memberGrowth[userId] ?? null;
    };
  });

  /**
   * 获取指定用户在指定圈子的徽章列表
   */
  const getAchievementsForUser = computed(() => {
    return (circleId: string, userId: string): AchievementVO[] => {
      return cache.value[circleId]?.achievements[userId] ?? [];
    };
  });

  /**
   * 获取指定排行榜数据
   */
  const getLeaderboardData = computed(() => {
    return (circleId: string, dimension: string, period: string): LeaderboardResponse | null => {
      const key = getLeaderboardKey(dimension, period);
      return cache.value[circleId]?.leaderboard[key] ?? null;
    };
  });

  // ===== 内部工具方法 =====

  /**
   * 初始化圈子缓存结构
   */
  function ensureCircleCache(circleId: string) {
    if (!cache.value[circleId]) {
      cache.value[circleId] = {
        circleLevel: null,
        memberGrowth: {},
        achievements: {},
        participationDays: {},
        leaderboard: {},
        lastLoaded: {
          memberGrowth: {},
          achievements: {},
          participationDays: {},
          leaderboard: {},
        },
      };
    }
  }

  // ===== Actions =====

  /**
   * 获取圈子等级信息
   * @param circleId 圈子ID
   * @param forceRefresh 是否强制刷新
   */
  async function fetchCircleLevel(circleId: string, forceRefresh = false) {
    ensureCircleCache(circleId);

    const cached = cache.value[circleId];
    if (!forceRefresh && cached.circleLevel && isCacheValid(cached.lastLoaded.circleLevel)) {
      return cached.circleLevel;
    }

    if (loading.value.circleLevel[circleId]) {
      return cached.circleLevel;
    }

    loading.value.circleLevel[circleId] = true;
    try {
      const data = await getCircleLevelInfo(circleId);
      cached.circleLevel = data;
      cached.lastLoaded.circleLevel = Date.now();
      return data;
    } finally {
      loading.value.circleLevel[circleId] = false;
    }
  }

  /**
   * 获取成员成长信息
   * @param circleId 圈子ID
   * @param userId 用户ID
   * @param forceRefresh 是否强制刷新
   */
  async function fetchMemberGrowth(circleId: string, userId: string, forceRefresh = false) {
    ensureCircleCache(circleId);

    const cached = cache.value[circleId];
    if (
      !forceRefresh &&
      cached.memberGrowth[userId] &&
      isCacheValid(cached.lastLoaded.memberGrowth[userId])
    ) {
      return cached.memberGrowth[userId];
    }

    const loadKey = `${circleId}_${userId}`;
    if (loading.value.memberGrowth[loadKey]) {
      return cached.memberGrowth[userId];
    }

    loading.value.memberGrowth[loadKey] = true;
    try {
      const data = await getMemberGrowth(circleId, userId);
      cached.memberGrowth[userId] = data;
      cached.lastLoaded.memberGrowth[userId] = Date.now();
      return data;
    } finally {
      loading.value.memberGrowth[loadKey] = false;
    }
  }

  /**
   * 获取成就徽章列表
   * @param circleId 圈子ID
   * @param userId 用户ID
   * @param forceRefresh 是否强制刷新
   */
  async function fetchAchievements(circleId: string, userId: string, forceRefresh = false) {
    ensureCircleCache(circleId);

    const cached = cache.value[circleId];
    if (
      !forceRefresh &&
      cached.achievements[userId] &&
      isCacheValid(cached.lastLoaded.achievements[userId])
    ) {
      return cached.achievements[userId];
    }

    const loadKey = `${circleId}_${userId}`;
    if (loading.value.achievements[loadKey]) {
      return cached.achievements[userId];
    }

    loading.value.achievements[loadKey] = true;
    try {
      const data = await getAchievements(circleId, userId);
      cached.achievements[userId] = data;
      cached.lastLoaded.achievements[userId] = Date.now();
      return data;
    } finally {
      loading.value.achievements[loadKey] = false;
    }
  }

  /**
   * 获取连续参与天数
   * @param circleId 圈子ID
   * @param userId 用户ID
   * @param forceRefresh 是否强制刷新
   */
  async function fetchParticipationDays(circleId: string, userId: string, forceRefresh = false) {
    ensureCircleCache(circleId);

    const cached = cache.value[circleId];
    if (
      !forceRefresh &&
      cached.participationDays[userId] &&
      isCacheValid(cached.lastLoaded.participationDays[userId])
    ) {
      return cached.participationDays[userId];
    }

    const loadKey = `${circleId}_${userId}`;
    if (loading.value.participationDays[loadKey]) {
      return cached.participationDays[userId];
    }

    loading.value.participationDays[loadKey] = true;
    try {
      const data = await getParticipationDays(circleId, userId);
      const result = {
        days: data.participationDays,
        streakDetail: data.streakDetail,
      };
      cached.participationDays[userId] = result;
      cached.lastLoaded.participationDays[userId] = Date.now();
      return result;
    } finally {
      loading.value.participationDays[loadKey] = false;
    }
  }

  /**
   * 获取排行榜
   * @param circleId 圈子ID
   * @param dimension 维度（exp/contribution/post）
   * @param period 周期（week/month/all）
   * @param currentUserId 当前用户ID
   * @param forceRefresh 是否强制刷新
   */
  async function fetchLeaderboard(
    circleId: string,
    dimension: string,
    period: string,
    currentUserId: string,
    forceRefresh = false,
  ) {
    ensureCircleCache(circleId);

    const key = getLeaderboardKey(dimension, period);
    const cached = cache.value[circleId];
    if (!forceRefresh && cached.leaderboard[key] && isCacheValid(cached.lastLoaded.leaderboard[key])) {
      return cached.leaderboard[key];
    }

    const loadKey = `${circleId}_${key}`;
    if (loading.value.leaderboard[loadKey]) {
      return cached.leaderboard[key];
    }

    loading.value.leaderboard[loadKey] = true;
    try {
      const data = await getLeaderboard({ circleId, dimension, period, currentUserId });
      cached.leaderboard[key] = data;
      cached.lastLoaded.leaderboard[key] = Date.now();
      return data;
    } finally {
      loading.value.leaderboard[loadKey] = false;
    }
  }

  /**
   * 清除指定圈子的缓存
   * @param circleId 圈子ID
   */
  function clearCache(circleId?: string) {
    if (circleId) {
      delete cache.value[circleId];
      delete loading.value.circleLevel[circleId];
      // 清理相关加载状态
      Object.keys(loading.value.memberGrowth).forEach((key) => {
        if (key.startsWith(`${circleId}_`)) {
          delete loading.value.memberGrowth[key];
          delete loading.value.achievements[key];
          delete loading.value.participationDays[key];
        }
      });
      Object.keys(loading.value.leaderboard).forEach((key) => {
        if (key.startsWith(`${circleId}_`)) {
          delete loading.value.leaderboard[key];
        }
      });
    } else {
      cache.value = {};
      loading.value = {
        circleLevel: {},
        memberGrowth: {},
        achievements: {},
        participationDays: {},
        leaderboard: {},
      };
    }
  }

  /**
   * 刷新指定圈子的所有数据
   * @param circleId 圈子ID
   */
  function refreshCircle(circleId: string) {
    clearCache(circleId);
  }

  return {
    // State
    cache,
    loading,
    // Getters
    getCircleLevel,
    getMemberGrowthForUser,
    getAchievementsForUser,
    getLeaderboardData,
    // Actions
    fetchCircleLevel,
    fetchMemberGrowth,
    fetchAchievements,
    fetchParticipationDays,
    fetchLeaderboard,
    clearCache,
    refreshCircle,
  };
});

// Support use outside of setup
export function useCircleGrowthStoreWithOut() {
  return useCircleGrowthStore(store);
}
