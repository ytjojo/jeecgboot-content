export const ANALYTICS_EVENTS = {
  registerPageView: 'register_page_view',
  registerTabSwitch: 'register_tab_switch',
  registerFormStart: 'register_form_start',
  registerCaptchaClick: 'register_captcha_click',
  registerCaptchaSuccess: 'register_captcha_success',
  registerCaptchaFail: 'register_captcha_fail',
  registerSubmit: 'register_submit',
  registerSuccess: 'register_success',
  registerFail: 'register_fail',
  registerAgreementClick: 'register_agreement_click',
  loginPageView: 'login_page_view',
  loginSubmit: 'login_submit',
  loginSuccess: 'login_success',
  loginFail: 'login_fail',
  loginLockout: 'login_lockout',
  loginThirdPartyClick: 'login_third_party_click',
  loginThirdPartyAuth: 'login_third_party_auth',
  loginThirdPartyCancel: 'login_third_party_cancel',
  passwordResetStart: 'password_reset_start',
  passwordResetSuccess: 'password_reset_success',
  accountBind: 'account_bind',
  accountUnbind: 'account_unbind',
  accountRebind: 'account_rebind',
  deviceRevoke: 'device_revoke',
  anomalyConfirm: 'anomaly_confirm',
  anomalyDeny: 'anomaly_deny',
  accountCancelApply: 'account_cancel_apply',
  accountCancelRevoke: 'account_cancel_revoke',
} as const;

export type AnalyticsEventName = (typeof ANALYTICS_EVENTS)[keyof typeof ANALYTICS_EVENTS];

export function trackEvent(name: AnalyticsEventName, payload?: Record<string, unknown>) {
  if (typeof window === 'undefined') return;
  try {
    const w = window as any;
    if (typeof w.trackEvent === 'function') {
      w.trackEvent(name, payload);
    } else if (typeof w.dataLayer?.push === 'function') {
      w.dataLayer.push({ event: name, ...payload });
    } else {
      console.debug('[analytics]', name, payload);
    }
  } catch {
    // 静默失败
  }
}
