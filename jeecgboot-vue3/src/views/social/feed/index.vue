<template>
  <div class="feed-page">
    <div class="feed-page__header">
      <h2 class="feed-page__title">关注动态</h2>
      <a-button :loading="feedStore.followLoading" @click="handleRefresh">刷新</a-button>
    </div>

    <FeedFilter :types="['post', 'like', 'favorite']" :model-value="feedStore.followTypes" @update:model-value="handleTypesChange" />

    <SpecialFeed
      v-if="feedStore.priorityItems.length > 0"
      :feeds="feedStore.priorityItems"
      :loading="feedStore.followLoading"
      :has-more="false"
    />

    <a-spin :spinning="feedStore.followLoading && feedStore.followFeedList.length === 0">
      <div v-if="feedStore.followFeedList.length > 0" class="feed-page__list">
        <FeedCard
          v-for="feed in feedStore.followFeedList"
          :key="feed.id"
          :feed="feed"
          @click="handleFeedClick"
        />
        <div v-if="feedStore.followLoading" class="feed-page__loading">
          <a-spin />
        </div>
      </div>
      <a-empty v-else-if="!feedStore.followLoading" description="暂无关注动态" />
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue';
import { useFeedStore, FeedItem } from '/@/store/modules/feed';
import FeedCard from '/@/components/social/FeedCard.vue';
import FeedFilter from '/@/components/social/FeedFilter.vue';
import SpecialFeed from '/@/components/social/SpecialFeed.vue';

const feedStore = useFeedStore();

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
      if (!feedStore.followLoading && feedStore.followHasMore) {
        feedStore.fetchFollowFeed();
      }
    }
  }, 200);
}

function handleRefresh() {
  feedStore.fetchFollowFeed(true);
}

function handleTypesChange(types: string[]) {
  feedStore.setFollowTypes(types);
}

function handleFeedClick(feed: FeedItem) {
  window.open(`/social/content/detail?contentId=${feed.contentId}`, '_blank', 'noopener,noreferrer');
}

onMounted(() => {
  feedStore.fetchFollowFeed(true).catch(console.error);
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
.feed-page {
  max-width: 680px;
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
}
</style>
