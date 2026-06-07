import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import ReviewQueue from '../governance/ReviewQueue.vue';

vi.mock('/@/api/content/channel/review', () => ({
  getReviewList: vi.fn().mockResolvedValue({
    records: [
      { id: '1', title: '测试文章', contentType: 'article', submitter: '张三', submitTime: '2026-06-01 10:00', sourceScene: '公开投稿', hitRule: '先审后发规则', isTimeout: false },
    ],
    total: 1,
  }),
  getReviewStats: vi.fn().mockResolvedValue({ total: 5, timeoutCount: 2 }),
  executeReview: vi.fn().mockResolvedValue({}),
}));

describe('ReviewQueue', () => {
  beforeEach(() => { setActivePinia(createPinia()); });

  it('应加载待审列表', async () => {
    const wrapper = mount(ReviewQueue, { props: { channelId: '1' } });
    await vi.dynamicImportSettled();
    expect(wrapper.text()).toContain('测试文章');
    expect(wrapper.text()).toContain('张三');
  });

  it('点击拒绝应弹出原因弹窗', async () => {
    const wrapper = mount(ReviewQueue, { props: { channelId: '1' } });
    await vi.dynamicImportSettled();
    await wrapper.find('.reject-btn').trigger('click');
    expect(wrapper.findComponent({ name: 'RejectReasonModal' }).exists()).toBe(true);
  });

  it('超时内容应有高亮标识', async () => {
    const wrapper = mount(ReviewQueue, { props: { channelId: '1' } });
    await vi.dynamicImportSettled();
    expect(wrapper.find('.timeout-row').exists()).toBe(false);
  });
});
