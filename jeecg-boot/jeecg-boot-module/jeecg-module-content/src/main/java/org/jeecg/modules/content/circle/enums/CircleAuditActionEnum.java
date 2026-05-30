package org.jeecg.modules.content.circle.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 圈子审核操作类型枚举。
 */
@Getter
@RequiredArgsConstructor
public enum CircleAuditActionEnum {
    PIN("PIN", "置顶"),
    UNPIN("UNPIN", "取消置顶"),
    FEATURE("FEATURE", "设为精华"),
    UNFEATURE("UNFEATURE", "取消精华"),
    PUBLISH_ANNOUNCEMENT("PUBLISH_ANNOUNCEMENT", "发布公告"),
    APPROVE_JOIN("APPROVE_JOIN", "批准加入"),
    REJECT_JOIN("REJECT_JOIN", "拒绝加入"),
    DELETE_REPORTED("DELETE_REPORTED", "删除举报内容"),
    IGNORE_REPORT("IGNORE_REPORT", "忽略举报"),
    MUTE_FROM_REPORT("MUTE_FROM_REPORT", "举报禁言");

    private final String code;
    private final String description;

    /**
     * Returns all supported audit action codes.
     */
    public static List<String> codes() {
        return Arrays.stream(values()).map(CircleAuditActionEnum::getCode).collect(Collectors.toList());
    }
}
