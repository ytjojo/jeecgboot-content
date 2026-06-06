import { defineStore } from 'pinia';
import { ref } from 'vue';
import { store } from '/@/store';
import type { ChannelVO, ChannelReviewVO } from '/@/api/content/channel/model/channelModel';

/** 频道类型选项 */
export const channelTypeOptions = [
  { label: '系统频道', value: 'system' },
  { label: '个人频道', value: 'personal' },
  { label: '组织频道', value: 'organization' },
];

/** 频道状态选项 */
export const channelStatusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '待审核', value: 'PENDING_REVIEW' },
  { label: '已激活', value: 'ACTIVE' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '删除冷静期', value: 'DELETE_COOLING' },
  { label: '已删除', value: 'DELETED' },
];

export const useChannelStore = defineStore('channel', () => {
  // ===== State =====
  const currentChannel = ref<ChannelVO | null>(null);
  const channelList = ref<ChannelVO[]>([]);
  const reviewQueue = ref<ChannelReviewVO[]>([]);

  // ===== Actions =====
  function setCurrentChannel(channel: ChannelVO | null) {
    currentChannel.value = channel;
  }

  function setChannelList(list: ChannelVO[]) {
    channelList.value = list;
  }

  function setReviewQueue(list: ChannelReviewVO[]) {
    reviewQueue.value = list;
  }

  function clearCurrentChannel() {
    currentChannel.value = null;
  }

  return {
    // State
    currentChannel,
    channelList,
    reviewQueue,
    // Actions
    setCurrentChannel,
    setChannelList,
    setReviewQueue,
    clearCurrentChannel,
  };
});

// Support use outside of setup
export function useChannelStoreWithOut() {
  return useChannelStore(store);
}
