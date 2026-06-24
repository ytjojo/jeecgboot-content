package org.jeecg.modules.content.user.controller;

import org.jeecg.modules.content.user.req.growth.ContentPointAdjustReq;
import org.jeecg.modules.content.user.req.growth.ContentUserBadgeRecycleReq;
import org.jeecg.modules.content.user.req.growth.ContentUserBadgeWearReq;
import org.jeecg.modules.content.user.req.growth.ContentUserExchangeReq;
import org.jeecg.modules.content.user.req.growth.ContentUserFeatureUnlockReq;
import org.jeecg.modules.content.user.req.growth.ContentUserVirtualGiftReq;
import org.jeecg.modules.content.user.service.IContentUserBadgeService;
import org.jeecg.modules.content.user.service.IContentUserGrowthDecayStateService;
import org.jeecg.modules.content.user.service.IContentUserGrowthService;
import org.jeecg.modules.content.user.service.IContentUserLevelBenefitService;
import org.jeecg.modules.content.user.service.IContentUserLevelConfigService;
import org.jeecg.modules.content.user.service.IContentUserPointSpendService;
import org.jeecg.modules.content.user.vo.ContentUserBadgeVO;
import org.jeecg.modules.content.user.vo.ContentUserDistributionWeightVO;
import org.jeecg.modules.content.user.vo.ContentUserFeatureUnlockVO;
import org.jeecg.modules.content.user.vo.ContentUserGrowthDecayRuleVO;
import org.jeecg.modules.content.user.vo.ContentUserGrowthDecayStatusVO;
import org.jeecg.modules.content.user.vo.ContentUserGrowthVO;
import org.jeecg.modules.content.user.vo.ContentUserLevelBenefitSummaryVO;
import org.jeecg.modules.content.user.vo.ContentUserPointLedgerPageVO;
import org.jeecg.modules.content.user.vo.ContentUserPointSpendResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ContentUserGrowthControllerWebMvcTest {

    private MockMvc mockMvc;

    @Mock
    private IContentUserGrowthService growthService;

    @Mock
    private IContentUserBadgeService badgeService;

    @Mock
    private IContentUserPointSpendService pointSpendService;

    @Mock
    private IContentUserLevelBenefitService levelBenefitService;

    @Mock
    private IContentUserLevelConfigService levelConfigService;

    @Mock
    private IContentUserGrowthDecayStateService growthDecayStateService;

    @InjectMocks
    private ContentUserGrowthController controller;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setValidator(validator)
            .build();
    }

    @Test
    void shouldRecordBehavior() throws Exception {
        mockMvc.perform(post("/content/user/growth/record")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"u1\",\"sourceType\":\"PUBLISH\",\"pointDelta\":10,\"growthDelta\":5}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("记录成功"));

        verify(growthService).recordBehavior("u1", "PUBLISH", 10, 5);
    }

    @Test
    void shouldRejectRecordWithBlankUserId() throws Exception {
        mockMvc.perform(post("/content/user/growth/record")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"\",\"sourceType\":\"PUBLISH\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnGrowthSummary() throws Exception {
        when(growthService.getGrowthSummary("u1"))
            .thenReturn(new ContentUserGrowthVO()
                .setUserId("u1")
                .setPointBalance(200)
                .setGrowthValue(500)
                .setLevel(3)
                .setLevelBenefitSummary(new ContentUserLevelBenefitSummaryVO().setTopicQuota(5)));

        mockMvc.perform(get("/content/user/growth/summary").param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.userId").value("u1"))
            .andExpect(jsonPath("$.result.pointBalance").value(200))
            .andExpect(jsonPath("$.result.growthValue").value(500))
            .andExpect(jsonPath("$.result.level").value(3));
    }

    @Test
    void shouldReturnBadgeCatalog() throws Exception {
        when(badgeService.listBadgeCatalog("u1"))
            .thenReturn(Map.of("SOCIAL", List.of(
                new ContentUserBadgeVO().setBadgeCode("B1").setBadgeName("社交达人").setGranted(true)
            )));

        mockMvc.perform(get("/content/user/growth/badge/catalog").param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.SOCIAL[0].badgeCode").value("B1"))
            .andExpect(jsonPath("$.result.SOCIAL[0].granted").value(true));
    }

    @Test
    void shouldReturnBadgeDetail() throws Exception {
        when(badgeService.getBadgeDetail("u1", "B1"))
            .thenReturn(new ContentUserBadgeVO().setBadgeCode("B1").setBadgeName("测试勋章").setGranted(false));

        mockMvc.perform(get("/content/user/growth/badge/detail")
                .param("userId", "u1")
                .param("badgeCode", "B1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.badgeCode").value("B1"))
            .andExpect(jsonPath("$.result.granted").value(false));
    }

    @Test
    void shouldSaveWornBadges() throws Exception {
        when(badgeService.saveWornBadges(eq("u1"), any()))
            .thenReturn(List.of(new ContentUserBadgeVO().setBadgeCode("B1").setDisplaying(true)));

        mockMvc.perform(post("/content/user/growth/badge/wear")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"grantIds\":[\"g1\",\"g2\"]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result[0].displaying").value(true));
    }

    @Test
    void shouldRejectEmptyWornBadgesList() throws Exception {
        mockMvc.perform(post("/content/user/growth/badge/wear")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"grantIds\":[]}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldListWornBadges() throws Exception {
        when(badgeService.listWornBadges("u1"))
            .thenReturn(List.of(new ContentUserBadgeVO().setBadgeCode("B1").setDisplaying(true)));

        mockMvc.perform(get("/content/user/growth/badge/worn").param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result[0].displaying").value(true));
    }

    @Test
    void shouldRecycleBadge() throws Exception {
        mockMvc.perform(post("/content/user/growth/badge/recycle")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"grantId\":\"g1\",\"operatorUserId\":\"admin1\",\"reason\":\"违规撤销\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("回收成功"));

        verify(badgeService).recycleBadge("g1", "admin1", "违规撤销");
    }

    @Test
    void shouldRejectRecycleWithMissingFields() throws Exception {
        mockMvc.perform(post("/content/user/growth/badge/recycle")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"grantId\":\"\",\"operatorUserId\":\"\",\"reason\":\"\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldListExchangeGoods() throws Exception {
        when(pointSpendService.listExchangeGoods("VIRTUAL"))
            .thenReturn(List.of(new ContentUserExchangeGoodsStub("g1", "虚拟商品", 100).toVO()));

        mockMvc.perform(get("/content/user/growth/point/exchange/goods").param("goodsType", "VIRTUAL"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result[0].goodsId").value("g1"))
            .andExpect(jsonPath("$.result[0].goodsName").value("虚拟商品"));
    }

    @Test
    void shouldExchangeGoods() throws Exception {
        when(pointSpendService.exchangeGoods("u1", "g1", 3, null))
            .thenReturn(new ContentUserPointSpendResultVO()
                .setOrderId("o1")
                .setOrderNo("ON1")
                .setGoodsId("g1")
                .setQuantity(3)
                .setPointCost(300)
                .setBalanceAfter(700));

        mockMvc.perform(post("/content/user/growth/point/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"u1\",\"goodsId\":\"g1\",\"quantity\":3}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.orderId").value("o1"))
            .andExpect(jsonPath("$.result.pointCost").value(300));
    }

    @Test
    void shouldRejectExchangeWithZeroQuantity() throws Exception {
        mockMvc.perform(post("/content/user/growth/point/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"u1\",\"goodsId\":\"g1\",\"quantity\":0}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUnlockFeature() throws Exception {
        when(pointSpendService.unlockFeature("u1", "g1"))
            .thenReturn(new ContentUserPointSpendResultVO()
                .setOrderId("o2")
                .setGoodsId("g1")
                .setPointCost(50)
                .setBalanceAfter(950)
                .setReusedUnlock(false));

        mockMvc.perform(post("/content/user/growth/point/feature/unlock")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"u1\",\"goodsId\":\"g1\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.reusedUnlock").value(false));
    }

    @Test
    void shouldReturnFeatureUnlock() throws Exception {
        when(pointSpendService.getFeatureUnlock("u1", "FEATURE_X"))
            .thenReturn(new ContentUserFeatureUnlockVO()
                .setFeatureCode("FEATURE_X")
                .setEnabled(true));

        mockMvc.perform(get("/content/user/growth/point/feature/unlock")
                .param("userId", "u1")
                .param("featureCode", "FEATURE_X"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.featureCode").value("FEATURE_X"))
            .andExpect(jsonPath("$.result.enabled").value(true));
    }

    @Test
    void shouldSendVirtualGift() throws Exception {
        when(pointSpendService.sendVirtualGift(eq("u1"), eq("u2"), eq("gift1"), eq(1), any()))
            .thenReturn(new ContentUserPointSpendResultVO()
                .setOrderId("o3")
                .setGoodsId("gift1")
                .setPointCost(99)
                .setBalanceAfter(901));

        mockMvc.perform(post("/content/user/growth/point/gift/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"senderUserId\":\"u1\",\"receiverUserId\":\"u2\",\"giftGoodsId\":\"gift1\",\"quantity\":1,\"message\":\"生日快乐\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.pointCost").value(99));
    }

    @Test
    void shouldQueryPointLedger() throws Exception {
        when(pointSpendService.queryPointLedger(any()))
            .thenReturn(new ContentUserPointLedgerPageVO()
                .setTotal(0L)
                .setCurrent(1L)
                .setSize(10L)
                .setRecords(List.of()));

        mockMvc.perform(get("/content/user/growth/point/ledger")
                .param("userId", "u1")
                .param("type", "EARN")
                .param("current", "1")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.total").value(0))
            .andExpect(jsonPath("$.result.size").value(10));
    }

    @Test
    void shouldReturnLevelBenefit() throws Exception {
        when(levelBenefitService.getBenefitSummary("u1"))
            .thenReturn(new ContentUserLevelBenefitSummaryVO()
                .setUploadSizeLimitMb(200)
                .setHdVideoEnabled(true)
                .setTopicQuota(10));

        mockMvc.perform(get("/content/user/growth/level/benefit").param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.uploadSizeLimitMb").value(200))
            .andExpect(jsonPath("$.result.hdVideoEnabled").value(true))
            .andExpect(jsonPath("$.result.topicQuota").value(10));
    }

    @Test
    void shouldListLevelConfigs() throws Exception {
        when(levelConfigService.listValidEnabledLevels())
            .thenReturn(List.of(
                new org.jeecg.modules.content.user.entity.ContentUserLevelConfig()
                    .setLevel(1).setLevelName("新手").setGrowthThreshold(0).setBadgeStyleKey("lv1"),
                new org.jeecg.modules.content.user.entity.ContentUserLevelConfig()
                    .setLevel(2).setLevelName("进阶").setGrowthThreshold(500).setBadgeStyleKey("lv2")
            ));

        mockMvc.perform(get("/content/user/growth/level/config"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result[0].level").value(1))
            .andExpect(jsonPath("$.result[0].levelName").value("新手"))
            .andExpect(jsonPath("$.result[1].growthThreshold").value(500));
    }

    @Test
    void shouldReturnDistributionWeight() throws Exception {
        when(levelBenefitService.resolveDistributionWeight(eq("u1"), any(BigDecimal.class)))
            .thenReturn(new ContentUserDistributionWeightVO()
                .setUserId("u1")
                .setLevel(3)
                .setQualityScore(new BigDecimal("85.50"))
                .setDistributionWeight(new BigDecimal("1.20"))
                .setWeighted(true));

        mockMvc.perform(get("/content/user/growth/level/distribution-weight")
                .param("userId", "u1")
                .param("qualityScore", "85.50"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.distributionWeight").value(1.20))
            .andExpect(jsonPath("$.result.weighted").value(true));
    }

    @Test
    void shouldReturnDecayRule() throws Exception {
        when(growthDecayStateService.getDecayRule())
            .thenReturn(new ContentUserGrowthDecayRuleVO()
                .setEnabled(true)
                .setInactiveDays(30)
                .setDecayRate(new BigDecimal("0.10"))
                .setProtectionDays(7)
                .setRuleDescription("30天未活跃衰减10%，7天降级保护"));

        mockMvc.perform(get("/content/user/growth/decay/rule"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.inactiveDays").value(30))
            .andExpect(jsonPath("$.result.ruleDescription").value("30天未活跃衰减10%，7天降级保护"));
    }

    @Test
    void shouldReturnDecayStatus() throws Exception {
        when(growthDecayStateService.getDecayStatus("u1"))
            .thenReturn(new ContentUserGrowthDecayStatusVO()
                .setStatus("NORMAL")
                .setInactiveDays(0)
                .setCurrentLevel(3)
                .setCurrentGrowthValue(200));

        mockMvc.perform(get("/content/user/growth/decay/status").param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.status").value("NORMAL"))
            .andExpect(jsonPath("$.result.currentLevel").value(3));
    }

    @Test
    void shouldUseZeroForNullPointDeltaInRecord() throws Exception {
        mockMvc.perform(post("/content/user/growth/record")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"u1\",\"sourceType\":\"LIKE\",\"pointDelta\":null,\"growthDelta\":null}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(growthService).recordBehavior("u1", "LIKE", 0, 0);
    }

    private static class ContentUserExchangeGoodsStub {
        private final String id;
        private final String name;
        private final int price;

        ContentUserExchangeGoodsStub(String id, String name, int price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }

        org.jeecg.modules.content.user.vo.ContentUserExchangeGoodsVO toVO() {
            return new org.jeecg.modules.content.user.vo.ContentUserExchangeGoodsVO()
                .setGoodsId(id)
                .setGoodsName(name)
                .setPointPrice(price);
        }
    }
}
