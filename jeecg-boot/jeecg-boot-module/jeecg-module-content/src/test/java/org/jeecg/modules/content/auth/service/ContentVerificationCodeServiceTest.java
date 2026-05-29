package org.jeecg.modules.content.auth.service;

import org.jeecg.modules.content.auth.constant.AuthRedisKeyConstant;
import org.jeecg.modules.content.auth.enums.VerificationCodeSceneEnum;
import org.jeecg.modules.content.auth.service.impl.ContentVerificationCodeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
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
 * 验证码服务单元测试。
 * 覆盖场景：生成、校验（正确/错误/过期/空值）、冷却期、失败计数。
 */
@ExtendWith(MockitoExtension.class)
class ContentVerificationCodeServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private ContentVerificationCodeServiceImpl service;

    private static final VerificationCodeSceneEnum SCENE = VerificationCodeSceneEnum.LOGIN;
    private static final String TARGET = "13800138000";
    private static final String CODE_KEY = AuthRedisKeyConstant.CODE_PREFIX + SCENE.getCode() + ":" + TARGET;
    private static final String FAIL_KEY = AuthRedisKeyConstant.CODE_FAIL_PREFIX + SCENE.getCode() + ":" + TARGET;
    private static final String COOLDOWN_KEY = AuthRedisKeyConstant.COOLDOWN_PREFIX + SCENE.getCode() + ":" + TARGET;

    @BeforeEach
    void setup() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    // ========== generateCode ==========

    @Test
    void generateCode_shouldReturnSixDigitCode() {
        String code = service.generateCode(SCENE, TARGET);

        assertThat(code).hasSize(6);
        assertThat(code).matches("\\d{6}");
    }

    @Test
    void generateCode_shouldStoreCodeInRedisWithCorrectTtl() {
        service.generateCode(SCENE, TARGET);

        verify(valueOps).set(eq(CODE_KEY), anyString(), eq(AuthRedisKeyConstant.SMS_CODE_TTL), eq(TimeUnit.SECONDS));
    }

    @Test
    void generateCode_shouldSetCooldownKeyWith60sTtl() {
        service.generateCode(SCENE, TARGET);

        verify(valueOps).set(eq(COOLDOWN_KEY), eq("1"), eq(AuthRedisKeyConstant.CODE_COOLDOWN_TTL), eq(TimeUnit.SECONDS));
    }

    @Test
    void generateCode_shouldUseEmailTtlForEmailScene() {
        VerificationCodeSceneEnum emailScene = VerificationCodeSceneEnum.BIND_EMAIL;
        String emailTarget = "test@example.com";
        String emailCodeKey = AuthRedisKeyConstant.CODE_PREFIX + emailScene.getCode() + ":" + emailTarget;
        String emailCooldownKey = AuthRedisKeyConstant.COOLDOWN_PREFIX + emailScene.getCode() + ":" + emailTarget;

        service.generateCode(emailScene, emailTarget);

        verify(valueOps).set(eq(emailCodeKey), anyString(), eq(AuthRedisKeyConstant.EMAIL_CODE_TTL), eq(TimeUnit.SECONDS));
        verify(valueOps).set(eq(emailCooldownKey), eq("1"), eq(AuthRedisKeyConstant.CODE_COOLDOWN_TTL), eq(TimeUnit.SECONDS));
    }

    // ========== verifyCode - 正确验证码 ==========

    @Test
    void verifyCode_shouldReturnTrue_whenCodeMatches() {
        when(valueOps.get(CODE_KEY)).thenReturn("123456");

        boolean result = service.verifyCode(SCENE, TARGET, "123456");

        assertThat(result).isTrue();
        verify(redisTemplate).delete(CODE_KEY);
        verify(redisTemplate).delete(FAIL_KEY);
    }

    // ========== verifyCode - 错误验证码 ==========

    @Test
    void verifyCode_shouldReturnFalse_whenCodeDoesNotMatch() {
        when(valueOps.get(CODE_KEY)).thenReturn("123456");
        when(valueOps.increment(FAIL_KEY)).thenReturn(1L);

        boolean result = service.verifyCode(SCENE, TARGET, "000000");

        assertThat(result).isFalse();
        verify(valueOps).increment(FAIL_KEY);
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void verifyCode_shouldReturnFalse_whenFailCountReaches3() {
        when(valueOps.get(CODE_KEY)).thenReturn("123456");
        when(valueOps.increment(FAIL_KEY)).thenReturn(3L);

        boolean result = service.verifyCode(SCENE, TARGET, "000000");

        assertThat(result).isFalse();
    }

    @Test
    void verifyCode_shouldReturnFalse_whenFailCountExceeds3() {
        when(valueOps.get(CODE_KEY)).thenReturn("123456");
        when(valueOps.increment(FAIL_KEY)).thenReturn(4L);

        boolean result = service.verifyCode(SCENE, TARGET, "000000");

        assertThat(result).isFalse();
    }

    // ========== verifyCode - 空值/过期 ==========

    @Test
    void verifyCode_shouldReturnFalse_whenCodeIsNull() {
        boolean result = service.verifyCode(SCENE, TARGET, null);

        assertThat(result).isFalse();
        verifyNoInteractions(valueOps);
    }

    @Test
    void verifyCode_shouldReturnFalse_whenCodeIsEmpty() {
        boolean result = service.verifyCode(SCENE, TARGET, "");

        assertThat(result).isFalse();
        verifyNoInteractions(valueOps);
    }

    @Test
    void verifyCode_shouldReturnFalse_whenCodeExpired() {
        when(valueOps.get(CODE_KEY)).thenReturn(null);

        boolean result = service.verifyCode(SCENE, TARGET, "123456");

        assertThat(result).isFalse();
    }

    @Test
    void verifyCode_shouldReturnFalse_whenSubmittedCodeIsNull_andStoredCodeExists() {
        boolean result = service.verifyCode(SCENE, TARGET, null);

        assertThat(result).isFalse();
        verifyNoInteractions(valueOps);
    }

    // ========== isInCooldown ==========

    @Test
    void isInCooldown_shouldReturnTrue_whenCooldownKeyExists() {
        when(redisTemplate.hasKey(COOLDOWN_KEY)).thenReturn(true);

        boolean result = service.isInCooldown(SCENE, TARGET);

        assertThat(result).isTrue();
    }

    @Test
    void isInCooldown_shouldReturnFalse_whenNoCooldownKey() {
        when(redisTemplate.hasKey(COOLDOWN_KEY)).thenReturn(false);

        boolean result = service.isInCooldown(SCENE, TARGET);

        assertThat(result).isFalse();
    }

    @Test
    void isInCooldown_shouldReturnFalse_whenHasKeyReturnsNull() {
        when(redisTemplate.hasKey(COOLDOWN_KEY)).thenReturn(null);

        boolean result = service.isInCooldown(SCENE, TARGET);

        assertThat(result).isFalse();
    }

    // ========== getFailCount ==========

    @Test
    void getFailCount_shouldReturn0_whenNoFailKey() {
        when(valueOps.get(FAIL_KEY)).thenReturn(null);

        int count = service.getFailCount(SCENE, TARGET);

        assertThat(count).isZero();
    }

    @Test
    void getFailCount_shouldReturnCount_whenFailKeyExists() {
        when(valueOps.get(FAIL_KEY)).thenReturn("2");

        int count = service.getFailCount(SCENE, TARGET);

        assertThat(count).isEqualTo(2);
    }
}
