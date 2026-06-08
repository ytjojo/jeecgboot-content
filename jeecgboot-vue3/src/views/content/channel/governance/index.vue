<template>
  <div class="channel-governance-page">
    <h2 class="channel-governance-page__title">频道治理</h2>

    <!-- 筛选条件 -->
    <a-card size="small" class="channel-governance-page__filter">
      <a-form layout="inline">
        <a-form-item label="频道名称">
          <a-input v-model:value="filters.channelName" allowClear placeholder="搜索频道" />
        </a-form-item>
        <a-form-item label="频道类型">
          <a-select v-model:value="filters.channelType" allowClear placeholder="全部" style="width: 120px">
            <a-select-option value="PUBLIC">公开</a-select-option>
            <a-select-option value="PRIVATE">私有</a-select-option>
            <a-select-option value="ORG">组织</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="filters.status" allowClear placeholder="全部" style="width: 140px">
            <a-select-option value="Active">运营中</a-select-option>
            <a-select-option value="ReadonlyFrozen">已冻结</a-select-option>
            <a-select-option value="Hidden">已隐藏</a-select-option>
            <a-select-option value="Archived">已归档</a-select-option>
            <a-select-option value="Merged">已合并</a-select-option>
            <a-select-option value="Closed">已关闭</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">查询</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 频道列表 -->
    <a-card size="small">
      <a-table
        :columns="columns"
        :dataSource="channelList"
        :loading="loading"
        :pagination="{ total, pageSize, current: currentPage, onChange: handlePageChange }"
        rowKey="channelId"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <StatusTag :status="record.status" size="small" />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button size="small" type="link" @click="handleViewDetail(record)">
              查看详情
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getGovernanceChannelList } from '/@/api/content/channel/lifecycle';
import type { GovernanceChannelVO, GovernanceListQuery } from '/@/api/content/channel/lifecycle';
import StatusTag from '/@/views/content/channel/components/StatusTag.vue';

const router = useRouter();
const channelList = ref<GovernanceChannelVO[]>([]);
const loading = ref(false);
const total = ref(0);
const pageSize = ref(20);
const currentPage = ref(1);

const filters = reactive<GovernanceListQuery>({
  channelName: undefined,
  channelType: undefined,
  status: undefined,
});

const columns = [
  { title: '频道名称', dataIndex: 'channelName', width: 200, ellipsis: true },
  { title: '频道类型', dataIndex: 'channelType', width: 80 },
  { title: '状态', key: 'status', width: 100 },
  { title: '订阅数', dataIndex: 'subscriberCount', width: 80, align: 'right' as const },
  { title: '最后活跃', dataIndex: 'lastActiveTime', width: 160 },
  { title: '创建时间', dataIndex: 'createTime', width: 160 },
  { title: '操作', key: 'action', width: 100, fixed: 'right' as const },
];

async function fetchList() {
  loading.value = true;
  try {
    const res = await getGovernanceChannelList({
      ...filters,
      current: currentPage.value,
      size: pageSize.value,
    });
    channelList.value = res.records || [];
    total.value = res.total || 0;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  currentPage.value = 1;
  fetchList();
}

function handlePageChange(page: number) {
  currentPage.value = page;
  fetchList();
}

function handleViewDetail(record: GovernanceChannelVO) {
  router.push(`/content/channel/governance/${record.channelId}`);
}

onMounted(() => {
  fetchList();
});
</script>

<style lang="less" scoped>
.channel-governance-page {
  padding: 16px;

  &__title {
    margin: 0 0 16px;
    font-size: 20px;
    font-weight: 600;
  }

  &__filter {
    margin-bottom: 16px;
  }
}
</style>
