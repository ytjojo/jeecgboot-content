package org.jeecg.modules.content.auth.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.auth.service.IContentTokenService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.UUID;

/**
 * 内容社区Token服务实现。
 * 使用Redis存储token元数据，支持邮箱验证和密码重置token的生成、验证和消费。
 */
@Slf4j
@Service
public class ContentTokenServiceImpl implements IContentTokenService {

    private static final String TOKEN_KEY_PREFIX = "content:auth:token:";
    private static final String USED_SUFFIX = ":used";
    private static final long EMAIL_VERIFY_TTL_SECONDS = 86400L;   // 24小时
    private static final long PASSWORD_RESET_TTL_SECONDS = 3600L;  // 1小时

    @Resource
    private StringRedisTemplate redisTemplate;

    @Override
    public String generateEmailVerifyToken(String userId, String email) {
        String token = UUID.randomUUID().toString();
        String value = userId + ":EMAIL_VERIFY:" + email;
        redisTemplate.opsForValue().set(
                TOKEN_KEY_PREFIX + token,
                value,
                Duration.ofSeconds(EMAIL_VERIFY_TTL_SECONDS)
        );
        log.info("Generated email verify token for userId={}, email={}", userId, email);
        return token;
    }

    @Override
    public String generatePasswordResetToken(String userId) {
        String token = UUID.randomUUID().toString();
        String value = userId + ":PASSWORD_RESET:";
        redisTemplate.opsForValue().set(
                TOKEN_KEY_PREFIX + token,
                value,
                Duration.ofSeconds(PASSWORD_RESET_TTL_SECONDS)
        );
        log.info("Generated password reset token for userId={}", userId);
        return token;
    }

    @Override
    public String validateAndConsumeToken(String token, String expectedType) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        String value = redisTemplate.opsForValue().get(TOKEN_KEY_PREFIX + token);
        if (value == null) {
            return null;
        }

        String[] parts = value.split(":", 3);
        if (parts.length < 2) {
            return null;
        }

        String userId = parts[0];
        String type = parts[1];

        if (!expectedType.equals(type)) {
            return null;
        }

        if (isTokenUsed(token)) {
            return null;
        }

        // 标记token为已使用，保留1小时用于防重放
        redisTemplate.opsForValue().set(
                TOKEN_KEY_PREFIX + token + USED_SUFFIX,
                "true",
                Duration.ofSeconds(3600)
        );
        log.info("Consumed token for userId={}, type={}", userId, type);
        return userId;
    }

    @Override
    public boolean isTokenUsed(String token) {
        String usedFlag = redisTemplate.opsForValue().get(TOKEN_KEY_PREFIX + token + USED_SUFFIX);
        return "true".equals(usedFlag);
    }
}
