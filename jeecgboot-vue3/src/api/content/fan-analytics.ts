import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/content/user/fan/list',
  trend = '/content/user/fan/trend',
  profile = '/content/user/fan/profile',
  exportCsv = '/content/user/fan/export',
}

/** 查询粉丝列表 */
export const listFans = (userId: string, params?: { keyword?: string; pageNo?: number; pageSize?: number }) =>
  defHttp.get({ url: Api.list, params: { userId, ...params } });

/** 查询粉丝趋势数据 */
export const getFanTrend = (userId: string, params?: { period?: string; startDate?: string; endDate?: string }) =>
  defHttp.get({ url: Api.trend, params: { userId, ...params } });

/** 查询粉丝画像 */
export const getFanProfile = (userId: string) =>
  defHttp.get({ url: Api.profile, params: { userId } });

/** 导出粉丝数据 CSV */
export const exportFansCsv = (userId: string) =>
  defHttp.get({ url: Api.exportCsv, params: { userId }, responseType: 'blob' });
