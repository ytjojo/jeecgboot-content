import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import GovernanceLog from '../governance/GovernanceLog.vue';

vi.mock('/@/api/content/channel/governance', () => ({
  getGovernanceLogList: vi.fn().mockResolvedValue({
    records: [
      { id: '1', time: '2026-06-01 10:00', operator: '管理员', actionType: 'pin', targetTitle: '置顶文章', result: '成功', remark: '' },
    ],
    total: 1,
  }),
}));

describe('GovernanceLog', () => {
  beforeEach(() => { setActivePinia(createPinia()); });

  it('应加载治理日志列表', async () => {
    const wrapper = mount(GovernanceLog, { props: { channelId: '1' } });
    await vi.dynamicImportSettled();
    expect(wrapper.text()).toContain('置顶文章');
    expect(wrapper.text()).toContain('管理员');
  });

  it('应展示操作类型筛选', async () => {
    const wrapper = mount(GovernanceLog, { props: { channelId: '1' } });
    await vi.dynamicImportSettled();
    expect(wrapper.find('.filter-bar').exists()).toBe(true);
  });
});
