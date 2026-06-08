import { defHttp } from '/@/utils/http/axios';

enum Api {
  create = '/api/v1/content/channel/export/create',
  status = '/api/v1/content/channel/export/status',
  download = '/api/v1/content/channel/export/download',
  history = '/api/v1/content/channel/export/history',
}

export interface ExportCreateReq {
  channelId: string;
  fields: string[];
  format: 'EXCEL' | 'CSV';
  timeRange: { start: string; end: string };
}

export interface ExportTaskVO {
  taskId: string;
  exportTime: string;
  timeRange: { start: string; end: string };
  format: string;
  rowCount: number;
  status: 'processing' | 'completed' | 'failed';
  downloadUrl?: string;
  failReason?: string;
  expiredAt?: string;
}

export interface ExportHistoryQuery {
  channelId: string;
  current?: number;
  size?: number;
}

export interface ExportHistoryResult {
  records: ExportTaskVO[];
  total: number;
}

/** 创建导出任务 */
export const createExportTask = (data: ExportCreateReq) =>
  defHttp.post<ExportTaskVO>({ url: Api.create, data });

/** 查询导出任务状态 */
export const getExportTaskStatus = (taskId: string) =>
  defHttp.get<ExportTaskVO>({ url: Api.status, params: { taskId } });

/** 下载导出文件 */
export const downloadExportFile = (taskId: string) =>
  defHttp.get<Blob>({ url: Api.download, params: { taskId }, responseType: 'blob' });

/** 获取导出历史列表 */
export const getExportHistory = (params: ExportHistoryQuery) =>
  defHttp.get<ExportHistoryResult>({ url: Api.history, params });
