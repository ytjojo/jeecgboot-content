<template>
  <div class="special-follow-button-wrapper">
    <Tooltip v-if="!isFollowing" title="请先关注该用户">
      <Button type="text" disabled>
        <StarOutlined />
      </Button>
    </Tooltip>
    <Popconfirm
      v-else
      :title="isSpecialState ? '确定取消特别关注吗？' : '确定设为特别关注吗？'"
      ok-text="确定"
      cancel-text="取消"
      @confirm="handleToggle"
    >
      <Button type="text" :loading="loading">
        <StarFilled v-if="isSpecialState" class="special-star filled" />
        <StarOutlined v-else class="special-star" />
      </Button>
    </Popconfirm>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { message } from 'ant-design-vue';
import { StarFilled, StarOutlined } from '@ant-design/icons-vue';
import { useFollowStore } from '/@/store/modules/follow';

const props = defineProps<{
  userId: string;
  targetUserId: string;
  isFollowing: boolean;
  isSpecial: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:isSpecial', value: boolean): void;
  (e: 'special'): void;
  (e: 'cancelSpecial'): void;
}>();

const followStore = useFollowStore();
const loading = ref(false);
const isSpecialState = ref(props.isSpecial);

watch(
  () => props.isSpecial,
  (val) => {
    isSpecialState.value = val;
  }
);

function handleToggle() {
  loading.value = true;
  const prev = isSpecialState.value;
  const next = !prev;
  isSpecialState.value = next;
  emit('update:isSpecial', next);

  const action = next ? 'special' : 'cancelSpecial';
  emit(action);

  const storeMethod = next
    ? followStore.setSpecial(props.userId, props.targetUserId)
    : followStore.cancelSpecial(props.userId, props.targetUserId);

  storeMethod
    .then(() => {
      message.success(next ? '已设为特别关注' : '已取消特别关注');
    })
    .catch(() => {
      isSpecialState.value = prev;
      emit('update:isSpecial', prev);
      message.error(next ? '设置特别关注失败' : '取消特别关注失败');
    })
    .finally(() => {
      loading.value = false;
    });
}
</script>

<style scoped lang="less">
.special-follow-button-wrapper {
  display: inline-block;

  .special-star {
    font-size: 16px;
    color: #d9d9d9;
    transition: color 0.2s;

    &.filled {
      color: #faad14;
    }
  }
}
</style>
