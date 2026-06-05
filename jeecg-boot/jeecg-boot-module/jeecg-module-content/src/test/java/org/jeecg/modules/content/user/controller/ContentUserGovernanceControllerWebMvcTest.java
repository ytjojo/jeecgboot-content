package org.jeecg.modules.content.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.entity.ContentUserDeviceSession;
import org.jeecg.modules.content.user.req.governance.ContentUserStatusChangeReq;
import org.jeecg.modules.content.user.service.IContentUserGovernanceService;
import org.jeecg.modules.content.user.vo.ContentUserAuditLogItemVO;
import org.jeecg.modules.content.user.vo.ContentUserAuditLogPageVO;
import org.jeecg.modules.content.user.vo.ContentUserStatusHistoryItemVO;
import org.jeecg.modules.content.user.vo.ContentUserStatusHistoryPageVO;
import org.jeecg.modules.content.user.vo.ContentUserStatusVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ContentUserGovernanceControllerWebMvcTest {

    private final IContentUserGovernanceService governanceService = mock(IContentUserGovernanceService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        ContentUserGovernanceController controller = new ContentUserGovernanceController();
        ReflectionTestUtils.setField(controller, "governanceService", governanceService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).setValidator(validator).build();
    }

    private ContentUserGovernanceController newController() {
        ContentUserGovernanceController c = new ContentUserGovernanceController();
        ReflectionTestUtils.setField(c, "governanceService", governanceService);
        return c;
    }

    private MockMvc buildMockMvc() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return MockMvcBuilders.standaloneSetup(newController()).setValidator(validator).build();
    }

    @Test
    void shouldChangeStatus() throws Exception {
        ContentUserStatusChangeReq req = new ContentUserStatusChangeReq()
            .setUserId("u1")
            .setCurrentStatus("NORMAL")
            .setTargetStatus("MUTED")
            .setOperatorUserId("admin1")
            .setReason("违规发言");

        mockMvc.perform(post("/content/user/governance/status/change")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("状态变更成功"));

        ArgumentCaptor<ContentUserStatusChangeReq> captor = ArgumentCaptor.forClass(ContentUserStatusChangeReq.class);
        verify(governanceService).changeStatus(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo("u1");
        assertThat(captor.getValue().getTargetStatus()).isEqualTo("MUTED");
    }

    @Test
    void shouldRejectStatusChangeWithBlankUserId() throws Exception {
        ContentUserStatusChangeReq req = new ContentUserStatusChangeReq()
            .setUserId("")
            .setCurrentStatus("NORMAL")
            .setTargetStatus("MUTED")
            .setOperatorUserId("admin1");

        mockMvc.perform(post("/content/user/governance/status/change")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectStatusChangeWithInvalidStatusPattern() throws Exception {
        ContentUserStatusChangeReq req = new ContentUserStatusChangeReq()
            .setUserId("u1")
            .setCurrentStatus("INVALID_STATUS")
            .setTargetStatus("MUTED")
            .setOperatorUserId("admin1");

        mockMvc.perform(post("/content/user/governance/status/change")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCheckPermission() throws Exception {
        when(governanceService.canExecuteAction("u1", "POST_COMMENT")).thenReturn(true);

        mockMvc.perform(get("/content/user/governance/permission/check")
                .param("userId", "u1")
                .param("actionType", "POST_COMMENT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    void shouldCheckPermissionDenied() throws Exception {
        when(governanceService.canExecuteAction("u1", "POST_COMMENT")).thenReturn(false);

        mockMvc.perform(get("/content/user/governance/permission/check")
                .param("userId", "u1")
                .param("actionType", "POST_COMMENT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value(false));
    }

    @Test
    void shouldGetCurrentStatus() throws Exception {
        ContentUserStatusVO vo = new ContentUserStatusVO()
            .setUserId("u1")
            .setCurrentStatus("MUTED")
            .setTargetStatus("MUTED")
            .setReason("违规");
        when(governanceService.getCurrentStatus("u1")).thenReturn(vo);

        mockMvc.perform(get("/content/user/governance/status/current")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.currentStatus").value("MUTED"));
    }

    @Test
    void shouldListStatusHistory() throws Exception {
        ContentUserStatusHistoryItemVO item = new ContentUserStatusHistoryItemVO()
            .setRecordId("r1")
            .setUserId("u1")
            .setCurrentStatus("NORMAL")
            .setTargetStatus("MUTED")
            .setTriggerSource("MANUAL");
        ContentUserStatusHistoryPageVO page = new ContentUserStatusHistoryPageVO()
            .setRecords(List.of(item))
            .setTotal(1L)
            .setPageNo(1L)
            .setPageSize(10L);
        when(governanceService.listStatusHistory(eq("u1"), anyLong(), anyLong())).thenReturn(page);

        mockMvc.perform(get("/content/user/governance/status/history")
                .param("userId", "u1")
                .param("pageNo", "1")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.total").value(1))
            .andExpect(jsonPath("$.result.records[0].recordId").value("r1"));
    }

    @Test
    void shouldUseDefaultPaginationWhenNotProvided() throws Exception {
        ContentUserStatusHistoryPageVO page = new ContentUserStatusHistoryPageVO()
            .setRecords(List.of())
            .setTotal(0L)
            .setPageNo(1L)
            .setPageSize(10L);
        when(governanceService.listStatusHistory(eq("u1"), eq(1L), eq(10L))).thenReturn(page);

        mockMvc.perform(get("/content/user/governance/status/history")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.pageNo").value(1))
            .andExpect(jsonPath("$.result.pageSize").value(10));
    }

    @Test
    void shouldListDeviceSessions() throws Exception {
        ContentUserDeviceSession session = new ContentUserDeviceSession()
            .setUserId("u1")
            .setSessionToken("tok-123")
            .setDeviceId("dev-1");
        when(governanceService.listDeviceSessions("u1")).thenReturn(List.of(session));

        mockMvc.perform(get("/content/user/governance/device/sessions")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result[0].sessionToken").value("tok-123"));
    }

    @Test
    void shouldOfflineDeviceSession() throws Exception {
        mockMvc.perform(post("/content/user/governance/device/offline")
                .param("userId", "u1")
                .param("sessionId", "s1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("下线成功"));

        verify(governanceService).offlineDeviceSession("u1", "s1");
    }

    @Test
    void shouldDeleteComment() throws Exception {
        mockMvc.perform(post("/content/user/governance/moderator/comment/delete")
                .param("commentId", "c1")
                .param("reason", "违规"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("评论删除成功"));

        verify(governanceService).deleteComment(any(), eq("c1"), eq("违规"));
    }

    @Test
    void shouldWarnUser() throws Exception {
        mockMvc.perform(post("/content/user/governance/moderator/user/warn")
                .param("targetUserId", "u2")
                .param("reason", "首次警告"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("警告发送成功"));

        verify(governanceService).warnUser(any(), eq("u2"), eq("首次警告"));
    }

    @Test
    void shouldListAuditLog() throws Exception {
        ContentUserAuditLogItemVO item = new ContentUserAuditLogItemVO()
            .setId("log-1")
            .setUserId("u1")
            .setEventType("USER_STATUS_CHANGE")
            .setOperatorUserId("admin-1")
            .setEventContent("NORMAL -> MUTED")
            .setEventTime(new Date(1735689600000L));
        ContentUserAuditLogPageVO page = new ContentUserAuditLogPageVO()
            .setRecords(List.of(item))
            .setTotal(1L)
            .setPageNo(1L)
            .setPageSize(10L);
        when(governanceService.listAuditLog(any(), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/content/user/governance/audit-log")
                .param("pageNo", "1")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.total").value(1))
            .andExpect(jsonPath("$.result.records[0].id").value("log-1"))
            .andExpect(jsonPath("$.result.records[0].eventType").value("USER_STATUS_CHANGE"));
    }

    @Test
    void shouldListAuditLogWithDefaultPagination() throws Exception {
        ContentUserAuditLogPageVO page = new ContentUserAuditLogPageVO()
            .setRecords(List.of())
            .setTotal(0L)
            .setPageNo(1L)
            .setPageSize(10L);
        when(governanceService.listAuditLog(any(), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/content/user/governance/audit-log"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.pageNo").value(1))
            .andExpect(jsonPath("$.result.pageSize").value(10));
    }
}
