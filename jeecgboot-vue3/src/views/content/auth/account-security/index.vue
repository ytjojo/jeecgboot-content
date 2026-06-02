<template>
  <div class="account-security">
    <a-page-header title="账号安全" :back-icon="true" @back="$router.back()" />
    <a-alert
      v-if="status && !status.emailBound"
      type="warning"
      message="您还未绑定邮箱，部分功能将受限"
      show-icon
      class="account-security__alert"
    />
    <a-alert
      v-if="coolingOffDays > 0"
      type="error"
      :message="`您的账号处于注销冷静期，剩余 ${coolingOffDays} 天`"
      show-icon
      class="account-security__alert"
      action="<a-button type='link' @click='goCancel'>取消注销</a-button>"
    />
    <a-card title="账号绑定" :bordered="false">
      <a-list :data-source="bindingItems" :split="false">
        <template #renderItem="{ item }">
          <a-list-item class="account-security__item">
            <a-list-item-meta :title="item.title" :description="item.description">
              <template #avatar><Icon :icon="item.icon" :size="24" /></template>
            </a-list-item-meta>
            <template #actions>
              <a v-if="item.status === 'unbound'" @click="onBind(item)">绑定</a>
              <template v-else>
                <a v-if="item.key !== 'phone' || canUnbindPhone" @click="onUnbind(item)">解绑</a>
                <a v-if="['phone', 'email'].includes(item.key)" @click="onRebind(item)">换绑</a>
              </template>
            </template>
            <a-tag :color="item.status === 'bound' ? 'green' : 'default'">
              {{ item.status === 'bound' ? '已绑定' : '未绑定' }}
            </a-tag>
          </a-list-item>
        </template>
      </a-list>
    </a-card>
    <a-card title="安全设置" :bordered="false" class="account-security__card">
      <a-list :data-source="securityItems" :split="false">
        <template #renderItem="{ item }">
          <a-list-item class="account-security__item" @click="item.onClick">
            <a-list-item-meta :title="item.title" :description="item.description">
              <template #avatar><Icon :icon="item.icon" :size="24" /></template>
            </a-list-item-meta>
            <Icon icon="mdi:chevron-right" />
          </a-list-item>
        </template>
      </a-list>
    </a-card>
    <a-modal v-model:open="bindOpen" :title="bindAction === 'unbind' ? '解绑确认' : bindAction === 'rebind' ? '换绑' : '绑定'" @ok="submitBind" :confirm-loading="bindSubmitting">
      <a-form layout="vertical" v-if="bindAction !== 'unbind'">
        <a-form-item v-if="currentItem?.key === 'phone' || currentItem?.key === 'email'" label="新{{ currentItem.title }}">
          <a-input v-model:value="bindForm.target" :placeholder="currentItem.key === 'phone' ? '手机号' : '邮箱'" />
        </a-form-item>
        <a-form-item label="验证码">
          <a-input v-model:value="bindForm.code" placeholder="验证码" />
        </a-form-item>
      </a-form>
      <a-form layout="vertical" v-else>
        <p>解绑后将无法通过{{ currentItem?.title }}登录，请确认</p>
        <a-form-item label="验证码">
          <a-input v-model:value="bindForm.code" placeholder="请输入安全验证码" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { message, Modal } from 'ant-design-vue';
import { getAccountSecurityStatus, unbindPhone, unbindEmail, unbindThirdParty, rebindPhone, rebindEmail, bindPhone, bindEmail, bindThirdParty } from '/@/api/content/account/security';
import { useUserStore } from '/@/store/modules/user';
import { trackEvent, ANALYTICS_EVENTS } from '/@/components/Auth/analytics';

const router = useRouter();
const userStore = useUserStore();

const status = ref<any>(null);
const bindOpen = ref(false);
const bindAction = ref<'bind' | 'unbind' | 'rebind'>('bind');
const currentItem = ref<any>(null);
const bindSubmitting = ref(false);
const bindForm = reactive({ target: '', code: '' });

