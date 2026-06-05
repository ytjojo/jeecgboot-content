jest.mock('/@/utils/http/axios', () => ({
  defHttp: {
    get: jest.fn(),
    post: jest.fn(),
  },
}));

const defHttp = require('/@/utils/http/axios').defHttp as { get: jest.Mock; post: jest.Mock };

import {
  getMutualFollowList,
  getRelationDetail,
  followUser,
  unfollowUser,
  setSpecialFollow,
  cancelSpecialFollow,
  getFollowGroupList,
  createFollowGroup,
  renameFollowGroup,
  deleteFollowGroup,
  moveFollowGroup,
  removeFromGroup,
  getFollowList,
  getSpecialFollowList,
  getRecommendations,
  batchUnfollow,
  batchCancelSpecial,
  getFollowingFeed,
} from '/@/api/content/relation';

describe('api/content/relation', () => {
  beforeEach(() => {
    defHttp.get.mockReset();
    defHttp.post.mockReset();
  });

  describe('getMutualFollowList', () => {
    it('GETs /content/user/relation/mutual-follow-list with userId + params', () => {
      getMutualFollowList('user-1', { keyword: 'test', pageNo: 1, pageSize: 10 });
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/content/user/relation/mutual-follow-list',
        params: { userId: 'user-1', keyword: 'test', pageNo: 1, pageSize: 10 },
      });
    });

    it('works without optional params', () => {
      getMutualFollowList('user-1');
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/content/user/relation/mutual-follow-list',
        params: { userId: 'user-1' },
      });
    });
  });

  describe('getRelationDetail', () => {
    it('GETs /content/user/relation/detail with userId + targetUserId', () => {
      getRelationDetail('user-1', 'target-1');
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/content/user/relation/detail',
        params: { userId: 'user-1', targetUserId: 'target-1' },
      });
    });
  });

  describe('followUser', () => {
    it('POSTs to /content/user/relation/follow with userId and data', () => {
      followUser('user-1', { targetUserId: 'target-1', relationGroupId: 'group-1' });
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/relation/follow',
        params: { userId: 'user-1' },
        data: { targetUserId: 'target-1', relationGroupId: 'group-1' },
      });
    });

    it('works without optional groupId', () => {
      followUser('user-1', { targetUserId: 'target-1' });
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/relation/follow',
        params: { userId: 'user-1' },
        data: { targetUserId: 'target-1' },
      });
    });
  });

  describe('unfollowUser', () => {
    it('POSTs to /content/user/relation/unfollow with userId + targetUserId', () => {
      unfollowUser('user-1', 'target-1');
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/relation/unfollow',
        params: { userId: 'user-1', targetUserId: 'target-1' },
      });
    });
  });

  describe('setSpecialFollow / cancelSpecialFollow', () => {
    it('POSTs to /content/user/relation/special-follow', () => {
      setSpecialFollow('user-1', 'target-1');
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/relation/special-follow',
        params: { userId: 'user-1', targetUserId: 'target-1' },
      });
    });

    it('POSTs to /content/user/relation/special-follow/cancel', () => {
      cancelSpecialFollow('user-1', 'target-1');
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/relation/special-follow/cancel',
        params: { userId: 'user-1', targetUserId: 'target-1' },
      });
    });
  });

  describe('group APIs', () => {
    it('getFollowGroupList GETs /groups', () => {
      getFollowGroupList('user-1');
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/content/user/relation/groups',
        params: { userId: 'user-1' },
      });
    });

    it('createFollowGroup POSTs to /group/create', () => {
      createFollowGroup('user-1', { name: 'Friends', sortOrder: 1 });
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/relation/group/create',
        params: { userId: 'user-1' },
        data: { name: 'Friends', sortOrder: 1 },
      });
    });

    it('renameFollowGroup POSTs to /group/rename', () => {
      renameFollowGroup('user-1', { groupId: 'g-1', name: 'New Name' });
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/relation/group/rename',
        params: { userId: 'user-1' },
        data: { groupId: 'g-1', name: 'New Name' },
      });
    });

    it('deleteFollowGroup POSTs to /group/delete', () => {
      deleteFollowGroup('user-1', 'g-1');
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/relation/group/delete',
        params: { userId: 'user-1', groupId: 'g-1' },
      });
    });

    it('moveFollowGroup POSTs to /group/move', () => {
      moveFollowGroup('user-1', { targetUserId: 't-1', groupId: 'g-1' });
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/relation/group/move',
        params: { userId: 'user-1' },
        data: { targetUserId: 't-1', groupId: 'g-1' },
      });
    });

    it('removeFromGroup POSTs to /group/remove', () => {
      removeFromGroup('user-1', { targetUserId: 't-1', groupId: 'g-1' });
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/relation/group/remove',
        params: { userId: 'user-1' },
        data: { targetUserId: 't-1', groupId: 'g-1' },
      });
    });
  });

  describe('list APIs', () => {
    it('getFollowList GETs /follow-list with filters', () => {
      getFollowList('user-1', { keyword: 'test', groupId: 'g-1', pageNo: 2, pageSize: 20 });
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/content/user/relation/follow-list',
        params: { userId: 'user-1', keyword: 'test', groupId: 'g-1', pageNo: 2, pageSize: 20 },
      });
    });

    it('getSpecialFollowList GETs /special-follow-list', () => {
      getSpecialFollowList('user-1', { pageNo: 1, pageSize: 10 });
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/content/user/relation/special-follow-list',
        params: { userId: 'user-1', pageNo: 1, pageSize: 10 },
      });
    });

    it('getRecommendations GETs /recommendations', () => {
      getRecommendations({ page: 1, size: 10 });
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/content/user/relation/recommendations',
        params: { page: 1, size: 10 },
      });
    });
  });

  describe('batch APIs', () => {
    it('batchUnfollow POSTs to /batch/unfollow', () => {
      batchUnfollow('user-1', { targetUserIds: ['t-1', 't-2'] });
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/relation/batch/unfollow',
        params: { userId: 'user-1' },
        data: { targetUserIds: ['t-1', 't-2'] },
      });
    });

    it('batchCancelSpecial POSTs to /batch/special-follow/cancel', () => {
      batchCancelSpecial('user-1', { targetUserIds: ['t-1'] });
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/content/user/relation/batch/special-follow/cancel',
        params: { userId: 'user-1' },
        data: { targetUserIds: ['t-1'] },
      });
    });
  });

  describe('getFollowingFeed', () => {
    it('GETs /content/user/relation/feed with params', () => {
      getFollowingFeed({ page: 1, size: 20, types: 'post,like' });
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/content/user/relation/feed',
        params: { page: 1, size: 20, types: 'post,like' },
      });
    });
  });
});
