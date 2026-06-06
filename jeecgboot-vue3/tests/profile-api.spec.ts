import { vi } from 'vitest';
import { getProfileDetail, updateProfile, updateHomepage, restoreHomepageDefaults, getHomepageModules, updatePrivacy, getBadgeList, getBadgeDetail, getHistoryList, restoreHistory } from '/@/api/content/profile';

vi.mock('/@/utils/http/axios', () => ({
  defHttp: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

import { defHttp } from '/@/utils/http/axios';

describe('api/content/profile', () => {
  beforeEach(() => {
    defHttp.get.mockReset();
    defHttp.post.mockReset();
  });

  describe('getProfileDetail', () => {
    it('hits /content/user/profile/detail with ownerUserId + viewerUserId', () => {
      getProfileDetail('owner-1', 'viewer-1');
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/content/user/profile/detail',
        params: { ownerUserId: 'owner-1', viewerUserId: 'viewer-1' },
      });
    });
  });

  describe('updateProfile', () => {
    it('POSTs to /content/user/profile/update with userId in query and body', () => {
      const req = { nickname: 'A', avatar: 'https://cdn/x.png' };
      updateProfile('user-1', req);
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/profile/update',
        params: { userId: 'user-1' },
        data: req,
      });
    });
  });

  describe('homepage APIs', () => {
    it('updateHomepage sends body to /content/user/profile/homepage/update', () => {
      const req = { themeColor: '#ff0000' };
      updateHomepage('user-1', req);
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/profile/homepage/update',
        params: { userId: 'user-1' },
        data: req,
      });
    });

    it('restoreHomepageDefaults POSTs to /defaults/restore with userId', () => {
      restoreHomepageDefaults('user-1');
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/profile/homepage/defaults/restore',
        params: { userId: 'user-1' },
      });
    });

    it('getHomepageModules GETs /content/user/profile/homepage/modules', () => {
      getHomepageModules('user-1');
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/content/user/profile/homepage/modules',
        params: { userId: 'user-1' },
      });
    });
  });

  describe('privacy APIs', () => {
    it('updatePrivacy POSTs to /content/user/profile/privacy/update with userId', () => {
      const req = { bioVisibility: 'PUBLIC' as const, onlineStatusVisibility: 'HIDDEN' as const };
      updatePrivacy('user-1', req);
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/profile/privacy/update',
        params: { userId: 'user-1' },
        data: req,
      });
    });
  });

  describe('badge APIs', () => {
    it('getBadgeList GETs /content/user/profile/badge/list with userId', () => {
      getBadgeList('user-1');
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/content/user/profile/badge/list',
        params: { userId: 'user-1' },
      });
    });

    it('getBadgeDetail GETs /content/user/profile/badge/detail with badgeId', () => {
      getBadgeDetail('badge-99');
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/content/user/profile/badge/detail',
        params: { badgeId: 'badge-99' },
      });
    });
  });

  describe('history APIs', () => {
    it('getHistoryList GETs with userId + historyType=NICKNAME', () => {
      getHistoryList('user-1', 'NICKNAME');
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/content/user/profile/history/list',
        params: { userId: 'user-1', historyType: 'NICKNAME' },
      });
    });

    it('getHistoryList GETs with userId + historyType=AVATAR', () => {
      getHistoryList('user-1', 'AVATAR');
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/content/user/profile/history/list',
        params: { userId: 'user-1', historyType: 'AVATAR' },
      });
    });

    it('restoreHistory POSTs /content/user/profile/history/restore with userId+historyId', () => {
      restoreHistory('user-1', 'hist-77');
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/profile/history/restore',
        params: { userId: 'user-1', historyId: 'hist-77' },
      });
    });
  });
});
