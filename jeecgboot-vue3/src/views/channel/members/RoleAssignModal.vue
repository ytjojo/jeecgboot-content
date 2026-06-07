<!-- jeecgboot-vue3/src/views/channel/members/RoleAssignModal.vue -->
<template>
  <Modal v-model:open="visible" title="修改角色" :confirmLoading="loading" @ok="handleConfirm">
    <p>确认将 <strong>{{ memberName }}</strong> 的角色从 <Tag>{{ currentRole }}</Tag> 变更为：</p>
    <Select v-model:value="newRole" style="width: 100%">
      <Select.Option value="ADMIN">管理员</Select.Option>
      <Select.Option value="EDITOR">内容编辑</Select.Option>
      <Select.Option value="MEMBER">普通成员</Select.Option>
    </Select>
  </Modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Modal, Select, Tag } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { updateMemberRole } from '/@/api/content/channelMember';

  const props = defineProps<{ channelId: string }>();
  const emit = defineEmits<{ (e: 'updated'): void }>();

  const { createMessage } = useMessage();
  const visible = ref(false);
  const loading = ref(false);
  const memberId = ref('');
  const memberName = ref('');
  const currentRole = ref('');
  const newRole = ref('ADMIN');

  function open(member: any) {
    memberId.value = member.id;
    memberName.value = member.nickname;
    currentRole.value = member.role;
    newRole.value = member.role;
    visible.value = true;
  }

  async function handleConfirm() {
    loading.value = true;
    try {
      await updateMemberRole({ channelId: props.channelId, memberId: memberId.value, role: newRole.value });
      createMessage.success('角色已更新');
      visible.value = false;
      emit('updated');
    } finally {
      loading.value = false;
    }
  }

  defineExpose({ open });
</script>
