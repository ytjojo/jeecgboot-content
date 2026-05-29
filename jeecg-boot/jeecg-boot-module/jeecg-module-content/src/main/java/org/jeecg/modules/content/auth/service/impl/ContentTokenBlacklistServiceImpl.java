package org.jeecg.modules.content.auth.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.auth.constant.AuthRedisKeyConstant;
import org.jeecg.modules.content.auth.service.IContentDeviceSessionService;
import org.jeecg.modules.content.auth.service.IContentTokenBlacklistService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Token 黑名单服务实现。
 * 使用 Redis 存储已吊销的 token JTI，防止已下线 token 继续访问。
 */
@Slf4j
@Service
public class ContentTokenBlacklistServiceImpl implements IContentTokenBlacklistService {

    @Resource
    private StringRedisTemplate redisTemplate;

    @Resource
    private IContentDeviceSessionService deviceSessionService;

    @Override
    public void addToBlacklist(String jti, long ttlSeconds) {
        String key = AuthRedisKeyConstant.TOKEN_BLACKLIST_PREFIX + jti;
        redisTemplate.opsForValue().set(key, "1", ttlSeconds, TimeUnit.SECONDS);
        log.info("Token JTI 已加入黑名单, jti={}, ttl={}s", jti, ttlSeconds);
    }

    @Override
    public boolean isBlacklisted(String jti) {
        String key = AuthRedisKeyConstant.TOKEN_BLACKLIST_PREFIX + jti;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean validateToken(String jti) {
        if (jti == null) {
            return false;
        }
        // 1. 检查黑名单
        if (isBlacklisted(jti)) {
            return false;
        }
        // 2. 检查设备会话状态
        return deviceSessionService.isTokenValid(jti);
    }
}