const bindingItems = computed(() => [
  { key: 'phone', title: '手机号', description: status.value?.phone || '未绑定', icon: 'mdi:cellphone', status: status.value?.phoneBound ? 'bound' : 'unbound' },
  { key: 'email', title: '邮箱', description: status.value?.email || '未绑定', icon: 'mdi:email-outline', status: status.value?.emailBound ? 'bound' : 'unbound' },
  { key: 'wechat', title: '微信', icon: 'mdi:wechat', status: status.value?.wechatBound ? 'bound' : 'unbound' },
  { key: 'apple', title: 'Apple', icon: 'mdi:apple', status: status.value?.appleBound ? 'bound' : 'unbound' },
  { key: 'google', title: 'Google', icon: 'mdi:google', status: status.value?.googleBound ? 'bound' : 'unbound' },
]);

const securityItems = [
  { title: '修改密码', description: '定期修改密码可提升账号安全', icon: 'mdi:lock-outline', onClick: () => message.info('请前往密码修改流程') },
  { title: '设备管理', description: '查看登录设备，主动下线或信任设备', icon: 'mdi:devices', onClick: () => router.push('/content/account-security/devices') },
  { title: '账号注销', description: '注销后无法恢复，请谨慎操作', icon: 'mdi:delete-outline', onClick: () => router.push('/content/account-security/cancellation') },
];

const canUnbindPhone = computed(() => status.value?.emailBound);
const coolingOffDays = computed(() => userStore.coolingOffDays || 0);

onMounted(async () => {
  try {
    status.value = await getAccountSecurityStatus();
    userStore.setAccountSecurityStatus(status.value);
  } catch {
    // 静默
  }
});

function onBind(item: any) {
  currentItem.value = item;
  bindAction.value = 'bind';
  bindForm.target = '';
  bindForm.code = '';
  bindOpen.value = true;
}

function onUnbind(item: any) {
  Modal.confirm({
    title: `解绑${item.title}`,
    content: '解绑后可能影响登录，请确认',
    onOk: () => {
      currentItem.value = item;
      bindAction.value = 'unbind';
      bindForm.code = '';
      bindOpen.value = true;
    },
  });
}

function onRebind(item: any) {
  currentItem.value = item;
  bindAction.value = 'rebind';
  bindForm.target = '';
  bindForm.code = '';
  bindOpen.value = true;
}

async function submitBind() {
  bindSubmitting.value = true;
  try {
    const key = currentItem.value?.key;
    if (bindAction.value === 'unbind') {
      if (key === 'phone') await unbindPhone({ smsCode: bindForm.code });
      else if (key === 'email') await unbindEmail({ emailCode: bindForm.code });
      else await unbindThirdParty({ channel: key, smsCode: bindForm.code });
      trackEvent(ANALYTICS_EVENTS.accountUnbind, { type: key });
    } else if (bindAction.value === 'rebind') {
      if (key === 'phone') await rebindPhone({ oldPhone: status.value?.phone, newPhone: bindForm.target, oldSmsCode: bindForm.code, newSmsCode: bindForm.code, countryCode: '+86' });
      else await rebindEmail({ oldEmail: status.value?.email, newEmail: bindForm.target, oldEmailCode: bindForm.code, newEmailCode: bindForm.code });
      trackEvent(ANALYTICS_EVENTS.accountRebind, { type: key });
    } else {
      if (key === 'phone') await bindPhone({ phone: bindForm.target, smsCode: bindForm.code, countryCode: '+86' });
      else if (key === 'email') await bindEmail({ email: bindForm.target, emailCode: bindForm.code });
      else {
        // 第三方
        const state = Math.random().toString(36).slice(2);
        const code = prompt(`请输入${currentItem.value.title}授权码（演示）`) || '';
        await bindThirdParty({ channel: key, code, state });
      }
      trackEvent(ANALYTICS_EVENTS.accountBind, { type: key });
    }
    message.success('操作成功');
    bindOpen.value = false;
    status.value = await getAccountSecurityStatus();
    userStore.setAccountSecurityStatus(status.value);
  } catch (e: any) {
    message.error(e?.message || '操作失败');
  } finally {
    bindSubmitting.value = false;
  }
}

function goCancel() {
  router.push('/content/account-security/cancellation');
}
</script>

<style lang="less" scoped>
.account-security {
  max-width: 640px;
  margin: 0 auto;
  padding: 16px;
  @media (max-width: 640px) { padding: 0; }
  &__alert { margin-bottom: 16px; }
  &__card { margin-top: 16px; }
  &__item { padding: 16px 0; cursor: pointer; }
}
</style>
