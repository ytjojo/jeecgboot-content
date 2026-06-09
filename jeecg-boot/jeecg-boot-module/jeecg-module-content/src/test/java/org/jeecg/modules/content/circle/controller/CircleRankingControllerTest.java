package org.jeecg.modules.content.circle.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.circle.service.ICircleRankingService;
import org.jeecg.modules.content.circle.vo.CircleRankingVO;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleRankingController WebMvc 测试")
class CircleRankingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ICircleRankingService rankingService;

    @InjectMocks
    private CircleRankingController rankingController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(rankingController).build();
    }

    @Test
    @DisplayName("getHotRanking - 返回热门榜单")
    void shouldReturnHotRanking() throws Exception {
        // Given
        CircleRankingVO vo = new CircleRankingVO();
        vo.setType("HOT");
        CircleRankingVO.CircleRankingItem item = new CircleRankingVO.CircleRankingItem();
        item.setRank(1);
        item.setCircleId("circle-1");
        item.setCircleName("技术圈");
        vo.setItems(Collections.singletonList(item));

        when(rankingService.getHotRanking(20)).thenReturn(vo);

        // When & Then
        mockMvc.perform(get("/api/v1/content/circle/ranking/hot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.type").value("HOT"))
                .andExpect(jsonPath("$.result.items[0].rank").value(1));
    }

    @Test
    @DisplayName("getNewRanking - 返回新增榜单")
    void shouldReturnNewRanking() throws Exception {
        // Given
        CircleRankingVO vo = new CircleRankingVO();
        vo.setType("NEW");
        vo.setItems(Collections.emptyList());

        when(rankingService.getNewRanking(20)).thenReturn(vo);

        // When & Then
        mockMvc.perform(get("/api/v1/content/circle/ranking/new"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.type").value("NEW"));
    }
}
