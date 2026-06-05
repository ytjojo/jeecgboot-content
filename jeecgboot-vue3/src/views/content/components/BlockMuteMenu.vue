<template>
  <a-dropdown :trigger="['click']" :placement="placement">
    <slot>
      <a-button type="text" size="small">
        <template #icon><MoreOutlined /></template>
      </a-button>
    </slot>
    <template #overlay>
      <a-menu @click="handleMenuClick">
        <a-menu-item v-if="!isSelf" key="block">
          <span class="block-mute-menu-item danger">拉黑该用户</span>
        </a-menu-item>
        <a-menu-item v-if="!isSelf" key="mute">
          <span class="block-mute-menu-item">屏蔽该用户</span>
        </a-menu-item>
      </a-menu>
    </template>
  </a-dropdown>

  <BlockConfirmModal
    v-model:open="blockModalVisible"
    :target-user-id="targetUserId"
    @success="$emit('blocked')"
  />
  <MuteConfirmModal
    v-model:open="muteModalVisible"
    :target-user-id="targetUserId"
    @success="$emit('muted')"
  />
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { MoreOutlined } from '@ant-design/icons-vue';
import BlockConfirmModal from './BlockConfirmModal.vue';
import MuteConfirmModal from './MuteConfirmModal.vue';

const props = defineProps({
  targetUserId: { type: String, required: true },
  currentUserId: { type: String, required: true },
  placement: { type: String, default: 'bottomRight' },
});

defineEmits<{
  blocked: [];
  muted: [];
}>();

const isSelf = computed(() => props.currentUserId === props.targetUserId);

const blockModalVisible = ref(false);
const muteModalVisible = ref(false);

function handleMenuClick({ key }: { key: string }) {
  if (key === 'block') {
    blockModalVisible.value = true;
  } else if (key === 'mute') {
    muteModalVisible.value = true;
  }
}
</script>

<style scoped>
.block-mute-menu-item {
  display: inline-block;
  min-width: 100px;
}
.block-mute-menu-item.danger {
  color: #ff4d4f;
}
</style>
