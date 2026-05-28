package org.jeecg.modules.content.userstatus.model;

import org.jeecg.modules.content.userstatus.entity.UserStatusEnum;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 用户状态转换规则定义。
 * 定义每种状态可以转换到哪些目标状态。
 */
public class UserStatusTransition {

    /**
     * 状态转换规则表：Map<当前状态, Set<允许的目标状态>>
     */
    private static final Map<UserStatusEnum, Set<UserStatusEnum>> TRANSITION_RULES = new EnumMap<>(UserStatusEnum.class);

    static {
        // GUEST -> REGISTERED_INCOMPLETE, NORMAL
        TRANSITION_RULES.put(UserStatusEnum.GUEST, EnumSet.of(UserStatusEnum.REGISTERED_INCOMPLETE, UserStatusEnum.NORMAL));

        // REGISTERED_INCOMPLETE -> NORMAL, DEACTIVATING
        TRANSITION_RULES.put(UserStatusEnum.REGISTERED_INCOMPLETE, EnumSet.of(UserStatusEnum.NORMAL, UserStatusEnum.DEACTIVATING));

        // NORMAL -> MUTED, RESTRICTED_RECOMMEND, FROZEN, BANNED, DEACTIVATING
        TRANSITION_RULES.put(UserStatusEnum.NORMAL, EnumSet.of(
            UserStatusEnum.MUTED,
            UserStatusEnum.RESTRICTED_RECOMMEND,
            UserStatusEnum.FROZEN,
            UserStatusEnum.BANNED,
            UserStatusEnum.DEACTIVATING
        ));

        // MUTED -> NORMAL, BANNED, DEACTIVATING
        TRANSITION_RULES.put(UserStatusEnum.MUTED, EnumSet.of(UserStatusEnum.NORMAL, UserStatusEnum.BANNED, UserStatusEnum.DEACTIVATING));

        // RESTRICTED_RECOMMEND -> NORMAL, MUTED, BANNED, DEACTIVATING
        TRANSITION_RULES.put(UserStatusEnum.RESTRICTED_RECOMMEND, EnumSet.of(
            UserStatusEnum.NORMAL,
            UserStatusEnum.MUTED,
            UserStatusEnum.BANNED,
            UserStatusEnum.DEACTIVATING
        ));

        // FROZEN -> NORMAL, BANNED
        TRANSITION_RULES.put(UserStatusEnum.FROZEN, EnumSet.of(UserStatusEnum.NORMAL, UserStatusEnum.BANNED));

        // BANNED -> NORMAL (only for temporary ban)
        TRANSITION_RULES.put(UserStatusEnum.BANNED, EnumSet.of(UserStatusEnum.NORMAL));

        // DEACTIVATING -> DEACTIVATED, NORMAL (cancel deactivation)
        TRANSITION_RULES.put(UserStatusEnum.DEACTIVATING, EnumSet.of(UserStatusEnum.DEACTIVATED, UserStatusEnum.NORMAL));

        // DEACTIVATED -> no transitions (final state)
        TRANSITION_RULES.put(UserStatusEnum.DEACTIVATED, Collections.emptySet());
    }

    /**
     * 检查状态转换是否合法
     *
     * @param from 当前状态
     * @param to   目标状态
     * @return 如果转换合法返回 true，否则返回 false
     */
    public static boolean isValidTransition(UserStatusEnum from, UserStatusEnum to) {
        Set<UserStatusEnum> allowedTargets = TRANSITION_RULES.get(from);
        return allowedTargets != null && allowedTargets.contains(to);
    }

    /**
     * 获取允许的目标状态集合
     *
     * @param from 当前状态
     * @return 允许的目标状态集合，如果状态不存在返回空集合
     */
    public static Set<UserStatusEnum> getAllowedTransitions(UserStatusEnum from) {
        return TRANSITION_RULES.getOrDefault(from, Collections.emptySet());
    }
}
