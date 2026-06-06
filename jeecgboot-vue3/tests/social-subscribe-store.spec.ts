import { vi } from 'vitest';
const mockGetSubscribeList = vi.fn();
const mockSubscribeSource = vi.fn();
const mockCancelSubscription = vi.fn();
const mockPauseSubscription = vi.fn();
const mockResumeSubscription = vi.fn();
const mockGetNotificationPreference = vi.fn();
const mockSaveNotificationPreference = vi.fn();
const mockGetSubscribePlaza = vi.fn();
const mockGetSubscribeSourceDetail = vi.fn();
const mockSubscribeFromPlaza = vi.fn();
const mockBatchPauseSubscribe = vi.fn();
const mockBatchResumeSubscribe = vi.fn();
const mockBatchCancelSubscribe = vi.fn();

vi.mock('/@/api/content/subscribe', () => ({
  getSubscribeList: (...args: any[]) => mockGetSubscribeList(...args),
  subscribeSource: (...args: any[]) => mockSubscribeSource(...args),
  cancelSubscription: (...args: any[]) => mockCancelSubscription(...args),
  pauseSubscription: (...args: any[]) => mockPauseSubscription(...args),
  resumeSubscription: (...args: any[]) => mockResumeSubscription(...args),
  getNotificationPreference: (...args: any[]) => mockGetNotificationPreference(...args),
  saveNotificationPreference: (...args: any[]) => mockSaveNotificationPreference(...args),
  getSubscribePlaza: (...args: any[]) => mockGetSubscribePlaza(...args),
  getSubscribeSourceDetail: (...args: any[]) => mockGetSubscribeSourceDetail(...args),
  subscribeFromPlaza: (...args: any[]) => mockSubscribeFromPlaza(...args),
  batchPauseSubscribe: (...args: any[]) => mockBatchPauseSubscribe(...args),
  batchResumeSubscribe: (...args: any[]) => mockBatchResumeSubscribe(...args),
  batchCancelSubscribe: (...args: any[]) => mockBatchCancelSubscribe(...args),
}));

vi.mock('/@/store', () => ({ store: {} }));

import { setActivePinia, createPinia } from 'pinia';
import { useSubscribeStore } from '/@/store/modules/subscribe';

function makeSubscribeItem(overrides: Record<string, any> = {}) {
  return {
    id: '1',
    sourceId: 'src-1',
    sourceType: 'RSS',
    sourceName: 'Tech News',
    sourceIcon: 'https://cdn/icon.png',
    category: '科技',
    subscriberCount: 100,
    lastUpdateTime: '2025-06-01',
    subscribeTime: '2025-01-01',
    status: 'active' as const,
    ...overrides,
  };
}

