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
}
