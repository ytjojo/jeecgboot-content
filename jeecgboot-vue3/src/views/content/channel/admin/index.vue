<template>
  <div class="channel-admin">
    <a-page-header title="频道管理" :back-icon="false">
      <template #extra>
        <a-button type="primary" @click="showSystemModal = true">
          <PlusOutlined /> 创建系统频道
        </a-button>
      </template>
    </a-page-header>

    <!-- 筛选区 -->
    <a-card :bordered="false" class="channel-admin__filter">
      <a-form layout="inline" :model="filterForm">
        <a-form-item label="频道类型">
          <a-select
            v-model:value="filterForm.channelType"
            placeholder="全部"
            allow-clear
            style="width: 140px"
            :options="channelTypeOptions"
          />
        </a-form-item>
        <a-form-item label="审核状态">
          <a-select
            v-model:value="filterForm.status"
            placeholder="全部"
            allow-clear
            style="width: 140px"
            :options="channelStatusOptions"
          />
        </a-form-item>
        <a-form-item label="名称">
          <a-input v-model:value="filterForm.name" placeholder="搜索频道名称" allow-clear style="width: 180px" />
        </a-form-item>
        <a-form-item label="创建时间">
          <a-range-picker v-model:value="filterForm.dateRange" style="width: 240px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 批量操作 -->
    <a-card v-if="selectedRowKeys.length > 0" :bordered="false" class="channel-admin__batch">
      <a-space>
        <span>已选 {{ selectedRowKeys.length }} 项</span>
        <a-button size="small" @click="handleBatchApprove">批量通过</a-button>
        <a-button size="small" danger @click="handleBatchReject">批量拒绝</a-button>
      </a-space>
    </a-card>

    <!-- 列表 -->
    <a-card :bordered="false" class="channel-admin__table">
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'icon'">
            <a-avatar :src="record.iconUrl" :size="32">{{ record.name?.charAt(0) }}</a-avatar>
          </template>
          <template v-if="column.key === 'name'">
            <a @click="showDetail(record)">{{ record.name }}</a>
          </template>
          <template v-if="column.key === 'channelType'">
            <ChannelTypeTag :type="record.channelType" />
          </template>
          <template v-if="column.key === 'status'">
            <ChannelStatusTag :status="record.status" />
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="showDetail(record)">查看详情</a-button>
              <a-button
                v-if="record.channelType === 'system'"
                type="link"
                size="small"
                @click="handleEditSystem(record)"
              >编辑</a-button>
              <a-popconfirm
                v-if="record.status === 'DELETE_COOLING'"
                title="确定强制删除吗？此操作不可撤销。"
                @confirm="handleForceDelete(record)"
              >
                <a-button type="link" size="small" danger>强制删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <SystemChannelModal ref="systemModalRef" @created="loadData" />
    <ChannelDetailDrawer ref="detailDrawerRef" />
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue';
  import { PlusOutlined } from '@ant-design/icons-vue';
  import { message, Modal } from 'ant-design-vue';
  import ChannelTypeTag from '/@/components/jeecg/channel/ChannelTypeTag.vue';
  import ChannelStatusTag from '/@/components/jeecg/channel/ChannelStatusTag.vue';
  import SystemChannelModal from './SystemChannelModal.vue';
  import ChannelDetailDrawer from './ChannelDetailDrawer.vue';
  import { getChannelList, deleteChannel, reviewAction } from '/@/api/content/channel';
  import { channelTypeOptions, channelStatusOptions } from '/@/store/modules/channel';
  import type { ChannelVO } from '/@/api/content/channel/model/channelModel';
  import type { TablePaginationConfig } from 'ant-design-vue';

  const loading = ref(false);
  const dataSource = ref<ChannelVO[]>([]);
  const selectedRowKeys = ref<string[]>([]);
  const showSystemModal = ref(false);
  const systemModalRef = ref<InstanceType<typeof SystemChannelModal>>();
  const detailDrawerRef = ref<InstanceType<typeof ChannelDetailDrawer>>();

  const filterForm = reactive({
    channelType: undefined as string | undefined,
    status: undefined as string | undefined,
    name: '',
    dateRange: null as any,
  });

  const pagination = reactive<TablePaginationConfig>({
    current: 1,
    pageSize: 20,
    total: 0,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  });

  const columns = [
    { title: '图标', key: 'icon', width: 50, align: 'center' as const },
    { title: '频道名称', key: 'name', dataIndex: 'name' },
    { title: '类型', key: 'channelType', width: 100 },
    { title: '状态', key: 'status', width: 120 },
    { title: '归属', dataIndex: 'ownerName', key: 'ownerName', width: 120 },
    { title: '分类', dataIndex: 'categoryName', key: 'categoryName', width: 100 },
    { title: '置顶权重', dataIndex: 'topWeight', key: 'topWeight', width: 90, align: 'center' as const },
    { title: '创建时间', dataIndex: 'createdTime', key: 'createdTime', width: 170 },
    { title: '操作', key: 'action', width: 180, fixed: 'right' as const },
  ];

  async function loadData() {
    loading.value = true;
    try {
      const result = await getChannelList({
        current: pagination.current!,
        pageSize: pagination.pageSize!,
        channelType: filterForm.channelType,
        status: filterForm.status,
        name: filterForm.name || undefined,
      });
      dataSource.value = result.records || [];
      pagination.total = result.total || 0;
    } catch {
      message.error('加载数据失败');
    } finally {
      loading.value = false;
    }
  }

  function handleSearch() {
    pagination.current = 1;
    loadData();
  }

  function handleReset() {
    filterForm.channelType = undefined;
    filterForm.status = undefined;
    filterForm.name = '';
    filterForm.dateRange = null;
    handleSearch();
  }

  function handleTableChange(pag: TablePaginationConfig) {
    pagination.current = pag.current;
    pagination.pageSize = pag.pageSize;
    loadData();
  }

  function onSelectChange(keys: string[]) {
    selectedRowKeys.value = keys;
  }

  function showDetail(record: ChannelVO) {
    detailDrawerRef.value?.open(record);
  }

  function handleEditSystem(record: ChannelVO) {
    // TODO: 打开系统频道编辑
    message.info('系统频道编辑功能开发中');
  }

  async function handleForceDelete(record: ChannelVO) {
    try {
      await deleteChannel(record.id);
      message.success('已强制删除');
      loadData();
    } catch {
      message.error('删除失败');
    }
  }

  async function handleBatchApprove() {
    Modal.confirm({
      title: '批量通过',
      content: `确定通过选中的 ${selectedRowKeys.value.length} 条审核吗？`,
      onOk: async () => {
        try {
          for (const id of selectedRowKeys.value) {
            await reviewAction({ channelId: id, action: 'APPROVE' });
          }
          message.success('批量通过成功');
          selectedRowKeys.value = [];
          loadData();
        } catch {
          message.error('批量操作失败');
        }
      },
    });
  }

  async function handleBatchReject() {
    Modal.confirm({
      title: '批量拒绝',
      content: `确定拒绝选中的 ${selectedRowKeys.value.length} 条审核吗？`,
      onOk: async () => {
        try {
          for (const id of selectedRowKeys.value) {
            await reviewAction({ channelId: id, action: 'REJECT', note: '批量拒绝' });
          }
          message.success('批量拒绝成功');
          selectedRowKeys.value = [];
          loadData();
        } catch {
          message.error('批量操作失败');
        }
      },
    });
  }

  onMounted(loadData);
</script>

<style scoped lang="less">
  .channel-admin {
    padding: 24px;

    &__filter {
      margin-bottom: 16px;
    }

    &__batch {
      margin-bottom: 16px;
      background: #e6f7ff;
    }
  }
</style>
