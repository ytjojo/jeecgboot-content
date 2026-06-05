import { defineStore } from 'pinia';
import { ref } from 'vue';
import { store } from '/@/store';

export interface InviteStats {
  totalInvited: number;
  totalReward: number;
  pendingReward: number;
}

export const useInviteStore = defineStore('social-invite', () => {
  const inviteCode = ref<string | null>(null);
  const stats = ref<InviteStats | null>(null);

  async function loadInviteCode(userId: string): Promise<string | null> {
    if (inviteCode.value) return inviteCode.value;
    const { generateInviteCode } = await import('/@/api/content/invite');
    const res = await generateInviteCode(userId);
    inviteCode.value = res?.inviteCode || null;
    return inviteCode.value;
  }

  async function loadStats(userId: string): Promise<InviteStats | null> {
    const { getInviteStats } = await import('/@/api/content/invite');
    const res = await getInviteStats(userId);
    if (res) {
      stats.value = {
        totalInvited: res.totalInvited || 0,
        totalReward: res.totalReward || 0,
        pendingReward: res.pendingReward || 0,
      };
    }
    return stats.value;
  }

  function clear() {
    inviteCode.value = null;
    stats.value = null;
  }

  return { inviteCode, stats, loadInviteCode, loadStats, clear };
});

export function useInviteStoreWithOut() {
  return useInviteStore(store);
}
