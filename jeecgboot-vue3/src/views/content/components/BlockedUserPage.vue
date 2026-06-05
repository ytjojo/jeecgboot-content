<template>
  <!-- 拉黑发起方看到的页面 -->
  <div v-if="isBlocker" class="blocked-user-page">
    <div class="blocked-placeholder">
      <div class="blocked-icon">
        <StopOutlined style="font-size: 64px; color: #d9d9d9" />
      </div>
      <h3>无法查看该用户</h3>
      <p class="blocked-hint">您已拉黑该用户，如需查看可在黑名单中解除拉黑</p>
      <a-button type="primary" @click="goToBlacklist">前往黑名单管理</a-button>
    </div>
  </div>

  <!-- 被拉黑方看到的页面（404 风格） -->
  <div v-else class="blocked-user-page">
    <div class="blocked-placeholder">
      <div class="blocked-icon">
        <QuestionCircleOutlined style="font-size: 64px; color: #d9d9d9" />
      </div>
      <h3>该用户不存在</h3>
      <p class="blocked-hint">请检查链接是否正确</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { StopOutlined, QuestionCircleOutlined } from '@ant-design/icons-vue';
import { useRouter } from 'vue-router';

defineProps({
  isBlocker: { type: Boolean, default: false },
});

const router = useRouter();

function goToBlacklist() {
  router.push('/content/privacy/blacklist');
}
</script>

<style scoped>
.blocked-user-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}
.blocked-placeholder {
  text-align: center;
  padding: 48px 24px;
}
.blocked-icon {
  margin-bottom: 24px;
}
.blocked-placeholder h3 {
  font-size: 20px;
  color: rgba(0, 0, 0, 0.85);
  margin-bottom: 8px;
}
.blocked-hint {
  color: rgba(0, 0, 0, 0.45);
  margin-bottom: 24px;
}
</style>
