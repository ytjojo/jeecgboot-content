<!-- src/views/support/appeal/components/AppealDetailDrawer.vue -->
<template>
  <a-drawer :open="visible" title="申诉详情" :width="480" @close="handleClose">
    <template v-if="detail">
      <a-descriptions :column="1" bordered size="small">
        <a-descriptions-item label="申诉编号">{{ detail.appealNo }}</a-descriptions-item>
        <a-descriptions-item label="申诉类型">{{ detail.appealTypeLabel }}</a-descriptions-item>
        <a-descriptions-item label="关联处罚">{{ detail.relatedSummary }}</a-descriptions-item>
        <a-descriptions-item label="申诉理由">{{ detail.reason }}</a-descriptions-item>
        <a-descriptions-item label="提交时间">{{ detail.createTime }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="statusColor(detail.status)">{{ detail.statusLabel }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item v-if="detail.status === 'reviewing'" label="预计处理时间">
          {{ detail.estimatedTime || '预计 1-3 个工作日' }}
        </a-descriptions-item>
        <a-descriptions-item v-if="detail.status !== 'reviewing'" label="审核结果">
          {{ detail.auditResult }}
        </a-descriptions-item>
        <a-descriptions-item v-if="detail.auditTime" label="审核时间">
          {{ detail.auditTime }}
        </a-descriptions-item>
      </a-descriptions>

      <!-- 附件预览 -->
      <div v-if="detail.attachmentUrls?.length" class="attachment-section">
        <h4>附件材料</h4>
        <a-image-preview-group>
          <a-space>
            <a-image
              v-for="(url, index) in detail.attachmentUrls"
              :key="index"
              :src="url"
              :width="80"
              :height="80"
              style="object-fit: cover; border-radius: 4px"
            />
          </a-space>
        </a-image-preview-group>
      </div>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { getAppealDetail, type AppealItem } from '/@/api/support/appeal';

const props = defineProps<{
  visible: boolean;
  appealId: string;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
}>();

const detail = ref<AppealItem | null>(null);
const loading = ref(false);

watch(
  () => props.visible,
  async (val) => {
    if (val && props.appealId) {
      loading.value = true;
      try {
        const res = await getAppealDetail(props.appealId);
        detail.value = res.result;
      } finally {
        loading.value = false;
      }
    }
  },
);

const statusColor = (status: string) => {
  const map: Record<string, string> = {
    reviewing: 'blue',
    approved: 'green',
    rejected: 'red',
    withdrawn: 'default',
  };
  return map[status] || 'default';
};

const handleClose = () => {
  emit('close');
};
</script>

<style scoped lang="less">
.attachment-section {
  margin-top: 16px;

  h4 {
    margin-bottom: 12px;
    font-weight: 500;
  }
}
</style>
