<template>
  <div class="mutual-follow-list">
    <div class="page-header">
      <h3>互关好友</h3>
      <a-input-search
        v-model:value="keyword"
        placeholder="搜索昵称"
        style="width: 240px"
        @search="handleSearch"
      />
    </div>

    <a-spin :spinning="loading">
      <a-list
        :data-source="list"
        :pagination="pagination"
        @change="handlePageChange"
      >
        <template #renderItem="{ item }">
          <a-list-item>
            <a-list-item-meta>
              <template #avatar>
                <a-avatar :src="item.avatar" />
              </template>
              <template #title>
                <span>{{ item.nickname }}</span>
                <MutualFollowBadge :mutual-follow="true" />
              </template>
            </a-list-item-meta>
            <template #actions>
              <a-popconfirm
                title="确定取消互关？取消后将移除互关关系"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleUnfollow(item)"
              >
                <a-button type="link" danger size="small">取消互关</a-button>
              </a-popconfirm>
            </template>
          </a-list-item>
        </template>
      </a-list>

      <a-empty v-if="!loading && list.length === 0" :description="keyword ? '未找到匹配的互关好友' : '暂无互关好友'" />
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue';
import { getMutualFollowList, unfollowUser } from '/@/api/content/relation';
import { useUserStore } from '/@/store/modules/user';
import { useMessage } from '/@/hooks/web/useMessage';
import MutualFollowBadge from '../components/MutualFollowBadge.vue';
import { SOCIAL_EVENTS, trackSocialEvent } from '/@/utils/social/analytics';

const userStore = useUserStore();
const { createMessage } = useMessage();
const keyword = ref('');
const loading = ref(false);
const list = ref<any[]>([]);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
});

const userId = String(userStore.getUserInfo.userId || '');

let debounceTimer: ReturnType<typeof setTimeout> | null = null;

watch(keyword, () => {
  if (debounceTimer) clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => {
    pagination.current = 1;
    fetchData();
  }, 300);
});

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getMutualFollowList(userId, {
      keyword: keyword.value || undefined,
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
    });
    list.value = res.records || [];
    pagination.total = res.total || 0;
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.current = 1;
  fetchData();
};

const handleUnfollow = async (item: any) => {
  try {
    await unfollowUser(userId, item.id);
    list.value = list.value.filter((r) => r.id !== item.id);
    pagination.total--;
    trackSocialEvent(SOCIAL_EVENTS.MUTUAL_FOLLOW_CANCEL, { targetUserId: item.id });
    createMessage.success('已取消互关');
  } catch {
    createMessage.error('取消互关失败');
  }
};

const handlePageChange = (page: any) => {
  pagination.current = page.current;
  pagination.pageSize = page.pageSize;
  fetchData();
};

onMounted(fetchData);
</script>

<style scoped>
.mutual-follow-list {
  padding: 16px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
</style>
