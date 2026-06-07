import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import RecycleBin from '../governance/RecycleBin.vue';

vi.mock('/@/api/content/channel/governance', () => ({
  getRecycleBinList: vi.fn().mockResolvedValue({
    records: [
      { id: '1', title: '已删除文章', contentType: 'article', originalAuthor: '李四', deletedBy: '管理员', deleteTime: '2026-06-01', deleteReason: '违规', remainingDays: 25 },
    ],
    total: 1,
  }),
  executeGovernance: vi.fn().mockResolvedValue({}),
}));

describe('RecycleBin', () => {
  beforeEach(() => { setActivePinia(createPinia()); });

  it('应加载回收站列表', async () => {
    const wrapper = mount(RecycleBin, { props: { channelId: '1' } });
    await vi.dynamicImportSettled();
    expect(wrapper.text()).toContain('已删除文章');
    expect(wrapper.text()).toContain('25天');
  });

  it('已过保留期内容应禁用恢复按钮', async () => {
    vi.mocked(await import('/@/api/content/channel/governance')).getRecycleBinList.mockResolvedValueOnce({
      records: [{ id: '2', title: '过期内容', remainingDays: 0 }],
      total: 1,
    });
    const wrapper = mount(RecycleBin, { props: { channelId: '1' } });
    await vi.dynamicImportSettled();
    expect(wrapper.find('.expired').exists()).toBe(true);
  });
});
