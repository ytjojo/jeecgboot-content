package org.jeecg.modules.content.auth.service;

/**
 * 邮件发送端口。
 */
public interface EmailSenderPort {

    /**
     * 发送邮件。
     *
     * @param to          收件人
     * @param subject     主题
     * @param htmlContent HTML内容
     * @return 发送是否成功
     */
    boolean send(String to, String subject, String htmlContent);
}
