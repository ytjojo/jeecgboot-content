import { setActivePinia, createPinia } from 'pinia';
import { useCircleRecommendStore } from '/@/store/modules/circleRecommend';

vi.mock('/@/api/content/circle/recommend', () => ({
  getRecommendList: vi.fn(),
  reportRecommendExposure: vi.fn(),
  reportRecommendClick: vi.fn(),
}));
vi.mock('/@/api/content/circle/ranking', () => ({
  getHotRankList: vi.fn(),
  getNewRankList: vi.fn(),
}));

import { getRecommendList } from '/@/api/content/circle/recommend';
import { getHotRankList } from '/@/api/content/circle/ranking';

describe('useCircleRecommendStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it('should have correct initial state', () => {
    const store = useCircleRecommendStore();
    expect(store.recommendList).toEqual([]);
    expect(store.hotRankList).toEqual([]);
    expect(store.newRankList).toEqual([]);
    expect(store.activeTab).toBe('recommend');
    expect(store.fallbackMode).toBe(false);
  });

  it('should fetch recommend list and extract items from VO', async () => {
    const mockVO = { items: [{ circleId: '1', circleName: 'Test', privacyType: 'PUBLIC', memberCount: 10, category: '', description: '', sourceId: 's1' }] };
    (getRecommendList as any).mockResolvedValue(mockVO);

    const store = useCircleRecommendStore();
    await store.fetchRecommendList();

    expect(store.recommendList).toEqual(mockVO.items);
    expect(store.fallbackMode).toBe(false);
  });

  it('should enter fallback mode when recommend returns empty items and fetch hot rank', async () => {
    (getRecommendList as any).mockResolvedValue({ items: [] });
    (getHotRankList as any).mockResolvedValue({ type: 'HOT', items: [{ circleId: '1', circleName: 'Hot', memberCount: 100, rank: 1, category: '', description: '', createTime: '2024-01-01' }] });

    const store = useCircleRecommendStore();
    await store.fetchRecommendList();

    expect(store.fallbackMode).toBe(true);
    expect(store.hotRankList.length).toBeGreaterThan(0);
  });

  it('should enter fallback mode when recommend request fails', async () => {
    (getRecommendList as any).mockRejectedValue(new Error('Network error'));
    (getHotRankList as any).mockResolvedValue({ type: 'HOT', items: [{ circleId: '1', circleName: 'Hot', memberCount: 100, rank: 1, category: '', description: '', createTime: '2024-01-01' }] });

    const store = useCircleRecommendStore();
    await store.fetchRecommendList();

    expect(store.fallbackMode).toBe(true);
  });

  it('should clear fallback mode when recommend returns data', async () => {
    const mockVO = { items: [{ circleId: '1', circleName: 'Test', privacyType: 'PUBLIC', memberCount: 10, category: '', description: '', sourceId: 's1' }] };
    (getRecommendList as any).mockResolvedValue(mockVO);

    const store = useCircleRecommendStore();
    store.fallbackMode = true;
    await store.fetchRecommendList();

    expect(store.fallbackMode).toBe(false);
  });

  it('should cache tab data and not re-fetch', async () => {
    (getHotRankList as any).mockResolvedValue({ type: 'HOT', items: [{ circleId: '1', circleName: 'H', memberCount: 1, rank: 1, category: '', description: '', createTime: '' }] });

    const store = useCircleRecommendStore();
    await store.fetchHotRankList();
    await store.fetchHotRankList();

    expect(getHotRankList).toHaveBeenCalledTimes(1);
  });

  it('should set active tab', () => {
    const store = useCircleRecommendStore();
    store.setActiveTab('hot');
    expect(store.activeTab).toBe('hot');
  });
});
