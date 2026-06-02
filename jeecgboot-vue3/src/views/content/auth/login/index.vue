<template>
  <div class="auth-page auth-page--login">
    <div class="auth-page__brand">
      <h1 class="auth-page__title">内容社区</h1>
      <p class="auth-page__subtitle">分享你的故事，发现更多精彩</p>
    </div>
    <div class="auth-page__content">
      <a-tabs v-model:active-key="tab" :destroy-inactive-tab-pane="true" centered class="auth-page__tabs">
        <a-tab-pane key="password" tab="密码登录">
          <a-form layout="vertical" :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" @finish="onPasswordSubmit">
            <a-form-item name="account">
              <a-input v-model:value="passwordForm.account" placeholder="手机号/邮箱" size="large" allow-clear />
            </a-form-item>
            <a-form-item name="password">
              <a-input-password v-model:value="passwordForm.password" placeholder="密码" size="large" />
            </a-form-item>
            <a-form-item>
              <div class="auth-page__row">
                <a-checkbox v-model:checked="passwordForm.remember">记住我</a-checkbox>
                <router-link to="/content/forgot-password" class="auth-page__link">忘记密码？</router-link>
              </div>
            </a-form-item>
            <a-form-item>
              <a-button type="primary" html-type="submit" size="large" block :loading="submitting" :disabled="locked">
                {{ locked ? `已锁定(${lockSeconds}s)` : '登录' }}
              </a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>
        <a-tab-pane key="sms" tab="验证码登录">
          <a-form layout="vertical" :model="smsForm" :rules="smsRules" ref="smsFormRef" @finish="onSmsSubmit">
            <a-form-item name="phone">
              <a-input v-model:value="smsForm.phone" placeholder="手机号" size="large" addon-before="+86" />
            </a-form-item>
            <a-form-item name="captchaCode">
              <a-input v-model:value="smsForm.captchaCode" placeholder="图形验证码" size="large" maxlength="6">
                <template #suffix>
                  <CaptchaImage @change="onCaptchaChange" />
                </template>
              </a-input>
            </a-form-item>
            <a-form-item name="smsCode">
              <a-input v-model:value="smsForm.smsCode" placeholder="短信验证码" size="large" maxlength="6">
                <template #suffix>
                  <SmsCodeButton :text="'获取验证码'" @click="sendSms" />
                </template>
              </a-input>
            </a-form-item>
            <a-form-item>
              <a-button type="primary" html-type="submit" size="large" block :loading="submitting">登录</a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>
      </a-tabs>
      <div class="auth-page__divider"><span>第三方登录</span></div>
      <div class="auth-page__third-party">
        <a-button v-for="ch in thirdPartyChannels" :key="ch.key" shape="circle" size="large" :title="ch.label" @click="thirdPartyLogin(ch.key)">
          <Icon :icon="ch.icon" />
        </a-button>
      </div>
      <div class="auth-page__footer">
        还没有账号？<router-link to="/content/register" class="auth-page__link">立即注册</router-link>
      </div>
    </div>
    <RiskControlModal
      :open="riskOpen"
      :mode="riskMode"
      :lock-seconds="lockSeconds"
      @success="onRiskSuccess"
      @cancel="riskOpen = false"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message, Modal } from 'ant-design-vue';
import CaptchaImage from '/@/components/Auth/CaptchaImage.vue';
import SmsCodeButton from '/@/components/Auth/SmsCodeButton.vue';
import RiskControlModal from '/@/components/Auth/RiskControlModal.vue';
import { loginByPassword, loginBySmsCode, sendSmsCode, thirdPartyLogin as tpLogin } from '/@/api/content/auth';
import { isSafeRedirect } from '/@/components/Auth/redirectGuard';
import { isMobileDevice } from '/@/components/Auth/device';
import { trackEvent, ANALYTICS_EVENTS } from '/@/components/Auth/analytics';
import { useUserStore } from '/@/store/modules/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const tab = ref<'password' | 'sms'>('password');
const submitting = ref(false);
const locked = ref(false);
const lockSeconds = ref(0);
const lockTimer = ref<ReturnType<typeof setInterval> | null>(null);

