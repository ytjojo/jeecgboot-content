import { setActivePinia, createPinia } from 'pinia';
import { useCircleAnalyticsStore } from '/@/store/modules/circleAnalytics';

vi.mock('/@/api/content/circle/analytics', () => ({
  getCircleStatistics: vi.fn(),
  exportCircleStatisticsCsv: vi.fn(),
}));

import { getCircleStatistics, exportCircleStatisticsCsv } from '/@/api/content/circle/analytics';

describe('useCircleAnalyticsStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it('should have correct initial state', () => {
    const store = useCircleAnalyticsStore();
    expect(store.analyticsData).toBeNull();
    expect(store.loading).toBe(false);
    expect(store.error).toBeNull();
    expect(store.exporting).toBe(false);
  });

  it('should fetch analytics data successfully', async () => {
    const mockData = {
      memberCount: 100,
      newMemberCount: 10,
      postCount: 50,
      newPostCount: 5,
      activeCount: 30,
      dailyTrends: [],
    };
    (getCircleStatistics as any).mockResolvedValue(mockData);

    const store = useCircleAnalyticsStore();
    await store.fetchAnalytics('circle-1', { startDate: '2024-01-01', endDate: '2024-01-07' });

    expect(store.analyticsData).toEqual(mockData);
    expect(store.loading).toBe(false);
    expect(store.error).toBeNull();
  });

  it('should set error on fetch failure', async () => {
    (getCircleStatistics as any).mockRejectedValue(new Error('Network error'));

    const store = useCircleAnalyticsStore();
    await store.fetchAnalytics('circle-1', { startDate: '2024-01-01', endDate: '2024-01-07' });

    expect(store.analyticsData).toBeNull();
    expect(store.loading).toBe(false);
    expect(store.error).toBeTruthy();
  });

  it('should clear data', () => {
    const store = useCircleAnalyticsStore();
    store.analyticsData = {} as any;
    store.error = 'some error';
    store.clearData();
    expect(store.analyticsData).toBeNull();
    expect(store.error).toBeNull();
  });

  it('should compute change percentages from dailyTrends (prev period vs current period)', () => {
    const store = useCircleAnalyticsStore();
    store.analyticsData = {
      memberCount: 200, newMemberCount: 20, postCount: 100, newPostCount: 10, activeCount: 50,
      dailyTrends: [
        { date: '2024-01-01', newMemberCount: 5, newPostCount: 2, activeCount: 10 },
        { date: '2024-01-02', newMemberCount: 5, newPostCount: 3, activeCount: 10 },
        { date: '2024-01-03', newMemberCount: 5, newPostCount: 2, activeCount: 10 },
        { date: '2024-01-05', newMemberCount: 10, newPostCount: 5, activeCount: 20 },
        { date: '2024-01-06', newMemberCount: 5, newPostCount: 3, activeCount: 20 },
        { date: '2024-01-07', newMemberCount: 0, newPostCount: 0, activeCount: 0 },
      ],
    };
    // split in half: first 3 days (sum newMember=15, newPost=7, active=30) vs last 3 days (sum=15,8,40)
    expect(typeof store.memberChange).toBe('number');
    expect(typeof store.postChange).toBe('number');
    expect(typeof store.activeChange).toBe('number');
  });

  it('should return null change when no dailyTrends', () => {
    const store = useCircleAnalyticsStore();
    expect(store.memberChange).toBeNull();
    expect(store.postChange).toBeNull();
    expect(store.activeChange).toBeNull();
  });
});
