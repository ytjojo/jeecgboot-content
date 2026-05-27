import { defHttp } from '/@/utils/http/axios';

enum Api {
  notification = '/content/user/settings/notification',
  notificationUpdate = '/content/user/settings/notification/update',
  dndUpdate = '/content/user/settings/notification/dnd/update',
  privacy = '/content/user/settings/privacy',
  privacyUpdate = '/content/user/settings/privacy/update',
}

/**
 * 获取通知设置
 */
export const getNotificationSetting = (userId: string) => defHttp.get({ url: Api.notification, params: { userId } });

/**
 * 更新通知设置
 */
export const updateNotificationSetting = (userId: string, data: any) =>
  defHttp.post({ url: Api.notificationUpdate, params: { userId }, data });

/**
 * 更新免打扰规则
 */
export const updateDndRule = (userId: string, data: any) =>
  defHttp.post({ url: Api.dndUpdate, params: { userId }, data });

/**
 * 获取隐私设置
 */
export const getPrivacySetting = (userId: string) => defHttp.get({ url: Api.privacy, params: { userId } });

/**
 * 更新隐私设置
 */
export const updatePrivacySetting = (userId: string, data: any) =>
  defHttp.post({ url: Api.privacyUpdate, params: { userId }, data });

/**
 * 获取用户安全设置信息
 * @param userId 用户ID
 */
export const getSecuritySetting = (userId: string) =>
  defHttp.get({ url: '/content/user/settings/security', params: { userId } });

/**
 * 更新登录提醒设置
 * @param params { userId, enabled }
 */
export const updateLoginAlert = (params: { userId: string; enabled: boolean }) =>
  defHttp.put({ url: '/content/user/settings/security/login-alert', params });

/**
 * 获取第三方授权列表
 * @param userId 用户ID
 */
export const listThirdPartyAuths = (userId: string) =>
  defHttp.get({ url: '/content/user/auth/third-party', params: { userId } });

/**
 * 撤销第三方授权
 * @param userId 用户ID
 * @param authId 授权记录ID
 */
export const revokeThirdPartyAuth = (userId: string, authId: string) =>
  defHttp.delete({ url: `/content/user/auth/third-party/${authId}`, params: { userId } });
