<!-- jeecgboot-vue3/src/views/channel/governance/GovernanceLog.vue -->
<template>
  <div class="governance-log">
    <div class="page-header">
      <h3>治理日志</h3>
    </div>

    <div class="filter-bar">
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

    <Table :dataSource="logList" :columns="columns" :loading="loading" :pagination="pagination" rowKey="id" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <Tag :color="getActionColor(record.action)">{{ getActionText(record.action) }}</Tag>
        </template>
        <template v-if="column.dataIndex === 'detail'">
          <Button type="link" size="small" @click="handleViewDetail(record)">查看</Button>
        </template>
      </template>
    </Table>

    <GovernanceDetailDrawer ref="detailDrawerRef" />
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue';
  import { Table, Button, Space, Tag, Select, Input, RangePicker } from 'ant-design-vue';
  import { getGovernanceLog } from '/@/api/content/channelGovernance';
  import GovernanceDetailDrawer from './GovernanceDetailDrawer.vue';

  const props = defineProps<{ channelId: string }>();

  const loading = ref(false);
  const logList = ref<any[]>([]);
  const actionFilter = ref('');
  const dateRange = ref<any>(null);
  const operatorSearch = ref('');
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

  function getActionColor(action: string) {
    const map: Record<string, string> = { REMOVE: 'red', MUTE: 'orange', UNMUTE: 'green', BLACKLIST_ADD: 'default', BLACKLIST_REMOVE: 'blue' };
    return map[action] || 'default';
  }

  function getActionText(action: string) {
    const map: Record<string, string> = { REMOVE: '移除', MUTE: '禁言', UNMUTE: '解除禁言', BLACKLIST_ADD: '加入黑名单', BLACKLIST_REMOVE: '移出黑名单' };
    return map[action] || action;
  }

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
</style>
