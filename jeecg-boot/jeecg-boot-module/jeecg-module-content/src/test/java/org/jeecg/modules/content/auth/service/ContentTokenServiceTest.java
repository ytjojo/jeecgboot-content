package org.jeecg.modules.content.auth.service;

import org.jeecg.modules.content.auth.service.impl.ContentTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentTokenServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private ContentTokenServiceImpl service;

    @BeforeEach
    void setup() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void generateEmailVerifyToken_shouldReturnNonNullUuid() {
        String result = service.generateEmailVerifyToken("user123", "test@example.com");

        assertThat(result).isNotNull();
        // Verify it's a valid UUID format
        UUID.fromString(result);
        verify(valueOps).set(
                eq("content:auth:token:" + result),
                eq("user123:EMAIL_VERIFY:test@example.com"),
                eq(Duration.ofSeconds(86400))
        );
    }

    @Test
    void generatePasswordResetToken_shouldReturnNonNullUuid() {
        String result = service.generatePasswordResetToken("user456");

        assertThat(result).isNotNull();
        UUID.fromString(result);
        verify(valueOps).set(
                eq("content:auth:token:" + result),
                eq("user456:PASSWORD_RESET:"),
                eq(Duration.ofSeconds(3600))
        );
    }

    @Test
    void validateAndConsumeToken_withValidToken_shouldReturnUserId() {
        String token = "valid-token-uuid";
        when(valueOps.get("content:auth:token:" + token)).thenReturn("user123:EMAIL_VERIFY:test@example.com");

        String result = service.validateAndConsumeToken(token, "EMAIL_VERIFY");

        assertThat(result).isEqualTo("user123");
        // Verify token is marked as used
        verify(valueOps).set(
                eq("content:auth:token:" + token + ":used"),
                eq("true"),
                eq(Duration.ofSeconds(3600))
        );
    }

    @Test
    void validateAndConsumeToken_withExpiredToken_shouldReturnNull() {
        String token = "expired-token-uuid";
        when(valueOps.get("content:auth:token:" + token)).thenReturn(null);

        String result = service.validateAndConsumeToken(token, "EMAIL_VERIFY");

        assertThat(result).isNull();
    }

    @Test
    void validateAndConsumeToken_withAlreadyUsedToken_shouldReturnNull() {
        String token = "used-token-uuid";
        when(valueOps.get("content:auth:token:" + token)).thenReturn("user123:EMAIL_VERIFY:test@example.com");
        when(valueOps.get("content:auth:token:" + token + ":used")).thenReturn("true");

        String result = service.validateAndConsumeToken(token, "EMAIL_VERIFY");

        assertThat(result).isNull();
    }

    @Test
    void validateAndConsumeToken_withWrongType_shouldReturnNull() {
        String token = "wrong-type-token";
        when(valueOps.get("content:auth:token:" + token)).thenReturn("user123:EMAIL_VERIFY:test@example.com");

        String result = service.validateAndConsumeToken(token, "PASSWORD_RESET");

        assertThat(result).isNull();
    }

    @Test
    void validateAndConsumeToken_withNullToken_shouldReturnNull() {
        String result = service.validateAndConsumeToken(null, "EMAIL_VERIFY");

        assertThat(result).isNull();
        verifyNoInteractions(valueOps);
    }

    @Test
    void validateAndConsumeToken_withEmptyToken_shouldReturnNull() {
        String result = service.validateAndConsumeToken("", "EMAIL_VERIFY");

        assertThat(result).isNull();
        verifyNoInteractions(valueOps);
    }

    @Test
    void isTokenUsed_shouldReturnTrueWhenUsedFlagExists() {
        String token = "used-token";
        when(valueOps.get("content:auth:token:" + token + ":used")).thenReturn("true");

        boolean result = service.isTokenUsed(token);

        assertThat(result).isTrue();
    }

    @Test
    void isTokenUsed_shouldReturnFalseWhenTokenNotFound() {
        String token = "not-found-token";
        when(valueOps.get("content:auth:token:" + token + ":used")).thenReturn(null);

        boolean result = service.isTokenUsed(token);

        assertThat(result).isFalse();
    }
}