describe('store/modules/subscribe', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  describe('initial state', () => {
    it('has empty list and correct defaults', () => {
      const store = useSubscribeStore();
      expect(store.subscribeList).toEqual([]);
      expect(store.totalSubscribes).toBe(0);
      expect(store.hasMore).toBe(true);
      expect(store.searchKeyword).toBe('');
      expect(store.selectedSourceType).toBe('');
      expect(store.currentNotificationConfig).toBeNull();
    });
  });

  describe('fetchSubscribeList', () => {
    it('loads list on reset', async () => {
      const items = [makeSubscribeItem(), makeSubscribeItem({ id: '2', sourceId: 'src-2' })];
      mockGetSubscribeList.mockResolvedValue({ records: items, total: 2 });

      const store = useSubscribeStore();
      await store.fetchSubscribeList('user-1', true);

      expect(store.subscribeList).toHaveLength(2);
      expect(store.totalSubscribes).toBe(2);
      expect(store.hasMore).toBe(false);
    });

    it('appends when not reset', async () => {
      mockGetSubscribeList.mockResolvedValue({ records: [makeSubscribeItem()], total: 3 });

      const store = useSubscribeStore();
      await store.fetchSubscribeList('user-1', true);

      mockGetSubscribeList.mockResolvedValue({ records: [makeSubscribeItem({ id: '2' })], total: 3 });
      await store.fetchSubscribeList('user-1', false);

      expect(store.subscribeList).toHaveLength(2);
    });

    it('skips when hasMore=false and not reset', async () => {
      mockGetSubscribeList.mockResolvedValue({ records: [makeSubscribeItem()], total: 1 });

      const store = useSubscribeStore();
      await store.fetchSubscribeList('user-1', true);
      mockGetSubscribeList.mockClear();

      await store.fetchSubscribeList('user-1', false);
      expect(mockGetSubscribeList).not.toHaveBeenCalled();
    });
  });

  describe('subscribe / unsubscribe', () => {
    it('subscribe calls API then refreshes list', async () => {
      mockSubscribeSource.mockResolvedValue(undefined);
      mockGetSubscribeList.mockResolvedValue({ records: [], total: 0 });

      const store = useSubscribeStore();
      await store.subscribe('user-1', 'src-1', 'RSS');

      expect(mockSubscribeSource).toHaveBeenCalledWith('user-1', { sourceId: 'src-1', sourceType: 'RSS' });
    });

    it('unsubscribe calls API then refreshes list', async () => {
      mockCancelSubscription.mockResolvedValue(undefined);
      mockGetSubscribeList.mockResolvedValue({ records: [], total: 0 });

      const store = useSubscribeStore();
      await store.unsubscribe('user-1', 'src-1');

      expect(mockCancelSubscription).toHaveBeenCalledWith('user-1', 'src-1');
    });
  });

  describe('pause / resume', () => {
    it('pause calls API then refreshes list', async () => {
      mockPauseSubscription.mockResolvedValue(undefined);
      mockGetSubscribeList.mockResolvedValue({ records: [], total: 0 });

      const store = useSubscribeStore();
      await store.pause('user-1', 'src-1');

      expect(mockPauseSubscription).toHaveBeenCalledWith('user-1', 'src-1');
    });

    it('resume calls API then refreshes list', async () => {
      mockResumeSubscription.mockResolvedValue(undefined);
      mockGetSubscribeList.mockResolvedValue({ records: [], total: 0 });

      const store = useSubscribeStore();
      await store.resume('user-1', 'src-1');

      expect(mockResumeSubscription).toHaveBeenCalledWith('user-1', 'src-1');
    });
  });

  describe('notification config', () => {
    it('fetchNotificationConfig loads config from API', async () => {
      const config = { channelInApp: true, channelPush: false, channelEmail: true, frequency: 'daily' };
      mockGetNotificationPreference.mockResolvedValue(config);

      const store = useSubscribeStore();
      await store.fetchNotificationConfig('user-1', 'src-1');

      expect(store.currentNotificationConfig).toEqual(config);
    });

    it('saveConfig saves and updates local state', async () => {
      mockSaveNotificationPreference.mockResolvedValue(undefined);

      const store = useSubscribeStore();
      const config = { channelInApp: true, channelPush: true, channelEmail: false, frequency: 'realtime' as const };
      await store.saveConfig('user-1', 'src-1', config);

      expect(mockSaveNotificationPreference).toHaveBeenCalledWith('user-1', 'src-1', config);
      expect(store.currentNotificationConfig).toEqual(config);
    });
  });

  describe('fetchPlaza', () => {
    it('calls API and returns result', async () => {
      const result = { records: [makeSubscribeItem()], total: 1 };
      mockGetSubscribePlaza.mockResolvedValue(result);

      const store = useSubscribeStore();
      const res = await store.fetchPlaza({ keyword: 'tech', page: 1, size: 20 });

      expect(res).toEqual(result);
    });
  });

  describe('batch operations', () => {
    it('batchPause calls API then refreshes', async () => {
      mockBatchPauseSubscribe.mockResolvedValue(undefined);
      mockGetSubscribeList.mockResolvedValue({ records: [], total: 0 });

      const store = useSubscribeStore();
      await store.batchPause('user-1', ['s-1', 's-2']);

      expect(mockBatchPauseSubscribe).toHaveBeenCalledWith('user-1', { sourceIds: ['s-1', 's-2'] });
    });

    it('batchCancel calls API then refreshes', async () => {
      mockBatchCancelSubscribe.mockResolvedValue(undefined);
      mockGetSubscribeList.mockResolvedValue({ records: [], total: 0 });

      const store = useSubscribeStore();
      await store.batchCancel('user-1', ['s-1']);

      expect(mockBatchCancelSubscribe).toHaveBeenCalledWith('user-1', { sourceIds: ['s-1'] });
    });
  });

  describe('setters', () => {
    it('setSearchKeyword updates keyword', () => {
      const store = useSubscribeStore();
      store.setSearchKeyword('test');
      expect(store.searchKeyword).toBe('test');
    });

    it('setSelectedSourceType updates type', () => {
      const store = useSubscribeStore();
      store.setSelectedSourceType('RSS');
      expect(store.selectedSourceType).toBe('RSS');
    });
  });
});
