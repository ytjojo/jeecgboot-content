package org.jeecg.modules.content.user.controller;

import org.jeecg.modules.content.user.service.IContentUserSupportService;
import org.jeecg.modules.content.user.vo.ContentCustomerServiceVO;
import org.jeecg.modules.content.user.vo.ContentHelpCenterVO;
import org.jeecg.modules.content.user.vo.ContentUserAppealProgressVO;
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

import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ContentUserSupportControllerWebMvcTest {

    private MockMvc mockMvc;

    @Mock
    private IContentUserSupportService supportService;

    @InjectMocks
    private ContentUserSupportController supportController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(supportController)
            .setValidator(validator)
            .build();
    }

    @Test
    void shouldRejectInvalidReportRequest() throws Exception {
        mockMvc.perform(post("/content/user/support/report/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"u1\",\"targetType\":\"\",\"targetId\":\"post-1\",\"reportType\":\"SPAM\",\"reason\":\"垃圾内容\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnAppealProgress() throws Exception {
        Date resolvedAt = new Date(1735689600000L);
        when(supportService.getAppealProgress("u1", "appeal-1"))
            .thenReturn(new ContentUserAppealProgressVO()
                .setAppealId("appeal-1")
                .setStatus("RESOLVED")
                .setProgressNote("客服已受理")
                .setResultStatus("APPROVED")
                .setResultNote("处罚已撤销")
                .setResolvedBy("admin-1")
                .setResolvedAt(resolvedAt));

        mockMvc.perform(get("/content/user/support/appeal/progress")
                .param("userId", "u1")
                .param("appealId", "appeal-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.status").value("RESOLVED"))
            .andExpect(jsonPath("$.result.resultStatus").value("APPROVED"))
            .andExpect(jsonPath("$.result.resultNote").value("处罚已撤销"))
            .andExpect(jsonPath("$.result.resolvedBy").value("admin-1"))
            .andExpect(jsonPath("$.result.resolvedAt").exists());
    }

    @Test
    void shouldReturnAppealList() throws Exception {
        Date resolvedAt = new Date(1735689600000L);
        when(supportService.listAppeals("u1"))
            .thenReturn(List.of(new ContentUserAppealProgressVO()
                .setAppealId("appeal-1")
                .setStatus("PENDING")
                .setProgressNote("等待处理")
                .setResolvedAt(resolvedAt)));

        mockMvc.perform(get("/content/user/support/appeal/list")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result[0].appealId").value("appeal-1"))
            .andExpect(jsonPath("$.result[0].resolvedAt").exists());
    }

    @Test
    void shouldReturnReportProgress() throws Exception {
        Date resolvedAt = new Date(1735689600000L);
        when(supportService.getReportProgress("u1", "report-1"))
            .thenReturn(new ContentUserReportProgressVO()
                .setReportId("report-1")
                .setStatus("RESOLVED")
                .setProgressNote("已处理完成")
                .setResultStatus("CONFIRMED")
                .setResultNote("违规成立")
                .setResolvedBy("admin-1")
                .setResolvedAt(resolvedAt));

        mockMvc.perform(get("/content/user/support/report/progress")
                .param("userId", "u1")
                .param("reportId", "report-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.reportId").value("report-1"))
            .andExpect(jsonPath("$.result.status").value("RESOLVED"))
            .andExpect(jsonPath("$.result.resultStatus").value("CONFIRMED"))
            .andExpect(jsonPath("$.result.resolvedAt").exists());
    }

    @Test
    void shouldReturnHelpCenterMetadata() throws Exception {
        when(supportService.getHelpCenter())
            .thenReturn(new ContentHelpCenterVO()
                .setFaqCategories(List.of("账号安全"))
                .setGuideEntries(List.of("新手指南"))
                .setReleaseNotes(List.of("产品更新")));

        mockMvc.perform(get("/content/user/support/help-center"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.faqCategories[0]").value("账号安全"));
    }

    @Test
    void shouldReturnCustomerServiceEntry() throws Exception {
        when(supportService.getCustomerServiceEntry("u1"))
            .thenReturn(new ContentCustomerServiceVO()
                .setRouteType("SMART_FIRST")
                .setTitle("在线客服")
                .setDescription("优先进入智能客服")
                .setManualSupported(true));

        mockMvc.perform(get("/content/user/support/customer-service")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.routeType").value("SMART_FIRST"));
    }
}
