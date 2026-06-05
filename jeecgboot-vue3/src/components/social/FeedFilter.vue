<template>
  <div class="feed-filter">
    <div class="feed-filter__label">动态类型：</div>
    <div class="feed-filter__buttons">
      <Button
        v-for="type in types"
        :key="type"
        :type="isSelected(type) ? 'primary' : 'default'"
        size="small"
        @click="toggleType(type)"
      >
        {{ typeLabelMap[type] || type }}
      </Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';

const props = defineProps<{
  types: string[];
  modelValue: string[];
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: string[]): void;
}>();

const typeLabelMap: Record<string, string> = {
  post: '发帖',
  like: '点赞',
  favorite: '收藏',
};

const selectedState = ref<string[]>([...props.modelValue]);

watch(
  () => props.modelValue,
  (val) => {
    selectedState.value = [...val];
  }
);

function isSelected(type: string): boolean {
  return selectedState.value.includes(type);
}

function toggleType(type: string) {
  const idx = selectedState.value.indexOf(type);
  if (idx > -1) {
    selectedState.value.splice(idx, 1);
  } else {
    selectedState.value.push(type);
  }
  emit('update:modelValue', [...selectedState.value]);
}
</script>

<style scoped lang="less">
.feed-filter {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;

  &__label {
    font-size: 13px;
    color: #666;
    margin-right: 12px;
    white-space: nowrap;
  }

  &__buttons {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }
}
</style>
