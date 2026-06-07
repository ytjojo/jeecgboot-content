import { defHttp } from '/@/utils/http/axios';

enum Api {
  log = '/api/v1/content/channel/governance/log',
}

/** 治理日志列表 */
export const getGovernanceLog = (params: { channelId: string; [key: string]: any }) =>
  defHttp.get({ url: Api.log, params });
