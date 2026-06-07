<template>
  <div class="recycle-bin">
    <div class="bin-header">
      <span>回收站</span>
      <Button size="small" @click="$emit('back')">返回管理</Button>
    </div>
    <Table
      :dataSource="recycleBinList"
      :columns="columns"
      :loading="loading"
      :rowSelection="{ selectedRowKeys: selectedIds, onChange: onSelectChange }"
      rowKey="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'remaining'">
          <span :class="{ expired: record.remainingDays <= 0 }">
            {{ record.remainingDays > 0 ? `${record.remainingDays}天` : '已过保留期' }}
          </span>
        </template>
        <template v-if="column.key === 'action'">
          <Button size="small" type="link" :disabled="record.remainingDays <= 0" @click="handleRestore(record.id)">
            恢复
          </Button>
        </template>
      </template>
    </Table>
    <div class="batch-bar" v-if="selectedIds.length > 0">
      <Space>
        <span>已选 {{ selectedIds.length }} 条</span>
        <Button type="primary" @click="handleBatchRestore">批量恢复</Button>
      </Space>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { Table, Button, Space, Modal, message } from 'ant-design-vue';
import { useChannelGovernanceStore } from '/@/store/modules/channelGovernance';
import { storeToRefs } from 'pinia';
import { track } from '/@/utils/track';

const props = defineProps<{ channelId: string }>();
defineEmits(['back']);

const store = useChannelGovernanceStore();
const { recycleBinList, loading } = storeToRefs(store);

const selectedIds = ref<string[]>([]);

const columns = [
  { title: '标题', dataIndex: 'title', key: 'title' },
  { title: '类型', dataIndex: 'contentType', key: 'contentType', width: 80 },
  { title: '原作者', dataIndex: 'originalAuthor', key: 'originalAuthor', width: 100 },
  { title: '删除人', dataIndex: 'deletedBy', key: 'deletedBy', width: 100 },
  { title: '删除时间', dataIndex: 'deleteTime', key: 'deleteTime', width: 160 },
  { title: '删除原因', dataIndex: 'deleteReason', key: 'deleteReason' },
  { title: '剩余天数', key: 'remaining', width: 100 },
  { title: '操作', key: 'action', width: 80 },
];

const onSelectChange = (keys: string[]) => { selectedIds.value = keys; };

const handleRestore = (id: string) => {
  Modal.confirm({
    title: '确认恢复',
    content: '确认恢复此内容到频道？',
    onOk: async () => {
      try {
        await store.restore(id, props.channelId);
        await store.fetchRecycleBin(props.channelId);
        track('recycle_bin_restore', { channel_id: props.channelId, is_batch: false });
        message.success('已恢复');
      } catch {
        message.error('恢复失败，请重试');
      }
    },
  });
};

const handleBatchRestore = () => {
  Modal.confirm({
    title: '批量恢复',
    content: `确认恢复选中的 ${selectedIds.value.length} 条内容？`,
    onOk: async () => {
      try {
        for (const id of selectedIds.value) {
          await store.restore(id, props.channelId);
        }
        await store.fetchRecycleBin(props.channelId);
        track('recycle_bin_restore', { channel_id: props.channelId, is_batch: true, batch_count: selectedIds.value.length });
        selectedIds.value = [];
        message.success('批量恢复完成');
      } catch {
        message.error('批量恢复失败，请重试');
      }
    },
  });
};

onMounted(() => store.fetchRecycleBin(props.channelId));
</script>

<style lang="less" scoped>
.recycle-bin {
  .bin-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; font-weight: 600; }
  .batch-bar { position: sticky; bottom: 0; background: #fff; padding: 12px; border-top: 1px solid #e8e8e8; display: flex; justify-content: flex-end; }
  .expired { color: #999; }
}
</style>
