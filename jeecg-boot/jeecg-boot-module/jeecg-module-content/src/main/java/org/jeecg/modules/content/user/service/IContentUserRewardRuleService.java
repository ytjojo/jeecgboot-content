package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.user.entity.ContentUserRewardRule;

import java.util.Optional;

/**
 * 内容社区用户奖励规则服务契约。
 */
public interface IContentUserRewardRuleService extends IService<ContentUserRewardRule> {

    /**
     * 按行为来源加载唯一启用且合法的奖励规则。
     */
    Optional<ContentUserRewardRule> getEnabledRule(String sourceType);
}
