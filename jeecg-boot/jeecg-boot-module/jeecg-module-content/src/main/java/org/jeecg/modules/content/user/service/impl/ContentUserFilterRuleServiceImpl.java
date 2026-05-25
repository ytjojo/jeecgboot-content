package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserFilterRule;
import org.jeecg.modules.content.user.mapper.ContentUserFilterRuleMapper;
import org.jeecg.modules.content.user.service.IContentUserFilterRuleService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 内容社区用户屏蔽规则服务实现。
 */
@Service
public class ContentUserFilterRuleServiceImpl
    extends ServiceImpl<ContentUserFilterRuleMapper, ContentUserFilterRule>
    implements IContentUserFilterRuleService {

    private static final int KEYWORD_MAX_LENGTH = 128;
    private static final int TOPIC_MAX_LENGTH = 128;
    private static final int CONTENT_TYPE_MAX_LENGTH = 64;
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String CANCELLED_STATUS = "CANCELLED";

    @Resource
    private ContentUserFilterRuleMapper filterRuleMapper;

    @Override
    public void saveContentTypeRule(String userId, String contentType) {
        requireValidUserId(userId);
        requireValidContentType(contentType);

        ContentUserFilterRule rule = new ContentUserFilterRule()
            .setUserId(userId)
            .setRuleType("CONTENT_TYPE")
            .setRuleValue(contentType)
            .setNormalizedValue(contentType.trim().toUpperCase())
            .setStatus(ACTIVE_STATUS);
        filterRuleMapper.insert(rule);
    }

    @Override
    public void saveTopicRule(String userId, String topic) {
        saveTopicRuleWithExpiry(userId, topic, 0);
    }

    @Override
    public void saveTopicRuleWithExpiry(String userId, String topic, int daysValid) {
        requireValidUserId(userId);
        requireValidTopic(topic);

        ContentUserFilterRule rule = new ContentUserFilterRule()
            .setUserId(userId)
            .setRuleType("TOPIC")
            .setRuleValue(topic)
            .setNormalizedValue(topic.trim())
            .setStatus(ACTIVE_STATUS);
        if (daysValid > 0) {
            rule.setExpiresAt(new Date(System.currentTimeMillis() + (long) daysValid * 24 * 60 * 60 * 1000));
        }
        filterRuleMapper.insert(rule);
    }

    @Override
    public void saveKeywordRule(String userId, String keyword) {
        requireValidUserId(userId);
        requireValidKeyword(keyword);

        ContentUserFilterRule rule = new ContentUserFilterRule()
            .setUserId(userId)
            .setRuleType("WORD")
            .setRuleValue(keyword)
            .setNormalizedValue(keyword.trim().toLowerCase())
            .setStatus(ACTIVE_STATUS);
        filterRuleMapper.insert(rule);
    }

    @Override
    public void saveRegexRule(String userId, String regex) {
        requireValidUserId(userId);
        requireValidRegex(regex);

        ContentUserFilterRule rule = new ContentUserFilterRule()
            .setUserId(userId)
            .setRuleType("REGEX")
            .setRuleValue(regex)
            .setStatus(ACTIVE_STATUS);
        filterRuleMapper.insert(rule);
    }

    @Override
    public void cancelRule(String userId, String ruleId) {
        ContentUserFilterRule rule = filterRuleMapper.selectById(ruleId);
        if (rule == null) {
            throw new JeecgBootException("规则不存在");
        }
        if (!userId.equals(rule.getUserId())) {
            throw new JeecgBootException("无权操作该规则");
        }
        rule.setStatus(CANCELLED_STATUS);
        filterRuleMapper.updateById(rule);
    }

    @Override
    public void batchCancelRules(String userId, List<String> ruleIds) {
        for (String ruleId : ruleIds) {
            cancelRule(userId, ruleId);
        }
    }

    private void requireValidUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new JeecgBootException("用户ID不能为空");
        }
    }

    private void requireValidContentType(String contentType) {
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new JeecgBootException("内容类型不能为空");
        }
        if (contentType.length() > CONTENT_TYPE_MAX_LENGTH) {
            throw new JeecgBootException("内容类型长度不能超过64位");
        }
    }

    private void requireValidTopic(String topic) {
        if (topic == null || topic.trim().isEmpty()) {
            throw new JeecgBootException("话题不能为空");
        }
        if (topic.length() > TOPIC_MAX_LENGTH) {
            throw new JeecgBootException("话题长度不能超过128位");
        }
    }

    private void requireValidKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new JeecgBootException("屏蔽词不能为空");
        }
        if (keyword.length() > KEYWORD_MAX_LENGTH) {
            throw new JeecgBootException("屏蔽词长度不能超过128位");
        }
    }

    private void requireValidRegex(String regex) {
        if (regex == null || regex.trim().isEmpty()) {
            throw new JeecgBootException("正则表达式不能为空");
        }
        if (regex.length() > KEYWORD_MAX_LENGTH) {
            throw new JeecgBootException("正则表达式长度不能超过128位");
        }
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new JeecgBootException("正则表达式格式不正确: " + e.getDescription());
        }
    }
}
