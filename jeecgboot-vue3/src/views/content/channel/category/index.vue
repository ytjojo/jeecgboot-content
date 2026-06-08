<template>
  <div class="category-browse-page">
    <!-- 面包屑 -->
    <div class="category-browse-page__breadcrumb">
      <a-breadcrumb>
        <a-breadcrumb-item @click="handleBreadcrumbClick(null)">全部分类</a-breadcrumb-item>
        <a-breadcrumb-item v-for="item in breadcrumb" :key="item.id" @click="handleBreadcrumbClick(item)">
          {{ item.name }}
        </a-breadcrumb-item>
      </a-breadcrumb>
    </div>

    <div class="category-browse-page__layout">
      <!-- PC: 左侧分类树 -->
      <aside class="category-browse-page__sidebar">
        <CategoryTreeNav
          :categories="categoryStore.categoryTree"
          :selected-key="currentCategoryId"
          mode="browse"
          @select="handleCategorySelect"
        />
      </aside>

      <!-- 右侧频道列表 -->
      <main class="category-browse-page__main">
        <!-- 移动端分类选择 -->
        <div class="category-browse-page__mobile-category">
          <a-select
            v-if="isMobile"
            :value="currentCategoryId"
            placeholder="选择分类"
            style="width: 100%"
            @change="handleMobileCategoryChange"
          >
            <a-select-option v-for="cat in flatCategories" :key="cat.id" :value="cat.id">
              {{ cat.name }}
            </a-select-option>
          </a-select>
        </div>

        <!-- 筛选排序 -->
        <FilterPanel
          :filters="{ channelType: true, sortBy: true }"
          :values="filterValues"
          :sort-options="browseSortOptions"
          @change="handleFilterChange"
        />

        <!-- 频道列表 -->
        <a-spin :spinning="loading">
          <div v-if="channels.length === 0 && !loading" class="category-browse-page__empty">
            <a-empty description="该分类下暂无频道">
              <template #children>
                <a-space>
                  <a-button @click="handleBreadcrumbClick(null)">返回上级</a-button>
                  <a-button type="primary" @click="router.push('/channel/discovery')">浏览热门</a-button>
                  <a-button @click="router.push('/channel/search')">搜索频道</a-button>
                </a-space>
              </template>
            </a-empty>
          </div>

          <div v-else class="category-browse-page__grid">
            <div v-for="item in channels" :key="item.id">
              <ChannelCard :channel="item" mode="browse" :show-reason="false" />
            </div>
          </div>
        </a-spin>

        <!-- 加载更多 -->
        <div v-if="channels.length > 0" class="category-browse-page__load-more" ref="loadMoreRef">
          <a-button v-if="hasMore" :loading="loading" block @click="loadMore">加载更多</a-button>
          <span v-else class="category-browse-page__all-loaded">已加载全部</span>
        </div>
      </main>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useChannelCategoryStore } from '/@/store/modules/channelCategory';
import { getBrowseChannelList } from '/@/api/content/channelDiscovery';
import type { ChannelInfo, CategoryTreeVO } from '/@/api/content/model/channelDiscoveryModel';
import CategoryTreeNav from '../components/CategoryTreeNav.vue';
import ChannelCard from '../components/ChannelCard.vue';
import FilterPanel from '../components/FilterPanel.vue';

const router = useRouter();
const route = useRoute();
const categoryStore = useChannelCategoryStore();

const channels = ref<ChannelInfo[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const total = ref(0);
const currentCategoryId = ref<string | undefined>(undefined);
const filterValues = ref({
  channelType: undefined as string[] | undefined,
  sortBy: 'subscriber' as string,
});
const loadMoreRef = ref<HTMLElement | null>(null);

const isMobile = ref(false);
if (typeof window !== 'undefined') {
  isMobile.value = window.innerWidth < 768;
}

const pageSize = 20;
const hasMore = computed(() => channels.value.length < total.value);
const breadcrumb = computed(() => {
  if (!currentCategoryId.value) return [];
  return categoryStore.getCategoryPath(currentCategoryId.value);
});

const browseSortOptions = [
  { label: '订阅数', value: 'subscriber' },
  { label: '活跃度', value: 'active' },
  { label: '创建时间', value: 'created' },
];

const flatCategories = computed(() => {
  const result: { id: string; name: string }[] = [];
  const flatten = (nodes: CategoryTreeVO[], prefix = '') => {
    for (const node of nodes) {
      result.push({ id: node.id, name: prefix + node.name });
      if (node.children?.length) flatten(node.children, prefix + '  ');
    }
  };
  flatten(categoryStore.categoryTree);
  return result;
});

onMounted(async () => {
  await categoryStore.ensureCategoryTree();
  const categoryId = route.query.categoryId as string;
  if (categoryId) {
    currentCategoryId.value = categoryId;
  }
  await fetchChannels();
});

function handleCategorySelect(category: CategoryTreeVO) {
  currentCategoryId.value = category.id;
  currentPage.value = 1;
  channels.value = [];
  fetchChannels();
}

function handleMobileCategoryChange(value: string) {
  currentCategoryId.value = value;
  currentPage.value = 1;
  channels.value = [];
  fetchChannels();
}

function handleBreadcrumbClick(item: CategoryTreeVO | null) {
  currentCategoryId.value = item?.id;
  currentPage.value = 1;
  channels.value = [];
  fetchChannels();
}

function handleFilterChange(values: any) {
  filterValues.value = values;
  currentPage.value = 1;
  channels.value = [];
  fetchChannels();
}

async function fetchChannels() {
  loading.value = true;
  try {
    const data = await getBrowseChannelList({
      categoryId: currentCategoryId.value,
      channelType: filterValues.value.channelType?.[0],
      sortBy: filterValues.value.sortBy as any,
      page: currentPage.value,
      pageSize,
    });
    if (currentPage.value === 1) {
      channels.value = data.records || [];
    } else {
      channels.value = [...channels.value, ...(data.records || [])];
    }
    total.value = data.total || 0;
  } finally {
    loading.value = false;
  }
}

async function loadMore() {
  currentPage.value += 1;
  await fetchChannels();
}
</script>

<style lang="less" scoped>
.category-browse-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px;

  &__breadcrumb {
    margin-bottom: 16px;

    :deep(.ant-breadcrumb) {
      cursor: pointer;
    }
  }

  &__layout {
    display: flex;
    gap: 24px;
  }

  &__sidebar {
    width: 240px;
    flex-shrink: 0;
    background: #fff;
    border-radius: 8px;
    padding: 8px;
    max-height: calc(100vh - 120px);
    overflow-y: auto;
  }

  &__main {
    flex: 1;
    min-width: 0;
  }

  &__mobile-category {
    margin-bottom: 12px;
  }

  &__grid {
    display: grid;
    gap: 12px;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  }

  &__empty {
    padding: 48px 0;
  }

  &__load-more {
    text-align: center;
    padding: 24px 0;
  }

  &__all-loaded {
    color: #999;
    font-size: 13px;
  }
}

// 移动端
@media (max-width: 767px) {
  .category-browse-page {
    &__layout {
      flex-direction: column;
    }

    &__sidebar {
      display: none;
    }

    &__grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>
