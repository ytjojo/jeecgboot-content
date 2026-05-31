package org.jeecg.modules.content.user.growth.constant;

/**
 * 成长激励常量定义。
 */
public final class GrowthConstant {
    private GrowthConstant() {}

    /** 每日经验值上限 */
    public static final int DAILY_EXP_CAP = 100;

    /** 等级门槛: L1=0, L2=100, L3=300, L4=600, L5=850 */
    public static final int[] LEVEL_THRESHOLDS = {0, 100, 300, 600, 850};

    /** 成长分上限 */
    public static final int MAX_GROWTH_SCORE = 1000;

    /** 排行榜展示数量 */
    public static final int LEADERBOARD_TOP_N = 50;
}
