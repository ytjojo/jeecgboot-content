package org.jeecg.modules.content.circle.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.service.ICircleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleSearchController WebMvc")
class CircleSearchControllerWebMvcTest {

    private MockMvc mockMvc;

    @Mock
    private ICircleService circleService;

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

    @Nested
    @DisplayName("search")
    class Search {

        @Test
        @DisplayName("keyword matches - returns results")
        void keywordMatches_returnsResults() throws Exception {
            Circle circle = new Circle();
            circle.setId("c_001");
            circle.setName("Java技术圈");
            circle.setDescription("Java技术交流");
            circle.setIconUrl("http://icon.png");
            circle.setMemberCount(100);

            doAnswer(inv -> {
                Page<Circle> p = inv.getArgument(0);
                p.setRecords(List.of(circle));
                return p;
            }).when(circleService).page(any(), any());

            mockMvc.perform(get("/content/circle/search")
                            .param("keyword", "Java"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result[0].id").value("c_001"))
                    .andExpect(jsonPath("$.result[0].name").value("Java技术圈"));
        }

        @Test
        @DisplayName("no results - returns empty list")
        void noResults_returnsEmptyList() throws Exception {
            doAnswer(inv -> {
                Page<Circle> p = inv.getArgument(0);
                p.setRecords(List.of());
                return p;
            }).when(circleService).page(any(), any());

            mockMvc.perform(get("/content/circle/search")
                            .param("keyword", "不存在的关键词"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result").isEmpty());
        }
    }
}
