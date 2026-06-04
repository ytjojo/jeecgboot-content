/**
 * 内容社区 - 游戏化成长（等级 / 衰减） 类型定义
 *
 * 对接后端成长汇总模块，路径前缀 `/content/user/growth/*`。
 * 勋章类型见 `./badge-types.ts`，积分类型见 `./point-types.ts`。
 */

/** 成长汇总 VO */
export interface GrowthSummaryVO {
  /** 用户 ID */
  userId: string;
  /** 当前积分余额 */
  pointBalance: number;
  /** 当前成长值 */
  growthValue: number;
  /** 当前等级 */
  level: number;
  /** 当前等级名称 */
  levelName?: string;
  /** 距离下一等级所需成长值 */
  nextLevelGap?: number;
  /** 下一等级名称 */
  nextLevelName?: string;
  /** 升级进度百分比 0-100 */
  levelProgress?: number;
}

/** 等级配置 VO */
export interface LevelConfigVO {
  /** 等级序号 */
  level: number;
  /** 等级名称 */
  levelName: string;
  /** 所需成长值门槛 */
  requiredGrowth: number;
  /** 等级图标 URL */
  icon?: string;
  /** 等级描述 */
  description?: string;
}

/** 等级权益 VO */
export interface LevelBenefitVO {
  /** 当前等级 */
  level: number;
  /** 权益列表 */
  benefits: BenefitItem[];
}

/** 单项权益 */
export interface BenefitItem {
  /** 权益编码 */
  benefitCode: string;
  /** 权益名称 */
  benefitName: string;
  /** 权益描述 */
  description?: string;
  /** 权益图标 */
  icon?: string;
}

/** 衰减规则 VO */
export interface DecayRuleVO {
  /** 是否启用衰减 */
  enabled: boolean;
  /** 衰减周期（天） */
  decayCycleDays?: number;
  /** 每周期衰减比例 0-1 */
  decayRate?: number;
  /** 免衰减最低活跃次数 */
  minActivityCount?: number;
  /** 规则说明 */
  description?: string;
}

/** 用户衰减状态 VO */
export interface DecayStatusVO {
  /** 用户 ID */
  userId: string;
  /** 距下次衰减剩余天数 */
  daysUntilDecay?: number;
  /** 本周期活跃次数 */
  currentActivityCount?: number;
  /** 达到免衰减所需活跃次数 */
  requiredActivityCount?: number;
  /** 预计下次衰减成长值 */
  nextDecayAmount?: number;
}

// 统一 re-export 子模块类型，便于外部一次性导入
export type * from './badge-types';
export type * from './point-types';
