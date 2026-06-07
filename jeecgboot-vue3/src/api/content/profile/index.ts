import { defHttp } from '/@/utils/http/axios';
import type {
  ContentUserProfileVO,
  ContentUserProfileUpdateReq,
  ContentUserHomepageUpdateReq,
  ContentUserHomepageModuleVO,
  ContentUserPrivacyUpdateReq,
  ContentUserVerificationBadgeVO,
  ContentUserProfileHistoryVO,
  HistoryType,
} from './types';

enum Api {
  detail = '/content/user/profile/detail',
  update = '/content/user/profile/update',
  reviewHandle = '/content/user/profile/review/handle',
  privacyUpdate = '/content/user/profile/privacy/update',
  homepageUpdate = '/content/user/profile/homepage/update',
  homepageDefaultsRestore = '/content/user/profile/homepage/defaults/restore',
  homepageModules = '/content/user/profile/homepage/modules',
  badgeList = '/content/user/profile/badge/list',
  badgeDetail = '/content/user/profile/badge/detail',
  historyList = '/content/user/profile/history/list',
  historyRestore = '/content/user/profile/history/restore',
}

/** 查询资料详情（owner + viewer 视角裁剪） */
export const getProfileDetail = (ownerUserId: string, viewerUserId: string) =>
  defHttp.get<ContentUserProfileVO>({
    url: Api.detail,
    params: { ownerUserId, viewerUserId },
  });

/** 统一更新资料（基础资料 + 主页配置合并提交） */
export const updateProfile = (userId: string, data: ContentUserProfileUpdateReq) =>
  defHttp.post<ContentUserProfileVO>({ url: Api.update, params: { userId }, data });

/** 更新主页配置 */
export const updateHomepage = (userId: string, data: ContentUserHomepageUpdateReq) =>
  defHttp.post<ContentUserProfileVO>({ url: Api.homepageUpdate, params: { userId }, data });

/** 恢复主页默认 */
export const restoreHomepageDefaults = (userId: string) =>
  defHttp.post<ContentUserProfileVO>({
    url: Api.homepageDefaultsRestore,
    params: { userId },
  });

/** 查询主页模块列表 */
export const getHomepageModules = (userId: string) =>
  defHttp.get<ContentUserHomepageModuleVO[]>({
    url: Api.homepageModules,
    params: { userId },
  });

/** 更新隐私设置（覆盖 14 个 visibility + 2 个特殊字段 + 2 个 boolean） */
export const updatePrivacy = (userId: string, data: ContentUserPrivacyUpdateReq) =>
  defHttp.post<string>({
    url: Api.privacyUpdate,
    params: { userId },
    data,
  });

/** 查询认证标识列表 */
export const getBadgeList = (userId: string) =>
  defHttp.get<ContentUserVerificationBadgeVO[]>({
    url: Api.badgeList,
    params: { userId },
  });

/** 查询认证标识详情 */
export const getBadgeDetail = (badgeId: string) =>
  defHttp.get<ContentUserVerificationBadgeVO>({
    url: Api.badgeDetail,
    params: { badgeId },
  });

/** 查询历史记录（按 historyType 区分 NICKNAME / AVATAR） */
export const getHistoryList = (userId: string, historyType: HistoryType) =>
  defHttp.get<ContentUserProfileHistoryVO[]>({
    url: Api.historyList,
    params: { userId, historyType },
  });

/** 恢复历史值（复用资料更新流程） */
export const restoreHistory = (userId: string, historyId: string) =>
  defHttp.post<ContentUserProfileVO>({
    url: Api.historyRestore,
    params: { userId, historyId },
  });
