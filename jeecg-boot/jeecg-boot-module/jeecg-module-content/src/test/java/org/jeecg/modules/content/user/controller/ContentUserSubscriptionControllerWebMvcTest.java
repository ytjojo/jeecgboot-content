package org.jeecg.modules.content.user.controller;

import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionBatchReq;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionNotificationPreferenceReq;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionReq;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionSourceReq;
import org.jeecg.modules.content.user.service.IContentSubscriptionNotificationPreferenceService;
import org.jeecg.modules.content.user.service.IContentSubscriptionSourceService;
import org.jeecg.modules.content.user.service.IContentUserSubscriptionService;
import org.jeecg.modules.content.user.vo.ContentSubscriptionBatchResultVO;
import org.jeecg.modules.content.user.vo.ContentSubscriptionFeedPageVO;
import org.jeecg.modules.content.user.vo.ContentSubscriptionNotificationDecisionVO;
import org.jeecg.modules.content.user.vo.ContentSubscriptionNotificationPreferenceVO;
import org.jeecg.modules.content.user.vo.ContentSubscriptionSourceDetailVO;
import org.jeecg.modules.content.user.vo.ContentSubscriptionSourcePageVO;
import org.jeecg.modules.content.user.vo.ContentSubscriptionSourceVO;
import org.jeecg.modules.content.user.vo.ContentUserSubscriptionPageVO;
import org.jeecg.modules.content.user.vo.ContentUserSubscriptionVO;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ContentUserSubscriptionControllerWebMvcTest {

    private MockMvc mockMvc;

    @Mock
    private IContentUserSubscriptionService subscriptionService;

    @Mock
    private IContentSubscriptionNotificationPreferenceService notificationPreferenceService;

    @Mock
    private IContentSubscriptionSourceService sourceService;

    @InjectMocks
    private ContentUserSubscriptionController controller;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setValidator(validator)
            .build();
    }

    @Test
    void shouldSubscribe() throws Exception {
        when(subscriptionService.subscribe(eq("u1"), any(ContentSubscriptionReq.class)))
            .thenReturn(new ContentUserSubscriptionVO()
                .setSubscriptionId("sub1")
                .setSourceType("TOPIC")
                .setSourceId("t1")
                .setSourceName("Java")
                .setPaused(false));

        mockMvc.perform(post("/content/user/subscription/subscribe?userId=u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sourceType\":\"TOPIC\",\"sourceId\":\"t1\",\"sourceName\":\"Java\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.subscriptionId").value("sub1"))
            .andExpect(jsonPath("$.result.sourceType").value("TOPIC"))
            .andExpect(jsonPath("$.result.paused").value(false));
    }

    @Test
    void shouldRejectSubscribeWithMissingFields() throws Exception {
        mockMvc.perform(post("/content/user/subscription/subscribe?userId=u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sourceType\":\"\",\"sourceId\":\"\",\"sourceName\":\"\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPauseSubscription() throws Exception {
        when(subscriptionService.pauseSubscription("u1", "sub1"))
            .thenReturn(new ContentUserSubscriptionVO()
                .setSubscriptionId("sub1").setPaused(true));

        mockMvc.perform(post("/content/user/subscription/pause")
                .param("userId", "u1")
                .param("subscriptionId", "sub1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.paused").value(true));
    }

    @Test
    void shouldResumeSubscription() throws Exception {
        when(subscriptionService.resumeSubscription("u1", "sub1"))
            .thenReturn(new ContentUserSubscriptionVO()
                .setSubscriptionId("sub1").setPaused(false));

        mockMvc.perform(post("/content/user/subscription/resume")
                .param("userId", "u1")
                .param("subscriptionId", "sub1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.paused").value(false));
    }

    @Test
    void shouldCancelSubscription() throws Exception {
        when(subscriptionService.cancelSubscription("u1", "sub1"))
            .thenReturn(new ContentUserSubscriptionVO()
                .setSubscriptionId("sub1").setSubscriptionStatus("CANCELLED"));

        mockMvc.perform(post("/content/user/subscription/cancel")
                .param("userId", "u1")
                .param("subscriptionId", "sub1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.subscriptionStatus").value("CANCELLED"));
    }

    @Test
    void shouldListSubscriptions() throws Exception {
        when(subscriptionService.listSubscriptions("u1", "TOPIC", 1L, 10L))
            .thenReturn(new ContentUserSubscriptionPageVO()
                .setTotal(2L)
                .setPageNo(1L)
                .setPageSize(10L)
                .setRecords(List.of(
                    new ContentUserSubscriptionVO().setSubscriptionId("sub1").setSourceType("TOPIC"),
                    new ContentUserSubscriptionVO().setSubscriptionId("sub2").setSourceType("TOPIC")
                )));

        mockMvc.perform(get("/content/user/subscription/list")
                .param("userId", "u1")
                .param("sourceType", "TOPIC")
                .param("pageNo", "1")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.total").value(2))
            .andExpect(jsonPath("$.result.records[0].sourceType").value("TOPIC"));
    }

    @Test
    void shouldListSubscriptionFeed() throws Exception {
        when(subscriptionService.listSubscriptionFeed("u1", null, 1L, 10L))
            .thenReturn(new ContentSubscriptionFeedPageVO()
                .setTotal(0L)
                .setPageNo(1L)
                .setPageSize(10L)
                .setHasMore(false)
                .setRecords(List.of()));

        mockMvc.perform(get("/content/user/subscription/feed")
                .param("userId", "u1")
                .param("pageNo", "1")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.hasMore").value(false));
    }

    @Test
    void shouldSaveSource() throws Exception {
        when(sourceService.saveSource(any(ContentSubscriptionSourceReq.class)))
            .thenReturn(new ContentSubscriptionSourceVO()
                .setSourceType("TOPIC")
                .setSourceId("t1")
                .setSourceName("Java")
                .setSubscriberCount(0));

        mockMvc.perform(post("/content/user/subscription/source/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sourceType\":\"TOPIC\",\"sourceId\":\"t1\",\"sourceName\":\"Java\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.sourceName").value("Java"));
    }

    @Test
    void shouldListPlaza() throws Exception {
        when(sourceService.listPlaza(eq("u1"), eq("DEV"), eq("java"), eq("TOPIC"), eq(1L), eq(10L)))
            .thenReturn(new ContentSubscriptionSourcePageVO()
                .setTotal(1L)
                .setPageNo(1L)
                .setPageSize(10L)
                .setRecords(List.of(
                    new ContentSubscriptionSourceVO().setSourceType("TOPIC").setSourceId("t1").setSourceName("Java")
                )));

        mockMvc.perform(get("/content/user/subscription/plaza")
                .param("userId", "u1")
                .param("category", "DEV")
                .param("keyword", "java")
                .param("sourceType", "TOPIC")
                .param("pageNo", "1")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.total").value(1))
            .andExpect(jsonPath("$.result.records[0].sourceName").value("Java"));
    }

    @Test
    void shouldGetSourceDetail() throws Exception {
        ContentSubscriptionSourceDetailVO detail = new ContentSubscriptionSourceDetailVO();
        detail.setSourceType("TOPIC");
        detail.setSourceId("t1");
        detail.setSourceName("Java");
        detail.setRecentContentSummary("Java 21 LTS");
        detail.setPaused(false);
        when(sourceService.getSourceDetail("u1", "TOPIC", "t1")).thenReturn(detail);

        mockMvc.perform(get("/content/user/subscription/source/detail")
                .param("userId", "u1")
                .param("sourceType", "TOPIC")
                .param("sourceId", "t1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.recentContentSummary").value("Java 21 LTS"));
    }

    @Test
    void shouldSubscribeSourceFromPlaza() throws Exception {
        when(sourceService.subscribeFromPlaza("u1", "TOPIC", "t1"))
            .thenReturn(new ContentUserSubscriptionVO()
                .setSubscriptionId("sub1")
                .setSourceType("TOPIC")
                .setSourceId("t1"));

        mockMvc.perform(post("/content/user/subscription/source/subscribe")
                .param("userId", "u1")
                .param("sourceType", "TOPIC")
                .param("sourceId", "t1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.subscriptionId").value("sub1"));
    }

    @Test
    void shouldBatchPause() throws Exception {
        when(subscriptionService.batchPause(eq("u1"), any()))
            .thenReturn(batchResult(2, 0));

        mockMvc.perform(post("/content/user/subscription/batch/pause?userId=u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"subscriptionIds\":[\"s1\",\"s2\"]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.successCount").value(2))
            .andExpect(jsonPath("$.result.failureCount").value(0));
    }

    @Test
    void shouldBatchResume() throws Exception {
        when(subscriptionService.batchResume(eq("u1"), any()))
            .thenReturn(batchResult(1, 1));

        mockMvc.perform(post("/content/user/subscription/batch/resume?userId=u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"subscriptionIds\":[\"s1\",\"s2\"]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.successCount").value(1))
            .andExpect(jsonPath("$.result.failureCount").value(1));
    }

    @Test
    void shouldBatchCancel() throws Exception {
        when(subscriptionService.batchCancel(eq("u1"), any()))
            .thenReturn(batchResult(3, 0));

        mockMvc.perform(post("/content/user/subscription/batch/cancel?userId=u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"subscriptionIds\":[\"s1\",\"s2\",\"s3\"]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.successCount").value(3));
    }

    @Test
    void shouldSaveNotificationPreference() throws Exception {
        when(notificationPreferenceService.savePreference(eq("u1"), any(ContentSubscriptionNotificationPreferenceReq.class)))
            .thenReturn(new ContentSubscriptionNotificationPreferenceVO()
                .setSubscriptionId("sub1")
                .setUserId("u1")
                .setNotificationFrequency("DAILY"));

        mockMvc.perform(post("/content/user/subscription/notification/preference?userId=u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"subscriptionId\":\"sub1\",\"notificationChannels\":[\"IN_APP\"],\"notificationFrequency\":\"DAILY\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.notificationFrequency").value("DAILY"));
    }

    @Test
    void shouldGetNotificationPreference() throws Exception {
        when(notificationPreferenceService.getEffectivePreference("u1", "sub1"))
            .thenReturn(new ContentSubscriptionNotificationPreferenceVO()
                .setSubscriptionId("sub1")
                .setUserId("u1")
                .setNotificationFrequency("REALTIME")
                .setInherited(false));

        mockMvc.perform(get("/content/user/subscription/notification/preference")
                .param("userId", "u1")
                .param("subscriptionId", "sub1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.notificationFrequency").value("REALTIME"))
            .andExpect(jsonPath("$.result.inherited").value(false));
    }

    @Test
    void shouldDecideUpdateNotification() throws Exception {
        when(notificationPreferenceService.decideUpdateNotification("u1", "sub1", "biz1"))
            .thenReturn(new ContentSubscriptionNotificationDecisionVO()
                .setRealtimeDelivery(true)
                .setDailySummary(false)
                .setDelayedByDnd(false)
                .setChannels(List.of("IN_APP"))
                .setUpdateBizId("biz1"));

        mockMvc.perform(get("/content/user/subscription/notification/decision")
                .param("userId", "u1")
                .param("subscriptionId", "sub1")
                .param("updateBizId", "biz1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.realtimeDelivery").value(true))
            .andExpect(jsonPath("$.result.updateBizId").value("biz1"));
    }

    private static ContentSubscriptionBatchResultVO batchResult(int success, int failure) {
        ContentSubscriptionBatchResultVO vo = new ContentSubscriptionBatchResultVO();
        for (int i = 0; i < success; i++) {
            vo.addSuccess();
        }
        for (int i = 0; i < failure; i++) {
            vo.addFailure("sub-x", "reason");
        }
        return vo;
    }
}
