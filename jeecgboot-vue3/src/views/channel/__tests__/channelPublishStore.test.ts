import { describe, it, expect, vi, beforeEach } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useChannelPublishStore } from '/@/store/modules/channelPublish';

vi.mock('/@/api/content/channel/publish', () => ({
  getAvailableChannels: vi.fn().mockResolvedValue({
    list: [{ id: '1', name: '频道A', type: 'system', userRole: 'admin', publishResult: 'direct', publishable: true }],
    maxChannelCount: 3,
  }),
  getScheduledList: vi.fn().mockResolvedValue([
    { id: '1', contentId: 'c1', contentTitle: '文章A', channelName: '频道A', scheduledTime: '2026-06-15', status: 'pending' },
  ]),
  updateScheduledPublish: vi.fn().mockResolvedValue({}),
  cancelScheduledPublish: vi.fn().mockResolvedValue({}),
}));

describe('useChannelPublishStore', () => {
  beforeEach(() => { setActivePinia(createPinia()); });

  it('应加载可发布频道列表', async () => {
    const store = useChannelPublishStore();
    await store.fetchAvailableChannels();
    expect(store.availableChannels).toHaveLength(1);
    expect(store.maxChannelCount).toBe(3);
  });

  it('添加频道不应超过上限', async () => {
    const store = useChannelPublishStore();
    store.maxChannelCount = 1;
    store.addChannel({ id: '1', name: 'A', type: '', userRole: '', publishResult: '', publishable: true });
    store.addChannel({ id: '2', name: 'B', type: '', userRole: '', publishResult: '', publishable: true });
    expect(store.selectedChannels).toHaveLength(1);
  });

  it('不应重复添加同一频道', () => {
    const store = useChannelPublishStore();
    store.addChannel({ id: '1', name: 'A', type: '', userRole: '', publishResult: '', publishable: true });
    store.addChannel({ id: '1', name: 'A', type: '', userRole: '', publishResult: '', publishable: true });
    expect(store.selectedChannels).toHaveLength(1);
  });

  it('应加载定时发布任务列表', async () => {
    const store = useChannelPublishStore();
    await store.fetchScheduledTasks();
    expect(store.scheduledTaskList).toHaveLength(1);
  });
});
