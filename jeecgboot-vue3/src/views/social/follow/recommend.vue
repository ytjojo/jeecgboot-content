<template>
  <div class="recommend-page">
    <div class="recommend-page__header">
      <h2 class="recommend-page__title">推荐关注</h2>
    </div>

    <a-spin :spinning="followStore.recommendationsLoading">
      <div v-if="followStore.recommendations.length > 0" class="recommend-page__list">
        <div v-for="item in followStore.recommendations" :key="item.userId" class="recommend-page__item">
          <div class="recommend-page__user">
            <a-avatar :size="48" :src="item.avatar" />
            <div class="recommend-page__user-info">
              <span class="recommend-page__nickname">{{ item.nickname }}</span>
              <span v-if="item.bio" class="recommend-page__bio">{{ item.bio }}</span>
              <span v-if="item.reason" class="recommend-page__reason">{{ item.reason }}</span>
              <span v-if="item.mutualFollowCount" class="recommend-page__mutual">
                {{ item.mutualFollowCount }} 位共同关注
              </span>
            </div>
          </div>
          <div class="recommend-page__actions">
            <FollowButton
              :user-id="currentUserId"
              :target-user-id="item.userId"
              :is-following="false"
              @follow="handleFollowed"
            />
            <a-button type="link" size="small" @click="handleNotInterested(item.userId)">不感兴趣</a-button>
          </div>
        </div>
      </div>
      <a-empty v-else-if="!followStore.recommendationsLoading" description="暂无推荐" />
    </a-spin>

    <div v-if="followStore.recommendationsHasMore && followStore.recommendations.length > 0" class="recommend-page__load-more">
      <a-button :loading="followStore.recommendationsLoading" @click="loadMore">加载更多</a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useFollowStore } from '/@/store/modules/follow';
import { useUserStore } from '/@/store/modules/user';
import { useBreakpoint } from '/@/hooks/event/useBreakpoint';
import FollowButton from '/@/components/social/FollowButton.vue';

const followStore = useFollowStore();
const userStore = useUserStore();
const { screenRef } = useBreakpoint();
const isMobile = computed(() => screenRef.value === 'XS' || screenRef.value === 'SM');

const currentUserId = computed(() => userStore.getUserInfo?.userId ?? '');

function handleFollowed() {
  // FollowButton 内部已处理关注逻辑，这里刷新推荐列表
  followStore.fetchRecommendations(true);
}

function handleNotInterested(userId: string) {
  // 前端移除该推荐项（不感兴趣操作无需后端接口，直接从列表移除）
  followStore.dismissRecommendation(userId);
}

function loadMore() {
  followStore.fetchRecommendations();
}

onMounted(() => {
  followStore.fetchRecommendations(true).catch(console.error);
});
</script>

<style scoped lang="less">
.recommend-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 16px;

  &__header {
    margin-bottom: 24px;
  }

  &__title {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }

  &__list {
    border: 1px solid #f0f0f0;
    border-radius: 4px;
  }

  &__item {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    padding: 16px;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }
  }

  &__user {
    display: flex;
    align-items: flex-start;
    flex: 1;
    min-width: 0;
  }

  &__user-info {
    display: flex;
    flex-direction: column;
    margin-left: 12px;
    min-width: 0;
  }

  &__nickname {
    font-size: 14px;
    font-weight: 500;
    color: #1a1a1a;
  }

  &__bio {
    font-size: 12px;
    color: #666;
    margin-top: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__reason {
    font-size: 12px;
    color: #1890ff;
    margin-top: 4px;
  }

  &__mutual {
    font-size: 12px;
    color: #999;
    margin-top: 2px;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
    margin-left: 12px;
  }

  &__load-more {
    text-align: center;
    padding: 16px 0;
  }

  @media (max-width: 767px) {
    padding: 16px 12px;

    &__item {
      flex-direction: column;
      gap: 12px;
    }

    &__actions {
      align-self: flex-end;
      margin-left: 0;
    }
  }
}
</style>
