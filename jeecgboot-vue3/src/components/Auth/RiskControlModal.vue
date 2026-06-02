<template>
  <a-modal
    :open="open"
    :title="mode === 'lock' ? '账号已锁定' : '安全验证'"
    :footer="null"
    :closable="false"
    :mask-closable="false"
    width="380px"
    @cancel="handleCancel"
  >
    <template v-if="mode === 'lock'">
      <p class="risk-modal__desc">密码错误次数过多，请稍后重试</p>
      <div class="risk-modal__countdown">
        <a-statistic-countdown :value="lockDeadline" format="mm 分 ss 秒" @finish="handleCancel" />
      </div>
    </template>
    <template v-else>
      <p class="risk-modal__desc">请输入图形验证码以继续操作</p>
      <a-form layout="inline" class="risk-modal__form">
        <a-form-item>
          <CaptchaImage ref="captchaRef" @change="onCaptchaChange" />
        </a-form-item>
        <a-form-item class="risk-modal__form-item">
          <a-input v-model:value="captchaCode" placeholder="验证码" :maxlength="6" @press-enter="submit" />
        </a-form-item>
      </a-form>
      <a-button type="primary" :loading="submitting" block class="risk-modal__btn" @click="submit">验证</a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { message } from 'ant-design-vue';
import CaptchaImage from './CaptchaImage.vue';
import { verifyCaptcha } from '/@/api/content/auth/captcha';

interface Props {
  open: boolean;
  mode?: 'captcha' | 'lock';
  lockSeconds?: number;
}
const props = withDefaults(defineProps<Props>(), { mode: 'captcha', lockSeconds: 900 });
const emit = defineEmits<{ (e: 'success', payload: { captchaId: string; captchaCode: string }): void; (e: 'cancel'): void }>();

const captchaRef = ref<InstanceType<typeof CaptchaImage> | null>(null);
const captchaId = ref<string>('');
const captchaCode = ref<string>('');
const submitting = ref<boolean>(false);
const lockDeadline = ref<number>(Date.now() + props.lockSeconds * 1000);

watch(
  () => props.open,
  (v) => {
    if (v && props.mode === 'lock') {
      lockDeadline.value = Date.now() + props.lockSeconds * 1000;
    }
    if (v && props.mode === 'captcha') {
      captchaCode.value = '';
      captchaRef.value?.refresh();
    }
  }
);

function onCaptchaChange(v: { captchaId: string }) {
  captchaId.value = v.captchaId;
}

async function submit() {
  if (!captchaCode.value) {
    message.warning('请输入验证码');
    return;
  }
  submitting.value = true;
  try {
    await verifyCaptcha({ captchaId: captchaId.value, captchaCode: captchaCode.value });
    emit('success', { captchaId: captchaId.value, captchaCode: captchaCode.value });
  } catch (e: any) {
    message.error(e?.message || '验证失败');
    captchaRef.value?.refresh();
    captchaCode.value = '';
  } finally {
    submitting.value = false;
  }
}

function handleCancel() {
  if (props.mode === 'lock') return;
  emit('cancel');
}
</script>

<style lang="less" scoped>
.risk-modal {
  &__desc { color: #666; font-size: 14px; margin-bottom: 16px; }
  &__countdown { text-align: center; font-size: 18px; color: #ff4d4f; }
  &__form { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
  &__form-item { flex: 1; }
  &__btn { margin-top: 8px; }
}
</style>
