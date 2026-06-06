import { defHttp } from '/@/utils/http/axios';
import type {
  UserStatusDetail,
  UserStatusHistoryItem,
  UserStatusQueryReq,
  UserStatusChangeReq,
  AuditLogQueryReq,
  AuditLogExportReq,
  AuditLogDetail,
  PageResult,
  UserStatusEnum,
} from './model/userStatusModel';

enum Api {
  current = '/api/content/user-status/current',
  base = '/api/content/user-status',
  transitions = '/api/content/user-status/transitions',
  list = '/api/content/user-status/list',
  batchRelease = '/api/content/user-status/batch-release',
  auditLogs = '/api/content/user-status/audit-logs',
  auditLogExport = '/api/content/user-status/audit-logs/export',
  verifySecurity = '/api/content/user-status/verify-security',
  sendVerifyCode = '/api/content/user-status/send-verify-code',
}

/** 获取当前用户状态 */
export const getCurrentStatus = (userId: string) =>
  defHttp.get<UserStatusDetail>({ url: Api.current, params: { userId } });

/** 获取指定用户状态 */
export const getUserStatus = (userId: string) =>
  defHttp.get<UserStatusDetail>({ url: `${Api.base}/${userId}` });

/** 分页查询用户状态列表 */
export const getStatusList = (params: UserStatusQueryReq) =>
  defHttp.get<PageResult<UserStatusDetail>>({ url: Api.list, params });

/** 获取可转换状态列表 */
export const getTransitions = (currentStatus: string) =>
  defHttp.get<UserStatusEnum[]>({ url: `${Api.transitions}/${currentStatus}` });

/** 变更用户状态 */
export const changeUserStatus = (userId: string, payload: UserStatusChangeReq) =>
  defHttp.post<void>({ url: `${Api.base}/${userId}/change`, params: payload });

/** 解禁用户 */
export const releaseUser = (userId: string, reason: string) =>
  defHttp.post<void>({ url: `${Api.base}/${userId}/release`, params: { reason } });

/** 批量解禁 */
export const batchReleaseUsers = (userIds: string[], reason: string) =>
  defHttp.post<void>({ url: Api.batchRelease, params: { userIds, reason } });

/** 获取状态变更历史 */
export const getStatusHistory = (userId: string, params?: { page?: number; pageSize?: number }) =>
  defHttp.get<UserStatusHistoryItem[]>({ url: `${Api.base}/${userId}/history`, params });

/** 审计日志分页查询 */
export const getAuditLogList = (params: AuditLogQueryReq) =>
  defHttp.get<PageResult<AuditLogDetail>>({ url: Api.auditLogs, params });

/** 审计日志详情 */
export const getAuditLogDetail = (logId: string) =>
  defHttp.get<AuditLogDetail>({ url: `${Api.auditLogs}/${logId}` });

/** 查询用户审计日志 */
export const getUserAuditLogs = (userId: string, params: { page?: number; pageSize?: number }) =>
  defHttp.get<PageResult<AuditLogDetail>>({ url: `${Api.base}/users/${userId}/audit-logs`, params });

/** 导出审计日志 */
export const exportAuditLogs = (params: AuditLogExportReq) =>
  defHttp.get(
    { url: Api.auditLogExport, params, responseType: 'blob' },
    { isTransformResponse: false }
  );

/** 安全核验 */
export const verifySecurity = (phone: string, verifyCode: string) =>
  defHttp.post<void>({ url: Api.verifySecurity, params: { phone, verifyCode } });

/** 发送验证码 */
export const sendVerifyCode = (phone: string) =>
  defHttp.post<void>({ url: Api.sendVerifyCode, params: { phone } });
