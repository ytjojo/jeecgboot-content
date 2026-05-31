package org.jeecg.modules.content.channel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChannelExportStatus {
    PENDING("pending", "待处理"),
    PROCESSING("processing", "处理中"),
    COMPLETED("completed", "已完成"),
    FAILED("failed", "失败");

    private final String code;
    private final String desc;
}
