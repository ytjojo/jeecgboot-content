import { defineStore } from 'pinia';
import { store } from '/@/store';
import { getReviewList, executeReview, getReviewStats } from '/@/api/content/channel/review';

interface ReviewItem {
  id: string;
  title: string;
  contentType: string;
  submitter: string;
  submitTime: string;
  sourceScene: string;
  hitRule: string;
  isTimeout: boolean;
}

interface ReviewStats {
  total: number;
  timeoutCount: number;
}

interface ReviewFilter {
  channelId: string;
  contentType?: string;
  submitter?: string;
  submitTimeStart?: string;
  submitTimeEnd?: string;
  reviewStatus?: string;
  timeoutStatus?: string;
  keyword?: string;
  current: number;
  size: number;
}

interface ChannelReviewState {
  reviewList: ReviewItem[];
  filterParams: ReviewFilter;
  selectedIds: string[];
  stats: ReviewStats;
  total: number;
  loading: boolean;
}

export const useChannelReviewStore = defineStore({
  id: 'app-channel-review',
  state: (): ChannelReviewState => ({
    reviewList: [],
    filterParams: {
      channelId: '',
      current: 1,
      size: 20,
    },
    selectedIds: [],
    stats: { total: 0, timeoutCount: 0 },
    total: 0,
    loading: false,
  }),
  actions: {
    async fetchList() {
      this.loading = true;
      try {
        const res = await getReviewList(this.filterParams);
        this.reviewList = res.records || [];
        this.total = res.total || 0;
      } finally {
        this.loading = false;
      }
    },
    async fetchStats(channelId: string) {
      try {
        const res = await getReviewStats(channelId);
        this.stats = res || { total: 0, timeoutCount: 0 };
      } catch {
        // 统计获取失败不阻塞主流程
      }
    },
    setFilter(params: Partial<ReviewFilter>) {
      this.filterParams = { ...this.filterParams, ...params };
    },
    setSelectedIds(ids: string[]) {
      this.selectedIds = ids;
    },
    async approve(reviewId: string) {
      await executeReview({ reviewId, action: 'APPROVE' });
      await this.fetchList();
    },
    async reject(reviewId: string, rejectReason: string) {
      await executeReview({ reviewId, action: 'REJECT', rejectReason });
      await this.fetchList();
    },
    async batchApprove() {
      for (const id of this.selectedIds) {
        await executeReview({ reviewId: id, action: 'APPROVE' });
      }
      this.selectedIds = [];
      await this.fetchList();
    },
    async batchReject(reason: string) {
      for (const id of this.selectedIds) {
        await executeReview({ reviewId: id, action: 'REJECT', rejectReason: reason });
      }
      this.selectedIds = [];
      await this.fetchList();
    },
  },
});

export function useChannelReviewStoreWithOut() {
  return useChannelReviewStore(store);
}
