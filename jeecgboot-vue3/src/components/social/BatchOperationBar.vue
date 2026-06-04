<template>
  <div v-if="selectedCount > 0" class="batch-operation-bar">
    <div class="batch-operation-bar__info">
      <span>已选择 {{ selectedCount }} 项</span>
    </div>
    <div class="batch-operation-bar__actions">
      <Popconfirm
        title="确定批量取消关注吗？"
        ok-text="确定"
        cancel-text="取消"
        @confirm="handleBatchUnfollow"
      >
        <Button :loading="loading">取消关注</Button>
      </Popconfirm>
      <Popconfirm
        title="确定批量暂停订阅吗？"
        ok-text="确定"
        cancel-text="取消"
        @confirm="handleBatchPause"
      >
        <Button :loading="loading">暂停</Button>
      </Popconfirm>
      <Popconfirm
        title="确定批量恢复订阅吗？"
        ok-text="确定"
        cancel-text="取消"
        @confirm="handleBatchResume"
      >
        <Button :loading="loading">恢复</Button>
      </Popconfirm>
      <Popconfirm
        title="确定批量取消订阅吗？"
        ok-text="确定"
        cancel-text="取消"
        @confirm="handleBatchCancel"
      >
        <Button :loading="loading">取消订阅</Button>
      </Popconfirm>
      <Button type="link" @click="handleCancel">取消选择</Button>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  selectedCount: number;
  loading?: boolean;
}>();

const emit = defineEmits<{
  (e: 'cancel'): void;
  (e: 'batchUnfollow'): void;
  (e: 'batchPause'): void;
  (e: 'batchResume'): void;
  (e: 'batchCancel'): void;
}>();

function handleCancel() {
  emit('cancel');
}

function handleBatchUnfollow() {
  emit('batchUnfollow');
}

function handleBatchPause() {
  emit('batchPause');
}

function handleBatchResume() {
  emit('batchResume');
}

function handleBatchCancel() {
  emit('batchCancel');
}
</script>

<style scoped lang="less">
.batch-operation-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
  z-index: 100;

  &__info {
    font-size: 14px;
    color: #1a1a1a;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}
</style>
