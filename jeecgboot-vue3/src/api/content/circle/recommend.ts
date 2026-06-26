import { defHttp } from '/@/utils/http/axios';
import type { CircleRecommendVO, RecommendExposureReq } from '/@/api/content/model/circleAnalyticsModel';

enum Api {
  Recommend = '/api/v1/content/circle/recommend',
  Exposure = '/api/v1/content/circle/recommend/exposure',
  Click = '/api/v1/content/circle/recommend/click',
}

export const getRecommendList = (params?: { limit?: number }) =>
  defHttp.get<CircleRecommendVO>({ url: Api.Recommend, params });

export const reportRecommendExposure = (data: RecommendExposureReq) =>
  defHttp.post({ url: Api.Exposure, data });

export const reportRecommendClick = (sourceId: string) =>
  defHttp.post({ url: Api.Click, params: { sourceId } });
