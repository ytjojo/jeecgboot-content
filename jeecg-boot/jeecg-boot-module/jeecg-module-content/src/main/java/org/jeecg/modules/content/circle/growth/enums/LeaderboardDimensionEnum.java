package org.jeecg.modules.content.circle.growth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 排行榜维度枚举。
 */
@Getter
@RequiredArgsConstructor
public enum LeaderboardDimensionEnum {
    EXP("EXP", "经验值"),
    CONTRIBUTION("CONTRIBUTION", "贡献值"),
    POST("POST", "发帖数");

    private final String code;
    private final String description;
}
