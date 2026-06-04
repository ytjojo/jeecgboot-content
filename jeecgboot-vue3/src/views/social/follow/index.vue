<template>
  <div class="follow-page">
    <div class="follow-page__header">
      <div class="follow-page__title-row">
        <h2 class="follow-page__title">我的关注</h2>
        <span class="follow-page__count">共 {{ followStore.totalFollows }} 人</span>
      </div>
      <div class="follow-page__toolbar">
        <a-input-search
          v-model:value="searchValue"
          placeholder="搜索关注的人"
          style="width: 300px"
          allow-clear
          @search="handleSearch"
          @change="handleSearchChange"
        />
        <a-radio-group v-model:value="selectedGroupId" button-style="solid" @change="handleGroupChange">
          <a-radio-button value="">全部</a-radio-button>
          <a-radio-button v-for="group in followStore.followGroups" :key="group.id" :value="group.id">
            {{ group.name }}
          </a-radio-button>
        </a-radio-group>
      </div>
      <div class="follow-page__actions">
        <a-button @click="goToBatchManage">批量管理</a-button>
        <a-button @click="goToGroupManage">分组管理</a-button>
        <a-button type="primary" @click="goToRecommend">推荐关注</a-button>
      </div>
    </div>

    <a-spin :spinning="followStore.followListLoading">
      <div v-if="followStore.followList.length > 0" class="follow-page__list">
        <UserCard
          v-for="item in followStore.followList"
          :key="item.id"
          :user-id="currentUserId"
          :user="{
            userId: item.userId,
            nickname: item.nickname,
            avatar: item.avatar,
            bio: item.bio,
            followTime: item.followTime,
            groupName: getGroupName(item.groupId),
            isSpecial: item.isSpecial,
          }"
          @unfollow="handleUnfollow"
          @special-change="handleSpecialChange"
        />
      </div>
      <a-empty v-else-if="!followStore.followListLoading" description="暂无关注的人">
        <a-button type="primary" @click="goToRecommend">去推荐关注</a-button>
      </a-empty>
    </a-spin>

    <div v-if="followStore.hasMore && followStore.followList.length > 0" class="follow-page__load-more">
      <a-button :loading="followStore.followListLoading" @click="loadMore">加载更多</a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useFollowStore } from '/@/store/modules/follow';
import { useUserStore } from '/@/store/modules/user';
import UserCard from '/@/components/social/UserCard.vue';

const router = useRouter();
const followStore = useFollowStore();
const userStore = useUserStore();

const currentUserId = computed(() => userStore.getUserInfo?.userId ?? '');
const searchValue = ref('');
const selectedGroupId = ref('');

let debounceTimer: ReturnType<typeof setTimeout> | null = null;

const groupMap = computed(() => {
  const map = new Map<string, string>();
  for (const g of followStore.followGroups) {
    map.set(g.id, g.name);
  }
  return map;
});

function getGroupName(groupId: string): string {
  if (!groupId) return '';
  return groupMap.value.get(groupId) ?? '';
}

function handleSearch(value: string) {
  if (debounceTimer) { clearTimeout(debounceTimer); debounceTimer = null; }
  followStore.setSearchKeyword(value);
  followStore.fetchFollowList(currentUserId.value, true);
}

function handleSearchChange() {
  if (debounceTimer) clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => {
    followStore.setSearchKeyword(searchValue.value);
    followStore.fetchFollowList(currentUserId.value, true);
  }, 300);
}

function handleGroupChange(value: string) {
  followStore.setSelectedGroupId(value);
  followStore.fetchFollowList(currentUserId.value, true);
}

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
  followStore.fetchFollowList(currentUserId.value);
}

function goToBatchManage() {
  router.push({ path: '/social/follow/batch' });
}

function goToGroupManage() {
  router.push({ path: '/social/follow/group' });
}

function goToRecommend() {
  router.push({ path: '/social/follow/recommend' });
}

onMounted(() => {
  followStore.fetchFollowGroups(currentUserId.value).catch(console.error);
  followStore.fetchFollowList(currentUserId.value, true).catch(console.error);
});

onUnmounted(() => {
  if (debounceTimer) { clearTimeout(debounceTimer); debounceTimer = null; }
});
</script>

<style scoped lang="less">
.follow-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 16px;

  &__header {
    margin-bottom: 24px;
  }

  &__title-row {
    display: flex;
    align-items: baseline;
    gap: 12px;
    margin-bottom: 16px;
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

  &__toolbar {
    display: flex;
    align-items: center;
    gap: 16px;
    flex-wrap: wrap;
    margin-bottom: 12px;
  }

  &__actions {
    display: flex;
    gap: 8px;
  }

  &__list {
    border: 1px solid #f0f0f0;
    border-radius: 4px;
  }

  &__load-more {
    text-align: center;
    padding: 16px 0;
  }
}
</style>
