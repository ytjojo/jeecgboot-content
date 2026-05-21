package org.jeecg.modules.content.user.constant;

/**
 * 内容社区用户域错误码常量。
 */
public interface ContentUserErrorCode {

    /** 用户不存在。 */
    int USER_NOT_FOUND = 5404;

    /** 用户关系不允许执行当前操作。 */
    int RELATION_FORBIDDEN = 5409;

    /** 用户状态流转不合法。 */
    int STATUS_TRANSITION_INVALID = 5410;

    /** 可见性校验不通过。 */
    int VISIBILITY_DENIED = 5411;

    /** 奖励事件参数不合法。 */
    int REWARD_EVENT_INVALID = 5420;

    /** 奖励来源类型不支持。 */
    int REWARD_SOURCE_UNSUPPORTED = 5421;

    /** 奖励金额或成长值不合法。 */
    int REWARD_AMOUNT_INVALID = 5422;

    /** 勋章不存在或不可用。 */
    int BADGE_NOT_FOUND = 5430;

    /** 勋章佩戴请求不合法。 */
    int BADGE_WEAR_INVALID = 5431;

    /** 勋章佩戴数量超过限制。 */
    int BADGE_WEAR_LIMIT_EXCEEDED = 5432;

    /** 勋章回收请求不合法。 */
    int BADGE_RECYCLE_INVALID = 5433;

    /** 积分余额不足。 */
    int POINT_BALANCE_INSUFFICIENT = 5440;

    /** 积分明细查询条件不合法。 */
    int POINT_LEDGER_QUERY_INVALID = 5441;

    /** 兑换商品不存在或不可用。 */
    int EXCHANGE_GOODS_NOT_FOUND = 5450;

    /** 兑换商品已禁用。 */
    int EXCHANGE_GOODS_DISABLED = 5451;

    /** 兑换商品库存不足。 */
    int EXCHANGE_STOCK_INSUFFICIENT = 5452;

    /** 兑换数量不合法。 */
    int EXCHANGE_QUANTITY_INVALID = 5453;

    /** 功能解锁请求不合法。 */
    int FEATURE_UNLOCK_INVALID = 5460;

    /** 礼物接收人不合法。 */
    int GIFT_RECEIVER_INVALID = 5461;

    /** 不允许给自己赠送礼物。 */
    int GIFT_SELF_NOT_ALLOWED = 5462;

    /** 等级配置不合法。 */
    int LEVEL_CONFIG_INVALID = 5470;

    /** 等级权益不支持。 */
    int LEVEL_BENEFIT_UNSUPPORTED = 5471;

    /** 推荐分发权重缺少内容质量分。 */
    int DISTRIBUTION_QUALITY_SCORE_REQUIRED = 5472;

    /** 成长值衰减规则不合法。 */
    int GROWTH_DECAY_RULE_INVALID = 5480;

    /** 成长值降级保护状态不合法。 */
    int GROWTH_DECAY_PROTECTION_INVALID = 5481;

    /** 关注流配置不合法。 */
    int SOCIAL_FEED_SETTING_INVALID = 5490;

    /** 关注推荐查询不合法。 */
    int SOCIAL_RECOMMENDATION_QUERY_INVALID = 5491;

    /** 订阅请求不合法。 */
    int SOCIAL_SUBSCRIPTION_INVALID = 5492;

    /** 订阅关系不存在或无权操作。 */
    int SOCIAL_SUBSCRIPTION_FORBIDDEN = 5493;

    /** 订阅源目录参数不合法。 */
    int SOCIAL_SUBSCRIPTION_SOURCE_INVALID = 5494;

    /** 订阅源不存在或未启用。 */
    int SOCIAL_SUBSCRIPTION_SOURCE_NOT_FOUND = 5495;

    /** 订阅通知配置不合法。 */
    int SOCIAL_SUBSCRIPTION_NOTIFICATION_INVALID = 5496;

    /** 资料更新请求不合法。 */
    int PROFILE_UPDATE_INVALID = 5500;

    /** 资料修改次数超过限制。 */
    int PROFILE_UPDATE_LIMIT_EXCEEDED = 5501;

    /** 资料正在审核中。 */
    int PROFILE_REVIEW_PENDING = 5502;

    /** 主页配置不合法。 */
    int PROFILE_HOMEPAGE_INVALID = 5510;

    /** 认证标识不可见或不存在。 */
    int PROFILE_VERIFICATION_BADGE_NOT_FOUND = 5520;

    /** 隐私设置不合法。 */
    int PROFILE_PRIVACY_INVALID = 5530;

    /** 隐私设置修改次数超过限制。 */
    int PROFILE_PRIVACY_LIMIT_EXCEEDED = 5531;

    /** 资料历史不存在或已过期。 */
    int PROFILE_HISTORY_UNAVAILABLE = 5540;
}
