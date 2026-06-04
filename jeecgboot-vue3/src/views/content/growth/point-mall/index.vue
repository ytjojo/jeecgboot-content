<template>
  <div class="point-mall-page">
    <a-page-header title="积分商城" />

    <!-- 积分余额 -->
    <PointBalance :balance="balance" :loading="balanceLoading" style="margin-bottom: 16px" />

    <!-- 商品列表 -->
    <a-spin :spinning="goodsLoading">
      <a-empty v-if="!goodsLoading && exchangeGoods.length === 0" description="暂无兑换商品" />
      <a-row v-else :gutter="[16, 16]">
        <a-col v-for="goods in exchangeGoods" :key="goods.goodsId" :xs="24" :sm="12" :md="8" :lg="6">
          <a-card class="point-mall-page__goods-card" hoverable>
            <div class="point-mall-page__icon">
              <img v-if="goods.icon" :src="goods.icon" :alt="goods.goodsName" />
              <span v-else style="font-size: 40px; line-height: 64px">🎁</span>
            </div>
            <a-card-meta :title="goods.goodsName" :description="goods.description" />
            <div class="point-mall-page__meta">
              <span class="point-mall-page__cost">
                <span class="point-mall-page__cost-value">{{ goods.pointCost }}</span> 积分
              </span>
              <span class="point-mall-page__stock">
                库存: {{ goods.stock === -1 ? '不限' : goods.stock }}
              </span>
            </div>
            <div class="point-mall-page__actions">
              <a-button type="primary" block @click="openExchange(goods)">兑换</a-button>
              <a-button block @click="openGift(goods)" style="margin-top: 8px">赠送</a-button>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </a-spin>

    <!-- 兑换确认弹窗 -->
    <a-modal v-model:open="exchangeVisible" title="确认兑换" :footer="null" destroy-on-close>
      <ExchangeConfirm
        v-if="currentGoods"
        :goods="currentGoods"
        :balance="balance"
        @confirm="handleExchange"
      />
    </a-modal>

    <!-- 赠送弹窗 -->
    <GiftSendModal
      v-if="currentGoods"
      v-model:visible="giftVisible"
      :goods="currentGoods"
      :balance="balance"
      @confirm="handleGift"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted } from 'vue';
  import { message } from 'ant-design-vue';
  import { PointBalance } from '/@/components/content/PointBalance';
  import { ExchangeConfirm } from '/@/components/content/ExchangeConfirm';
  import { GiftSendModal } from '/@/components/content/GiftSendModal';
  import { usePointStore } from '/@/store/modules/point';
  import { useGrowthStore } from '/@/store/modules/growth';
  import type { ExchangeGoodsVO } from '/@/api/content/growth/point-types';

  const pointStore = usePointStore();
  const growthStore = useGrowthStore();

  // 余额
  const balance = ref(0);
  const balanceLoading = ref(false);

  // 商品
  const goodsLoading = ref(false);
  const exchangeGoods = ref<ExchangeGoodsVO[]>([]);

  // 兑换弹窗
  const exchangeVisible = ref(false);
  const currentGoods = ref<ExchangeGoodsVO | null>(null);

  // 赠送弹窗
  const giftVisible = ref(false);

  /** 加载余额 */
  async function loadBalance() {
    balanceLoading.value = true;
    try {
      await growthStore.loadSummary();
      balance.value = growthStore.pointBalance;
      pointStore.setBalance(balance.value);
    } finally {
      balanceLoading.value = false;
    }
  }

  /** 加载兑换商品 */
  async function loadGoods() {
    goodsLoading.value = true;
    try {
      exchangeGoods.value = await pointStore.loadExchangeGoods();
    } finally {
      goodsLoading.value = false;
    }
  }

  /** 打开兑换弹窗 */
  function openExchange(goods: ExchangeGoodsVO) {
    currentGoods.value = goods;
    exchangeVisible.value = true;
  }

  /** 打开赠送弹窗 */
  function openGift(goods: ExchangeGoodsVO) {
    currentGoods.value = goods;
    giftVisible.value = true;
  }

  /** 确认兑换 */
  async function handleExchange(payload: { goodsId: string; quantity: number; requestId: string }) {
    try {
      await pointStore.exchange(payload);
      message.success('兑换成功');
      exchangeVisible.value = false;
      await loadBalance();
    } catch {
      message.error('兑换失败');
    }
  }

  /** 确认赠送 */
  async function handleGift(payload: { receiverUserId: string; goodsId: string; quantity: number; message?: string }) {
    try {
      await pointStore.gift(payload);
      message.success('赠送成功');
      giftVisible.value = false;
      await loadBalance();
    } catch {
      message.error('赠送失败');
    }
  }

  onMounted(() => {
    loadBalance();
    loadGoods();
  });
</script>

<style scoped lang="less">
  .point-mall-page {
    padding: 16px;

    &__goods-card {
      height: 100%;
    }

    &__icon {
      text-align: center;
      margin-bottom: 12px;

      img {
        width: 64px;
        height: 64px;
        object-fit: contain;
      }
    }

    &__meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin: 12px 0;
      font-size: 13px;
    }

    &__cost {
      color: #666;
    }

    &__cost-value {
      font-size: 18px;
      font-weight: 600;
      color: var(--j-global-primary-color, #1890ff);
    }

    &__stock {
      color: #999;
    }

    &__actions {
      margin-top: 8px;
    }
  }
</style>
