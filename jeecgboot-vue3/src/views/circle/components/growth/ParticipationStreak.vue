<template>
  <a-card class="streak-card" :bordered="false">
    <template #title>
      <div class="card-title">
        <FireOutlined class="title-icon" />
        连续参与
      </div>
    </template>

    <div v-if="participationDays === 0" class="empty-state">
      <div class="empty-text">发帖、评论或点赞即可开始记录</div>
      <div class="streak-hint">保持每日参与，解锁更多成就</div>
    </div>

    <div v-else class="streak-content">
      <div class="streak-header">
        <div class="streak-number">
          <CountTo :end-val="participationDays" :duration="1200" class="days-count" />
          <span class="days-unit">天</span>
        </div>
        <div class="streak-text">已连续参与</div>
      </div>

      <div class="streak-progress">
        <div class="progress-track">
          <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
          <div class="progress-milestones">
            <div
              v-for="day in 7"
              :key="day"
              class="milestone-dot"
              :class="{ active: participationDays >= day, milestone: day === 3 || day === 7 }"
            >
              {{ day }}
            </div>
          </div>
        </div>
      </div>

      <div v-if="milestoneMessage" class="milestone-tip">
        <GiftOutlined />
        {{ milestoneMessage }}
      </div>
    </div>
  </a-card>
</template>

<script setup lang="ts">
  import { computed } from 'vue';
  import { FireOutlined, GiftOutlined } from '@ant-design/icons-vue';
  import { CountTo } from '/@/components/CountTo';

  interface Props {
    participationDays: number;
  }

  const props = defineProps<Props>();

  const progressPercent = computed(() => {
    return Math.min((props.participationDays / 7) * 100, 100);
  });

  const milestoneMessage = computed(() => {
    if (props.participationDays >= 7) {
      return '太棒了！已达成 7 天连续参与里程碑';
    }
    if (props.participationDays >= 3) {
      return '已达成 3 天连续参与里程碑，继续加油向 7 天迈进！';
    }
    if (props.participationDays >= 1) {
      return `再坚持 ${3 - props.participationDays} 天即可达成 3 天里程碑`;
    }
    return '';
  });
</script>

<style lang="less" scoped>
  .streak-card {
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
      color: #ff4d4f;
      font-size: 18px;
    }
  }

  .empty-state {
    text-align: center;
    padding: 24px 0;

    .empty-text {
      font-size: 15px;
      color: var(--text-color, rgba(0, 0, 0, 0.85));
      margin-bottom: 8px;
    }

    .streak-hint {
      font-size: 13px;
      color: var(--text-color-secondary, rgba(0, 0, 0, 0.45));
    }
  }

  .streak-content {
    padding: 8px 0;
  }

  .streak-header {
    text-align: center;
    margin-bottom: 24px;
  }

  .streak-number {
    display: flex;
    align-items: baseline;
    justify-content: center;
    gap: 4px;
  }

  .days-count {
    font-size: 48px;
    font-weight: 700;
    color: #ff4d4f;
    line-height: 1;
  }

  .days-unit {
    font-size: 20px;
    font-weight: 600;
    color: var(--text-color, rgba(0, 0, 0, 0.85));
  }

  .streak-text {
    font-size: 14px;
    color: var(--text-color-secondary, rgba(0, 0, 0, 0.45));
    margin-top: 4px;
  }

  .streak-progress {
    margin-bottom: 16px;
  }

  .progress-track {
    position: relative;
    height: 32px;
    background: var(--background-color-light, #f5f5f5);
    border-radius: 16px;
    overflow: hidden;
  }

  .progress-fill {
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    background: linear-gradient(90deg, #ff7a45 0%, #ff4d4f 100%);
    border-radius: 16px;
    transition: width 0.8s ease;
  }

  .progress-milestones {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    justify-content: space-between;
    padding: 0 8px;
    align-items: center;
  }

  .milestone-dot {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background: #fff;
    border: 2px solid #d9d9d9;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 11px;
    font-weight: 600;
    color: #bfbfbf;
    z-index: 1;
    transition: all 0.3s ease;

    &.active {
      background: #fff;
      border-color: #ff4d4f;
      color: #ff4d4f;
    }

    &.milestone {
      transform: scale(1.2);

      &.active {
        background: #ff4d4f;
        color: #fff;
        box-shadow: 0 2px 8px rgba(255, 77, 79, 0.4);
      }
    }
  }

  .milestone-tip {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    padding: 10px 16px;
    background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
    border-radius: 8px;
    font-size: 13px;
    color: #d46b08;
    font-weight: 500;
  }
</style>
