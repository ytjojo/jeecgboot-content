package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserRewardRule;
import org.jeecg.modules.content.user.mapper.ContentUserRewardRuleMapper;
import org.jeecg.modules.content.user.service.IContentUserRewardRuleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 内容社区用户奖励规则服务实现。
 */
@Service
public class ContentUserRewardRuleServiceImpl
    extends ServiceImpl<ContentUserRewardRuleMapper, ContentUserRewardRule>
    implements IContentUserRewardRuleService {

    /**
     * 按行为来源加载启用规则，并拒绝会破坏奖励计算的配置。
     */
    @Override
    public Optional<ContentUserRewardRule> getEnabledRule(String sourceType) {
        if (sourceType == null || sourceType.isBlank()) {
            return Optional.empty();
        }
        List<ContentUserRewardRule> rules = baseMapper.selectList(
            Wrappers.<ContentUserRewardRule>lambdaQuery()
                .eq(ContentUserRewardRule::getSourceType, sourceType)
                .eq(ContentUserRewardRule::getEnabled, Boolean.TRUE)
        );
        List<ContentUserRewardRule> validRules = rules.stream()
            .filter(this::isValidAwardRule)
            .toList();
        if (validRules.size() > 1) {
            throw new JeecgBootException("奖励规则配置重复");
        }
        return validRules.stream().findFirst();
    }

    private boolean isValidAwardRule(ContentUserRewardRule rule) {
        return rule != null
            && rule.getRuleCode() != null
            && !rule.getRuleCode().isBlank()
            && Boolean.TRUE.equals(rule.getEnabled())
            && defaultZero(rule.getPointAmount()) >= 0
            && defaultZero(rule.getGrowthAmount()) >= 0
            && (rule.getDailyPointCap() == null || rule.getDailyPointCap() >= 0)
            && (rule.getDailyGrowthCap() == null || rule.getDailyGrowthCap() >= 0);
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }
}
