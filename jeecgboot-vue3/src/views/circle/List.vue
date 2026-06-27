<template>
  <div class="circle-list-page">
    <!-- 顶部操作栏 -->
    <div class="list-header">
      <div class="header-search">
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="搜索圈子..."
          allow-clear
          aria-label="搜索圈子"
          @search="handleSearch"
          @press-enter="handleSearch(searchKeyword)"
        />
      </div>
      <a-button type="primary" @click="goCreate" aria-label="创建圈子">
        <template #icon><PlusOutlined /></template>
        创建圈子
      </a-button>
    </div>

    <!-- Tab 切换 -->
    <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
      <a-tab-pane key="joined" tab="已加入" />
      <a-tab-pane key="discover" tab="发现" />
    </a-tabs>

    <!-- 已加入 Tab 内容 -->
    <template v-if="activeTab === 'joined'">
      <!-- 加载骨架屏 -->
      <div v-if="loading" class="circle-grid">
        <a-skeleton v-for="i in 6" :key="i" active avatar :paragraph="{ rows: 2 }" />
      </div>

      <!-- 空状态 -->
      <a-empty
        v-else-if="!loading && listData.length === 0"
        description="还没有加入任何圈子"
      >
        <a-button type="primary" @click="activeTab = 'discover'">
          发现圈子
        </a-button>
      </a-empty>

      <!-- 圈子卡片网格 -->
      <div v-else class="circle-grid">
        <CircleCard
          v-for="circle in listData"
          :key="circle.id"
          :circle="circle"
          @click="goDetail(circle.id)"
          @join-success="handleJoinSuccess"
          @governance="goGovernance"
        />
      </div>

      <!-- 加载更多 -->
      <div v-if="hasMore && !loading" class="load-more">
        <a-button :loading="loadingMore" @click="loadMore">加载更多</a-button>
      </div>

      <!-- 网络错误 -->
      <a-result
        v-if="error"
        status="error"
        title="加载失败，请重试"
      >
        <template #extra>
          <a-button type="primary" @click="fetchData">重试</a-button>
        </template>
      </a-result>
    </template>

    <!-- 发现 Tab 内容 -->
    <div v-else class="discover-content">
      <div class="discover-subtabs">
        <a-radio-group v-model:value="discoverSubTab" button-style="solid" size="small" @change="handleDiscoverSubTabChange">
          <a-radio-button value="recommend">为你推荐</a-radio-button>
          <a-radio-button value="hot">热门榜</a-radio-button>
          <a-radio-button value="new">最新圈</a-radio-button>
          <a-radio-button value="all">全部</a-radio-button>
        </a-radio-group>
      </div>

      <RecommendList v-if="discoverSubTab === 'recommend'" />
      <HotRankList v-else-if="discoverSubTab === 'hot'" />
      <NewRankList v-else-if="discoverSubTab === 'new'" />

      <!-- 全部公开圈子 -->
      <template v-else>
        <!-- 加载骨架屏 -->
        <div v-if="loading" class="circle-grid">
          <a-skeleton v-for="i in 6" :key="i" active avatar :paragraph="{ rows: 2 }" />
        </div>

        <!-- 空状态 -->
        <a-empty
          v-else-if="!loading && listData.length === 0"
          description="暂无公开圈子"
        />

        <!-- 圈子卡片网格 -->
        <div v-else class="circle-grid">
          <CircleCard
            v-for="circle in listData"
            :key="circle.id"
            :circle="circle"
            @click="goDetail(circle.id)"
            @join-success="handleJoinSuccess"
            @governance="goGovernance"
          />
        </div>

        <!-- 加载更多 -->
        <div v-if="hasMore && !loading" class="load-more">
          <a-button :loading="loadingMore" @click="loadMore">加载更多</a-button>
        </div>

        <!-- 网络错误 -->
        <a-result
          v-if="error"
          status="error"
          title="加载失败，请重试"
        >
          <template #extra>
            <a-button type="primary" @click="fetchData">重试</a-button>
          </template>
        </a-result>
      </template>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { PlusOutlined } from '@ant-design/icons-vue';
