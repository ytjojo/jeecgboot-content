/**
 * 内容社区 - 游戏化积分 类型定义
 *
 * 对接后端积分模块，路径前缀 `/content/user/growth/point/*`。
 */

/** 积分明细查询参数 */
export interface PointLedgerQuery {
  /** 页码 */
  page?: number;
  /** 每页条数 */
  pageSize?: number;
  /** 积分变动类型筛选 */
  changeType?: string;
  /** 开始时间 */
  startTime?: string;
  /** 结束时间 */
  endTime?: string;
}

/** 积分明细 VO */
export interface PointLedgerVO {
  /** 记录 ID */
  ledgerId: string;
  /** 变动类型 */
  changeType: string;
  /** 变动积分（正数为获得，负数为消费） */
  amount: number;
  /** 变动后余额 */
  balance: number;
  /** 变动说明 */
  remark?: string;
  /** 关联业务 ID */
  refId?: string;
  /** 创建时间 */
  createdAt: string;
}

/** 兑换商品 VO */
export interface ExchangeGoodsVO {
  /** 商品 ID */
  goodsId: string;
  /** 商品名称 */
  goodsName: string;
  /** 商品图标 */
  icon?: string;
  /** 商品描述 */
  description?: string;
  /** 所需积分 */
  pointCost: number;
  /** 库存数量（-1 表示不限量） */
  stock: number;
  /** 兑换上限（-1 表示不限次） */
  exchangeLimit: number;
}

/** 积分兑换请求 Req */
export interface ExchangeReq {
  /** 商品 ID */
  goodsId: string;
  /** 兑换数量 */
  quantity: number;
  /** 幂等请求 ID（可选） */
  requestId?: string;
}

/** 功能解锁请求 Req */
export interface FeatureUnlockReq {
  /** 功能编码 */
  featureCode: string;
}

/** 功能解锁状态 VO */
export interface FeatureUnlockStatusVO {
  /** 功能编码 */
  featureCode: string;
  /** 是否已解锁 */
  unlocked: boolean;
  /** 解锁时间 */
  unlockedAt?: string;
  /** 是否永久有效 */
  permanent?: boolean;
  /** 到期时间（非永久时有值） */
  expiresAt?: string;
}

/** 赠送虚拟礼物请求 Req */
export interface GiftSendReq {
  /** 接收用户 ID */
  receiverUserId: string;
  /** 礼物商品 ID */
  goodsId: string;
  /** 赠送数量 */
  quantity: number;
  /** 留言（可选） */
  message?: string;
}
