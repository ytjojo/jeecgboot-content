import { defHttp } from '/@/utils/http/axios';

export interface SmsCodeParams {
  phone: string;
  countryCode?: string;
  captchaId?: string;
  captchaCode?: string;
}

export interface EmailCodeParams {
  email: string;
  captchaId?: string;
  captchaCode?: string;
}

export interface RegisterMobileParams {
  phone: string;
  countryCode?: string;
  smsCode: string;
  captchaId?: string;
  captchaCode?: string;
  agreement: boolean;
}

export interface RegisterEmailParams {
  email: string;
  password: string;
  confirmPassword: string;
  agreement: boolean;
}

export interface LoginPasswordParams {
  account: string;
  password: string;
  captchaId?: string;
  captchaCode?: string;
  remember?: boolean;
}

export interface LoginSmsParams {
  phone: string;
  countryCode?: string;
  smsCode: string;
  captchaId?: string;
  captchaCode?: string;
}

export interface ThirdPartyLoginParams {
  channel: 'wechat' | 'apple' | 'google';
  code: string;
  state?: string;
  redirectUri?: string;
}

export interface LoginResult {
  token: string;
  refreshToken: string;
  userInfo: any;
  isFirstLogin?: boolean;
}

enum Api {
  registerMobile = '/api/v1/auth/register/mobile',
  registerEmail = '/api/v1/auth/register/email',
  confirmEmail = '/api/v1/auth/email/confirm',
  resendEmail = '/api/v1/auth/email/resend',
  thirdParty = '/api/v1/auth/login/third-party',
  smsCode = '/api/v1/auth/sms-code',
  emailCode = '/api/v1/auth/email-code',
  loginPassword = '/api/v1/auth/login/password',
  loginSmsCode = '/api/v1/auth/login/sms-code',
  refreshToken = '/api/v1/auth/token/refresh',
  logout = '/api/v1/auth/logout',
  sendSms = '/api/v1/auth/sms/send',
  sendEmail = '/api/v1/auth/email/send',
}

export const registerMobile = (params: RegisterMobileParams) =>
  defHttp.post<LoginResult>({ url: Api.registerMobile, params });

export const registerEmail = (params: RegisterEmailParams) =>
  defHttp.post<{ message: string }>({ url: Api.registerEmail, params });

export const confirmEmail = (token: string) =>
  defHttp.post({ url: Api.confirmEmail, params: { token } });

export const resendConfirmEmail = (email: string) =>
  defHttp.post({ url: Api.resendEmail, params: { email } });

export const thirdPartyLogin = (params: ThirdPartyLoginParams) =>
  defHttp.post<LoginResult>({ url: Api.thirdParty, params });

export const loginByPassword = (params: LoginPasswordParams) =>
  defHttp.post<LoginResult>({ url: Api.loginPassword, params });

export const loginBySmsCode = (params: LoginSmsParams) =>
  defHttp.post<LoginResult>({ url: Api.loginSmsCode, params });

export const sendSmsCode = (params: SmsCodeParams) =>
  defHttp.post({ url: Api.sendSms, params });

export const sendEmailCode = (params: EmailCodeParams) =>
  defHttp.post({ url: Api.sendEmail, params });

export const refreshToken = (refreshToken: string) =>
  defHttp.post<{ token: string; refreshToken: string }>({ url: Api.refreshToken, params: { refreshToken } });

export const logout = () => defHttp.post({ url: Api.logout });
