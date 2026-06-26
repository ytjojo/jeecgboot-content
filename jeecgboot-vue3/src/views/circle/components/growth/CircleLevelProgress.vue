<template>
  <div class="circle-level-progress">
    <!-- 最高等级提示 -->
    <div v-if="isMaxLevel" class="max-level-banner">
      <CheckCircleOutlined class="max-level-icon" />
      <span>已达最高等级 · 标杆圈</span>
    </div>

    <template v-else>
      <!-- 进度条区域 -->
      <div class="progress-header" @click="toggleExpand">
        <div class="progress-score">
          <span class="current-score">{{ levelInfo.growthScore }}</span>
          <span class="score-divider">/</span>
          <span class="target-score">{{ levelInfo.nextLevelThreshold }}</span>
          <span class="score-label">成长分</span>
        </div>
        <div class="expand-trigger">
          <span class="expand-text">{{ expanded ? '收起详情' : '查看详情' }}</span>
          <DownOutlined :class="['expand-icon', { expanded }]" />
        </div>
      </div>

      <a-progress
        :percent="levelInfo.progressPercent"
        :stroke-color="progressColor"
        :show-info="false"
        class="level-progress-bar"
        size="small"
      />

      <div class="progress-labels">
        <span>L{{ levelInfo.level }} {{ levelInfo.levelName }}</span>
        <span>L{{ levelInfo.level + 1 }}</span>
      </div>
    </template>

    <!-- 展开详情 -->
    <a-collapse v-model:activeKey="expandedKey" ghost class="detail-collapse">
      <a-collapse-panel key="detail" :show-arrow="false">
        <!-- 分项指标 -->
        <div class="detail-section">
          <div class="section-title">分项得分</div>
          <div class="score-breakdown">
            <div class="score-item">
              <span class="score-item-label">成员规模</span>
              <a-progress :percent="getScorePercent(levelInfo.memberScore)" :stroke-color="'#1890ff'" :show-info="false" size="small" />
              <span class="score-item-value">{{ levelInfo.memberScore }}</span>
            </div>
            <div class="score-item">
              <span class="score-item-label">内容贡献</span>
              <a-progress :percent="getScorePercent(levelInfo.contentScore)" :stroke-color="'#52c41a'" :show-info="false" size="small" />
              <span class="score-item-value">{{ levelInfo.contentScore }}</span>
            </div>
            <div class="score-item">
              <span class="score-item-label">活跃互动</span>
              <a-progress :percent="getScorePercent(levelInfo.activityScore)" :stroke-color="'#fa8c16'" :show-info="false" size="small" />
              <span class="score-item-value">{{ levelInfo.activityScore }}</span>
            </div>
          </div>
        </div>

        <!-- 升级条件 -->
        <div v-if="levelInfo.nextLevelConditions?.length" class="detail-section">
          <div class="section-title">升级条件</div>
          <div class="conditions-list">
            <div v-for="condition in levelInfo.nextLevelConditions" :key="condition.type" class="condition-item">
              <div class="condition-info">
                <span class="condition-label">{{ condition.label }}</span>
                <span class="condition-gap" v-if="condition.gap > 0">还差 {{ condition.gap }}</span>
                <span class="condition-met" v-else>
                  <CheckOutlined /> 已达成
                </span>
              </div>
              <a-progress
                :percent="Math.min(100, Math.round((condition.current / condition.required) * 100))"
                :stroke-color="condition.gap <= 0 ? '#52c41a' : '#faad14'"
                :show-info="false"
                size="small"
              />
              <div class="condition-values">
                <span>{{ condition.current }}</span>
                <span>/</span>
                <span>{{ condition.required }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 已解锁权益 -->
        <div v-if="levelInfo.benefits?.length" class="detail-section">
          <div class="section-title">圈子权益</div>
          <div class="benefits-list">
            <div v-for="(benefit, index) in levelInfo.benefits" :key="index" :class="['benefit-item', { unlocked: benefit.unlocked }]">
              <span class="benefit-icon">
                <CheckCircleFilled v-if="benefit.unlocked" class="unlocked-icon" />
                <LockOutlined v-else class="locked-icon" />
              </span>
              <span :class="['benefit-name', { 'is-locked': !benefit.unlocked }]">{{ benefit.name }}</span>
            </div>
          </div>
        </div>
      </a-collapse-panel>
    </a-collapse>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';
import { DownOutlined, CheckCircleOutlined, CheckOutlined, CheckCircleFilled, LockOutlined } from '@ant-design/icons-vue';
import type { CircleLevelVO } from '/@/api/content/circle/growth';

const props = defineProps<{
  levelInfo: CircleLevelVO;
}>();

const LEVEL_COLORS: Record<number, string> = {
  1: '#bfbfbf',
  2: '#52c41a',
  3: '#1890ff',
  4: '#fa8c16',
  5: '#faad14',
};

const expanded = ref(false);
const expandedKey = ref<string[]>([]);

const isMaxLevel = computed(() => props.levelInfo.level >= 5);

const progressColor = computed(() => LEVEL_COLORS[props.levelInfo.level] || '#1890ff');

function toggleExpand() {
  expanded.value = !expanded.value;
  expandedKey.value = expanded.value ? ['detail'] : [];
}

function getScorePercent(score: number): number {
  const maxScore = Math.max(
    props.levelInfo.memberScore,
    props.levelInfo.contentScore,
    props.levelInfo.activityScore,
    1,
  );
  return Math.round((score / maxScore) * 100);
}
</script>

<style lang="less" scoped>
.circle-level-progress {
  background: var(--component-background, #fff);
  border-radius: 8px;
  padding: 16px;
}

.max-level-banner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  background: linear-gradient(135deg, #fffbe6 0%, #fff7cc 100%);
  border-radius: 8px;
  color: #d48806;
  font-weight: 500;

  .max-level-icon {
    font-size: 18px;
    color: #faad14;
  }
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  cursor: pointer;

  .progress-score {
    display: flex;
    align-items: baseline;
    gap: 4px;

    .current-score {
      font-size: 24px;
      font-weight: 700;
      color: var(--text-color, #333);
    }

    .score-divider {
      font-size: 16px;
      color: var(--text-color-secondary, #999);
    }

    .target-score {
      font-size: 16px;
      color: var(--text-color-secondary, #999);
    }

    .score-label {
      font-size: 13px;
      color: var(--text-color-tertiary, #aaa);
      margin-left: 4px;
    }
  }

  .expand-trigger {
    display: flex;
    align-items: center;
    gap: 4px;
    color: var(--text-color-secondary, #666);
    font-size: 13px;
    transition: color 0.3s;

    &:hover {
      color: var(--primary-color, #1890ff);
    }

    .expand-icon {
      font-size: 12px;
      transition: transform 0.3s;

      &.expanded {
        transform: rotate(180deg);
      }
    }
  }
}

.level-progress-bar {
  margin-bottom: 8px;

  :deep(.ant-progress-bg) {
    border-radius: 4px;
    height: 8px !important;
  }
}

.progress-labels {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-color-secondary, #999);
}

.detail-collapse {
  margin-top: 16px;
  border-top: 1px solid var(--border-color-base, #f0f0f0);
  padding-top: 16px;

  :deep(.ant-collapse-content-box) {
    padding: 0;
  }
}

.detail-section {
  margin-bottom: 20px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-color, #333);
  margin-bottom: 12px;
}

.score-breakdown {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.score-item {
  display: flex;
  align-items: center;
  gap: 12px;

  .score-item-label {
    width: 64px;
    font-size: 13px;
    color: var(--text-color-secondary, #666);
    flex-shrink: 0;
  }

  :deep(.ant-progress) {
    flex: 1;
    margin: 0;

    .ant-progress-bg {
      border-radius: 3px;
      height: 6px !important;
    }
  }

  .score-item-value {
    width: 40px;
    font-size: 13px;
    font-weight: 500;
    color: var(--text-color, #333);
    text-align: right;
    flex-shrink: 0;
  }
}

.conditions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.condition-item {
  .condition-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 6px;
  }

  .condition-label {
    font-size: 13px;
    color: var(--text-color, #333);
  }

  .condition-gap {
    font-size: 12px;
    color: var(--text-color-secondary, #999);
  }

  .condition-met {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #52c41a;
  }

  :deep(.ant-progress) {
    margin-bottom: 4px;

    .ant-progress-bg {
      border-radius: 3px;
      height: 6px !important;
    }
  }

  .condition-values {
    display: flex;
    gap: 4px;
    font-size: 12px;
    color: var(--text-color-tertiary, #aaa);
  }
}

.benefits-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;

  @media (max-width: 576px) {
    grid-template-columns: 1fr;
  }
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--background-color-base, #f5f5f5);
  border-radius: 6px;
  transition: all 0.3s;

  &.unlocked {
    background: #f6ffed;
  }

  .benefit-icon {
    font-size: 14px;
  }

  .unlocked-icon {
    color: #52c41a;
  }

  .locked-icon {
    color: var(--text-color-tertiary, #ccc);
  }

  .benefit-name {
    font-size: 13px;
    color: var(--text-color, #333);

    &.is-locked {
      color: var(--text-color-tertiary, #aaa);
    }
  }
}

// 移动端响应式适配
@media (max-width: 767px) {
  .progress-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .score-breakdown {
    .score-item {
      flex-wrap: wrap;

      .score-item-label {
        width: 100%;
        margin-bottom: 4px;
      }

      .score-item-value {
        width: 100%;
        text-align: left;
        margin-top: 4px;
      }
    }
  }

  .conditions-list {
    .condition-item {
      .condition-values {
        width: 100%;
        justify-content: flex-end;
      }
    }
  }
}
</style>
