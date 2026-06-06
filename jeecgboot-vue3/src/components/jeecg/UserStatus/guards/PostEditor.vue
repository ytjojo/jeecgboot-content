<template>
  <div class="post-editor-guard">
    <a-tooltip :title="tooltipTitle" :visible="showTooltip">
      <div :class="{ 'guard-disabled': !canPost }">
        <slot>
          <div class="post-editor-default">
            <a-textarea
              v-model:value="postContent"
              :placeholder="placeholder"
              :disabled="!canPost"
              :rows="4"
              :maxlength="2000"
              show-count
            />
            <div class="post-editor-actions">
              <a-button type="primary" :disabled="!canPost" :loading="sending" @click="handlePublish">
                发布
              </a-button>
            </div>
          </div>
        </slot>
      </div>
    </a-tooltip>
    <a-modal v-model:open="blockModalVisible" title="无法发布" :footer="null">
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
  placeholder: '分享你的想法...',
});

const emit = defineEmits<{
  (e: 'publish', content: string): void;
}>();

const userStore = useUserStore();
const userStatusStore = useUserStatusStore();
const { canPerformAction } = useStatusGuard();

const postContent = ref('');
const sending = ref(false);
const blockModalVisible = ref(false);
const showTooltip = ref(false);

const canPost = computed(() => canPerformAction('post'));

const tooltipTitle = computed(() => {
  if (!canPost.value) {
    return '当前账号状态不允许发布内容';
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
  if (status === 'RESTRICTED_RECOMMEND') return '账号受限';
  return '无法发布';
});

const blockMessage = computed(() => {
  const status = userStatusStore.currentStatus;
  if (status === 'FROZEN') return '您的账号已被冻结，无法发布内容。';
  if (status === 'BANNED') return '您的账号已被封禁，无法发布内容。';
  if (status === 'MUTED') return '您当前处于禁言状态，无法发布内容。';
  if (status === 'RESTRICTED_RECOMMEND') return '您的账号受到推荐限制，发布内容将不会被推荐。';
  return '当前账号状态不允许发布内容。';
});

async function handlePublish() {
  if (!canPost.value) {
    blockModalVisible.value = true;
    return;
  }
  if (!postContent.value.trim()) return;
  sending.value = true;
  try {
    emit('publish', postContent.value);
    postContent.value = '';
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
.post-editor-default {
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  padding: 12px;
}
.post-editor-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}
</style>
