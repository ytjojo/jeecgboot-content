package org.jeecg.modules.channel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jeecg.modules.common.enums.BaseEnum;

@Getter
@AllArgsConstructor
public enum ChannelType implements BaseEnum<ChannelType,Integer> {
    /**
     * 私有频道
     */
    PRIVATE(0, "私有频道", "仅邀请用户可以查看和加入"),
    
    PUBLIC(1, "公开频道", "任何人都可以查看和加入"),
    /**
     * 受保护频道
     */
    PROTECTED(2, "受保护频道", "任何人可以查看，但需要申请加入"),
    /**
     * 秘密频道
     */
    SECRET(3, "秘密频道", "仅成员可见");

    private final Integer value;
    private final String name;
    private final String description;
}