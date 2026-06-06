/** 用户状态枚举 */
export enum UserStatusEnum {
  GUEST = 'GUEST',
  REGISTERED_INCOMPLETE = 'REGISTERED_INCOMPLETE',
  NORMAL = 'NORMAL',
  MUTED = 'MUTED',
  RESTRICTED_RECOMMEND = 'RESTRICTED_RECOMMEND',
  FROZEN = 'FROZEN',
  BANNED = 'BANNED',
  DEACTIVATING = 'DEACTIVATING',
  DEACTIVATED = 'DEACTIVATED',
}

/** 用户状态详情 */
export interface UserStatusDetail {
  userId: string;
  status: UserStatusEnum;
  statusName: string;
  reason?: string;
  startTime?: string;
  endTime?: string;
  operatorId?: string;
  operatorName?: string;
}

/** 状态历史记录 */
export interface UserStatusHistoryItem {
  id: string;
  userId: string;
  fromStatus: UserStatusEnum;
  toStatus: UserStatusEnum;
  reason: string;
  operatorId: string;
  operatorName: string;
  operatorType: string;
  createdAt: string;
}

/** 状态查询请求 */
export interface UserStatusQueryReq {
  userId?: string;
  status?: UserStatusEnum | string;
  page?: number;
  pageSize?: number;
}

/** 状态变更请求 */
export interface UserStatusChangeReq {
  toStatus: UserStatusEnum | string;
  reason: string;
  endTime?: string;
  remark?: string;
}

/** 审计日志查询请求 */
export interface AuditLogQueryReq {
  userId?: string;
  startTime?: string;
  endTime?: string;
  operatorType?: string;
  page?: number;
  pageSize?: number;
}

/** 审计日志导出请求 */
export interface AuditLogExportReq {
  userId?: string;
  startTime?: string;
  endTime?: string;
  format?: 'excel' | 'csv';
}

/** 审计日志详情 */
export interface AuditLogDetail {
  id: string;
  userId: string;
  userName: string;
  action: string;
  fromStatus: UserStatusEnum;
  toStatus: UserStatusEnum;
  reason: string;
  operatorId: string;
  operatorName: string;
  operatorType: string;
  ipAddress: string;
  createdAt: string;
  remark?: string;
}

/** 分页响应 */
export interface PageResult<T> {
  records: T[];
  total: number;
  page: number;
  pageSize: number;
}

/** 登录拦截响应 */
export interface LoginBlockedResponse {
  status: UserStatusEnum;
  statusName: string;
  reason?: string;
  endTime?: string;
  canVerify: boolean;
}
