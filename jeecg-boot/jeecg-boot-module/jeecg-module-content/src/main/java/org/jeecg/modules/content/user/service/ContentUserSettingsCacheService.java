package org.jeecg.modules.content.user.service;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 用户隐私和通知设置的 Redis 缓存服务。
 * 所有缓存 key 统一使用 content:privacy: / content:notification: 前缀，TTL 300 秒。
 */
@Service
public class ContentUserSettingsCacheService {

    private static final String PRIVACY_KEY_PREFIX = "content:privacy:";
    private static final String NOTIFICATION_KEY_PREFIX = "content:notification:";
    private static final long TTL_SECONDS = 300;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 读取用户隐私设置的缓存值。
     *
     * @param userId 用户ID
     * @return 缓存的 JSON 字符串，不存在时返回 null
     */
    public String getPrivacySetting(String userId) {
        return stringRedisTemplate.opsForValue().get(PRIVACY_KEY_PREFIX + userId);
    }

    /**
     * 缓存用户隐私设置。
     *
     * @param userId 用户ID
     * @param json   隐私设置 JSON
     */
    public void cachePrivacySetting(String userId, String json) {
        stringRedisTemplate.opsForValue().set(PRIVACY_KEY_PREFIX + userId, json, TTL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 驱逐用户隐私设置缓存。
     *
     * @param userId 用户ID
     */
    public void evictPrivacy(String userId) {
        stringRedisTemplate.delete(PRIVACY_KEY_PREFIX + userId);
    }

    /**
     * 读取用户通知设置的缓存值。
     *
     * @param userId 用户ID
     * @return 缓存的 JSON 字符串，不存在时返回 null
     */
    public String getNotificationSetting(String userId) {
        return stringRedisTemplate.opsForValue().get(NOTIFICATION_KEY_PREFIX + userId);
    }

    /**
     * 缓存用户通知设置。
     *
     * @param userId 用户ID
     * @param json   通知设置 JSON
     */
    public void cacheNotificationSetting(String userId, String json) {
        stringRedisTemplate.opsForValue().set(NOTIFICATION_KEY_PREFIX + userId, json, TTL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 驱逐用户通知设置缓存。
     *
     * @param userId 用户ID
     */
    public void evictNotification(String userId) {
        stringRedisTemplate.delete(NOTIFICATION_KEY_PREFIX + userId);
    }
}
