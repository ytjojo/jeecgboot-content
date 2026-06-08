import { describe, it, expect, beforeEach, vi } from 'vitest';

// Mock defHttp
vi.mock('/@/utils/http/axios', () => ({
  defHttp: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

import { defHttp } from '/@/utils/http/axios';
import {
  getDiscoveryHome,
  getRecommendationList,
  getColdStartRecommendations,
  markNotInterested,
  getCategoryTree,
  createCategory,
  updateCategory,
  disableCategory,
  enableCategory,
  deleteCategory,
  sortCategories,
  getBrowseChannelList,
  searchChannels,
  submitSearchFeedback,
  getHotRanking,
  getNewRanking,
  getSystemRanking,
  getEditorialPickList,
  createEditorialPick,
  updateEditorialPick,
  removeEditorialPick,
  getTagList,
  createTag,
  updateTag,
  deleteTag,
} from '/@/api/content/channelDiscovery';

describe('Channel Discovery API', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Discovery Home', () => {
    it('getDiscoveryHome should call GET with correct URL', async () => {
      await getDiscoveryHome();
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/discovery/home',
      });
    });
  });

  describe('Recommendation', () => {
    it('getRecommendationList should call GET with params', async () => {
      await getRecommendationList({ page: 1, pageSize: 20, userId: 'user1' });
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/recommendation/list',
        params: { page: 1, pageSize: 20, userId: 'user1' },
      });
    });

    it('getColdStartRecommendations should call GET', async () => {
      await getColdStartRecommendations();
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/recommendation/cold-start',
      });
    });

    it('markNotInterested should call POST with data', async () => {
      await markNotInterested({ channelId: 'ch1', reason: '不感兴趣' });
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/recommendation/not-interested',
        data: { channelId: 'ch1', reason: '不感兴趣' },
      });
    });
  });

  describe('Category Management', () => {
    it('getCategoryTree should call GET', async () => {
      await getCategoryTree();
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/category/tree',
      });
    });

    it('createCategory should call POST with data', async () => {
      await createCategory({ name: '新分类', sortOrder: 1 });
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/category/create',
        data: { name: '新分类', sortOrder: 1 },
      });
    });

    it('updateCategory should call POST with data including id', async () => {
      await updateCategory({ id: 'cat1', name: '改名', sortOrder: 2 });
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/category/update',
        data: { id: 'cat1', name: '改名', sortOrder: 2 },
      });
    });

    it('disableCategory should call POST', async () => {
      await disableCategory('cat1');
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/category/disable',
        data: { categoryId: 'cat1' },
      });
    });

    it('enableCategory should call POST', async () => {
      await enableCategory('cat1');
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/category/enable',
        data: { categoryId: 'cat1' },
      });
    });

    it('deleteCategory should call POST', async () => {
      await deleteCategory('cat1');
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/category/delete',
        data: { categoryId: 'cat1' },
      });
    });

    it('sortCategories should call POST with array', async () => {
      await sortCategories([{ id: 'cat1', sortOrder: 1 }, { id: 'cat2', sortOrder: 2 }]);
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/category/sort',
        data: [{ id: 'cat1', sortOrder: 1 }, { id: 'cat2', sortOrder: 2 }],
      });
    });
  });

  describe('Browse', () => {
    it('getBrowseChannelList should call GET with params', async () => {
      await getBrowseChannelList({ categoryId: 'cat1', sortBy: 'active', page: 1, pageSize: 20 });
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/browse/list',
        params: { categoryId: 'cat1', sortBy: 'active', page: 1, pageSize: 20 },
      });
    });
  });

  describe('Search', () => {
    it('searchChannels should call GET with search params', async () => {
      await searchChannels({ keyword: 'test', sortBy: 'relevance', page: 1, pageSize: 20 });
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/search/query',
        params: { keyword: 'test', sortBy: 'relevance', page: 1, pageSize: 20 },
      });
    });

    it('submitSearchFeedback should call POST', async () => {
      await submitSearchFeedback({ keyword: 'test', helpful: true });
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/search/feedback',
        data: { keyword: 'test', helpful: true },
      });
    });
  });

  describe('Ranking', () => {
    it('getHotRanking should call GET with params', async () => {
      await getHotRanking({ dimension: 'day', page: 1, pageSize: 20 });
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/ranking/hot',
        params: { dimension: 'day', page: 1, pageSize: 20 },
      });
    });

    it('getNewRanking should call GET with params', async () => {
      await getNewRanking({ page: 1, pageSize: 20 });
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/ranking/new',
        params: { page: 1, pageSize: 20 },
      });
    });

    it('getSystemRanking should call GET with params', async () => {
      await getSystemRanking({ page: 1, pageSize: 20 });
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/ranking/system',
        params: { page: 1, pageSize: 20 },
      });
    });
  });

  describe('Editorial Pick', () => {
    it('getEditorialPickList should call GET', async () => {
      await getEditorialPickList();
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/editorial-pick/list',
      });
    });

    it('createEditorialPick should call POST with data', async () => {
      await createEditorialPick({
        channelId: 'ch1',
        recommendation: '推荐',
        startTime: '2024-01-01',
        endTime: '2024-12-31',
      });
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/editorial-pick/create',
        data: { channelId: 'ch1', recommendation: '推荐', startTime: '2024-01-01', endTime: '2024-12-31' },
      });
    });

    it('removeEditorialPick should call POST', async () => {
      await removeEditorialPick('pick1');
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/editorial-pick/remove',
        data: { id: 'pick1' },
      });
    });
  });

  describe('Tag Management', () => {
    it('getTagList should call GET with channelId param', async () => {
      await getTagList('ch1');
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/tag/list',
        params: { channelId: 'ch1' },
      });
    });

    it('createTag should call POST with data', async () => {
      await createTag({ name: '新标签', channelId: 'ch1' });
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/tag/create',
        data: { name: '新标签', channelId: 'ch1' },
      });
    });

    it('updateTag should call POST with data', async () => {
      await updateTag({ tagId: 'tag1', name: '改名' });
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/tag/update',
        data: { tagId: 'tag1', name: '改名' },
      });
    });

    it('deleteTag should call POST', async () => {
      await deleteTag('tag1');
      expect(defHttp.post).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/tag/delete',
        data: { tagId: 'tag1' },
      });
    });
  });

  describe('Pagination format', () => {
    it('should use page/pageSize as parameter names', async () => {
      await getBrowseChannelList({ page: 1, pageSize: 20 });
      expect(defHttp.get).toHaveBeenCalledWith(
        expect.objectContaining({
          params: expect.objectContaining({ page: 1, pageSize: 20 }),
        }),
      );
    });

    it('should use default pagination when not specified', async () => {
      await getRecommendationList({});
      expect(defHttp.get).toHaveBeenCalledWith({
        url: '/api/v1/content/channel/recommendation/list',
        params: {},
      });
    });
  });
});
