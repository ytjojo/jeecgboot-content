<template>
  <a-config-provider>
    <slot />
  </a-config-provider>
</template>

<script setup lang="ts">
// 异常登录通知全局侦听包装组件
import { onMounted, onUnmounted, ref } from 'vue';
import { Modal } from 'ant-design-vue';
import AnomalyNotificationModal from '/@/components/Auth/AnomalyNotificationModal.vue';
import { listAnomalyNotifications, confirmAnomaly, denyAnomaly } from '/@/api/content/account/security';
import { useUserStore } from '/@/store/modules/user';

const userStore = useUserStore();
const open = ref(false);
const data = ref<any>(null);
let timer: ReturnType<typeof setInterval> | null = null;
let handledIds = new Set<string>();

async function poll() {
  if (!userStore.getToken) return;
  try {
    const { records } = await listAnomalyNotifications({ pageNo: 1, pageSize: 5 });
    const unhandled = (records || []).filter((r: any) => !r.handled && !handledIds.has(r.id));
    if (unhandled.length && !open.value) {
      data.value = unhandled[0];
      open.value = true;
    }
  } catch {
    // 静默
  }
}

onMounted(() => {
  poll();
  timer = setInterval(poll, 60_000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});

function onConfirmed() {
  if (data.value) handledIds.add(data.value.id);
  open.value = false;
}

function onDenied(revokeDeviceId?: string) {
  if (data.value) handledIds.add(data.value.id);
  open.value = false;
  Modal.confirm({
    title: '建议修改密码',
    content: '是否立即前往修改密码？',
    onOk: () => location.assign('/content/account-security'),
  });
}
</script>
