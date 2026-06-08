<template>
  <div class="common-states">
    <!-- 加载骨架屏 -->
    <template v-if="loading">
      <a-skeleton v-if="type === 'card'" active :paragraph="{ rows: 3 }" />
      <a-skeleton v-else-if="type === 'list'" active :paragraph="{ rows: 5 }" />
      <a-skeleton v-else-if="type === 'detail'" active :paragraph="{ rows: 6 }" />
      <a-spin v-else :spinning="true" />
    </template>

    <!-- 错误状态 -->
    <a-result
      v-else-if="error"
      status="warning"
      :title="errorTitle || '加载失败'"
      :sub-title="errorSubtitle || '请检查网络后重试'"
    >
      <template #extra>
        <a-button type="primary" @click="$emit('retry')">重试</a-button>
      </template>
    </a-result>

    <!-- 空状态 -->
    <a-empty
      v-else-if="empty"
      :description="emptyDescription || '暂无数据'"
    >
      <template v-if="emptyActionText" #children>
        <a-button type="primary" @click="$emit('empty-action')">
          {{ emptyActionText }}
        </a-button>
      </template>
    </a-empty>

    <!-- 默认插槽 -->
    <slot v-else />
  </div>
</template>

<script lang="ts" setup>
interface Props {
  loading?: boolean;
  error?: boolean;
  empty?: boolean;
  errorTitle?: string;
  errorSubtitle?: string;
  emptyDescription?: string;
  emptyActionText?: string;
  type?: 'card' | 'list' | 'detail' | 'default';
}

withDefaults(defineProps<Props>(), {
  loading: false,
  error: false,
  empty: false,
  type: 'default',
});

defineEmits<{
  (e: 'retry'): void;
  (e: 'empty-action'): void;
}>();
</script>

<style lang="less" scoped>
.common-states {
  padding: 16px;

  :deep(.ant-result) {
    padding: 24px 0;
  }

  :deep(.ant-empty) {
    padding: 32px 0;
  }
}
</style>