const passwordForm = reactive({ account: '', password: '', remember: true });
const passwordFormRef = ref<any>(null);
const passwordRules = {
  account: [{ required: true, message: '请输入手机号或邮箱' }],
  password: [{ required: true, min: 6, message: '密码至少 6 位' }],
};

const smsForm = reactive({ phone: '', captchaId: '', captchaCode: '', smsCode: '' });
const smsFormRef = ref<any>(null);
const smsRules = {
  phone: [
    { required: true, message: '请输入手机号' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' },
  ],
  captchaCode: [{ required: true, message: '请输入图形验证码' }],
  smsCode: [{ required: true, len: 6, message: '请输入 6 位短信验证码' }],
};

const captchaId = ref<string>('');

const thirdPartyChannels = [
  { key: 'wechat', label: '微信', icon: 'mdi:wechat' },
  { key: 'apple', label: 'Apple', icon: 'mdi:apple' },
  { key: 'google', label: 'Google', icon: 'mdi:google' },
];

const riskOpen = ref(false);
const riskMode = ref<'captcha' | 'lock'>('captcha');
const pendingRequest = ref<null | (() => Promise<any>)>(null);

onMounted(() => {
  trackEvent(ANALYTICS_EVENTS.loginPageView, { tab: tab.value });
});

watch(tab, (v) => {
  trackEvent(ANALYTICS_EVENTS.loginPageView, { tab: v, event: 'tab_switch' });
});

function onCaptchaChange(v: { captchaId: string }) {
  captchaId.value = v.captchaId;
  smsForm.captchaId = v.captchaId;
}

async function sendSms() {
  if (!/^1[3-9]\d{9}$/.test(smsForm.phone)) {
    message.warning('请输入有效的手机号');
    return false;
  }
  if (!smsForm.captchaCode) {
    message.warning('请先输入图形验证码');
    return false;
  }
  await sendSmsCode({ phone: smsForm.phone, captchaId: smsForm.captchaId, captchaCode: smsForm.captchaCode, countryCode: '+86' });
  message.success('验证码已发送');
  return true;
}

function afterLogin(data: { isFirstLogin?: boolean }) {
  userStore.setToken(data?.token || '');
  userStore.setLoginMethod(tab.value === 'sms' ? 'sms' : 'password');
  const redirect = route.query.redirect ? isSafeRedirect(route.query.redirect as string) : null;
  if (data?.isFirstLogin) {
    message.success('欢迎加入！请完善基础资料');
  }
  trackEvent(ANALYTICS_EVENTS.loginSuccess, { method: tab.value });
  if (redirect) {
    router.replace(redirect);
  } else {
    router.replace('/');
  }
}

async function onPasswordSubmit() {
  if (locked.value) return;
  submitting.value = true;
  trackEvent(ANALYTICS_EVENTS.loginSubmit, { method: 'password' });
  try {
    const data = await loginByPassword({ account: passwordForm.account, password: passwordForm.password, remember: passwordForm.remember });
    afterLogin(data);
  } catch (e: any) {
    handleLoginError(e, () => onPasswordSubmit());
  } finally {
    submitting.value = false;
  }
}

async function onSmsSubmit() {
  submitting.value = true;
  trackEvent(ANALYTICS_EVENTS.loginSubmit, { method: 'sms' });
  try {
    const data = await loginBySmsCode({ phone: smsForm.phone, smsCode: smsForm.smsCode, countryCode: '+86', captchaId: smsForm.captchaId, captchaCode: smsForm.captchaCode });
    afterLogin(data);
  } catch (e: any) {
    handleLoginError(e, () => onSmsSubmit());
  } finally {
    submitting.value = false;
  }
}

function handleLoginError(e: any, retry: () => Promise<any>) {
  const msg = e?.message || '';
  trackEvent(ANALYTICS_EVENTS.loginFail, { method: tab.value, message: msg });
  if (msg.includes('手机号尚未注册') || msg.includes('not_registered')) {
    Modal.confirm({
      title: '该手机号尚未注册',
      content: '是否立即注册？',
      okText: '去注册',
      onOk: () => router.push({ path: '/content/register', query: { phone: smsForm.phone } }),
    });
    return;
  }
  if (msg.includes('风控') || msg.includes('risk') || msg.includes('captcha_required')) {
    riskMode.value = 'captcha';
    pendingRequest.value = retry;
    riskOpen.value = true;
    return;
  }
  if (msg.includes('locked') || msg.includes('锁定') || msg.includes('LOCKED')) {
    locked.value = true;
    const seconds = e?.data?.remainingSeconds || 900;
    lockSeconds.value = seconds;
    riskMode.value = 'lock';
    riskOpen.value = true;
    if (lockTimer.value) clearInterval(lockTimer.value);
    lockTimer.value = setInterval(() => {
      lockSeconds.value -= 1;
      if (lockSeconds.value <= 0) {
        locked.value = false;
        clearInterval(lockTimer.value!);
        lockTimer.value = null;
      }
    }, 1000);
    trackEvent(ANALYTICS_EVENTS.loginLockout, { account: passwordForm.account });
    return;
  }
  if (msg.includes('账号或密码错误') || msg.includes('密码错误')) {
    message.error('账号或密码错误');
    return;
  }
  message.error(msg || '登录失败');
}

async function onRiskSuccess(payload: { captchaId: string; captchaCode: string }) {
  riskOpen.value = false;
  if (pendingRequest.value) {
    smsForm.captchaId = payload.captchaId;
    smsForm.captchaCode = payload.captchaCode;
    passwordForm.captchaId = payload.captchaId as any;
    await pendingRequest.value();
    pendingRequest.value = null;
  }
}

function thirdPartyLogin(channel: string) {
  trackEvent(ANALYTICS_EVENTS.loginThirdPartyClick, { channel });
  const redirectUri = `${window.location.origin}/content/login/callback`;
  const state = Math.random().toString(36).slice(2);
  const authUrl = {
    wechat: `https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=${encodeURIComponent(redirectUri)}&response_type=code&scope=snsapi_login&state=${state}`,
    apple: `https://appleid.apple.com/auth/authorize?client_id=CLIENT_ID&redirect_uri=${encodeURIComponent(redirectUri)}&response_type=code&state=${state}`,
    google: `https://accounts.google.com/o/oauth2/v2/auth?client_id=CLIENT_ID&redirect_uri=${encodeURIComponent(redirectUri)}&response_type=code&scope=openid+email+profile&state=${state}`,
  }[channel];
  if (!authUrl) return;
  if (isMobileDevice()) {
    window.location.href = authUrl;
  } else {
    const win = window.open('about:blank', '_blank');
    if (win) win.location.href = authUrl;
  }
}
</script>

<style lang="less" scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  background: #f5f7fa;
  &--login { padding: 24px; }
  &__brand {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    background: linear-gradient(135deg, #4f46e5, #7c3aed);
    color: #fff;
    padding: 48px;
    @media (max-width: 768px) { display: none; }
  }
  &__title { font-size: 36px; margin: 0 0 12px; }
  &__subtitle { opacity: 0.85; }
  &__content {
    width: 100%;
    max-width: 420px;
    margin: auto;
    padding: 32px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
  }
  &__tabs { margin-bottom: 16px; }
  &__row { display: flex; justify-content: space-between; align-items: center; }
  &__link { color: #4f46e5; }
  &__divider { text-align: center; color: #999; margin: 16px 0; position: relative; &::before, &::after { content: ''; position: absolute; top: 50%; width: 30%; height: 1px; background: #eee; } &::before { left: 0; } &::after { right: 0; } }
  &__third-party { display: flex; justify-content: center; gap: 16px; }
  &__footer { text-align: center; margin-top: 24px; color: #666; }
}
</style>
