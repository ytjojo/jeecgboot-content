<template>
  <Modal :visible="visible" title="拒绝原因" @cancel="handleClose" @update:visible="(val: boolean) => emit('update:visible', val)" :width="480">
    <div class="reject-reason-modal">
      <div class="preset-reasons">
        <div class="preset-label">快捷选择：</div>
        <Space wrap>
          <Tag v-for="reason in presetReasons" :key="reason" class="preset-tag" @click="handlePresetClick(reason)">
            {{ reason }}
          </Tag>
        </Space>
      </div>
      <div class="custom-reason">
        <div class="reason-label">拒绝原因（必填，至少10字）：</div>
        <Input.TextArea
          v-model:value="reason"
          :rows="4"
          placeholder="请输入拒绝原因..."
          :status="showError ? 'error' : ''"
          @input="isPreset = false"
        />
        <div v-if="showError" class="error-text">拒绝原因至少需要10个字</div>
      </div>
    </div>
    <template #footer>
      <Button @click="handleClose">取消</Button>
      <Button class="confirm-btn" type="primary" danger @click="handleConfirm">
        确认拒绝
      </Button>
    </template>
  </Modal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { Modal, Input, Tag, Space, Button } from 'ant-design-vue';

const props = defineProps<{ visible: boolean }>();
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'confirm', reason: string): void;
}>();

const reason = ref('');
const showError = ref(false);
const isPreset = ref(false);

const presetReasons = ['违反社区规范', '内容重复', '与频道主题不符', '低质量内容', '涉嫌广告'];

const handlePresetClick = (preset: string) => {
  reason.value = preset;
  isPreset.value = true;
  showError.value = false;
};

const handleConfirm = () => {
  if (!isPreset.value && reason.value.length < 10) {
    showError.value = true;
    return;
  }
  emit('confirm', reason.value);
};

const handleClose = () => {
  emit('update:visible', false);
  reason.value = '';
  isPreset.value = false;
  showError.value = false;
};
</script>

<style lang="less" scoped>
.reject-reason-modal {
  .preset-reasons { margin-bottom: 16px;
    .preset-label { margin-bottom: 8px; color: #666; }
    .preset-tag { cursor: pointer; }
  }
  .custom-reason {
    .reason-label { margin-bottom: 8px; color: #666; }
    .error-text { color: #ff4d4f; font-size: 12px; margin-top: 4px; }
  }
}
</style>
