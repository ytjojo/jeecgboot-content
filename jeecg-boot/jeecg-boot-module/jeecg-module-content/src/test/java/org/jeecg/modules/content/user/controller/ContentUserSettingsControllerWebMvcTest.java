package org.jeecg.modules.content.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeecg.modules.content.user.req.profile.ContentUserPrivacyUpdateReq;
import org.jeecg.modules.content.user.req.settings.ContentFeedSettingUpdateReq;
import org.jeecg.modules.content.user.req.settings.ContentNotificationDndRuleReq;
import org.jeecg.modules.content.user.req.settings.ContentUserNotificationUpdateReq;
import org.jeecg.modules.content.user.service.IContentUserFeedSettingService;
import org.jeecg.modules.content.user.service.IContentUserNotificationSettingService;
import org.jeecg.modules.content.user.service.IContentUserProfileService;
import org.jeecg.modules.content.user.service.IContentUserSecuritySettingService;
import org.jeecg.modules.content.user.service.IContentUserVisibilityPolicyService;
import org.jeecg.modules.content.user.vo.ContentNotificationDndRuleVO;
import org.jeecg.modules.content.user.vo.ContentUserFeedSettingVO;
import org.jeecg.modules.content.user.vo.ContentUserNotificationSettingVO;
import org.jeecg.modules.content.user.vo.ContentUserSecuritySettingVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 内容社区用户设置 Controller WebMvc 测试。
 * 覆盖所有 Settings Controller 端点。
 */
class ContentUserSettingsControllerWebMvcTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private IContentUserProfileService profileService;
    private IContentUserVisibilityPolicyService visibilityPolicyService;
    private IContentUserNotificationSettingService notificationSettingService;
    private IContentUserFeedSettingService feedSettingService;
    private IContentUserSecuritySettingService securitySettingService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ContentUserSettingsController controller = new ContentUserSettingsController();
        profileService = mock(IContentUserProfileService.class);
        visibilityPolicyService = mock(IContentUserVisibilityPolicyService.class);
        notificationSettingService = mock(IContentUserNotificationSettingService.class);
        feedSettingService = mock(IContentUserFeedSettingService.class);
        securitySettingService = mock(IContentUserSecuritySettingService.class);
        ReflectionTestUtils.setField(controller, "profileService", profileService);
        ReflectionTestUtils.setField(controller, "visibilityPolicyService", visibilityPolicyService);
        ReflectionTestUtils.setField(controller, "notificationSettingService", notificationSettingService);
        ReflectionTestUtils.setField(controller, "feedSettingService", feedSettingService);
        ReflectionTestUtils.setField(controller, "securitySettingService", securitySettingService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    /**
     * 合法的免打扰规则更新请求应返回 200。
     */
    @Test
    void shouldReturn200ForValidDndRuleUpdate() throws Exception {
        ContentNotificationDndRuleVO vo = new ContentNotificationDndRuleVO()
            .setEnabled(true)
            .setStartTime("22:00")
            .setEndTime("08:00");
        when(notificationSettingService.updateDndRule(eq("u1"), any(ContentNotificationDndRuleReq.class)))
            .thenReturn(vo);

        ContentNotificationDndRuleReq req = new ContentNotificationDndRuleReq()
            .setEnabled(true)
            .setStartTime("22:00")
            .setEndTime("08:00");

        mockMvc.perform(post("/content/user/settings/notification/dnd/update")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.enabled").value(true))
            .andExpect(jsonPath("$.result.startTime").value("22:00"))
            .andExpect(jsonPath("$.result.endTime").value("08:00"));
    }

    /**
     * 非法时间格式（如 "25:00"）应触发校验失败。
     */
    @Test
    void shouldRejectInvalidTimeFormat() throws Exception {
        ContentNotificationDndRuleReq req = new ContentNotificationDndRuleReq()
            .setStartTime("25:00");

        mockMvc.perform(post("/content/user/settings/notification/dnd/update")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    /**
     * 合法的隐私更新请求应返回 200 和"更新成功"。
     */
    @Test
    void shouldReturn200ForValidPrivacyUpdate() throws Exception {
        ContentUserPrivacyUpdateReq req = new ContentUserPrivacyUpdateReq()
            .setBirthdayVisibility("PUBLIC")
            .setHomepageVisibility("FOLLOWERS_ONLY");

        mockMvc.perform(post("/content/user/settings/privacy/update")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("更新成功"));
    }

    /**
     * 查询通知设置应返回 200 和 VO 字段。
     */
    @Test
    void shouldReturn200ForNotificationQuery() throws Exception {
        ContentUserNotificationSettingVO vo = new ContentUserNotificationSettingVO()
            .setUserId("u1")
            .setLikeNoticeEnabled(true)
            .setCommentNoticeEnabled(false);
        when(notificationSettingService.getSetting("u1")).thenReturn(vo);

        mockMvc.perform(get("/content/user/settings/notification")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.userId").value("u1"))
            .andExpect(jsonPath("$.result.likeNoticeEnabled").value(true))
            .andExpect(jsonPath("$.result.commentNoticeEnabled").value(false));
    }

    /**
     * 合法的通知更新请求应返回 200。
     */
    @Test
    void shouldReturn200ForNotificationUpdate() throws Exception {
        ContentUserNotificationSettingVO vo = new ContentUserNotificationSettingVO()
            .setUserId("u1")
            .setLikeNoticeEnabled(false);
        when(notificationSettingService.updateSetting(eq("u1"), any(ContentUserNotificationUpdateReq.class)))
            .thenReturn(vo);

        ContentUserNotificationUpdateReq req = new ContentUserNotificationUpdateReq()
            .setLikeNoticeEnabled(false);

        mockMvc.perform(post("/content/user/settings/notification/update")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.userId").value("u1"))
            .andExpect(jsonPath("$.result.likeNoticeEnabled").value(false));
    }

    /**
     * 查询关注流设置应返回 200 和 VO 字段。
     */
    @Test
    void shouldReturn200ForFeedSettingQuery() throws Exception {
        ContentUserFeedSettingVO vo = new ContentUserFeedSettingVO()
            .setUserId("u1")
            .setPublishEnabled(true)
            .setLikeEnabled(true)
            .setActivityTypes(List.of("PUBLISH", "LIKE"));
        when(feedSettingService.getSetting("u1")).thenReturn(vo);

        mockMvc.perform(get("/content/user/settings/feed")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.userId").value("u1"))
            .andExpect(jsonPath("$.result.publishEnabled").value(true))
            .andExpect(jsonPath("$.result.activityTypes[0]").value("PUBLISH"));
    }

    /**
     * 合法的关注流更新请求应返回 200。
     */
    @Test
    void shouldReturn200ForFeedSettingUpdate() throws Exception {
        ContentUserFeedSettingVO vo = new ContentUserFeedSettingVO()
            .setUserId("u1")
            .setActivityTypes(List.of("PUBLISH", "FAVORITE"));
        when(feedSettingService.updateSetting(eq("u1"), any(ContentFeedSettingUpdateReq.class)))
            .thenReturn(vo);

        ContentFeedSettingUpdateReq req = new ContentFeedSettingUpdateReq()
            .setActivityTypes(List.of("PUBLISH", "FAVORITE"));

        mockMvc.perform(post("/content/user/settings/feed/update")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.userId").value("u1"))
            .andExpect(jsonPath("$.result.activityTypes[0]").value("PUBLISH"));
    }

    /**
     * 可见性检查应返回 200 和布尔结果。
     */
    @Test
    void shouldReturn200ForVisibilityCheck() throws Exception {
        when(visibilityPolicyService.canViewContent("owner", "viewer")).thenReturn(true);

        mockMvc.perform(get("/content/user/settings/visibility/content")
                .param("ownerUserId", "owner")
                .param("viewerUserId", "viewer"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value(true));
    }

    /**
     * 查询安全设置应返回 200 和 VO 字段。
     */
    @Test
    void shouldReturn200ForSecuritySetting() throws Exception {
        ContentUserSecuritySettingVO vo = new ContentUserSecuritySettingVO()
            .setDeviceManagementEnabled(true)
            .setTwoFactorEnabled(false)
            .setLoginAlertEnabled(true);
        when(securitySettingService.getSecuritySetting("u1")).thenReturn(vo);

        mockMvc.perform(get("/content/user/settings/security")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.deviceManagementEnabled").value(true))
            .andExpect(jsonPath("$.result.twoFactorEnabled").value(false))
            .andExpect(jsonPath("$.result.loginAlertEnabled").value(true));
    }
}
