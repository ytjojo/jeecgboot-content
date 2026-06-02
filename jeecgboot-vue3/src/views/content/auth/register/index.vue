<template>
  <div class="auth-page auth-page--register">
    <div class="auth-page__brand">
      <h1 class="auth-page__title">加入内容社区</h1>
      <p class="auth-page__subtitle">立即注册，开启你的创作之旅</p>
    </div>
    <div class="auth-page__content">
      <a-tabs v-model:active-key="tab" :destroy-inactive-tab-pane="true" centered>
        <a-tab-pane key="mobile" tab="手机号注册">
          <a-form layout="vertical" :model="mobileForm" :rules="mobileRules" ref="mobileFormRef" @finish="onMobileSubmit">
            <a-form-item name="phone">
              <a-input v-model:value="mobileForm.phone" placeholder="手机号" size="large" addon-before="+86" @blur="validateField('phone')" />
            </a-form-item>
            <a-form-item name="captchaCode">
              <a-input v-model:value="mobileForm.captchaCode" placeholder="图形验证码" size="large" maxlength="6">
                <template #suffix>
                  <CaptchaImage @change="onCaptchaChange" />
                </template>
              </a-input>
            </a-form-item>
            <a-form-item name="smsCode">
              <a-input v-model:value="mobileForm.smsCode" placeholder="短信验证码" size="large" maxlength="6" @input="onSmsInput">
                <template #suffix>
                  <SmsCodeButton :text="'获取验证码'" @click="sendSms" />
                </template>
              </a-input>
            </a-form-item>
            <a-form-item name="agreement">
              <a-checkbox v-model:checked="mobileForm.agreement">
                我已阅读并同意 <a @click="openAgreement">《用户协议》</a> 和 <a @click="openAgreement">《隐私政策》</a>
              </a-checkbox>
            </a-form-item>
            <a-form-item>
              <a-button type="primary" html-type="submit" size="large" block :loading="submitting" :disabled="!mobileForm.agreement">注册</a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>
        <a-tab-pane key="email" tab="邮箱注册">
          <a-form layout="vertical" :model="emailForm" :rules="emailRules" ref="emailFormRef" @finish="onEmailSubmit">
            <a-form-item name="email">
              <a-input v-model:value="emailForm.email" placeholder="邮箱" size="large" @blur="validateEmailField" />
            </a-form-item>
            <a-form-item name="password">
              <a-input-password v-model:value="emailForm.password" placeholder="密码（至少 8 位，含数字）" size="large" />
              <StrengthMeter :value="emailForm.password" />
            </a-form-item>
            <a-form-item name="confirmPassword">
              <a-input-password v-model:value="emailForm.confirmPassword" placeholder="确认密码" size="large" />
            </a-form-item>
            <a-form-item name="agreement">
              <a-checkbox v-model:checked="emailForm.agreement">
                我已阅读并同意 <a @click="openAgreement">《用户协议》</a>
              </a-checkbox>
            </a-form-item>
            <a-form-item>
              <a-button type="primary" html-type="submit" size="large" block :loading="submitting" :disabled="!emailForm.agreement">注册</a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>
      </a-tabs>
      <div class="auth-page__footer">
        已有账号？<router-link to="/content/login" class="auth-page__link">立即登录</router-link>
      </div>
    </div>
    <a-modal v-model:open="agreementOpen" title="用户协议" :footer="null" width="600px">
      <p>本协议由您与内容社区平台共同签署。注册即视为同意本协议全部条款。</p>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { message, Modal } from 'ant-design-vue';
import CaptchaImage from '/@/components/Auth/CaptchaImage.vue';
import SmsCodeButton from '/@/components/Auth/SmsCodeButton.vue';
import StrengthMeter from '/@/components/Auth/StrengthMeter.vue';
import { registerMobile, registerEmail, sendSmsCode } from '/@/api/content/auth';
import { evaluatePasswordStrength } from '/@/components/Auth/passwordStrength';
import { useUserStore } from '/@/store/modules/user';
import { trackEvent, ANALYTICS_EVENTS } from '/@/components/Auth/analytics';

const router = useRouter();
const userStore = useUserStore();

const tab = ref<'mobile' | 'email'>('mobile');
const submitting = ref(false);
const agreementOpen = ref(false);

const captchaId = ref('');

