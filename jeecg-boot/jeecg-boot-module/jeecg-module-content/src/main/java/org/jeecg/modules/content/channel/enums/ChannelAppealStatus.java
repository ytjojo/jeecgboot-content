package org.jeecg.modules.content.channel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChannelAppealStatus {
    PENDING("pending", "待处理"),
    PROCESSING("processing", "处理中"),
    APPROVED("approved", "已通过"),
    REJECTED("rejected", "已驳回");

    private final String code;
    private final String desc;
}
