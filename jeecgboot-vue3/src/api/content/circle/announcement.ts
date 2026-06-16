import { defHttp } from '/@/utils/http/axios';
import type { CircleAnnouncementVO } from './model/circleAnnouncementModel';

enum Api {
  publish = '/api/v1/content/circle/announcement',
  active = '/api/v1/content/circle/announcement/active',
  history = '/api/v1/content/circle/announcement/history',
}

/** 发布圈子公告 */
export const publishCircleAnnouncement = (data: {
  circleId: string;
  content: string;
  expireAt?: string;
}) => defHttp.post({ url: `${Api.publish}/`, data });

/** 删除圈子公告 */
export const deleteCircleAnnouncement = (id: string) =>
  defHttp.delete({ url: `${Api.publish}/${id}` });

/** 获取圈子当前有效公告 */
export const getActiveCircleAnnouncement = (circleId: string) =>
  defHttp.get<CircleAnnouncementVO>({ url: `${Api.active}/${circleId}` });

/** 获取圈子历史公告列表 */
export const getCircleAnnouncementHistory = (circleId: string) =>
  defHttp.get<CircleAnnouncementVO[]>({ url: `${Api.history}/${circleId}` });
