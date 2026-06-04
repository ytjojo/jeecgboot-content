import { defineStore } from 'pinia';
import { store } from '/@/store';
import type {
  PointLedgerVO,
  PointLedgerQuery,
  ExchangeGoodsVO,
  ExchangeReq,
  FeatureUnlockReq,
  FeatureUnlockStatusVO,
  GiftSendReq,
} from '/@/api/content/growth/point-types';
import {
  getPointLedger,
  getExchangeGoods,
  createExchange,
  unlockFeature,
  getFeatureUnlockStatus,
  sendGift,
} from '/@/api/content/growth/point';

interface PointState {
  /** 积分余额（从成长汇总获取，此处仅缓存） */
  balance: number;
  /** 兑换商品列表（不缓存，每次加载） */
  exchangeGoods: ExchangeGoodsVO[];
}

export const usePointStore = defineStore({
  id: 'app-point',
  state: (): PointState => ({
    balance: 0,
    exchangeGoods: [],
  }),
  getters: {
    getBalance(): number {
      return this.balance;
    },
    getExchangeGoods(): ExchangeGoodsVO[] {
      return this.exchangeGoods;
    },
  },
  actions: {
    /** 设置积分余额（由成长汇总 store 同步） */
    setBalance(balance: number) {
      this.balance = balance;
    },
    /** 查询积分明细 */
    async loadLedger(params: PointLedgerQuery): Promise<PointLedgerVO[]> {
      return await getPointLedger(params);
    },
    /** 加载兑换商品列表 */
    async loadExchangeGoods(): Promise<ExchangeGoodsVO[]> {
      const data = await getExchangeGoods();
      this.exchangeGoods = data;
      return data;
    },
    /** 积分兑换（带幂等 requestId） */
    async exchange(req: ExchangeReq): Promise<void> {
      const data = { ...req };
      if (!data.requestId) {
        data.requestId = crypto.randomUUID();
      }
      await createExchange(data);
    },
    /** 功能解锁 */
    async unlock(req: FeatureUnlockReq): Promise<void> {
      await unlockFeature(req);
    },
    /** 查询功能解锁状态 */
    async queryUnlockStatus(featureCode: string): Promise<FeatureUnlockStatusVO> {
      return await getFeatureUnlockStatus(featureCode);
    },
    /** 赠送虚拟礼物 */
    async gift(req: GiftSendReq): Promise<void> {
      await sendGift(req);
    },
  },
});

export function usePointStoreWithOut() {
  return usePointStore(store);
}