import { getMyCircleList, getPublicCircleList } from '/@/api/content/circle';
import type { CircleVO } from '/@/api/content/model/circleModel';
import CircleCard from './components/CircleCard.vue';
import RecommendList from './discovery/components/RecommendList.vue';
import HotRankList from './discovery/components/HotRankList.vue';
import NewRankList from './discovery/components/NewRankList.vue';
import { useCircleStoreWithOut } from '/@/store/modules/circle';

const router = useRouter();
const circleStore = useCircleStoreWithOut();

// 状态
const activeTab = ref<'joined' | 'discover'>('joined');
const discoverSubTab = ref<'recommend' | 'hot' | 'new' | 'all'>('recommend');
const searchKeyword = ref('');
const listData = ref<CircleVO[]>([]);
const loading = ref(false);
const loadingMore = ref(false);
const error = ref(false);

// 分页
const pageNum = ref(1);
const pageSize = 20;
const total = ref(0);
const hasMore = ref(false);

// 缓存时间戳
const cacheTimestamps: Record<string, number> = {};
const CACHE_TTL = 5 * 60 * 1000; // 5分钟

// 获取数据
async function fetchData(reset = true) {
  if (reset) {
    pageNum.value = 1;
    loading.value = true;
    error.value = false;
    listData.value = [];
  } else {
    loadingMore.value = true;
  }

  try {
    const params = { pageNum: pageNum.value, pageSize };
    const isJoined = activeTab.value === 'joined';
    const fetcher = isJoined ? getMyCircleList : getPublicCircleList;
    const result = await fetcher(params);

    if (result) {
      if (reset) {
        listData.value = result.records || [];
      } else {
        listData.value.push(...(result.records || []));
      }
      total.value = result.total || 0;
      hasMore.value = listData.value.length < total.value;
      const cacheKey = isJoined ? 'joined' : 'all';
      cacheTimestamps[cacheKey] = Date.now();
    }
  } catch {
    if (reset) {
      error.value = true;
    }
  } finally {
    loading.value = false;
    loadingMore.value = false;
  }
}

// 加载更多
function loadMore() {
  pageNum.value++;
  fetchData(false);
}

// Tab 切换
function handleTabChange(key: string) {
  const tab = key as 'joined' | 'discover';
  if (tab === 'discover') {
    discoverSubTab.value = 'recommend';
    return;
  }
  const now = Date.now();
  const cached = cacheTimestamps['joined'];
  if (!cached || (now - cached) > CACHE_TTL) {
    fetchData(true);
  }
}

// 发现子 Tab 切换
function handleDiscoverSubTabChange() {
  if (discoverSubTab.value === 'all') {
    const now = Date.now();
    const cached = cacheTimestamps['all'];
    if (!cached || (now - cached) > CACHE_TTL) {
      fetchData(true);
    }
  }
}

// 搜索
function handleSearch(keyword: string) {
  if (keyword.trim()) {
    circleStore.setSearchKeyword(keyword.trim());
    router.push({ path: '/circle/search', query: { q: keyword.trim() } });
  }
}

// 跳转
function goCreate() {
  router.push('/circle/create');
}

function goDetail(id: string) {
  router.push(`/circle/${id}`);
}

function goGovernance(id: string) {
  router.push(`/channel/${id}/governance`);
}

// 加入/退出后刷新两个 Tab
function handleJoinSuccess() {
  if (activeTab.value === 'joined' || (activeTab.value === 'discover' && discoverSubTab.value === 'all')) {
    fetchData(true);
  }
  delete cacheTimestamps['joined'];
  delete cacheTimestamps['all'];
}

// 初始化
fetchData(true);
</script>

<style lang="less" scoped>
.circle-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 24px;
  min-height: calc(100vh - 64px);
}

.list-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;

  .header-search {
    flex: 1;
    max-width: 360px;
  }
}

.circle-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-top: 16px;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }

  @media (min-width: 769px) and (max-width: 1199px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.load-more {
  text-align: center;
  margin-top: 24px;
  padding: 16px 0;
}

.discover-subtabs {
  margin-bottom: 16px;
  margin-top: 16px;
}

:deep(.ant-tabs) {
  margin-top: 0;

  .ant-tabs-nav {
    margin-bottom: 0;
  }
}
</style>
