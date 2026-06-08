import { defHttp } from '/@/utils/http/axios';
import type {
  DiscoveryHomeVO,
  ChannelRecommendationVO,
  ChannelSearchResultVO,
  ChannelRankingItemVO,
  ChannelEditorialPickVO,
  CategoryTreeVO,
  ChannelTagVO,
  PageResult,
  ChannelInfo,
  ChannelSearchParams,
  ChannelBrowseParams,
  ChannelRankingParams,
  CategoryFormData,
  EditorialPickFormData,
  TagFormData,
  NotInterestedReq,
  SearchFeedbackReq,
} from './model/channelDiscoveryModel';

enum Api {
  // 发现页聚合
  discoveryHome = '/api/v1/content/channel/discovery/home',
  // 推荐
  recommendationList = '/api/v1/content/channel/recommendation/list',
  recommendationColdStart = '/api/v1/content/channel/recommendation/cold-start',
  notInterested = '/api/v1/content/channel/recommendation/not-interested',
  // 分类
  categoryTree = '/api/v1/content/channel/category/tree',
  categoryCreate = '/api/v1/content/channel/category/create',
  categoryUpdate = '/api/v1/content/channel/category/update',
  categoryDisable = '/api/v1/content/channel/category/disable',
  categoryEnable = '/api/v1/content/channel/category/enable',
  categoryDelete = '/api/v1/content/channel/category/delete',
  categorySort = '/api/v1/content/channel/category/sort',
  // 分类浏览
  browseList = '/api/v1/content/channel/browse/list',
  // 搜索
  searchQuery = '/api/v1/content/channel/search/query',
  searchFeedback = '/api/v1/content/channel/search/feedback',
  // 排行榜
  rankingHot = '/api/v1/content/channel/ranking/hot',
  rankingNew = '/api/v1/content/channel/ranking/new',
  rankingSystem = '/api/v1/content/channel/ranking/system',
  // 编辑精选
  editorialPickList = '/api/v1/content/channel/editorial-pick/list',
  editorialPickCreate = '/api/v1/content/channel/editorial-pick/create',
  editorialPickUpdate = '/api/v1/content/channel/editorial-pick/update',
  editorialPickRemove = '/api/v1/content/channel/editorial-pick/remove',
  // 标签管理
  tagList = '/api/v1/content/channel/tag/list',
  tagCreate = '/api/v1/content/channel/tag/create',
  tagUpdate = '/api/v1/content/channel/tag/update',
  tagDelete = '/api/v1/content/channel/tag/delete',
}

// ==================== 发现页聚合 ====================

/** 获取发现页聚合数据 */
export const getDiscoveryHome = () =>
  defHttp.get<DiscoveryHomeVO>({ url: Api.discoveryHome });

// ==================== 推荐 ====================

/** 获取推荐频道列表 */
export const getRecommendationList = (params?: { userId?: string; page?: number; pageSize?: number }) =>
  defHttp.get<PageResult<ChannelRecommendationVO>>({ url: Api.recommendationList, params });

/** 获取冷启动推荐 */
export const getColdStartRecommendations = () =>
  defHttp.get<ChannelRecommendationVO[]>({ url: Api.recommendationColdStart });

/** 不感兴趣反馈 */
export const markNotInterested = (data: NotInterestedReq) =>
  defHttp.post<void>({ url: Api.notInterested, data });

// ==================== 分类管理 ====================

/** 获取分类树 */
export const getCategoryTree = () =>
  defHttp.get<CategoryTreeVO[]>({ url: Api.categoryTree });

/** 创建分类 */
export const createCategory = (data: CategoryFormData) =>
  defHttp.post<void>({ url: Api.categoryCreate, data });

/** 更新分类 */
export const updateCategory = (data: CategoryFormData & { id: string }) =>
  defHttp.post<void>({ url: Api.categoryUpdate, data });

/** 停用分类 */
export const disableCategory = (categoryId: string, params?: { action?: string; targetCategoryId?: string }) =>
  defHttp.post<void>({ url: Api.categoryDisable, data: { categoryId, ...params } });

/** 启用分类 */
export const enableCategory = (categoryId: string) =>
  defHttp.post<void>({ url: Api.categoryEnable, data: { categoryId } });

/** 删除分类 */
export const deleteCategory = (categoryId: string) =>
  defHttp.post<void>({ url: Api.categoryDelete, data: { categoryId } });

/** 分类排序 */
export const sortCategories = (data: { id: string; sortOrder: number }[]) =>
  defHttp.post<void>({ url: Api.categorySort, data });

// ==================== 分类浏览 ====================

/** 分类浏览频道列表 */
export const getBrowseChannelList = (params: ChannelBrowseParams) =>
  defHttp.get<PageResult<ChannelInfo>>({ url: Api.browseList, params });

// ==================== 搜索 ====================

/** 频道搜索 */
export const searchChannels = (params: ChannelSearchParams) =>
  defHttp.get<PageResult<ChannelSearchResultVO>>({ url: Api.searchQuery, params });

/** 搜索反馈 */
export const submitSearchFeedback = (data: SearchFeedbackReq) =>
  defHttp.post<void>({ url: Api.searchFeedback, data });

// ==================== 排行榜 ====================

/** 获取热门排行榜 */
export const getHotRanking = (params?: ChannelRankingParams) =>
  defHttp.get<PageResult<ChannelRankingItemVO>>({ url: Api.rankingHot, params });

/** 获取新晋排行榜 */
export const getNewRanking = (params?: ChannelRankingParams) =>
  defHttp.get<PageResult<ChannelRankingItemVO>>({ url: Api.rankingNew, params });

/** 获取系统排行榜 */
export const getSystemRanking = (params?: ChannelRankingParams) =>
  defHttp.get<PageResult<ChannelRankingItemVO>>({ url: Api.rankingSystem, params });

// ==================== 编辑精选 ====================

/** 获取编辑精选列表（用户端） */
export const getEditorialPickList = () =>
  defHttp.get<ChannelEditorialPickVO[]>({ url: Api.editorialPickList });

/** 添加编辑精选 */
export const createEditorialPick = (data: EditorialPickFormData) =>
  defHttp.post<void>({ url: Api.editorialPickCreate, data });

/** 更新编辑精选 */
export const updateEditorialPick = (data: EditorialPickFormData & { id: string }) =>
  defHttp.post<void>({ url: Api.editorialPickUpdate, data });

/** 取消编辑精选 */
export const removeEditorialPick = (id: string) =>
  defHttp.post<void>({ url: Api.editorialPickRemove, data: { id } });

// ==================== 标签管理 ====================

/** 获取频道标签列表 */
export const getTagList = (channelId: string) =>
  defHttp.get<ChannelTagVO[]>({ url: Api.tagList, params: { channelId } });

/** 创建标签 */
export const createTag = (data: TagFormData & { channelId: string }) =>
  defHttp.post<void>({ url: Api.tagCreate, data });

/** 更新标签 */
export const updateTag = (data: { tagId: string; name: string }) =>
  defHttp.post<void>({ url: Api.tagUpdate, data });

/** 删除标签 */
export const deleteTag = (tagId: string) =>
  defHttp.post<void>({ url: Api.tagDelete, data: { tagId } });
