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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ContentUserSettingsControllerWebMvcTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private IContentUserProfileService profileService;
    @Mock
    private IContentUserVisibilityPolicyService visibilityPolicyService;
    @Mock
    private IContentUserNotificationSettingService notificationSettingService;
    @Mock
    private IContentUserFeedSettingService feedSettingService;
    @Mock
    private IContentUserSecuritySettingService securitySettingService;

    @InjectMocks
    private ContentUserSettingsController controller;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setValidator(validator)
            .build();
    }

    @Test
    void shouldUpdatePrivacy() throws Exception {
        ContentUserPrivacyUpdateReq req = new ContentUserPrivacyUpdateReq();

        mockMvc.perform(post("/content/user/settings/privacy/update")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("更新成功"));

        verify(profileService).updatePrivacy(eq("u1"), any(ContentUserPrivacyUpdateReq.class));
    }

    @Test
    void shouldGetNotificationSetting() throws Exception {
        ContentUserNotificationSettingVO vo = new ContentUserNotificationSettingVO();
        when(notificationSettingService.getSetting("u1")).thenReturn(vo);

        mockMvc.perform(get("/content/user/settings/notification")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(notificationSettingService).getSetting("u1");
    }

    @Test
    void shouldUpdateNotificationSetting() throws Exception {
        ContentUserNotificationSettingVO vo = new ContentUserNotificationSettingVO();
        ContentUserNotificationUpdateReq req = new ContentUserNotificationUpdateReq();
        when(notificationSettingService.updateSetting(eq("u1"), any())).thenReturn(vo);

        mockMvc.perform(post("/content/user/settings/notification/update")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(notificationSettingService).updateSetting(eq("u1"), any(ContentUserNotificationUpdateReq.class));
    }

    @Test
    void shouldGetFeedSetting() throws Exception {
        ContentUserFeedSettingVO vo = new ContentUserFeedSettingVO();
        when(feedSettingService.getSetting("u1")).thenReturn(vo);

        mockMvc.perform(get("/content/user/settings/feed")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(feedSettingService).getSetting("u1");
    }

    @Test
    void shouldUpdateFeedSetting() throws Exception {
        ContentUserFeedSettingVO vo = new ContentUserFeedSettingVO();
        ContentFeedSettingUpdateReq req = new ContentFeedSettingUpdateReq();
        when(feedSettingService.updateSetting(eq("u1"), any())).thenReturn(vo);

        mockMvc.perform(post("/content/user/settings/feed/update")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(feedSettingService).updateSetting(eq("u1"), any(ContentFeedSettingUpdateReq.class));
    }

    @Test
    void shouldCheckContentVisibility() throws Exception {
        when(visibilityPolicyService.canViewContent("owner1", "viewer1")).thenReturn(true);

        mockMvc.perform(get("/content/user/settings/visibility/content")
                .param("ownerUserId", "owner1")
                .param("viewerUserId", "viewer1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value(true));

        verify(visibilityPolicyService).canViewContent("owner1", "viewer1");
    }

    @Test
    void shouldUpdateDndRule() throws Exception {
        ContentNotificationDndRuleVO vo = new ContentNotificationDndRuleVO();
        ContentNotificationDndRuleReq req = new ContentNotificationDndRuleReq();
        when(notificationSettingService.updateDndRule(eq("u1"), any())).thenReturn(vo);

        mockMvc.perform(post("/content/user/settings/notification/dnd/update")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(notificationSettingService).updateDndRule(eq("u1"), any(ContentNotificationDndRuleReq.class));
    }

    @Test
    void shouldGetSecuritySetting() throws Exception {
        ContentUserSecuritySettingVO vo = new ContentUserSecuritySettingVO();
        when(securitySettingService.getSecuritySetting("u1")).thenReturn(vo);

        mockMvc.perform(get("/content/user/settings/security")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(securitySettingService).getSecuritySetting("u1");
    }
}
