package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_device_session")
public class ContentUserDeviceSession extends JeecgEntity {

    private String userId;
    private String sessionToken;
    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String loginIp;
    private String loginLocation;
    private Date lastActiveTime;
    private Boolean offline;
}
