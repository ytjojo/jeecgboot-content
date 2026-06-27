package org.jeecg.modules.content.circle.growth.constant;

/**
 * 成长激励常量定义。
 */
public final class GrowthConstant {
    private GrowthConstant() {}

    /** 每日经验值上限 */
    public static final int DAILY_EXP_CAP = 100;

    /** 圈子等级门槛（成长分）: L1=0, L2=100, L3=300, L4=600, L5=850 */
    public static final int[] CIRCLE_LEVEL_THRESHOLDS = {0, 100, 300, 600, 850};

    /** 成员等级门槛（经验值）: L1=0, L2=100, L3=300, L4=600, L5=1000 */
    public static final int[] MEMBER_LEVEL_THRESHOLDS = {0, 100, 300, 600, 1000};

    /** 成长分上限 */
    public static final int MAX_GROWTH_SCORE = 1000;

    /** 排行榜展示数量 */
    public static final int LEADERBOARD_TOP_N = 50;

    /** 排行榜快照刷新周期 */
    public static final String[] LEADERBOARD_PERIODS = {"WEEK", "MONTH", "ALL"};
}
