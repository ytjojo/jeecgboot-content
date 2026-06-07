<template>
  <Modal :visible="visible" title="移出频道" :width="560" @cancel="handleClose" @update:visible="(val: boolean) => emit('update:visible', val)">
    <div class="move-channel-dialog">
      <div class="section-label">选择目标频道：</div>
      <Select v-model:value="targetChannelId" placeholder="选择目标频道" style="width: 100%" :options="channelOptions" />
      <div v-if="expectedResult" class="expected-result">
        <InfoCircleOutlined /> 预期结果：{{ expectedResult }}
      </div>
    </div>
    <template #footer>
      <Button @click="handleClose">取消</Button>
      <Button type="primary" :disabled="!targetChannelId" :loading="submitting" @click="handleConfirm">确认移出</Button>
    </template>
  </Modal>
</template>

<script lang="ts" setup>
import { ref, computed, watch } from 'vue';
import { Modal, Select, Button, message } from 'ant-design-vue';
import { InfoCircleOutlined } from '@ant-design/icons-vue';
import { useChannelGovernanceStore } from '/@/store/modules/channelGovernance';
import { getAvailableChannels } from '/@/api/content/channel/publish';

const props = defineProps<{
  visible: boolean;
  contentId: string;
  sourceChannelId: string;
}>();

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'moved'): void;
}>();

const store = useChannelGovernanceStore();
const targetChannelId = ref<string | undefined>(undefined);
const submitting = ref(false);
const channelOptions = ref<{ label: string; value: string; publishResult: string }[]>([]);

watch(() => props.visible, async (val) => {
  if (val) {
    targetChannelId.value = undefined;
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

const expectedResult = computed(() => {
  const ch = channelOptions.value.find((c) => c.value === targetChannelId.value);
  if (!ch) return '';
  return ch.publishResult === 'direct' ? '将直接展示' : '将进入目标频道待审区';
});

const handleConfirm = async () => {
  if (!targetChannelId.value) return;
  submitting.value = true;
  try {
    await store.moveContent(props.contentId, props.sourceChannelId, targetChannelId.value);
    message.success('已移出');
    emit('moved');
    emit('update:visible', false);
  } catch {
    message.error('操作失败，请重试');
  } finally {
    submitting.value = false;
  }
};

const handleClose = () => emit('update:visible', false);
</script>

<style lang="less" scoped>
.move-channel-dialog {
  .section-label { margin-bottom: 8px; color: #666; }
  .expected-result { margin-top: 12px; color: #1890ff; }
}
</style>
