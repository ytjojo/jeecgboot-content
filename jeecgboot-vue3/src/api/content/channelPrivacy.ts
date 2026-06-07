import { defHttp } from '/@/utils/http/axios';

enum Api {
  updatePrivacy = '/api/v1/content/channels/privacy',
  updateJoinMethod = '/api/v1/content/channels/join-method',
}

/** 更新隐私设置 */
export const updateChannelPrivacy = (data: { channelId: string; privacyType: 'PUBLIC' | 'PRIVATE' }) =>
  defHttp.put({ url: Api.updatePrivacy, data });

/** 更新加入方式 */
export const updateJoinMethod = (data: { channelId: string; joinMethod: string; config?: any }) =>
  defHttp.put({ url: Api.updateJoinMethod, data });
