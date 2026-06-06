// Mock the API module
jest.mock('/@/api/content/userStatus', () => ({
  getCurrentStatus: jest.fn(),
  getUserStatus: jest.fn(),
  getStatusHistory: jest.fn(),
  getTransitions: jest.fn(),
  changeUserStatus: jest.fn(),
  releaseUser: jest.fn(),
  batchReleaseUsers: jest.fn(),
  verifySecurity: jest.fn(),
}));

// Mock pinia store
jest.mock('/@/store', () => ({
  store: { install: jest.fn() },
}));

import { createPinia, setActivePinia } from 'pinia';
import {
  getCurrentStatus,
  getUserStatus,
  getStatusHistory,
  getTransitions,
  changeUserStatus,
  releaseUser,
  batchReleaseUsers,
  verifySecurity,
} from '/@/api/content/userStatus';

import { useUserStatusStore } from '/@/store/modules/userStatus';

beforeEach(() => {
  setActivePinia(createPinia());
});

const createStore = () => {
  const store = useUserStatusStore();
  store.$reset();
  return store;
};

describe('UserStatusStore', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('initial state', () => {
    it('should have empty initial state', () => {
      const store = createStore();
      expect(store.currentStatus).toBeNull();
      expect(store.statusDetail).toBeNull();
      expect(store.transitions).toEqual([]);
      expect(store.loading).toBe(false);
      expect(store.lastFetchedAt).toBe(0);
    });
  });

  describe('fetchCurrentStatus', () => {
    it('should fetch and update current status', async () => {
      const mockDetail = { userId: 'u1', status: 'NORMAL', statusName: '正常' };
      (getCurrentStatus as jest.Mock).mockResolvedValue(mockDetail);

      const store = createStore();
      await store.fetchCurrentStatus('u1');

      expect(getCurrentStatus).toHaveBeenCalledWith('u1');
      expect(store.currentStatus).toBe('NORMAL');
      expect(store.statusDetail).toEqual(mockDetail);
      expect(store.lastFetchedAt).toBeGreaterThan(0);
    });

    it('should not update state on API failure', async () => {
      (getCurrentStatus as jest.Mock).mockRejectedValue(new Error('Network error'));

      const store = createStore();
      await store.fetchCurrentStatus('u1');

      expect(store.currentStatus).toBeNull();
      expect(store.statusDetail).toBeNull();
    });

    it('should set loading during fetch', async () => {
      let resolveFn: Function;
      (getCurrentStatus as jest.Mock).mockReturnValue(
        new Promise((resolve) => { resolveFn = resolve; })
      );

      const store = createStore();
      expect(store.loading).toBe(false);

      const promise = store.fetchCurrentStatus('u1');
      expect(store.loading).toBe(true);

      resolveFn!({ userId: 'u1', status: 'NORMAL' });
      await promise;
      expect(store.loading).toBe(false);
    });
  });

  describe('fetchUserStatus', () => {
    it('should fetch status for a specific user', async () => {
      const mockDetail = { userId: 'u2', status: 'MUTED', statusName: '禁言' };
      (getUserStatus as jest.Mock).mockResolvedValue(mockDetail);

      const store = createStore();
      const result = await store.fetchUserStatus('u2');

      expect(getUserStatus).toHaveBeenCalledWith('u2');
      expect(result).toEqual(mockDetail);
    });
  });

  describe('fetchStatusHistory', () => {
    it('should fetch status history for a user', async () => {
      const mockHistory = [{ id: 'h1', fromStatus: 'NORMAL', toStatus: 'MUTED' }];
      (getStatusHistory as jest.Mock).mockResolvedValue(mockHistory);

      const store = createStore();
      const result = await store.fetchStatusHistory('u1');

      expect(getStatusHistory).toHaveBeenCalledWith('u1', undefined);
      expect(result).toEqual(mockHistory);
    });

    it('should pass pagination params', async () => {
      (getStatusHistory as jest.Mock).mockResolvedValue([]);

      const store = createStore();
      await store.fetchStatusHistory('u1', { page: 2, pageSize: 20 });

      expect(getStatusHistory).toHaveBeenCalledWith('u1', { page: 2, pageSize: 20 });
    });
  });

  describe('fetchTransitions', () => {
    it('should fetch transitions for current status', async () => {
      const mockTransitions = ['NORMAL', 'MUTED', 'FROZEN'];
      (getTransitions as jest.Mock).mockResolvedValue(mockTransitions);

      const store = createStore();
      const result = await store.fetchTransitions('NORMAL');

      expect(getTransitions).toHaveBeenCalledWith('NORMAL');
      expect(store.transitions).toEqual(mockTransitions);
      expect(result).toEqual(mockTransitions);
    });
  });

  describe('changeStatus', () => {
    it('should change user status and refresh', async () => {
      (changeUserStatus as jest.Mock).mockResolvedValue(undefined);
      const mockDetail = { userId: 'u1', status: 'MUTED', statusName: '禁言' };
      (getCurrentStatus as jest.Mock).mockResolvedValue(mockDetail);

      const store = createStore();
      await store.changeStatus('u1', { toStatus: 'MUTED', reason: '违规' });

      expect(changeUserStatus).toHaveBeenCalledWith('u1', { toStatus: 'MUTED', reason: '违规' });
      expect(getCurrentStatus).toHaveBeenCalledWith('u1');
    });
  });

  describe('releaseUser', () => {
    it('should release user and refresh', async () => {
      (releaseUser as jest.Mock).mockResolvedValue(undefined);
      const mockDetail = { userId: 'u1', status: 'NORMAL', statusName: '正常' };
      (getCurrentStatus as jest.Mock).mockResolvedValue(mockDetail);

      const store = createStore();
      await store.releaseUser('u1', '误封');

      expect(releaseUser).toHaveBeenCalledWith('u1', '误封');
      expect(getCurrentStatus).toHaveBeenCalledWith('u1');
    });
  });

  describe('batchRelease', () => {
    it('should batch release users', async () => {
      (batchReleaseUsers as jest.Mock).mockResolvedValue(undefined);

      const store = createStore();
      await store.batchRelease(['u1', 'u2'], '批量解禁');

      expect(batchReleaseUsers).toHaveBeenCalledWith(['u1', 'u2'], '批量解禁');
    });
  });

  describe('verifySecurity', () => {
    it('should verify security code', async () => {
      (verifySecurity as jest.Mock).mockResolvedValue(undefined);

      const store = createStore();
      await store.verifySecurity('13800138000', '123456');

      expect(verifySecurity).toHaveBeenCalledWith('13800138000', '123456');
    });
  });

  describe('refreshStatus', () => {
    it('should force refresh ignoring cache', async () => {
      const mockDetail = { userId: 'u1', status: 'FROZEN', statusName: '冻结' };
      (getCurrentStatus as jest.Mock).mockResolvedValue(mockDetail);

      const store = createStore();
      store.lastFetchedAt = Date.now();
      await store.refreshStatus('u1');

      expect(getCurrentStatus).toHaveBeenCalledWith('u1');
      expect(store.currentStatus).toBe('FROZEN');
    });
  });

  describe('computed getters', () => {
    it('isRestricted should return true for restricted statuses', () => {
      const store = createStore();
      store.currentStatus = 'MUTED';
      expect(store.isRestricted).toBe(true);

      store.currentStatus = 'FROZEN';
      expect(store.isRestricted).toBe(true);

      store.currentStatus = 'BANNED';
      expect(store.isRestricted).toBe(true);

      store.currentStatus = 'NORMAL';
      expect(store.isRestricted).toBe(false);
    });

    it('isFrozenOrBanned should return true only for FROZEN/BANNED', () => {
      const store = createStore();
      store.currentStatus = 'FROZEN';
      expect(store.isFrozenOrBanned).toBe(true);

      store.currentStatus = 'BANNED';
      expect(store.isFrozenOrBanned).toBe(true);

      store.currentStatus = 'MUTED';
      expect(store.isFrozenOrBanned).toBe(false);

      store.currentStatus = 'NORMAL';
      expect(store.isFrozenOrBanned).toBe(false);
    });

    it('statusEndTime should return end time from detail', () => {
      const store = createStore();
      store.statusDetail = { userId: 'u1', status: 'MUTED', statusName: '禁言', endTime: '2026-06-10' } as any;
      expect(store.statusEndTime).toBe('2026-06-10');
    });
  });
});
