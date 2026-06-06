<template>
  <BasicModal v-bind="$attrs" title="确认解禁" @ok="handleOk" @cancel="handleCancel" :confirmLoading="loading">
    <a-form :model="formState" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
      <a-form-item label="解禁原因" required>
        <a-textarea v-model:value="formState.reason" placeholder="请输入解禁原因" :rows="3" />
      </a-form-item>
    </a-form>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue';
import { BasicModal } from '/@/components/Modal';
import { useUserStatusStore } from '/@/store/modules/userStatus';
import { useMessage } from '/@/hooks/web/useMessage';

const props = defineProps({
  userId: { type: String, required: true },
});

const emit = defineEmits(['success', 'cancel']);

const userStatusStore = useUserStatusStore();
const { createMessage } = useMessage();
const loading = ref(false);
const formState = reactive({ reason: '' });

async function handleOk() {
  if (!formState.reason.trim()) {
    createMessage.warning('请输入解禁原因');
    return;
  }
  loading.value = true;
  try {
    await userStatusStore.releaseUser(props.userId, formState.reason);
    createMessage.success('解禁成功');
    emit('success');
  } catch {
    createMessage.error('解禁失败');
  } finally {
    loading.value = false;
  }
}

function handleCancel() {
  formState.reason = '';
  emit('cancel');
}
</script>
