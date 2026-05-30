package org.jeecg.modules.content.channel.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApplicationStatus {

    PENDING(1, "待审核"),
    APPROVED(2, "已批准"),
    REJECTED(3, "已拒绝");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
}
