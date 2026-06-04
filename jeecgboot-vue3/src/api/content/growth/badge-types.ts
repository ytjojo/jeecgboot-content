/**
 * 内容社区 - 游戏化勋章 类型定义
 *
 * 对接后端勋章模块，路径前缀 `/content/user/growth/badge/*`。
 */

/** 勋章分类目录 VO */
export interface BadgeCatalogVO {
  /** 分类编码 */
  categoryCode: string;
  /** 分类名称 */
  categoryName: string;
  /** 分类描述 */
  description?: string;
  /** 该分类下的勋章列表 */
  badges?: BadgeDetailVO[];
}

/** 勋章详情 VO */
export interface BadgeDetailVO {
  /** 勋章 ID */
  badgeId: string;
  /** 勋章名称 */
  badgeName: string;
  /** 勋章图标 URL */
  icon: string;
  /** 勋章描述 */
  description?: string;
  /** 所属分类编码 */
  categoryCode?: string;
  /** 稀有度等级 */
  rarity?: string;
  /** 获得条件说明 */
  unlockCondition?: string;
  /** 是否已获得 */
  earned?: boolean;
  /** 获得时间 */
  earnedAt?: string;
  /** 是否佩戴中 */
  worn?: boolean;
}

/** 佩戴勋章请求 Req */
export interface BadgeWearReq {
  /** 要佩戴的勋章 ID 列表 */
  badgeIds: string[];
}

/** 回收勋章请求 Req */
export interface BadgeRecycleReq {
  /** 目标用户 ID */
  userId: string;
  /** 勋章 ID */
  badgeId: string;
  /** 回收原因 */
  reason?: string;
}

/** 管理端 - 用户勋章查询 VO */
export interface AdminBadgeVO {
  /** 记录 ID */
  id: string;
  /** 用户 ID */
  userId: string;
  /** 勋章 ID */
  badgeId: string;
  /** 勋章名称 */
  badgeName: string;
  /** 获得时间 */
  earnedAt?: string;
  /** 状态：worn=佩戴中, owned=已拥有 */
  status?: string;
}

/** 管理端 - 查询参数 */
export interface AdminBadgeQuery {
  userId?: string;
  badgeName?: string;
  pageNo?: number;
  pageSize?: number;
}
