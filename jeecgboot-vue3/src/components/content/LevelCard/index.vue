<template>
  <a-card class="level-card" :body-style="{ padding: '16px' }" hoverable>
    <!-- 骨架屏加载态 -->
    <template v-if="loading">
      <div class="level-card__skeleton">
        <a-skeleton active :paragraph="{ rows: 3 }" />
      </div>
    </template>

    <template v-else>
      <div class="level-card__header">
        <!-- 等级徽章 -->
        <div class="level-card__badge">
          <span class="level-card__badge-level">{{ level }}</span>
        </div>
        <!-- 等级名称 -->
        <div class="level-card__name">{{ levelName || `Lv.${level}` }}</div>
      </div>

      <div class="level-card__body">
        <!-- 当前成长值 -->
        <div class="level-card__value">
          <span class="level-card__value-label">当前成长值:</span>
          <span class="level-card__value-num">{{ growthValue }}</span>
        </div>

        <!-- 进度条 -->
        <div class="level-card__progress">
          <a-progress :percent="levelProgress" :show-info="false" :stroke-color="progressColor" />
        </div>

        <!-- 距下一级提示 -->
        <div v-if="nextLevelGap > 0" class="level-card__gap">
          距下一级还需 <strong>{{ nextLevelGap }}</strong>
        </div>
      </div>
    </template>
  </a-card>
</template>

<script setup lang="ts">
  import { computed } from 'vue';

  const props = withDefaults(
    defineProps<{
      level: number;
      levelName?: string;
      growthValue: number;
      nextLevelGap?: number;
      levelProgress?: number;
      loading?: boolean;
    }>(),
    {
      levelName: '',
      nextLevelGap: 0,
      levelProgress: 0,
      loading: false,
    }
  );

  /** 进度条颜色：接近满级时渐变 */
  const progressColor = computed(() => {
    if (props.levelProgress >= 80) return '#52c41a';
    if (props.levelProgress >= 50) return '#1890ff';
    return '#faad14';
  });
</script>

<style scoped lang="less">
  .level-card {
    &__header {
      display: flex;
      align-items: center;
      margin-bottom: 16px;
      padding: 12px 16px;
      border-radius: 8px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    &__badge {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.2);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 12px;
      flex-shrink: 0;
    }

    &__badge-level {
      font-size: 22px;
      font-weight: 700;
      color: #fff;
    }

    &__name {
      font-size: 18px;
      font-weight: 600;
      color: #fff;
    }

    &__body {
      padding-top: 4px;
    }

    &__value {
      margin-bottom: 8px;
    }

    &__value-label {
      font-size: 13px;
      color: rgba(0, 0, 0, 0.45);
      margin-right: 4px;
    }

    &__value-num {
      font-size: 20px;
      font-weight: 600;
      color: rgba(0, 0, 0, 0.85);
    }

    &__progress {
      margin-bottom: 8px;
    }

    &__gap {
      font-size: 13px;
      color: rgba(0, 0, 0, 0.45);

      strong {
        color: var(--j-global-primary-color, #1890ff);
      }
    }

    &__skeleton {
      min-height: 120px;
    }
  }
</style>
