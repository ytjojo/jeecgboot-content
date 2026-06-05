<template>
  <a-modal
    :visible="visible"
    :title="modalTitle"
    :confirmLoading="loading"
    :okText="okText"
    cancelText="取消"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <a-form :model="formState">
      <a-form-item v-if="actionType === 'warnUser'" label="警告级别">
        <a-radio-group v-model:value="formState.level">
          <a-radio value="low">轻微</a-radio>
          <a-radio value="medium">中等</a-radio>
          <a-radio value="high">严重</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item label="原因" name="reason" :rules="[{ required: true, message: '请输入操作原因' }]">
        <a-textarea
          v-model:value="formState.reason"
          placeholder="请输入操作原因"
          :rows="4"
          :maxlength="200"
          showCount
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue';

const props = withDefaults(
  defineProps<{
    visible?: boolean;
    actionType?: 'deleteComment' | 'warnUser';
    targetId?: string;
  }>(),
  {
    visible: false,
    actionType: 'deleteComment',
    targetId: '',
  },
);

const emit = defineEmits<{
  (e: 'confirm', data: { targetId: string; reason: string; level?: string }): void;
  (e: 'cancel'): void;
}>();

const loading = ref(false);
const formState = reactive({ reason: '', level: 'medium' });

const modalTitle = computed(() => (props.actionType === 'deleteComment' ? '删除评论' : '警告用户'));
const okText = computed(() => (props.actionType === 'deleteComment' ? '确认删除' : '确认警告'));

function handleOk() {
  if (!formState.reason.trim()) return;
  emit('confirm', {
    targetId: props.targetId,
    reason: formState.reason,
    level: props.actionType === 'warnUser' ? formState.level : undefined,
  });
  formState.reason = '';
  formState.level = 'medium';
}

function handleCancel() {
  emit('cancel');
}

defineExpose({ formState });
</script>
