package org.jeecg.modules.content.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeecg.modules.content.user.req.profile.ContentUserHomepageUpdateReq;
import org.jeecg.modules.content.user.req.profile.ContentUserPrivacyUpdateReq;
import org.jeecg.modules.content.user.req.profile.ContentUserProfileUpdateReq;
import org.jeecg.modules.content.user.service.IContentUserHomepageService;
import org.jeecg.modules.content.user.service.IContentUserProfileHistoryService;
import org.jeecg.modules.content.user.service.IContentUserProfileService;
import org.jeecg.modules.content.user.service.IContentUserVerificationBadgeService;
import org.jeecg.modules.content.user.vo.ContentUserProfileVO;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 内容社区用户资料 Controller WebMvc 测试。
 */
class ContentUserProfileControllerWebMvcTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private IContentUserProfileService profileService;
    private IContentUserHomepageService homepageService;
    private IContentUserVerificationBadgeService verificationBadgeService;
    private IContentUserProfileHistoryService historyService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ContentUserProfileController controller = new ContentUserProfileController();
        profileService = mock(IContentUserProfileService.class);
        homepageService = mock(IContentUserHomepageService.class);
        verificationBadgeService = mock(IContentUserVerificationBadgeService.class);
        historyService = mock(IContentUserProfileHistoryService.class);
        ReflectionTestUtils.setField(controller, "profileService", profileService);
        ReflectionTestUtils.setField(controller, "homepageService", homepageService);
        ReflectionTestUtils.setField(controller, "verificationBadgeService", verificationBadgeService);
        ReflectionTestUtils.setField(controller, "profileHistoryService", historyService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldReturnProfileDetailAndAcceptProfileUpdate() throws Exception {
        when(profileService.getProfile("u1", "viewer")).thenReturn(new ContentUserProfileVO().setUserId("u1").setNickname("作者"));
        when(profileService.updateProfile(eq("u1"), any(ContentUserProfileUpdateReq.class)))
            .thenReturn(new ContentUserProfileVO().setUserId("u1").setNickname("作者更新"));

        mockMvc.perform(get("/api/v1/content/user/profile/detail").param("ownerUserId", "u1").param("viewerUserId", "viewer"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.userId").value("u1"));

        mockMvc.perform(post("/api/v1/content/user/profile/update")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ContentUserProfileUpdateReq()
                    .setNickname("作者")
                    .setAvatar("https://cdn.example.com/a.png"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.userId").value("u1"))
            .andExpect(jsonPath("$.result.nickname").value("作者更新"));
        verify(profileService).updateProfile(eq("u1"), any(ContentUserProfileUpdateReq.class));
    }

    @Test
    void shouldExposePrivacyHomepageBadgeAndHistoryEndpoints() throws Exception {
        mockMvc.perform(post("/api/v1/content/user/profile/privacy/update")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ContentUserPrivacyUpdateReq().setBirthdayVisibility("PRIVATE"))))
            .andExpect(status().isOk());

        ContentUserProfileVO homepageResult = new ContentUserProfileVO().setUserId("u1").setThemeColor("#123456");
        when(homepageService.updateHomepage(eq("u1"), any(ContentUserHomepageUpdateReq.class))).thenReturn(homepageResult);
        mockMvc.perform(post("/api/v1/content/user/profile/homepage/update")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ContentUserHomepageUpdateReq().setThemeColor("#123456"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.themeColor").value("#123456"));

        ContentUserProfileVO restoredDefault = new ContentUserProfileVO().setUserId("u1").setThemeColor("#1677ff");
        when(homepageService.restoreDefaults("u1")).thenReturn(restoredDefault);
        mockMvc.perform(post("/api/v1/content/user/profile/homepage/defaults/restore").param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.themeColor").value("#1677ff"));

        when(homepageService.listModules("u1")).thenReturn(List.of());
        when(verificationBadgeService.listVisibleBadges("u1")).thenReturn(List.of());
        when(historyService.listHistory("u1", "NICKNAME")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/content/user/profile/homepage/modules").param("userId", "u1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/content/user/profile/badge/list").param("userId", "u1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/content/user/profile/history/list").param("userId", "u1").param("historyType", "NICKNAME")).andExpect(status().isOk());

        ContentUserProfileVO historyRestoreResult = new ContentUserProfileVO().setUserId("u1").setNickname("旧昵称");
        when(historyService.restoreHistory("u1", "h1")).thenReturn(historyRestoreResult);
        mockMvc.perform(post("/api/v1/content/user/profile/history/restore").param("userId", "u1").param("historyId", "h1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.nickname").value("旧昵称"));
        verify(profileService).updatePrivacy(eq("u1"), any(ContentUserPrivacyUpdateReq.class));
        verify(historyService).restoreHistory("u1", "h1");
    }

    @Test
    void shouldRejectInvalidProfileRequestAtControllerBoundary() throws Exception {
        mockMvc.perform(post("/api/v1/content/user/profile/update")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ContentUserProfileUpdateReq().setNickname("").setAvatar(""))))
            .andExpect(status().isBadRequest());
    }
}
