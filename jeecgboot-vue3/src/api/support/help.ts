import { defHttp } from '/@/utils/http/axios';

enum Api {
  search = '/content/user/support/help/search',
  categories = '/content/user/support/help/categories',
  article = '/content/user/support/help/article/{id}',
  feedback = '/content/user/support/help/article/{id}/feedback',
}

export interface HelpSearchParams {
  keyword: string;
  pageNo?: number;
  pageSize?: number;
}

export interface HelpCategory {
  id: string;
  name: string;
  icon: string;
  articleCount: number;
}

export interface HelpArticle {
  id: string;
  title: string;
  content: string; // Markdown 内容
  categoryId: string;
  categoryName: string;
  viewCount: number;
  helpfulCount: number;
  unhelpfulCount: number;
  createTime: string;
  updateTime: string;
}

export interface HelpSearchResult {
  id: string;
  title: string;
  summary: string;
  categoryName: string;
}

/** 搜索帮助文章 */
export const searchHelpArticles = (params: HelpSearchParams) =>
  defHttp.get({ url: Api.search, params });

/** 获取帮助分类 */
export const getHelpCategories = () =>
  defHttp.get({ url: Api.categories });

/** 获取文章详情 */
export const getHelpArticleDetail = (id: string) =>
  defHttp.get({ url: Api.article.replace('{id}', id) });

/** 提交文章反馈 */
export const submitArticleFeedback = (id: string, data: { helpful: boolean }) =>
  defHttp.post({ url: Api.feedback.replace('{id}', id), data });
