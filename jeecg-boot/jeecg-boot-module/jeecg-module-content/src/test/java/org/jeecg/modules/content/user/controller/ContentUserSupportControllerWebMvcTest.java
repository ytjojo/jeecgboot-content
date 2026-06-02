package org.jeecg.modules.content.user.controller;

import org.jeecg.modules.content.user.req.support.ContentAppealCreateReq;
import org.jeecg.modules.content.user.req.support.ContentAppealHandleReq;
import org.jeecg.modules.content.user.req.support.ContentReportCreateReq;
import org.jeecg.modules.content.user.req.support.ContentReportHandleReq;
import org.jeecg.modules.content.user.req.support.ContentUserReportAdminQueryReq;
import org.jeecg.modules.content.user.service.IContentUserSupportService;
import org.jeecg.modules.content.user.vo.ContentCustomerServiceVO;
import org.jeecg.modules.content.user.vo.ContentHelpCenterEntryVO;
import org.jeecg.modules.content.user.vo.ContentHelpCenterVO;
import org.jeecg.modules.content.user.vo.ContentUserAppealPageVO;
import org.jeecg.modules.content.user.vo.ContentUserAppealProgressVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminDetailVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminPageVO;
import org.jeecg.modules.content.user.vo.ContentUserReportProgressVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ContentUserSupportControllerWebMvcTest {

    private MockMvc mockMvc;
    private MockMvc adminMockMvc;

    @Mock
    private IContentUserSupportService supportService;

    @InjectMocks
    private ContentUserSupportController controller;

    @InjectMocks
    private ContentUserSupportAdminController adminController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setValidator(validator)
            .build();
        adminMockMvc = MockMvcBuilders.standaloneSetup(adminController)
            .setValidator(validator)
            .build();
    }

    @Test
    void shouldCreateAppeal() throws Exception {
        when(supportService.createAppeal(any(ContentAppealCreateReq.class))).thenReturn("appeal-1");

        mockMvc.perform(post("/content/user/support/appeal/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"u1\",\"appealType\":\"BAN\",\"targetId\":\"rec1\",\"targetType\":\"STATUS_RECORD\",\"reason\":\"误判申诉\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("appeal-1"));
    }

    @Test
    void shouldCreateReport() throws Exception {
        when(supportService.createReport(any(ContentReportCreateReq.class))).thenReturn("report-1");

        mockMvc.perform(post("/content/user/support/report/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"u1\",\"targetType\":\"CONTENT\",\"targetId\":\"c1\",\"reportType\":\"SPAM\",\"reason\":\"垃圾内容\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("report-1"));
    }

    @Test
    void shouldReturnAppealProgress() throws Exception {
        when(supportService.getAppealProgress("u1", "appeal-1"))
            .thenReturn(new ContentUserAppealProgressVO()
                .setAppealId("appeal-1")
                .setStatus("PENDING")
                .setProgressNote("受理确认中")
                .setResultStatus(null)
                .setResultNote(null));

        mockMvc.perform(get("/content/user/support/appeal/progress")
                .param("userId", "u1")
                .param("appealId", "appeal-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.appealId").value("appeal-1"))
            .andExpect(jsonPath("$.result.status").value("PENDING"))
            .andExpect(jsonPath("$.result.progressNote").value("受理确认中"));
    }

    @Test
    void shouldListAppeals() throws Exception {
        when(supportService.listAppeals("u1", 1L, 10L))
            .thenReturn(new ContentUserAppealPageVO()
                .setTotal(1L)
                .setPageNo(1L)
                .setPageSize(10L)
                .setRecords(List.of()));

        mockMvc.perform(get("/content/user/support/appeal/list")
                .param("userId", "u1")
                .param("pageNo", "1")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.total").value(1))
            .andExpect(jsonPath("$.result.pageNo").value(1))
            .andExpect(jsonPath("$.result.pageSize").value(10));
    }

    @Test
    void shouldReturnReportProgress() throws Exception {
        when(supportService.getReportProgress("u1", "report-1"))
            .thenReturn(new ContentUserReportProgressVO()
                .setReportId("report-1")
                .setStatus("REVIEWING")
                .setProgressNote("审核中"));

        mockMvc.perform(get("/content/user/support/report/progress")
                .param("userId", "u1")
                .param("reportId", "report-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.status").value("REVIEWING"))
            .andExpect(jsonPath("$.result.progressNote").value("审核中"));
    }

    @Test
    void shouldReturnHelpCenter() throws Exception {
        when(supportService.getHelpCenter("u1"))
            .thenReturn(new ContentHelpCenterVO()
                .setFaqCategories(List.of(
                    new ContentHelpCenterEntryVO().setCode("ACCOUNT").setTitle("账户问题").setDescription("登录注册")
                ))
                .setGuideEntries(List.of())
                .setReleaseNotes(List.of()));

        mockMvc.perform(get("/content/user/support/help-center")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.faqCategories[0].code").value("ACCOUNT"))
            .andExpect(jsonPath("$.result.faqCategories[0].title").value("账户问题"));
    }

    @Test
    void shouldReturnCustomerServiceEntry() throws Exception {
        when(supportService.getCustomerServiceEntry("u1"))
            .thenReturn(new ContentCustomerServiceVO()
                .setRouteType("ONLINE")
                .setTitle("在线客服")
                .setDescription("工作时间9-18")
                .setManualSupported(true));

        mockMvc.perform(get("/content/user/support/customer-service")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.routeType").value("ONLINE"))
            .andExpect(jsonPath("$.result.manualSupported").value(true));
    }

    @Test
    void shouldRejectInvalidAppealCreate() throws Exception {
        mockMvc.perform(post("/content/user/support/appeal/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"\",\"appealType\":\"\",\"targetId\":\"\",\"targetType\":\"\",\"reason\":\"\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectInvalidReportCreate() throws Exception {
        mockMvc.perform(post("/content/user/support/report/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"\",\"targetType\":\"\",\"targetId\":\"\",\"reportType\":\"\",\"reason\":\"\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleAppealThroughAdmin() throws Exception {
        when(supportService.handleAppeal(any(ContentAppealHandleReq.class))).thenReturn("处理成功");

        adminMockMvc.perform(post("/content/user/support/admin/appeal/handle")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"appealId\":\"a1\",\"operatorUserId\":\"admin1\",\"status\":\"RESOLVED\",\"resultStatus\":\"APPROVED\",\"resultNote\":\"证据有效\",\"progressNote\":\"已确认\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("处理成功"));
    }

    @Test
    void shouldHandleReportThroughAdmin() throws Exception {
        when(supportService.handleReport(any(ContentReportHandleReq.class))).thenReturn("处理成功");

        adminMockMvc.perform(post("/content/user/support/admin/report/handle")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reportId\":\"r1\",\"operatorUserId\":\"admin1\",\"status\":\"RESOLVED\",\"resultStatus\":\"REMOVED\",\"resultNote\":\"违规\",\"progressNote\":\"已处理\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("处理成功"));
    }

    @Test
    void shouldListReportsThroughAdmin() throws Exception {
        when(supportService.listReportsForAdmin(any(ContentUserReportAdminQueryReq.class)))
            .thenReturn(new ContentUserReportAdminPageVO()
                .setTotal(0L)
                .setPageNo(1L)
                .setPageSize(10L)
                .setRecords(List.of()));

        adminMockMvc.perform(get("/content/user/support/admin/report/list")
                .param("pageNo", "1")
                .param("pageSize", "10")
                .param("status", "PENDING"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.total").value(0));
    }

    @Test
    void shouldReturnReportDetailThroughAdmin() throws Exception {
        when(supportService.getReportDetailForAdmin(eq("r1")))
            .thenReturn(new ContentUserReportAdminDetailVO()
                .setReportId("r1")
                .setStatus("PENDING")
                .setResultNote("违规举报"));

        adminMockMvc.perform(get("/content/user/support/admin/report/detail")
                .param("reportId", "r1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.reportId").value("r1"))
            .andExpect(jsonPath("$.result.status").value("PENDING"));
    }
}
