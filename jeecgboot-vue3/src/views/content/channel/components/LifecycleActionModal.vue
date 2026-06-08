<template>
  <a-modal
    v-model:open="visible"
    :title="title"
    :confirmLoading="confirmLoading"
    :okText="okText"
    :okButtonProps="{ danger: isHighRisk }"
    @ok="handleConfirm"
    @cancel="handleCancel"
    :maskClosable="false"
  >
    <div class="lifecycle-action-modal">
      <div class="lifecycle-action-modal__info" v-if="impactDescription">
        <a-alert :message="impactDescription" type="warning" show-icon />
      </div>

      <!-- 频道名称确认（永久关闭操作） -->
      <div class="lifecycle-action-modal__name-confirm" v-if="requireChannelNameConfirm">
        <a-form-item label="请输入频道名称确认" required>
          <a-input
            v-model:value="channelNameConfirm"
            :placeholder="`请输入「${channelName}」确认`"
          />
        </a-form-item>
      </div>

      <!-- 原因输入 -->
      <div class="lifecycle-action-modal__reason">
        <a-form-item :label="reasonLabel" required>
          <a-textarea
            v-model:value="reason"
            :placeholder="`请输入${reasonLabel}（最少10个字符）`"
            :rows="3"
            :maxlength="500"
            showCount
          />
        </a-form-item>
      </div>

      <!-- 合并操作：目标频道选择 -->
      <div class="lifecycle-action-modal__merge" v-if="showMergeTarget">
        <a-form-item label="目标频道" required>
          <a-input
            v-model:value="targetChannelId"
            placeholder="请输入目标频道ID"
          />
        </a-form-item>
        <!-- 影响范围预览 -->
        <div class="lifecycle-action-modal__merge-preview" v-if="mergePreview">
          <a-descriptions :column="2" size="small" bordered>
            <a-descriptions-item label="订阅者迁移">{{ mergePreview.subscriberCount }} 人</a-descriptions-item>
            <a-descriptions-item label="内容迁移">{{ mergePreview.contentCount }} 条</a-descriptions-item>
            <a-descriptions-item label="历史数据处理" :span="2">
              {{ mergePreview.historyDataHandling }}
            </a-descriptions-item>
          </a-descriptions>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue';

interface MergePreview {
  subscriberCount: number;
  contentCount: number;
  historyDataHandling: string;
}

const props = withDefaults(defineProps<{
  visible: boolean;
  title?: string;
  impactDescription?: string;
  isHighRisk?: boolean;
  okText?: string;
  reasonLabel?: string;
  requireChannelNameConfirm?: boolean;
  channelName?: string;
  showMergeTarget?: boolean;
  confirmLoading?: boolean;
}>(), {
  title: '操作确认',
  impactDescription: '',
  isHighRisk: false,
  okText: '确认',
  reasonLabel: '操作原因',
  requireChannelNameConfirm: false,
  channelName: '',
  showMergeTarget: false,
  confirmLoading: false,
});

const emit = defineEmits<{
  'update:visible': [value: boolean];
  confirm: [data: { reason: string; channelNameConfirm?: string; targetChannelId?: string }];
  cancel: [];
}>();

const visible = ref(props.visible);
const reason = ref('');
const channelNameConfirm = ref('');
const targetChannelId = ref('');
const mergePreview = ref<MergePreview | null>(null);

watch(() => props.visible, (val) => {
  visible.value = val;
  if (val) {
    reason.value = '';
    channelNameConfirm.value = '';
    targetChannelId.value = '';
    mergePreview.value = null;
  }
});

watch(visible, (val) => {
  emit('update:visible', val);
});

function handleConfirm() {
  emit('confirm', {
    reason: reason.value,
    channelNameConfirm: channelNameConfirm.value || undefined,
    targetChannelId: targetChannelId.value || undefined,
  });
}

function handleCancel() {
  emit('cancel');
}
</script>

<style lang="less" scoped>
.lifecycle-action-modal {
  &__info {
    margin-bottom: 16px;
  }

  &__name-confirm {
    margin-bottom: 16px;
  }

  &__reason {
    margin-bottom: 16px;
  }

  &__merge {
    margin-bottom: 16px;

    &-preview {
      margin-top: 12px;
    }
  }
}
</style>
