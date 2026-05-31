package org.jeecg.modules.content.channel.constant;

/**
 * 频道成员管理常量
 */
public final class ChannelMemberConstants {

    private ChannelMemberConstants() {}

    /** 禁言天数：1天 */
    public static final int MUTE_1_DAY = 1;

    /** 禁言天数：7天 */
    public static final int MUTE_7_DAYS = 7;

    /** 禁言天数：30天 */
    public static final int MUTE_30_DAYS = 30;

    /** 禁言天数：永久（0表示永久） */
    public static final int MUTE_PERMANENT = 0;

    /** 加入申请超时阈值（小时） */
    public static final int APPLICATION_OVERDUE_HOURS = 48;

    /** 解除禁言方式：自动到期 */
    public static final int UNMUTE_TYPE_AUTO = 1;

    /** 解除禁言方式：手动解除 */
    public static final int UNMUTE_TYPE_MANUAL = 2;
}
