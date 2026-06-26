<template>
  <a-modal
    :visible="visible"
    :title="null"
    :footer="null"
    :closable="true"
    @cancel="handleClose"
    :width="480"
    centered
  >
    <div v-if="badge" class="badge-detail">
      <div class="detail-header">
        <div class="detail-icon-wrapper">
          <img
            :src="badge.iconUrl"
            :alt="badge.name"
            class="detail-icon"
            :class="{ 'icon-disabled': !badge.earned || isRevoked }"
          />
          <a-tag v-if="isRevoked" color="default" class="revoked-tag">已撤销</a-tag>
        </div>

        <div class="detail-title-area">
          <h3 class="detail-name">{{ badge.name }}</h3>
          <div class="detail-tags">
            <a-tag v-if="badge.earned && !isRevoked" color="success">已获得</a-tag>
            <a-tag v-else-if="badge.status === 'CLOSE' && !isRevoked" color="orange">即将达成</a-tag>
            <a-tag v-else-if="!isRevoked" color="default">未获得</a-tag>
          </div>
        </div>
      </div>

      <a-divider />

      <div class="detail-content">
        <template v-if="isRevoked">
          <a-alert message="该徽章已被撤销" type="warning" show-icon class="revoked-alert" />
        </template>

        <template v-else-if="badge.earned">
          <div class="info-row">
            <span class="info-label">获得时间</span>
            <span class="info-value">{{ formatDate(badge.earnedDate) }}</span>
          </div>
          <div class="info-description" v-if="badge.description">
            <h4 class="desc-title">徽章说明</h4>
            <p class="desc-text">{{ badge.description }}</p>
          </div>
        </template>

        <template v-else>
          <div class="info-row">
            <span class="info-label">达成条件</span>
            <span class="info-value">{{ badge.conditionDesc }}</span>
          </div>
          <div class="progress-section" v-if="badge.targetProgress > 0">
            <div class="progress-label">
              <span>当前进度</span>
              <span class="progress-value-text">
                {{ badge.currentProgress }} / {{ badge.targetProgress }}
              </span>
            </div>
            <a-progress
              :percent="progressPercent"
              :stroke-color="badge.status === 'CLOSE' ? '#fa8c16' : '#1890ff'"
              :show-info="false"
            />
          </div>
          <div class="info-description" v-if="badge.description">
            <h4 class="desc-title">徽章说明</h4>
            <p class="desc-text">{{ badge.description }}</p>
          </div>
        </template>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
  import { computed } from 'vue';
  import type { AchievementVO } from '/@/api/content/circle/growth';

  interface BadgeDetailModalProps {
    visible: boolean;
    badge: (AchievementVO & { revoked?: boolean }) | null;
  }

  const props = defineProps<BadgeDetailModalProps>();
  const emit = defineEmits<{
    (e: 'close'): void;
  }>();

  const isRevoked = computed(() => props.badge?.revoked === true);

  const progressPercent = computed(() => {
    if (!props.badge || props.badge.targetProgress <= 0) return 0;
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

  function handleClose() {
    emit('close');
  }
</script>

<style lang="less" scoped>
  .badge-detail {
    padding: 8px 0;

    .detail-header {
      display: flex;
      align-items: center;
      gap: 20px;
    }

    .detail-icon-wrapper {
      position: relative;
      flex-shrink: 0;

      .detail-icon {
        width: 96px;
        height: 96px;
        object-fit: contain;

        &.icon-disabled {
          filter: grayscale(100%) opacity(0.5);
        }
      }

      .revoked-tag {
        position: absolute;
        top: -8px;
        right: -8px;
        text-decoration: line-through;
      }
    }

    .detail-title-area {
      flex: 1;
    }

    .detail-name {
      margin: 0 0 8px 0;
      font-size: 20px;
      font-weight: 600;
      color: rgba(0, 0, 0, 0.85);
    }

    .detail-tags {
      display: flex;
      gap: 8px;
    }

    .detail-content {
      .info-row {
        display: flex;
        margin-bottom: 16px;

        .info-label {
          width: 80px;
          flex-shrink: 0;
          color: rgba(0, 0, 0, 0.45);
          font-size: 14px;
        }

        .info-value {
          flex: 1;
          color: rgba(0, 0, 0, 0.85);
          font-size: 14px;
        }
      }

      .progress-section {
        margin-bottom: 20px;

        .progress-label {
          display: flex;
          justify-content: space-between;
          margin-bottom: 8px;
          font-size: 14px;
          color: rgba(0, 0, 0, 0.65);

          .progress-value-text {
            color: rgba(0, 0, 0, 0.85);
            font-weight: 500;
          }
        }
      }

      .info-description {
        margin-top: 16px;

        .desc-title {
          margin: 0 0 8px 0;
          font-size: 14px;
          font-weight: 500;
          color: rgba(0, 0, 0, 0.85);
        }

        .desc-text {
          margin: 0;
          font-size: 14px;
          line-height: 1.6;
          color: rgba(0, 0, 0, 0.65);
        }
      }

      .revoked-alert {
        margin-bottom: 16px;
      }
    }
  }
</style>
