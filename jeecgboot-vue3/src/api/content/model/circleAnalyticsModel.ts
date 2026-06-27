/** 趋势数据点 — 对应后端 CircleDataStatisticsVO.DailyTrend */
export interface DailyTrend {
  date: string;
  newMemberCount: number;
  newPostCount: number;
  activeCount: number;
}

/** 圈子统计数据响应 — 对应后端 CircleDataStatisticsVO */
export interface CircleDataStatisticsVO {
  memberCount: number;
  newMemberCount: number;
  postCount: number;
  newPostCount: number;
  activeCount: number;
  dailyTrends: DailyTrend[];
}

/** 推荐圈子项 — 对应后端 CircleRecommendVO.CircleRecommendItem */
export interface CircleRecommendItem {
  circleId: string;
  circleName: string;
  description: string;
  memberCount: number;
  category: string;
  privacyType: string;
  sourceId: string;
  why?: string;
}

/** 推荐圈子响应 — 对应后端 CircleRecommendVO */
export interface CircleRecommendVO {
  items: CircleRecommendItem[];
  personalizationEnabled?: boolean;
}

/** 榜单圈子项 — 对应后端 CircleRankingVO.CircleRankingItem */
export interface CircleRankingItem {
  rank: number;
  circleId: string;
  circleName: string;
  description: string;
  memberCount: number;
  category: string;
  createTime: string;
}

/** 榜单响应 — 对应后端 CircleRankingVO */
export interface CircleRankingVO {
  type: string;
  items: CircleRankingItem[];
}

/** 推荐曝光上报请求 */
export interface RecommendExposureReq {
  circleIds: string[];
  source: string;
}

/** 时间范围 */
export interface DateRange {
  startDate: string;
  endDate: string;
}
