import { defHttp } from '/@/utils/http/axios';
import type {
  GrowthSummaryVO,
  LevelConfigVO,
  LevelBenefitVO,
  DecayRuleVO,
  DecayStatusVO,
} from './types';

enum Api {
  summary = '/api/v1/content/user/growth/summary',
  levelConfig = '/api/v1/content/circle/growth/level/config',
  levelBenefit = '/api/v1/content/circle/growth/level/benefit',
  decayRule = '/api/v1/content/user/growth/decay/rule',
  decayStatus = '/api/v1/content/user/growth/decay/status',
}

/** 查询成长汇总（积分 + 成长值 + 等级） */
export const getGrowthSummary = () =>
  defHttp.get<GrowthSummaryVO>({ url: Api.summary });

/** 查询等级配置列表 */
export const getLevelConfig = () =>
  defHttp.get<LevelConfigVO[]>({ url: Api.levelConfig });

/** 查询当前等级权益摘要 */
export const getLevelBenefit = () =>
  defHttp.get<LevelBenefitVO>({ url: Api.levelBenefit });

/** 查询衰减规则说明 */
export const getDecayRule = () =>
  defHttp.get<DecayRuleVO>({ url: Api.decayRule });

/** 查询用户级衰减状态 */
export const getDecayStatus = () =>
  defHttp.get<DecayStatusVO>({ url: Api.decayStatus });
