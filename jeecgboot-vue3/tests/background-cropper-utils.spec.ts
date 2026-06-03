import { calculateTargetCropArea, ASPECT_16_9 } from '/@/views/content/profile/components/backgroundCropper';

describe('backgroundCropper utilities', () => {
  describe('ASPECT_16_9', () => {
    it('is 16/9 ≈ 1.7777', () => {
      expect(ASPECT_16_9).toBeCloseTo(16 / 9, 5);
    });
  });

  describe('calculateTargetCropArea', () => {
    it('returns full width and reduced height for a wide image', () => {
      // 4000x3000, aspect 1.333 < 1.777 → too tall, crop height
      // img_w = 4000, img_h = 4000 * 9/16 = 2250
      const r = calculateTargetCropArea(4000, 3000);
      expect(r).toEqual({ width: 4000, height: 2250 });
    });

    it('returns reduced width and full height for a tall image', () => {
      // 1000x1000, aspect 1.0 < 1.777 → too tall, crop height
      // img_w = 1000, img_h = 1000 * 9/16 = 562.5
      const r = calculateTargetCropArea(1000, 1000);
      expect(r.width).toBe(1000);
      expect(r.height).toBeCloseTo(562.5, 5);
    });

    it('returns full image when already 16:9', () => {
      const r = calculateTargetCropArea(1920, 1080);
      expect(r).toEqual({ width: 1920, height: 1080 });
    });

    it('returns full height and reduced width for a square-ish wider image', () => {
      // 2000x1000, aspect 2.0 > 1.777 → too wide, crop width
      // img_w = 1000 * 16/9 = 1777.77..., img_h = 1000
      const r = calculateTargetCropArea(2000, 1000);
      expect(r.width).toBeCloseTo(1777.777, 2);
      expect(r.height).toBe(1000);
    });

    it('result maintains exactly 16:9 ratio', () => {
      const cases: Array<[number, number]> = [
        [4000, 3000],
        [800, 600],
        [1000, 1000],
        [1920, 1080],
        [3000, 2000],
        [500, 1000],
      ];
      for (const [w, h] of cases) {
        const r = calculateTargetCropArea(w, h);
        expect(r.width / r.height).toBeCloseTo(ASPECT_16_9, 5);
        // also fits inside original
        expect(r.width).toBeLessThanOrEqual(w + 1e-6);
        expect(r.height).toBeLessThanOrEqual(h + 1e-6);
      }
    });
  });
});
