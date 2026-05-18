package org.jeecg.modules.content.user.constant;

import java.util.Set;

/**
 * 内容社区用户勋章常量。
 */
public interface ContentUserBadgeConstant {

    String CATEGORY_ACHIEVEMENT = "ACHIEVEMENT";
    String CATEGORY_IDENTITY = "IDENTITY";
    String CATEGORY_ACTIVITY = "ACTIVITY";
    String CATEGORY_RELATIONSHIP = "RELATIONSHIP";

    Set<String> SUPPORTED_CATEGORIES = Set.of(
        CATEGORY_ACHIEVEMENT,
        CATEGORY_IDENTITY,
        CATEGORY_ACTIVITY,
        CATEGORY_RELATIONSHIP
    );

    String STATUS_ACTIVE = "ACTIVE";
    String STATUS_EXPIRED = "EXPIRED";
    String STATUS_RECYCLED = "RECYCLED";

    int MAX_WORN_BADGE_COUNT = 5;
}
