package org.jeecg.modules.content.auth.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.auth.entity.ContentCancellationRequest;
import org.jeecg.modules.content.auth.entity.ContentUserAccount;
import org.jeecg.modules.content.auth.mapper.ContentCancellationRequestMapper;
import org.jeecg.modules.content.auth.mapper.ContentUserAccountMapper;
import org.jeecg.modules.content.auth.req.ContentCancelApplyReq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 账号注销业务逻辑测试。
 * 覆盖注销申请、冷静期检查、撤销注销、完成注销等核心场景。
 */
@ExtendWith(MockitoExtension.class)
class ContentAccountCancellationBizServiceTest {

    @Mock
    private ContentCancellationRequestMapper cancellationRequestMapper;
    @Mock
    private ContentUserAccountMapper accountMapper;
    @InjectMocks
    private ContentAccountCancellationBizServiceImpl service;

    private static final String TEST_USER_ID = "u_1001";
    private static final String TEST_ACCOUNT_ID = "acc_001";

    // ==================== 注销申请测试 ====================

    @Nested
    @DisplayName("注销申请")
    class ApplyCancellation {

        @Test
        @DisplayName("申请成功 - 创建请求和更新账号状态")
        void applyCancellation_success() {
            // given
            ContentCancelApplyReq req = new ContentCancelApplyReq()
                    .setUserId(TEST_USER_ID)
                    .setReason("不再使用")
                    .setCooldownDays(7);
            when(cancellationRequestMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(createActiveAccount());
            when(cancellationRequestMapper.insert(any(ContentCancellationRequest.class))).thenReturn(1);
            when(accountMapper.updateById(any(ContentUserAccount.class))).thenReturn(1);

            // when
            service.applyCancellation(req);

            // then
            verify(cancellationRequestMapper).insert(any(ContentCancellationRequest.class));
            verify(accountMapper).updateById(any(ContentUserAccount.class));
        }

        @Test
        @DisplayName("未登录/空账号 - userId无效时抛出异常")
        void applyCancellation_invalidUserId_throwsError() {
            // given
            ContentCancelApplyReq req = new ContentCancelApplyReq()
                    .setUserId("invalid_user")
                    .setReason("测试")
                    .setCooldownDays(7);
            when(cancellationRequestMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(accountMapper.selectActiveByUserId("invalid_user")).thenReturn(null);

            // when / then
            assertThatThrownBy(() -> service.applyCancellation(req))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("账号不存在或已注销");
        }

        @Test
        @DisplayName("未完成事项检查 - 账号非ACTIVE状态时拒绝")
        void applyCancellation_accountNotActive_throwsError() {
            // given
            ContentCancelApplyReq req = new ContentCancelApplyReq()
                    .setUserId(TEST_USER_ID)
                    .setReason("测试")
                    .setCooldownDays(7);
            when(cancellationRequestMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(null);

            // when / then
            assertThatThrownBy(() -> service.applyCancellation(req))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("账号不存在或已注销");
        }

        @Test
        @DisplayName("重复申请 - 已有进行中的注销申请时拒绝")
        void applyCancellation_duplicateRequest_throwsError() {
            // given
            ContentCancelApplyReq req = new ContentCancelApplyReq()
                    .setUserId(TEST_USER_ID)
                    .setReason("测试")
                    .setCooldownDays(7);
            ContentCancellationRequest existingRequest = new ContentCancellationRequest()
                    .setUserId(TEST_USER_ID)
                    .setStatus("PENDING");
            when(cancellationRequestMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingRequest);

            // when / then
            assertThatThrownBy(() -> service.applyCancellation(req))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("已有进行中的注销申请");
        }
    }

    // ==================== 冷静期状态检查测试 ====================

    @Nested
    @DisplayName("冷静期状态检查")
    class CheckCooldownStatus {

        @Test
        @DisplayName("冷静期登录提示 - 返回剩余天数")
        void checkCooldownStatus_cooldownActive_returnsRemainingDays() {
            // given
            Date cooldownDeadline = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(5));
            ContentCancellationRequest request = new ContentCancellationRequest()
                    .setUserId(TEST_USER_ID)
                    .setStatus("PENDING")
                    .setCooldownDeadline(cooldownDeadline);
            when(cancellationRequestMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(request);

            // when
            String status = service.checkCooldownStatus(TEST_USER_ID);

            // then
            assertThat(status).startsWith("COOLDOWN_ACTIVE:剩余");
        }

        @Test
        @DisplayName("已过冷静期 - 返回READY_TO_CANCEL")
        void checkCooldownStatus_readyToCancel_returnsReady() {
            // given
            Date cooldownDeadline = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));
            ContentCancellationRequest request = new ContentCancellationRequest()
                    .setUserId(TEST_USER_ID)
                    .setStatus("PENDING")
                    .setCooldownDeadline(cooldownDeadline);
            when(cancellationRequestMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(request);

            // when
            String status = service.checkCooldownStatus(TEST_USER_ID);

            // then
            assertThat(status).isEqualTo("READY_TO_CANCEL");
        }

        @Test
        @DisplayName("无进行中的申请 - 返回null")
        void checkCooldownStatus_noRequest_returnsNull() {
            // given
            when(cancellationRequestMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // when
            String status = service.checkCooldownStatus(TEST_USER_ID);

            // then
            assertThat(status).isNull();
        }
    }

    // ==================== 取消注销测试 ====================

    @Nested
    @DisplayName("取消注销")
    class RevokeCancellation {

        @Test
        @DisplayName("取消注销成功")
        void revokeCancellation_success() {
            // given
            ContentCancellationRequest request = new ContentCancellationRequest()
                    .setUserId(TEST_USER_ID)
                    .setStatus("PENDING");
            when(cancellationRequestMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(request);
            when(cancellationRequestMapper.updateById(any(ContentCancellationRequest.class))).thenReturn(1);
            when(accountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(createActiveAccount());
            when(accountMapper.updateById(any(ContentUserAccount.class))).thenReturn(1);

            // when
            service.revokeCancellation(TEST_USER_ID);

            // then
            verify(cancellationRequestMapper).updateById(any(ContentCancellationRequest.class));
            verify(accountMapper).updateById(any(ContentUserAccount.class));
        }

        @Test
        @DisplayName("重复取消 - 无申请时抛异常")
        void revokeCancellation_noRequest_throwsError() {
            // given
            when(cancellationRequestMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // when / then
            assertThatThrownBy(() -> service.revokeCancellation(TEST_USER_ID))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("无进行中的注销申请");
        }
    }

    // ==================== 最终注销测试 ====================

    @Nested
    @DisplayName("最终注销")
    class CompleteCancellation {

        @Test
        @DisplayName("冷静期结束后 - 注销成功")
        void completeCancellation_afterCooldown_success() {
            // given
            Date cooldownDeadline = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));
            ContentCancellationRequest request = new ContentCancellationRequest()
                    .setUserId(TEST_USER_ID)
                    .setStatus("PENDING")
                    .setCooldownDeadline(cooldownDeadline);
            when(cancellationRequestMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(request);
            when(cancellationRequestMapper.updateById(any(ContentCancellationRequest.class))).thenReturn(1);
            when(accountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(createActiveAccount());
            when(accountMapper.updateById(any(ContentUserAccount.class))).thenReturn(1);

            // when
            service.completeCancellation(TEST_USER_ID);

            // then
            verify(cancellationRequestMapper).updateById(any(ContentCancellationRequest.class));
            verify(accountMapper).updateById(any(ContentUserAccount.class));
        }

        @Test
        @DisplayName("冷静期未结束时 - 拒绝注销")
        void completeCancellation_duringCooldown_throwsError() {
            // given
            Date cooldownDeadline = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(5));
            ContentCancellationRequest request = new ContentCancellationRequest()
                    .setUserId(TEST_USER_ID)
                    .setStatus("PENDING")
                    .setCooldownDeadline(cooldownDeadline);
            when(cancellationRequestMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(request);

            // when / then
            assertThatThrownBy(() -> service.completeCancellation(TEST_USER_ID))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("冷静期未结束");
        }

        @Test
        @DisplayName("无进行中的申请时 - 拒绝注销")
        void completeCancellation_noRequest_throwsError() {
            // given
            when(cancellationRequestMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // when / then
            assertThatThrownBy(() -> service.completeCancellation(TEST_USER_ID))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("无进行中的注销申请");
        }
    }

    // ==================== 冷静期配置校验测试 ====================

    @Nested
    @DisplayName("冷静期配置校验")
    class ValidateCooldownDays {

        @Test
        @DisplayName("null值 - 抛出不能为空异常")
        void validateCooldownDays_null_throwsError() {
            assertThatThrownBy(() -> service.validateCooldownDays(null))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("冷静期天数不能为空");
        }

        @Test
        @DisplayName("小于7天 - 抛出最少7天异常")
        void validateCooldownDays_lessThan7_throwsError() {
            assertThatThrownBy(() -> service.validateCooldownDays(6))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("冷静期最少7天");
        }

        @Test
        @DisplayName("大于30天 - 抛出最多30天异常")
        void validateCooldownDays_greaterThan30_throwsError() {
            assertThatThrownBy(() -> service.validateCooldownDays(31))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("冷静期最多30天");
        }

        @Test
        @DisplayName("7天边界 - 校验通过")
        void validateCooldownDays_exactly7_noError() {
            service.validateCooldownDays(7);
        }

        @Test
        @DisplayName("30天边界 - 校验通过")
        void validateCooldownDays_exactly30_noError() {
            service.validateCooldownDays(30);
        }
    }

    // ==================== 辅助方法 ====================

    private ContentUserAccount createActiveAccount() {
        ContentUserAccount account = new ContentUserAccount();
        account.setId(TEST_ACCOUNT_ID);
        account.setUserId(TEST_USER_ID);
        account.setNickname("testUser");
        account.setAccountStatus("ACTIVE");
        account.setCancellationStatus("NONE");
        return account;
    }
}
