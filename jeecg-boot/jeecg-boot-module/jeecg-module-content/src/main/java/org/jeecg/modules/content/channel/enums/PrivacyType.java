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

    public static PrivacyType fromCode(int code) {
        for (PrivacyType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的隐私设置值: " + code);
    }
}
