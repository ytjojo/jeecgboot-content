package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.req.settings.ContentUserNotificationUpdateReq;
import org.jeecg.modules.content.user.vo.ContentUserNotificationSettingVO;

import java.time.LocalTime;

/**
 * 内容社区用户通知设置服务。
 */
public interface IContentUserNotificationSettingService {

    ContentUserNotificationSettingVO getSetting(String userId);

    ContentUserNotificationSettingVO updateSetting(String userId, ContentUserNotificationUpdateReq req);

    boolean canSendNotice(String userId, String noticeType, String channel, LocalTime currentTime);
}
