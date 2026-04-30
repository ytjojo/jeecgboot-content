package org.jeecg.modules.content.user.controller;

import org.jeecg.modules.content.user.service.IContentUserGovernanceService;
import org.jeecg.modules.content.user.service.IContentUserProfileService;
import org.jeecg.modules.content.user.service.IContentUserRelationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(
    controllers = {
        ContentUserGovernanceController.class,
        ContentUserProfileController.class,
        ContentUserRelationController.class
    },
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
    }
)
class ContentUserControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IContentUserGovernanceService governanceService;

    @MockitoBean
    private IContentUserProfileService profileService;

    @MockitoBean
    private IContentUserRelationService relationService;

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
}
