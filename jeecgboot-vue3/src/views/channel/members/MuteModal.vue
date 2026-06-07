<!-- jeecgboot-vue3/src/views/channel/members/MuteModal.vue -->
<template>
  <Modal v-model:open="visible" title="禁言成员" :confirmLoading="loading" @ok="handleConfirm">
    <Form layout="vertical">
      <!-- 注意：禁言时长值需与后端对齐，后端若使用小时格式则改为 1h/24h/168h/720h -->
      <Form.Item label="禁言时长" required>
        <Select v-model:value="duration" style="width: 100%">
          <Select.Option value="1h">1 小时</Select.Option>
          <Select.Option value="24h">24 小时</Select.Option>
          <Select.Option value="7d">7 天</Select.Option>
          <Select.Option value="30d">30 天</Select.Option>
          <Select.Option value="permanent">永久</Select.Option>
        </Select>
      </Form.Item>
      <Form.Item label="禁言原因" required>
        <Input.TextArea v-model:value="reason" :rows="3" placeholder="请输入禁言原因" />
      </Form.Item>
    </Form>
  </Modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Modal, Form, Input, Select } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { muteMember } from '/@/api/content/channelMember';

  const props = defineProps<{ channelId: string }>();
  const emit = defineEmits<{ (e: 'muted'): void }>();

  const { createMessage } = useMessage();
  const visible = ref(false);
  const loading = ref(false);
  const memberId = ref('');
  const duration = ref('1h');
  const reason = ref('');

  function open(member: { id: string; nickname: string }) {
    memberId.value = member.id;
    duration.value = '1h';
    reason.value = '';
    visible.value = true;
  }

  async function handleConfirm() {
    if (!reason.value.trim()) return;
    loading.value = true;
    try {
      await muteMember({ channelId: props.channelId, memberId: memberId.value, duration: duration.value, reason: reason.value });
      createMessage.success('已禁言');
      visible.value = false;
      emit('muted');
    } finally {
      loading.value = false;
    }
  }

  defineExpose({ open });
</script>
