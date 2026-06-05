import { defineStore } from 'pinia';
import { store } from '/@/store';
import { checkRelation, blockUser as apiBlock, unblockUser as apiUnblock, getBlacklist } from '/@/api/content/block';
import { muteUser as apiMute, unmuteUser as apiUnmute, getMuteList } from '/@/api/content/mute';
import type { RelationDetailVO } from '/@/api/content/block';

const CACHE_TTL = 5 * 60 * 1000; // 5 分钟

interface RelationCacheEntry {
  data: RelationDetailVO;
  cachedAt: number;
}

interface BlockMuteState {
  /** 关系缓存: targetUserId -> cache entry */
  relationCache: Record<string, RelationCacheEntry>;
  /** 黑名单总数 */
  blacklistCount: number;
  /** 屏蔽列表总数 */
  muteListCount: number;
}

export const useBlockMuteStore = defineStore({
  id: 'app-block-mute',
  state: (): BlockMuteState => ({
    relationCache: {},
    blacklistCount: 0,
    muteListCount: 0,
  }),
  getters: {
    /** 获取缓存的关系状态 */
    getRelation(): (targetUserId: string) => RelationDetailVO | null {
      return (targetUserId: string) => {
        const entry = this.relationCache[targetUserId];
        if (!entry) return null;
        if (Date.now() - entry.cachedAt > CACHE_TTL) return null;
        return entry.data;
      };
    },
  },
  actions: {
    /** 查询并缓存关系状态 */
    async checkRelationAction(userId: string, targetUserId: string, force = false): Promise<RelationDetailVO> {
      if (!force) {
        const cached = this.getRelation(targetUserId);
        if (cached) return cached;
      }
      const data = await checkRelation(userId, targetUserId);
      this.relationCache[targetUserId] = { data, cachedAt: Date.now() };
      return data;
    },

    /** 拉黑用户 */
    async blockUser(userId: string, targetUserId: string): Promise<void> {
      await apiBlock(userId, targetUserId);
      const entry = this.relationCache[targetUserId];
      if (entry) {
        entry.data.isBlocked = true;
        entry.cachedAt = Date.now();
      }
      this.blacklistCount += 1;
    },

    /** 解除拉黑 */
    async unblockUser(userId: string, targetUserId: string): Promise<void> {
      await apiUnblock(userId, targetUserId);
      const entry = this.relationCache[targetUserId];
      if (entry) {
        entry.data.isBlocked = false;
        entry.cachedAt = Date.now();
      }
      this.blacklistCount = Math.max(0, this.blacklistCount - 1);
    },

    /** 屏蔽用户 */
    async muteUser(userId: string, targetUserId: string): Promise<void> {
      await apiMute(userId, targetUserId);
      const entry = this.relationCache[targetUserId];
      if (entry) {
        entry.data.isMuted = true;
        entry.cachedAt = Date.now();
      }
      this.muteListCount += 1;
    },

    /** 取消屏蔽 */
    async unmuteUser(userId: string, targetUserId: string): Promise<void> {
      await apiUnmute(userId, targetUserId);
      const entry = this.relationCache[targetUserId];
      if (entry) {
        entry.data.isMuted = false;
        entry.cachedAt = Date.now();
      }
      this.muteListCount = Math.max(0, this.muteListCount - 1);
    },

    /** 刷新黑名单和屏蔽列表数量 */
    async refreshCounts(userId: string): Promise<void> {
      const [blacklistRes, muteListRes] = await Promise.all([
        getBlacklist(userId, 1, 1),
        getMuteList(userId, 1, 1),
      ]);
      this.blacklistCount = blacklistRes.total;
      this.muteListCount = muteListRes.total;
    },

    /** 清空关系缓存（登出时调用） */
    clearRelationCache(): void {
      this.relationCache = {};
      this.blacklistCount = 0;
      this.muteListCount = 0;
    },
  },
});

export function useBlockMuteStoreWithOut() {
  return useBlockMuteStore(store);
}
