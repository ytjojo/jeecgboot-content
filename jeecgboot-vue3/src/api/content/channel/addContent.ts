import { defHttp } from '/@/utils/http/axios';

enum Api {
  add = '/api/v1/content/channel/publish/add-existing',
  search = '/api/v1/content/channel/publish/add-existing/search',
  channels = '/api/v1/content/channel/publish/content-channels',
}

/** 添加已发布内容到频道 */
export const addContentToChannel = (data: {
  contentId: string;
  channelIds: string[];
  operatorNote?: string;
}) => defHttp.post({ url: Api.add, data });

/** 搜索可添加的已发布内容 */
export const searchAddableContent = (params: { keyword: string; contentType?: string; current?: number; size?: number }) =>
  defHttp.get({ url: Api.search, params });

/** 查看内容所在频道列表 */
export const getContentChannels = (contentId: string) =>
  defHttp.get({ url: `${Api.channels}/${contentId}` });
