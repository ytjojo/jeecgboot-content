import { vi } from 'vitest';
const mockGetMutualStatus = vi.fn();
vi.mock('/@/api/content/relation', () => ({
  getMutualStatus: (...args: any[]) => mockGetMutualStatus(...args),
}));
vi.mock('/@/store', () => ({ store: {} }));

import { setActivePinia, createPinia } from 'pinia';
import { useMutualFollowStore } from '/@/store/modules/mutualFollow';

describe('store/modules/mutualFollow', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it('initializes with empty cache', () => {
    const store = useMutualFollowStore();
    expect(store.isMutual('user-1')).toBe(false);
  });

  it('sets and retrieves mutual follow status', () => {
    const store = useMutualFollowStore();
    store.setStatus('user-1', true);
    expect(store.isMutual('user-1')).toBe(true);
    store.setStatus('user-2', false);
    expect(store.isMutual('user-2')).toBe(false);
  });

  it('batch updates from API response', () => {
    const store = useMutualFollowStore();
    store.batchSet({ 'user-1': true, 'user-2': false, 'user-3': true });
    expect(store.isMutual('user-1')).toBe(true);
    expect(store.isMutual('user-2')).toBe(false);
    expect(store.isMutual('user-3')).toBe(true);
  });

  it('clears specific user cache', () => {
    const store = useMutualFollowStore();
    store.setStatus('user-1', true);
    store.clear('user-1');
    expect(store.isMutual('user-1')).toBe(false);
  });

  it('clears all cache', () => {
    const store = useMutualFollowStore();
    store.batchSet({ 'user-1': true, 'user-2': true });
    store.clearAll();
    expect(store.isMutual('user-1')).toBe(false);
    expect(store.isMutual('user-2')).toBe(false);
  });

  it('fetches mutual status from API and caches result', async () => {
    mockGetMutualStatus.mockResolvedValue({ 'user-1': true, 'user-2': false });
    const store = useMutualFollowStore();
    await store.fetchAndCache(['user-1', 'user-2']);
    expect(mockGetMutualStatus).toHaveBeenCalledWith(['user-1', 'user-2']);
    expect(store.isMutual('user-1')).toBe(true);
    expect(store.isMutual('user-2')).toBe(false);
  });

  it('skips API call when all requested users are cached', async () => {
    const store = useMutualFollowStore();
    store.setStatus('user-1', true);
    await store.fetchAndCache(['user-1']);
    expect(mockGetMutualStatus).not.toHaveBeenCalled();
  });

  it('only fetches uncached users', async () => {
    mockGetMutualStatus.mockResolvedValue({ 'user-2': true });
    const store = useMutualFollowStore();
    store.setStatus('user-1', true);
    await store.fetchAndCache(['user-1', 'user-2']);
    expect(mockGetMutualStatus).toHaveBeenCalledWith(['user-2']);
  });
});
