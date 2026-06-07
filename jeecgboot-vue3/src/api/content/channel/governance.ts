import { defHttp } from '/@/utils/http/axios';

enum Api {
  governance = '/api/v1/content/channel/governance',
  contentList = '/api/v1/content/channel/governance/content/list',
  editAssistHistory = '/api/v1/content/channel/governance/edit-assist/history',
  recycleBinList = '/api/v1/content/channel/governance/recycle-bin/list',
  logList = '/api/v1/content/channel/governance/log',
}

/** 统一治理操作（后端使用 action 字段区分操作类型） */
export const executeGovernance = (data: {
  channelId: string;
  contentId: string;
  action: 'PIN' | 'UNPIN' | 'FEATURE' | 'UNFEATURE' | 'DELETE' | 'RESTORE' | 'MOVE' | 'EDIT_ASSIST';
  targetChannelId?: string;
  reason?: string;
  editFields?: Record<string, any>;
}) => defHttp.post({ url: Api.governance, data });

/** 频道内容列表（后端使用 current/size 分页） */
export const getGovernanceContentList = (params: {
  channelId: string;
  contentType?: string;
  status?: string;
  author?: string;
  startTime?: string;
  endTime?: string;
  sortBy?: string;
  keyword?: string;
  current?: number;
  size?: number;
}) => defHttp.get({ url: Api.contentList, params });

/** 获取编辑协助修订历史 */
export const getEditAssistHistory = (contentId: string) =>
  defHttp.get({ url: `${Api.editAssistHistory}/${contentId}` });

/** 回收站列表（后端使用 current/size 分页） */
export const getRecycleBinList = (params: {
  channelId: string;
  contentType?: string;
  deletedBy?: string;
  startTime?: string;
  endTime?: string;
  current?: number;
  size?: number;
}) => defHttp.get({ url: Api.recycleBinList, params });

/** 治理日志列表（后端使用 current/size 分页） */
export const getGovernanceLogList = (params: {
  channelId: string;
  actionType?: string;
  operator?: string;
  startTime?: string;
  endTime?: string;
  keyword?: string;
  current?: number;
  size?: number;
}) => defHttp.get({ url: Api.logList, params });
