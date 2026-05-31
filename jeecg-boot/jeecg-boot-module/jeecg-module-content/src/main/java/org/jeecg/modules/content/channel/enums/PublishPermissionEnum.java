package org.jeecg.modules.content.channel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PublishPermissionEnum {
    ADMIN_ONLY("ADMIN_ONLY", "仅管理员可发布"),
    ALL_MEMBERS("ALL_MEMBERS", "所有成员可发布"),
    PUBLIC_SUBMIT("PUBLIC_SUBMIT", "公开投稿"),
    PRE_REVIEW("PRE_REVIEW", "先审后发");

    private final String code;
    private final String desc;
}
