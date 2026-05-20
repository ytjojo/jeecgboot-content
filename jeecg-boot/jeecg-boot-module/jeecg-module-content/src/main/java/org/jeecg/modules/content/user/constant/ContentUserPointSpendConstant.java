package org.jeecg.modules.content.user.constant;

import java.util.Set;

/**
 * 内容社区积分消耗常量。
 */
public interface ContentUserPointSpendConstant {

    /** 普通权益商品。 */
    String GOODS_TYPE_BENEFIT = "BENEFIT";

    /** 功能解锁商品。 */
    String GOODS_TYPE_FEATURE_UNLOCK = "FEATURE_UNLOCK";

    /** 虚拟礼物商品。 */
    String GOODS_TYPE_VIRTUAL_GIFT = "VIRTUAL_GIFT";

    /** 支持兑换的商品类型。 */
    Set<String> SUPPORTED_GOODS_TYPES = Set.of(GOODS_TYPE_BENEFIT, GOODS_TYPE_FEATURE_UNLOCK, GOODS_TYPE_VIRTUAL_GIFT);

    /** 订单成功。 */
    String ORDER_STATUS_SUCCESS = "SUCCESS";

    /** 权益发放成功。 */
    String BENEFIT_STATUS_GRANTED = "GRANTED";

    /** 通知已发送。 */
    String NOTIFICATION_STATUS_SENT = "SENT";

    /** 兑换消耗来源。 */
    String SOURCE_EXCHANGE = "POINT_EXCHANGE";

    /** 功能解锁来源。 */
    String SOURCE_FEATURE_UNLOCK = "FEATURE_UNLOCK";

    /** 虚拟礼物来源。 */
    String SOURCE_VIRTUAL_GIFT = "VIRTUAL_GIFT";

    /** 明细查询：获取积分。 */
    String LEDGER_TYPE_EARN = "EARN";

    /** 明细查询：消耗积分。 */
    String LEDGER_TYPE_SPEND = "SPEND";
}
