package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.dto.ContentUserPointLedgerQueryDTO;
import org.jeecg.modules.content.user.vo.ContentUserExchangeGoodsVO;
import org.jeecg.modules.content.user.vo.ContentUserFeatureUnlockVO;
import org.jeecg.modules.content.user.vo.ContentUserPointLedgerPageVO;
import org.jeecg.modules.content.user.vo.ContentUserPointSpendResultVO;

import java.util.List;

/**
 * 内容社区积分消费编排服务。
 */
public interface IContentUserPointSpendService {

    /**
     * 查询可兑换商品。
     */
    List<ContentUserExchangeGoodsVO> listExchangeGoods(String goodsType);

    /**
     * 使用积分兑换商品。
     */
    ContentUserPointSpendResultVO exchangeGoods(String userId, String goodsId, Integer quantity, String requestId);

    /**
     * 使用积分解锁功能；已解锁且仍有效时不重复扣费。
     */
    ContentUserPointSpendResultVO unlockFeature(String userId, String goodsId);

    /**
     * 查询用户功能解锁状态。
     */
    ContentUserFeatureUnlockVO getFeatureUnlock(String userId, String featureCode);

    /**
     * 赠送虚拟礼物。
     */
    ContentUserPointSpendResultVO sendVirtualGift(String senderUserId, String receiverUserId, String giftGoodsId,
                                                  Integer quantity, String message);

    /**
     * 查询积分明细。
     */
    ContentUserPointLedgerPageVO queryPointLedger(ContentUserPointLedgerQueryDTO query);
}
