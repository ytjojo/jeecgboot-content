package org.jeecg.modules.content.user.controller;

import org.jeecg.modules.content.user.service.IContentUserFollowRecommendationService;
import org.jeecg.modules.content.user.service.IContentUserRelationService;
import org.jeecg.modules.content.user.vo.ContentBlockMuteHelpVO;
import org.jeecg.modules.content.user.vo.ContentUserBlacklistPageVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 内容社区用户关系 Controller WebMvc 测试。
 */
class ContentUserRelationControllerWebMvcTest {

    private IContentUserRelationService relationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ContentUserRelationController controller = new ContentUserRelationController();
        relationService = mock(IContentUserRelationService.class);
        IContentUserFollowRecommendationService recommendationService = mock(IContentUserFollowRecommendationService.class);
        ReflectionTestUtils.setField(controller, "relationService", relationService);
        ReflectionTestUtils.setField(controller, "recommendationService", recommendationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldReturnBlockMuteHelp() throws Exception {
        ContentBlockMuteHelpVO help = new ContentBlockMuteHelpVO()
            .setBlockConfirmation("拉黑确认文案")
            .setMuteConfirmation("屏蔽确认文案")
            .setUnblockConfirmation("解除拉黑文案")
            .setBlockVsMuteComparison("拉黑是双向切断，屏蔽是单向降噪");
        when(relationService.getBlockMuteHelp()).thenReturn(help);

        mockMvc.perform(get("/api/v1/content/user/relation/block-mute/help"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.blockConfirmation").value("拉黑确认文案"))
            .andExpect(jsonPath("$.result.muteConfirmation").value("屏蔽确认文案"))
            .andExpect(jsonPath("$.result.unblockConfirmation").value("解除拉黑文案"))
            .andExpect(jsonPath("$.result.blockVsMuteComparison").value("拉黑是双向切断，屏蔽是单向降噪"));
    }

    @Test
    void shouldReturnBlacklist() throws Exception {
        when(relationService.listBlacklist("u1", 1L, 10L))
            .thenReturn(new ContentUserBlacklistPageVO()
                .setRecords(java.util.Collections.emptyList())
                .setTotal(0L).setPageNo(1L).setPageSize(10L));

        mockMvc.perform(get("/api/v1/content/user/relation/blacklist")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").exists());
    }

    @Test
    void shouldBlacklistUser() throws Exception {
        mockMvc.perform(post("/api/v1/content/user/relation/block")
                .param("userId", "u1")
                .param("targetUserId", "u2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("拉黑成功"));
    }

    @Test
    void shouldUnblacklistUser() throws Exception {
        mockMvc.perform(post("/api/v1/content/user/relation/unblock")
                .param("userId", "u1")
                .param("targetUserId", "u2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("解除拉黑成功"));
    }

    @Test
    void shouldMuteUser() throws Exception {
        mockMvc.perform(post("/api/v1/content/user/relation/mute")
                .param("userId", "u1")
                .param("targetUserId", "u2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("屏蔽成功"));
    }

    @Test
    void shouldUnmuteUser() throws Exception {
        mockMvc.perform(post("/api/v1/content/user/relation/mute/cancel")
                .param("userId", "u1")
                .param("targetUserId", "u2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("解除屏蔽成功"));
    }
}
