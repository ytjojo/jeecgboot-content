<template>
  <a-modal
    :visible="visible"
    :title="titleConfig.title"
    :confirm-loading="loading"
    :mask-closable="false"
    :ok-text="titleConfig.okText"
    :ok-type="titleConfig.okType"
    @ok="$emit('confirm')"
    @cancel="handleCancel"
  >
    <div class="governance-confirm">
      <a-alert
        v-if="titleConfig.warning"
        :message="titleConfig.warning"
        type="warning"
        show-icon
        class="confirm-warning"
      />
      <p class="confirm-text">{{ titleConfig.message }}</p>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';

const props = defineProps<{
  visible: boolean;
  type: 'set-moderator' | 'unset-moderator' | 'remove';
  memberName?: string;
}>();

const emit = defineEmits<{
  'update:visible': [value: boolean];
  confirm: [];
}>();

const loading = ref(false);

const titleConfig = computed(() => {
  const name = props.memberName || '该成员';
  switch (props.type) {
    case 'set-moderator':
      return {
        title: '设为版主',
        okText: '确认设置',
        okType: 'primary' as const,
        message: `确定要将 ${name} 设置为版主吗？`,
        warning: '版主可以管理成员和执行禁言操作',
      };
    case 'unset-moderator':
      return {
        title: '取消版主',
        okText: '确认取消',
        okType: 'warning' as const,
        message: `确定要取消 ${name} 的版主身份吗？`,
        warning: null,
      };
    case 'remove':
      return {
        title: '移除成员',
        okText: '确认移除',
        okType: 'danger' as const,
        message: `确定要移除 ${name} 吗？移除后可重新申请加入`,
        warning: '移除后该成员可重新申请加入',
      };
    default:
      return {
        title: '确认操作',
        okText: '确认',
        okType: 'primary' as const,
        message: '确定要执行此操作吗？',
        warning: null,
      };
  }
});

function handleCancel() {
  emit('update:visible', false);
}

function setLoading(val: boolean) {
  loading.value = val;
}

defineExpose({ setLoading });
</script>

<style lang="less" scoped>
.governance-confirm {
  .confirm-warning {
    margin-bottom: 12px;
  }

  .confirm-text {
    font-size: 14px;
    color: var(--text-color-secondary, #666);
    line-height: 1.6;
    margin: 0;
  }
}
</style>
