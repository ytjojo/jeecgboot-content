import { defHttp } from '/@/utils/http/axios';

enum Api {
  available = '/api/v1/content/channel/publish/available',
  submit = '/api/v1/content/channel/publish',
  result = '/api/v1/content/channel/publish/result',
  scheduled = '/api/v1/content/channel/publish/scheduled',
  scheduledList = '/api/v1/content/channel/publish/scheduled/list',
  limitCheck = '/api/v1/content/channel/publish/limit/check',
  permission = '/api/v1/content/channel/publish/permission',
}

/** 获取用户可发布/投稿/管理的频道列表 */
export const getAvailableChannels = () => defHttp.get({ url: Api.available });

/** 提交内容到频道（支持多频道） */
export const submitPublish = (data: { contentId: string; channelIds: string[]; scheduledTime?: string }) =>
  defHttp.post({ url: Api.submit, data });

/** 查询发布结果 */
export const getPublishResult = (taskId: string) => defHttp.get({ url: `${Api.result}/${taskId}` });

/** 设定定时发布 */
export const createScheduledPublish = (data: { contentId: string; channelIds: string[]; scheduledTime: string }) =>
  defHttp.post({ url: Api.scheduled, data });

/** 修改定时发布时间 */
export const updateScheduledPublish = (id: string, data: { scheduledTime: string }) =>
  defHttp.put({ url: `${Api.scheduled}/${id}`, data });

/** 取消定时发布 */
export const cancelScheduledPublish = (id: string) =>
  defHttp.delete({ url: `${Api.scheduled}/${id}` });

/** 获取当前用户的定时发布任务列表 */
export const getScheduledList = () => defHttp.get({ url: Api.scheduledList });

/** 预校验发布限额 */
export const checkPublishLimit = (data: { contentId: string; channelIds: string[] }) =>
  defHttp.post({ url: Api.limitCheck, data });

/** 获取频道发布权限配置 */
export const getPublishPermission = (channelId: string) =>
  defHttp.get({ url: `${Api.permission}/${channelId}` });

/** 保存频道发布权限配置 */
export const savePublishPermission = (data: {
  channelId: string;
  publishModel: string;
  hourlyLimit?: number;
  dailyLimit?: number;
  minWordCount?: number;
}) => defHttp.post({ url: Api.permission, data });
