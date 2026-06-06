package org.jeecg.modules.content.channel.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JoinMethod {

    FREE(1, "自由加入"),
    REVIEW(2, "审核加入"),
    INVITE(3, "邀请加入");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;

    public static JoinMethod fromCode(int code) {
        for (JoinMethod method : values()) {
            if (method.code == code) {
                return method;
            }
        }
        throw new IllegalArgumentException("无效的加入方式值: " + code);
    }
}
