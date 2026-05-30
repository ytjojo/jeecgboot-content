package org.jeecg.modules.content.channel.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChannelStatus {

    DRAFT(0, "Draft"),
    PENDING_REVIEW(1, "PendingReview"),
    ACTIVE(2, "Active"),
    REJECTED(3, "Rejected"),
    DELETE_COOLING(4, "DeleteCooling"),
    DELETED(5, "Deleted");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
}
