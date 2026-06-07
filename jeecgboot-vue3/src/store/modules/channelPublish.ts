import { defineStore } from 'pinia';
import { store } from '/@/store';
import {
  getAvailableChannels,
  getScheduledList,
  updateScheduledPublish,
  cancelScheduledPublish,
} from '/@/api/content/channel/publish';

interface Channel {
  id: string;
  name: string;
  type: string;
  userRole: string;
  publishResult: string;
  publishable: boolean;
  reason?: string;
}

interface PublishResultItem {
  channelId: string;
  channelName: string;
  status: 'success' | 'review' | 'pending' | 'fail';
  failReason?: string;
}

interface ScheduledTask {
  id: string;
  contentId: string;
  contentTitle: string;
  channelName: string;
  scheduledTime: string;
  status: string;
}

interface ChannelPublishState {
  availableChannels: Channel[];
  selectedChannels: Channel[];
  publishResult: Record<string, PublishResultItem>;
  scheduledTime: string | null;
  maxChannelCount: number;
  scheduledTaskList: ScheduledTask[];
  loading: boolean;
}

export const useChannelPublishStore = defineStore({
  id: 'app-channel-publish',
  state: (): ChannelPublishState => ({
    availableChannels: [],
    selectedChannels: [],
    publishResult: {},
    scheduledTime: null,
    maxChannelCount: 5,
    scheduledTaskList: [],
    loading: false,
  }),
  actions: {
    async fetchAvailableChannels() {
      this.loading = true;
      try {
        const res = await getAvailableChannels();
        this.availableChannels = res.list || [];
        this.maxChannelCount = res.maxChannelCount || 5;
      } finally {
        this.loading = false;
      }
    },
    addChannel(channel: Channel) {
      if (this.selectedChannels.length >= this.maxChannelCount) return;
      if (this.selectedChannels.find((c) => c.id === channel.id)) return;
      this.selectedChannels.push(channel);
    },
    removeChannel(channelId: string) {
      this.selectedChannels = this.selectedChannels.filter((c) => c.id !== channelId);
    },
    setPublishResult(result: Record<string, PublishResultItem>) {
      this.publishResult = result;
    },
    clearResult() {
      this.publishResult = {};
    },
    setScheduledTime(time: string | null) {
      this.scheduledTime = time;
    },
    async fetchScheduledTasks() {
      this.loading = true;
      try {
        const res = await getScheduledList();
        this.scheduledTaskList = res || [];
      } finally {
        this.loading = false;
      }
    },
    async editScheduledTime(id: string, newTime: string) {
      await updateScheduledPublish(id, { scheduledTime: newTime });
      await this.fetchScheduledTasks();
    },
    async cancelScheduledTask(id: string) {
      await cancelScheduledPublish(id);
      this.scheduledTaskList = this.scheduledTaskList.filter((t) => t.id !== id);
    },
  },
});

export function useChannelPublishStoreWithOut() {
  return useChannelPublishStore(store);
}
