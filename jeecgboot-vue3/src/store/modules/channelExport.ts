import { defineStore } from 'pinia';
import { store } from '/@/store';
import {
  createExportTask,
  getExportTaskStatus,
  getExportHistory,
  downloadExportFile,
} from '/@/api/content/channel/export';
import type {
  ExportTaskVO,
  ExportCreateReq,
  ExportHistoryQuery,
} from '/@/api/content/channel/export';

interface ChannelExportState {
  channelId: string;
  exportHistory: ExportTaskVO[];
  historyTotal: number;
  loading: boolean;
  submitting: boolean;
  pollingTimer: ReturnType<typeof setInterval> | null;
  isPolling: boolean;
}

export const useChannelExportStore = defineStore({
  id: 'app-channel-export',
  state: (): ChannelExportState => ({
    channelId: '',
    exportHistory: [],
    historyTotal: 0,
    loading: false,
    submitting: false,
    pollingTimer: null,
    isPolling: false,
  }),
  actions: {
    setChannelId(channelId: string) {
      this.channelId = channelId;
    },
    /** 提交导出任务 */
    async submitExport(data: ExportCreateReq): Promise<ExportTaskVO | null> {
      this.submitting = true;
      try {
        const task = await createExportTask(data);
        await this.fetchHistory({ current: 1, size: 20 });
        return task;
      } finally {
        this.submitting = false;
      }
    },
    /** 获取导出历史 */
    async fetchHistory(params?: Partial<ExportHistoryQuery>) {
      this.loading = true;
      try {
        const res = await getExportHistory({
          channelId: this.channelId,
          current: 1,
          size: 20,
          ...params,
        });
        this.exportHistory = res.records || [];
        this.historyTotal = res.total || 0;
      } finally {
        this.loading = false;
      }
    },
    /** 查询单个任务状态 */
    async queryTaskStatus(taskId: string): Promise<ExportTaskVO | null> {
      try {
        return await getExportTaskStatus(taskId);
      } catch {
        return null;
      }
    },
    /** 下载导出文件 */
    async downloadFile(taskId: string): Promise<Blob | null> {
      try {
        return await downloadExportFile(taskId);
      } catch {
        return null;
      }
    },
    /** 启动任务轮询 */
    startPolling() {
      if (this.pollingTimer) return;
      this.isPolling = true;
      this.pollingTimer = setInterval(async () => {
        const processingTasks = this.exportHistory.filter(
          (t) => t.status === 'processing'
        );
        if (processingTasks.length === 0) {
          this.stopPolling();
          return;
        }
        // 批量更新处理中任务状态
        for (const task of processingTasks) {
          const updated = await this.queryTaskStatus(task.taskId);
          if (updated) {
            const idx = this.exportHistory.findIndex((t) => t.taskId === task.taskId);
            if (idx !== -1) {
              this.exportHistory[idx] = updated;
            }
          }
        }
      }, 3000);
    },
    /** 停止轮询 */
    stopPolling() {
      if (this.pollingTimer) {
        clearInterval(this.pollingTimer);
        this.pollingTimer = null;
      }
      this.isPolling = false;
    },
    /** 清理轮询 */
    clearPolling() {
      this.stopPolling();
    },
  },
});

export function useChannelExportStoreWithOut() {
  return useChannelExportStore(store);
}
