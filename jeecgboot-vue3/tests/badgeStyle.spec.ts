import { isKnownBadgeKey, getBadgeStyle, selectPrimaryBadge, partitionBadges } from '/@/views/content/profile/components/badgeStyle';

describe('badgeStyle', () => {
  describe('isKnownBadgeKey', () => {
    it('accepts all documented visual style keys', () => {
      expect(isKnownBadgeKey('OFFICIAL')).toBe(true);
      expect(isKnownBadgeKey('ENTERPRISE')).toBe(true);
      expect(isKnownBadgeKey('CREATOR')).toBe(true);
      expect(isKnownBadgeKey('INDIVIDUAL')).toBe(true);
      expect(isKnownBadgeKey('REAL_NAME')).toBe(true);
      expect(isKnownBadgeKey('MOBILE')).toBe(true);
      expect(isKnownBadgeKey('EMAIL')).toBe(true);
    });
    it('rejects unknown keys, null and empty strings', () => {
      expect(isKnownBadgeKey('LEGACY_GOLD')).toBe(false);
      expect(isKnownBadgeKey(null)).toBe(false);
      expect(isKnownBadgeKey(undefined)).toBe(false);
      expect(isKnownBadgeKey('')).toBe(false);
    });
  });

  describe('getBadgeStyle', () => {
    it('returns the dictionary entry for known keys', () => {
      expect(getBadgeStyle('OFFICIAL').key).toBe('OFFICIAL');
      expect(getBadgeStyle('MOBILE').tooltip).toBe('手机绑定');
    });
    it('returns DEFAULT for unknown keys', () => {
      const style = getBadgeStyle('UNKNOWN_TYPE');
      expect(style.key).toBe('DEFAULT');
      expect(style.backgroundColor).toBe('#bfbfbf');
    });
    it('returns DEFAULT for null/undefined', () => {
      expect(getBadgeStyle(null).key).toBe('DEFAULT');
      expect(getBadgeStyle(undefined).key).toBe('DEFAULT');
    });
  });

  describe('selectPrimaryBadge', () => {
    it('returns the highest priority style in the list', () => {
      const badges = [
        { visualStyleKey: 'EMAIL' },
        { visualStyleKey: 'OFFICIAL' },
        { visualStyleKey: 'MOBILE' },
      ];
      const primary = selectPrimaryBadge(badges as any);
      expect(primary.key).toBe('OFFICIAL');
    });
    it('returns DEFAULT for empty list', () => {
      expect(selectPrimaryBadge([]).key).toBe('DEFAULT');
    });
    it('returns DEFAULT when all badges have unknown keys', () => {
      const badges = [{ visualStyleKey: 'LEGACY' }, { visualStyleKey: 'FOO' }];
      expect(selectPrimaryBadge(badges as any).key).toBe('DEFAULT');
    });
  });

  describe('partitionBadges', () => {
    it('separates known and unknown badges', () => {
      const badges = [
        { badgeId: '1', visualStyleKey: 'EMAIL' },
        { badgeId: '2', visualStyleKey: 'LEGACY' },
        { badgeId: '3', visualStyleKey: 'OFFICIAL' },
      ];
      const { known, unknown } = partitionBadges(badges as any);
      expect(known.map((b) => b.badgeId)).toEqual(['3', '1']);
      expect(unknown.map((b) => b.badgeId)).toEqual(['2']);
    });
    it('returns empty partitions for empty input', () => {
      expect(partitionBadges([])).toEqual({ known: [], unknown: [] });
    });
  });
});
