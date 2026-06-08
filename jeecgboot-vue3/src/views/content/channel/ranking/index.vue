<template>
  <div class="ranking-page">
    <h2 class="ranking-page__title">频道排行榜</h2>

    <!-- 榜单类型切换 -->
    <div class="ranking-page__tabs">
      <a-tabs v-model:activeKey="rankingType" size="large" @change="handleTypeChange">
        <a-tab-pane key="hot" tab="热门榜" />
        <a-tab-pane key="new" tab="新晋榜" />
        <a-tab-pane key="system" tab="系统榜" />
      </a-tabs>
    </div>

    <!-- 榜单内容 -->
    <RankingList
      :data="rankingData"
      :dimension="dimension"
      :loading="loading"
      :show-dimension-switch="rankingType === 'hot'"
      :update-time="updateTime"
      :show-methodology="true"
      @dimension-change="handleDimensionChange"
      @methodology="showMethodology = true"
    />

    <!-- 加载更多 -->
    <div class="ranking-page__load-more">
      <a-button v-if="hasMore" :loading="loading" block @click="loadMore">加载更多</a-button>
    </div>

    <!-- 口径说明弹窗 -->
    <a-modal v-model:open="showMethodology" title="排行口径说明" :footer="null">
      <div v-if="rankingType === 'hot'">
        <p>热门榜根据频道在指定时间维度内的订阅增长、活跃度和内容互动综合计算排名。</p>
        <ul>
          <li>日榜：过去 24 小时的综合热度</li>
          <li>周榜：过去 7 天的综合热度</li>
          <li>月榜：过去 30 天的综合热度</li>
        </ul>
      </div>
      <div v-else-if="rankingType === 'new'">
        <p>新晋榜统计近 30 天内创建的新频道，按活跃度评分排名。</p>
      </div>
      <div v-else>
        <p>系统榜展示所有官方系统频道，按创建时间排序。</p>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue';
import { getHotRanking, getNewRanking, getSystemRanking } from '/@/api/content/channelDiscovery';
import type { ChannelRankingItemVO } from '/@/api/content/model/channelDiscoveryModel';
import RankingList from '../components/RankingList.vue';

const rankingType = ref('hot');
const dimension = ref('day');
const rankingData = ref<ChannelRankingItemVO[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const total = ref(0);
const updateTime = ref('');
const showMethodology = ref(false);

const pageSize = 20;
const hasMore = computed(() => rankingData.value.length < total.value);

onMounted(() => {
  fetchRanking();
});

function formatUpdateTime() {
  const now = new Date();
  updateTime.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
}

async function fetchRanking() {
  loading.value = true;
  try {
    let data;
    const params = { dimension: dimension.value as any, page: currentPage.value, pageSize };
    switch (rankingType.value) {
      case 'hot':
        data = await getHotRanking(params);
        break;
      case 'new':
        data = await getNewRanking({ page: currentPage.value, pageSize });
        break;
      case 'system':
        data = await getSystemRanking({ page: currentPage.value, pageSize });
        break;
    }
    if (data) {
      if (currentPage.value === 1) {
        rankingData.value = data.records || [];
      } else {
        rankingData.value = [...rankingData.value, ...(data.records || [])];
      }
      total.value = data.total || 0;
    }
    formatUpdateTime();
  } finally {
    loading.value = false;
  }
}

function handleTypeChange(type: string) {
  rankingType.value = type;
  currentPage.value = 1;
  rankingData.value = [];
  if (type === 'hot') {
    dimension.value = 'day';
  } else if (type === 'new') {
    dimension.value = 'month';
  }
  fetchRanking();
}

function handleDimensionChange(dim: string) {
  dimension.value = dim;
  currentPage.value = 1;
  rankingData.value = [];
  fetchRanking();
}

async function loadMore() {
  currentPage.value += 1;
  await fetchRanking();
}
</script>

<style lang="less" scoped>
.ranking-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 16px;

  &__title {
    font-size: 22px;
    font-weight: 600;
    text-align: center;
    margin-bottom: 16px;
  }

  &__tabs {
    :deep(.ant-tabs-nav) {
      margin-bottom: 0;
    }
  }

  &__load-more {
    text-align: center;
    padding: 24px 0;
  }
}

@media (max-width: 767px) {
  .ranking-page {
    padding: 8px;

    &__title {
      font-size: 18px;
    }
  }
}
</style>
