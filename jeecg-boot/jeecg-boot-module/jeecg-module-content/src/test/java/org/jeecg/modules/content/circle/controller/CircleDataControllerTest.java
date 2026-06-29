package org.jeecg.modules.content.circle.controller;

import com.alibaba.fastjson2.JSON;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.mapper.CircleMapper;
import org.jeecg.modules.content.circle.mapper.CircleMemberMapper;
import org.jeecg.modules.content.circle.service.ICircleDataService;
import org.jeecg.modules.content.circle.vo.CircleDataStatisticsVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CircleDataController WebMvc 测试")
class CircleDataControllerTest {

    private static final String TEST_USER_ID = "user-creator-001";
    private static final String TEST_CIRCLE_ID = "circle-1";

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

    @Mock
    private CircleMapper circleMapper;

    @Mock
    private CircleMemberMapper circleMemberMapper;

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

        LoginUser loginUser = new LoginUser();
        loginUser.setId(TEST_USER_ID);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                JSON.toJSONString(loginUser), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Circle circle = new Circle();
        circle.setId(TEST_CIRCLE_ID);
        circle.setCreatorId(TEST_USER_ID);
        when(circleMapper.selectById(eq(TEST_CIRCLE_ID))).thenReturn(circle);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getStatistics - 正常请求返回数据")
    void shouldReturnStatisticsWhenAuthorized() throws Exception {
        CircleDataStatisticsVO vo = new CircleDataStatisticsVO();
        vo.setMemberCount(100);
        vo.setNewMemberCount(10);
        when(circleDataService.getStatistics(eq(TEST_CIRCLE_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(vo);

        mockMvc.perform(get("/api/v1/content/circle/" + TEST_CIRCLE_ID + "/data/statistics")
                        .param("startDate", LocalDate.now().minusDays(7).toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.memberCount").value(100));
    }

    @Test
    @DisplayName("getStatistics - 未登录返回错误")
    void shouldReturnErrorWhenNotAuthenticated() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/content/circle/" + TEST_CIRCLE_ID + "/data/statistics")
                        .param("startDate", LocalDate.now().minusDays(7).toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }
}
