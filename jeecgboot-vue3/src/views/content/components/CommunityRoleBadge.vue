<template>
  <a-tag v-if="role && role !== 'NORMAL'" :color="roleColor" class="community-role-badge">
    {{ roleLabel }}
  </a-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps({
  role: {
    type: String,
    default: 'NORMAL',
  },
});

const roleColorMap: Record<string, string> = {
  CREATOR: 'gold',
  MODERATOR: 'green',
  ADMIN: 'red',
};

const roleLabelMap: Record<string, string> = {
  NORMAL: '',
  CREATOR: '创作者',
  MODERATOR: '版主',
  ADMIN: '管理员',
};

const roleColor = computed(() => roleColorMap[props.role] || 'default');
const roleLabel = computed(() => roleLabelMap[props.role] || '');
</script>

<style scoped>
.community-role-badge {
  display: inline-flex;
  align-items: center;
  font-size: 12px;
}
</style>
