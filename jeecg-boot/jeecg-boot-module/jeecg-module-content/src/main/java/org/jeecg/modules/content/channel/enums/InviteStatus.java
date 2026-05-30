package org.jeecg.modules.content.channel.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InviteStatus {

    ACTIVE(1, "有效"),
    USED_UP(2, "已用完"),
    REVOKED(3, "已撤销"),
    EXPIRED(4, "已过期");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
}
