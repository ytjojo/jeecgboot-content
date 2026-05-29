package org.jeecg.modules.content.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 设备信息DTO。
 */
@Data
@Accessors(chain = true)
@Schema(description = "设备信息")
public class DeviceInfo {

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备类型")
    private String deviceType;

    @Schema(description = "操作系统类型")
    private String osType;

    @Schema(description = "操作系统版本")
    private String osVersion;

    @Schema(description = "浏览器类型")
    private String browserType;

    @Schema(description = "设备指纹")
    private String deviceFingerprint;

    @Schema(description = "登录IP")
    private String loginIp;

    @Schema(description = "登录地点")
    private String loginLocation;
}
