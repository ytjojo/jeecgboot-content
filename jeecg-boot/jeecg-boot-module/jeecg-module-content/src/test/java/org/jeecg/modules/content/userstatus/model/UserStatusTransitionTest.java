package org.jeecg.modules.content.userstatus.model;

import org.jeecg.modules.content.userstatus.entity.UserStatusEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 用户状态转换规则测试。
 * 覆盖每种源状态允许的目标状态集合。
 */
@DisplayName("UserStatusTransition 状态机规则")
class UserStatusTransitionTest {

    @Nested
    @DisplayName("isValidTransition")
    class IsValidTransition {

        @ParameterizedTest(name = "{0} -> {1} 期望合法={2}")
        @CsvSource({
            "GUEST,REGISTERED_INCOMPLETE,true",
            "GUEST,NORMAL,true",
            "GUEST,BANNED,false",
            "REGISTERED_INCOMPLETE,NORMAL,true",
            "REGISTERED_INCOMPLETE,DEACTIVATING,true",
            "REGISTERED_INCOMPLETE,BANNED,false",
            "NORMAL,MUTED,true",
            "NORMAL,RESTRICTED_RECOMMEND,true",
            "NORMAL,FROZEN,true",
            "NORMAL,BANNED,true",
            "NORMAL,DEACTIVATING,true",
            "NORMAL,REGISTERED_INCOMPLETE,false",
            "NORMAL,GUEST,false",
            "MUTED,NORMAL,true",
            "MUTED,BANNED,true",
            "MUTED,DEACTIVATING,true",
            "MUTED,FROZEN,false",
            "RESTRICTED_RECOMMEND,NORMAL,true",
            "RESTRICTED_RECOMMEND,MUTED,true",
            "RESTRICTED_RECOMMEND,BANNED,true",
            "RESTRICTED_RECOMMEND,DEACTIVATING,true",
            "FROZEN,NORMAL,true",
            "FROZEN,BANNED,true",
            "FROZEN,DEACTIVATING,false",
            "BANNED,NORMAL,true",
            "BANNED,MUTED,false",
            "DEACTIVATING,DEACTIVATED,true",
            "DEACTIVATING,NORMAL,true",
            "DEACTIVATED,NORMAL,false",
            "DEACTIVATED,DEACTIVATING,false"
        })
        void shouldMatchTransitionRule(UserStatusEnum from, UserStatusEnum to, boolean expected) {
            assertThat(UserStatusTransition.isValidTransition(from, to)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("getAllowedTransitions")
    class GetAllowedTransitions {

        @Test
        @DisplayName("NORMAL 允许 5 个目标状态")
        void normalHasFiveTargets() {
            Set<UserStatusEnum> targets = UserStatusTransition.getAllowedTransitions(UserStatusEnum.NORMAL);
            assertThat(targets)
                .containsExactlyInAnyOrder(
                    UserStatusEnum.MUTED,
                    UserStatusEnum.RESTRICTED_RECOMMEND,
                    UserStatusEnum.FROZEN,
                    UserStatusEnum.BANNED,
                    UserStatusEnum.DEACTIVATING
                );
        }

        @Test
        @DisplayName("DEACTIVATED 终态：不允许任何转换")
        void deactivatedIsFinalState() {
            assertThat(UserStatusTransition.getAllowedTransitions(UserStatusEnum.DEACTIVATED)).isEmpty();
            assertThat(UserStatusTransition.isValidTransition(UserStatusEnum.DEACTIVATED, UserStatusEnum.NORMAL)).isFalse();
        }

        @Test
        @DisplayName("每个枚举值都能查到目标集合（即使为空）")
        void everyEnumValueIsRegistered() {
            for (UserStatusEnum status : UserStatusEnum.values()) {
                Set<UserStatusEnum> targets = UserStatusTransition.getAllowedTransitions(status);
                assertThat(targets)
                    .as("状态 %s 必须有规则定义", status)
                    .isNotNull();
            }
        }
    }
}
