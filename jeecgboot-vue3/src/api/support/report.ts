import { defHttp } from '/@/utils/http/axios';

enum Api {
  create = '/content/user/support/report/create',
  withdraw = '/content/user/support/report/{id}/withdraw',
  list = '/content/user/support/report/list',
  detail = '/content/user/support/report/{id}',
}

export interface ReportCreateParams {
  targetType: string; // 'article' | 'comment' | 'user'
  targetId: string;
  reportType: string; // 'porn' | 'violence' | 'fraud' | 'harassment' | 'other'
  description?: string;
  evidenceUrls?: string[];
}

export interface ReportQueryParams {
  status?: string;
  reportType?: string;
  pageNo?: number;
  pageSize?: number;
}

export interface ReportItem {
  id: string;
  reportNo: string;
  targetType: string;
  targetId: string;
  targetSummary: string;
  reportType: string;
  reportTypeLabel: string;
  description: string;
  evidenceUrls: string[];
  status: string; // 'pending' | 'processing' | 'processed' | 'withdrawn'
  statusLabel: string;
  result: string;
  createTime: string;
  updateTime: string;
}

/** 创建举报 */
export const createReport = (data: ReportCreateParams) =>
  defHttp.post({ url: Api.create, data });

/** 撤回举报 */
export const withdrawReport = (id: string) =>
  defHttp.post({ url: Api.withdraw.replace('{id}', id) });

/** 查询举报列表 */
export const getReportList = (params: ReportQueryParams) =>
  defHttp.get({ url: Api.list, params });

/** 查询举报详情 */
export const getReportDetail = (id: string) =>
  defHttp.get({ url: Api.detail.replace('{id}', id) });
