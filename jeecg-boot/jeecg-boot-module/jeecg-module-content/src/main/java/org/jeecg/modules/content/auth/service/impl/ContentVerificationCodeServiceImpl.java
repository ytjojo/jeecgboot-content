package org.jeecg.modules.content.auth.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.auth.constant.AuthRedisKeyConstant;
import org.jeecg.modules.content.auth.enums.VerificationCodeSceneEnum;
import org.jeecg.modules.content.auth.service.IContentVerificationCodeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现。
 */
@Slf4j
@Service
public class ContentVerificationCodeServiceImpl implements IContentVerificationCodeService {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Resource
    private StringRedisTemplate redisTemplate;

    @Override
    public String generateCode(VerificationCodeSceneEnum scene, String target) {
        String code = generateSixDigitCode();
        long ttl = getCodeTtl(scene);

        // 存储验证码
        String codeKey = buildCodeKey(scene, target);
        redisTemplate.opsForValue().set(codeKey, code, ttl, TimeUnit.SECONDS);

        // 设置冷却期
        String cooldownKey = buildCooldownKey(scene, target);
        redisTemplate.opsForValue().set(cooldownKey, "1", AuthRedisKeyConstant.CODE_COOLDOWN_TTL, TimeUnit.SECONDS);

        log.debug("验证码已生成 scene={}, target={}, ttl={}s", scene.getCode(), target, ttl);
        return code;
    }

    @Override
    public boolean verifyCode(VerificationCodeSceneEnum scene, String target, String code) {
        if (code == null || code.isEmpty()) {
            log.debug("验证码为空 scene={}, target={}", scene.getCode(), target);
            return false;
        }

        String codeKey = buildCodeKey(scene, target);
        String storedCode = redisTemplate.opsForValue().get(codeKey);
        if (storedCode == null) {
            log.debug("验证码已过期或不存在 scene={}, target={}", scene.getCode(), target);
            return false;
        }

        // 检查失败次数
        String failKey = buildFailKey(scene, target);
        int failCount = getFailCount(scene, target);
        if (failCount >= AuthRedisKeyConstant.CODE_MAX_FAIL_COUNT) {
            log.debug("验证码失败次数超限 scene={}, target={}, failCount={}", scene.getCode(), target, failCount);
            return false;
        }

        if (!storedCode.equals(code)) {
            // 验证码错误，累加失败计数
            Long newCount = redisTemplate.opsForValue().increment(failKey);
            if (newCount != null && newCount == 1L) {
                // 首次失败，设置TTL
                redisTemplate.expire(failKey, AuthRedisKeyConstant.SMS_CODE_TTL, TimeUnit.SECONDS);
            }
            log.debug("验证码错误 scene={}, target={}, failCount={}", scene.getCode(), target, newCount);
            return false;
        }

        // 验证成功，删除验证码和失败计数
        redisTemplate.delete(codeKey);
        redisTemplate.delete(failKey);
        log.debug("验证码校验成功 scene={}, target={}", scene.getCode(), target);
        return true;
    }

    @Override
    public boolean isInCooldown(VerificationCodeSceneEnum scene, String target) {
        String cooldownKey = buildCooldownKey(scene, target);
        Boolean hasKey = redisTemplate.hasKey(cooldownKey);
        return Boolean.TRUE.equals(hasKey);
    }

    @Override
    public int getFailCount(VerificationCodeSceneEnum scene, String target) {
        String failKey = buildFailKey(scene, target);
        String countStr = redisTemplate.opsForValue().get(failKey);
        if (countStr == null) {
            return 0;
        }
        try {
            return Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            log.warn("失败计数格式异常 failKey={}, value={}", failKey, countStr);
            return 0;
        }
    }

    /**
     * 根据场景判断验证码TTL。
     * 邮箱场景使用较长TTL，短信场景使用较短TTL。
     */
    private long getCodeTtl(VerificationCodeSceneEnum scene) {
        return switch (scene) {
            case BIND_EMAIL, UNBIND_EMAIL -> AuthRedisKeyConstant.EMAIL_CODE_TTL;
            default -> AuthRedisKeyConstant.SMS_CODE_TTL;
        };
    }

    private String generateSixDigitCode() {
        int code = RANDOM.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    private String buildCodeKey(VerificationCodeSceneEnum scene, String target) {
        return AuthRedisKeyConstant.CODE_PREFIX + scene.getCode() + ":" + target;
    }

    private String buildFailKey(VerificationCodeSceneEnum scene, String target) {
        return AuthRedisKeyConstant.CODE_FAIL_PREFIX + scene.getCode() + ":" + target;
    }

    private String buildCooldownKey(VerificationCodeSceneEnum scene, String target) {
        return AuthRedisKeyConstant.COOLDOWN_PREFIX + scene.getCode() + ":" + target;
    }
}
