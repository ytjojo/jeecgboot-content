<template>
  <div class="auth-page auth-page--forgot">
    <div class="auth-page__content">
      <h2 class="auth-page__title">找回密码</h2>
      <a-steps :current="step" size="small" class="auth-page__steps" @change="onStepClick">
        <a-step title="验证身份" />
        <a-step title="设置新密码" />
        <a-step title="完成" />
      </a-steps>
      <template v-if="step === 0">
        <a-tabs v-model:active-key="verifyTab">
          <a-tab-pane key="mobile" tab="手机号">
            <a-form layout="vertical" :model="form" :rules="rules" ref="formRef" @finish="onVerifyMobile">
              <a-form-item name="phone">
                <a-input v-model:value="form.phone" placeholder="手机号" size="large" addon-before="+86" />
              </a-form-item>
              <a-form-item name="smsCode">
                <a-input v-model:value="form.smsCode" placeholder="短信验证码" size="large" maxlength="6">
                  <template #suffix>
                    <SmsCodeButton @click="sendSms" />
                  </template>
                </a-input>
              </a-form-item>
              <a-button type="primary" size="large" block html-type="submit" :loading="submitting">下一步</a-button>
            </a-form>
          </a-tab-pane>
          <a-tab-pane key="email" tab="邮箱">
            <a-form layout="vertical" :model="form" :rules="emailRules" ref="formRef" @finish="onVerifyEmail">
              <a-form-item name="email">
                <a-input v-model:value="form.email" placeholder="邮箱" size="large" />
              </a-form-item>
              <a-form-item>
                <a-button :loading="submitting" @click="sendEmailCode">发送验证邮件</a-button>
              </a-form-item>
            </a-form>
          </a-tab-pane>
        </a-tabs>
        <div class="auth-page__footer">
          想起来密码了？<router-link to="/content/login">立即登录</router-link>
        </div>
      </template>
      <template v-else-if="step === 1">
        <a-alert v-if="isHighRisk" type="warning" message="本次找回需要额外验证，请联系客服" show-icon class="auth-page__alert" />
        <a-form layout="vertical" :model="form" :rules="passwordRules" ref="pwdFormRef" @finish="onReset">
          <a-form-item name="password">
            <a-input-password v-model:value="form.password" placeholder="新密码（至少 8 位）" size="large" />
            <StrengthMeter :value="form.password" />
          </a-form-item>
          <a-form-item name="confirmPassword">
            <a-input-password v-model:value="form.confirmPassword" placeholder="确认密码" size="large" />
          </a-form-item>
          <a-button type="primary" size="large" block html-type="submit" :loading="submitting" :disabled="isHighRisk">确认重置</a-button>
        </a-form>
      </template>
      <template v-else>
        <a-result status="success" title="密码重置成功" sub-title="请使用新密码登录">
          <template #extra>
            <a-button type="primary" @click="$router.push('/content/login')">去登录</a-button>
          </template>
        </a-result>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import SmsCodeButton from '/@/components/Auth/SmsCodeButton.vue';
import StrengthMeter from '/@/components/Auth/StrengthMeter.vue';
import { sendSmsCode, sendEmailCode as sendEmail } from '/@/api/content/auth';
import { resetPassword } from '/@/api/content/account/security';
import { trackEvent, ANALYTICS_EVENTS } from '/@/components/Auth/analytics';

const route = useRoute();
const router = useRouter();

const step = ref(0);
const verifyTab = ref<'mobile' | 'email'>('mobile');
const submitting = ref(false);
const isHighRisk = ref(false);

const form = reactive({ phone: '', smsCode: '', email: '', password: '', confirmPassword: '' });
const formRef = ref<any>(null);
const pwdFormRef = ref<any>(null);
const rules = {
  phone: [{ required: true, pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号' }],
  smsCode: [{ required: true, len: 6, message: '请输入 6 位验证码' }],
};
const emailRules = {
  email: [{ required: true, type: 'email', message: '请输入有效邮箱' }],
};
const passwordRules = {
  password: [{ required: true, min: 8, message: '密码至少 8 位' }],
  confirmPassword: [
    { required: true, message: '请再次输入密码' },
    { validator: (_: any, v: string) => (v === form.password ? Promise.resolve() : Promise.reject('两次输入的密码不一致')) },
  ],
};

onMounted(() => {
  trackEvent(ANALYTICS_EVENTS.passwordResetStart, { method: verifyTab.value });
  if (route.query.token) {
    step.value = 1;
  }
  if (route.query.highRisk) {
    isHighRisk.value = true;
  }
});

async function sendSms() {
  if (!form.phone) {
    message.warning('请输入手机号');
    return false;
  }
  await sendSmsCode({ phone: form.phone, countryCode: '+86' });
  message.success('验证码已发送');
  return true;
}

async function sendEmailCode() {
  await sendEmail({ email: form.email });
  message.success('验证邮件已发送，请查收');
}

function onStepClick(target: number) {
  if (target < step.value) step.value = target;
}

async function onVerifyMobile() {
  submitting.value = true;
  try {
    if (verifyTab.value === 'mobile') {
      step.value = 1;
    } else {
      await sendEmail({ email: form.email });
      step.value = 1;
    }
  } finally {
    submitting.value = false;
  }
}

async function onVerifyEmail() {
  submitting.value = true;
  try {
    await sendEmail({ email: form.email });
    step.value = 1;
  } finally {
    submitting.value = false;
  }
}

async function onReset() {
  submitting.value = true;
  try {
    const token = (route.query.token as string) || form.smsCode;
    await resetPassword({
      account: verifyTab.value === 'mobile' ? form.phone : form.email,
      newPassword: form.password,
      smsOrEmailCode: token,
      type: verifyTab.value === 'mobile' ? 'sms' : 'email',
    });
    step.value = 2;
    trackEvent(ANALYTICS_EVENTS.passwordResetSuccess);
  } catch (e: any) {
    if (e?.message?.includes('高风险') || e?.message?.includes('高')) {
      isHighRisk.value = true;
    }
    message.error(e?.message || '重置失败');
  } finally {
    submitting.value = false;
  }
}
</script>

<style lang="less" scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  background: #f5f7fa;
  padding: 24px;
  &__content {
    width: 100%;
    max-width: 480px;
    margin: auto;
    padding: 32px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
  }
  &__title { text-align: center; margin: 0 0 24px; }
  &__steps { margin-bottom: 24px; }
  &__footer { text-align: center; margin-top: 16px; color: #666; }
  &__alert { margin-bottom: 16px; }
}
</style>
