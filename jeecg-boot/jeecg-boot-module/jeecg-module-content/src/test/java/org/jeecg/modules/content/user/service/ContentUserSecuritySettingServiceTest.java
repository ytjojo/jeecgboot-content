package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.service.impl.ContentUserSecuritySettingServiceImpl;
import org.jeecg.modules.content.user.vo.ContentUserSecuritySettingVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 用户账号安全设置服务测试。
 * 覆盖 Task 5.2（VO 默认值）、Task 5.4（Service 返回默认 VO）、Task 5.6（端到端聚合）。
 */
@ExtendWith(MockitoExtension.class)
class ContentUserSecuritySettingServiceTest {

    @InjectMocks
    private ContentUserSecuritySettingServiceImpl securitySettingService;

    // ===== Task 5.2: VO 默认值测试 =====

    @Test
    void voShouldReturnDefaultWhenFieldsAreNull() {
        ContentUserSecuritySettingVO vo = new ContentUserSecuritySettingVO();

        // 所有字段未设置，getter 应返回安全默认值
        assertThat(vo.getDeviceManagementEnabled()).isTrue();
        assertThat(vo.getPasswordChangeEnabled()).isTrue();
        assertThat(vo.getTwoFactorEnabled()).isFalse();
        assertThat(vo.getLoginAlertEnabled()).isTrue();
    }

    @Test
    void voShouldReturnSetValueWhenExplicitlySet() {
        ContentUserSecuritySettingVO vo = new ContentUserSecuritySettingVO()
            .setDeviceManagementEnabled(false)
            .setPasswordChangeEnabled(false)
            .setTwoFactorEnabled(true)
            .setLoginAlertEnabled(false);

        assertThat(vo.getDeviceManagementEnabled()).isFalse();
        assertThat(vo.getPasswordChangeEnabled()).isFalse();
        assertThat(vo.getTwoFactorEnabled()).isTrue();
        assertThat(vo.getLoginAlertEnabled()).isFalse();
    }

    // ===== Task 5.4: Service 返回默认 VO 测试 =====

    @Test
    void serviceShouldReturnVOWithDefaultValues() {
        ContentUserSecuritySettingVO vo = securitySettingService.getSecuritySetting("u_1001");

        assertThat(vo).isNotNull();
        assertThat(vo.getDeviceManagementEnabled()).isTrue();
        assertThat(vo.getPasswordChangeEnabled()).isTrue();
        assertThat(vo.getTwoFactorEnabled()).isFalse();
        assertThat(vo.getLoginAlertEnabled()).isTrue();
    }

    @Test
    void serviceShouldHandleNullUserIdGracefully() {
        // 传入 null 不应抛异常，当前实现不依赖 userId 查询外部服务
        ContentUserSecuritySettingVO vo = securitySettingService.getSecuritySetting(null);

        assertThat(vo).isNotNull();
        assertThat(vo.getDeviceManagementEnabled()).isTrue();
    }

    // ===== Task 5.6: 聚合端到端测试 =====

    @Test
    void aggregatedSecuritySettingShouldHaveAllFields() {
        ContentUserSecuritySettingVO vo = securitySettingService.getSecuritySetting("u_1001");

        // 验证所有安全设置字段都已填充
        assertThat(vo.getDeviceManagementEnabled()).isNotNull();
        assertThat(vo.getPasswordChangeEnabled()).isNotNull();
        assertThat(vo.getTwoFactorEnabled()).isNotNull();
        assertThat(vo.getLoginAlertEnabled()).isNotNull();
    }

    @Test
    void aggregatedSecuritySettingDefaultsShouldBeSecure() {
        ContentUserSecuritySettingVO vo = securitySettingService.getSecuritySetting("u_1001");

        // 安全默认：设备管理和密码修改开启，两步验证关闭，登录提醒开启
        assertThat(vo.getDeviceManagementEnabled()).as("设备管理默认开启").isTrue();
        assertThat(vo.getPasswordChangeEnabled()).as("密码修改默认开启").isTrue();
        assertThat(vo.getLoginAlertEnabled()).as("登录提醒默认开启").isTrue();
        assertThat(vo.getTwoFactorEnabled()).as("两步验证默认关闭").isFalse();
    }
}
