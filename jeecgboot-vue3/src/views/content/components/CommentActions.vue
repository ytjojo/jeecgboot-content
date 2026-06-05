<template>
  <div class="comment-actions">
    <CommunityRoleBadge
      v-if="comment.communityRole && comment.communityRole !== 'NORMAL'"
      :role="comment.communityRole"
      :verified="comment.roleVerified"
      size="small"
      inline
    />
    <template v-if="isModerator">
      <a-divider type="vertical" />
      <a-dropdown :trigger="['click']">
        <a-button type="link" size="small">
          <more-outlined />
        </a-button>
        <template #overlay>
          <a-menu @click="handleMenuClick">
            <a-menu-item key="deleteComment">
              <delete-outlined /> 删除评论
            </a-menu-item>
            <a-menu-item key="warnUser">
              <warning-outlined /> 警告用户
            </a-menu-item>
            <a-menu-item v-if="isAdmin" key="manageUser">
              <setting-outlined /> 前往用户管理
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { MoreOutlined, DeleteOutlined, WarningOutlined, SettingOutlined } from '@ant-design/icons-vue';
import CommunityRoleBadge from './CommunityRoleBadge.vue';
import { usePermission } from '/@/hooks/web/usePermission';
import { SOCIAL_EVENTS, trackSocialEvent } from '/@/utils/social/analytics';

interface CommentData {
  id: string;
  fromUserId: string;
  communityRole?: string;
  roleVerified?: boolean;
}

const props = defineProps<{
  comment: CommentData;
}>();

const emit = defineEmits<{
  (e: 'deleteComment', commentId: string): void;
  (e: 'warnUser', userId: string): void;
}>();

const router = useRouter();
const { hasPermission } = usePermission();

const isModerator = computed(() => hasPermission('content:moderator'));
const isAdmin = computed(() => hasPermission('content:admin'));

defineExpose({ handleMenuClick });

function handleMenuClick({ key }: { key: string }) {
  switch (key) {
    case 'deleteComment':
      trackSocialEvent(SOCIAL_EVENTS.MODERATOR_ACTION_EXECUTE, { action: 'deleteComment', commentId: props.comment.id });
      emit('deleteComment', props.comment.id);
      break;
    case 'warnUser':
      trackSocialEvent(SOCIAL_EVENTS.MODERATOR_ACTION_EXECUTE, { action: 'warnUser', userId: props.comment.fromUserId });
      emit('warnUser', props.comment.fromUserId);
      break;
    case 'manageUser':
      router.push({ path: `/system/user/${props.comment.fromUserId}` });
      break;
  }
}
</script>

<style scoped>
.comment-actions {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
</style>
