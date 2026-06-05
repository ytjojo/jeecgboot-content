<template>
  <div class="special-follow-page">
    <div class="special-follow-page__header">
      <h2 class="special-follow-page__title">特别关注</h2>
      <span class="special-follow-page__count">共 {{ followStore.totalSpecialFollows }} 人</span>
    </div>

    <a-spin :spinning="followStore.specialFollowLoading">
      <div v-if="followStore.specialFollowList.length > 0" class="special-follow-page__list">
        <div v-for="item in followStore.specialFollowList" :key="item.id" class="special-follow-page__item">
          <UserCard
            :user-id="currentUserId"
            :user="{
              userId: item.userId,
              nickname: item.nickname,
              avatar: item.avatar,
              bio: item.bio,
              followTime: item.followTime,
              isSpecial: item.isSpecial,
              lastActiveTime: item.lastActiveTime,
            }"
            :is-mobile="isMobile"
            @unfollow="handleUnfollow"
            @special-change="handleSpecialChange"
          />
          <div v-if="item.latestActivityHint" class="special-follow-page__activity-hint">
            {{ item.latestActivityHint }}
          </div>
        </div>
      </div>
      <a-empty v-else-if="!followStore.specialFollowLoading" description="还没有设置特别关注">
        <p class="special-follow-page__guide">在关注列表中点击星标，即可将用户设为特别关注</p>
        <a-button type="primary" @click="goToFollowList">前往关注列表</a-button>
      </a-empty>
    </a-spin>

    <div v-if="followStore.specialHasMore && followStore.specialFollowList.length > 0" class="special-follow-page__load-more">
      <a-button :loading="followStore.specialFollowLoading" @click="loadMore">加载更多</a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useFollowStore } from '/@/store/modules/follow';
import { useUserStore } from '/@/store/modules/user';
import { useBreakpoint } from '/@/hooks/event/useBreakpoint';
import UserCard from '/@/components/social/UserCard.vue';

const router = useRouter();
const followStore = useFollowStore();
const userStore = useUserStore();
const { screenRef } = useBreakpoint();
const isMobile = computed(() => screenRef.value === 'XS' || screenRef.value === 'SM');

const currentUserId = computed(() => userStore.getUserInfo?.userId ?? '');

function handleUnfollow(targetUserId: string) {
  followStore.unfollow(currentUserId.value, targetUserId);
}

function handleSpecialChange(targetUserId: string, isSpecial: boolean) {
  if (isSpecial) {
    followStore.setSpecial(currentUserId.value, targetUserId);
  } else {
    followStore.cancelSpecial(currentUserId.value, targetUserId);
  }
}

function loadMore() {
  followStore.fetchSpecialFollowList(currentUserId.value);
}

function goToFollowList() {
  router.push({ path: '/social/follow' });
}

onMounted(() => {
  followStore.fetchSpecialFollowList(currentUserId.value, true).catch(console.error);
});
</script>

<style scoped lang="less">
.special-follow-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 16px;

  &__header {
    display: flex;
    align-items: baseline;
    gap: 12px;
    margin-bottom: 24px;
  }

  &__title {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }

  &__count {
    font-size: 14px;
    color: #999;
  }

  &__list {
    border: 1px solid #f0f0f0;
    border-radius: 4px;
  }

  &__item {
    position: relative;
  }

  &__activity-hint {
    padding: 4px 16px 8px 76px;
    font-size: 12px;
    color: #999;
  }

  &__guide {
    font-size: 14px;
    color: #666;
    margin-bottom: 16px;
  }

  &__load-more {
    text-align: center;
    padding: 16px 0;
  }

  @media (max-width: 767px) {
    padding: 16px 12px;

    &__activity-hint {
      padding: 4px 12px 8px 12px;
    }
  }
}
</style>
