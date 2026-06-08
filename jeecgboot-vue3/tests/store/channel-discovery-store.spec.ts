import { describe, it, expect, beforeEach, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';

// Mock API
const mockGetDiscoveryHome = vi.fn();
const mockGetRecommendationList = vi.fn();
const mockGetColdStartRecommendations = vi.fn();
const mockGetHotRanking = vi.fn();
const mockGetNewRanking = vi.fn();
const mockGetSystemRanking = vi.fn();
const mockGetEditorialPickList = vi.fn();
const mockMarkNotInterested = vi.fn();

vi.mock('/@/api/content/channelDiscovery', () => ({
  getDiscoveryHome: (...args: any[]) => mockGetDiscoveryHome(...args),
  getRecommendationList: (...args: any[]) => mockGetRecommendationList(...args),
  getColdStartRecommendations: (...args: any[]) => mockGetColdStartRecommendations(...args),
  getHotRanking: (...args: any[]) => mockGetHotRanking(...args),
  getNewRanking: (...args: any[]) => mockGetNewRanking(...args),
  getSystemRanking: (...args: any[]) => mockGetSystemRanking(...args),
  getEditorialPickList: (...args: any[]) => mockGetEditorialPickList(...args),
  markNotInterested: (...args: any[]) => mockMarkNotInterested(...args),
}));

import { useChannelDiscoveryStore } from '/@/store/modules/channelDiscovery';

describe('useChannelDiscoveryStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it('should have correct initial state', () => {
    const store = useChannelDiscoveryStore();
    expect(store.recommendations).toEqual([]);
    expect(store.hotRanking).toEqual([]);
    expect(store.newRanking).toEqual([]);
    expect(store.systemRanking).toEqual([]);
    expect(store.editorialPicks).toEqual([]);
    expect(store.categories).toEqual([]);
    expect(store.loading).toBe(false);
    expect(store.error).toBeNull();
    expect(store.lastFetchTime).toBe(0);
  });

  it('should fetch discovery home and cache data', async () => {
    const mockData = {
      recommendations: [{ id: '1', name: 'Test', subscriberCount: 100 }],
      hotRanking: [{ id: '2', name: 'Hot', rank: 1, score: 99 }],
      newRanking: [],
      systemRanking: [],
      editorialPicks: [],
      categories: [],
    };
    mockGetDiscoveryHome.mockResolvedValue(mockData);

    const store = useChannelDiscoveryStore();
    await store.fetchDiscoveryHome();

    expect(mockGetDiscoveryHome).toHaveBeenCalledTimes(1);
    expect(store.recommendations).toHaveLength(1);
    expect(store.recommendations[0].name).toBe('Test');
    expect(store.hotRanking).toHaveLength(1);
    expect(store.loading).toBe(false);
  });

  it('should use cache when valid and not force refresh', async () => {
    const store = useChannelDiscoveryStore();
    // 手动设置缓存数据
    store.recommendations = [{ id: 'cached', name: 'Cached', subscriberCount: 50 } as any];
    store.hotRanking = [{ id: 'h', name: 'H', rank: 1, score: 80 } as any];
    store.lastFetchTime = Date.now(); // 刚缓存

    await store.fetchDiscoveryHome();
    // 不应再次调用 API
    expect(mockGetDiscoveryHome).not.toHaveBeenCalled();
  });

  it('should bypass cache on force refresh', async () => {
    const mockData = {
      recommendations: [{ id: 'fresh', name: 'Fresh', subscriberCount: 10 }],
      hotRanking: [],
      newRanking: [],
      systemRanking: [],
      editorialPicks: [],
      categories: [],
    };
    mockGetDiscoveryHome.mockResolvedValue(mockData);

    const store = useChannelDiscoveryStore();
    store.recommendations = [{ id: 'old', name: 'Old', subscriberCount: 5 } as any];
    store.hotRanking = [{ id: 'h', name: 'H', rank: 1, score: 50 } as any];
    store.lastFetchTime = Date.now();

    await store.fetchDiscoveryHome(true);

    expect(mockGetDiscoveryHome).toHaveBeenCalledTimes(1);
    expect(store.recommendations[0].name).toBe('Fresh');
  });

  it('should fallback to parallel calls on aggregation failure', async () => {
    mockGetDiscoveryHome.mockRejectedValue(new Error('Service Error'));
    mockGetRecommendationList.mockResolvedValue({ records: [{ id: 'r1', name: 'Rec', subscriberCount: 10 }] });
    mockGetHotRanking.mockResolvedValue({ records: [{ id: 'h1', name: 'Hot', rank: 1, score: 90 }] });
    mockGetNewRanking.mockResolvedValue({ records: [] });
    mockGetSystemRanking.mockResolvedValue({ records: [] });
    mockGetEditorialPickList.mockResolvedValue([{ id: 'p1', recommendation: 'Good' }]);

    const store = useChannelDiscoveryStore();
    await store.fetchDiscoveryHome();

    expect(mockGetDiscoveryHome).toHaveBeenCalled();
    expect(mockGetRecommendationList).toHaveBeenCalled();
    expect(mockGetHotRanking).toHaveBeenCalled();
    expect(mockGetEditorialPickList).toHaveBeenCalled();
    expect(store.recommendations).toHaveLength(1);
    expect(store.hotRanking).toHaveLength(1);
    expect(store.editorialPicks).toHaveLength(1);
    expect(store.loading).toBe(false);
  });

  it('should handle not-interested feedback and remove from list', async () => {
    mockMarkNotInterested.mockResolvedValue({});

    const store = useChannelDiscoveryStore();
    store.recommendations = [
      { id: '1', name: 'A', subscriberCount: 10 } as any,
      { id: '2', name: 'B', subscriberCount: 20 } as any,
    ];

    await store.feedbackNotInterested('1');

    expect(mockMarkNotInterested).toHaveBeenCalledWith({ channelId: '1', reason: undefined });
    expect(store.recommendations).toHaveLength(1);
    expect(store.recommendations[0].id).toBe('2');
  });

  it('should fetch cold start recommendations', async () => {
    mockGetColdStartRecommendations.mockResolvedValue([
      { id: 'c1', name: 'Cold', subscriberCount: 5 },
    ]);

    const store = useChannelDiscoveryStore();
    await store.fetchColdStart();

    expect(mockGetColdStartRecommendations).toHaveBeenCalledTimes(1);
    expect(store.recommendations).toHaveLength(1);
  });

  it('should invalidate cache', () => {
    const store = useChannelDiscoveryStore();
    store.lastFetchTime = Date.now();
    store.invalidateCache();
    expect(store.lastFetchTime).toBe(0);
  });
});
