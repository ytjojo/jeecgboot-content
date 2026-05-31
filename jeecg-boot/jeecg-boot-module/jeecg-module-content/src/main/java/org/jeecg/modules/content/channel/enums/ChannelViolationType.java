package org.jeecg.modules.content.channel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChannelViolationType {
    RESTRICT_RECOMMEND("restrict_recommend", "限制推荐"),
    FREEZE("freeze", "只读冻结"),
    HIDE("hide", "强制隐藏"),
    CLOSE("close", "永久关闭");

    private final String code;
    private final String desc;
}
