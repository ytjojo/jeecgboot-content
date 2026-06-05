<template>
  <div class="subscribe-square-page">
    <div class="subscribe-square-page__header">
      <h2 class="subscribe-square-page__title">订阅广场</h2>
      <a-input-search
        v-model:value="searchKeyword"
        placeholder="搜索订阅源"
        :style="{ width: isMobile ? '100%' : '300px' }"
        allow-clear
        @search="handleSearch"
        @change="handleSearchChange"
      />
    </div>

    <div class="subscribe-square-page__categories">
      <a-tag
        v-for="cat in categories"
        :key="cat"
        :color="selectedCategory === cat ? 'blue' : 'default'"
        class="subscribe-square-page__category-tag"
        @click="handleCategoryClick(cat)"
      >
        {{ cat }}
      </a-tag>
    </div>

    <a-spin :spinning="loading">
      <div v-if="sourceList.length > 0" class="subscribe-square-page__list">
        <div
          v-for="item in sourceList"
          :key="item.sourceId"
          class="subscribe-square-page__card"
          @click="goToDetail(item.sourceId)"
        >
          <div class="subscribe-square-page__card-info">
            <div class="subscribe-square-page__card-icon">
              <img v-if="item.sourceIcon" :src="item.sourceIcon" :alt="item.sourceName" loading="lazy" />
              <AppstoreOutlined v-else />
            </div>
            <div class="subscribe-square-page__card-body">
              <div class="subscribe-square-page__card-name-row">
                <span class="subscribe-square-page__card-name">{{ item.sourceName }}</span>
                <a-tag v-if="item.category" color="cyan">{{ item.category }}</a-tag>
              </div>
              <div class="subscribe-square-page__card-meta">
                <span class="subscribe-square-page__card-count">
                  <UserOutlined />
                  {{ item.subscriberCount }} 人订阅
                </span>
              </div>
              <p v-if="item.description" class="subscribe-square-page__card-desc">{{ item.description }}</p>
            </div>
          </div>
          <div class="subscribe-square-page__card-action" @click.stop>
            <SubscribeButton
              :user-id="currentUserId"
              :source-id="item.sourceId"
              :source-type="item.sourceType"
              :is-subscribed="item.isSubscribed"
              @subscribe="handleSubscribed"
              @unsubscribe="handleUnsubscribed"
            />
          </div>
        </div>
      </div>
      <a-empty
        v-else-if="!loading"
        :description="emptyDescription"
      />
    </a-spin>

    <div v-if="hasMore && sourceList.length > 0" class="subscribe-square-page__load-more">
      <a-button :loading="loading" @click="loadMore">加载更多</a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { AppstoreOutlined, UserOutlined } from '@ant-design/icons-vue';
import { useSubscribeStore } from '/@/store/modules/subscribe';
import { useUserStore } from '/@/store/modules/user';
import { useBreakpoint } from '/@/hooks/event/useBreakpoint';
import SubscribeButton from '/@/components/social/SubscribeButton.vue';

interface PlazaItem {
  sourceId: string;
  sourceName: string;
  sourceIcon: string;
  sourceType: string;
  category: string;
  subscriberCount: number;
  description: string;
  isSubscribed: boolean;
}

const router = useRouter();
const subscribeStore = useSubscribeStore();
const userStore = useUserStore();
const { screenRef } = useBreakpoint();
const isMobile = computed(() => screenRef.value === 'XS' || screenRef.value === 'SM');

const currentUserId = computed(() => userStore.getUserInfo?.userId ?? '');
const searchKeyword = ref('');
const selectedCategory = ref('');
const sourceList = ref<PlazaItem[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(20);
const total = ref(0);
const hasMore = computed(() => sourceList.value.length < total.value);

const categories = ['科技', '娱乐', '体育', '生活', '教育', '其他'];

const emptyDescription = computed(() => {
  if (searchKeyword.value) return '未找到匹配的内容源';
  if (selectedCategory.value) return '该分类暂无订阅源';
  return '暂无订阅源';
});

let debounceTimer: ReturnType<typeof setTimeout> | null = null;

function handleSearch(value: string) {
  if (debounceTimer) {
    clearTimeout(debounceTimer);
    debounceTimer = null;
  }
  searchKeyword.value = value;
  fetchSources(true);
}

function handleSearchChange() {
  if (debounceTimer) clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => {
    fetchSources(true);
  }, 300);
}

function handleCategoryClick(category: string) {
  selectedCategory.value = selectedCategory.value === category ? '' : category;
  fetchSources(true);
}

async function fetchSources(reset = false) {
  if (reset) {
    currentPage.value = 1;
    sourceList.value = [];
  }
  loading.value = true;
  try {
    const res = await subscribeStore.fetchPlaza({
      keyword: searchKeyword.value || undefined,
      category: selectedCategory.value || undefined,
      page: currentPage.value,
      size: pageSize.value,
      sort: 'popularity',
    });
    const { records = [], total: t = 0 } = res;
    if (reset) {
      sourceList.value = records;
    } else {
      sourceList.value.push(...records);
    }
    total.value = t;
    currentPage.value++;
  } catch (error) {
    console.error('[SubscribeSquare] fetch plaza failed:', error);
  } finally {
    loading.value = false;
  }
}

function loadMore() {
  fetchSources(false);
}

function handleSubscribed() {
  fetchSources(true);
}

function handleUnsubscribed() {
  fetchSources(true);
}

function goToDetail(sourceId: string) {
  router.push({ path: '/social/subscribe/detail', query: { sourceId } });
}

onMounted(() => {
  fetchSources(true).catch(console.error);
});

onUnmounted(() => {
  if (debounceTimer) {
    clearTimeout(debounceTimer);
    debounceTimer = null;
  }
});
</script>

<style scoped lang="less">
.subscribe-square-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 16px;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
  }

  &__title {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }

  &__categories {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 20px;
  }

  &__category-tag {
    cursor: pointer;
    user-select: none;
  }

  &__list {
    border: 1px solid #f0f0f0;
    border-radius: 4px;
  }

  &__card {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    padding: 16px;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: background-color 0.2s;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background-color: #fafafa;
    }
  }

  &__card-info {
    display: flex;
    align-items: flex-start;
    flex: 1;
    min-width: 0;
  }

  &__card-icon {
    width: 40px;
    height: 40px;
    border-radius: 8px;
    background: #f5f5f5;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    margin-right: 12px;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .anticon {
      font-size: 20px;
      color: #999;
    }
  }

  &__card-body {
    flex: 1;
    min-width: 0;
  }

  &__card-name-row {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__card-name {
    font-size: 14px;
    font-weight: 500;
    color: #1a1a1a;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__card-meta {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-top: 4px;
  }

  &__card-count {
    font-size: 12px;
    color: #666;

    .anticon {
      margin-right: 4px;
    }
  }

  &__card-desc {
    font-size: 12px;
    color: #999;
    margin-top: 6px;
    margin-bottom: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__card-action {
    flex-shrink: 0;
    margin-left: 12px;
  }

  &__load-more {
    text-align: center;
    padding: 16px 0;
  }

  @media (max-width: 767px) {
    padding: 16px 12px;

    &__header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
    }

    &__card {
      flex-direction: column;
      gap: 12px;
    }

    &__card-action {
      align-self: flex-end;
      margin-left: 0;
    }
  }
}
</style>
