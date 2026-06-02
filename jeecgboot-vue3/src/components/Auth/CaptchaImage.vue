<template>
  <a-spin :spinning="loading" size="small">
    <div class="captcha-image" :class="{ 'captcha-image--disabled': disabled }" @click="refresh">
      <img v-if="imageBase64" :src="imageBase64" alt="captcha" class="captcha-image__img" />
      <div v-else class="captcha-image__placeholder">{{ loading ? '加载中' : '点击获取' }}</div>
    </div>
  </a-spin>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { getCaptchaImage } from '/@/api/content/auth/captcha';

const props = defineProps<{ disabled?: boolean }>();
const emit = defineEmits<{ (e: 'change', v: { captchaId: string; imageBase64: string }): void }>();

const imageBase64 = ref<string>('');
const loading = ref(false);
const captchaId = ref<string>('');

async function refresh() {
  if (props.disabled || loading.value) return;
  loading.value = true;
  try {
    const data = await getCaptchaImage();
    captchaId.value = data.captchaId;
    imageBase64.value = data.imageBase64?.startsWith('data:') ? data.imageBase64 : `data:image/png;base64,${data.imageBase64}`;
    emit('change', { captchaId: captchaId.value, imageBase64: imageBase64.value });
  } finally {
    loading.value = false;
  }
}

defineExpose({ refresh, getCaptchaId: () => captchaId.value });
onMounted(refresh);
</script>

<style lang="less" scoped>
.captcha-image {
  display: inline-block;
  width: 110px;
  height: 36px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  overflow: hidden;
  vertical-align: middle;
  &--disabled { cursor: not-allowed; opacity: 0.6; }
  &__img { width: 100%; height: 100%; object-fit: contain; }
  &__placeholder { display: flex; align-items: center; justify-content: center; height: 100%; font-size: 12px; color: #999; }
}
</style>
