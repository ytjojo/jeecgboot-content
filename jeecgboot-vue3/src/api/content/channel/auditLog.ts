import { defHttp } from '/@/utils/http/axios';

enum Api {
  logs = '/api/v1/content/channel/lifecycle/logs',
}

export interface AuditLogVO {
  id: string;
  operateTime: string;
  channelName: string;
  operator: string;
  operationType: string;
  beforeStatus: string;
  afterStatus: string;
  reason: string;
  impactScope?: string;
}

export interface AuditLogQuery {
  channelName?: string;
  operator?: string;
  operationType?: string;
  operateTimeStart?: string;
  operateTimeEnd?: string;
  channelId?: string;
  current?: number;
  size?: number;
}

/** 获取审计日志列表 */
export const getAuditLogList = (params: AuditLogQuery) =>
  defHttp.get<{ records: AuditLogVO[]; total: number }>({ url: Api.logs, params });

/** 获取审计日志详情 */
export const getAuditLogDetail = (id: string) =>
  defHttp.get<AuditLogVO>({ url: `${Api.logs}/${id}` });
