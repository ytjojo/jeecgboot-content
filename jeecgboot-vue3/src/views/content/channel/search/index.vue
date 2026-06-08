<template>
  <div class="search-page">
    <!-- 搜索框 -->
    <div class="search-page__search">
      <SearchBar
        v-model="searchStore.keyword"
        placeholder="搜索频道"
        :search-history="searchStore.searchHistory"
        @search="handleSearch"
        @clear-history="searchStore.clearHistory()"
        @remove-history="searchStore.removeHistoryItem"
      />
    </div>

    <!-- 结果统计 -->
    <div v-if="searchStore.keyword && !searchStore.loading" class="search-page__stats">
      <span v-if="searchStore.total > 0">共找到 <strong>{{ searchStore.total }}</strong> 个频道</span>
    </div>

    <!-- 筛选面板 -->
    <FilterPanel
      :filters="{ channelType: true, category: true, sortBy: true }"
      :values="searchStore.filterValues"
      :categories="categoryStore.categoryTree"
      :sort-options="searchSortOptions"
      :collapsible="true"
      @change="handleFilterChange"
    />

    <!-- 搜索降级提示 -->
    <a-alert
      v-if="searchStore.error"
      type="warning"
      message="搜索服务繁忙，为您展示热门频道"
      closable
      style="margin-bottom: 16px"
      @close="searchStore.error = null"
    />

    <!-- 搜索结果列表 -->
    <a-spin :spinning="searchStore.loading">
      <div v-if="searchStore.isEmpty" class="search-page__empty">
        <a-empty description="未找到相关频道">
          <template #children>
            <a-space>
              <a-button @click="searchStore.clearFilters(); searchStore.executeSearch()">清除筛选</a-button>
              <a-button type="primary" @click="router.push('/channel/category')">浏览分类</a-button>
            </a-space>
          </template>
        </a-empty>
      </div>

      <div v-else-if="searchStore.results.length > 0">
        <!-- 结果反馈 -->
        <div v-if="!feedbackGiven" class="search-page__feedback">
          <span>搜索结果对你有帮助吗？</span>
          <a-button size="small" @click="handleFeedback(true)">有帮助</a-button>
          <a-button size="small" @click="handleFeedback(false)">没有</a-button>
        </div>

        <div class="search-page__grid">
          <div v-for="item in searchStore.results" :key="item.id">
            <ChannelCard
              :channel="item"
              mode="search"
              :highlight-name="item.highlightName"
              :match-reason="item.matchReason"
            />
          </div>
        </div>

        <!-- 加载更多 -->
        <div class="search-page__load-more">
          <a-button v-if="searchStore.hasMore" :loading="searchStore.loading" block @click="searchStore.loadMore()">
            加载更多
          </a-button>
        </div>
      </div>
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useChannelSearchStore } from '/@/store/modules/channelSearch';
import { useChannelCategoryStore } from '/@/store/modules/channelCategory';
import SearchBar from '../components/SearchBar.vue';
import ChannelCard from '../components/ChannelCard.vue';
import FilterPanel from '../components/FilterPanel.vue';

const router = useRouter();
const route = useRoute();
const searchStore = useChannelSearchStore();
const categoryStore = useChannelCategoryStore();

const feedbackGiven = ref(false);

const searchSortOptions = [
  { label: '相关度', value: 'relevance' },
  { label: '活跃度', value: 'active' },
  { label: '订阅数', value: 'subscriber' },
  { label: '创建时间', value: 'created' },
];

onMounted(async () => {
  await categoryStore.ensureCategoryTree();
  const keyword = route.query.keyword as string;
  if (keyword) {
    searchStore.keyword = keyword;
    await searchStore.executeSearch({ keyword });
  }
});

function handleSearch(keyword: string) {
  searchStore.keyword = keyword;
  searchStore.executeSearch({ keyword });
}

function handleFilterChange(values: any) {
  if (values.channelType !== undefined) {
    searchStore.setFilter('channelType', values.channelType?.[0] || '');
  }
  if (values.categoryId !== undefined) {
    searchStore.setFilter('categoryId', values.categoryId || '');
  }
  if (values.sortBy !== undefined) {
    searchStore.setFilter('sortBy', values.sortBy);
  }
}

async function handleFeedback(helpful: boolean) {
  const success = await searchStore.feedbackSearch(helpful);
  if (success) {
    feedbackGiven.value = true;
  }
}
</script>

<style lang="less" scoped>
.search-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 16px;

  &__search {
    margin-bottom: 16px;
  }

  &__stats {
    margin-bottom: 12px;
    font-size: 14px;
    color: #666;
  }

  &__feedback {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 16px;
    font-size: 13px;
    color: #666;
  }

  &__grid {
    display: grid;
    gap: 12px;
    grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  }

  &__empty {
    padding: 48px 0;
  }

  &__load-more {
    text-align: center;
    padding: 24px 0;
  }
}

@media (max-width: 767px) {
  .search-page {
    &__grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>
