import { vi, describe, it, expect, beforeEach } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useChannelStore, channelTypeOptions, channelStatusOptions } from '/@/store/modules/channel';

describe('store/modules/channel', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  describe('useChannelStore', () => {
    it('initializes with empty state', () => {
      const store = useChannelStore();
      expect(store.currentChannel).toBeNull();
      expect(store.channelList).toEqual([]);
      expect(store.reviewQueue).toEqual([]);
    });

    it('setCurrentChannel sets and clearCurrentChannel clears', () => {
      const store = useChannelStore();
      const channel = { id: 'ch-1', name: 'test' } as any;
      store.setCurrentChannel(channel);
      expect(store.currentChannel).toStrictEqual(channel);

      store.clearCurrentChannel();
      expect(store.currentChannel).toBeNull();
    });

    it('setChannelList updates list', () => {
      const store = useChannelStore();
      const list = [{ id: 'ch-1' }, { id: 'ch-2' }] as any[];
      store.setChannelList(list);
      expect(store.channelList).toStrictEqual(list);
    });

    it('setReviewQueue updates queue', () => {
      const store = useChannelStore();
      const queue = [{ id: 'rv-1' }] as any[];
      store.setReviewQueue(queue);
      expect(store.reviewQueue).toStrictEqual(queue);
    });
  });

  describe('channelTypeOptions', () => {
    it('has 3 options: system, personal, organization', () => {
      expect(channelTypeOptions).toHaveLength(3);
      expect(channelTypeOptions.map((o) => o.value)).toEqual(['system', 'personal', 'organization']);
    });
  });

  describe('channelStatusOptions', () => {
    it('has 6 options covering all statuses', () => {
      expect(channelStatusOptions).toHaveLength(6);
      expect(channelStatusOptions.map((o) => o.value)).toEqual([
        'DRAFT',
        'PENDING_REVIEW',
        'ACTIVE',
        'REJECTED',
        'DELETE_COOLING',
        'DELETED',
      ]);
    });
  });
});
