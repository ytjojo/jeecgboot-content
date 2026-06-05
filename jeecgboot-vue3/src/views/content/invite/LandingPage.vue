<template>
  <div class="invite-landing">
    <div class="landing-banner">
      <h1>你的好友邀请你加入</h1>
      <p class="subtitle">发现精彩内容，结识志同道合的朋友</p>
    </div>

    <div class="landing-content">
      <a-spin :spinning="validating">
        <a-result
          v-if="invalidReason"
          status="warning"
          :title="invalidTitle"
          :sub-title="invalidReason"
        >
          <template #extra>
            <a-button type="primary" @click="goHome">返回首页</a-button>
          </template>
        </a-result>

        <template v-else-if="!validating">
          <div class="value-list">
            <div class="value-item" v-for="(item, i) in valuePoints" :key="i">
              <check-circle-outlined class="value-icon" />
              <span>{{ item }}</span>
            </div>
          </div>

          <div class="action-area">
            <a-button type="primary" size="large" block @click="goRegister">
              立即注册
            </a-button>
            <p class="login-hint">
              已有账号？<a @click="goLogin">去登录</a>
            </p>
          </div>
        </template>
      </a-spin>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { CheckCircleOutlined } from '@ant-design/icons-vue';
import { validateInviteCode } from '/@/api/content/invite';
import { useUserStore } from '/@/store/modules/user';
import { SOCIAL_EVENTS, trackSocialEvent } from '/@/utils/social/analytics';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const validating = ref(true);
const invalidReason = ref('');
const invalidTitle = ref('');
const inviteCode = ref('');

const valuePoints = ['发现优质内容', '关注感兴趣的人', '参与社区互动', '获得成长积分'];

onMounted(async () => {
  // Redirect logged-in users
  if (userStore.getToken) {
    router.replace('/');
    return;
  }

  inviteCode.value = (route.params.inviteCode as string) || '';
  if (!inviteCode.value) {
    invalidTitle.value = '邀请无效';
    invalidReason.value = '邀请码不能为空';
    validating.value = false;
    return;
  }

  try {
    const res = await validateInviteCode(inviteCode.value);
    if (!res?.valid) {
      invalidTitle.value = '邀请无效';
      invalidReason.value = res?.reason || '邀请码无效或已过期';
    }
  } catch {
    invalidTitle.value = '验证失败';
    invalidReason.value = '无法验证邀请码，请稍后重试';
  } finally {
    validating.value = false;
    if (!invalidReason.value) {
      trackSocialEvent(SOCIAL_EVENTS.INVITE_LANDING_PAGE_VIEW, { inviteCode: inviteCode.value });
    }
  }
});

function goRegister() {
  trackSocialEvent(SOCIAL_EVENTS.INVITE_REGISTER_CLICK, { inviteCode: inviteCode.value });
  router.push({ path: '/register', query: { inviteCode: inviteCode.value } });
}

function goLogin() {
  router.push('/login');
}

function goHome() {
  router.push('/');
}
</script>

<style scoped>
.invite-landing {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #1890ff 0%, #722ed1 100%);
}
.landing-banner {
  text-align: center;
  padding: 60px 20px 40px;
  color: #fff;
}
.landing-banner h1 {
  font-size: 28px;
  margin-bottom: 8px;
}
.subtitle {
  font-size: 16px;
  opacity: 0.85;
}
.landing-content {
  flex: 1;
  background: #fff;
  border-radius: 24px 24px 0 0;
  padding: 32px 24px;
  margin-top: -16px;
}
.value-list {
  margin-bottom: 32px;
}
.value-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  font-size: 16px;
}
.value-icon {
  color: #52c41a;
  font-size: 20px;
}
.action-area {
  margin-top: 24px;
}
.login-hint {
  text-align: center;
  margin-top: 16px;
  color: #999;
}
.login-hint a {
  color: #1890ff;
  cursor: pointer;
}
@media (max-width: 768px) {
  .landing-banner {
    padding: 40px 16px 24px;
  }
  .landing-banner h1 {
    font-size: 22px;
  }
  .action-area {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 16px 24px;
    background: #fff;
    box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
  }
}
</style>
