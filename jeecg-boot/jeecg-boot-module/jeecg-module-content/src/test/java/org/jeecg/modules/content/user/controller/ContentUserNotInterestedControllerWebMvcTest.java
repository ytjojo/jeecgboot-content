package org.jeecg.modules.content.user.controller;

import org.jeecg.modules.content.user.service.IContentUserNotInterestedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 内容社区不感兴趣反馈 Controller WebMvc 测试。
 */
class ContentUserNotInterestedControllerWebMvcTest {

    private IContentUserNotInterestedService notInterestedService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ContentUserNotInterestedController controller = new ContentUserNotInterestedController();
        notInterestedService = mock(IContentUserNotInterestedService.class);
        ReflectionTestUtils.setField(controller, "notInterestedService", notInterestedService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldRecordNotInterestedFeedback() throws Exception {
        mockMvc.perform(post("/content/user/not-interested")
                .param("userId", "u1")
                .param("contentId", "c1")
                .param("contentType", "article"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("记录成功"));

        verify(notInterestedService).recordFeedback("u1", "c1", "article");
    }
}
