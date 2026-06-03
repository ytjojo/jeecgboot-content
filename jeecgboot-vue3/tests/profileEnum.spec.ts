import { PrivacyVisibility, OnlineStatusVisibility, isPrivacyVisibility, isOnlineStatusVisibility, PRIVACY_VISIBILITY_OPTIONS, ONLINE_STATUS_VISIBILITY_OPTIONS } from '/@/enums/profileEnum';

describe('profileEnum', () => {
  describe('PrivacyVisibility', () => {
    it('exposes the four canonical privacy scopes', () => {
      expect(PrivacyVisibility.PUBLIC).toBe('PUBLIC');
      expect(PrivacyVisibility.FOLLOWERS_ONLY).toBe('FOLLOWERS_ONLY');
      expect(PrivacyVisibility.MUTUAL_ONLY).toBe('MUTUAL_ONLY');
      expect(PrivacyVisibility.PRIVATE).toBe('PRIVATE');
    });

    it('classifies each canonical value via isPrivacyVisibility', () => {
      expect(isPrivacyVisibility('PUBLIC')).toBe(true);
      expect(isPrivacyVisibility('FOLLOWERS_ONLY')).toBe(true);
      expect(isPrivacyVisibility('MUTUAL_ONLY')).toBe(true);
      expect(isPrivacyVisibility('PRIVATE')).toBe(true);
    });

    it('rejects values outside the four canonical scopes', () => {
      expect(isPrivacyVisibility('HIDDEN')).toBe(false);
      expect(isPrivacyVisibility('public')).toBe(false);
      expect(isPrivacyVisibility('')).toBe(false);
      expect(isPrivacyVisibility(null)).toBe(false);
      expect(isPrivacyVisibility(undefined)).toBe(false);
      expect(isPrivacyVisibility(123)).toBe(false);
    });

    it('provides display-friendly option labels', () => {
      expect(PRIVACY_VISIBILITY_OPTIONS).toEqual([
        { value: 'PUBLIC', label: '公开' },
        { value: 'FOLLOWERS_ONLY', label: '仅关注者' },
        { value: 'MUTUAL_ONLY', label: '互关可见' },
        { value: 'PRIVATE', label: '仅自己' },
      ]);
    });
  });

  describe('OnlineStatusVisibility', () => {
    it('exposes the three allowed online-status scopes', () => {
      expect(OnlineStatusVisibility.PUBLIC).toBe('PUBLIC');
      expect(OnlineStatusVisibility.HIDDEN).toBe('HIDDEN');
      expect(OnlineStatusVisibility.MUTUAL_ONLY).toBe('MUTUAL_ONLY');
    });

    it('classifies the three allowed values via isOnlineStatusVisibility', () => {
      expect(isOnlineStatusVisibility('PUBLIC')).toBe(true);
      expect(isOnlineStatusVisibility('HIDDEN')).toBe(true);
      expect(isOnlineStatusVisibility('MUTUAL_ONLY')).toBe(true);
    });

    it('rejects PRIVATE — online status has no PRIVATE scope', () => {
      expect(isOnlineStatusVisibility('PRIVATE')).toBe(false);
    });

    it('rejects non-string / out-of-vocabulary values', () => {
      expect(isOnlineStatusVisibility('public')).toBe(false);
      expect(isOnlineStatusVisibility('FOLLOWERS_ONLY')).toBe(false);
      expect(isOnlineStatusVisibility('')).toBe(false);
      expect(isOnlineStatusVisibility(null)).toBe(false);
      expect(isOnlineStatusVisibility(undefined)).toBe(false);
    });

    it('provides display-friendly option labels without PRIVATE', () => {
      expect(ONLINE_STATUS_VISIBILITY_OPTIONS).toEqual([
        { value: 'PUBLIC', label: '公开' },
        { value: 'HIDDEN', label: '完全隐藏' },
        { value: 'MUTUAL_ONLY', label: '仅互关' },
      ]);
    });
  });
});
