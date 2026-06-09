<template>
  <a-modal
    :visible="visible"
    title="禁言"
    :confirm-loading="loading"
    :mask-closable="false"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <a-form :model="form" layout="vertical">
      <a-form-item label="禁言时长" required>
        <a-radio-group v-model:value="form.duration">
          <a-radio-button value="1h" aria-label="1小时">1小时</a-radio-button>
          <a-radio-button value="24h" aria-label="24小时">24小时</a-radio-button>
          <a-radio-button value="7d" aria-label="7天">7天</a-radio-button>
          <a-radio-button value="forever" aria-label="永久">永久</a-radio-button>
        </a-radio-group>
      </a-form-item>
      <a-form-item label="禁言原因（可选）">
        <a-textarea
          v-model:value="form.reason"
          placeholder="请输入禁言原因"
          :maxlength="200"
          :rows="3"
          show-count
          aria-label="禁言原因"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref, reactive, watch } from 'vue';

defineProps<{
  visible: boolean;
  memberName?: string;
}>();

const emit = defineEmits<{
  'update:visible': [value: boolean];
  confirm: [data: { duration: string; reason: string }];
}>();

const loading = ref(false);

const form = reactive({
  duration: '1h',
  reason: '',
});

watch(() => props.visible, (val) => {
  if (val) {
    form.duration = '1h';
    form.reason = '';
    loading.value = false;
  }
});

function handleOk() {
  emit('confirm', {
    duration: form.duration,
    reason: form.reason,
  });
}

function handleCancel() {
  emit('update:visible', false);
}

function setLoading(val: boolean) {
  loading.value = val;
}

defineExpose({ setLoading });
</script>

<style lang="less" scoped>
:deep(.ant-radio-group) {
  display: flex;
  gap: 0;

  .ant-radio-button-wrapper {
    flex: 1;
    text-align: center;
  }
}
</style>
