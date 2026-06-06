<template>
  <div class="login-blocked">
    <a-card class="blocked-card">
      <a-result :status="resultStatus" :title="resultTitle" :sub-title="resultSubTitle">
        <template #extra>
          <a-space direction="vertical" :size="16" :style="isMobile ? { width: '100%' } : {}">
            <a-button v-if="canVerify" type="primary" size="large" block :style="!isMobile ? { width: '200px' } : {}" @click="goVerify">安全核验</a-button>
            <a-button size="large" block :style="!isMobile ? { width: '200px' } : {}" @click="goLogin">返回登录</a-button>
          </a-space>
        </template>
      </a-result>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStatusStore } from '/@/store/modules/userStatus';
import { useBreakpoint } from '/@/hooks/event/useBreakpoint';

const { screenRef } = useBreakpoint();
const isMobile = computed(() => {
  const screen = screenRef.value;
  return screen === 'xs' || screen === 'sm';
});

const router = useRouter();
const userStatusStore = useUserStatusStore();

const blockedStatus = ref<string>('');

const resultStatus = computed(() => blockedStatus.value === 'BANNED' ? 'error' : 'warning');
const resultTitle = computed(() => blockedStatus.value === 'BANNED' ? '账号已被封禁' : '账号已被冻结');
const resultSubTitle = computed(() => {
  if (blockedStatus.value === 'BANNED') {
    return '您的账号因违规已被封禁，如有疑问请联系管理员。';
  }
  return '您的账号已被冻结，请通过安全核验恢复正常使用。';
});
const canVerify = computed(() => blockedStatus.value === 'FROZEN');

function goVerify() {
  router.push('/login/verify');
}

function goLogin() {
  router.push('/login');
}

onMounted(() => {
  blockedStatus.value = userStatusStore.currentStatus || 'FROZEN';
});
</script>

<style scoped>
.login-blocked {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f0f2f5;
}
.blocked-card {
  max-width: 500px;
  width: 100%;
}
</style>
