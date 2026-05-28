package org.jeecg.modules.content.user.service;

/**
 * 通知发送服务接口。
 */
public interface IContentNotificationService {

    /**
     * 发送通知。
     *
     * @param userId      接收用户ID
     * @param noticeType  通知类型
     * @param title       通知标题
     * @param content     通知内容
     */
    void sendNotification(String userId, String noticeType, String title, String content);
}
