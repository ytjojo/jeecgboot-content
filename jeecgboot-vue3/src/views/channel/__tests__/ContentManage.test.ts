import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import ContentManage from '../governance/ContentManage.vue';

vi.mock('/@/api/content/channel/governance', () => ({
  getGovernanceContentList: vi.fn().mockResolvedValue({
    records: [
      { id: '1', title: 'Vue3最佳实践', contentType: 'article', author: '王五', publishTime: '2026-06-01 10:00', status: 'published', isPinned: true, isFeatured: false },
    ],
    total: 1,
  }),
  executeGovernance: vi.fn().mockResolvedValue({}),
}));

describe('ContentManage', () => {
  beforeEach(() => { setActivePinia(createPinia()); });

  it('应加载内容列表', async () => {
    const wrapper = mount(ContentManage, { props: { channelId: '1' } });
    await vi.dynamicImportSettled();
    expect(wrapper.text()).toContain('Vue3最佳实践');
    expect(wrapper.text()).toContain('王五');
  });

  it('应展示置顶标识', async () => {
    const wrapper = mount(ContentManage, { props: { channelId: '1' } });
    await vi.dynamicImportSettled();
    expect(wrapper.text()).toContain('置顶');
  });
});
