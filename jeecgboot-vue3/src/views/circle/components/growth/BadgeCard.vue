<template>
  <a-card
    :class="[
      'badge-card',
      {
        'badge-earned': badge.earned && !isRevoked,
        'badge-close': badge.status === 'CLOSE' && !badge.earned && !isRevoked,
        'badge-unearned': !badge.earned && badge.status !== 'CLOSE' && !isRevoked,
        'badge-revoked': isRevoked,
      },
    ]"
    :hoverable="!isRevoked"
    :bordered="true"
    @click="handleClick"
  >
    <div class="badge-content">
      <div class="badge-icon-wrapper">
        <img
          :src="badge.iconUrl"
          :alt="badge.name"
          class="badge-icon"
          :class="{ 'icon-disabled': !badge.earned || isRevoked }"
        />
        <a-tag v-if="isRevoked" color="default" class="revoked-tag">已撤销</a-tag>
      </div>

      <div class="badge-info">
        <div class="badge-name">{{ badge.name }}</div>

        <template v-if="isRevoked">
          <div class="badge-date text-muted">已撤销</div>
        </template>

        <template v-else-if="badge.earned">
          <div class="badge-date">{{ formatDate(badge.earnedDate) }}</div>
        </template>

        <template v-else>
          <div class="badge-condition">{{ badge.conditionDesc }}</div>
          <div class="badge-progress-wrapper" v-if="badge.targetProgress > 0">
            <a-progress
              :percent="progressPercent"
              :show-info="false"
              :stroke-color="badge.status === 'CLOSE' ? '#fa8c16' : undefined"
              size="small"
            />
            <span class="progress-text">{{ badge.currentProgress }}/{{ badge.targetProgress }}</span>
          </div>
          <a-tag v-if="badge.status === 'CLOSE'" color="orange" class="close-tag">即将达成</a-tag>
        </template>
      </div>
    </div>
  </a-card>
</template>

<script setup lang="ts">
  import { computed } from 'vue';
  import type { AchievementVO } from '/@/api/content/circle/growth';

  interface BadgeProps {
    badge: AchievementVO & { revoked?: boolean };
  }

  const props = defineProps<BadgeProps>();
  const emit = defineEmits<{
    (e: 'click', badge: AchievementVO): void;
  }>();

  const isRevoked = computed(() => props.badge.revoked === true);

  const progressPercent = computed(() => {
    if (props.badge.targetProgress <= 0) return 0;
    const percent = (props.badge.currentProgress / props.badge.targetProgress) * 100;
    return Math.min(100, Math.round(percent));
  });

  function formatDate(dateStr: string | null): string {
    if (!dateStr) return '';
    try {
      const date = new Date(dateStr);
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
      });
    } catch {
      return dateStr;
    }
  }

  function handleClick() {
    if (!isRevoked.value) {
      emit('click', props.badge);
    }
  }
</script>

<style lang="less" scoped>
  .badge-card {
    cursor: pointer;
    transition: all 0.3s ease;
    margin-bottom: 16px;

    &:hover {
      transform: translateY(-2px);
    }

    .badge-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
    }

    .badge-icon-wrapper {
      position: relative;
      margin-bottom: 12px;

      .badge-icon {
        width: 80px;
        height: 80px;
        object-fit: contain;
        transition: filter 0.3s ease;

        &.icon-disabled {
          filter: grayscale(100%) opacity(0.5);
        }
      }

      .revoked-tag {
        position: absolute;
        top: -8px;
        right: -16px;
        text-decoration: line-through;
      }
    }

    .badge-info {
      width: 100%;
    }

    .badge-name {
      font-size: 14px;
      font-weight: 600;
      color: rgba(0, 0, 0, 0.85);
      margin-bottom: 8px;
      line-height: 1.4;
    }

    .badge-date {
      font-size: 12px;
      color: rgba(0, 0, 0, 0.45);
    }

    .text-muted {
      color: rgba(0, 0, 0, 0.25);
      text-decoration: line-through;
    }

    .badge-condition {
      font-size: 12px;
      color: rgba(0, 0, 0, 0.45);
      margin-bottom: 8px;
      line-height: 1.4;
    }

    .badge-progress-wrapper {
      display: flex;
      align-items: center;
      gap: 8px;

      .progress-text {
        font-size: 12px;
        color: rgba(0, 0, 0, 0.45);
        white-space: nowrap;
      }
    }

    .close-tag {
      margin-top: 8px;
    }
  }

  .badge-earned {
    border-color: #52c41a;
    background: linear-gradient(135deg, #f6ffed 0%, #ffffff 100%);
  }

  .badge-close {
    border-color: #fa8c16;
    border-width: 2px;
    box-shadow: 0 0 8px rgba(250, 140, 22, 0.2);
  }

  .badge-unearned {
    border-color: #d9d9d9;
    background: #fafafa;
  }

  .badge-revoked {
    cursor: not-allowed;
    border-color: #d9d9d9;
    background: #f5f5f5;
    opacity: 0.7;

    &:hover {
      transform: none;
    }

    .badge-name {
      text-decoration: line-through;
      color: rgba(0, 0, 0, 0.25);
    }
  }

  @media (max-width: 767px) {
    .badge-content {
      .badge-icon-wrapper {
        margin-bottom: 8px;

        .badge-icon {
          width: 60px;
          height: 60px;
        }
      }
    }

    .badge-name {
      font-size: 13px;
    }

    .badge-condition {
      font-size: 11px;
    }
  }
</style>
