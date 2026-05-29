package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

/**
 * Entity for content user device session.
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_device_session")
public class ContentUserDeviceSession extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "会话令牌")
    private String sessionToken;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备类型")
    private String deviceType;

    @Schema(description = "登录IP")
    private String loginIp;

    @Schema(description = "登录地点")
    private String loginLocation;

    @Schema(description = "最近活跃时间")
    private Date lastActiveTime;

    @Schema(description = "是否已下线")
    private Boolean offline;

    @Schema(description = "Token JTI")
    @TableField("token_jti")
    private String tokenJti;

    @Schema(description = "操作系统类型")
    @TableField("os_type")
    private String osType;

    @Schema(description = "操作系统版本")
    @TableField("os_version")
    private String osVersion;

    @Schema(description = "浏览器类型")
    @TableField("browser_type")
    private String browserType;

    @Schema(description = "设备指纹")
    @TableField("device_fingerprint")
    private String deviceFingerprint;

    @Schema(description = "是否受信任设备")
    @TableField("trusted")
    private Boolean trusted;

    @Schema(description = "会话状态")
    @TableField("session_status")
    private String sessionStatus;

    @Schema(description = "下线时间")
    @TableField("offline_time")
    private Date offlineTime;

    @Schema(description = "下线原因")
    @TableField("offline_reason")
    private String offlineReason;
}
