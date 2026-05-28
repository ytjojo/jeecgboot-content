package org.jeecg.modules.content.userstatus.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.userstatus.annotation.CheckUserStatus;
import org.jeecg.modules.content.userstatus.entity.UserStatusEnum;
import org.jeecg.modules.content.userstatus.model.UserRestriction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * 功能限制 AOP 切面测试。
 * 测试各状态拦截、注解参数解析、错误响应格式。
 */
@ExtendWith(MockitoExtension.class)
class UserStatusCheckAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private UserStatusCheckAspect userStatusCheckAspect;

    @Test
    void shouldAllowNormalUserToAccess() throws Throwable {
        // Given
        UserStatusEnum userStatus = UserStatusEnum.NORMAL;
        CheckUserStatus annotation = createAnnotation(new UserStatusEnum[]{UserStatusEnum.NORMAL, UserStatusEnum.MUTED});
        when(joinPoint.proceed()).thenReturn("success");

        // When
        Object result = userStatusCheckAspect.checkUserStatus(joinPoint, annotation, userStatus);

        // Then
        assertThat(result).isEqualTo("success");
    }

    @Test
    void shouldRejectBannedUserFromAccess() {
        // Given
        UserStatusEnum userStatus = UserStatusEnum.BANNED;
        CheckUserStatus annotation = createAnnotation(new UserStatusEnum[]{UserStatusEnum.NORMAL, UserStatusEnum.MUTED});

        // When & Then
        assertThatThrownBy(() ->
            userStatusCheckAspect.checkUserStatus(joinPoint, annotation, userStatus)
        ).isInstanceOf(JeecgBootException.class)
         .hasMessageContaining("当前状态不允许访问");
    }

    @Test
    void shouldRejectMutedUserFromPublishing() {
        // Given
        UserStatusEnum userStatus = UserStatusEnum.MUTED;
        CheckUserStatus annotation = createAnnotationWithForbid(new String[]{"publish"});

        // When & Then
        assertThatThrownBy(() ->
            userStatusCheckAspect.checkUserStatus(joinPoint, annotation, userStatus)
        ).isInstanceOf(JeecgBootException.class)
         .hasMessageContaining("已被禁言");
    }

    @Test
    void shouldAllowMutedUserToBrowse() throws Throwable {
        // Given
        UserStatusEnum userStatus = UserStatusEnum.MUTED;
        CheckUserStatus annotation = createAnnotationWithForbid(new String[]{"publish", "comment"});
        when(joinPoint.proceed()).thenReturn("success");

        // When
        Object result = userStatusCheckAspect.checkUserStatus(joinPoint, annotation, userStatus);

        // Then
        assertThat(result).isEqualTo("success");
    }

    @Test
    void shouldRejectFrozenUserFromLogin() {
        // Given
        UserStatusEnum userStatus = UserStatusEnum.FROZEN;
        CheckUserStatus annotation = createAnnotationWithForbid(new String[]{"login"});

        // When & Then
        assertThatThrownBy(() ->
            userStatusCheckAspect.checkUserStatus(joinPoint, annotation, userStatus)
        ).isInstanceOf(JeecgBootException.class)
         .hasMessageContaining("已被冻结");
    }

    private CheckUserStatus createAnnotation(UserStatusEnum[] allow) {
        return new CheckUserStatus() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return CheckUserStatus.class;
            }

            @Override
            public UserStatusEnum[] allow() {
                return allow;
            }

            @Override
            public String[] forbid() {
                return new String[]{};
            }
        };
    }

    private CheckUserStatus createAnnotationWithForbid(String[] forbid) {
        return new CheckUserStatus() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return CheckUserStatus.class;
            }

            @Override
            public UserStatusEnum[] allow() {
                return new UserStatusEnum[]{};
            }

            @Override
            public String[] forbid() {
                return forbid;
            }
        };
    }
}
