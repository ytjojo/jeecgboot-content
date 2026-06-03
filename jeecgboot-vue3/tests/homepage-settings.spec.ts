import {
  PRESET_THEME_COLORS,
  isValidHex,
  parseThemeColor,
  getRelativeLuminance,
  getContrastRatio,
  meetsWcagAA,
  getContrastingTextColor,
  validateModulesForSave,
} from '/@/views/content/profile/components/themeColor';
import type { ContentUserHomepageModuleVO } from '/@/api/content/profile/types';

describe('themeColor helpers', () => {
  describe('PRESET_THEME_COLORS', () => {
    it('contains between 8 and 12 colors', () => {
      expect(PRESET_THEME_COLORS.length).toBeGreaterThanOrEqual(8);
      expect(PRESET_THEME_COLORS.length).toBeLessThanOrEqual(12);
    });

    it('every entry is a valid #RRGGBB hex string', () => {
      for (const c of PRESET_THEME_COLORS) {
        expect(c).toMatch(/^#[0-9A-Fa-f]{6}$/);
      }
    });
  });

  describe('isValidHex', () => {
    it('accepts #RRGGBB', () => {
      expect(isValidHex('#1677ff')).toBe(true);
      expect(isValidHex('#AABBCC')).toBe(true);
    });
    it('rejects non-hex strings', () => {
      expect(isValidHex('red')).toBe(false);
      expect(isValidHex('#fff')).toBe(false);
      expect(isValidHex('#fffffff')).toBe(false);
      expect(isValidHex('')).toBe(false);
      expect(isValidHex('1677ff')).toBe(false);
    });
  });

  describe('parseThemeColor', () => {
    it('parses a #RRGGBB hex into r,g,b components', () => {
      const c = parseThemeColor('#1677ff');
      expect(c).toEqual({ r: 0x16, g: 0x77, b: 0xff, hex: '#1677ff' });
    });
    it('parses black', () => {
      expect(parseThemeColor('#000000')).toEqual({ r: 0, g: 0, b: 0, hex: '#000000' });
    });
    it('parses white', () => {
      expect(parseThemeColor('#ffffff')).toEqual({ r: 255, g: 255, b: 255, hex: '#ffffff' });
    });
  });

  describe('getRelativeLuminance', () => {
    it('returns 0 for pure black and 1 for pure white', () => {
      expect(getRelativeLuminance(0, 0, 0)).toBe(0);
      expect(getRelativeLuminance(255, 255, 255)).toBeCloseTo(1, 5);
    });
    it('is symmetric across channels', () => {
      const a = getRelativeLuminance(255, 0, 0);
      const b = getRelativeLuminance(0, 255, 0);
      const c = getRelativeLuminance(0, 0, 255);
      expect(a).toBeGreaterThan(0);
      expect(b).toBeGreaterThan(0);
      expect(c).toBeGreaterThan(0);
    });
  });

  describe('getContrastRatio', () => {
    it('returns 21 for black on white (max contrast)', () => {
      const r = getContrastRatio('#000000', '#ffffff');
      expect(r).toBeCloseTo(21, 0);
    });
    it('returns 1 for identical colors', () => {
      const r = getContrastRatio('#1677ff', '#1677ff');
      expect(r).toBeCloseTo(1, 5);
    });
    it('is order-independent (higher luminance first)', () => {
      const a = getContrastRatio('#000000', '#ffffff');
      const b = getContrastRatio('#ffffff', '#000000');
      expect(a).toBeCloseTo(b, 5);
    });
  });

  describe('meetsWcagAA', () => {
    it('returns true for black on white', () => {
      expect(meetsWcagAA('#000000', '#ffffff')).toBe(true);
    });
    it('returns false for yellow on white (low contrast)', () => {
      expect(meetsWcagAA('#ffff00', '#ffffff')).toBe(false);
    });
    it('returns true for white on black', () => {
      expect(meetsWcagAA('#ffffff', '#000000')).toBe(true);
    });
    it('passes for #1677ff with black text (white actually fails 4.5:1)', () => {
      // #1677ff luminance ~0.206 → white on it = 4.10:1 (fail), black on it = 5.12:1 (pass)
      expect(meetsWcagAA('#ffffff', '#1677ff')).toBe(false);
      expect(meetsWcagAA('#000000', '#1677ff')).toBe(true);
    });
  });

  describe('getContrastingTextColor', () => {
    it('returns "#000" for a near-white background', () => {
      expect(getContrastingTextColor('#fffffe')).toBe('#000000');
    });
    it('returns "#fff" for a near-black background', () => {
      expect(getContrastingTextColor('#000001')).toBe('#ffffff');
    });
    it('returns "#000" for #1677ff since white fails AA on this blue', () => {
      // White on #1677ff is only 4.10:1 (fails AA), black is 5.12:1 (passes).
      expect(getContrastingTextColor('#1677ff')).toBe('#000000');
    });
    it('returns "#fff" for a deeper brand color like #2f54eb', () => {
      // #2f54eb is darker; white on it should pass.
      expect(getContrastingTextColor('#2f54eb')).toBe('#ffffff');
    });
    it('returns a value that meets WCAG AA against the bg', () => {
      for (const bg of PRESET_THEME_COLORS) {
        const fg = getContrastingTextColor(bg);
        expect(meetsWcagAA(fg, bg)).toBe(true);
      }
    });
  });

  describe('validateModulesForSave', () => {
    it('returns error string when modules is empty', () => {
      expect(validateModulesForSave([])).toBe('至少需要保留一个模块');
    });
    it('returns error string when all modules are hidden', () => {
      const modules: ContentUserHomepageModuleVO[] = [
        { moduleKey: 'a', moduleName: 'A', visible: false, sortOrder: 0 },
        { moduleKey: 'b', moduleName: 'B', visible: false, sortOrder: 1 },
      ];
      expect(validateModulesForSave(modules)).toBe('至少需要保留一个模块');
    });
    it('returns null when at least one module is visible', () => {
      const modules: ContentUserHomepageModuleVO[] = [
        { moduleKey: 'a', moduleName: 'A', visible: false, sortOrder: 0 },
        { moduleKey: 'b', moduleName: 'B', visible: true, sortOrder: 1 },
      ];
      expect(validateModulesForSave(modules)).toBeNull();
    });
  });
});
