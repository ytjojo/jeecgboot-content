package org.jeecg.modules.content.user.controller;

import org.jeecg.modules.content.user.service.IContentFanAnalyticsService;
import org.jeecg.modules.content.user.vo.ContentFanProfileVO;
import org.jeecg.modules.content.user.vo.ContentFanTrendVO;
import org.jeecg.modules.content.user.vo.ContentRelationUserPageVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ContentFanAnalyticsControllerWebMvcTest {

    private MockMvc mockMvc;

    @Mock
    private IContentFanAnalyticsService fanAnalyticsService;

    @InjectMocks
    private ContentFanAnalyticsController controller;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setValidator(validator)
            .build();
    }

    @Test
    void shouldListFans() throws Exception {
        ContentRelationUserPageVO page = new ContentRelationUserPageVO();
        when(fanAnalyticsService.listFans(eq("u1"), eq("test"), eq(1L), eq(10L)))
            .thenReturn(page);

        mockMvc.perform(get("/content/user/fan/list")
                .param("userId", "u1")
                .param("keyword", "test")
                .param("pageNo", "1")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(fanAnalyticsService).listFans("u1", "test", 1L, 10L);
    }

    @Test
    void shouldListFansWithDefaults() throws Exception {
        ContentRelationUserPageVO page = new ContentRelationUserPageVO();
        when(fanAnalyticsService.listFans(eq("u1"), eq(null), eq(1L), eq(10L)))
            .thenReturn(page);

        mockMvc.perform(get("/content/user/fan/list")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(fanAnalyticsService).listFans("u1", null, 1L, 10L);
    }

    @Test
    void shouldGetFanTrend() throws Exception {
        ContentFanTrendVO trend = new ContentFanTrendVO();
        when(fanAnalyticsService.getFanTrend(eq("u1"), eq("day"), any(), any()))
            .thenReturn(List.of(trend));

        mockMvc.perform(get("/content/user/fan/trend")
                .param("userId", "u1")
                .param("period", "day")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").isArray());
    }

    @Test
    void shouldGetFanProfile() throws Exception {
        ContentFanProfileVO profile = new ContentFanProfileVO();
        when(fanAnalyticsService.getFanProfile("u1")).thenReturn(profile);

        mockMvc.perform(get("/content/user/fan/profile")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(fanAnalyticsService).getFanProfile("u1");
    }

    @Test
    void shouldExportFans() throws Exception {
        mockMvc.perform(get("/content/user/fan/export")
                .param("userId", "u1"))
            .andExpect(status().isOk());

        verify(fanAnalyticsService).exportFans(eq("u1"), any());
    }
}
