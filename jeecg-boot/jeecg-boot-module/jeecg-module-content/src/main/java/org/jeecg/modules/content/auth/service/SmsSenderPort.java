package org.jeecg.modules.content.auth.service;

/**
 * 短信发送端口，业务层只依赖此抽象。
 */
public interface SmsSenderPort {

    /**
     * 发送短信验证码。
     *
     * @param mobile  手机号
     * @param content 短信内容(含验证码)
     * @return 发送是否成功
     */
    boolean send(String mobile, String content);
}
