<template>
  <a-drawer
    :open="visible"
    :title="title"
    :width="drawerWidth"
    @close="handleClose"
    :loading="loading"
  >
    <template v-if="detail">
      <a-descriptions :column="2" size="small" bordered>
        <a-descriptions-item label="频道名称">{{ detail.channelName }}</a-descriptions-item>
        <a-descriptions-item label="频道类型">{{ detail.channelType }}</a-descriptions-item>
        <a-descriptions-item label="频道简介" :span="2">
          {{ detail.channelDescription || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="分类">{{ detail.channelCategory || '-' }}</a-descriptions-item>
        <a-descriptions-item label="申请人">{{ detail.applicantName }}</a-descriptions-item>
        <a-descriptions-item label="申请时间">{{ detail.submitTime }}</a-descriptions-item>
        <a-descriptions-item label="申请人信息" :span="2">
          {{ detail.applicantInfo || '-' }}
        </a-descriptions-item>
      </a-descriptions>

      <!-- 频道图标和封面 -->
      <div class="review-detail__media" v-if="detail.channelIcon || detail.channelCover">
        <div class="review-detail__icon" v-if="detail.channelIcon">
          <span class="review-detail__media-label">频道图标：</span>
          <a-image :src="detail.channelIcon" width="64" />
        </div>
        <div class="review-detail__cover" v-if="detail.channelCover">
          <span class="review-detail__media-label">频道封面：</span>
          <a-image :src="detail.channelCover" width="200" />
        </div>
      </div>

      <!-- 历史审核记录 -->
      <div class="review-detail__history" v-if="detail.historyRecords?.length">
        <h4 class="review-detail__section-title">历史审核记录</h4>
        <a-timeline>
          <a-timeline-item
            v-for="record in detail.historyRecords"
            :key="record.time"
            :color="record.result === 'approved' ? 'green' : 'red'"
          >
            <div class="review-detail__history-item">
              <span class="review-detail__history-time">{{ record.time }}</span>
              <span class="review-detail__history-action">
                {{ record.action }} - {{ record.operator }}
              </span>
              <span class="review-detail__history-result">{{ record.result }}</span>
            </div>
          </a-timeline-item>
        </a-timeline>
      </div>
    </template>

    <template v-else-if="!loading">
      <a-empty description="暂无审核详情" />
    </template>
  </a-drawer>
</template>

<script lang="ts" setup>
import { ref, watch, computed } from 'vue';
import { useBreakpoint } from '/@/hooks/event/useBreakpoint';

interface ReviewDetail {
  channelName: string;
  channelType: string;
  channelDescription?: string;
  channelCategory?: string;
  channelIcon?: string;
  channelCover?: string;
  applicantName: string;
  applicantInfo?: string;
  submitTime: string;
  historyRecords: { time: string; action: string; operator: string; result: string }[];
}

const props = withDefaults(defineProps<{
  visible: boolean;
  title?: string;
  detail?: ReviewDetail | null;
  loading?: boolean;
}>(), {
  title: '审核详情',
  detail: null,
  loading: false,
});

const emit = defineEmits<{
  'update:visible': [value: boolean];
}>();

const { widthRef, screenEnum } = useBreakpoint();
const visible = ref(props.visible);

const drawerWidth = computed(() => {
  if (widthRef.value <= screenEnum.MD) return '100%';
  if (widthRef.value <= screenEnum.LG) return '600px';
  return '720px';
});

watch(() => props.visible, (val) => {
  visible.value = val;
});

watch(visible, (val) => {
  emit('update:visible', val);
});

function handleClose() {
  visible.value = false;
}
</script>

<style lang="less" scoped>
.review-detail {
  &__media {
    margin-top: 16px;
    display: flex;
    gap: 24px;
    align-items: flex-start;

    &-label {
      font-weight: 500;
      margin-right: 8px;
    }
  }

  &__icon, &__cover {
    display: flex;
    align-items: center;
  }

  &__history {
    margin-top: 24px;
  }

  &__section-title {
    margin-bottom: 12px;
    font-weight: 500;
  }

  &__history-item {
    display: flex;
    gap: 12px;
    font-size: 13px;
  }

  &__history-time {
    color: var(--text-color-secondary, #999);
  }

  &__history-result {
    color: var(--text-color-secondary, #999);
  }
}
</style>
