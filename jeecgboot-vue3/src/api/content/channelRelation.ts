import { defHttp } from '/@/utils/http/axios';

enum Api {
  userRelation = '/api/v1/content/channels/',
}

/** 获取用户与频道的关系（订阅状态、角色、禁言、黑名单） */
export const getUserChannelRelation = (channelId: string) =>
  defHttp.get({ url: Api.userRelation + channelId + '/user-relation' });
