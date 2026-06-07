import { defHttp } from '/@/utils/http/axios';
import type {
  PointLedgerVO,
  PointLedgerQuery,
  ExchangeGoodsVO,
  ExchangeReq,
  FeatureUnlockReq,
  FeatureUnlockStatusVO,
  GiftSendReq,
} from './point-types';

enum Api {
  ledger = '/api/v1/content/user/growth/point/ledger',
  exchangeGoods = '/api/v1/content/user/growth/point/exchange/goods',
  exchange = '/api/v1/content/user/growth/point/exchange',
  featureUnlock = '/api/v1/content/user/growth/point/feature/unlock',
  giftSend = '/api/v1/content/user/growth/point/gift/send',
}

/** 查询积分明细 */
export const getPointLedger = (params: PointLedgerQuery) =>
  defHttp.get<PointLedgerVO[]>({ url: Api.ledger, params });

/** 查询兑换商品列表 */
export const getExchangeGoods = () =>
  defHttp.get<ExchangeGoodsVO[]>({ url: Api.exchangeGoods });

/** 积分兑换 */
export const createExchange = (data: ExchangeReq) =>
  defHttp.post<void>({ url: Api.exchange, data });

/** 功能解锁 */
export const unlockFeature = (data: FeatureUnlockReq) =>
  defHttp.post<void>({ url: Api.featureUnlock, data });

/** 查询功能解锁状态 */
export const getFeatureUnlockStatus = (featureCode: string) =>
  defHttp.get<FeatureUnlockStatusVO>({ url: Api.featureUnlock, params: { featureCode } });

/** 赠送虚拟礼物 */
export const sendGift = (data: GiftSendReq) =>
  defHttp.post<void>({ url: Api.giftSend, data });
