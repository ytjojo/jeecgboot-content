package org.jeecg.modules.content.userstatus.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.userstatus.entity.UserStatusEnum;
import org.jeecg.modules.content.userstatus.service.impl.UserStatusServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 用户状态机核心逻辑测试。
 * 测试合法状态转换、非法转换拒绝、管理员强制转换、并发冲突检测。
 */
@ExtendWith(MockitoExtension.class)
class UserStatusServiceTest {

    @InjectMocks
    private UserStatusServiceImpl userStatusService;

    @Test
    void shouldAllowValidTransitionFromNormalToMuted() {
        assertThat(userStatusService.isValidTransition(UserStatusEnum.NORMAL, UserStatusEnum.MUTED)).isTrue();
    }

    @Test
    void shouldAllowValidTransitionFromMutedToNormal() {
        assertThat(userStatusService.isValidTransition(UserStatusEnum.MUTED, UserStatusEnum.NORMAL)).isTrue();
    }

    @Test
    void shouldRejectInvalidTransitionFromGuestToBanned() {
        assertThat(userStatusService.isValidTransition(UserStatusEnum.GUEST, UserStatusEnum.BANNED)).isFalse();
    }

    @Test
    void shouldRejectInvalidTransitionFromDeactivatedToAny() {
        assertThat(userStatusService.isValidTransition(UserStatusEnum.DEACTIVATED, UserStatusEnum.NORMAL)).isFalse();
        assertThat(userStatusService.isValidTransition(UserStatusEnum.DEACTIVATED, UserStatusEnum.GUEST)).isFalse();
    }

    @Test
    void shouldAllowAdminForceTransition() {
        // 管理员强制转换允许从任意状态到任意状态
        assertThat(userStatusService.isValidAdminForceTransition(UserStatusEnum.GUEST, UserStatusEnum.BANNED)).isTrue();
        assertThat(userStatusService.isValidAdminForceTransition(UserStatusEnum.DEACTIVATED, UserStatusEnum.NORMAL)).isTrue();
    }

    @Test
    void shouldValidateStatusChangeRequest() {
        // 合法请求应该通过验证
        userStatusService.validateStatusChange(
            UserStatusEnum.NORMAL,
            UserStatusEnum.MUTED,
            "违规发言",
            false
        );
    }

    @Test
    void shouldRejectInvalidStatusChangeRequest() {
        // 非法请求应该抛出异常
        assertThatThrownBy(() ->
            userStatusService.validateStatusChange(
                UserStatusEnum.GUEST,
                UserStatusEnum.BANNED,
                "测试",
                false
            )
        ).isInstanceOf(JeecgBootException.class)
         .hasMessageContaining("非法的状态转换");
    }

    @Test
    void shouldAllowAdminForceStatusChange() {
        // 管理员强制转换应该通过验证
        userStatusService.validateStatusChange(
            UserStatusEnum.GUEST,
            UserStatusEnum.BANNED,
            "管理员强制封禁",
            true
        );
    }

    @Test
    void shouldDetectConcurrentConflict() {
        // 模拟并发冲突：版本号不匹配
        assertThat(userStatusService.detectConcurrentConflict(1L, 2L)).isTrue();
        assertThat(userStatusService.detectConcurrentConflict(1L, 1L)).isFalse();
    }

    @Test
    void shouldGetAllowedTransitions() {
        assertThat(userStatusService.getAllowedTransitions(UserStatusEnum.NORMAL))
            .containsExactlyInAnyOrder(
                UserStatusEnum.MUTED,
                UserStatusEnum.RESTRICTED_RECOMMEND,
                UserStatusEnum.FROZEN,
                UserStatusEnum.BANNED,
                UserStatusEnum.DEACTIVATING
            );
    }

    @Test
    void shouldReturnEmptyForDeactivatedTransitions() {
        assertThat(userStatusService.getAllowedTransitions(UserStatusEnum.DEACTIVATED)).isEmpty();
    }
}
