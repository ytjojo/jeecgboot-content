import { defHttp } from '/@/utils/http/axios';
import type {
  NotificationSettingVO,
  NotificationUpdateReq,
  DndRuleUpdateReq,
  PrivacySettingVO,
  PrivacyUpdateReq,
  SecuritySettingVO,
  SecurityUpdateReq,
  ThirdPartyAuthVO,
  ThirdPartyAuthDetailVO,
} from './settings-types';

enum Api {
  notification = '/api/v1/content/user/settings/notification',
  notificationUpdate = '/api/v1/content/user/settings/notification/update',
  dndUpdate = '/api/v1/content/user/settings/notification/dnd/update',
  privacy = '/api/v1/content/user/settings/privacy',
  privacyUpdate = '/api/v1/content/user/settings/privacy/update',
  security = '/api/v1/content/user/settings/security',
  securityUpdate = '/api/v1/content/user/settings/security/update',
  thirdPartyList = '/api/v1/content/user/auth/third-party',
  thirdPartyDetail = '/api/v1/content/user/auth/third-party/',
  thirdPartyRevoke = '/api/v1/content/user/auth/third-party/',
}

/** 获取通知设置 */
export const getNotificationSetting = (userId: string) =>
  defHttp.get<NotificationSettingVO>({ url: Api.notification, params: { userId } });

/** 更新通知设置 */
export const updateNotificationSetting = (userId: string, data: NotificationUpdateReq) =>
  defHttp.post<NotificationSettingVO>({ url: Api.notificationUpdate, params: { userId }, data });

/** 更新免打扰规则 */
export const updateDndRule = (userId: string, data: DndRuleUpdateReq) =>
  defHttp.post({ url: Api.dndUpdate, params: { userId }, data });

/** 获取隐私设置 */
export const getPrivacySetting = (userId: string) =>
  defHttp.get<PrivacySettingVO>({ url: Api.privacy, params: { userId } });

/** 更新隐私设置 */
export const updatePrivacySetting = (userId: string, data: PrivacyUpdateReq) =>
  defHttp.post<string>({ url: Api.privacyUpdate, params: { userId }, data });

/** 获取安全设置 */
export const getSecuritySetting = (userId: string) =>
  defHttp.get<SecuritySettingVO>({ url: Api.security, params: { userId } });

/** 更新安全设置 */
export const updateSecuritySetting = (userId: string, data: SecurityUpdateReq) =>
  defHttp.post<SecuritySettingVO>({ url: Api.securityUpdate, params: { userId }, data });

/** 更新登录提醒 */
export const updateLoginAlert = (data: { userId: string; enabled: boolean }) =>
  updateSecuritySetting(data.userId, { loginAlertEnabled: data.enabled });

/** 获取第三方授权列表 */
export const listThirdPartyAuths = (userId: string) =>
  defHttp.get<ThirdPartyAuthVO[]>({ url: Api.thirdPartyList, params: { userId } });

/** 获取第三方授权详情 */
export const getThirdPartyAuthDetail = (userId: string, authId: string) =>
  defHttp.get<ThirdPartyAuthDetailVO>({ url: Api.thirdPartyDetail + authId, params: { userId } });

/** 撤销第三方授权 */
export const revokeThirdPartyAuth = (userId: string, authId: string) =>
  defHttp.delete<string>({ url: Api.thirdPartyRevoke + authId, params: { userId } });
