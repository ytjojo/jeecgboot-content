import {
  isMobileViewport,
  resolveSelectComponent,
  mapOptionsToActionItems,
} from '/@/views/content/profile/privacy/useResponsiveSelect';

describe('privacy responsive select (C9)', () => {
  describe('isMobileViewport', () => {
    it('returns true when width < 768', () => {
      expect(isMobileViewport(375)).toBe(true);
      expect(isMobileViewport(767)).toBe(true);
      expect(isMobileViewport(0)).toBe(true);
    });

    it('returns false when width >= 768', () => {
      expect(isMobileViewport(768)).toBe(false);
      expect(isMobileViewport(1024)).toBe(false);
      expect(isMobileViewport(1920)).toBe(false);
    });

    it('handles edge case at exact breakpoint', () => {
      expect(isMobileViewport(768)).toBe(false);
      expect(isMobileViewport(767)).toBe(true);
    });
  });

  describe('resolveSelectComponent', () => {
    it('returns "action-sheet" for mobile', () => {
      expect(resolveSelectComponent(true)).toBe('action-sheet');
    });

    it('returns "select" for desktop', () => {
      expect(resolveSelectComponent(false)).toBe('select');
    });
  });

  describe('mapOptionsToActionItems', () => {
    const options = [
      { value: 'PUBLIC', label: '公开' },
      { value: 'FOLLOWERS_ONLY', label: '仅关注者' },
      { value: 'PRIVATE', label: '仅自己' },
    ];

    it('maps select options to action items with value, label, and text', () => {
      const items = mapOptionsToActionItems(options);
      expect(items).toHaveLength(3);
      expect(items[0]).toEqual({ key: 'PUBLIC', label: '公开', value: 'PUBLIC' });
      expect(items[1]).toEqual({ key: 'FOLLOWERS_ONLY', label: '仅关注者', value: 'FOLLOWERS_ONLY' });
      expect(items[2]).toEqual({ key: 'PRIVATE', label: '仅自己', value: 'PRIVATE' });
    });

    it('returns empty array for empty input', () => {
      expect(mapOptionsToActionItems([])).toEqual([]);
    });

    it('preserves order of options', () => {
      const items = mapOptionsToActionItems(options);
      expect(items.map((i) => i.key)).toEqual(['PUBLIC', 'FOLLOWERS_ONLY', 'PRIVATE']);
    });
  });
});
