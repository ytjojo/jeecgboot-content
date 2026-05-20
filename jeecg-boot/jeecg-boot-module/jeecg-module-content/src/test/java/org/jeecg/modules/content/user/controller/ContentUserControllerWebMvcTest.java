package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jeecg.modules.content.user.constant.ContentUserErrorCode;
import org.jeecg.modules.content.user.dto.ContentUserBadgeProgressDTO;
import org.jeecg.modules.content.user.dto.ContentUserPointLedgerQueryDTO;
import org.jeecg.modules.content.user.entity.ContentUserLevelConfig;
import org.jeecg.modules.content.user.req.growth.ContentUserBadgeRecycleReq;
import org.jeecg.modules.content.user.req.growth.ContentUserBadgeWearReq;
import org.jeecg.modules.content.user.req.growth.ContentUserExchangeReq;
import org.jeecg.modules.content.user.req.growth.ContentUserFeatureUnlockReq;
import org.jeecg.modules.content.user.req.growth.ContentUserVirtualGiftReq;
import org.jeecg.modules.content.user.service.IContentUserBadgeService;
import org.jeecg.modules.content.user.service.IContentUserGovernanceService;
import org.jeecg.modules.content.user.service.IContentUserGrowthDecayStateService;
import org.jeecg.modules.content.user.service.IContentUserGrowthService;
import org.jeecg.modules.content.user.service.IContentUserLevelBenefitService;
import org.jeecg.modules.content.user.service.IContentUserLevelConfigService;
import org.jeecg.modules.content.user.service.IContentUserNotificationSettingService;
import org.jeecg.modules.content.user.service.IContentUserPointSpendService;
import org.jeecg.modules.content.user.service.IContentUserProfileService;
import org.jeecg.modules.content.user.service.IContentUserRelationService;
import org.jeecg.modules.content.user.vo.ContentRelationBatchResultVO;
import org.jeecg.modules.content.user.vo.ContentRelationUserItemVO;
import org.jeecg.modules.content.user.vo.ContentRelationUserPageVO;
import org.jeecg.modules.content.user.vo.ContentNotificationChannelConfigVO;
import org.jeecg.modules.content.user.vo.ContentNotificationDndRuleVO;
import org.jeecg.modules.content.user.vo.ContentUserNotificationSettingVO;
import org.jeecg.modules.content.user.vo.ContentUserBadgeVO;
import org.jeecg.modules.content.user.vo.ContentUserDistributionWeightVO;
import org.jeecg.modules.content.user.vo.ContentUserExchangeGoodsVO;
import org.jeecg.modules.content.user.vo.ContentUserFeatureUnlockVO;
import org.jeecg.modules.content.user.vo.ContentUserGrowthDecayRuleVO;
import org.jeecg.modules.content.user.vo.ContentUserLevelBenefitSummaryVO;
import org.jeecg.modules.content.user.vo.ContentUserPointLedgerPageVO;
import org.jeecg.modules.content.user.vo.ContentUserPointLedgerVO;
import org.jeecg.modules.content.user.vo.ContentUserPointSpendResultVO;
import org.jeecg.modules.content.user.vo.ContentUserRelationVO;
import org.jeecg.modules.content.user.vo.ContentUserStatusHistoryItemVO;
import org.jeecg.modules.content.user.vo.ContentUserStatusHistoryPageVO;
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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ContentUserControllerWebMvcTest {

    private MockMvc mockMvc;

    @Mock
    private IContentUserGovernanceService governanceService;

    @Mock
    private IContentUserProfileService profileService;

    @Mock
    private IContentUserRelationService relationService;

    @Mock
    private IContentUserNotificationSettingService notificationSettingService;

    @Mock
    private IContentUserGrowthService growthService;

    @Mock
    private IContentUserGrowthDecayStateService growthDecayStateService;

    @Mock
    private IContentUserBadgeService badgeService;

    @Mock
    private IContentUserPointSpendService pointSpendService;

    @Mock
    private IContentUserLevelBenefitService levelBenefitService;

    @Mock
    private IContentUserLevelConfigService levelConfigService;

    @InjectMocks
    private ContentUserGovernanceController governanceController;

    @InjectMocks
    private ContentUserProfileController profileController;

    @InjectMocks
    private ContentUserRelationController relationController;

    @InjectMocks
    private ContentUserSettingsController settingsController;

    @InjectMocks
    private ContentUserGrowthController growthController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(governanceController, profileController, relationController,
                settingsController, growthController)
            .setValidator(validator)
            .build();
    }

    @Test
    void shouldRejectMutedUserCommentPermission() throws Exception {
        when(governanceService.canExecuteAction("u1", "COMMENT")).thenReturn(false);

        mockMvc.perform(get("/content/user/governance/permission/check")
                .param("userId", "u1")
                .param("actionType", "COMMENT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value(false));
    }

    @Test
    void shouldReturnPagedStatusHistory() throws Exception {
        Date createTime = new Date(1735689600000L);
        when(governanceService.listStatusHistory("u1", 2L, 1L))
            .thenReturn(new ContentUserStatusHistoryPageVO()
                .setTotal(3L)
                .setPageNo(2L)
                .setPageSize(1L)
                .setRecords(List.of(new ContentUserStatusHistoryItemVO()
                    .setRecordId("record-1")
                    .setCurrentStatus("NORMAL")
                    .setTargetStatus("FROZEN")
                    .setTriggerSource("MANUAL")
                    .setOperatorUserId("admin-1")
                    .setReason("违规处理")
                    .setCreateTime(createTime))));

        mockMvc.perform(get("/content/user/governance/status/history")
                .param("userId", "u1")
                .param("pageNo", "2")
                .param("pageSize", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.total").value(3))
            .andExpect(jsonPath("$.result.pageNo").value(2))
            .andExpect(jsonPath("$.result.pageSize").value(1))
            .andExpect(jsonPath("$.result.records[0].recordId").value("record-1"))
            .andExpect(jsonPath("$.result.records[0].targetStatus").value("FROZEN"));
    }

    @Test
    void shouldRejectInvalidFollowRequest() throws Exception {
        mockMvc.perform(post("/content/user/relation/follow")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetUserId\":\"\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnGrowthDecayRule() throws Exception {
        when(growthDecayStateService.getDecayRule()).thenReturn(new ContentUserGrowthDecayRuleVO()
            .setEnabled(Boolean.TRUE)
            .setInactiveDays(30)
            .setDecayRate(new BigDecimal("0.05"))
            .setProtectionDays(7)
            .setRuleDescription("连续 30 天未登录后衰减，7 天保护"));

        mockMvc.perform(get("/content/user/growth/decay/rule"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.enabled").value(true))
            .andExpect(jsonPath("$.result.inactiveDays").value(30))
            .andExpect(jsonPath("$.result.decayRate").value(0.05))
            .andExpect(jsonPath("$.result.protectionDays").value(7))
            .andExpect(jsonPath("$.result.ruleDescription").value("连续 30 天未登录后衰减，7 天保护"));
    }

    @Test
    void shouldReturnBadgeCatalogAndDetailThroughGrowthController() throws Exception {
        ContentUserBadgeVO badge = new ContentUserBadgeVO()
            .setBadgeCode("PUBLISHER")
            .setBadgeName("创作者")
            .setCategory("ACHIEVEMENT")
            .setGranted(Boolean.FALSE)
            .setProgress(new ContentUserBadgeProgressDTO()
                .setBadgeCode("PUBLISHER")
                .setCurrentProgress(3)
                .setTargetProgress(10)
                .setRemainingRequirement(7));
        when(badgeService.listBadgeCatalog("u1")).thenReturn(Map.of("ACHIEVEMENT", List.of(badge)));
        when(badgeService.getBadgeDetail("u1", "PUBLISHER")).thenReturn(badge);

        mockMvc.perform(get("/content/user/growth/badge/catalog").param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.ACHIEVEMENT[0].badgeCode").value("PUBLISHER"))
            .andExpect(jsonPath("$.result.ACHIEVEMENT[0].progress.remainingRequirement").value(7));

        mockMvc.perform(get("/content/user/growth/badge/detail")
                .param("userId", "u1")
                .param("badgeCode", "PUBLISHER"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.badgeName").value("创作者"));
    }

    @Test
    void shouldSaveWornBadgesAndRecycleBadgeThroughGrowthController() throws Exception {
        ContentUserBadgeVO worn = new ContentUserBadgeVO()
            .setBadgeGrantId("grant-1")
            .setBadgeCode("PUBLISHER")
            .setDisplaying(Boolean.TRUE);
        when(badgeService.saveWornBadges("u1", List.of("grant-1"))).thenReturn(List.of(worn));

        mockMvc.perform(post("/content/user/growth/badge/wear")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"grantIds\":[\"grant-1\"]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result[0].badgeGrantId").value("grant-1"))
            .andExpect(jsonPath("$.result[0].displaying").value(true));

        mockMvc.perform(post("/content/user/growth/badge/recycle")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"grantId\":\"grant-1\",\"operatorUserId\":\"admin-1\",\"reason\":\"违规获取\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("回收成功"));
    }

    @Test
    void shouldRejectTooManyWornBadgesAtControllerBoundary() throws Exception {
        mockMvc.perform(post("/content/user/growth/badge/wear")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"grantIds\":[\"g1\",\"g2\",\"g3\",\"g4\",\"g5\",\"g6\"]}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnPointGoodsExchangeGiftAndLedgerThroughGrowthController() throws Exception {
        when(pointSpendService.listExchangeGoods("BENEFIT"))
            .thenReturn(List.of(new ContentUserExchangeGoodsVO()
                .setGoodsId("goods-1")
                .setGoodsCode("VIP_BADGE")
                .setGoodsName("会员标识")
                .setGoodsType("BENEFIT")
                .setPointPrice(100)));
        when(pointSpendService.exchangeGoods("u1", "goods-1", 2))
            .thenReturn(new ContentUserPointSpendResultVO()
                .setOrderId("order-1")
                .setGoodsId("goods-1")
                .setQuantity(2)
                .setPointCost(200)
                .setBalanceAfter(800));
        when(pointSpendService.sendVirtualGift("u1", "u2", "gift-1", 1, "加油"))
            .thenReturn(new ContentUserPointSpendResultVO()
                .setOrderId("gift-order-1")
                .setGoodsId("gift-1")
                .setQuantity(1)
                .setPointCost(10)
                .setBenefitStatus("GRANTED"));
        when(pointSpendService.queryPointLedger(any(ContentUserPointLedgerQueryDTO.class)))
            .thenReturn(new ContentUserPointLedgerPageVO()
                .setCurrent(1L)
                .setSize(10L)
                .setTotal(1L)
                .setRecords(List.of(new ContentUserPointLedgerVO()
                    .setId("ledger-1")
                    .setPointDelta(-200)
                    .setSourceType("POINT_EXCHANGE")
                    .setSourceDescription("会员标识"))));

        mockMvc.perform(get("/content/user/growth/point/exchange/goods").param("goodsType", "BENEFIT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result[0].goodsCode").value("VIP_BADGE"));

        mockMvc.perform(post("/content/user/growth/point/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"u1\",\"goodsId\":\"goods-1\",\"quantity\":2}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.orderId").value("order-1"))
            .andExpect(jsonPath("$.result.balanceAfter").value(800));

        mockMvc.perform(post("/content/user/growth/point/gift/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"senderUserId\":\"u1\",\"receiverUserId\":\"u2\",\"giftGoodsId\":\"gift-1\",\"quantity\":1,\"message\":\"加油\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.orderId").value("gift-order-1"))
            .andExpect(jsonPath("$.result.benefitStatus").value("GRANTED"));

        mockMvc.perform(get("/content/user/growth/point/ledger")
                .param("userId", "u1")
                .param("type", "SPEND")
                .param("current", "1")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.records[0].sourceType").value("POINT_EXCHANGE"))
            .andExpect(jsonPath("$.result.records[0].sourceDescription").value("会员标识"));
    }

    @Test
    void shouldRejectInvalidPointExchangeAtControllerBoundary() throws Exception {
        mockMvc.perform(post("/content/user/growth/point/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"u1\",\"goodsId\":\"goods-1\",\"quantity\":0}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnFeatureUnlockLevelBenefitConfigAndDistributionWeight() throws Exception {
        when(pointSpendService.unlockFeature("u1", "feature-goods-1"))
            .thenReturn(new ContentUserPointSpendResultVO()
                .setGoodsId("feature-goods-1")
                .setPointCost(50)
                .setReusedUnlock(Boolean.FALSE));
        when(pointSpendService.getFeatureUnlock("u1", "PIN_TOP"))
            .thenReturn(new ContentUserFeatureUnlockVO()
                .setFeatureCode("PIN_TOP")
                .setEnabled(Boolean.TRUE));
        when(levelBenefitService.getBenefitSummary("u1"))
            .thenReturn(new ContentUserLevelBenefitSummaryVO()
                .setUploadSizeLimitMb(500)
                .setHdVideoEnabled(Boolean.TRUE)
                .setTopicQuota(30)
                .setSupportPriority(1)
                .setEnabledBenefitCodes(List.of("HD_VIDEO")));
        when(levelConfigService.listValidEnabledLevels())
            .thenReturn(List.of(new ContentUserLevelConfig()
                .setLevel(2)
                .setLevelName("进阶创作者")
                .setGrowthThreshold(100)
                .setBadgeStyleKey("level-2")));
        when(levelBenefitService.resolveDistributionWeight("u1", new BigDecimal("0.80")))
            .thenReturn(new ContentUserDistributionWeightVO()
                .setUserId("u1")
                .setLevel(2)
                .setQualityScore(new BigDecimal("0.80"))
                .setDistributionWeight(new BigDecimal("1.10"))
                .setQualityScoreRequired(Boolean.TRUE)
                .setWeighted(Boolean.TRUE));

        mockMvc.perform(post("/content/user/growth/point/feature/unlock")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"u1\",\"goodsId\":\"feature-goods-1\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.goodsId").value("feature-goods-1"))
            .andExpect(jsonPath("$.result.reusedUnlock").value(false));

        mockMvc.perform(get("/content/user/growth/point/feature/unlock")
                .param("userId", "u1")
                .param("featureCode", "PIN_TOP"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.featureCode").value("PIN_TOP"))
            .andExpect(jsonPath("$.result.enabled").value(true));

        mockMvc.perform(get("/content/user/growth/level/benefit").param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.uploadSizeLimitMb").value(500))
            .andExpect(jsonPath("$.result.enabledBenefitCodes[0]").value("HD_VIDEO"));

        mockMvc.perform(get("/content/user/growth/level/config"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result[0].level").value(2))
            .andExpect(jsonPath("$.result[0].growthThreshold").value(100));

        mockMvc.perform(get("/content/user/growth/level/distribution-weight")
                .param("userId", "u1")
                .param("qualityScore", "0.80"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.distributionWeight").value(1.10))
            .andExpect(jsonPath("$.result.qualityScoreRequired").value(true));
    }

    @Test
    void shouldKeepGrowthApiDocsAndErrorCodeConstantsCovered() throws Exception {
        assertTrue(ContentUserErrorCode.BADGE_WEAR_LIMIT_EXCEEDED > 0);
        assertTrue(ContentUserErrorCode.POINT_LEDGER_QUERY_INVALID > 0);
        assertTrue(ContentUserErrorCode.DISTRIBUTION_QUALITY_SCORE_REQUIRED > 0);
        assertTrue(ContentUserErrorCode.GROWTH_DECAY_RULE_INVALID > 0);
        assertRequiredSchema(ContentUserExchangeReq.class, "userId");
        assertRequiredSchema(ContentUserExchangeReq.class, "goodsId");
        assertRequiredSchema(ContentUserBadgeWearReq.class, "grantIds");
        assertRequiredSchema(ContentUserBadgeRecycleReq.class, "reason");
        assertRequiredSchema(ContentUserFeatureUnlockReq.class, "goodsId");
        assertRequiredSchema(ContentUserVirtualGiftReq.class, "receiverUserId");
    }

    @Test
    void shouldCancelBlacklistSuccessfully() throws Exception {
        mockMvc.perform(post("/content/user/relation/blacklist/cancel")
                .param("userId", "u1")
                .param("targetUserId", "u2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("解除拉黑成功"));
    }

    @Test
    void shouldEnableSpecialFollowSuccessfully() throws Exception {
        mockMvc.perform(post("/content/user/relation/special-follow")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetUserId\":\"u2\",\"relationGroupId\":\"g1\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("特别关注成功"));
    }

    @Test
    void shouldReturnRelationDetailWithoutEntityOnlyFields() throws Exception {
        when(relationService.getRelation("u1", "u2"))
            .thenReturn(new ContentUserRelationVO()
                .setOwnerUserId("u1")
                .setTargetUserId("u2")
                .setFollowed(Boolean.TRUE)
                .setSpecialFollow(Boolean.TRUE)
                .setMuted(Boolean.FALSE)
                .setBlacklisted(Boolean.FALSE)
                .setBlockedByOwner(Boolean.FALSE)
                .setRelationGroupId("g1")
                .setFollowedAt(new Date(1735689600000L))
                .setSpecialFollowAt(new Date(1735776000000L)));

        mockMvc.perform(get("/content/user/relation/detail")
                .param("userId", "u1")
                .param("targetUserId", "u2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.ownerUserId").value("u1"))
            .andExpect(jsonPath("$.result.targetUserId").value("u2"))
            .andExpect(jsonPath("$.result.followed").value(true))
            .andExpect(jsonPath("$.result.specialFollow").value(true))
            .andExpect(jsonPath("$.result.relationGroupId").value("g1"))
            .andExpect(jsonPath("$.result.id").doesNotExist())
            .andExpect(jsonPath("$.result.createBy").doesNotExist())
            .andExpect(jsonPath("$.result.createTime").doesNotExist())
            .andExpect(jsonPath("$.result.updateBy").doesNotExist())
            .andExpect(jsonPath("$.result.updateTime").doesNotExist())
            .andExpect(jsonPath("$.result.blacklistedAt").doesNotExist());
    }

    @Test
    void shouldKeepLegacyRelationEndpointsCompatible() throws Exception {
        mockMvc.perform(post("/content/user/relation/follow")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetUserId\":\"u2\",\"relationGroupId\":\"g1\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("关注成功"));

        mockMvc.perform(post("/content/user/relation/unfollow")
                .param("userId", "u1")
                .param("targetUserId", "u2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("取消关注成功"));

        mockMvc.perform(post("/content/user/relation/special-follow/cancel")
                .param("userId", "u1")
                .param("targetUserId", "u2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("取消特别关注成功"));
    }

    @Test
    void shouldRejectInvalidRelationGroupRequestsAtControllerBoundary() throws Exception {
        mockMvc.perform(post("/content/user/relation/group/create")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupName\":\"\",\"sortOrder\":1}"))
            .andExpect(status().isBadRequest());

        mockMvc.perform(post("/content/user/relation/group/move")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetUserIds\":[],\"relationGroupId\":\"g1\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldMoveRelationGroupThroughController() throws Exception {
        when(relationService.moveTargetsToGroup("u1", List.of("u2"), "g1"))
            .thenReturn(new ContentRelationBatchResultVO().setSuccessCount(1).setFailureCount(0));

        mockMvc.perform(post("/content/user/relation/group/move")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetUserIds\":[\"u2\"],\"relationGroupId\":\"g1\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.successCount").value(1))
            .andExpect(jsonPath("$.result.failureCount").value(0));
    }

    @Test
    void shouldReturnFollowListThroughController() throws Exception {
        when(relationService.listFollowedUsers("u1", "g1", "小", 2L, 1L))
            .thenReturn(new ContentRelationUserPageVO()
                .setTotal(1L)
                .setPageNo(2L)
                .setPageSize(1L)
                .setRecords(List.of(new ContentRelationUserItemVO()
                    .setTargetUserId("u2")
                    .setNickname("小明")
                    .setRelationGroupId("g1")
                    .setFollowed(Boolean.TRUE))));

        mockMvc.perform(get("/content/user/relation/follow-list")
                .param("userId", "u1")
                .param("relationGroupId", "g1")
                .param("keyword", "小")
                .param("pageNo", "2")
                .param("pageSize", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.total").value(1))
            .andExpect(jsonPath("$.result.records[0].targetUserId").value("u2"))
            .andExpect(jsonPath("$.result.records[0].nickname").value("小明"));
    }

    @Test
    void shouldReturnSpecialFollowListEmptyStateThroughController() throws Exception {
        when(relationService.listSpecialFollowedUsers("u1", 1L, 10L))
            .thenReturn(new ContentRelationUserPageVO()
                .setTotal(0L)
                .setPageNo(1L)
                .setPageSize(10L)
                .setEmptyStateCode("NO_SPECIAL_FOLLOW"));

        mockMvc.perform(get("/content/user/relation/special-follow-list")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.total").value(0))
            .andExpect(jsonPath("$.result.emptyStateCode").value("NO_SPECIAL_FOLLOW"));
    }

    @Test
    void shouldBatchUnfollowThroughController() throws Exception {
        when(relationService.batchUnfollow("u1", List.of("u2", "missing")))
            .thenReturn(new ContentRelationBatchResultVO()
                .setSuccessCount(1)
                .setFailureCount(1)
                .setFailures(List.of(new ContentRelationBatchResultVO.FailureItem()
                    .setTargetUserId("missing")
                    .setReason("关注关系不存在"))));

        mockMvc.perform(post("/content/user/relation/batch/unfollow")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetUserIds\":[\"u2\",\"missing\"]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.successCount").value(1))
            .andExpect(jsonPath("$.result.failureCount").value(1))
            .andExpect(jsonPath("$.result.failures[0].targetUserId").value("missing"));
    }

    @Test
    void shouldRejectEmptyBatchRelationRequestAtControllerBoundary() throws Exception {
        mockMvc.perform(post("/content/user/relation/batch/special-follow/cancel")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetUserIds\":[]}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotificationSetting() throws Exception {
        when(notificationSettingService.getSetting("u1"))
            .thenReturn(new ContentUserNotificationSettingVO()
                .setUserId("u1")
                .setLikeNoticeEnabled(Boolean.TRUE)
                .setCommentNoticeEnabled(Boolean.FALSE)
                .setChannelConfig(new ContentNotificationChannelConfigVO()
                    .setLikeChannels(List.of("IN_APP", "EMAIL")))
                .setDndRule(new ContentNotificationDndRuleVO()
                    .setEnabled(Boolean.TRUE)
                    .setStartTime("22:00")
                    .setEndTime("08:00")));

        mockMvc.perform(get("/content/user/settings/notification")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.userId").value("u1"))
            .andExpect(jsonPath("$.result.commentNoticeEnabled").value(false))
            .andExpect(jsonPath("$.result.channelConfig.likeChannels[1]").value("EMAIL"))
            .andExpect(jsonPath("$.result.dndRule.startTime").value("22:00"));
    }

    @Test
    void shouldRejectInvalidNotificationChannel() throws Exception {
        mockMvc.perform(post("/content/user/settings/notification/update")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"channelConfig\":{\"likeChannels\":[\"BAD\"]}}"))
            .andExpect(status().isBadRequest());
    }

    private void assertRequiredSchema(Class<?> targetType, String fieldName) throws NoSuchFieldException {
        Schema schema = targetType.getDeclaredField(fieldName).getAnnotation(Schema.class);
        assertNotNull(schema);
        assertTrue(schema.description() != null && !schema.description().isBlank());
        assertTrue(schema.requiredMode() == Schema.RequiredMode.REQUIRED);
    }
}
