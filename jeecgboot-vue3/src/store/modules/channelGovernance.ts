import { defineStore } from 'pinia';
import { store } from '/@/store';
import {
  getGovernanceContentList,
  executeGovernance,
  getRecycleBinList,
  getGovernanceLogList,
} from '/@/api/content/channel/governance';

interface ContentItem {
  id: string;
  title: string;
  contentType: string;
  author: string;
  publishTime: string;
  status: string;
  isPinned: boolean;
  isFeatured: boolean;
}

interface RecycleBinItem {
  id: string;
  title: string;
  contentType: string;
  originalAuthor: string;
  deletedBy: string;
  deleteTime: string;
  deleteReason: string;
  remainingDays: number;
}

interface LogItem {
  id: string;
  time: string;
  operator: string;
  actionType: string;
  targetTitle: string;
  contentId: string;
  result: string;
  remark: string;
}

interface GovernanceFilter {
  channelId: string;
  contentType?: string;
  status?: string;
  author?: string;
  startTime?: string;
  endTime?: string;
  sortBy?: string;
  keyword?: string;
  current: number;
  size: number;
}

interface ChannelGovernanceState {
  contentList: ContentItem[];
  filterParams: GovernanceFilter;
  total: number;
  recycleBinList: RecycleBinItem[];
  recycleBinTotal: number;
  governanceLogList: LogItem[];
  governanceLogTotal: number;
  loading: boolean;
}

export const useChannelGovernanceStore = defineStore({
  id: 'app-channel-governance',
  state: (): ChannelGovernanceState => ({
    contentList: [],
    filterParams: { channelId: '', current: 1, size: 20 },
    total: 0,
    recycleBinList: [],
    recycleBinTotal: 0,
    governanceLogList: [],
    governanceLogTotal: 0,
    loading: false,
  }),
  actions: {
    async fetchList() {
      this.loading = true;
      try {
        const res = await getGovernanceContentList(this.filterParams);
        this.contentList = res.records || [];
        this.total = res.total || 0;
      } finally {
        this.loading = false;
      }
    },
    setFilter(params: Partial<GovernanceFilter>) {
      this.filterParams = { ...this.filterParams, ...params };
    },
    async pin(contentId: string, channelId: string, isPinned: boolean) {
      await executeGovernance({ contentId, channelId, action: isPinned ? 'UNPIN' : 'PIN' });
      await this.fetchList();
    },
    async feature(contentId: string, channelId: string, isFeatured: boolean) {
      await executeGovernance({ contentId, channelId, action: isFeatured ? 'UNFEATURE' : 'FEATURE' });
      await this.fetchList();
    },
    async deleteContent(contentId: string, channelId: string, reason?: string) {
      await executeGovernance({ contentId, channelId, action: 'DELETE', reason });
      await this.fetchList();
    },
    async moveContent(contentId: string, channelId: string, targetChannelId: string) {
      await executeGovernance({ contentId, channelId, action: 'MOVE', targetChannelId });
      await this.fetchList();
    },
    async editAssist(data: { contentId: string; channelId: string; title?: string; tags?: string[]; summary?: string; reason: string }) {
      await executeGovernance({
        contentId: data.contentId,
        channelId: data.channelId,
        action: 'EDIT_ASSIST',
        reason: data.reason,
        editFields: { title: data.title, tags: data.tags, summary: data.summary },
      });
      await this.fetchList();
    },
    async fetchRecycleBin(channelId: string, params?: { contentType?: string; deletedBy?: string; startTime?: string; endTime?: string; current?: number; size?: number }) {
      this.loading = true;
      try {
        const res = await getRecycleBinList({ channelId, ...params });
        this.recycleBinList = res.records || [];
        this.recycleBinTotal = res.total || 0;
      } finally {
        this.loading = false;
      }
    },
    async restore(contentId: string, channelId: string) {
      await executeGovernance({ contentId, channelId, action: 'RESTORE' });
    },
    async fetchGovernanceLog(channelId: string, params?: { actionType?: string; operator?: string; startTime?: string; endTime?: string; keyword?: string; current?: number; size?: number }) {
      this.loading = true;
      try {
        const res = await getGovernanceLogList({ channelId, ...params });
        this.governanceLogList = res.records || [];
        this.governanceLogTotal = res.total || 0;
      } finally {
        this.loading = false;
      }
    },
  },
});

export function useChannelGovernanceStoreWithOut() {
  return useChannelGovernanceStore(store);
}
