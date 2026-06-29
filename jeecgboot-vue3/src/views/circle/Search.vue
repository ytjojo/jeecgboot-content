<template>
  <div class="circle-search-page">
    <div class="search-container">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <a-button type="link" @click="goBack" aria-label="返回">
          <ArrowLeftOutlined />
        </a-button>
        <a-input-search
          v-model:value="keyword"
          placeholder="搜索圈子..."
          size="large"
          allow-clear
          enter-button="搜索"
          aria-label="搜索圈子"
          @search="handleSearch"
        />
      </div>

      <!-- 结果统计 -->
      <div v-if="!loading && !error && searched" class="search-stats">
        共 {{ total }} 个结果
      </div>

      <!-- 加载 -->
      <div v-if="loading" class="search-loading">
        <a-skeleton v-for="i in 4" :key="i" active avatar :paragraph="{ rows: 1 }" />
      </div>

      <!-- 错误 -->
      <a-result
        v-else-if="error"
        status="error"
        title="搜索暂时不可用"
        sub-title="请稍后重试"
      >
        <template #extra>
          <a-button type="primary" @click="goList">浏览公开圈子</a-button>
        </template>
      </a-result>

      <!-- 空结果 -->
      <a-empty
        v-else-if="searched && results.length === 0"
        description="未找到相关圈子"
      >
        <template #children>
          <a-button type="primary" @click="goList">浏览公开圈子</a-button>
        </template>
      </a-empty>

      <!-- 搜索结果列表 -->
      <div v-else-if="results.length > 0" class="search-results">
        <div v-for="item in results" :key="item.id" class="search-item" @click="goDetail(item.id)">
          <img :src="item.iconUrl" :alt="item.name" class="search-item-icon" />
          <div class="search-item-info">
            <h3 class="search-item-name" v-html="highlight(item.name)" />
            <p class="search-item-desc">{{ item.description }}</p>
            <div class="search-item-meta">
              <span>{{ item.memberCount }} 成员</span>
              <span v-if="item.category" class="meta-category">{{ item.category }}</span>
            </div>
          </div>
          <div class="search-item-action" @click.stop>
            <template v-if="item.joined">
              <a-button disabled size="small">已加入</a-button>
            </template>
            <template v-else>
              <a-button type="primary" size="small" :loading="joiningIds.has(item.id)" @click="handleJoin(item)">
                加入
              </a-button>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowLeftOutlined } from '@ant-design/icons-vue';
import { searchCircle, joinCircle } from '/@/api/content/circle';
import { useMessage } from '/@/hooks/web/useMessage';
import type { CircleSearchResultVO } from '/@/api/content/model/circleModel';

const route = useRoute();
const router = useRouter();
const { createMessage } = useMessage();

const keyword = ref('');
const results = ref<CircleSearchResultVO[]>([]);
const loading = ref(false);
const error = ref(false);
const searched = ref(false);
const total = ref(0);
const joiningIds = ref(new Set<string>());

// URL 参数读取关键词（立即搜索）
watch(() => route.query.q, (val) => {
  if (val) {
    keyword.value = val as string;
    doSearch(keyword.value, true);
  }
}, { immediate: true });

// 输入变化防抖搜索
watch(keyword, (val) => {
  if (val && val.trim() && searched.value) {
    router.replace({ query: { q: val } });
    doSearch(val);
  }
});

let debounceTimer: ReturnType<typeof setTimeout> | null = null;
const DEBOUNCE_MS = 300;

function handleSearch(val?: string) {
  const term = val || keyword.value;
  if (!term.trim()) return;
  keyword.value = term;
  router.replace({ query: { q: term } });
  if (debounceTimer) {
    clearTimeout(debounceTimer);
    debounceTimer = null;
  }
  doSearch(term, true);
}

function doSearch(term: string, immediate = false) {
  if (debounceTimer) {
    clearTimeout(debounceTimer);
    debounceTimer = null;
  }

  const executeSearch = () => {
    debounceTimer = null;
    loading.value = true;
    error.value = false;
    searched.value = true;

    searchCircle({ keyword: term, pageNum: 1, pageSize: 20 })
      .then((res) => {
        results.value = res?.records || [];
        total.value = res?.total || 0;
      })
      .catch(() => {
        error.value = true;
      })
      .finally(() => {
        loading.value = false;
      });
  };

  if (immediate) {
    executeSearch();
  } else {
    debounceTimer = setTimeout(executeSearch, DEBOUNCE_MS);
  }
}

onUnmounted(() => {
  if (debounceTimer) {
    clearTimeout(debounceTimer);
    debounceTimer = null;
  }
});

function highlight(text: string): string {
  if (!keyword.value) return text;
  const regex = new RegExp(`(${escapeRegExp(keyword.value)})`, 'gi');
  return text.replace(regex, '<span class="highlight">$1</span>');
}

function escapeRegExp(str: string): string {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

async function handleJoin(item: CircleSearchResultVO) {
  joiningIds.value.add(item.id);
  try {
    await joinCircle({ circleId: item.id });
    createMessage.success('加入成功');
    item.joined = true;
  } catch (error: any) {
    createMessage.error(error?.message || '加入失败');
  } finally {
    joiningIds.value.delete(item.id);
  }
}

function goDetail(id: string) {
  router.push(`/circle/${id}`);
}

function goList() {
  router.push('/circle/list');
}

function goBack() {
  router.back();
}
</script>

<style lang="less" scoped>
.circle-search-page {
  min-height: calc(100vh - 64px);
  padding: 16px 24px;
  background: var(--background-color-base, #f5f5f5);
}

.search-container {
  max-width: 700px;
  margin: 0 auto;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.search-stats {
  font-size: 13px;
  color: var(--text-color-secondary, #666);
  margin-bottom: 12px;
}

.search-loading {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  background: var(--component-background, #fff);
  border-radius: 12px;
}

.search-results {
  display: flex;
  flex-direction: column;
  gap: 0;
  background: var(--component-background, #fff);
  border-radius: 12px;
  overflow: hidden;
}

.search-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid var(--border-color-base, #f0f0f0);

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: var(--background-color-base, #fafafa);
  }

  &-icon {
    width: 48px;
    height: 48px;
    border-radius: 10px;
    object-fit: cover;
    flex-shrink: 0;
  }

  &-info {
    flex: 1;
    min-width: 0;
  }

  &-name {
    font-size: 15px;
    font-weight: 600;
    margin: 0 0 4px;

    :deep(.highlight) {
      color: #1890ff;
      background: #e6f7ff;
      padding: 1px 2px;
      border-radius: 2px;
    }
  }

  &-desc {
    font-size: 13px;
    color: var(--text-color-secondary, #666);
    margin: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &-meta {
    display: flex;
    gap: 8px;
    font-size: 12px;
    color: var(--text-color-tertiary, #999);
    margin-top: 4px;

    .meta-category {
      padding: 1px 6px;
      background: var(--background-color-base, #f5f5f5);
      border-radius: 4px;
    }
  }

  &-action {
    flex-shrink: 0;
  }
}

@media (max-width: 768px) {
  .circle-search-page {
    padding: 8px;
  }

  .search-item {
    padding: 12px;

    &-icon {
      width: 40px;
      height: 40px;
    }
  }
}
</style>
