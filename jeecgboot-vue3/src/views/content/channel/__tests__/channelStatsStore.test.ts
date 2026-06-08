import { describe, it, expect, vi, beforeEach } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useChannelStatsStore } from '/@/store/modules/channelStats';

vi.mock('/@/api/content/channel/stats', () => ({
  getCoreStats: vi.fn().mockResolvedValue({
    subscriberCount: 10000,
    contentCount: 500,
    pv: 50000,
    uv: 8000,
    subscriberTrend: 5.2,
    contentTrend: 3.1,
    pvTrend: 12.3,
    uvTrend: -2.1,
    updateTime: '2026-06-08 10:00:00',
  }),
  getTrendData: vi.fn().mockResolvedValue([
    { date: '2026-06-01', subscriberCount: 9000, contentCount: 480, pv: 45000, uv: 7500 },
    { date: '2026-06-08', subscriberCount: 10000, contentCount: 500, pv: 50000, uv: 8000 },
  ]),
  getHotContent: vi.fn().mockResolvedValue([
    { id: '1', title: '热门文章1', contentType: 'article', publishTime: '2026-06-07', interactionCount: 1500, rank: 1 },
    { id: '2', title: '热门文章2', contentType: 'post', publishTime: '2026-06-06', interactionCount: 1200, rank: 2 },
  ]),
  getUserAnalysis: vi.fn().mockResolvedValue({
    subscribeTrend: [{ date: '2026-06-01', subscribe: 100, unsubscribe: 20 }],
    activityDistribution: [{ level: '高活跃', count: 200, percentage: 40 }],
    contributionRank: [{ userId: 'u1', userName: '张三', contribution: 500, rank: 1 }],
  }),
  getInteraction: vi.fn().mockResolvedValue({
    likeCount: 5000,
    commentCount: 2000,
    favoriteCount: 800,
    shareCount: 300,
    visitCount: 12000,
    newContentCount: 50,
    contentTypeDistribution: [{ type: 'article', count: 30 }],
  }),
}));

describe('useChannelStatsStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('fetchCoreStats 应加载核心指标', async () => {
    const store = useChannelStatsStore();
    store.setChannelId('test-channel');
    await store.fetchCoreStats();
    expect(store.coreStats).toBeTruthy();
    expect(store.coreStats?.subscriberCount).toBe(10000);
    expect(store.coreStats?.pv).toBe(50000);
  });

  it('fetchTrendData 应加载趋势数据', async () => {
    const store = useChannelStatsStore();
    store.setChannelId('test-channel');
    store.setTimeRange('week');
    await store.fetchTrendData();
    expect(store.trendData).toHaveLength(2);
  });

  it('fetchAllData 应并行加载所有数据 (Promise.allSettled)', async () => {
    const store = useChannelStatsStore();
    store.setChannelId('test-channel');
    const results = await store.fetchAllData();
    expect(results).toHaveLength(5);
    expect(store.coreStats).toBeTruthy();
    expect(store.trendData).toHaveLength(2);
    expect(store.hotContent).toHaveLength(2);
    expect(store.userAnalysis).toBeTruthy();
    expect(store.interaction).toBeTruthy();
  });

  it('setTimeRange 应更新时间范围', () => {
    const store = useChannelStatsStore();
    store.setTimeRange('month');
    expect(store.timeRange).toBe('month');
  });

  it('setCustomDateRange 应更新自定义日期范围', () => {
    const store = useChannelStatsStore();
    store.setCustomDateRange(['2026-06-01', '2026-06-08']);
    expect(store.customDateRange).toEqual(['2026-06-01', '2026-06-08']);
  });

  it('单个模块加载失败不应影响其他模块', async () => {
    const { getCoreStats } = await import('/@/api/content/channel/stats');
    vi.mocked(getCoreStats).mockRejectedValueOnce(new Error('网络错误'));
    const store = useChannelStatsStore();
    store.setChannelId('test-channel');
    await store.fetchAllData();
    expect(store.coreError).toBeTruthy();
    expect(store.trendData).toHaveLength(2);
  });
});
