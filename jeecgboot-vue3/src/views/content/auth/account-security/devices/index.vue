<template>
  <div class="device-list">
    <a-page-header title="设备管理" :back-icon="true" @back="$router.back()" />
    <a-skeleton v-if="loading" :paragraph="{ rows: 6 }" active />
    <a-empty v-else-if="!devices.length" description="暂无登录设备" />
    <a-list v-else :data-source="devices" item-layout="vertical">
      <template #renderItem="{ item }">
        <a-list-item class="device-list__item">
          <a-list-item-meta>
            <template #avatar>
              <Icon :icon="deviceIcon(item.type)" :size="32" />
            </template>
            <template #title>
              <span>{{ item.name }}</span>
              <a-tag v-if="item.current" color="blue" class="device-list__tag">当前设备</a-tag>
              <a-tag v-if="item.trusted" color="green" class="device-list__tag">已信任</a-tag>
              <a-tag v-else color="orange" class="device-list__tag">未信任</a-tag>
              <a-tag v-if="item.evicted" color="red" class="device-list__tag">已被挤出</a-tag>
            </template>
            <template #description>
              <div>最后登录：{{ item.lastLoginAt }}</div>
              <div>IP：{{ item.ip }} · {{ item.location }}</div>
            </template>
          </a-list-item-meta>
          <template #actions>
            <a v-if="!item.current" @click="confirmRevoke(item)">下线</a>
            <a v-if="!item.current && !item.trusted" @click="confirmTrust(item, true)">信任设备</a>
            <a v-if="!item.current && item.trusted" @click="confirmTrust(item, false)">取消信任</a>
          </template>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { Modal, message } from 'ant-design-vue';
import { listDevices, revokeDevice, trustDevice, untrustDevice } from '/@/api/content/account/security';
import { trackEvent, ANALYTICS_EVENTS } from '/@/components/Auth/analytics';

const devices = ref<any[]>([]);
const loading = ref(false);

function deviceIcon(type: string) {
  return { pc: 'mdi:monitor', mobile: 'mdi:cellphone', tablet: 'mdi:tablet' }[type] || 'mdi:devices';
}

async function load() {
  loading.value = true;
  try {
    devices.value = (await listDevices()) || [];
  } catch (e: any) {
    message.error(e?.message || '加载失败');
  } finally {
    loading.value = false;
  }
}

function confirmRevoke(item: any) {
  Modal.confirm({
    title: '确定下线该设备吗？',
    content: '下线后该设备需要重新登录',
    onOk: async () => {
      await revokeDevice(item.id);
      message.success('已下线');
      trackEvent(ANALYTICS_EVENTS.deviceRevoke, { deviceId: item.id });
      await load();
    },
  });
}

function confirmTrust(item: any, trust: boolean) {
  Modal.confirm({
    title: trust ? '信任该设备' : '取消信任',
    content: trust ? '信任后该设备登录将跳过异常检测' : '取消信任后该设备登录将触发异常检测',
    onOk: async () => {
      if (trust) await trustDevice(item.id);
      else await untrustDevice(item.id);
      message.success('操作成功');
      await load();
    },
  });
}

onMounted(load);
</script>

<style lang="less" scoped>
.device-list {
  max-width: 800px;
  margin: 0 auto;
  padding: 16px;
  &__item { padding: 16px 0; }
  &__tag { margin-left: 8px; }
}
</style>
