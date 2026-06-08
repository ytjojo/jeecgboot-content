import { defHttp } from '/@/utils/http/axios';

enum Api {
  // 生命周期操作
  freeze = '/api/v1/content/channel/lifecycle/freeze',
  unfreeze = '/api/v1/content/channel/lifecycle/unfreeze',
  hide = '/api/v1/content/channel/lifecycle/hide',
  close = '/api/v1/content/channel/lifecycle/close',
  archive = '/api/v1/content/channel/lifecycle/archive',
  restrictRecommend = '/api/v1/content/channel/lifecycle/restrict-recommend',
  restoreVisibility = '/api/v1/content/channel/lifecycle/restore-visibility',
  logs = '/api/v1/content/channel/lifecycle/logs',
  // 治理频道列表与详情
  governanceList = '/api/v1/content/channel/governance/list',
  governanceDetail = '/api/v1/content/channel/governance/detail',
  // 合并管理
  mergeValidate = '/api/v1/content/channel/lifecycle/merge/validate',
  mergeExecute = '/api/v1/content/channel/lifecycle/merge/execute',
}

export interface LifecycleActionReq {
  channelId: string;
  reason: string;
}

export interface FreezeReq extends LifecycleActionReq {
  channelId: string;
  reason: string;
}

export interface UnfreezeReq extends LifecycleActionReq {
  channelId: string;
  reason: string;
}

export interface HideReq extends LifecycleActionReq {
  channelId: string;
  reason: string;
}

export interface CloseReq {
  channelId: string;
  channelNameConfirm: string;
  reason: string;
}

export interface ArchiveReq extends LifecycleActionReq {
  channelId: string;
  reason: string;
}

export interface RestrictRecommendReq extends LifecycleActionReq {
  channelId: string;
  reason: string;
}

export interface RestoreVisibilityReq extends LifecycleActionReq {
  channelId: string;
  reason: string;
}

export interface MergeValidateReq {
  sourceChannelId: string;
  targetChannelId: string;
}

export interface MergeValidateResult {
  valid: boolean;
  reason?: string;
  preview?: {
    subscriberCount: number;
    contentCount: number;
    historyDataHandling: string;
  };
}

export interface MergeExecuteReq {
  sourceChannelId: string;
  targetChannelId: string;
  reason: string;
}

export interface GovernanceChannelVO {
  channelId: string;
  channelName: string;
  channelType: string;
  status: string;
  subscriberCount: number;
  lastActiveTime: string;
  createTime: string;
}

export interface GovernanceDetailVO {
  channelId: string;
  channelName: string;
  channelType: string;
  status: string;
  createTime: string;
  subscriberCount: number;
  contentCount: number;
  lastActiveTime: string;
  description?: string;
  icon?: string;
  cover?: string;
  category?: string;
}

export interface GovernanceListQuery {
  channelName?: string;
  channelType?: string;
  status?: string;
  current?: number;
  size?: number;
}

/** 冻结频道 */
export const freezeChannel = (data: FreezeReq) =>
  defHttp.post({ url: Api.freeze, data });

/** 解冻频道 */
export const unfreezeChannel = (data: UnfreezeReq) =>
  defHttp.post({ url: Api.unfreeze, data });

/** 强制隐藏频道 */
export const hideChannel = (data: HideReq) =>
  defHttp.post({ url: Api.hide, data });

/** 永久关闭频道 */
export const closeChannel = (data: CloseReq) =>
  defHttp.post({ url: Api.close, data });

/** 归档频道 */
export const archiveChannel = (data: ArchiveReq) =>
  defHttp.post({ url: Api.archive, data });

/** 限制推荐 */
export const restrictChannelRecommend = (data: RestrictRecommendReq) =>
  defHttp.post({ url: Api.restrictRecommend, data });

/** 恢复可见 */
export const restoreChannelVisibility = (data: RestoreVisibilityReq) =>
  defHttp.post({ url: Api.restoreVisibility, data });

/** 获取审计日志 */
export const getLifecycleLogs = (params: { channelId?: string; current?: number; size?: number }) =>
  defHttp.get({ url: Api.logs, params });

/** 获取治理频道列表 */
export const getGovernanceChannelList = (params: GovernanceListQuery) =>
  defHttp.get<{ records: GovernanceChannelVO[]; total: number }>({ url: Api.governanceList, params });

/** 获取治理频道详情 */
export const getGovernanceChannelDetail = (channelId: string) =>
  defHttp.get<GovernanceDetailVO>({ url: `${Api.governanceDetail}/${channelId}` });

/** 合并校验 */
export const validateChannelMerge = (data: MergeValidateReq) =>
  defHttp.post<MergeValidateResult>({ url: Api.mergeValidate, data });

/** 执行合并 */
export const executeChannelMerge = (data: MergeExecuteReq) =>
  defHttp.post({ url: Api.mergeExecute, data });
