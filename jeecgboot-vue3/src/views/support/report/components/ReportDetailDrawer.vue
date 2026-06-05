<!-- src/views/support/report/components/ReportDetailDrawer.vue -->
<template>
  <a-drawer
    :open="visible"
    title="举报详情"
    :width="480"
    @close="handleClose"
  >
    <template v-if="detail">
      <a-descriptions :column="1" bordered size="small">
        <a-descriptions-item label="举报编号">{{ detail.reportNo }}</a-descriptions-item>
        <a-descriptions-item label="举报对象">{{ detail.targetSummary }}</a-descriptions-item>
        <a-descriptions-item label="举报类型">{{ detail.reportTypeLabel }}</a-descriptions-item>
        <a-descriptions-item label="举报说明">{{ detail.description || '无' }}</a-descriptions-item>
        <a-descriptions-item label="提交时间">{{ detail.createTime }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="statusColor(detail.status)">
            {{ detail.statusLabel }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item v-if="detail.status === 'processed'" label="处理结果">
          {{ detail.result }}
        </a-descriptions-item>
      </a-descriptions>

      <!-- 证据预览 -->
      <div v-if="detail.evidenceUrls?.length" class="evidence-section">
        <h4>证据材料</h4>
        <a-image-preview-group>
          <a-space>
            <a-image
              v-for="(url, index) in detail.evidenceUrls"
              :key="index"
              :src="url"
              :width="80"
              :height="80"
              style="object-fit: cover; border-radius: 4px"
            />
          </a-space>
        </a-image-preview-group>
      </div>

      <!-- 申诉入口 -->
      <div v-if="detail.status === 'processed'" class="appeal-entry">
        <a-divider />
        <a-alert
          message="对处理结果不满意？"
          description="您可以发起申诉，请求复核处理结果。"
          type="info"
          show-icon
        >
          <template #action>
            <a-button size="small" type="primary" @click="handleAppeal">
              发起申诉
            </a-button>
          </template>
        </a-alert>
      </div>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { getReportDetail, type ReportItem } from '/@/api/support/report';

const props = defineProps<{
  visible: boolean;
  reportId: string;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
}>();

const router = useRouter();
const detail = ref<ReportItem | null>(null);
const loading = ref(false);

watch(
  () => props.visible,
  async (val) => {
    if (val && props.reportId) {
      loading.value = true;
      try {
        const res = await getReportDetail(props.reportId);
        detail.value = res.result;
      } finally {
        loading.value = false;
      }
    }
  }
);

const statusColor = (status: string) => {
  const map: Record<string, string> = {
    pending: 'orange',
    processing: 'blue',
    processed: 'green',
    withdrawn: 'default',
  };
  return map[status] || 'default';
};

const handleAppeal = () => {
  router.push({ path: '/user/appeals/create', query: { reportId: props.reportId } });
  handleClose();
};

const handleClose = () => {
  emit('close');
};
</script>

<style scoped lang="less">
.evidence-section {
  margin-top: 16px;

  h4 {
    margin-bottom: 12px;
    font-weight: 500;
  }
}

.appeal-entry {
  margin-top: 16px;
}
</style>
