import { defineStore } from 'pinia';
import { store } from '/@/store';
import mitt from '/@/utils/mitt';
import type {
  GrowthSummaryVO,
  LevelConfigVO,
  LevelBenefitVO,
  DecayRuleVO,
  DecayStatusVO,
} from '/@/api/content/growth/types';
import {
  getGrowthSummary,
  getLevelConfig,
  getLevelBenefit,
  getDecayRule,
  getDecayStatus,
} from '/@/api/content/growth';

const CACHE_TTL = 5 * 60 * 1000; // 5 分钟

/** 升级事件载荷 */
export interface LevelUpPayload {
  oldLevel: number;
  newLevel: number;
  levelName?: string;
}

/** 成长模块 mitt 事件映射 */
export interface GrowthEvents {
  'growth:level-up': LevelUpPayload;
}

/** 跨模块事件总线 */
export const growthEmitter = mitt();

interface GrowthState {
  /** 成长汇总（每次刷新） */
  summary: GrowthSummaryVO | null;
  /** 等级配置列表（5 分钟缓存） */
  levelConfigs: LevelConfigVO[];
  /** 等级配置缓存时间戳 */
  levelConfigsCachedAt: number;
  /** 等级权益 */
  levelBenefit: LevelBenefitVO | null;
  /** 衰减规则 */
  decayRule: DecayRuleVO | null;
  /** 用户衰减状态 */
  decayStatus: DecayStatusVO | null;
}

export const useGrowthStore = defineStore({
  id: 'app-growth',
  state: (): GrowthState => ({
    summary: null,
    levelConfigs: [],
    levelConfigsCachedAt: 0,
    levelBenefit: null,
    decayRule: null,
    decayStatus: null,
  }),
  getters: {
    getSummary(): GrowthSummaryVO | null {
      return this.summary;
    },
    getLevelConfigs(): LevelConfigVO[] {
      return this.levelConfigs;
    },
    getLevelBenefit(): LevelBenefitVO | null {
      return this.levelBenefit;
    },
    getDecayRule(): DecayRuleVO | null {
      return this.decayRule;
    },
    getDecayStatus(): DecayStatusVO | null {
      return this.decayStatus;
    },
    /** 等级配置缓存是否有效 */
    isLevelConfigsCacheValid(): boolean {
      return this.levelConfigs.length > 0 && Date.now() - this.levelConfigsCachedAt < CACHE_TTL;
    },
    /** 当前等级 */
    currentLevel(): number {
      return this.summary?.level ?? 0;
    },
    /** 积分余额 */
    pointBalance(): number {
      return this.summary?.pointBalance ?? 0;
    },
    /** 成长值 */
    growthValue(): number {
      return this.summary?.growthValue ?? 0;
    },
  },
  actions: {
    /** 加载成长汇总（每次进入页面刷新） */
    async loadSummary(): Promise<GrowthSummaryVO> {
      const data = await getGrowthSummary();
      const oldLevel = this.summary?.level ?? 0;
      this.summary = data;
      // 检测升级事件并广播
      if (data.level > oldLevel && oldLevel > 0) {
        growthEmitter.emit('growth:level-up', {
          oldLevel,
          newLevel: data.level,
          levelName: data.levelName,
        });
      }
      return data;
    },
    /** 加载等级配置列表（带 5 分钟 TTL 缓存） */
    async loadLevelConfigs(force = false): Promise<LevelConfigVO[]> {
      if (!force && this.isLevelConfigsCacheValid) {
        return this.levelConfigs;
      }
      const data = await getLevelConfig();
      this.levelConfigs = data;
      this.levelConfigsCachedAt = Date.now();
      return data;
    },
    /** 加载当前等级权益 */
    async loadLevelBenefit(): Promise<LevelBenefitVO> {
      const data = await getLevelBenefit();
      this.levelBenefit = data;
      return data;
    },
    /** 加载衰减规则 */
    async loadDecayRule(): Promise<DecayRuleVO> {
      const data = await getDecayRule();
      this.decayRule = data;
      return data;
    },
    /** 加载用户衰减状态 */
    async loadDecayStatus(): Promise<DecayStatusVO> {
      const data = await getDecayStatus();
      this.decayStatus = data;
      return data;
    },
    /** 清除缓存 */
    resetCache() {
      this.summary = null;
      this.levelConfigs = [];
      this.levelConfigsCachedAt = 0;
      this.levelBenefit = null;
      this.decayRule = null;
      this.decayStatus = null;
    },
  },
});

export function useGrowthStoreWithOut() {
  return useGrowthStore(store);
}
