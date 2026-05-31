package org.jeecg.modules.content.channel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChannelReviewStatus {
    PENDING("pending", "待审核"),
    APPROVED("approved", "已通过"),
    REJECTED("rejected", "已拒绝"),
    RETURNED("returned", "已退回");

    private final String code;
    private final String desc;
}
