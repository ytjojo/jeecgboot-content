import { describe, it, expect, beforeEach, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';

const mockSearchChannels = vi.fn();
const mockSubmitSearchFeedback = vi.fn();

vi.mock('/@/api/content/channelDiscovery', () => ({
  searchChannels: (...args: any[]) => mockSearchChannels(...args),
  submitSearchFeedback: (...args: any[]) => mockSubmitSearchFeedback(...args),
}));

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: vi.fn((key: string) => store[key] || null),
    setItem: vi.fn((key: string, value: string) => { store[key] = value; }),
    removeItem: vi.fn((key: string) => { delete store[key]; }),
    clear: vi.fn(() => { store = {}; }),
  };
})();
Object.defineProperty(global, 'localStorage', { value: localStorageMock });

import { useChannelSearchStore } from '/@/store/modules/channelSearch';

describe('useChannelSearchStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
    localStorageMock.clear();
  });

  it('should have correct initial state', () => {
    const store = useChannelSearchStore();
    expect(store.keyword).toBe('');
    expect(store.results).toEqual([]);
    expect(store.total).toBe(0);
    expect(store.loading).toBe(false);
    expect(store.sortBy).toBe('relevance');
  });

  it('should not search with empty keyword', async () => {
    const store = useChannelSearchStore();
    store.keyword = '';
    await store.executeSearch();
    expect(mockSearchChannels).not.toHaveBeenCalled();
  });

  it('should execute search with keyword', async () => {
    const mockResults = {
      records: [
        { id: '1', name: 'Test Channel', matchReason: '名称匹配' },
      ],
      total: 1,
      page: 1,
      pageSize: 20,
    };
    mockSearchChannels.mockResolvedValue(mockResults);

    const store = useChannelSearchStore();
    await store.executeSearch({ keyword: 'Test' });

    expect(mockSearchChannels).toHaveBeenCalledWith(
      expect.objectContaining({ keyword: 'Test', page: 1, pageSize: 20 }),
    );
    expect(store.results).toHaveLength(1);
    expect(store.total).toBe(1);
    expect(store.keyword).toBe('Test');
  });

  it('should add keyword to search history', async () => {
    mockSearchChannels.mockResolvedValue({ records: [], total: 0 });

    const store = useChannelSearchStore();
    await store.executeSearch({ keyword: 'test keyword' });

    expect(store.searchHistory).toContain('test keyword');
  });

  it('should deduplicate search history', async () => {
    mockSearchChannels.mockResolvedValue({ records: [], total: 0 });

    const store = useChannelSearchStore();
    store.searchHistory = ['old', 'test'];

    await store.executeSearch({ keyword: 'test' });

    expect(store.searchHistory).toEqual(['test', 'old']);
  });

  it('should limit search history to 10 items', async () => {
    mockSearchChannels.mockResolvedValue({ records: [], total: 0 });

    const store = useChannelSearchStore();
    store.searchHistory = Array.from({ length: 10 }, (_, i) => `item-${i}`);

    await store.executeSearch({ keyword: 'new-item' });

    expect(store.searchHistory).toHaveLength(10);
    expect(store.searchHistory[0]).toBe('new-item');
  });

  it('should remove single history item', () => {
    const store = useChannelSearchStore();
    store.searchHistory = ['a', 'b', 'c'];
    store.removeHistoryItem('b');
    expect(store.searchHistory).toEqual(['a', 'c']);
  });

  it('should clear all history', () => {
    const store = useChannelSearchStore();
    store.searchHistory = ['a', 'b', 'c'];
    store.clearHistory();
    expect(store.searchHistory).toEqual([]);
  });

  it('should set filter and re-search', async () => {
    mockSearchChannels.mockResolvedValue({ records: [], total: 0 });

    const store = useChannelSearchStore();
    store.keyword = 'test';
    await store.setFilter('channelType', 'personal');

    expect(store.channelType).toBe('personal');
    expect(mockSearchChannels).toHaveBeenCalled();
  });

  it('should clear filters', () => {
    const store = useChannelSearchStore();
    store.channelType = 'personal';
    store.categoryId = 'cat1';
    store.sortBy = 'active';
    store.clearFilters();

    expect(store.channelType).toBeUndefined();
    expect(store.categoryId).toBeUndefined();
    expect(store.sortBy).toBe('relevance');
  });

  it('should load more results', async () => {
    const firstPage = { records: [{ id: '1', name: 'A' }], total: 2 };
    const secondPage = { records: [{ id: '2', name: 'B' }], total: 2 };
    mockSearchChannels
      .mockResolvedValueOnce(firstPage)
      .mockResolvedValueOnce(secondPage);

    const store = useChannelSearchStore();
    await store.executeSearch({ keyword: 'test' });
    expect(store.results).toHaveLength(1);

    await store.loadMore();
    expect(store.results).toHaveLength(2);
    expect(store.currentPage).toBe(2);
  });

  it('should not load more if no more results', async () => {
    const store = useChannelSearchStore();
    store.keyword = 'test';
    store.total = 5;
    store.results = Array.from({ length: 5 }, (_, i) => ({ id: String(i), name: `Item ${i}` }) as any);

    await store.loadMore();
    // should not call API since there are no more results
    expect(mockSearchChannels).not.toHaveBeenCalled();
  });

  it('should reset search state', () => {
    const store = useChannelSearchStore();
    store.keyword = 'test';
    store.results = [{ id: '1' } as any];
    store.total = 10;
    store.channelType = 'personal';
    store.error = 'error msg';
    store.resetSearch();

    expect(store.keyword).toBe('');
    expect(store.results).toEqual([]);
    expect(store.total).toBe(0);
    expect(store.channelType).toBeUndefined();
    expect(store.error).toBeNull();
  });

  it('should submit search feedback', async () => {
    mockSubmitSearchFeedback.mockResolvedValue({});

    const store = useChannelSearchStore();
    store.keyword = 'test';
    const result = await store.feedbackSearch(true);

    expect(mockSubmitSearchFeedback).toHaveBeenCalledWith({ keyword: 'test', helpful: true });
    expect(result).toBe(true);
  });
});
