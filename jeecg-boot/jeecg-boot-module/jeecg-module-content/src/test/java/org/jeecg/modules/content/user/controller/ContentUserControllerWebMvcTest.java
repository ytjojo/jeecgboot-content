package org.jeecg.modules.content.user.controller;

import org.jeecg.modules.content.user.service.IContentUserGovernanceService;
import org.jeecg.modules.content.user.service.IContentUserNotificationSettingService;
import org.jeecg.modules.content.user.service.IContentUserProfileService;
import org.jeecg.modules.content.user.service.IContentUserRelationService;
import org.jeecg.modules.content.user.vo.ContentNotificationChannelConfigVO;
import org.jeecg.modules.content.user.vo.ContentNotificationDndRuleVO;
import org.jeecg.modules.content.user.vo.ContentUserNotificationSettingVO;
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

    @InjectMocks
    private ContentUserGovernanceController governanceController;

    @InjectMocks
    private ContentUserProfileController profileController;

    @InjectMocks
    private ContentUserRelationController relationController;

    @InjectMocks
    private ContentUserSettingsController settingsController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(governanceController, profileController, relationController, settingsController)
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
}
