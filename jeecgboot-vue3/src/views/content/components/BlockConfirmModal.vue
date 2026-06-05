<template>
  <a-modal
    v-model:open="open"
    title="确认拉黑"
    ok-text="确认拉黑"
    cancel-text="取消"
    :ok-button-props="{ danger: true }"
    :confirm-loading="submitting"
    @ok="handleConfirm"
    @cancel="handleCancel"
  >
    <p>拉黑后，双方将无法查看对方的主页、内容和评论，已有的关注关系将自动解除。确定拉黑？</p>
  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { message } from 'ant-design-vue';
import { useBlockMuteStore } from '/@/store/modules/blockMute';

const props = defineProps({
  targetUserId: { type: String, required: true },
});

const open = defineModel<boolean>('open', { default: false });
const emit = defineEmits<{ success: [] }>();

const blockMuteStore = useBlockMuteStore();
const submitting = ref(false);

async function getCurrentUserId(): Promise<string> {
  const { useUserStore } = await import('/@/store/modules/user');
  const userStore = useUserStore();
  return String((userStore.userInfo as any)?.id || (userStore.userInfo as any)?.userId || '');
}

async function handleConfirm() {
  submitting.value = true;
  try {
    const userId = await getCurrentUserId();
    await blockMuteStore.blockUser(userId, props.targetUserId);
    message.success('已拉黑');
    open.value = false;
    emit('success');
  } catch (e: any) {
    message.error(e?.message || '操作失败，请重试');
  } finally {
    submitting.value = false;
  }
}

function handleCancel() {
  open.value = false;
}
</script>
