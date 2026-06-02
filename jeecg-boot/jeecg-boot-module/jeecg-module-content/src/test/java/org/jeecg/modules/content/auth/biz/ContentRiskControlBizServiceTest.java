package org.jeecg.modules.content.auth.biz;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.auth.entity.ContentRiskEvent;
import org.jeecg.modules.content.auth.entity.ContentUserAccount;
import org.jeecg.modules.content.auth.mapper.ContentRiskEventMapper;
import org.jeecg.modules.content.auth.mapper.ContentUserAccountMapper;
import org.jeecg.modules.content.user.entity.ContentUserDeviceSession;
import org.jeecg.modules.content.user.mapper.ContentUserDeviceSessionMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 风控与异常登录业务服务测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentRiskControlBizServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private ContentUserAccountMapper accountMapper;
    @Mock
    private ContentRiskEventMapper riskEventMapper;
    @Mock
    private ContentUserDeviceSessionMapper deviceSessionMapper;

    @InjectMocks
    private ContentRiskControlBizServiceImpl riskControlService;

    private static final String TEST_USER_ID = "u_1001";
    private static final String TEST_ACCOUNT_ID = "acc_001";
    private static final String TEST_IP = "192.168.1.100";
    private static final String TEST_DEVICE_FP = "fp_abc123";

    @BeforeAll
    static void initMybatisPlus() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, ContentUserDeviceSession.class);
        TableInfoHelper.initTableInfo(assistant, ContentRiskEvent.class);
        TableInfoHelper.initTableInfo(assistant, ContentUserAccount.class);
    }

    // ==================== 登录失败记录 ====================

    @Nested
    @DisplayName("recordLoginFail - 登录失败记录")
    class RecordLoginFail {

        @Test
        @DisplayName("10次失败 - 返回需要验证码挑战")
        void failCount10_returnsTrue() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.increment(anyString())).thenReturn(10L);

            boolean needCaptcha = riskControlService.recordLoginFail(TEST_USER_ID, TEST_IP);

            assertThat(needCaptcha).isTrue();
        }

        @Test
        @DisplayName("9次失败 - 返回不需要验证码挑战")
        void failCount9_returnsFalse() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.increment(anyString())).thenReturn(9L);

            boolean needCaptcha = riskControlService.recordLoginFail(TEST_USER_ID, TEST_IP);

            assertThat(needCaptcha).isFalse();
        }

        @Test
        @DisplayName("20次失败 - 锁定账号30分钟")
        void failCount20_locksAccount() {
            ContentUserAccount account = new ContentUserAccount();
            account.setId(TEST_ACCOUNT_ID);
            account.setUserId(TEST_USER_ID);
            account.setAccountStatus("ACTIVE");

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.increment(anyString())).thenReturn(20L);
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(account);

            boolean needCaptcha = riskControlService.recordLoginFail(TEST_USER_ID, TEST_IP);

            assertThat(needCaptcha).isTrue();
            ArgumentCaptor<ContentUserAccount> accountCaptor = ArgumentCaptor.forClass(ContentUserAccount.class);
            verify(accountMapper).updateById(accountCaptor.capture());
            assertThat(accountCaptor.getValue().getLockedUntil()).isNotNull();
            assertThat(accountCaptor.getValue().getLockedUntil()).isAfter(new Date(System.currentTimeMillis() + 25 * 60 * 1000L));
        }

        @Test
        @DisplayName("首次失败 - 设置TTL")
        void firstFail_setsTTL() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.increment(anyString())).thenReturn(1L);

            riskControlService.recordLoginFail(TEST_USER_ID, TEST_IP);

            verify(redisTemplate).expire(anyString(), eq(1800L), any());
        }
    }

    // ==================== 账号锁定检查 ====================

    @Nested
    @DisplayName("isAccountLocked - 账号锁定检查")
    class IsAccountLocked {

        @Test
        @DisplayName("锁定期内 - 返回true")
        void lockedUntilInFuture_returnsTrue() {
            ContentUserAccount account = new ContentUserAccount();
            account.setLockedUntil(new Date(System.currentTimeMillis() + 600_000));
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(account);

            assertThat(riskControlService.isAccountLocked(TEST_USER_ID)).isTrue();
        }

        @Test
        @DisplayName("锁定已过期 - 返回false")
        void lockedUntilInPast_returnsFalse() {
            ContentUserAccount account = new ContentUserAccount();
            account.setLockedUntil(new Date(System.currentTimeMillis() - 600_000));
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(account);

            assertThat(riskControlService.isAccountLocked(TEST_USER_ID)).isFalse();
        }

        @Test
        @DisplayName("未锁定 - 返回false")
        void noLockedUntil_returnsFalse() {
            ContentUserAccount account = new ContentUserAccount();
            account.setLockedUntil(null);
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(account);

            assertThat(riskControlService.isAccountLocked(TEST_USER_ID)).isFalse();
        }

        @Test
        @DisplayName("账号不存在 - 返回false")
        void accountNotFound_returnsFalse() {
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(null);

            assertThat(riskControlService.isAccountLocked(TEST_USER_ID)).isFalse();
        }
    }

    // ==================== IP注册限流 ====================

    @Nested
    @DisplayName("IP注册限流")
    class IpRegisterRateLimit {

        @Test
        @DisplayName("1小时内10次 - 被限流")
        void tenInOneHour_returnsTrue() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(anyString())).thenReturn("10");

            assertThat(riskControlService.isIpRegisterRateLimited(TEST_IP)).isTrue();
        }

        @Test
        @DisplayName("9次 - 未被限流")
        void nine_returnsFalse() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(anyString())).thenReturn("9");

            assertThat(riskControlService.isIpRegisterRateLimited(TEST_IP)).isFalse();
        }

        @Test
        @DisplayName("无记录 - 未被限流")
        void noRecord_returnsFalse() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(anyString())).thenReturn(null);

            assertThat(riskControlService.isIpRegisterRateLimited(TEST_IP)).isFalse();
        }

        @Test
        @DisplayName("recordIpRegister - 首次记录设置TTL")
        void recordIpRegister_firstTime_setsTTL() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.increment(anyString())).thenReturn(1L);

            riskControlService.recordIpRegister(TEST_IP);

            verify(redisTemplate).expire(anyString(), eq(3600L), any());
        }
    }

    // ==================== 风险事件记录 ====================

    @Nested
    @DisplayName("recordRiskEvent - 风险事件记录")
    class RecordRiskEvent {

        @Test
        @DisplayName("记录风险事件成功")
        void record_success() {
            riskControlService.recordRiskEvent(TEST_USER_ID, "LOGIN_FAIL", "HIGH",
                    "多次登录失败", TEST_IP, TEST_DEVICE_FP, "Mozilla/5.0");

            ArgumentCaptor<ContentRiskEvent> eventCaptor = ArgumentCaptor.forClass(ContentRiskEvent.class);
            verify(riskEventMapper).insert(eventCaptor.capture());
            ContentRiskEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(capturedEvent.getEventType()).isEqualTo("LOGIN_FAIL");
            assertThat(capturedEvent.getRiskLevel()).isEqualTo("HIGH");
            assertThat(capturedEvent.getResolved()).isFalse();
        }
    }

    // ==================== 申诉解除 ====================

    @Nested
    @DisplayName("appealRiskEvent - 申诉解除")
    class AppealRiskEvent {

        @Test
        @DisplayName("申诉成功")
        void appeal_success() {
            ContentRiskEvent event = new ContentRiskEvent();
            event.setId("evt_001");
            event.setUserId(TEST_USER_ID);
            event.setResolved(false);
            when(riskEventMapper.selectById("evt_001")).thenReturn(event);

            riskControlService.appealRiskEvent("evt_001", TEST_USER_ID, "误操作");

            ArgumentCaptor<ContentRiskEvent> eventCaptor = ArgumentCaptor.forClass(ContentRiskEvent.class);
            verify(riskEventMapper).updateById(eventCaptor.capture());
            ContentRiskEvent updated = eventCaptor.getValue();
            assertThat(updated.getResolved()).isTrue();
            assertThat(updated.getResolvedBy()).isEqualTo(TEST_USER_ID);
            assertThat(updated.getResolveNote()).isEqualTo("误操作");
        }

        @Test
        @DisplayName("事件不存在 - 抛出异常")
        void appeal_eventNotFound_throws() {
            when(riskEventMapper.selectById("evt_not_exist")).thenReturn(null);

            assertThatThrownBy(() -> riskControlService.appealRiskEvent("evt_not_exist", TEST_USER_ID, "note"))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("风险事件不存在");
        }

        @Test
        @DisplayName("已处理事件 - 抛出异常")
        void appeal_alreadyResolved_throws() {
            ContentRiskEvent event = new ContentRiskEvent();
            event.setId("evt_002");
            event.setResolved(true);
            when(riskEventMapper.selectById("evt_002")).thenReturn(event);

            assertThatThrownBy(() -> riskControlService.appealRiskEvent("evt_002", TEST_USER_ID, "note"))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("该风险事件已处理");
        }
    }

    // ==================== 新设备检测 ====================

    @Nested
    @DisplayName("isNewDevice - 新设备检测")
    class IsNewDevice {

        @Test
        @DisplayName("无历史记录 - 新设备")
        void noHistory_returnsTrue() {
            when(deviceSessionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            assertThat(riskControlService.isNewDevice(TEST_USER_ID, TEST_DEVICE_FP)).isTrue();
        }

        @Test
        @DisplayName("有历史记录 - 非新设备")
        void hasHistory_returnsFalse() {
            when(deviceSessionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            assertThat(riskControlService.isNewDevice(TEST_USER_ID, TEST_DEVICE_FP)).isFalse();
        }

        @Test
        @DisplayName("设备指纹为空 - 视为新设备")
        void emptyFingerprint_returnsTrue() {
            assertThat(riskControlService.isNewDevice(TEST_USER_ID, null)).isTrue();
            assertThat(riskControlService.isNewDevice(TEST_USER_ID, "")).isTrue();
            assertThat(riskControlService.isNewDevice(TEST_USER_ID, "  ")).isTrue();
        }
    }

    // ==================== 空IP处理 ====================

    @Nested
    @DisplayName("边界条件")
    class EdgeCases {

        @Test
        @DisplayName("空IP地址 - recordLoginFail不抛异常")
        void recordLoginFail_nullIp_doesNotThrow() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.increment(anyString())).thenReturn(1L);

            riskControlService.recordLoginFail(TEST_USER_ID, null);
            // 不抛异常即通过
        }

        @Test
        @DisplayName("异地登录检测 - 首次登录不报异地")
        void isAbnormalLocation_firstLogin_returnsFalse() {
            ContentUserAccount account = new ContentUserAccount();
            account.setLastLoginLocation(null);
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(account);

            assertThat(riskControlService.isAbnormalLocation(TEST_USER_ID, "Beijing")).isFalse();
        }

        @Test
        @DisplayName("异地登录检测 - 同地不报异地")
        void isAbnormalLocation_sameLocation_returnsFalse() {
            ContentUserAccount account = new ContentUserAccount();
            account.setLastLoginLocation("Beijing");
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(account);

            assertThat(riskControlService.isAbnormalLocation(TEST_USER_ID, "Beijing")).isFalse();
        }

        @Test
        @DisplayName("异地登录检测 - 不同地点报异地")
        void isAbnormalLocation_differentLocation_returnsTrue() {
            ContentUserAccount account = new ContentUserAccount();
            account.setLastLoginLocation("Beijing");
            when(accountMapper.selectActiveByUserId(TEST_USER_ID)).thenReturn(account);

            assertThat(riskControlService.isAbnormalLocation(TEST_USER_ID, "Shanghai")).isTrue();
        }
    }

    // ==================== 确认异常登录 ====================

    @Nested
    @DisplayName("confirmAbnormalLogin - 确认异常登录")
    class ConfirmAbnormalLogin {

        @Test
        @DisplayName("非本人确认 - 下线其他设备")
        void notSelf_offlinesOtherDevices() {
            ContentRiskEvent event = new ContentRiskEvent();
            event.setId("evt_003");
            event.setUserId(TEST_USER_ID);
            event.setDeviceFingerprint(TEST_DEVICE_FP);
            event.setResolved(false);
            when(riskEventMapper.selectById("evt_003")).thenReturn(event);

            riskControlService.confirmAbnormalLogin(TEST_USER_ID, "evt_003", false);

            ArgumentCaptor<ContentRiskEvent> eventCaptor = ArgumentCaptor.forClass(ContentRiskEvent.class);
            verify(riskEventMapper).updateById(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getResolved()).isTrue();
            assertThat(eventCaptor.getValue().getResolveNote()).isEqualTo("非本人确认，已下线其他设备");
            // 批量下线非当前设备的会话（通过 LambdaUpdateWrapper）
            verify(deviceSessionMapper, times(1)).update(isNull(), any(LambdaUpdateWrapper.class));
        }

        @Test
        @DisplayName("本人确认 - 不下线设备")
        void self_doesNotOffline() {
            ContentRiskEvent event = new ContentRiskEvent();
            event.setId("evt_004");
            event.setUserId(TEST_USER_ID);
            event.setDeviceFingerprint(TEST_DEVICE_FP);
            event.setResolved(false);
            when(riskEventMapper.selectById("evt_004")).thenReturn(event);

            riskControlService.confirmAbnormalLogin(TEST_USER_ID, "evt_004", true);

            ArgumentCaptor<ContentRiskEvent> eventCaptor = ArgumentCaptor.forClass(ContentRiskEvent.class);
            verify(riskEventMapper).updateById(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getResolved()).isTrue();
            assertThat(eventCaptor.getValue().getResolveNote()).isEqualTo("本人确认");
            verify(deviceSessionMapper, never()).updateById(any(ContentUserDeviceSession.class));
        }

        @Test
        @DisplayName("事件不存在 - 抛出异常")
        void eventNotFound_throws() {
            when(riskEventMapper.selectById("evt_not_exist")).thenReturn(null);

            assertThatThrownBy(() -> riskControlService.confirmAbnormalLogin(TEST_USER_ID, "evt_not_exist", true))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("风险事件不存在");
        }

        @Test
        @DisplayName("无权操作 - 抛出异常")
        void unauthorized_throws() {
            ContentRiskEvent event = new ContentRiskEvent();
            event.setId("evt_005");
            event.setUserId("other_user");
            when(riskEventMapper.selectById("evt_005")).thenReturn(event);

            assertThatThrownBy(() -> riskControlService.confirmAbnormalLogin(TEST_USER_ID, "evt_005", true))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("无权操作该风险事件");
        }
    }
}
