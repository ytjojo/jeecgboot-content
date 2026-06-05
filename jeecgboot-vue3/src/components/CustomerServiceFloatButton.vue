<!-- src/components/CustomerServiceFloatButton.vue -->
<template>
  <div class="cs-float-button" @click="handleClick">
    <a-badge :count="queuePosition" :offset="[-5, 5]">
      <a-button type="primary" shape="circle" size="large">
        <template #icon>
          <customer-service-outlined />
        </template>
      </a-button>
    </a-badge>
    <div v-if="hasActiveSession" class="active-dot"></div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { CustomerServiceOutlined } from '@ant-design/icons-vue';
import { useFeedbackStore } from '/@/store/modules/feedback';

const router = useRouter();
const feedbackStore = useFeedbackStore();

const queuePosition = computed(() => feedbackStore.queuePosition || 0);
const hasActiveSession = computed(() => feedbackStore.currentSession !== null);

const handleClick = () => {
  router.push('/customer-service');
};
</script>

<style scoped lang="less">
.cs-float-button {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 1000;
  cursor: pointer;

  .active-dot {
    position: absolute;
    top: 5px;
    right: 5px;
    width: 10px;
    height: 10px;
    background: #52c41a;
    border-radius: 50%;
    border: 2px solid #fff;
  }
}
</style>
