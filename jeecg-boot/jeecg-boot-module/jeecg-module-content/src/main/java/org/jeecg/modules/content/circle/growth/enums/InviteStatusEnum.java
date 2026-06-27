package org.jeecg.modules.content.circle.growth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 圈子邀请状态枚举。
 */
@Getter
@RequiredArgsConstructor
public enum InviteStatusEnum {
    PENDING("PENDING", "待接受"),
    JOINED("JOINED", "已加入"),
    EXPIRED("EXPIRED", "已过期");

    private final String code;
    private final String description;
}
