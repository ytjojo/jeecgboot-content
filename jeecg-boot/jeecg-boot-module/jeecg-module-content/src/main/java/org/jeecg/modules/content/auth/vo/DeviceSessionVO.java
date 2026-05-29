package org.jeecg.modules.content.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 设备会话信息VO。
 */
@Data
@Accessors(chain = true)
@Schema(description = "设备会话信息")
public class DeviceSessionVO {

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备类型")
    private String deviceType;

    @Schema(description = "操作系统类型")
    private String osType;

    @Schema(description = "浏览器类型")
    private String browserType;

    @Schema(description = "登录IP")
    private String loginIp;

    @Schema(description = "登录地点")
    private String loginLocation;

    @Schema(description = "最后活跃时间")
    private Date lastActiveTime;

    @Schema(description = "是否当前设备")
    private boolean current;

    @Schema(description = "是否信任设备")
    private boolean trusted;
}
