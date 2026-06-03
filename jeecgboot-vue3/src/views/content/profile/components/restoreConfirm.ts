import type { HistoryType } from '/@/api/content/profile/types';

export const HISTORY_RETENTION_DAYS = 180;

export const HISTORY_RETENTION_NOTICE = '历史记录仅保留 180 天，过期后将无法恢复。';

export function getRestoreTypeLabel(tab: HistoryType): '昵称' | '头像' {
  return tab === 'NICKNAME' ? '昵称' : '头像';
}

export interface RestoreConfirmOptions {
  title: string;
  content: string;
  okText: string;
  cancelText: string;
}

export function buildRestoreConfirmOptions(tab: HistoryType): RestoreConfirmOptions {
  const typeLabel = getRestoreTypeLabel(tab);
  return {
    title: '确认恢复？',
    content: `恢复后当前 ${typeLabel} 将被覆盖，确认继续？`,
    okText: '确认',
    cancelText: '取消',
  };
}
