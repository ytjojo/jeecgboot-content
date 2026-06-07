<template>
  <div class="scheduled-publish">
    <div class="scheduled-header">
      <span>我的定时发布</span>
      <Button size="small" @click="refresh">刷新</Button>
    </div>
    <Table :dataSource="scheduledTaskList" :columns="columns" :loading="loading" rowKey="id" size="small">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <Space>
            <Button size="small" type="link" @click="handleEditTime(record)">编辑时间</Button>
            <Button size="small" type="link" danger @click="handleCancel(record)">取消发布</Button>
          </Space>
        </template>
      </template>
    </Table>
    <Modal v-model:visible="editVisible" title="编辑定时发布时间" @ok="handleSaveTime">
      <DatePicker v-model:value="newTime" show-time format="YYYY-MM-DD HH:mm" :disabledDate="disabledDate" style="width: 100%" />
    </Modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { Table, Button, Space, Modal, DatePicker } from 'ant-design-vue';
import { useChannelPublishStore } from '/@/store/modules/channelPublish';
import { storeToRefs } from 'pinia';
import { useMessage } from '/@/hooks/web/useMessage';
import dayjs from 'dayjs';

const { createMessage, createConfirmSync } = useMessage();

const store = useChannelPublishStore();
const { scheduledTaskList, loading } = storeToRefs(store);

const editVisible = ref(false);
const editingId = ref('');
const newTime = ref(null);

const columns = [
  { title: '标题', dataIndex: 'contentTitle', key: 'contentTitle' },
  { title: '目标频道', dataIndex: 'channelName', key: 'channelName' },
  { title: '计划发布时间', dataIndex: 'scheduledTime', key: 'scheduledTime' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '操作', key: 'action', width: 160 },
];

const disabledDate = (current: any) => current && current < Date.now();

const handleEditTime = (record: any) => {
  editingId.value = record.id;
  newTime.value = null;
  editVisible.value = true;
};

const handleSaveTime = async () => {
  if (!newTime.value) return;
  await store.editScheduledTime(editingId.value, dayjs(newTime.value).format('YYYY-MM-DD HH:mm'));
  editVisible.value = false;
  createMessage.success('定时发布时间已更新');
};

const handleCancel = (record: any) => {
  createConfirmSync({
    title: '确认取消',
    content: `确认取消《${record.contentTitle}》的定时发布？`,
    onOk: async () => {
      await store.cancelScheduledTask(record.id);
      createMessage.success('定时发布已取消');
    },
  });
};

const refresh = () => store.fetchScheduledTasks();

onMounted(() => store.fetchScheduledTasks());
</script>

<style lang="less" scoped>
.scheduled-publish {
  .scheduled-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; font-weight: 600; }
}
</style>
