import { defHttp } from '/@/utils/http/axios';

enum Api {
  create = '/content/user/support/appeal/create',
  withdraw = '/content/user/support/appeal/{id}/withdraw',
  list = '/content/user/support/appeal/list',
  detail = '/content/user/support/appeal/{id}',
}

export interface AppealCreateParams {
  appealType: string; // 'content_delete' | 'account_ban' | 'points_deduct' | 'badge_deduct'
  relatedId: string;
  reason: string;
  attachmentUrls?: string[];
}

export interface AppealQueryParams {
  status?: string;
  pageNo?: number;
  pageSize?: number;
}

export interface AppealItem {
  id: string;
  appealNo: string;
  appealType: string;
  appealTypeLabel: string;
  relatedId: string;
  relatedSummary: string;
  reason: string;
  attachmentUrls: string[];
  status: string; // 'reviewing' | 'approved' | 'rejected' | 'withdrawn'
  statusLabel: string;
  auditResult: string;
  auditTime: string;
  appealCount: number;
  maxAppealCount: number;
  estimatedTime: string;
  createTime: string;
}

/** 创建申诉 */
export const createAppeal = (data: AppealCreateParams) =>
  defHttp.post({ url: Api.create, data });

/** 撤回申诉 */
export const withdrawAppeal = (id: string) =>
  defHttp.post({ url: Api.withdraw.replace('{id}', id) });

/** 查询申诉列表 */
export const getAppealList = (params: AppealQueryParams) =>
  defHttp.get({ url: Api.list, params });

/** 查询申诉详情 */
export const getAppealDetail = (id: string) =>
  defHttp.get({ url: Api.detail.replace('{id}', id) });
