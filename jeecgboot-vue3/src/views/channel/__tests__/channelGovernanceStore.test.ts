import { describe, it, expect, vi, beforeEach } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useChannelGovernanceStore } from '/@/store/modules/channelGovernance';

vi.mock('/@/api/content/channel/governance', () => ({
  getGovernanceContentList: vi.fn().mockResolvedValue({
    records: [{ id: '1', title: '测试内容', contentType: 'article', author: '王五', publishTime: '2026-06-01', status: 'published', isPinned: false, isFeatured: false }],
    total: 1,
  }),
  executeGovernance: vi.fn().mockResolvedValue({}),
  getRecycleBinList: vi.fn().mockResolvedValue({
    records: [{ id: '1', title: '已删除', remainingDays: 25 }],
    total: 1,
  }),
  getGovernanceLogList: vi.fn().mockResolvedValue({
    records: [{ id: '1', time: '2026-06-01', operator: '管理员', actionType: 'pin', targetTitle: '文章', result: '成功', remark: '' }],
    total: 1,
  }),
}));

describe('useChannelGovernanceStore', () => {
  beforeEach(() => { setActivePinia(createPinia()); });

  it('应加载内容列表', async () => {
    const store = useChannelGovernanceStore();
    store.setFilter({ channelId: '1' });
    await store.fetchList();
    expect(store.contentList).toHaveLength(1);
    expect(store.total).toBe(1);
  });

  it('pin 应发送正确的 action', async () => {
    const store = useChannelGovernanceStore();
    await store.pin('c1', 'ch1', false);
    const { executeGovernance } = await import('/@/api/content/channel/governance');
    expect(executeGovernance).toHaveBeenCalledWith({ contentId: 'c1', channelId: 'ch1', action: 'PIN' });
  });

  it('feature 已精华时应发送 UNFEATURE', async () => {
    const store = useChannelGovernanceStore();
    await store.feature('c1', 'ch1', true);
    const { executeGovernance } = await import('/@/api/content/channel/governance');
    expect(executeGovernance).toHaveBeenCalledWith({ contentId: 'c1', channelId: 'ch1', action: 'UNFEATURE' });
  });

  it('应加载回收站列表', async () => {
    const store = useChannelGovernanceStore();
    await store.fetchRecycleBin('1');
    expect(store.recycleBinList).toHaveLength(1);
    expect(store.recycleBinTotal).toBe(1);
  });

  it('应加载治理日志列表', async () => {
    const store = useChannelGovernanceStore();
    await store.fetchGovernanceLog('1');
    expect(store.governanceLogList).toHaveLength(1);
    expect(store.governanceLogTotal).toBe(1);
  });
});
