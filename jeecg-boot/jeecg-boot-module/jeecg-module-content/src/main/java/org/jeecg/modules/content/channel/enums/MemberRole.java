package org.jeecg.modules.content.channel.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberRole {

    OWNER(1, "频道主"),
    ADMIN(2, "管理员"),
    EDITOR(3, "内容编辑"),
    MEMBER(4, "普通成员");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
}
