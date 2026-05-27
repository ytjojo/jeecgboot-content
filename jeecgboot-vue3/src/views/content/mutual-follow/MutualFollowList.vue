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
          </a-list-item>
        </template>
      </a-list>

      <a-empty v-if="!loading && list.length === 0" description="暂无互关好友" />
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { getMutualFollowList } from '/@/api/content/relation';
import MutualFollowBadge from '../components/MutualFollowBadge.vue';

const keyword = ref('');
const loading = ref(false);
const list = ref<any[]>([]);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
});

const userId = ''; // TODO: get from user store

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
