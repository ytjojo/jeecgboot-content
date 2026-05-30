package org.jeecg.modules.content.channel.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GovernanceAction {

    REMOVE(1, "移除"),
    MUTE(2, "禁言"),
    UNMUTE(3, "解除禁言"),
    BLACKLIST_ADD(4, "加入黑名单"),
    BLACKLIST_REMOVE(5, "移出黑名单");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
}
