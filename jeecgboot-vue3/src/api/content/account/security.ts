import { defHttp } from '/@/utils/http/axios';

export interface AccountSecurityStatus {
  phoneBound: boolean;
  phone?: string;
  emailBound: boolean;
  email?: string;
  wechatBound: boolean;
  appleBound: boolean;
  googleBound: boolean;
  emailVerified?: boolean;
  loginMethod?: string;
}

export interface DeviceInfo {
  id: string;
  name: string;
  type: 'pc' | 'mobile' | 'tablet';
  os?: string;
  browser?: string;
  lastLoginAt: string;
  ip: string;
  location: string;
  trusted: boolean;
  current: boolean;
  evicted?: boolean;
}

export interface AnomalyNotification {
  id: string;
  loginAt: string;
  device: string;
  ip: string;
  location: string;
  handled: boolean;
  handledAt?: string;
  handleResult?: 'confirmed' | 'denied';
}

enum Api {
  status = '/api/v1/content/account-security/status',
  bindPhone = '/api/v1/content/auth/bind/phone',
  rebindPhone = '/api/v1/content/auth/rebind/phone',
  unbindPhone = '/api/v1/content/auth/unbind/phone',
  bindEmail = '/api/v1/content/auth/bind/email',
  rebindEmail = '/api/v1/content/auth/rebind/email',
  unbindEmail = '/api/v1/content/auth/unbind/email',
  bindThirdParty = '/api/v1/content/auth/bind/third-party',
  unbindThirdParty = '/api/v1/content/auth/unbind/third-party',
  devices = '/api/v1/content/auth/devices',
  deviceRevoke = '/api/v1/content/auth/devices/revoke',
  deviceTrust = '/api/v1/content/account-security/devices/trust',
  deviceUntrust = '/api/v1/content/account-security/devices/untrust',
  resetPassword = '/api/v1/content/auth/password/reset',
  changePassword = '/api/v1/content/account-security/password/change',
  anomalyList = '/api/v1/content/account-security/anomaly/list',
  anomalyConfirm = '/api/v1/content/account-security/anomaly/confirm',
  anomalyDeny = '/api/v1/content/account-security/anomaly/deny',
  sendSecurityCode = '/api/v1/content/account-security/send-code',
}

export const getAccountSecurityStatus = () => defHttp.get<AccountSecurityStatus>({ url: Api.status });

export const bindPhone = (params: { phone: string; countryCode?: string; smsCode: string }) =>
  defHttp.post({ url: Api.bindPhone, params });

export const rebindPhone = (params: { oldPhone: string; newPhone: string; oldSmsCode: string; newSmsCode: string; countryCode?: string }) =>
  defHttp.post({ url: Api.rebindPhone, params });

export const unbindPhone = (params: { smsCode: string }) => defHttp.post({ url: Api.unbindPhone, params });

export const bindEmail = (params: { email: string; emailCode: string }) => defHttp.post({ url: Api.bindEmail, params });

export const rebindEmail = (params: { oldEmail: string; newEmail: string; oldEmailCode: string; newEmailCode: string }) =>
  defHttp.post({ url: Api.rebindEmail, params });

export const unbindEmail = (params: { emailCode: string }) => defHttp.post({ url: Api.unbindEmail, params });

export const bindThirdParty = (params: { channel: string; code: string; state?: string }) =>
  defHttp.post({ url: Api.bindThirdParty, params });

export const unbindThirdParty = (params: { channel: string; smsCode: string }) =>
  defHttp.post({ url: Api.unbindThirdParty, params });

export const listDevices = () => defHttp.get<DeviceInfo[]>({ url: Api.devices });

export const revokeDevice = (deviceId: string) => defHttp.post({ url: Api.deviceRevoke, params: { deviceId } });

export const trustDevice = (deviceId: string) => defHttp.post({ url: Api.deviceTrust, params: { deviceId } });

export const untrustDevice = (deviceId: string) => defHttp.post({ url: Api.deviceUntrust, params: { deviceId } });

export const resetPassword = (params: { account: string; newPassword: string; smsOrEmailCode: string; type: 'sms' | 'email' }) =>
  defHttp.post({ url: Api.resetPassword, params });

export const changePassword = (params: { oldPassword: string; newPassword: string }) =>
  defHttp.post({ url: Api.changePassword, params });

export const listAnomalyNotifications = (params?: { pageNo?: number; pageSize?: number }) =>
  defHttp.get<{ records: AnomalyNotification[]; total: number }>({ url: Api.anomalyList, params });

export const confirmAnomaly = (id: string) => defHttp.post({ url: Api.anomalyConfirm, params: { id } });

export const denyAnomaly = (params: { id: string; revokeDeviceId?: string }) =>
  defHttp.post({ url: Api.anomalyDeny, params });

export const sendSecurityCode = (params: { type: 'sms' | 'email'; target: string; purpose: string }) =>
  defHttp.post({ url: Api.sendSecurityCode, params });
