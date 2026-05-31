package org.jeecg.modules.content.channel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PublishStatusEnum {
    PUBLISHED("PUBLISHED", "已发布"),
    PENDING("PENDING", "待审核"),
    REJECTED("REJECTED", "已拒绝"),
    RECYCLED("RECYCLED", "已回收"),
    SCHEDULED("SCHEDULED", "定时发布"),
    FAILED("FAILED", "失败");

    private final String code;
    private final String desc;
}
