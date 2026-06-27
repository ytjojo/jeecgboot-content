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
  levelConfig = '/api/v1/content/user/growth/level/config',
  levelBenefit = '/api/v1/content/user/growth/level/benefit',
  decayRule = '/api/v1/content/user/growth/decay/rule',
  decayStatus = '/api/v1/content/user/growth/decay/status',
}

/** 查询成长汇总（积分 + 成长值 + 等级），需要 userId 参数 */
export const getGrowthSummary = (userId: string) =>
  defHttp.get<GrowthSummaryVO>({ url: Api.summary, params: { userId } });

/** 查询等级配置列表 */
export const getLevelConfig = () =>
  defHttp.get<LevelConfigVO[]>({ url: Api.levelConfig });

/** 查询当前等级权益摘要，需要 userId 参数 */
export const getLevelBenefit = (userId: string) =>
  defHttp.get<LevelBenefitVO>({ url: Api.levelBenefit, params: { userId } });

/** 查询衰减规则说明 */
export const getDecayRule = () =>
  defHttp.get<DecayRuleVO>({ url: Api.decayRule });

/** 查询用户级衰减状态，需要 userId 参数 */
export const getDecayStatus = (userId: string) =>
  defHttp.get<DecayStatusVO>({ url: Api.decayStatus, params: { userId } });
