import { defineStore } from 'pinia';
import { store } from '/@/store';
import type { UserStatusDetail, UserStatusChangeReq, UserStatusEnum } from '/@/api/content/model/userStatusModel';
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

interface UserStatusState {
  currentStatus: string | null;
  statusDetail: UserStatusDetail | null;
  transitions: UserStatusEnum[];
  loading: boolean;
  lastFetchedAt: number;
}

const RESTRICTED_STATUSES = ['MUTED', 'RESTRICTED_RECOMMEND', 'FROZEN', 'BANNED', 'DEACTIVATING', 'DEACTIVATED'];
const FROZEN_OR_BANNED = ['FROZEN', 'BANNED'];

export const useUserStatusStore = defineStore({
  id: 'app-user-status',
  state: (): UserStatusState => ({
    currentStatus: null,
    statusDetail: null,
    transitions: [],
    loading: false,
    lastFetchedAt: 0,
  }),
  getters: {
    isRestricted(): boolean {
      return this.currentStatus ? RESTRICTED_STATUSES.includes(this.currentStatus) : false;
    },
    isFrozenOrBanned(): boolean {
      return this.currentStatus ? FROZEN_OR_BANNED.includes(this.currentStatus) : false;
    },
    statusEndTime(): string | null {
      return this.statusDetail?.endTime ?? null;
    },
  },
  actions: {
    async fetchCurrentStatus(userId: string) {
      this.loading = true;
      try {
        const detail = await getCurrentStatus(userId);
        if (detail) {
          this.currentStatus = detail.status;
          this.statusDetail = detail;
          this.lastFetchedAt = Date.now();
        }
      } catch {
        // Keep previous state on failure — don't block rendering
      } finally {
        this.loading = false;
      }
    },

    async fetchUserStatus(userId: string) {
      return await getUserStatus(userId);
    },

    async fetchStatusHistory(userId: string, params?: { page?: number; pageSize?: number }) {
      return await getStatusHistory(userId, params);
    },

    async fetchTransitions(currentStatus: string) {
      const result = await getTransitions(currentStatus);
      if (result) {
        this.transitions = result;
      }
      return result;
    },

    async changeStatus(userId: string, payload: UserStatusChangeReq) {
      await changeUserStatus(userId, payload);
      await this.fetchCurrentStatus(userId);
    },

    async releaseUser(userId: string, reason: string) {
      await releaseUser(userId, reason);
      await this.fetchCurrentStatus(userId);
    },

    async batchRelease(userIds: string[], reason: string) {
      await batchReleaseUsers(userIds, reason);
    },

    async verifySecurity(phone: string, verifyCode: string) {
      await verifySecurity(phone, verifyCode);
    },

    async refreshStatus(userId: string) {
      this.lastFetchedAt = 0;
      await this.fetchCurrentStatus(userId);
    },
  },
});

export function useUserStatusStoreWithOut() {
  return useUserStatusStore(store);
}
