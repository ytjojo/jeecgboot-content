package org.jeecg.modules.content.circle.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.content.circle.biz.ICircleBiz;
import org.jeecg.modules.content.circle.req.query.CircleSearchReq;
import org.jeecg.modules.content.circle.vo.CircleSearchResultVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleSearchController WebMvc")
class CircleSearchControllerWebMvcTest {

    private MockMvc mockMvc;

    @Mock
    private ICircleBiz circleBiz;

    @InjectMocks
    private CircleSearchController controller;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("search")
    class Search {

        @Test
        @DisplayName("anonymous access - returns results with joined=false")
        void anonymousAccess_returnsResultsWithJoinedFalse() throws Exception {
            CircleSearchResultVO vo = new CircleSearchResultVO();
            vo.setId("c_001");
            vo.setName("Java技术圈");
            vo.setDescription("Java技术交流");
            vo.setIconUrl("http://icon.png");
            vo.setMemberCount(100);
            vo.setJoined(false);

            Page<CircleSearchResultVO> page = new Page<>(1, 20, 1);
            page.setRecords(List.of(vo));
            when(circleBiz.search(any(CircleSearchReq.class), eq(null))).thenReturn(page);

            mockMvc.perform(get("/api/v1/content/circle/search")
                            .param("keyword", "Java"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result.records[0].id").value("c_001"))
                    .andExpect(jsonPath("$.result.records[0].name").value("Java技术圈"))
                    .andExpect(jsonPath("$.result.records[0].joined").value(false));
        }

        @Test
        @DisplayName("no results - returns empty list")
        void noResults_returnsEmptyList() throws Exception {
            Page<CircleSearchResultVO> page = new Page<>(1, 20, 0);
            page.setRecords(List.of());
            when(circleBiz.search(any(CircleSearchReq.class), eq(null))).thenReturn(page);

            mockMvc.perform(get("/api/v1/content/circle/search")
                            .param("keyword", "不存在的关键词"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result.records").isEmpty());
        }
    }
}
