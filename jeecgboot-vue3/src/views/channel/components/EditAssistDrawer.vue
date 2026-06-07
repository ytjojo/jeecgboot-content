<template>
  <Drawer :visible="visible" title="编辑协助" :width="480" @close="handleClose" @update:visible="(val: boolean) => emit('update:visible', val)">
    <div class="edit-assist-drawer">
      <div class="author-info">
        <div>原作者：{{ content.author }}</div>
        <div>原标题：{{ content.title }}</div>
      </div>
      <Form :model="formData" layout="vertical">
        <Form.Item label="标题" name="title">
          <Input v-model:value="formData.title" />
        </Form.Item>
        <Form.Item label="标签" name="tags">
          <Select v-model:value="formData.tags" mode="tags" placeholder="输入标签" />
        </Form.Item>
        <Form.Item label="摘要" name="summary">
          <Input.TextArea v-model:value="formData.summary" :rows="3" />
        </Form.Item>
        <Form.Item label="修改原因（必填）" name="reason" required>
          <Input.TextArea v-model:value="formData.reason" :rows="2" placeholder="请说明修改原因" />
        </Form.Item>
      </Form>
      <Divider />
      <div class="history-section">
        <div class="section-title">修订历史</div>
        <Timeline v-if="history.length > 0">
          <Timeline.Item v-for="item in history" :key="item.id">
            <div>{{ item.operator }} 修改了 {{ item.field }}</div>
            <div class="history-time">{{ item.time }}</div>
            <div class="history-reason">{{ item.reason }}</div>
          </Timeline.Item>
        </Timeline>
        <Empty v-else description="暂无修订记录" />
      </div>
    </div>
    <template #footer>
      <Space>
        <Button @click="handleClose">取消</Button>
        <Button type="primary" :loading="saving" @click="handleSave">保存</Button>
      </Space>
    </template>
  </Drawer>
</template>

<script lang="ts" setup>
import { ref, reactive, watch } from 'vue';
import { Drawer, Form, Input, Select, Button, Space, Divider, Timeline, Empty, message } from 'ant-design-vue';
import { useChannelGovernanceStore } from '/@/store/modules/channelGovernance';
import { getEditAssistHistory } from '/@/api/content/channel/governance';

const props = defineProps<{
  visible: boolean;
  contentId: string;
  channelId: string;
  content: { title: string; author: string };
}>();

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'saved'): void;
}>();

const store = useChannelGovernanceStore();
const saving = ref(false);
const history = ref<any[]>([]);
const formData = reactive({
  title: '',
  tags: [] as string[],
  summary: '',
  reason: '',
});

watch(() => props.visible, async (val) => {
  if (val && props.contentId) {
    formData.title = props.content.title;
    formData.tags = [];
    formData.summary = '';
    formData.reason = '';
    try {
      const res = await getEditAssistHistory(props.contentId);
      history.value = res || [];
    } catch {
      message.error('加载修订历史失败');
      history.value = [];
    }
  }
});

const handleSave = async () => {
  if (!formData.reason) { message.warning('请填写修改原因'); return; }
  saving.value = true;
  try {
    await store.editAssist({ contentId: props.contentId, channelId: props.channelId, title: formData.title, tags: formData.tags, summary: formData.summary, reason: formData.reason });
    message.success('编辑协助已保存');
    emit('saved');
    emit('update:visible', false);
  } catch {
    message.error('保存失败，请重试');
  } finally {
    saving.value = false;
  }
};

const handleClose = () => emit('update:visible', false);
</script>

<style lang="less" scoped>
.edit-assist-drawer {
  .author-info { background: #f5f5f5; padding: 12px; border-radius: 6px; margin-bottom: 16px; }
  .history-section { .section-title { font-weight: 600; margin-bottom: 12px; } }
  .history-time { color: #999; font-size: 12px; }
  .history-reason { color: #666; font-size: 12px; }
}
</style>
