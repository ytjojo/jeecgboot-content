<template>
  <div class="special-feed">
    <div class="special-feed__header">
      <StarFilled class="special-feed__icon" />
      <h3 class="special-feed__title">特别关注</h3>
    </div>
    <div class="special-feed__list">
      <FeedCard
        v-for="feed in feeds"
        :key="feed.id"
        :feed="feed"
        :is-mobile="isMobile"
        @click="handleFeedClick"
      />
      <div v-if="loading" class="special-feed__loading">
        <Spin />
      </div>
      <div
        v-if="!loading && feeds.length === 0"
        class="special-feed__empty"
      >
        暂无特别关注动态
      </div>
    </div>
    <div v-if="!loading && feeds.length > 0 && hasMore" class="special-feed__footer">
      <Button type="link" @click="handleLoadMore">
        加载更多
      </Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { StarFilled } from '@ant-design/icons-vue';
import type { FeedItem } from '/@/store/modules/feed';

defineProps<{
  feeds: FeedItem[];
  loading?: boolean;
  isMobile?: boolean;
  hasMore?: boolean;
}>();

const emit = defineEmits<{
  (e: 'loadMore'): void;
}>();

function handleFeedClick(feed: FeedItem) {
  window.open(`/content/${feed.contentId}`, '_blank', 'noopener,noreferrer');
}

function handleLoadMore() {
  emit('loadMore');
}
</script>

<style scoped lang="less">
.special-feed {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;

  &__header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    border-bottom: 1px solid #f0f0f0;
  }

  &__icon {
    color: #faad14;
    font-size: 16px;
  }

  &__title {
    font-size: 15px;
    font-weight: 500;
    color: #1a1a1a;
    margin: 0;
  }

  &__list {
    min-height: 100px;
  }

  &__loading {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 32px 0;
  }

  &__empty {
    text-align: center;
    padding: 32px 0;
    font-size: 13px;
    color: #999;
  }

  &__footer {
    text-align: center;
    padding: 8px 0;
    border-top: 1px solid #f0f0f0;
  }
}
</style>
