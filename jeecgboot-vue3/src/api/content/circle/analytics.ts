import { defHttp } from '/@/utils/http/axios';
import type { CircleDataStatisticsVO, DateRange } from '/@/api/content/model/circleAnalyticsModel';

enum Api {
  Statistics = '/api/v1/content/circle',
  Export = '/api/v1/content/circle',
}

export const getCircleStatistics = (circleId: string, params: DateRange) =>
  defHttp.get<CircleDataStatisticsVO>({
    url: `${Api.Statistics}/${circleId}/data/statistics`,
    params,
  });

export const exportCircleStatisticsCsv = (circleId: string, params: DateRange) =>
  defHttp.get<Blob>({
    url: `${Api.Export}/${circleId}/data/export`,
    params,
    responseType: 'blob',
  });
