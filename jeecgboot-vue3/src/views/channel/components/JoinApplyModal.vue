<!-- jeecgboot-vue3/src/views/channel/components/JoinApplyModal.vue -->
<template>
  <Modal
    v-model:open="visible"
    :title="`申请加入 ${channelName}`"
    :confirmLoading="submitting"
    :okButtonProps="{ disabled: !isValid }"
    @ok="handleSubmit"
    @cancel="visible = false"
  >
    <Form layout="vertical">
      <Form.Item label="申请理由" :validateStatus="validateStatus" :help="validateHelp">
        <Input.TextArea
          v-model:value="reason"
          :maxlength="200"
          :minlength="10"
          :rows="4"
          placeholder="请输入申请理由（10-200字）"
        />
        <div class="char-count">{{ reason.length }} / 200</div>
      </Form.Item>
    </Form>
  </Modal>
</template>

<script setup lang="ts">
  import { ref, computed } from 'vue';
  import { Modal, Form, Input } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { applyToJoin } from '/@/api/content/channelMember';

  const props = defineProps<{
    channelId: string;
    channelName: string;
  }>();

  const emit = defineEmits<{
    (e: 'applied'): void;
  }>();

  const { createMessage } = useMessage();
  const visible = ref(false);
  const reason = ref('');
  const submitting = ref(false);

  const isValid = computed(() => reason.value.length >= 10 && reason.value.length <= 200);
  const validateStatus = computed(() => {
    if (reason.value.length === 0) return '';
    return isValid.value ? 'success' : 'error';
  });
  const validateHelp = computed(() => {
    if (reason.value.length === 0) return '';
    if (reason.value.length < 10) return '申请理由至少 10 个字';
    if (reason.value.length > 200) return '申请理由不能超过 200 个字';
    return '';
  });

  async function handleSubmit() {
    if (!isValid.value) return;
    submitting.value = true;
    try {
      await applyToJoin({ channelId: props.channelId, reason: reason.value });
      createMessage.success('申请已提交');
      visible.value = false;
      reason.value = '';
      emit('applied');
    } catch {
      createMessage.error('申请提交失败，请重试');
    } finally {
      submitting.value = false;
    }
  }

  function open() {
    visible.value = true;
    reason.value = '';
  }

  defineExpose({ open });
</script>

<style scoped>
.char-count {
  text-align: right;
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}
</style>
