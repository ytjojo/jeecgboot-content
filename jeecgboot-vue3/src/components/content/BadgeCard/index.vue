<template>
  <a-card
    :class="['badge-card', `badge-card--${size}`, { 'badge-card--selected': selected, 'badge-card--selectable': selectable }]"
    :body-style="{ padding: size === 'small' ? '8px' : '12px', textAlign: 'center' }"
    hoverable
    @click="handleClick"
  >
    <!-- 选中状态复选框 -->
    <div v-if="selectable" class="badge-card__checkbox">
      <a-checkbox :checked="selected" @click.stop @change="handleSelectChange" />
    </div>

    <!-- 勋章图标 -->
    <div class="badge-card__icon">
      <img
        v-if="!imgError"
        :src="badge.icon"
        :alt="badge.badgeName"
        class="badge-card__img"
        @error="imgError = true"
      />
      <img v-else :src="fallbackSrc" :alt="badge.badgeName" class="badge-card__img" />
    </div>

    <!-- 勋章名称 -->
    <div class="badge-card__name" :title="badge.badgeName">
      {{ badge.badgeName }}
    </div>

    <!-- 状态标签 -->
    <div class="badge-card__status">
      <a-tag v-if="badge.worn" color="blue">佩戴中</a-tag>
      <a-tag v-else-if="badge.earned" color="green">已获得</a-tag>
      <a-tag v-else color="default">未获得</a-tag>
    </div>

    <!-- 稀有度标识 -->
    <div v-if="badge.rarity" class="badge-card__rarity">
      <a-tag :color="rarityColor">{{ badge.rarity }}</a-tag>
    </div>
  </a-card>
</template>

<script setup lang="ts">
  import { ref, computed } from 'vue';
  import type { BadgeDetailVO } from '/@/api/content/growth/badge-types';

  const props = withDefaults(
    defineProps<{
      badge: BadgeDetailVO;
      selectable?: boolean;
      selected?: boolean;
      size?: 'small' | 'medium';
    }>(),
    {
      selectable: false,
      selected: false,
      size: 'medium',
    }
  );

  const emit = defineEmits<{
    (e: 'click', badge: BadgeDetailVO): void;
    (e: 'select', badgeId: string, selected: boolean): void;
  }>();

  const imgError = ref(false);

  /** 根据分类编码生成 fallback 图标路径 */
  const fallbackSrc = computed(() => {
    const code = props.badge.categoryCode || 'achievement';
    return new URL(`/src/assets/icons/badge-fallback/${code}.svg`, import.meta.url).href;
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

  function handleClick() {
    emit('click', props.badge);
  }

  function handleSelectChange() {
    emit('select', props.badge.badgeId, !props.selected);
  }
</script>

<style scoped lang="less">
  .badge-card {
    cursor: pointer;
    transition: all 0.2s;
    position: relative;

    &:hover {
      transform: translateY(-2px);
    }

    &--selected {
      border-color: var(--j-global-primary-color, #1890ff);
      box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
    }

    &__checkbox {
      position: absolute;
      top: 4px;
      right: 4px;
      z-index: 1;
    }

    &__icon {
      display: flex;
      justify-content: center;
      align-items: center;
      margin-bottom: 8px;
    }

    &__img {
      width: 48px;
      height: 48px;
      object-fit: contain;
    }

    &__name {
      font-size: 14px;
      font-weight: 500;
      margin-bottom: 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &__status {
      margin-bottom: 4px;
    }

    &__rarity {
      margin-top: 4px;
    }

    // small 尺寸适配
    &--small {
      .badge-card__img {
        width: 36px;
        height: 36px;
      }

      .badge-card__name {
        font-size: 12px;
      }
    }
  }
</style>
