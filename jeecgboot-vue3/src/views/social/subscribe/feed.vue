<template>
  <div class="subscribe-feed-page">
    <div class="subscribe-feed-page__header">
      <h2 class="subscribe-feed-page__title">订阅动态</h2>
      <a-radio-group v-model:value="selectedSourceType" button-style="solid" @change="handleSourceTypeChange">
        <a-radio-button v-for="tab in sourceTypeTabs" :key="tab.value" :value="tab.value">
          {{ tab.label }}
        </a-radio-button>
      </a-radio-group>
    </div>

    <a-spin :spinning="feedStore.subscribeLoading && feedStore.subscribeFeedList.length === 0">
      <div v-if="feedStore.subscribeFeedList.length > 0" class="subscribe-feed-page__list">
        <FeedCard
          v-for="feed in feedStore.subscribeFeedList"
          :key="feed.id"
          :feed="feed"
          :is-mobile="isMobile"
          @click="handleFeedClick"
        />
        <div v-if="feedStore.subscribeLoading" class="subscribe-feed-page__loading">
          <a-spin />
        </div>
      </div>
      <a-empty v-else-if="!feedStore.subscribeLoading" description="订阅你感兴趣的内容源">
        <a-button type="primary" @click="goToSquare">去订阅广场</a-button>
      </a-empty>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useFeedStore, FeedItem } from '/@/store/modules/feed';
import { useBreakpoint } from '/@/hooks/event/useBreakpoint';
import FeedCard from '/@/components/social/FeedCard.vue';

const router = useRouter();
const feedStore = useFeedStore();
const { screenRef } = useBreakpoint();
const isMobile = computed(() => screenRef.value === 'XS' || screenRef.value === 'SM');

const sourceTypeTabs = [
  { value: '', label: '全部' },
  { value: '专题', label: '专题' },
  { value: '话题', label: '话题' },
  { value: '栏目', label: '栏目' },
  { value: '频道', label: '频道' },
];

const selectedSourceType = ref('');

let scrollTimer: ReturnType<typeof setTimeout> | null = null;

function handleScroll() {
  if (scrollTimer) return;
  scrollTimer = setTimeout(() => {
    scrollTimer = null;
    const el = document.documentElement;
    const scrollTop = el.scrollTop || document.body.scrollTop;
    const scrollHeight = el.scrollHeight;
    const clientHeight = el.clientHeight;
    if (scrollHeight - scrollTop - clientHeight < 200) {
      if (!feedStore.subscribeLoading && feedStore.subscribeHasMore) {
        feedStore.fetchSubscribeFeed();
      }
    }
  }, 200);
}

function handleSourceTypeChange(value: string) {
  feedStore.setSubscribeSourceType(value);
}

function handleFeedClick(feed: FeedItem) {
  window.open(`/social/content/detail?contentId=${feed.contentId}`, '_blank', 'noopener,noreferrer');
}

function goToSquare() {
  router.push({ path: '/social/subscribe/square' });
}

onMounted(() => {
  feedStore.fetchSubscribeFeed(true).catch(console.error);
  window.addEventListener('scroll', handleScroll, { passive: true });
});

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
  if (scrollTimer) {
    clearTimeout(scrollTimer);
    scrollTimer = null;
  }
});
</script>

<style scoped lang="less">
.subscribe-feed-page {
  max-width: 680px;
  margin: 0 auto;
  padding: 24px 16px;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
    flex-wrap: wrap;
    gap: 12px;
  }

  &__title {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }

  &__list {
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
  }

  &__loading {
    display: flex;
    justify-content: center;
    padding: 24px 0;
  }

  @media (max-width: 767px) {
    padding: 16px 12px;

    &__header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
    }
  }
}
</style>
