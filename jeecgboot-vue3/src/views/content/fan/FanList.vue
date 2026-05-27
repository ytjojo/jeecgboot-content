<template>
  <div class="fan-list">
    <div class="page-header">
      <h3>粉丝列表</h3>
      <a-space>
        <a-input-search
          v-model:value="keyword"
          placeholder="搜索昵称"
          style="width: 200px"
          @search="handleSearch"
        />
        <a-button @click="handleExport">
          <template #icon><download-outlined /></template>
          导出
        </a-button>
      </a-space>
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
              <template #title>{{ item.nickname }}</template>
              <template #description>关注于 {{ item.followedAt }}</template>
            </a-list-item-meta>
          </a-list-item>
        </template>
      </a-list>

      <a-empty v-if="!loading && list.length === 0" description="暂无粉丝" />
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { DownloadOutlined } from '@ant-design/icons-vue';
import { listFans, exportFansCsv } from '/@/api/content/fan-analytics';
import { useMessage } from '/@/hooks/web/useMessage';

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

const userId = ''; // TODO: get from user store

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await listFans(userId, {
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

const handleExport = async () => {
  try {
    const blob = await exportFansCsv(userId);
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `fans_${new Date().toISOString().slice(0, 10)}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
    createMessage.success('导出成功');
  } catch {
    createMessage.error('导出失败');
  }
};

onMounted(fetchData);
</script>

<style scoped>
.fan-list {
  padding: 16px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
</style>
