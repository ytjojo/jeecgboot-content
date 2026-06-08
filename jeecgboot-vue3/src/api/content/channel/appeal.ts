import { defHttp } from '/@/utils/http/axios';

enum Api {
  submit = '/api/v1/content/channel/lifecycle/appeal/submit',
  handle = '/api/v1/content/channel/lifecycle/appeal/handle',
  list = '/api/v1/content/channel/lifecycle/appeal/list',
  detail = '/api/v1/content/channel/lifecycle/appeal/detail',
}

export interface AppealVO {
  id: string;
  appealNo: string;
  channelName: string;
  penaltyType: string;
  appellant: string;
  submitTime: string;
  status: string;
  isTimeout: boolean;
}

export interface AppealDetailVO {
  id: string;
  appealNo: string;
  channelName: string;
  penaltyType: string;
  penaltyInfo: string;
  appealExplain: string;
  supplementaryMaterial?: string[];
  appellant: string;
  submitTime: string;
  status: string;
  historyRecords: { time: string; action: string; operator: string; result: string }[];
}

export interface AppealSubmitReq {
  channelId: string;
  penaltyId: string;
  explain: string;
  materials?: string[];
}

export interface AppealHandleReq {
  appealId: string;
  result: 'RESTORE' | 'MAINTAIN';
  note: string;
}

export interface AppealListQuery {
  status?: string;
  channelName?: string;
  submitTimeStart?: string;
  submitTimeEnd?: string;
  channelId?: string;
  current?: number;
  size?: number;
}

/** 提交申诉 */
export const submitAppeal = (data: AppealSubmitReq) =>
  defHttp.post({ url: Api.submit, data });

/** 处理申诉 */
export const handleAppeal = (data: AppealHandleReq) =>
  defHttp.post({ url: Api.handle, data });

/** 获取申诉列表 */
export const getAppealList = (params: AppealListQuery) =>
  defHttp.get<{ records: AppealVO[]; total: number }>({ url: Api.list, params });

/** 获取申诉详情 */
export const getAppealDetail = (id: string) =>
  defHttp.get<AppealDetailVO>({ url: `${Api.detail}/${id}` });
