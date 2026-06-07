<!-- jeecgboot-vue3/src/views/channel/governance/GovernanceLog.vue -->
<template>
  <div class="governance-log">
    <div class="page-header">
      <h3>治理日志</h3>
    </div>

    <!-- 桌面端：内联筛选栏 -->
    <div v-if="!isMobile" class="filter-bar">
      <Space>
        <Select v-model:value="actionFilter" placeholder="操作类型" style="width: 150px" @change="loadData" allowClear>
          <Select.Option value="">全部</Select.Option>
          <Select.Option value="REMOVE">移除</Select.Option>
          <Select.Option value="MUTE">禁言</Select.Option>
          <Select.Option value="UNMUTE">解除禁言</Select.Option>
          <Select.Option value="BLACKLIST_ADD">加入黑名单</Select.Option>
          <Select.Option value="BLACKLIST_REMOVE">移出黑名单</Select.Option>
        </Select>
        <RangePicker v-model:value="dateRange" @change="loadData" />
        <Input.Search v-model:value="operatorSearch" placeholder="搜索操作者" style="width: 180px" @search="loadData" />
      </Space>
    </div>

    <!-- 移动端：筛选按钮 -->
    <div v-else class="mobile-filter-row">
      <Button @click="filterDrawerVisible = true">筛选</Button>
    </div>

    <!-- 移动端筛选 Drawer -->
    <Drawer v-model:open="filterDrawerVisible" title="筛选" placement="bottom" :height="'60%'">
      <Space direction="vertical" style="width: 100%">
        <Select v-model:value="actionFilter" placeholder="操作类型" style="width: 100%" allowClear>
          <Select.Option value="">全部</Select.Option>
          <Select.Option value="REMOVE">移除</Select.Option>
          <Select.Option value="MUTE">禁言</Select.Option>
          <Select.Option value="UNMUTE">解除禁言</Select.Option>
          <Select.Option value="BLACKLIST_ADD">加入黑名单</Select.Option>
          <Select.Option value="BLACKLIST_REMOVE">移出黑名单</Select.Option>
        </Select>
        <RangePicker v-model:value="dateRange" style="width: 100%" />
        <Input.Search v-model:value="operatorSearch" placeholder="搜索操作者" @search="loadData" />
      </Space>
      <template #footer>
        <Button @click="filterDrawerVisible = false">关闭</Button>
        <Button type="primary" @click="filterDrawerVisible = false; loadData()">确定</Button>
      </template>
    </Drawer>

    <Table v-if="!isMobile" :dataSource="logList" :columns="columns" :loading="loading" :pagination="pagination" rowKey="id" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <Tag :color="getActionColor(record.action)">{{ getActionText(record.action) }}</Tag>
        </template>
        <template v-if="column.dataIndex === 'detail'">
          <Button type="link" size="small" @click="handleViewDetail(record)">查看</Button>
        </template>
      </template>
    </Table>

    <!-- 移动端卡片视图 -->
    <div v-else class="mobile-card-list">
      <div v-for="item in logList" :key="item.id" class="mobile-card">
        <div class="mobile-card-header">
          <Tag :color="getActionColor(item.action)">{{ getActionText(item.action) }}</Tag>
          <span class="mobile-card-time">{{ item.createTime }}</span>
        </div>
        <div class="mobile-card-body">
          <div class="mobile-card-field">操作者：{{ item.operatorName }}</div>
          <div class="mobile-card-field">目标用户：{{ item.targetUserName }}</div>
          <div class="mobile-card-field">原因：{{ item.reason || '-' }}</div>
        </div>
        <div class="mobile-card-actions">
          <Button type="link" size="small" @click="handleViewDetail(item)">查看详情</Button>
        </div>
      </div>
    </div>

    <GovernanceDetailDrawer ref="detailDrawerRef" />
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, onMounted } from 'vue';
  import { Table, Button, Space, Tag, Select, Input, RangePicker, Drawer } from 'ant-design-vue';
  import { useBreakpoint } from '/@/hooks/event/useBreakpoint';
  import { sizeEnum } from '/@/enums/breakpointEnum';
  import { getGovernanceLog } from '/@/api/content/channelGovernance';
  import { getActionColor, getActionText } from './constants';

  const { screenRef } = useBreakpoint();
  const isMobile = computed(() => {
    const s = screenRef.value;
    return s === sizeEnum.XS || s === sizeEnum.SM;
  });
  import GovernanceDetailDrawer from './GovernanceDetailDrawer.vue';

  const props = defineProps<{ channelId: string }>();

  const loading = ref(false);
  const logList = ref<any[]>([]);
  const actionFilter = ref('');
  const dateRange = ref<any>(null);
  const operatorSearch = ref('');
  const filterDrawerVisible = ref(false);
  const pagination = reactive({ current: 1, pageSize: 20, total: 0 });
  const detailDrawerRef = ref();

  const columns = [
    { title: '操作类型', dataIndex: 'action', key: 'action', width: 120 },
    { title: '操作者', dataIndex: 'operatorName', key: 'operatorName', width: 120 },
    { title: '目标用户', dataIndex: 'targetUserName', key: 'targetUserName', width: 120 },
    { title: '时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
    { title: '原因', dataIndex: 'reason', key: 'reason', ellipsis: true },
    { title: '详情', dataIndex: 'detail', key: 'detail', width: 80 },
  ];

  async function loadData() {
    loading.value = true;
    try {
      const params: any = {
        channelId: props.channelId,
        pageNo: pagination.current,
        pageSize: pagination.pageSize,
      };
      if (actionFilter.value) params.action = actionFilter.value;
      if (dateRange.value) {
        params.startTime = dateRange.value[0];
        params.endTime = dateRange.value[1];
      }
      if (operatorSearch.value) params.operator = operatorSearch.value;
      const res = await getGovernanceLog(params);
      logList.value = res.records || res;
      pagination.total = res.total || logList.value.length;
    } finally {
      loading.value = false;
    }
  }

  function handleTableChange(pag: any) {
    pagination.current = pag.current;
    pagination.pageSize = pag.pageSize;
    loadData();
  }

  function handleViewDetail(record: any) {
    detailDrawerRef.value.open(record);
  }

  onMounted(loadData);
</script>

<style scoped>
.governance-log { padding: 16px; }
.page-header { margin-bottom: 16px; }
.filter-bar { margin-bottom: 16px; }
.mobile-filter-row { margin-bottom: 16px; }
.mobile-card-list { display: flex; flex-direction: column; gap: 12px; }
.mobile-card { background: #fff; border: 1px solid #f0f0f0; border-radius: 8px; padding: 12px; }
.mobile-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.mobile-card-time { font-size: 12px; color: #999; }
.mobile-card-body { margin-bottom: 8px; }
.mobile-card-field { font-size: 13px; color: #666; line-height: 1.8; }
.mobile-card-actions { display: flex; gap: 8px; justify-content: flex-end; }
@media (max-width: 575px) {
  .mobile-card .ant-btn {
    min-height: 44px;
    min-width: 44px;
  }
}
</style>
