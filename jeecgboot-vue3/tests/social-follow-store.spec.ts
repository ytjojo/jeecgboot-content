// Mock API module
const mockGetFollowList = jest.fn();
const mockGetSpecialFollowList = jest.fn();
const mockGetFollowGroupList = jest.fn();
const mockCreateFollowGroup = jest.fn();
const mockRenameFollowGroup = jest.fn();
const mockDeleteFollowGroup = jest.fn();
const mockFollowUser = jest.fn();
const mockUnfollowUser = jest.fn();
const mockSetSpecialFollow = jest.fn();
const mockCancelSpecialFollow = jest.fn();
const mockMoveFollowGroup = jest.fn();
const mockRemoveFromGroup = jest.fn();
const mockGetRecommendations = jest.fn();
const mockBatchUnfollow = jest.fn();
const mockBatchCancelSpecial = jest.fn();

jest.mock('/@/api/content/relation', () => ({
  getFollowList: (...args: any[]) => mockGetFollowList(...args),
  getSpecialFollowList: (...args: any[]) => mockGetSpecialFollowList(...args),
  getFollowGroupList: (...args: any[]) => mockGetFollowGroupList(...args),
  createFollowGroup: (...args: any[]) => mockCreateFollowGroup(...args),
  renameFollowGroup: (...args: any[]) => mockRenameFollowGroup(...args),
  deleteFollowGroup: (...args: any[]) => mockDeleteFollowGroup(...args),
  followUser: (...args: any[]) => mockFollowUser(...args),
  unfollowUser: (...args: any[]) => mockUnfollowUser(...args),
  setSpecialFollow: (...args: any[]) => mockSetSpecialFollow(...args),
  cancelSpecialFollow: (...args: any[]) => mockCancelSpecialFollow(...args),
  moveFollowGroup: (...args: any[]) => mockMoveFollowGroup(...args),
  removeFromGroup: (...args: any[]) => mockRemoveFromGroup(...args),
  getRecommendations: (...args: any[]) => mockGetRecommendations(...args),
  batchUnfollow: (...args: any[]) => mockBatchUnfollow(...args),
  batchCancelSpecial: (...args: any[]) => mockBatchCancelSpecial(...args),
}));

jest.mock('/@/store', () => ({ store: {} }));

import { setActivePinia, createPinia } from 'pinia';
import { useFollowStore } from '/@/store/modules/follow';

function makeFollowItem(overrides: Record<string, any> = {}) {
  return {
    id: '1',
    userId: 'u-1',
    nickname: 'Test User',
    avatar: 'https://cdn/avatar.png',
    bio: 'bio',
    followTime: '2025-01-01',
    groupId: '',
    isSpecial: false,
    lastActiveTime: '2025-06-01',
    ...overrides,
  };
}

