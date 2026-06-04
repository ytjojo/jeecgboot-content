import { defineStore } from 'pinia';
import { store } from '/@/store';
import type { BadgeCatalogVO, BadgeDetailVO, BadgeWearReq } from '/@/api/content/growth/badge-types';
import { getBadgeCatalog, getWornBadges, saveWornBadges } from '/@/api/content/growth/badge';

const CACHE_TTL = 5 * 60 * 1000; // 5 分钟

interface BadgeState {
  /** 勋章分类目录 */
  catalog: BadgeCatalogVO[];
  /** 当前用户佩戴中的勋章 */
  wornBadges: BadgeDetailVO[];
  /** 目录缓存时间戳 */
  catalogCachedAt: number;
  /** 佩戴缓存时间戳 */
  wornCachedAt: number;
}

export const useBadgeStore = defineStore({
  id: 'app-badge',
  state: (): BadgeState => ({
    catalog: [],
    wornBadges: [],
    catalogCachedAt: 0,
    wornCachedAt: 0,
  }),
  getters: {
    getCatalog(): BadgeCatalogVO[] {
      return this.catalog;
    },
    getWornBadges(): BadgeDetailVO[] {
      return this.wornBadges;
    },
    /** 目录缓存是否有效 */
    isCatalogCacheValid(): boolean {
      return this.catalog.length > 0 && Date.now() - this.catalogCachedAt < CACHE_TTL;
    },
    /** 佩戴缓存是否有效 */
    isWornCacheValid(): boolean {
      return this.wornBadges.length > 0 && Date.now() - this.wornCachedAt < CACHE_TTL;
    },
  },
  actions: {
    /** 加载勋章分类目录（带 5 分钟 TTL 缓存） */
    async loadCatalog(force = false): Promise<BadgeCatalogVO[]> {
      if (!force && this.isCatalogCacheValid) {
        return this.catalog;
      }
      const data = await getBadgeCatalog();
      this.catalog = data;
      this.catalogCachedAt = Date.now();
      return data;
    },
    /** 加载佩戴中的勋章（带 5 分钟 TTL 缓存） */
    async loadWornBadges(userId: string, force = false): Promise<BadgeDetailVO[]> {
      if (!force && this.isWornCacheValid) {
        return this.wornBadges;
      }
      const data = await getWornBadges(userId);
      this.wornBadges = data;
      this.wornCachedAt = Date.now();
      return data;
    },
    /** 保存佩戴勋章配置并刷新缓存 */
    async saveWorn(data: BadgeWearReq): Promise<void> {
      await saveWornBadges(data);
      this.wornCachedAt = 0; // 使缓存失效，下次访问重新加载
    },
    /** 清除缓存 */
    resetCache() {
      this.catalog = [];
      this.wornBadges = [];
      this.catalogCachedAt = 0;
      this.wornCachedAt = 0;
    },
  },
});

export function useBadgeStoreWithOut() {
  return useBadgeStore(store);
}
