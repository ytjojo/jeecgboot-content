package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.constant.ContentUserPointSpendConstant;
import org.jeecg.modules.content.user.dto.ContentUserPointLedgerQueryDTO;
import org.jeecg.modules.content.user.entity.ContentUserAuditLog;
import org.jeecg.modules.content.user.entity.ContentUserExchangeGoods;
import org.jeecg.modules.content.user.entity.ContentUserExchangeOrder;
import org.jeecg.modules.content.user.entity.ContentUserFeatureUnlock;
import org.jeecg.modules.content.user.entity.ContentUserPointLedger;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserVirtualGiftRecord;
import org.jeecg.modules.content.user.mapper.ContentUserAuditLogMapper;
import org.jeecg.modules.content.user.mapper.ContentUserExchangeGoodsMapper;
import org.jeecg.modules.content.user.mapper.ContentUserExchangeOrderMapper;
import org.jeecg.modules.content.user.mapper.ContentUserFeatureUnlockMapper;
import org.jeecg.modules.content.user.mapper.ContentUserPointLedgerMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.mapper.ContentUserVirtualGiftRecordMapper;
import org.jeecg.modules.content.user.service.IContentUserPointSpendService;
import org.jeecg.modules.content.user.vo.ContentUserExchangeGoodsVO;
import org.jeecg.modules.content.user.vo.ContentUserFeatureUnlockVO;
import org.jeecg.modules.content.user.vo.ContentUserPointLedgerPageVO;
import org.jeecg.modules.content.user.vo.ContentUserPointLedgerVO;
import org.jeecg.modules.content.user.vo.ContentUserPointSpendResultVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 内容社区积分消费编排服务实现。
 */
@Service
public class ContentUserPointSpendServiceImpl implements IContentUserPointSpendService {

    private static final int MAX_QUANTITY = 999;
    private static final int MAX_PAGE_SIZE = 100;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Resource
    private ContentUserExchangeGoodsMapper exchangeGoodsMapper;

    @Resource
    private ContentUserExchangeOrderMapper exchangeOrderMapper;

    @Resource
    private ContentUserFeatureUnlockMapper featureUnlockMapper;

    @Resource
    private ContentUserVirtualGiftRecordMapper virtualGiftRecordMapper;

    @Resource
    private ContentUserPointLedgerMapper pointLedgerMapper;

    @Resource
    private ContentUserProfileMapper profileMapper;

    @Resource
    private ContentUserAuditLogMapper auditLogMapper;

