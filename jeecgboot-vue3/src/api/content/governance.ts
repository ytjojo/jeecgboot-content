import { defHttp } from '/@/utils/http/axios';

enum Api {
  deleteComment = '/content/user/governance/moderator/comment/delete',
  warnUser = '/content/user/governance/moderator/user/warn',
  auditLog = '/content/user/governance/audit-log',
}

/** 版主删除评论 */
export const deleteComment = (commentId: string, reason: string) =>
  defHttp.post<void>({ url: Api.deleteComment, data: { commentId, reason } });

/** 版主警告用户 */
export const warnUser = (userId: string, reason: string) =>
  defHttp.post<void>({ url: Api.warnUser, data: { userId, reason } });

/** 分页查询审计日志 */
export const listAuditLog = (params?: {
  pageNo?: number;
  pageSize?: number;
  operatorUserId?: string;
  eventType?: string;
  startTime?: string;
  endTime?: string;
}) => defHttp.get({ url: Api.auditLog, params: params || {} });
