package org.jeecg.modules.content.circle.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.circle.service.ICircleRecommendService;
import org.jeecg.modules.content.circle.vo.CircleRecommendVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleRecommendController WebMvc 测试")
class CircleRecommendControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ICircleRecommendService recommendService;

    @InjectMocks
    private CircleRecommendController recommendController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(recommendController).build();
    }

    @Test
    @DisplayName("getRecommendations - 正常请求返回推荐列表")
    void shouldReturnRecommendations() throws Exception {
        // Given
        CircleRecommendVO vo = new CircleRecommendVO();
        CircleRecommendVO.CircleRecommendItem item = new CircleRecommendVO.CircleRecommendItem();
        item.setCircleId("circle-1");
        item.setCircleName("技术圈");
        vo.setItems(Collections.singletonList(item));

        when(recommendService.getRecommendations("user-1", 10)).thenReturn(vo);

        // When & Then
        mockMvc.perform(get("/api/circle/recommend")
                        .param("userId", "user-1")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.items[0].circleId").value("circle-1"));
    }

    @Test
    @DisplayName("recordClick - 记录推荐点击")
    void shouldRecordClick() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/circle/recommend/click")
                        .param("sourceId", "source-1")
                        .param("userId", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
