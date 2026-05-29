package org.jeecg.modules.content.auth.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.auth.service.EmailSenderPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * 邮件发送端口的空实现，未配置实际邮件通道时兜底使用。
 */
@Slf4j
@Component
@ConditionalOnMissingBean(EmailSenderPort.class)
public class NoopEmailSenderPort implements EmailSenderPort {

    @Override
    public boolean send(String to, String subject, String htmlContent) {
        log.warn("[NoopEmailSenderPort] 邮件发送未配置实际通道, to={}", to);
        return true;
    }
}
