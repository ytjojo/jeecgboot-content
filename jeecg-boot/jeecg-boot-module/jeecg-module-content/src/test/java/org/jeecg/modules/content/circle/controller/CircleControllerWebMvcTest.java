package org.jeecg.modules.content.circle.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.biz.ICircleBiz;
import org.jeecg.modules.content.circle.biz.ICircleMemberBiz;
import org.jeecg.modules.content.circle.vo.CircleVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleController WebMvc")
class CircleControllerWebMvcTest {

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(JeecgBootException.class)
        Result<?> handleJeecgBootException(JeecgBootException e) {
            return Result.error(e.getErrCode(), e.getMessage());
        }

        @ExceptionHandler(IllegalStateException.class)
        Result<?> handleIllegalStateException(IllegalStateException e) {
            return Result.error(510, e.getMessage());
        }
    }

    private MockMvc mockMvc;

    @Mock
    private ICircleBiz circleBiz;

    @Mock
    private ICircleMemberBiz circleMemberBiz;

    @InjectMocks
    private CircleController controller;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .setControllerAdvice(new TestExceptionHandler())
                .build();

        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getName()).thenReturn("{\"id\":\"u_001\"}");
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("createCircle")
    class CreateCircle {

        @Test
        @DisplayName("valid request - returns success")
        void validRequest_returnsSuccess() throws Exception {
            CircleVO vo = new CircleVO();
            vo.setId("c_001");
            vo.setName("测试圈子");

            when(circleBiz.createCircle(any(), eq("u_001"))).thenReturn(vo);

            mockMvc.perform(post("/api/v1/content/circle/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"测试圈子","description":"简介","privacyType":"PUBLIC","joinType":"DIRECT"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result.id").value("c_001"));
        }

        @Test
        @DisplayName("blank name - returns 400")
        void blankName_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/content/circle/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"","description":"简介","privacyType":"PUBLIC","joinType":"DIRECT"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("name exists - returns business error")
        void nameExists_returnsBusinessError() throws Exception {
            when(circleBiz.createCircle(any(), eq("u_001")))
                    .thenThrow(new JeecgBootException("该圈子名称已存在，请修改"));

            mockMvc.perform(post("/api/v1/content/circle/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"已存在","description":"简介","privacyType":"PUBLIC","joinType":"DIRECT"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("该圈子名称已存在，请修改"));
        }
    }

    @Nested
    @DisplayName("updateCircle")
    class UpdateCircle {

        @Test
        @DisplayName("valid request - returns success")
        void validRequest_returnsSuccess() throws Exception {
            mockMvc.perform(put("/api/v1/content/circle/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"circleId":"c_001","description":"新简介"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("publicList")
    class PublicList {

        @Test
        @DisplayName("anonymous access - returns list without NPE")
        void anonymousAccess_returnsListWithoutNpe() throws Exception {
            SecurityContextHolder.clearContext();

            Page<CircleVO> page = new Page<>(1, 20, 0);
            when(circleBiz.publicList(eq(1), eq(20), eq(null))).thenReturn(page);

            mockMvc.perform(get("/api/v1/content/circle/public-list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("checkName")
    class CheckName {

        @Test
        @DisplayName("name available - returns true")
        void nameAvailable_returnsTrue() throws Exception {
            when(circleBiz.checkNameAvailable("新圈子")).thenReturn(true);

            mockMvc.perform(get("/api/v1/content/circle/check-name")
                            .param("name", "新圈子"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result").value(true));
        }

        @Test
        @DisplayName("name exists - returns false")
        void nameExists_returnsFalse() throws Exception {
            when(circleBiz.checkNameAvailable("已存在")).thenReturn(false);

            mockMvc.perform(get("/api/v1/content/circle/check-name")
                            .param("name", "已存在"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result").value(false));
        }

        @Test
        @DisplayName("system error - returns error")
        void systemError_returnsError() throws Exception {
            when(circleBiz.checkNameAvailable("错误")).thenThrow(new JeecgBootException("数据库连接失败"));

            mockMvc.perform(get("/api/v1/content/circle/check-name")
                            .param("name", "错误"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
