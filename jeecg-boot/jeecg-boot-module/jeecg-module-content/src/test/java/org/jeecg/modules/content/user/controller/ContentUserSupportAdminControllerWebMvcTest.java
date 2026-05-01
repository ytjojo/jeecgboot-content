package org.jeecg.modules.content.user.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.req.support.ContentAppealHandleReq;
import org.jeecg.modules.content.user.req.support.ContentReportHandleReq;
import org.jeecg.modules.content.user.req.support.ContentUserReportAdminQueryReq;
import org.jeecg.modules.content.user.service.IContentUserSupportService;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminDetailVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminListItemVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminPageVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web MVC tests for content user support admin controller.
 */
@ExtendWith(MockitoExtension.class)
class ContentUserSupportAdminControllerWebMvcTest {

    @RestControllerAdvice
    static class TestJeecgBootExceptionHandler {

        @ExceptionHandler(JeecgBootException.class)
        Result<?> handleJeecgBootException(JeecgBootException e) {
            return Result.error(e.getErrCode(), e.getMessage());
        }
    }

    private MockMvc mockMvc;

    @Mock
    private IContentUserSupportService supportService;

    @InjectMocks
    private ContentUserSupportAdminController supportAdminController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(supportAdminController)
            .setValidator(validator)
            .setControllerAdvice(new TestJeecgBootExceptionHandler())
            .build();
        ReflectionTestUtils.setField(supportAdminController, "supportService", supportService);
    }

    @Test
    void shouldHandleAppeal() throws Exception {
        when(supportService.handleAppeal(any(ContentAppealHandleReq.class))).thenReturn("appeal-1");

        mockMvc.perform(post("/content/user/support/admin/appeal/handle")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"appealId":"appeal-1","operatorUserId":"admin-1","status":"RESOLVED","resultStatus":"APPROVED","resultNote":"处罚撤销","progressNote":"已处理完成"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("appeal-1"));
    }

    @Test
    void shouldReturnBusinessErrorWhenHandleAppealThrowsException() throws Exception {
        when(supportService.handleAppeal(any(ContentAppealHandleReq.class)))
            .thenThrow(new JeecgBootException("申诉不存在"));

        mockMvc.perform(post("/content/user/support/admin/appeal/handle")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"appealId":"appeal-404","operatorUserId":"admin-1","status":"RESOLVED","resultStatus":"REJECTED","resultNote":"驳回","progressNote":"已处理完成"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("申诉不存在"));
    }

    @Test
    void shouldRejectInvalidHandleRequest() throws Exception {
        mockMvc.perform(post("/content/user/support/admin/appeal/handle")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"appealId":"","operatorUserId":"admin-1","status":"RESOLVED","resultStatus":"","resultNote":"","progressNote":""}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleReport() throws Exception {
        when(supportService.handleReport(any(ContentReportHandleReq.class))).thenReturn("report-1");

        mockMvc.perform(post("/content/user/support/admin/report/handle")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"reportId":"report-1","operatorUserId":"admin-1","status":"RESOLVED","resultStatus":"CONFIRMED","resultNote":"违规成立","progressNote":"已处理完成"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("report-1"));
    }

    @Test
    void shouldReturnBusinessErrorWhenHandleReportThrowsException() throws Exception {
        when(supportService.handleReport(any(ContentReportHandleReq.class)))
            .thenThrow(new JeecgBootException("举报不存在"));

        mockMvc.perform(post("/content/user/support/admin/report/handle")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"reportId":"report-404","operatorUserId":"admin-1","status":"RESOLVED","resultStatus":"CONFIRMED","resultNote":"违规成立","progressNote":"已处理完成"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("举报不存在"));
    }

    @Test
    void shouldRejectInvalidHandleReportRequest() throws Exception {
        mockMvc.perform(post("/content/user/support/admin/report/handle")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"reportId":"","operatorUserId":"admin-1","status":"RESOLVED","resultStatus":"","resultNote":"","progressNote":""}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldListReportsForAdminWithPageQuery() throws Exception {
        Date resolvedAt = new Date(1735689600000L);
        when(supportService.listReportsForAdmin(any(ContentUserReportAdminQueryReq.class)))
            .thenReturn(new ContentUserReportAdminPageVO()
                .setTotal(1L)
                .setPageNo(2L)
                .setPageSize(20L)
                .setRecords(List.of(new ContentUserReportAdminListItemVO()
                    .setReportId("report-1")
                    .setUserId("u1")
                    .setTargetType("CONTENT")
                    .setTargetId("post-1")
                    .setReportType("SPAM")
                    .setStatus("RESOLVED")
                    .setResultStatus("CONFIRMED")
                    .setResolvedBy("admin-1")
                    .setResolvedAt(resolvedAt)
                    .setCreateTime(resolvedAt))));

        mockMvc.perform(get("/content/user/support/admin/report/list")
                .param("pageNo", "2")
                .param("pageSize", "20")
                .param("status", "RESOLVED")
                .param("resultStatus", "CONFIRMED")
                .param("userId", "u1")
                .param("targetType", "CONTENT")
                .param("targetId", "post-1")
                .param("reportType", "SPAM")
                .param("resolvedBy", "admin-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.total").value(1))
            .andExpect(jsonPath("$.result.pageNo").value(2))
            .andExpect(jsonPath("$.result.pageSize").value(20))
            .andExpect(jsonPath("$.result.records[0].reportId").value("report-1"));
    }

    @Test
    void shouldRejectInvalidPageSizeForAdminReportList() throws Exception {
        mockMvc.perform(get("/content/user/support/admin/report/list")
                .param("pageNo", "1")
                .param("pageSize", "101"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBindCreateTimeRangeForAdminReportList() throws Exception {
        Date resolvedAt = new Date(1735689600000L);
        when(supportService.listReportsForAdmin(argThat(req ->
            req.getCreateTimeStart() != null
                && req.getCreateTimeEnd() != null
                && req.getPageNo() == 1L
                && req.getPageSize() == 10L)))
            .thenReturn(new ContentUserReportAdminPageVO()
                .setTotal(1L)
                .setPageNo(1L)
                .setPageSize(10L)
                .setRecords(List.of(new ContentUserReportAdminListItemVO()
                    .setReportId("report-1")
                    .setCreateTime(resolvedAt))));

        mockMvc.perform(get("/content/user/support/admin/report/list")
                .param("pageNo", "1")
                .param("pageSize", "10")
                .param("createTimeStart", "2026-05-01 10:00:00")
                .param("createTimeEnd", "2026-05-01 12:00:00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.total").value(1))
            .andExpect(jsonPath("$.result.records[0].reportId").value("report-1"));
    }

    @Test
    void shouldReturnBusinessErrorWhenListReportsThrowsException() throws Exception {
        when(supportService.listReportsForAdmin(any(ContentUserReportAdminQueryReq.class)))
            .thenThrow(new JeecgBootException("创建时间范围非法"));

        mockMvc.perform(get("/content/user/support/admin/report/list")
                .param("createTimeStart", "2026-05-01 12:00:00")
                .param("createTimeEnd", "2026-05-01 10:00:00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("创建时间范围非法"));
    }

    @Test
    void shouldRejectInvalidCreateTimeFormatForAdminReportList() throws Exception {
        mockMvc.perform(get("/content/user/support/admin/report/list")
                .param("createTimeStart", "2026/05/01 10:00:00")
                .param("createTimeEnd", "2026-05-01 12:00:00"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetReportDetailForAdmin() throws Exception {
        Date resolvedAt = new Date(1735689600000L);
        when(supportService.getReportDetailForAdmin("report-1"))
            .thenReturn(new ContentUserReportAdminDetailVO()
                .setReportId("report-1")
                .setUserId("u1")
                .setTargetType("CONTENT")
                .setTargetId("post-1")
                .setReportType("SPAM")
                .setReason("垃圾内容")
                .setEvidenceJson("{\"screenshot\":true}")
                .setStatus("RESOLVED")
                .setResultStatus("CONFIRMED")
                .setResultNote("违规成立")
                .setProgressNote("已处理完成")
                .setResolvedBy("admin-1")
                .setResolvedAt(resolvedAt)
                .setCreateTime(resolvedAt));

        mockMvc.perform(get("/content/user/support/admin/report/detail")
                .param("reportId", "report-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.reportId").value("report-1"))
            .andExpect(jsonPath("$.result.reason").value("垃圾内容"))
            .andExpect(jsonPath("$.result.resultStatus").value("CONFIRMED"));
    }

    @Test
    void shouldReturnBusinessErrorWhenGetReportDetailThrowsException() throws Exception {
        when(supportService.getReportDetailForAdmin("report-404"))
            .thenThrow(new JeecgBootException("举报不存在"));

        mockMvc.perform(get("/content/user/support/admin/report/detail")
                .param("reportId", "report-404"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("举报不存在"));
    }
}
