<template>
  <a-modal
    :open="visible"
    title="服务评价"
    :footer="null"
    :width="420"
    @cancel="handleClose"
  >
    <div class="rating-modal-content">
      <div class="rating-stars">
        <span class="rating-label">请为本次服务评分</span>
        <a-rate v-model:value="score" :count="5" />
      </div>

      <div class="rating-comment">
        <a-textarea
          v-model:value="comment"
          placeholder="留下您的评价（可选）"
          :maxlength="200"
          :rows="4"
          show-count
        />
      </div>

      <div class="rating-actions">
        <a-button
          data-testid="submit-rating"
          type="primary"
          :disabled="score === 0"
          :loading="submitting"
          @click="handleSubmit"
        >
          提交评价
        </a-button>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { message } from 'ant-design-vue';
import { submitServiceRating } from '/@/api/support/customer-service';

const props = defineProps<{
  visible: boolean;
  sessionId: string;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'rated'): void;
}>();

const score = ref(0);
const comment = ref('');
const submitting = ref(false);

function handleClose() {
  emit('close');
}

async function handleSubmit() {
  if (score.value === 0) return;
  submitting.value = true;
  try {
    await submitServiceRating(props.sessionId, {
      score: score.value,
      comment: comment.value || undefined,
    });
    message.success('评价成功');
    emit('rated');
    emit('close');
  } catch {
    message.error('评价失败，请重试');
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped lang="less">
.rating-modal-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.rating-stars {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;

  .rating-label {
    font-size: 14px;
    color: #666;
  }
}

.rating-comment {
  width: 100%;
}

.rating-actions {
  display: flex;
  justify-content: center;
}
</style>
