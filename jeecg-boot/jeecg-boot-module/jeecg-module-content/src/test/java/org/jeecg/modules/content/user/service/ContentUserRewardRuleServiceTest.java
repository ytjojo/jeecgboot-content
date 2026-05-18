package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserRewardRule;
import org.jeecg.modules.content.user.mapper.ContentUserRewardRuleMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserRewardRuleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 内容社区奖励规则服务测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentUserRewardRuleServiceTest {

    @Mock
    private ContentUserRewardRuleMapper rewardRuleMapper;

    @InjectMocks
    private ContentUserRewardRuleServiceImpl rewardRuleService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(rewardRuleService, "baseMapper", rewardRuleMapper);
    }

    @Test
    void shouldLoadSingleEnabledValidRule() {
        when(rewardRuleMapper.selectList(any(Wrapper.class))).thenReturn(List.of(rule("R_LIKE", 1, 1, true)));

        Optional<ContentUserRewardRule> result = rewardRuleService.getEnabledRule("LIKE");

        assertThat(result).isPresent();
        assertThat(result.get().getRuleCode()).isEqualTo("R_LIKE");
    }

    @Test
    void shouldIgnoreBlankCodeIllegalAmountAndDisabledRule() {
        when(rewardRuleMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
            rule("", 1, 1, true),
            rule("R_BAD_POINT", -1, 1, true),
            rule("R_BAD_GROWTH", 1, -1, true),
            rule("R_DISABLED", 1, 1, false)
        ));

        Optional<ContentUserRewardRule> result = rewardRuleService.getEnabledRule("LIKE");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldRejectDuplicateEnabledValidRules() {
        when(rewardRuleMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
            rule("R_LIKE_A", 1, 1, true),
            rule("R_LIKE_B", 1, 1, true)
        ));

        assertThatThrownBy(() -> rewardRuleService.getEnabledRule("LIKE"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("奖励规则配置重复");
    }

    private ContentUserRewardRule rule(String ruleCode, int pointAmount, int growthAmount, boolean enabled) {
        return new ContentUserRewardRule()
            .setRuleCode(ruleCode)
            .setSourceType("LIKE")
            .setPointAmount(pointAmount)
            .setGrowthAmount(growthAmount)
            .setDailyPointCap(10)
            .setDailyGrowthCap(10)
            .setEnabled(enabled);
    }
}
