<template>
  <a-modal :open="open" title="异常登录检测" :footer="null" :closable="false" :mask-closable="false" width="420px">
    <div class="anomaly-modal">
      <a-alert type="warning" message="检测到您的账号在新的设备/地点登录" show-icon />
      <a-descriptions :column="1" bordered size="small" class="anomaly-modal__desc">
        <a-descriptions-item label="登录时间">{{ data?.loginAt }}</a-descriptions-item>
        <a-descriptions-item label="设备">{{ data?.device }}</a-descriptions-item>
        <a-descriptions-item label="IP">{{ data?.ip }}</a-descriptions-item>
        <a-descriptions-item label="地点">{{ data?.location }}</a-descriptions-item>
      </a-descriptions>
      <p class="anomaly-modal__hint">如果这不是您的操作，请立即否认并修改密码</p>
      <a-space class="anomaly-modal__actions">
        <a-button :loading="denyLoading" danger @click="handleDeny">否认操作</a-button>
        <a-button type="primary" :loading="confirmLoading" @click="handleConfirm">确认是我</a-button>
      </a-space>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Modal } from 'ant-design-vue';
import { confirmAnomaly, denyAnomaly } from '/@/api/content/account/security';

interface AnomalyData {
  id: string;
  loginAt: string;
  device: string;
  ip: string;
  location: string;
  deviceId?: string;
}

const props = defineProps<{ open: boolean; data: AnomalyData | null }>();
const emit = defineEmits<{ (e: 'confirmed'): void; (e: 'denied', revokeDeviceId?: string): void }>();

const confirmLoading = ref(false);
const denyLoading = ref(false);

async function handleConfirm() {
  if (!props.data) return;
  confirmLoading.value = true;
  try {
    await confirmAnomaly(props.data.id);
    emit('confirmed');
  } finally {
    confirmLoading.value = false;
  }
}

function handleDeny() {
  if (!props.data) return;
  Modal.confirm({
    title: '确认否认此次登录？',
    content: '我们将下线该设备，并建议您修改密码',
    okText: '确认否认',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      denyLoading.value = true;
      try {
        await denyAnomaly({ id: props.data!.id, revokeDeviceId: props.data!.deviceId });
        emit('denied', props.data!.deviceId);
      } finally {
        denyLoading.value = false;
      }
    },
  });
}
</script>

<style lang="less" scoped>
.anomaly-modal {
  &__desc { margin: 16px 0; }
  &__hint { color: #999; font-size: 12px; margin-bottom: 16px; }
  &__actions { width: 100%; display: flex; justify-content: flex-end; }
}
</style>
