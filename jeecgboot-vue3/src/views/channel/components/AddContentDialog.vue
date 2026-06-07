<template>
  <Modal :visible="visible" title="添加内容到频道" :width="720" @cancel="handleClose" @update:visible="(val: boolean) => emit('update:visible', val)">
    <div class="add-content-dialog">
      <!-- 搜索区 -->
      <div class="search-section">
        <div class="section-label">搜索已发布内容：</div>
        <InputSearch v-model:value="keyword" placeholder="输入关键词搜索" @search="handleSearch" />
      </div>

      <!-- 搜索结果列表 -->
      <div v-if="searchResults.length > 0" class="search-results">
        <div class="section-label">搜索结果：</div>
        <div
          v-for="item in searchResults"
          :key="item.id"
          :class="['result-item', !item.addable ? 'disabled' : '', selectedContent?.id === item.id ? 'selected' : '']"
          @click="item.addable && handleSelectContent(item)"
        >
          <span class="result-title">{{ item.title }}</span>
          <span class="result-meta">{{ item.contentType }} | {{ item.author }} | {{ item.publishTime }}</span>
          <Tag v-if="!item.addable" color="orange">不可添加</Tag>
          <Tag v-if="selectedContent?.id === item.id" color="blue">已选</Tag>
        </div>
      </div>

      <!-- 已选内容预览 -->
      <div v-if="selectedContent" class="selected-preview">
        <div class="section-label">已选内容：</div>
        <div class="preview-card">
          <span class="preview-title">{{ selectedContent.title }}</span>
          <span class="preview-meta">{{ selectedContent.contentType }} | {{ selectedContent.author }}</span>
          <Button size="small" @click="selectedContent = undefined">取消选择</Button>
        </div>
      </div>

      <!-- 目标频道选择 -->
      <div class="channel-section">
        <div class="section-label">目标频道：</div>
        <Select
          v-model:value="selectedChannelIds"
          mode="multiple"
          placeholder="选择目标频道"
          style="width: 100%"
          :options="channelOptions"
        />
      </div>

      <!-- 预期结果展示 -->
      <div v-if="expectedResults.length > 0" class="expected-results">
        <div class="section-label">预期结果：</div>
        <div v-for="r in expectedResults" :key="r.id" class="expected-item">
          <InfoCircleOutlined /> {{ r.name }}：{{ r.publishResult === 'direct' ? '将直接展示' : '将进入待审区' }}
        </div>
      </div>

      <!-- system 入口添加原因 -->
      <div v-if="entryType === 'system'" class="reason-section">
        <div class="section-label">添加原因（必填）：</div>
        <InputTextArea v-model:value="operatorNote" placeholder="请输入添加原因" :rows="3" />
      </div>
    </div>

    <template #footer>
      <Space>
        <Button @click="handleClose">取消</Button>
        <Button type="primary" :disabled="!canSubmit" :loading="submitting" @click="handleSubmit">确认添加</Button>
      </Space>
    </template>
  </Modal>
</template>

<script lang="ts" setup>
import { ref, computed, watch } from 'vue';
import { Modal, Select, Button, Space, Tag, Input, message } from 'ant-design-vue';

const { Search: InputSearch, TextArea: InputTextArea } = Input;
import { InfoCircleOutlined } from '@ant-design/icons-vue';
import { searchAddableContent, addContentToChannel } from '/@/api/content/channel/addContent';
import { getAvailableChannels } from '/@/api/content/channel/publish';

interface SearchItem {
  id: string;
  title: string;
  contentType: string;
  author: string;
  publishTime: string;
  addable: boolean;
}

const props = withDefaults(defineProps<{
  visible: boolean;
  entryType?: 'system' | 'author' | 'owner';
}>(), {
  entryType: 'system',
});

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'added'): void;
}>();

const keyword = ref('');
const searchResults = ref<SearchItem[]>([]);
const selectedContent = ref<SearchItem | undefined>(undefined);
const selectedChannelIds = ref<string[]>([]);
const channelOptions = ref<{ label: string; value: string; publishResult: string }[]>([]);
const operatorNote = ref('');
const submitting = ref(false);

// 重置表单
const resetForm = () => {
  keyword.value = '';
  searchResults.value = [];
  selectedContent.value = undefined;
  selectedChannelIds.value = [];
  operatorNote.value = '';
};

// 加载频道列表
watch(() => props.visible, async (val) => {
  if (val) {
    resetForm();
    try {
      const res = await getAvailableChannels();
      channelOptions.value = (res || []).map((ch: any) => ({
        label: ch.name,
        value: ch.id,
        publishResult: ch.publishResult,
      }));
    } catch {
      message.error('加载频道列表失败');
    }
  }
});

// 预期结果
const expectedResults = computed(() => {
  return selectedChannelIds.value
    .map((id) => channelOptions.value.find((c) => c.value === id))
    .filter(Boolean) as { id: string; name: string; publishResult: string }[];
});

// 表单校验
const canSubmit = computed(() => {
  if (!selectedContent.value) return false;
  if (selectedChannelIds.value.length === 0) return false;
  if (props.entryType === 'system' && !operatorNote.value.trim()) return false;
  return true;
});

// 搜索
const handleSearch = async () => {
  if (!keyword.value.trim()) {
    message.info('请输入搜索关键词');
    return;
  }
  try {
    const res = await searchAddableContent({ keyword: keyword.value });
    searchResults.value = res || [];
  } catch {
    message.error('搜索失败，请重试');
  }
};

// 选择内容
const handleSelectContent = (item: SearchItem) => {
  selectedContent.value = item;
};

// 提交
const handleSubmit = async () => {
  if (!selectedContent.value) return;
  submitting.value = true;
  try {
    await addContentToChannel({
      contentId: selectedContent.value.id,
      channelIds: selectedChannelIds.value,
      operatorNote: props.entryType === 'system' ? operatorNote.value : undefined,
    });
    message.success('添加成功');
    emit('added');
    emit('update:visible', false);
  } catch {
    message.error('添加失败，请重试');
  } finally {
    submitting.value = false;
  }
};

// 关闭
const handleClose = () => emit('update:visible', false);
</script>

<style lang="less" scoped>
.add-content-dialog {
  .search-section,
  .search-results,
  .selected-preview,
  .channel-section,
  .expected-results,
  .reason-section {
    margin-bottom: 16px;
  }
  .section-label {
    margin-bottom: 8px;
    color: #666;
  }
  .result-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 8px 12px;
    border: 1px solid #eee;
    border-radius: 4px;
    margin-bottom: 4px;
    cursor: pointer;
    &.disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
    &.selected {
      border-color: #1890ff;
      background: #e6f7ff;
    }
    .result-title {
      font-weight: 500;
    }
    .result-meta {
      color: #999;
      font-size: 12px;
    }
  }
  .preview-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 8px 12px;
    background: #f5f5f5;
    border-radius: 4px;
    .preview-title {
      font-weight: 500;
    }
    .preview-meta {
      color: #999;
      font-size: 12px;
    }
  }
  .expected-item {
    color: #1890ff;
    margin-bottom: 4px;
  }
}
</style>
