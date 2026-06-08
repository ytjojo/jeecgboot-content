/** 频道类型 */
export type ChannelType = 'system' | 'personal' | 'organization';

/** 频道基本信息（发现页通用） */
export interface ChannelInfo {
  id: string;
  name: string;
  description: string;
  iconUrl: string;
  coverUrl?: string;
  channelType: ChannelType;
  categoryName: string;
  subscriberCount: number;
  activeScore?: number;
  createdAt: string;
}

/** 推荐频道 VO */
export interface ChannelRecommendationVO extends ChannelInfo {
  recommendReason?: string;
  score?: number;
}

/** 搜索结果 VO */
export interface ChannelSearchResultVO extends ChannelInfo {
  matchType?: 'name' | 'tag' | 'description';
  matchReason?: string;
  highlightName?: string;
}

/** 排行榜项 VO */
export interface ChannelRankingItemVO extends ChannelInfo {
  rank: number;
  score: number;
  dimension?: string;
}

/** 编辑精选 VO */
export interface ChannelEditorialPickVO {
  id: string;
  channelId: string;
  channelInfo: ChannelInfo;
  recommendation: string;
  startTime: string;
  endTime: string;
  status: 'active' | 'expired' | 'abnormal';
  operatorName?: string;
  operateTime?: string;
}

/** 分类树节点 */
export interface CategoryTreeVO {
  id: string;
  name: string;
  parentId: string;
  level: number;
  sortOrder: number;
  status: 'enabled' | 'disabled';
  channelCount?: number;
  children: CategoryTreeVO[];
}

/** 频道标签 VO */
export interface ChannelTagVO {
  id: string;
  name: string;
  contentCount?: number;
  channelId: string;
  createdAt: string;
}

/** 发现页聚合数据 */
export interface DiscoveryHomeVO {
  recommendations: ChannelRecommendationVO[];
  hotRanking: ChannelRankingItemVO[];
  newRanking: ChannelRankingItemVO[];
  systemRanking: ChannelRankingItemVO[];
  editorialPicks: ChannelEditorialPickVO[];
  categories: CategoryTreeVO[];
}

/** 分页结果 */
export interface PageResult<T> {
  records: T[];
  total: number;
  page: number;
  pageSize: number;
}

/** 搜索参数 */
export interface ChannelSearchParams {
  keyword: string;
  channelType?: string;
  categoryId?: string;
  sortBy?: 'relevance' | 'active' | 'subscriber' | 'created';
  page?: number;
  pageSize?: number;
}

/** 分类浏览参数 */
export interface ChannelBrowseParams {
  categoryId?: string;
  channelType?: string;
  sortBy?: 'subscriber' | 'active' | 'created';
  page?: number;
  pageSize?: number;
}

/** 排行榜参数 */
export interface ChannelRankingParams {
  dimension?: 'day' | 'week' | 'month';
  page?: number;
  pageSize?: number;
}

/** 分类表单数据 */
export interface CategoryFormData {
  name: string;
  parentId?: string;
  sortOrder?: number;
}

/** 编辑精选表单数据 */
export interface EditorialPickFormData {
  channelId: string;
  recommendation: string;
  startTime: string;
  endTime: string;
}

/** 标签表单数据 */
export interface TagFormData {
  name: string;
}

/** 不感兴趣请求 */
export interface NotInterestedReq {
  channelId: string;
  reason?: string;
}

/** 搜索反馈请求 */
export interface SearchFeedbackReq {
  keyword: string;
  helpful: boolean;
}
