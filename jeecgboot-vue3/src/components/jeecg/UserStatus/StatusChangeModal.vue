<template>
  <BasicModal v-bind="$attrs" :title="title" @ok="handleOk" @cancel="handleCancel" :confirmLoading="loading">
    <a-form :model="formState" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
      <a-form-item label="目标状态" required>
        <a-select v-model:value="formState.toStatus" placeholder="请选择目标状态">
          <a-select-option v-for="s in transitions" :key="s" :value="s">{{ statusLabelMap[s] || s }}</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="变更原因" required>
        <a-textarea v-model:value="formState.reason" placeholder="请输入变更原因" :rows="3" />
      </a-form-item>
      <a-form-item v-if="showEndTime" label="到期时间">
        <a-date-picker v-model:value="formState.endTime" show-time placeholder="选择到期时间（可选）" style="width: 100%" />
      </a-form-item>
      <a-form-item label="备注">
        <a-textarea v-model:value="formState.remark" placeholder="备注信息（可选）" :rows="2" />
      </a-form-item>
    </a-form>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, reactive, watch, computed } from 'vue';
import { BasicModal } from '/@/components/Modal';
import { useUserStatusStore } from '/@/store/modules/userStatus';
import { useMessage } from '/@/hooks/web/useMessage';
import dayjs from 'dayjs';

const props = defineProps({
  userId: { type: String, required: true },
  currentStatus: { type: String, required: true },
});

const emit = defineEmits(['success', 'cancel']);

const userStatusStore = useUserStatusStore();
const { createMessage } = useMessage();
const loading = ref(false);
const transitions = ref<string[]>([]);

const formState = reactive({
  toStatus: undefined as string | undefined,
  reason: '',
  endTime: null as any,
  remark: '',
});

const statusLabelMap: Record<string, string> = {
  GUEST: '游客',
  REGISTERED_INCOMPLETE: '注册未完善',
  NORMAL: '正常',
  MUTED: '禁言',
  RESTRICTED_RECOMMEND: '限制推荐',
  FROZEN: '冻结',
  BANNED: '封禁',
  DEACTIVATING: '注销中',
  DEACTIVATED: '已注销',
};

const title = computed(() => `变更用户状态 - ${statusLabelMap[props.currentStatus] || props.currentStatus}`);
const showEndTime = computed(() => ['MUTED', 'RESTRICTED_RECOMMEND', 'FROZEN'].includes(formState.toStatus || ''));

watch(() => props.currentStatus, async (status) => {
  if (status) {
    transitions.value = await userStatusStore.fetchTransitions(status) || [];
  }
}, { immediate: true });

async function handleOk() {
  if (!formState.toStatus) {
    createMessage.warning('请选择目标状态');
    return;
  }
  if (!formState.reason.trim()) {
    createMessage.warning('请输入变更原因');
    return;
  }
  loading.value = true;
  try {
    await userStatusStore.changeStatus(props.userId, {
      toStatus: formState.toStatus,
      reason: formState.reason,
      endTime: formState.endTime ? dayjs(formState.endTime).format('YYYY-MM-DD HH:mm:ss') : undefined,
      remark: formState.remark || undefined,
    });
    createMessage.success('状态变更成功');
    emit('success');
  } catch {
    createMessage.error('状态变更失败');
  } finally {
    loading.value = false;
  }
}

function handleCancel() {
  formState.toStatus = undefined;
  formState.reason = '';
  formState.endTime = null;
  formState.remark = '';
  emit('cancel');
}
</script>
