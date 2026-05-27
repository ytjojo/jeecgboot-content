package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 内容社区用户账号安全设置视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户账号安全设置")
public class ContentUserSecuritySettingVO {

    @Schema(description = "设备管理是否开启", defaultValue = "true")
    private Boolean deviceManagementEnabled;

    @Schema(description = "密码修改是否开启", defaultValue = "true")
    private Boolean passwordChangeEnabled;

    @Schema(description = "两步验证是否开启", defaultValue = "false")
    private Boolean twoFactorEnabled;

    @Schema(description = "登录提醒是否开启", defaultValue = "true")
    private Boolean loginAlertEnabled;

    /**
     * 获取设备管理开关，null 时返回 true。
     */
    public Boolean getDeviceManagementEnabled() {
        return deviceManagementEnabled == null ? Boolean.TRUE : deviceManagementEnabled;
    }

    /**
     * 获取密码修改开关，null 时返回 true。
     */
    public Boolean getPasswordChangeEnabled() {
        return passwordChangeEnabled == null ? Boolean.TRUE : passwordChangeEnabled;
    }

    /**
     * 获取两步验证开关，null 时返回 false。
     */
    public Boolean getTwoFactorEnabled() {
        return twoFactorEnabled == null ? Boolean.FALSE : twoFactorEnabled;
    }

    /**
     * 获取登录提醒开关，null 时返回 true。
     */
    public Boolean getLoginAlertEnabled() {
        return loginAlertEnabled == null ? Boolean.TRUE : loginAlertEnabled;
    }
}
