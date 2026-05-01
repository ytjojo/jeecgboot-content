package org.jeecg.modules.content.user.controller;

import org.jeecg.modules.content.user.service.IContentUserGovernanceService;
import org.jeecg.modules.content.user.service.IContentUserProfileService;
import org.jeecg.modules.content.user.service.IContentUserRelationService;
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

    @InjectMocks
    private ContentUserGovernanceController governanceController;

    @InjectMocks
    private ContentUserProfileController profileController;

    @InjectMocks
    private ContentUserRelationController relationController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(governanceController, profileController, relationController)
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
}
