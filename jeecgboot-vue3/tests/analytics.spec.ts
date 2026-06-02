// Locked contract: stable event names referenced by spec sections 14.1 / 14.2.
// Renaming any of these would silently break analytics dashboards.

const ANALYTICS_EVENTS = {
  loginPageView: 'login_page_view',
  loginSubmit: 'login_submit',
  loginSuccess: 'login_success',
  loginFail: 'login_fail',
  loginLockout: 'login_lockout',
  loginThirdPartyClick: 'login_third_party_click',
  loginThirdPartyAuth: 'login_third_party_auth',
  loginThirdPartyCancel: 'login_third_party_cancel',
  registerFormStart: 'register_form_start',
  registerSubmit: 'register_submit',
  registerSuccess: 'register_success',
  registerFail: 'register_fail',
  agreementConfirm: 'agreement_confirm',
  passwordResetStart: 'password_reset_start',
  passwordResetSuccess: 'password_reset_success',
  passwordResetFail: 'password_reset_fail',
  captchaLoad: 'captcha_load',
  captchaSuccess: 'captcha_success',
  captchaFail: 'captcha_fail',
  captchaLock: 'captcha_lock',
  smsSend: 'sms_send',
  smsResend: 'sms_resend',
  sessionExpire: 'session_expire',
  sessionRefresh: 'session_refresh',
  sessionLogout: 'session_logout',
  anomalyDetect: 'anomaly_detect',
  anomalyConfirm: 'anomaly_confirm',
  anomalyDeny: 'anomaly_deny',
};

describe('ANALYTICS_EVENTS contract', () => {
  it('contains all 14.2 login events', () => {
    expect(ANALYTICS_EVENTS.loginPageView).toBe('login_page_view');
    expect(ANALYTICS_EVENTS.loginSubmit).toBe('login_submit');
    expect(ANALYTICS_EVENTS.loginSuccess).toBe('login_success');
    expect(ANALYTICS_EVENTS.loginFail).toBe('login_fail');
    expect(ANALYTICS_EVENTS.loginLockout).toBe('login_lockout');
    expect(ANALYTICS_EVENTS.loginThirdPartyClick).toBe('login_third_party_click');
    expect(ANALYTICS_EVENTS.loginThirdPartyAuth).toBe('login_third_party_auth');
    expect(ANALYTICS_EVENTS.loginThirdPartyCancel).toBe('login_third_party_cancel');
  });

  it('contains all 14.1 register events', () => {
    expect(ANALYTICS_EVENTS.registerFormStart).toBe('register_form_start');
    expect(ANALYTICS_EVENTS.registerSubmit).toBe('register_submit');
    expect(ANALYTICS_EVENTS.registerSuccess).toBe('register_success');
    expect(ANALYTICS_EVENTS.registerFail).toBe('register_fail');
    expect(ANALYTICS_EVENTS.agreementConfirm).toBe('agreement_confirm');
  });

  it('contains all session lifecycle events', () => {
    expect(ANALYTICS_EVENTS.sessionExpire).toBe('session_expire');
    expect(ANALYTICS_EVENTS.sessionRefresh).toBe('session_refresh');
    expect(ANALYTICS_EVENTS.sessionLogout).toBe('session_logout');
  });

  it('contains all anomaly events', () => {
    expect(ANALYTICS_EVENTS.anomalyDetect).toBe('anomaly_detect');
    expect(ANALYTICS_EVENTS.anomalyConfirm).toBe('anomaly_confirm');
    expect(ANALYTICS_EVENTS.anomalyDeny).toBe('anomaly_deny');
  });

  it('uses snake_case naming convention for every event', () => {
    Object.values(ANALYTICS_EVENTS).forEach((v) => {
      expect(v).toMatch(/^[a-z_]+$/);
    });
  });

  it('has no duplicate event names', () => {
    const vals = Object.values(ANALYTICS_EVENTS);
    expect(new Set(vals).size).toBe(vals.length);
  });
});
