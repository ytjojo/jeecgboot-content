<template>
  <div class="comment-input-guard">
    <a-tooltip :title="tooltipTitle" :visible="showTooltip">
      <div :class="{ 'guard-disabled': !canComment }">
        <slot>
          <a-input
            v-model:value="commentText"
            :placeholder="placeholder"
            :disabled="!canComment"
            @pressEnter="handleSend"
          >
            <template #suffix>
              <a-button type="primary" size="small" :disabled="!canComment" :loading="sending" @click="handleSend">
                发送
              </a-button>
            </template>
          </a-input>
        </slot>
      </div>
    </a-tooltip>
    <a-modal v-model:open="blockModalVisible" title="无法评论" :footer="null">
      <a-result :status="blockResultStatus" :title="blockTitle" :sub-title="blockMessage">
        <template #extra>
          <a-button type="primary" @click="blockModalVisible = false">知道了</a-button>
        </template>
      </a-result>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue';
import { useStatusGuard } from '/@/composables/useStatusGuard';
import { useUserStore } from '/@/store/modules/user';
import { useUserStatusStore } from '/@/store/modules/userStatus';

const props = withDefaults(defineProps<{
  placeholder?: string;
}>(), {
  placeholder: '请输入评论...',
});

const emit = defineEmits<{
  (e: 'send', content: string): void;
}>();

const userStore = useUserStore();
const userStatusStore = useUserStatusStore();
const { canPerformAction, showBlockModal } = useStatusGuard();

const commentText = ref('');
const sending = ref(false);
const blockModalVisible = ref(false);
const showTooltip = ref(false);

const canComment = computed(() => canPerformAction('comment'));

const tooltipTitle = computed(() => {
  if (!canComment.value) {
    return '当前账号状态不允许评论';
  }
  return '';
});

const blockResultStatus = computed(() => {
  const status = userStatusStore.currentStatus;
  return status === 'FROZEN' || status === 'BANNED' ? 'error' : 'warning';
});

const blockTitle = computed(() => {
  const status = userStatusStore.currentStatus;
  if (status === 'FROZEN') return '账号已冻结';
  if (status === 'BANNED') return '账号已封禁';
  if (status === 'MUTED') return '账号已禁言';
  return '无法评论';
});

const blockMessage = computed(() => {
  const status = userStatusStore.currentStatus;
  if (status === 'FROZEN') return '您的账号已被冻结，无法进行评论操作。';
  if (status === 'BANNED') return '您的账号已被封禁，无法进行评论操作。';
  if (status === 'MUTED') return '您当前处于禁言状态，无法进行评论操作。';
  return '当前账号状态不允许评论。';
});

async function handleSend() {
  if (!canComment.value) {
    blockModalVisible.value = true;
    return;
  }
  if (!commentText.value.trim()) return;
  sending.value = true;
  try {
    emit('send', commentText.value);
    commentText.value = '';
  } finally {
    sending.value = false;
  }
}

onMounted(async () => {
  const userId = userStore.getUserInfo?.id;
  if (userId && !userStatusStore.currentStatus) {
    try {
      await userStatusStore.fetchCurrentStatus(userId as string);
    } catch {}
  }
});
</script>

<style scoped>
.guard-disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
