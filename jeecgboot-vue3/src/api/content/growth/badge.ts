import { defHttp } from '/@/utils/http/axios';
import type {
  BadgeCatalogVO,
  BadgeDetailVO,
  BadgeWearReq,
  BadgeRecycleReq,
  AdminBadgeVO,
  AdminBadgeQuery,
} from './badge-types';

enum Api {
  catalog = '/api/v1/content/user/growth/badge/catalog',
  detail = '/api/v1/content/user/growth/badge/detail',
  worn = '/api/v1/content/user/growth/badge/worn',
  wear = '/api/v1/content/user/growth/badge/wear',
  recycle = '/api/v1/content/user/growth/badge/recycle',
  adminList = '/api/v1/content/user/growth/badge/admin/list',
}

/** 查询勋章分类目录 */
export const getBadgeCatalog = () =>
  defHttp.get<BadgeCatalogVO[]>({ url: Api.catalog });

/** 查询勋章详情 */
export const getBadgeDetail = (badgeId: string) =>
  defHttp.get<BadgeDetailVO>({ url: Api.detail, params: { badgeId } });

/** 查询佩戴中的勋章列表 */
export const getWornBadges = (userId: string) =>
  defHttp.get<BadgeDetailVO[]>({ url: Api.worn, params: { userId } });

/** 保存佩戴勋章配置 */
export const saveWornBadges = (data: BadgeWearReq) =>
  defHttp.post<void>({ url: Api.wear, data });

/** 管理员回收勋章 */
export const recycleBadge = (data: BadgeRecycleReq) =>
  defHttp.post<void>({ url: Api.recycle, data });

/** 管理端查询用户勋章列表（分页） */
export const listAdminBadges = (params: AdminBadgeQuery) =>
  defHttp.get<{ records: AdminBadgeVO[]; total: number }>({ url: Api.adminList, params });
