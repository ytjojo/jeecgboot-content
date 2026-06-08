import { describe, it, expect, vi, beforeEach } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useChannelExportStore } from '/@/store/modules/channelExport';

vi.mock('/@/api/content/channel/export', () => ({
  createExportTask: vi.fn().mockResolvedValue({
    taskId: 'task-1',
    exportTime: '2026-06-08 10:00:00',
    format: 'EXCEL',
    rowCount: 15000,
    status: 'processing',
  }),
  getExportTaskStatus: vi.fn().mockResolvedValue({
    taskId: 'task-1',
    status: 'completed',
    downloadUrl: '/download/task-1.xlsx',
  }),
  getExportHistory: vi.fn().mockResolvedValue({
    records: [
      { taskId: 'task-1', status: 'completed', format: 'EXCEL', rowCount: 5000 },
      { taskId: 'task-2', status: 'processing', format: 'CSV', rowCount: 0 },
    ],
    total: 2,
  }),
  downloadExportFile: vi.fn().mockResolvedValue(new Blob()),
}));

describe('useChannelExportStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('submitExport 应提交导出任务', async () => {
    const store = useChannelExportStore();
    store.setChannelId('test-channel');
    const task = await store.submitExport({
      channelId: 'test-channel',
      fields: ['channelName', 'subscriberCount'],
      format: 'EXCEL',
      timeRange: { start: '2026-01-01', end: '2026-06-08' },
    });
    expect(task).toBeTruthy();
    expect(task?.taskId).toBe('task-1');
    expect(task?.status).toBe('processing');
  });

  it('fetchHistory 应加载导出历史', async () => {
    const store = useChannelExportStore();
    store.setChannelId('test-channel');
    await store.fetchHistory();
    expect(store.exportHistory).toHaveLength(2);
    expect(store.historyTotal).toBe(2);
  });

  it('queryTaskStatus 应查询单个任务状态', async () => {
    const store = useChannelExportStore();
    const result = await store.queryTaskStatus('task-1');
    expect(result).toBeTruthy();
    expect(result?.status).toBe('completed');
  });

  it('startPolling 应启动轮询', () => {
    vi.useFakeTimers();
    const store = useChannelExportStore();
    store.startPolling();
    expect(store.isPolling).toBe(true);
    expect(store.pollingTimer).toBeTruthy();
    store.stopPolling();
    vi.useRealTimers();
  });

  it('stopPolling 应停止轮询', () => {
    const store = useChannelExportStore();
    store.startPolling();
    store.stopPolling();
    expect(store.isPolling).toBe(false);
    expect(store.pollingTimer).toBeNull();
  });

  it('clearPolling 应在页面离开时清理', () => {
    const store = useChannelExportStore();
    store.startPolling();
    store.clearPolling();
    expect(store.isPolling).toBe(false);
  });
});
