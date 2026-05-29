package org.jeecg.modules.content.auth.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.auth.service.SmsSenderPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * 短信发送端口的空实现，未配置实际短信通道时兜底使用。
 */
@Slf4j
@Component
@ConditionalOnMissingBean(SmsSenderPort.class)
public class NoopSmsSenderPort implements SmsSenderPort {

    @Override
    public boolean send(String mobile, String content) {
        log.warn("[NoopSmsSenderPort] 短信发送未配置实际通道, mobile={}", mobile);
        return true;
    }
}
