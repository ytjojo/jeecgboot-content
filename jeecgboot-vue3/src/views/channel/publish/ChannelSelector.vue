<template>
  <Modal v-model:visible="visible" title="选择频道" :width="640" @cancel="handleClose">
    <div class="channel-selector">
      <InputSearch
        v-model:value="keyword"
        placeholder="搜索频道"
        class="channel-search"
      />
      <div v-if="selectedChannels.length > 0" class="selected-tags">
        <Tag v-for="ch in selectedChannels" :key="ch.id" closable @close="removeChannel(ch.id)">
          {{ ch.name }}
        </Tag>
        <span class="selected-count">已选 {{ selectedChannels.length }}/{{ maxChannelCount }}</span>
      </div>
      <Spin :spinning="loading">
        <div v-if="filteredChannels.length === 0 && !loading" class="empty-state">
          <Empty description="暂无可发布的频道，去加入或创建频道" />
        </div>
        <div v-else>
          <div v-for="group in channelGroups" :key="group.label" class="channel-group">
            <div class="group-label">{{ group.label }}</div>
            <div class="channel-list">
              <div
                v-for="ch in group.channels"
                :key="ch.id"
                :data-channel-id="ch.id"
                :class="['channel-card', { disabled: !ch.publishable || isMaxReached, selected: isSelected(ch.id) }]"
                @click="handleSelect(ch)"
              >
                <div class="channel-name">{{ ch.name }}</div>
                <div class="channel-meta">
                  <Tag size="small">{{ ch.type }}</Tag>
                  <Tag size="small">{{ ch.userRole }}</Tag>
                  <span v-if="!ch.publishable" class="blocked-reason">{{ ch.reason }}</span>
                  <span v-if="isSelected(ch.id)" class="selected-icon">已选</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </Spin>
    </div>
    <template #footer>
      <Button @click="handleClose">取消</Button>
      <Button type="primary" :disabled="selectedChannels.length === 0" @click="handleConfirm">
        确认选择 ({{ selectedChannels.length }})
      </Button>
    </template>
  </Modal>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, watch } from 'vue';
import { Modal, InputSearch, Tag, Spin, Empty, Button } from 'ant-design-vue';
import { getAvailableChannels } from '/@/api/content/channel/publish';
import { useChannelPublishStore } from '/@/store/modules/channelPublish';

interface Channel {
  id: string;
  name: string;
  type: string;
  userRole: string;
  publishResult: string;
  publishable: boolean;
  reason?: string;
}

const props = defineProps<{
  modelValue: boolean;
  maxChannelCount?: number;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'confirm', channels: Channel[]): void;
}>();

const store = useChannelPublishStore();
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const keyword = ref('');
const loading = ref(false);
const allChannels = ref<Channel[]>([]);
const selectedChannels = ref<Channel[]>([]);
const maxChannelCount = computed(() => props.maxChannelCount || store.maxChannelCount || 5);
const isMaxReached = computed(() => selectedChannels.value.length >= maxChannelCount.value);

const filteredChannels = computed(() => {
  if (!keyword.value) return allChannels.value;
  return allChannels.value.filter((ch) => ch.name.includes(keyword.value));
});

const channelGroups = computed(() => {
  const groups = [
    { label: '我管理的频道', channels: [] as Channel[] },
    { label: '我加入的频道', channels: [] as Channel[] },
  ];
  filteredChannels.value.forEach((ch) => {
    if (ch.userRole === 'admin' || ch.userRole === 'owner') {
      groups[0].channels.push(ch);
    } else {
      groups[1].channels.push(ch);
    }
  });
  return groups.filter((g) => g.channels.length > 0);
});

const isSelected = (id: string) => selectedChannels.value.some((ch) => ch.id === id);

const handleSelect = (ch: Channel) => {
  if (!ch.publishable) return;
  if (isSelected(ch.id)) {
    selectedChannels.value = selectedChannels.value.filter((c) => c.id !== ch.id);
    return;
  }
  if (isMaxReached.value) return;
  selectedChannels.value.push(ch);
};

const removeChannel = (id: string) => {
  selectedChannels.value = selectedChannels.value.filter((c) => c.id !== id);
};

const handleConfirm = () => {
  emit('confirm', selectedChannels.value);
  visible.value = false;
};

const handleClose = () => {
  visible.value = false;
};

onMounted(async () => {
  loading.value = true;
  try {
    const res = await getAvailableChannels();
    allChannels.value = res.list || [];
    store.maxChannelCount = res.maxChannelCount || 5;
  } finally {
    loading.value = false;
  }
});
</script>

<style lang="less" scoped>
.channel-selector {
  .channel-search { margin-bottom: 12px; }
  .selected-tags { margin-bottom: 12px; display: flex; flex-wrap: wrap; gap: 4px; align-items: center;
    .selected-count { margin-left: 8px; color: #999; font-size: 12px; }
  }
  .channel-group { margin-bottom: 16px;
    .group-label { font-weight: 600; margin-bottom: 8px; color: #666; }
  }
  .channel-list { display: flex; flex-wrap: wrap; gap: 8px; }
  .channel-card {
    padding: 8px 12px; border: 1px solid #e8e8e8; border-radius: 6px; cursor: pointer; min-width: 180px;
    &:hover:not(.disabled) { border-color: #1890ff; }
    &.selected { border-color: #1890ff; background: #e6f7ff; }
    &.disabled { opacity: 0.5; cursor: not-allowed; }
    .channel-name { font-weight: 500; margin-bottom: 4px; }
    .channel-meta { display: flex; gap: 4px; align-items: center; font-size: 12px; }
    .blocked-reason { color: #ff4d4f; font-size: 12px; }
  }
}
</style>
