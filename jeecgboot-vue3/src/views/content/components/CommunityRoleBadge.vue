<template>
  <a-popover v-if="showDetail && role && role !== 'NORMAL'" :title="roleLabel" overlayClassName="community-role-popover">
    <template #content>
      <div class="community-role-detail">
        <p>社区角色：{{ roleLabel }}</p>
        <p v-if="verified">已认证</p>
      </div>
    </template>
    <a-tag :color="roleColor" class="community-role-badge">
      {{ roleLabel }}
      <check-circle-outlined v-if="verified" class="community-role-badge__verified" />
    </a-tag>
  </a-popover>
  <a-tag v-else-if="role && role !== 'NORMAL'" :color="roleColor" class="community-role-badge">
    {{ roleLabel }}
    <check-circle-outlined v-if="verified" class="community-role-badge__verified" />
  </a-tag>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { CheckCircleOutlined } from '@ant-design/icons-vue';
import { SOCIAL_EVENTS, trackSocialEvent } from '/@/utils/social/analytics';

const props = withDefaults(
  defineProps<{
    role?: string;
    verified?: boolean;
    showDetail?: boolean;
  }>(),
  {
    role: 'NORMAL',
    verified: false,
    showDetail: false,
  },
);

onMounted(() => {
  if (props.role && props.role !== 'NORMAL') {
    trackSocialEvent(SOCIAL_EVENTS.COMMUNITY_ROLE_BADGE_SHOW, { role: props.role, verified: props.verified });
  }
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
.community-role-badge__verified {
  margin-left: 4px;
  color: #52c41a;
}
</style>