const mobileForm = reactive({ phone: '', captchaCode: '', smsCode: '', agreement: false });
const mobileFormRef = ref<any>(null);
const mobileRules = {
  phone: [
    { required: true, message: '请输入手机号' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号' },
  ],
  captchaCode: [{ required: true, message: '请输入图形验证码' }],
  smsCode: [{ required: true, len: 6, message: '请输入 6 位短信验证码' }],
  agreement: [{ validator: (_: any, v: boolean) => (v ? Promise.resolve() : Promise.reject('请先阅读并同意用户协议')) }],
};

const emailForm = reactive({ email: '', password: '', confirmPassword: '', agreement: false });
const emailFormRef = ref<any>(null);
const emailRules = {
  email: [
    { required: true, message: '请输入邮箱' },
    { type: 'email', message: '邮箱格式不正确' },
  ],
  password: [
    { required: true, message: '请输入密码' },
    { validator: (_: any, v: string) => (v && v.length >= 8 && /\d/.test(v) ? Promise.resolve() : Promise.reject('密码长度至少为 8 位且必须包含至少一个数字')) },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码' },
    { validator: (_: any, v: string) => (v === emailForm.password ? Promise.resolve() : Promise.reject('两次输入的密码不一致')) },
  ],
  agreement: [{ validator: (_: any, v: boolean) => (v ? Promise.resolve() : Promise.reject('请先阅读并同意用户协议')) }],
};

onMounted(() => trackEvent(ANALYTICS_EVENTS.registerPageView, { tab: tab.value }));

function onCaptchaChange(v: { captchaId: string }) {
  captchaId.value = v.captchaId;
  mobileForm.captchaCode && trackEvent(ANALYTICS_EVENTS.registerCaptchaSuccess);
  trackEvent(ANALYTICS_EVENTS.registerCaptchaClick);
}

function validateField(name: string) {
  mobileFormRef.value?.validateFields([name]).catch(() => undefined);
}

function validateEmailField() {
  emailFormRef.value?.validateFields(['email']).catch(() => undefined);
}

function onSmsInput(e: any) {
  const v = (e?.target?.value ?? e).toString();
  if (v.length === 6 && mobileForm.agreement) {
    onMobileSubmit();
  }
}

async function sendSms() {
  if (!/^1[3-9]\d{9}$/.test(mobileForm.phone)) {
    message.warning('请输入有效的手机号');
    return false;
  }
  if (!mobileForm.captchaCode) {
    message.warning('请先输入图形验证码');
    return false;
  }
  await sendSmsCode({ phone: mobileForm.phone, captchaId: captchaId.value, captchaCode: mobileForm.captchaCode, countryCode: '+86' });
  message.success('验证码已发送');
  return true;
}

function afterRegister(token?: string) {
  if (token) userStore.setToken(token);
  userStore.setLoginMethod('password');
  Modal.success({
    title: '注册成功',
    content: '欢迎加入内容社区！为你推荐感兴趣的内容',
    okText: '去完善兴趣',
    onOk: () => router.replace({ path: '/', query: { showInterestModal: '1' } }),
  });
}

async function onMobileSubmit() {
  submitting.value = true;
  trackEvent(ANALYTICS_EVENTS.registerSubmit, { method: 'mobile' });
  try {
    const data = await registerMobile({ phone: mobileForm.phone, smsCode: mobileForm.smsCode, captchaId: captchaId.value, captchaCode: mobileForm.captchaCode, agreement: mobileForm.agreement, countryCode: '+86' });
    trackEvent(ANALYTICS_EVENTS.registerSuccess, { method: 'mobile' });
    afterRegister((data as any).token);
  } catch (e: any) {
    const msg = e?.message || '';
    trackEvent(ANALYTICS_EVENTS.registerFail, { method: 'mobile', message: msg });
    if (msg.includes('已注册') || msg.includes('ALREADY_REGISTERED')) {
      Modal.confirm({
        title: '该手机号已注册',
        content: '是否直接登录？',
        okText: '去登录',
        onOk: () => router.push({ path: '/content/login', query: { phone: mobileForm.phone } }),
      });
      return;
    }
    message.error(msg || '注册失败');
  } finally {
    submitting.value = false;
  }
}

async function onEmailSubmit() {
  if (evaluatePasswordStrength(emailForm.password) === 'weak') {
    message.warning('密码强度不足，请设置更复杂的密码');
    return;
  }
  submitting.value = true;
  trackEvent(ANALYTICS_EVENTS.registerSubmit, { method: 'email' });
  try {
    await registerEmail({ email: emailForm.email, password: emailForm.password, confirmPassword: emailForm.confirmPassword, agreement: emailForm.agreement });
    trackEvent(ANALYTICS_EVENTS.registerSuccess, { method: 'email' });
    message.success(`验证邮件已发送至 ${emailForm.email}，请在 24 小时内完成验证`);
    router.push({ path: '/content/login' });
  } catch (e: any) {
    const msg = e?.message || '';
    trackEvent(ANALYTICS_EVENTS.registerFail, { method: 'email', message: msg });
    if (msg.includes('已注册')) {
      Modal.confirm({
        title: '该邮箱已注册',
        content: '是否直接登录？',
        okText: '去登录',
        onOk: () => router.push('/content/login'),
      });
      return;
    }
    message.error(msg || '注册失败');
  } finally {
    submitting.value = false;
  }
}

function openAgreement() {
  trackEvent(ANALYTICS_EVENTS.registerAgreementClick);
  agreementOpen.value = true;
}
</script>

<style lang="less" scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  background: #f5f7fa;
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
    max-width: 460px;
    margin: auto;
    padding: 32px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
  }
  &__link { color: #4f46e5; }
  &__footer { text-align: center; margin-top: 16px; color: #666; }
}
</style>
