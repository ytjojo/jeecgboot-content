<template>
  <div class="badge-detail">
    <!-- 已获得状态 -->
    <template v-if="badge.earned && !expired">
      <div class="badge-detail__icon">
        <img
          v-if="!imgError"
          :src="badge.icon"
          :alt="badge.badgeName"
          class="badge-detail__img"
          @error="imgError = true"
        />
        <img v-else :src="fallbackSrc" :alt="badge.badgeName" class="badge-detail__img" />
      </div>
      <div class="badge-detail__info">
        <div class="badge-detail__name">{{ badge.badgeName }}</div>
        <div v-if="badge.description" class="badge-detail__desc">{{ badge.description }}</div>
        <div v-if="badge.earnedAt" class="badge-detail__earned-at">获得时间：{{ formattedEarnedAt }}</div>
        <div class="badge-detail__tags">
          <a-tag v-if="badge.rarity" :color="rarityColor">{{ badge.rarity }}</a-tag>
          <a-tag v-if="badge.worn" color="blue">佩戴中</a-tag>
        </div>
        <div v-if="badge.unlockCondition" class="badge-detail__condition">{{ badge.unlockCondition }}</div>
      </div>
    </template>

    <!-- 已过期状态 -->
    <template v-else-if="badge.earned && expired">
      <div class="badge-detail__icon badge-detail__icon--expired">
        <img
          v-if="!imgError"
          :src="badge.icon"
          :alt="badge.badgeName"
          class="badge-detail__img"
          @error="imgError = true"
        />
        <img v-else :src="fallbackSrc" :alt="badge.badgeName" class="badge-detail__img" />
      </div>
      <div class="badge-detail__info">
        <div class="badge-detail__name">{{ badge.badgeName }}</div>
        <div class="badge-detail__expired-warning">
          <a-tag color="warning">已过期</a-tag>
        </div>
        <div v-if="badge.description" class="badge-detail__desc">{{ badge.description }}</div>
        <div v-if="badge.unlockCondition" class="badge-detail__condition">{{ badge.unlockCondition }}</div>
      </div>
    </template>

    <!-- 未获得状态 -->
    <template v-else>
      <div class="badge-detail__icon badge-detail__icon--locked">
        <img
          v-if="!imgError"
          :src="badge.icon"
          :alt="badge.badgeName"
          class="badge-detail__img"
          @error="imgError = true"
        />
        <img v-else :src="fallbackSrc" :alt="badge.badgeName" class="badge-detail__img" />
        <div class="badge-detail__lock-overlay">🔒</div>
      </div>
      <div class="badge-detail__info">
        <div class="badge-detail__name">{{ badge.badgeName }}</div>
        <div v-if="badge.unlockCondition" class="badge-detail__condition">
          <span class="badge-detail__condition-label">获取条件：</span>{{ badge.unlockCondition }}
        </div>
        <div v-if="badge.description" class="badge-detail__desc">{{ badge.description }}</div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed } from 'vue';
  import dayjs from 'dayjs';
  import type { BadgeDetailVO } from '/@/api/content/growth/badge-types';

  const props = defineProps<{
    badge: BadgeDetailVO;
    /** 是否已过期（由外部判断传入） */
    expired?: boolean;
  }>();

  const imgError = ref(false);

  /** 根据分类编码生成 fallback 图标路径 */
  const fallbackSrc = computed(() => {
    const code = props.badge.categoryCode || 'achievement';
    return new URL(`/src/assets/icons/badge-fallback/${code}.svg`, import.meta.url).href;
  });

  /** 格式化获得时间 */
  const formattedEarnedAt = computed(() => {
    if (!props.badge.earnedAt) return '';
    return dayjs(props.badge.earnedAt).format('YYYY-MM-DD HH:mm');
  });

  /** 稀有度颜色映射 */
  const rarityColor = computed(() => {
    const map: Record<string, string> = {
      common: 'default',
      uncommon: 'green',
      rare: 'blue',
      epic: 'purple',
      legendary: 'gold',
    };
    return map[props.badge.rarity?.toLowerCase() || ''] || 'default';
  });
</script>

<style scoped lang="less">
  .badge-detail {
    display: flex;
    align-items: flex-start;
    gap: 16px;
    padding: 16px;

    &__icon {
      flex-shrink: 0;
      position: relative;
      width: 72px;
      height: 72px;
      display: flex;
      align-items: center;
      justify-content: center;

      &--locked {
        filter: grayscale(100%);
        opacity: 0.5;
      }

      &--expired {
        opacity: 0.6;
      }
    }

    &__img {
      width: 72px;
      height: 72px;
      object-fit: contain;
    }

    &__lock-overlay {
      position: absolute;
      bottom: 2px;
      right: 2px;
      font-size: 16px;
      line-height: 1;
    }

    &__info {
      flex: 1;
      min-width: 0;
    }

    &__name {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 8px;
      color: rgba(0, 0, 0, 0.85);
    }

    &__desc {
      font-size: 14px;
      color: rgba(0, 0, 0, 0.65);
      margin-bottom: 8px;
      line-height: 1.5;
    }

    &__earned-at {
      font-size: 13px;
      color: rgba(0, 0, 0, 0.45);
      margin-bottom: 8px;
    }

    &__tags {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
      margin-bottom: 8px;
    }

    &__condition {
      font-size: 13px;
      color: rgba(0, 0, 0, 0.45);
      line-height: 1.5;
    }

    &__condition-label {
      font-weight: 500;
      color: rgba(0, 0, 0, 0.65);
    }

    &__expired-warning {
      margin-bottom: 8px;
    }
  }
</style>
