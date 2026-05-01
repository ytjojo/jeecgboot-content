package org.jeecg.modules.content.user.controller;

import org.jeecg.modules.content.user.service.IContentUserSupportService;
import org.jeecg.modules.content.user.vo.ContentCustomerServiceVO;
import org.jeecg.modules.content.user.vo.ContentHelpCenterEntryVO;
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

import static org.hamcrest.Matchers.nullValue;
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
    void shouldRejectHelpCenterRequestWithoutUserId() throws Exception {
        mockMvc.perform(get("/content/user/support/help-center"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnStructuredHelpCenterMetadata() throws Exception {
        when(supportService.getHelpCenter("u1"))
            .thenReturn(new ContentHelpCenterVO()
                .setFaqCategories(List.of(new ContentHelpCenterEntryVO()
                    .setCode("ACCOUNT_SECURITY")
                    .setTitle("账号安全")
                    .setDescription("账号登录、密码与设备安全相关问题")
                    .setRecommendedRouteType("SMART_FIRST")
                    .setRecommendedRouteTitle("在线客服")
                    .setManualSupported(Boolean.TRUE)))
                .setGuideEntries(List.of(new ContentHelpCenterEntryVO()
                    .setCode("BEGINNER_GUIDE")
                    .setTitle("新手指南")
                    .setDescription("帮助新用户快速了解社区基础功能")
                    .setRecommendedRouteType("SMART_FIRST")
                    .setRecommendedRouteTitle("在线客服")
                    .setManualSupported(Boolean.TRUE)))
                .setReleaseNotes(List.of(new ContentHelpCenterEntryVO()
                    .setCode("PRODUCT_UPDATE")
                    .setTitle("产品更新")
                    .setDescription("版本更新与功能发布日志")
                    .setRecommendedRouteType(null)
                    .setRecommendedRouteTitle(null)
                    .setManualSupported(null))));

        mockMvc.perform(get("/content/user/support/help-center")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.faqCategories[0].code").value("ACCOUNT_SECURITY"))
            .andExpect(jsonPath("$.result.faqCategories[0].recommendedRouteType").value("SMART_FIRST"))
            .andExpect(jsonPath("$.result.faqCategories[0].recommendedRouteTitle").value("在线客服"))
            .andExpect(jsonPath("$.result.faqCategories[0].manualSupported").value(true))
            .andExpect(jsonPath("$.result.releaseNotes[0].recommendedRouteType").value(nullValue()))
            .andExpect(jsonPath("$.result.releaseNotes[0].recommendedRouteTitle").value(nullValue()))
            .andExpect(jsonPath("$.result.releaseNotes[0].manualSupported").value(nullValue()));
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

    @Test
    void shouldReturnManualPriorityCustomerServiceEntry() throws Exception {
        when(supportService.getCustomerServiceEntry("u100"))
            .thenReturn(new ContentCustomerServiceVO()
                .setRouteType("MANUAL_PRIORITY")
                .setTitle("专属客服")
                .setDescription("高等级用户优先进入人工客服通道")
                .setManualSupported(true));

        mockMvc.perform(get("/content/user/support/customer-service")
                .param("userId", "u100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.routeType").value("MANUAL_PRIORITY"))
            .andExpect(jsonPath("$.result.title").value("专属客服"))
            .andExpect(jsonPath("$.result.manualSupported").value(true));
    }
}
