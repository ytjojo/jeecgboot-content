<template>
  <a-select
    :value="modelValue"
    :size="size"
    :style="{ width }"
    @change="handleChange"
  >
    <a-select-option value="PUBLIC">
      <eye-outlined /> 公开
    </a-select-option>
    <a-select-option value="MUTUAL_FOLLOW">
      <eye-invisible-outlined /> 仅互关可见
    </a-select-option>
  </a-select>
</template>

<script setup lang="ts">
import { EyeOutlined, EyeInvisibleOutlined } from '@ant-design/icons-vue';

withDefaults(defineProps<{
  modelValue?: string;
  size?: 'small' | 'middle' | 'large';
  width?: string;
}>(), {
  modelValue: 'PUBLIC',
  size: 'middle',
  width: '140px',
});

const emit = defineEmits<{ (e: 'update:modelValue', value: string): void }>();

function handleChange(value: string) {
  emit('update:modelValue', value);
}
</script>
