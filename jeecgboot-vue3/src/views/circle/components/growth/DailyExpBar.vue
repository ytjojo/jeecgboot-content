<template>
  <a-card class="daily-exp-card" :bordered="false">
    <template #title>
      <div class="card-title">
        <ThunderboltFilled class="title-icon" />
        今日经验
      </div>
    </template>

    <div class="exp-content">
      <div class="exp-header">
        <div class="exp-text">
          <span v-if="isLimitReached" class="limit-reached">已达今日上限</span>
          <span v-else>今日经验 {{ todayExp }} / {{ dailyExpLimit }}</span>
        </div>
        <div class="exp-percent">{{ percentText }}</div>
      </div>

      <a-progress
        :percent="progressPercent"
        :stroke-color="isLimitReached ? '#52c41a' : { '0%': '#fa8c16', '100%': '#fa541c' }"
        :show-info="false"
        :stroke-width="12"
        class="exp-progress"
      />

      <div class="exp-tip">
        <InfoCircleOutlined />
        <span v-if="isLimitReached">明日继续参与可获得更多经验值</span>
        <span v-else>发帖、评论、点赞均可获得经验值</span>
      </div>
    </div>
  </a-card>
</template>

<script setup lang="ts">
  import { computed } from 'vue';
  import { ThunderboltFilled, InfoCircleOutlined } from '@ant-design/icons-vue';

  interface Props {
    todayExp: number;
    dailyExpLimit: number;
  }

  const props = defineProps<Props>();

  const progressPercent = computed(() => {
    if (props.dailyExpLimit <= 0) return 0;
    return Math.min((props.todayExp / props.dailyExpLimit) * 100, 100);
  });

  const isLimitReached = computed(() => {
    return props.todayExp >= props.dailyExpLimit;
  });

  const percentText = computed(() => {
    return `${Math.round(progressPercent.value)}%`;
  });
</script>

<style lang="less" scoped>
  .daily-exp-card {
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    margin-bottom: 16px;
  }

  .card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;

    .title-icon {
      color: #fa8c16;
      font-size: 18px;
    }
  }

  .exp-content {
    padding: 4px 0;
  }

  .exp-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  .exp-text {
    font-size: 15px;
    font-weight: 500;
    color: var(--text-color, rgba(0, 0, 0, 0.85));

    .limit-reached {
      color: #52c41a;
      font-weight: 600;
    }
  }

  .exp-percent {
    font-size: 18px;
    font-weight: 700;
    color: #fa8c16;
  }

  .exp-progress {
    margin-bottom: 12px;

    :deep(.ant-progress-bg) {
      border-radius: 6px;
      transition: width 0.8s ease;
    }
  }

  .exp-tip {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: var(--text-color-secondary, rgba(0, 0, 0, 0.45));
  }
</style>
