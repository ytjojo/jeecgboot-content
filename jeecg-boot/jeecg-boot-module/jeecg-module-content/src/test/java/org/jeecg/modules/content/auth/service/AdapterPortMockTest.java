package org.jeecg.modules.content.auth.service;

import org.jeecg.modules.content.auth.service.impl.NoopCaptchaVerifyPort;
import org.jeecg.modules.content.auth.service.impl.NoopEmailSenderPort;
import org.jeecg.modules.content.auth.service.impl.NoopIpGeolocationPort;
import org.jeecg.modules.content.auth.service.impl.NoopSmsSenderPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * 适配器端口接口可Mock性测试 + No-op实现默认行为测试。
 */
@ExtendWith(MockitoExtension.class)
class AdapterPortMockTest {

    @Mock
    SmsSenderPort smsSenderPort;

    @Mock
    EmailSenderPort emailSenderPort;

    @Mock
    CaptchaVerifyPort captchaVerifyPort;

    @Mock
    IpGeolocationPort ipGeolocationPort;

    // ==================== Mock 接口能力测试 ====================

    @Test
    void smsSenderPort_canBeMocked() {
        when(smsSenderPort.send("13800138000", "code:123456")).thenReturn(true);
        assertThat(smsSenderPort.send("13800138000", "code:123456")).isTrue();
    }

    @Test
    void smsSenderPort_canReturnFalse() {
        when(smsSenderPort.send(anyString(), anyString())).thenReturn(false);
        assertThat(smsSenderPort.send("13800138000", "code:123456")).isFalse();
    }

    @Test
    void emailSenderPort_canBeMocked() {
        when(emailSenderPort.send("test@example.com", "主题", "<html>内容</html>")).thenReturn(true);
        assertThat(emailSenderPort.send("test@example.com", "主题", "<html>内容</html>")).isTrue();
    }

    @Test
    void emailSenderPort_canReturnFalse() {
        when(emailSenderPort.send(anyString(), anyString(), anyString())).thenReturn(false);
        assertThat(emailSenderPort.send("test@example.com", "主题", "<html>内容</html>")).isFalse();
    }

    @Test
    void captchaVerifyPort_canBeMocked() {
        when(captchaVerifyPort.verify("token-abc", "192.168.1.1")).thenReturn(true);
        assertThat(captchaVerifyPort.verify("token-abc", "192.168.1.1")).isTrue();
    }

    @Test
    void captchaVerifyPort_canReturnFalse() {
        when(captchaVerifyPort.verify(anyString(), anyString())).thenReturn(false);
        assertThat(captchaVerifyPort.verify("bad-token", "192.168.1.1")).isFalse();
    }

    @Test
    void ipGeolocationPort_canBeMocked() {
        when(ipGeolocationPort.resolve("114.114.114.114")).thenReturn("北京市 电信");
        assertThat(ipGeolocationPort.resolve("114.114.114.114")).isEqualTo("北京市 电信");
    }

    @Test
    void ipGeolocationPort_canReturnNull() {
        when(ipGeolocationPort.resolve(anyString())).thenReturn(null);
        assertThat(ipGeolocationPort.resolve("unknown")).isNull();
    }

    // ==================== No-op 实现默认行为测试 ====================

    @Test
    void noopSmsSenderPort_alwaysReturnsTrue() {
        NoopSmsSenderPort port = new NoopSmsSenderPort();
        assertThat(port.send("13800138000", "code:123456")).isTrue();
        assertThat(port.send("", "")).isTrue();
    }

    @Test
    void noopEmailSenderPort_alwaysReturnsTrue() {
        NoopEmailSenderPort port = new NoopEmailSenderPort();
        assertThat(port.send("test@example.com", "主题", "<html>内容</html>")).isTrue();
        assertThat(port.send("", "", "")).isTrue();
    }

    @Test
    void noopCaptchaVerifyPort_alwaysReturnsTrue() {
        NoopCaptchaVerifyPort port = new NoopCaptchaVerifyPort();
        assertThat(port.verify("any-token", "127.0.0.1")).isTrue();
        assertThat(port.verify("", "")).isTrue();
    }

    @Test
    void noopIpGeolocationPort_alwaysReturnsNull() {
        NoopIpGeolocationPort port = new NoopIpGeolocationPort();
        assertThat(port.resolve("114.114.114.114")).isNull();
        assertThat(port.resolve("")).isNull();
        assertThat(port.resolve(null)).isNull();
    }

    // ==================== 接口契约测试 ====================

    @Test
    void smsSenderPort_isFunctionalInterface() {
        // 验证接口只有单个抽象方法
        assertThat(SmsSenderPort.class.getMethods())
                .filteredOn(m -> m.getDeclaringClass() == SmsSenderPort.class)
                .hasSize(1);
    }

    @Test
    void emailSenderPort_isFunctionalInterface() {
        assertThat(EmailSenderPort.class.getMethods())
                .filteredOn(m -> m.getDeclaringClass() == EmailSenderPort.class)
                .hasSize(1);
    }

    @Test
    void captchaVerifyPort_isFunctionalInterface() {
        assertThat(CaptchaVerifyPort.class.getMethods())
                .filteredOn(m -> m.getDeclaringClass() == CaptchaVerifyPort.class)
                .hasSize(1);
    }

    @Test
    void ipGeolocationPort_isFunctionalInterface() {
        assertThat(IpGeolocationPort.class.getMethods())
                .filteredOn(m -> m.getDeclaringClass() == IpGeolocationPort.class)
                .hasSize(1);
    }
}
