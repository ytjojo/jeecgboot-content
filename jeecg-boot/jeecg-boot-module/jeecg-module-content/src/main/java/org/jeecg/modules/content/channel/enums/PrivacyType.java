package org.jeecg.modules.content.channel.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PrivacyType {

    PUBLIC(1, "公开"),
    PRIVATE(2, "私有");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
}
