package org.jeecg.modules.content.user.service;

/**
 * 内容社区社交订阅默认配置服务契约。
 */
public interface IContentSocialSubscriptionDefaultsService {

    void ensureDefaults(String userId);
}
