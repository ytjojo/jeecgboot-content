package org.jeecg.modules.content.userstatus.scheduler;

import org.jeecg.modules.content.userstatus.biz.UserStatusBizManageService;
import org.jeecg.modules.content.userstatus.entity.UserStatusEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 自动解禁定时任务测试。
 * 测试到期解禁、永久封禁跳过、异常告警。
 */
@ExtendWith(MockitoExtension.class)
class UserStatusAutoReleaseSchedulerTest {

    @Mock
    private UserStatusBizManageService bizManageService;

    @InjectMocks
    private UserStatusAutoReleaseScheduler scheduler;

    @Test
    void shouldAutoReleaseExpiredMutedUsers() {
        // Given
        List<String> expiredUserIds = Arrays.asList("user001", "user002");
        when(bizManageService.findExpiredStatusUsers(UserStatusEnum.MUTED)).thenReturn(expiredUserIds);

        // When
        scheduler.autoReleaseExpiredStatus();

        // Then
        verify(bizManageService).batchChangeStatus(
            expiredUserIds,
            UserStatusEnum.MUTED,
            UserStatusEnum.NORMAL,
            "自动解禁：禁言期限到期",
            "SYSTEM",
            "SYSTEM"
        );
    }

    @Test
    void shouldSkipPermanentBannedUsers() {
        // Given
        List<String> expiredUserIds = Arrays.asList(); // 永久封禁没有到期时间
        when(bizManageService.findExpiredStatusUsers(UserStatusEnum.BANNED)).thenReturn(expiredUserIds);

        // When
        scheduler.autoReleaseExpiredStatus();

        // Then
        verify(bizManageService, never()).batchChangeStatus(
            any(),
            eq(UserStatusEnum.BANNED),
            eq(UserStatusEnum.NORMAL),
            any(),
            any(),
            any()
        );
    }

    @Test
    void shouldHandleExceptionGracefully() {
        // Given
        when(bizManageService.findExpiredStatusUsers(UserStatusEnum.MUTED))
            .thenThrow(new RuntimeException("数据库异常"));

        // When - 应该不抛出异常
        scheduler.autoReleaseExpiredStatus();

        // Then - 验证异常被捕获
        verify(bizManageService, never()).batchChangeStatus(
            any(),
            any(),
            any(),
            any(),
            any(),
            any()
        );
    }

    @Test
    void shouldLogWarningWhenNoExpiredUsers() {
        // Given
        when(bizManageService.findExpiredStatusUsers(UserStatusEnum.MUTED)).thenReturn(Arrays.asList());

        // When
        scheduler.autoReleaseExpiredStatus();

        // Then - 不应该调用批量变更
        verify(bizManageService, never()).batchChangeStatus(
            any(),
            any(),
            any(),
            any(),
            any(),
            any()
        );
    }
}
