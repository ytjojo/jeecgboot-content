package org.jeecg.modules.content.user.entity;

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
}
