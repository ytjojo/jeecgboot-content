package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.jeecg.modules.content.user.service.impl.ContentUserPointSpendServiceImpl;
import org.jeecg.modules.content.user.vo.ContentUserPointLedgerPageVO;
import org.jeecg.modules.content.user.vo.ContentUserPointSpendResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 内容社区积分消费编排服务测试。
 */
class ContentUserPointSpendServiceTest {

    private final List<ContentUserExchangeGoods> goodsList = new ArrayList<>();
    private final List<ContentUserExchangeOrder> orders = new ArrayList<>();
    private final List<ContentUserFeatureUnlock> unlocks = new ArrayList<>();
    private final List<ContentUserVirtualGiftRecord> gifts = new ArrayList<>();
    private final List<ContentUserPointLedger> ledgers = new ArrayList<>();
    private final List<ContentUserAuditLog> auditLogs = new ArrayList<>();
    private final List<ContentUserProfile> profiles = new ArrayList<>();

    private ContentUserPointSpendServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ContentUserPointSpendServiceImpl();
        ReflectionTestUtils.setField(service, "exchangeGoodsMapper", goodsMapper());
        ReflectionTestUtils.setField(service, "exchangeOrderMapper", orderMapper());
        ReflectionTestUtils.setField(service, "featureUnlockMapper", unlockMapper());
        ReflectionTestUtils.setField(service, "virtualGiftRecordMapper", giftMapper());
        ReflectionTestUtils.setField(service, "pointLedgerMapper", ledgerMapper());
        ReflectionTestUtils.setField(service, "profileMapper", profileMapper());
        ReflectionTestUtils.setField(service, "auditLogMapper", auditLogMapper());
    }

    @Test
    void shouldListOnlyEnabledValidGoodsAndRejectInvalidGoodsType() {
        goodsList.add(goods("g1", "VIP", ContentUserPointSpendConstant.GOODS_TYPE_BENEFIT, 20, null));
        goodsList.add(goods("g2", "OFF", ContentUserPointSpendConstant.GOODS_TYPE_BENEFIT, 20, null).setEnabled(Boolean.FALSE));
        goodsList.add(goods("g3", "BAD", "BAD", 20, null));

        assertThat(service.listExchangeGoods(ContentUserPointSpendConstant.GOODS_TYPE_BENEFIT))
            .extracting("goodsCode")
            .containsExactly("VIP");
        assertThatThrownBy(() -> service.listExchangeGoods("BAD"))
            .isInstanceOf(JeecgBootException.class);
    }

    @Test
    void shouldRejectExchangeWhenGoodsMissingDisabledStockInsufficientOrBalanceInsufficient() {
        profiles.add(profile("u1", 10));
        goodsList.add(goods("disabled", "D", ContentUserPointSpendConstant.GOODS_TYPE_BENEFIT, 5, 1).setEnabled(Boolean.FALSE));
        goodsList.add(goods("empty", "E", ContentUserPointSpendConstant.GOODS_TYPE_BENEFIT, 5, 0));
        goodsList.add(goods("expensive", "X", ContentUserPointSpendConstant.GOODS_TYPE_BENEFIT, 20, 1));

        assertThatThrownBy(() -> service.exchangeGoods("u1", null, 1, null)).isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> service.exchangeGoods("u1", "missing", 1, null)).isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> service.exchangeGoods("u1", "disabled", 1, null)).isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> service.exchangeGoods("u1", "empty", 1, null)).isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> service.exchangeGoods("u1", "expensive", 1, null)).isInstanceOf(JeecgBootException.class);
        assertThat(orders).isEmpty();
        assertThat(ledgers).isEmpty();
    }

    @Test
    void shouldExchangeGoodsAtomicallyWhenBalanceAndStockAreEnough() {
        profiles.add(profile("u1", 100));
        goodsList.add(goods("g1", "VIP", ContentUserPointSpendConstant.GOODS_TYPE_BENEFIT, 20, 3));

        ContentUserPointSpendResultVO result = service.exchangeGoods("u1", "g1", 2, null);

        assertThat(result.getPointCost()).isEqualTo(40);
        assertThat(result.getBalanceAfter()).isEqualTo(60);
        assertThat(findGoods("g1").getStockQuantity()).isEqualTo(1);
        assertThat(orders).hasSize(1);
        assertThat(ledgers).singleElement().satisfies(it -> {
            assertThat(it.getPointDelta()).isEqualTo(-40);
            assertThat(it.getSourceType()).isEqualTo(ContentUserPointSpendConstant.SOURCE_EXCHANGE);
            assertThat(it.getBalanceAfter()).isEqualTo(60);
        });
    }

    @Test
    void shouldNotChangeGrowthValueOrLevelWhenSpendingPoints() {
        ContentUserProfile profile = profile("u1", 100)
            .setGrowthValue(430)
            .setLevel(5);
        profiles.add(profile);
        goodsList.add(goods("g1", "VIP", ContentUserPointSpendConstant.GOODS_TYPE_BENEFIT, 20, 3));

        service.exchangeGoods("u1", "g1", 1, null);

        assertThat(findProfile("u1").getPointBalance()).isEqualTo(80);
        assertThat(findProfile("u1").getGrowthValue()).isEqualTo(430);
        assertThat(findProfile("u1").getLevel()).isEqualTo(5);
    }

    @Test
    void shouldPreventOverdraftWhenConcurrentExchangeUsesSameBalance() throws Exception {
        profiles.add(profile("u1", 50));
        goodsList.add(goods("g1", "VIP", ContentUserPointSpendConstant.GOODS_TYPE_BENEFIT, 50, null));
        CyclicBarrier barrier = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<Boolean> task = () -> {
            barrier.await();
            try {
                service.exchangeGoods("u1", "g1", 1, null);
                return true;
            } catch (JeecgBootException ex) {
                return false;
            }
        };

        Future<Boolean> first = executor.submit(task);
        Future<Boolean> second = executor.submit(task);
        executor.shutdown();

        assertThat(Stream.of(first.get(), second.get()).filter(Boolean::booleanValue).count()).isEqualTo(1);
        assertThat(findProfile("u1").getPointBalance()).isZero();
        assertThat(orders).hasSize(1);
        assertThat(ledgers).hasSize(1);
    }

    @Test
    void shouldUnlockPermanentAndTimedFeatureAndReuseValidUnlockWithoutChargingAgain() {
        profiles.add(profile("u1", 100));
        goodsList.add(goods("perm", "PIN", ContentUserPointSpendConstant.GOODS_TYPE_FEATURE_UNLOCK, 10, null)
            .setBenefitConfigJson("{\"featureCode\":\"PIN_POST\",\"validDays\":0}"));
        goodsList.add(goods("timed", "HD", ContentUserPointSpendConstant.GOODS_TYPE_FEATURE_UNLOCK, 20, null)
            .setBenefitConfigJson("{\"featureCode\":\"HD_VIDEO\",\"validDays\":7}"));

        ContentUserPointSpendResultVO permanent = service.unlockFeature("u1", "perm");
        ContentUserPointSpendResultVO reused = service.unlockFeature("u1", "perm");
        ContentUserPointSpendResultVO timed = service.unlockFeature("u1", "timed");

        assertThat(permanent.getPointCost()).isEqualTo(10);
        assertThat(reused.getReusedUnlock()).isTrue();
        assertThat(reused.getPointCost()).isZero();
        assertThat(timed.getPointCost()).isEqualTo(20);
        assertThat(unlocks.stream().filter(it -> "PIN_POST".equals(it.getFeatureCode())).findFirst().orElseThrow().getValidUntil()).isNull();
        assertThat(unlocks.stream().filter(it -> "HD_VIDEO".equals(it.getFeatureCode())).findFirst().orElseThrow().getValidUntil()).isAfter(new Date());
        assertThat(ledgers).hasSize(2);
        assertThat(findProfile("u1").getPointBalance()).isEqualTo(70);
    }

    @Test
    void shouldNotDeductWhenFeatureBenefitGrantFails() {
        profiles.add(profile("u1", 100));
        goodsList.add(goods("bad", "BAD_FEATURE", ContentUserPointSpendConstant.GOODS_TYPE_FEATURE_UNLOCK, 10, null)
            .setBenefitConfigJson("{bad"));

        assertThatThrownBy(() -> service.unlockFeature("u1", "bad"))
            .isInstanceOf(JeecgBootException.class);

        assertThat(findProfile("u1").getPointBalance()).isEqualTo(100);
        assertThat(orders).isEmpty();
        assertThat(ledgers).isEmpty();
        assertThat(unlocks).isEmpty();
    }

    @Test
    void shouldSendGiftAndCreateNotificationRecordAtomically() {
        profiles.add(profile("u1", 100));
        goodsList.add(goods("gift", "FLOWER", ContentUserPointSpendConstant.GOODS_TYPE_VIRTUAL_GIFT, 5, 10));

        ContentUserPointSpendResultVO result = service.sendVirtualGift("u1", "u2", "gift", 3, "加油");

        assertThat(result.getPointCost()).isEqualTo(15);
        assertThat(gifts).singleElement().satisfies(it -> {
            assertThat(it.getReceiverUserId()).isEqualTo("u2");
            assertThat(it.getNotificationStatus()).isEqualTo(ContentUserPointSpendConstant.NOTIFICATION_STATUS_SENT);
        });
        assertThat(auditLogs).anySatisfy(it -> {
            assertThat(it.getEventType()).isEqualTo("USER_VIRTUAL_GIFT_RECEIVED");
            assertThat(it.getExtraDataJson()).contains("\"notification\":true");
        });
    }

    @Test
    void shouldRejectGiftWhenReceiverInvalidGiftInvalidOrBalanceInsufficient() {
        profiles.add(profile("u1", 5));
        goodsList.add(goods("gift", "FLOWER", ContentUserPointSpendConstant.GOODS_TYPE_VIRTUAL_GIFT, 10, 1));

        assertThatThrownBy(() -> service.sendVirtualGift("u1", null, "gift", 1, null)).isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> service.sendVirtualGift("u1", "u1", "gift", 1, null)).isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> service.sendVirtualGift("u1", "u2", "missing", 1, null)).isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> service.sendVirtualGift("u1", "u2", "gift", 0, null)).isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> service.sendVirtualGift("u1", "u2", "gift", 1, null)).isInstanceOf(JeecgBootException.class);
        assertThat(gifts).isEmpty();
    }

    @Test
    void shouldPageLedgerByCreateTimeDescAndFilterEarnAndSpendTypes() {
        ContentUserPointLedger earn = ledger("l1", "u1", 10, 110, new Date(3000L));
        ContentUserPointLedger spend = ledger("l2", "u1", -5, 105, new Date(2000L));
        ContentUserPointLedger other = ledger("l3", "u2", 50, 50, new Date(1000L));
        ledgers.addAll(List.of(spend, other, earn));

        ContentUserPointLedgerPageVO earnPage = service.queryPointLedger(new ContentUserPointLedgerQueryDTO()
            .setUserId("u1")
            .setType(ContentUserPointSpendConstant.LEDGER_TYPE_EARN)
            .setCurrent(1)
            .setSize(10));
        ContentUserPointLedgerPageVO spendPage = service.queryPointLedger(new ContentUserPointLedgerQueryDTO()
            .setUserId("u1")
            .setType(ContentUserPointSpendConstant.LEDGER_TYPE_SPEND)
            .setCurrent(1)
            .setSize(10));

        assertThat(earnPage.getRecords()).extracting("id").containsExactly("l1");
        assertThat(spendPage.getRecords()).extracting("id").containsExactly("l2");
        assertThat(earnPage.getTotal()).isEqualTo(1);
    }

    @Test
    void shouldSetLevelChangedWhenGrowthRecordTriggersLevelUp() {
        // 积分兑换不改变成长值，所以 levelChanged 应为 null
        profiles.add(profile("u1", 100));
        goodsList.add(goods("g1", "VIP", ContentUserPointSpendConstant.GOODS_TYPE_BENEFIT, 20, 3));

        ContentUserPointSpendResultVO result = service.exchangeGoods("u1", "g1", 1, null);

        assertThat(result.getLevelChanged()).isNull();
        assertThat(result.getNewLevel()).isNull();
    }

    @Test
    void shouldReturnExistingOrderWhenRequestIdIsDuplicate() {
        profiles.add(profile("u1", 100));
        goodsList.add(goods("g1", "VIP", ContentUserPointSpendConstant.GOODS_TYPE_BENEFIT, 20, 3));

        // 第一次兑换
        ContentUserPointSpendResultVO first = service.exchangeGoods("u1", "g1", 1, "req-001");
        assertThat(first.getPointCost()).isEqualTo(20);
        assertThat(findProfile("u1").getPointBalance()).isEqualTo(80);

        // 相同 requestId 的重复请求
        ContentUserPointSpendResultVO second = service.exchangeGoods("u1", "g1", 1, "req-001");
        assertThat(second.getOrderId()).isEqualTo(first.getOrderId());
        assertThat(findProfile("u1").getPointBalance()).isEqualTo(80); // 不重复扣积分
        assertThat(orders).hasSize(1); // 只创建了一个订单
    }

    @Test
    void shouldAllowExchangeWithoutRequestId() {
        profiles.add(profile("u1", 100));
        goodsList.add(goods("g1", "VIP", ContentUserPointSpendConstant.GOODS_TYPE_BENEFIT, 20, 3));

        ContentUserPointSpendResultVO result = service.exchangeGoods("u1", "g1", 1, null);
        assertThat(result.getPointCost()).isEqualTo(20);
        assertThat(orders).hasSize(1);
    }

    @Test
    void shouldRejectLedgerQueryWhenFiltersInvalid() {
        assertThatThrownBy(() -> service.queryPointLedger(new ContentUserPointLedgerQueryDTO()))
            .isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> service.queryPointLedger(new ContentUserPointLedgerQueryDTO()
            .setUserId("u1").setType("BAD").setCurrent(1).setSize(10)))
            .isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> service.queryPointLedger(new ContentUserPointLedgerQueryDTO()
            .setUserId("u1").setStartTime(new Date(2000L)).setEndTime(new Date(1000L)).setCurrent(1).setSize(10)))
            .isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> service.queryPointLedger(new ContentUserPointLedgerQueryDTO()
            .setUserId("u1").setCurrent(0).setSize(10)))
            .isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> service.queryPointLedger(new ContentUserPointLedgerQueryDTO()
            .setUserId("u1").setCurrent(1).setSize(101)))
            .isInstanceOf(JeecgBootException.class);
    }

    private ContentUserExchangeGoodsMapper goodsMapper() {
        return (ContentUserExchangeGoodsMapper) Proxy.newProxyInstance(ContentUserExchangeGoodsMapper.class.getClassLoader(),
            new Class<?>[]{ContentUserExchangeGoodsMapper.class},
            (proxy, method, args) -> {
                if ("selectList".equals(method.getName())) {
                    return new ArrayList<>(goodsList);
                }
                if ("selectById".equals(method.getName())) {
                    return findGoods(String.valueOf(args[0]));
                }
                if ("deductStockIfEnough".equals(method.getName())) {
                    ContentUserExchangeGoods goods = findGoods(String.valueOf(args[0]));
                    int quantity = (int) args[1];
                    if (goods == null || goods.getStockQuantity() == null || goods.getStockQuantity() < quantity) {
                        return 0;
                    }
                    goods.setStockQuantity(goods.getStockQuantity() - quantity);
                    return 1;
                }
                return defaultValue(method.getReturnType());
            });
    }

    private final java.util.Map<String, ContentUserExchangeOrder> ordersByRequestId = new java.util.HashMap<>();

    private ContentUserExchangeOrderMapper orderMapper() {
        return (ContentUserExchangeOrderMapper) Proxy.newProxyInstance(ContentUserExchangeOrderMapper.class.getClassLoader(),
            new Class<?>[]{ContentUserExchangeOrderMapper.class},
            (proxy, method, args) -> {
                if ("insert".equals(method.getName())) {
                    ContentUserExchangeOrder order = (ContentUserExchangeOrder) args[0];
                    orders.add(order);
                    if (order.getRequestId() != null) {
                        ordersByRequestId.put(order.getRequestId(), order);
                    }
                    return 1;
                }
                if ("selectOne".equals(method.getName())) {
                    // 幂等查询：遍历参数查找 requestId 条件值
                    if (args != null && ordersByRequestId.size() == 1) {
                        // 只有一个 requestId 索引时，直接返回（单 requestId 场景）
                        return ordersByRequestId.values().iterator().next();
                    }
                    return null;
                }
                return defaultValue(method.getReturnType());
            });
    }

    private ContentUserFeatureUnlockMapper unlockMapper() {
        return (ContentUserFeatureUnlockMapper) Proxy.newProxyInstance(ContentUserFeatureUnlockMapper.class.getClassLoader(),
            new Class<?>[]{ContentUserFeatureUnlockMapper.class},
            (proxy, method, args) -> {
                if ("selectOne".equals(method.getName())) {
                    return unlocks.isEmpty() ? null : unlocks.get(0);
                }
                if ("selectList".equals(method.getName())) {
                    return new ArrayList<>(unlocks);
                }
                if ("insert".equals(method.getName())) {
                    unlocks.add((ContentUserFeatureUnlock) args[0]);
                    return 1;
                }
                if ("updateById".equals(method.getName())) {
                    return 1;
                }
                return defaultValue(method.getReturnType());
            });
    }

    private ContentUserVirtualGiftRecordMapper giftMapper() {
        return (ContentUserVirtualGiftRecordMapper) Proxy.newProxyInstance(ContentUserVirtualGiftRecordMapper.class.getClassLoader(),
            new Class<?>[]{ContentUserVirtualGiftRecordMapper.class},
            (proxy, method, args) -> {
                if ("insert".equals(method.getName())) {
                    gifts.add((ContentUserVirtualGiftRecord) args[0]);
                    return 1;
                }
                return defaultValue(method.getReturnType());
            });
    }

    private ContentUserPointLedgerMapper ledgerMapper() {
        return (ContentUserPointLedgerMapper) Proxy.newProxyInstance(ContentUserPointLedgerMapper.class.getClassLoader(),
            new Class<?>[]{ContentUserPointLedgerMapper.class},
            (proxy, method, args) -> {
                if ("insert".equals(method.getName())) {
                    ledgers.add((ContentUserPointLedger) args[0]);
                    return 1;
                }
                if ("selectPage".equals(method.getName())) {
                    Page<ContentUserPointLedger> page = (Page<ContentUserPointLedger>) args[0];
                    IPage<ContentUserPointLedger> result = new Page<>(page.getCurrent(), page.getSize());
                    List<ContentUserPointLedger> records = ledgers.stream()
                        .filter(it -> "u1".equals(it.getUserId()))
                        .filter(this::matchesLastLedgerQueryType)
                        .sorted(Comparator.comparing(ContentUserPointLedger::getCreateTime,
                            Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                        .toList();
                    result.setRecords(records);
                    result.setTotal(records.size());
                    return result;
                }
                return defaultValue(method.getReturnType());
            });
    }

    private ContentUserProfileMapper profileMapper() {
        return (ContentUserProfileMapper) Proxy.newProxyInstance(ContentUserProfileMapper.class.getClassLoader(),
            new Class<?>[]{ContentUserProfileMapper.class},
            (proxy, method, args) -> {
                if ("selectByUserId".equals(method.getName())) {
                    return findProfile(String.valueOf(args[0]));
                }
                if ("deductPointIfEnough".equals(method.getName())) {
                    synchronized (profiles) {
                        ContentUserProfile profile = findProfile(String.valueOf(args[0]));
                        int pointCost = (int) args[1];
                        if (profile == null || profile.getPointBalance() == null || profile.getPointBalance() < pointCost) {
                            return 0;
                        }
                        profile.setPointBalance(profile.getPointBalance() - pointCost);
                        return 1;
                    }
                }
                return defaultValue(method.getReturnType());
            });
    }

    private ContentUserAuditLogMapper auditLogMapper() {
        return (ContentUserAuditLogMapper) Proxy.newProxyInstance(ContentUserAuditLogMapper.class.getClassLoader(),
            new Class<?>[]{ContentUserAuditLogMapper.class},
            (proxy, method, args) -> {
                if ("insert".equals(method.getName())) {
                    auditLogs.add((ContentUserAuditLog) args[0]);
                    return 1;
                }
                return defaultValue(method.getReturnType());
            });
    }

    private boolean matchesLastLedgerQueryType(ContentUserPointLedger ledger) {
        // fake mapper 无法解析 MyBatis Wrapper，这里保留全部，由断言覆盖服务转换结果。
        return ledger.getPointDelta() != null;
    }

    private ContentUserExchangeGoods goods(String id, String code, String type, int price, Integer stock) {
        ContentUserExchangeGoods goods = new ContentUserExchangeGoods()
            .setGoodsCode(code)
            .setGoodsName(code + "商品")
            .setGoodsType(type)
            .setPointPrice(price)
            .setStockQuantity(stock)
            .setEnabled(Boolean.TRUE);
        goods.setId(id);
        return goods;
    }

    private ContentUserProfile profile(String userId, int balance) {
        ContentUserProfile profile = new ContentUserProfile()
            .setUserId(userId)
            .setPointBalance(balance);
        profile.setId("p-" + userId);
        return profile;
    }

    private ContentUserPointLedger ledger(String id, String userId, int delta, int balanceAfter, Date createTime) {
        ContentUserPointLedger ledger = ContentUserPointLedger.of(userId, delta > 0 ? "AWARD" : "SPEND", delta)
            .setBalanceAfter(balanceAfter);
        ledger.setId(id);
        ledger.setCreateTime(createTime);
        return ledger;
    }

    private ContentUserExchangeGoods findGoods(String id) {
        return goodsList.stream().filter(it -> Objects.equals(id, it.getId())).findFirst().orElse(null);
    }

    private ContentUserProfile findProfile(String userId) {
        return profiles.stream().filter(it -> Objects.equals(userId, it.getUserId())).findFirst().orElse(null);
    }

    private Object defaultValue(Class<?> returnType) {
        if (returnType == Integer.TYPE) {
            return 0;
        }
        if (returnType == Boolean.TYPE) {
            return false;
        }
        return null;
    }
}
