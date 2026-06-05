const mockGenerateInviteCode = jest.fn();
const mockGetInviteStats = jest.fn();
jest.mock('/@/api/content/invite', () => ({
  generateInviteCode: (...args: any[]) => mockGenerateInviteCode(...args),
  getInviteStats: (...args: any[]) => mockGetInviteStats(...args),
}));
jest.mock('/@/store', () => ({ store: {} }));

import { setActivePinia, createPinia } from 'pinia';
import { useInviteStore } from '/@/store/modules/invite';

describe('store/modules/invite', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    jest.clearAllMocks();
  });

  it('initializes with null invite code and stats', () => {
    const store = useInviteStore();
    expect(store.inviteCode).toBeNull();
    expect(store.stats).toBeNull();
  });

  it('loads invite code from API and caches it', async () => {
    mockGenerateInviteCode.mockResolvedValue({ inviteCode: 'ABC123' });
    const store = useInviteStore();
    const code = await store.loadInviteCode('user-1');
    expect(code).toBe('ABC123');
    expect(store.inviteCode).toBe('ABC123');
    expect(mockGenerateInviteCode).toHaveBeenCalledWith('user-1');
  });

  it('returns cached invite code on subsequent calls', async () => {
    mockGenerateInviteCode.mockResolvedValue({ inviteCode: 'ABC123' });
    const store = useInviteStore();
    await store.loadInviteCode('user-1');
    const code = await store.loadInviteCode('user-1');
    expect(code).toBe('ABC123');
    expect(mockGenerateInviteCode).toHaveBeenCalledTimes(1);
  });

  it('loads invite stats from API', async () => {
    const statsData = { totalInvited: 5, totalReward: 100, pendingReward: 20 };
    mockGetInviteStats.mockResolvedValue(statsData);
    const store = useInviteStore();
    const result = await store.loadStats('user-1');
    expect(result).toEqual(statsData);
    expect(store.stats).toEqual(statsData);
  });

  it('clears cached data', async () => {
    mockGenerateInviteCode.mockResolvedValue({ inviteCode: 'ABC123' });
    const store = useInviteStore();
    await store.loadInviteCode('user-1');
    store.clear();
    expect(store.inviteCode).toBeNull();
    expect(store.stats).toBeNull();
  });
});
