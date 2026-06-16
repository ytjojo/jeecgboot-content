import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/api/v1/content/circle/report/list',
  deleteContent = '/api/v1/content/circle/report',
}

/** 圈子举报项 */
export interface CircleReportVO {
  id: string;
  circleId: string;
  contentId: string;
  reporterId: string;
  reason: string;
  status: string; // 'PENDING' | 'RESOLVED' | 'IGNORED'
  handleAction?: string;
  createTime?: string;
}

/** 获取圈子举报列表 */
export const getCircleReportList = (circleId: string, status?: string) =>
  defHttp.get<CircleReportVO[]>({
    url: `${Api.list}/${circleId}`,
    params: status ? { status } : undefined,
  });

/** 删除被举报内容 */
export const deleteReportContent = (reportId: string, circleId: string) =>
  defHttp.post({
    url: `${Api.deleteContent}/${reportId}/delete-content`,
    params: { circleId },
  });

/** 忽略举报 */
export const ignoreReport = (reportId: string, circleId: string) =>
  defHttp.post({
    url: `${Api.deleteContent}/${reportId}/ignore`,
    params: { circleId },
  });

/** 禁言被举报用户 */
export const muteReportUser = (reportId: string, circleId: string) =>
  defHttp.post({
    url: `${Api.deleteContent}/${reportId}/mute`,
    params: { circleId },
  });
