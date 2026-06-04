<template>
  <div v-if="badges.length > 0" class="badge-display" :class="`badge-display--${size}`">
    <template v-for="badge in visibleBadges" :key="badge.badgeId">
      <a-tooltip :title="badge.badgeName">
        <div class="badge-display__item">
          <img
            v-if="!failedIcons.has(badge.badgeId)"
            :src="badge.icon"
            :alt="badge.badgeName"
            class="badge-display__img"
            @error="handleImgError(badge.badgeId)"
          />
          <img v-else :src="getFallbackSrc(badge)" :alt="badge.badgeName" class="badge-display__img" />
        </div>
      </a-tooltip>
    </template>
    <div v-if="overflowCount > 0" class="badge-display__overflow">+{{ overflowCount }}</div>
  </div>
</template>

<script setup lang="ts">
  import { computed, reactive } from 'vue';
  import type { BadgeDetailVO } from '/@/api/content/growth/badge-types';

  const props = withDefaults(
    defineProps<{
      badges: BadgeDetailVO[];
      size?: 'small' | 'medium';
      maxDisplay?: number;
    }>(),
    {
      size: 'small',
      maxDisplay: 3,
    }
  );

  /** 记录加载失败的图标 */
  const failedIcons = reactive(new Set<string>());

  /** 可见的勋章列表（截断到 maxDisplay） */
  const visibleBadges = computed(() => props.badges.slice(0, props.maxDisplay));

  /** 溢出数量 */
  const overflowCount = computed(() => Math.max(0, props.badges.length - props.maxDisplay));

  /** 图标加载失败时标记 */
  function handleImgError(badgeId: string) {
    failedIcons.add(badgeId);
  }

  /** 根据分类编码生成 fallback 图标路径 */
  function getFallbackSrc(badge: BadgeDetailVO): string {
    const code = badge.categoryCode || 'achievement';
    return new URL(`/src/assets/icons/badge-fallback/${code}.svg`, import.meta.url).href;
  }
</script>

<style scoped lang="less">
  .badge-display {
    display: inline-flex;
    align-items: center;
    gap: 4px;

    &__item {
      flex-shrink: 0;
      border-radius: 50%;
      overflow: hidden;
    }

    &__img {
      display: block;
      object-fit: contain;
    }

    &__overflow {
      flex-shrink: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      background: rgba(0, 0, 0, 0.06);
      color: rgba(0, 0, 0, 0.45);
      font-size: 12px;
      font-weight: 500;
    }

    // small 尺寸
    &--small {
      .badge-display__img {
        width: 24px;
        height: 24px;
      }

      .badge-display__overflow {
        width: 24px;
        height: 24px;
      }
    }

    // medium 尺寸
    &--medium {
      .badge-display__img {
        width: 32px;
        height: 32px;
      }

      .badge-display__overflow {
        width: 32px;
        height: 32px;
      }
    }
  }
</style>
