<template>
  <div class="help-search">
    <a-input-search
      v-model:value="keyword"
      placeholder="搜索帮助文章..."
      size="large"
      @search="handleSearch"
      @keydown.enter="handleSearch"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useDebounceFn } from '@vueuse/core';

const emit = defineEmits<{
  (e: 'search', keyword: string): void;
}>();

const keyword = ref('');
const debouncedSearch = useDebounceFn((val: string) => {
  if (val.trim()) {
    emit('search', val.trim());
  }
}, 300);

watch(keyword, (val) => {
  debouncedSearch(val);
});

const handleSearch = () => {
  if (keyword.value.trim()) {
    emit('search', keyword.value.trim());
  }
};
</script>

<style scoped lang="less">
.help-search {
  max-width: 600px;
  margin: 0 auto;
}
</style>
