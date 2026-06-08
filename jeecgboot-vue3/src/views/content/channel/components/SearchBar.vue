<template>
  <div class="search-bar">
    <div class="search-bar__input">
      <a-input-search
        :value="modelValue"
        :placeholder="placeholder"
        size="large"
        allow-clear
        @search="handleSearch"
        @input="handleInput"
        @focus="handleFocus"
        @blur="handleBlur"
      />
    </div>

    <!-- 搜索历史 & 热门搜索词 下拉面板 -->
    <div v-if="showPanel && (searchHistory.length > 0 || hotCategories.length > 0)" class="search-bar__panel">
      <!-- 搜索历史 -->
      <div v-if="searchHistory.length > 0" class="search-bar__section">
        <div class="search-bar__section-header">
          <span>搜索历史</span>
          <a-button type="link" size="small" @click="handleClearHistory">清除</a-button>
        </div>
        <div class="search-bar__history-list">
          <a-tag
            v-for="item in searchHistory"
            :key="item"
            closable
            @close="handleRemoveHistory(item)"
            @click="handleHistoryClick(item)"
          >
            {{ item }}
          </a-tag>
        </div>
      </div>

      <!-- 热门搜索 -->
      <div v-if="hotCategories.length > 0" class="search-bar__section">
        <div class="search-bar__section-header">
          <span>热门搜索</span>
        </div>
        <div class="search-bar__hot-list">
          <a-tag v-for="item in hotCategories" :key="item" color="processing" @click="handleHotClick(item)">
            {{ item }}
          </a-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';

interface Props {
  modelValue?: string;
  placeholder?: string;
  hotCategories?: string[];
  searchHistory?: string[];
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '搜索频道',
  hotCategories: () => [],
  searchHistory: () => [],
});

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
  (e: 'search', keyword: string): void;
  (e: 'clear-history'): void;
  (e: 'remove-history', item: string): void;
}>();

const showPanel = ref(false);
let debounceTimer: ReturnType<typeof setTimeout> | null = null;

function handleInput(e: Event) {
  const value = (e.target as HTMLInputElement).value;
  emit('update:modelValue', value);

  // 300ms 防抖
  if (debounceTimer) clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => {
    if (value.trim()) {
      emit('search', value.trim());
    }
  }, 300);
}

function handleSearch(value: string) {
  if (debounceTimer) clearTimeout(debounceTimer);
  if (value.trim()) {
    emit('search', value.trim());
  }
}

function handleFocus() {
  showPanel.value = true;
}

function handleBlur() {
  // 延迟隐藏，让点击事件先触发
  setTimeout(() => {
    showPanel.value = false;
  }, 200);
}

function handleHistoryClick(item: string) {
  emit('update:modelValue', item);
  emit('search', item);
  showPanel.value = false;
}

function handleHotClick(item: string) {
  emit('update:modelValue', item);
  emit('search', item);
  showPanel.value = false;
}

function handleRemoveHistory(item: string) {
  emit('remove-history', item);
}

function handleClearHistory() {
  emit('clear-history');
}
</script>

<style lang="less" scoped>
.search-bar {
  position: relative;

  &__input {
    :deep(.ant-input-search) {
      .ant-input {
        border-radius: 8px;
      }
    }
  }

  &__panel {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    margin-top: 4px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    padding: 12px;
    z-index: 100;
  }

  &__section {
    & + & {
      margin-top: 12px;
      padding-top: 12px;
      border-top: 1px solid #f0f0f0;
    }
  }

  &__section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    font-size: 12px;
    color: #999;
  }

  &__history-list,
  &__hot-list {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;

    .ant-tag {
      cursor: pointer;
      margin: 0;
    }
  }
}
</style>
