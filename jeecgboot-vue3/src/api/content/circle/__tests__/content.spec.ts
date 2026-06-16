import { describe, it, expect, beforeEach, vi } from 'vitest';

// Mock defHttp（必须在 import 被测模块之前）
vi.mock('/@/utils/http/axios', () => ({
  defHttp: {
    put: vi.fn(),
  },
}));

import { defHttp } from '/@/utils/http/axios';
import { togglePin, toggleFeatured } from '../content';

describe('circle content API', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('togglePin', () => {
    it('应调用 PUT /api/v1/content/circle/content/{contentId}/pin?circleId={circleId}', async () => {
      vi.mocked(defHttp.put).mockResolvedValue({ code: 200, message: '操作成功' });

      await togglePin('content-001', 'circle-123');

      expect(defHttp.put).toHaveBeenCalledWith({
        url: '/api/v1/content/circle/content/content-001/pin',
        params: { circleId: 'circle-123' },
      });
    });
  });

  describe('toggleFeatured', () => {
    it('应调用 PUT /api/v1/content/circle/content/{contentId}/featured?circleId={circleId}', async () => {
      vi.mocked(defHttp.put).mockResolvedValue({ code: 200, message: '操作成功' });

      await toggleFeatured('content-002', 'circle-456');

      expect(defHttp.put).toHaveBeenCalledWith({
        url: '/api/v1/content/circle/content/content-002/featured',
        params: { circleId: 'circle-456' },
      });
    });
  });
});
