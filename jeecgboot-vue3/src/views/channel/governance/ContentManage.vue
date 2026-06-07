<template>
  <div class="content-manage">
    <div class="manage-header">
      <span>内容管理</span>
      <Space>
        <Button size="small" @click="$emit('goRecycleBin')">回收站</Button>
        <Button size="small" @click="$emit('goLog')">日志</Button>
      </Space>
    </div>
    <div class="filter-bar">
      <Space wrap>
        <Input v-model:value="filter.keyword" placeholder="搜索" style="width: 200px" allowClear />
        <Select v-model:value="filter.contentType" placeholder="内容类型" allowClear style="width: 120px">
          <Select.Option value="article">文章</Select.Option>
          <Select.Option value="post">图文帖子</Select.Option>
          <Select.Option value="video">视频</Select.Option>
          <Select.Option value="note">笔记</Select.Option>
        </Select>
        <Select v-model:value="filter.status" placeholder="状态" allowClear style="width: 120px">
          <Select.Option value="published">已发布</Select.Option>
          <Select.Option value="pinned">已置顶</Select.Option>
          <Select.Option value="featured">精华</Select.Option>
        </Select>
        <Select v-model:value="filter.sortBy" placeholder="排序" style="width: 140px">
          <Select.Option value="latest">最新发布</Select.Option>
          <Select.Option value="likes">最多点赞</Select.Option>
        </Select>
      </Space>
    </div>
    <Table
      :dataSource="contentList"
      :columns="columns"
      :loading="loading"
      :rowSelection="{ selectedRowKeys: selectedIds, onChange: onSelectChange }"
      rowKey="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <Tag v-if="record.isPinned" color="blue">置顶</Tag>
          <Tag v-if="record.isFeatured" color="gold">精华</Tag>
        </template>
        <template v-if="column.key === 'action'">
          <GovernanceActionMenu
            :is-pinned="record.isPinned"
            :is-featured="record.isFeatured"
            @action="(action) => handleAction(action, record)"
          />
        </template>
      </template>
    </Table>
    <div class="batch-bar" v-if="selectedIds.length > 0">
      <Space>
        <span>已选 {{ selectedIds.length }} 条</span>
        <Button danger @click="handleBatchDelete">批量删除</Button>
        <Button @click="handleBatchPin">批量置顶</Button>
        <Button @click="handleBatchFeature">批量精华</Button>
      </Space>
    </div>
    <MoveChannelDialog v-model:visible="moveVisible" :content-id="movingContentId" :source-channel-id="channelId" @moved="refresh" />
    <EditAssistDrawer v-model:visible="editVisible" :content-id="editingContentId" :channel-id="channelId" :content="editingContent" @saved="refresh" />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, watch, onUnmounted } from 'vue';
import { Table, Button, Space, Select, Input, Tag, Modal, message } from 'ant-design-vue';
import { useChannelGovernanceStore } from '/@/store/modules/channelGovernance';
import { storeToRefs } from 'pinia';
import GovernanceActionMenu from '../components/GovernanceActionMenu.vue';
import MoveChannelDialog from '../components/MoveChannelDialog.vue';
import EditAssistDrawer from '../components/EditAssistDrawer.vue';
import { track } from '/@/utils/track';

const props = defineProps<{ channelId: string }>();
defineEmits(['goRecycleBin', 'goLog']);

const store = useChannelGovernanceStore();
const { contentList, loading } = storeToRefs(store);

const filter = reactive({ keyword: '', contentType: undefined as string | undefined, status: undefined as string | undefined, sortBy: 'latest' });
const selectedIds = ref<string[]>([]);
const moveVisible = ref(false);
const movingContentId = ref('');
const editVisible = ref(false);
const editingContentId = ref('');
const editingContent = ref({ title: '', author: '' });

const columns = [
  { title: '标题', dataIndex: 'title', key: 'title' },
  { title: '类型', dataIndex: 'contentType', key: 'contentType', width: 80 },
  { title: '作者', dataIndex: 'author', key: 'author', width: 100 },
  { title: '发布时间', dataIndex: 'publishTime', key: 'publishTime', width: 160 },
  { title: '状态', key: 'status', width: 120 },
  { title: '操作', key: 'action', width: 80 },
];

const onSelectChange = (keys: string[]) => { selectedIds.value = keys; };

const handleAction = async (action: string, record: any) => {
  try {
    switch (action) {
      case 'pin': await store.pin(record.id, props.channelId, true); track('governance_action', { action_type: 'pin', channel_id: props.channelId }); message.success('已置顶'); break;
      case 'unpin': await store.pin(record.id, props.channelId, false); track('governance_action', { action_type: 'unpin', channel_id: props.channelId }); message.success('已取消置顶'); break;
      case 'feature': await store.feature(record.id, props.channelId, true); track('governance_action', { action_type: 'feature', channel_id: props.channelId }); message.success('已标记精华'); break;
      case 'unfeature': await store.feature(record.id, props.channelId, false); track('governance_action', { action_type: 'unfeature', channel_id: props.channelId }); message.success('已取消精华'); break;
      case 'move': movingContentId.value = record.id; moveVisible.value = true; break;
      case 'editAssist': editingContentId.value = record.id; editingContent.value = { title: record.title, author: record.author }; editVisible.value = true; break;
      case 'delete':
        Modal.confirm({
          title: '确认删除',
          content: `确认将《${record.title}》从频道删除？内容将进入回收站，30天内可恢复。`,
          onOk: async () => {
            try {
              await store.deleteContent(record.id, props.channelId);
              track('governance_action', { action_type: 'delete', channel_id: props.channelId });
              message.success('已删除并移入回收站');
            } catch {
              message.error('删除失败，请重试');
            }
          },
        });
        break;
    }
  } catch {
    message.error('操作失败，请重试');
  }
};

const handleBatchDelete = () => {
  Modal.confirm({
    title: '批量删除',
    content: `确认删除选中的 ${selectedIds.value.length} 条内容？`,
    onOk: async () => {
      try {
        for (const id of selectedIds.value) { await store.deleteContent(id, props.channelId); }
        track('governance_action', { action_type: 'batch_delete', channel_id: props.channelId, is_batch: true, batch_count: selectedIds.value.length });
        selectedIds.value = [];
        message.success('批量删除完成');
      } catch {
        message.error('批量删除失败，请重试');
      }
    },
  });
};

const handleBatchPin = async () => {
  try {
    for (const id of selectedIds.value) await store.pin(id, props.channelId, true);
    selectedIds.value = [];
    message.success('批量置顶完成');
  } catch {
    message.error('批量置顶失败，请重试');
  }
};

const handleBatchFeature = async () => {
  try {
    for (const id of selectedIds.value) await store.feature(id, props.channelId, true);
    selectedIds.value = [];
    message.success('批量精华完成');
  } catch {
    message.error('批量精华失败，请重试');
  }
};

const refresh = () => store.fetchList();

let debounceTimer: ReturnType<typeof setTimeout>;
watch(filter, () => {
  clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => {
    store.setFilter({ ...filter });
    store.fetchList();
  }, 300);
}, { deep: true });

onUnmounted(() => clearTimeout(debounceTimer));

onMounted(() => { store.setFilter({ channelId: props.channelId }); store.fetchList(); });
</script>

<style lang="less" scoped>
.content-manage {
  .manage-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; font-weight: 600; }
  .filter-bar { margin-bottom: 12px; }
  .batch-bar { position: sticky; bottom: 0; background: #fff; padding: 12px; border-top: 1px solid #e8e8e8; display: flex; justify-content: flex-end; }
}
</style>
