package org.jeecg.modules.content.circle.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.service.ICircleDataService;
import org.jeecg.modules.content.circle.vo.CircleDataStatisticsVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleDataController WebMvc 测试")
class CircleDataControllerTest {

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(JeecgBootException.class)
        Result<?> handleJeecgBootException(JeecgBootException e) {
            return Result.error(e.getErrCode(), e.getMessage());
        }
    }

    private MockMvc mockMvc;

    @Mock
    private ICircleDataService circleDataService;

    @InjectMocks
    private CircleDataController circleDataController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(circleDataController)
                .setValidator(validator)
                .setControllerAdvice(new TestExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("getStatistics - 正常请求返回数据")
    void shouldReturnStatisticsWhenAuthorized() throws Exception {
        // Given
        CircleDataStatisticsVO vo = new CircleDataStatisticsVO();
        vo.setMemberCount(100);
        vo.setNewMemberCount(10);
        when(circleDataService.getStatistics(eq("circle-1"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(vo);

        // When & Then
        mockMvc.perform(get("/api/circle/circle-1/data/statistics")
                        .param("startDate", LocalDate.now().minusDays(7).toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.memberCount").value(100));
    }
}
