package org.jeecg.modules.content.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum for content notification channel.
 */
@Getter
@RequiredArgsConstructor
public enum ContentNotificationChannelEnum {
    IN_APP("IN_APP", "站内通知"),
    PUSH("PUSH", "推送"),
    EMAIL("EMAIL", "邮件"),
    SMS("SMS", "短信");

    private final String code;
    private final String description;
}
