<!-- jeecgboot-vue3/src/views/channel/blacklist/BlacklistPage.vue -->
<template>
  <div class="blacklist-page">
    <div class="page-header">
      <h3>黑名单 <span class="count">共 {{ total }} 人</span></h3>
    </div>

    <Table :dataSource="blacklist" :columns="columns" :loading="loading" :pagination="pagination" rowKey="id" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'user'">
          <Space>
            <Avatar :src="record.avatar" size="small" />
            <span>{{ record.nickname }}</span>
          </Space>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <Button type="link" size="small" @click="handleRemove(record)">移出黑名单</Button>
        </template>
      </template>
    </Table>

    <Modal v-model:open="removeModalVisible" title="移出黑名单" :confirmLoading="removing" @ok="handleConfirmRemove">
      <p>确认将 <strong>{{ removeTarget?.nickname }}</strong> 移出黑名单？移出后该用户可按频道当前加入规则重新申请或加入。</p>
    </Modal>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue';
  import { Table, Button, Space, Avatar, Modal } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getBlacklist, removeFromBlacklist } from '/@/api/content/channelBlacklist';

  const props = defineProps<{ channelId: string }>();
  const { createMessage } = useMessage();

  const loading = ref(false);
  const blacklist = ref<any[]>([]);
  const total = ref(0);
  const pagination = reactive({ current: 1, pageSize: 20, total: 0 });

  const removeModalVisible = ref(false);
  const removing = ref(false);
  const removeTarget = ref<any>(null);

  const columns = [
    { title: '用户', dataIndex: 'user', key: 'user', width: 200 },
    { title: '拉黑时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
    { title: '操作人', dataIndex: 'operator', key: 'operator', width: 120 },
    { title: '原因', dataIndex: 'reason', key: 'reason', ellipsis: true },
    { title: '操作', dataIndex: 'action', key: 'action', width: 120 },
  ];

  async function loadData() {
    loading.value = true;
    try {
      const res = await getBlacklist({ channelId: props.channelId, pageNo: pagination.current, pageSize: pagination.pageSize });
      blacklist.value = res.records || res;
      total.value = res.total || blacklist.value.length;
      pagination.total = total.value;
    } finally {
      loading.value = false;
    }
  }

  function handleTableChange(pag: any) {
    pagination.current = pag.current;
    pagination.pageSize = pag.pageSize;
    loadData();
  }

  function handleRemove(record: any) {
    removeTarget.value = record;
    removeModalVisible.value = true;
  }

  async function handleConfirmRemove() {
    removing.value = true;
    try {
      await removeFromBlacklist({ channelId: props.channelId, userId: removeTarget.value.userId });
      createMessage.success('已移出黑名单');
      removeModalVisible.value = false;
      loadData();
    } finally {
      removing.value = false;
    }
  }

  onMounted(loadData);
</script>

<style scoped>
.blacklist-page { padding: 16px; }
.page-header { margin-bottom: 16px; }
.count { font-size: 14px; color: #999; font-weight: normal; }
</style>
