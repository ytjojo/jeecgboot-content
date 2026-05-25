package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.user.entity.ContentUserFilterRule;

import java.util.List;

/**
 * 内容社区用户屏蔽规则服务契约。
 */
public interface IContentUserFilterRuleService extends IService<ContentUserFilterRule> {

    /**
     * 保存内容类型屏蔽规则。
     */
    void saveContentTypeRule(String userId, String contentType);

    /**
     * 保存话题屏蔽规则（永久）。
     */
    void saveTopicRule(String userId, String topic);

    /**
     * 保存话题屏蔽规则（带过期时间）。
     */
    void saveTopicRuleWithExpiry(String userId, String topic, int daysValid);

    /**
     * 保存关键词屏蔽规则。
     */
    void saveKeywordRule(String userId, String keyword);

    /**
     * 保存正则表达式屏蔽规则。
     */
    void saveRegexRule(String userId, String regex);

    /**
     * 取消单条规则。
     */
    void cancelRule(String userId, String ruleId);

    /**
     * 批量取消规则。
     */
    void batchCancelRules(String userId, List<String> ruleIds);
}
