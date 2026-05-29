package org.jeecg.modules.content.auth.service;

/**
 * 人机验证端口，用于风控挑战场景。
 */
public interface CaptchaVerifyPort {

    /**
     * 校验人机验证结果。
     *
     * @param token    前端传来的验证token
     * @param clientIp 客户端IP
     * @return 是否通过验证
     */
    boolean verify(String token, String clientIp);
}
