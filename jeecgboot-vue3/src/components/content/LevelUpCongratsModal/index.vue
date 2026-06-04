<template>
  <a-modal
    :open="visible"
    :closable="false"
    :footer="null"
    centered
    width="360px"
    @cancel="handleClose"
  >
    <div class="congrats-modal">
      <!-- 等级徽章 -->
      <div class="congrats-modal__badge">
        <span class="congrats-modal__badge-level">{{ newLevel }}</span>
      </div>

      <!-- 标题 -->
      <h2 class="congrats-modal__title">恭喜升级！</h2>

      <!-- 等级描述 -->
      <p class="congrats-modal__desc">
        你已升级到 <strong>Lv.{{ newLevel }}</strong>
        <template v-if="levelName"> {{ levelName }}</template>
      </p>

      <!-- 新权益列表 -->
      <div v-if="newBenefits.length" class="congrats-modal__benefits">
        <h4 class="congrats-modal__benefits-title">解锁新权益</h4>
        <div v-for="item in newBenefits" :key="item.benefitCode" class="congrats-modal__benefit-item">
          <span class="congrats-modal__benefit-icon">{{ item.icon ? '' : '🏅' }}</span>
          <span class="congrats-modal__benefit-name">{{ item.benefitName }}</span>
        </div>
      </div>

      <!-- 确认按钮 -->
      <a-button type="primary" block size="large" class="congrats-modal__btn" @click="handleClose">
        太棒了！
      </a-button>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
  import type { PropType } from 'vue';
  import type { BenefitItem } from '/@/api/content/growth/types';

  const props = defineProps({
    visible: {
      type: Boolean as PropType<boolean>,
      required: true,
    },
    newLevel: {
      type: Number as PropType<number>,
      required: true,
    },
    levelName: {
      type: String as PropType<string>,
      default: '',
    },
    newBenefits: {
      type: Array as PropType<BenefitItem[]>,
      default: () => [],
    },
  });

  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void;
  }>();

  function handleClose() {
    emit('update:visible', false);
  }
</script>

<style scoped lang="less">
  .congrats-modal {
    text-align: center;
    padding: 16px 0;

    &__badge {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 20px;
      box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
    }

    &__badge-level {
      font-size: 36px;
      font-weight: 700;
      color: #fff;
    }

    &__title {
      font-size: 24px;
      font-weight: 700;
      color: rgba(0, 0, 0, 0.85);
      margin-bottom: 8px;
    }

    &__desc {
      font-size: 15px;
      color: rgba(0, 0, 0, 0.65);
      margin-bottom: 20px;

      strong {
        color: var(--j-global-primary-color, #1890ff);
      }
    }

    &__benefits {
      text-align: left;
      background: #fafafa;
      border-radius: 8px;
      padding: 12px 16px;
      margin-bottom: 20px;
    }

    &__benefits-title {
      font-size: 14px;
      font-weight: 600;
      color: rgba(0, 0, 0, 0.85);
      margin-bottom: 8px;
    }

    &__benefit-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 6px 0;
      font-size: 14px;
      color: rgba(0, 0, 0, 0.65);
    }

    &__benefit-icon {
      font-size: 18px;
    }

    &__btn {
      margin-top: 8px;
    }
  }
</style>
