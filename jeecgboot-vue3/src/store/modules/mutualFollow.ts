import { defineStore } from 'pinia';
import { ref } from 'vue';
import { store } from '/@/store';

export const useMutualFollowStore = defineStore('social-mutual-follow', () => {
  const statusMap = ref<Record<string, boolean>>({});

  function isMutual(userId: string): boolean {
    return statusMap.value[userId] ?? false;
  }

  function setStatus(userId: string, mutual: boolean) {
    statusMap.value[userId] = mutual;
  }

  function batchSet(map: Record<string, boolean>) {
    Object.assign(statusMap.value, map);
  }

  function clear(userId: string) {
    delete statusMap.value[userId];
  }

  function clearAll() {
    statusMap.value = {};
  }

  async function fetchAndCache(userIds: string[]) {
    const uncached = userIds.filter((id) => !(id in statusMap.value));
    if (uncached.length === 0) return;
    const { getMutualStatus } = await import('/@/api/content/relation');
    const result = await getMutualStatus(uncached);
    if (result) {
      batchSet(result);
    }
  }

  return { statusMap, isMutual, setStatus, batchSet, clear, clearAll, fetchAndCache };
});

export function useMutualFollowStoreWithOut() {
  return useMutualFollowStore(store);
}
