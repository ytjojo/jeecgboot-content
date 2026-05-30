package org.jeecg.modules.content.channel.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReviewResult {

    PASS(1, "Pass"),
    REJECT(2, "Reject"),
    RETURN_FOR_EDIT(3, "ReturnForEdit");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
}
