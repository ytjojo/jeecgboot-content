import {
  HISTORY_RETENTION_DAYS,
  HISTORY_RETENTION_NOTICE,
  getRestoreTypeLabel,
  buildRestoreConfirmOptions,
} from '/@/views/content/profile/components/restoreConfirm';

describe('restoreConfirm helpers', () => {
  describe('HISTORY_RETENTION_DAYS', () => {
    it('is 180 per spec', () => {
      expect(HISTORY_RETENTION_DAYS).toBe(180);
    });
  });

  describe('HISTORY_RETENTION_NOTICE', () => {
    it('mentions 180 天', () => {
      expect(HISTORY_RETENTION_NOTICE).toContain('180 天');
    });
    it('mentions 无法恢复', () => {
      expect(HISTORY_RETENTION_NOTICE).toContain('无法恢复');
    });
  });

  describe('getRestoreTypeLabel', () => {
    it('returns 昵称 for NICKNAME tab', () => {
      expect(getRestoreTypeLabel('NICKNAME')).toBe('昵称');
    });
    it('returns 头像 for AVATAR tab', () => {
      expect(getRestoreTypeLabel('AVATAR')).toBe('头像');
    });
  });

  describe('buildRestoreConfirmOptions', () => {
    it('builds confirm options for NICKNAME tab with 昵称', () => {
      const opts = buildRestoreConfirmOptions('NICKNAME');
      expect(opts.title).toBe('确认恢复？');
      expect(opts.content).toBe('恢复后当前 昵称 将被覆盖，确认继续？');
      expect(opts.okText).toBe('确认');
      expect(opts.cancelText).toBe('取消');
    });

    it('builds confirm options for AVATAR tab with 头像', () => {
      const opts = buildRestoreConfirmOptions('AVATAR');
      expect(opts.title).toBe('确认恢复？');
      expect(opts.content).toBe('恢复后当前 头像 将被覆盖，确认继续？');
      expect(opts.okText).toBe('确认');
      expect(opts.cancelText).toBe('取消');
    });

    it('returns the expected shape (string fields, no functions)', () => {
      const opts = buildRestoreConfirmOptions('NICKNAME');
      expect(Object.keys(opts).sort()).toEqual(['cancelText', 'content', 'okText', 'title']);
      for (const v of Object.values(opts)) {
        expect(typeof v).toBe('string');
      }
    });
  });
});
