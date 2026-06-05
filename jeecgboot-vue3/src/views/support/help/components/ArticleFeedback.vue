<template>
  <div class="article-feedback">
    <div class="feedback-prompt">这篇文章有帮助吗？</div>
    <div class="feedback-buttons">
      <a-button
        data-testid="helpful-btn"
        :class="{ selected: feedback === 'helpful' }"
        :disabled="feedback !== null"
        @click="handleFeedback(true)"
      >
        <like-outlined />
        有用
      </a-button>
      <a-button
        data-testid="unhelpful-btn"
        :class="{ selected: feedback === 'unhelpful' }"
        :disabled="feedback !== null"
        @click="handleFeedback(false)"
      >
        <dislike-outlined />
        无用
      </a-button>
    </div>
    <div v-if="feedback === 'unhelpful'" class="contact-cs">
      <a-divider />
      <p>是否需要更多帮助？</p>
      <a-button type="primary" @click="goToCustomerService">联系客服</a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { LikeOutlined, DislikeOutlined } from '@ant-design/icons-vue';
import { submitArticleFeedback } from '/@/api/support/help';

const props = defineProps<{ articleId: string }>();
const emit = defineEmits<{
  (e: 'feedback', helpful: boolean): void;
}>();

const router = useRouter();
const feedback = ref<string | null>(null);

const handleFeedback = async (helpful: boolean) => {
  if (feedback.value !== null) return;
  feedback.value = helpful ? 'helpful' : 'unhelpful';
  try {
    await submitArticleFeedback(props.articleId, { helpful });
  } catch {
    // 静默失败
  }
  emit('feedback', helpful);
};

const goToCustomerService = () => {
  router.push('/support/customer-service');
};
</script>

<style scoped lang="less">
.article-feedback {
  text-align: center;
  padding: 24px 0;

  .feedback-prompt {
    font-size: 16px;
    margin-bottom: 16px;
  }

  .feedback-buttons {
    display: flex;
    justify-content: center;
    gap: 16px;

    .selected {
      color: #1890ff;
      border-color: #1890ff;
    }
  }

  .contact-cs {
    margin-top: 16px;
  }
}
</style>
