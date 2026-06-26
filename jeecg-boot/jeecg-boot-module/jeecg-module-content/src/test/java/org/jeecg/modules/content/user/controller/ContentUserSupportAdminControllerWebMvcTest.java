package org.jeecg.modules.content.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeecg.modules.content.user.req.support.ContentAppealHandleReq;
import org.jeecg.modules.content.user.req.support.ContentReportHandleReq;
import org.jeecg.modules.content.user.req.support.ContentUserReportAdminQueryReq;
import org.jeecg.modules.content.user.service.IContentUserSupportService;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminDetailVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminPageVO;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ContentUserSupportAdminControllerWebMvcTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private IContentUserSupportService supportService;

    @InjectMocks
    private ContentUserSupportAdminController controller;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setValidator(validator)
            .build();
    }

    @Test
    void shouldHandleAppeal() throws Exception {
        ContentAppealHandleReq req = new ContentAppealHandleReq()
            .setAppealId("a1")
            .setOperatorUserId("admin1")
            .setStatus("RESOLVED")
            .setResultStatus("APPROVED")
            .setResultNote("申诉通过")
            .setProgressNote("已处理");

        mockMvc.perform(post("/api/v1/content/user/support/admin/appeal/handle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(supportService).handleAppeal(any(ContentAppealHandleReq.class));
    }

    @Test
    void shouldRejectAppealWithBlankFields() throws Exception {
        ContentAppealHandleReq req = new ContentAppealHandleReq();

        mockMvc.perform(post("/api/v1/content/user/support/admin/appeal/handle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleReport() throws Exception {
        ContentReportHandleReq req = new ContentReportHandleReq()
            .setReportId("r1")
            .setOperatorUserId("admin1")
            .setStatus("RESOLVED")
            .setResultStatus("CONFIRMED")
            .setResultNote("举报属实")
            .setProgressNote("已处理");

        mockMvc.perform(post("/api/v1/content/user/support/admin/report/handle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(supportService).handleReport(any(ContentReportHandleReq.class));
    }

    @Test
    void shouldRejectReportWithBlankFields() throws Exception {
        ContentReportHandleReq req = new ContentReportHandleReq();

        mockMvc.perform(post("/api/v1/content/user/support/admin/report/handle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldListReportsForAdmin() throws Exception {
        ContentUserReportAdminPageVO page = new ContentUserReportAdminPageVO();
        when(supportService.listReportsForAdmin(any(ContentUserReportAdminQueryReq.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/content/user/support/admin/report/list"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(supportService).listReportsForAdmin(any(ContentUserReportAdminQueryReq.class));
    }

    @Test
    void shouldGetReportDetail() throws Exception {
        ContentUserReportAdminDetailVO detail = new ContentUserReportAdminDetailVO();
        when(supportService.getReportDetailForAdmin("r1")).thenReturn(detail);

        mockMvc.perform(get("/api/v1/content/user/support/admin/report/detail")
                .param("reportId", "r1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(supportService).getReportDetailForAdmin("r1");
    }
}
