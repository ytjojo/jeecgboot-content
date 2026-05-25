package org.jeecg.modules.content.user.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserFilterRule;
import org.jeecg.modules.content.user.mapper.ContentUserFilterRuleMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserFilterRuleServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 屏蔽规则服务测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentUserFilterRuleServiceTest {

    @Mock
    private ContentUserFilterRuleMapper filterRuleMapper;

    @InjectMocks
    private ContentUserFilterRuleServiceImpl filterRuleService;

    @Test
    void shouldSaveContentTypeFilterRule() {
        filterRuleService.saveContentTypeRule("u1", "ARTICLE");

        verify(filterRuleMapper).insert(any(ContentUserFilterRule.class));
    }

    @Test
    void shouldRejectNullContentTypeForFilterRule() {
        assertThatThrownBy(() -> filterRuleService.saveContentTypeRule("u1", null))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("内容类型不能为空");
    }

    @Test
    void shouldRejectEmptyContentTypeForFilterRule() {
        assertThatThrownBy(() -> filterRuleService.saveContentTypeRule("u1", "  "))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("内容类型不能为空");
    }

    @Test
    void shouldRejectOverLengthContentTypeForFilterRule() {
        assertThatThrownBy(() -> filterRuleService.saveContentTypeRule("u1", "a".repeat(65)))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("内容类型长度不能超过64位");
    }

    @Test
    void shouldSaveTopicFilterRule() {
        filterRuleService.saveTopicRule("u1", "科技");

        verify(filterRuleMapper).insert(any(ContentUserFilterRule.class));
    }

    @Test
    void shouldSaveTopicFilterRuleWithExpiration() {
        filterRuleService.saveTopicRuleWithExpiry("u1", "科技", 7);

        verify(filterRuleMapper).insert(any(ContentUserFilterRule.class));
    }

    @Test
    void shouldRejectNullTopicForFilterRule() {
        assertThatThrownBy(() -> filterRuleService.saveTopicRule("u1", null))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("话题不能为空");
    }

    @Test
    void shouldRejectEmptyTopicForFilterRule() {
        assertThatThrownBy(() -> filterRuleService.saveTopicRule("u1", "  "))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("话题不能为空");
    }

    @Test
    void shouldRejectOverLengthTopicForFilterRule() {
        assertThatThrownBy(() -> filterRuleService.saveTopicRule("u1", "a".repeat(129)))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("话题长度不能超过128位");
    }

    @Test
    void shouldSaveKeywordFilterRule() {
        filterRuleService.saveKeywordRule("u1", "敏感词");

        verify(filterRuleMapper).insert(any(ContentUserFilterRule.class));
    }

    @Test
    void shouldRejectNullKeywordForFilterRule() {
        assertThatThrownBy(() -> filterRuleService.saveKeywordRule("u1", null))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("屏蔽词不能为空");
    }

    @Test
    void shouldRejectOverLengthKeywordForFilterRule() {
        assertThatThrownBy(() -> filterRuleService.saveKeywordRule("u1", "a".repeat(129)))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("屏蔽词长度不能超过128位");
    }

    @Test
    void shouldSaveRegexFilterRule() {
        filterRuleService.saveRegexRule("u1", "\\d+");

        verify(filterRuleMapper).insert(any(ContentUserFilterRule.class));
    }

    @Test
    void shouldRejectInvalidRegexFilterRule() {
        assertThatThrownBy(() -> filterRuleService.saveRegexRule("u1", "["))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("正则表达式");
    }

    @Test
    void shouldCancelSingleFilterRule() {
        ContentUserFilterRule rule = createFilterRule("rule1", "u1");
        when(filterRuleMapper.selectById("rule1")).thenReturn(rule);

        filterRuleService.cancelRule("u1", "rule1");

        assertThat(rule.getStatus()).isEqualTo("CANCELLED");
        verify(filterRuleMapper).updateById(rule);
    }

    @Test
    void shouldRejectCancelRuleNotOwnedByUser() {
        ContentUserFilterRule rule = createFilterRule("rule1", "u2");
        when(filterRuleMapper.selectById("rule1")).thenReturn(rule);

        assertThatThrownBy(() -> filterRuleService.cancelRule("u1", "rule1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("无权操作该规则");
    }

    @Test
    void shouldBatchCancelFilterRules() {
        ContentUserFilterRule rule1 = createFilterRule("r1", "u1");
        ContentUserFilterRule rule2 = createFilterRule("r2", "u1");
        when(filterRuleMapper.selectById("r1")).thenReturn(rule1);
        when(filterRuleMapper.selectById("r2")).thenReturn(rule2);

        filterRuleService.batchCancelRules("u1", java.util.List.of("r1", "r2"));

        assertThat(rule1.getStatus()).isEqualTo("CANCELLED");
        assertThat(rule2.getStatus()).isEqualTo("CANCELLED");
        verify(filterRuleMapper, times(2)).updateById(any(ContentUserFilterRule.class));
    }

    @Test
    void shouldRejectBatchCancelWithIdsNotOwnedByUser() {
        ContentUserFilterRule rule = createFilterRule("r1", "u2");
        when(filterRuleMapper.selectById("r1")).thenReturn(rule);

        assertThatThrownBy(() -> filterRuleService.batchCancelRules("u1", java.util.List.of("r1")))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("无权操作该规则");
    }

    private ContentUserFilterRule createFilterRule(String id, String userId) {
        ContentUserFilterRule rule = new ContentUserFilterRule();
        rule.setId(id);
        rule.setUserId(userId);
        rule.setStatus("ACTIVE");
        return rule;
    }
}
