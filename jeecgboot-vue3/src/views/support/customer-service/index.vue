<template>
  <div class="customer-service-page" :class="{ 'mobile-fullscreen': isMobile }">
    <ChatPanel />
    <RatingModal :visible="showRating" :session-id="closedSessionId" @close="showRating = false" @rated="handleRated" />
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted, onUnmounted } from 'vue';
  import { useFeedbackStore } from '/@/store/modules/feedback';
  import ChatPanel from './components/ChatPanel.vue';
  import RatingModal from './components/RatingModal.vue';

  const feedbackStore = useFeedbackStore();
  const showRating = ref(false);
  const closedSessionId = ref('');
  const isMobile = ref(window.innerWidth < 768);

  const handleResize = () => {
    isMobile.value = window.innerWidth < 768;
  };

  const handleRated = () => {
    showRating.value = false;
  };

  onMounted(() => {
    window.addEventListener('resize', handleResize);
    const unwatch = feedbackStore.$onAction(({ name }) => {
      if (name === 'clearSession') {
        closedSessionId.value = feedbackStore.currentSession?.id || '';
        showRating.value = true;
      }
    });
    onUnmounted(() => {
      window.removeEventListener('resize', handleResize);
      unwatch();
    });
  });
</script>

<style scoped lang="less">
  .customer-service-page {
    height: calc(100vh - 120px);
    padding: 16px;
    &.mobile-fullscreen {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      z-index: 1000;
      padding: 0;
      background: #fff;
    }
  }
</style>
