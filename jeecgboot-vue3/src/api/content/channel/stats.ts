import { defHttp } from '/@/utils/http/axios';

enum Api {
  core = '/api/v1/content/channel/stats/core',
  trend = '/api/v1/content/channel/stats/trend',
  hotContent = '/api/v1/content/channel/stats/hot-content',
  userAnalysis = '/api/v1/content/channel/stats/user-analysis',
  interaction = '/api/v1/content/channel/stats/interaction',
}

export interface CoreStatsVO {
  subscriberCount: number;
  contentCount: number;
  pv: number;
  uv: number;
  subscriberTrend?: number;
  contentTrend?: number;
  pvTrend?: number;
  uvTrend?: number;
  updateTime?: string;
}

export interface TrendQuery {
  channelId: string;
  range: 'day' | 'week' | 'month' | 'custom';
  startDate?: string;
  endDate?: string;
}

export interface TrendVO {
  date: string;
  subscriberCount: number;
  contentCount: number;
  pv: number;
  uv: number;
}

export interface HotContentQuery {
  channelId: string;
  period: '7d' | '30d' | '90d';
}

export interface HotContentVO {
  id: string;
  title: string;
  contentType: string;
  publishTime: string;
  interactionCount: number;
  rank: number;
}

export interface UserAnalysisVO {
  subscribeTrend: { date: string; subscribe: number; unsubscribe: number }[];
  activityDistribution: { level: string; count: number; percentage: number }[];
  contributionRank: { userId: string; userName: string; contribution: number; rank: number }[];
}

export interface InteractionVO {
  likeCount: number;
  commentCount: number;
  favoriteCount: number;
  shareCount: number;
  visitCount: number;
  newContentCount: number;
  contentTypeDistribution: { type: string; count: number }[];
}

/** 获取核心指标 */
export const getCoreStats = (params: { channelId: string }) =>
  defHttp.get<CoreStatsVO>({ url: Api.core, params });

/** 获取趋势数据 */
export const getTrendData = (params: TrendQuery) =>
  defHttp.get<TrendVO[]>({ url: Api.trend, params });

/** 获取热门内容 */
export const getHotContent = (params: HotContentQuery) =>
  defHttp.get<HotContentVO[]>({ url: Api.hotContent, params });

/** 获取用户分析 */
export const getUserAnalysis = (params: { channelId: string }) =>
  defHttp.get<UserAnalysisVO>({ url: Api.userAnalysis, params });

/** 获取互动数据 */
export const getInteraction = (params: { channelId: string }) =>
  defHttp.get<InteractionVO>({ url: Api.interaction, params });
