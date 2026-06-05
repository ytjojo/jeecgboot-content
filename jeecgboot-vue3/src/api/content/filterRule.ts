import { defHttp } from '/@/utils/http/axios';

enum Api {
  addRule = '/content/user/filter-rule',
  deleteRule = '/content/user/filter-rule/delete',
  batchDelete = '/content/user/filter-rule/batch-delete',
  ruleList = '/content/user/filter-rule/list',
  notInterested = '/content/user/not-interested',
}

/** 添加屏蔽规则 */
export const addFilterRule = (userId: string, ruleType: string, value: string, daysValid?: number) =>
  defHttp.post<void>({ url: Api.addRule, params: { userId, ruleType, value, daysValid } });

/** 删除屏蔽规则 */
export const deleteFilterRule = (userId: string, ruleId: string) =>
  defHttp.post<void>({ url: Api.deleteRule, params: { userId, ruleId } });

/** 批量删除屏蔽规则 */
export const batchDeleteFilterRules = (userId: string, ruleIds: string[]) =>
  defHttp.post<void>({ url: Api.batchDelete, params: { userId }, data: ruleIds });

/** 查询屏蔽规则列表 */
export const getFilterRuleList = (userId: string, ruleType?: string, pageNo?: number, pageSize?: number) =>
  defHttp.get<FilterRulePageVO>({ url: Api.ruleList, params: { userId, ruleType, pageNo, pageSize } });

/** 不感兴趣反馈 */
export const recordNotInterested = (userId: string, contentId: string, contentType: string) =>
  defHttp.post<void>({ url: Api.notInterested, params: { userId, contentId, contentType } });

/** 屏蔽规则分页 VO */
export interface FilterRulePageVO {
  records: FilterRuleItemVO[];
  total: number;
}

/** 屏蔽规则项 */
export interface FilterRuleItemVO {
  ruleId: string;
  ruleType: string;
  value: string;
  createdAt: string;
  expiresAt?: string;
}