describe('store/modules/follow', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    jest.clearAllMocks();
  });

  describe('initial state', () => {
    it('has empty lists and correct defaults', () => {
      const store = useFollowStore();
      expect(store.followList).toEqual([]);
      expect(store.specialFollowList).toEqual([]);
      expect(store.followGroups).toEqual([]);
      expect(store.totalFollows).toBe(0);
      expect(store.hasMore).toBe(true);
      expect(store.searchKeyword).toBe('');
      expect(store.selectedGroupId).toBe('');
    });
  });

  describe('fetchFollowList', () => {
    it('loads follow list on reset', async () => {
      const items = [makeFollowItem(), makeFollowItem({ id: '2', userId: 'u-2' })];
      mockGetFollowList.mockResolvedValue({ records: items, total: 2 });

      const store = useFollowStore();
      await store.fetchFollowList('user-1', true);

      expect(store.followList).toHaveLength(2);
      expect(store.totalFollows).toBe(2);
      expect(store.hasMore).toBe(false);
      expect(store.currentPage).toBe(2);
    });

    it('appends to list when not reset', async () => {
      mockGetFollowList.mockResolvedValue({ records: [makeFollowItem()], total: 3 });

      const store = useFollowStore();
      await store.fetchFollowList('user-1', true);

      mockGetFollowList.mockResolvedValue({ records: [makeFollowItem({ id: '2' })], total: 3 });
      await store.fetchFollowList('user-1', false);

      expect(store.followList).toHaveLength(2);
    });

    it('sets hasMore=false when all loaded', async () => {
      mockGetFollowList.mockResolvedValue({ records: [makeFollowItem()], total: 1 });

      const store = useFollowStore();
      await store.fetchFollowList('user-1', true);

      expect(store.hasMore).toBe(false);
    });

    it('skips fetch when hasMore is false and not reset', async () => {
      mockGetFollowList.mockResolvedValue({ records: [makeFollowItem()], total: 1 });

      const store = useFollowStore();
      await store.fetchFollowList('user-1', true);
      mockGetFollowList.mockClear();

      await store.fetchFollowList('user-1', false);
      expect(mockGetFollowList).not.toHaveBeenCalled();
    });
  });

  describe('fetchFollowGroups', () => {
    it('loads groups from API', async () => {
      const groups = [
        { id: 'g-1', name: 'Default', sortOrder: 0, memberCount: 5, isDefault: true },
        { id: 'g-2', name: 'Friends', sortOrder: 1, memberCount: 3, isDefault: false },
      ];
      mockGetFollowGroupList.mockResolvedValue(groups);

      const store = useFollowStore();
      await store.fetchFollowGroups('user-1');

      expect(store.followGroups).toEqual(groups);
    });

    it('defaultGroup computed returns the default group', async () => {
      mockGetFollowGroupList.mockResolvedValue([
        { id: 'g-1', name: 'Default', sortOrder: 0, memberCount: 5, isDefault: true },
        { id: 'g-2', name: 'Friends', sortOrder: 1, memberCount: 3, isDefault: false },
      ]);

      const store = useFollowStore();
      await store.fetchFollowGroups('user-1');

      expect(store.defaultGroup?.id).toBe('g-1');
    });

    it('customGroups computed excludes default group', async () => {
      mockGetFollowGroupList.mockResolvedValue([
        { id: 'g-1', name: 'Default', sortOrder: 0, memberCount: 5, isDefault: true },
        { id: 'g-2', name: 'Friends', sortOrder: 1, memberCount: 3, isDefault: false },
      ]);

      const store = useFollowStore();
      await store.fetchFollowGroups('user-1');

      expect(store.customGroups).toHaveLength(1);
      expect(store.customGroups[0].id).toBe('g-2');
    });
  });

  describe('createGroup', () => {
    it('calls API then refreshes groups', async () => {
      mockCreateFollowGroup.mockResolvedValue(undefined);
      mockGetFollowGroupList.mockResolvedValue([]);

      const store = useFollowStore();
      await store.createGroup('user-1', 'New Group');

      expect(mockCreateFollowGroup).toHaveBeenCalledWith('user-1', { name: 'New Group', sortOrder: undefined });
      expect(mockGetFollowGroupList).toHaveBeenCalled();
    });
  });

  describe('follow / unfollow', () => {
    it('follow calls API then refreshes list', async () => {
      mockFollowUser.mockResolvedValue(undefined);
      mockGetFollowList.mockResolvedValue({ records: [], total: 0 });

      const store = useFollowStore();
      await store.follow('user-1', 'target-1');

      expect(mockFollowUser).toHaveBeenCalledWith('user-1', { targetUserId: 'target-1', relationGroupId: undefined });
      expect(mockGetFollowList).toHaveBeenCalled();
    });

    it('unfollow calls API then refreshes list', async () => {
      mockUnfollowUser.mockResolvedValue(undefined);
      mockGetFollowList.mockResolvedValue({ records: [], total: 0 });

      const store = useFollowStore();
      await store.unfollow('user-1', 'target-1');

      expect(mockUnfollowUser).toHaveBeenCalledWith('user-1', 'target-1');
      expect(mockGetFollowList).toHaveBeenCalled();
    });
  });

  describe('setSpecial / cancelSpecial', () => {
    it('setSpecial calls API then refreshes both lists', async () => {
      mockSetSpecialFollow.mockResolvedValue(undefined);
      mockGetFollowList.mockResolvedValue({ records: [], total: 0 });
      mockGetSpecialFollowList.mockResolvedValue({ records: [], total: 0 });

      const store = useFollowStore();
      await store.setSpecial('user-1', 'target-1');

      expect(mockSetSpecialFollow).toHaveBeenCalledWith('user-1', 'target-1');
      expect(mockGetFollowList).toHaveBeenCalled();
      expect(mockGetSpecialFollowList).toHaveBeenCalled();
    });
  });

  describe('dismissRecommendation', () => {
    it('removes item from recommendations by userId', async () => {
      mockGetRecommendations.mockResolvedValue({
        records: [
          { userId: 'u-1', nickname: 'A', avatar: '' },
          { userId: 'u-2', nickname: 'B', avatar: '' },
        ],
        total: 2,
      });

      const store = useFollowStore();
      await store.fetchRecommendations(true);
      store.dismissRecommendation('u-1');

      expect(store.recommendations).toHaveLength(1);
      expect(store.recommendations[0].userId).toBe('u-2');
    });
  });

  describe('searchKeyword / selectedGroupId', () => {
    it('setSearchKeyword updates searchKeyword', () => {
      const store = useFollowStore();
      store.setSearchKeyword('test');
      expect(store.searchKeyword).toBe('test');
    });

    it('setSelectedGroupId updates selectedGroupId', () => {
      const store = useFollowStore();
      store.setSelectedGroupId('g-1');
      expect(store.selectedGroupId).toBe('g-1');
    });
  });
});
