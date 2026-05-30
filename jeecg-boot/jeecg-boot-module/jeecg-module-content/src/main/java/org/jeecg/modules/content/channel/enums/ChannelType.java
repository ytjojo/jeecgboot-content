package org.jeecg.modules.content.channel.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChannelType {

    SYSTEM(1, "system"),
    PERSONAL(2, "personal"),
    ORGANIZATION(3, "organization");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
}
