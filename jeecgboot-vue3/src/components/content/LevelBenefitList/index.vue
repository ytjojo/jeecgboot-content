<template>
  <div class="level-benefit-list">
    <!-- 加载骨架屏 -->
    <template v-if="loading">
      <div v-for="i in 3" :key="i" class="level-benefit-list__item">
        <a-skeleton :active="true" :paragraph="{ rows: 1 }" />
      </div>
    </template>

    <!-- 空状态 -->
    <a-empty v-else-if="!benefits.length" description="暂无权益信息" />

    <!-- 权益列表 -->
    <template v-else>
      <div v-for="item in benefits" :key="item.benefitCode" class="level-benefit-list__item">
        <div class="level-benefit-list__icon">
          <img v-if="item.icon" :src="item.icon" :alt="item.benefitName" />
          <span v-else>🏅</span>
        </div>
        <div class="level-benefit-list__info">
          <div class="level-benefit-list__name">{{ item.benefitName }}</div>
          <div v-if="item.description" class="level-benefit-list__desc">{{ item.description }}</div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
  import type { PropType } from 'vue';
  import type { BenefitItem } from '/@/api/content/growth/types';

  defineProps({
    benefits: {
      type: Array as PropType<BenefitItem[]>,
      required: true,
    },
    loading: {
      type: Boolean as PropType<boolean>,
      default: false,
    },
  });
</script>

<style scoped lang="less">
  .level-benefit-list {
    &__item {
      display: flex;
      gap: 12px;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }
    }

    &__icon {
      flex-shrink: 0;
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;

      img {
        width: 40px;
        height: 40px;
        object-fit: contain;
      }
    }

    &__info {
      flex: 1;
      min-width: 0;
    }

    &__name {
      font-size: 14px;
      font-weight: 500;
      color: #333;
    }

    &__desc {
      font-size: 12px;
      color: #999;
      margin-top: 2px;
    }
  }
</style>
