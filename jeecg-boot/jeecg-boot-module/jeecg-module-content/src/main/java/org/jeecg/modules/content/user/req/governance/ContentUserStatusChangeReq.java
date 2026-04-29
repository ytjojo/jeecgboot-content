package org.jeecg.modules.content.user.req.governance;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * Request model for content user status change.
 */
@Data
@Accessors(chain = true)
public class ContentUserStatusChangeReq {

    private String userId;
    private String currentStatus;
    private String targetStatus;
    private String operatorUserId;
    private String reason;
    private String ruleCode;
    private Date effectiveStartTime;
    private Date effectiveEndTime;
}
