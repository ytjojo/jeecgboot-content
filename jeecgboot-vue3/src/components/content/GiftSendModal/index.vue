<template>
  <a-modal title="赠送礼物" :open="visible" @update:open="$emit('update:visible', $event)" @cancel="handleClose" :width="420" destroy-on-close>
    <div class="gift-send">
      <!-- 礼物信息 -->
      <div class="gift-send__goods">
        <div class="gift-send__icon">
          <img v-if="goods.icon" :src="goods.icon" :alt="goods.goodsName" />
          <span v-else style="font-size: 32px; line-height: 48px">🎁</span>
        </div>
        <div class="gift-send__info">
          <div class="gift-send__name">{{ goods.goodsName }}</div>
          <div v-if="goods.description" class="gift-send__desc">{{ goods.description }}</div>
          <div class="gift-send__cost">
            单价: <span class="gift-send__point">{{ goods.pointCost }}</span> 积分
          </div>
        </div>
      </div>

      <!-- 接收人 -->
      <div class="gift-send__field">
        <span class="gift-send__label">接收人 ID:</span>
        <a-input v-model:value="receiverUserId" placeholder="请输入接收用户 ID" allow-clear />
      </div>

      <!-- 数量选择 -->
      <div class="gift-send__field">
        <span class="gift-send__label">赠送数量:</span>
        <a-input-number v-model:value="quantity" :min="1" :max="10" :precision="0" style="width: 120px" />
      </div>

      <!-- 留言 -->
      <div class="gift-send__field">
        <span class="gift-send__label">留言 (可选):</span>
        <a-textarea v-model:value="message" placeholder="给 TA 留句话吧" :maxlength="200" :rows="3" show-count />
      </div>

      <!-- 消耗合计 -->
      <div class="gift-send__total">
        <span>消耗积分:</span>
        <span class="gift-send__total-value">{{ totalCost }}</span>
      </div>

      <!-- 余额不足警告 -->
      <a-alert v-if="balance < totalCost" message="积分余额不足" type="warning" show-icon style="margin-top: 12px" />
    </div>

    <template #footer>
      <a-button @click="handleClose">取消</a-button>
      <a-button
        type="primary"
        :disabled="!receiverUserId.trim() || balance < totalCost || quantity < 1"
        @click="handleConfirm"
      >
        确认赠送
      </a-button>
    </template>
  </a-modal>
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
    visible: {
      type: Boolean as PropType<boolean>,
      required: true,
    },
  });

  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void;
    (e: 'confirm', payload: { receiverUserId: string; goodsId: string; quantity: number; message?: string }): void;
  }>();

  const receiverUserId = ref('');
  const quantity = ref(1);
  const message = ref('');

  const totalCost = computed(() => props.goods.pointCost * quantity.value);

  function handleClose() {
    emit('update:visible', false);
    resetForm();
  }

  function handleConfirm() {
    emit('confirm', {
      receiverUserId: receiverUserId.value.trim(),
      goodsId: props.goods.goodsId,
      quantity: quantity.value,
      message: message.value.trim() || undefined,
    });
  }

  function resetForm() {
    receiverUserId.value = '';
    quantity.value = 1;
    message.value = '';
  }
</script>

<style scoped lang="less">
  .gift-send {
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

    &__field {
      margin-bottom: 12px;
    }

    &__label {
      display: block;
      font-size: 14px;
      margin-bottom: 4px;
    }

    &__total {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 12px;
      background: #fafafa;
      border-radius: 4px;
      font-size: 14px;
      margin-top: 12px;
    }

    &__total-value {
      font-size: 18px;
      font-weight: 600;
      color: var(--j-global-primary-color, #1890ff);
    }
  }
</style>
