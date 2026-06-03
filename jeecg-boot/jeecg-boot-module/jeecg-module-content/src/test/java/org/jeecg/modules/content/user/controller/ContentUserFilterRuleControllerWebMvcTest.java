package org.jeecg.modules.content.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.content.user.entity.ContentUserFilterRule;
import org.jeecg.modules.content.user.mapper.ContentUserFilterRuleMapper;
import org.jeecg.modules.content.user.service.IContentUserFilterRuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 内容社区用户屏蔽规则 Controller WebMvc 测试。
 */
class ContentUserFilterRuleControllerWebMvcTest {

    private IContentUserFilterRuleService filterRuleService;
    private ContentUserFilterRuleMapper filterRuleMapper;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ContentUserFilterRuleController controller = new ContentUserFilterRuleController();
        filterRuleService = mock(IContentUserFilterRuleService.class);
        filterRuleMapper = mock(ContentUserFilterRuleMapper.class);
        ReflectionTestUtils.setField(controller, "filterRuleService", filterRuleService);
        ReflectionTestUtils.setField(controller, "filterRuleMapper", filterRuleMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldAddKeywordRule() throws Exception {
        mockMvc.perform(post("/content/user/filter-rule")
                .param("userId", "u1")
                .param("ruleType", "KEYWORD")
                .param("value", "广告"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("规则添加成功"));

        verify(filterRuleService).saveKeywordRule("u1", "广告");
    }

    @Test
    void shouldAddRegexRule() throws Exception {
        mockMvc.perform(post("/content/user/filter-rule")
                .param("userId", "u1")
                .param("ruleType", "REGEX")
                .param("value", "\\d+"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("规则添加成功"));

        verify(filterRuleService).saveRegexRule("u1", "\\d+");
    }

    @Test
    void shouldAddTopicRule() throws Exception {
        mockMvc.perform(post("/content/user/filter-rule")
                .param("userId", "u1")
                .param("ruleType", "TOPIC")
                .param("value", "科技"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("规则添加成功"));

        verify(filterRuleService).saveTopicRule("u1", "科技");
    }

    @Test
    void shouldAddTopicRuleWithExpiry() throws Exception {
        mockMvc.perform(post("/content/user/filter-rule")
                .param("userId", "u1")
                .param("ruleType", "TOPIC")
                .param("value", "科技")
                .param("daysValid", "7"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("规则添加成功"));

        verify(filterRuleService).saveTopicRuleWithExpiry("u1", "科技", 7);
    }

    @Test
    void shouldAddContentTypeRule() throws Exception {
        mockMvc.perform(post("/content/user/filter-rule")
                .param("userId", "u1")
                .param("ruleType", "CONTENT_TYPE")
                .param("value", "VIDEO"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("规则添加成功"));

        verify(filterRuleService).saveContentTypeRule("u1", "VIDEO");
    }

    @Test
    void shouldDeleteRule() throws Exception {
        mockMvc.perform(post("/content/user/filter-rule/delete")
                .param("userId", "u1")
                .param("ruleId", "r1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("规则删除成功"));

        verify(filterRuleService).cancelRule("u1", "r1");
    }

    @Test
    void shouldBatchDeleteRules() throws Exception {
        mockMvc.perform(post("/content/user/filter-rule/batch-delete")
                .param("userId", "u1")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("[\"r1\",\"r2\",\"r3\"]"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("批量删除成功"));

        verify(filterRuleService).batchCancelRules(eq("u1"), any());
    }

    @Test
    void shouldListRules() throws Exception {
        ContentUserFilterRule rule = new ContentUserFilterRule();
        rule.setId("r1");
        rule.setUserId("u1")
            .setRuleType("KEYWORD")
            .setRuleValue("广告")
            .setStatus("ACTIVE")
            .setCreateTime(new Date());
        when(filterRuleMapper.selectPage(any(IPage.class), any(LambdaQueryWrapper.class)))
            .thenAnswer(invocation -> {
                IPage<ContentUserFilterRule> p = invocation.getArgument(0);
                p.setRecords(Collections.singletonList(rule));
                p.setTotal(1);
                return p;
            });

        mockMvc.perform(get("/content/user/filter-rule/list")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.total").value(1))
            .andExpect(jsonPath("$.result.records[0].id").value("r1"))
            .andExpect(jsonPath("$.result.records[0].ruleType").value("KEYWORD"));
    }

    @Test
    void shouldListRulesWithRuleTypeFilter() throws Exception {
        when(filterRuleMapper.selectPage(any(IPage.class), any(LambdaQueryWrapper.class)))
            .thenAnswer(invocation -> {
                IPage<ContentUserFilterRule> p = invocation.getArgument(0);
                p.setRecords(Collections.emptyList());
                p.setTotal(0);
                return p;
            });

        mockMvc.perform(get("/content/user/filter-rule/list")
                .param("userId", "u1")
                .param("ruleType", "KEYWORD"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.total").value(0));
    }
}
