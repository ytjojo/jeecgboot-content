<template>
  <div class="discovery-page">
    <!-- 搜索入口区 -->
    <div class="discovery-page__search">
      <SearchBar
        v-model="searchKeyword"
        placeholder="发现你感兴趣的频道"
        :hot-categories="hotSearchWords"
        @search="handleSearch"
      />
    </div>

    <a-spin :spinning="store.loading">
      <!-- 推荐频道区 -->
      <section v-if="store.recommendations.length > 0" class="discovery-page__section">
        <div class="discovery-page__section-header">
          <h3>为你推荐</h3>
        </div>
        <div class="discovery-page__recommend-grid">
          <div
            v-for="item in store.recommendations"
            :key="item.id"
            class="discovery-page__recommend-item"
          >
            <ChannelCard
              :channel="item"
              mode="recommend"
              :show-reason="true"
              :show-not-interested="true"
              @not-interested="handleNotInterested"
            />
          </div>
        </div>
      </section>

      <!-- 排行榜入口区 -->
      <section class="discovery-page__section">
        <div class="discovery-page__section-header">
          <h3>频道排行</h3>
          <a-button type="link" size="small" @click="router.push('/channel/ranking')">
            查看完整榜单 →
          </a-button>
        </div>
        <a-tabs v-model:activeKey="rankingTab" size="small">
          <a-tab-pane key="hot" tab="热门榜">
            <RankingList
              :data="store.hotRanking.slice(0, 5)"
              :loading="store.loading"
              :show-dimension-switch="false"
            />
          </a-tab-pane>
          <a-tab-pane key="new" tab="新晋榜">
            <RankingList
              :data="store.newRanking.slice(0, 5)"
              :loading="store.loading"
              :show-dimension-switch="false"
            />
          </a-tab-pane>
          <a-tab-pane key="system" tab="系统榜">
            <RankingList
              :data="store.systemRanking.slice(0, 5)"
              :loading="store.loading"
              :show-dimension-switch="false"
            />
          </a-tab-pane>
        </a-tabs>
      </section>

      <!-- 编辑精选区 -->
      <section v-if="store.editorialPicks.length > 0" class="discovery-page__section">
        <div class="discovery-page__section-header">
          <h3>编辑精选</h3>
        </div>
        <div class="discovery-page__picks-grid">
          <div
            v-for="pick in store.editorialPicks"
            :key="pick.id"
            class="discovery-page__pick-item"
          >
            <a-card size="small">
              <div class="discovery-page__pick-content">
                <img :src="pick.channelInfo.iconUrl" :alt="pick.channelInfo.name" loading="lazy" />
                <div class="discovery-page__pick-info">
                  <div class="discovery-page__pick-name">{{ pick.channelInfo.name }}</div>
                  <div class="discovery-page__pick-reason">{{ pick.recommendation }}</div>
                </div>
              </div>
            </a-card>
          </div>
        </div>
      </section>

      <!-- 分类入口区 -->
      <section v-if="store.categories.length > 0" class="discovery-page__section">
        <div class="discovery-page__section-header">
          <h3>分类浏览</h3>
          <a-button type="link" size="small" @click="router.push('/channel/category')">
            全部分类 →
          </a-button>
        </div>
        <div class="discovery-page__category-grid">
          <div
            v-for="cat in store.categories"
            :key="cat.id"
            class="discovery-page__category-item"
            @click="router.push(`/channel/category?categoryId=${cat.id}`)"
          >
            <div class="discovery-page__category-card">
              <span class="discovery-page__category-name">{{ cat.name }}</span>
              <span v-if="cat.channelCount !== undefined" class="discovery-page__category-count">
                {{ cat.channelCount }} 个频道
              </span>
            </div>
          </div>
        </div>
      </section>
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useChannelDiscoveryStore } from '/@/store/modules/channelDiscovery';
import { useUserStore } from '/@/store/modules/user';
import SearchBar from '../components/SearchBar.vue';
import ChannelCard from '../components/ChannelCard.vue';
import RankingList from '../components/RankingList.vue';

const router = useRouter();
const store = useChannelDiscoveryStore();
const userStore = useUserStore();

const searchKeyword = ref('');
const rankingTab = ref('hot');
const hotSearchWords = ref<string[]>([]);

onMounted(async () => {
  const isLoggedIn = !!userStore.getUserInfo?.id;
  if (isLoggedIn) {
    await store.fetchDiscoveryHome();
  } else {
    await store.fetchColdStart();
  }
});

function handleSearch(keyword: string) {
  router.push(`/channel/search?keyword=${encodeURIComponent(keyword)}`);
}

function handleNotInterested(channelId: string) {
  store.feedbackNotInterested(channelId);
}
</script>

<style lang="less" scoped>
.discovery-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px;

  &__search {
    max-width: 600px;
    margin: 0 auto 24px;
  }

  &__section {
    margin-bottom: 32px;
  }

  &__section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;

    h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
    }
  }

  &__recommend-grid {
    display: grid;
    gap: 12px;
  }

  &__picks-grid {
    display: grid;
    gap: 12px;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }

  &__pick-content {
    display: flex;
    gap: 12px;
    align-items: center;

    img {
      width: 40px;
      height: 40px;
      border-radius: 8px;
    }
  }

  &__pick-name {
    font-weight: 600;
    font-size: 14px;
  }

  &__pick-reason {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }

  &__category-grid {
    display: grid;
    gap: 12px;
  }

  &__category-item {
    cursor: pointer;
  }

  &__category-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 16px 8px;
    background: #fafafa;
    border-radius: 8px;
    transition: background 0.2s;

    &:hover {
      background: #e6f4ff;
    }
  }

  &__category-name {
    font-size: 14px;
    font-weight: 500;
  }

  &__category-count {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }
}

// 响应式布局
// xl: 推荐 4 列，分类 6 列
@media (min-width: 1200px) {
  .discovery-page {
    &__recommend-grid {
      grid-template-columns: repeat(4, 1fr);
    }

    &__category-grid {
      grid-template-columns: repeat(6, 1fr);
    }
  }
}

// lg: 推荐 4 列，分类 6 列
@media (min-width: 992px) and (max-width: 1199px) {
  .discovery-page {
    &__recommend-grid {
      grid-template-columns: repeat(4, 1fr);
    }

    &__category-grid {
      grid-template-columns: repeat(6, 1fr);
    }
  }
}

// md: 推荐 3 列，分类 4 列
@media (min-width: 768px) and (max-width: 991px) {
  .discovery-page {
    &__recommend-grid {
      grid-template-columns: repeat(3, 1fr);
    }

    &__category-grid {
      grid-template-columns: repeat(4, 1fr);
    }
  }
}

// sm: 推荐 2 列，分类 3 列
@media (min-width: 576px) and (max-width: 767px) {
  .discovery-page {
    &__recommend-grid {
      grid-template-columns: repeat(2, 1fr);
    }

    &__category-grid {
      grid-template-columns: repeat(3, 1fr);
    }
  }
}

// xs: 推荐横向滚动单卡，分类 2 列
@media (max-width: 575px) {
  .discovery-page {
    padding: 8px;

    &__recommend-grid {
      display: flex;
      overflow-x: auto;
      gap: 8px;
      padding-bottom: 8px;
      scroll-snap-type: x mandatory;

      & > * {
        min-width: 280px;
        scroll-snap-align: start;
      }
    }

    &__category-grid {
      grid-template-columns: repeat(2, 1fr);
    }
  }
}
</style>
