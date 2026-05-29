package org.jeecg.modules.content.auth.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.auth.service.CaptchaVerifyPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * 人机验证端口的空实现，未配置实际验证服务时默认放行。
 */
@Slf4j
@Component
@ConditionalOnMissingBean(CaptchaVerifyPort.class)
public class NoopCaptchaVerifyPort implements CaptchaVerifyPort {

    @Override
    public boolean verify(String token, String clientIp) {
        log.warn("[NoopCaptchaVerifyPort] 人机验证未配置实际服务, 默认放行, clientIp={}", clientIp);
        return true;
    }
}
