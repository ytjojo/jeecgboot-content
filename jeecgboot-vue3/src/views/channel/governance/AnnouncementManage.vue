<template>
  <div class="announcement-manage">
    <div class="announcement-header">
      <span>频道公告管理</span>
      <Tag v-if="announcement" :color="announcement.status === 'published' ? 'green' : 'orange'">
        {{ announcement.status === 'published' ? '已发布' : '未发布' }}
      </Tag>
      <Tag v-else color="default">未发布</Tag>
    </div>

    <Form layout="vertical">
      <Form.Item label="公告标题">
        <Input v-model:value="form.title" placeholder="请输入公告标题" />
      </Form.Item>
      <Form.Item label="公告内容">
        <Suspense>
          <template #default>
            <Tinymce v-model="form.content" :height="300" />
          </template>
          <template #fallback>
            <div class="editor-loading">
              <a-spin tip="编辑器加载中..." />
            </div>
          </template>
        </Suspense>
      </Form.Item>
      <Form.Item label="有效期截止时间" required>
        <DatePicker
          v-model:value="form.expireAt"
          show-time
          value-format="YYYY-MM-DD HH:mm:ss"
          placeholder="请选择截止时间"
          style="width: 100%"
        />
      </Form.Item>
    </Form>

    <Space>
      <Button @click="handlePreview">预览</Button>
      <Button @click="handleSaveDraft">保存草稿</Button>
      <Button type="primary" @click="handlePublish">发布公告</Button>
      <Button v-if="announcement" danger @click="handleDelete">删除公告</Button>
    </Space>

    <Divider />

    <div class="history-section">
      <div class="history-header">历史版本</div>
      <Table :dataSource="historyList" :columns="historyColumns" :loading="historyLoading" rowKey="id" size="small">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <Button size="small" type="link" @click="handleRestore(record.id)">恢复</Button>
          </template>
        </template>
      </Table>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, defineAsyncComponent } from 'vue';
import { Form, Button, Space, Tag, Divider, Table, Input, DatePicker, Modal, message } from 'ant-design-vue';
const Tinymce = defineAsyncComponent(() => import('/@/components/Tinymce/index'));
import {
  getAnnouncement,
  saveAnnouncement,
  deleteAnnouncement,
  previewAnnouncement,
  getAnnouncementHistory,
  restoreAnnouncementVersion,
} from '/@/api/content/channel/announcement';

const props = defineProps<{ channelId: string }>();

const announcement = ref<any>(null);
const historyList = ref<any[]>([]);
const historyLoading = ref(false);

const form = reactive({ title: '', content: '', expireAt: undefined as string | undefined });

const historyColumns = [
  { title: '版本号', dataIndex: 'version', key: 'version', width: 80 },
  { title: '标题', dataIndex: 'title', key: 'title' },
  { title: '操作人', dataIndex: 'operator', key: 'operator', width: 100 },
  { title: '时间', dataIndex: 'createTime', key: 'createTime', width: 160 },
  { title: '操作', key: 'action', width: 80 },
];

const loadAnnouncement = async () => {
  try {
    const res = await getAnnouncement(props.channelId);
    if (res) {
      announcement.value = res;
      form.title = res.title || '';
      form.content = res.content || '';
      form.expireAt = res.expireAt || undefined as string | undefined;
    }
  } catch {
    // No existing announcement, keep form empty
  }
};

const loadHistory = async () => {
  historyLoading.value = true;
  try {
    const res = await getAnnouncementHistory(props.channelId);
    historyList.value = res || [];
  } catch {
    message.error('加载历史版本失败');
  } finally {
    historyLoading.value = false;
  }
};

const handlePreview = async () => {
  try {
    const res = await previewAnnouncement({ content: form.content });
    if (res) {
      Modal.info({
        title: '公告预览',
        content: res.preview || form.content,
        width: 600,
      });
    }
  } catch {
    message.error('预览失败，请重试');
  }
};

const handleSaveDraft = async () => {
  try {
    await saveAnnouncement({
      channelId: props.channelId,
      title: form.title,
      content: form.content,
      expireAt: form.expireAt,
      version: announcement.value?.version,
    });
    message.success('草稿已保存');
    await loadAnnouncement();
  } catch {
    message.error('保存失败，请重试');
  }
};

const handlePublish = () => {
  Modal.confirm({
    title: '确认发布',
    content: '确认发布此公告？发布后频道成员将可见。',
    onOk: async () => {
      try {
        await saveAnnouncement({
          channelId: props.channelId,
          title: form.title,
          content: form.content,
          expireAt: form.expireAt,
          version: announcement.value?.version,
        });
        message.success('公告已发布');
        await loadAnnouncement();
        await loadHistory();
      } catch {
        message.error('发布失败，请重试');
      }
    },
  });
};

const handleDelete = () => {
  Modal.confirm({
    title: '确认删除',
    content: '确认删除此公告？删除后不可恢复。',
    onOk: async () => {
      try {
        await deleteAnnouncement(announcement.value.id);
        announcement.value = null;
        form.title = '';
        form.content = '';
        form.expireAt = undefined;
        message.success('公告已删除');
        await loadHistory();
      } catch {
        message.error('删除失败，请重试');
      }
    },
  });
};

const handleRestore = (versionId: string) => {
  Modal.confirm({
    title: '确认恢复',
    content: '确认恢复此版本？当前内容将被覆盖。',
    onOk: async () => {
      try {
        await restoreAnnouncementVersion(versionId);
        message.success('版本已恢复');
        await loadAnnouncement();
        await loadHistory();
      } catch {
        message.error('恢复失败，请重试');
      }
    },
  });
};

onMounted(async () => {
  await Promise.all([loadAnnouncement(), loadHistory()]);
});
</script>

<style lang="less" scoped>
.announcement-manage {
  .announcement-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    margin-bottom: 16px;
  }
  .history-section {
    .history-header {
      font-weight: 600;
      margin-bottom: 12px;
    }
  }
}
</style>
