import { defHttp } from '/@/utils/http/axios';

enum Api {
  base = '/api/v1/content/channel/announcement',
  channel = '/api/v1/content/channel/announcement/channel',
  preview = '/api/v1/content/channel/announcement/preview',
  history = '/api/v1/content/channel/announcement/history',
  restore = '/api/v1/content/channel/announcement/restore',
}

/** 获取频道当前公告 */
export const getAnnouncement = (channelId: string) =>
  defHttp.get({ url: `${Api.channel}/${channelId}` });

/** 发布/更新公告 */
export const saveAnnouncement = (data: { channelId: string; title: string; content: string; version?: number }) =>
  defHttp.post({ url: Api.base, data });

/** 删除公告 */
export const deleteAnnouncement = (id: string) =>
  defHttp.delete({ url: `${Api.base}/${id}` });

/** 公告预览 */
export const previewAnnouncement = (data: { content: string }) =>
  defHttp.post({ url: Api.preview, data });

/** 获取频道公告历史版本列表 */
export const getAnnouncementHistory = (channelId: string) =>
  defHttp.get({ url: `${Api.history}/${channelId}` });

/** 恢复历史版本 */
export const restoreAnnouncementVersion = (versionId: string) =>
  defHttp.post({ url: `${Api.restore}/${versionId}` });
