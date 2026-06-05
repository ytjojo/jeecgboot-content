import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/content/user/support/changelog/list',
}

export interface ChangelogQueryParams {
  keyword?: string;
  pageNo?: number;
  pageSize?: number;
}

// 注意：后端 ContentChangelogVO 字段名为 additions/improvements/fixes，无 id 字段
// 前端使用 features/bugfixes 作为别名，需在 API 层做字段映射
export interface ChangelogVersion {
  id?: string; // 后端无此字段，前端可从 version 生成
  version: string;
  releaseDate: string;
  additions: string[];
  improvements: string[];
  fixes: string[];
}

/** 获取更新日志列表 */
export const getChangelogList = (params: ChangelogQueryParams) =>
  defHttp.get({ url: Api.list, params });
