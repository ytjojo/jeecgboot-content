import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/api/v1/content/channel/review/list',
  action = '/api/v1/content/channel/review/action',
  stats = '/api/v1/content/channel/review/stats',
}

/** 获取待审区列表（后端使用 current/size 分页） */
export const getReviewList = (params: {
  channelId: string;
  contentType?: string;
  submitter?: string;
  submitTimeStart?: string;
  submitTimeEnd?: string;
  reviewStatus?: string;
  timeoutStatus?: string;
  keyword?: string;
  current?: number;
  size?: number;
}) => defHttp.get({ url: Api.list, params });

/** 审核操作（逐条，action: APPROVE | REJECT） */
export const executeReview = (data: {
  reviewId: string;
  action: 'APPROVE' | 'REJECT';
  rejectReason?: string;
}) => defHttp.post({ url: Api.action, data });

/** 待审统计 */
export const getReviewStats = (channelId: string) =>
  defHttp.get({ url: Api.stats, params: { channelId } });
