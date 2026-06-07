<!-- jeecgboot-vue3/src/views/channel/members/RemoveMemberModal.vue -->
<template>
  <Modal v-model:open="visible" title="移除成员" :confirmLoading="loading" @ok="handleConfirm">
    <p>确认将 <strong>{{ memberName }}</strong> 移出频道？移除后 7 天内该用户无法再次加入。</p>
    <Form layout="vertical">
      <Form.Item label="移除原因" required>
        <Input.TextArea v-model:value="reason" :rows="3" placeholder="请输入移除原因" />
      </Form.Item>
    </Form>
  </Modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Modal, Form, Input } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { removeMembers } from '/@/api/content/channelMember';

  const props = defineProps<{ channelId: string }>();
  const emit = defineEmits<{ (e: 'removed'): void }>();

  const { createMessage } = useMessage();
  const visible = ref(false);
  const loading = ref(false);
  const memberIds = ref<string[]>([]);
  const memberName = ref('');
  const reason = ref('');

  function open(members: { id: string; nickname: string }[]) {
    memberIds.value = members.map((m) => m.id);
    memberName.value = members.map((m) => m.nickname).join('、');
    reason.value = '';
    visible.value = true;
  }

  async function handleConfirm() {
    if (!reason.value.trim()) return;
    loading.value = true;
    try {
      await removeMembers({ channelId: props.channelId, memberIds: memberIds.value, reason: reason.value });
      createMessage.success('成员已移除');
      visible.value = false;
      emit('removed');
    } finally {
      loading.value = false;
    }
  }

  defineExpose({ open });
</script>
