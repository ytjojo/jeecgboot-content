package org.jeecg.modules.content.circle.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.biz.CircleAnnouncementBizService;
import org.jeecg.modules.content.circle.entity.CircleAnnouncement;
import org.jeecg.modules.content.circle.service.ICircleAnnouncementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleAnnouncementController WebMvc")
class CircleAnnouncementControllerWebMvcTest {

    private static final String TEST_OPERATOR_ID = "u_001";

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(JeecgBootException.class)
        Result<?> handleJeecgBootException(JeecgBootException e) {
            return Result.error(e.getErrCode(), e.getMessage());
        }
    }

    private MockMvc mockMvc;

    @Mock
    private CircleAnnouncementBizService circleAnnouncementBizService;

    @Mock
    private ICircleAnnouncementService circleAnnouncementService;

    @InjectMocks
    private CircleAnnouncementController controller;

    private String validJwtToken;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .setControllerAdvice(new TestExceptionHandler())
                .build();

        validJwtToken = JWT.create()
                .withClaim("username", TEST_OPERATOR_ID)
                .sign(Algorithm.HMAC256("test-secret"));
    }

    // ==================== publish ====================

    @Nested
    @DisplayName("publish")
    class Publish {

        @Test
        @DisplayName("valid request - returns success and forwards fields to biz")
        void validRequest_returnsSuccess() throws Exception {
            mockMvc.perform(post("/api/v1/content/circle/announcement/")
                            .header("X-Access-Token", validJwtToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"circleId":"c_001","content":"新公告内容"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("发布成功"));

            ArgumentCaptor<CircleAnnouncement> captor = ArgumentCaptor.forClass(CircleAnnouncement.class);
            verify(circleAnnouncementBizService).publish(captor.capture(), eq(TEST_OPERATOR_ID));
            assertThat(captor.getValue().getCircleId()).isEqualTo("c_001");
            assertThat(captor.getValue().getContent()).isEqualTo("新公告内容");
        }

        @Test
        @DisplayName("blank content - returns 400")
        void blankContent_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/content/circle/announcement/")
                            .header("X-Access-Token", validJwtToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"circleId":"c_001","content":""}
                                    """))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(circleAnnouncementBizService);
        }

        @Test
        @DisplayName("biz throws - returns business error")
        void bizThrows_returnsBusinessError() throws Exception {
            doThrow(new JeecgBootException("该圈子无发布权限"))
                    .when(circleAnnouncementBizService).publish(any(), eq(TEST_OPERATOR_ID));

            mockMvc.perform(post("/api/v1/content/circle/announcement/")
                            .header("X-Access-Token", validJwtToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"circleId":"c_001","content":"新公告"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("该圈子无发布权限"));
        }
    }

    // ==================== getActive ====================

    @Nested
    @DisplayName("getActive")
    class GetActive {

        @Test
        @DisplayName("active exists - returns VO with fields mapped")
        void activeExists_returnsVO() throws Exception {
            CircleAnnouncement active = new CircleAnnouncement();
            active.setId("a_001");
            active.setCircleId("c_001");
            active.setContent("有效公告");
            active.setStatus("ACTIVE");
            active.setExpireAt(new Date(1735689600000L));
            when(circleAnnouncementService.getActiveByCircleId("c_001")).thenReturn(active);

            mockMvc.perform(get("/api/v1/content/circle/announcement/active/c_001")
                            .header("X-Access-Token", validJwtToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result.id").value("a_001"))
                    .andExpect(jsonPath("$.result.circleId").value("c_001"))
                    .andExpect(jsonPath("$.result.content").value("有效公告"));
        }

        @Test
        @DisplayName("active not found - returns OK with null result, no 500")
        void activeNotFound_returnsOKWithNull() throws Exception {
            when(circleAnnouncementService.getActiveByCircleId("c_empty")).thenReturn(null);

            mockMvc.perform(get("/api/v1/content/circle/announcement/active/c_empty")
                            .header("X-Access-Token", validJwtToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result").doesNotExist());
        }
    }
}
