package org.jeecg.modules.content.channel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChannelLifecycleStatus {
    PENDING_REVIEW("PendingReview", "待审核"),
    ACTIVE("Active", "正常"),
    READONLY_FROZEN("ReadonlyFrozen", "只读冻结"),
    HIDDEN("Hidden", "强制隐藏"),
    ARCHIVED("Archived", "已归档"),
    MERGED("Merged", "已合并"),
    CLOSED("Closed", "永久关闭"),
    DELETED("Deleted", "已删除");

    private final String code;
    private final String desc;

    public static ChannelLifecycleStatus fromCode(String code) {
        for (ChannelLifecycleStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown lifecycle status code: " + code);
    }
}
