import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/api/v1/content/channel/review/list',
  action = '/api/v1/content/channel/review/action',
  stats = '/api/v1/content/channel/review/stats',
  detail = '/api/v1/content/channel/review/detail',
}

export interface ChannelReviewVO {
  id: string;
  channelName: string;
  channelType: string;
  applicantName: string;
  applicationType: string;
  submitTime: string;
  status: string;
  isTimeout: boolean;
}

export interface ReviewDetailVO {
  id: string;
  channelName: string;
  channelDescription: string;
  channelIcon: string;
  channelCover: string;
  channelCategory: string;
  applicantName: string;
  applicantInfo: string;
  submitTime: string;
  historyRecords: { time: string; action: string; operator: string; result: string }[];
}

export interface ReviewListQuery {
  channelType?: string;
  applicationType?: string;
  status?: string;
  submitTimeStart?: string;
  submitTimeEnd?: string;
  keyword?: string;
  current?: number;
  size?: number;
}

export interface ReviewActionReq {
  reviewId: string;
  action: 'APPROVE' | 'REJECT' | 'RETURN';
  reason?: string;
}

/** 获取审核列表 */
export const getReviewList = (params: ReviewListQuery) =>
  defHttp.get<{ records: ChannelReviewVO[]; total: number }>({ url: Api.list, params });

/** 审核操作 */
export const executeReview = (data: ReviewActionReq) =>
  defHttp.post({ url: Api.action, data });

/** 审核统计 */
export const getReviewStats = () =>
  defHttp.get<{ total: number; timeoutCount: number }>({ url: Api.stats });

/** 获取审核详情 */
export const getReviewDetail = (id: string) =>
  defHttp.get<ReviewDetailVO>({ url: `${Api.detail}/${id}` });
