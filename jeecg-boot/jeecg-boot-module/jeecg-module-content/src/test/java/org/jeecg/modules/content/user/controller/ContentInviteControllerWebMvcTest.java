package org.jeecg.modules.content.user.controller;

import org.jeecg.modules.content.user.service.IContentInviteService;
import org.jeecg.modules.content.user.vo.ContentInviteCodeVO;
import org.jeecg.modules.content.user.vo.ContentInviteRecordPageVO;
import org.jeecg.modules.content.user.vo.ContentInviteStatsVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ContentInviteControllerWebMvcTest {

    private MockMvc mockMvc;

    @Mock
    private IContentInviteService inviteService;

    @InjectMocks
    private ContentInviteController controller;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setValidator(validator)
            .build();
    }

    @Test
    void shouldGenerateInviteCode() throws Exception {
        ContentInviteCodeVO vo = new ContentInviteCodeVO();
        when(inviteService.generateOrGetInviteCode("u1")).thenReturn(vo);

        mockMvc.perform(post("/content/user/invite/generate")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(inviteService).generateOrGetInviteCode("u1");
    }

    @Test
    void shouldBindInviteRelation() throws Exception {
        mockMvc.perform(post("/content/user/invite/bind")
                .param("inviteCode", "ABC123")
                .param("inviteeUserId", "u2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("绑定成功"));

        verify(inviteService).bindInviteRelation("ABC123", "u2");
    }

    @Test
    void shouldListInviteRecords() throws Exception {
        ContentInviteRecordPageVO page = new ContentInviteRecordPageVO();
        when(inviteService.listInviteRecords(eq("u1"), eq(1L), eq(10L))).thenReturn(page);

        mockMvc.perform(get("/content/user/invite/records")
                .param("userId", "u1")
                .param("pageNo", "1")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(inviteService).listInviteRecords("u1", 1L, 10L);
    }

    @Test
    void shouldListInviteRecordsWithDefaults() throws Exception {
        ContentInviteRecordPageVO page = new ContentInviteRecordPageVO();
        when(inviteService.listInviteRecords(eq("u1"), eq(1L), eq(10L))).thenReturn(page);

        mockMvc.perform(get("/content/user/invite/records")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldGetInviteStats() throws Exception {
        ContentInviteStatsVO stats = new ContentInviteStatsVO();
        when(inviteService.getInviteStats("u1")).thenReturn(stats);

        mockMvc.perform(get("/content/user/invite/stats")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(inviteService).getInviteStats("u1");
    }
}