    @Override
    public List<ContentUserExchangeGoodsVO> listExchangeGoods(String goodsType) {
        LambdaQueryWrapper<ContentUserExchangeGoods> wrapper = Wrappers.<ContentUserExchangeGoods>lambdaQuery()
            .eq(ContentUserExchangeGoods::getEnabled, Boolean.TRUE)
            .orderByAsc(ContentUserExchangeGoods::getGoodsType)
            .orderByAsc(ContentUserExchangeGoods::getGoodsCode);
        if (!isBlank(goodsType)) {
            validateGoodsType(goodsType);
            wrapper.eq(ContentUserExchangeGoods::getGoodsType, goodsType);
        }
        return exchangeGoodsMapper.selectList(wrapper).stream()
            .filter(this::isValidGoods)
            .filter(goods -> isBlank(goodsType) || goodsType.equals(goods.getGoodsType()))
            .map(this::toGoodsVO)
            .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentUserPointSpendResultVO exchangeGoods(String userId, String goodsId, Integer quantity) {
        validateUserId(userId);
        int safeQuantity = validateQuantity(quantity);
        ContentUserExchangeGoods goods = requireGoods(goodsId, safeQuantity);
        if (ContentUserPointSpendConstant.GOODS_TYPE_FEATURE_UNLOCK.equals(goods.getGoodsType())) {
            throw new JeecgBootException("功能解锁请使用功能解锁入口");
        }
        if (ContentUserPointSpendConstant.GOODS_TYPE_VIRTUAL_GIFT.equals(goods.getGoodsType())) {
            throw new JeecgBootException("虚拟礼物请使用赠礼入口");
        }
        int pointCost = calculatePointCost(goods, safeQuantity);
        return spendAndCreateOrder(userId, goods, safeQuantity, pointCost, ContentUserPointSpendConstant.SOURCE_EXCHANGE, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentUserPointSpendResultVO unlockFeature(String userId, String goodsId) {
        validateUserId(userId);
        ContentUserExchangeGoods goods = requireGoods(goodsId, 1);
        if (!ContentUserPointSpendConstant.GOODS_TYPE_FEATURE_UNLOCK.equals(goods.getGoodsType())) {
            throw new JeecgBootException("商品不是功能解锁类型");
        }
        FeatureBenefit benefit = parseFeatureBenefit(goods);
        ContentUserFeatureUnlock existing = selectFeatureUnlock(userId, benefit.featureCode());
        if (isActiveUnlock(existing, new Date())) {
            return new ContentUserPointSpendResultVO()
                .setGoodsId(goods.getId())
                .setGoodsCode(goods.getGoodsCode())
                .setQuantity(1)
                .setPointCost(0)
                .setBalanceAfter(currentBalance(userId))
                .setBenefitStatus(ContentUserPointSpendConstant.BENEFIT_STATUS_GRANTED)
                .setReusedUnlock(Boolean.TRUE);
        }
        int pointCost = calculatePointCost(goods, 1);
        ContentUserPointSpendResultVO result = spendAndCreateOrder(userId, goods, 1, pointCost,
            ContentUserPointSpendConstant.SOURCE_FEATURE_UNLOCK, benefit.featureCode());
        Date now = new Date();
        ContentUserFeatureUnlock unlock = existing == null ? new ContentUserFeatureUnlock() : existing;
        unlock.setUserId(userId)
            .setFeatureCode(benefit.featureCode())
            .setSourceOrderId(result.getOrderId())
            .setValidFrom(now)
            .setValidUntil(benefit.validUntil(now))
            .setEnabled(Boolean.TRUE);
        if (existing == null) {
            unlock.setId(uuid());
            featureUnlockMapper.insert(unlock);
        } else {
            featureUnlockMapper.updateById(unlock);
        }
        return result.setReusedUnlock(Boolean.FALSE);
    }

    @Override
    public ContentUserFeatureUnlockVO getFeatureUnlock(String userId, String featureCode) {
        validateUserId(userId);
        if (isBlank(featureCode)) {
            throw new JeecgBootException("功能编码不能为空");
        }
        ContentUserFeatureUnlock unlock = selectFeatureUnlock(userId, featureCode);
        if (unlock == null) {
            return null;
        }
        return new ContentUserFeatureUnlockVO()
            .setFeatureCode(unlock.getFeatureCode())
            .setValidFrom(unlock.getValidFrom())
            .setValidUntil(unlock.getValidUntil())
            .setEnabled(isActiveUnlock(unlock, new Date()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentUserPointSpendResultVO sendVirtualGift(String senderUserId, String receiverUserId, String giftGoodsId,
                                                         Integer quantity, String message) {
        validateUserId(senderUserId);
        if (isBlank(receiverUserId)) {
            throw new JeecgBootException("接收人不能为空");
        }
        if (senderUserId.equals(receiverUserId)) {
            throw new JeecgBootException("不能给自己赠送礼物");
        }
        int safeQuantity = validateQuantity(quantity);
        if (message != null && message.length() > 255) {
            throw new JeecgBootException("赠言长度不能超过255");
        }
        ContentUserExchangeGoods goods = requireGoods(giftGoodsId, safeQuantity);
        if (!ContentUserPointSpendConstant.GOODS_TYPE_VIRTUAL_GIFT.equals(goods.getGoodsType())) {
            throw new JeecgBootException("商品不是虚拟礼物类型");
        }
        int pointCost = calculatePointCost(goods, safeQuantity);
        ContentUserPointSpendResultVO result = spendAndCreateOrder(senderUserId, goods, safeQuantity, pointCost,
            ContentUserPointSpendConstant.SOURCE_VIRTUAL_GIFT, receiverUserId);
        ContentUserVirtualGiftRecord record = new ContentUserVirtualGiftRecord()
            .setSenderUserId(senderUserId)
            .setReceiverUserId(receiverUserId)
            .setGiftGoodsId(goods.getId())
            .setGiftCode(goods.getGoodsCode())
            .setQuantity(safeQuantity)
            .setPointCost(pointCost)
            .setMessage(message)
            .setNotificationStatus(ContentUserPointSpendConstant.NOTIFICATION_STATUS_SENT);
        record.setId(uuid());
        virtualGiftRecordMapper.insert(record);
        if (auditLogMapper != null) {
            auditLogMapper.insert(ContentUserAuditLog.virtualGiftSent(receiverUserId, senderUserId,
                goods.getGoodsCode(), safeQuantity, record.getId()));
        }
        return result;
    }

    @Override
    public ContentUserPointLedgerPageVO queryPointLedger(ContentUserPointLedgerQueryDTO query) {
        validateLedgerQuery(query);
        int current = query.getCurrent() == null ? 1 : query.getCurrent();
        int size = query.getSize() == null ? 10 : query.getSize();
        LambdaQueryWrapper<ContentUserPointLedger> wrapper = Wrappers.<ContentUserPointLedger>lambdaQuery()
            .eq(ContentUserPointLedger::getUserId, query.getUserId())
            .ge(query.getStartTime() != null, ContentUserPointLedger::getCreateTime, query.getStartTime())
            .le(query.getEndTime() != null, ContentUserPointLedger::getCreateTime, query.getEndTime())
            .orderByDesc(ContentUserPointLedger::getCreateTime);
        if (ContentUserPointSpendConstant.LEDGER_TYPE_EARN.equals(query.getType())) {
            wrapper.gt(ContentUserPointLedger::getPointDelta, 0);
        } else if (ContentUserPointSpendConstant.LEDGER_TYPE_SPEND.equals(query.getType())) {
            wrapper.lt(ContentUserPointLedger::getPointDelta, 0);
        }
        IPage<ContentUserPointLedger> page = pointLedgerMapper.selectPage(new Page<>(current, size), wrapper);
        List<ContentUserPointLedger> records = page.getRecords().stream()
            .filter(ledger -> matchesLedgerType(ledger, query.getType()))
            .toList();
        return new ContentUserPointLedgerPageVO()
            .setCurrent(page.getCurrent())
            .setSize(page.getSize())
            .setTotal((long) records.size())
            .setRecords(records.stream().map(this::toLedgerVO).toList());
    }

    private ContentUserPointSpendResultVO spendAndCreateOrder(String userId, ContentUserExchangeGoods goods, int quantity,
                                                              int pointCost, String sourceType, String bizRemark) {
        deductStock(goods, quantity);
        int affectedRows = profileMapper.deductPointIfEnough(userId, pointCost);
        if (affectedRows <= 0) {
            throw new JeecgBootException("积分余额不足");
        }
        int balanceAfter = currentBalance(userId);
        ContentUserExchangeOrder order = new ContentUserExchangeOrder()
            .setOrderNo("PEX" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8))
            .setUserId(userId)
            .setGoodsId(goods.getId())
            .setGoodsCode(goods.getGoodsCode())
            .setQuantity(quantity)
            .setPointCost(pointCost)
            .setOrderStatus(ContentUserPointSpendConstant.ORDER_STATUS_SUCCESS)
            .setBenefitStatus(ContentUserPointSpendConstant.BENEFIT_STATUS_GRANTED);
        order.setId(uuid());
        exchangeOrderMapper.insert(order);
        pointLedgerMapper.insert(ContentUserPointLedger.of(userId, sourceType, order.getId(), -pointCost, "POINT_SPEND")
            .setSourceDescription(goods.getGoodsName())
            .setBalanceAfter(balanceAfter)
            .setRuleSnapshotJson(buildSpendSnapshot(goods, quantity, bizRemark)));
        return new ContentUserPointSpendResultVO()
            .setOrderId(order.getId())
            .setOrderNo(order.getOrderNo())
            .setGoodsId(goods.getId())
            .setGoodsCode(goods.getGoodsCode())
            .setQuantity(quantity)
            .setPointCost(pointCost)
            .setBalanceAfter(balanceAfter)
            .setBenefitStatus(order.getBenefitStatus());
    }

    private void deductStock(ContentUserExchangeGoods goods, int quantity) {
        Integer stock = goods.getStockQuantity();
        if (stock == null) {
            return;
        }
        int affectedRows = exchangeGoodsMapper.deductStockIfEnough(goods.getId(), quantity);
        if (affectedRows <= 0) {
            throw new JeecgBootException("商品库存不足");
        }
    }

    private ContentUserExchangeGoods requireGoods(String goodsId, int quantity) {
        if (isBlank(goodsId)) {
            throw new JeecgBootException("商品ID不能为空");
        }
        ContentUserExchangeGoods goods = exchangeGoodsMapper.selectById(goodsId);
        if (!isValidGoods(goods)) {
            throw new JeecgBootException("商品不存在或不可用");
        }
        if (goods.getStockQuantity() != null && goods.getStockQuantity() < quantity) {
            throw new JeecgBootException("商品库存不足");
        }
        return goods;
    }

    private boolean isValidGoods(ContentUserExchangeGoods goods) {
        return goods != null
            && !isBlank(goods.getId())
            && !isBlank(goods.getGoodsCode())
            && !isBlank(goods.getGoodsName())
            && ContentUserPointSpendConstant.SUPPORTED_GOODS_TYPES.contains(goods.getGoodsType())
            && Boolean.TRUE.equals(goods.getEnabled())
            && goods.getPointPrice() != null
            && goods.getPointPrice() >= 0
            && (goods.getStockQuantity() == null || goods.getStockQuantity() >= 0);
    }

    private FeatureBenefit parseFeatureBenefit(ContentUserExchangeGoods goods) {
        try {
            JsonNode root = isBlank(goods.getBenefitConfigJson())
                ? OBJECT_MAPPER.createObjectNode()
                : OBJECT_MAPPER.readTree(goods.getBenefitConfigJson());
            String featureCode = root.path("featureCode").asText(goods.getGoodsCode());
            if (isBlank(featureCode)) {
                throw new JeecgBootException("功能编码不能为空");
            }
            int validDays = root.path("validDays").asInt(0);
            if (validDays < 0) {
                throw new JeecgBootException("功能有效期配置不合法");
            }
            return new FeatureBenefit(featureCode, validDays);
        } catch (JeecgBootException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JeecgBootException("权益配置JSON不合法");
        }
    }

    private ContentUserFeatureUnlock selectFeatureUnlock(String userId, String featureCode) {
        return featureUnlockMapper.selectList(Wrappers.<ContentUserFeatureUnlock>lambdaQuery()
            .eq(ContentUserFeatureUnlock::getUserId, userId)
            .eq(ContentUserFeatureUnlock::getFeatureCode, featureCode))
            .stream()
            .filter(item -> userId.equals(item.getUserId()) && featureCode.equals(item.getFeatureCode()))
            .findFirst()
            .orElse(null);
    }

    private boolean isActiveUnlock(ContentUserFeatureUnlock unlock, Date now) {
        return unlock != null
            && Boolean.TRUE.equals(unlock.getEnabled())
            && (unlock.getValidUntil() == null || unlock.getValidUntil().after(now));
    }

    private int currentBalance(String userId) {
        ContentUserProfile profile = profileMapper.selectByUserId(userId);
        return profile == null || profile.getPointBalance() == null ? 0 : profile.getPointBalance();
    }

    private int calculatePointCost(ContentUserExchangeGoods goods, int quantity) {
        long cost = (long) goods.getPointPrice() * quantity;
        if (cost > Integer.MAX_VALUE) {
            throw new JeecgBootException("积分消耗超出限制");
        }
        return (int) cost;
    }

    private int validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1 || quantity > MAX_QUANTITY) {
            throw new JeecgBootException("数量不合法");
        }
        return quantity;
    }

    private void validateLedgerQuery(ContentUserPointLedgerQueryDTO query) {
        if (query == null || isBlank(query.getUserId())) {
            throw new JeecgBootException("用户ID不能为空");
        }
        if (!isBlank(query.getType())
            && !ContentUserPointSpendConstant.LEDGER_TYPE_EARN.equals(query.getType())
            && !ContentUserPointSpendConstant.LEDGER_TYPE_SPEND.equals(query.getType())) {
            throw new JeecgBootException("积分明细类型不支持");
        }
        if (query.getStartTime() != null && query.getEndTime() != null && query.getStartTime().after(query.getEndTime())) {
            throw new JeecgBootException("开始时间不能晚于结束时间");
        }
        int current = query.getCurrent() == null ? 1 : query.getCurrent();
        int size = query.getSize() == null ? 10 : query.getSize();
        if (current < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new JeecgBootException("分页参数不合法");
        }
    }

    private void validateUserId(String userId) {
        if (isBlank(userId)) {
            throw new JeecgBootException("用户ID不能为空");
        }
    }

    private void validateGoodsType(String goodsType) {
        if (!ContentUserPointSpendConstant.SUPPORTED_GOODS_TYPES.contains(goodsType)) {
            throw new JeecgBootException("商品类型不支持");
        }
    }

    private ContentUserExchangeGoodsVO toGoodsVO(ContentUserExchangeGoods goods) {
        return new ContentUserExchangeGoodsVO()
            .setGoodsId(goods.getId())
            .setGoodsCode(goods.getGoodsCode())
            .setGoodsName(goods.getGoodsName())
            .setGoodsType(goods.getGoodsType())
            .setPointPrice(goods.getPointPrice())
            .setStockQuantity(goods.getStockQuantity());
    }

    private ContentUserPointLedgerVO toLedgerVO(ContentUserPointLedger ledger) {
        return new ContentUserPointLedgerVO()
            .setId(ledger.getId())
            .setPointDelta(ledger.getPointDelta())
            .setBalanceAfter(ledger.getBalanceAfter())
            .setSourceType(ledger.getSourceType())
            .setSourceDescription(ledger.getSourceDescription())
            .setBizId(ledger.getBizId())
            .setCreateTime(ledger.getCreateTime());
    }

    private boolean matchesLedgerType(ContentUserPointLedger ledger, String type) {
        if (isBlank(type)) {
            return true;
        }
        int delta = ledger.getPointDelta() == null ? 0 : ledger.getPointDelta();
        if (ContentUserPointSpendConstant.LEDGER_TYPE_EARN.equals(type)) {
            return delta > 0;
        }
        if (ContentUserPointSpendConstant.LEDGER_TYPE_SPEND.equals(type)) {
            return delta < 0;
        }
        return false;
    }

    private String buildSpendSnapshot(ContentUserExchangeGoods goods, int quantity, String bizRemark) {
        return "{\"goodsCode\":\"" + goods.getGoodsCode() + "\",\"goodsType\":\"" + goods.getGoodsType()
            + "\",\"quantity\":" + quantity + ",\"remark\":\"" + Objects.toString(bizRemark, "") + "\"}";
    }

    private String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record FeatureBenefit(String featureCode, int validDays) {

        private Date validUntil(Date now) {
            if (validDays <= 0) {
                return null;
            }
            return new Date(now.getTime() + validDays * 24L * 60L * 60L * 1000L);
        }
    }
}
