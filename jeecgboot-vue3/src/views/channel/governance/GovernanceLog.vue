<template>
  <div class="governance-log">
    <div class="log-header">治理日志</div>
    <div class="filter-bar">
      <Space>
        <Select v-model:value="filter.actionType" placeholder="操作类型" allowClear style="width: 140px">
          <Select.Option value="pin">置顶</Select.Option>
          <Select.Option value="unpin">取消置顶</Select.Option>
          <Select.Option value="feature">标记精华</Select.Option>
          <Select.Option value="delete">删除</Select.Option>
          <Select.Option value="restore">恢复</Select.Option>
          <Select.Option value="move">移出</Select.Option>
          <Select.Option value="edit_assist">编辑协助</Select.Option>
          <Select.Option value="announcement">公告变更</Select.Option>
        </Select>
        <Input v-model:value="filter.keyword" placeholder="搜索内容" style="width: 200px" allowClear />
      </Space>
    </div>
    <Table :dataSource="governanceLogList" :columns="columns" :loading="loading" rowKey="id">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'target'">
          <a @click="handleViewContent(record.contentId)">{{ record.targetTitle }}</a>
        </template>
      </template>
    </Table>
  </div>
</template>

<script lang="ts" setup>
import { reactive, onMounted, watch } from 'vue';
import { Table, Space, Select, Input } from 'ant-design-vue';
import { useChannelGovernanceStore } from '/@/store/modules/channelGovernance';
import { storeToRefs } from 'pinia';

const props = defineProps<{ channelId: string }>();
const store = useChannelGovernanceStore();
const { governanceLogList, loading } = storeToRefs(store);

const filter = reactive({ actionType: undefined as string | undefined, keyword: '' });

const columns = [
  { title: '时间', dataIndex: 'time', key: 'time', width: 160 },
  { title: '操作者', dataIndex: 'operator', key: 'operator', width: 100 },
  { title: '操作类型', dataIndex: 'actionType', key: 'actionType', width: 100 },
  { title: '操作对象', key: 'target' },
  { title: '结果', dataIndex: 'result', key: 'result', width: 80 },
  { title: '原因/备注', dataIndex: 'remark', key: 'remark' },
];

const handleViewContent = (contentId: string) => {
  // Navigation handled by parent or router
};

watch(filter, () => {
  store.fetchGovernanceLog(props.channelId, { actionType: filter.actionType, keyword: filter.keyword });
}, { deep: true });

onMounted(() => store.fetchGovernanceLog(props.channelId));
</script>

<style lang="less" scoped>
.governance-log {
  .log-header { font-weight: 600; margin-bottom: 12px; }
  .filter-bar { margin-bottom: 12px; }
}
</style>
