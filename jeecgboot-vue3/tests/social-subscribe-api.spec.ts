import { vi } from 'vitest';
vi.mock('/@/utils/http/axios', () => ({
  defHttp: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

import { defHttp } from '/@/utils/http/axios';

import {
  subscribeSource,
  cancelSubscription,
  pauseSubscription,
  resumeSubscription,
  getSubscribeList,
  getSubscribeFeed,
  getSubscribePlaza,
  getSubscribeSourceDetail,
  subscribeFromPlaza,
  batchPauseSubscribe,
  batchResumeSubscribe,
  batchCancelSubscribe,
  getNotificationPreference,
  saveNotificationPreference,
} from '/@/api/content/subscribe';

describe('api/content/subscribe', () => {
  beforeEach(() => {
    defHttp.get.mockReset();
    defHttp.post.mockReset();
  });

  describe('subscribeSource', () => {
    it('POSTs to /content/user/subscription/subscribe with userId + data', () => {
      subscribeSource('user-1', { sourceId: 'src-1', sourceType: 'RSS' });
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/subscription/subscribe',
        params: { userId: 'user-1' },
        data: { sourceId: 'src-1', sourceType: 'RSS' },
      });
    });
  });

  describe('cancelSubscription', () => {
    it('POSTs to /content/user/subscription/cancel with userId + sourceId', () => {
      cancelSubscription('user-1', 'src-1');
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/subscription/cancel',
        params: { userId: 'user-1', sourceId: 'src-1' },
      });
    });
  });

  describe('pauseSubscription / resumeSubscription', () => {
    it('POSTs to /pause', () => {
      pauseSubscription('user-1', 'src-1');
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/subscription/pause',
        params: { userId: 'user-1', sourceId: 'src-1' },
      });
    });

    it('POSTs to /resume', () => {
      resumeSubscription('user-1', 'src-1');
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/subscription/resume',
        params: { userId: 'user-1', sourceId: 'src-1' },
      });
    });
  });

  describe('getSubscribeList', () => {
    it('GETs /list with userId + filters', () => {
      getSubscribeList('user-1', { keyword: 'tech', sourceType: 'RSS', pageNo: 1, pageSize: 20 });
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/content/user/subscription/list',
        params: { userId: 'user-1', keyword: 'tech', sourceType: 'RSS', pageNo: 1, pageSize: 20 },
      });
    });

    it('works without optional params', () => {
      getSubscribeList('user-1');
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/content/user/subscription/list',
        params: { userId: 'user-1' },
      });
    });
  });

  describe('getSubscribeFeed', () => {
    it('GETs /feed with pagination + sourceType', () => {
      getSubscribeFeed({ page: 1, size: 20, sourceType: 'RSS' });
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/content/user/subscription/feed',
        params: { page: 1, size: 20, sourceType: 'RSS' },
      });
    });
  });

  describe('getSubscribePlaza', () => {
    it('GETs /plaza with keyword + category + pagination', () => {
      getSubscribePlaza({ keyword: 'tech', category: '科技', page: 1, size: 20, sort: 'popularity' });
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/content/user/subscription/plaza',
        params: { keyword: 'tech', category: '科技', page: 1, size: 20, sort: 'popularity' },
      });
    });
  });

  describe('getSubscribeSourceDetail', () => {
    it('GETs /source/detail with sourceId', () => {
      getSubscribeSourceDetail('src-1');
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/content/user/subscription/source/detail',
        params: { sourceId: 'src-1' },
      });
    });
  });

  describe('subscribeFromPlaza', () => {
    it('POSTs to /source/subscribe', () => {
      subscribeFromPlaza('user-1', { sourceId: 'src-1', sourceType: 'RSS' });
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/subscription/source/subscribe',
        params: { userId: 'user-1' },
        data: { sourceId: 'src-1', sourceType: 'RSS' },
      });
    });
  });

  describe('batch APIs', () => {
    it('batchPauseSubscribe POSTs to /batch/pause', () => {
      batchPauseSubscribe('user-1', { sourceIds: ['s-1', 's-2'] });
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/subscription/batch/pause',
        params: { userId: 'user-1' },
        data: { sourceIds: ['s-1', 's-2'] },
      });
    });

    it('batchResumeSubscribe POSTs to /batch/resume', () => {
      batchResumeSubscribe('user-1', { sourceIds: ['s-1'] });
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/subscription/batch/resume',
        params: { userId: 'user-1' },
        data: { sourceIds: ['s-1'] },
      });
    });

    it('batchCancelSubscribe POSTs to /batch/cancel', () => {
      batchCancelSubscribe('user-1', { sourceIds: ['s-1'] });
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/subscription/batch/cancel',
        params: { userId: 'user-1' },
        data: { sourceIds: ['s-1'] },
      });
    });
  });

  describe('notification preference APIs', () => {
    it('getNotificationPreference GETs with userId + sourceId', () => {
      getNotificationPreference('user-1', 'src-1');
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/content/user/subscription/notification/preference',
        params: { userId: 'user-1', sourceId: 'src-1' },
      });
    });

    it('saveNotificationPreference POSTs with userId + sourceId + config', () => {
      const config = { channelInApp: true, channelPush: false, channelEmail: true, frequency: 'daily', quietStart: '22:00', quietEnd: '08:00' };
      saveNotificationPreference('user-1', 'src-1', config);
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/subscription/notification/preference',
        params: { userId: 'user-1', sourceId: 'src-1' },
        data: config,
      });
    });
  });
});
