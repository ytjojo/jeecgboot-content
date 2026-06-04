<template>
  <div class="exchange-confirm">
    <!-- 商品信息 -->
    <div class="exchange-confirm__goods">
      <div class="exchange-confirm__icon">
        <img v-if="goods.icon" :src="goods.icon" :alt="goods.goodsName" />
        <span v-else style="font-size: 32px; line-height: 48px">🎁</span>
      </div>
      <div class="exchange-confirm__info">
        <div class="exchange-confirm__name">{{ goods.goodsName }}</div>
        <div v-if="goods.description" class="exchange-confirm__desc">{{ goods.description }}</div>
        <div class="exchange-confirm__cost">
          单价: <span class="exchange-confirm__point">{{ goods.pointCost }}</span> 积分
        </div>
      </div>
    </div>

    <!-- 数量选择 -->
    <div class="exchange-confirm__quantity">
      <span class="exchange-confirm__label">兑换数量:</span>
      <a-input-number v-model:value="quantity" :min="1" :max="10" :precision="0" style="width: 120px" />
    </div>

    <!-- 消耗合计 -->
    <div class="exchange-confirm__total">
      <span>消耗积分:</span>
      <span class="exchange-confirm__total-value">{{ totalCost }}</span>
    </div>

    <!-- 余额不足警告 -->
    <a-alert v-if="balance < totalCost" message="积分余额不足" type="warning" show-icon style="margin-top: 12px" />

    <!-- 确认按钮 -->
    <div class="exchange-confirm__action">
      <a-button type="primary" :disabled="balance < totalCost || quantity < 1" @click="handleConfirm"> 确认兑换 </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed } from 'vue';
  import type { PropType } from 'vue';
  import type { ExchangeGoodsVO } from '/@/api/content/growth/point-types';

  const props = defineProps({
    goods: {
      type: Object as PropType<ExchangeGoodsVO>,
      required: true,
    },
    balance: {
      type: Number as PropType<number>,
      required: true,
    },
  });

  const emit = defineEmits<{
    (e: 'confirm', payload: { goodsId: string; quantity: number; requestId: string }): void;
  }>();

  const quantity = ref(1);

  const totalCost = computed(() => props.goods.pointCost * quantity.value);

  function handleConfirm() {
    emit('confirm', {
      goodsId: props.goods.goodsId,
      quantity: quantity.value,
      requestId: crypto.randomUUID(),
    });
  }
</script>

<style scoped lang="less">
  .exchange-confirm {
    &__goods {
      display: flex;
      gap: 12px;
      margin-bottom: 16px;
    }

    &__icon {
      flex-shrink: 0;
      width: 48px;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;

      img {
        width: 48px;
        height: 48px;
        object-fit: contain;
      }
    }

    &__info {
      flex: 1;
      min-width: 0;
    }

    &__name {
      font-size: 16px;
      font-weight: 500;
      margin-bottom: 4px;
    }

    &__desc {
      font-size: 13px;
      color: #999;
      margin-bottom: 4px;
    }

    &__cost {
      font-size: 14px;
    }

    &__point {
      color: var(--j-global-primary-color, #1890ff);
      font-weight: 600;
    }

    &__quantity {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 12px;
    }

    &__label {
      font-size: 14px;
    }

    &__total {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 12px;
      background: #fafafa;
      border-radius: 4px;
      font-size: 14px;
    }

    &__total-value {
      font-size: 18px;
      font-weight: 600;
      color: var(--j-global-primary-color, #1890ff);
    }

    &__action {
      margin-top: 16px;
      text-align: right;
    }
  }
</style>
