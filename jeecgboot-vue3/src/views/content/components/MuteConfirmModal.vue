<template>
  <a-modal
    v-model:open="open"
    title="确认屏蔽"
    ok-text="确认屏蔽"
    cancel-text="取消"
    :confirm-loading="submitting"
    @ok="handleConfirm"
    @cancel="handleCancel"
  >
    <p>屏蔽后，您将不再看到该用户在信息流中的内容，但仍可访问其主页。对方不受影响，关注关系保持不变。确定屏蔽？</p>
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
    await blockMuteStore.muteUser(userId, props.targetUserId);
    message.success('已屏蔽，该用户内容将不再出现在信息流中');
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
