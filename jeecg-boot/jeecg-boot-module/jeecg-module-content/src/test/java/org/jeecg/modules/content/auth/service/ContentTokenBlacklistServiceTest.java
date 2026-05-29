package org.jeecg.modules.content.auth.service;

import org.jeecg.modules.content.auth.constant.AuthRedisKeyConstant;
import org.jeecg.modules.content.auth.service.impl.ContentTokenBlacklistServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Token 黑名单服务测试。
 * 覆盖黑名单存储、查询及 token 校验逻辑。
 */
@ExtendWith(MockitoExtension.class)
class ContentTokenBlacklistServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private IContentDeviceSessionService deviceSessionService;

    @InjectMocks
    private ContentTokenBlacklistServiceImpl blacklistService;

    private static final String TEST_JTI = "jti_abc123";
    private static final String BLACKLIST_KEY = AuthRedisKeyConstant.TOKEN_BLACKLIST_PREFIX + TEST_JTI;

    // ==================== addToBlacklist 测试 ====================

    @Nested
    @DisplayName("addToBlacklist")
    class AddToBlacklist {

        @Test
        @DisplayName("将 jti 存入 Redis 并设置正确的 TTL")
        void addToBlacklist_storesWithCorrectTtl() {
            // given
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            // when
            blacklistService.addToBlacklist(TEST_JTI, AuthRedisKeyConstant.TOKEN_BLACKLIST_TTL);

            // then
            verify(valueOperations).set(BLACKLIST_KEY, "1", AuthRedisKeyConstant.TOKEN_BLACKLIST_TTL, TimeUnit.SECONDS);
        }

        @Test
        @DisplayName("使用自定义 TTL 存入黑名单")
        void addToBlacklist_customTtl() {
            // given
            long customTtl = 3600L;
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            // when
            blacklistService.addToBlacklist(TEST_JTI, customTtl);

            // then
            verify(valueOperations).set(BLACKLIST_KEY, "1", customTtl, TimeUnit.SECONDS);
        }
    }

    // ==================== isBlacklisted 测试 ====================

    @Nested
    @DisplayName("isBlacklisted")
    class IsBlacklisted {

        @Test
        @DisplayName("jti 在黑名单中 - 返回 true")
        void isBlacklisted_returnsTrue_whenPresent() {
            // given
            when(redisTemplate.hasKey(BLACKLIST_KEY)).thenReturn(true);

            // when
            boolean result = blacklistService.isBlacklisted(TEST_JTI);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("jti 不在黑名单中 - 返回 false")
        void isBlacklisted_returnsFalse_whenAbsent() {
            // given
            when(redisTemplate.hasKey(BLACKLIST_KEY)).thenReturn(false);

            // when
            boolean result = blacklistService.isBlacklisted(TEST_JTI);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Redis 返回 null - 返回 false")
        void isBlacklisted_returnsFalse_whenRedisReturnsNull() {
            // given
            when(redisTemplate.hasKey(BLACKLIST_KEY)).thenReturn(null);

            // when
            boolean result = blacklistService.isBlacklisted(TEST_JTI);

            // then
            assertThat(result).isFalse();
        }
    }

    // ==================== validateToken 测试 ====================

    @Nested
    @DisplayName("validateToken")
    class ValidateToken {

        @Test
        @DisplayName("未在黑名单且会话有效 - 返回 true")
        void validateToken_returnsTrue_whenNotBlacklistedAndSessionActive() {
            // given
            when(redisTemplate.hasKey(BLACKLIST_KEY)).thenReturn(false);
            when(deviceSessionService.isTokenValid(TEST_JTI)).thenReturn(true);

            // when
            boolean result = blacklistService.validateToken(TEST_JTI);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("在黑名单中 - 返回 false（不检查会话）")
        void validateToken_returnsFalse_whenBlacklisted() {
            // given
            when(redisTemplate.hasKey(BLACKLIST_KEY)).thenReturn(true);

            // when
            boolean result = blacklistService.validateToken(TEST_JTI);

            // then
            assertThat(result).isFalse();
            verify(deviceSessionService, never()).isTokenValid(anyString());
        }

        @Test
        @DisplayName("会话已下线 - 返回 false（即使不在黑名单）")
        void validateToken_returnsFalse_whenSessionOffline() {
            // given
            when(redisTemplate.hasKey(BLACKLIST_KEY)).thenReturn(false);
            when(deviceSessionService.isTokenValid(TEST_JTI)).thenReturn(false);

            // when
            boolean result = blacklistService.validateToken(TEST_JTI);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("jti 为 null - 返回 false")
        void validateToken_returnsFalse_whenJtiIsNull() {
            // when
            boolean result = blacklistService.validateToken(null);

            // then
            assertThat(result).isFalse();
            verify(redisTemplate, never()).hasKey(anyString());
            verify(deviceSessionService, never()).isTokenValid(anyString());
        }
    }
}
