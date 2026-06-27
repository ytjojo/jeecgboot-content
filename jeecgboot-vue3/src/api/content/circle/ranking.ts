import { defHttp } from '/@/utils/http/axios';
import type { CircleRankingVO } from '/@/api/content/model/circleAnalyticsModel';

enum Api {
  HotRank = '/api/v1/content/circle/ranking/hot',
  NewRank = '/api/v1/content/circle/ranking/new',
}

export const getHotRankList = (params?: { limit?: number }) =>
  defHttp.get<CircleRankingVO>({ url: Api.HotRank, params });

export const getNewRankList = (params?: { limit?: number }) =>
  defHttp.get<CircleRankingVO>({ url: Api.NewRank, params });
