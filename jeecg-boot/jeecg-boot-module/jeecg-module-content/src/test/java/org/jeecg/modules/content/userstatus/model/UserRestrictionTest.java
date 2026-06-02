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
 * 用户功能限制规则测试。
 * 覆盖每种状态对各功能的限制。
 */
@DisplayName("UserRestriction 功能限制规则")
class UserRestrictionTest {

    @Nested
    @DisplayName("isRestricted / getRestrictions")
    class IsRestricted {

        @Test
        @DisplayName("GUEST 全功能受限（6 项）")
        void guestRestrictedOnAllInteraction() {
            Set<String> restrictions = UserRestriction.getRestrictions(UserStatusEnum.GUEST);
            assertThat(restrictions)
                .containsExactlyInAnyOrder("publish", "comment", "like", "favorite", "message", "follow");
            assertThat(UserRestriction.isRestricted(UserStatusEnum.GUEST, "publish")).isTrue();
            assertThat(UserRestriction.isRestricted(UserStatusEnum.GUEST, "comment")).isTrue();
        }

        @Test
        @DisplayName("NORMAL 无任何限制")
        void normalHasNoRestrictions() {
            assertThat(UserRestriction.getRestrictions(UserStatusEnum.NORMAL)).isEmpty();
            assertThat(UserRestriction.isRestricted(UserStatusEnum.NORMAL, "publish")).isFalse();
            assertThat(UserRestriction.isRestricted(UserStatusEnum.NORMAL, "login")).isFalse();
        }

        @Test
        @DisplayName("MUTED 仅禁止 publish 和 comment")
        void mutedBlocksPublishAndComment() {
            assertThat(UserRestriction.isRestricted(UserStatusEnum.MUTED, "publish")).isTrue();
            assertThat(UserRestriction.isRestricted(UserStatusEnum.MUTED, "comment")).isTrue();
            assertThat(UserRestriction.isRestricted(UserStatusEnum.MUTED, "like")).isFalse();
            assertThat(UserRestriction.isRestricted(UserStatusEnum.MUTED, "message")).isFalse();
        }

        @Test
        @DisplayName("RESTRICTED_RECOMMEND 仅限制 recommend")
        void restrictedRecommendOnlyBlocksRecommend() {
            assertThat(UserRestriction.isRestricted(UserStatusEnum.RESTRICTED_RECOMMEND, "recommend")).isTrue();
            assertThat(UserRestriction.isRestricted(UserStatusEnum.RESTRICTED_RECOMMEND, "publish")).isFalse();
            assertThat(UserRestriction.isRestricted(UserStatusEnum.RESTRICTED_RECOMMEND, "comment")).isFalse();
        }

        @Test
        @DisplayName("FROZEN 禁止登录 + 全互动")
        void frozenBlocksLoginAndAllInteraction() {
            Set<String> restrictions = UserRestriction.getRestrictions(UserStatusEnum.FROZEN);
            assertThat(restrictions)
                .containsExactlyInAnyOrder("login", "publish", "comment", "like", "favorite", "message", "follow");
            assertThat(UserRestriction.isRestricted(UserStatusEnum.FROZEN, "login")).isTrue();
        }

        @Test
        @DisplayName("BANNED/DEACTIVATING/DEACTIVATED 禁止 login + api + 全部互动")
        void terminalStatesBlockLoginAndApi() {
            for (UserStatusEnum status : new UserStatusEnum[]{
                UserStatusEnum.BANNED, UserStatusEnum.DEACTIVATING, UserStatusEnum.DEACTIVATED}) {
                assertThat(UserRestriction.isRestricted(status, "login")).isTrue();
                assertThat(UserRestriction.isRestricted(status, "api")).isTrue();
                assertThat(UserRestriction.isRestricted(status, "publish")).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("can* 便捷方法")
    class CanMethods {

        @ParameterizedTest(name = "{0} 允许登录={1}")
        @CsvSource({
            "GUEST,true",
            "REGISTERED_INCOMPLETE,true",
            "NORMAL,true",
            "MUTED,true",
            "RESTRICTED_RECOMMEND,true",
            "FROZEN,false",
            "BANNED,false",
            "DEACTIVATING,false",
            "DEACTIVATED,false"
        })
        void shouldMatchCanLogin(UserStatusEnum status, boolean expected) {
            assertThat(UserRestriction.canLogin(status)).isEqualTo(expected);
        }

        @ParameterizedTest(name = "{0} 允许发布={1}")
        @CsvSource({
            "GUEST,false",
            "REGISTERED_INCOMPLETE,false",
            "NORMAL,true",
            "MUTED,false",
            "RESTRICTED_RECOMMEND,true",
            "FROZEN,false",
            "BANNED,false",
            "DEACTIVATING,false",
            "DEACTIVATED,false"
        })
        void shouldMatchCanPublish(UserStatusEnum status, boolean expected) {
            assertThat(UserRestriction.canPublish(status)).isEqualTo(expected);
        }

        @Test
        @DisplayName("canComment: BANNED 禁止评论")
        void bannedCannotComment() {
            assertThat(UserRestriction.canComment(UserStatusEnum.BANNED)).isFalse();
            assertThat(UserRestriction.canComment(UserStatusEnum.NORMAL)).isTrue();
        }

        @Test
        @DisplayName("canSendMessage: BANNED 禁止私信")
        void bannedCannotMessage() {
            assertThat(UserRestriction.canSendMessage(UserStatusEnum.BANNED)).isFalse();
            assertThat(UserRestriction.canSendMessage(UserStatusEnum.NORMAL)).isTrue();
        }
    }
}
