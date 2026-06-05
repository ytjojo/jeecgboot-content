<template>
  <div class="follow-button-wrapper">
    <Popconfirm
      v-if="isFollowingState && !isSelf"
      title="确定取消关注该用户吗？"
      ok-text="确定"
      cancel-text="取消"
      @confirm="handleUnfollow"
    >
      <Button
        :type="isFollowingState ? 'default' : 'primary'"
        :loading="loading"
        :disabled="disabled"
        @mouseenter="hovering = true"
        @mouseleave="hovering = false"
      >
        {{ buttonText }}
      </Button>
    </Popconfirm>
    <Button
      v-else
      :type="isFollowingState ? 'default' : 'primary'"
      :loading="loading"
      :disabled="disabled || isSelf"
      @click="handleFollow"
      @mouseenter="hovering = true"
      @mouseleave="hovering = false"
    >
      {{ buttonText }}
    </Button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onUnmounted } from 'vue';
import { message } from 'ant-design-vue';
import { useFollowStore } from '/@/store/modules/follow';

const props = defineProps<{
  userId: string;
  targetUserId: string;
  isFollowing: boolean;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:isFollowing', value: boolean): void;
  (e: 'follow'): void;
  (e: 'unfollow'): void;
}>();

const followStore = useFollowStore();
const loading = ref(false);
const hovering = ref(false);
const isFollowingState = ref(props.isFollowing);

watch(
  () => props.isFollowing,
  (val) => {
    isFollowingState.value = val;
  }
);

const isSelf = computed(() => props.userId === props.targetUserId);

const buttonText = computed(() => {
  if (isSelf.value) return '自己';
  if (isFollowingState.value && hovering.value) return '取消关注';
  if (isFollowingState.value) return '已关注';
  return '关注';
});

let debounceTimer: ReturnType<typeof setTimeout> | null = null;

function handleFollow() {
  if (debounceTimer) return;
  debounceTimer = setTimeout(() => {
    debounceTimer = null;
  }, 500);

  loading.value = true;
  const prev = isFollowingState.value;
  isFollowingState.value = true;
  emit('update:isFollowing', true);
  emit('follow');

  followStore
    .follow(props.userId, props.targetUserId)
    .then(() => {
      message.success('关注成功');
    })
    .catch(() => {
      isFollowingState.value = prev;
      emit('update:isFollowing', prev);
      message.error('关注失败');
    })
    .finally(() => {
      loading.value = false;
    });
}

function handleUnfollow() {
  if (debounceTimer) return;
  debounceTimer = setTimeout(() => {
    debounceTimer = null;
  }, 500);

  loading.value = true;
  const prev = isFollowingState.value;
  isFollowingState.value = false;
  emit('update:isFollowing', false);
  emit('unfollow');

  followStore
    .unfollow(props.userId, props.targetUserId)
    .then(() => {
      message.success('已取消关注');
    })
    .catch(() => {
      isFollowingState.value = prev;
      emit('update:isFollowing', prev);
      message.error('取消关注失败');
    })
    .finally(() => {
      loading.value = false;
    });
}

onUnmounted(() => {
  if (debounceTimer) clearTimeout(debounceTimer);
});
</script>

<style scoped lang="less">
.follow-button-wrapper {
  display: inline-block;
}
</style>
