<template>
  <div class="channel-list">
    <a-page-header title="我的频道" :back-icon="false">
      <template #extra>
        <a-button type="primary" @click="goCreate">
          <PlusOutlined /> 创建频道
        </a-button>
      </template>
    </a-page-header>

    <!-- 筛选区 -->
    <a-card :bordered="false" class="channel-list__filter">
      <a-space>
        <a-select
          v-model:value="filterType"
          placeholder="频道类型"
          allow-clear
          style="width: 140px"
          :options="channelTypeOptions"
          @change="handleSearch"
        />
        <a-select
          v-model:value="filterStatus"
          placeholder="审核状态"
          allow-clear
          style="width: 140px"
          :options="channelStatusOptions"
          @change="handleSearch"
        />
      </a-space>
    </a-card>

    <!-- 列表 -->
    <a-card :bordered="false" class="channel-list__table">
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'icon'">
            <a-avatar :src="record.iconUrl" :size="40">
              {{ record.name?.charAt(0) }}
            </a-avatar>
          </template>
          <template v-if="column.key === 'name'">
            <a @click="goManage(record)">{{ record.name }}</a>
          </template>
          <template v-if="column.key === 'channelType'">
            <ChannelTypeTag :type="record.channelType" />
          </template>
          <template v-if="column.key === 'status'">
            <ChannelStatusTag :status="record.status" />
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button
                v-if="record.status === 'ACTIVE'"
                type="link"
                size="small"
                @click="goManage(record)"
              >管理</a-button>
              <a-button
                v-if="record.status === 'PENDING_REVIEW'"
                type="link"
                size="small"
                @click="goManage(record)"
              >查看详情</a-button>
              <a-button
                v-if="record.status === 'REJECTED'"
                type="link"
                size="small"
                @click="handleResubmit(record)"
              >重新提交</a-button>
              <a-popconfirm
                v-if="record.status === 'DELETE_COOLING'"
                title="确定撤销删除吗？"
                @confirm="handleCancelDelete(record)"
              >
                <a-button type="link" size="small" danger>撤销删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>

        <template #emptyText>
          <a-empty description="暂无频道">
            <a-button type="primary" @click="goCreate">创建你的第一个频道</a-button>
          </a-empty>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { PlusOutlined } from '@ant-design/icons-vue';
  import { message } from 'ant-design-vue';
  import ChannelTypeTag from '/@/components/jeecg/channel/ChannelTypeTag.vue';
  import ChannelStatusTag from '/@/components/jeecg/channel/ChannelStatusTag.vue';
  import { getChannelList, cancelDeleteChannel } from '/@/api/content/channel';
  import { channelTypeOptions, channelStatusOptions } from '/@/store/modules/channel';
  import type { ChannelVO } from '/@/api/content/channel/model/channelModel';
  import type { TablePaginationConfig } from 'ant-design-vue';

  const router = useRouter();
  const loading = ref(false);
  const dataSource = ref<ChannelVO[]>([]);
  const filterType = ref<string | undefined>();
  const filterStatus = ref<string | undefined>();

  const pagination = reactive<TablePaginationConfig>({
    current: 1,
    pageSize: 10,
    total: 0,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  });

  const columns = [
    { title: '图标', key: 'icon', width: 60, align: 'center' as const },
    { title: '频道名称', key: 'name', dataIndex: 'name', sorter: true },
    { title: '类型', key: 'channelType', width: 100 },
    { title: '状态', key: 'status', width: 120 },
    { title: '创建时间', dataIndex: 'createdTime', key: 'createdTime', width: 180, sorter: true },
    { title: '操作', key: 'action', width: 160, fixed: 'right' as const },
  ];

  async function loadData() {
    loading.value = true;
    try {
      const result = await getChannelList({
        current: pagination.current!,
        size: pagination.pageSize!,
        channelType: filterType.value,
        status: filterStatus.value,
      });
      dataSource.value = result.records || [];
      pagination.total = result.total || 0;
    } catch {
      message.error('加载频道列表失败');
    } finally {
      loading.value = false;
    }
  }

  function handleSearch() {
    pagination.current = 1;
    loadData();
  }

  function handleTableChange(pag: TablePaginationConfig) {
    pagination.current = pag.current;
    pagination.pageSize = pag.pageSize;
    loadData();
  }

  function goCreate() {
    router.push('/content/channel/create');
  }

  function goManage(record: ChannelVO) {
    router.push(`/content/channel/manage/${record.id}`);
  }

  function handleResubmit(record: ChannelVO) {
    router.push(`/content/channel/manage/${record.id}`);
  }

  async function handleCancelDelete(record: ChannelVO) {
    try {
      await cancelDeleteChannel(record.id);
      message.success('已撤销删除');
      loadData();
    } catch {
      message.error('撤销失败');
    }
  }

  onMounted(loadData);
</script>

<style scoped lang="less">
  .channel-list {
    padding: 24px;

    &__filter {
      margin-bottom: 16px;
    }
  }
</style>
