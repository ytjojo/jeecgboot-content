import { defHttp } from '/@/utils/http/axios';

export interface CancellationEligibility {
  eligible: boolean;
  checks: Array<{ name: string; passed: boolean; action?: string; reason?: string }>;
  outstandingPoints?: number;
}

export interface CancellationApplication {
  id: string;
  status: 'pending' | 'cooling_off' | 'cancelled' | 'completed';
  appliedAt: string;
  coolingOffExpiresAt?: string;
  remainingDays?: number;
}

enum Api {
  checkEligibility = '/api/v1/account-cancellation/eligibility',
  apply = '/api/v1/account-cancellation/apply',
  status = '/api/v1/account-cancellation/status',
  cancel = '/api/v1/account-cancellation/cancel',
}

export const checkCancellationEligibility = () =>
  defHttp.get<CancellationEligibility>({ url: Api.checkEligibility });

export const applyCancellation = (params: { smsCode: string; reason?: string; abandonPoints?: boolean }) =>
  defHttp.post<CancellationApplication>({ url: Api.apply, params });

export const getCancellationStatus = () => defHttp.get<CancellationApplication | null>({ url: Api.status });

export const cancelCancellation = (id: string) => defHttp.post({ url: Api.cancel, params: { id } });
