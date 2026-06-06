<template>
  <div class="review-waiting">
    <a-result status="info" title="频道审核中" sub-title="您的频道已提交审核，请耐心等待">
      <template #extra>
        <a-descriptions :column="1" bordered size="small" class="review-waiting__info">
          <a-descriptions-item label="频道名称">{{ channelName }}</a-descriptions-item>
          <a-descriptions-item label="当前状态">
            <ChannelStatusTag status="PENDING_REVIEW" />
          </a-descriptions-item>
        </a-descriptions>
      </template>
    </a-result>

    <a-card title="审核进度" :bordered="false" class="review-waiting__timeline">
      <a-timeline>
        <a-timeline-item color="green">频道创建提交</a-timeline-item>
        <a-timeline-item color="blue">审核中 · 预计 {{ estimatedTime }} 内完成</a-timeline-item>
        <a-timeline-item>审核完成</a-timeline-item>
      </a-timeline>
    </a-card>

    <div class="review-waiting__actions">
      <a-button type="primary" @click="goToList">返回我的频道</a-button>
      <a-button @click="showHelp = true">帮助</a-button>
    </div>

    <a-modal v-model:open="showHelp" title="审核帮助" :footer="null">
      <p>频道审核通常在 1-3 个工作日内完成。</p>
      <p>如需加急审核或有其他问题，请联系客服。</p>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted, onUnmounted } from 'vue';
  import { useRouter } from 'vue-router';
  import ChannelStatusTag from '/@/components/jeecg/channel/ChannelStatusTag.vue';
  import { getChannelDetail } from '/@/api/content/channel';

  const props = defineProps<{ channelId: string }>();
  const router = useRouter();
  const channelName = ref('');
  const showHelp = ref(false);
  const estimatedTime = ref('1-3 个工作日');

  let timer: ReturnType<typeof setInterval> | null = null;
  const startTime = Date.now();

  function updateEstimatedTime() {
    const elapsed = Date.now() - startTime;
    const remaining = Math.max(0, 3 * 24 * 60 * 60 * 1000 - elapsed);
    if (remaining > 24 * 60 * 60 * 1000) {
      const days = Math.ceil(remaining / (24 * 60 * 60 * 1000));
      estimatedTime.value = `${days} 天`;
    } else if (remaining > 60 * 60 * 1000) {
      const hours = Math.ceil(remaining / (60 * 60 * 1000));
      estimatedTime.value = `${hours} 小时`;
    } else {
      estimatedTime.value = '不到 1 小时';
    }
  }

  onMounted(async () => {
    try {
      const detail = await getChannelDetail(props.channelId);
      channelName.value = detail.name;
    } catch {
      // ignore
    }
    updateEstimatedTime();
    timer = setInterval(updateEstimatedTime, 60 * 1000);
  });

  onUnmounted(() => {
    if (timer) clearInterval(timer);
  });

  function goToList() {
    router.push('/content/channel/list');
  }
</script>

<style scoped lang="less">
  .review-waiting {
    max-width: 600px;
    margin: 0 auto;

    &__info {
      margin: 16px 0;
    }

    &__timeline {
      margin-top: 24px;
    }

    &__actions {
      margin-top: 24px;
      text-align: center;

      .ant-btn + .ant-btn {
        margin-left: 12px;
      }
    }
  }
</style>
