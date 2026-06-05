const mockGetFollowingFeed = jest.fn();
const mockGetSubscribeFeed = jest.fn();

jest.mock('/@/api/content/relation', () => ({
  getFollowingFeed: (...args: any[]) => mockGetFollowingFeed(...args),
}));

jest.mock('/@/api/content/subscribe', () => ({
  getSubscribeFeed: (...args: any[]) => mockGetSubscribeFeed(...args),
}));

jest.mock('/@/store', () => ({ store: {} }));

import { setActivePinia, createPinia } from 'pinia';
import { useFeedStore } from '/@/store/modules/feed';

function makeFeedItem(overrides: Record<string, any> = {}) {
  return {
    id: '1',
    userId: 'u-1',
    nickname: 'Test User',
    avatar: 'https://cdn/avatar.png',
    contentId: 'c-1',
    contentTitle: 'Test Post',
    contentSummary: 'Summary',
    dynamicType: 'post' as const,
    createTime: '2025-06-01',
    isPriority: false,
    ...overrides,
  };
}

describe('store/modules/feed', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    jest.clearAllMocks();
  });

  describe('initial state', () => {
    it('has empty lists and correct defaults', () => {
      const store = useFeedStore();
      expect(store.followFeedList).toEqual([]);
      expect(store.subscribeFeedList).toEqual([]);
      expect(store.priorityItems).toEqual([]);
      expect(store.followHasMore).toBe(true);
      expect(store.subscribeHasMore).toBe(true);
      expect(store.followTypes).toEqual(['post', 'like', 'favorite']);
      expect(store.subscribeSourceType).toBe('');
    });
  });

  describe('fetchFollowFeed', () => {
    it('loads feed on reset', async () => {
      const items = [makeFeedItem(), makeFeedItem({ id: '2' })];
      mockGetFollowingFeed.mockResolvedValue({ items, priorityItems: [], total: 2 });

      const store = useFeedStore();
      await store.fetchFollowFeed(true);

      expect(store.followFeedList).toHaveLength(2);
      expect(store.followHasMore).toBe(false);
    });

    it('loads priority items on reset', async () => {
      const pItems = [makeFeedItem({ id: 'p-1', isPriority: true })];
      mockGetFollowingFeed.mockResolvedValue({ items: [], priorityItems: pItems, total: 0 });

      const store = useFeedStore();
      await store.fetchFollowFeed(true);

      expect(store.priorityItems).toHaveLength(1);
      expect(store.priorityItems[0].isPriority).toBe(true);
    });

    it('appends when not reset', async () => {
      mockGetFollowingFeed.mockResolvedValue({ items: [makeFeedItem()], total: 3 });

      const store = useFeedStore();
      await store.fetchFollowFeed(true);

      mockGetFollowingFeed.mockResolvedValue({ items: [makeFeedItem({ id: '2' })], total: 3 });
      await store.fetchFollowFeed(false);

      expect(store.followFeedList).toHaveLength(2);
    });

    it('skips when hasMore=false and not reset', async () => {
      mockGetFollowingFeed.mockResolvedValue({ items: [makeFeedItem()], total: 1 });

      const store = useFeedStore();
      await store.fetchFollowFeed(true);
      mockGetFollowingFeed.mockClear();

      await store.fetchFollowFeed(false);
      expect(mockGetFollowingFeed).not.toHaveBeenCalled();
    });
  });

  describe('fetchSubscribeFeed', () => {
    it('loads subscribe feed on reset', async () => {
      const items = [makeFeedItem({ sourceType: 'RSS', sourceName: 'Tech' })];
      mockGetSubscribeFeed.mockResolvedValue({ records: items, total: 1 });

      const store = useFeedStore();
      await store.fetchSubscribeFeed(true);

      expect(store.subscribeFeedList).toHaveLength(1);
    });

    it('appends when not reset', async () => {
      mockGetSubscribeFeed.mockResolvedValue({ records: [makeFeedItem()], total: 3 });

      const store = useFeedStore();
      await store.fetchSubscribeFeed(true);

      mockGetSubscribeFeed.mockResolvedValue({ records: [makeFeedItem({ id: '2' })], total: 3 });
      await store.fetchSubscribeFeed(false);

      expect(store.subscribeFeedList).toHaveLength(2);
    });
  });

  describe('setFollowTypes', () => {
    it('updates types and triggers refetch', async () => {
      mockGetFollowingFeed.mockResolvedValue({ items: [], total: 0 });

      const store = useFeedStore();
      store.setFollowTypes(['post']);

      expect(store.followTypes).toEqual(['post']);
      // fetchFollowFeed should be called
      await new Promise((r) => setTimeout(r, 0));
      expect(mockGetFollowingFeed).toHaveBeenCalled();
    });

    it('ignores empty types array (keeps previous)', () => {
      const store = useFeedStore();
      store.setFollowTypes([]);
      expect(store.followTypes).toEqual(['post', 'like', 'favorite']);
    });
  });

  describe('removeUserFeeds / restoreUserFeeds', () => {
    it('removeUserFeeds removes items by userId and returns removed data', async () => {
      mockGetFollowingFeed.mockResolvedValue({
        items: [
          makeFeedItem({ id: '1', userId: 'u-1' }),
          makeFeedItem({ id: '2', userId: 'u-2' }),
          makeFeedItem({ id: '3', userId: 'u-1' }),
        ],
        total: 3,
      });

      const store = useFeedStore();
      await store.fetchFollowFeed(true);

      const removed = store.removeUserFeeds('u-1');

      expect(removed.items).toHaveLength(2);
      expect(store.followFeedList).toHaveLength(1);
      expect(store.followFeedList[0].userId).toBe('u-2');
    });

    it('restoreUserFeeds reinserts items at original positions', async () => {
      mockGetFollowingFeed.mockResolvedValue({
        items: [
          makeFeedItem({ id: '1', userId: 'u-1' }),
          makeFeedItem({ id: '2', userId: 'u-2' }),
          makeFeedItem({ id: '3', userId: 'u-1' }),
        ],
        total: 3,
      });

      const store = useFeedStore();
      await store.fetchFollowFeed(true);

      const removed = store.removeUserFeeds('u-1');
      expect(store.followFeedList).toHaveLength(1);

      store.restoreUserFeeds(removed);
      expect(store.followFeedList).toHaveLength(3);
    });
  });
});

