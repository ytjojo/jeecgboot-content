<template>
  <div class="cancellation">
    <a-page-header title="账号注销" :back-icon="true" @back="$router.back()" />
    <template v-if="coolingOff">
      <a-card>
        <a-result status="warning" title="您的账号正在冷静期">
          <template #subTitle>
            <p>剩余 {{ coolingOff.remainingDays }} 天后将永久注销</p>
            <a-statistic-countdown :value="coolingOff.coolingOffExpiresAt" format="DD 天 HH 时 mm 分 ss 秒" />
          </template>
          <template #extra>
            <a-button type="primary" @click="confirmRevoke">取消注销</a-button>
          </template>
        </a-result>
      </a-card>
    </template>
    <template v-else>
      <a-card title="注销前检查">
        <a-skeleton v-if="loading" :paragraph="{ rows: 4 }" active />
        <template v-else-if="eligibility">
          <a-list :data-source="eligibility.checks" :split="false">
            <template #renderItem="{ item }">
              <a-list-item>
                <Icon :icon="item.passed ? 'mdi:check-circle' : 'mdi:close-circle'" :color="item.passed ? '#52c41a' : '#ff4d4f'" :size="20" />
                <span style="margin-left: 8px; flex: 1">{{ item.name }}</span>
                <a v-if="!item.passed && item.action" @click="goAction(item.action)">去处理</a>
              </a-list-item>
            </template>
          </a-list>
          <a-alert v-if="eligibility.outstandingPoints" type="warning" :message="`您还有 ${eligibility.outstandingPoints} 积分未使用`" show-icon />
        </template>
        <a-button type="primary" danger block size="large" :disabled="!eligibility?.eligible" class="cancellation__btn" @click="applyOpen = true">申请注销</a-button>
      </a-card>
    </template>
    <a-modal v-model:open="applyOpen" title="确认注销" @ok="confirmApply" :confirm-loading="applying">
      <a-alert type="error" message="注销后无法恢复，请确认" show-icon />
      <a-form layout="vertical">
        <a-form-item label="请输入「确认注销」">
          <a-input v-model:value="confirmText" placeholder="确认注销" />
        </a-form-item>
        <a-form-item label="短信验证码">
          <a-input v-model:value="smsCode" placeholder="请输入安全验证码" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { message, Modal } from 'ant-design-vue';
import { checkCancellationEligibility, applyCancellation, getCancellationStatus, cancelCancellation } from '/@/api/content/account/cancellation';
import { useUserStore } from '/@/store/modules/user';
import { trackEvent, ANALYTICS_EVENTS } from '/@/components/Auth/analytics';

const router = useRouter();
const userStore = useUserStore();

const loading = ref(false);
const eligibility = ref<any>(null);
const coolingOff = ref<any>(null);
const applyOpen = ref(false);
const applying = ref(false);
const confirmText = ref('');
const smsCode = ref('');

async function load() {
  loading.value = true;
  try {
    const status = await getCancellationStatus();
    if (status && status.status === 'cooling_off') {
      coolingOff.value = status;
      userStore.setCancellationStatus(status, status.remainingDays);
    } else {
      eligibility.value = await checkCancellationEligibility();
      userStore.setCancellationStatus(null, 0);
    }
  } finally {
    loading.value = false;
  }
}

function goAction(action: string) {
  router.push(action);
}

async function confirmApply() {
  if (confirmText.value !== '确认注销') {
    message.warning('请输入「确认注销」');
    return;
  }
  if (!smsCode.value) {
    message.warning('请输入验证码');
    return;
  }
  applying.value = true;
  try {
    await applyCancellation({ smsCode: smsCode.value });
    message.success('注销申请已提交');
    applyOpen.value = false;
    trackEvent(ANALYTICS_EVENTS.accountCancelApply);
    await load();
  } catch (e: any) {
    message.error(e?.message || '提交失败');
  } finally {
    applying.value = false;
  }
}

function confirmRevoke() {
  Modal.confirm({
    title: '确认取消注销？',
    content: '取消后您的账号将继续正常使用',
    onOk: async () => {
      if (coolingOff.value?.id) {
        await cancelCancellation(coolingOff.value.id);
        message.success('已取消注销');
        trackEvent(ANALYTICS_EVENTS.accountCancelRevoke);
        coolingOff.value = null;
        await load();
      }
    },
  });
}

onMounted(load);
</script>

<style lang="less" scoped>
.cancellation {
  max-width: 480px;
  margin: 0 auto;
  padding: 16px;
  &__btn { margin-top: 24px; }
}
</style>
