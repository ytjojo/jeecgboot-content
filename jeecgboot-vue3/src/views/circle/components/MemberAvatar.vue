<template>
  <div class="member-avatar">
    <a-avatar :src="avatar" :size="size">
      {{ initial }}
    </a-avatar>
    <div class="member-avatar-info">
      <span class="member-avatar-name">{{ nickname }}</span>
      <a-tag v-if="roleTag" :color="roleTag.color" class="member-avatar-role">
        {{ roleTag.label }}
      </a-tag>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import type { MemberRole } from '/@/api/content/model/circleModel';

const props = withDefaults(defineProps<{
  avatar?: string;
  nickname?: string;
  role?: MemberRole | null;
  size?: number;
}>(), {
  avatar: '',
  nickname: '',
  role: null,
  size: 32,
});

const initial = computed(() => {
  return props.nickname ? props.nickname.charAt(0) : '?';
});

const roleTag = computed(() => {
  switch (props.role) {
    case 'CREATOR':
      return { label: '创建者', color: 'gold' };
    case 'MODERATOR':
      return { label: '版主', color: 'blue' };
    default:
      return null;
  }
});
</script>

<style lang="less" scoped>
.member-avatar {
  display: flex;
  align-items: center;
  gap: 8px;

  &-info {
    display: flex;
    align-items: center;
    gap: 6px;
    min-width: 0;
  }

  &-name {
    font-size: 14px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &-role {
    font-size: 11px;
    line-height: 1;
  }
}
</style>
