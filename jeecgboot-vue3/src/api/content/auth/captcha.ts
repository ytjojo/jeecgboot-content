import { defHttp } from '/@/utils/http/axios';

export interface CaptchaImage {
  captchaId: string;
  imageBase64: string;
}

export interface RiskCheckResult {
  passed: boolean;
  captchaId?: string;
}

enum Api {
  image = '/api/v1/content/auth/captcha/image',
  verify = '/api/v1/content/auth/captcha/verify',
  lockStatus = '/api/v1/content/auth/captcha/lock-status',
}

export const getCaptchaImage = () => defHttp.post<CaptchaImage>({ url: Api.image });

export const verifyCaptcha = (params: { captchaId: string; captchaCode: string }) =>
  defHttp.post<RiskCheckResult>({ url: Api.verify, params });

export const getLockStatus = (account: string) => defHttp.get<{ locked: boolean; remainingSeconds?: number }>({ url: Api.lockStatus, params: { account } });
