<template>
  <div class="blacklist-page">
    <a-page-header title="黑名单" sub-title="管理已拉黑的用户" @back="router.back()" />

    <a-input-search
      v-model:value="keyword"
      placeholder="搜索用户昵称"
      style="margin-bottom: 16px; max-width: 300px"
      @search="handleSearch"
      allow-clear
    />

    <a-list
      :data-source="filteredList"
      :loading="loading"
      :pagination="pagination"
      item-layout="horizontal"
    >
      <template #renderItem="{ item }">
        <a-list-item>
          <a-list-item-meta>
            <template #avatar>
              <a-avatar :src="item.avatar">{{ item.nickname?.charAt(0) }}</a-avatar>
            </template>
            <template #title>{{ item.nickname }}</template>
            <template #description>拉黑于 {{ item.blockedAt }}</template>
          </a-list-item-meta>
          <template #actions>
            <a-popconfirm
              title="解除拉黑后，您可以正常查看该用户内容，但之前的关注关系不会自动恢复。确定解除拉黑？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleUnblock(item)"
            >
              <a-button type="link" danger>解除拉黑</a-button>
            </a-popconfirm>
          </template>
        </a-list-item>
      </template>

      <template #empty>
        <a-empty description="暂无拉黑用户" />
      </template>
    </a-list>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { getBlacklist, unblockUser } from '/@/api/content/block';
import { useBlockMuteStore } from '/@/store/modules/blockMute';
import type { BlacklistItemVO } from '/@/api/content/block';

const router = useRouter();
const blockMuteStore = useBlockMuteStore();

const loading = ref(false);
const keyword = ref('');
const list = ref<BlacklistItemVO[]>([]);
const total = ref(0);
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  onChange: (page: number) => {
    pagination.current = page;
    loadList();
  },
});

const filteredList = computed(() => {
  if (!keyword.value) return list.value;
  return list.value.filter(item =>
    item.nickname.toLowerCase().includes(keyword.value.toLowerCase())
  );
});

async function getCurrentUserId(): Promise<string> {
  const { useUserStore } = await import('/@/store/modules/user');
  const userStore = useUserStore();
  return String((userStore.userInfo as any)?.id || (userStore.userInfo as any)?.userId || '');
}

async function loadList() {
  loading.value = true;
  try {
    const userId = await getCurrentUserId();
    const res = await getBlacklist(userId, pagination.current, pagination.pageSize);
    list.value = res.records;
    total.value = res.total;
    pagination.total = res.total;
  } catch {
    // ignore
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  // client-side filter via computed
}

async function handleUnblock(item: BlacklistItemVO) {
  try {
    const userId = await getCurrentUserId();
    await unblockUser(userId, item.userId);
    message.success('已解除拉黑');
    list.value = list.value.filter(i => i.userId !== item.userId);
    blockMuteStore.blacklistCount = Math.max(0, blockMuteStore.blacklistCount - 1);
  } catch (e: any) {
    message.error(e?.message || '操作失败');
  }
}

onMounted(loadList);
</script>

<style scoped>
.blacklist-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 16px;
}
</style>
