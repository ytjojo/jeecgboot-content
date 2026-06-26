package org.jeecg.modules.content.circle.growth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 成就徽章类型枚举。
 */
@Getter
@RequiredArgsConstructor
public enum AchievementTypeEnum {
    CONTINUOUS_CREATOR("CONTINUOUS_CREATOR", "持续创作者"),
    QUALITY_CONTRIBUTOR("QUALITY_CONTRIBUTOR", "优质贡献者"),
    ACTIVE_PARTICIPANT("ACTIVE_PARTICIPANT", "活跃参与者"),
    RISING_STAR("RISING_STAR", "圈内新星");

    private final String code;
    private final String description;
}
