<template>
  <a-button class="sms-code-btn" :loading="loading" :disabled="active || disabled" @click="handleClick">
    <template v-if="active">重新获取({{ seconds }}s)</template>
    <template v-else>{{ text }}</template>
  </a-button>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useCountdown } from './useCountdown';
import { message } from 'ant-design-vue';

const props = withDefaults(defineProps<{ duration?: number; text?: string; disabled?: boolean }>(), {
  duration: 60,
  text: '获取验证码',
  disabled: false,
});
const emit = defineEmits<{ (e: 'click'): Promise<boolean> | boolean }>();

const loading = ref(false);
const { seconds, active, start, stop } = useCountdown(props.duration);

async function handleClick() {
  if (active.value || loading.value || props.disabled) return;
  loading.value = true;
  try {
    const result = await emit('click');
    if (result === false) {
      message.warning('请稍后再试');
      return;
    }
    start(props.duration);
  } catch (e: any) {
    if (e?.message?.includes('请稍后再试') || e?.message?.includes('冷却')) {
      message.warning('请稍后再试');
    } else {
      message.error(e?.message || '发送失败');
    }
  } finally {
    loading.value = false;
  }
}

defineExpose({ stop, start: (d?: number) => start(d ?? props.duration) });
</script>

<style lang="less" scoped>
.sms-code-btn { min-width: 110px; }
</style>
