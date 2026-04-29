package org.jeecg.modules.content.user.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * View object for content user status.
 */
@Data
@Accessors(chain = true)
public class ContentUserStatusVO {

    private String userId;
    private String currentStatus;
    private String targetStatus;
    private String reason;
    private Date effectiveStartTime;
    private Date effectiveEndTime;
}
