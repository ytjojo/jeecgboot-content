<template>
  <div class="private-content-guard" v-if="!accessible">
    <div class="guard-icon">
      <eye-invisible-outlined />
    </div>
    <p class="guard-message">{{ message }}</p>
    <a-button v-if="showFollowButton" type="primary" size="small" @click="$emit('follow')">
      重新关注
    </a-button>
  </div>
  <slot v-else />
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { EyeInvisibleOutlined } from '@ant-design/icons-vue';
import { SOCIAL_EVENTS, trackSocialEvent } from '/@/utils/social/analytics';

const props = withDefaults(defineProps<{
  accessible?: boolean;
  reason?: 'not_mutual_follow' | 'unfollowed' | 'no_permission';
  showFollowButton?: boolean;
}>(), {
  accessible: true,
  reason: 'no_permission',
  showFollowButton: false,
});

onMounted(() => {
  if (!props.accessible) {
    trackSocialEvent(SOCIAL_EVENTS.PRIVATE_CONTENT_ACCESS_DENIED, { reason: props.reason });
  }
});

defineEmits<{ (e: 'follow'): void }>();

const message = computed(() => {
  switch (props.reason) {
    case 'not_mutual_follow':
      return '该内容仅互关好友可见';
    case 'unfollowed':
      return '内容已不可见，互关后可重新查看';
    default:
      return '无权查看该内容';
  }
});
</script>

<style scoped>
.private-content-guard {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
  text-align: center;
  color: #999;
}
.guard-icon {
  font-size: 32px;
  margin-bottom: 12px;
  color: #d9d9d9;
}
.guard-message {
  font-size: 14px;
  margin-bottom: 12px;
}
</style>
