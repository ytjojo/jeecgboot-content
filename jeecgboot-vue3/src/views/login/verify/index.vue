<template>
  <div class="login-verify">
    <a-card class="verify-card" title="安全核验">
      <a-form :model="formState" layout="vertical">
        <a-form-item label="手机号">
          <a-input v-model:value="formState.phone" placeholder="请输入手机号" size="large" />
        </a-form-item>
        <a-form-item label="验证码">
          <a-row :gutter="12">
            <a-col :span="16">
              <a-input v-model:value="formState.verifyCode" placeholder="请输入验证码" size="large" />
            </a-col>
            <a-col :span="8">
              <a-button size="large" block :disabled="countdown > 0" @click="handleSendCode">
                {{ countdown > 0 ? `${countdown}秒` : '获取验证码' }}
              </a-button>
            </a-col>
          </a-row>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" size="large" block :loading="submitting" @click="handleSubmit">
            验证并恢复
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStatusStore } from '/@/store/modules/userStatus';
import { sendVerifyCode } from '/@/api/content/userStatus';
import { useMessage } from '/@/hooks/web/useMessage';
import { useBreakpoint } from '/@/hooks/event/useBreakpoint';

const { screenRef } = useBreakpoint();
const isMobile = computed(() => {
  const screen = screenRef.value;
  return screen === 'xs' || screen === 'sm';
});

const router = useRouter();
const userStatusStore = useUserStatusStore();
const { createMessage } = useMessage();

const formState = reactive({ phone: '', verifyCode: '' });
const countdown = ref(0);
const submitting = ref(false);
let timer: ReturnType<typeof setInterval> | null = null;

async function handleSendCode() {
  if (!formState.phone) {
    createMessage.warning('请输入手机号');
    return;
  }
  try {
    await sendVerifyCode(formState.phone);
    createMessage.success('验证码已发送');
    countdown.value = 60;
    timer = setInterval(() => {
      countdown.value--;
      if (countdown.value <= 0) {
        clearInterval(timer!);
        timer = null;
      }
    }, 1000);
  } catch {
    createMessage.error('验证码发送失败');
  }
}

async function handleSubmit() {
  if (!formState.phone || !formState.verifyCode) {
    createMessage.warning('请输入手机号和验证码');
    return;
  }
  submitting.value = true;
  try {
    await userStatusStore.verifySecurity(formState.phone, formState.verifyCode);
    createMessage.success('核验成功，账号已恢复');
    router.push('/login');
  } catch {
    createMessage.error('核验失败，请检查验证码');
  } finally {
    submitting.value = false;
  }
}

onUnmounted(() => {
  if (timer) clearInterval(timer);
});
</script>

<style scoped>
.login-verify {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f0f2f5;
  padding: 16px;
}
.verify-card {
  max-width: 420px;
  width: 100%;
}
</style>
