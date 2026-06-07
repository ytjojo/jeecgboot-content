import { describe, it, expect, vi, beforeEach } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useChannelReviewStore } from '/@/store/modules/channelReview';

vi.mock('/@/api/content/channel/review', () => ({
  getReviewList: vi.fn().mockResolvedValue({
    records: [{ id: '1', title: '待审文章', contentType: 'article', submitter: '张三', submitTime: '2026-06-01', sourceScene: '投稿', hitRule: '规则', isTimeout: false }],
    total: 1,
  }),
  executeReview: vi.fn().mockResolvedValue({}),
  getReviewStats: vi.fn().mockResolvedValue({ total: 5, timeoutCount: 2 }),
}));

describe('useChannelReviewStore', () => {
  beforeEach(() => { setActivePinia(createPinia()); });

  it('应加载待审列表', async () => {
    const store = useChannelReviewStore();
    store.setFilter({ channelId: '1' });
    await store.fetchList();
    expect(store.reviewList).toHaveLength(1);
    expect(store.total).toBe(1);
  });

  it('应加载审核统计', async () => {
    const store = useChannelReviewStore();
    await store.fetchStats('1');
    expect(store.stats.total).toBe(5);
    expect(store.stats.timeoutCount).toBe(2);
  });

  it('approve 应调用 executeReview 并刷新列表', async () => {
    const store = useChannelReviewStore();
    store.setFilter({ channelId: '1' });
    await store.approve('1');
    const { executeReview, getReviewList } = await import('/@/api/content/channel/review');
    expect(executeReview).toHaveBeenCalledWith({ reviewId: '1', action: 'APPROVE' });
    expect(getReviewList).toHaveBeenCalled();
  });

  it('reject 应传递拒绝原因', async () => {
    const store = useChannelReviewStore();
    store.setFilter({ channelId: '1' });
    await store.reject('1', '内容不符合规范要求至少十个字');
    const { executeReview } = await import('/@/api/content/channel/review');
    expect(executeReview).toHaveBeenCalledWith({ reviewId: '1', action: 'REJECT', rejectReason: '内容不符合规范要求至少十个字' });
  });
});
